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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * The domain model object for player.
 *
 * @author Rey Vincent Babilonia
 */
public class Player {

    @Expose
    private String uuid;

    @Expose
    private String emailAddress;

    @Expose
    private String nickname;

    private String password;

    @Expose
    private String avatar;

    @Expose
    private LocalDateTime registrationDate;

    @Expose
    private LocalDateTime lastLoginDate;

    @Expose
    private Statistics statistics;

    @Expose
    private Map<String, Statistics> tournamentStatistics = new HashMap<>();

    private String accessToken;

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
     * Returns the email address.
     *
     * @return the email address
     */
    public String getEmailAddress() {
        return emailAddress;
    }

    /**
     * Sets the email address.
     *
     * @param emailAddress the email address
     */
    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Returns the nickname.
     *
     * @return the nickname
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * Sets the nickname.
     *
     * @param nickname the nickname
     */
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    /**
     * Returns the password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the password.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the avatar.
     *
     * @return the avatar
     */
    public String getAvatar() {
        return avatar;
    }

    /**
     * Sets the avatar.
     *
     * @param avatar the avatar
     */
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    /**
     * Returns the registration {@link LocalDateTime}.
     *
     * @return the registration {@link LocalDateTime}
     */
    public LocalDateTime getRegistrationDate() {
        return registrationDate;
    }

    /**
     * Sets the registration {@link LocalDateTime}.
     *
     * @param registrationDate the registration {@link LocalDateTime}
     */
    public void setRegistrationDate(LocalDateTime registrationDate) {
        this.registrationDate = registrationDate;
    }

    /**
     * Returns the last login {@link LocalDateTime}.
     *
     * @return the last login {@link LocalDateTime}
     */
    public LocalDateTime getLastLoginDate() {
        return lastLoginDate;
    }

    /**
     * Sets the last login {@link LocalDateTime}.
     *
     * @param lastLoginDate the last login {@link LocalDateTime}
     */
    public void setLastLoginDate(LocalDateTime lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    /**
     * Returns the {@link Statistics}.
     *
     * @return the {@link Statistics}
     */
    public Statistics getStatistics() {
        return statistics;
    }

    /**
     * Sets the {@link Statistics}.
     *
     * @param statistics the {@link Statistics}
     */
    public void setStatistics(Statistics statistics) {
        this.statistics = statistics;
    }

    /**
     * Returns the {@link Map} of {@link Statistics} for various tournaments.
     *
     * @return the {@link Map} of {@link Statistics} for various tournaments
     */
    public Map<String, Statistics> getTournamentStatistics() {
        return tournamentStatistics;
    }

    /**
     * Sets the {@link Map} of {@link Statistics} for various tournaments.
     *
     * @param tournamentStatistics the {@link Map} of {@link Statistics} for various tournaments
     */
    public void setTournamentStatistics(Map<String, Statistics> tournamentStatistics) {
        this.tournamentStatistics = tournamentStatistics;
    }

    /**
     * Returns the access token.
     *
     * @return the access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken the access token
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Player player = (Player) o;
        return Objects.equals(uuid, player.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().create();
        return gson.toJson(this);
    }

    /**
     * The player's statistics.
     */
    public static final class Statistics {

        @Expose
        private BigDecimal wins;

        @Expose
        private BigDecimal losses;

        @Expose
        private BigDecimal draws;

        /**
         * Returns the number of wins.
         *
         * @return the number of wins
         */
        public BigDecimal getWins() {
            return wins;
        }

        /**
         * Sets the number of wins.
         *
         * @param wins the number of wins
         */
        public void setWins(BigDecimal wins) {
            this.wins = wins;
        }

        /**
         * Returns the number of losses.
         *
         * @return the number of losses
         */
        public BigDecimal getLosses() {
            return losses;
        }

        /**
         * Sets the number of losses.
         *
         * @param losses the number of losses
         */
        public void setLosses(BigDecimal losses) {
            this.losses = losses;
        }

        /**
         * Returns the number of draws.
         *
         * @return the number of draws
         */
        public BigDecimal getDraws() {
            return draws;
        }

        /**
         * Sets the number of draws.
         *
         * @param draws the number of draws
         */
        public void setDraws(BigDecimal draws) {
            this.draws = draws;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Statistics that = (Statistics) o;
            return Objects.equals(wins, that.wins)
                    && Objects.equals(losses, that.losses)
                    && Objects.equals(draws, that.draws);
        }

        @Override
        public int hashCode() {
            return Objects.hash(wins, losses, draws);
        }

        @Override
        public String toString() {
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            return gson.toJson(this);
        }
    }
}
