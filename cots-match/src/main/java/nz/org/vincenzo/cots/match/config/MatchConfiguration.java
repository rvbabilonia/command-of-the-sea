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
package nz.org.vincenzo.cots.match.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nz.org.vincenzo.cots.match.dao.MatchDAO;
import nz.org.vincenzo.cots.match.dao.impl.MatchDAODynamoDBImpl;
import nz.org.vincenzo.cots.match.service.ArbitrationService;
import nz.org.vincenzo.cots.match.service.MatchService;
import nz.org.vincenzo.cots.match.service.PlayerService;
import nz.org.vincenzo.cots.match.service.impl.ArbitrationServiceImpl;
import nz.org.vincenzo.cots.match.service.impl.MatchServiceImpl;
import nz.org.vincenzo.cots.match.service.impl.PlayerServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.lambda.LambdaClient;

/**
 * The match configuration.
 *
 * @author Rey Vincent Babilonia
 */
@Configuration
public class MatchConfiguration {

    /**
     * Returns the {@link DynamoDbClient}.
     *
     * @return the {@link DynamoDbClient}
     */
    @Bean
    public DynamoDbClient dynamoDbClient() {
        return DynamoDbClient.builder()
                .region(Region.AP_SOUTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }

    /**
     * Returns the {@link LambdaClient}.
     *
     * @return the {@link LambdaClient}
     */
    @Bean
    public LambdaClient lambdaClient() {
        return LambdaClient.builder()
                .region(Region.AP_SOUTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }

    /**
     * Returns the {@link MatchDAO}.
     *
     * @return the {@link MatchDAO}
     */
    @Bean
    public MatchDAO matchDAO() {
        return new MatchDAODynamoDBImpl(dynamoDbClient(), gson());
    }

    /**
     * Returns the {@link MatchService}.
     *
     * @return the {@link MatchService}
     */
    @Bean
    public MatchService matchService() {
        return new MatchServiceImpl(arbitrationService(), matchDAO());
    }

    /**
     * Returns the {@link ArbitrationService}.
     *
     * @return the {@link ArbitrationService}
     */
    @Bean
    public ArbitrationService arbitrationService() {
        return new ArbitrationServiceImpl();
    }

    /**
     * Returns the {@link PlayerService}.
     *
     * @return the {@link PlayerService}
     */
    @Bean
    public PlayerService playerService() {
        return new PlayerServiceImpl(lambdaClient(), gson());
    }

    /**
     * Returns the {@link Gson}.
     *
     * @return the {@link Gson}
     */
    @Bean
    public Gson gson() {
        return new GsonBuilder().create();
    }
}
