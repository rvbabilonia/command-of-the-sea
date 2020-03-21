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

import org.vincenzolabs.cots.domain.Player;

/**
 * The player service.
 *
 * @author Rey Vincent Babilonia
 */
public interface PlayerService {

    /**
     * Returns the {@link Player} matching the given refresh token.
     *
     * @param refreshToken the refresh token
     * @return the {@link Player}
     */
    Player retrievePlayer(String refreshToken);

    /**
     * Updates the {@link Player}'s {@link Player.Statistics}.
     *
     * @param playerUuid the UUID of the {@link Player}
     * @param result     the {@link Result} of the match
     * @param tournament the tournament
     * @return the updated {@link Player}
     */
    Player updateStatistics(String playerUuid, Result result, String tournament);

    /**
     * The enumeration of the possible results of a match.
     */
    enum Result {
        /**
         * Player wins.
         */
        WIN,
        /**
         * Player loses.
         */
        LOSE,
        /**
         * Match ended in a draw.
         */
        DRAW
    }
}
