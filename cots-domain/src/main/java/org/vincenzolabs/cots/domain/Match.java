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
package org.vincenzolabs.cots.domain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The domain model object for match.
 *
 * @author Rey Vincent Babilonia
 */
public class Match {

    @Expose
    private String uuid;

    @Expose
    private String whitePlayer;

    @Expose
    private String blackPlayer;

    @Expose
    private String winner;

    @Expose
    private String loser;

    @Expose
    private boolean draw;

    @Expose
    private boolean whitePlayerAgreedToDraw;

    @Expose
    private boolean blackPlayerAgreedToDraw;

    @Expose
    private String host;

    @Expose
    private LocalDateTime creationDate;

    @Expose
    private Ship.Color turn;

    @Expose
    private boolean started;

    @Expose
    private LocalDateTime startDate;

    @Expose
    private LocalDateTime endDate;

    @Expose
    private boolean whitePlayerReady;

    @Expose
    private boolean blackPlayerReady;

    @Expose
    private List<Map<String, Set<Ship>>> moves = new LinkedList<>();

    @Expose
    private Map<String, Set<Ship>> fleets = new HashMap<>();

    /**
     * Returns the universally unique identifier.
     *
     * @return the universally unique identifier
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * Sets the universally unique identifier.
     *
     * @param uuid the universally unique identifier
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Returns the UUID of the white {@link Player}.
     *
     * @return the UUID of the white {@link Player}
     */
    public String getWhitePlayer() {
        return whitePlayer;
    }

