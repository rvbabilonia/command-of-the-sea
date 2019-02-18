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

import by.dev.madhead.aws_junit5.dynamodb.v1.DynamoDBLocal;
import by.dev.madhead.aws_junit5.dynamodb.v1.DynamoDBLocalExtension;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.model.AttributeDefinition;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;
import com.amazonaws.services.dynamodbv2.model.KeyType;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput;
import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType;
import nz.org.vincenzo.cots.domain.Match;
import nz.org.vincenzo.cots.match.dao.impl.MatchDAODynamoDBImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The test case for {@link MatchDAO}.
 *
 * @author Rey Vincent Babilonia
 */
@ExtendWith(DynamoDBLocalExtension.class)
class MatchDAOTest {

    @DynamoDBLocal(url = "http://localhost:8000")
    private AmazonDynamoDB amazonDynamoDB;

    private MatchDAO matchDAO;

    @BeforeEach
    void setUp() {
        amazonDynamoDB.deleteTable("match");

        CreateTableRequest request = new CreateTableRequest()
                .withTableName("match")
                .withKeySchema(new KeySchemaElement("uuid", KeyType.HASH))
                .withAttributeDefinitions(new AttributeDefinition("uuid", ScalarAttributeType.S))
                .withProvisionedThroughput(new ProvisionedThroughput(5L, 5L));
        amazonDynamoDB.createTable(request);

        DynamoDBMapper mapper = new DynamoDBMapper(amazonDynamoDB);

        matchDAO = new MatchDAODynamoDBImpl(mapper);
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
        assertThat(actual.getCreationDate()).isBeforeOrEqualsTo(Date.from(OffsetDateTime.now().toInstant()));
        assertThat(actual.getTurn()).isNull();
        assertThat(actual.hasStarted()).isFalse();
        assertThat(actual.getStartDate()).isNull();
        assertThat(actual.getEndDate()).isNull();
        assertThat(actual.isWhitePlayerReady()).isFalse();
        assertThat(actual.isBlackPlayerReady()).isFalse();
        assertThat(actual.getTurns()).isEmpty();
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
        matchDAO.updateMatch(expected);

        List<Match> matches = matchDAO.retrieveFinishedMatches();
        assertThat(matches.size()).isEqualTo(1);
    }

    @Test
    void retrieveFinishedDrawMatches() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);
        expected.setStarted(true);
        expected.setDraw(true);
        matchDAO.updateMatch(expected);

        List<Match> matches = matchDAO.retrieveFinishedMatches();
        assertThat(matches.size()).isEqualTo(1);
    }

    @Test
    void retrieveActiveMatches() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);
        expected.setStarted(true);
        matchDAO.updateMatch(expected);

        List<Match> matches = matchDAO.retrieveActiveMatches();
        assertThat(matches.size()).isEqualTo(1);
    }

    @Test
    void retrieveUnstartedMatches() {
        String playerUuid = UUID.randomUUID().toString();

        matchDAO.createMatch(playerUuid);

        List<Match> matches = matchDAO.retrieveUnstartedMatches();
        assertThat(matches.size()).isEqualTo(1);
    }

    @Test
    void updateMatch() {
        String playerUuid = UUID.randomUUID().toString();

        Match expected = matchDAO.createMatch(playerUuid);
        expected.setStarted(true);
        expected.setStartDate(Date.from(OffsetDateTime.now().toInstant()));
        expected.setWhitePlayer(UUID.randomUUID().toString());
        expected.setWhitePlayerReady(true);
        expected.setBlackPlayerReady(true);

        matchDAO.updateMatch(expected);

        Match actual = matchDAO.retrieveMatch(expected.getUuid());

        assertThat(actual).isEqualTo(expected);
    }
}
