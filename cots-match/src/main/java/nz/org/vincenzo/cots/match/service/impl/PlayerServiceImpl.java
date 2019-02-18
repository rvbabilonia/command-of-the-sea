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
package nz.org.vincenzo.cots.match.service.impl;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.util.StringUtils;
import com.google.gson.Gson;
import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.match.handler.Response;
import nz.org.vincenzo.cots.match.service.PlayerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * The implementation of {@link PlayerService}.
 *
 * @author Rey Vincent Babilonia
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlayerServiceImpl.class);

    private final AWSLambda awsLambda;

    private final Gson gson;

    /**
     * Default constructor.
     *
     * @param awsLambda the {@link AWSLambda}
     */
    @Autowired
    public PlayerServiceImpl(AWSLambda awsLambda, Gson gson) {
        this.awsLambda = awsLambda;
        this.gson = gson;
    }

    @Override
    public Player retrievePlayer(final String accessToken) {
        if (StringUtils.isNullOrEmpty(accessToken)) {
            throw new IllegalArgumentException("Access token cannot be null or empty");
        }

        try {
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName("retrievePlayer");
            invokeRequest.setPayload(String.format("{\"headers\":{\"Authorization\":\"Bearer %s\"}}",
                    accessToken));

            InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
            ByteBuffer payload = invokeResult.getPayload();
            String json = new String(payload.array(), Charset.forName("UTF-8"));

            Response response = gson.fromJson(json, Response.class);

            return gson.fromJson(response.getBody(), Player.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            return null;
        }
    }

    @Override
    public Player retrievePlayerByUuid(final String playerUuid) {
        if (StringUtils.isNullOrEmpty(playerUuid)) {
            throw new IllegalArgumentException("Player UUID cannot be null or empty");
        }

        try {
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName("retrievePlayerByUuid");
            invokeRequest.setPayload(String.format("{\"body\":\"{\"playerUuid\":\"%s\"}\"}", playerUuid));

            InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
            ByteBuffer payload = invokeResult.getPayload();
            String json = payload.toString();

            Response response = gson.fromJson(json, Response.class);

            return gson.fromJson(response.getBody(), Player.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            return null;
        }
    }

    @Override
    public Player updateStatistics(final String playerUuid, final Result result) {
        if (StringUtils.isNullOrEmpty(playerUuid)) {
            throw new IllegalArgumentException("Player UUID cannot be null or empty");
        }

        try {
            InvokeRequest invokeRequest = new InvokeRequest();
            invokeRequest.setFunctionName("updateStatistics");
            invokeRequest.setPayload(String.format("{\"playerUuid\":\"%s\",\"result\":\"%s\"}", playerUuid, result));

            InvokeResult invokeResult = awsLambda.invoke(invokeRequest);
            ByteBuffer payload = invokeResult.getPayload();
            String json = payload.toString();

            Response response = gson.fromJson(json, Response.class);

            return gson.fromJson(response.getBody(), Player.class);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);

            return null;
        }
    }
}
