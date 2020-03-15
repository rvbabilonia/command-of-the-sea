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
package nz.org.vincenzo.cots.player.dao.impl;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.player.dao.PlayerDAO;
import org.apache.commons.lang3.StringUtils;
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
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;
import software.amazon.awssdk.services.dynamodb.model.QueryResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;
import software.amazon.awssdk.services.dynamodb.model.ScanResponse;
import software.amazon.awssdk.services.dynamodb.model.UpdateItemRequest;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The DynamoDB implementation of {@link PlayerDAO}.
 *
 * @author Rey Vincent Babilonia
 */
@Repository
public class PlayerDAODynamoDBImpl implements PlayerDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerDAODynamoDBImpl.class);

    private static final String PLAYER_TABLE_NAME = "player";

    private static final String[] COLUMNS = {"uuid", "emailAddress", "nickname", "avatar", "registrationDate",
            "lastLoginDate", "statistics", "tournamentStatistics", "accessToken"};

    private static final Type STATISTICS_TYPE = new TypeToken<Map<String, Player.Statistics>>() {
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
    public PlayerDAODynamoDBImpl(DynamoDbClient dynamoDbClient, Gson gson) {
        this.dynamoDbClient = dynamoDbClient;
        this.gson = gson;
    }

    @Override
    public Player createPlayer(final String nickname, final String emailAddress) {
        Player player = new Player();
        player.setUuid(UUID.randomUUID().toString());
        player.setNickname(nickname);
        player.setEmailAddress(emailAddress);
        player.setRegistrationDate(LocalDateTime.now());

        Map<String, AttributeValue> item = new HashMap<>();
        item.put("uuid", AttributeValue.builder().s(player.getUuid()).build());
        item.put("nickname", AttributeValue.builder().s(player.getNickname()).build());
        item.put("emailAddress", AttributeValue.builder().s(player.getEmailAddress()).build());
        item.put("registrationDate", AttributeValue.builder().s(player.getRegistrationDate().toString()).build());

        PutItemRequest request = PutItemRequest.builder()
                .tableName(PLAYER_TABLE_NAME)
                .item(item)
                .build();

        try {
            dynamoDbClient.putItem(request);

            return player;
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Failed to create player: Table [{}] does not exist", PLAYER_TABLE_NAME);

            throw e;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to create player: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public Player retrievePlayerByUuid(final String playerUuid) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uuid", AttributeValue.builder().s(playerUuid).build());

        GetItemRequest request = GetItemRequest.builder()
                .tableName(PLAYER_TABLE_NAME)
                .key(key)
                .attributesToGet(COLUMNS)
                .build();

        try {
            Map<String, AttributeValue> item = dynamoDbClient.getItem(request).item();

            if (item != null && !item.isEmpty()) {
                Player player = new Player();
                player.setUuid(getValue(item.get("uuid"), String.class));
                player.setNickname(getValue(item.get("nickname"), String.class));
                player.setEmailAddress(getValue(item.get("emailAddress"), String.class));
                player.setAccessToken(getValue(item.get("accessToken"), String.class));
                player.setAvatar(getValue(item.get("avatar"), String.class));
                if (item.get("registrationDate") != null) {
                    player.setRegistrationDate(LocalDateTime.parse(getValue(item.get("registrationDate"),
                            String.class)));
                }
                if (item.get("lastLoginDate") != null) {
                    player.setLastLoginDate(LocalDateTime.parse(getValue(item.get("lastLoginDate"), String.class)));
                }
                if (item.get("statistics") != null) {
                    player.setStatistics(gson.fromJson(item.get("statistics").s(), Player.Statistics.class));
                }
                if (item.get("tournamentStatistics") != null) {
                    Map<String, Player.Statistics> deserializedMap = gson.fromJson(item.get("tournamentStatistics").s(),
                            STATISTICS_TYPE);

                    player.setTournamentStatistics(deserializedMap);
                }

                return player;
            } else {
                LOGGER.error("Failed to retrieve player: Player with UUID [{}] does not exist", playerUuid);

                return null;
            }
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to retrieve player: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public Player retrievePlayerByAccessToken(final String accessToken) {
        return retrievePlayer("accessToken", accessToken, "accessTokens", "access token");
    }

    @Override
    public Player retrievePlayerByEmailAddress(final String emailAddress) {
        return retrievePlayer("emailAddress", emailAddress, "emailAddresses", "email address");
    }

    @Override
    public Player retrievePlayerByNickname(final String nickname) {
        return retrievePlayer("nickname", nickname, "nicknames", "nickname");
    }

    @Override
    public Set<Player> retrievePlayers() {
        ScanRequest request = ScanRequest.builder()
                .tableName(PLAYER_TABLE_NAME)
                .build();

        Set<Player> players = new HashSet<>();
        try {
            ScanResponse response = dynamoDbClient.scan(request);
            response.items().forEach(item -> {
                Player player = new Player();
                player.setUuid(getValue(item.get("uuid"), String.class));
                player.setNickname(getValue(item.get("nickname"), String.class));
                player.setEmailAddress(getValue(item.get("emailAddress"), String.class));
                player.setAvatar(getValue(item.get("avatar"), String.class));
                if (item.get("registrationDate") != null) {
                    player.setRegistrationDate(LocalDateTime.parse(getValue(item.get("registrationDate"),
                            String.class)));
                }
                if (item.get("lastLoginDate") != null) {
                    player.setLastLoginDate(LocalDateTime.parse(getValue(item.get("lastLoginDate"), String.class)));
                }
                if (item.get("statistics") != null) {
                    player.setStatistics(gson.fromJson(item.get("statistics").s(), Player.Statistics.class));
                }
                if (item.get("tournamentStatistics") != null) {
                    Map<String, Player.Statistics> deserializedMap = gson.fromJson(item.get("tournamentStatistics").s(),
                            STATISTICS_TYPE);

                    player.setTournamentStatistics(deserializedMap);
                }

                players.add(player);
            });

            return players;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to retrieve unstarted matches: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public boolean updatePlayer(Player player) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uuid", AttributeValue.builder().s(player.getUuid()).build());

        Map<String, AttributeValueUpdate> updatedValues = new HashMap<>();
        if (StringUtils.isNotBlank(player.getAccessToken())) {
            updatedValues.put("accessToken", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(player.getAccessToken()).build())
                    .action(AttributeAction.PUT)
                    .build());
        } else {
            updatedValues.put("accessToken", AttributeValueUpdate.builder()
                    .action(AttributeAction.DELETE)
                    .build());
        }
        if (StringUtils.isNotBlank(player.getAvatar())) {
            updatedValues.put("avatar", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(player.getAvatar()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        if (player.getLastLoginDate() != null) {
            updatedValues.put("lastLoginDate", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(player.getLastLoginDate().toString()).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        if (player.getStatistics() != null) {
            updatedValues.put("statistics", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().s(gson.toJson(player.getStatistics())).build())
                    .action(AttributeAction.PUT)
                    .build());
        }
        if (!player.getTournamentStatistics().isEmpty()) {
            updatedValues.put("tournamentStatistics", AttributeValueUpdate.builder()
                    .value(AttributeValue.builder().ss(gson.toJson(player.getTournamentStatistics())).build())
                    .action(AttributeAction.PUT)
                    .build());
        }

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(PLAYER_TABLE_NAME)
                .key(key)
                .attributeUpdates(updatedValues)
                .build();

        try {
            dynamoDbClient.updateItem(request);

            return true;
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Failed to update player: Player with UUID [{}] does not exist", player.getUuid());

            throw e;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to update player: [{}]", e.getMessage(), e);

            throw e;
        }
    }

    @Override
    public boolean deletePlayer(String playerUuid) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("uuid", AttributeValue.builder().s(playerUuid).build());

        DeleteItemRequest request = DeleteItemRequest.builder()
                .tableName(PLAYER_TABLE_NAME)
                .key(key)
                .build();

        try {
            dynamoDbClient.deleteItem(request);

            return true;
        } catch (ResourceNotFoundException e) {
            LOGGER.error("Failed to delete player: Player with UUID [{}] does not exist", playerUuid);

            throw e;
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to delete player: [{}]", e.getMessage(), e);

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

    private Player retrievePlayer(final String attributeKey, final String attributeValue,
                                  final String secondaryIndexName, final String description) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":" + attributeKey, AttributeValue.builder().s(attributeValue).build());

        QueryRequest request = QueryRequest.builder()
                .tableName(PLAYER_TABLE_NAME)
                .indexName(secondaryIndexName)
                .keyConditionExpression(attributeKey + " = :" + attributeKey)
                .expressionAttributeValues(expressionAttributeValues)
                .consistentRead(false)
                .build();

        try {
            QueryResponse response = dynamoDbClient.query(request);

            if (response.count() == 0) {
                LOGGER.warn("Failed to retrieve player: Player with {} [{}] does not exist", description,
                        attributeValue);

                return null;
            }

            List<Map<String, AttributeValue>> items = response.items();
            Map<String, AttributeValue> item = items.get(0);

            String uuid = getValue(item.get("uuid"), String.class);

            return retrievePlayerByUuid(uuid);
        } catch (DynamoDbException e) {
            LOGGER.error("Failed to retrieve player by {}: [{}]", description, e.getMessage(), e);

            throw e;
        }
    }
}
