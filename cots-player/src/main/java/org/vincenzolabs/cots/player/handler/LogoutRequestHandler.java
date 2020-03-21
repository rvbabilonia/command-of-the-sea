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
package org.vincenzolabs.cots.player.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.vincenzolabs.cots.player.configuration.PlayerConfiguration;
import org.vincenzolabs.cots.player.service.CognitoService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The {@link RequestHandler} for player logout.
 *
 * @author Rey Vincent Babilonia
 */
@Component
public class LogoutRequestHandler implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogoutRequestHandler.class);

    private static final ApplicationContext APPLICATION_CONTEXT =
            new AnnotationConfigApplicationContext(PlayerConfiguration.class);

    @Override
    public Response handleRequest(Request request, Context context) {
        CognitoService cognitoService = APPLICATION_CONTEXT.getBean(CognitoService.class);

        Response response = new Response();
        // configure CORS
        response.setHeaders(Map.of("Access-Control-Allow-Origin", "*"));
        response.setHeaders(Map.of("Access-Control-Allow-Credentials", "true"));
        response.setHeaders(Map.of("Access-Control-Allow-Headers", "Set-Cookie"));
        response.setHeaders(Map.of("Access-Control-Allow-Methods", "OPTIONS,POST,GET"));

        try {
            String refreshToken = getRefreshToken(request);
            if (StringUtils.isBlank(refreshToken)) {
                throw new IllegalArgumentException("refreshToken cookie not found");
            }

            String accessToken = cognitoService.getToken(CognitoService.GrantType.REFRESH_TOKEN, refreshToken)
                    .getAccessToken();

            cognitoService.signOut(accessToken);

            response.setHeaders(Map.of("Set-Cookie", "refreshToken="));
            response.setStatusCode(200);
        } catch (AwsServiceException e) {
            LOGGER.error(e.getMessage(), e);

            response.setStatusCode(e.statusCode());
            response.setBody(String.format(Response.ERROR_MESSAGE, e.getMessage()));
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);

            response.setStatusCode(400);
            response.setBody(String.format(Response.ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            response.setStatusCode(500);
            response.setBody(String.format(Response.ERROR_MESSAGE, e.getMessage()));
        }

        return response;
    }

    private String getRefreshToken(Request request) {
        List<HttpCookie> cookies = HttpCookie.parse(request.getHeaders().get("Cookie"));
        Optional<HttpCookie> cookie = cookies.stream()
                .filter(c -> "refreshToken".equals(c.getName()))
                .findFirst();

        return cookie.map(HttpCookie::getValue).orElse(null);
    }
}
