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
package org.vincenzolabs.cots.match.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.vincenzolabs.cots.domain.Match;
import org.vincenzolabs.cots.domain.Player;
import org.vincenzolabs.cots.match.configuration.MatchConfiguration;
import org.vincenzolabs.cots.match.service.MatchService;
import org.vincenzolabs.cots.match.service.PlayerService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The {@link RequestHandler} to handle player operations during a {@link Match}.
 *
 * @author Rey Vincent Babilonia
 */
@Component
public class HandleMatchRequestHandler implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HandleMatchRequestHandler.class);

    private static final ApplicationContext APPLICATION_CONTEXT =
            new AnnotationConfigApplicationContext(MatchConfiguration.class);

    @Override
    public Response handleRequest(Request request, Context context) {
        MatchService matchService = APPLICATION_CONTEXT.getBean(MatchService.class);
        PlayerService playerService = APPLICATION_CONTEXT.getBean(PlayerService.class);
        Gson gson = APPLICATION_CONTEXT.getBean(Gson.class);

        Response response = new Response();
        // enable CORS
        response.setHeaders(Map.of("Access-Control-Allow-Origin", "*"));
        response.setHeaders(Map.of("Access-Control-Allow-Credentials", "true"));
        response.setHeaders(Map.of("Access-Control-Allow-Headers", "Set-Cookie"));
        response.setHeaders(Map.of("Access-Control-Allow-Methods", "OPTIONS,POST,GET"));

        try {
            String refreshToken = getRefreshToken(request);
            if (StringUtils.isBlank(refreshToken)) {
                throw new IllegalArgumentException("refreshToken cookie not found");
            }

            Player player = playerService.retrievePlayer(refreshToken);
            if (player == null) {
                throw new IllegalArgumentException("Not logged in");
            }
            String playerUuid = player.getUuid();

            String matchUuid = request.getPathParameters().get("matchUuid");

            Match match = matchService.retrieveMatch(matchUuid);
            if (match == null) {
                throw new IllegalArgumentException("Match does not exist");
            }

            MatchService.Operation operation = gson.fromJson(request.getBody(), MatchService.Operation.class);
            switch (operation) {
                case CONNECT:
                    match = matchService.connectToMatch(playerUuid, matchUuid);
                    break;
                case DISCONNECT:
                    matchService.disconnectFromMatch(playerUuid, matchUuid);
                    break;
                case READY:
                    match = matchService.ready(playerUuid, matchUuid);
                    break;
                case START:
                    match = matchService.startMatch(playerUuid, matchUuid);
                    break;
                case RESIGN:
                    match = matchService.resign(playerUuid, matchUuid);
                    break;
                case DRAW:
                    match = matchService.draw(player.getUuid(), matchUuid);
                    break;
            }

            response.setStatusCode(200);
            response.setBody(match.toString());
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
