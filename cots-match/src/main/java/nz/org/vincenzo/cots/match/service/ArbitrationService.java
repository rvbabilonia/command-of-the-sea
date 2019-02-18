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

import nz.org.vincenzo.cots.domain.Ship;

/**
 * The service for arbitrating a {@link nz.org.vincenzo.cots.domain.Match}.
 *
 * @author Rey Vincent Babilonia
 */
public interface ArbitrationService {

    /**
     * Arbitrates between 2 {@link Ship}s occupying the same {@link Ship.Coordinates}. The winning {@link Ship} is
     * returned; {@code null} if the 2 {@link Ship}s are of the same {@link Ship.ShipClass}.
     *
     * @param attackingShip the attacking {@link Ship}
     * @param defendingShip the defending {@link Ship}
     * @return the winning {@link Ship} or {@code null} if the 2 {@link Ship}s are of the same {@link Ship.ShipClass}
     */
    Ship arbitrate(Ship attackingShip, Ship defendingShip);

    // FIXME
    // A player shall be given 5 minutes to arrange his pieces from the time his opponent joins a match.

    // FIXME
    // A player is allowed a maximum of 2 minutes per move. A player over-stepping this limit three times,
    // automatically loses the game by technicality. The arbiter is under no obligation to warn the player of the
    // approaching time limit.

    // FIXME
    // If no challenge is made after 30 complete moves from the start of the game, the player with more pieces
    // past the mid-point of the board wins the match. If no piece has gone beyond the mid-point, or if there is an
    // equal number of pieces beyond the mid-point, the game is automatically declared a draw.

    // FIXME back and forth
    // A 5-move perpetual position results in a  drawn game. This happens when an attacked piece, which faces
    // immediate challenge, move 5 consecutive move by the same attacking piece.

    // FIXME all over the board
    // A player position results in a drawn game. This happens when an attacked piece, which faces immediate challenge,
    // moves 16 consecutive times through more than 2 squares in order to avoid being challenged on the next, move by
    // the same attacking piece.

    // FIXME
    // 1-hour time limit
}
