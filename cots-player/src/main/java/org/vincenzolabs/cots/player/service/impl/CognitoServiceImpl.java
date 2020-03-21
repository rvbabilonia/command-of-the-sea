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
package org.vincenzolabs.cots.player.service.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.vincenzolabs.cots.domain.Token;
import org.vincenzolabs.cots.domain.UserInformation;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vincenzolabs.cots.player.service.CognitoService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GlobalSignOutRequest;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The implementation of {@link CognitoService}.
 *
 * @author Rey Vincent Babilonia
 */
@Service
public class CognitoServiceImpl implements CognitoService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CognitoServiceImpl.class);

    private static final Type IDENTITIES_TYPE = new TypeToken<List<Map<String, Object>>>() {
    }.getType();

    private final CognitoIdentityProviderClient cognitoIdentityProviderClient;

    private final SsmClient ssmClient;

    private final Client client;

    private final JSONParser parser;

    private final Gson gson;

    /**
     * Default constructor.
     *
     * @param cognitoIdentityProviderClient the {@link CognitoIdentityProviderClient}
     * @param ssmClient                     the {@link SsmClient}
     * @param client                        the {@link Client}
     * @param parser                        the {@link JSONParser}
     * @param gson                          the {@link Gson}
     */
    @Autowired
    public CognitoServiceImpl(CognitoIdentityProviderClient cognitoIdentityProviderClient, SsmClient ssmClient,
                              Client client, JSONParser parser, Gson gson) {
        this.cognitoIdentityProviderClient = cognitoIdentityProviderClient;
        this.ssmClient = ssmClient;
        this.client = client;
        this.parser = parser;
        this.gson = gson;
    }

    @Override
    public Token getToken(GrantType grantType, final String value) {
        Form form = new Form();

        if (GrantType.AUTHORIZATION_CODE == grantType) {
            // use the authorization grant code to get the tokens
            // access token is valid for 1 hour whereas refresh token is for 30 days
            form.param("grant_type", GrantType.AUTHORIZATION_CODE.name().toLowerCase());
            form.param("code", value);
            form.param("client_id", getClientId());
            form.param("redirect_uri", getRedirectUri());

            Response postResponse = createInvocationBuilder().post(Entity.form(form));
            if (postResponse != null) {
                if (200 == postResponse.getStatus()) {
                    try {
                        JSONObject body = (JSONObject) parser.parse(postResponse.readEntity(String.class));
                        String idToken = (String) body.get("id_token");
                        String accessToken = (String) body.get("access_token");
                        String refreshToken = (String) body.get("refresh_token");
                        Long expiresIn = (Long) body.get("expires_in");
                        String tokenType = (String) body.get("token_type");

                        return Token.builder()
                                .withIdToken(idToken)
                                .withAccessToken(accessToken)
                                .withRefreshToken(refreshToken)
                                .withExpiresIn(expiresIn)
                                .withTokenType(tokenType)
                                .build();
                    } catch (ParseException e) {
                        LOGGER.error("Failed to parse response: [{}]", e.getMessage(), e);

                        return null;
                    }
                } else {
                    // authorization grant code is invalid
                    try {
                        JSONObject body = (JSONObject) parser.parse(postResponse.readEntity(String.class));
                        String error = (String) body.get("error");
                        LOGGER.error("Failed to fetch token: [{}]", error);

                        throw AwsServiceException.builder()
                                .statusCode(postResponse.getStatus())
                                .message(error)
                                .build();
                    } catch (ParseException e) {
                        LOGGER.error("Failed to parse response: [{}]", e.getMessage(), e);

                        return null;
                    }
                }
            } else {
                LOGGER.error("Failed to fetch access token using authorization grant code");

                return null;
            }
        } else {
            // use the refresh token to generate a new access token
            form.param("grant_type", GrantType.REFRESH_TOKEN.name().toLowerCase());
            form.param("client_id", getClientId());
            form.param("refresh_token", value);

            Response postResponse = createInvocationBuilder().post(Entity.form(form));
            if (postResponse != null) {
                if (200 == postResponse.getStatus()) {
                    try {
                        JSONObject body = (JSONObject) parser.parse(postResponse.readEntity(String.class));
                        String idToken = (String) body.get("id_token");
                        String accessToken = (String) body.get("access_token");
                        Long expiresIn = (Long) body.get("expires_in");
                        String tokenType = (String) body.get("token_type");

                        return Token.builder()
                                .withIdToken(idToken)
                                .withAccessToken(accessToken)
                                .withExpiresIn(expiresIn)
                                .withTokenType(tokenType)
                                .build();
                    } catch (ParseException e) {
                        LOGGER.error("Failed to parse response: [{}]", e.getMessage(), e);

                        return null;
                    }
                } else {
                    try {
                        JSONObject body = (JSONObject) parser.parse(postResponse.readEntity(String.class));
                        String error = (String) body.get("error");
                        LOGGER.error("Failed to fetch token: [{}]", error);

                        throw AwsServiceException.builder()
                                .statusCode(postResponse.getStatus())
                                .message(error)
                                .build();
                    } catch (ParseException e) {
                        LOGGER.error("Failed to parse response: [{}]", e.getMessage(), e);

                        return null;
                    }
                }
            } else {
                LOGGER.error("Failed to fetch access token using refresh token");

                return null;
            }
        }
    }

    @Override
    public UserInformation getUserInformation(final String accessToken) {
        GetUserRequest getUserRequest = GetUserRequest.builder()
                .accessToken(accessToken)
                .build();

        try {
            GetUserResponse getUserResponse = cognitoIdentityProviderClient.getUser(getUserRequest);

            String email = null;
            String subject = null;
            List<Map<String, Object>> identities = new ArrayList<>();
            for (AttributeType attributeType : getUserResponse.userAttributes()) {
                if ("email".equalsIgnoreCase(attributeType.name())) {
                    email = attributeType.value();
                }
                if ("sub".equalsIgnoreCase(attributeType.name())) {
                    subject = attributeType.value();
                }
                if ("identities".equalsIgnoreCase(attributeType.name())) {
                    identities.addAll(gson.fromJson(attributeType.value(), IDENTITIES_TYPE));
                }
            }

            return UserInformation.builder()
                    .withUsername(getUserResponse.username())
                    .withEmailAddress(email)
                    .withSubject(subject)
                    .withIdentities(identities)
                    .build();
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve user information: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public void signOut(final String accessToken) {
        try {
            cognitoIdentityProviderClient.globalSignOut(
                    GlobalSignOutRequest.builder()
                            .accessToken(accessToken)
                            .build());
        } catch (Exception e) {
            LOGGER.error("Failed to sign out: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    private String getClientId() {
        return ssmClient.getParameter(
                GetParameterRequest.builder()
                        .name("CLIENT_ID")
                        .build())
                .parameter()
                .value();
    }

    private String getCognitoUrl() {
        return ssmClient.getParameter(
                GetParameterRequest.builder()
                        .name("COGNITO_URL")
                        .build())
                .parameter()
                .value();
    }

    private String getRedirectUri() {
        return ssmClient.getParameter(
                GetParameterRequest.builder()
                        .name("REDIRECT_URI")
                        .build())
                .parameter()
                .value();
    }

    private Invocation.Builder createInvocationBuilder() {
        WebTarget webTarget = client.target(getCognitoUrl())
                .path("/oauth2/token");

        return webTarget.request(MediaType.APPLICATION_JSON_TYPE);
    }
}
