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
package org.vincenzolabs.cots.player.service;

import org.vincenzolabs.cots.domain.Token;
import org.vincenzolabs.cots.domain.UserInformation;

/**
 * The service for accessing Amazon Cognito.
 *
 * @author Rey Vincent Babilonia
 */
public interface CognitoService {

    /**
     * Retrieves the {@link Token} containing the ID token, access token, refresh token, expiry and token type from
     * Amazon Cognito.
     *
     * @param grantType the {@link GrantType}
     * @param value     the authorization grant code or refresh token, depending on the {@link GrantType}
     * @return the {@link Token}
     */
    Token getToken(GrantType grantType, String value);

    /**
     * Retrieves the {@link UserInformation} from Amazon Cognito.
     *
     * @param accessToken the access token
     * @return the {@link UserInformation}
     */
    UserInformation getUserInformation(String accessToken);

    /**
     * Signs out the user with the given access token.
     *
     * @param accessToken the access token
     */
    void signOut(String accessToken);

    /**
     * The enumeration of grant types.
     */
    enum GrantType {

        /**
         * Authorization code grant type.
         */
        AUTHORIZATION_CODE,

        /**
         * Refresh token grant type.
         */
        REFRESH_TOKEN
    }
}
