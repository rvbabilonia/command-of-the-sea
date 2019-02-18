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

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import nz.org.vincenzo.cots.domain.Match;
import nz.org.vincenzo.cots.domain.Ship;
import nz.org.vincenzo.cots.match.dao.MatchDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DynamoDB implementation of {@link MatchDAO}.
 *
 * @author Rey Vincent Babilonia
 */
@Repository
public class MatchDAODynamoDBImpl implements MatchDAO {

    private final DynamoDBMapper dynamoDBMapper;

    /**
     * Default constructor.
     *
     * @param dynamoDBMapper the {@link DynamoDBMapper}
     */
    @Autowired
    public MatchDAODynamoDBImpl(DynamoDBMapper dynamoDBMapper) {
        this.dynamoDBMapper = dynamoDBMapper;
    }

    @Override
    public Match createMatch(final String playerUuid) {
        Match match = new Match();
        match.setHost(playerUuid);
        match.setCreationDate(Date.from(OffsetDateTime.now().toInstant()));

        Ship.Color shipColor = Math.random() < 0.5 ? Ship.Color.WHITE : Ship.Color.BLACK;
        if (shipColor == Ship.Color.BLACK) {
            match.setBlackPlayer(playerUuid);
        } else {
            match.setWhitePlayer(playerUuid);
        }

        dynamoDBMapper.save(match);

        return match;
    }

    @Override
    public Match retrieveMatch(final String matchUuid) {
        return dynamoDBMapper.load(Match.class, matchUuid);
    }

    @Override
    public List<Match> retrieveFinishedMatches() {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":draw", new AttributeValue().withN("1"));

        DynamoDBScanExpression expression = new DynamoDBScanExpression()
                .withFilterExpression("attribute_exists(winner) or draw = :draw")
                .withExpressionAttributeValues(values);

        return dynamoDBMapper.scan(Match.class, expression);
    }

    @Override
    public List<Match> retrieveActiveMatches() {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":started", new AttributeValue().withN("1"));
        values.put(":draw", new AttributeValue().withN("0"));

        DynamoDBScanExpression expression = new DynamoDBScanExpression()
                .withFilterExpression("started = :started and (attribute_not_exists(winner) or draw = :draw)")
                .withExpressionAttributeValues(values);

        return dynamoDBMapper.scan(Match.class, expression);
    }

    @Override
    public List<Match> retrieveUnstartedMatches() {
        Map<String, AttributeValue> values = new HashMap<>();
        values.put(":started", new AttributeValue().withN("0"));

        DynamoDBScanExpression expression = new DynamoDBScanExpression()
                .withFilterExpression("started = :started")
                .withExpressionAttributeValues(values);

        return dynamoDBMapper.scan(Match.class, expression);
    }

    @Override
    public void updateMatch(Match match) {
        dynamoDBMapper.save(match);
    }

    @Override
    public void deleteMatch(Match match) {
        dynamoDBMapper.delete(match);
    }
}
