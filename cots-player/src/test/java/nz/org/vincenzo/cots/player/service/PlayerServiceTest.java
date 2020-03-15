package nz.org.vincenzo.cots.player.service;

import by.dev.madhead.aws_junit5.common.AWSClient;
import by.dev.madhead.aws_junit5.common.AWSEndpoint;
import by.dev.madhead.aws_junit5.dynamo.v2.DynamoDB;
import com.google.gson.GsonBuilder;
import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.player.dao.PlayerDAO;
import nz.org.vincenzo.cots.player.dao.impl.PlayerDAODynamoDBImpl;
import nz.org.vincenzo.cots.player.service.impl.PlayerServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.Projection;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The test case for {@link PlayerService}.
 *
 * @author Rey Vincent Babilonia
 */
@ExtendWith(DynamoDB.class)
class PlayerServiceTest {

    private static final String PLAYER_TABLE_NAME = "player";

    @AWSClient(endpoint = Endpoint.class)
    private DynamoDbClient dynamoDbClient;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName("player")
                .keySchema(KeySchemaElement.builder()
                        .attributeName("uuid")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(
                        AttributeDefinition.builder()
                                .attributeName("uuid")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("emailAddress")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("nickname")
                                .attributeType(ScalarAttributeType.S)
                                .build(),
                        AttributeDefinition.builder()
                                .attributeName("accessToken")
                                .attributeType(ScalarAttributeType.S)
                                .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                .globalSecondaryIndexes(
                        GlobalSecondaryIndex.builder()
                                .indexName("emailAddresses")
                                .keySchema(KeySchemaElement.builder()
                                        .attributeName("emailAddress")
                                        .keyType(KeyType.HASH)
                                        .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.KEYS_ONLY)
                                        .build())
                                .provisionedThroughput(ProvisionedThroughput.builder()
                                        .readCapacityUnits(5L)
                                        .writeCapacityUnits(5L)
                                        .build())
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName("nicknames")
                                .keySchema(KeySchemaElement.builder()
                                        .attributeName("nickname")
                                        .keyType(KeyType.HASH)
                                        .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.KEYS_ONLY)
                                        .build())
                                .provisionedThroughput(ProvisionedThroughput.builder()
                                        .readCapacityUnits(5L)
                                        .writeCapacityUnits(5L)
                                        .build())
                                .build(),
                        GlobalSecondaryIndex.builder()
                                .indexName("accessTokens")
                                .keySchema(KeySchemaElement.builder()
                                        .attributeName("accessToken")
                                        .keyType(KeyType.HASH)
                                        .build())
                                .projection(Projection.builder()
                                        .projectionType(ProjectionType.KEYS_ONLY)
                                        .build())
                                .provisionedThroughput(ProvisionedThroughput.builder()
                                        .readCapacityUnits(5L)
                                        .writeCapacityUnits(5L)
                                        .build())
                                .build())
                .build();

        dynamoDbClient.createTable(createTableRequest);

        PlayerDAO playerDAO = new PlayerDAODynamoDBImpl(dynamoDbClient, new GsonBuilder().create());

