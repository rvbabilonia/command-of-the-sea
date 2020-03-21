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
package org.vincenzolabs.cots.player.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vincenzolabs.cots.domain.Player;
import org.vincenzolabs.cots.domain.UserInformation;
import org.vincenzolabs.cots.player.dao.PlayerDAO;
import org.vincenzolabs.cots.player.service.CognitoService;
import org.vincenzolabs.cots.player.service.PlayerService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

/**
 * The implementation of {@link PlayerService}.
 *
 * @author Rey Vincent Babilonia
 */
@Service
public class PlayerServiceImpl implements PlayerService {

    private final PlayerDAO playerDAO;

    private final CognitoService cognitoService;

    /**
     * Default constructor.
     *
     * @param playerDAO      the {@link PlayerDAO}
     * @param cognitoService the {@link CognitoService}
     */
    @Autowired
    public PlayerServiceImpl(PlayerDAO playerDAO, CognitoService cognitoService) {
        this.playerDAO = playerDAO;
        this.cognitoService = cognitoService;
    }

    @Override
    public Player createPlayer(final String refreshToken, final String nickname) {
        if (StringUtils.isBlank(nickname)) {
            throw new IllegalArgumentException("Nickname must not be null or empty");
        }

        if (playerDAO.retrievePlayerByNickname(nickname) != null) {
            throw new IllegalArgumentException("Nickname is already in use");
        }

        UserInformation userInformation = getUserInformation(refreshToken);

        String uuid = userInformation.getSubject();
        if (playerDAO.retrievePlayerByUuid(uuid) != null) {
            throw new IllegalArgumentException("UUID is already in use");
        }

        String emailAddress = userInformation.getEmailAddress();
        if (playerDAO.retrievePlayerByEmailAddress(emailAddress) != null) {
            throw new IllegalArgumentException("Email address is already in use");
        }

        return playerDAO.createPlayer(uuid, nickname, emailAddress);
    }

    @Override
    public Player retrievePlayer(final String refreshToken) {
        UserInformation userInformation = getUserInformation(refreshToken);

        Player player = retrievePlayerByUuid(userInformation.getSubject());

        if (player == null) {
            player = new Player();
            player.setUuid(userInformation.getSubject());
            player.setEmailAddress(userInformation.getEmailAddress());
            player.setNickname(userInformation.getUsername());
        } else {
            player.setLastLoginDate(LocalDateTime.now());
            playerDAO.updatePlayer(player);
        }

        return player;
    }

    @Override
    public Player retrievePlayerByUuid(final String playerUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        return playerDAO.retrievePlayerByUuid(playerUuid);
    }

    @Override
    public Player retrievePlayerByNickname(final String nickname) {
        if (StringUtils.isBlank(nickname)) {
            throw new IllegalArgumentException("Nickname must not be null or empty");
        }

        Player player = playerDAO.retrievePlayerByNickname(nickname);
        if (player == null) {
            throw new IllegalArgumentException("No player associated with the given nickname");
        }

        return player;
    }

    @Override
    public Player retrievePlayerByEmailAddress(final String emailAddress) {
        if (StringUtils.isBlank(emailAddress)) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }

        Player player = playerDAO.retrievePlayerByEmailAddress(emailAddress);
        if (player == null) {
            throw new IllegalArgumentException("No player associated with the given email address");
        }

        return player;
    }

    @Override
    public Set<Player> retrievePlayers(final String tournament) {
        return playerDAO.retrievePlayers(tournament);
    }

    @Override
    public void logout(final String refreshToken) {
        String accessToken = cognitoService.getToken(CognitoService.GrantType.REFRESH_TOKEN, refreshToken)
                .getAccessToken();

        cognitoService.signOut(accessToken);
    }

    @Override
    public Player updateStatistics(Player player, Result result) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }

        if (result == null) {
            throw new IllegalArgumentException("Result must not be null");
        }

        Player.Statistics statistics = player.getStatistics() != null ? player.getStatistics()
                : new Player.Statistics();
        switch (result) {
            case WIN:
                statistics.setWins(statistics.getWins().add(BigDecimal.ONE));
                break;
            case LOSE:
                statistics.setLosses(statistics.getLosses().add(BigDecimal.ONE));
                break;
            case DRAW:
                statistics.setDraws(statistics.getDraws().add(BigDecimal.ONE));
                break;
        }
        player.setStatistics(statistics);

        playerDAO.updatePlayer(player);

        return player;
    }

    @Override
    public Player updateStatistics(Player player, Result result, final String tournament) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }

        if (result == null) {
            throw new IllegalArgumentException("Result must not be null");
        }

        Player.Statistics tournamentStatistics = player.getTournamentStatistics()
                .getOrDefault(tournament, new Player.Statistics());
        switch (result) {
            case WIN:
                tournamentStatistics.setWins(tournamentStatistics.getWins().add(BigDecimal.ONE));
                break;
            case LOSE:
                tournamentStatistics.setLosses(tournamentStatistics.getLosses().add(BigDecimal.ONE));
                break;
            case DRAW:
                tournamentStatistics.setDraws(tournamentStatistics.getDraws().add(BigDecimal.ONE));
                break;
        }
        player.setTournamentStatistics(Map.of(tournament, tournamentStatistics));

        playerDAO.updatePlayer(player);

        return player;
    }

    @Override
    public Player updateAvatar(final String refreshToken, final String avatar) {
        UserInformation userInformation = getUserInformation(refreshToken);

        if (userInformation == null) {
            return null;
        }

        Player player = retrievePlayerByUuid(userInformation.getSubject());

        if (player == null) {
            return null;
        }

        player.setAvatar(avatar);

        playerDAO.updatePlayer(player);

        return player;
    }

    @Override
    public void deletePlayer(final String refreshToken) {
        UserInformation userInformation = getUserInformation(refreshToken);

        if (userInformation == null) {
            return;
        }

        playerDAO.deletePlayer(userInformation.getSubject());
    }

    private UserInformation getUserInformation(String refreshToken) {
        String accessToken = cognitoService.getToken(CognitoService.GrantType.REFRESH_TOKEN, refreshToken)
                .getAccessToken();

        return cognitoService.getUserInformation(accessToken);
    }
}
