/*
 * This file is part of Command of the Sea.
 *
 * Copyright (c) 2019 Vincenzo
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
package nz.org.vincenzo.cots.match.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.StringUtils;
import nz.org.vincenzo.cots.domain.Match;
import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.match.config.MatchConfiguration;
import nz.org.vincenzo.cots.match.service.MatchService;
import nz.org.vincenzo.cots.match.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

/**
 * The {@link RequestHandler} to retrieve a {@link Match}.
 *
 * @author Rey Vincent Babilonia
 */
@Component
public class MatchRetrievalRequestHandler implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchRetrievalRequestHandler.class);

    private static final ApplicationContext APPLICATION_CONTEXT =
            new AnnotationConfigApplicationContext(MatchConfiguration.class);

    @Override
    public Response handleRequest(Request request, Context context) {
        MatchService matchService = APPLICATION_CONTEXT.getBean(MatchService.class);
        PlayerService playerService = APPLICATION_CONTEXT.getBean(PlayerService.class);

        Response response = new Response();
        try {
            String accessToken = getAccessToken(request);
            if (StringUtils.isNullOrEmpty(accessToken)) {
                throw new IllegalArgumentException("Authorization header not found");
            }

            Player player = playerService.retrievePlayer(accessToken);
            if (player == null) {
                throw new IllegalArgumentException("No player associated with the given access token");
            }

            String matchUuid = request.getPathParameters().get("matchUuid");

            Match match = matchService.retrieveMatch(matchUuid);
            if (match == null) {
                throw new IllegalArgumentException("Match does not exist");
            }

            response.setStatusCode(200);
            response.setBody(match.toString());
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);

            response.setStatusCode(401);
            response.setBody(String.format(Response.ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            response.setStatusCode(500);
            response.setBody(String.format(Response.ERROR_MESSAGE, e.getMessage()));
        }

        return response;
    }

    private String getAccessToken(Request request) {
        String authorization = request.getHeaders().getOrDefault("Authorization", "Bearer ");
        return authorization.substring(7);
    }
}
