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
import nz.org.vincenzo.cots.match.service.impl.ArbitrationServiceImpl;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * The test case for {@link ArbitrationService}.
 *
 * @author Rey Vincent Babilonia
 */
class ArbitrationServiceTest {

    private ArbitrationService arbitrationService = new ArbitrationServiceImpl();

    @Test
    void arbitrateWithNullArguments() {
        assertThatThrownBy(() -> arbitrationService.arbitrate(null, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Attacking ship cannot be null or have null values");
    }

    @Test
    void arbitrateWithInvalidCoordinates() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.AMERICA_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.BLACK);
        defendingShip.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4);
        defendingShip.setCoordinates(new Ship.Coordinates(4, 3));

        assertThatThrownBy(() -> arbitrationService.arbitrate(attackingShip, defendingShip))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Coordinates are not the same");
    }

    @Test
    void arbitrateWithSameColor() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.AMERICA_CLASS_AMPHIBIOUS_ASSAULT_SHIP);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.WHITE);
        defendingShip.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThatThrownBy(() -> arbitrationService.arbitrate(attackingShip, defendingShip))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Attacking own ship is not allowed");
    }

    @Test
    void arbitrateWithUnknownShipClass() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.UNKNOWN);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.BLACK);
        defendingShip.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThatThrownBy(() -> arbitrationService.arbitrate(attackingShip, defendingShip))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Attacking ship class cannot be unknown");
    }

    @Test
    void arbitrateBetweenSubmarineAndLittoralCombatShip() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_0);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.BLACK);
        defendingShip.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThat(arbitrationService.arbitrate(attackingShip, defendingShip)).isEqualTo(defendingShip);
    }

    @Test
    void arbitrateBetweenLittoralCombatShipAndSubmarine() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.BLACK);
        attackingShip.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.WHITE);
        defendingShip.setShipClass(Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_0);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThat(arbitrationService.arbitrate(attackingShip, defendingShip)).isEqualTo(attackingShip);
    }

    @Test
    void arbitrateBetweenCommandShips() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.BLACK);
        defendingShip.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThat(arbitrationService.arbitrate(attackingShip, defendingShip)).isEqualTo(attackingShip);
    }

    @Test
    void arbitrateBetweenDestroyerAndCarrier() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.ZUMWALT_CLASS_GUIDED_MISSILE_DESTROYER);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.BLACK);
        defendingShip.setShipClass(Ship.ShipClass.NIMITZ_SUBCLASS_AIRCRAFT_CARRIER);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThat(arbitrationService.arbitrate(attackingShip, defendingShip)).isEqualTo(defendingShip);
    }

    @Test
    void arbitrateBetweenLittoralCombatShipAndCommandShip() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.BLACK);
        defendingShip.setShipClass(Ship.ShipClass.BLUE_RIDGE_CLASS_COMMAND_SHIP);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThat(arbitrationService.arbitrate(attackingShip, defendingShip)).isEqualTo(attackingShip);
    }

    @Test
    void arbitrateBetweenCarrierAndSubmarine() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.ENTERPRISE_CLASS_AIRCRAFT_CARRIER);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.BLACK);
        defendingShip.setShipClass(Ship.ShipClass.VIRGINIA_CLASS_ATTACK_SUBMARINE_0);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThat(arbitrationService.arbitrate(attackingShip, defendingShip)).isEqualTo(defendingShip);
    }

    @Test
    void arbitrateBetweenSameShips() {
        Ship attackingShip = new Ship();
        attackingShip.setColor(Ship.Color.WHITE);
        attackingShip.setShipClass(Ship.ShipClass.ARLEIGH_BURKE_CLASS_GUIDED_MISSILE_DESTROYER);
        attackingShip.setCoordinates(new Ship.Coordinates(3, 4));

        Ship defendingShip = new Ship();
        defendingShip.setColor(Ship.Color.BLACK);
        defendingShip.setShipClass(Ship.ShipClass.ARLEIGH_BURKE_CLASS_GUIDED_MISSILE_DESTROYER);
        defendingShip.setCoordinates(new Ship.Coordinates(3, 4));

        assertThat(arbitrationService.arbitrate(attackingShip, defendingShip)).isNull();
    }

    @Test
    void givenInvalidShipsWhenValidateShipsInvokedThenThrowException() {
        assertThatThrownBy(() -> arbitrationService.validateShips(getDefaultFleet(Ship.Color.WHITE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageEndingWith("must be positioned");
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
}
