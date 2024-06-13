package com.tencentcloud.dbauth.internal;

import com.tencentcloud.dbauth.model.AuthTokenInfoOuterClass;
import com.tencentcloud.dbauth.model.GenerateAuthenticationTokenRequest;
import com.tencentcloudapi.cam.v20190116.CamClient;
import com.tencentcloudapi.cam.v20190116.CamErrorCode;
import com.tencentcloudapi.cam.v20190116.models.AuthToken;
import com.tencentcloudapi.cam.v20190116.models.BuildDataFlowAuthTokenRequest;
import com.tencentcloudapi.cam.v20190116.models.BuildDataFlowAuthTokenResponse;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.common.profile.HttpProfile;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

/**
 * Signer is a utility class that provides methods for generating and updating authentication tokens.
 */
public final class Signer {
    private static final Logger log = LoggerFactory.getLogger(Signer.class);
    // The token cache to store the authentication token
    private static final TokenCache TOKEN_CACHE = new TokenCache();

    // The timer manager to schedule the token update
    private static final TimerManager TIMER_MANAGER = new TimerManager();

    // The interval to update the token in milliseconds
    private static final long TOKEN_UPDATE_INTERVAL = 5 * 1000;
    // The request to generate the authentication token
    private final GenerateAuthenticationTokenRequest request;
    // The authentication key
    private final String authKey;

    /**
     * Constructs a new Signer with the provided request.
     *
     * @param request the request containing the necessary information to generate an authentication token
     */
    public Signer(GenerateAuthenticationTokenRequest request) {
        this.request = request;

        // Generate the authentication key
        String key = request.region()
                + Constants.DELIMITER
                + request.instanceId()
                + Constants.DELIMITER
                + request.userName()
                + Constants.DELIMITER
                + request.credential().getSecretId();

        this.authKey = Base64.getEncoder().encodeToString(key.getBytes());
    }

    /**
     * Returns the delay for the next token update.
     *
     * @param remainingTimeBeforeExpiry the remaining time before the token expires
     * @return the delay for the next token update
     */
    private static long getDelayForNextTokenUpdate(long remainingTimeBeforeExpiry) {
        return Math.min(remainingTimeBeforeExpiry, TOKEN_UPDATE_INTERVAL);
    }

    /**
     * Returns the authentication token from the cache.
     *
     * @return the authentication token from the cache
     */
    public Token getAuthTokenFromCache() {
        return TOKEN_CACHE.getAuthToken(authKey);
    }

    /**
     * Builds the authentication token.
     *
     * @throws TencentCloudSDKException if there is an error during the token generation
     */
    public void buildAuthToken() throws TencentCloudSDKException {
        log.debug("Building authentication token for key");

        try {
            // 1. Request the authentication token
            Token token = getAuthToken();
            setTokenAndUpdateTask(token);
        } catch (TencentCloudSDKException e) {
            // 2. If the error code requires user notification, throw the exception
            if (ErrorCodeMatcher.isUserNotificationRequired(e.getErrorCode())) {
                throw e;
            }

            // 3. If the token generation fails, use the fallback token
            Token fallbackToken = TOKEN_CACHE.fallback(authKey);
            if (fallbackToken != null) {
                log.info("Using the fallback token");
                setTokenAndUpdateTask(fallbackToken);
            } else {
                // 4. If there is no fallback token, throw the exception
                throw e;
            }
        }
    }

    /**
     * Sets the authentication token and updates the token update task.
     *
     * @param token the authentication token
     */
    private void setTokenAndUpdateTask(Token token) {
        TOKEN_CACHE.setAuthToken(authKey, token);
        updateAuthTokenTask(token.getExpires());
    }

    /**
     * Returns the authentication token.
     *
     * @return the authentication token
     * @throws TencentCloudSDKException if there is an error during the token generation
     */
    public Token getAuthToken() throws TencentCloudSDKException {
        BuildDataFlowAuthTokenResponse response = requestAuthToken();
        if (response == null) {
            log.error("Failed to request AuthToken, response is null");
            throw new TencentCloudSDKException(
                    "Failed to request AuthToken, response is null", "", CamErrorCode.INTERNALERROR.getValue());
        }

        String requestId = response.getRequestId();
        if (response.getCredentials() == null) {
            log.error("Failed to request AuthToken, tokenResponse is null, requestId: {}", requestId);
            throw new TencentCloudSDKException(
                    "Failed to request AuthToken, tokenResponse is null",
                    requestId,
                    CamErrorCode.INTERNALERROR.getValue());
        }
        AuthToken tokenResponse = response.getCredentials();

        // Decrypt the authToken
        String encAuthToken = tokenResponse.getToken();
        String authToken = null;
        try {
            authToken = decryptAuthToken(encAuthToken);
        } catch (Exception e) {
            String errorMsg = "Failed to decrypt AuthToken, requestId: " + requestId + ", error: " + e.getMessage();
            log.error(errorMsg);
            throw new TencentCloudSDKException(errorMsg, requestId, CamErrorCode.INTERNALERROR.getValue());
        }

        if (StringUtils.isEmpty(authToken)) {
            log.error("Failed to decrypt AuthToken, authToken is empty, requestId: {}", requestId);
            throw new TencentCloudSDKException(
                    "Failed to decrypt AuthToken, authToken is empty",
                    requestId,
                    CamErrorCode.INTERNALERROR.getValue());
        }

        long camServerTime = tokenResponse.getCurrentTime();
        long authTokenExpires = tokenResponse.getNextRotationTime();

        // Calculate the expiry time of the authToken
        long expiry = expiry(camServerTime, authTokenExpires);
        return new Token(authToken, expiry);
    }

