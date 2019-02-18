package nz.org.vincenzo.cots.player.handler;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.util.StringUtils;
import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.player.configuration.PlayerConfiguration;
import nz.org.vincenzo.cots.player.service.PlayerService;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * The {@link RequestHandler} for player modification.
 *
 * @author Rey Vincent Babilonia
 */
@Component
public class ModificationRequestHandler implements RequestHandler<Request, Response> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ModificationRequestHandler.class);

    private static final ApplicationContext APPLICATION_CONTEXT =
            new AnnotationConfigApplicationContext(PlayerConfiguration.class);

    @Override
    public Response handleRequest(Request request, Context context) {
        PlayerService playerService = APPLICATION_CONTEXT.getBean(PlayerService.class);
        JSONParser parser = APPLICATION_CONTEXT.getBean(JSONParser.class);

        Response response = new Response();
        try {
            JSONObject body = (JSONObject) parser.parse(request.getBody());
            String nickname = (String) body.get("nickname");
            String password = (String) body.get("password");
            String passwordVerification = (String) body.get("passwordVerification");
            String avatar = (String) body.get("avatar");

            String accessToken = getAccessToken(request);
            if (StringUtils.isNullOrEmpty(accessToken)) {
                throw new IllegalArgumentException("Authorization header not found");
            }

            Player player = playerService.updatePlayer(accessToken, nickname, password, passwordVerification, avatar);

            response.setStatusCode(200);
            response.setBody(player.toString());
        } catch (IllegalArgumentException e) {
            LOGGER.error(e.getMessage(), e);

            response.setStatusCode(401);
            response.setBody(String.format(Response.ERROR_MESSAGE, e.getMessage()));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            response.setStatusCode(500);
            // FIXME
            response.setBody(String.format(Response.ERROR_MESSAGE, e.getMessage() + ", "
                    + Arrays.toString(e.getStackTrace())));
        }

        return response;
    }

    private String getAccessToken(Request request) {
        String authorization = request.getHeaders().getOrDefault("Authorization", "Bearer ");
        return authorization.substring(7);
    }
}
