package org.vincenzolabs.cots.player.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import org.vincenzolabs.cots.domain.Player;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;
import org.vincenzolabs.cots.player.configuration.PlayerConfiguration;
import org.vincenzolabs.cots.player.service.PlayerService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

/**
 * The {@link RequestHandler} for updating player statistics. This lambda function is not exposed to the API gateway
 * and is used only by match module.
 *
 * @author Rey Vincent Babilonia
 */
@Component
public class UpdateStatisticsRequestHandler implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateStatisticsRequestHandler.class);

    private static final ApplicationContext APPLICATION_CONTEXT =
            new AnnotationConfigApplicationContext(PlayerConfiguration.class);

    @Override
    public Response handleRequest(Request request, Context context) {
        PlayerService playerService = APPLICATION_CONTEXT.getBean(PlayerService.class);
        JSONParser parser = APPLICATION_CONTEXT.getBean(JSONParser.class);
        Gson gson = APPLICATION_CONTEXT.getBean(Gson.class);

        Response response = new Response();
        try {
            JSONObject body = (JSONObject) parser.parse(request.getBody());
            String playerUuid = (String) body.get("playerUuid");
            PlayerService.Result result = gson.fromJson((String) body.get("result"), PlayerService.Result.class);
            String tournament = (String) body.get("tournament");

            Player player = playerService.retrievePlayerByUuid(playerUuid);

            if (StringUtils.isNotBlank(tournament)) {
                player = playerService.updateStatistics(player, result, tournament);
            } else {
                player = playerService.updateStatistics(player, result);
            }

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
