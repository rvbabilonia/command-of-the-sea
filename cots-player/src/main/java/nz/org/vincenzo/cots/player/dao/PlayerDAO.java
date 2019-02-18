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
package nz.org.vincenzo.cots.player.dao;

import nz.org.vincenzo.cots.domain.Player;

import java.util.List;

/**
 * The domain access object for player.
 *
 * @author Rey Vincent Babilonia
 */
public interface PlayerDAO {

    /**
     * Creates a {@link Player}.
     *
     * @param nickname     the nickname
     * @param emailAddress the email address
     * @param password     the scrypt-hashed password
     * @return the newly-created {@link Player}
     */
    Player createPlayer(String nickname, String emailAddress, String password);

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
     * @param player     the {@link Player}
     * @param indexNames the index names
     * @return the {@link List} of {@link Player}s
     */
    List<Player> retrievePlayers(Player player, String... indexNames);

    /**
     * Returns the {@link List} of {@link Player}s .
     *
     * @return the {@link List} of {@link Player}s
     */
    List<Player> retrievePlayers();

    /**
     * Updates a {@link Player}.
     *
     * @param player the {@link Player}
     */
    void updatePlayer(Player player);
}
