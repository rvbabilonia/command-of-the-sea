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
package nz.org.vincenzo.cots.player.service.impl;

import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.player.dao.PlayerDAO;
import nz.org.vincenzo.cots.player.service.PlayerService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    /**
     * Default constructor.
     *
     * @param playerDAO the {@link PlayerDAO}
     */
    @Autowired
    public PlayerServiceImpl(PlayerDAO playerDAO) {
        this.playerDAO = playerDAO;
    }

    @Override
    public Player createPlayer(final String nickname, final String emailAddress) {
        if (StringUtils.isBlank(nickname)) {
            throw new IllegalArgumentException("Nickname must not be null or empty");
        }

        if (StringUtils.isBlank(emailAddress)) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }

        if (!EmailValidator.getInstance().isValid(emailAddress)) {
            throw new IllegalArgumentException("Email address is invalid");
        }

        Player player = new Player();

        player.setNickname(nickname);
        if (playerDAO.retrievePlayerByNickname(nickname) != null) {
            throw new IllegalArgumentException("Nickname is already in use");
        }

        player.setEmailAddress(emailAddress);
        if (playerDAO.retrievePlayerByEmailAddress(emailAddress) != null) {
            throw new IllegalArgumentException("Email address is already in use");
        }

        return playerDAO.createPlayer(nickname, emailAddress);
    }

    @Override
    public Player retrievePlayerByUuid(final String playerUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        Player player = playerDAO.retrievePlayerByUuid(playerUuid);
        if (player == null) {
            throw new IllegalArgumentException("No player associated with the given UUID");
        }

        return player;
    }

    @Override
    public Player retrievePlayerByAccessToken(final String accessToken) {
        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("Access token must not be null or empty");
        }

        Player player = playerDAO.retrievePlayerByAccessToken(accessToken);
        if (player == null) {
            throw new IllegalArgumentException("No player associated with the given access token");
        }

        return player;
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
    public Set<Player> retrievePlayers() {
        return playerDAO.retrievePlayers();
    }

    @Override
    public String login(final String emailAddress, final String accessToken) {
        // FIXME retrieve access token from Cognito

        if (StringUtils.isBlank(emailAddress)) {
            throw new IllegalArgumentException("Email address must not be null");
        }

        if (StringUtils.isBlank(accessToken)) {
            throw new IllegalArgumentException("Access token must not be null");
        }

        Player player = retrievePlayerByEmailAddress(emailAddress);
        player.setAccessToken(accessToken);
        player.setLastLoginDate(LocalDateTime.now());

        if (playerDAO.updatePlayer(player)) {
            return accessToken;
        }

        return "";
    }

    @Override
    public void logout(final String accessToken) {
        Player player = retrievePlayerByAccessToken(accessToken);
        player.setAccessToken("");

        playerDAO.updatePlayer(player);
    }

    @Override
    public Player updateStatistics(Player player, final String result) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }

        if (StringUtils.isBlank(result)) {
            throw new IllegalArgumentException("Result must not be null or empty");
        }

        Player.Statistics statistics = player.getStatistics() != null ? player.getStatistics()
                : new Player.Statistics();
        if ("win".equalsIgnoreCase(result)) {
            statistics.setWins(statistics.getWins().add(BigDecimal.ONE));
        } else if ("lose".equalsIgnoreCase(result)) {
            statistics.setLosses(statistics.getLosses().add(BigDecimal.ONE));
        } else if ("draw".equalsIgnoreCase(result)) {
            statistics.setDraws(statistics.getDraws().add(BigDecimal.ONE));
        } else {
            throw new IllegalArgumentException("Unknown result");
        }
        player.setStatistics(statistics);

        playerDAO.updatePlayer(player);

        return player;
    }

    @Override
    public Player updateTournamentStatistics(Player player, final String result, final String tournament) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }

        if (StringUtils.isBlank(result)) {
            throw new IllegalArgumentException("Result must not be null or empty");
        }

        Player.Statistics statistics = player.getTournamentStatistics()
                .getOrDefault(tournament, new Player.Statistics());
        if ("win".equalsIgnoreCase(result)) {
            statistics.setWins(statistics.getWins().add(BigDecimal.ONE));
        } else if ("lose".equalsIgnoreCase(result)) {
            statistics.setLosses(statistics.getLosses().add(BigDecimal.ONE));
        } else if ("draw".equalsIgnoreCase(result)) {
            statistics.setDraws(statistics.getDraws().add(BigDecimal.ONE));
        } else {
            throw new IllegalArgumentException("Unknown result");
        }
        player.setTournamentStatistics(Map.of(tournament, statistics));

        playerDAO.updatePlayer(player);

        return player;
    }

    @Override
    public Player updateAvatar(final String accessToken, final String avatar) {
        Player existentPlayer = retrievePlayerByAccessToken(accessToken);
        existentPlayer.setAvatar(avatar);

        playerDAO.updatePlayer(existentPlayer);

        return existentPlayer;
    }
}
