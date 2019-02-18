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
package nz.org.vincenzo.cots.domain;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBDocument;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConvertedEnum;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.util.Objects;

/**
 * The domain model object for ship.
 *
 * @author Rey Vincent Babilonia
 */
@DynamoDBDocument
public class Ship {

    @Expose
    private Color color;

    @Expose
    private ShipClass shipClass;

    @Expose
    private Coordinates coordinates;

    /**
     * Returns the {@link Color}.
     *
     * @return the {@link Color}
     */
    @DynamoDBTypeConvertedEnum
    public Color getColor() {
        return color;
    }

    /**
     * Sets the {@link Color}.
     *
     * @param color the {@link Color}
     */
    public void setColor(Color color) {
        this.color = color;
    }

    /**
     * Returns the {@link ShipClass}.
     *
     * @return the {@link ShipClass}
     */
    @DynamoDBTypeConvertedEnum
    public ShipClass getShipClass() {
        return shipClass;
    }

    /**
     * Sets the {@link ShipClass}.
     *
     * @param shipClass the {@link ShipClass}
     */
    public void setShipClass(ShipClass shipClass) {
        this.shipClass = shipClass;
    }

    /**
     * Returns the {@link Coordinates}.
     *
     * @return the {@link Coordinates}
     */
    @DynamoDBAttribute
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the {@link Coordinates}.
     *
     * @param coordinates the {@link Coordinates}
     */
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Ship ship = (Ship) o;
        return color == ship.color
                && shipClass == ship.shipClass;
    }

    @Override
    public int hashCode() {
        return Objects.hash(color, shipClass);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        return gson.toJson(this);
    }

    /**
     * The ship's color.
     */
    public enum Color {
        /**
         * White color.
         */
        WHITE,
        /**
         * Black color.
         */
        BLACK
    }

    /**
     * The ship's class.
     */
    public enum ShipClass {
        /**
         * Virgina-class attack submarine.
         */
        VIRGINIA_CLASS_ATTACK_SUBMARINE_0(14),
        /**
         * Virgina-class attack submarine.
         */
        VIRGINIA_CLASS_ATTACK_SUBMARINE_1(14),
        /**
         * Gerald R. Ford-class aircraft carrier.
         */
        GERALD_FORD_CLASS_AIRCRAFT_CARRIER(13),
        /**
         * Ronald Reagan-subclass aircraft carrier.
         */
        RONALD_REAGAN_SUBCLASS_AIRCRAFT_CARRIER(12),
        /**
         * Theodore Roosevelt-subclass aircraft carrier.
         */
        THEODORE_ROOSEVELT_SUBCLASS_AIRCRAFT_CARRIER(11),
        /**
         * Nimitz-subclass aircraft carrier.
         */
        NIMITZ_SUBCLASS_AIRCRAFT_CARRIER(10),
        /**
         * Enterprise-class aircraft carrier.
         */
        ENTERPRISE_CLASS_AIRCRAFT_CARRIER(9),
        /**
         * America-class amphibious assault ship.
         */
        AMERICA_CLASS_AMPHIBIOUS_ASSAULT_SHIP(8),
        /**
         * Wasp-class amphibious assault ship.
         */
        WASP_CLASS_AMPHIBIOUS_ASSAULT_SHIP(7),
        /**
         * Tarawa-class amphibious assault ship.
         */
        TARAWA_CLASS_AMPHIBIOUS_ASSAULT_SHIP(6),
        /**
         * Zumwalt-class guided missile destroyer.
         */
        ZUMWALT_CLASS_GUIDED_MISSILE_DESTROYER(5),
        /**
         * Arleigh Burke-class guided missile destroyer.
         */
        ARLEIGH_BURKE_CLASS_GUIDED_MISSILE_DESTROYER(4),
        /**
         * Kidd-class guided missile destroyer.
         */
        KIDD_CLASS_GUIDED_MISSILE_DESTROYER(3),
        /**
         * Ticonderoga-class guided missile cruiser.
         */
        TICONDEROGA_CLASS_GUIDED_MISSILE_CRUISER(2),
        /**
         * Independence-class littoral combat ship.
         */
        INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_0(1),
        /**
         * Independence-class littoral combat ship.
         */
        INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_1(1),
        /**
         * Independence-class littoral combat ship.
         */
        INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_2(1),
        /**
         * Independence-class littoral combat ship.
         */
        INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_3(1),
        /**
         * Independence-class littoral combat ship.
         */
        INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_4(1),
        /**
         * Independence-class littoral combat ship.
         */
        INDEPENDENCE_CLASS_LITTORAL_COMBAT_SHIP_5(1),
        /**
         * Blue Ridge-class command ship.
         */
        BLUE_RIDGE_CLASS_COMMAND_SHIP(0),
        /**
         * Unknown ship.
         */
        UNKNOWN(-1);

        private final int rank;

        ShipClass(int rank) {
            this.rank = rank;
        }

        /**
         * Returns the rank.
         *
         * @return the rank
         */
        public int getRank() {
            return rank;
        }
    }

    /**
     * The {@link Ship}'s coordinates.
     */
    @DynamoDBDocument
    public static class Coordinates {

        @Expose
        private int x = -1;

        @Expose
        private int y = -1;

        /**
         * Default constructor.
         */
        public Coordinates() {
            // do nothing
        }

        /**
         * Constructor with X and Y coordinates.
         *
         * @param x the X coordinate
         * @param y the Y coordinate
         */
        public Coordinates(int x, int y) {
            this.x = x;
            this.y = y;
        }

        /**
         * Returns the X coordinate which is from 0 to 8.
         *
         * @return the X coordinate
         */
        @DynamoDBAttribute
        public int getX() {
            return x;
        }

        /**
         * Sets the X coordinate which is from 0 to 8.
         *
         * @param x the X coordinate
         */
        public void setX(int x) {
            this.x = x;
        }

        /**
         * Returns the Y coordinate which is from 0 to 7.
         *
         * @return the Y coordinate
         */
        @DynamoDBAttribute
        public int getY() {
            return y;
        }

        /**
         * Sets the Y coordinate which is from 0 to 7.
         *
         * @param y the Y coordinate
         */
        public void setY(int y) {
            this.y = y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Coordinates coordinates = (Coordinates) o;
            return x == coordinates.getX()
                    && y == coordinates.getY();
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }

        @Override
        public String toString() {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            return gson.toJson(this);
        }
    }
}
