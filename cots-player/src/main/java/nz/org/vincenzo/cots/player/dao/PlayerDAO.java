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

import java.util.Set;

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
     * @return the newly-created {@link Player}
     */
    Player createPlayer(String nickname, String emailAddress);

    /**
     * Returns the {@link Player} matching the given UUID.
     *
     * @param playerUuid the UUID of the {@link Player}
     * @return the {@link Player}
     */
    Player retrievePlayerByUuid(String playerUuid);

    /**
     * Returns the {@link Player} matching the given access token from a secondary index.
     *
     * @param accessToken the access token
     * @return the {@link Player}
     */
    Player retrievePlayerByAccessToken(String accessToken);

    /**
     * Returns the {@link Player} matching the given email address from a secondary index.
     *
     * @param emailAddress the email address
     * @return the {@link Player}
     */
    Player retrievePlayerByEmailAddress(String emailAddress);

    /**
     * Returns the {@link Player} matching the given nickname from a secondary index.
     *
     * @param nickname the nickname
     * @return the {@link Player}
     */
    Player retrievePlayerByNickname(String nickname);

    /**
     * Returns the {@link Set} of {@link Player}s .
     *
     * @return the {@link Set} of {@link Player}s
     */
    Set<Player> retrievePlayers();

    /**
     * Updates a {@link Player}.
     *
     * @param player the {@link Player}
     * @return {@code true} if the {@link Player} has been updated; {@code false} otherwise
     */
    boolean updatePlayer(Player player);

    /**
     * Deletes a {@link Player}.
     *
     * @param playerUUid the UUID of the {@link Player}
     * @return {@code true} if the {@link Player} has been deleted; {@code false} otherwise
     */
    boolean deletePlayer(String playerUUid);
}
