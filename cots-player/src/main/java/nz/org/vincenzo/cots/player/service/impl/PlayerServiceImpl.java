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

import com.amazonaws.util.StringUtils;
import com.lambdaworks.crypto.SCryptUtil;
import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.player.dao.PlayerDAO;
import nz.org.vincenzo.cots.player.service.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.validator.routines.EmailValidator;

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
    public Player createPlayer(final String nickname, final String emailAddress, final String emailAddressVerification,
                               final String password, final String passwordVerification) {
        if (StringUtils.isNullOrEmpty(nickname)) {
            throw new IllegalArgumentException("Nickname must not be null or empty");
        }

        if (StringUtils.isNullOrEmpty(emailAddress)) {
            throw new IllegalArgumentException("Email address must not be null or empty");
        }

        if (!emailAddress.equals(emailAddressVerification)) {
            throw new IllegalArgumentException("Email addresses must match");
        }

        if (!EmailValidator.getInstance().isValid(emailAddress)) {
            throw new IllegalArgumentException("Email address is invalid");
        }

        if (StringUtils.isNullOrEmpty(password)) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }

        if (!password.equals(passwordVerification)) {
            throw new IllegalArgumentException("Passwords must match");
        }

        Player player = new Player();

        player.setNickname(nickname);
        if (!playerDAO.retrievePlayers(player, "nicknames").isEmpty()) {
            throw new IllegalArgumentException("Nickname is already in use");
        }

        player.setEmailAddress(emailAddress);
        if (!playerDAO.retrievePlayers(player, "emailAddresses").isEmpty()) {
            throw new IllegalArgumentException("Email address is already in use");
        }

        return playerDAO.createPlayer(nickname, emailAddress, SCryptUtil.scrypt(password, 16, 8, 1));
    }

    @Override
    public Player retrievePlayerByUuid(final String playerUuid) {
        if (StringUtils.isNullOrEmpty(playerUuid)) {
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
        if (StringUtils.isNullOrEmpty(accessToken)) {
            throw new IllegalArgumentException("Access token must not be null or empty");
        }

        Player player = playerDAO.retrievePlayerByAccessToken(accessToken);
        if (player == null) {
            throw new IllegalArgumentException("No player associated with the given access token");
        }

        return player;
    }

    @Override
    public List<Player> retrievePlayers() {
        return playerDAO.retrievePlayers();
    }

    @Override
    public String login(final String emailAddress, final String password) {
        if (StringUtils.isNullOrEmpty(emailAddress)) {
            throw new IllegalArgumentException("Email address must not be null");
        }

        if (StringUtils.isNullOrEmpty(password)) {
            throw new IllegalArgumentException("Password must not be null");
        }

        Player player = new Player();
        player.setEmailAddress(emailAddress);

        List<Player> players = playerDAO.retrievePlayers(player, "emailAddresses");
        if (players.isEmpty()) {
            throw new IllegalArgumentException("Email address is invalid");
        }

        Player existentPlayer = retrievePlayerByUuid(players.get(0).getUuid());

        if (!SCryptUtil.check(password, existentPlayer.getPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        existentPlayer.setAccessToken(UUID.randomUUID().toString());
        existentPlayer.setLastLoginDate(Date.from(OffsetDateTime.now().toInstant()));

        playerDAO.updatePlayer(existentPlayer);

        return existentPlayer.getAccessToken();
    }

    @Override
    public void logout(final String accessToken) {
        Player player = retrievePlayerByAccessToken(accessToken);

        player.setAccessToken("");

        playerDAO.updatePlayer(player);
    }

    @Override
    public Player updatePlayer(Player player, final String result) {
        if (player == null) {
            throw new IllegalArgumentException("Player must not be null");
        }

        if (StringUtils.isNullOrEmpty(result)) {
            throw new IllegalArgumentException("Result must not be null or empty");
        }

        Player.Statistics statistics = player.getStatistics();
        if ("win".equalsIgnoreCase(result)) {
            statistics.setWins(statistics.getWins().add(BigDecimal.ONE));
        } else if ("lose".equalsIgnoreCase(result)) {
            statistics.setLosses(statistics.getLosses().add(BigDecimal.ONE));
        } else if ("draw".equalsIgnoreCase(result)) {
            statistics.setDraws(statistics.getDraws().add(BigDecimal.ONE));
        } else {
            throw new IllegalArgumentException("Unknown result");
        }

        playerDAO.updatePlayer(player);

        return player;
    }

    @Override
    public Player updatePlayer(final String accessToken, final String nickname, final String password,
                               final String passwordVerification, final String avatar) {
        if (StringUtils.isNullOrEmpty(nickname)) {
            throw new IllegalArgumentException("Nickname must not be null or empty");
        }

        if (StringUtils.isNullOrEmpty(password)) {
            throw new IllegalArgumentException("Password must not be null or empty");
        }

        if (!password.equals(passwordVerification)) {
            throw new IllegalArgumentException("Passwords must match");
        }

        Player player = new Player();
        player.setNickname(nickname);

        List<Player> players = playerDAO.retrievePlayers(player, "nicknames");
        if (!players.isEmpty()) {
            throw new IllegalArgumentException("Nickname is already in use");
        }

        Player existentPlayer = retrievePlayerByAccessToken(accessToken);

        existentPlayer.setNickname(nickname);
        existentPlayer.setPassword(SCryptUtil.scrypt(password, 16, 8, 1));
        existentPlayer.setAvatar(avatar);

        playerDAO.updatePlayer(existentPlayer);

        return existentPlayer;
    }
}