        playerService = new PlayerServiceImpl(playerDAO);
    }

    @AfterEach
    void tearDown() {
        DeleteTableRequest deleteTableRequest = DeleteTableRequest.builder()
                .tableName(PLAYER_TABLE_NAME)
                .build();

        dynamoDbClient.deleteTable(deleteTableRequest);
    }

    @Test
    void createPlayer() {
        Player actual = putPlayer();

        assertThat(actual)
                .extracting("emailAddress", "nickname")
                .containsExactly("rvbabilonia@gmail.com", "rvincent");
        assertThat(actual.getUuid()).isNotBlank();
        assertThat(actual.getRegistrationDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void retrievePlayerByUuid() {
        Player player = putPlayer();

        Player actual = playerService.retrievePlayerByUuid(player.getUuid());

        assertThat(actual).isEqualTo(player);
    }

    @Test
    void loginAndRetrievePlayerByAccessToken() {
        putPlayer();

        String accessToken = playerService.login("rvbabilonia@gmail.com", UUID.randomUUID().toString());

        Player actual = playerService.retrievePlayerByAccessToken(accessToken);
        assertThat(actual.getAccessToken()).isEqualTo(accessToken);
        assertThat(actual.getLastLoginDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void retrievePlayerByNickname() {
        Player player = putPlayer();

        Player actual = playerService.retrievePlayerByNickname(player.getNickname());

        assertThat(actual).isEqualTo(player);
    }

    @Test
    void retrievePlayersByEmailAddress() {
        Player player = putPlayer();

        Player actual = playerService.retrievePlayerByEmailAddress(player.getEmailAddress());

        assertThat(actual).isEqualTo(player);
    }

    @Test
    void retrievePlayers() {
        Player player = putPlayer();

        Set<Player> players = playerService.retrievePlayers();
        assertThat(players.size()).isEqualTo(1);
        assertThat(players.stream().findFirst().get()).isEqualTo(player);
    }

    @Test
    void loginAndLogout() {
        putPlayer();

        String accessToken = playerService.login("rvbabilonia@gmail.com", UUID.randomUUID().toString());

        Player actual = playerService.retrievePlayerByAccessToken(accessToken);
        assertThat(actual.getAccessToken()).isEqualTo(accessToken);
        assertThat(actual.getLastLoginDate()).isBeforeOrEqualTo(LocalDateTime.now());

        playerService.logout(accessToken);

        assertThatThrownBy(() -> playerService.retrievePlayerByAccessToken(accessToken))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No player associated with the given access token");
    }

    @Test
    void updateStatistics() {
        Player player = putPlayer();

        Player actual = playerService.updateStatistics(player, "win");
        assertThat(actual.getStatistics())
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

        actual = playerService.updateStatistics(player, "win");
        assertThat(actual.getStatistics())
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ZERO);

        actual = playerService.updateStatistics(player, "lose");
        assertThat(actual.getStatistics())
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ONE);

        actual = playerService.updateStatistics(player, "draw");
        assertThat(actual.getStatistics())
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test
    void updateTournamentStatistics() {
        Player player = putPlayer();

        Player actual = playerService.updateTournamentStatistics(player, "win", "RIMPAC Cup 2020");
        assertThat(actual.getTournamentStatistics().get("RIMPAC Cup 2020"))
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

        actual = playerService.updateTournamentStatistics(player, "win", "RIMPAC Cup 2020");
        assertThat(actual.getTournamentStatistics().get("RIMPAC Cup 2020"))
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ZERO);

        actual = playerService.updateTournamentStatistics(player, "lose", "RIMPAC Cup 2020");
        assertThat(actual.getTournamentStatistics().get("RIMPAC Cup 2020"))
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ONE);

        actual = playerService.updateTournamentStatistics(player, "draw", "RIMPAC Cup 2020");
        assertThat(actual.getTournamentStatistics().get("RIMPAC Cup 2020"))
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test
    void updateAvatar() {
        putPlayer();

        String accessToken = playerService.login("rvbabilonia@gmail.com", UUID.randomUUID().toString());

        Player actual = playerService.retrievePlayerByAccessToken(accessToken);
        assertThat(actual.getAccessToken()).isEqualTo(accessToken);
        assertThat(actual.getLastLoginDate()).isBeforeOrEqualTo(LocalDateTime.now());

        actual = playerService.updateAvatar(accessToken, "avatar.png");
        assertThat(actual.getAvatar()).isEqualTo("avatar.png");
    }

    private Player putPlayer() {
        return playerService.createPlayer("rvincent", "rvbabilonia@gmail.com");
    }

    /**
     * The implementation of {@link AWSEndpoint}.
     */
    public static class Endpoint implements AWSEndpoint {
        @Override
        public String url() {
            return System.getenv("DYNAMODB_URL");
        }

        @Override
        public String region() {
            return System.getenv("DYNAMODB_REGION");
        }

        @Override
        public String accessKey() {
            return System.getenv("DYNAMODB_ACCESS_KEY");
        }

        @Override
        public String secretKey() {
            return System.getenv("DYNAMODB_SECRET_KEY");
        }
    }
}
