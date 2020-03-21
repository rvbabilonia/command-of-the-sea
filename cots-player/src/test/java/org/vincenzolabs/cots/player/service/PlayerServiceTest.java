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

import by.dev.madhead.aws_junit5.common.AWSClient;
import by.dev.madhead.aws_junit5.common.AWSEndpoint;
import by.dev.madhead.aws_junit5.dynamo.v2.DynamoDB;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vincenzolabs.cots.domain.Player;
import org.vincenzolabs.cots.domain.Token;
import org.vincenzolabs.cots.domain.UserInformation;
import org.vincenzolabs.cots.player.dao.PlayerDAO;
import org.vincenzolabs.cots.player.dao.impl.PlayerDAODynamoDBImpl;
import org.vincenzolabs.cots.player.service.impl.PlayerServiceImpl;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link PlayerService}.
 *
 * @author Rey Vincent Babilonia
 */
@ExtendWith({DynamoDB.class, MockitoExtension.class})
class PlayerServiceTest {

    private static final String PLAYER_TABLE_NAME = "player";

    @AWSClient(endpoint = Endpoint.class)
    private DynamoDbClient dynamoDbClient;

    private PlayerDAO playerDAO;

    private CognitoService cognitoService;

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
                                .attributeName("refreshToken")
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
                                .indexName("refreshTokens")
                                .keySchema(KeySchemaElement.builder()
                                        .attributeName("refreshToken")
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

        cognitoService = mock(CognitoService.class);

        playerService = new PlayerServiceImpl(playerDAO, cognitoService);
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
        LocalDateTime now = LocalDateTime.now();
        assertThat(actual.getRegistrationDate()).isBeforeOrEqualTo(now);
        assertThat(actual.getLastLoginDate()).isBeforeOrEqualTo(now);
    }

    @Test
    void retrievePlayerByUuid() {
        Player player = putPlayer();

        Player actual = playerService.retrievePlayerByUuid(player.getUuid());

        assertThat(actual).isEqualTo(player);
    }

    @Test
    void loginAndRetrievePlayerByRefreshToken() {
        Player player = putPlayer();

        Token token = mock(Token.class);
        when(token.getAccessToken()).thenReturn("accessToken");

        when(cognitoService.getToken(eq(CognitoService.GrantType.REFRESH_TOKEN), eq("refreshToken"))).thenReturn(token);
        when(cognitoService.getUserInformation(any(String.class))).thenReturn(UserInformation.builder()
                .withSubject(player.getUuid())
                .withEmailAddress(player.getEmailAddress())
                .build());

        // simulate login from Cognito
        Player actual = playerService.retrievePlayer("refreshToken");
        assertThat(actual.getNickname()).isEqualTo("rvincent");
        assertThat(actual.getEmailAddress()).isEqualTo("rvbabilonia@gmail.com");
        assertThat(actual.getLastLoginDate()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    void loginAndCreateDummyPlayer() {
        Token token = mock(Token.class);
        when(token.getAccessToken()).thenReturn("accessToken");

        when(cognitoService.getToken(eq(CognitoService.GrantType.REFRESH_TOKEN), eq("refreshToken"))).thenReturn(token);
        String subject = UUID.randomUUID().toString();
        String emailAddress = "rvbabilonia@yahoo.com";
        String username = "Yahoo_1234567890";
        when(cognitoService.getUserInformation(any(String.class))).thenReturn(UserInformation.builder()
                .withSubject(subject)
                .withEmailAddress(emailAddress)
                .withUsername(username)
                .build());

        // simulate login from Cognito
        Player actual = playerService.retrievePlayer("refreshToken");
        assertThat(actual.getNickname()).isEqualTo(username);
        assertThat(actual.getEmailAddress()).isEqualTo(emailAddress);
        assertThat(actual.getUuid()).isEqualTo(subject);
        assertThat(actual.getLastLoginDate()).isNull();
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

        playerService.updateStatistics(player, PlayerService.Result.WIN);

        Set<Player> players = playerService.retrievePlayers(null);
        assertThat(players.size()).isEqualTo(1);
        assertThat(players.stream().findFirst().get()).isEqualTo(player);

        playerService.updateStatistics(player, PlayerService.Result.WIN, "RIMPAC Cup 2020");

        players = playerService.retrievePlayers("tournament");
        assertThat(players.size()).isEqualTo(1);
        assertThat(players.stream().findFirst().get()).isEqualTo(player);
    }

    @Test
    void loginAndLogout() {
        Player player = putPlayer();

        Token token = mock(Token.class);
        when(token.getAccessToken()).thenReturn("accessToken");

        when(cognitoService.getToken(eq(CognitoService.GrantType.REFRESH_TOKEN), eq("refreshToken"))).thenReturn(token);
        when(cognitoService.getUserInformation(any(String.class))).thenReturn(UserInformation.builder()
                .withSubject(player.getUuid())
                .withEmailAddress(player.getEmailAddress())
                .build());

        // simulate login from Cognito
        Player actual = playerService.retrievePlayer("refreshToken");
        assertThat(actual.getLastLoginDate()).isBeforeOrEqualTo(LocalDateTime.now());

        // logout
        playerService.logout("refreshToken");

        when(cognitoService.getToken(eq(CognitoService.GrantType.REFRESH_TOKEN), eq("refreshToken")))
                .thenThrow(AwsServiceException.builder().statusCode(400).message("invalid_grant").build());

        assertThatThrownBy(() -> playerService.retrievePlayer("refreshToken"))
                .isInstanceOf(AwsServiceException.class)
                .hasMessage("invalid_grant");
    }

    @Test
    void updateStatistics() {
        Player player = putPlayer();

        Player actual = playerService.updateStatistics(player, PlayerService.Result.WIN);
        assertThat(actual.getStatistics())
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

        actual = playerService.updateStatistics(player, PlayerService.Result.WIN);
        assertThat(actual.getStatistics())
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ZERO);

        actual = playerService.updateStatistics(player, PlayerService.Result.LOSE);
        assertThat(actual.getStatistics())
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ONE);

        actual = playerService.updateStatistics(player, PlayerService.Result.DRAW);
        assertThat(actual.getStatistics())
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test
    void updateTournamentStatistics() {
        Player player = putPlayer();

        Player actual = playerService.updateStatistics(player, PlayerService.Result.WIN, "RIMPAC Cup 2020");
        assertThat(actual.getTournamentStatistics().get("RIMPAC Cup 2020"))
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ZERO);

        actual = playerService.updateStatistics(player, PlayerService.Result.WIN, "RIMPAC Cup 2020");
        assertThat(actual.getTournamentStatistics().get("RIMPAC Cup 2020"))
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ZERO);

