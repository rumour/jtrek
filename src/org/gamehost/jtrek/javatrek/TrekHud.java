/**
 *  Copyright (C) 2003-2012  Joe Hopkinson, Jay Ashworth
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

import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Represents the heads up display for a player.
 *
 * @author Joe Hopkinson
 */
public class TrekHud {
    protected Vector bufferedMessages;
    protected boolean damageGvnWaiting = false;
    protected boolean damageRcvdWaiting = false;
    protected int dmgGvnTimeout = 30;
    protected int dmgRvdTimeout = 30;
    protected Hashtable hudobjects;

    protected int messageTimeout = 60;
    protected boolean messageWaiting = false;
    protected int topMessageTimeout = 10;
    protected boolean topMessageWaiting = false;
    protected int bottomMessageTimeout = 10;
    protected boolean bottomMessageWaiting = false;

    protected int talkMessagesTimeout = 60;
    protected boolean talkMessageWaiting = false;

    protected TrekPlayer player;
    protected int showPlayerPage = 0;
    protected boolean showStats = false;
    protected boolean showOdometer = false;
    protected TrekShip targetShip;
    protected boolean showOptionScreen = false;
    protected boolean showInventory = false;

    // For display of the Help screen.
    protected boolean showHelpScreen = false;
    protected String helpScreenLetter = "";
    protected boolean showingHelpScreen = false;

    protected static final int SCANNER_HELP = 1;
    protected static final int SCANNER_INVENTORY = 2;
    protected static final int SCANNER_ODOMETER = 3;
    protected static final int SCANNER_ROSTER = 4;
    protected static final int SCANNER_STATS = 5;

    protected int tick = 0;

    protected boolean disabled = false;

    protected String loadingMessage = "";
    protected boolean loadMessageWaiting = false;
    protected int loadMessageTimeout = 0;

    protected TrekObject[] tacticalDisplay;

    public TrekHud(TrekPlayer playerin) {
        player = playerin;
        hudobjects = new Hashtable();
        bufferedMessages = new Vector();
        tacticalDisplay = new TrekObject[12];
    }

    public TrekHud() {
    }

    protected void clearMessage(int line) {
        player.sendText(TrekAnsi.clearRow(line, player));
        hudobjects.remove("MyHudLine" + line);
    }

    protected void clearTopMessage() {
        player.sendText(TrekAnsi.clearRow(1, player));
    }

    protected void clearObject(int position, String hudValue) {
        String buffer = "                                    ";
        if (!getHudValueString(hudValue).equals(buffer)) {
            player.sendText(TrekAnsi.locate(position + 3, 23, player) + buffer);
            setHudValue(hudValue, buffer);
        }
    }

    protected void clearScanner() {
        player.ship.scanTarget = null;

        for (int x = 3; x < 17; x++) {
            drawScannerLine(x, TrekAnsi.eraseToEndOfLine(player));
        }

        hudobjects.remove("ScanPhasers");
        hudobjects.remove("ScanDamage");
        hudobjects.remove("ScanTorpedoes");
        hudobjects.remove("ScanDamageControl");
        hudobjects.remove("ScanHudX");
        hudobjects.remove("ScanHudX2");
        hudobjects.remove("ScanHudY");
        hudobjects.remove("ScanHudY2");
        hudobjects.remove("ScanHudZ");
        hudobjects.remove("ScanHudZ2");

        for (int x = 3; x < 14; x++) {
            hudobjects.remove("MyScannerLine" + x);
        }
    }

    public void clearScannerChecker(int callingScreen) {
        boolean clearScreen = false;

        switch (callingScreen) {
            case TrekHud.SCANNER_HELP:
                if (showStats) {
                    clearScreen = true;
                    showStats = false;
                }

                if (showPlayerPage > 0) {
                    clearScreen = true;
                    showPlayerPage = 0;
                }

                if (showInventory) {
                    clearScreen = true;
                    showInventory = false;
                }

                if (showOdometer) {
                    clearScreen = true;
                    showOdometer = false;
                }

                if (player.ship.scanTarget != null) {
                    player.ship.scanTarget = null;
                    clearScreen = true;
                }

                break;
            case TrekHud.SCANNER_INVENTORY:
                if (showPlayerPage > 0) {
                    showPlayerPage = 0;
                    clearScreen = true;
                }

                if (showOdometer) {
                    clearScreen = true;
                    showOdometer = false;
                }

                if (showStats) {
                    clearScreen = true;
                    showStats = false;
                }

                if (showingHelpScreen) {
                    showHelpScreen = false;
                    showingHelpScreen = false;
                    helpScreenLetter = "";
                    clearScreen = true;
                }

                if (player.ship.scanTarget != null) {
                    player.ship.scanTarget = null;
                    clearScreen = true;
                }

                break;
            case TrekHud.SCANNER_ODOMETER:
                if (showStats) {
                    clearScreen = true;
                    showStats = false;
                }

                if (showPlayerPage > 0) {
                    clearScreen = true;
                    showPlayerPage = 0;
                }

                if (showInventory) {
                    clearScreen = true;
                    showInventory = false;
                }

                if (showingHelpScreen) {
                    showHelpScreen = false;
                    showingHelpScreen = false;
                    helpScreenLetter = "";
                    clearScreen = true;
                }

                if (player.ship.scanTarget != null) {
                    player.ship.scanTarget = null;
                    clearScreen = true;
                }

                break;
            case TrekHud.SCANNER_ROSTER:
                if (showStats) {
                    clearScreen = true;
                    showStats = false;
                }

                if (showOdometer) {
                    clearScreen = true;
                    showOdometer = false;
                }

                if (showInventory) {
                    clearScreen = true;
                    showInventory = false;
                }

                if (showingHelpScreen) {
                    showHelpScreen = false;
                    showingHelpScreen = false;
                    helpScreenLetter = "";
                    clearScreen = true;
                }

                if (player.ship.scanTarget != null) {
                    player.ship.scanTarget = null;
                    clearScreen = true;
                }

                break;
            case TrekHud.SCANNER_STATS:

                if (showPlayerPage > 0) {
                    showPlayerPage = 0;
                    clearScreen = true;
                }

                if (showOdometer) {
                    clearScreen = true;
                    showOdometer = false;
                }

                if (showingHelpScreen) {
                    showHelpScreen = false;
                    showingHelpScreen = false;
                    helpScreenLetter = "";
                    clearScreen = true;
                }

                if (showInventory) {
                    clearScreen = true;
                    showInventory = false;
                }

                if (player.ship.scanTarget != null) {
                    player.ship.scanTarget = null;
                    clearScreen = true;
                }

                break;
            default:
        }

        if (clearScreen) clearScanner();
    }

