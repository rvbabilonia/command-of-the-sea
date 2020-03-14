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
package nz.org.vincenzo.cots.match.service;

import nz.org.vincenzo.cots.domain.Match;
import nz.org.vincenzo.cots.domain.Player;
import nz.org.vincenzo.cots.domain.Ship;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The service for a {@link Match}.
 *
 * @author Rey Vincent Babilonia
 */
public interface MatchService {

    /**
     * Hosts a {@link Match}.
     *
     * @param playerUuid the UUID of the {@link Player} hosting the {@link Match}
     * @return a {@link Match}
     */
    Match hostMatch(String playerUuid);

    /**
     * Cancels a {@link Match}.
     *
     * @param playerUuid the UUID of the {@link Player} hosting the {@link Match}
     * @param matchUuid  the UUID of the {@link Match}
     */
    void cancelMatch(String playerUuid, String matchUuid);

    /**
     * Connects to a {@link Match}. A {@link Player} cannot connect to a {@link Match} that has already started or has 2
     * {@link Player}s.
     *
     * @param playerUuid the UUID of the {@link Player} joining the {@link Match}
     * @param matchUuid  the UUID of the {@link Match}
     * @return the {@link Match}
     */
    Match connectToMatch(String playerUuid, String matchUuid);

    /**
     * Disconnects from a {@link Match}. A {@link Player} cannot disconnect from a {@link Match} that has already
     * started. He can either resign or offer a draw.
     *
     * @param playerUuid the UUID of the {@link Player} disconnecting from the {@link Match}
     * @param matchUuid  the UUID of the {@link Match}
     */
    void disconnectFromMatch(String playerUuid, String matchUuid);

    /**
     * Positions a {@link Player}'s {@link Ship} before the start of the {@link Match}. The {@link Ship}s must be
     * within the first 3 rows closest to the {@link Player}.
     *
     * @param playerUuid the UUID of the {@link Player}
     * @param matchUuid  the UUID of the {@link Match}
     * @param ship       the {@link Ship}
     * @return the {@link Map} containing the UUID's of the {@link Player}s and their {@link Ship}s
     */
    Map<String, Set<Ship>> positionShip(String playerUuid, String matchUuid, Ship ship);

    /**
     * Signals that the {@link Player} is ready to start the {@link Match}.
     *
     * @param playerUuid the UUID of the {@link Player}
     * @param matchUuid  the UUID of the {@link Match}
     * @return the {@link Match}
     */
    Match ready(String playerUuid, String matchUuid);

    /**
     * Starts a {@link Match} when all {@link Player}s have positioned their {@link Ship}s and all {@link Player}s are
     * ready.
     *
     * @param playerUuid the UUID of the {@link Player}
     * @param matchUuid  the UUID of the {@link Match}
     * @return the {@link Match}
     */
    Match startMatch(String playerUuid, String matchUuid);

    /**
     * Moves a {@link Ship} and updates the board game. This method first validates if the {@link Player}'s UUID from
     * the UUID belongs to the {@link Match} and it is his turn. This will also check if the {@link Ship.Coordinates}
     * are valid and that it has changed. In the event of challenging an opponent's {@link Ship}, the
     * {@link ArbitrationService#arbitrate(Ship, Ship)} will be called. The player turn will be toggled after a
     * successful move.
     *
     * @param playerUuid the UUID of the {@link Player}
     * @param matchUuid  the UUID of the {@link Match}
     * @param ship       the {@link Ship}
     * @return the {@link Map} containing the UUID's of the {@link Player}s and their {@link Ship}s
     */
    Map<String, Set<Ship>> moveShip(String playerUuid, String matchUuid, Ship ship);

    /**
     * Shows the turns that both {@link Player}s made and is only available after the match is over.
     *
     * @param matchUuid the UUID of the {@link Match}
     * @return the {@link List} of all the {@link Ship}s per turn
     */
    List<Map<String, Set<Ship>>> replay(String matchUuid);

    /**
     * Returns the {@link Set} of finished {@link Match}es.
     *
     * @return the {@link Set} of finished {@link Match}es
     */
    Set<Match> retrieveFinishedMatches();

    /**
     * Returns the {@link Set} of ongoing {@link Match}es.
     *
     * @return the {@link Set} of ongoing {@link Match}es
     */
    Set<Match> retrieveActiveMatches();

    /**
     * Returns the {@link Set} of new {@link Match}es.
     *
     * @return the {@link Set} of new {@link Match}es
     */
    Set<Match> retrieveUnstartedMatches();

    /**
     * Resigns from the {@link Match}. The {@link Player} who resigned losses.
     *
     * @param playerUuid the UUID of the resigning {@link Player}
     * @param matchUuid  the UUID of the {@link Match}
     * @return the {@link Match}
     */
    Match resign(String playerUuid, String matchUuid);

    /**
     * Offers or agrees to a draw out of respect.
     *
     * @param playerUuid the UUID of the {@link Player}
     * @param matchUuid  the UUID of the {@link Match}
     * @return the {@link Match}
     */
    Match draw(String playerUuid, String matchUuid);

    /**
     * Retrieves the {@link Match} with the given UUID. This is only for administrators.
     *
     * @param matchUuid the UUID of the {@link Match}
     * @return the {@link Match}
     */
    Match retrieveMatch(String matchUuid);

    /**
     * Deletes a {@link Match} with the given UUID. Only the creator of the {@link Match} or an administrator can
     * perform this operation.
     *
     * @param playerUuid the UUID of the {@link Player}
     * @param matchUuid  the UUID of the {@link Match}
     * @return {@code true} if the {@link Match} has been deleted; {@code false} otherwise
     */
    boolean deleteMatch(String playerUuid, String matchUuid);
}
