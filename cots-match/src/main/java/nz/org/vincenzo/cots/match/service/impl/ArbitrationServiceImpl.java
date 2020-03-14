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
package nz.org.vincenzo.cots.match.service.impl;

import nz.org.vincenzo.cots.domain.Ship;
import nz.org.vincenzo.cots.match.service.ArbitrationService;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * The implementation of {@link ArbitrationService}.
 *
 * @author Rey Vincent Babilonia
 */
@Service
public class ArbitrationServiceImpl implements ArbitrationService {

    @Override
    public Ship arbitrate(Ship attackingShip, Ship defendingShip) {
        if (attackingShip == null || attackingShip.getColor() == null || attackingShip.getShipClass() == null
                || attackingShip.getCoordinates() == null) {
            throw new IllegalArgumentException("Attacking ship cannot be null or have null values");
        }

        if (defendingShip == null || defendingShip.getColor() == null || defendingShip.getShipClass() == null
                || defendingShip.getCoordinates() == null) {
            throw new IllegalArgumentException("Attacking ship cannot be null or have null values");
        }

        // unlikely but double-check anyway
        if (!attackingShip.getCoordinates().equals(defendingShip.getCoordinates())) {
            throw new IllegalArgumentException("Coordinates are not the same");
        }

        if (attackingShip.getColor() == defendingShip.getColor()) {
            throw new IllegalArgumentException("Attacking own ship is not allowed");
        }

        if (attackingShip.getShipClass() == Ship.ShipClass.UNKNOWN) {
            throw new IllegalArgumentException("Attacking ship class cannot be unknown");
        }

        if (defendingShip.getShipClass() == Ship.ShipClass.UNKNOWN) {
            throw new IllegalArgumentException("Defending ship class cannot be unknown");
        }

        Ship.ShipClass attackingShipClass = attackingShip.getShipClass();
        Ship.ShipClass defendingShipClass = defendingShip.getShipClass();

        // submarine attacks littoral combat ship
        if (attackingShipClass.getRank() == Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_0.getRank()
                && defendingShipClass.getRank() == Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_0.getRank()) {
            return defendingShip;
        }

        // littoral combat ship attacks submarine
        if (attackingShipClass.getRank() == Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_0.getRank()
                && defendingShipClass.getRank() == Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_0.getRank()) {
            return attackingShip;
        }

        // command ship attacks another command ship
        if (attackingShipClass == Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP
                && defendingShipClass == Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP) {
            return attackingShip;
        }

        // tie
        if (attackingShipClass == defendingShipClass) {
            return null;
        }

        return attackingShipClass.getRank() > defendingShipClass.getRank() ? attackingShip : defendingShip;
    }

    @Override
    public boolean validateShips(Set<Ship> ships) {
        for (Ship ship : ships) {
            if (ship.getCoordinates().equals(new Ship.Coordinates(-1, -1))) {
                throw new IllegalArgumentException(String.format("%s must be positioned", ship.getShipClass()));
            }
        }

        return true;
    }
}
