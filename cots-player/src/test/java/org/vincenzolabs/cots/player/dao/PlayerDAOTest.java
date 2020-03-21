package org.vincenzolabs.cots.player.dao;

import by.dev.madhead.aws_junit5.common.AWSClient;
import by.dev.madhead.aws_junit5.common.AWSEndpoint;
import by.dev.madhead.aws_junit5.dynamo.v2.DynamoDB;
import com.google.gson.GsonBuilder;
import org.vincenzolabs.cots.domain.Player;
import org.vincenzolabs.cots.player.dao.impl.PlayerDAODynamoDBImpl;
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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test case for {@link PlayerDAO}.
 *
 * @author Rey Vincent Babilonia
 */
@ExtendWith(DynamoDB.class)
class PlayerDAOTest {

    private static final String PLAYER_TABLE_NAME = "player";

    @AWSClient(endpoint = Endpoint.class)
    private DynamoDbClient dynamoDbClient;

    private PlayerDAO playerDAO;

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

        playerDAO = new PlayerDAODynamoDBImpl(dynamoDbClient, new GsonBuilder().create());
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

        Player actual = playerDAO.retrievePlayerByUuid(player.getUuid());

        assertThat(actual).isEqualTo(player);
    }

    @Test
    void retrievePlayers() {
        Player player = putPlayer();

        Player.Statistics statistics = new Player.Statistics();
        statistics.setWins(BigDecimal.TEN);
        statistics.setLosses(BigDecimal.ONE);
        statistics.setDraws(BigDecimal.ONE);

        player.setStatistics(statistics);

        assertThat(playerDAO.updatePlayer(player)).isTrue();

        Set<Player> players = playerDAO.retrievePlayers(null);
        assertThat(players.size()).isEqualTo(1);
        assertThat(players.stream().findFirst().get()).isEqualTo(player);
    }

    @Test
    void retrievePlayerByNickname() {
        Player player = putPlayer();

        Player actual = playerDAO.retrievePlayerByNickname(player.getNickname());

        assertThat(actual).isEqualTo(player);
    }

    @Test
    void retrievePlayersByEmailAddress() {
        Player player = putPlayer();

        Player actual = playerDAO.retrievePlayerByEmailAddress(player.getEmailAddress());

        assertThat(actual).isEqualTo(player);
    }

    @Test
    void updatePlayer() {
        Player player = putPlayer();

        Player.Statistics statistics = new Player.Statistics();
        statistics.setWins(BigDecimal.TEN);
        statistics.setLosses(BigDecimal.ONE);
        statistics.setDraws(BigDecimal.ONE);

        player.setStatistics(statistics);

        assertThat(playerDAO.updatePlayer(player)).isTrue();

        assertThat(player.getStatistics().getWins()).isEqualTo(BigDecimal.TEN);
        assertThat(player.getStatistics().getLosses()).isEqualTo(BigDecimal.ONE);
        assertThat(player.getStatistics().getDraws()).isEqualTo(BigDecimal.ONE);
    }

    @Test
    void deletePlayer() {
        Player player = putPlayer();

        assertThat(playerDAO.deletePlayer(player.getUuid())).isTrue();
    }

    private Player putPlayer() {
        return playerDAO.createPlayer("accessToken", "rvincent", "rvbabilonia@gmail.com");
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
