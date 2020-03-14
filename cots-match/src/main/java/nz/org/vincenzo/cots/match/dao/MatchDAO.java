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
package nz.org.vincenzo.cots.match.dao;

import nz.org.vincenzo.cots.domain.Match;

import java.util.Set;

/**
 * The data access object for matches.
 *
 * @author Rey Vincent Babilonia
 */
public interface MatchDAO {

    /**
     * Creates a {@link Match}.
     *
     * @param playerUuid the UUID of the player hosting the {@link Match}
     * @return the {@link Match}
     */
    Match createMatch(String playerUuid);

    /**
     * Retrieves the {@link Match} matching the given UUID.
     *
     * @param matchUuid the UUID of the {@link Match}
     * @return the {@link Match}
     */
    Match retrieveMatch(String matchUuid);

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
     * Updates a {@link Match}.
     *
     * @param match the {@link Match}
     * @return {@code true} if the {@link Match} has been deleted; {@code false} otherwise
     */
    boolean updateMatch(Match match);

    /**
     * Deletes a {@link Match}.
     *
     * @param match the {@link Match}
     * @return {@code true} if the {@link Match} has been deleted; {@code false} otherwise
     */
    boolean deleteMatch(Match match);
}
