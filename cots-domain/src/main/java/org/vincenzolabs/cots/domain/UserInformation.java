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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * The domain model object for OAuth2 user information.
 *
 * @author Rey Vincent Babilonia
 */
public class UserInformation {

    @Expose
    private final String subject;

    @Expose
    private final List<Map<String, Object>> identities;

    @Expose
    private final String emailAddress;

    @Expose
    private final String username;

    /**
     * Private constructor.
     *
     * @param builder the {@link Builder}
     */
    private UserInformation(Builder builder) {
        this.subject = builder.subject;
        this.identities = builder.identities;
        this.emailAddress = builder.emailAddress;
        this.username = builder.username;
    }

    /**
     * Returns the subject.
     *
     * @return the subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Returns the {@link List} of identities.
     *
     * @return the {@link List} of identities
     */
    public List<Map<String, Object>> getIdentities() {
        return identities;
    }

    /**
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Returns the username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
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
        UserInformation that = (UserInformation) o;
        return Objects.equals(subject, that.subject)
                && Objects.equals(identities, that.identities)
                && Objects.equals(emailAddress, that.emailAddress)
                && Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, identities, emailAddress, username);
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

        private String subject;

        private List<Map<String, Object>> identities = new ArrayList<>();

        private String emailAddress;

        private String username;

        /**
         * Private constructor.
         */
        private Builder() {
            // prevent instantiation
        }

        /**
         * Sets the subject.
         *
         * @param subject the subject
         * @return the {@link Builder}
         */
        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        /**
         * Sets the {@link List} of identities.
         *
         * @param identities the {@link List} of identities
         * @return the {@link Builder}
         */
        public Builder withIdentities(List<Map<String, Object>> identities) {
            this.identities = identities;
            return this;
        }

        /**
         * Sets the email address.
         *
         * @param emailAddress the email address
         * @return the {@link Builder}
         */
        public Builder withEmailAddress(String emailAddress) {
            this.emailAddress = emailAddress;
            return this;
        }

        /**
         * Sets the username.
         *
         * @param username the username
         * @return the {@link Builder}
         */
        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        /**
         * Builds a {@link UserInformation}.
         *
         * @return the {@link UserInformation}
         */
        public UserInformation build() {
            return new UserInformation(this);
        }
    }
}
