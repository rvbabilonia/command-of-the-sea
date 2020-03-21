package org.vincenzolabs.cots.player.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.vincenzolabs.cots.domain.Player;
import org.vincenzolabs.cots.player.configuration.PlayerConfiguration;
import org.vincenzolabs.cots.player.service.PlayerService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.util.Map;
import java.util.Set;

/**
 * The {@link RequestHandler} for retrieving players.
 *
 * @author Rey Vincent Babilonia
 */
@Component
public class RetrievePlayersRequestHandler implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RetrievePlayersRequestHandler.class);

    private static final ApplicationContext APPLICATION_CONTEXT =
            new AnnotationConfigApplicationContext(PlayerConfiguration.class);

    @Override
    public Response handleRequest(Request request, Context context) {
        PlayerService playerService = APPLICATION_CONTEXT.getBean(PlayerService.class);

        Response response = new Response();
        // enable CORS
        response.setHeaders(Map.of("Access-Control-Allow-Origin", "*"));
        response.setHeaders(Map.of("Access-Control-Allow-Credentials", "true"));
        response.setHeaders(Map.of("Access-Control-Allow-Headers", "Set-Cookie"));
        response.setHeaders(Map.of("Access-Control-Allow-Methods", "OPTIONS,POST,GET"));

        try {
            String tournament = request.getQueryStringParameters().get("tournament");

            Set<Player> player = playerService.retrievePlayers(tournament);

            response.setStatusCode(200);
            response.setBody(player.toString());
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
}
