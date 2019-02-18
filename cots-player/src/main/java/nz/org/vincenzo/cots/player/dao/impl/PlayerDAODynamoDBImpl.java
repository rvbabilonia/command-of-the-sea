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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBQueryExpression;
import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.player.dao.PlayerDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;

/**
 * The DynamoDB implementation of {@link PlayerDAO}.
 *
 * @author Rey Vincent Babilonia
 */
@Repository
public class PlayerDAODynamoDBImpl implements PlayerDAO {

    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Default constructor.
     *
     * @param dynamoDBMapper the {@link DynamoDBMapper}
     */
    @Autowired
    public PlayerDAODynamoDBImpl(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public Player createPlayer(final String nickname, final String emailAddress, final String password) {
        Player player = new Player();
        player.setNickname(nickname);
        player.setEmailAddress(emailAddress);
        player.setPassword(password);
        player.setRegistrationDate(Date.from(OffsetDateTime.now().toInstant()));

        dynamoDBMapper.save(player);

        return player;
    }

    @Override
    public Player retrievePlayerByUuid(final String playerUuid) {
        return dynamoDBMapper.load(Player.class, playerUuid);
    }

    @Override
    public Player retrievePlayerByAccessToken(final String accessToken) {
        Player player = new Player();
        player.setAccessToken(accessToken);

        List<Player> players = retrievePlayers(player, "accessTokens");
        if (players.isEmpty()) {
            return null;
        }

        return retrievePlayerByUuid(players.get(0).getUuid());
    }

    @Override
    public List<Player> retrievePlayers(Player player, final String... indexNames) {
        DynamoDBQueryExpression<Player> queryExpression = new DynamoDBQueryExpression<Player>()
                .withHashKeyValues(player)
                .withConsistentRead(false);

        for (String indexName : indexNames) {
            queryExpression.withIndexName(indexName);
        }

        return dynamoDBMapper.query(Player.class, queryExpression);
    }

    @Override
    public List<Player> retrievePlayers() {
        return dynamoDBMapper.scan(Player.class, null);
    }

    @Override
    public void updatePlayer(Player player) {
        dynamoDBMapper.save(player);
    }
}
