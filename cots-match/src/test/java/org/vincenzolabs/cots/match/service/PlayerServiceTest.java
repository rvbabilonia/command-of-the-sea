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
package org.vincenzolabs.cots.match.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.vincenzolabs.cots.domain.Player;
import org.vincenzolabs.cots.match.service.impl.PlayerServiceImpl;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * The test case for {@link PlayerService}.
 *
 * @author Rey Vincent Babilonia
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlayerServiceTest {

    private static final Gson GSON = new GsonBuilder().create();

    @Mock
    private LambdaClient lambdaClient;

    private PlayerService playerService;

    @BeforeEach
    void setUp() {
        playerService = new PlayerServiceImpl(lambdaClient, GSON);
    }

    private Player createPlayer() {
        Player player = new Player();
        player.setUuid(UUID.randomUUID().toString());
        player.setEmailAddress("player@vincenzo.org.nz");
        player.setNickname("pl@y3r");
        player.setAvatar("https://vincenzo.org.nz/images/" + player.getUuid() + ".png");
        player.setRegistrationDate(LocalDateTime.now().minusMonths(2));
        player.setLastLoginDate(LocalDateTime.now().minusMinutes(2));

        Player.Statistics statistics = new Player.Statistics();
        statistics.setWins(BigDecimal.TEN);
        statistics.setDraws(BigDecimal.ONE);
        statistics.setLosses(BigDecimal.ZERO);
        player.setStatistics(statistics);

        return player;
    }

    @Test
    void retrievePlayer() {
        Player expected = createPlayer();

        InvokeResponse invokeResponse = InvokeResponse.builder()
                .payload(SdkBytes.fromUtf8String(GSON.toJson(expected)))
                .build();
        when(lambdaClient.invoke(any(InvokeRequest.class))).thenReturn(invokeResponse);

        assertThat(playerService.retrievePlayer("access-token")).isEqualTo(expected);
    }

    @Test
    void retrievePlayerWithoutAccessToken() {
        assertThatThrownBy(() -> playerService.retrievePlayer(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("refreshToken cookie cannot be null or empty");
    }

    @Test
    void updateStatistics() {
        Player expected = createPlayer();

        InvokeResponse invokeResponse = InvokeResponse.builder()
                .payload(SdkBytes.fromUtf8String(GSON.toJson(expected)))
                .build();
        when(lambdaClient.invoke(any(InvokeRequest.class))).thenReturn(invokeResponse);

        assertThat(playerService.updateStatistics("uuid", PlayerService.Result.LOSE, null)).isEqualTo(expected);
    }

    @Test
    void updateStatisticsWithoutUuid() {
        assertThatThrownBy(() -> playerService.updateStatistics("", PlayerService.Result.WIN, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Player UUID cannot be null or empty");
    }
}
