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
package org.vincenzolabs.cots.match.service;

import by.dev.madhead.aws_junit5.common.AWSClient;
import by.dev.madhead.aws_junit5.common.AWSEndpoint;
import by.dev.madhead.aws_junit5.dynamo.v2.DynamoDB;
import com.google.gson.GsonBuilder;
import org.vincenzolabs.cots.domain.Match;
import org.vincenzolabs.cots.domain.Ship;
import org.vincenzolabs.cots.match.dao.MatchDAO;
import org.vincenzolabs.cots.match.dao.impl.MatchDAODynamoDBImpl;
import org.vincenzolabs.cots.match.service.impl.ArbitrationServiceImpl;
import org.vincenzolabs.cots.match.service.impl.MatchServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The test case for {@link MatchService}.
 *
 * @author Rey Vincent Babilonia
 */
@ExtendWith(DynamoDB.class)
class MatchServiceTest {

    private static final String MATCH_TABLE_NAME = "match";

    @AWSClient(endpoint = Endpoint.class)
    private DynamoDbClient dynamoDbClient;

    private MatchService matchService;

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

        MatchDAO matchDAO = new MatchDAODynamoDBImpl(dynamoDbClient, new GsonBuilder().create());

        matchService = new MatchServiceImpl(new ArbitrationServiceImpl(), matchDAO);
    }

    @AfterEach
    void tearDown() {
        DeleteTableRequest deleteTableRequest = DeleteTableRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .build();

        dynamoDbClient.deleteTable(deleteTableRequest);
    }

    @Test
    void hostMatchWithoutPlayerUuid() {
        assertThatThrownBy(() -> matchService.hostMatch(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player UUID must not be null or empty");
    }

    @RepeatedTest(4)
    void hostMatch() {
        String host = UUID.randomUUID().toString();

        Match actual = matchService.hostMatch(host);

        assertThat(actual.getUuid()).isNotBlank();
        if (StringUtils.isNotBlank(actual.getWhitePlayer())) {
            assertThat(actual.getWhitePlayer()).isEqualTo(host);
            assertThat(actual.getBlackPlayer()).isNull();
        } else {
            assertThat(actual.getBlackPlayer()).isEqualTo(host);
            assertThat(actual.getWhitePlayer()).isNull();
        }
        assertThat(actual.getWinner()).isNull();
        assertThat(actual.getLoser()).isNull();
        assertThat(actual.getHost()).isEqualTo(host);
        assertThat(actual.getCreationDate()).isBeforeOrEqualTo(LocalDateTime.now());
        assertThat(actual.hasStarted()).isFalse();
        assertThat(actual.getStartDate()).isNull();
        assertThat(actual.getEndDate()).isNull();
        assertThat(actual.isWhitePlayerReady()).isFalse();
        assertThat(actual.isBlackPlayerReady()).isFalse();
        assertThat(actual.getMoves()).isEmpty();
        assertThat(actual.getTurn()).isNull();
    }

    @Test
    void connectToMatchWithoutPlayerUuid() {
        String host = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        assertThatThrownBy(() -> matchService.connectToMatch("", match.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player UUID must not be null or empty");
    }

    @Test
    void connectToMatchWithoutMatchUuid() {
        String host = UUID.randomUUID().toString();

        assertThatThrownBy(() -> matchService.connectToMatch(host, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Match UUID must not be null or empty");
    }

    @Test
    void connectToMatchWithInvalidMatchUuid() {
        String host = UUID.randomUUID().toString();

        assertThatThrownBy(() -> matchService.connectToMatch(host, "uuid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Match UUID is invalid");
    }

    @RepeatedTest(4)
    void connectToMatch() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        Match expected = matchService.connectToMatch(guest, match.getUuid());
        if (expected.getWhitePlayer().equals(host)) {
            assertThat(expected.getBlackPlayer()).isEqualTo(guest);
        } else {
            assertThat(expected.getBlackPlayer()).isEqualTo(host);
        }
    }

    @RepeatedTest(4)
    void connectToMatchThatHasStarted() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        assertThat(actual.getFleets()).hasSize(2);

        assertThatThrownBy(() -> matchService.connectToMatch(UUID.randomUUID().toString(), actual.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Match has already started");
    }

    @RepeatedTest(4)
    void positionWhiteFleet() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        Ship ship1 = new Ship();
        ship1.setColor(Ship.Color.WHITE);
        ship1.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
        ship1.setCoordinates(new Ship.Coordinates(1, 0));
        Map<String, Set<Ship>> fleets = matchService.positionShip(match.getWhitePlayer(), match.getUuid(), ship1);

        for (Ship whiteShip : fleets.get(match.getWhitePlayer())) {
            if (Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP == whiteShip.getShipClass()) {
                assertThat(whiteShip.getCoordinates())
                        .extracting("x", "y")
                        .containsExactly(1, 0);
                break;
            }
        }

        for (Ship blackShip : fleets.get(match.getBlackPlayer())) {
            assertThat(blackShip.getShipClass()).isEqualTo(Ship.ShipClass.UNKNOWN);
        }

        Ship ship2 = new Ship();
        ship2.setColor(Ship.Color.BLACK);
        ship2.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
        ship2.setCoordinates(new Ship.Coordinates(4, 7));

        fleets = matchService.positionShip(match.getBlackPlayer(), match.getUuid(), ship2);

        for (Ship whiteShip : fleets.get(match.getWhitePlayer())) {
            assertThat(whiteShip.getShipClass()).isEqualTo(Ship.ShipClass.UNKNOWN);

            if (whiteShip.getCoordinates().getX() != -1 || whiteShip.getCoordinates().getY() != -1) {
                assertThat(whiteShip.getCoordinates())
                        .extracting("x", "y")
                        .containsExactly(1, 0);
            }
        }

        for (Ship blackShip : fleets.get(match.getBlackPlayer())) {
            if (Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP == blackShip.getShipClass()) {
                assertThat(blackShip.getCoordinates()).isEqualTo(new Ship.Coordinates(4, 7));
                break;
            }
        }
    }

    @RepeatedTest(4)
    void readyWithoutShips() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        matchService.connectToMatch(guest, match.getUuid());

        assertThatThrownBy(() -> matchService.ready(host, match.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("All ships must be positioned");
    }

    @RepeatedTest(4)
    void readyWithUnpositionedShips() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        matchService.connectToMatch(guest, match.getUuid());

        Ship ship1 = new Ship();
        ship1.setColor(Ship.Color.WHITE);
        ship1.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
        ship1.setCoordinates(new Ship.Coordinates(1, 0));

        matchService.positionShip(host, match.getUuid(), ship1);

        assertThatThrownBy(() -> matchService.ready(host, match.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("must be positioned");
    }

    @RepeatedTest(4)
    void ready() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());
    }

    @RepeatedTest(4)
    void startMatch() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        assertThat(actual.getFleets()).hasSize(2);
    }

    @RepeatedTest(4)
    void startAlreadyStartedMatch() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        assertThat(actual.getFleets()).hasSize(2);

        assertThatThrownBy(() -> matchService.startMatch(host, actual.getUuid()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Match has already started");
    }

    @RepeatedTest(4)
    void moveShip() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        Ship ship = new Ship();
        if (actual.getTurn() == Ship.Color.WHITE) {
            ship.setColor(Ship.Color.WHITE);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(0, 3));

            Map<String, Set<Ship>> whiteFleet = matchService.moveShip(actual.getWhitePlayer(), actual.getUuid(), ship);
            assertThat(whiteFleet.get(host)).hasSize(21);
        } else {
            ship.setColor(Ship.Color.BLACK);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(0, 4));

            Map<String, Set<Ship>> blackFleet = matchService.moveShip(actual.getBlackPlayer(), actual.getUuid(), ship);
            assertThat(blackFleet.get(guest)).hasSize(21);
        }
    }

    @RepeatedTest(4)
    void moveShipWhileNotYetTurn() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        Ship ship = new Ship();
        if (actual.getTurn() != Ship.Color.WHITE) {
            ship.setColor(Ship.Color.WHITE);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(0, 3));

            assertThatThrownBy(() -> matchService.moveShip(actual.getWhitePlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Black player has the turn");
        } else {
            ship.setColor(Ship.Color.BLACK);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(0, 4));

            assertThatThrownBy(() -> matchService.moveShip(actual.getBlackPlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("White player has the turn");
        }
    }

    @RepeatedTest(4)
    void moveShipOutOfTheXAxis() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        Ship ship = new Ship();
        if (actual.getTurn() == Ship.Color.WHITE) {
            ship.setColor(Ship.Color.WHITE);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(-1, 2));

            assertThatThrownBy(() -> matchService.moveShip(actual.getWhitePlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("X coordinate is invalid for NIMITZ_SUBCLASS_AIRCRAFT_CARRIER");
        } else {
            ship.setColor(Ship.Color.BLACK);
            ship.setShipClass(Ship.ShipClass.THEODORE_ROOSEVELT_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(9, 5));

            assertThatThrownBy(() -> matchService.moveShip(actual.getBlackPlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("X coordinate is invalid for THEODORE_ROOSEVELT_SUBCLASS_AIRCRAFT_CARRIER");
        }
    }

    @RepeatedTest(4)
    void moveShipOutOfTheYAxis() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        Ship ship = new Ship();
        if (actual.getTurn() == Ship.Color.WHITE) {
            ship.setColor(Ship.Color.WHITE);
            ship.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
            ship.setCoordinates(new Ship.Coordinates(1, -1));

            assertThatThrownBy(() -> matchService.moveShip(actual.getWhitePlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Y coordinate is invalid for BLUE_RIDGE_CLASS_COMMAND_SHIP");
        } else {
            ship.setColor(Ship.Color.BLACK);
            ship.setShipClass(Ship.ShipClass.TICONDEROGA_CLASS_GUIDED_MISSILE_CRUISER);
            ship.setCoordinates(new Ship.Coordinates(7, 8));

            assertThatThrownBy(() -> matchService.moveShip(actual.getBlackPlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Y coordinate is invalid for TICONDEROGA_CLASS_GUIDED_MISSILE_CRUISER");
        }
    }

    @RepeatedTest(4)
    void moveShipOnToAnOccupiedTile() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        Ship ship = new Ship();
        if (actual.getTurn() == Ship.Color.WHITE) {
            ship.setColor(Ship.Color.WHITE);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(0, 1));

            assertThatThrownBy(() -> matchService.moveShip(actual.getWhitePlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Coordinates is already occupied");
        } else {
            ship.setColor(Ship.Color.BLACK);
            ship.setShipClass(Ship.ShipClass.THEODORE_ROOSEVELT_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(8, 6));

            assertThatThrownBy(() -> matchService.moveShip(actual.getBlackPlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Coordinates is already occupied");
        }
    }

    @RepeatedTest(4)
    void shipDidNotMove() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        Ship ship = new Ship();
        if (actual.getTurn() == Ship.Color.WHITE) {
            ship.setColor(Ship.Color.WHITE);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(0, 2));

            assertThatThrownBy(() -> matchService.moveShip(actual.getWhitePlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageEndingWith("did not move");
        } else {
            ship.setColor(Ship.Color.BLACK);
            ship.setShipClass(Ship.ShipClass.THEODORE_ROOSEVELT_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(8, 5));

            assertThatThrownBy(() -> matchService.moveShip(actual.getBlackPlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageEndingWith("did not move");
        }
    }

    @RepeatedTest(4)
    void moveShipMoreThanOneTile() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        Ship ship = new Ship();
        if (actual.getTurn() == Ship.Color.WHITE) {
            ship.setColor(Ship.Color.WHITE);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(0, 4));

            assertThatThrownBy(() -> matchService.moveShip(actual.getWhitePlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageEndingWith("Y coordinate is invalid");
        } else {
            ship.setColor(Ship.Color.BLACK);
            ship.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
            ship.setCoordinates(new Ship.Coordinates(0, 3));

            assertThatThrownBy(() -> matchService.moveShip(actual.getBlackPlayer(), actual.getUuid(), ship))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageEndingWith("Y coordinate is invalid");
        }
    }

    @RepeatedTest(4)
    void replay() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        matchService.resign(guest, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        assertThat(actual.getFleets()).hasSize(2);
        assertThat(actual.getWinner()).isEqualTo(host);
        assertThat(actual.getLoser()).isEqualTo(guest);
        actual.getMoves().add(actual.getFleets());

        assertThat(matchService.retrieveFinishedMatches()).hasSize(1);

        assertThat(matchService.replay(match.getUuid())).hasSize(42);
        Map<String, Set<Ship>> fleets = matchService.replay(match.getUuid()).get(41);
        Ship whiteCommandShip = fleets.get(actual.getWhitePlayer()).stream().findFirst().get();
        assertThat(whiteCommandShip)
                .extracting("color", "shipClass", "coordinates")
                .containsExactly(Ship.Color.WHITE, Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP,
                        new Ship.Coordinates(1, 0));
        Ship blackCommandShip = fleets.get(actual.getBlackPlayer()).stream().findFirst().get();
        assertThat(blackCommandShip)
                .extracting("color", "shipClass", "coordinates")
                .containsExactly(Ship.Color.BLACK, Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP,
                        new Ship.Coordinates(1, 7));
    }

    @RepeatedTest(4)
    void draw() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        matchService.draw(host, match.getUuid());
        matchService.draw(guest, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        assertThat(actual.getFleets()).hasSize(2);
        assertThat(actual.isDraw()).isTrue();
    }

    @RepeatedTest(4)
    void resign() {
        String host = UUID.randomUUID().toString();
        String guest = UUID.randomUUID().toString();

        Match match = matchService.hostMatch(host);

        match = matchService.connectToMatch(guest, match.getUuid());

        positionWhiteFleet(match.getWhitePlayer(), match.getUuid());

        matchService.ready(match.getWhitePlayer(), match.getUuid());

        positionBlackFleet(match.getBlackPlayer(), match.getUuid());

        matchService.ready(match.getBlackPlayer(), match.getUuid());

        matchService.startMatch(host, match.getUuid());

        matchService.resign(guest, match.getUuid());

        Match actual = matchService.retrieveMatch(match.getUuid());
        assertThat(actual.getFleets()).hasSize(2);
        assertThat(actual.getWinner()).isEqualTo(host);
        assertThat(actual.getLoser()).isEqualTo(guest);
    }

    @Test
    void retrieveFinishedMatches() {
        resign();

        assertThat(matchService.retrieveFinishedMatches()).isNotEmpty();
    }

    @Test
    void retrieveActiveMatches() {
        startMatch();

        assertThat(matchService.retrieveActiveMatches()).isNotEmpty();
    }

    @Test
    void retrieveUnstartedMatches() {
        String host = UUID.randomUUID().toString();

        matchService.hostMatch(host);

        assertThat(matchService.retrieveUnstartedMatches()).isNotEmpty();
    }

    private void positionWhiteFleet(String host, String matchUuid) {
        // 1st row

        Ship ship1 = new Ship();
        ship1.setColor(Ship.Color.WHITE);
        ship1.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
        ship1.setCoordinates(new Ship.Coordinates(1, 0));

        matchService.positionShip(host, matchUuid, ship1);

        Ship ship2 = new Ship();
        ship2.setColor(Ship.Color.WHITE);
        ship2.setShipClass(Ship.ShipClass.KIDD_CLASS_GUIDED_MISSILE_DESTROYER);
        ship2.setCoordinates(new Ship.Coordinates(4, 0));

        matchService.positionShip(host, matchUuid, ship2);

        Ship ship3 = new Ship();
        ship3.setColor(Ship.Color.WHITE);
        ship3.setShipClass(Ship.ShipClass.TICONDEROGA_CLASS_GUIDED_MISSILE_CRUISER);
        ship3.setCoordinates(new Ship.Coordinates(7, 0));

        matchService.positionShip(host, matchUuid, ship3);

        // 2nd row

        Ship ship4 = new Ship();
        ship4.setColor(Ship.Color.WHITE);
        ship4.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_0);
        ship4.setCoordinates(new Ship.Coordinates(0, 1));

        matchService.positionShip(host, matchUuid, ship4);

        Ship ship5 = new Ship();
        ship5.setColor(Ship.Color.WHITE);
        ship5.setShipClass(Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_0);
        ship5.setCoordinates(new Ship.Coordinates(1, 1));

        matchService.positionShip(host, matchUuid, ship5);

        Ship ship6 = new Ship();
        ship6.setColor(Ship.Color.WHITE);
        ship6.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_1);
        ship6.setCoordinates(new Ship.Coordinates(2, 1));

        matchService.positionShip(host, matchUuid, ship6);

        Ship ship7 = new Ship();
        ship7.setColor(Ship.Color.WHITE);
        ship7.setShipClass(Ship.ShipClass.ARLEIGH_BURKE_CLASS_GUIDED_MISSILE_DESTROYER);
        ship7.setCoordinates(new Ship.Coordinates(3, 1));

        matchService.positionShip(host, matchUuid, ship7);

        Ship ship8 = new Ship();
        ship8.setColor(Ship.Color.WHITE);
        ship8.setShipClass(Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_1);
        ship8.setCoordinates(new Ship.Coordinates(4, 1));

        matchService.positionShip(host, matchUuid, ship8);

        Ship ship9 = new Ship();
        ship9.setColor(Ship.Color.WHITE);
        ship9.setShipClass(Ship.ShipClass.ZUMWALT_CLASS_GUIDED_MISSILE_DESTROYER);
        ship9.setCoordinates(new Ship.Coordinates(5, 1));

        matchService.positionShip(host, matchUuid, ship9);

        Ship ship10 = new Ship();
        ship10.setColor(Ship.Color.WHITE);
        ship10.setShipClass(Ship.ShipClass.TARAWA_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        ship10.setCoordinates(new Ship.Coordinates(6, 1));

        matchService.positionShip(host, matchUuid, ship10);

        Ship ship11 = new Ship();
        ship11.setColor(Ship.Color.WHITE);
        ship11.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_2);
        ship11.setCoordinates(new Ship.Coordinates(7, 1));

        matchService.positionShip(host, matchUuid, ship11);

        Ship ship12 = new Ship();
        ship12.setColor(Ship.Color.WHITE);
        ship12.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_3);
        ship12.setCoordinates(new Ship.Coordinates(8, 1));

        matchService.positionShip(host, matchUuid, ship12);

        // 3rd row

        Ship ship13 = new Ship();
        ship13.setColor(Ship.Color.WHITE);
        ship13.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
        ship13.setCoordinates(new Ship.Coordinates(0, 2));

        matchService.positionShip(host, matchUuid, ship13);

        Ship ship14 = new Ship();
        ship14.setColor(Ship.Color.WHITE);
        ship14.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4);
        ship14.setCoordinates(new Ship.Coordinates(1, 2));

        matchService.positionShip(host, matchUuid, ship14);

        Ship ship15 = new Ship();
        ship15.setColor(Ship.Color.WHITE);
        ship15.setShipClass(Ship.ShipClass.ENTERPRISE_CLASS_AIRCRAFT_CARRIER);
        ship15.setCoordinates(new Ship.Coordinates(2, 2));

        matchService.positionShip(host, matchUuid, ship15);

        Ship ship16 = new Ship();
        ship16.setColor(Ship.Color.WHITE);
        ship16.setShipClass(Ship.ShipClass.RONALD_REAGAN_SUBCLASS_AIRCRAFT_CARRIER);
        ship16.setCoordinates(new Ship.Coordinates(3, 2));

        matchService.positionShip(host, matchUuid, ship16);

        Ship ship17 = new Ship();
        ship17.setColor(Ship.Color.WHITE);
        ship17.setShipClass(Ship.ShipClass.WASP_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        ship17.setCoordinates(new Ship.Coordinates(4, 2));

        matchService.positionShip(host, matchUuid, ship17);

        Ship ship18 = new Ship();
        ship18.setColor(Ship.Color.WHITE);
        ship18.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_5);
        ship18.setCoordinates(new Ship.Coordinates(5, 2));

        matchService.positionShip(host, matchUuid, ship18);

        Ship ship19 = new Ship();
        ship19.setColor(Ship.Color.WHITE);
        ship19.setShipClass(Ship.ShipClass.AMERICA_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        ship19.setCoordinates(new Ship.Coordinates(6, 2));

        matchService.positionShip(host, matchUuid, ship19);

        Ship ship20 = new Ship();
        ship20.setColor(Ship.Color.WHITE);
        ship20.setShipClass(Ship.ShipClass.GERALD_FORD_CLASS_AIRCRAFT_CARRIER);
        ship20.setCoordinates(new Ship.Coordinates(7, 2));

        matchService.positionShip(host, matchUuid, ship20);

        Ship ship21 = new Ship();
        ship21.setColor(Ship.Color.WHITE);
        ship21.setShipClass(Ship.ShipClass.THEODORE_ROOSEVELT_SUBCLASS_AIRCRAFT_CARRIER);
        ship21.setCoordinates(new Ship.Coordinates(8, 2));

        matchService.positionShip(host, matchUuid, ship21);
    }

    private void positionBlackFleet(String guest, String matchUuid) {
        // 1st row

        Ship ship1 = new Ship();
        ship1.setColor(Ship.Color.BLACK);
        ship1.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
        ship1.setCoordinates(new Ship.Coordinates(1, 7));

        matchService.positionShip(guest, matchUuid, ship1);

        Ship ship2 = new Ship();
        ship2.setColor(Ship.Color.BLACK);
        ship2.setShipClass(Ship.ShipClass.KIDD_CLASS_GUIDED_MISSILE_DESTROYER);
        ship2.setCoordinates(new Ship.Coordinates(4, 7));

        matchService.positionShip(guest, matchUuid, ship2);

        Ship ship3 = new Ship();
        ship3.setColor(Ship.Color.BLACK);
        ship3.setShipClass(Ship.ShipClass.TICONDEROGA_CLASS_GUIDED_MISSILE_CRUISER);
        ship3.setCoordinates(new Ship.Coordinates(7, 7));

        matchService.positionShip(guest, matchUuid, ship3);

        // 2nd row

        Ship ship4 = new Ship();
        ship4.setColor(Ship.Color.BLACK);
        ship4.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_0);
        ship4.setCoordinates(new Ship.Coordinates(0, 6));

        matchService.positionShip(guest, matchUuid, ship4);

        Ship ship5 = new Ship();
        ship5.setColor(Ship.Color.BLACK);
        ship5.setShipClass(Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_0);
        ship5.setCoordinates(new Ship.Coordinates(1, 6));

        matchService.positionShip(guest, matchUuid, ship5);

        Ship ship6 = new Ship();
        ship6.setColor(Ship.Color.BLACK);
        ship6.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_1);
        ship6.setCoordinates(new Ship.Coordinates(2, 6));

        matchService.positionShip(guest, matchUuid, ship6);

        Ship ship7 = new Ship();
        ship7.setColor(Ship.Color.BLACK);
        ship7.setShipClass(Ship.ShipClass.ARLEIGH_BURKE_CLASS_GUIDED_MISSILE_DESTROYER);
        ship7.setCoordinates(new Ship.Coordinates(3, 6));

        matchService.positionShip(guest, matchUuid, ship7);

        Ship ship8 = new Ship();
        ship8.setColor(Ship.Color.BLACK);
        ship8.setShipClass(Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_1);
        ship8.setCoordinates(new Ship.Coordinates(4, 6));

        matchService.positionShip(guest, matchUuid, ship8);

        Ship ship9 = new Ship();
        ship9.setColor(Ship.Color.BLACK);
        ship9.setShipClass(Ship.ShipClass.ZUMWALT_CLASS_GUIDED_MISSILE_DESTROYER);
        ship9.setCoordinates(new Ship.Coordinates(5, 6));

        matchService.positionShip(guest, matchUuid, ship9);

        Ship ship10 = new Ship();
        ship10.setColor(Ship.Color.BLACK);
        ship10.setShipClass(Ship.ShipClass.TARAWA_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        ship10.setCoordinates(new Ship.Coordinates(6, 6));

        matchService.positionShip(guest, matchUuid, ship10);

        Ship ship11 = new Ship();
        ship11.setColor(Ship.Color.BLACK);
        ship11.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_2);
        ship11.setCoordinates(new Ship.Coordinates(7, 6));

        matchService.positionShip(guest, matchUuid, ship11);

        Ship ship12 = new Ship();
        ship12.setColor(Ship.Color.BLACK);
        ship12.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_3);
        ship12.setCoordinates(new Ship.Coordinates(8, 6));

        matchService.positionShip(guest, matchUuid, ship12);

        // 3rd row

        Ship ship13 = new Ship();
        ship13.setColor(Ship.Color.BLACK);
        ship13.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
        ship13.setCoordinates(new Ship.Coordinates(0, 5));

        matchService.positionShip(guest, matchUuid, ship13);

        Ship ship14 = new Ship();
        ship14.setColor(Ship.Color.BLACK);
        ship14.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4);
        ship14.setCoordinates(new Ship.Coordinates(1, 5));

        matchService.positionShip(guest, matchUuid, ship14);

        Ship ship15 = new Ship();
        ship15.setColor(Ship.Color.BLACK);
        ship15.setShipClass(Ship.ShipClass.ENTERPRISE_CLASS_AIRCRAFT_CARRIER);
        ship15.setCoordinates(new Ship.Coordinates(2, 5));

        matchService.positionShip(guest, matchUuid, ship15);

        Ship ship16 = new Ship();
        ship16.setColor(Ship.Color.BLACK);
        ship16.setShipClass(Ship.ShipClass.RONALD_REAGAN_SUBCLASS_AIRCRAFT_CARRIER);
        ship16.setCoordinates(new Ship.Coordinates(3, 5));

        matchService.positionShip(guest, matchUuid, ship16);

        Ship ship17 = new Ship();
        ship17.setColor(Ship.Color.BLACK);
        ship17.setShipClass(Ship.ShipClass.WASP_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        ship17.setCoordinates(new Ship.Coordinates(4, 5));

        matchService.positionShip(guest, matchUuid, ship17);

        Ship ship18 = new Ship();
        ship18.setColor(Ship.Color.BLACK);
        ship18.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_5);
        ship18.setCoordinates(new Ship.Coordinates(5, 5));

        matchService.positionShip(guest, matchUuid, ship18);

        Ship ship19 = new Ship();
        ship19.setColor(Ship.Color.BLACK);
        ship19.setShipClass(Ship.ShipClass.AMERICA_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        ship19.setCoordinates(new Ship.Coordinates(6, 5));

        matchService.positionShip(guest, matchUuid, ship19);

        Ship ship20 = new Ship();
        ship20.setColor(Ship.Color.BLACK);
        ship20.setShipClass(Ship.ShipClass.GERALD_FORD_CLASS_AIRCRAFT_CARRIER);
        ship20.setCoordinates(new Ship.Coordinates(7, 5));

        matchService.positionShip(guest, matchUuid, ship20);

        Ship ship21 = new Ship();
        ship21.setColor(Ship.Color.BLACK);
        ship21.setShipClass(Ship.ShipClass.THEODORE_ROOSEVELT_SUBCLASS_AIRCRAFT_CARRIER);
        ship21.setCoordinates(new Ship.Coordinates(8, 5));

        matchService.positionShip(guest, matchUuid, ship21);
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
