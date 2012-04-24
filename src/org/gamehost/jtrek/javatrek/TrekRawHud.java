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

import java.text.NumberFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: jay
 * Date: Sep 8, 2004
 * Time: 5:13:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrekRawHud extends TrekHud {
    private TrekHudDataShip curShipStats = null;
    private TrekHudDataScanner curScanStats = null;
    private TrekHudDataTactical curTactData[];

    public TrekRawHud(TrekPlayer playerin) {
        player = playerin;
        loadingMessage = "";  // make sure this is initialized
        tacticalDisplay = new TrekObject[20];  // raw data hud enabled for up to 20 ships/objects
        curTactData = new TrekHudDataTactical[20];  // a tactical element for each potential ship/object
    }

    protected void clearMessage(int line) {
        // nothing
    }

    protected void clearTopMessage() {
        // nothing
    }

    protected void clearObject(int position, String hudValue) {
        // nothing
    }

    protected void sendError(String s) {
        // cast the player to the child class to use additional functionality
        TrekRawDataInterface trdi = (TrekRawDataInterface) player;
        trdi.sendError(s);
    }

    protected void clearScanner() {
        player.ship.scanTarget = null;
        player.sendText("<clearscanner />");

        // if we clear the scanner, we also want to set the curScanStats to null
        // this will force all data to be resent to the client on a subsequent scan
        curScanStats = null;
    }

    public void clearScannerChecker(int callingScreen) {
        // nothing
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
            player.sendText("<stat id=\"dmggvn\"><name>" + stat.victim.name + "</name><weapon>" +
                    stat.type + "</weapon><damage>" + tempDmg + "</damage><bonus>" + bonusString +
                    "</bonus><ints>" + stat.structuralDamage + "</ints></stat>");
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

        StringBuffer outData = new StringBuffer();
        outData.append("<stat id=\"dmgrcvd\">");
        if (stat.showAttacker)
            outData.append("<name>" + stat.victor.name + "</name>");
        outData.append("<weapon>" + stat.type + "</weapon>");
        outData.append("<damage>" + stat.damageGiven + "</damage>");
        if (stat.structuralDamage != 0)
            outData.append("<ints>" + stat.structuralDamage + "</ints>");
        outData.append("</stat>");

        player.sendText(outData.toString());
    }

    protected void drawHudLine(int thisLine, String thisString) {
        // nothing
    }

    protected void drawObject(int position, TrekObject thisObj, String hudValue) {
        // nothing
    }

    protected void setStatus(TrekHudDataTactical thdt) {
        if (thdt == null) {
            return;
        }

        thdt.name = thdt.theObj.name;
        TrekObject thisObj = thdt.theObj;
        thdt.bearing = player.ship.getBearingToObj(thdt.theObj);
        thdt.distance = (int) TrekMath.getDistance(player.ship, thdt.theObj);
        thdt.teamNum = player.teamNumber;

        // Check for scan and lock...
        if (TrekUtilities.isObjectShip(thisObj)) {
            TrekShip testScan = (TrekShip) thisObj;

            if (testScan.lockTarget == player.ship) {
                if (testScan.scanTarget != null && testScan.scanTarget == player.ship) {
                    thdt.locked = true;
                } else {
                    thdt.locked = false;
                }
            } else {
                thdt.locked = false;
            }

            if (testScan.scanTarget == player.ship) {
                thdt.scanning = true;
            } else {
                thdt.scanning = false;
            }
        }

        if (TrekUtilities.isObjectWormhole(thisObj)) {
            TrekWormhole hole = (TrekWormhole) thisObj;
            thdt.active = hole.active;
        }

        if (TrekUtilities.isObjectPulsar(thisObj)) {
            TrekZone pulsar = (TrekZone) thisObj;
            thdt.active = pulsar.active;
        }

        if (TrekUtilities.isObjectDrone(thisObj)) {
            TrekDrone drone = (TrekDrone) thisObj;
            if (drone.owner == player.ship) {
                thdt.mine = true;
            } else {
                thdt.mine = false;
            }
        }

        if (TrekUtilities.isObjectBuoy(thisObj)) {
            TrekBuoy buoy = (TrekBuoy) thisObj;
            if (buoy.owner == player.ship) {
                thdt.mine = true;
            } else {
                thdt.mine = false;
            }
        }

        if (TrekUtilities.isObjectMine(thisObj)) {
            TrekMine mine = (TrekMine) thisObj;
            if (mine.owner == player.ship) {
                thdt.mine = true;
            } else {
                thdt.mine = false;
            }
        }

        if (TrekUtilities.isObjectTorpedo(thisObj)) {
            TrekTorpedo torp = (TrekTorpedo) thisObj;
            if (torp.torpType != TrekShip.TORPEDO_WARHEAD) {
                if (torp.owner == player.ship) {
                    thdt.mine = true;
                } else {
                    thdt.mine = false;
                }
            } else {
                if (torp.interceptTarget == player.ship) {
                    thdt.locked = true;
                } else {
                    thdt.locked = false;
                }
            }
        }

        if (TrekUtilities.isObjectObserverDevice(thisObj)) {
            TrekObserverDevice observer = (TrekObserverDevice) thisObj;

            if (observer.scanTarget == player.ship) {
                thdt.scanning = true;
            } else {
                thdt.scanning = false;
            }
        }

        if (TrekUtilities.isObjectCorbomite(thisObj)) {
            TrekCorbomite corbomite = (TrekCorbomite) thisObj;

            if (corbomite.owner == player.ship) {
                thdt.mine = true;
            } else {
                thdt.mine = false;
            }
        }

        if (TrekUtilities.isObjectIridium(thisObj)) {
            TrekIridium iridium = (TrekIridium) thisObj;

            if (iridium.owner == player.ship) {
                thdt.mine = true;
            } else {
                thdt.mine = false;
            }
        }

        if (TrekUtilities.isObjectMagnabuoy(thisObj)) {
            TrekMagnabuoy magnabuoy = (TrekMagnabuoy) thisObj;

            if (magnabuoy.owner == player.ship) {
                thdt.mine = true;
            } else {
                thdt.mine = false;
            }
        }

        if (TrekUtilities.isObjectNeutron(thisObj)) {
            TrekNeutron neutron = (TrekNeutron) thisObj;

            if (neutron.owner == player.ship) {
                thdt.mine = true;
            } else {
                thdt.mine = false;
            }
        }
    }

    protected void drawObjects() {
        player.sendText(doUpdateTactical());
    }

    protected String doUpdateTactical() {
        StringBuffer outTact = new StringBuffer();

        if (!player.isObserving) {
            // store a copy of the tactical array as it was last tick
            TrekHudDataTactical prevTactData[] = new TrekHudDataTactical[20];
            for (int x = 0; x < 20; x++) {
                if (curTactData[x] != null)
                    prevTactData[x] = new TrekHudDataTactical(curTactData[x]);
            }

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

            // First clear all objects and ships that are no longer visible.
            // increased from 12 to 20
            for (int x = 0; x < 20; x++) {
                if (!objects.contains(tacticalDisplay[x]) && !ships.contains(tacticalDisplay[x])) {
                    tacticalDisplay[x] = null;
                    //System.out.println("TACTCHECK: cleared " + x);
                    outTact.append(curTactData[x].clear());
                    curTactData[x] = null;
                }

                //System.out.print("TACTCHECK:" + x);
                if (curTactData[x] != null) {
                    //System.out.print("  - curTactData: " + curTactData[x].name);
                }
                if (prevTactData[x] != null) {
                    //System.out.print("  - prevTactData: " + prevTactData[x].name);
                }
                //System.out.print("\r\n");
            }

            // Add ships to the top most slot in the tactical.
            for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
                object = (TrekObject) e.nextElement();

                if (object == null) continue;

                // Get the index of the object if it exists in the array.
                indexOfObject = tacticalIndexOf(object);

                // If it doesn't exist in the array, add it to the top most slot.
                if (indexOfObject == -1) {
                    tacticalAddShip(object);
                }
            }

            for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
                object = (TrekObject) e.nextElement();

                if (object == null) continue;

                indexOfObject = tacticalIndexOf(object);

                if (indexOfObject == -1) {
                    tacticalAddObject(object);
                }
            }

            // Now draw the tactical 'lines'
            for (int x = 0; x < 20; x++) {
                // update any of the status values (object active, ship scanning/locked, etc.)
                setStatus(curTactData[x]);

                // if that line isn't null, check for differences from the previous tick, and send that data
                if (curTactData[x] != null) {
                    outTact.append(curTactData[x].returnDiff(prevTactData[x]));
                    //                   System.out.println("" + tick + " dist, cur: " + curTactData[x].distance + ", prev: " + prevTactData[x].distance);
                } else if (curTactData[x] == null && prevTactData[x] != null) {
                    //System.out.println("TACTCHECK: cleared(2): " + x);
                    //outTact.append(prevTactData[x].clear());
                    //prevTactData[x] = null;                                      
                }


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

            if (outTact.length() > 0)
                return "<tacupdate>" + outTact.toString() + "</tacupdate>";
        }

        return outTact.toString();
    }

    protected int tacticalIndexOf(TrekObject thisObject) {
        for (int x = 0; x < 20; x++) {
            if (tacticalDisplay[x] != null && tacticalDisplay[x] == thisObject) {
                return x;
            }
        }

        return -1;
    }

    protected void tacticalAddShip(TrekObject thisObject) {
        for (int x = 0; x < 20; x++) {
            if (tacticalDisplay[x] == null) {
                tacticalDisplay[x] = thisObject;
                curTactData[x] = new TrekHudDataTactical(player.ship, thisObject);
                return;
            }
        }
    }

    protected void tacticalAddObject(TrekObject thisObject) {
        for (int x = 19; x > -1; x--) {
            if (tacticalDisplay[x] == null) {
                tacticalDisplay[x] = thisObject;
                curTactData[x] = new TrekHudDataTactical(player.ship, thisObject);
                return;
            }
        }
    }

    protected void drawScannerLine(int thisLine, String thisString) {
        // nothing
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
        hudobjects = new Hashtable();
        tick = 0;
    }

    protected void sendMessage(String thisMessage, int msgTimeout) {
        player.sendMessage("<gamemsg>" + thisMessage.replaceAll("<", "&lt;") + "</gamemsg>");
        //System.out.println("**** Sending message: " + thisMessage);
    }

    public void sendMessage(String thisMessage) {
        sendMessage(thisMessage, 10);
    }

    protected void sendTopMessage(String thisMessage, int msgTimeout) {
        player.sendMessage("<gamemsg>" + thisMessage.replaceAll("<", "&lt;") + "</gamemsg>");
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
        //player.sendMessage("<comm>" + thisMessage.replaceAll("<", "&lt;") + "</comm>");
        player.sendText("<comm>" + thisMessage + "</comm>");
    }

    protected void setHudValue(String key, Object obj) {
        hudobjects.put(key, obj);
    }

    /**
     * Displays the player roster.
     */
    protected void showPlayers() {
        int playerCount = 0;
        StringBuffer shipLetters = new StringBuffer();
        StringBuffer outRoster = new StringBuffer();

        outRoster.append("<roster>");

        for (int x = 0; x < TrekServer.players.size(); x++) {
            TrekPlayer thisPlayer = (TrekPlayer) TrekServer.players.elementAt(x);

            if (thisPlayer == null)
                continue;

            if (thisPlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (thisPlayer.ship.scanLetter == null)
                continue;

            shipLetters.append(thisPlayer.ship.scanLetter);

            playerCount++;
        }

        outRoster.append("<players>" + playerCount + "</players>");

        StringBuffer sortedShipLetters = new StringBuffer();
        for (char m = 'a'; m <= 'z'; m++) {
            if (shipLetters.indexOf(new Character(m).toString()) != -1)
                sortedShipLetters.append(m);
        }
        for (char m = 'A'; m <= 'Z'; m++) {
            if (shipLetters.indexOf(new Character(m).toString()) != -1)
                sortedShipLetters.append(m);
        }
        for (char m = '0'; m <= '9'; m++) {
            if (shipLetters.indexOf(new Character(m).toString()) != -1)
                sortedShipLetters.append(m);
        }
        shipLetters = sortedShipLetters;

        for (int x = 0; x < shipLetters.length(); x++) {
            TrekShip thisShip = TrekServer.getPlayerShipByScanLetter(new Character(shipLetters.charAt(x)).toString());

            if (thisShip.isPlaying()) {
                outRoster.append("<ship>");
                outRoster.append("<name>" + thisShip.name + "</name>");
                outRoster.append("<slot>" + shipLetters.charAt(x) + "</slot>");
                outRoster.append("<goldk>" + (thisShip.gold / 1000) + "</goldk>");
                outRoster.append("<class>" + thisShip.classLetter + "</class>");
                outRoster.append("<quad>");
                if (thisShip.currentQuadrant.name.equals("Alpha Quadrant")) {
                    outRoster.append("A");
                } else if (thisShip.currentQuadrant.name.equals("Beta Quadrant")) {
                    outRoster.append("B");
                } else if (thisShip.currentQuadrant.name.equals("Gamma Quadrant")) {
                    outRoster.append("G");
                } else if (thisShip.currentQuadrant.name.equals("Omega Quadrant")) {
                    outRoster.append("O");
                } else if (thisShip.currentQuadrant.name.equals("Nu Quadrant")) {
                    outRoster.append("N");
                } else if (thisShip.currentQuadrant.name.equals("Delta Quadrant")) {
                    outRoster.append("D");
                }
                outRoster.append("</quad>");
                if (TrekServer.isTeamPlayEnabled())
                    outRoster.append("<team>" + thisShip.parent.teamNumber + "</team>");
                outRoster.append("</ship>");
            }
        }
        outRoster.append("</roster>");

        player.sendText(outRoster.toString());
    }

    /**
     * Displays the score for a given player.
     *
     * @param thisPlayer Player to display the score of.
     */
    protected void showScore(TrekPlayer thisPlayer) {
        StringBuffer outScore = new StringBuffer();

        outScore.append("<score>");
        outScore.append("<name>" + thisPlayer.shipName + "</name>");
        outScore.append("<gold>" + thisPlayer.ship.gold + "</gold>");
        outScore.append("<dmggvn>" + thisPlayer.ship.totalDamageGiven + "</dmggvn>");
        outScore.append("<bonus>" + thisPlayer.ship.totalBonus + "</bonus>");
        outScore.append("<dmgrcvd>" + thisPlayer.ship.totalDamageReceived + "</dmgrcvd>");
        outScore.append("<cfl>" + thisPlayer.ship.conflicts + "</cfl>");
        outScore.append("<breaks>" + thisPlayer.ship.breakSaves + "</breaks>");
        outScore.append("<quad>");
        if (thisPlayer.ship.currentQuadrant.name.equals("Alpha Quadrant")) {
            outScore.append("A");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Beta Quadrant")) {
            outScore.append("B");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Gamma Quadrant")) {
            outScore.append("G");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Omega Quadrant")) {
            outScore.append("O");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Nu Quadrant")) {
            outScore.append("N");
        } else if (thisPlayer.ship.currentQuadrant.name.equals("Delta Quadrant")) {
            outScore.append("D");
        }
        outScore.append("</quad>");
        outScore.append("<class>" + thisPlayer.ship.shipClass + "</class>");
        outScore.append("</score>");

        player.sendText(outScore.toString());
    }

    protected void showHelpScreen() {
        TrekDataInterface data = new TrekDataInterface();
        Vector helpLines = data.loadHelpFile(helpScreenLetter);
        StringBuffer outHelp = new StringBuffer();

        if (helpLines.size() == 0) {
            sendError("Help file does not exist.");
            return;
        }

        outHelp.append("<help id=\"" + helpScreenLetter + "\">");

        for (int x = 0; x < 13; x++) {
            try {
                outHelp.append(format((String) helpLines.elementAt(x), "\r\n", 22));
            } catch (Exception e) {
                outHelp.append("\r\n");
            }
        }

        outHelp.append("</help>");

        player.sendText(outHelp.toString());
    }

    protected void showOdometerPage() {
        StringBuffer outOdom = new StringBuffer();

        outOdom.append("<odometer>");
        outOdom.append("<name>" + player.shipName + "</name>");
        outOdom.append("<rank>" + player.getRankText(player.ship) + "</rank>");
        outOdom.append("<class>" + player.ship.shipClass + "</class>");
        outOdom.append("<launched>" + (new Date(player.ship.dateLaunched).toString()) + "</launched>");

        outOdom.append("<timeplayed>");
        int hours = this.player.ship.secondsPlayed / 3600;
        int minutes = (this.player.ship.secondsPlayed / 60) - (hours * 60);
        int seconds = this.player.ship.secondsPlayed % 60;
        // format to 2 characters
        if (hours < 10)
            outOdom.append("0");
        outOdom.append(new Integer(hours).toString());
        outOdom.append(":");
        if (minutes < 10)
            outOdom.append("0");
        outOdom.append(new Integer(minutes).toString());
        outOdom.append(":");
        if (seconds < 10)
            outOdom.append("0");
        outOdom.append(new Integer(seconds).toString());
        outOdom.append("</timeplayed>");
        outOdom.append("<torps>" + player.ship.torpsFired + "</torps>");
        outOdom.append("<mines>" + player.ship.minesDropped + "</mines>");
        outOdom.append("<drones>" + player.ship.dronesFired + "</drones>");
        outOdom.append("<units>" + (new Long(Math.round(player.ship.unitsTraveled)).toString()) + "</units>");
        outOdom.append("</odometer>");

        player.sendText(outOdom.toString());
    }

    protected void showShipStats(TrekShip targetShip) {
        if (targetShip == null || !targetShip.isPlaying()) {
            //this.targetShip = null;
            return;
        }

        StringBuffer outStats = new StringBuffer();

        outStats.append("<specs>");
        outStats.append("<name>" + targetShip.name + "</name>");
        outStats.append("<fullclass>" + targetShip.fullClassName + "</fullclass>");
        outStats.append("<turnwarp>" + targetShip.maxTurnWarp + "</turnwarp>");
        outStats.append("<maxwarp>" + targetShip.maxCruiseWarp + "</maxwarp>");
        outStats.append("<emergwarp>" + targetShip.damageWarp + "</emergwarp>");
        outStats.append("<phbanks>" + targetShip.maxPhaserBanks + "</phbanks>");

        outStats.append("<phtype>");
        switch (targetShip.phaserType) {
            case TrekShip.PHASER_AGONIZER:
                outStats.append("AG");
                break;
            case TrekShip.PHASER_EXPANDINGSPHEREINDUCER:
                outStats.append("ESI");
                break;
            case TrekShip.PHASER_NORMAL:
                outStats.append("PH");
                break;
            case TrekShip.PHASER_TELEPORTER:
                outStats.append("TL");
                break;
            case TrekShip.PHASER_DISRUPTOR:
                outStats.append("DI");
                break;
            default:
                outStats.append("PH");
                break;
        }
        outStats.append("</phtype>");

        outStats.append("<phrange>");
        if (targetShip.homePlanet.equals("Cardassia"))
            outStats.append("1 - " + targetShip.minPhaserRange);
        else
            outStats.append("0 - " + targetShip.minPhaserRange);
        outStats.append("</phrange>");

        outStats.append("<torptubes>" + targetShip.maxTorpedoBanks + "</torptubes>");

        outStats.append("<torptype>");
        switch (targetShip.torpedoType) {
            case TrekShip.TORPEDO_BOLTPLASMA:
                outStats.append("BP");
                break;
            case TrekShip.TORPEDO_NORMAL:
                outStats.append("TP");
                break;
            case TrekShip.TORPEDO_OBLITERATOR:
                outStats.append("OB");
                break;
            case TrekShip.TORPEDO_PHOTON:
                outStats.append("PH");
                break;
            case TrekShip.TORPEDO_PLASMA:
                outStats.append("PL");
                break;
            case TrekShip.TORPEDO_VARIABLESPEEDPLASMA:
                outStats.append("VP");
                break;
            default:
                outStats.append("PH");
                break;
        }
        outStats.append("</torptype>");

        outStats.append("<torprange>" + targetShip.minTorpedoRange + " - " + targetShip.maxTorpedoRange + "</torprange>");
        outStats.append("<torpqty>" + targetShip.maxTorpedoStorage + "</torpqty>");
        outStats.append("<mineqty>" + targetShip.maxMineStorage + "</mineqty>");
        outStats.append("<droneqty>" + targetShip.maxDroneStorage + "</droneqty>");
        outStats.append("<scanrange>" + (new Double(targetShip.scanRange)).intValue() + "</scanrange>");
        outStats.append("<cloak>" + targetShip.cloak + "</cloak>");
        outStats.append("<home>" + targetShip.homePlanet + "</home>");

        outStats.append("</specs>");

        player.sendText(outStats.toString());
    }

    protected void showInventoryScreen() {
        StringBuffer outInv = new StringBuffer();

        outInv.append("<inv>");

        if (player.ship.buoyTimeout <= 0) {
            outInv.append("<device>");
            outInv.append("<name>buoy</name>");
            outInv.append("<fire>b</fire>");
            outInv.append("</device>");
        }

        if (player.ship.corbomite) {
            outInv.append("<device>");
            outInv.append("<name>corbomite device</name>");
            outInv.append("<fire>c</fire>");
            outInv.append("</device>");
        }

        if (player.ship.iridium) {
            outInv.append("<device>");
            outInv.append("<name>iridium mine</name>");
            outInv.append("<fire>i</fire>");
            outInv.append("</device>");
        }

        if (player.ship.lithium) {
            outInv.append("<device>");
            outInv.append("<name>lithium mine</name>");
            outInv.append("<fire>l</fire>");
            outInv.append("</device>");
        }

        if (player.ship.magnabuoy) {
            outInv.append("<device>");
            outInv.append("<name>magnabuoy</name>");
            outInv.append("<fire>m</fire>");
            outInv.append("</device>");
        }

        if (player.ship.neutron) {
            outInv.append("<device>");
            outInv.append("<name>neutron mine</name>");
            outInv.append("<fire>n</fire>");
            outInv.append("</device>");
        }

        if (player.ship.seeker) {
            outInv.append("<device>");
            outInv.append("<name>seeker probe</name>");
            outInv.append("<fire>s</fire>");
            outInv.append("</device>");
        }

        outInv.append("</inv>");

        player.sendText(outInv.toString());
    }

    protected synchronized void updateHud() {
        // If the players ship is dead.
        if (player.ship == null)
            return;

        if (disabled) return;

        try {
            tick++;

            // if the user requested the roster, send it
            if (showPlayerPage > 0) {
                showPlayers();
                showPlayerPage = 0;
            }

            // if the user requested help, send it
            if (showHelpScreen) {
                showHelpScreen();
                showHelpScreen = false;
            }

            StringBuffer outHud = new StringBuffer();

            // add ship stats to the update
            if (curShipStats == null) {  // 1st update
                curShipStats = new TrekHudDataShip(player.ship);
                updateStatus(curShipStats);
                outHud.append(curShipStats.writeInitial());
            } else {
                TrekHudDataShip updatedStats = new TrekHudDataShip(player.ship);
                updateStatus(updatedStats);
                // preserve zone indicator settings
                //updatedStats.ast = curShipStats.ast;

                //updatedStats.neb = curShipStats.neb;
                //System.out.println("neb = " + curShipStats.neb);
                //updatedStats.pul = curShipStats.pul;
                //updatedStats.qua = curShipStats.qua;

                outHud.append(updatedStats.returnDiff(curShipStats));

                // update the data for next tick
                curShipStats = updatedStats;
            }

            // add updated scanner data
            outHud.append(doUpdateScanner());

            // add updated tactical data
            outHud.append(doUpdateTactical());

            if (outHud.length() > 0) {
                player.sendText("<hudupdate>" + outHud.toString() + "</hudupdate>");
            }

            if (tick == 64)
                tick = 0;

        } catch (NullPointerException npe) {
            // in case we killed the socket
        }
    }

    protected void showOptionScreen() {
        // TODO: options, may want to defer them for now
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

    protected String doUpdateScanner() {

        if (disabled) return "";

        if (player.ship.scanTarget == null) {
            return "";
        }

        if (!TrekMath.canScanShip(player.ship, player.ship.scanTarget)) {
            clearScanner();
        }

        // correct NPE when currently scanned object has been removed from quadrant since last tick
        // ie, shot/expired drone/mine/buoy/obs device, or a player quitting, saving, etc.
        if (!player.ship.currentQuadrant.isInQuadrant(player.ship.scanTarget)) {
            player.ship.scanTarget = null;
            return "";
        }

        if (curScanStats == null) {
            curScanStats = new TrekHudDataScanner(player, player.ship.scanTarget);
            return curScanStats.writeInitial();
        } else {
            TrekHudDataScanner prevScan = curScanStats;
            curScanStats = new TrekHudDataScanner(player, player.ship.scanTarget);
            return curScanStats.returnDiff(prevScan);
        }
    }

    protected void clearMessages() {
        // nothing
    }

    protected void setLoadMessage(String thisMessage) {
        loadingMessage = thisMessage;

        //System.out.println("Debug load message: thisMessage = " + thisMessage);
        if (curShipStats == null) return;

        if (thisMessage.equals("")) {
            curShipStats.loadType = "";
            curShipStats.load1 = -1;
            curShipStats.load2 = -1;
        } else if (thisMessage.indexOf("Torp") != -1) {
            curShipStats.loadType = "Torpedoes";
            curShipStats.load1 = player.ship.torpedoCount;
            curShipStats.load2 = player.ship.maxTorpedoStorage;
        } else if (thisMessage.indexOf("Crys") != -1) {
            curShipStats.loadType = "Crystals";
            curShipStats.load1 = player.ship.crystalsReceived;
            curShipStats.load2 = player.ship.getCrystalCount();
        } else if (thisMessage.indexOf("Dron") != -1) {
            curShipStats.loadType = "Drones";
            curShipStats.load1 = (player.ship.maxDroneStorage - player.ship.droneCount);
            curShipStats.load2 = -1;
        } else if (thisMessage.indexOf("Mine") != -1) {
            curShipStats.loadType = "Mines";
            curShipStats.load1 = (player.ship.maxMineStorage - player.ship.mineCount);
        }
        //System.out.println("Debug load message: loadType = " + curShipStats.loadType + "  - load1 = " + curShipStats.load1 + "  - load2 = " + curShipStats.load2);
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
        //if (!(curShipStats == null))
        //    curShipStats.ast = b;
    }

    protected void nebulaIndicator(boolean b) {
        //if (!(curShipStats == null))
        //    curShipStats.neb = b;
    }

    protected void pulsarIndicator(boolean b) {
        //if (!(curShipStats == null))
        //    curShipStats.pul = b;
    }

    protected void quasarIndicator(boolean b) {
        //if (!(curShipStats == null))
        //    curShipStats.qua = b;
    }

    protected void updateStatus(TrekHudDataShip thds) {
        setDockOrbitStatus(thds);
        setWarpMsg(thds);
        setIntData(thds);
        setWpnLock(thds);
        setZoneIndicators(thds);
    }

    protected void setDockOrbitStatus(TrekHudDataShip thds) {
        if (player.ship.docked || player.ship.orbiting) {
            if (player.ship.docked)
                thds.doStat = "Docked";
            else
                thds.doStat = "In Orbit";
        } else if (player.ship.dockable || player.ship.orbitable) {
            if (player.ship.dockable)
                thds.doStat = "Dockable";
            else
                thds.doStat = "Orbitable";
        } else {
            thds.doStat = "";
        }
    }

    protected void setWarpMsg(TrekHudDataShip thds) {
        if (Math.abs(player.ship.warpSpeed) <= player.ship.maxCruiseWarp) {
            thds.warpMsg = "";
        }

        if (Math.abs(player.ship.warpSpeed) > player.ship.maxTurnWarp) {
            thds.warpMsg = "Exceeding Turn Warp";
        }

        if (Math.abs(player.ship.warpSpeed) > player.ship.maxCruiseWarp) {
            thds.warpMsg = "Exceeding Cruise Warp";
        }
    }

    protected void setIntData(TrekHudDataShip thds) {
        if (player.ship.isIntercepting()) {
            if (player.ship.interceptTarget != null)
                thds.intData = player.ship.interceptTarget.name;
            else if (player.ship.intCoordPoint != null)
                thds.intData = player.ship.intCoordPoint.toString();
        } else {
            thds.intData = "";
        }
    }

    protected void setWpnLock(TrekHudDataShip thds) {
        if (player.ship.lockTarget != null) {
            thds.lockData = player.ship.lockTarget.scanLetter;
        } else {
            thds.lockData = "";
        }
    }

    protected void setZoneIndicators(TrekHudDataShip thds) {
        if (player.ship.asteroidTarget != null) {
            thds.ast = true;
        }

        if (player.ship.nebulaTarget != null) {
            thds.neb = true;
        }

        if (player.ship.pulsarTarget != null) {
            thds.pul = true;
        }

        if (player.ship.quasarTarget != null) {
            thds.qua = true;
        }
    }

    protected void updateScanner() {
        player.sendText(doUpdateScanner());
    }
}