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
package nz.org.vincenzo.cots.player.configuration;

import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import nz.org.vincenzo.cots.player.dao.PlayerDAO;
import nz.org.vincenzo.cots.player.dao.impl.PlayerDAODynamoDBImpl;
import nz.org.vincenzo.cots.player.service.PlayerService;
import nz.org.vincenzo.cots.player.service.impl.PlayerServiceImpl;
import org.json.simple.parser.JSONParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The player configuration.
 *
 * @author Rey Vincent Babilonia
 */
@Configuration
public class PlayerConfiguration {

    /**
     * Returns the {@link AmazonDynamoDB} client.
     *
     * @return the {@link AmazonDynamoDB} client
     */
    @Bean
    public AmazonDynamoDB amazonDynamoDB() {
        return AmazonDynamoDBClientBuilder.standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .build();
    }

    /**
     * Returns the {@link DynamoDBMapper}.
     *
     * @return the {@link DynamoDBMapper}
     */
    @Bean
    public DynamoDBMapper dynamoDBMapper() {
        return new DynamoDBMapper(amazonDynamoDB());
    }

    /**
     * Returns the {@link JSONParser}.
     *
     * @return the {@link JSONParser}
     */
    @Bean
    public JSONParser jsonParser() {
        return new JSONParser();
    }

    /**
     * Returns the {@link PlayerDAO}.
     *
     * @return the {@link PlayerDAO}
     */
    @Bean
    public PlayerDAO playerDAO() {
        return new PlayerDAODynamoDBImpl(dynamoDBMapper());
    }

    /**
     * Returns the {@link PlayerService}.
     *
     * @return the {@link PlayerService}
     */
    @Bean
    public PlayerService playerService() {
        return new PlayerServiceImpl(playerDAO());
    }
}