    /**
     * Sets the UUID of the white {@link Player}.
     *
     * @param whitePlayer the UUID of the white {@link Player}
     */
    public void setWhitePlayer(String whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    /**
     * Returns the UUID of the black {@link Player}.
     *
     * @return the UUID of the black {@link Player}
     */
    public String getBlackPlayer() {
        return blackPlayer;
    }

    /**
     * Sets the UUID of the black {@link Player}.
     *
     * @param blackPlayer the UUID of the black {@link Player}
     */
    public void setBlackPlayer(String blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    /**
     * Returns the UUID of the winner.
     *
     * @return the UUID of the winner
     */
    public String getWinner() {
        return winner;
    }

    /**
     * Sets the UUID of the winner.
     *
     * @param winner the UUID of the winner
     */
    public void setWinner(String winner) {
        this.winner = winner;
    }

    /**
     * Returns the UUID of the loser.
     *
     * @return the UUID of the loser
     */
    public String getLoser() {
        return loser;
    }

    /**
     * Sets the UUID of the loser.
     *
     * @param loser the UUID of the loser
     */
    public void setLoser(String loser) {
        this.loser = loser;
    }

    /**
     * Checks if the match ended in a draw.
     *
     * @return {@code true} if the match ended in a draw; {@code false} otherwise
     */
    public boolean isDraw() {
        return draw;
    }

    /**
     * Sets the flag if the match ended in a draw.
     *
     * @param draw {@code true} if the match ended in a draw; {@code false} otherwise
     */
    public void setDraw(boolean draw) {
        this.draw = draw;
    }

    /**
     * Checks if the white {@link Player} has agreed to a draw.
     *
     * @return {@code true} if the white {@link Player} agreed to a draw; {@code false} otherwise
     */
    public boolean hasWhitePlayerAgreedToDraw() {
        return whitePlayerAgreedToDraw;
    }

    /**
     * Sets the flag if the white {@link Player} agreed to a draw.
     *
     * @param whitePlayerAgreedToDraw {@code true} if the white {@link Player} agreed to a draw; {@code false} otherwise
     */
    public void setWhitePlayerAgreedToDraw(boolean whitePlayerAgreedToDraw) {
        this.whitePlayerAgreedToDraw = whitePlayerAgreedToDraw;
    }

    /**
     * Checks if the black {@link Player} has agreed to a draw.
     *
     * @return {@code true} if the black {@link Player} agreed to a draw; {@code false} otherwise
     */
    public boolean hasBlackPlayerAgreedToDraw() {
        return blackPlayerAgreedToDraw;
    }

    /**
     * Sets the flag if the black {@link Player} agreed to a draw.
     *
     * @param blackPlayerAgreedToDraw {@code true} if the black {@link Player} agreed to a draw; {@code false} otherwise
     */
    public void setBlackPlayerAgreedToDraw(boolean blackPlayerAgreedToDraw) {
        this.blackPlayerAgreedToDraw = blackPlayerAgreedToDraw;
    }

    /**
     * Returns the UUID of the host.
     *
     * @return the UUID of the host
     */
    public String getHost() {
        return host;
    }

    /**
     * Sets the UUID of the host.
     *
     * @param host the UUID of the host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Returns the creation date.
     *
     * @return the creation date
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation date.
     *
     * @param creationDate the creation date
     */
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the color of the player who has the turn.
     *
     * @return the color of the player who has the turn
     */
    public Ship.Color getTurn() {
        return turn;
    }

    /**
     * Sets the color of the player who has the turn.
     *
     * @param turn the color of the player who has the turn
     */
    public void setTurn(Ship.Color turn) {
        this.turn = turn;
    }

    /**
     * Checks if the {@link Match} has already started.
     *
     * @return {@code true} if the {@link Match} has already started; {@code false} otherwise
     */
    public boolean hasStarted() {
        return started;
    }

    /**
     * Sets the {@link Match} to started.
     *
     * @param started {@code true} if the {@link Match} has already started; {@code false} otherwise
     */
    public void setStarted(boolean started) {
        this.started = started;
    }

    /**
     * Returns the start date.
     *
     * @return the start date
     */
    public LocalDateTime getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     *
     * @param startDate the start date
     */
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    /**
     * Returns the end date.
     *
     * @return the end date
     */
    public LocalDateTime getEndDate() {
        return endDate;
    }

    /**
     * Sets the endDate.
     *
     * @param endDate the endDate
     */
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    /**
     * Checks if the white {@link Player} is ready.
     *
     * @return {@code true} if the white {@link Player} is ready
     */
    public boolean isWhitePlayerReady() {
        return whitePlayerReady;
    }

    /**
     * Sets the ready flag for the white {@link Player}.
     *
     * @param whitePlayerReady {@code true} if the white {@link Player} is ready
     */
    public void setWhitePlayerReady(boolean whitePlayerReady) {
        this.whitePlayerReady = whitePlayerReady;
    }

    /**
     * Checks if the black {@link Player} is ready.
     *
     * @return {@code true} if the black {@link Player} is ready
     */
    public boolean isBlackPlayerReady() {
        return blackPlayerReady;
    }

    /**
     * Sets the ready flag for the black {@link Player}.
     *
     * @param blackPlayerReady {@code true} if the black {@link Player} is ready
     */
    public void setBlackPlayerReady(boolean blackPlayerReady) {
        this.blackPlayerReady = blackPlayerReady;
    }

    /**
     * Returns the {@link List} of the {@link Player}s' {@link Ship}s per turn.
     *
     * @return the {@link List} of the {@link Player}s' {@link Ship}s per turn
     */
    public List<Map<String, Set<Ship>>> getMoves() {
        return moves;
    }

    /**
     * Sets the {@link List} of the {@link Player}s' {@link Ship}s per turn.
     *
     * @param moves the {@link List} of the {@link Player}s' {@link Ship}s per turn
     */
    public void setMoves(List<Map<String, Set<Ship>>> moves) {
        this.moves = moves;
    }

    /**
     * Returns the {@link Map} of both {@link Player}s' {@link Ship}s during a turn.
     *
     * @return the {@link Map} of both {@link Player}s' {@link Ship}s during a turn
     */
    public Map<String, Set<Ship>> getFleets() {
        return fleets;
    }

    /**
     * Sets the {@link Map} of both {@link Player}s' {@link Ship}s during a turn.
     *
     * @param fleets the {@link Map} of both {@link Player}s' {@link Ship}s during a turn
     */
    public void setFleets(Map<String, Set<Ship>> fleets) {
        this.fleets = fleets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Match match = (Match) o;

        return Objects.equals(uuid, match.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }
}