        actual = playerService.updateStatistics(player, PlayerService.Result.LOSE, "RIMPAC Cup 2020");
        assertThat(actual.getTournamentStatistics().get("RIMPAC Cup 2020"))
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ZERO, BigDecimal.ONE);

        actual = playerService.updateStatistics(player, PlayerService.Result.DRAW, "RIMPAC Cup 2020");
        assertThat(actual.getTournamentStatistics().get("RIMPAC Cup 2020"))
                .extracting("wins", "draws", "losses")
                .containsExactly(BigDecimal.valueOf(2), BigDecimal.ONE, BigDecimal.ONE);
    }

    @Test
    void updateAvatar() {
        Player player = putPlayer();

        Token token = mock(Token.class);
        when(token.getAccessToken()).thenReturn("accessToken");

        when(cognitoService.getToken(eq(CognitoService.GrantType.REFRESH_TOKEN), eq("refreshToken"))).thenReturn(token);
        when(cognitoService.getUserInformation(any(String.class))).thenReturn(UserInformation.builder()
                .withSubject(player.getUuid())
                .withEmailAddress(player.getEmailAddress())
                .build());

        // simulate login from Cognito
        Player actual = playerService.retrievePlayer("refreshToken");
        assertThat(actual.getLastLoginDate()).isBeforeOrEqualTo(LocalDateTime.now());

        actual = playerService.updateAvatar("refreshToken", "avatar.png");
        assertThat(actual.getAvatar()).isEqualTo("avatar.png");
    }

    @Test
    void deletePlayer() {
        Player player = putPlayer();

        Token token = mock(Token.class);
        when(token.getAccessToken()).thenReturn("accessToken");

        when(cognitoService.getToken(eq(CognitoService.GrantType.REFRESH_TOKEN), eq("refreshToken"))).thenReturn(token);
        when(cognitoService.getUserInformation(any(String.class))).thenReturn(UserInformation.builder()
                .withSubject(player.getUuid())
                .withEmailAddress(player.getEmailAddress())
                .build());

        // simulate login from Cognito
        Player actual = playerService.retrievePlayer("refreshToken");
        assertThat(actual.getLastLoginDate()).isBeforeOrEqualTo(LocalDateTime.now());

        playerService.deletePlayer("refreshToken");

        assertThat(playerService.retrievePlayerByUuid(player.getUuid())).isNull();
    }

    private Player putPlayer() {
        return playerDAO.createPlayer(UUID.randomUUID().toString(), "rvincent", "rvbabilonia@gmail.com");
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
