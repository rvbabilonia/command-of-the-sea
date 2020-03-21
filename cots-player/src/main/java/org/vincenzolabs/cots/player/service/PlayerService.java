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
package org.vincenzolabs.cots.player.service;

import org.json.simple.parser.ParseException;
import org.vincenzolabs.cots.domain.Player;

import java.util.Set;

/**
 * The service for player.
 *
 * @author Rey Vincent Babilonia
 */
public interface PlayerService {

    /**
     * Creates a {@link Player}.
     *
     * @param refreshToken the refresh token
     * @param nickname     the nickname
     * @return the newly-created {@link Player}
     */
    Player createPlayer(String refreshToken, String nickname) throws ParseException;

    /**
     * Returns a dummy or existing {@link Player}.
     *
     * @param refreshToken the refresh token
     * @return the dummy or existing {@link Player}
     */
    Player retrievePlayer(String refreshToken);

    /**
     * Returns the {@link Player} matching the given UUID.
     *
     * @param playerUuid the UUID
     * @return the {@link Player} or null
     */
    Player retrievePlayerByUuid(String playerUuid);

    /**
     * Returns the {@link Player} matching the given nickname.
     *
     * @param nickname the nickname
     * @return the {@link Player}
     */
    Player retrievePlayerByNickname(String nickname);

    /**
     * Returns the {@link Player} matching the given email address.
     *
     * @param emailAddress the email address
     * @return the {@link Player}
     */
    Player retrievePlayerByEmailAddress(String emailAddress);

    /**
     * Returns the {@link Set} of {@link Player}s according to their number of wins.
     *
     * @param tournament the tournament
     * @return the {@link Set} of {@link Player}s
     */
    Set<Player> retrievePlayers(String tournament);

    /**
     * Logs out a {@link Player}.
     *
     * @param refreshToken the refresh token
     */
    void logout(String refreshToken);

    /**
     * Updates a {@link Player}'s {@link Player.Statistics}.
     *
     * @param player the {@link Player}
     */
    Player updateStatistics(Player player, Result result);

    /**
     * Updates a {@link Player}'s tournament {@link Player.Statistics}.
     *
     * @param player the {@link Player}
     */
    Player updateStatistics(Player player, Result result, String tournament);

    /**
     * Updates a {@link Player}'s avatar.
     *
     * @param refreshToken the refresh token
     * @param avatar       the avatar
     * @return the {@link Player}
     */
    Player updateAvatar(String refreshToken, String avatar);

    /**
     * Deletes a {@link Player}.
     *
     * @param refreshToken the refresh token
     */
    void deletePlayer(String refreshToken);

    /**
     * The enumeration of match results.
     */
    enum Result {

        /**
         * Player won.
         */
        WIN,
        /**
         * Player lost.
         */
        LOSE,
        /**
         * Match was a draw.
         */
        DRAW
    }
}
