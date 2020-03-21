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
package org.vincenzolabs.cots.player.configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.json.simple.parser.JSONParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.vincenzolabs.cots.player.dao.PlayerDAO;
import org.vincenzolabs.cots.player.dao.impl.PlayerDAODynamoDBImpl;
import org.vincenzolabs.cots.player.service.CognitoService;
import org.vincenzolabs.cots.player.service.PlayerService;
import org.vincenzolabs.cots.player.service.impl.CognitoServiceImpl;
import org.vincenzolabs.cots.player.service.impl.PlayerServiceImpl;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.ssm.SsmClient;

import javax.ws.rs.client.Client;

/**
 * The player configuration.
 *
 * @author Rey Vincent Babilonia
 */
@Configuration
public class PlayerConfiguration {

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
     * Returns the {@link CognitoIdentityProviderClient}.
     *
     * @return the {@link CognitoIdentityProviderClient}
     */
    @Bean
    public CognitoIdentityProviderClient cognitoIdentityProviderClient() {
        return CognitoIdentityProviderClient.builder()
                .region(Region.AP_SOUTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }

    /**
     * Returns the {@link SsmClient}.
     *
     * @return the {@link SsmClient}
     */
    @Bean
    public SsmClient ssmClient() {
        return SsmClient.builder()
                .region(Region.AP_SOUTHEAST_2)
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .build();
    }

    /**
     * Returns the {@link CognitoService}.
     *
     * @return the {@link CognitoService}
     */
    @Bean
    public CognitoService cognitoService() {
        return new CognitoServiceImpl(cognitoIdentityProviderClient(), ssmClient(), client(), jsonParser(),
                gson());
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
     * Returns the {@link Gson}.
     *
     * @return the {@link Gson}
     */
    @Bean
    public Gson gson() {
        return new GsonBuilder().create();
    }

    /**
     * Returns the {@link PlayerDAO}.
     *
     * @return the {@link PlayerDAO}
     */
    @Bean
    public PlayerDAO playerDAO() {
        return new PlayerDAODynamoDBImpl(dynamoDbClient(), gson());
    }

    /**
     * Returns the {@link PlayerService}.
     *
     * @return the {@link PlayerService}
     */
    @Bean
    public PlayerService playerService() {
        return new PlayerServiceImpl(playerDAO(), cognitoService());
    }

    /**
     * Returns the {@link Client}.
     *
     * @return the {@link Client}
     */
    @Bean
    public Client client() {
        return JerseyClientBuilder.newClient();
    }
}
