/**
 *  Copyright (C) 2003-2007  Joe Hopkinson, Jay Ashworth
 *
 *  JavaTrek is based on Chuck L. Peterson's MTrek.
 *
 *  This file is part of JavaTrek.
 *
 *  JavaTrek is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  JavaTrek is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with JavaTrek; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.gamehost.jtrek.javatrek;

import java.sql.*;
import java.util.*;

/**
 * TrekJDBCInterface provides the interface between the JTrek server and the back-end database.
 * It provides for loading and saving ship data, verifying passwords match, and checking for ship/player existence.
 * Has methods available for retrieving top scores overall and by ship class with various sort options.
 *
 * @author Jay Ashworth
 */
public class TrekJDBCInterface {

    private static TrekPropertyReader tpr = TrekPropertyReader.getInstance();
    // read URL and JDBC driver settings from a properties file
    private static final String jdbcURL = tpr.getValue("jdbc.url");
    private static final String jdbcDriver = tpr.getValue("jdbc.driver");
    private static final String jdbcUser = tpr.getValue("jdbc.user");
    private static final String jdbcPwd = tpr.getValue("jdbc.password");

    private Connection myCon;
    private ResultSet rs;

    // order by options for score lists
    public static final String ORDER_BY_GOLD = "ship_gold";
//    public static final String ORDER_BY_DMGGVN = "ship_damagegiven";
//    public static final String ORDER_BY_DMGRCVD = "ship_damagereceived";
//    public static final String ORDER_BY_BONUS = "ship_bonus";


