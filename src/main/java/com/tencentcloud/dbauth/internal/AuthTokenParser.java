package com.tencentcloud.dbauth.internal;

import com.google.protobuf.InvalidProtocolBufferException;
import com.tencentcloud.dbauth.model.AuthTokenInfoOuterClass;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * AuthTokenParser is a utility class that provides methods for parsing and storing authentication token information.
 */
public class AuthTokenParser {
    /**
     * Parses the authentication token and returns the authentication token information.
     *
     * @param instanceId the instance ID
     * @param region     the region
     * @param userName   the userName
     * @param token      the authentication token
     * @return the authentication token information
     * @throws Exception if an error occurs during parsing
     */
    public static AuthTokenInfoOuterClass.AuthTokenInfo parseAuthToken(
            String instanceId, String region, String userName, String token) throws Exception {
        if (StringUtils.isAnyEmpty(instanceId, region, userName, token)) {
            throw new Exception("param empty");
        }

        // Generate encryption key
        String seedKey = sha256(
                (instanceId + Constants.DELIMITER + region + Constants.DELIMITER + userName).getBytes()
        );
        String key = seedKey.substring(0, 32);
        String iv = seedKey.substring(33, 49);

        // Decrypt AuthToken
        byte[] decToken = decrypt(token.substring(64), key, iv);

        // Compare if the token has been truncated
        String tokenHash = sha256(decToken);
        if (!token.substring(0, 64).equals(tokenHash)) {
            throw new Exception("token not compare");
        }

        // Parse token
        return getAuthTokenInfo(decToken);
    }

    /**
     * Parses the authentication token information from the decrypted token.
     *
     * @param decToken the decrypted token
     * @return the parsed authentication token information
     * @throws TencentCloudSDKException if an error occurs during parsing
     */
    private static AuthTokenInfoOuterClass.AuthTokenInfo getAuthTokenInfo(byte[] decToken) throws Exception {
        byte[] subToken = Arrays.copyOfRange(decToken, 4, decToken.length);

        AuthTokenInfoOuterClass.AuthTokenInfo tokenInfo;
        try {
            tokenInfo = AuthTokenInfoOuterClass.AuthTokenInfo.parseFrom(subToken);
        } catch (InvalidProtocolBufferException e) {
            throw new Exception("Failed to parse AuthTokenInfo", e);
        }
        return tokenInfo;
    }

    /**
     * Computes the SHA-256 hash of the given byte array.
     *
     * @param base the byte array to hash
     * @return the computed hash
     * @throws NoSuchAlgorithmException if the SHA-256 algorithm is not available
     */
    public static String sha256(byte[] base) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(Constants.SHA256);
        byte[] hash = digest.digest(base);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = String.format("%02x", b);
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Decrypts the given input string using the AES algorithm with the given key and initialization vector.
     *
     * @param input the string to decrypt
     * @param key   the decryption key
     * @param iv    the initialization vector
     * @return the decrypted string
     * @throws Exception if an error occurs during decryption
     */
    private static byte[] decrypt(String input, String key, String iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key.getBytes(), "AES"), new IvParameterSpec(iv.getBytes()));

        return cipher.doFinal(base64Decode(input));
    }

    /**
     * Decodes the given Base64-encoded string into a byte array.
     *
     * @param string the Base64-encoded string to decode
     * @return the decoded byte array
     */
    private static byte[] base64Decode(String string) {
        // replace URL-safe characters
        String data = string.replace("-", "+").replace("_", "/");

        // add padding characters if necessary
        int mod4 = data.length() % 4;
        if (mod4 != 0) {
            data += "====".substring(mod4);
        }

        return Base64.getDecoder().decode(data);

    }
}