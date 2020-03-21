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
package org.vincenzolabs.cots.match.service.impl;

import com.google.gson.Gson;
import org.vincenzolabs.cots.domain.Player;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vincenzolabs.cots.match.service.PlayerService;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;
import software.amazon.awssdk.services.lambda.model.ServiceException;

/**
 * The implementation of {@link PlayerService}.
 *
 * @author Rey Vincent Babilonia
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final LambdaClient lambdaClient;

    private final Gson gson;

    /**
     * Default constructor.
     *
     * @param lambdaClient the {@link LambdaClient}
     */
    @Autowired
    public PlayerServiceImpl(LambdaClient lambdaClient, Gson gson) {
        this.lambdaClient = lambdaClient;
        this.gson = gson;
    }

    @Override
    public Player retrievePlayer(final String refreshToken) {
        if (StringUtils.isBlank(refreshToken)) {
            throw new IllegalArgumentException("refreshToken cookie cannot be null or empty");
        }

        try {
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName("retrievePlayer")
                    .payload(SdkBytes.fromUtf8String(String.format("{\"headers\":{\"Cookie\":\"refreshToken=%s\"}}",
                            refreshToken)))
                    .build();

            InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
            String json = invokeResponse.payload().asUtf8String();

            return gson.fromJson(json, Player.class);
        } catch (ServiceException e) {
            LOGGER.error("Failed to retrieve player: [{}]", e.getMessage(), e);

            return null;
        }
    }

    @Override
    public Player updateStatistics(final String playerUuid, final Result result, String tournament) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID cannot be null or empty");
        }

        try {
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName("updateStatistics")
                    .payload(SdkBytes.fromUtf8String(
                            String.format("{\"playerUuid\":\"%s\",\"result\":\"%s\",\"tournament\":\"%s\"}",
                            playerUuid, result, tournament)))
                    .build();

            InvokeResponse invokeResponse = lambdaClient.invoke(invokeRequest);
            String json = invokeResponse.payload().asUtf8String();

            return gson.fromJson(json, Player.class);
        } catch (ServiceException e) {
            LOGGER.error("Failed to update statistics of player with UUID [{}], result [{}] and tournament [{}]: [{}]",
                    playerUuid, result, tournament, e.getMessage(), e);

            return null;
        }
    }
}
