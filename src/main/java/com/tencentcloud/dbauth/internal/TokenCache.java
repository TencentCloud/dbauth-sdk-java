package com.tencentcloud.dbauth.internal;

import com.tencentcloud.dbauth.model.GenerateAuthenticationTokenRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TokenCache is a utility class that provides methods for storing and retrieving authentication tokens.
 */
public final class TokenCache {
    private static final Logger log = LoggerFactory.getLogger(TokenCache.class);

    private static final long MAX_PASSWORD_SIZE = 200;

    // A concurrent hash map to store tokens associated with a key
    private final ConcurrentHashMap<String, Token> tokenMap = new ConcurrentHashMap<>();

    /**
     * Returns the authentication token associated with the given key.
     *
     * @param key the key associated with the token
     * @return the authentication token
     */
    public Token getAuthToken(String key) {
        return tokenMap.get(key);
    }

    /**
     * Saves the authentication token associated with the given key.
     *
     * @param key   the key associated with the token
     * @param token the authentication token
     */
    public void setAuthToken(String key, Token token) {
        if (StringUtils.isEmpty(key) || token == null) {
            return;
        }
        tokenMap.put(key, token);
    }

    /**
     * Removes the authentication token associated with the given key.
     *
     * @param key the key associated with the token
     */
    public void removeAuthToken(String key) {
        tokenMap.remove(key);
    }

    /**
     * Returns the fallback token associated with the given key.
     *
     * @param request the request containing the necessary information to generate a fallback token
     * @return the fallback token
     */
    public Token fallback(GenerateAuthenticationTokenRequest request) {

        // Generate the input file path
        Path inputFilePath = generateInputFilePath(request);
        if (inputFilePath == null) {
            return null;
        }

        // If the file exists, read the password from the file
        if (Files.exists(inputFilePath)) {
            try {
                log.info("file name: {}, file size: {}", inputFilePath, Files.size(inputFilePath));
                // If the file size is 0 or the file size is greater than 200, skip the file
                if (Files.size(inputFilePath) == 0) {
                    return null;
                }
                if (Files.size(inputFilePath) > MAX_PASSWORD_SIZE) {
                    log.error("The file size is greater than 200, skip the file: {}", inputFilePath);
                    return null;
                }
                // Read the password from the file
                List<String> lines = Files.readAllLines(inputFilePath);
                if (lines.size() == 0) {
                    return null;
                }
                if (lines.size() > 1) {
                    log.error("The file has more than one line, skip the file: {}", inputFilePath);
                    return null;
                }
                String password = lines.get(0);
                if (StringUtils.isEmpty(password)) {
                    return null;
                }

                log.info("Reading the password from the file: {}", inputFilePath);
                return new Token(password, System.currentTimeMillis() + Constants.MAX_DELAY);

            } catch (Exception e) {
                log.error("Failed to read the password from the file: {}", inputFilePath, e);
            }
        }
        return null;
    }

    /**
     * Generates the input file path based on the token key.
     *
     * @param request the request containing the necessary information to generate the input file path
     * @return the input file path
     */
    private Path generateInputFilePath(GenerateAuthenticationTokenRequest request) {
        String region = request.region();
        String instanceId = request.instanceId();
        String userName = request.userName();

        Path path = Paths.get(Constants.INPUT_PATH_DIR)
                .resolve(region + Constants.DELIMITER + instanceId + Constants.DELIMITER + userName + ".pwd");

        return Paths.get(System.getProperty("user.dir"), path.toString());
    }
}