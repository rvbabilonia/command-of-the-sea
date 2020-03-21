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
package org.vincenzolabs.cots.match.service.impl;

import org.vincenzolabs.cots.domain.Match;
import org.vincenzolabs.cots.domain.Ship;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.vincenzolabs.cots.match.dao.MatchDAO;
import org.vincenzolabs.cots.match.service.ArbitrationService;
import org.vincenzolabs.cots.match.service.MatchService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The implementation of {@link MatchService}.
 *
 * @author Rey Vincent Babilonia
 */
@Service
public class MatchServiceImpl implements MatchService {

    private final ArbitrationService arbitrationService;

    private final MatchDAO matchDAO;

    /**
     * Default constructor.
     *
     * @param arbitrationService the {@link ArbitrationService}
     * @param matchDAO           the {@link MatchDAO}
     */
    @Autowired
    public MatchServiceImpl(ArbitrationService arbitrationService, MatchDAO matchDAO) {
        this.arbitrationService = arbitrationService;
        this.matchDAO = matchDAO;
    }

    @Override
    public Match hostMatch(final String playerUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        return matchDAO.createMatch(playerUuid);
    }

    @Override
    public void cancelMatch(final String playerUuid, final String matchUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (match.hasStarted()) {
            throw new IllegalArgumentException("Match has already started");
        }

        if (!playerUuid.equals(match.getHost())) {
            throw new IllegalArgumentException("Player did not create this match");
        }

        matchDAO.deleteMatch(match);
    }

    @Override
    public Match connectToMatch(final String playerUuid, final String matchUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (match.hasStarted()) {
            throw new IllegalArgumentException("Match has already started");
        }

        if (StringUtils.isBlank(match.getWhitePlayer())) {
            match.setWhitePlayer(playerUuid);
        } else if (StringUtils.isBlank(match.getBlackPlayer())) {
            match.setBlackPlayer(playerUuid);
        } else {
            throw new IllegalArgumentException("Match already has 2 players");
        }

        Ship.Color color = Math.random() < 0.5 ? Ship.Color.WHITE : Ship.Color.BLACK;
        match.setTurn(color);

        matchDAO.updateMatch(match);

        return match;
    }

    @Override
    public void disconnectFromMatch(final String playerUuid, final String matchUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (match.hasStarted()) {
            throw new IllegalArgumentException("Match has already started");
        }

        if (playerUuid.equals(match.getWhitePlayer())) {
            match.setWhitePlayer(null);
        } else if (playerUuid.equals(match.getBlackPlayer())) {
            match.setBlackPlayer(null);
        } else {
            throw new IllegalArgumentException("Player is not in the match");
        }

        matchDAO.updateMatch(match);
    }

    @Override
    public Map<String, Set<Ship>> positionShip(final String playerUuid, final String matchUuid, Ship ship) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (match.hasStarted()) {
            throw new IllegalArgumentException("Match has already started");
        }

        if (isPlayerNotInMatch(playerUuid, match)) {
            throw new IllegalArgumentException("Player does not belong to match");
        }

        Map<String, Set<Ship>> fleets;
        if (!match.getFleets().isEmpty()) {
            fleets = match.getFleets();
        } else {
            fleets = new LinkedHashMap<>();
        }

        Set<Ship> whiteFleet = fleets.getOrDefault(match.getWhitePlayer(), getDefaultFleet(Ship.Color.WHITE));
        Set<Ship> blackFleet = fleets.getOrDefault(match.getBlackPlayer(), getDefaultFleet(Ship.Color.BLACK));

        // validate starting ship coordinates
        // FIXME move validation to arbitration service
        // FIXME allow turning of the game board
        if (ship.getCoordinates().getX() < 0 || ship.getCoordinates().getX() > 8) {
            throw new IllegalArgumentException(
                    String.format("X coordinate is invalid for %s", ship.getShipClass()));
        }

        if (Ship.Color.WHITE == ship.getColor()
                && (ship.getCoordinates().getY() < 0 || ship.getCoordinates().getY() > 2)) {
            throw new IllegalArgumentException(
                    String.format("Y coordinate is invalid for %s", ship.getShipClass()));
        }

        if (Ship.Color.BLACK == ship.getColor()
                && (ship.getCoordinates().getY() < 5 || ship.getCoordinates().getY() > 7)) {
            throw new IllegalArgumentException(
                    String.format("Y coordinate is invalid for %s", ship.getShipClass()));
        }

