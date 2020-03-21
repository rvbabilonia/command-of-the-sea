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

import com.google.gson.GsonBuilder;
import org.vincenzolabs.cots.domain.Token;
import org.vincenzolabs.cots.domain.UserInformation;
import org.glassfish.jersey.client.JerseyInvocation;
import org.json.simple.parser.JSONParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.vincenzolabs.cots.player.service.impl.CognitoServiceImpl;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GetUserResponse;
import software.amazon.awssdk.services.cognitoidentityprovider.model.GlobalSignOutRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.NotAuthorizedException;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.model.GetParameterRequest;
import software.amazon.awssdk.services.ssm.model.GetParameterResponse;
import software.amazon.awssdk.services.ssm.model.Parameter;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link CognitoService}.
 *
 * @author Rey Vincent Babilonia
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CognitoServiceTest {

    @Mock
    private CognitoIdentityProviderClient cognitoIdentityProviderClient;

    @Mock
    private SsmClient ssmClient;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Client client;

    private CognitoService cognitoService;

    @BeforeEach
    void setUp() {
        cognitoService = new CognitoServiceImpl(cognitoIdentityProviderClient, ssmClient, client,
                new JSONParser(), new GsonBuilder().create());
    }

    @Test
    void getTokenByAuthorizationGrantCode() {
        Parameter clientIdParameter = Parameter.builder()
                .name("CLIENT_ID")
                .value("clientId")
                .build();
        GetParameterResponse clientIdResponse = GetParameterResponse.builder()
                .parameter(clientIdParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("CLIENT_ID").build())))
                .thenReturn(clientIdResponse);

        Parameter cognitoUrlParameter = Parameter.builder()
                .name("COGNITO_URL")
                .value("https://cots.auth.ap-southeast-2.amazoncognito.com")
                .build();
        GetParameterResponse cognitoUrlResponse = GetParameterResponse.builder()
                .parameter(cognitoUrlParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("COGNITO_URL").build())))
                .thenReturn(cognitoUrlResponse);

        Parameter redirectUriParameter = Parameter.builder()
                .name("REDIRECT_URI")
                .value("https://cots.cloudfront.net")
                .build();
        GetParameterResponse redirectUriResponse = GetParameterResponse.builder()
                .parameter(redirectUriParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("REDIRECT_URI").build())))
                .thenReturn(redirectUriResponse);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(String.class))
                .thenAnswer((Answer<String>) invocation -> createTokenFromAuthorizationGrantCode());

        JerseyInvocation.Builder builder = mock(JerseyInvocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);

        when(client.target(any(String.class)).path(any(String.class)).request(any(MediaType.class)))
                .thenReturn(builder);

        Token token = cognitoService.getToken(CognitoService.GrantType.AUTHORIZATION_CODE,
                "51535656-6797-4ff8-b4c8-56323fbe1672");

        assertThat(token)
                .extracting("expiresIn", "tokenType")
                .containsExactly(3600L, "Bearer");
        assertThat(token.getIdToken()).isNotNull();
        assertThat(token.getAccessToken()).isNotNull();
        assertThat(token.getRefreshToken()).isNotNull();
    }

    @Test
    void getTokenByAuthorizedGrantCodeWithInvalidCode() {
        Parameter clientIdParameter = Parameter.builder()
                .name("CLIENT_ID")
                .value("clientId")
                .build();
        GetParameterResponse clientIdResponse = GetParameterResponse.builder()
                .parameter(clientIdParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("CLIENT_ID").build())))
                .thenReturn(clientIdResponse);

        Parameter cognitoUrlParameter = Parameter.builder()
                .name("COGNITO_URL")
                .value("https://cots.auth.ap-southeast-2.amazoncognito.com")
                .build();
        GetParameterResponse cognitoUrlResponse = GetParameterResponse.builder()
                .parameter(cognitoUrlParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("COGNITO_URL").build())))
                .thenReturn(cognitoUrlResponse);

        Parameter redirectUriParameter = Parameter.builder()
                .name("REDIRECT_URI")
                .value("https://cots.cloudfront.net")
                .build();
        GetParameterResponse redirectUriResponse = GetParameterResponse.builder()
                .parameter(redirectUriParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("REDIRECT_URI").build())))
                .thenReturn(redirectUriResponse);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(String.class))
                .thenAnswer((Answer<String>) invocation -> "{\"error\": \"invalid_grant\"}");

        JerseyInvocation.Builder builder = mock(JerseyInvocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);

        when(client.target(any(String.class)).path(any(String.class)).request(any(MediaType.class)))
                .thenReturn(builder);

        assertThatThrownBy(() -> cognitoService.getToken(CognitoService.GrantType.AUTHORIZATION_CODE,
                "51535656-6797-4ff8-b4c8-56323fbe1672"))
                .isInstanceOf(AwsServiceException.class)
                .hasMessage("invalid_grant");
    }

    @Test
    void getTokenByRefreshToken() {
        Parameter clientIdParameter = Parameter.builder()
                .name("CLIENT_ID")
                .value("clientId")
                .build();
        GetParameterResponse clientIdResponse = GetParameterResponse.builder()
                .parameter(clientIdParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("CLIENT_ID").build())))
                .thenReturn(clientIdResponse);

        Parameter cognitoUrlParameter = Parameter.builder()
                .name("COGNITO_URL")
                .value("https://cots.auth.ap-southeast-2.amazoncognito.com")
                .build();
        GetParameterResponse cognitoUrlResponse = GetParameterResponse.builder()
                .parameter(cognitoUrlParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("COGNITO_URL").build())))
                .thenReturn(cognitoUrlResponse);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);
        when(response.readEntity(String.class))
                .thenAnswer((Answer<String>) invocation -> createTokenFromRefreshToken());

        JerseyInvocation.Builder builder = mock(JerseyInvocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);

        when(client.target(any(String.class)).path(any(String.class)).request(any(MediaType.class)))
                .thenReturn(builder);

        Token token = cognitoService.getToken(CognitoService.GrantType.REFRESH_TOKEN, "refreshToken");

        assertThat(token)
                .extracting("refreshToken", "expiresIn", "tokenType")
                .containsExactly(null, 3600L, "Bearer");
        assertThat(token.getIdToken()).isNotNull();
        assertThat(token.getAccessToken()).isNotNull();
    }

    @Test
    void getTokenByRefreshTokenWithInvalidRefreshToken() {
        Parameter clientIdParameter = Parameter.builder()
                .name("CLIENT_ID")
                .value("clientId")
                .build();
        GetParameterResponse clientIdResponse = GetParameterResponse.builder()
                .parameter(clientIdParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("CLIENT_ID").build())))
                .thenReturn(clientIdResponse);

        Parameter cognitoUrlParameter = Parameter.builder()
                .name("COGNITO_URL")
                .value("https://cots.auth.ap-southeast-2.amazoncognito.com")
                .build();
        GetParameterResponse cognitoUrlResponse = GetParameterResponse.builder()
                .parameter(cognitoUrlParameter)
                .build();
        when(ssmClient.getParameter(eq(GetParameterRequest.builder().name("COGNITO_URL").build())))
                .thenReturn(cognitoUrlResponse);

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(400);
        when(response.readEntity(String.class))
                .thenAnswer((Answer<String>) invocation -> "{\"error\": \"invalid_grant\"}");

        JerseyInvocation.Builder builder = mock(JerseyInvocation.Builder.class);
        when(builder.post(any(Entity.class))).thenReturn(response);

        when(client.target(any(String.class)).path(any(String.class)).request(any(MediaType.class)))
                .thenReturn(builder);

        assertThatThrownBy(() -> cognitoService.getToken(CognitoService.GrantType.REFRESH_TOKEN, "refreshToken"))
                .isInstanceOf(AwsServiceException.class)
                .hasMessage("invalid_grant");
    }

    @Test
    void getUserInformation() {
        GetUserResponse response = GetUserResponse.builder()
                .username("Google_132394663945457867236")
                .userAttributes(
                        AttributeType.builder()
                                .name("sub")
                                .value("0adcd84d-e7a6-4f02-be15-2024815dc6f1")
                                .build(),
                        AttributeType.builder()
                                .name("email")
                                .value("rvbabilonia@gmail.com")
                                .build(),
                        AttributeType.builder()
                                .name("identities")
                                .value("[{\"userId\":\"132394663945457867236\",\"providerName\":\"Google\"," +
                                        "\"providerType\":\"Google\",\"issuer\":null,\"primary\":true," +
                                        "\"dateCreated\":1572324485926}]")
                                .build())
                .build();
        when(cognitoIdentityProviderClient.getUser(any(GetUserRequest.class))).thenReturn(response);

        UserInformation userInformation = cognitoService.getUserInformation("accessToken");

        assertThat(userInformation)
                .extracting("subject", "emailAddress", "username")
                .containsExactly("0adcd84d-e7a6-4f02-be15-2024815dc6f1", "rvbabilonia@gmail.com",
                        "Google_132394663945457867236");

        assertThat(userInformation.getIdentities())
                .hasSize(1)
                .first()
                .extracting("userId", "providerName", "providerType", "issuer", "primary")
                .containsExactly("132394663945457867236", "Google", "Google", null, true);
    }

    @Test
    void getUserInformationWithInvalidAccessToken() {
        when(cognitoIdentityProviderClient.getUser(any(GetUserRequest.class)))
                .thenThrow(NotAuthorizedException.builder()
                        .message("Invalid Access Token (Service: CognitoIdentityProvider,"
                                + " Status Code: 400, Request ID: 5c01ad1b-7aa0-4891-8a5a-5cdfef8ca10c)")
                        .build());

        assertThatThrownBy(() -> cognitoService.getUserInformation("accessToken"))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessage("Invalid Access Token (Service: CognitoIdentityProvider," +
                        " Status Code: 400, Request ID: 5c01ad1b-7aa0-4891-8a5a-5cdfef8ca10c)");
    }

    @Test
    void signOut() {
        cognitoService.signOut("accessToken");

        verify(cognitoIdentityProviderClient).globalSignOut(any(GlobalSignOutRequest.class));
    }

    @Test
    void signOutWithInvalidAccessToken() {
        when(cognitoIdentityProviderClient.globalSignOut(any(GlobalSignOutRequest.class)))
                .thenThrow(NotAuthorizedException.builder()
                        .message("Invalid Access Token (Service: CognitoIdentityProvider,"
                                + " Status Code: 400, Request ID: 56f16177-df3b-4cfb-9336-856500f5a89c)")
                        .build());

        assertThatThrownBy(() -> cognitoService.signOut("accessToken"))
                .isInstanceOf(NotAuthorizedException.class)
                .hasMessage("Invalid Access Token (Service: CognitoIdentityProvider,"
                        + " Status Code: 400, Request ID: 56f16177-df3b-4cfb-9336-856500f5a89c)");
    }

    private String createTokenFromAuthorizationGrantCode() {
        return "{\"id_token\":\"eyJraWQiOiJrd2N6bDFMeTk4VXdEc3hseUpZXC9GRnBodlZYXC9iRnl0V003N1lEYXZST1E9IiwiYWxnIjoiU"
                + "lMyNTYifQ.eyJhdF9oYXNoIjoiT2tWRk1Ma3F5Ykl6cmwzQWpkTV9vQSIsInN1YiI6IjUzYzY1NWEyLTM4M2YtNDRiMi04ODQ1L"
                + "TYxY2Q1MWJiODFkMCIsImF1ZCI6IjFya2l0ajlmbGxsZjhtZGs0bDBkanM0a245IiwiY29nbml0bzpncm91cHMiOlsiYXAtc291"
                + "dGhlYXN0LTJfNEk0T0tXR2lJX0dvb2dsZSJdLCJpZGVudGl0aWVzIjpbeyJ1c2VySWQiOiIxMTIzOTQ2NjM5ODg0NTQ4NjcxMzQ"
                + "iLCJwcm92aWRlck5hbWUiOiJHb29nbGUiLCJwcm92aWRlclR5cGUiOiJHb29nbGUiLCJpc3N1ZXIiOm51bGwsInByaW1hcnkiOi"
                + "J0cnVlIiwiZGF0ZUNyZWF0ZWQiOiIxNTcyMzI0NDg1OTI2In1dLCJ0b2tlbl91c2UiOiJpZCIsImF1dGhfdGltZSI6MTU4NDU4M"
                + "DQzNCwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmFwLXNvdXRoZWFzdC0yLmFtYXpvbmF3cy5jb21cL2FwLXNvdXRoZWFz"
                + "dC0yXzRJNE9LV0dpSSIsImNvZ25pdG86dXNlcm5hbWUiOiJHb29nbGVfMTEyMzk0NjYzOTg4NDU0ODY3MTM0IiwiZXhwIjoxNTg"
                + "0NTg0MDM0LCJpYXQiOjE1ODQ1ODA0MzQsImVtYWlsIjoicnZiYWJpbG9uaWFAZ21haWwuY29tIn0.NRM7goGIh3BA-2ot8lAP10"
                + "IPBz7kqA4PJrZ08FiB4i1L8SU-887fpnvKffMgqPKv_zf8HtyW1J1U3NQ9tFiy2s0y8ScACpqEFIT5bfheJP5fs2jAQA3-ivUD_"
                + "cYwfRqSCVzn6MIhjLeu3vmXnzhYZaBuQgMvCffxMrahaKOR-XwgLx7O-FBHAeiLNMhUv5x6HAdaR3NoXl6d1Zc1VXJrsp8JHbKS"
                + "JP8Pu80jh4EjXnWw223s0iiSGuNdNqTOm5EP85s-dC_JuRQ57HLLFEKU8Rhwoa7OGArWLgvhVELFU68JwjQCvvmFRc71emdQBnV"
                + "KMm8mbn9kafZw9z59lOSfig\","
                + "\"access_token\":\"eyJraWQiOiJtXC9jSTQzY01JSW53aUgwYnhpaEZKOW56eFFGUG1kR2JmUkNLNG1XeEFHST0iLCJhbGciO"
                + "iJSUzI1NiJ9.eyJzdWIiOiI1M2M2NTVhMi0zODNmLTQ0YjItODg0NS02MWNkNTFiYjgxZDAiLCJjb2duaXRvOmdyb3VwcyI6WyJ"
                + "hcC1zb3V0aGVhc3QtMl80STRPS1dHaUlfR29vZ2xlIl0sInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG"
                + "8uc2lnbmluLnVzZXIuYWRtaW4gb3BlbmlkIHByb2ZpbGUgZW1haWwiLCJhdXRoX3RpbWUiOjE1ODQ1ODA0MzQsImlzcyI6Imh0d"
                + "HBzOlwvXC9jb2duaXRvLWlkcC5hcC1zb3V0aGVhc3QtMi5hbWF6b25hd3MuY29tXC9hcC1zb3V0aGVhc3QtMl80STRPS1dHaUki"
                + "LCJleHAiOjE1ODQ1ODQwMzQsImlhdCI6MTU4NDU4MDQzNCwidmVyc2lvbiI6MiwianRpIjoiOWEzNTFhMGMtODExMC00NWI2LWE"
                + "2Y2ItNjI2NjA3M2JkMmJjIiwiY2xpZW50X2lkIjoiMXJraXRqOWZsbGxmOG1kazRsMGRqczRrbjkiLCJ1c2VybmFtZSI6Ikdvb2"
                + "dsZV8xMTIzOTQ2NjM5ODg0NTQ4NjcxMzQifQ.Xbr2OzqGMFnjiBHAojlrgXXYwVgA6VxeasUfB_rYi0A-MrOzI6CpPafUXBJmSH"
                + "CFj8GIwVluTtt_SXQesLMLoFQwfQm0c8JDDadzSvfZj50GUMJL1HPmIM4QDj-vXAPfmgkVYZQD9lt8bqK-C1bqSI4F6JsOVmwGQ"
                + "fsQ1R7EmqMFQfrir2eVYrlVxkSWjEbNoJyoMdXwEN5FeagOpOiTSs0k0YaAhspMw7hBKX6MCFfvpn2GT2Os-B3JdBTeYw74hkcZ"
                + "KEEOyi_vIu7ZVTX_DTC47GOs6eS4IcmTWg8XNT3DbPF14xlc3PuTMs48Bi8kEE9Mtw-OJu6ZwENv5n5qkg\","
                + "\"refresh_token\":\"eyJjdHkiOiJKV1QiLCJlbmMiOiJBMjU2R0NNIiwiYWxnIjoiUlNBLU9BRVAifQ.UDM_Lnt366d7P7J22"
                + "7Fgk-xX_fuOTDT3r-5SIpyvlJ2zMUnCChBBqdbipxy2vmSlTc1WfFKvtNUrage5JBslkX8lHaEWWvim6mPoGyyzUJNhZmUlVf86"
                + "T_qRnVzlKTcTHlnKavZ-JA-dJQmxYRlSx0kjm4D0yIblNVA_MbbG837HclDg6iOHt5vhjMCAZRHhI8DwoRKmcYTrWyH-zBoE5bK"
                + "R6tw4K4_GvE4UWfIaI-ANVXjSp1N6OjTlpJAfB9LrxARo6oamlzZssgul8ARhbJItm-nlj_OqRnOlvKws_lxQr-ASM2fabjVS72"
                + "jy-Eiiugsv5cj8xY9ru1DnhdWLoA.H32GmyWYxrAare25.WENnmRgdMuNv5G7b3C_QkamLyVLPjYbHsdXyNtYPlSjS22bUYfJkq"
                + "DcGJUSEMeM-RA7S-mBLkzIsgAJGvVj02HPhPhayTs_JbNEbLyRLvuzJILu_0TaK2pH9YIxuwLRywM53l_YoCPKVVvBqrx5NW9fU"
                + "uQRZPMf9N6kh144ZF7RX2qEtK1oSsNPCILhbyBnHeYA-MldGTfDs7R-O_vlKMXx6NwuSGzwWupbfZ6By34-aONh5m-YkatVrmiw"
                + "i0TKUcm9jBkPmLjaqQwQ6gEm-eNPNbQ15pU8dHEJmtyPA4xzJDv9nt-UlKIKNXK8vcT5OHYavK87QXuiSY7Q5wEBH11vInG7EQq"
                + "QEvxR8yRXhWnLXeKJDASDZXzvqIfx1jkyrL3MvHX7itU0CPzGRrVUQWkpkHSFFtiflsfa5jhKkHKPyRX7WGPMruxdMzHfIUrBUp"
                + "EVfZtId56FW_EG4Ry52i6pMirc3jo_GUpt9tDRr-mNChNn5_KRNQZ7-eLFTlURnZ6C1Pyz52AOZPkW2MEFaO66erjaZt26to3ad"
                + "-jVFGIi7X4D4RFM3P0VGe4XhrFlVtcwPGYaf8MurnwcjNnxqnmDCDXr-z0FuE_t1vhH-FA6xWS-LUz1N7HG-AM1Facf1BPZeVPH"
                + "oNeQkKjo0CU8F62yIewyBGwkkA-7AhA8O7PXSkWnvGxrxPkYCnzz_Tqxu51PBhw2-6C61_TT3yStIaot8VsGo0SAg-tSFAbsAAH"
                + "QDZCx58qBVVVbpGs5U85Z-WkkGI8fnNO3AcCkqSDL-HI296DbFP4rTHcO9BrS9QS8vQ4OnKPDK3sYu12fN0Lw2c7JqcRmZlJJoJ"
                + "HqkBhsqHCl23sXr0q4jwS21WxZ5Et5vnLgJNWt7-RRgovoXTnKmuSJ5t_BFjRBEInnk2Ctiu3Bcx7txLe7Xz_icMyHxYeR6ZIuI"
                + "nJYKMt_iza-u2zacriA6CD0xftl_76Fyuy9zQsDZ85YMLnsGUUJU5o8Qib9AhZPxtZT1aUgBmmbItk4ZfLwT05V_HYHdEAIh_kv"
                + "fAzAsLojQ7xbWW31iaZA1P5PqdVZ0T-JeNziaez7lii1GOZkpnKLg15TClinmOzUIC2KoQkL8j44jhCoC86Uu7tsYeeI21CvQUg"
                + "X0C94ofFmTFE84NrqYjxOBWh83cu6AoLogm3tdAcrWHWur3dxflOhvJFZkD1x1b6AN8Ge6up8cuNt3-Eak0xPm_SlWIOygpdNpi"
                + "_3vf83VbX_bvF0OiOi8_CeRp6HG6aYrukgwEDTU868nmlRn7lt4KlrC9hHeNAeFyTIKruM.jixgssNLI1CLGrLGSGWuxA\","
                + "\"expires_in\":3600,"
                + "\"token_type\":\"Bearer\"}";
    }

    private String createTokenFromRefreshToken() {
        return "{\"id_token\":\"eyJraWQiOiJrd2N6bDFMeTk4VXdEc3hseUpZXC9GRnBodlZYXC9iRnl0V003N1lEYXZST1E9IiwiYWxnIjoiU"
                + "lMyNTYifQ.eyJhdF9oYXNoIjoiT2tWRk1Ma3F5Ykl6cmwzQWpkTV9vQSIsInN1YiI6IjUzYzY1NWEyLTM4M2YtNDRiMi04ODQ1L"
                + "TYxY2Q1MWJiODFkMCIsImF1ZCI6IjFya2l0ajlmbGxsZjhtZGs0bDBkanM0a245IiwiY29nbml0bzpncm91cHMiOlsiYXAtc291"
                + "dGhlYXN0LTJfNEk0T0tXR2lJX0dvb2dsZSJdLCJpZGVudGl0aWVzIjpbeyJ1c2VySWQiOiIxMTIzOTQ2NjM5ODg0NTQ4NjcxMzQ"
                + "iLCJwcm92aWRlck5hbWUiOiJHb29nbGUiLCJwcm92aWRlclR5cGUiOiJHb29nbGUiLCJpc3N1ZXIiOm51bGwsInByaW1hcnkiOi"
                + "J0cnVlIiwiZGF0ZUNyZWF0ZWQiOiIxNTcyMzI0NDg1OTI2In1dLCJ0b2tlbl91c2UiOiJpZCIsImF1dGhfdGltZSI6MTU4NDU4M"
                + "DQzNCwiaXNzIjoiaHR0cHM6XC9cL2NvZ25pdG8taWRwLmFwLXNvdXRoZWFzdC0yLmFtYXpvbmF3cy5jb21cL2FwLXNvdXRoZWFz"
                + "dC0yXzRJNE9LV0dpSSIsImNvZ25pdG86dXNlcm5hbWUiOiJHb29nbGVfMTEyMzk0NjYzOTg4NDU0ODY3MTM0IiwiZXhwIjoxNTg"
                + "0NTg0MDM0LCJpYXQiOjE1ODQ1ODA0MzQsImVtYWlsIjoicnZiYWJpbG9uaWFAZ21haWwuY29tIn0.NRM7goGIh3BA-2ot8lAP10"
                + "IPBz7kqA4PJrZ08FiB4i1L8SU-887fpnvKffMgqPKv_zf8HtyW1J1U3NQ9tFiy2s0y8ScACpqEFIT5bfheJP5fs2jAQA3-ivUD_"
                + "cYwfRqSCVzn6MIhjLeu3vmXnzhYZaBuQgMvCffxMrahaKOR-XwgLx7O-FBHAeiLNMhUv5x6HAdaR3NoXl6d1Zc1VXJrsp8JHbKS"
                + "JP8Pu80jh4EjXnWw223s0iiSGuNdNqTOm5EP85s-dC_JuRQ57HLLFEKU8Rhwoa7OGArWLgvhVELFU68JwjQCvvmFRc71emdQBnV"
                + "KMm8mbn9kafZw9z59lOSfig\","
                + "\"access_token\":\"eyJraWQiOiJtXC9jSTQzY01JSW53aUgwYnhpaEZKOW56eFFGUG1kR2JmUkNLNG1XeEFHST0iLCJhbGciO"
                + "iJSUzI1NiJ9.eyJzdWIiOiI1M2M2NTVhMi0zODNmLTQ0YjItODg0NS02MWNkNTFiYjgxZDAiLCJjb2duaXRvOmdyb3VwcyI6WyJ"
                + "hcC1zb3V0aGVhc3QtMl80STRPS1dHaUlfR29vZ2xlIl0sInRva2VuX3VzZSI6ImFjY2VzcyIsInNjb3BlIjoiYXdzLmNvZ25pdG"
                + "8uc2lnbmluLnVzZXIuYWRtaW4gb3BlbmlkIHByb2ZpbGUgZW1haWwiLCJhdXRoX3RpbWUiOjE1ODQ1ODA0MzQsImlzcyI6Imh0d"
                + "HBzOlwvXC9jb2duaXRvLWlkcC5hcC1zb3V0aGVhc3QtMi5hbWF6b25hd3MuY29tXC9hcC1zb3V0aGVhc3QtMl80STRPS1dHaUki"
                + "LCJleHAiOjE1ODQ1ODQwMzQsImlhdCI6MTU4NDU4MDQzNCwidmVyc2lvbiI6MiwianRpIjoiOWEzNTFhMGMtODExMC00NWI2LWE"
                + "2Y2ItNjI2NjA3M2JkMmJjIiwiY2xpZW50X2lkIjoiMXJraXRqOWZsbGxmOG1kazRsMGRqczRrbjkiLCJ1c2VybmFtZSI6Ikdvb2"
                + "dsZV8xMTIzOTQ2NjM5ODg0NTQ4NjcxMzQifQ.Xbr2OzqGMFnjiBHAojlrgXXYwVgA6VxeasUfB_rYi0A-MrOzI6CpPafUXBJmSH"
                + "CFj8GIwVluTtt_SXQesLMLoFQwfQm0c8JDDadzSvfZj50GUMJL1HPmIM4QDj-vXAPfmgkVYZQD9lt8bqK-C1bqSI4F6JsOVmwGQ"
                + "fsQ1R7EmqMFQfrir2eVYrlVxkSWjEbNoJyoMdXwEN5FeagOpOiTSs0k0YaAhspMw7hBKX6MCFfvpn2GT2Os-B3JdBTeYw74hkcZ"
                + "KEEOyi_vIu7ZVTX_DTC47GOs6eS4IcmTWg8XNT3DbPF14xlc3PuTMs48Bi8kEE9Mtw-OJu6ZwENv5n5qkg\","
                + "\"expires_in\":3600,"
                + "\"token_type\":\"Bearer\"}";
    }
}