    public TrekJDBCInterface() {
        try { // load the JDBC driver
            Class.forName(jdbcDriver).newInstance();
        } catch (Exception e) {
            TrekLog.logMessage(
                    "Failed to load current JDBC driver: " + jdbcDriver);
            TrekLog.logException(e);
            return;
        }

        // establish connection
        try {
            myCon = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPwd);
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problems connecting to " + jdbcURL + ": as " + jdbcUser + "/" + jdbcPwd);
            TrekLog.logError("SQL State: " + SQLe.getSQLState());
            TrekLog.logException(SQLe);

            if (myCon != null) {
                try {
                    myCon.close();
                } catch (Exception e) {
                    TrekLog.logException(e);
                }
            }
        }
    }

    public boolean doesShipExist(String shipName) {
        boolean returnFlag = false;
        try {
            PreparedStatement doesShipExistStmt =
                    myCon.prepareStatement(
                            "SELECT ship_id FROM " + getShipDB() + " WHERE ship_name = ? AND ship_alive = 1");
            doesShipExistStmt.setString(1, shipName);
            rs = doesShipExistStmt.executeQuery();
            if (rs.next()) {
                returnFlag = true;
            }
            rs.close();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception checking for ship existence: " + shipName);
            TrekLog.logException(SQLe);
        }
        return returnFlag;
    }

    public boolean doesShipExist(int shipID) {
        boolean returnFlag = false;
        try {
            PreparedStatement doesShipExistStmt =
                    myCon.prepareStatement(
                            "SELECT ship_id FROM " + getShipDB() + " WHERE ship_id = ? AND ship_alive = 1");
            doesShipExistStmt.setInt(1, shipID);
            rs = doesShipExistStmt.executeQuery();
            if (rs.next()) {
                returnFlag = true;
            }
            rs.close();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception checking for ship existance: " + shipID);
            TrekLog.logException(SQLe);
        }
        return returnFlag;
    }

    public boolean doesPreservedShipExist(String shipName) {
        boolean returnFlag = false;
        long currentTime = Calendar.getInstance().getTimeInMillis();
        try {
            PreparedStatement shipPreservedStmt =
                    myCon.prepareStatement(
                            "SELECT ship_id FROM " + getShipDB() + " WHERE ship_name = ? AND ship_preservePwd > ?");
            shipPreservedStmt.setString(1, shipName);
            shipPreservedStmt.setTimestamp(2, new Timestamp(currentTime));
            rs = shipPreservedStmt.executeQuery();
            if (rs.next()) {
                returnFlag = true;
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem checking for preserved ship: " + shipName + "  using long: " + currentTime);
            TrekLog.logException(SQLe);
        }
        return returnFlag;
    }

    public int getPreservedShipPlayer(String shipName) {
        int returnID = 0;
        long currentTime = Calendar.getInstance().getTimeInMillis();
        try {
            PreparedStatement getPreservedPlayer =
                    myCon.prepareStatement(
                            "SELECT player_id FROM " + getShipDB() + " WHERE ship_name = ? AND ship_preservePwd > ?");
            getPreservedPlayer.setString(1, shipName);
            getPreservedPlayer.setTimestamp(2, new Timestamp(currentTime));
            rs = getPreservedPlayer.executeQuery();
            while (rs.next()) {
                returnID = rs.getInt("player_id");
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***  Exception trying to retrieve player ID for preserved ship: " + shipName);
            TrekLog.logException(SQLe);
        }
        return returnID;
    }

    // return player id #, or 0 if not found
    public int doesPlayerExist(String playerName) {
        int returnID = 0;
        try {
            PreparedStatement doesPlayerExistStmt =
                    myCon.prepareStatement(
                            "SELECT player_id FROM players WHERE player_name = ?");
            doesPlayerExistStmt.setString(1, playerName);
            rs = doesPlayerExistStmt.executeQuery();
            if (rs.next()) {
                // should only return 1 row, and then only if player name exists
                returnID = rs.getInt("player_id");
            }
            rs.close();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception checking for player existance: " + playerName);
            TrekLog.logException(SQLe);
        }
        return returnID;
    }

    public int getPlayerID(String shipName) {
        int returnID = 0;

        try {
            PreparedStatement plyrIDStmt =
                    myCon.prepareStatement(
                            "SELECT player_id FROM " + getShipDB() + " WHERE ship_name = ? AND ship_alive = 1");
            plyrIDStmt.setString(1, shipName);
            rs = plyrIDStmt.executeQuery();
            if (rs.next()) {
                // should only return one row
                returnID = rs.getInt("player_id");
            }
            rs.close();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception trying to retrieve player id for ship: " + shipName);
            TrekLog.logException(SQLe);
        }

        return returnID;
    }

    public void logPlayerIDs() {
        try {
            PreparedStatement plyrIDStmt =
                    myCon.prepareStatement(
                            "SELECT player_id, player_name FROM players");
            rs = plyrIDStmt.executeQuery();

            do {
                if (rs.next()) {
                    // should only return one row
                    TrekLog.logMessage("Player ID: " + rs.getInt("player_id") + ", Player: " + rs.getString("player_name"));
                } else {
                    break;
                }
            } while (true);

            rs.close();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception dumping players to the log.");
            TrekLog.logException(SQLe);
        }
    }

    // return true if password parameter matches that stored in the player record
    public boolean doesShipPasswordMatch(String shipName, String typedPwd) {
        boolean returnFlag = false;
        String storedPwd;
        try {
            PreparedStatement getShipPasswordStmt =
                    myCon.prepareStatement(
                            "SELECT player_pwd FROM players p "
                                    + "JOIN " + getShipDB() + " s ON s.player_id = p.player_id WHERE s.ship_name = ? AND ship_alive = 1");
            getShipPasswordStmt.setString(1, shipName);
            rs = getShipPasswordStmt.executeQuery();
            if (rs.next()) { // should only return 1 row
                storedPwd = rs.getString("player_pwd");
                if (storedPwd.equals(typedPwd)) {
                    returnFlag = true;
                }
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception verifying password for: " + shipName + " pwd: " + typedPwd);
            TrekLog.logException(SQLe);
        }
        return returnFlag;
    }

    public boolean doesPreservedShipPasswordMatch(
            String shipName,
            String typedPwd) {
        boolean returnFlag = false;
        String storedPwd = "";
        long curTime = Calendar.getInstance().getTimeInMillis();
        try {
            PreparedStatement getPreservedShipPasswordStmt =
                    myCon.prepareStatement(
                            "SELECT player_pwd FROM players p "
                                    + "JOIN " + getShipDB() + " s on s.player_id = p.player_id WHERE s.ship_name = ? AND ship_alive = 0 AND ship_preservePwd > ?");
            getPreservedShipPasswordStmt.setString(1, shipName);
            getPreservedShipPasswordStmt.setTimestamp(2, new Timestamp(curTime));
            rs = getPreservedShipPasswordStmt.executeQuery();
            while (rs.next()) { // could return multiple rows
                storedPwd = rs.getString("player_pwd");
            }
            if (storedPwd.equals(typedPwd)) {
                returnFlag = true;
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception verifying preserved password for: " + shipName + " pwd: " + typedPwd);
            TrekLog.logException(SQLe);
        }
        return returnFlag;
    }

    public boolean doesPlayerPasswordMatch(String playerName, String typedPwd) {
        boolean returnFlag = false;
        String storedPwd;
        try {
            PreparedStatement getPlayerPasswordStmt =
                    myCon.prepareStatement(
                            "SELECT player_pwd FROM players WHERE player_name = ?");
            getPlayerPasswordStmt.setString(1, playerName);
            rs = getPlayerPasswordStmt.executeQuery();
            if (rs.next()) {
                storedPwd = rs.getString("player_pwd");
                if (storedPwd.equals(typedPwd)) {
                    returnFlag = true;
                }
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception verifying password for: " + playerName + " pwd: " + typedPwd);
            TrekLog.logException(SQLe);
        }
        return returnFlag;
    }

    public boolean isShipPasswordProtected(String shipName) {
        boolean result = false;
        if ((doesShipExist(shipName)) || (doesPreservedShipExist(shipName))) {
            result = true;
        }
        return result;
    }

    public boolean saveNewPlayer(String playerName, String password) {
        boolean saveSuccess = false;

        try {

            PreparedStatement writePlayer =
                    myCon.prepareStatement(
                            "INSERT INTO players (player_name,player_pwd ) VALUES (?, ? )");
            writePlayer.setString(1, playerName);
            writePlayer.setString(2, password);

            saveSuccess = writePlayer.execute();
        } catch (Exception e) {
            TrekLog.logError("*** ERROR ***   Problem saving new player: " + playerName);
            TrekLog.logException(e);
        }

        return saveSuccess;
    }

    public boolean saveNewShipRecord(TrekShip ship) {
        boolean saveSuccess = false;
        try {
            PreparedStatement writeNewShipStmt =
                    myCon.prepareStatement(
                            "INSERT INTO " + getShipDB() + " (player_id, ship_name, ship_class, ship_quadrant, "
                                    + "ship_docktarget, ship_transwarp, ship_xmitter, ship_shields, ship_raisingshields, ship_warpenused, ship_lifesupportfailing, "
                                    + "ship_docked, ship_currentcrystalcount, ship_damagecontrol, ship_warpenergy, ship_impulseenergy, ship_lifesupport, "
                                    + "ship_antimatter, ship_damage, ship_pitch, ship_yaw, ship_xyz, ship_torpedoes, ship_torpedotype, ship_torpedocount, "
                                    + "ship_torpedowarpspeed, ship_torpedowarpspeedauto, ship_torpedofiretimeout, ship_phasers, ship_phasertype, ship_phaserfiretype, "
                                    + "ship_phaserfiretimeout, ship_phaserenergyreturned, ship_dronecount, ship_dronefiretimeout, ship_minecount, ship_minefiretimeout, "
                                    + "ship_cloak, ship_cloaktimecurrent, ship_cloakburnt, ship_cloakfiretimeout, ship_ramtimeout, ship_buoytimeout, "
                                    + "ship_corbomite, ship_corbomitetimeout, ship_iridium, ship_lithium, ship_lithiumtimeout, ship_magnabuoy, ship_magnabuoytimeout, "
                                    + "ship_neutron, ship_seeker, ship_seekertimeout, ship_gold, "
                                    + "ship_damagegiven, ship_temp_dmggvn, ship_damagereceived, ship_bonus, ship_temp_bonus, ship_breaksaves, ship_conflicts, "
                                    + "ship_unitstraveled, ship_torpsfired, ship_minesdropped, ship_dronesfired, ship_secondsplayed, ship_datelaunched, "
                                    + "ship_lastlogin, ship_option_beep, ship_option_roster, ship_option_range, ship_option_bearing, ship_option_xyz, "
                                    + "ship_option_unknown, ship_option_damagereports, ship_option_objects, ship_macros, ship_alive) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, "
                                    + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
            writeNewShipStmt.setInt(1, ship.parent.dbPlayerID);
            writeNewShipStmt.setString(2, ship.parent.shipName);
            writeNewShipStmt.setString(3, ship.shipClass);
            writeNewShipStmt.setString(4, ship.currentQuadrant.name);
            if (ship.dockTarget == null) { // brokesave
                writeNewShipStmt.setString(5, "coord");
            } else {
                writeNewShipStmt.setString(5, ship.dockTarget.scanLetter);
            }
            writeNewShipStmt.setBoolean(6, ship.transwarp);
            writeNewShipStmt.setBoolean(7, ship.parent.transmitterBurnt);
            writeNewShipStmt.setInt(8, ship.shields);
            writeNewShipStmt.setBoolean(9, ship.raisingShields);
            writeNewShipStmt.setInt(10, ship.warpenUsed);
            writeNewShipStmt.setBoolean(11, ship.lifeSupportFailing);
            writeNewShipStmt.setBoolean(12, ship.docked);
            writeNewShipStmt.setInt(13, ship.currentCrystalCount);
            writeNewShipStmt.setInt(14, ship.damageControl);
            writeNewShipStmt.setInt(15, ship.warpEnergy);
            writeNewShipStmt.setInt(16, ship.impulseEnergy);
            writeNewShipStmt.setInt(17, ship.lifeSupport);
            writeNewShipStmt.setInt(18, ship.antiMatter);
            writeNewShipStmt.setInt(19, ship.damage);
            writeNewShipStmt.setInt(20, (int) Math.round(ship.vector.getPitch()));
            writeNewShipStmt.setInt(21, (int) Math.round(ship.vector.getHeading()));
            // only need to write the xyz if the ship brokesave
            writeNewShipStmt.setString(22, ship.point.toString());
            writeNewShipStmt.setInt(23, ship.torpedoes);
            writeNewShipStmt.setInt(24, ship.torpedoType);
            writeNewShipStmt.setInt(25, ship.torpedoCount);
            writeNewShipStmt.setInt(26, ship.torpedoWarpSpeed);
            writeNewShipStmt.setBoolean(27, ship.torpedoWarpSpeedAuto);
            writeNewShipStmt.setInt(28, ship.torpFireTimeout);
            writeNewShipStmt.setInt(29, ship.phasers);
            writeNewShipStmt.setInt(30, ship.phaserType);
            writeNewShipStmt.setInt(31, ship.phaserFireType);
            writeNewShipStmt.setInt(32, ship.phaserFireTimeout);
            writeNewShipStmt.setInt(33, ship.phaserEnergyReturned);
            writeNewShipStmt.setInt(34, ship.droneCount);
            writeNewShipStmt.setInt(35, ship.droneFireTimeout);
            writeNewShipStmt.setInt(36, ship.mineCount);
            writeNewShipStmt.setInt(37, ship.mineFireTimeout);
            writeNewShipStmt.setBoolean(38, ship.cloak);
            writeNewShipStmt.setInt(39, ship.cloakTimeCurrent);
            writeNewShipStmt.setBoolean(40, ship.cloakBurnt);
            writeNewShipStmt.setInt(41, ship.cloakFireTimeout);
            writeNewShipStmt.setInt(42, ship.ramTimeout);
            writeNewShipStmt.setInt(43, ship.buoyTimeout);
            writeNewShipStmt.setBoolean(44, ship.corbomite);
            writeNewShipStmt.setInt(45, ship.corbomiteTimeout);
            writeNewShipStmt.setBoolean(46, ship.iridium);
            writeNewShipStmt.setBoolean(47, ship.lithium);
            writeNewShipStmt.setInt(48, ship.lithiumTimeout);
            writeNewShipStmt.setBoolean(49, ship.magnabuoy);
            writeNewShipStmt.setInt(50, ship.magnabuoyTimeout);
            writeNewShipStmt.setBoolean(51, ship.neutron);
            writeNewShipStmt.setBoolean(52, ship.seeker);
            writeNewShipStmt.setInt(53, ship.seekerTimeout);
            writeNewShipStmt.setInt(54, ship.gold);
            writeNewShipStmt.setInt(55, ship.totalDamageGiven);
            writeNewShipStmt.setInt(56, ship.damageGiven);
            writeNewShipStmt.setInt(57, ship.totalDamageReceived);
            writeNewShipStmt.setInt(58, ship.totalBonus);
            writeNewShipStmt.setInt(59, ship.bonus);
            writeNewShipStmt.setInt(60, ship.breakSaves);
            writeNewShipStmt.setInt(61, ship.conflicts);
            writeNewShipStmt.setDouble(62, ship.unitsTraveled);
            writeNewShipStmt.setInt(63, ship.torpsFired);
            writeNewShipStmt.setInt(64, ship.minesDropped);
            writeNewShipStmt.setInt(65, ship.dronesFired);
            writeNewShipStmt.setInt(66, ship.secondsPlayed);
            writeNewShipStmt.setTimestamp(67, new Timestamp(ship.dateLaunched));
            writeNewShipStmt.setTimestamp(68, new Timestamp(ship.lastLogin));
            writeNewShipStmt.setInt(69, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_BEEP));
            writeNewShipStmt.setInt(70, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_ROSTER));
            writeNewShipStmt.setInt(71, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_RANGEUPDATE));
            writeNewShipStmt.setInt(72, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_BEARINGUPDATE));
            writeNewShipStmt.setInt(73, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_XYZUPDATE));
            writeNewShipStmt.setInt(74, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_UNKNOWN));
            writeNewShipStmt.setInt(75, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_DAMAGEREPORT));
            writeNewShipStmt.setInt(76, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_OBJECTRANGE));
            StringBuilder sb = new StringBuilder();
            for (Enumeration e = ship.parent.macros.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String macro = (String) ship.parent.macros.get(key);
                sb.append("macro=").append(key).append("~").append(macro).append("\n\r");
            }
            writeNewShipStmt.setBytes(77, sb.toString().getBytes());
            writeNewShipStmt.setInt(78, ship.dbShipAlive ? 1 : 0);
            saveSuccess = writeNewShipStmt.execute();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem saving new ship: " + ship.parent.shipName);
            TrekLog.logException(SQLe);
        }
        return saveSuccess;
    }

    public int getNewShipID(String shipName) {
        int shipID = 0;
        try {
            PreparedStatement getShipIDStmt =
                    myCon.prepareStatement(
                            "SELECT ship_id FROM " + getShipDB() + " WHERE ship_name = ? AND ship_alive = 1");
            getShipIDStmt.setString(1, shipName);
            rs = getShipIDStmt.executeQuery();
            if (rs.next()) { // should be only 1 record that matches
                shipID = rs.getInt("ship_id");
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception trying to retrieve new ship's ID value: " + shipName);
            TrekLog.logException(SQLe);
        }
        return shipID;
    }

    public boolean updateShipRecord(TrekShip ship) {
        boolean updateSuccess = false;
        String query = "";

        try {
            // player_id, ship_id, ship_name, ship_class, ship_datelaunched shouldn't change
            PreparedStatement writeUpdatedShipStmt =
                    myCon.prepareStatement(
                            "UPDATE " + getShipDB(ship) + " SET ship_quadrant = ?, ship_docktarget = ?, ship_transwarp = ?, "
                                    + "ship_xmitter = ?, ship_shields = ?, ship_raisingshields = ?, ship_warpenused = ?, ship_lifesupportfailing = ?, "
                                    + "ship_docked = ?, ship_currentcrystalcount = ?, ship_damagecontrol = ?, ship_warpenergy = ?, ship_impulseenergy = ?, ship_lifesupport = ?, "
                                    + "ship_antimatter = ?, ship_damage = ?, ship_pitch = ?, ship_yaw = ?, ship_xyz = ?, ship_torpedoes = ?, ship_torpedotype = ?, ship_torpedocount = ?, "
                                    + "ship_torpedowarpspeed = ?, ship_torpedowarpspeedauto = ?, ship_torpedofiretimeout = ?, ship_phasers = ?, ship_phasertype = ?, ship_phaserfiretype = ?, "
                                    + "ship_phaserfiretimeout = ?, ship_phaserenergyreturned = ?, ship_dronecount = ?, ship_dronefiretimeout = ?, ship_minecount = ?, ship_minefiretimeout = ?, "
                                    + "ship_cloak = ?, ship_cloaktimecurrent = ?, ship_cloakburnt = ?, ship_cloakfiretimeout = ?, ship_ramtimeout = ?, ship_buoytimeout = ?, "
                                    + "ship_corbomite = ?, ship_corbomitetimeout = ?, ship_iridium = ?, ship_magnabuoy = ?, ship_magnabuoytimeout = ?, ship_neutron = ?, ship_gold = ?, "
                                    + "ship_damagegiven = ?, ship_temp_dmggvn = ?, ship_damagereceived = ?, ship_bonus = ?, ship_temp_bonus = ?, ship_breaksaves = ?, ship_conflicts = ?, "
                                    + "ship_unitstraveled = ?, ship_torpsfired = ?, ship_minesdropped = ?, ship_dronesfired = ?, ship_secondsplayed = ?, "
                                    + "ship_lastlogin = ?, ship_option_beep = ?, ship_option_roster = ?, ship_option_range = ?, ship_option_bearing = ?, ship_option_xyz = ?, "
                                    + "ship_option_unknown = ?, ship_option_damagereports = ?, ship_option_objects = ?, ship_macros = ?, ship_alive = ?, "
                                    + "ship_lithium = ?, ship_lithiumtimeout = ?, ship_seeker = ?, ship_seekertimeout = ?, ship_class = ? WHERE ship_id = ?");
            writeUpdatedShipStmt.setString(1, ship.currentQuadrant.name);
            if (ship.dockTarget == null) { // brokesave
                writeUpdatedShipStmt.setString(2, "coord");
            } else {
                writeUpdatedShipStmt.setString(2, ship.dockTarget.scanLetter);
            }
            writeUpdatedShipStmt.setBoolean(3, ship.transwarp);
            writeUpdatedShipStmt.setBoolean(4, ship.parent.transmitterBurnt);
            writeUpdatedShipStmt.setInt(5, ship.shields);
            writeUpdatedShipStmt.setBoolean(6, ship.raisingShields);
            writeUpdatedShipStmt.setInt(7, ship.warpenUsed);
            writeUpdatedShipStmt.setBoolean(8, ship.lifeSupportFailing);
            writeUpdatedShipStmt.setBoolean(9, ship.docked);
            writeUpdatedShipStmt.setInt(10, ship.currentCrystalCount);
            writeUpdatedShipStmt.setInt(11, ship.damageControl);
            writeUpdatedShipStmt.setInt(12, ship.warpEnergy);
            writeUpdatedShipStmt.setInt(13, ship.impulseEnergy);
            writeUpdatedShipStmt.setInt(14, ship.lifeSupport);
            writeUpdatedShipStmt.setInt(15, ship.antiMatter);
            writeUpdatedShipStmt.setInt(16, ship.damage);
            writeUpdatedShipStmt.setInt(17, (int) Math.round(ship.vector.getPitch()));
            writeUpdatedShipStmt.setInt(18, (int) Math.round(ship.vector.getHeading()));
            writeUpdatedShipStmt.setString(19, ship.point.toString());
            writeUpdatedShipStmt.setInt(20, ship.torpedoes);
            writeUpdatedShipStmt.setInt(21, ship.torpedoType);
            writeUpdatedShipStmt.setInt(22, ship.torpedoCount);
            writeUpdatedShipStmt.setInt(23, ship.torpedoWarpSpeed);
            writeUpdatedShipStmt.setBoolean(24, ship.torpedoWarpSpeedAuto);
            writeUpdatedShipStmt.setInt(25, ship.torpFireTimeout);
            writeUpdatedShipStmt.setInt(26, ship.phasers);
            writeUpdatedShipStmt.setInt(27, ship.phaserType);
            writeUpdatedShipStmt.setInt(28, ship.phaserFireType);
            writeUpdatedShipStmt.setInt(29, ship.phaserFireTimeout);
            writeUpdatedShipStmt.setInt(30, ship.phaserEnergyReturned);
            writeUpdatedShipStmt.setInt(31, ship.droneCount);
            writeUpdatedShipStmt.setInt(32, ship.droneFireTimeout);
            writeUpdatedShipStmt.setInt(33, ship.mineCount);
            writeUpdatedShipStmt.setInt(34, ship.mineFireTimeout);
            writeUpdatedShipStmt.setBoolean(35, ship.cloak);
            writeUpdatedShipStmt.setInt(36, ship.cloakTimeCurrent);
            writeUpdatedShipStmt.setBoolean(37, ship.cloakBurnt);
            writeUpdatedShipStmt.setInt(38, ship.cloakFireTimeout);
            writeUpdatedShipStmt.setInt(39, ship.ramTimeout);
            writeUpdatedShipStmt.setInt(40, ship.buoyTimeout);
            writeUpdatedShipStmt.setBoolean(41, ship.corbomite);
            writeUpdatedShipStmt.setInt(42, ship.corbomiteTimeout);
            writeUpdatedShipStmt.setBoolean(43, ship.iridium);
            writeUpdatedShipStmt.setBoolean(44, ship.magnabuoy);
            writeUpdatedShipStmt.setInt(45, ship.magnabuoyTimeout);
            writeUpdatedShipStmt.setBoolean(46, ship.neutron);
            writeUpdatedShipStmt.setInt(47, ship.gold);
            writeUpdatedShipStmt.setInt(48, ship.totalDamageGiven);
            writeUpdatedShipStmt.setInt(49, ship.damageGiven);
            writeUpdatedShipStmt.setInt(50, ship.totalDamageReceived);
            writeUpdatedShipStmt.setInt(51, ship.totalBonus);
            writeUpdatedShipStmt.setInt(52, ship.bonus);
            writeUpdatedShipStmt.setInt(53, ship.breakSaves);
            writeUpdatedShipStmt.setInt(54, ship.conflicts);
            writeUpdatedShipStmt.setDouble(55, ship.unitsTraveled);
            writeUpdatedShipStmt.setInt(56, ship.torpsFired);
            writeUpdatedShipStmt.setInt(57, ship.minesDropped);
            writeUpdatedShipStmt.setInt(58, ship.dronesFired);
            writeUpdatedShipStmt.setInt(59, ship.secondsPlayed);
            writeUpdatedShipStmt.setTimestamp(60, new Timestamp(ship.lastLogin));
            writeUpdatedShipStmt.setInt(61, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_BEEP));
            writeUpdatedShipStmt.setInt(62, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_ROSTER));
            writeUpdatedShipStmt.setInt(63, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_RANGEUPDATE));
            writeUpdatedShipStmt.setInt(64, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_BEARINGUPDATE));
            writeUpdatedShipStmt.setInt(65, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_XYZUPDATE));
            writeUpdatedShipStmt.setInt(66, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_UNKNOWN));
            writeUpdatedShipStmt.setInt(67, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_DAMAGEREPORT));
            writeUpdatedShipStmt.setInt(68, ship.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_OBJECTRANGE));
            StringBuilder sb = new StringBuilder();
            for (Enumeration e = ship.parent.macros.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String macro = (String) ship.parent.macros.get(key);
                sb.append("macro=").append(key).append("~").append(macro).append("\n\r");
            }
            writeUpdatedShipStmt.setBytes(69, sb.toString().getBytes());
            writeUpdatedShipStmt.setInt(70, ship.dbShipAlive ? 1 : 0);
            writeUpdatedShipStmt.setBoolean(71, ship.lithium);
            writeUpdatedShipStmt.setInt(72, ship.lithiumTimeout);
            writeUpdatedShipStmt.setBoolean(73, ship.seeker);
            writeUpdatedShipStmt.setInt(74, ship.seekerTimeout);
            writeUpdatedShipStmt.setString(75, ship.shipClass);
            writeUpdatedShipStmt.setInt(76, ship.dbShipID);

            query = writeUpdatedShipStmt.toString();

            updateSuccess = writeUpdatedShipStmt.execute();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem updating ship record: " + ship.parent.shipName);
            TrekLog.logError("Query: " + query);
            TrekLog.logException(SQLe);
        }
        return updateSuccess;
    }

    // ship_name, and player_id should already be set in Player object
    public void loadShipRecord(TrekPlayer player) {
        boolean breakSave = false;
        try {
            PreparedStatement readShipStmt =
                    myCon.prepareStatement(
                            "SELECT ship_id, ship_class, ship_quadrant, ship_docktarget, ship_transwarp, ship_xmitter, "
                                    + "ship_shields, ship_raisingshields, ship_warpenused, ship_lifesupportfailing, ship_docked, ship_currentcrystalcount, ship_damagecontrol, "
                                    + "ship_warpenergy, ship_impulseenergy, ship_lifesupport, ship_antimatter, ship_damage, ship_pitch, ship_yaw, ship_xyz, ship_torpedoes, "
                                    + "ship_torpedotype, ship_torpedocount, ship_torpedowarpspeed, ship_torpedowarpspeedauto, ship_torpedofiretimeout, ship_phasers, ship_phasertype, "
                                    + "ship_phaserfiretype, ship_phaserfiretimeout, ship_phaserenergyreturned, ship_dronecount, ship_dronefiretimeout, ship_minecount, "
                                    + "ship_minefiretimeout, ship_cloak, ship_cloaktimecurrent, ship_cloakburnt, ship_cloakfiretimeout, ship_ramtimeout, ship_buoytimeout, "
                                    + "ship_corbomite, ship_corbomitetimeout, ship_iridium, ship_lithium, ship_lithiumtimeout, ship_magnabuoy, ship_magnabuoytimeout, ship_neutron, "
                                    + "ship_seeker, ship_seekertimeout, ship_gold, "
                                    + "ship_damagegiven, ship_temp_dmggvn, ship_damagereceived, ship_bonus, ship_temp_bonus, ship_breaksaves, ship_conflicts, "
                                    + "ship_unitstraveled, ship_torpsfired, ship_minesdropped, ship_dronesfired, ship_secondsplayed, ship_datelaunched, "
                                    + "ship_lastlogin, ship_option_beep, ship_option_roster, ship_option_range, ship_option_bearing, ship_option_xyz, "
                                    + "ship_option_unknown, ship_option_damagereports, ship_option_objects, ship_macros FROM " + getShipDB() + " WHERE ship_name = ? AND ship_alive = 1");
            readShipStmt.setString(1, player.shipName);
            rs = readShipStmt.executeQuery();
            if (rs.next()) {
                player.ship = TrekUtilities.getShip(rs.getString("ship_class"), player);
                player.ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(rs.getString("ship_quadrant"));
                player.ship.dbShipID = rs.getInt("ship_id");
                if (!rs.getString("ship_docktarget").equals("coord")) {
                    // saved correctly
                    TrekStarbase base = (TrekStarbase) player.ship.currentQuadrant.getObjectByScanLetter(rs.getString("ship_docktarget"));
                    player.ship.point.x = base.point.x;
                    player.ship.point.y = base.point.y;
                    player.ship.point.z = base.point.z;
                    player.ship.vector = new Trek3DVector(0, 0, 1);
                    player.ship.docked = true;
                    player.ship.dockTarget = base;
                    player.ship.dockDuration = 0;
                    player.ship.dockable = false;
                } else {
                    breakSave = true;
                }
                player.ship.transwarp = rs.getBoolean("ship_transwarp");
                player.transmitterBurnt = rs.getBoolean("ship_xmitter");
                player.ship.shields = rs.getInt("ship_shields");
                player.ship.raisingShields = rs.getBoolean("ship_raisingshields");
                player.ship.warpenUsed = rs.getInt("ship_warpenused");
                player.ship.lifeSupportFailing = rs.getBoolean("ship_lifesupportfailing");
                player.ship.docked = rs.getBoolean("ship_docked");
                player.ship.currentCrystalCount = rs.getInt("ship_currentcrystalcount");
                player.ship.damageControl = rs.getInt("ship_damagecontrol");
                player.ship.warpEnergy = rs.getInt("ship_warpenergy");
                player.ship.impulseEnergy = rs.getInt("ship_impulseenergy");
                player.ship.lifeSupport = rs.getInt("ship_lifesupport");
                player.ship.antiMatter = rs.getInt("ship_antimatter");
                player.ship.damage = rs.getInt("ship_damage");
                // if ship brokesave, restore it at 'saved' point
                if (breakSave) {
                    player.ship.point = TrekUtilities.parsePoint(rs.getString("ship_xyz"));
                    player.ship.vector = new Trek3DVector(0, 0, 1);
                }
                // need to set heading based on stored pitch/yaw values
                player.ship.changeHeading(rs.getInt("ship_yaw"), rs.getInt("ship_pitch"));
                player.ship.torpedoes = rs.getInt("ship_torpedoes");
                player.ship.torpedoType = rs.getInt("ship_torpedotype");
                player.ship.torpedoCount = rs.getInt("ship_torpedocount");
                player.ship.torpedoWarpSpeed = rs.getInt("ship_torpedowarpspeed");
                player.ship.torpedoWarpSpeedAuto = rs.getBoolean("ship_torpedowarpspeedauto");
                player.ship.torpFireTimeout = rs.getInt("ship_torpedofiretimeout");
                player.ship.phasers = rs.getInt("ship_phasers");
                player.ship.phaserType = rs.getInt("ship_phasertype");
                player.ship.phaserFireType = rs.getInt("ship_phaserfiretype");
                player.ship.phaserFireTimeout = rs.getInt("ship_phaserfiretimeout");
                player.ship.phaserEnergyReturned = rs.getInt("ship_phaserenergyreturned");
                player.ship.droneCount = rs.getInt("ship_dronecount");
                player.ship.droneFireTimeout = rs.getInt("ship_dronefiretimeout");
                player.ship.mineCount = rs.getInt("ship_minecount");
                player.ship.mineFireTimeout = rs.getInt("ship_minefiretimeout");
                player.ship.cloak = rs.getBoolean("ship_cloak");
                player.ship.cloakTimeCurrent = rs.getInt("ship_cloaktimecurrent");
                player.ship.cloakBurnt = rs.getBoolean("ship_cloakburnt");
                player.ship.cloakFireTimeout = rs.getInt("ship_cloakfiretimeout");
                player.ship.ramTimeout = rs.getInt("ship_ramtimeout");
                player.ship.buoyTimeout = rs.getInt("ship_buoytimeout");
                player.ship.corbomite = rs.getBoolean("ship_corbomite");
                player.ship.corbomiteTimeout = rs.getInt("ship_corbomitetimeout");
                player.ship.iridium = rs.getBoolean("ship_iridium");
                player.ship.lithium = rs.getBoolean("ship_lithium");
                player.ship.lithiumTimeout = rs.getInt("ship_lithiumtimeout");
                player.ship.magnabuoy = rs.getBoolean("ship_magnabuoy");
                player.ship.magnabuoyTimeout = rs.getInt("ship_magnabuoytimeout");
                player.ship.neutron = rs.getBoolean("ship_neutron");
                player.ship.seeker = rs.getBoolean("ship_seeker");
                player.ship.seekerTimeout = rs.getInt("ship_seekertimeout");
                player.ship.gold = rs.getInt("ship_gold");
                player.ship.totalDamageGiven = rs.getInt("ship_damagegiven");
                player.ship.damageGiven = rs.getInt("ship_temp_dmggvn");
                player.ship.totalDamageReceived = rs.getInt("ship_damagereceived");
                player.ship.totalBonus = rs.getInt("ship_bonus");
                player.ship.bonus = rs.getInt("ship_temp_bonus");
                player.ship.breakSaves = rs.getInt("ship_breaksaves");
                player.ship.conflicts = rs.getInt("ship_conflicts");
                player.ship.unitsTraveled = rs.getDouble("ship_unitstraveled");
                player.ship.torpsFired = rs.getInt("ship_torpsfired");
                player.ship.minesDropped = rs.getInt("ship_minesdropped");
                player.ship.dronesFired = rs.getInt("ship_dronesfired");
                player.ship.secondsPlayed = rs.getInt("ship_secondsplayed");
                player.ship.dateLaunched = rs.getTimestamp("ship_datelaunched").getTime();
                player.ship.lastLogin = rs.getTimestamp("ship_lastlogin").getTime();
                player.playerOptions.options[TrekPlayerOptions.OPTION_BEEP] = rs.getInt("ship_option_beep");
                player.playerOptions.options[TrekPlayerOptions.OPTION_ROSTER] = rs.getInt("ship_option_roster");
                player.playerOptions.options[TrekPlayerOptions.OPTION_RANGEUPDATE] = rs.getInt("ship_option_range");
                player.playerOptions.options[TrekPlayerOptions.OPTION_BEARINGUPDATE] = rs.getInt("ship_option_bearing");
                player.playerOptions.options[TrekPlayerOptions.OPTION_XYZUPDATE] = rs.getInt("ship_option_xyz");
                player.playerOptions.options[TrekPlayerOptions.OPTION_UNKNOWN] = rs.getInt("ship_option_unknown");
                player.playerOptions.options[TrekPlayerOptions.OPTION_DAMAGEREPORT] = rs.getInt("ship_option_damagereports");
                player.playerOptions.options[TrekPlayerOptions.OPTION_OBJECTRANGE] = rs.getInt("ship_option_objects");
                String[] keymaps = new String(rs.getBytes("ship_macros")).split("\n\r");
                System.out.println("Ship " + player.shipName + " has " + keymaps.length + " stored keymaps.");
                if (keymaps.length != 0) { // ship has stored keymaps
                    // process the String, pull out each individual keymap
                    StringTokenizer t;
                    String keyToMap;
                    String keyToMapFunction;
                    for (int x = 0; x < keymaps.length; x++) {
                        if (!keymaps[x].contains("macro="))
                            break;
                        //System.out.println("Loading keymap " + x);
                        keymaps[x] = keymaps[x].substring(keymaps[x].indexOf("=") + 1, keymaps[x].length());
                        t = new StringTokenizer(keymaps[x], "~");
                        keyToMap = "";
                        keyToMapFunction = "";
                        try {
                            keyToMap = t.nextToken();
                            try {
                                keyToMapFunction = t.nextToken();
                            } catch (NoSuchElementException nsee) {
                                // hopefully catch keys that functionality has been mapped to be blank (i.e. mapping over shift-d)
                                keyToMapFunction = " ";
                            }
                            //System.out.println( "Key: " + keyToMap + "   Function: " + keyToMapFunction);
                            player.macros.put(keyToMap, keyToMapFunction);
                        } catch (Exception e) {
                            TrekLog.logError("There was a problem loading the keymap: " + keyToMap + "/" + keyToMapFunction);
                            TrekLog.logError("From ship: " + player.shipName + "  id: " + player.ship.dbShipID);
                            TrekLog.logException(e);
                        }
                    }
                }
                player.ship.saveTimeout = 3;
                if (player instanceof TrekRawDataInterface)
                    player.hud = new TrekRawHud(player);
                else
                    player.hud = new TrekHud(player);
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem reading ship record: " + player.shipName);
            TrekLog.logException(SQLe);
        }
    }

    public boolean setShipDestroyed(int shipID) {
        boolean returnResult = false;
        long preserveShip = Calendar.getInstance().getTimeInMillis() + 900000;
        // 15 minutes
        try {
            PreparedStatement setShipDestroyedStmt = myCon.prepareStatement("UPDATE " + getShipDB() + " SET ship_alive = 0, ship_preservePwd = ? WHERE ship_id = ?");
            setShipDestroyedStmt.setTimestamp(1, new Timestamp(preserveShip));
            setShipDestroyedStmt.setInt(2, shipID);
            returnResult = setShipDestroyedStmt.execute();
        } catch (SQLException SQLe) {
            TrekLog.logError(
                    "*** ERROR ***   Problem setting destroyed flag in ship record, id: "
                            + shipID);
            TrekLog.logException(SQLe);
        }
        return returnResult;
    }

    public Vector getOverallHighScores(String sortOption) {
        Vector highScores = new Vector();
        StringBuffer score = new StringBuffer();
        try {
            PreparedStatement getTopOverallStmt =
                    myCon.prepareStatement(
                            "SELECT ship_name, ship_class, ship_gold, ship_damagegiven, ship_bonus, ship_damagereceived, ship_conflicts, "
                                    + "ship_breaksaves, ship_lastlogin, ship_docktarget, ship_quadrant, DATE_FORMAT(ship_lastlogin, '%b %d') FROM " + getShipDB() + " WHERE "
                                    + " ship_alive = 1 AND ship_class <> 'Q' ORDER BY ? DESC LIMIT 20");
            getTopOverallStmt.setString(1, sortOption);
            rs = getTopOverallStmt.executeQuery();
            while (rs.next()) {
                score.append(rs.getString("ship_name"));
                score.append("~");
                score.append(rs.getString("ship_class"));
                score.append("~");
                score.append(rs.getInt("ship_gold"));
                score.append("~");
                score.append(rs.getInt("ship_damagegiven"));
                score.append("~");
                score.append(rs.getInt("ship_bonus"));
                score.append("~");
                score.append(rs.getInt("ship_damagereceived"));
                score.append("~");
                score.append(rs.getInt("ship_conflicts"));
                score.append("~");
                score.append(rs.getInt("ship_breaksaves"));
                score.append("~");
                score.append(rs.getString(12));
                score.append("~");
                String dockLetter = rs.getString("ship_docktarget");
                if (dockLetter.equals("coord")) {
                    score.append("?");
                } else {
                    String tmpQuad = rs.getString("ship_quadrant");
                    if (tmpQuad.equals("Beta Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("2");
                        } else {
                            score.append("1");
                        }
                    } else if (tmpQuad.equals("Gamma Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("3");
                        } else {
                            score.append("2");
                        }
                    } else if (tmpQuad.equals("Omega Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("4");
                        } else {
                            score.append("3");
                        }
                    } else if (tmpQuad.equals("Nu Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("5");
                        } else {
                            score.append("4");
                        }
                    } else if (tmpQuad.equals("Alpha Quadrant") &&
                            dockLetter.equals("0")) score.append("1");

                    score.append(dockLetter);
                }
                highScores.addElement(
                        new TrekHighScoreListing(score.toString()));
                score = new StringBuffer();
            }
        } catch (SQLException SQLe) {
            TrekLog.logError(
                    "*** ERROR ***   Problem retrieving top overall scores, sorted by: "
                            + sortOption);
            TrekLog.logException(SQLe);
        }
        return highScores;
    }

    public Vector getClassHighScores(String shipClass, String sortOption) {
        Vector highScores = new Vector();
        StringBuffer score = new StringBuffer();
        try {
            PreparedStatement getTopClassStmt =
                    myCon.prepareStatement(
                            "SELECT ship_name, ship_gold, ship_damagegiven, ship_bonus, ship_damagereceived, ship_conflicts, "
                                    + "ship_breaksaves, ship_lastlogin, ship_docktarget, ship_quadrant, DATE_FORMAT(ship_lastlogin, '%b %d') FROM " + getShipDB()
                                    + " WHERE ship_alive = 1 AND ship_class = ? ORDER BY ? DESC LIMIT 20");
            getTopClassStmt.setString(1, shipClass);
            getTopClassStmt.setString(2, sortOption);
            rs = getTopClassStmt.executeQuery();
            while (rs.next()) {
                score.append(rs.getString("ship_name"));
                score.append("~");
                score.append(shipClass);
                score.append("~");
                score.append(rs.getInt("ship_gold"));
                score.append("~");
                score.append(rs.getInt("ship_damagegiven"));
                score.append("~");
                score.append(rs.getInt("ship_bonus"));
                score.append("~");
                score.append(rs.getInt("ship_damagereceived"));
                score.append("~");
                score.append(rs.getInt("ship_conflicts"));
                score.append("~");
                score.append(rs.getInt("ship_breaksaves"));
                score.append("~");
                score.append(rs.getString(11));
                score.append("~");
                String dockLetter = rs.getString("ship_docktarget");
                if (dockLetter.equals("coord")) {
                    score.append("?");
                } else {
                    String tmpQuad = rs.getString("ship_quadrant");
                    if (tmpQuad.equals("Beta Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("2");
                        } else {
                            score.append("1");
                        }
                    } else if (tmpQuad.equals("Gamma Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("3");
                        } else {
                            score.append("2");
                        }
                    } else if (tmpQuad.equals("Omega Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("4");
                        } else {
                            score.append("3");
                        }
                    } else if (tmpQuad.equals("Nu Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("5");
                        } else {
                            score.append("4");
                        }
                    } else if (tmpQuad.equals("Alpha Quadrant") &&
                            dockLetter.equals("0")) score.append("1");

                    score.append(dockLetter);
                }
                highScores.addElement(
                        new TrekHighScoreListing(score.toString()));
                score = new StringBuffer();
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem retrieving class scores: " + shipClass + " sorted by: " + sortOption);
            TrekLog.logException(SQLe);
        }
        return highScores;
    }

    public Vector getFleetHighScores(int playerID, String sortOption) {
        Vector highScores = new Vector();
        StringBuffer score = new StringBuffer();
        try {
            PreparedStatement getTopClassStmt =
                    myCon.prepareStatement(
                            "SELECT ship_name, ship_class, ship_gold, ship_damagegiven, ship_bonus, ship_damagereceived, ship_conflicts, "
                                    + "ship_breaksaves, ship_lastlogin, ship_docktarget, ship_quadrant, DATE_FORMAT(ship_lastlogin, '%b %d') FROM " + getShipDB()
                                    + " WHERE ship_alive = 1 AND player_id = ? ORDER BY ? DESC LIMIT 20");
            getTopClassStmt.setInt(1, playerID);
            getTopClassStmt.setString(2, sortOption);
            rs = getTopClassStmt.executeQuery();
            while (rs.next()) {
                score.append(rs.getString("ship_name"));
                score.append("~");
                score.append(rs.getString("ship_class"));
                score.append("~");
                score.append(rs.getInt("ship_gold"));
                score.append("~");
                score.append(rs.getInt("ship_damagegiven"));
                score.append("~");
                score.append(rs.getInt("ship_bonus"));
                score.append("~");
                score.append(rs.getInt("ship_damagereceived"));
                score.append("~");
                score.append(rs.getInt("ship_conflicts"));
                score.append("~");
                score.append(rs.getInt("ship_breaksaves"));
                score.append("~");
                score.append(rs.getString(12));
                score.append("~");
                String dockLetter = rs.getString("ship_docktarget");
                if (dockLetter.equals("coord")) {
                    score.append("?");
                } else {
                    String tmpQuad = rs.getString("ship_quadrant");
                    if (tmpQuad.equals("Beta Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("2");
                        } else {
                            score.append("1");
                        }
                    } else if (tmpQuad.equals("Gamma Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("3");
                        } else {
                            score.append("2");
                        }
                    } else if (tmpQuad.equals("Omega Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("4");
                        } else {
                            score.append("3");
                        }
                    } else if (tmpQuad.equals("Nu Quadrant")) {
                        if (dockLetter.equals("0")) {
                            score.append("5");
                        } else {
                            score.append("4");
                        }
                    } else if (tmpQuad.equals("Alpha Quadrant") &&
                            dockLetter.equals("0")) score.append("1");

                    score.append(dockLetter);
                }
                highScores.addElement(
                        new TrekHighScoreListing(score.toString()));
                score = new StringBuffer();
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem retrieving fleet scores: " + playerID + " sorted by: " + sortOption);
            TrekLog.logException(SQLe);
        }
        return highScores;
    }

    public boolean updatePlayerLastLogin(int playerID, long loginDate) {
        boolean returnFlag = false;
        try {
            PreparedStatement updatePlayerLoginStmt =
                    myCon.prepareStatement(
                            "UPDATE players SET player_last_login = ? WHERE player_id = ?");
            updatePlayerLoginStmt.setTimestamp(1, new Timestamp(loginDate));
            updatePlayerLoginStmt.setInt(2, playerID);
            returnFlag = updatePlayerLoginStmt.execute();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem updating player login date, playerID: " + playerID);
            TrekLog.logException(SQLe);
        }
        return returnFlag;
    }

    public long createNewConnection(TrekPlayer player) {
        long returnValue = 0;

        try {
            PreparedStatement createConnStmt = myCon.prepareStatement("INSERT INTO connections (player_id, ship_id, connection_start, connection_end, connection_ip) " +
                    "VALUES (?, ?, ?, ?, ?)");
            Timestamp timeIn = new Timestamp(player.timeShipLogin);
            createConnStmt.setInt(1, player.dbPlayerID);
            createConnStmt.setInt(2, player.ship.dbShipID);
            createConnStmt.setTimestamp(3, timeIn);
            createConnStmt.setTimestamp(4, timeIn);
            createConnStmt.setString(5, player.playerIP);
            createConnStmt.execute();
            PreparedStatement getConnIDStmt = myCon.prepareStatement("SELECT last_insert_id() FROM connections LIMIT 1");
            rs = getConnIDStmt.executeQuery();
            if (rs.next()) {
                returnValue = rs.getLong(1);
            } else {
                TrekLog.logError("*** ERROR ***  Unable to get connection ID!");
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem retrieving connection id for shipID: " + player.ship.dbShipID + " at: " + player.timeShipLogin);
            TrekLog.logException(SQLe);
        }

        return returnValue;
    }

    public void updateConnection(TrekPlayer player) {
        try {
            PreparedStatement updConnStmt = myCon.prepareStatement("UPDATE connections SET connection_end = ? WHERE connection_id = ?");
            updConnStmt.setTimestamp(1, new Timestamp(player.timeShipLogout));
            updConnStmt.setLong(2, player.dbConnectionID);
            updConnStmt.execute();
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem updating connection data for shipID: " + player.ship.dbShipID + " connID: " + player.dbConnectionID);
            TrekLog.logException(SQLe);
        }
    }

    public void loadTemplateKeymaps(TrekPlayer player) {
        try {
            PreparedStatement retrStoredMapsStmt = myCon.prepareStatement("SELECT ship_macros FROM keymaps WHERE player_id = ? AND " +
                    "ship_class = ?");
            retrStoredMapsStmt.setInt(1, player.dbPlayerID);
            retrStoredMapsStmt.setString(2, player.ship.shipClass);
            rs = retrStoredMapsStmt.executeQuery();
            if (rs.next()) { // if there is an existing template record for this player/shipclass combination
                String[] keymaps = new String(rs.getBytes("ship_macros")).split("\n\r");
                System.out.println("Player has " + keymaps.length + " keymaps in " + player.ship.shipClass + " template.");
                if (keymaps.length != 0) {
                    StringTokenizer t;
                    String keyToMap;
                    String keyToMapFunction;
                    for (int x = 0; x < keymaps.length; x++) {
                        if (!keymaps[x].contains("macro="))
                            break;
                        keymaps[x] = keymaps[x].substring(keymaps[x].indexOf("=") + 1, keymaps[x].length());
                        t = new StringTokenizer(keymaps[x], "~");
                        keyToMap = "";
                        keyToMapFunction = "";
                        try {
                            keyToMap = t.nextToken();
                            try {
                                keyToMapFunction = t.nextToken();
                            } catch (NoSuchElementException nsee) {
                                // hopefully catch keys that functionality has been mapped to be blank (i.e. mapping over shift-d)
                                keyToMapFunction = " ";
                            }
                            player.macros.put(keyToMap, keyToMapFunction);
                        } catch (Exception e) {
                            TrekLog.logError("There was a problem loading the keymap: " + keyToMap + "/" + keyToMapFunction);
                            TrekLog.logError("From template, player: " + player.dbPlayerID + "  class: " + player.ship.shipClass);
                            TrekLog.logException(e);
                        }
                    }
                }
            }

        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Problem retrieving and setting template keymaps for player: " + player.dbPlayerID +
                    " and class: " + player.ship.shipClass);
            TrekLog.logException(SQLe);
        }
    }

    public boolean isPlayerAnonymous(int playerID) {
        boolean returnValue = false;

        try {
            PreparedStatement isAnon = myCon.prepareStatement("SELECT player_anonymous FROM players WHERE player_id = ?");
            isAnon.setInt(1, playerID);
            rs = isAnon.executeQuery();
            if (rs.next()) {
                returnValue = rs.getBoolean("player_anonymous");
            }
        } catch (SQLException sqle) {
            TrekLog.logError("*** ERROR ***  Problem checking anonymity of playerID: " + playerID);
            TrekLog.logException(sqle);
        }

        return returnValue;
    }

    public boolean isPlayerAccountLocked(int playerID) {
        boolean returnValue = false;

        try {
            PreparedStatement isLocked = myCon.prepareStatement("SELECT player_account_locked FROM players WHERE player_id = ?");
            isLocked.setInt(1, playerID);
            rs = isLocked.executeQuery();
            if (rs.next()) {
                returnValue = rs.getBoolean("player_account_locked");
            }
        } catch (SQLException sqle) {
            TrekLog.logError("*** ERROR ***  Problem checking locked status of playerID: " + playerID);
            TrekLog.logException(sqle);
        }

        return returnValue;
    }

    public String getPlayerLoginMsg(int playerID) {
        String loginMsg = "";

        try {
            PreparedStatement getMsg = myCon.prepareStatement("SELECT player_login_msg FROM players WHERE player_id = ? and player_login_msg IS NOT NULL");
            getMsg.setInt(1, playerID);
            rs = getMsg.executeQuery();
            if (rs.next()) {
                loginMsg = new String(rs.getBytes("player_login_msg"));
            }
        } catch (SQLException sqle) {
            TrekLog.logError("*** ERROR ***  Problem retrieving login message for playerID: " + playerID);
            TrekLog.logException(sqle);
        }

        return loginMsg;
    }

    public Hashtable getObjectEnemyList(String objName) {
        Hashtable returnValue = new Hashtable();

        try {
            PreparedStatement getAttkrs = myCon.prepareStatement("SELECT ship_id FROM ceasefire WHERE object_name = ?");
            getAttkrs.setString(1, objName);
            ResultSet attckrRS = getAttkrs.executeQuery();
            while (attckrRS.next()) {
                int curShip = attckrRS.getInt("ship_id");
                if (doesShipExist(curShip))
                    returnValue.put(Integer.toString(curShip), 1000);
                else
                    removeEnemy(objName, curShip);
            }
        } catch (SQLException s) {
            TrekLog.logError("*** ERROR ***  Couldn't retrieve cease-fire list for object: " + objName);
            TrekLog.logException(s);
        }

        return returnValue;
    }

    public boolean addEnemy(String objName, int enemyID) {
        boolean returnValue = false;

        try {
            if (myCon.isClosed()) {
                myCon = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPwd);
            }

            PreparedStatement addAttkr = myCon.prepareStatement("INSERT INTO ceasefire (object_name, ship_id) VALUES ( ?, ? )");
            addAttkr.setString(1, objName);
            addAttkr.setInt(2, enemyID);
            returnValue = addAttkr.execute();
        } catch (SQLException s) {
            TrekLog.logError("*** ERROR ***  Couldn't add shipID " + enemyID + " to cease-fire list for object: " + objName);
            TrekLog.logException(s);
        }
        return returnValue;
    }

    public boolean removeEnemy(String objName, int enemyID) {
        boolean returnValue = false;

        try {
            if (myCon.isClosed()) {
                myCon = DriverManager.getConnection(jdbcURL, jdbcUser, jdbcPwd);
            }

            PreparedStatement delAttkr = myCon.prepareStatement("DELETE FROM ceasefire WHERE object_name = ? AND ship_id = ?");
            delAttkr.setString(1, objName);
            delAttkr.setInt(2, enemyID);
            returnValue = delAttkr.execute();
        } catch (SQLException s) {
            TrekLog.logError("*** ERROR ***  Couldn't remove shipID " + enemyID + " from cease-fire list for object: " + objName);
            TrekLog.logException(s);
        }
        return returnValue;
    }

    private String getShipDB() {
        if (TrekServer.isTeamPlayEnabled()) return "ctfships";
        return "ships";
    }

    private String getShipDB(TrekShip ship) {
        if (ship.ctfShip) {
            return "ctfships";
        }

        return "ships";
    }

    protected boolean isPlayerAdmin(String playerName) {
        boolean returnFlag = false;
        try {
            PreparedStatement isAdminStmt =
                    myCon.prepareStatement(
                            "SELECT player_admin FROM players WHERE player_name = ?");
            isAdminStmt.setString(1, playerName);
            rs = isAdminStmt.executeQuery();
            if (rs.next()) {
                returnFlag = rs.getBoolean("player_admin");
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception checking for admin status for: " + playerName);
            TrekLog.logException(SQLe);
        }
        return returnFlag;
    }

    public String getPlayerName(int playerID) {
        String returnValue = "";
        try {
            PreparedStatement getName = myCon.prepareStatement("SELECT player_name FROM players WHERE player_id = ?");
            getName.setInt(1, playerID);
            rs = getName.executeQuery();
            if (rs.next()) {
                returnValue = rs.getString("player_name");
            }
        } catch (SQLException SQLe) {
            TrekLog.logError("*** ERROR ***   Exception trying to retrieve player name, id: " + playerID);
        }
        return returnValue;
    }
}