    /**
     * Decrypt the authentication token.
     *
     * @param encAuthToken the encrypted authentication token
     * @return the decrypted authentication token
     * @throws TencentCloudSDKException if there is an error during the decryption
     */
    private String decryptAuthToken(String encAuthToken) throws Exception {
        AuthTokenInfoOuterClass.AuthTokenInfo tokenInfo = AuthTokenParser.parseAuthToken(
                request.instanceId(), request.region(), request.userName(), encAuthToken);
        return tokenInfo.getPassword();
    }

    /**
     * Calculate the expiry time of the authentication token.
     *
     * @param camServerTime    the current time of the CAM server
     * @param authTokenExpires the expiration time of the authentication token
     * @return the expiry time of the authentication token
     */
    private long expiry(long camServerTime, long authTokenExpires) {
        if (authTokenExpires < camServerTime) {
            return System.currentTimeMillis() + TOKEN_UPDATE_INTERVAL;
        }
        return System.currentTimeMillis() + (authTokenExpires - camServerTime);
    }

    /**
     * Requests an authentication token from the server.
     *
     * @return an optional containing the response with the authentication token and its expiry time
     */
    private BuildDataFlowAuthTokenResponse requestAuthToken() throws TencentCloudSDKException {
        BuildDataFlowAuthTokenRequest req = new BuildDataFlowAuthTokenRequest();
        req.setResourceId(request.instanceId());
        req.setResourceRegion(request.region());
        req.setResourceAccount(request.userName());

        CamClient client = new CamClient(request.credential(), request.region());
        HttpProfile httpProfile = client.getClientProfile().getHttpProfile();
        httpProfile.setWriteTimeout(30); // default 0
        httpProfile.setReadTimeout(30);  // default 0

        TencentCloudSDKException lastException = null;
        for (int i = 0; i < 3; i++) {
            try {
                return client.BuildDataFlowAuthToken(req);
            } catch (TencentCloudSDKException e) {
                lastException = e;
                if (ErrorCodeMatcher.isUserNotificationRequired(e.getErrorCode())) {
                    log.error("Failed to request AuthToken, error: {}", e.toString());
                    break;
                } else {
                    log.error("Failed to request AuthToken, Retry to request the token, error: {}", e.toString());
                }
            } catch (Exception e) {
                log.error("Failed to request AuthToken , error: {}", e.getMessage());
                lastException = new TencentCloudSDKException(
                        "Failed to request AuthToken, error: " + e.getMessage(),
                        "",
                        CamErrorCode.INTERNALERROR.getValue());
            }
        }

        throw lastException;
    }

    /**
     * Updates the authentication token task.
     *
     * @param authTokenExpiry the expiry time of the authentication token
     */
    private void updateAuthTokenTask(long authTokenExpiry) {
        // Calculate the remaining time before the token expires
        long remainingTimeBeforeExpiry = authTokenExpiry - System.currentTimeMillis();
        // Get the delay for the next token update
        long delayForNextTokenUpdate = getDelayForNextTokenUpdate(remainingTimeBeforeExpiry);

        log.debug("Scheduling next token key update in {} ms, token remaining time: {} ms",
                delayForNextTokenUpdate, remainingTimeBeforeExpiry);

        // Save the timer for the next token update
        TIMER_MANAGER.saveTimer(authKey, delayForNextTokenUpdate, () -> {
            try {
                buildAuthToken();
            } catch (TencentCloudSDKException e) {
                if (ErrorCodeMatcher.isUserNotificationRequired(e.getErrorCode())) {
                    // If a user notification is required, remove the token from the cache
                    log.error("Failed to update the authentication token", e);
                    TOKEN_CACHE.removeAuthToken(authKey);
                } else {
                    // If an internal error occurs, try to update the token again
                    log.error("Failed to update the authentication token, Retry to update the token", e);
                    updateAuthTokenTask(System.currentTimeMillis() + TOKEN_UPDATE_INTERVAL);
                }
            }
        });
    }
}