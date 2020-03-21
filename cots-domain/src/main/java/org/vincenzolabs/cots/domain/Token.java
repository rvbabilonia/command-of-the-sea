/*
 * This file is part of Command of the Sea.
 *
 * Copyright (c) 2019 VincenzoLabs
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.vincenzolabs.cots.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.Objects;

/**
 * The domain model object for OAuth2 token.
 *
 * @author Rey Vincent Babilonia
 */
public class Token {

    @Expose
    private final String idToken;

    @Expose
    private final String accessToken;

    @Expose
    private final String refreshToken;

    @Expose
    private final Long expiresIn;

    @Expose
    private final String tokenType;

    /**
     * Private constructor.
     *
     * @param builder the {@link Builder}
     */
    private Token(Builder builder) {
        this.idToken = builder.idToken;
        this.accessToken = builder.accessToken;
        this.refreshToken = builder.refreshToken;
        this.expiresIn = builder.expiresIn;
        this.tokenType = builder.tokenType;
    }

    /**
     * Returns the ID token valid for 1 hour.
     *
     * @return the ID token
     */
    public String getIdToken() {
        return idToken;
    }

    /**
     * Returns the access token valid for 1 hour.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Returns the refresh token valid for 30 days.
     *
     * @return the refresh token
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Returns the validity of the access token. Defaults to 3600 seconds (1 hour).
     *
     * @return the validity of the access token
     */
    public Long getExpiresIn() {
        return expiresIn;
    }

    /**
     * Returns the token type. Defaults to Bearer.
     *
     * @return the token type
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * Returns the {@link Builder}.
     *
     * @return the {@link Builder}
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Token token = (Token) o;
        return Objects.equals(idToken, token.idToken)
                && Objects.equals(accessToken, token.accessToken)
                && Objects.equals(refreshToken, token.refreshToken)
                && Objects.equals(expiresIn, token.expiresIn)
                && Objects.equals(tokenType, token.tokenType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idToken, accessToken, refreshToken, expiresIn, tokenType);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    /**
     * The builder.
     */
    public static class Builder {

        private String idToken;

        private String accessToken;

        private String refreshToken;

        private Long expiresIn;

        private String tokenType;

        /**
         * Private constructor.
         */
        private Builder() {
            // prevent instantiation
        }

        /**
         * Sets the ID token.
         *
         * @param idToken the ID token
         * @return the {@link Builder}
         */
        public Builder withIdToken(String idToken) {
            this.idToken = idToken;
            return this;
        }

        /**
         * Sets the access token.
         *
         * @param accessToken the access token
         * @return the {@link Builder}
         */
        public Builder withAccessToken(String accessToken) {
            this.accessToken = accessToken;
            return this;
        }

        /**
         * Sets the refresh token.
         *
         * @param refreshToken the refresh token
         * @return the {@link Builder}
         */
        public Builder withRefreshToken(String refreshToken) {
            this.refreshToken = refreshToken;
            return this;
        }

        /**
         * Sets the validity of the access token.
         *
         * @param expiresIn the validity of the access token
         * @return the {@link Builder}
         */
        public Builder withExpiresIn(Long expiresIn) {
            this.expiresIn = expiresIn;
            return this;
        }

        /**
         * Sets the token type.
         *
         * @param tokenType the token type
         * @return the {@link Builder}
         */
        public Builder withTokenType(String tokenType) {
            this.tokenType = tokenType;
            return this;
        }

        /**
         * Builds a {@link Token}.
         *
         * @return the {@link Token}
         */
        public Token build() {
            return new Token(this);
        }
    }
}
