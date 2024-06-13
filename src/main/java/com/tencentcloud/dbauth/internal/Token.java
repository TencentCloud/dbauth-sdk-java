package com.tencentcloud.dbauth.internal;

/**
 * Token is a utility class that represents an authentication token with its expiration time.
 */
public final class Token {
    // The authentication token string
    private final String authToken;

    // The expiration time of the token
    private final long expires;

    /**
     * Constructs a new Token with the given authentication token string and expiration time.
     *
     * @param authToken the authentication token string
     * @param expires   the expiration time of the token
     */
    public Token(String authToken, Long expires) {
        this.authToken = authToken;
        this.expires = expires;
    }

    /**
     * Returns the authentication token string.
     *
     * @return the authentication token string
     */
    public String getAuthToken() {
        return authToken;
    }

    /**
     * Returns the expiration time of the token.
     *
     * @return the expiration time of the token
     */
    public Long getExpires() {
        return expires;
    }
}