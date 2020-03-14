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
package nz.org.vincenzo.cots.match.dao;

import by.dev.madhead.aws_junit5.common.AWSClient;
import by.dev.madhead.aws_junit5.common.AWSEndpoint;
import by.dev.madhead.aws_junit5.dynamo.v2.DynamoDB;
import com.google.gson.GsonBuilder;
import nz.org.vincenzo.cots.domain.Match;
import nz.org.vincenzo.cots.domain.Ship;
import nz.org.vincenzo.cots.match.dao.impl.MatchDAODynamoDBImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DeleteTableRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test case for {@link MatchDAO}.
 *
 * @author Rey Vincent Babilonia
 */
@ExtendWith(DynamoDB.class)
class MatchDAOTest {

    private static final String MATCH_TABLE_NAME = "match";

    @AWSClient(endpoint = Endpoint.class)
    private DynamoDbClient dynamoDbClient;

    private MatchDAO matchDAO;

    @BeforeEach
    void setUp() {
        CreateTableRequest createTableRequest = CreateTableRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .keySchema(KeySchemaElement.builder()
                        .attributeName("uuid")
                        .keyType(KeyType.HASH)
                        .build())
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("uuid")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(5L)
                        .writeCapacityUnits(5L)
                        .build())
                .build();

        dynamoDbClient.createTable(createTableRequest);

        matchDAO = new MatchDAODynamoDBImpl(dynamoDbClient, new GsonBuilder().create());
    }

    @AfterEach
    void tearDown() {
        DeleteTableRequest deleteTableRequest = DeleteTableRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .build();

        dynamoDbClient.deleteTable(deleteTableRequest);
    }

    @Test
    void createMatch() {
        String playerUuid = UUID.randomUUID().toString();

        Match actual = matchDAO.createMatch(playerUuid);

        assertThat(actual.getUuid()).isNotBlank();
        if (actual.getWhitePlayer() == null) {
            assertThat(actual.getBlackPlayer()).isEqualTo(playerUuid);
        } else if (actual.getBlackPlayer() == null) {
            assertThat(actual.getWhitePlayer()).isEqualTo(playerUuid);
        }
        assertThat(actual.getWinner()).isNull();
        assertThat(actual.getLoser()).isNull();
        assertThat(actual.getHost()).isEqualTo(playerUuid);
        assertThat(actual.getCreationDate()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(actual.getTurn()).isNull();
        assertThat(actual.hasStarted()).isFalse();
        assertThat(actual.getStartDate()).isNull();
        assertThat(actual.getEndDate()).isNull();
        assertThat(actual.isWhitePlayerReady()).isFalse();
        assertThat(actual.isBlackPlayerReady()).isFalse();
        assertThat(actual.getMoves()).isEmpty();
    }

    @Test
    void retrieveMatch() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);

        Match actual = matchDAO.retrieveMatch(expected.getUuid());

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void retrieveFinishedMatches() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);
        expected.setStarted(true);
        expected.setWinner(playerUuid);
        expected.setEndDate(LocalDateTime.now().minusHours(1));
        matchDAO.updateMatch(expected);

        Set<Match> matches = matchDAO.retrieveFinishedMatches();
        assertThat(matches).hasSize(1)
                .first()
                .extracting("winner", "draw")
                .containsExactly(playerUuid, false);
    }

    @Test
    void retrieveFinishedDrawMatches() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);
        expected.setStarted(true);
        expected.setDraw(true);
        expected.setEndDate(LocalDateTime.now().minusHours(1));
        matchDAO.updateMatch(expected);

        Set<Match> matches = matchDAO.retrieveFinishedMatches();
        assertThat(matches).hasSize(1)
                .first()
                .extracting("winner", "draw")
                .containsExactly(null, true);
    }

    @Test
    void retrieveActiveMatches() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);
        expected.setStarted(true);
        expected.setStartDate(LocalDateTime.now());
        matchDAO.updateMatch(expected);

        Set<Match> matches = matchDAO.retrieveActiveMatches();
        assertThat(matches).hasSize(1)
                .first()
                .extracting("winner", "draw", "started")
                .containsExactly(null, false, true);
    }

    @Test
    void retrieveUnstartedMatches() {
        String playerUuid = UUID.randomUUID().toString();

        matchDAO.createMatch(playerUuid);

        Set<Match> matches = matchDAO.retrieveUnstartedMatches();
        assertThat(matches).hasSize(1)
                .first()
                .extracting("host", "started")
                .containsExactly(playerUuid, false);
    }

    @Test
    void updateMatch() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);
        assertThat(expected.getCreationDate()).isBeforeOrEqualTo(LocalDateTime.now());

        expected.setStarted(true);
        expected.setStartDate(LocalDateTime.now());

        String whitePlayer = expected.getWhitePlayer();
        if (StringUtils.isBlank(whitePlayer)) {
            whitePlayer = UUID.randomUUID().toString();
            expected.setWhitePlayer(whitePlayer);
        }

        String blackPlayer = expected.getBlackPlayer();
        if (StringUtils.isBlank(blackPlayer)) {
            blackPlayer = UUID.randomUUID().toString();
            expected.setBlackPlayer(blackPlayer);
        }
        expected.setWhitePlayerReady(true);
        expected.setBlackPlayerReady(true);

        Ship whiteShip = new Ship();
        whiteShip.setColor(Ship.Color.WHITE);
        whiteShip.setShipClass(Ship.ShipClass.AMERICA_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        whiteShip.setCoordinates(new Ship.Coordinates(3, 3));
        expected.setFleets(Map.of(whitePlayer, Collections.singleton(whiteShip)));

        assertThat(matchDAO.updateMatch(expected)).isTrue();

        Ship blackShip = new Ship();
        blackShip.setColor(Ship.Color.BLACK);
        blackShip.setShipClass(Ship.ShipClass.GERALD_FORD_CLASS_AIRCRAFT_CARRIER);
        blackShip.setCoordinates(new Ship.Coordinates(6, 6));
        expected.setFleets(Map.of(blackPlayer, Set.of(blackShip)));

        assertThat(matchDAO.updateMatch(expected)).isTrue();

        Match actual = matchDAO.retrieveMatch(expected.getUuid());

        assertThat(actual)
                .extracting("uuid", "whitePlayer", "blackPlayer", "draw",
                        "whitePlayerAgreedToDraw", "blackPlayerAgreedToDraw", "host", "started",
                        "whitePlayerReady", "blackPlayerReady", "fleets")
                .containsExactly(expected.getUuid(), whitePlayer, blackPlayer, false,
                        false, false, playerUuid, true,
                        true, true, Collections.emptyMap());
        assertThat(actual.getMoves()).hasSize(2);

        assertThat(actual.getMoves().get(0)).isEqualTo(Map.of(whitePlayer, Set.of(whiteShip)));
        assertThat(actual.getMoves().get(1)).isEqualTo(Map.of(blackPlayer, Set.of(blackShip)));
    }

    @Test
    void deleteMatch() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);

        assertThat(matchDAO.deleteMatch(expected)).isTrue();
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
