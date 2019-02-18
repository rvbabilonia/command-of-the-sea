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
package nz.org.vincenzo.cots.player.service;

import nz.org.vincenzo.cots.domain.Player;

import java.util.List;

/**
 * The service for player.
 *
 * @author Rey Vincent Babilonia
 */
public interface PlayerService {

    /**
     * Creates a {@link Player}.
     *
     * @param nickname                 the nickname
     * @param emailAddress             the email address
     * @param emailAddressVerification the email address for verification
     * @param password                 the password
     * @param passwordVerification     the password for verification
     * @return the newly-created {@link Player}
     */
    Player createPlayer(String nickname, String emailAddress, String emailAddressVerification, String password,
                        String passwordVerification);

    /**
     * Returns the {@link Player} matching the given UUID.
     *
     * @param playerUuid the UUID
     * @return the {@link Player}
     */
    Player retrievePlayerByUuid(String playerUuid);

    /**
     * Returns the {@link Player} matching the given access token.
     *
     * @param accessToken the access token
     * @return the {@link Player}
     */
    Player retrievePlayerByAccessToken(String accessToken);

    /**
     * Returns the {@link List} of {@link Player}s matching the given filters.
     *
     * @return the {@link List} of {@link Player}s
     */
    List<Player> retrievePlayers();

    /**
     * Authenticates a {@link Player}.
     *
     * @param emailAddress the email address
     * @param password     the password
     * @return the access token
     */
    String login(String emailAddress, String password);

    /**
     * Logs out a {@link Player}.
     *
     * @param accessToken the access token
     */
    void logout(String accessToken);

    /**
     * Updates a {@link Player}'s {@link Player.Statistics}.
     *
     * @param player the {@link Player}
     */
    Player updatePlayer(Player player, String result);

    /**
     * Updates a {@link Player}'s details.
     *
     * @param accessToken          the access token
     * @param nickname             the nickname
     * @param password             the password
     * @param passwordVerification the password verification
     * @param avatar               the avatar
     * @return the {@link Player}
     */
    Player updatePlayer(String accessToken, String nickname, String password, String passwordVerification,
                        String avatar);
}