    protected void drawDamageGivenStat(TrekDamageStat stat) {
        if (stat == null) {
            return;
        }

        int bonus = 0;
        int tempDmg = 0;

        // Only add damage given if it was a ship that was attacked.
        // ... and it wasn't yourself.
        if (TrekUtilities.isObjectShip(stat.victim) && stat.victim != stat.victor) {
            TrekShip theVictimShip = (TrekShip) stat.victim;

            // Set our damage given for our next dock.
            //player.ship.damageGiven += stat.structuralDamage;
            if (stat.rammed) {
                tempDmg = stat.damageGiven;
            } else {
                tempDmg = (int) Math.round((float) stat.damageGiven / theVictimShip.shieldStrength * 4.0);
            }
            player.ship.damageGiven += tempDmg;

            // Calculate bonus. BonusForAttack = DmgGivenInAttack * OtherShipsTotalDmgGiven / 400000
            bonus = (theVictimShip.totalDamageGiven * tempDmg) / 400000;

            // Cap it at the amount of bonus to the damage.
            if (bonus > tempDmg) {
                bonus = tempDmg;
            }

            // Set the bonus for our next dock.
            player.ship.bonus += bonus;

            // Add to our permenant ship values.
            //player.ship.totalDamageGiven += stat.structuralDamage;
            player.ship.totalDamageGiven += tempDmg;
            player.ship.totalBonus += bonus;

            // Add to team stats
            if (TrekServer.isTeamPlayEnabled()) {
                TrekServer.teamStats[player.teamNumber].addDmgGvn(tempDmg);
                TrekServer.teamStats[player.teamNumber].addBonus(bonus);
            }
        }

        // If there was a bonus, decide what to tack on to the end of the damage message.
        String bonusString = "";

        if (bonus != 0) {
            bonusString = new Integer(bonus).toString();
        } else {
            bonusString = "0";
        }

        damageGvnWaiting = true;
        dmgGvnTimeout = 10;

        // Jailles: plasma: DmgGiven=387 Bonus=0 Structural=0 - Only if attacking a ship.
        if (TrekUtilities.isObjectShip(stat.victim)) {
            if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_DAMAGEREPORT) == 0) {
                player.addMessageToQueue(stat.victim.name + ": " + stat.type + ": DmgGiven=" + tempDmg + " Bonus=" + bonusString + " Structural=" + stat.structuralDamage + TrekAnsi.eraseToEndOfLine(player));
            } else {
                player.addMessageToQueue(stat.victim.name + ": " + tempDmg + " " + bonusString + " " + stat.structuralDamage + TrekAnsi.eraseToEndOfLine(player));
            }
        }
    }

    protected void drawDamageReceivedStat(TrekDamageStat stat) {
        if (stat == null) {
            return;
        }

        if (stat.victim == this.player.ship)
            this.player.ship.totalDamageReceived += stat.structuralDamage;

        if (TrekServer.isTeamPlayEnabled()) TrekServer.teamStats[player.teamNumber].addDmgRcvd(stat.structuralDamage);

        this.damageRcvdWaiting = true;
        this.dmgRvdTimeout = 10;

        if (stat.showAttacker)
            drawHudLine(20, "Receiving " + stat.damageGiven + " hit from " + stat.type + "! - " + stat.victor.name + TrekAnsi.eraseToEndOfLine(player));
        else
            drawHudLine(20, "Receiving " + stat.damageGiven + " hit from " + stat.type + "!" + TrekAnsi.eraseToEndOfLine(player));

        if (stat.structuralDamage != 0)
            drawHudLine(21, "Damage: " + stat.structuralDamage + " Structural" + TrekAnsi.eraseToEndOfLine(player));
        else
            this.clearMessage(21);
    }

    protected void drawHudLine(int thisLine, String thisString) {
        String currentHudLine = getHudValueString("MyHudLine" + thisLine);

        if (!currentHudLine.equals(thisString)) {
            int offset = getOffset(currentHudLine, thisString);

            if (offset != 0) {
                player.sendText(TrekAnsi.locate(thisLine, offset + 1, player) + getOptimizedOutput(currentHudLine, thisString));
                // System.out.println("offset: " + offset + "\r\ncurrent:" + currentHudLine + "\r\noptimized:" + getOptimizedOutput( currentHudLine, thisString ));
            } else {
                player.sendText(TrekAnsi.locate(thisLine, 1, player) + thisString);
            }

            setHudValue("MyHudLine" + thisLine, thisString);
        }
    }

    protected void drawObject(int position, TrekObject thisObj, String hudValue) {
        if (thisObj == null) {
            if (hudobjects.containsKey(hudValue) && !getHudValueString(hudValue).equals("")) {
                player.sendText(TrekAnsi.locate(position + 3, 23, player) + format("", "", 36));
                setHudValue(hudValue, "");
            }

            return;
        }

        String buffer = "", endBuffer = "";
        String distance = new Integer(new Double(Math.round(TrekMath.getDistance(player.ship, thisObj))).intValue()).toString();

        // Check for scan and lock...
        if (TrekUtilities.isObjectShip(thisObj)) {
            TrekShip testScan = (TrekShip) thisObj;

            if (testScan.lockTarget == player.ship) {
                if (testScan.scanTarget != null && testScan.scanTarget == player.ship) {
                    buffer = "#";
                } else {
                    buffer = " ";
                }
            } else if (testScan.scanTarget == player.ship) {
                buffer = "*";
            } else {
                buffer = " ";
            }

            if (TrekServer.isTeamPlayEnabled()) {
                buffer += thisObj.scanLetter + " " + format(thisObj.name, new Integer(testScan.parent.teamNumber).toString(), 18);
            } else {
                buffer += thisObj.scanLetter + " " + thisObj.name;
            }
        }

        if (TrekUtilities.isObjectWormhole(thisObj)) {
            TrekWormhole hole = (TrekWormhole) thisObj;
            if (hole.active) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            } else {
                buffer = " " + thisObj.scanLetter + ")";
            }
        }

        if (TrekUtilities.isObjectPulsar(thisObj)) {
            TrekZone pulsar = (TrekZone) thisObj;
            if (pulsar.active) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            } else {
                buffer = " " + thisObj.scanLetter + ")";
            }
        }

        if (TrekUtilities.isObjectDrone(thisObj)) {
            TrekDrone drone = (TrekDrone) thisObj;
            if (drone.owner == player.ship) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
            } else {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            }
        }

        if (TrekUtilities.isObjectBuoy(thisObj)) {
            TrekBuoy buoy = (TrekBuoy) thisObj;
            if (buoy.owner == player.ship) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
            } else {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            }
        }

        if (TrekUtilities.isObjectMine(thisObj)) {
            TrekMine mine = (TrekMine) thisObj;
            if (mine.owner == player.ship) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
            } else {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            }
        }

        if (TrekUtilities.isObjectTorpedo(thisObj)) {
            TrekTorpedo torp = (TrekTorpedo) thisObj;
            if (torp.torpType != TrekShip.TORPEDO_WARHEAD) {
                if (torp.owner == player.ship) {
                    buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
                } else {
                    buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
                }
            } else {
                if (torp.interceptTarget == player.ship) {
                    buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
                } else {
                    buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
                }
            }
        }

        if (TrekUtilities.isObjectObserverDevice(thisObj)) {
            TrekObserverDevice observer = (TrekObserverDevice) thisObj;

            if (observer.scanTarget == player.ship) {
                buffer = "*" + thisObj.scanLetter + ") " + thisObj.name;
            } else {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            }
        }

        if (TrekUtilities.isObjectCorbomite(thisObj)) {
            TrekCorbomite corbomite = (TrekCorbomite) thisObj;

            if (corbomite.owner == player.ship) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
            } else {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            }
        }

        if (TrekUtilities.isObjectIridium(thisObj)) {
            TrekIridium iridium = (TrekIridium) thisObj;

            if (iridium.owner == player.ship) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
            } else {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            }
        }

        if (TrekUtilities.isObjectMagnabuoy(thisObj)) {
            TrekMagnabuoy magnabuoy = (TrekMagnabuoy) thisObj;

            if (magnabuoy.owner == player.ship) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
            } else {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            }
        }

        if (TrekUtilities.isObjectNeutron(thisObj)) {
            TrekNeutron neutron = (TrekNeutron) thisObj;

            if (neutron.owner == player.ship) {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name + "*";
            } else {
                buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
            }
        }

        if (buffer.equals("")) {
            buffer = " " + thisObj.scanLetter + ") " + thisObj.name;
        }

        if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_BEARINGUPDATE) == 0) {
            try {
                setHudValue("TacticalObjectBearing" + position, player.ship.getBearingToObj(thisObj));
            } catch (NullPointerException npe) {
                System.out.println("TEST");
            }
        } else if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_BEARINGUPDATE) == -1) {
            setHudValue("TacticalObjectBearing" + position, "");
        } else {
            // by making it check for mod == 1 (instead of 0), we get a display when the user first loads the ship
            if ((tick % player.playerOptions.getOption(TrekPlayerOptions.OPTION_BEARINGUPDATE) == 1)) {
                setHudValue("TacticalObjectBearing" + position, player.ship.getBearingToObj(thisObj));
            }
        }

        if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_RANGEUPDATE) == 0) {
            setHudValue("TacticalObjectRange" + position, distance);
        } else {
            if ((tick % player.playerOptions.getOption(TrekPlayerOptions.OPTION_RANGEUPDATE) == 0)) {
                setHudValue("TacticalObjectRange" + position, distance);
            }
        }

        endBuffer = format(getHudValueString("TacticalObjectBearing" + position), getHudValueString("TacticalObjectRange" + position), 13);

        buffer = format(buffer, endBuffer, 35);

        String currentHudValue = getHudValueString(hudValue);

        byte[] current = currentHudValue.getBytes();
        byte[] buf = buffer.getBytes();

        if (!currentHudValue.equals(buffer)) {
            // Figure out what character starts the change.

            if (currentHudValue.equals("")) {
                player.sendText(TrekAnsi.locate(position + 3, 23, player) + buffer);
            } else {
                for (int x = 0; x < current.length; x++) {
                    if (current[x] != buf[x]) {
                        player.sendText(TrekAnsi.locate(position + 3, x + 23, player) + buffer.substring(x, buffer.length()));
                        break;
                    }
                }
            }

            setHudValue(hudValue, buffer);
        }
    }

    protected void drawObjects() {
        if (!player.isObserving) {
            TrekObject object;
            int indexOfObject = -1;

            // Get all visible objects.
            Vector objects = player.ship.currentQuadrant.getVisibleObjects(player.ship);

            // Get all visible ships.
            Vector ships = new Vector();
            if (player.ship.nebulaTarget != null) {
                ships = player.ship.currentQuadrant.getAllVisibleShipsInRange(player.ship, 1000);
            } else {
                ships = player.ship.currentQuadrant.getVisibleShips(player.ship);
            }

            // First clear all objects and ships that are not visible.
            for (int x = 0; x < 12; x++) {
                if (!objects.contains(tacticalDisplay[x]) && !ships.contains(tacticalDisplay[x])) {
                    tacticalDisplay[x] = null;
                }
            }

            // Add ships to the top most slot in the tactical.
            for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
                object = (TrekObject) e.nextElement();

                // Get the index of the object if it exists in the array.
                indexOfObject = tacticalIndexOf(object);

                // If it doesn't exist in the array, add it to the top most slot.
                if (indexOfObject == -1) {
                    tacticalAddShip(object);
                }
            }

            for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
                object = (TrekObject) e.nextElement();

                indexOfObject = tacticalIndexOf(object);

                if (indexOfObject == -1) {
                    tacticalAddObject(object);
                }
            }

            // Now draw the tactical display.
            for (int x = 0; x < 12; x++) {
                drawObject(x + 1, tacticalDisplay[x], "ScannerObj" + (x + 1));

                if (TrekUtilities.isObjectShip(tacticalDisplay[x])) {
                    TrekShip visibleShip = (TrekShip) tacticalDisplay[x];

                    if ((!visibleShip.cloaked) ||
                            (visibleShip.quasarTarget != null) ||
                            (visibleShip.pulsarTarget != null && visibleShip.pulsarTarget.active)) {
                        // update coord in ship spotted database
                        if (player.ship.scannedHistory.containsKey(visibleShip.scanLetter)) {
                            player.ship.scannedHistory.remove(visibleShip.scanLetter);
                        }

                        player.ship.scannedHistory.put(visibleShip.scanLetter, new TrekCoordHistory(visibleShip.scanLetter, visibleShip.name, new Trek3DPoint(visibleShip.point)));
                    }
                }
            }
        }
    }

    protected int tacticalIndexOf(TrekObject thisObject) {
        for (int x = 0; x < 12; x++) {
            if (tacticalDisplay[x] != null && tacticalDisplay[x] == thisObject) {
                return x;
            }
        }

        return -1;
    }

    protected void tacticalAddShip(TrekObject thisObject) {
        for (int x = 0; x < 12; x++) {
            if (tacticalDisplay[x] == null) {
                tacticalDisplay[x] = thisObject;
                return;
            }
        }
    }

    protected void tacticalAddObject(TrekObject thisObject) {
        for (int x = 11; x > -1; x--) {
            if (tacticalDisplay[x] == null) {
                tacticalDisplay[x] = thisObject;
                return;
            }
        }
    }

    protected void drawScannerLine(int thisLine, String thisString) {
        String currentScannerLine = getHudValueString("MyScannerLine" + thisLine);

        if (!currentScannerLine.equals(thisString)) {
            int offset = getOffset(currentScannerLine, thisString);

            if (offset != 0) {
                player.sendText(TrekAnsi.locate(thisLine, 60 + offset, player) + getOptimizedOutput(currentScannerLine, thisString) + TrekAnsi.eraseToEndOfLine(player));
            } else {
                player.sendText(TrekAnsi.locate(thisLine, 60, player) + thisString + TrekAnsi.eraseToEndOfLine(player));
            }

            setHudValue("MyScannerLine" + thisLine, thisString);
        }
    }

    protected String format(String start, String end, int chars) {
        String buffer = "";

        for (int x = 0; x < chars - start.length() - end.length(); x++) {
            buffer += " ";
        }

        return start + buffer + end;
    }

    protected String format(String start, int end, int chars) {
        String buffer = "";
        String actualEnd = new Integer(end).toString();

        for (int x = 0; x < chars - start.length() - actualEnd.length(); x++) {
            buffer += " ";
        }

        return start + buffer + actualEnd;
    }

    protected String format(String start, String end, int chars, String fillCharacter) {
        String buffer = "";

        for (int x = 0; x < chars - start.length() - end.length(); x++) {
            buffer += fillCharacter;
        }

        return start + buffer + end;
    }

    protected String getFormattedWarpSpeed(double warpSpeed) {
        NumberFormat numformat;
        numformat = NumberFormat.getInstance();
        numformat.setMaximumFractionDigits(1);
        numformat.setMinimumFractionDigits(1);
        String formattedWarp = numformat.format(warpSpeed);
        return formattedWarp;
    }

    protected String getHudValueString(String key) {
        String thisValue = (String) hudobjects.get(key);

        if (thisValue == null) {
            return "";
        } else {
            return thisValue;
        }
    }

    protected void reset(boolean firstReset) {
        TrekLog.logMessage("Reset hud for player: " + player.shipName);
        String msg1 = getHudValueString("MyHudLine22");
        String msg2 = getHudValueString("MyHudLine23");
        String msg3 = getHudValueString("MyHudLine24");

        // Preserve the messages, if any.
        hudobjects = new Hashtable();

        setHudValue("MyHudLine4", "Warp Energy:         ");
        setHudValue("MyHudLine5", "Impulse Energy:      ");
        setHudValue("MyHudLine6", "Power Unused:        ");
        setHudValue("MyHudLine7", "Warp:                ");
        setHudValue("MyHudLine8", "Shields:           % ");
        setHudValue("MyPhasers", "Ph:      ");
        setHudValue("MyDamage", "Damage:     ");
        setHudValue("MyTorpedoes", "Tp:      ");
        setHudValue("MyDamageControl", "DmgCtl:     ");
        setHudValue("MyHudLine11", "Life Support:      % ");
        setHudValue("MyHudLine12", "Anti-Matter:         ");
        setHudValue("MyHudX", "X:        ");
        setHudValue("MyHudY", "Y:        ");
        setHudValue("MyHudZ", "Z:        ");
        setHudValue("MyHudZ2", "HD:       ");

        // redraw any zone indicators
        if (!firstReset) {
            if (player.ship.nebulaTarget != null) {
                nebulaIndicator(true);
            }
            if (player.ship.pulsarTarget != null) {
                pulsarIndicator(true);
            }
            if (player.ship.quasarTarget != null) {
                quasarIndicator(true);
            }
            if (player.ship.asteroidTarget != null) {
                asteroidIndicator(true);
            }
        }

        if (firstReset)
            sendTopMessage(TrekAnsi.locate(1, 1, player) + "Welcome aboard " + player.ship.name + "; a " + player.ship.fullClassName + " class " + player.ship.shipType + ".");

        drawHudLine(22, msg1.replace('\007', '\000'));
        drawHudLine(23, msg2.replace('\007', '\000'));
        drawHudLine(24, msg3.replace('\007', '\000'));

        showingHelpScreen = false;
        tick = 0;
    }

    protected void sendMessage(String thisMessage, int msgTimeout) {
        messageWaiting = true;
        messageTimeout = msgTimeout;

        hudobjects.remove("MyHudLine2");
        thisMessage += TrekAnsi.eraseToEndOfLine(player);

        drawHudLine(2, thisMessage);
    }

    public void sendMessage(String thisMessage) {
        sendMessage(thisMessage, 10);
    }

    protected void sendTopMessage(String thisMessage, int msgTimeout) {
        topMessageWaiting = true;
        topMessageTimeout = msgTimeout;

        hudobjects.remove("MyHudLine1");
        thisMessage += TrekAnsi.eraseToEndOfLine(player);

        drawHudLine(1, thisMessage);
    }

    protected void sendTopMessage(String thisMessage) {
        sendTopMessage(thisMessage, 10);
    }

    protected void sendMessageBeep(String thisMessage) {
        if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_BEEP) == 0)
            sendMessage("\007" + thisMessage, 10);
        else
            sendMessage(thisMessage, 10);
    }

    protected void sendTextMessage(String thisMessage) {
        player.sendText(TrekAnsi.locate(22, 1, player));
        player.sendText(TrekAnsi.deleteLines(1, player));
        player.sendText(TrekAnsi.locate(24, 1, player));
        player.sendText(thisMessage + TrekAnsi.eraseToEndOfLine(player));

        setHudValue("MyHudLine22", getHudValueString("MyHudLine23"));
        setHudValue("MyHudLine23", getHudValueString("MyHudLine24"));
        setHudValue("MyHudLine24", thisMessage);

        talkMessageWaiting = true;
        talkMessagesTimeout = 180;

        //		hudobjects.remove("MyHudLine" + (thisMessageNumber + 22));
        //		drawHudLine(thisMessageNumber + 22, thisMessage);
    }

    protected void setHudValue(String key, Object obj) {
        hudobjects.put(key, obj);
    }

    /**
     * Displays the player roster.
     */
    protected void showPlayers() {
        int playerCount = 0;
        int numberOfPages = 1;
        String displayOption = "";
        String buffer = "";

        clearScannerChecker(TrekHud.SCANNER_ROSTER);

        for (int x = 0; x < TrekServer.players.size(); x++) {
            TrekPlayer thisPlayer = (TrekPlayer) TrekServer.players.elementAt(x);

            if (thisPlayer == null)
                continue;

            if (thisPlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (thisPlayer.ship.scanLetter == null)
                continue;

            char shipsScanLetter = thisPlayer.ship.scanLetter.charAt(0);

            if (shipsScanLetter >= 97 && shipsScanLetter <= 109 && numberOfPages < 1)
                numberOfPages = 1;

            if (shipsScanLetter >= 110 && shipsScanLetter <= 122 && numberOfPages < 2)
                numberOfPages = 2;

            if (shipsScanLetter >= 65 && shipsScanLetter <= 77 && numberOfPages < 3)
                numberOfPages = 3;

            if (shipsScanLetter >= 78 && shipsScanLetter <= 90 && numberOfPages < 4)
                numberOfPages = 4;

            if (shipsScanLetter >= 48 && shipsScanLetter <= 57 && numberOfPages < 5)
                numberOfPages = 5;

            playerCount++;
        }

        // Clear the scanner if we reach the last page of players.
        if (showPlayerPage > numberOfPages) {
            //TrekLog.logMessage("Clearing scanner.  showPlayerPage > numberOfPages");
            // reset player page
            showPlayerPage = 1;
            //return;
        }

        char[] lettersToShow;

        switch (showPlayerPage) {
            case 1:
                lettersToShow = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm'};
                break;
            case 2:
                lettersToShow = new char[]{'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
                break;
            case 3:
                lettersToShow = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M'};
                break;
            case 4:
                lettersToShow = new char[]{'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'};
                break;
            case 5:
                lettersToShow = new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '\0', '\0', '\0'};
                break;
            default:
                TrekLog.logError("Show Players: Page is greater than the number of pages we can show.");
                return;
        }

        this.drawScannerLine(3, format(new Integer(playerCount).toString() + " Active Ships:", (showPlayerPage + "of" + numberOfPages), 20));

        for (int x = 0; x < lettersToShow.length; x++) {
            if (lettersToShow[x] != '\0') {
                TrekShip thisShip = TrekServer.getPlayerShipByScanLetter(new Character(lettersToShow[x]).toString());

                if (thisShip == null || lettersToShow[x] == '\0') {
                    buffer = format("", "", 20);
                } else if (thisShip.isPlaying()) {
                    switch (player.playerOptions.getOption(TrekPlayerOptions.OPTION_ROSTER)) {
                        case 0:
                            if (thisShip.gold >= 1000) {
                                if (thisShip.gold > 99999) {
                                    displayOption = "*";
                                } else {
                                    displayOption = new Integer(thisShip.gold / 1000).toString();
                                }
                            } else {
                                displayOption = "";
                            }
                            break;
                        case 1:
                            displayOption = thisShip.classLetter;
                            break;
                        case 2:
                            if (thisShip.currentQuadrant.name.equals("Alpha Quadrant")) {
                                displayOption = "A";
                            } else if (thisShip.currentQuadrant.name.equals("Beta Quadrant")) {
                                displayOption = "B";
                            } else if (thisShip.currentQuadrant.name.equals("Gamma Quadrant")) {
                                displayOption = "G";
                            } else if (thisShip.currentQuadrant.name.equals("Omega Quadrant")) {
                                displayOption = "O";
                            } else if (thisShip.currentQuadrant.name.equals("Nu Quadrant")) {
                                displayOption = "N";
                            } else if (thisShip.currentQuadrant.name.equals("Delta Quadrant")) {
                                displayOption = "D";
                            }

                            break;
                        default:
                            if (thisShip.gold >= 1000) {
                                if (thisShip.gold > 99999) {
                                    displayOption = "*";
                                } else {
                                    displayOption = new Integer(thisShip.gold / 1000).toString();
                                }
                            } else {
                                displayOption = "";
                            }
                            break;
                    }

                    // Draw a special character for the Q.
                    String qShip = (thisShip instanceof ShipQ) ? "*" : " ";
                    buffer = format(lettersToShow[x] + qShip + thisShip.parent.shipName, displayOption, 20);

                    // Display team # preceding ship name
                    if (TrekServer.isTeamPlayEnabled()) {
                        if (thisShip instanceof ShipQ || thisShip.parent.teamNumber == 0) { // skip admin ships or ships w/ no team
                        } else {
                            buffer = format(lettersToShow[x] + new Integer(thisShip.parent.teamNumber).toString() + thisShip.parent.shipName, displayOption, 20);
                        }
                    }
                }
            } else {
                buffer = format("", "", 20);
            }

            drawScannerLine(x + 4, buffer);
        }
    }

    /**
     * Displays the score for a given player.
     *
     * @param thisPlayer Player to display the score of.
     */
    protected void showScore(TrekPlayer thisPlayer) {
        //sendMessage(thisPlayer.ship.shipClass + " - DmgRcvd: " + thisPlayer.ship.totalDamageReceived + "  DmgGvn: " + thisPlayer.ship.totalDamageGiven + "  Bonus: " + thisPlayer.ship.totalBonus + "  Gold: " + thisPlayer.ship.gold + "  Break: " + thisPlayer.ship.breakSaves);
        //              1234567890123456789012345678901234567890123456789012345
        //              1234567
        //                     1234567890
        //                               1234567
        //                                      123456789
        //                                               1234
        //                                                   12
        //                                                     1234567890123
        //																    123456789012345678
        sendTopMessage("   Gold  DmgGiven  Bonus  DmgRcvd Cfl Brk Q Class       Name");
        StringBuffer scoreSB = new StringBuffer();
        scoreSB.append(format("", new Integer(thisPlayer.ship.gold).toString(), 7));
        scoreSB.append(format("", new Integer(thisPlayer.ship.totalDamageGiven).toString(), 10));
        scoreSB.append(format("", new Integer(thisPlayer.ship.totalBonus).toString(), 7));
        scoreSB.append(format("", new Integer(thisPlayer.ship.totalDamageReceived).toString(), 9));
        scoreSB.append(format("", new Integer(thisPlayer.ship.conflicts).toString(), 4));
        scoreSB.append(format(" ", new Integer(thisPlayer.ship.breakSaves).toString(), 4));

        if (thisPlayer.ship.currentQuadrant.name.equals("Alpha Quadrant")) {
            scoreSB.append(" A");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Beta Quadrant")) {
            scoreSB.append(" B");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Gamma Quadrant")) {
            scoreSB.append(" G");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Omega Quadrant")) {
            scoreSB.append(" O");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Nu Quadrant")) {
            scoreSB.append(" N");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Delta Quadrant")) {
            scoreSB.append(" D");
        }

        scoreSB.append(format(" " + thisPlayer.ship.shipClass, "", 13));
        scoreSB.append(format(thisPlayer.shipName, "", 18));
        sendMessage(scoreSB.toString());

    }

    protected void showHelpScreen() {
        if (showingHelpScreen)
            return;

        TrekDataInterface data = new TrekDataInterface();
        Vector helpLines = data.loadHelpFile(helpScreenLetter);

        clearScannerChecker(TrekHud.SCANNER_HELP);

        if (helpLines.size() == 0) {
            sendMessage("Help file does not exist.");
            showHelpScreen = false;
            helpScreenLetter = "";
            return;
        }

        String helpBuffer = "";

        drawScannerLine(3, "Help Screen " + helpScreenLetter);

        for (int x = 0; x < 13; x++) {
            try {
                helpBuffer = format((String) helpLines.elementAt(x), "", 20);
                drawScannerLine(x + 4, helpBuffer);
            } catch (Exception e) {
                helpBuffer = format("", "", 20);
                drawScannerLine(x + 4, helpBuffer);
            }
        }

        showingHelpScreen = true;
    }

    protected void showOdometerPage() {
        showOdometer = true;

        clearScannerChecker(TrekHud.SCANNER_ODOMETER);

        StringBuffer sb = new StringBuffer();

        drawScannerLine(3, format("", "Odometer Page", 20));
        //drawScannerLine(4, "--------------------");
        drawScannerLine(4, format("", this.player.getRankText(this.player.ship), 20));
        drawScannerLine(5, format("", this.player.shipName, 20));
        drawScannerLine(6, format("", this.player.ship.shipClass, 20));
        drawScannerLine(7, format("Launched:", "", 20));
        String dateString = new Date(this.player.ship.dateLaunched).toString();
        dateString = dateString.substring(0, 19);
        drawScannerLine(8, format("", dateString, 20));
        drawScannerLine(9, format("Time Played:", "", 20));
        int hours = this.player.ship.secondsPlayed / 3600;
        int minutes = (this.player.ship.secondsPlayed / 60) - (hours * 60);
        int seconds = this.player.ship.secondsPlayed % 60;
        // format to 2 characters
        if (hours < 10)
            sb.append("0");
        sb.append(new Integer(hours).toString());
        sb.append(":");
        if (minutes < 10)
            sb.append("0");
        sb.append(new Integer(minutes).toString());
        sb.append(":");
        if (seconds < 10)
            sb.append("0");
        sb.append(new Integer(seconds).toString());
        drawScannerLine(10, format("", sb.toString(), 20));
        drawScannerLine(11, "                    ");
        drawScannerLine(12, format(" Torps Used:", new Integer(this.player.ship.torpsFired).toString(), 20));
        drawScannerLine(13, format(" Mines Used:", new Integer(this.player.ship.minesDropped).toString(), 20));
        drawScannerLine(14, format("Drones Used:", new Integer(this.player.ship.dronesFired).toString(), 20));
        drawScannerLine(15, format("Units Traveled:", "", 20));
        drawScannerLine(16, format("", new Long(Math.round(this.player.ship.unitsTraveled)).toString(), 20));
    }

    protected void showShipStats(TrekShip targetShip) {
        showStats = true;
        if (targetShip == null || !targetShip.isPlaying()) {
            this.targetShip = null;
            showStats = false;
            return;
        }

        if (this.targetShip != targetShip)
            this.targetShip = targetShip;

        clearScannerChecker(TrekHud.SCANNER_STATS);

        /*
           * +Friendly Voyager
             |Romulan Warbird
             |Warp:  Turn Max  Emer
             |       12.0 13.0 171
             |Phaser Banks:   40 AG
             |   Range:    0 -  500
             |Torpedo Tubes:   1 VP
                 |   Range:  500 - 2000
             |Torps Mines Drones
                 |  35     6     0
             |Scan Range:    ~ 8000
             |Cloak
             |Home:         Romulus
             +
           */

        drawScannerLine(3, format(targetShip.name, "", 20));
        drawScannerLine(4, format(targetShip.fullClassName, "", 20));
        drawScannerLine(5, format("Warp: Turn Max  Emer", "", 20));
        drawScannerLine(6, format("      " + format("", "" + targetShip.maxTurnWarp, 4) + " " + format("", "" + targetShip.maxCruiseWarp, 4) + " " + format("", "" + targetShip.damageWarp + ".0", 4), "", 20));

        String phaserString = "";

        switch (targetShip.phaserType) {
            case TrekShip.PHASER_AGONIZER:
                phaserString = "AG";
                break;
            case TrekShip.PHASER_EXPANDINGSPHEREINDUCER:
                phaserString = "ESI";
                break;
            case TrekShip.PHASER_NORMAL:
                phaserString = "PH";
                break;
            case TrekShip.PHASER_TELEPORTER:
                phaserString = "TL";
                break;
            case TrekShip.PHASER_DISRUPTOR:
                phaserString = "DI";
                break;
            default:
                phaserString = "PH";
                break;
        }
        drawScannerLine(7, format("Phaser Banks:", targetShip.maxPhaserBanks + " " + phaserString, 20));

        if (targetShip.homePlanet.equals("Cardassia"))
            drawScannerLine(8, format("   Range:", "1 - " + targetShip.minPhaserRange, 20));
        else
            drawScannerLine(8, format("   Range:", "0 - " + targetShip.minPhaserRange, 20));

        String torpString;

        switch (targetShip.torpedoType) {
            case TrekShip.TORPEDO_BOLTPLASMA:
                torpString = "BP";
                break;
            case TrekShip.TORPEDO_NORMAL:
                torpString = "TP";
                break;
            case TrekShip.TORPEDO_OBLITERATOR:
                torpString = "OB";
                break;
            case TrekShip.TORPEDO_PHOTON:
                torpString = "PH";
                break;
            case TrekShip.TORPEDO_PLASMA:
                torpString = "PL";
                break;
            case TrekShip.TORPEDO_VARIABLESPEEDPLASMA:
                torpString = "VP";
                break;
            default:
                torpString = "PH";
                break;
        }

        drawScannerLine(9, format("Torpedo Tubes:", targetShip.maxTorpedoBanks + " " + torpString, 20));
        drawScannerLine(10, format("   Range:", targetShip.minTorpedoRange + " - " + targetShip.maxTorpedoRange, 20));
        drawScannerLine(11, format("Torps Mines Drones", "", 20));
        drawScannerLine(12, format(format(" ", targetShip.maxTorpedoStorage + " ", 4) + " " + format(" ", targetShip.maxMineStorage + " ", 5) + " " + format(" ", targetShip.maxDroneStorage + " ", 6), "", 20));
        drawScannerLine(13, format("Scan Range:", "~ " + new Double(targetShip.scanRange).intValue(), 20));
        drawScannerLine(14, format((targetShip.cloak) ? "Cloak" : "", "", 20));
        drawScannerLine(15, format("", "", 20));
        drawScannerLine(16, format("Home:", targetShip.homePlanet, 20));
    }

    protected void showInventoryScreen() {
        String blankScannerLine = TrekAnsi.eraseToEndOfLine(player);
        showInventory = true;

        clearScannerChecker(TrekHud.SCANNER_INVENTORY);

        drawScannerLine(3, format(" Inventory", "", 20));
        drawScannerLine(4, format("--------------------", "", 20));

        if (player.ship.buoyTimeout <= 0) {
            drawScannerLine(6, format(" b)", "buoy", 20));
        } else drawScannerLine(6, blankScannerLine);
        if (player.ship.corbomite) {
            drawScannerLine(7, format(" c)", "corbomite device", 20));
        } else drawScannerLine(7, blankScannerLine);
        if (player.ship.iridium) {
            drawScannerLine(8, format(" i)", "iridium mine", 20));
        } else drawScannerLine(8, blankScannerLine);
        if (player.ship.lithium) {
            drawScannerLine(9, format(" l)", "lithium mine", 20));
        } else drawScannerLine(9, blankScannerLine);
        if (player.ship.magnabuoy) {
            drawScannerLine(10, format(" m)", "magnabuoy", 20));
        } else drawScannerLine(10, blankScannerLine);
        if (player.ship.neutron) {
            drawScannerLine(11, format(" n)", "neutron mine", 20));
        } else drawScannerLine(11, blankScannerLine);
        if (player.ship.seeker) {
            drawScannerLine(12, format(" s)", "seeker probe", 20));
        } else drawScannerLine(12, blankScannerLine);
    }

    protected void updateHud() {
        // If the players ship is dead.
        if (player.ship == null)
            return;

        if (disabled) return;

        try {
            tick++;
            int offset;

            updateScanner();

            if (tick == 64)
                tick = 0;

            if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_XYZUPDATE) == 0) {
                setHudValue("MyCurrentX", new Integer(new Double(player.ship.point.x).intValue()).toString());
                setHudValue("MyCurrentY", new Integer(new Double(player.ship.point.y).intValue()).toString());
                setHudValue("MyCurrentZ", new Integer(new Double(player.ship.point.z).intValue()).toString());
            } else {
                // by making this test for '1' instead of '0', we can force the stuff to show on initial ship load
                if ((tick % player.playerOptions.getOption(TrekPlayerOptions.OPTION_XYZUPDATE)) == 1) {
                    setHudValue("MyCurrentX", new Integer(new Double(player.ship.point.x).intValue()).toString());
                    setHudValue("MyCurrentY", new Integer(new Double(player.ship.point.y).intValue()).toString());
                    setHudValue("MyCurrentZ", new Integer(new Double(player.ship.point.z).intValue()).toString());
                }
            }

            String buffer1 = "", buffer2 = "", buffer3 = "";

            // Update the quadrant name and whether or not we are cloaked.
            if (!getHudValueString("MyCurrentQuadrant").equals(player.ship.currentQuadrant.name)) {
                player.sendText(TrekAnsi.locate(16, 43, player) + player.ship.currentQuadrant.name + "-");
                setHudValue("MyCurrentQuadrant", player.ship.currentQuadrant.name);
            }

            // Draw the objects in the tactical display.
            drawObjects();


            // Draw our stats screen for this ship.
            drawHudLine(4, format("Warp Energy:", new Integer(player.ship.getWarpEnergy()).toString(), 20));
            drawHudLine(5, format("Impulse Energy:", new Integer(player.ship.impulseEnergy).toString(), 20));
            drawHudLine(6, format("Power Unused:", new Integer(player.ship.getAvailablePower()).toString(), 20));

            if (player.ship.transwarpEngaged) {
                if (player.ship.warpSpeed >= 0) {
                    buffer1 = format("Warp:", "* " + getFormattedWarpSpeed(player.ship.warpSpeed - 30), 20);
                } else {
                    buffer1 = format("Warp:", "* " + getFormattedWarpSpeed(player.ship.warpSpeed + 30), 20);
                }
            } else {
                buffer1 = format("Warp:", getFormattedWarpSpeed(player.ship.warpSpeed), 20);
            }
            drawHudLine(7, buffer1);

            drawHudLine(8, format("Shields:", new Integer(player.ship.shields).toString() + "%", 20));

            buffer1 = format("Ph:", new Integer(player.ship.phasers).toString(), 7);
            buffer2 = getHudValueString("MyPhasers");

            if (!buffer2.equals(buffer1)) {
                offset = getOffset(buffer2, buffer1);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(9, offset + 1, player) + getOptimizedOutput(buffer2, buffer1));
                } else {
                    player.sendText(TrekAnsi.locate(9, 1, player) + buffer1);
                }

                setHudValue("MyPhasers", buffer1);
            }

            buffer2 = format("Damage:", new Integer(player.ship.damage).toString(), 11);
            buffer1 = getHudValueString("MyDamage");

            if (!buffer1.equals(buffer2)) {
                offset = getOffset(buffer1, buffer2);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(9, 10 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                } else {
                    player.sendText(TrekAnsi.locate(9, 10, player) + buffer2);
                }

                setHudValue("MyDamage", buffer2);
            }

            buffer1 = format("Tp:", new Integer(player.ship.torpedoes).toString(), 7);
            buffer2 = getHudValueString("MyTorpedoes");

            if (!buffer2.equals(buffer1)) {
                offset = getOffset(buffer2, buffer1);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(10, offset + 1, player) + getOptimizedOutput(buffer2, buffer1));
                } else {
                    player.sendText(TrekAnsi.locate(10, 1, player) + buffer1);
                }

                setHudValue("MyTorpedoes", buffer1);
            }

            buffer2 = format("DmgCtl:", new Integer(player.ship.damageControl).toString(), 11);
            buffer1 = getHudValueString("MyDamageControl");

            if (!buffer1.equals(buffer2)) {
                offset = getOffset(buffer1, buffer2);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(10, 10 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                } else {
                    player.sendText(TrekAnsi.locate(10, 10, player) + buffer2);
                }

                setHudValue("MyDamageControl", buffer2);
            }

            drawHudLine(11, format("Life Support:", new Integer(player.ship.lifeSupport).toString() + "%", 20));
            if (tick == 1 || (player.ship.orbiting && player.ship.orbitTarget.givesAntimatter(player.ship)))
                drawHudLine(12, format("Anti-Matter:", new Integer(player.ship.antiMatter).toString(), 20));

            // Draw all the rest as seperate.
            buffer1 = format("X:", getHudValueString("MyCurrentX"), 9);
            buffer2 = getHudValueString("MyHudX");

            if (!buffer2.equals(buffer1)) {
                offset = getOffset(buffer2, buffer1);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(13, offset + 1, player) + getOptimizedOutput(buffer2, buffer1));
                } else {
                    player.sendText(TrekAnsi.locate(13, 1, player) + buffer1);
                }

                setHudValue("MyHudX", buffer1);
            }

            if (player.ship.cloak) {
                buffer2 = format("Cloak:", new Integer(player.ship.cloakTimeCurrent).toString(), 10);
            } else {
                if (player.ship.drones) {
                    buffer2 = format("Drones:", new Integer(player.ship.droneCount).toString(), 10);
                } else if (player.ship.mines) {
                    buffer2 = format("Mines:", new Integer(player.ship.mineCount).toString(), 10);
                } else {
                    buffer2 = "          ";
                }
            }

            buffer1 = getHudValueString("MyHudX2");

            if (!buffer1.equals(buffer2)) {
                offset = getOffset(buffer1, buffer2);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(13, 11 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                } else {
                    player.sendText(TrekAnsi.locate(13, 11, player) + buffer2);
                }

                setHudValue("MyHudX2", buffer2);
            }

            buffer1 = format("Y:", getHudValueString("MyCurrentY"), 9);
            buffer2 = getHudValueString("MyHudY");

            if (!buffer2.equals(buffer1)) {
                offset = getOffset(buffer2, buffer1);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(14, 1 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                } else {
                    player.sendText(TrekAnsi.locate(14, 1, player) + buffer1);
                }

                setHudValue("MyHudY", buffer1);
            }

            if (player.ship.cloak) {
                if (player.ship.drones) {
                    buffer2 = format("Drones:", new Integer(player.ship.droneCount).toString(), 10);
                } else if (player.ship.mines) {
                    buffer2 = format("Mines:", new Integer(player.ship.mineCount).toString(), 10);
                } else {
                    buffer2 = "          ";
                }
            } else {
                if (player.ship.mines) {
                    if (player.ship.cloak || player.ship.drones) {
                        buffer2 = format("Mines:", new Integer(player.ship.mineCount).toString(), 10);
                    }
                } else {
                    buffer2 = "          ";
                }
            }

            buffer1 = getHudValueString("MyHudY2");

            if (!buffer1.equals(buffer2)) {
                offset = getOffset(buffer1, buffer2);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(14, 11 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                } else {
                    player.sendText(TrekAnsi.locate(14, 11, player) + buffer2);
                }

                setHudValue("MyHudY2", buffer2);
            }

            buffer1 = format("Z:", getHudValueString("MyCurrentZ"), 9);
            buffer2 = getHudValueString("MyHudZ");

            if (!buffer2.equals(buffer1)) {
                offset = getOffset(buffer2, buffer1);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(15, 1 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                } else {
                    player.sendText(TrekAnsi.locate(15, 1, player) + buffer1);
                }

                setHudValue("MyHudZ", buffer1);
            }

            buffer2 = format("HD:", player.ship.getHeading() + "'" + player.ship.getPitch(), 10);
            buffer1 = getHudValueString("MyHudZ2");

            if (!buffer1.equals(buffer2)) {
                offset = getOffset(buffer1, buffer2);

                if (offset != 0) {
                    player.sendText(TrekAnsi.locate(15, 11 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                } else {
                    player.sendText(TrekAnsi.locate(15, 11, player) + buffer2);
                }

                setHudValue("MyHudZ2", buffer2);
            }

            // Intercept, Orbitable, In Orbit, Docable, Docked, exceeded warp speeds maxes.
            if (Math.abs(player.ship.warpSpeed) != 0) {
                if (Math.abs(player.ship.warpSpeed) <= player.ship.maxCruiseWarp) {
                    buffer1 = format("", "", 23);
                }

                if (Math.abs(player.ship.warpSpeed) > player.ship.maxTurnWarp) {
                    buffer1 = format("Exceeding Turn Warp", "", 23);
                }

                if (Math.abs(player.ship.warpSpeed) > player.ship.maxCruiseWarp) {
                    buffer1 = format("Exceeding Cruise Warp", "", 23);
                }

                if (player.ship.dockable || player.ship.orbitable) {
                    if (player.ship.dockable)
                        buffer1 = format("Dockable", "", 23);
                    else
                        buffer1 = format("Orbitable", "", 23);
                }
            } else {
                if (player.ship.docked || player.ship.orbiting) {
                    if (player.ship.docked)
                        buffer1 = format("Docked", "", 23);
                    else
                        buffer1 = format("In Orbit", "", 23);
                } else {
                    if (player.ship.dockable || player.ship.orbitable) {
                        if (player.ship.dockable)
                            buffer1 = format("Dockable", "", 23);
                        else
                            buffer1 = format("Orbitable", "", 23);
                    } else {
                        buffer1 = format("", "", 23);
                    }
                }
            }

            if (player.ship.cloaked) {
                buffer2 = format("Cloak", "", 7);
            } else {
                buffer2 = format("", "", 7);
            }

            if (!loadingMessage.equals(""))
                buffer3 = format(loadingMessage, "", 29);
            else
                buffer3 = format("", "", 29);

            drawHudLine(17, buffer1 + buffer2 + buffer3);

            if (player.ship.isIntercepting()) {
                if (player.ship.interceptTarget != null)
                    buffer1 = "Intercepting: " + player.ship.interceptTarget.name + TrekAnsi.eraseToEndOfLine(player);
                else if (player.ship.intCoordPoint != null)
                    buffer1 = "Intercepting: " + player.ship.intCoordPoint.toString() + TrekAnsi.eraseToEndOfLine(player);
            } else {
                buffer1 = TrekAnsi.clearRow(18, player);
            }

            drawHudLine(18, buffer1);

            // Weapons lock.
            if (player.ship.lockTarget != null) {
                drawScannerLine(17, format("Weapons Locked (" + player.ship.lockTarget.scanLetter + ")", "", 20));
            } else {
                drawScannerLine(17, format("", "", 20));
            }
        } catch (NullPointerException npe) {
            // in case we killed the socket
        }
    }

    protected void showOptionScreen() {
        String optionBuffer = "";

        optionBuffer = format("Options", "", 20);
        drawScannerLine(3, optionBuffer);

        optionBuffer = format("1 Beep:", player.playerOptions.getOptionString(TrekPlayerOptions.OPTION_BEEP), 20);
        drawScannerLine(4, optionBuffer);

        optionBuffer = format("2 Roster:", player.playerOptions.getOptionString(TrekPlayerOptions.OPTION_ROSTER), 20);
        drawScannerLine(5, optionBuffer);

        optionBuffer = format("3 Tac Range:", player.playerOptions.getOptionString(TrekPlayerOptions.OPTION_OBJECTRANGE), 20);
        drawScannerLine(6, optionBuffer);

        optionBuffer = format("4 Bearing Updt:", player.playerOptions.getOptionString(TrekPlayerOptions.OPTION_BEARINGUPDATE), 20);
        drawScannerLine(7, optionBuffer);

        optionBuffer = format("5 Distance Updt:", player.playerOptions.getOptionString(TrekPlayerOptions.OPTION_RANGEUPDATE), 20);
        drawScannerLine(8, optionBuffer);

        optionBuffer = format("6 XYZ Updt:", player.playerOptions.getOptionString(TrekPlayerOptions.OPTION_XYZUPDATE), 20);
        drawScannerLine(9, optionBuffer);

        optionBuffer = format("7 DmgReport Min:", player.playerOptions.getOptionString(TrekPlayerOptions.OPTION_UNKNOWN), 20);
        drawScannerLine(10, optionBuffer);

        optionBuffer = format("8 DmgReport:", player.playerOptions.getOptionString(TrekPlayerOptions.OPTION_DAMAGEREPORT), 20);
        drawScannerLine(11, optionBuffer);

        optionBuffer = format("9 Scanner:", "", 20);
        drawScannerLine(12, optionBuffer);

        optionBuffer = format("", "", 20);
        drawScannerLine(13, optionBuffer);

        optionBuffer = format("Press any other key", "", 20);
        drawScannerLine(14, optionBuffer);

        optionBuffer = format("to exit options.", "", 20);
        drawScannerLine(15, optionBuffer);

        optionBuffer = format("", "", 20);
        drawScannerLine(16, optionBuffer);
    }

    protected void updateScanner() {
        int offset;
        double actualScanRange = player.ship.scanRange;

        if (disabled) return;

        if (player.ship.nebulaTarget != null)
            actualScanRange = 1000;

        if (showHelpScreen) {
            showHelpScreen();
            return;
        }

        if (this.showPlayerPage != 0) {
            showPlayers();
            return;
        }

        if (showStats) {
            showShipStats(targetShip);
            return;
        }

        if (showOptionScreen) {
            showOptionScreen();
            return;
        }


        if (showOdometer) {
            showOdometerPage();
            return;
        }

        if (showInventory) {
            showInventoryScreen();
            return;
        }

        if (!TrekMath.canScanShip(player.ship, player.ship.scanTarget)) {
            clearScanner();
        }

        if (player.ship.scanTarget == null) {
            return;
        }


        // correct NPE when currently scanned object has been removed from quadrant since last tick
        // ie, shot/expired drone/mine/buoy/obs device, or a player quitting, saving, etc.
        if (!player.ship.currentQuadrant.isInQuadrant(player.ship.scanTarget)) {
            player.ship.scanTarget = null;
            return;
        }

        try {

            // option is really only for updating your own ship's xyz; you always want to have
            // the up-to-date info on scanned objects/ships
            //if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_XYZUPDATE) == 0) {
            setHudValue("ScanCurrentX", new Integer(new Double(player.ship.scanTarget.point.x).intValue()).toString());
            setHudValue("ScanCurrentY", new Integer(new Double(player.ship.scanTarget.point.y).intValue()).toString());
            setHudValue("ScanCurrentZ", new Integer(new Double(player.ship.scanTarget.point.z).intValue()).toString());
            //}
            //else {
            //	if ((tick % player.playerOptions.getOption(TrekPlayerOptions.OPTION_XYZUPDATE)) == 0) {
            //		setHudValue("ScanCurrentX", new Integer(new Double(player.ship.scanTarget.point.x).intValue()).toString());
            //		setHudValue("ScanCurrentY", new Integer(new Double(player.ship.scanTarget.point.y).intValue()).toString());
            //		setHudValue("ScanCurrentZ", new Integer(new Double(player.ship.scanTarget.point.z).intValue()).toString());
            //	}
            //}

            if (TrekUtilities.isObjectShip(player.ship.scanTarget)) {
                TrekShip thisShip = null;
                String scanName = "";
                boolean observerScan = false;

                if (TrekUtilities.isObjectShip(player.ship.scanTarget)) {
                    thisShip = (TrekShip) player.ship.scanTarget;
                    scanName = thisShip.name;
                }

                if (TrekUtilities.isObjectObserverDevice(player.ship.scanTarget)) {
                    observerScan = true;
                    TrekObserverDevice observer = (TrekObserverDevice) player.ship.scanTarget;

                    if (observer.isScanning()) {
                        thisShip = observer.scanTarget;
                        scanName = observer.scanTarget.name;

                        // update the observed ship's coord in ship spotted database
                        if (player.ship.scannedHistory.containsKey(thisShip.scanLetter)) {
                            player.ship.scannedHistory.remove(thisShip.scanLetter);
                        }

                        player.ship.scannedHistory.put(thisShip.scanLetter, new TrekCoordHistory(thisShip.scanLetter, thisShip.name, new Trek3DPoint(thisShip.point)));
                    }
                }

                if (thisShip != null) {
                    // Draw the object name.
                    drawScannerLine(3, format(scanName, "", 20));

                    boolean canSeeFull = false;
                    double distance = TrekMath.getDistance(player.ship, thisShip);

                    if (distance < (actualScanRange * .90))
                        canSeeFull = true;

                    String buffer = "", buffer1 = "", buffer2 = "";

                    buffer = format("Warp Energy:", new Integer(thisShip.getWarpEnergy()).toString(), 20);
                    drawScannerLine(4, buffer);

                    buffer = format("Impulse Energy:", new Integer(thisShip.impulseEnergy).toString(), 20);
                    drawScannerLine(5, buffer);

                    buffer = format("Power Unused:", new Integer(thisShip.getAvailablePower()).toString(), 20);
                    drawScannerLine(6, buffer);

                    buffer = format("Warp:", getFormattedWarpSpeed(thisShip.warpSpeed), 20);
                    drawScannerLine(7, buffer);

                    if (canSeeFull)
                        buffer = format("Shields:", new Integer(thisShip.shields).toString() + "%", 20);
                    else
                        buffer = format("Shields:", "---%", 20);

                    drawScannerLine(8, buffer);

                    buffer1 = format("Ph:", new Integer(thisShip.phasers).toString(), 7);
                    buffer2 = getHudValueString("ScanPhasers");

                    if (!buffer2.equals(buffer1)) {
                        offset = getOffset(buffer2, buffer1);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(9, 60 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                        } else {
                            player.sendText(TrekAnsi.locate(9, 60, player) + buffer1);
                        }

                        setHudValue("ScanPhasers", buffer1);
                    }

                    buffer2 = format("Damage:", new Integer(thisShip.damage).toString(), 11);
                    buffer1 = getHudValueString("ScanDamage");

                    if (!buffer1.equals(buffer2)) {
                        offset = getOffset(buffer1, buffer2);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(9, 69 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                        } else {
                            player.sendText(TrekAnsi.locate(9, 69, player) + buffer2);
                        }

                        setHudValue("ScanDamage", buffer2);
                    }

                    setHudValue("MyScannerLine9", "Empty");

                    buffer1 = format("Tp:", new Integer(thisShip.torpedoes).toString(), 7);
                    buffer2 = getHudValueString("ScanTorpedoes");

                    if (!buffer2.equals(buffer1)) {
                        offset = getOffset(buffer2, buffer1);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(10, 60 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                        } else {
                            player.sendText(TrekAnsi.locate(10, 60, player) + buffer1);
                        }

                        setHudValue("ScanTorpedoes", buffer1);
                    }

                    buffer2 = format("DmgCtl:", new Integer(thisShip.damageControl).toString(), 11);
                    buffer1 = getHudValueString("ScanDamageControl");

                    if (!buffer1.equals(buffer2)) {
                        offset = getOffset(buffer1, buffer2);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(10, 69 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                        } else {
                            player.sendText(TrekAnsi.locate(10, 69, player) + buffer2);
                        }

                        setHudValue("ScanDamageControl", buffer2);
                    }

                    setHudValue("MyScannerLine10", "Empty");

                    if (canSeeFull)
                        buffer = format("Life Support:", new Integer(thisShip.lifeSupport).toString() + "%", 20);
                    else
                        buffer = format("Life Support:", "---%", 20);

                    drawScannerLine(11, buffer);

                    buffer = format("Anti-Matter", new Integer(thisShip.antiMatter).toString(), 20);
                    drawScannerLine(12, buffer);

                    buffer1 = format("X:", getHudValueString("ScanCurrentX"), 9);
                    buffer2 = getHudValueString("ScanHudX");

                    if (!buffer2.equals(buffer1)) {
                        offset = getOffset(buffer2, buffer1);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(13, 60 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                        } else {
                            player.sendText(TrekAnsi.locate(13, 60, player) + buffer1);
                        }

                        setHudValue("ScanHudX", buffer1);
                    }

                    if (thisShip.cloak) {
                        if (observerScan)
                            buffer2 = format("Cloak:", "" + thisShip.cloakTimeCurrent, 10);
                        else
                            buffer2 = format("Cloak:", "---", 10);
                    } else {
                        if (thisShip.drones) {
                            if (observerScan)
                                buffer2 = format("Drones:", "" + thisShip.droneCount, 10);
                            else
                                buffer2 = format("Drones:", "---", 10);

                        } else if (thisShip.mines) {
                            if (observerScan)
                                buffer2 = format("Mines:", "" + thisShip.mineCount, 10);
                            else
                                buffer2 = format("Mines:", "---", 10);
                        } else {
                            buffer2 = TrekAnsi.eraseToEndOfLine(player);
                        }
                    }

                    buffer1 = getHudValueString("ScanHudX2");

                    if (!buffer1.equals(buffer2)) {
                        offset = getOffset(buffer1, buffer2);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(13, 70 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                        } else {
                            player.sendText(TrekAnsi.locate(13, 70, player) + buffer2);
                        }

                        setHudValue("ScanHudX2", buffer2);
                    }

                    setHudValue("MyScannerLine13", "Empty");

                    buffer1 = format("Y:", getHudValueString("ScanCurrentY"), 9);
                    buffer2 = getHudValueString("ScanHudY");

                    if (!buffer2.equals(buffer1)) {
                        offset = getOffset(buffer2, buffer1);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(14, 60 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                        } else {
                            player.sendText(TrekAnsi.locate(14, 60, player) + buffer1);
                        }

                        setHudValue("ScanHudY", buffer1);
                    }

                    if (thisShip.cloak) {
                        if (thisShip.drones) {
                            if (observerScan)
                                buffer2 = format("Drones:", "" + thisShip.droneCount, 10);
                            else
                                buffer2 = format("Drones:", "---", 10);
                        } else if (thisShip.mines) {
                            if (observerScan)
                                buffer2 = format("Mines:", "" + thisShip.mineCount, 10);
                            else
                                buffer2 = format("Mines:", "---", 10);
                        } else {
                            buffer2 = "          ";
                        }
                    } else {
                        if (thisShip.mines) {
                            if (thisShip.cloak || thisShip.drones) {
                                if (observerScan)
                                    buffer2 = format("Mines:", "" + thisShip.mineCount, 10);
                                else
                                    buffer2 = format("Mines:", "---", 10);
                            }
                        } else {
                            buffer2 = "          ";
                        }
                    }

                    buffer1 = getHudValueString("ScanHudY2");

                    if (!buffer1.equals(buffer2)) {
                        offset = getOffset(buffer1, buffer2);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(14, 70 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                        } else {
                            player.sendText(TrekAnsi.locate(14, 70, player) + buffer2);
                        }

                        setHudValue("ScanHudY2", buffer2);
                    }

                    setHudValue("MyScannerLine14", "Empty");

                    buffer1 = format("Z:", getHudValueString("ScanCurrentZ"), 9);
                    buffer2 = getHudValueString("ScanHudZ");

                    if (!buffer2.equals(buffer1)) {
                        offset = getOffset(buffer2, buffer1);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(15, 60 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                        } else {
                            player.sendText(TrekAnsi.locate(15, 60, player) + buffer1);
                        }

                        setHudValue("ScanHudZ", buffer1);
                    }


                    if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_BEARINGUPDATE) == 0) {
                        setHudValue("ScanCurrentHD", format("HD:", thisShip.getHeading() + "'" + thisShip.getPitch(), 10));
                    } else {
                        if ((tick % player.playerOptions.getOption(TrekPlayerOptions.OPTION_BEARINGUPDATE)) == 0) {
                            setHudValue("ScanCurrentHD", format("HD:", thisShip.getHeading() + "'" + thisShip.getPitch(), 10));
                        }
                    }


                    buffer2 = getHudValueString("ScanCurrentHD");
                    buffer1 = getHudValueString("ScanHudZ2");

                    if (!buffer1.equals(buffer2)) {
                        offset = getOffset(buffer1, buffer2);

                        if (offset != 0) {
                            player.sendText(TrekAnsi.locate(15, 70 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                        } else {
                            player.sendText(TrekAnsi.locate(15, 70, player) + buffer2);
                        }

                        setHudValue("ScanHudZ2", buffer2);
                    }

                    setHudValue("MyScannerLine15", "Empty");

                    if (thisShip.shipClass.equals("INTERCEPTOR")) {
                        buffer = format("Gold:" + new Integer(thisShip.gold).toString(), "INTRCPTR", 20);
                    } else {
                        buffer = format("Gold:" + new Integer(thisShip.gold).toString(), thisShip.shipClass, 20);
                    }
                    drawScannerLine(16, buffer);

                    return;
                }
            }

            // Default lines for all other objects.
            // Draw the object name.
            drawScannerLine(3, format(player.ship.scanTarget.name, "", 20));

            String distance = new Integer(new Double(Math.round(TrekMath.getDistance(player.ship, player.ship.scanTarget))).intValue()).toString();
            String blankScannerLine = TrekAnsi.eraseToEndOfLine(player);

            boolean canSeeFull = false;

            if (TrekMath.getDistance(player.ship, player.ship.scanTarget) < (player.ship.scanRange * .90))
                canSeeFull = true;

            if (player.playerOptions.getOption(TrekPlayerOptions.OPTION_RANGEUPDATE) == 0) {
                setHudValue("ScanCurrentRange", distance);
            } else {
                if ((tick % player.playerOptions.getOption(TrekPlayerOptions.OPTION_RANGEUPDATE)) == 0) {
                    setHudValue("ScanCurrentRange", distance);
                }
            }

            drawScannerLine(6, format("Distance:", getHudValueString("ScanCurrentRange"), 20));

            // don't display common xyz aspects if the scanTarget is an observer device that is scanning a target
            if ((!TrekUtilities.isObjectObserverDevice(player.ship.scanTarget)) ||
                    ((TrekUtilities.isObjectObserverDevice(player.ship.scanTarget)) &&
                            !((TrekObserverDevice) player.ship.scanTarget).isScanning())) {
                drawScannerLine(13, format("X:" + format("", getHudValueString("ScanCurrentX"), 7), "", 20));
                drawScannerLine(14, format("Y:" + format("", getHudValueString("ScanCurrentY"), 7), "", 20));
                // don't display common z line, if the object is a drone (so that we can display drone heading/pitch)
                // ditto with comet
                if (!TrekUtilities.isObjectDrone(player.ship.scanTarget) &&
                        !TrekUtilities.isObjectComet(player.ship.scanTarget))
                    drawScannerLine(15, format("Z:" + format("", getHudValueString("ScanCurrentZ"), 7), "", 20));
            }

            // If it is a Starbase.
            if (TrekUtilities.isObjectStarbase(player.ship.scanTarget)) {
                TrekStarbase base = (TrekStarbase) player.ship.scanTarget;
                String buffer1 = "";

                buffer1 += (base.fixesLifeSupport) ? "L " : "";
                buffer1 += (base.givesMines) ? "M " : "";
                buffer1 += (base.givesDrones) ? "D " : "";
                buffer1 += (base.givesTorps) ? "T " : "";

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);

                if (canSeeFull) {
                    drawScannerLine(7, format("Damage:", "" + base.damage, 20));
                    drawScannerLine(8, format("Gold:", "" + base.gold, 20));
                } else {
                    drawScannerLine(7, format("Damage:", "----", 20));
                    drawScannerLine(8, format("Gold:", "------", 20));
                }

                drawScannerLine(9, format("Code: " + buffer1, "", 20));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);
            }

            if (TrekUtilities.isObjectPlanet(player.ship.scanTarget)) {
                TrekPlanet planet = (TrekPlanet) player.ship.scanTarget;
                String buffer1 = "";

                buffer1 = "";
                buffer1 += (planet.givesCrystals(player.ship)) ? "C " : "";
                buffer1 += (planet.repairsLifeSupport(player.ship)) ? "L " : "";
                buffer1 += (planet.givesMines(player.ship)) ? "M " : "";
                buffer1 += (planet.givesDrones(player.ship)) ? "D " : "";
                buffer1 += (planet.givesTorps(player.ship)) ? "T " : "";
                buffer1 += (planet.givesAntimatter(player.ship)) ? "A " : "";
                buffer1 += (planet.fixesCloak(player.ship)) ? "FC " : "";
                buffer1 += (planet.fixesTransmitter(player.ship)) ? "FR " : "";
                buffer1 += (planet.installsTranswarp(player.ship)) ? "TW " : "";
                buffer1 += (planet.givesCorbomite(player.ship)) ? "CM " : "";
                buffer1 += (planet.givesNeutron(player.ship)) ? "N " : "";
                buffer1 += (planet.givesIridium(player.ship)) ? "I " : "";
                buffer1 += (planet.givesMagnabuoy(player.ship)) ? "MB " : "";

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);

                if (canSeeFull) {
                    drawScannerLine(7, format("Damage:", "" + planet.damage, 20));
                    drawScannerLine(8, format("Gold:", "" + planet.gold, 20));
                } else {
                    drawScannerLine(7, format("Damage:", "----", 20));
                    drawScannerLine(8, format("Gold:", "------", 20));
                }

                drawScannerLine(9, format("Code: " + buffer1, "", 20));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);
            }

            if (TrekUtilities.isObjectDrone(player.ship.scanTarget)) {
                TrekDrone drone = (TrekDrone) player.ship.scanTarget;
                String buffer1, buffer2 = "";

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(7, format("Energy:", "" + drone.strength, 20));
                drawScannerLine(8, format("Warp:", getFormattedWarpSpeed(drone.warpSpeed), 20));
                drawScannerLine(9, format("Creator:", "", 20));
                drawScannerLine(10, format(drone.owner.name, "", 20));
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                buffer1 = format("Z:", getHudValueString("ScanCurrentZ"), 9);
                buffer2 = getHudValueString("ScanHudZ");

                if (!buffer2.equals(buffer1)) {
                    offset = getOffset(buffer2, buffer1);

                    if (offset != 0) {
                        player.sendText(TrekAnsi.locate(15, 60 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                    } else {
                        player.sendText(TrekAnsi.locate(15, 60, player) + buffer1);
                    }

                    setHudValue("ScanHudZ", buffer1);
                }

                buffer2 = format("HD:", drone.getYaw() + "'" + drone.getPitch(), 10);
                buffer1 = getHudValueString("ScanHudZ2");

                if (!buffer1.equals(buffer2)) {
                    offset = getOffset(buffer1, buffer2);

                    if (offset != 0) {
                        player.sendText(TrekAnsi.locate(15, 70 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                    } else {
                        player.sendText(TrekAnsi.locate(15, 70, player) + buffer2);
                    }

                    setHudValue("ScanHudZ2", buffer2);
                }
                setHudValue("MyScannerLine15", "Empty");

                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectObserverDevice(player.ship.scanTarget)) {
                TrekObserverDevice observer = (TrekObserverDevice) player.ship.scanTarget;
                if (observer.isScanning()) {
                    TrekShip obsScannedShip = observer.scanTarget;
                    drawScannerLine(4, format("[" + observer.scanTarget.name + "]", "", 20));
                    StringBuffer curLine = new StringBuffer();
                    curLine.append(format("WE:", new Integer(obsScannedShip.warpEnergy + obsScannedShip.currentCrystalCount).toString(), 7));
                    curLine.append(format("   Warp:", getFormattedWarpSpeed(obsScannedShip.warpSpeed), 13));
                    drawScannerLine(7, curLine.toString());
                    curLine = new StringBuffer();
                    curLine.append(format("IE:", new Integer(obsScannedShip.impulseEnergy).toString(), 7));
                    curLine.append(format("  Shlds:", new Integer(obsScannedShip.shields).toString() + "%", 13));
                    drawScannerLine(8, curLine.toString());
                    curLine = new StringBuffer();
                    curLine.append(format("Pwr:", new Integer(obsScannedShip.getAvailablePower()).toString(), 7));
                    curLine.append(format(" Damage:", new Integer(obsScannedShip.damage).toString(), 13));
                    drawScannerLine(9, curLine.toString());
                    curLine = new StringBuffer();
                    curLine.append(format("T:", new Integer(obsScannedShip.torpedoCount).toString(), 7));
                    curLine.append(format("  D:", new Integer(obsScannedShip.droneCount).toString(), 7));
                    curLine.append(format(" M:", new Integer(obsScannedShip.mineCount).toString(), 6));
                    drawScannerLine(10, curLine.toString());
                    curLine = new StringBuffer();
                    curLine.append(format("AM:", new Integer(obsScannedShip.antiMatter).toString(), 7));
                    curLine.append(format("     LS:", new Integer(obsScannedShip.lifeSupport).toString() + "%", 13));
                    drawScannerLine(11, curLine.toString());
                    curLine = new StringBuffer();
                    curLine.append(format("HD:", obsScannedShip.getHeading() + "'" + obsScannedShip.getPitch(), 10));
                    if (obsScannedShip.shipClass.equals("INTERCEPTOR")) {
                        curLine.append(format("", "INTRCPTR", 10));
                    } else {
                        curLine.append(format("", obsScannedShip.shipClass, 10));
                    }
                    drawScannerLine(12, curLine.toString());

                    drawScannerLine(13, format(format("X:", getHudValueString("ScanCurrentX"), 9),
                            format("[X:", obsScannedShip.point.getXString() + "]", 11), 20));
                    drawScannerLine(14, format(format("Y:", getHudValueString("ScanCurrentY"), 9),
                            format("[Y", obsScannedShip.point.getYString() + "]", 11), 20));
                    drawScannerLine(15, format(format("Z:", getHudValueString("ScanCurrentZ"), 9),
                            format("[Z:", obsScannedShip.point.getZString() + "]", 11), 20));

                    // update the observed ship's coord in ship spotted database
                    if (player.ship.scannedHistory.containsKey(obsScannedShip.scanLetter)) {
                        player.ship.scannedHistory.remove(obsScannedShip.scanLetter);
                    }
                    player.ship.scannedHistory.put(obsScannedShip.scanLetter, new TrekCoordHistory(obsScannedShip.scanLetter, obsScannedShip.name, new Trek3DPoint(obsScannedShip.point)));

                } else {
                    drawScannerLine(4, blankScannerLine);
                    drawScannerLine(7, format("Warp:", getFormattedWarpSpeed(observer.warpSpeed), 20));
                    drawScannerLine(8, blankScannerLine);
                    drawScannerLine(9, blankScannerLine);
                    drawScannerLine(10, blankScannerLine);
                    drawScannerLine(11, blankScannerLine);
                    drawScannerLine(12, blankScannerLine);
                }
                // common elements regardless of whether obs has scan target
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectTorpedo(player.ship.scanTarget)) {
                TrekTorpedo torpedo = (TrekTorpedo) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                if (torpedo.torpType == TrekShip.TORPEDO_WARHEAD) {
                    drawScannerLine(5, format("Damage:", "" + torpedo.damage, 20));
                } else {
                    drawScannerLine(5, blankScannerLine);
                }
                drawScannerLine(7, format("Energy:", "" + torpedo.strength, 20));
                drawScannerLine(8, format("Warp:", getFormattedWarpSpeed(torpedo.warpSpeed), 20));
                drawScannerLine(9, format("Creator:", "", 20));
                drawScannerLine(10, format(torpedo.owner.name, "", 20));
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectMine(player.ship.scanTarget)) {
                TrekMine mine = (TrekMine) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(7, format("Energy:", "" + mine.strength, 20));
                drawScannerLine(8, format("Creator:", "", 20));
                drawScannerLine(9, format(mine.owner.name, "", 20));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectBuoy(player.ship.scanTarget)) {
                TrekBuoy buoy = (TrekBuoy) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(7, blankScannerLine);
                drawScannerLine(8, format("Creator:", "", 20));
                drawScannerLine(9, format(buoy.owner.name, "", 20));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                if (player.ship instanceof ShipQ)
                    drawScannerLine(16, format("Ownr Cnctn:", new Long(buoy.buoyConn).toString(), 20));
                else
                    drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectCorbomite(player.ship.scanTarget)) {
                TrekCorbomite corbomite = (TrekCorbomite) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(7, format("Energy:", corbomite.energy, 20));
                drawScannerLine(8, format("Creator:", "", 20));
                drawScannerLine(9, format(corbomite.owner.name, "", 20));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectIridium(player.ship.scanTarget)) {
                TrekIridium iridium = (TrekIridium) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(7, blankScannerLine);
                drawScannerLine(8, format("Creator:", "", 20));
                drawScannerLine(9, format(iridium.owner.name, "", 20));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectMagnabuoy(player.ship.scanTarget)) {
                TrekMagnabuoy magnabuoy = (TrekMagnabuoy) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(7, blankScannerLine);
                drawScannerLine(8, format("Creator:", "", 20));
                drawScannerLine(9, format(magnabuoy.owner.name, "", 20));
                drawScannerLine(10, format("Locked On:", "", 20));
                drawScannerLine(11, format(magnabuoy.target.name, "", 20));
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectNeutron(player.ship.scanTarget)) {
                TrekNeutron neutron = (TrekNeutron) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(7, blankScannerLine);
                drawScannerLine(8, format("Creator:", "", 20));
                drawScannerLine(9, format(neutron.owner.name, "", 20));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectShipDebris(player.ship.scanTarget)) {
                TrekShipDebris debris = (TrekShipDebris) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);

                if (debris.gold > 0) {
                    drawScannerLine(7, format("Gold:", new Integer(debris.gold).toString(), 20));
                } else {
                    drawScannerLine(7, format("Gold:", "0", 20));
                }

                drawScannerLine(8, format("Remains of:", "", 20));
                drawScannerLine(9, format(debris.whos, "", 20));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);
                drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectGold(player.ship.scanTarget)) {
                TrekGold gold = (TrekGold) player.ship.scanTarget;

                drawScannerLine(4, blankScannerLine);
                drawScannerLine(5, blankScannerLine);
                drawScannerLine(7, format("Amount:", gold.amount, 20));
                drawScannerLine(8, format("Creator:", "", 20));
                drawScannerLine(9, gold.ownerName + TrekAnsi.eraseToEndOfLine(player));
                drawScannerLine(10, blankScannerLine);
                drawScannerLine(11, blankScannerLine);
                drawScannerLine(12, blankScannerLine);

                if (player.ship instanceof ShipQ)
                    drawScannerLine(16, format("Lifetime:", gold.goldTimeout, 20));
                else
                    drawScannerLine(16, blankScannerLine);

                return;
            }

            if (TrekUtilities.isObjectComet(player.ship.scanTarget)) {
                String buffer1, buffer2;
                TrekComet comet = (TrekComet) player.ship.scanTarget;
                drawScannerLine(7, format("Warp:", getFormattedWarpSpeed(comet.warpSpeed), 20));
                buffer1 = format("Z:", getHudValueString("ScanCurrentZ"), 9);
                buffer2 = getHudValueString("ScanHudZ");

                if (!buffer2.equals(buffer1)) {
                    offset = getOffset(buffer2, buffer1);

                    if (offset != 0) {
                        player.sendText(TrekAnsi.locate(15, 60 + offset, player) + getOptimizedOutput(buffer2, buffer1));
                    } else {
                        player.sendText(TrekAnsi.locate(15, 60, player) + buffer1);
                    }

                    setHudValue("ScanHudZ", buffer1);
                }

                buffer2 = format("HD:", comet.getYaw() + "'" + comet.getPitch(), 10);
                buffer1 = getHudValueString("ScanHudZ2");

                if (!buffer1.equals(buffer2)) {
                    offset = getOffset(buffer1, buffer2);

                    if (offset != 0) {
                        player.sendText(TrekAnsi.locate(15, 70 + offset, player) + getOptimizedOutput(buffer1, buffer2));
                    } else {
                        player.sendText(TrekAnsi.locate(15, 70, player) + buffer2);
                    }

                    setHudValue("ScanHudZ2", buffer2);
                }
                setHudValue("MyScannerLine15", "Empty");

            }

            if (TrekUtilities.isObjectFlag(player.ship.scanTarget)) {
                TrekFlag flag = player.ship.scanTarget.currentQuadrant.getFlag();

                drawScannerLine(4, "[ Payout: " + flag.getPayoutSeconds() + " ]");
                drawScannerLine(5, blankScannerLine);
            }
        } catch (NullPointerException npe) {
            //TrekLog.logException(npe);
        }
    }

    protected void clearMessages() {
        player.sendText(TrekAnsi.clearRow(22, player));
        player.sendText(TrekAnsi.clearRow(23, player));
        player.sendText(TrekAnsi.clearRow(24, player));

        setHudValue("MyHudLine22", format("", "", 79));
        setHudValue("MyHudLine23", format("", "", 79));
        setHudValue("MyHudLine24", format("", "", 79));
    }

    protected void setLoadMessage(String thisMessage) {
        loadingMessage = thisMessage;
    }

    protected String getOptimizedOutput(String currentString, String newString) {
        byte[] current = currentString.getBytes();
        byte[] buf = newString.getBytes();

        if (!currentString.equals(newString)) {
            // Figure out what character starts the change.

            if (currentString.equals("")) {
                return newString;
            } else {
                for (int x = 0; x < current.length; x++) {
                    if (current[x] != buf[x]) {
                        return newString.substring(x, newString.length());
                    }
                }
            }
        } else {
            return "";
        }

        return "";
    }

    protected int getOffset(String currentString, String newString) {
        try {
            byte[] current = currentString.getBytes();
            byte[] buf = newString.getBytes();

            if (!currentString.equals(newString)) {
                // Figure out what character starts the change.

                if (currentString.equals("")) {
                    return 0;
                } else {
                    for (int x = 0; x < current.length; x++) {
                        if (current[x] != buf[x]) {
                            return x;
                        }
                    }
                }
            } else {
                return 0;
            }

            return 0;
        } catch (Exception e) {
            return 0;
        }
    }

    protected void asteroidIndicator(boolean b) {
        if (b) {
            player.sendText(TrekAnsi.locate(16, 11, player) + "a");
        } else {
            player.sendText(TrekAnsi.locate(16, 11, player) + "-");
        }
    }

    protected void nebulaIndicator(boolean b) {
        if (b)
            player.sendText(TrekAnsi.locate(16, 13, player) + "n");
        else
            player.sendText(TrekAnsi.locate(16, 13, player) + "-");
    }

    protected void pulsarIndicator(boolean b) {
        if (b)
            player.sendText(TrekAnsi.locate(16, 15, player) + "p");
        else
            player.sendText(TrekAnsi.locate(16, 15, player) + "-");
    }

    protected void quasarIndicator(boolean b) {
        if (b)
            player.sendText(TrekAnsi.locate(16, 17, player) + "q");
        else
            player.sendText(TrekAnsi.locate(16, 17, player) + "-");
    }

}
