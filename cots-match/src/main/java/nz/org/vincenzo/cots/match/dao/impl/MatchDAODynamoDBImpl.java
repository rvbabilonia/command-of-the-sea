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
package nz.org.vincenzo.cots.match.dao.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nz.org.vincenzo.cots.domain.Match;
import nz.org.vincenzo.cots.domain.Ship;
import nz.org.vincenzo.cots.match.dao.MatchDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeAction;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.AttributeValueUpdate;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The DynamoDB implementation of {@link MatchDAO}.
 *
 * @author Rey Vincent Babilonia
 */
@Repository
public class MatchDAODynamoDBImpl implements MatchDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(MatchDAODynamoDBImpl.class);

    private static final String MATCH_TABLE_NAME = "match";

    private static final String[] COLUMNS = {"uuid", "whitePlayer", "blackPlayer", "winner", "loser", "draw",
            "whitePlayerAgreedToDraw", "blackPlayerAgreedToDraw", "host", "creationDate", "turn", "started",
            "startDate", "endDate", "whitePlayerReady", "blackPlayerReady", "moves", "fleets"};

    private static final Type MOVE_TYPE = new TypeToken<Map<String, Set<Ship>>>() {
    }.getType();

    private final DynamoDbClient dynamoDbClient;

    private final Gson gson;

    /**
     * Default constructor.
     *
     * @param dynamoDbClient the {@link DynamoDbClient}
     * @param gson           the {@link Gson}
     */
    @Autowired
    public MatchDAODynamoDBImpl(DynamoDbClient dynamoDbClient, Gson gson) {
        this.dynamoDbClient = dynamoDbClient;
        this.gson = gson;
    }

    @Override
    public Match createMatch(final String playerUuid) {
        Match match = new Match();
        match.setUuid(UUID.randomUUID().toString());
        match.setHost(playerUuid);
        match.setCreationDate(LocalDateTime.now());

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("uuid", AttributeValue.builder().s(match.getUuid()).build());
        item.put("host", AttributeValue.builder().s(playerUuid).build());
        item.put("creationDate", AttributeValue.builder().s(match.getCreationDate().toString()).build());
        item.put("started", AttributeValue.builder().bool(match.hasStarted()).build());

        Ship.Color shipColor = Math.random() < 0.5 ? Ship.Color.WHITE : Ship.Color.BLACK;
        if (shipColor == Ship.Color.BLACK) {
            match.setBlackPlayer(playerUuid);
            item.put("blackPlayer", AttributeValue.builder().s(match.getBlackPlayer()).build());
        } else {
            match.setWhitePlayer(playerUuid);
            item.put("whitePlayer", AttributeValue.builder().s(match.getWhitePlayer()).build());
        }

        PutItemRequest request = PutItemRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .item(item)
                .build();

        try {
            dynamoDbClient.putItem(request);

            return match;
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Failed to create match: Table [{}] does not exist", MATCH_TABLE_NAME);

            throw e;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to create match: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public Match retrieveMatch(final String matchUuid) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uuid", AttributeValue.builder().s(matchUuid).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .key(key)
                .attributesToGet(COLUMNS)
                .build();

        try {
            Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();

            if (item != null && !item.isEmpty()) {
                Match match = new Match();
                match.setUuid(getValue(item.get("uuid"), String.class));
                match.setWhitePlayer(getValue(item.get("whitePlayer"), String.class));
                match.setBlackPlayer(getValue(item.get("blackPlayer"), String.class));
                match.setWinner(getValue(item.get("winner"), String.class));
                match.setLoser(getValue(item.get("loser"), String.class));
                match.setDraw(Boolean.TRUE.equals(getValue(item.get("draw"), Boolean.class)));
                match.setWhitePlayerAgreedToDraw(Boolean.TRUE.equals(getValue(item.get("whitePlayerAgreedToDraw"),
                        Boolean.class)));
                match.setBlackPlayerAgreedToDraw(Boolean.TRUE.equals(getValue(item.get("blackPlayerAgreedToDraw"),
                        Boolean.class)));
                match.setHost(getValue(item.get("host"), String.class));
                if (item.get("turn") != null) {
                    match.setTurn(Ship.Color.valueOf(getValue(item.get("turn"), String.class)));
                }
                match.setStarted(Boolean.TRUE.equals(getValue(item.get("started"), Boolean.class)));
                if (item.get("startDate") != null) {
                    match.setStartDate(LocalDateTime.parse(getValue(item.get("startDate"), String.class)));
                }
                if (item.get("endDate") != null) {
                    match.setEndDate(LocalDateTime.parse(getValue(item.get("endDate"), String.class)));
                }
                if (item.get("creationDate") != null) {
                    match.setCreationDate(LocalDateTime.parse(getValue(item.get("creationDate"), String.class)));
                }
                match.setWhitePlayerReady(Boolean.TRUE.equals(getValue(item.get("whitePlayerReady"), Boolean.class)));
                match.setBlackPlayerReady(Boolean.TRUE.equals(getValue(item.get("blackPlayerReady"), Boolean.class)));

                if (item.get("moves") != null) {
                    List<String> moves = item.get("moves").ss();

                    List<Map<String, Set<Ship>>> deserializedList = moves
                            .stream()
                            .<Map<String, Set<Ship>>>map(move -> gson.fromJson(move, MOVE_TYPE))
                            .collect(Collectors.toCollection(LinkedList::new));

                    match.setMoves(deserializedList);
                }

                if (item.get("fleets") != null) {
                    Map<String, Set<Ship>> deserializedMap = gson.fromJson(item.get("fleets").s(), MOVE_TYPE);

                    match.setFleets(deserializedMap);
                }

                return match;
            } else {
                LOGGER.error("Failed to retrieve match: Match with UUID [{}] does not exist", matchUuid);

                return null;
            }
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to retrieve match: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public Set<Match> retrieveFinishedMatches() {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":draw", AttributeValue.builder().bool(true).build());

        ScanRequest request = ScanRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .filterExpression("attribute_exists(winner) or draw = :draw")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        Set<Match> matches = new HashSet<>();
        try {
            ScanResponse response = dynamoDbClient.scan(request);
            response.items().forEach(item -> {
                Match match = new Match();
                match.setUuid(getValue(item.get("uuid"), String.class));
                match.setWhitePlayer(getValue(item.get("whitePlayer"), String.class));
                match.setBlackPlayer(getValue(item.get("blackPlayer"), String.class));
                match.setHost(getValue(item.get("host"), String.class));
                match.setCreationDate(LocalDateTime.parse(getValue(item.get("creationDate"), String.class)));
                match.setWinner(getValue(item.get("winner"), String.class));
                match.setDraw(Boolean.TRUE.equals(getValue(item.get("draw"), Boolean.class)));

                matches.add(match);
            });

            return matches;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to retrieve unstarted matches: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public Set<Match> retrieveActiveMatches() {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":started", AttributeValue.builder().bool(true).build());
        expressionAttributeValues.put(":draw", AttributeValue.builder().bool(false).build());

        ScanRequest request = ScanRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .filterExpression("started = :started and (attribute_not_exists(winner) or draw = :draw)")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        Set<Match> matches = new HashSet<>();
        try {
            ScanResponse response = dynamoDbClient.scan(request);
            response.items().forEach(item -> {
                Match match = new Match();
                match.setUuid(getValue(item.get("uuid"), String.class));
                match.setWhitePlayer(getValue(item.get("whitePlayer"), String.class));
                match.setBlackPlayer(getValue(item.get("blackPlayer"), String.class));
                match.setHost(getValue(item.get("host"), String.class));
                match.setCreationDate(LocalDateTime.parse(getValue(item.get("creationDate"), String.class)));
                match.setStarted(Boolean.TRUE.equals(getValue(item.get("started"), Boolean.class)));
                match.setStartDate(LocalDateTime.parse(getValue(item.get("startDate"), String.class)));

                matches.add(match);
            });

            return matches;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to retrieve unstarted matches: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public Set<Match> retrieveUnstartedMatches() {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":started", AttributeValue.builder().bool(false).build());

        ScanRequest request = ScanRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .filterExpression("started = :started")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        Set<Match> matches = new HashSet<>();
        try {
            ScanResponse response = dynamoDbClient.scan(request);
            response.items().forEach(item -> {
                Match match = new Match();
                match.setUuid(getValue(item.get("uuid"), String.class));
                match.setWhitePlayer(getValue(item.get("whitePlayer"), String.class));
                match.setBlackPlayer(getValue(item.get("blackPlayer"), String.class));
                match.setHost(getValue(item.get("host"), String.class));
                match.setCreationDate(LocalDateTime.parse(getValue(item.get("creationDate"), String.class)));

                matches.add(match);
            });

            return matches;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to retrieve unstarted matches: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public boolean updateMatch(Match match) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uuid", AttributeValue.builder().s(match.getUuid()).build());

        Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        if (match.getWhitePlayer() != null) {
            updatedValues.put("whitePlayer", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(match.getWhitePlayer()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        if (match.getBlackPlayer() != null) {
            updatedValues.put("blackPlayer", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(match.getBlackPlayer()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        if (match.getWinner() != null) {
            updatedValues.put("winner", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(match.getWinner()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        if (match.getLoser() != null) {
            updatedValues.put("loser", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(match.getLoser()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        updatedValues.put("draw", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(match.isDraw()).build())
                .action(AttributeAction.PUT)
                .build());
        updatedValues.put("whitePlayerAgreedToDraw", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(match.hasWhitePlayerAgreedToDraw()).build())
                .action(AttributeAction.PUT)
                .build());
        updatedValues.put("blackPlayerAgreedToDraw", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(match.hasBlackPlayerAgreedToDraw()).build())
                .action(AttributeAction.PUT)
                .build());
        if (match.getTurn() != null) {
            updatedValues.put("turn", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(match.getTurn().name()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        updatedValues.put("started", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(match.hasStarted()).build())
                .action(AttributeAction.PUT)
                .build());
        if (match.getStartDate() != null) {
            updatedValues.put("startDate", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(match.getStartDate().toString()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        if (match.getEndDate() != null) {
            updatedValues.put("endDate", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(match.getEndDate().toString()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        updatedValues.put("whitePlayerReady", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(match.isWhitePlayerReady()).build())
                .action(AttributeAction.PUT)
                .build());
        updatedValues.put("blackPlayerReady", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().bool(match.isBlackPlayerReady()).build())
                .action(AttributeAction.PUT)
                .build());
        if (!match.getFleets().isEmpty()) {
            String fleets = gson.toJson(match.getFleets());
            updatedValues.put("moves", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().ss(fleets).build())
                    .action(AttributeAction.ADD)
                    .build());

            updatedValues.put("fleets", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(fleets).build())
                    .action(AttributeAction.PUT)
                    .build());
        }

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .key(key)
                .attributeUpdates(updatedValues)
                .build();

        try {
            dynamoDbClient.updateItem(request);

            return true;
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Failed to update match: Match with UUID [{}] does not exist", match.getUuid());

            throw e;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to update match: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public boolean deleteMatch(Match match) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uuid", AttributeValue.builder().s(match.getUuid()).build());

        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":host", AttributeValue.builder().s(match.getHost()).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(MATCH_TABLE_NAME)
                .key(key)
                .conditionExpression("host = :host")
                .expressionAttributeValues(expressionAttributeValues)
                .build();

        try {
            dynamoDbClient.deleteItem(request);

            return true;
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Failed to delete match: Match with UUID [{}] does not exist or was not created by host [{}]",
                    match.getUuid(), match.getHost());

            throw e;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to delete match: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    private <T> T getValue(AttributeValue value, Class<T> type) {
        if (value == null) {
            return null;
        }

        if (Boolean.class == type) {
            return type.cast(value.bool());
        } else if (Number.class == type) {
            return type.cast(value.n());
        }

        return type.cast(value.s());
    }
}
