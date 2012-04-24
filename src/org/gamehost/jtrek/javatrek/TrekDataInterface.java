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

import java.io.*;
import java.util.*;

/**
 * TrekDataInterface is used to load and save various game data.
 *
 * @author Joe Hopkinson
 */
public class TrekDataInterface {
    protected boolean breakSave = false;

    public TrekDataInterface() {
    }

    public synchronized void createDirectoryStructure() {
        try {
            File dir;
            dir = new File("./data/");
            dir.mkdir();
            dir = new File("./players/");
            dir.mkdir();
            dir = new File("./log/");
            dir.mkdir();
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    protected synchronized boolean hasPassword(String thisName) {
        try {
            String passwordFileName = thisName.toLowerCase() + ".pw";
            File passwordFile = new File("./players/" + passwordFileName);

            if (passwordFile.exists()) {
                TrekLog.logMessage("Password file exists.. getting password.");

                String passwordBuffer = "";
                BufferedReader passwordReader = new BufferedReader(new FileReader(passwordFile));

                passwordBuffer = passwordReader.readLine();

                if (passwordBuffer == null) {
                    TrekLog.logError("File input was null.  Returning empty string.");
                    return false;
                }

                if (passwordBuffer.equals("")) {
                    TrekLog.logError("Password was blank!");
                    return false;
                }

                return true;
            } else {
                TrekLog.logError("Password file does not exist!");
                return false;
            }
        } catch (Exception e) {
            TrekLog.logError("TrekDataInterface getShipPassword() error.");
            TrekLog.logException(e);
            return false;
        }
    }

    protected synchronized void removePlayer(String thisName, boolean removePasswordFile) {
        TrekLog.logMessage(thisName + " was destroyed, or quit without saving. Removing player files...");

        if (removePasswordFile) {
            File passwordFile = new File("./players/" + thisName.toLowerCase() + ".pw");
            if (passwordFile.exists()) {
                TrekLog.logMessage("Removing " + passwordFile.getAbsolutePath());
                passwordFile.delete();
            }
        } else {
            TrekServer.addDeletionTimer(thisName);
        }

        File playerFile = new File("./players/" + thisName.toLowerCase());
        if (playerFile.exists()) {
            TrekLog.logMessage("Removing " + playerFile.getAbsolutePath());
            playerFile.delete();
        }
    }

    protected synchronized String getShipPassword(String thisName) {
        try {
            String passwordFileName = thisName.toLowerCase() + ".pw";
            File passwordFile = new File("./players/" + passwordFileName);

            if (passwordFile.exists()) {
                TrekLog.logMessage("Password file exists.. getting password.");

                String passwordBuffer = "";
                BufferedReader passwordReader = new BufferedReader(new FileReader(passwordFile));

                passwordBuffer = passwordReader.readLine();

                if (passwordBuffer == null) {
                    TrekLog.logError("File input was null.  Returning empty string.");
                    return "";
                }

                if (passwordBuffer.equals("")) {
                    TrekLog.logError("Password was blank!");
                    return passwordBuffer;
                }

                return passwordBuffer;
            } else {
                TrekLog.logError("Password file does not exist!");
                return "";
            }
        } catch (Exception e) {
            TrekLog.logError("TrekDataInterface getShipPassword() error.");
            TrekLog.logException(e);
            return "";
        }
    }

    protected synchronized void setShipPassword(String thisName, String thisPassword) {
        try {
            String passwordFileName = thisName.toLowerCase() + ".pw";
            File passwordFile = new File("./players/" + passwordFileName);

            // If the password file exists, delete it.
            if (passwordFile.exists()) {
                TrekLog.logMessage("Password file exists.. deleting.");
                passwordFile.delete();
            }

            passwordFile = new File("./players/" + passwordFileName);

            FileOutputStream passwordOut = new FileOutputStream(passwordFile);
            passwordOut.write(thisPassword.getBytes());
            passwordOut.close();
        } catch (Exception e) {
            TrekLog.logError("TrekDataInterface setShipPassword() error.");
            TrekLog.logException(e);
        }
    }

    protected synchronized void loadShip(String thisName, TrekPlayer player) {
        try {
            String buffer = "";
            field f;
            BufferedReader in = new BufferedReader(new FileReader("./players/" + thisName.toLowerCase()));
            TrekLog.logMessage("Loading ship from file:./players/" + thisName.toLowerCase());

            do {
                buffer = in.readLine();

                if (buffer == null) {
                    break;
                }

                TrekLog.logDebug(buffer);
                f = new field(buffer);

                if (f.name.equals("shipName")) {
                    player.shipName = f.value;
                }

                if (f.name.equals("shipClass")) {
                    player.ship = TrekUtilities.getShip(f.value, player);
                }

                if (f.name.equals("quadrant")) {
                    player.ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(f.value);
                }

                if (f.name.equals("dockTarget")) {
                    if (f.value.compareToIgnoreCase("coord") != 0) {
                        TrekStarbase base = (TrekStarbase) player.ship.currentQuadrant.getObjectByScanLetter(f.value);
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
                }

                if (f.name.equals("xyz") && breakSave) {
                    Trek3DPoint bsPoint = TrekUtilities.parsePoint(f.value);
                    player.ship.point = bsPoint;
                    player.ship.vector = new Trek3DVector(0, 0, 1);
                    breakSave = false;
                }

                if (f.name.equals("transwarp")) {
                    if (f.value.equals("false")) {
                        player.ship.transwarp = false;
                    } else {
                        player.ship.transwarp = true;
                    }
                }

                if (f.name.equals("xmitter")) {
                    if (f.value.equals("false")) {
                        player.transmitterBurnt = false;
                    } else {
                        player.transmitterBurnt = true;
                    }
                }

                if (f.name.equals("shields")) {
                    player.ship.shields = new Integer(f.value).intValue();
                }

                if (f.name.equals("raisingShields")) {
                    if (f.value.equals("false")) {
                        player.ship.raisingShields = false;
                    } else {
                        player.ship.raisingShields = true;
                    }
                }

                if (f.name.equals("warpenUsed")) {
                    player.ship.warpenUsed = new Integer(f.value).intValue();
                }

                if (f.name.equals("lifeSupportFailing")) {
                    if (f.value.equals("false")) {
                        player.ship.lifeSupportFailing = false;
                    } else {
                        player.ship.lifeSupportFailing = true;
                    }
                }

                if (f.name.equals("currentCrystalCount")) {
                    player.ship.currentCrystalCount = new Integer(f.value).intValue();
                }

                if (f.name.equals("damageControl")) {
                    player.ship.damageControl = new Integer(f.value).intValue();
                }

                if (f.name.equals("warpEnergy")) {
                    player.ship.warpEnergy = new Integer(f.value).intValue();
                }

                if (f.name.equals("impulseEnergy")) {
                    player.ship.impulseEnergy = new Integer(f.value).intValue();
                }

                if (f.name.equals("lifeSupport")) {
                    player.ship.lifeSupport = new Integer(f.value).intValue();
                }

                if (f.name.equals("antiMatter")) {
                    player.ship.antiMatter = new Integer(f.value).intValue();
                }

                if (f.name.equals("torpedoes")) {
                    player.ship.torpedoes = new Integer(f.value).intValue();
                }

                if (f.name.equals("phasers")) {
                    player.ship.phasers = new Integer(f.value).intValue();
                }

                if (f.name.equals("cloak")) {
                    if (f.value.equals("false")) {
                        player.ship.cloak = false;
                    } else {
                        player.ship.cloak = true;
                    }
                }

                if (f.name.equals("cloakTimeCurrent")) {
                    player.ship.cloakTimeCurrent = new Integer(f.value).intValue();
                }

                if (f.name.equals("cloakBurnt")) {
                    if (f.value.equals("false")) {
                        player.ship.cloakBurnt = false;
                    } else {
                        player.ship.cloakBurnt = true;
                    }
                }

                if (f.name.equals("phaserType")) {
                    player.ship.phaserType = new Integer(f.value).intValue();
                }

                if (f.name.equals("phaserFireType")) {
                    player.ship.phaserFireType = new Integer(f.value).intValue();
                }

                if (f.name.equals("torpedoType")) {
                    player.ship.torpedoType = new Integer(f.value).intValue();
                }

                if (f.name.equals("torpedoCount")) {
                    player.ship.torpedoCount = new Integer(f.value).intValue();
                }

                if (f.name.equals("droneCount")) {
                    player.ship.droneCount = new Integer(f.value).intValue();
                }

                if (f.name.equals("mineCount")) {
                    player.ship.mineCount = new Integer(f.value).intValue();
                }

                if (f.name.equals("torpedoWarpSpeed")) {
                    player.ship.torpedoWarpSpeed = new Integer(f.value).intValue();
                }

                if (f.name.equals("torpedoWarpSpeedAuto")) {
                    if (f.value.equals("false")) {
                        player.ship.torpedoWarpSpeedAuto = false;
                    } else {
                        player.ship.torpedoWarpSpeedAuto = true;
                    }
                }

                if (f.name.equals("corbomite")) {
                    if (f.value.equals("false")) {
                        player.ship.corbomite = false;
                    } else {
                        player.ship.corbomite = true;
                    }
                }

                if (f.name.equals("corbomiteTimeout")) {
                    player.ship.corbomiteTimeout = new Integer(f.value).intValue();
                }

                if (f.name.equals("iridium")) {
                    if (f.value.equals("false")) {
                        player.ship.iridium = false;
                    } else {
                        player.ship.iridium = true;
                    }
                }

                if (f.name.equals("magnabuoy")) {
                    if (f.value.equals("false")) {
                        player.ship.magnabuoy = false;
                    } else {
                        player.ship.magnabuoy = true;
                    }
                }

                if (f.name.equals("magnabuoyTimeout")) {
                    player.ship.magnabuoyTimeout = new Integer(f.value).intValue();
                }

                if (f.name.equals("neutron")) {
                    if (f.value.equals("false")) {
                        player.ship.neutron = false;
                    } else {
                        player.ship.neutron = true;
                    }
                }

                if (f.name.equals("gold")) {
                    player.ship.gold = new Integer(f.value).intValue();
                }

                if (f.name.equals("damageGiven")) {
                    player.ship.totalDamageGiven = new Integer(f.value).intValue();
                }

                if (f.name.equals("damageReceived")) {
                    player.ship.totalDamageReceived = new Integer(f.value).intValue();
                }

                if (f.name.equals("bonus")) {
                    player.ship.totalBonus = new Integer(f.value).intValue();
                }

                if (f.name.equals("damage")) {
                    player.ship.damage = new Integer(f.value).intValue();
                }

                if (f.name.equals("breakSaves")) {
                    player.ship.breakSaves = new Integer(f.value).intValue();
                }

                if (f.name.equals("conflicts")) {
                    player.ship.conflicts = new Integer(f.value).intValue();
                }

                if (f.name.equals("phaserFireTimeout")) {
                    player.ship.phaserFireTimeout = new Integer(f.value).intValue();
                }

                if (f.name.equals("torpFireTimeout")) {
                    player.ship.torpFireTimeout = new Integer(f.value).intValue();
                }

                if (f.name.equals("droneFireTimeout")) {
                    player.ship.droneFireTimeout = new Integer(f.value).intValue();
                }

                if (f.name.equals("mineFireTimeout")) {
                    player.ship.mineFireTimeout = new Integer(f.value).intValue();
                }

                if (f.name.equals("buoyTimeout")) {
                    player.ship.buoyTimeout = new Integer(f.value).intValue();
                }

                if (f.name.equals("phaserEnergyReturned")) {
                    player.ship.phaserEnergyReturned = new Integer(f.value).intValue();
                }

                if (f.name.equals("ramTimeout")) {
                    player.ship.ramTimeout = new Integer(f.value).intValue();
                }

                if (f.name.equals("cloakFireTimeout")) {
                    player.ship.cloakFireTimeout = new Integer(f.value).intValue();
                }

                // these next two used to store 'temporary' gold yielding stats, in case player does some damage while docked
                // and doesn't redock before saving to collect on it
                if (f.name.equals("tmpDmgGvn")) {
                    player.ship.damageGiven = new Integer(f.value).intValue();
                }

                if (f.name.equals("tmpBonus")) {
                    player.ship.bonus = new Integer(f.value).intValue();
                }

                // odometer / statistic data
                if (f.name.equals("unitsTraveled")) {
                    player.ship.unitsTraveled = new Double(f.value).doubleValue();
                }

                if (f.name.equals("torpsFired")) {
                    player.ship.torpsFired = new Integer(f.value).intValue();
                }

                if (f.name.equals("minesDropped")) {
                    player.ship.minesDropped = new Integer(f.value).intValue();
                }

                if (f.name.equals("dronesFired")) {
                    player.ship.dronesFired = new Integer(f.value).intValue();
                }

                if (f.name.equals("secondsPlayed")) {
                    player.ship.secondsPlayed = new Integer(f.value).intValue();
                }

                if (f.name.equals("dateLaunched")) {
                    player.ship.dateLaunched = new Long(f.value).longValue();
                }

                if (f.name.equals("lastlogin")) {
                    player.ship.lastLogin = new Long(f.value).longValue();
                }

                if (f.name.equals("pitch")) {
                    player.ship.alterHeading(0, new Integer(f.value).intValue());
                }

                if (f.name.equals("yaw")) {
                    player.ship.alterHeading(new Integer(f.value).intValue(), 0);
                }

                if (f.name.equals("optionbeep")) {
                    player.playerOptions.options[TrekPlayerOptions.OPTION_BEEP] = new Integer(f.value).intValue();
                }

                if (f.name.equals("optionroster")) {
                    player.playerOptions.options[TrekPlayerOptions.OPTION_ROSTER] = new Integer(f.value).intValue();
                }

                if (f.name.equals("optionrange")) {
                    player.playerOptions.options[TrekPlayerOptions.OPTION_RANGEUPDATE] = new Integer(f.value).intValue();
                }

                if (f.name.equals("optionbearing")) {
                    player.playerOptions.options[TrekPlayerOptions.OPTION_BEARINGUPDATE] = new Integer(f.value).intValue();
                }

                if (f.name.equals("optionxyz")) {
                    player.playerOptions.options[TrekPlayerOptions.OPTION_XYZUPDATE] = new Integer(f.value).intValue();
                }

                if (f.name.equals("optionunknown")) {
                    player.playerOptions.options[TrekPlayerOptions.OPTION_UNKNOWN] = new Integer(f.value).intValue();
                }

                if (f.name.equals("optiondamagereport")) {
                    player.playerOptions.options[TrekPlayerOptions.OPTION_DAMAGEREPORT] = new Integer(f.value).intValue();
                }

                if (f.name.equals("optionobjects")) {
                    player.playerOptions.options[TrekPlayerOptions.OPTION_OBJECTRANGE] = new Integer(f.value).intValue();
                }

                if (f.name.equals("macro")) {
                    StringTokenizer t = new StringTokenizer(f.value, "~");
                    String keyToMap = "";
                    String keyToMapFunction = "";

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
                        TrekLog.logError("There was a problem loading macro.");
                        TrekLog.logException(e);
                        //						player.macros.put(keyToMap, "");
                    }
                }
            }
            while (buffer != null);

            in.close();

            player.ship.saveTimeout = 3;
            player.hud = new TrekHud(player);

            return;
        } catch (java.io.FileNotFoundException fnfe) {
            TrekLog.logMessage("File not found: " + fnfe.getMessage());
            return;
        } catch (Exception e) {
            TrekLog.logException(e);
            return;
        }
    }

    protected synchronized boolean saveShip(TrekShip thisName) {
        try {
            File file = new File("./players/" + thisName.parent.shipName.toLowerCase());
            FileOutputStream outFile = new FileOutputStream(file);
            PrintStream out = new PrintStream(outFile);

            out.println("shipName=" + thisName.parent.shipName);
            out.println("shipClass=" + thisName.shipClass);
            out.println("quadrant=" + thisName.currentQuadrant.name);
            if (!breakSave) {
                out.println("dockTarget=" + thisName.dockTarget.scanLetter);
            } else {
                out.println("dockTarget=" + "coord");
                out.println("xyz=" + thisName.point.toString());
            }
            out.println("transwarp=" + thisName.transwarp);
            out.println("xmitter=" + thisName.parent.transmitterBurnt);
            out.println("shields=" + thisName.shields);
            out.println("raisingShields=" + thisName.raisingShields);
            out.println("warpenUsed=" + thisName.warpenUsed);
            out.println("lifeSupportFailing=" + thisName.lifeSupportFailing);
            out.println("docked=" + thisName.docked);
            out.println("currentCrystalCount=" + thisName.currentCrystalCount);
            out.println("damageControl=" + thisName.damageControl);
            out.println("warpEnergy=" + thisName.warpEnergy);
            out.println("impulseEnergy=" + thisName.impulseEnergy);
            out.println("lifeSupport=" + thisName.lifeSupport);
            out.println("antiMatter=" + thisName.antiMatter);
            out.println("torpedoes=" + thisName.torpedoes);
            out.println("phasers=" + thisName.phasers);
            out.println("cloak=" + thisName.cloak);
            out.println("cloakTimeCurrent=" + thisName.cloakTimeCurrent);
            out.println("cloakBurnt=" + thisName.cloakBurnt);
            out.println("phaserType=" + thisName.phaserType);
            out.println("phaserFireType=" + thisName.phaserFireType);
            out.println("torpedoType=" + thisName.torpedoType);
            out.println("torpedoCount=" + thisName.torpedoCount);
            out.println("droneCount=" + thisName.droneCount);
            out.println("mineCount=" + thisName.mineCount);
            out.println("torpedoWarpSpeed=" + thisName.torpedoWarpSpeed);
            out.println("torpedoWarpSpeedAuto=" + thisName.torpedoWarpSpeedAuto);
            out.println("corbomite=" + thisName.corbomite);
            out.println("corbomiteTimeout=" + thisName.corbomiteTimeout);
            out.println("iridium=" + thisName.iridium);
            out.println("magnabuoy=" + thisName.magnabuoy);
            out.println("magnabuoyTimeout=" + thisName.magnabuoyTimeout);
            out.println("neutron=" + thisName.neutron);
            out.println("gold=" + thisName.gold);
            out.println("damageGiven=" + thisName.totalDamageGiven);
            out.println("damageReceived=" + thisName.totalDamageReceived);
            out.println("bonus=" + thisName.totalBonus);
            out.println("damage=" + thisName.damage);
            out.println("breakSaves=" + thisName.breakSaves);
            out.println("conflicts=" + thisName.conflicts);
            out.println("phaserFireTimeout=" + thisName.phaserFireTimeout);
            out.println("torpFireTimeout=" + thisName.torpFireTimeout);
            out.println("droneFireTimeout=" + thisName.droneFireTimeout);
            out.println("mineFireTimeout=" + thisName.mineFireTimeout);
            out.println("buoyTimeout=" + thisName.buoyTimeout);
            out.println("phaserEnergyReturned=" + thisName.phaserEnergyReturned);
            out.println("ramTimeout=" + thisName.ramTimeout);
            out.println("cloakFireTimeout=" + thisName.cloakFireTimeout);
            out.println("tmpDmgGvn=" + thisName.damageGiven);
            out.println("tmpBonus=" + thisName.bonus);
            out.println("unitsTraveled=" + thisName.unitsTraveled);
            out.println("torpsFired=" + thisName.torpsFired);
            out.println("minesDropped=" + thisName.minesDropped);
            out.println("dronesFired=" + thisName.dronesFired);
            out.println("secondsPlayed=" + thisName.secondsPlayed);
            out.println("dateLaunched=" + thisName.dateLaunched);
            out.println("lastlogin=" + thisName.lastLogin);

            out.println("pitch=" + Math.round(thisName.vector.getPitch()));
            out.println("yaw=" + Math.round(thisName.vector.getHeading()));
            out.println("optionbeep=" + thisName.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_BEEP));
            out.println("optionroster=" + thisName.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_ROSTER));
            out.println("optionrange=" + thisName.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_RANGEUPDATE));
            out.println("optionbearing=" + thisName.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_BEARINGUPDATE));
            out.println("optionxyz=" + thisName.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_XYZUPDATE));
            out.println("optionunknown=" + thisName.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_UNKNOWN));
            out.println("optiondamagereport=" + thisName.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_DAMAGEREPORT));
            out.println("optionobjects=" + thisName.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_OBJECTRANGE));

            for (Enumeration e = thisName.parent.macros.keys(); e.hasMoreElements(); ) {
                String key = (String) e.nextElement();
                String macro = (String) thisName.parent.macros.get(key);

                out.println("macro=" + key + "~" + macro);
            }

            out.close();

            return true;
        } catch (Exception e) {
            TrekLog.logException(e);
            return false;
        }
    }

    /**
     * Given a ship name, returns a true or false if the ship has a file.
     *
     * @param thisShipName The name of the ship.
     * @return boolean True or False if the ship file exists.
     */
    protected synchronized boolean hasPlayerFile(String thisShipName) {
        try {
            TrekLog.logMessage("Checking for file: ./players/" + thisShipName.toLowerCase());
            File playerFile = new File("./players/" + thisShipName.toLowerCase());
            return playerFile.exists();
        } catch (Exception e) {
            TrekLog.logException(e);
            return false;
        }
    }

    /**
     * Given a ship name, returns a true or false if the ship has a file.
     *
     * @param thisShipName The name of the ship.
     * @return boolean True or False if the ship file exists.
     */
    protected synchronized boolean hasPasswordFile(String thisShipName) {
        try {
            TrekLog.logMessage("Checking for file: ./players/" + thisShipName.toLowerCase() + ".pw");
            File playerFile = new File("./players/" + thisShipName.toLowerCase() + ".pw");
            return playerFile.exists();
        } catch (Exception e) {
            TrekLog.logException(e);
            return false;
        }
    }

    class field {
        public String name;
        public String value;

        public field(String thisLine) {
            try {
                name = "";
                value = "";

                if (thisLine == null) {
                    return;
                }

                StringTokenizer tokens = new StringTokenizer(thisLine, "=");
                name = tokens.nextToken();
                value = tokens.nextToken();
            } catch (Exception e) {
                TrekLog.logException(e);
            }
        }
    }

    protected synchronized Vector loadHighScoresOverall() {
        try {
            Vector highScores = new Vector();

            String buffer = "";

            BufferedReader in = new BufferedReader(new FileReader("./data/highscores"));
            TrekLog.logMessage("Loading highscores...");

            do {
                buffer = in.readLine();

                if (buffer == null) {
                    break;
                }

                TrekHighScoreListing score = new TrekHighScoreListing(buffer);
                highScores.addElement(score);

            }
            while (buffer != null);

            in.close();

            return highScores;
        } catch (java.io.FileNotFoundException fnf) {
            return new Vector();
        } catch (Exception e) {
            TrekLog.logException(e);
            return new Vector();
        }
    }

    protected synchronized Vector loadHighScoresClass(String thisClass) {
        try {
            Vector highScores = new Vector();

            String buffer = "";

            BufferedReader in = new BufferedReader(new FileReader("./data/highscores" + thisClass.toLowerCase()));
            TrekLog.logMessage("Loading highscores for class " + thisClass.toLowerCase());

            do {
                buffer = in.readLine();

                if (buffer == null) {
                    break;
                }

                TrekHighScoreListing score = new TrekHighScoreListing(buffer);
                highScores.addElement(score);

            }
            while (buffer != null);

            in.close();

            return highScores;
        } catch (java.io.FileNotFoundException fnf) {
            return new Vector();
        } catch (Exception e) {
            TrekLog.logException(e);
            return new Vector();
        }
    }

    protected synchronized void saveHighScores(TrekPlayer thisPlayer) {
        try {

            // Get the existing overall scores.
            Vector overallHS = loadHighScoresOverall();

            // Look for our ship name, and remove it.
            for (int x = 0; x < overallHS.size(); x++) {
                TrekHighScoreListing listing = (TrekHighScoreListing) overallHS.elementAt(x);

                if (listing.name.equals(thisPlayer.shipName)) {
                    overallHS.removeElementAt(x);
                }
            }

            // Get the existing class scores.
            Vector classHS = loadHighScoresClass(thisPlayer.ship.shipClass);

            // Look for our ship name, and remove it.
            for (int x = 0; x < classHS.size(); x++) {
                TrekHighScoreListing listing = (TrekHighScoreListing) classHS.elementAt(x);

                if (listing.name.equals(thisPlayer.shipName)) {
                    classHS.removeElementAt(x);
                }
            }

            // Create a new high score object from the player.
            TrekHighScoreListing newHighScore = new TrekHighScoreListing(thisPlayer);

            // Add to the high score list.
            overallHS.add(newHighScore);

            // Add to the high score class list.
            classHS.add(newHighScore);

            // Sort the lists.
            overallHS = sortList(overallHS);
            classHS = sortList(classHS);

            if (overallHS.size() > 20)
                overallHS.setSize(20);
            else
                overallHS.trimToSize();

            if (classHS.size() > 20)
                classHS.setSize(20);
            else
                classHS.trimToSize();

            // Write the new overall file.
            File file = new File("./data/highscores");
            FileOutputStream outFile = new FileOutputStream(file);
            PrintStream out = new PrintStream(outFile);

            for (int x = 0; x < overallHS.size(); x++) {
                TrekHighScoreListing tempListing = (TrekHighScoreListing) overallHS.elementAt(x);
                if (tempListing != null) {
                    out.println(tempListing.fileString);
                }
            }

            out.close();

            // Write the new class file.
            File file2 = new File("./data/highscores" + thisPlayer.ship.shipClass.toLowerCase());
            FileOutputStream outFile2 = new FileOutputStream(file2);
            PrintStream out2 = new PrintStream(outFile2);

            for (int x = 0; x < classHS.size(); x++) {
                TrekHighScoreListing tempListing = (TrekHighScoreListing) classHS.elementAt(x);

                if (tempListing != null) {
                    out2.println(tempListing.fileString);
                }
            }

            out2.close();
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    public static Vector sortList(Vector org) {
        // copy refence to temp vector....
        Vector temp = (Vector) org.clone();
        Object array[] = temp.toArray();
        Arrays.sort(array, new CompareGoldByAmount());
        return new Vector(Arrays.asList(array));
    }

    protected synchronized Vector loadHelpFile(String letter) {
        try {
            String buffer = "";
            int count = 0;
            Vector helpLines = new Vector();
            File helpFile = new File("./data/helpfile" + letter + ".txt");

            if (helpFile.exists()) {
                BufferedReader helpIn = new BufferedReader(new FileReader("./data/helpfile" + letter + ".txt"));

                do {
                    buffer = helpIn.readLine();

                    if (buffer == null)
                        break;

                    if (buffer.length() >= 20) {
                        helpLines.add(buffer.substring(0, 20));
                    } else {
                        helpLines.add(buffer);
                    }

                    count++;

                    if (count >= 13)
                        break;

                }
                while (buffer != null);

                helpIn.close();

            }

            return helpLines;
        } catch (Exception e) {
            TrekLog.logException(e);
            return new Vector();
        }
    }

    static class CompareGoldByAmount implements Comparator {
        public CompareGoldByAmount() {
        }

        public int compare(Object o1, Object o2) {
            TrekHighScoreListing hs1 = (TrekHighScoreListing) o1;
            TrekHighScoreListing hs2 = (TrekHighScoreListing) o2;

            if (hs1.gold < hs2.gold) {
                return 1;
            } else if (hs1.gold > hs2.gold) {
                return -1;
            } else {
                return 0;
            }
        }

        public boolean equals(Object obj) // not being used
        {
            return false;
        }
    }

}