        if (Ship.Color.WHITE == ship.getColor()) {
            whiteFleet.remove(ship);
            whiteFleet.add(ship);
        } else {
            blackFleet.remove(ship);
            blackFleet.add(ship);
        }

        fleets.put(match.getWhitePlayer(), whiteFleet);
        fleets.put(match.getBlackPlayer(), blackFleet);
        match.setFleets(fleets);

        matchDAO.updateMatch(match);

        // hide the ship classes of the opponent
        if (match.getWhitePlayer().equals(playerUuid)) {
            blackFleet.forEach(blackShip -> blackShip.setShipClass(Ship.ShipClass.UNKNOWN));
        } else {
            whiteFleet.forEach(whiteShip -> whiteShip.setShipClass(Ship.ShipClass.UNKNOWN));
        }
        return match.getFleets();
    }

    @Override
    public Match ready(final String playerUuid, final String matchUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (match.hasStarted()) {
            throw new IllegalArgumentException("Match has already started");
        }

        if (isPlayerNotInMatch(playerUuid, match)) {
            throw new IllegalArgumentException("Player does not belong to match");
        }

        if (match.getFleets().isEmpty()) {
            throw new IllegalArgumentException("All ships must be positioned");
        }

        Map<String, Set<Ship>> fleets = match.getFleets();

        if (playerUuid.equals(match.getWhitePlayer())) {
            if (arbitrationService.validateShips(fleets.get(playerUuid))) {
                match.setWhitePlayerReady(true);
            }
        } else {
            if (arbitrationService.validateShips(fleets.get(playerUuid))) {
                match.setBlackPlayerReady(true);
            }
        }

        matchDAO.updateMatch(match);

        return match;
    }

    @Override
    public Match startMatch(final String playerUuid, final String matchUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (match.hasStarted()) {
            throw new IllegalArgumentException("Match has already started");
        }

        if (isPlayerNotInMatch(playerUuid, match)) {
            throw new IllegalArgumentException("Player does not belong to match");
        }

        if (!match.isWhitePlayerReady() && !match.isBlackPlayerReady()) {
            throw new IllegalArgumentException("Both players must be ready");
        }

        if (playerUuid.equals(match.getHost())) {
            match.setStarted(true);
            match.setStartDate(LocalDateTime.now());
        } else {
            throw new IllegalArgumentException("Only the host can start the match");
        }

        matchDAO.updateMatch(match);

        return match;
    }

    @Override
    public Map<String, Set<Ship>> moveShip(final String playerUuid, final String matchUuid, Ship ship) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (!match.hasStarted()) {
            throw new IllegalArgumentException("Match has not yet started");
        }

        if (!StringUtils.isBlank(match.getWinner())) {
            throw new IllegalArgumentException("Game over");
        }

        if (isPlayerNotInMatch(playerUuid, match)) {
            throw new IllegalArgumentException("Player does not belong to match");
        }

        if (Ship.Color.WHITE == match.getTurn()) {
            if (!playerUuid.equals(match.getWhitePlayer())) {
                throw new IllegalArgumentException("White player has the turn");
            }
        } else {
            if (!playerUuid.equals(match.getBlackPlayer())) {
                throw new IllegalArgumentException("Black player has the turn");
            }
        }

        int turnNumber = match.getMoves().size() - 1;
        Map<String, Set<Ship>> fleets = match.getMoves().get(turnNumber);

        Set<Ship> whiteFleet = fleets.get(match.getWhitePlayer());
        Set<Ship> blackFleet = fleets.get(match.getBlackPlayer());

        // validate new ship coordinates
        if (ship.getCoordinates().getX() < 0 || ship.getCoordinates().getX() > 8) {
            throw new IllegalArgumentException(
                    String.format("X coordinate is invalid for %s", ship.getShipClass()));
        }

        if (ship.getCoordinates().getY() < 0 || ship.getCoordinates().getY() > 7) {
            throw new IllegalArgumentException(
                    String.format("Y coordinate is invalid for %s", ship.getShipClass()));
        }

        if (ship.getColor() == Ship.Color.WHITE) {
            if (ship.getShipClass() == Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP) {
                if (ship.getCoordinates().getY() == 7) {
                    // check if there are enemy ships beside it
                    boolean won = true;
                    for (Ship blackShip : blackFleet) {
                        if (blackShip.getCoordinates().getY() == 7) {
                            if ((blackShip.getCoordinates().getX() == ship.getCoordinates().getX() + 1)
                                    || blackShip.getCoordinates().getX() == ship.getCoordinates().getX() - 1) {
                                // wait for opponent to attack command ship
                                won = false;
                            }
                        }
                    }

                    if (won) {
                        match.setWinner(match.getWhitePlayer());
                        match.setLoser(match.getBlackPlayer());
                        match.setEndDate(LocalDateTime.now());
                    }
                }
            }

            for (Ship whiteShip : whiteFleet) {
                if (ship.equals(whiteShip)) {
                    if (ship.getCoordinates().equals(whiteShip.getCoordinates())) {
                        throw new IllegalArgumentException(String.format("%s did not move", ship.getShipClass()));
                    }

                    if ((whiteShip.getCoordinates().getX() == 0 && ship.getCoordinates().getX() > 1)
                            || (whiteShip.getCoordinates().getX() == 8 && ship.getCoordinates().getX() < 7)) {
                        throw new IllegalArgumentException("X coordinate is invalid");
                    }

                    if (whiteShip.getCoordinates().getX() > 0 && whiteShip.getCoordinates().getX() < 8
                            && (ship.getCoordinates().getX() != whiteShip.getCoordinates().getX() - 1
                            || ship.getCoordinates().getX() != whiteShip.getCoordinates().getX() + 1)) {
                        throw new IllegalArgumentException("X coordinate is invalid");
                    }

                    if ((whiteShip.getCoordinates().getY() == 0 && ship.getCoordinates().getY() > 1)
                            || (whiteShip.getCoordinates().getX() == 7 && ship.getCoordinates().getX() < 6)) {
                        throw new IllegalArgumentException("Y coordinate is invalid");
                    }

                    if (whiteShip.getCoordinates().getY() > 0 && whiteShip.getCoordinates().getY() < 7
                            && (ship.getCoordinates().getY() < whiteShip.getCoordinates().getY() - 1
                            || ship.getCoordinates().getY() > whiteShip.getCoordinates().getY() + 1)) {
                        throw new IllegalArgumentException("Y coordinate is invalid");
                    }
                }

                if (ship.getCoordinates().equals(whiteShip.getCoordinates())) {
                    throw new IllegalArgumentException("Coordinates is already occupied");
                }
            }
        } else {
            if (ship.getShipClass() == Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP) {
                if (ship.getCoordinates().getY() == 0) {
                    // check if there are enemy ships beside it
                    boolean won = true;
                    for (Ship whiteShip : whiteFleet) {
                        if (whiteShip.getCoordinates().getY() == 0) {
                            if ((whiteShip.getCoordinates().getX() == ship.getCoordinates().getX() + 1)
                                    || whiteShip.getCoordinates().getX() == ship.getCoordinates().getX() - 1) {
                                // wait for opponent to attack command ship
                                won = false;
                            }
                        }
                    }

                    if (won) {
                        match.setWinner(match.getBlackPlayer());
                        match.setLoser(match.getWhitePlayer());
                        match.setEndDate(LocalDateTime.now());
                    }
                }
            }

            for (Ship blackShip : blackFleet) {
                if (ship.equals(blackShip)) {
                    if (ship.getCoordinates().equals(blackShip.getCoordinates())) {
                        throw new IllegalArgumentException("Ship did not move");
                    }

                    if ((blackShip.getCoordinates().getX() == 0 && ship.getCoordinates().getX() > 1)
                            || (blackShip.getCoordinates().getX() == 8 && ship.getCoordinates().getX() < 7)) {
                        throw new IllegalArgumentException("X coordinate is invalid");
                    }

                    if (blackShip.getCoordinates().getX() > 0 && blackShip.getCoordinates().getX() < 8
                            && (ship.getCoordinates().getX() != blackShip.getCoordinates().getX() - 1
                            || ship.getCoordinates().getX() != blackShip.getCoordinates().getX() + 1)) {
                        throw new IllegalArgumentException("X coordinate is invalid");
                    }

                    if ((blackShip.getCoordinates().getY() == 0 && ship.getCoordinates().getY() > 1)
                            || (blackShip.getCoordinates().getX() == 7 && ship.getCoordinates().getX() < 6)) {
                        throw new IllegalArgumentException("Y coordinate is invalid");
                    }

                    if (blackShip.getCoordinates().getY() > 0 && blackShip.getCoordinates().getY() < 7
                            && (ship.getCoordinates().getY() < blackShip.getCoordinates().getY() - 1
                            || ship.getCoordinates().getY() > blackShip.getCoordinates().getY() + 1)) {
                        throw new IllegalArgumentException("Y coordinate is invalid");
                    }
                }

                if (ship.getCoordinates().equals(blackShip.getCoordinates())) {
                    throw new IllegalArgumentException("Coordinates is already occupied");
                }
            }
        }

        if (match.getWhitePlayer().equals(playerUuid)) {
            whiteFleet.remove(ship);
            whiteFleet.add(ship);
        } else {
            blackFleet.remove(ship);
            blackFleet.add(ship);
        }

        // use arbitration service
        if (ship.getColor() == Ship.Color.WHITE) {
            for (Ship blackShip : blackFleet) {
                if (ship.getCoordinates().equals(blackShip.getCoordinates())) {
                    Ship winningShip = arbitrationService.arbitrate(ship, blackShip);

                    if (winningShip == null) {
                        whiteFleet.remove(ship);
                        blackFleet.remove(blackShip);
                    } else if (winningShip.equals(ship)) {
                        blackFleet.remove(blackShip);
                    } else {
                        whiteFleet.remove(ship);
                    }
                }
            }
        } else {
            for (Ship whiteShip : whiteFleet) {
                if (ship.getCoordinates().equals(whiteShip.getCoordinates())) {
                    Ship winningShip = arbitrationService.arbitrate(ship, whiteShip);

                    if (winningShip == null) {
                        whiteFleet.remove(ship);
                        blackFleet.remove(whiteShip);
                    } else if (winningShip.equals(ship)) {
                        blackFleet.remove(whiteShip);
                    } else {
                        whiteFleet.remove(ship);
                    }
                }
            }
        }

        if (Ship.Color.WHITE == match.getTurn()) {
            match.setFleets(Map.of(match.getWhitePlayer(), whiteFleet, match.getBlackPlayer(), blackFleet));
        } else {
            match.setFleets(Map.of(match.getBlackPlayer(), blackFleet, match.getWhitePlayer(), whiteFleet));
        }

        toggleTurn(match);

        // check if command ship is still in play
        boolean lost = true;
        if (ship.getColor() == Ship.Color.WHITE) {
            for (Ship whiteShip : whiteFleet) {
                if (whiteShip.getShipClass() == Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP) {
                    lost = false;
                    break;
                }
            }

            if (lost) {
                match.setWinner(match.getBlackPlayer());
                match.setLoser(match.getWhitePlayer());
                match.setEndDate(LocalDateTime.now());
            }
        } else {
            for (Ship blackShip : blackFleet) {
                if (blackShip.getShipClass() == Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP) {
                    lost = false;
                    break;
                }
            }

            if (lost) {
                match.setWinner(match.getWhitePlayer());
                match.setLoser(match.getBlackPlayer());
                match.setEndDate(LocalDateTime.now());
            }
        }

        // check if the enemy command ship has reached your base
        if (ship.getColor() == Ship.Color.WHITE) {
            for (Ship blackShip : blackFleet) {
                if (blackShip.getShipClass() == Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP
                        && blackShip.getCoordinates().getY() == 0) {
                    match.setWinner(match.getBlackPlayer());
                    match.setLoser(match.getWhitePlayer());
                    match.setEndDate(LocalDateTime.now());
                }
            }
        } else {
            for (Ship whiteShip : whiteFleet) {
                if (whiteShip.getShipClass() == Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP
                        && whiteShip.getCoordinates().getY() == 7) {
                    match.setWinner(match.getWhitePlayer());
                    match.setLoser(match.getBlackPlayer());
                    match.setEndDate(LocalDateTime.now());
                }
            }
        }

        matchDAO.updateMatch(match);

        // hide the ship classes of the opponent
        if (match.getWhitePlayer().equals(playerUuid)) {
            blackFleet.forEach(blackShip -> blackShip.setShipClass(Ship.ShipClass.UNKNOWN));
        } else {
            whiteFleet.forEach(whiteShip -> whiteShip.setShipClass(Ship.ShipClass.UNKNOWN));
        }

        return fleets;
    }

    @Override
    public List<Map<String, Set<Ship>>> replay(final String matchUuid) {
        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (StringUtils.isBlank(match.getWinner())) {
            throw new IllegalArgumentException("Match is not yet over");
        }

        return match.getMoves();
    }

    @Override
    public Set<Match> retrieveFinishedMatches() {
        return matchDAO.retrieveFinishedMatches();
    }

    @Override
    public Set<Match> retrieveActiveMatches() {
        return matchDAO.retrieveActiveMatches();
    }

    @Override
    public Set<Match> retrieveUnstartedMatches() {
        return matchDAO.retrieveUnstartedMatches();
    }

    @Override
    public Match resign(final String playerUuid, final String matchUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (!match.hasStarted()) {
            throw new IllegalArgumentException("Match has not yet started");
        }

        if (!StringUtils.isBlank(match.getWinner())) {
            throw new IllegalArgumentException("Game over");
        }

        if (isPlayerNotInMatch(playerUuid, match)) {
            throw new IllegalArgumentException("Player does not belong to match");
        }

        if (playerUuid.equals(match.getWhitePlayer())) {
            match.setWinner(match.getBlackPlayer());
            match.setLoser(match.getWhitePlayer());
        } else {
            match.setWinner(match.getWhitePlayer());
            match.setLoser(match.getBlackPlayer());
        }

        match.setEndDate(LocalDateTime.now());

        matchDAO.updateMatch(match);

        return match;
    }

    @Override
    public Match draw(final String playerUuid, final String matchUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (!match.hasStarted()) {
            throw new IllegalArgumentException("Match has not yet started");
        }

        if (!StringUtils.isBlank(match.getWinner())) {
            throw new IllegalArgumentException("Game over");
        }

        if (isPlayerNotInMatch(playerUuid, match)) {
            throw new IllegalArgumentException("Player does not belong to match");
        }

        // check if other player offered a draw
        if (playerUuid.equals(match.getWhitePlayer())) {
            if (match.hasBlackPlayerAgreedToDraw()) {
                match.setWhitePlayerAgreedToDraw(true);
                match.setDraw(true);
                match.setEndDate(LocalDateTime.now());
            } else {
                match.setWhitePlayerAgreedToDraw(true);
            }
        } else {
            if (match.hasWhitePlayerAgreedToDraw()) {
                match.setDraw(true);
                match.setEndDate(LocalDateTime.now());
            } else {
                match.setBlackPlayerAgreedToDraw(true);
            }
        }

        matchDAO.updateMatch(match);

        return match;
    }

    @Override
    public Match retrieveMatch(final String matchUuid) {
        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        return matchDAO.retrieveMatch(matchUuid);
    }

    @Override
    public boolean deleteMatch(String playerUuid, String matchUuid) {
        if (StringUtils.isBlank(playerUuid)) {
            throw new IllegalArgumentException("Player UUID must not be null or empty");
        }

        if (StringUtils.isBlank(matchUuid)) {
            throw new IllegalArgumentException("Match UUID must not be null or empty");
        }

        Match match = matchDAO.retrieveMatch(matchUuid);
        if (match == null) {
            throw new IllegalArgumentException("Match UUID is invalid");
        }

        if (!match.getHost().equals(playerUuid)) {
            throw new IllegalArgumentException("Match cannot be deleted by the given player UUID");
        }

        return matchDAO.deleteMatch(match);
    }

    private boolean isPlayerNotInMatch(String playerUuid, Match match) {
        return !match.getWhitePlayer().equals(playerUuid)
                && !match.getBlackPlayer().equals(playerUuid);
    }

    private Set<Ship> getDefaultFleet(Ship.Color color) {
        Set<Ship> ships = new HashSet<>();
        Arrays
                .stream(Ship.ShipClass.values())
                .filter(shipClass -> shipClass != Ship.ShipClass.UNKNOWN)
                .forEachOrdered(shipClass -> {
                    Ship ship = new Ship();
                    ship.setColor(color);
                    ship.setShipClass(shipClass);
                    ship.setCoordinates(new Ship.Coordinates());
                    ships.add(ship);
                });

        return ships;
    }

    private void toggleTurn(Match match) {
        if (Ship.Color.WHITE == match.getTurn()) {
            match.setTurn(Ship.Color.BLACK);
        } else {
            match.setTurn(Ship.Color.WHITE);
        }
    }
}
