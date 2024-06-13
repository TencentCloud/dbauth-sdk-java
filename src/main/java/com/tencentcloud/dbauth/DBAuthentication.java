package com.tencentcloud.dbauth;

import com.tencentcloud.dbauth.internal.ErrorCodeMatcher;
import com.tencentcloud.dbauth.internal.Signer;
import com.tencentcloud.dbauth.internal.Token;
import com.tencentcloud.dbauth.model.GenerateAuthenticationTokenRequest;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DBAuthentication is a utility class that provides methods for generating authentication tokens.
 */
public final class DBAuthentication {

    private static final Logger log = LoggerFactory.getLogger(DBAuthentication.class);

    private DBAuthentication() {
    }

    /**
     * Generates an authentication token using the provided request.
     *
     * @param tokenRequest the request containing the necessary information to generate an authentication token
     * @return the generated authentication token
     * @throws TencentCloudSDKException if there is an error during the token generation
     */
    public static String generateAuthenticationToken(
            GenerateAuthenticationTokenRequest tokenRequest) throws TencentCloudSDKException {
        // Create a new Signer with the provided token request.
        Signer signer = new Signer(tokenRequest);
        // Get the authentication token from the cache.
        Token cachedToken = signer.getAuthTokenFromCache();
        if (cachedToken != null) {
            if (cachedToken.getExpires() > System.currentTimeMillis()) {
                // If the token has not expired, return the token.
                return cachedToken.getAuthToken();
            }
        }
        try {
            signer.buildAuthToken();
            return signer.getAuthTokenFromCache().getAuthToken();
        } catch (TencentCloudSDKException e) {
            log.error("Error occurred while generating authentication token", e);
            if (cachedToken != null) {
                if (ErrorCodeMatcher.isUserNotificationRequired(e.getErrorCode())) {
                    throw e;
                } else {
                    return cachedToken.getAuthToken();
                }
            }
            throw e;
        }
    }
}