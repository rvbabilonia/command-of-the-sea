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
import org.vincenzolabs.cots.player.service.PlayerService;
import software.amazon.awssdk.awscore.exception.AwsServiceException;

import java.net.HttpCookie;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The {@link RequestHandler} for deleting player's account.
 *
 * @author Rey Vincent Babilonia
 */
@Component
public class DeletePlayerRequestHandler implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeletePlayerRequestHandler.class);

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
            String refreshToken = getRefreshToken(request);
            if (StringUtils.isBlank(refreshToken)) {
                throw new IllegalArgumentException("refreshToken cookie not found");
            }

            playerService.deletePlayer(refreshToken);

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
