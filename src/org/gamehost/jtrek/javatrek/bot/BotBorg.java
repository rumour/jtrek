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
package org.gamehost.jtrek.javatrek.bot;


import org.gamehost.jtrek.javatrek.*;

import java.util.*;

/**
 * The borg bot unleashes iridium justice on spammers and base-sitters.
 *
 * @author Jay Ashworth
 */
public class BotBorg extends TrekBot {
    Random gen = new Random();
    boolean initialCommand = false;
    int changeLocTimeout = 0;
    int dropMineTimeout = 0;
    int torpFireTimeout = 0;
    int specialTorpFireTimeout = 0;
    int lifeSupportTimeout = 0;
    int subSpaceRiftCountdown = -1;
    int redrawCountdown = 1800;
    boolean subSpaceRiftEngaged = false;
    Hashtable warnedShips = new Hashtable();
    boolean healMode = false;
    int warpLevel;
    int[] playerCounters = new int[1024]; // only handles up to 1024 player IDs with this

    /**
     * @param serverIn
     * @param shipNameIn
     */
    public BotBorg(TrekServer serverIn, String shipNameIn) {
        super(serverIn, shipNameIn);
        this.shipName = "borg";
        try {
            out = new java.io.FileOutputStream("./data/" + shipName + ".log");
        } catch (java.io.FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

        warpLevel = Math.abs(gen.nextInt() % 61) + 90; // warp when reaching anywhere from 90 to 150
    }

    /* (non-Javadoc)
      * @see org.gamehost.jtrek.javatrek.TrekBot#loadInitialBot()
      */
    public void loadInitialBot() {
        ship = TrekUtilities.getShip("d", this);
        ship.setInitialDirection();
        ship.maxWarpEnergy = 150;
        ship.warpEnergy = 150;
        ship.damagePercent = 25;
        ship.shieldStrength = 35;

        ship.currentQuadrant.addShip(ship);

        this.drawHud(true);
    }

    /* (non-Javadoc)
      * @see org.gamehost.jtrek.javatrek.TrekBot#botSecondUpdate()
      */
    public void botSecondUpdate() {

        try {
            changeLocTimeout--;
            dropMineTimeout--;
            torpFireTimeout--;
            specialTorpFireTimeout--;
            lifeSupportTimeout--;
            redrawCountdown--;
            reducePlayerCounters();

            if (redrawCountdown <= 0) {
                redrawCountdown = 1800;
                runMacro("\022");
            }

            subSpaceRiftCountdown--;
            zap();
            manageWarnings();

            if (!initialCommand) {
                ship.interceptTarget = ship.currentQuadrant.getObjectByScanLetter("3");
                ship.scanTarget = ship.interceptTarget;
                runMacro("]");
                initialCommand = true;
            }

            if (ship == null)
                return;

            if (!ship.raisingShields)
                runMacro("s");

            if (healMode) {
                runHealRoutines();
                return;
            }

            if (!ship.isIntercepting()) {
                double preserveSpeed = ship.warpSpeed;
                ship.warpSpeed = 0;
                runMacro("i");
                ship.warpSpeed = preserveSpeed;
            }

            ship.gold = (ship.totalDamageGiven + ship.totalBonus) / 10;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
      * @see org.gamehost.jtrek.javatrek.TrekBot#botTickUpdate()
      */
    public void botTickUpdate() {
        incrementPlayerCounters();

        shootEnemies();

        applyExposureEffects();

        checkDamage();

        if (healMode) return;

        interceptMsg();

        setSpeed();

        if (ship.scanTarget != null) {
            double curDistance = TrekMath.getDistance(ship, ship.scanTarget);
            if (curDistance < 100) {
                changeLocTimeout = 0;
            }
        }

        return;
    }

    public void zap() {
        boolean plasmaWarning = false;
        TrekObject intHolder;
        intHolder = ship.scanTarget;
        TrekObject nearestObj = null;
        double nearest = 500;

        Vector objs = ship.currentQuadrant.getAllObjectsInRange(ship, ship.minPhaserRange);
        for (Enumeration e = objs.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();
            if (TrekUtilities.isObjectBuoy(curObj) ||
                    TrekUtilities.isObjectDrone(curObj) ||
                    TrekUtilities.isObjectMine(curObj) ||
                    TrekUtilities.isObjectMagnabuoy(curObj) ||
                    TrekUtilities.isObjectTorpedo(curObj)) {

                double curObjDist = TrekMath.getDistance(ship, curObj);

                if (!plasmaWarning && curObjDist < 300 && TrekUtilities.isObjectTorpedo(curObj)) {
                    TrekTorpedo theTorp = (TrekTorpedo) curObj;
                    if (theTorp.torpType == TrekShip.TORPEDO_PLASMA || theTorp.torpType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                        plasmaWarning = true;
                    }
                }

                if (!plasmaWarning && curObjDist < 150 && TrekUtilities.isObjectTorpedo(curObj)) {
                    TrekTorpedo theTorp = (TrekTorpedo) curObj;
                    if (theTorp.torpType == TrekShip.TORPEDO_WARHEAD && !(theTorp.owner == ship)) {
                        plasmaWarning = true;
                    }
                }

                // if this object is the closest one encountered so far
                if (curObjDist < nearest) {
                    nearest = curObjDist;

                    // of these objects, borg will likely only fire torps (warheads), so filter those out
                    if (TrekUtilities.isObjectTorpedo(curObj)) {
                        TrekTorpedo torpy = (TrekTorpedo) curObj;
                        if (!(torpy.owner == ship)) {

                            nearestObj = torpy;
                        }
                    } else {
                        nearestObj = curObj;
                    }

                }
            }
        }

        // shoot the nearest object
        if (nearestObj != null) {
            ship.phaserType = TrekShip.PHASER_NORMAL;
            ship.phaserFireType = TrekShip.PHASER_FIRENARROW;

            ship.lockTarget = nearestObj;
            if (ship.lockTarget != null) {
                if (plasmaWarning) {
                    ship.phaserType = TrekShip.PHASER_TELEPORTER;
                    runMacro("CCWWWWWPPPPp\020\020wwwwwcc");
                    //System.out.println("Borg:  teleporting from plasma");
                    ship.phaserFireTimeout = 0;
                } else {
                    if (TrekUtilities.isObjectTorpedo(nearestObj)) {
                        TrekTorpedo theTorp = (TrekTorpedo) nearestObj;
                        if (theTorp.torpType != TrekShip.TORPEDO_WARHEAD)
                            runMacro("CPp\020c");
                    } else {
                        runMacro("CPp\020c");
                    }
                }
            }

        }

        ship.scanTarget = intHolder;
    }

    public void shootEnemies() {
        if (ship.organiaModifier) return;

        TrekShip phaserTarget = null;
        TrekShip torpTarget = null;
        double nearest = 500;
        int mostHurt = -1;

        Vector ships = ship.currentQuadrant.getAllVisibleShipsInExactRange(ship, ship.maxTorpedoRange);
        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip curShip = (TrekShip) e.nextElement();
            // make borg not fire on another assimilated vessal
            if (curShip.name.equals("borg")) continue;

            double curShipDist = TrekMath.getDistance(ship, curShip);
            int curShipDmg = curShip.damage;

            // if this ship is the closest one encountered so far
            if (curShipDist < nearest) {
                nearest = curShipDist;
                phaserTarget = curShip;
            }

            // torp the ship that's hurt most
            if (curShipDmg > mostHurt) {
                torpTarget = curShip;
            }
        }

        // shoot the nearest ship
        if (nearest < ship.minPhaserRange && phaserTarget != null) {
            ship.phaserType = TrekShip.PHASER_NORMAL;
            ship.phaserFireType = TrekShip.PHASER_FIRENARROW;

            ship.lockTarget = phaserTarget;
            if (ship.lockTarget != null) {
                //runMacro("CCCCPPPPp\020\020\020\020cccc");  //phasers are un-borg-like
            }

            // drop an iridium mine if under 200
            if (nearest < 200 && dropMineTimeout <= 0) {
                ship.currentQuadrant.addObject(new TrekIridium(this.ship));
                dropMineTimeout = 60;
            }
        }

        // shoot the most hurt
        if (torpTarget != null && torpFireTimeout <= 0 && activeBorgTorps() <= 10) {
            ship.lockTarget = torpTarget;
            TrekTorpedo torpy = new TrekTorpedo(this.ship, TrekShip.TORPEDO_WARHEAD, ship.lockTarget);
            torpy.warpSpeed = Math.abs(gen.nextInt() % 21) / 10.0 + 8.0;
            torpy.lifeTime = 60;
            torpy.scanRange = 100;
            torpy.strength = Math.abs(gen.nextInt() % 51) + 100;
            ship.currentQuadrant.addObject(torpy);
            torpFireTimeout = Math.abs(gen.nextInt() % 8) + 3;

            // check if ship has been 'notified' (msg warning)
            TrekShip targetShip = (TrekShip) ship.lockTarget;
            long connValue = targetShip.parent.dbConnectionID;
            if (connValue != 0) {
                if (warnedShips.containsKey(new Long(connValue).toString())) {
                    return;
                }
                TrekServer.sendMsgToPlayer(targetShip.scanLetter, "Subspace Radiation Must Be Neutralized", torpy, false, false);
                targetShip.lastWarning = Calendar.getInstance().getTimeInMillis();
                warnedShips.put(new Long(connValue).toString(), targetShip);
            }
        }
    }

    public void checkDamage() {
        if (ship.doTranswarp)
            return;

        // apply damage control if necessary
        if (ship.damage > 0 || ship.warpEnergy < ship.maxWarpEnergy || ship.impulseEnergy < ship.maxImpulseEnergy) {
            runMacro("ccccccccccccccccccccc");
        }

        // apply extra fix tick for bot - calling this twice will make it fix 3 times faster than a normal dy .. or maybe 12 times faster?
        fixShip();
        fixShip();

        if (ship.damage > 99 || ship.warpEnergy <= 50) {
            runMacro("\005ccccccccccccccccccccccccccccccccccccccccccccc");
            healMode = true;
        }

        if (ship.antiMatter < 1000) ship.antiMatter = 5000;

        if (ship.damage == 0 && ship.warpEnergy == ship.maxWarpEnergy && ship.impulseEnergy == ship.maxImpulseEnergy) {
            runMacro("\003");
        }

        if (ship.lifeSupport < 100 && lifeSupportTimeout <= 0) {
            ship.lifeSupport++;
            lifeSupportTimeout = 10;
        }
    }

    public void interceptMsg() {
        if (ship.msgPoint == null && changeLocTimeout <= 0) {
            int placeToLurk = Math.abs(gen.nextInt() % 10);
            String lurkScanLtr = "";
            if (ship.currentQuadrant.name.equals("Delta Quadrant")) {
                String scanObjs[] = {"a", "b", "c", "f", "m", "m", "n", "p", "s", "t"};
                lurkScanLtr = scanObjs[placeToLurk];
            } else {
                switch (placeToLurk) {
                    case 0:
                        lurkScanLtr = "v";
                        break;
                    case 1:
                        lurkScanLtr = "3";
                        break;
                    case 2:
                        lurkScanLtr = "4";
                        break;
                    case 3:
                        lurkScanLtr = "d";
                        break;
                    case 4:
                        lurkScanLtr = "1";
                        break;
                    case 5:
                        lurkScanLtr = "2";
                        break;
                    case 6:
                        lurkScanLtr = "q";
                        break;
                    case 7:
                        lurkScanLtr = "p";
                        break;
                    case 8:
                        lurkScanLtr = "5";
                        break;
                    case 9:
                        lurkScanLtr = "7";
                        break;
                }
            }
            runMacro("\005");
//            ship.warpSpeed = 0;
            ship.interceptTarget = ship.currentQuadrant.getObjectByScanLetter(lurkScanLtr);
            ship.scanTarget = ship.interceptTarget;
            runMacro("}ii");
            changeLocTimeout = 300;

            System.out.println("Borg:  new destination is: " + lurkScanLtr);
        } else {
            boolean radiationPresent = false;
            int radiationEmitters = getNonBorgShipsInRange(3000);
            if (radiationEmitters > 0) radiationPresent = true;

            if (radiationPresent) {
                Vector radiationInArea = ship.currentQuadrant.getAllVisibleShipsInExactRange(this.ship, 3000);
                double nearest = 3000;
                for (Enumeration e = radiationInArea.elements(); e.hasMoreElements(); ) {
                    TrekShip curShip = (TrekShip) e.nextElement();

                    if (curShip.name.equals("borg")) continue;

                    double curDistance = TrekMath.getDistance(this.ship, curShip);
                    if (curDistance < nearest) {
                        nearest = curDistance;
                        ship.interceptTarget = curShip;
                    }
                }
            } else {
                ship.scanTarget = null;
                if (ship.msgPoint == null) {
                    //changeLocTimeout = 0;
                } else {
                    double preserveSpeed = ship.warpSpeed;
                    ship.warpSpeed = 0;
                    ship.interceptMsg();
                    ship.warpSpeed = preserveSpeed;
                }
                if (changeLocTimeout <= 0) {
                    ship.msgPoint = null;
                }
            }
        }
    }

    public void fixShip() {
        if (ship.damageControl > 0) {
            double repairIE = Math.abs(gen.nextInt() % 540);
            double repairWEINT = Math.log(ship.damageControl) / 15 * 100; //percentage chance of fixing
            double testWE = Math.abs(gen.nextInt() % 100) + 1;
            double testINT = Math.abs(gen.nextInt() % 100) + 1;

            if (repairIE < ship.damageControl && ship.impulseEnergy < ship.maxImpulseEnergy) {
                ship.impulseEnergy++;
            }

            if (testWE < repairWEINT && ship.warpEnergy < ship.maxWarpEnergy) {
                ship.warpEnergy++;
            }

            if (testINT < repairWEINT && ship.damage > 0) {
                ship.damage--;
            }

            if (ship.damage <= 100) {
                ship.lifeSupportFailing = false;
            }
        }
    }

    public int activeBorgTorps() {
        int returnValue = 0;

        Vector objs = ship.currentQuadrant.getAllObjectsInRange(this.ship, 20000);
        for (Enumeration e = objs.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();
            if (curObj.type == TrekObject.OBJ_TORPEDO) {
                TrekTorpedo torpy = (TrekTorpedo) curObj;
                if (torpy.torpType == TrekShip.TORPEDO_WARHEAD && torpy.owner == this.ship)
                    returnValue++;
            }
        }
        return returnValue;
    }

    public void manageWarnings() {
        long warningExpiration = 1000 * 60;  // 60 seconds

        if (warnedShips.size() == 0)
            return;

        for (Enumeration e = warnedShips.elements(); e.hasMoreElements(); ) {
            TrekShip currentEntry = (TrekShip) e.nextElement();

            if (Calendar.getInstance().getTimeInMillis() > currentEntry.lastWarning + warningExpiration) {
                warnedShips.remove(new Long(currentEntry.parent.dbConnectionID).toString());
            }
        }
    }

    public void runHealRoutines() {
        runMacro("\005cccccccccccccccccccccccccccccccccccccccccccc");

        if (ship.damage < 70 && ship.warpEnergy > 60 && ship.impulseEnergy > 50 && ship.lifeSupport > 15) {
            healMode = false;
            runMacro("\005");
            if (subSpaceRiftEngaged) {
                if (!ship.currentQuadrant.name.equals("Alpha Quadrant")) {
                    // return to alpha quad
                    Vector ships = ship.currentQuadrant.getAllShipsInRange(ship, 2000);
                    TrekIridium messager = new TrekIridium(this.ship);
                    ship.currentQuadrant.addObject(messager);
                    for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
                        TrekShip shipInRange = (TrekShip) e.nextElement();
                        TrekServer.sendMsgToPlayer(shipInRange.scanLetter, "Reversing Dimensional Rift", messager, false, false);
                    }

                    // take ships within 500 with it
                    ships = ship.currentQuadrant.getAllShipsInRange(ship, 500);
                    for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
                        TrekShip shipInRange = (TrekShip) e.nextElement();
                        shipInRange.currentQuadrant.removeShipByScanLetter(shipInRange.scanLetter);
//						shipInRange.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get("Alpha Quadrant");
                        shipInRange.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get("Delta Quadrant");
                        shipInRange.currentQuadrant.addShip(shipInRange);
//						shipInRange.parent.hud.sendMessage("Borg subspace rift pulling you to Alpha Quadrant...");
                        shipInRange.parent.hud.sendMessage("Borg subspace rift pulling you to Delta Quadrant...");
                    }

                    // and send it back
                    ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
//					ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get("Alpha Quadrant");
                    ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get("Delta Quadrant");
                    ship.currentQuadrant.addShip(ship);
                }

                subSpaceRiftEngaged = false;
            }
        }

        if ((subSpaceRiftCountdown < 0) && (!subSpaceRiftEngaged) &&
                ((ship.damage > warpLevel) ||
                        (ship.damage > 115 && ship.lifeSupport < 30) ||
                        (ship.damage > 75 && ship.lifeSupport < 10))) {

            Vector ships = ship.currentQuadrant.getAllShipsInRange(ship, 2000);
            TrekIridium messager = new TrekIridium(this.ship);
            ship.currentQuadrant.addObject(messager);
            for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
                TrekShip shipInRange = (TrekShip) e.nextElement();
                TrekServer.sendMsgToPlayer(shipInRange.scanLetter, "Opening Subspace Rift in 3 Seconds", messager, false, false);
            }

            subSpaceRiftCountdown = 3;
        }

        if (subSpaceRiftCountdown == 0) {
            int newQuadInt = Math.abs(gen.nextInt() % 3);
            String newQuad = "Alpha Quadrant";
            switch (newQuadInt) {
                case 0:
                    newQuad = "Beta Quadrant";
                    break;
                case 1:
                    newQuad = "Gamma Quadrant";
                    break;
                case 2:
                    newQuad = "Omega Quadrant";
                    break;
            }

            // take ships within 500 with it
            Vector ships = ship.currentQuadrant.getAllShipsInRange(ship, 500);
            for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
                TrekShip shipInRange = (TrekShip) e.nextElement();
                shipInRange.currentQuadrant.removeShipByScanLetter(shipInRange.scanLetter);
                shipInRange.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(newQuad);
                shipInRange.currentQuadrant.addShip(shipInRange);
                shipInRange.parent.hud.sendMessage("Borg subspace rift pulling you to " + newQuad + "...");
            }
            // and move itself too
            ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
            ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(newQuad);
            ship.currentQuadrant.addShip(ship);

            // reset warp level to a new value
            warpLevel = Math.abs(gen.nextInt() % 41) + 90;

            subSpaceRiftEngaged = true;
        }
    }

    public void setSpeed() {
        // going > warp 8 with asteroids, debris, or iridium mine leads to damaging self... this would be illogical.
        boolean slowZone = false;

        if (ship.asteroidTarget != null) slowZone = true;

        Vector objs = ship.currentQuadrant.getAllObjectsInRange(ship, 1000);
        for (Enumeration e = objs.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();
            if (TrekUtilities.isObjectIridium(curObj)) slowZone = true;
            if (TrekUtilities.isObjectShipDebris(curObj)) slowZone = true;
        }

        if (slowZone) {
            ship.warpSpeed = 8;
        } else {
            ship.warpSpeed = (double) Math.abs(gen.nextInt() % 31) / 10 + 8.0;
        }
    }

    public int getNonBorgShipsInRange(int theRange) {
        int returnValue = 0;
        Vector ships = ship.currentQuadrant.getAllVisibleShipsInExactRange(this.ship, theRange);
        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip curShip = (TrekShip) e.nextElement();
            if (!curShip.name.equals("borg")) returnValue++;
        }
        return returnValue;
    }

    public void applyExposureEffects() {
        // ships within borg sphere of influence
        Vector ships = ship.currentQuadrant.getAllShipsInRange(this.ship, 3000);

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip curShip = (TrekShip) e.nextElement();
            if (curShip instanceof ShipQ) continue;
            if (curShip.name.equals("borg")) continue;

            double curDistance = TrekMath.getDistance(this.ship, curShip);

            // drain antimatter
            if (curDistance < curShip.maxTorpedoRange || (curDistance <= 2500 && (curShip instanceof ShipCV97 || curShip instanceof ShipScout || curShip instanceof ShipRBOP || curShip instanceof ShipWarbird))) {
                if (curShip.antiMatter > 0) curShip.antiMatter--;
            }

            curShip.borgExposureTime++;

            if (specialTorpFireTimeout <= 0 && activeBorgTorps() <= 15 && !ship.organiaModifier) {
                // if a ship has been exposed for 10 minutes and is within 3k
                // or ... if a player has been exposed for 15 minutes and within 6k (!player 313)
                // or ... if a ship has either > 20k gold or 200k dmggvn or 10k bonus and within 2500
                if ((curShip.borgExposureTime > 1800 && curDistance < 3000) ||
                        (playerCounters[curShip.parent.dbPlayerID] > 2700 && curDistance < 5000) |
                                (curDistance < 2500 && (curShip.gold > 20000 || curShip.damageGiven > 200000 || curShip.bonus > 10000))
                        ) {
                    TrekTorpedo torpy = new TrekTorpedo(this.ship, TrekShip.TORPEDO_WARHEAD, curShip);
                    torpy.warpSpeed = Math.abs(gen.nextInt() % 21) / 10.0 + 14.0;
                    torpy.lifeTime = 120;
                    torpy.scanRange = 250;
                    torpy.strength = Math.abs(gen.nextInt() % 51) + 250;
                    ship.currentQuadrant.addObject(torpy);
                    specialTorpFireTimeout = Math.abs(gen.nextInt() % 8) + 3;

                    long connValue = curShip.parent.dbConnectionID;
                    if (connValue != 0) {
                        if (warnedShips.containsKey(new Long(connValue).toString())) {
                            return;
                        }
                        TrekServer.sendMsgToPlayer(curShip.scanLetter, "Resistance is futile.  You will be assimilated.", torpy, false, false);
                        curShip.lastWarning = Calendar.getInstance().getTimeInMillis();
                        warnedShips.put(new Long(connValue).toString(), curShip);
                    }
                }
                // if a ship has been exposed for 5 minutes, and is within their torp range
                // or a player has been exposted for 10 minutes and is within 3k
                // or a ship has either > 10k gold, 100k dmggvn, or 7500 bonus and within 2k
                else if ((curShip.borgExposureTime > 900 &&
                        (curDistance < curShip.maxTorpedoRange ||
                                (curDistance <= 2500 &&
                                        (curShip instanceof ShipCV97 ||
                                                curShip instanceof ShipScout ||
                                                curShip instanceof ShipRBOP ||
                                                curShip instanceof ShipWarbird)
                                )
                        )
                ) || (playerCounters[curShip.parent.dbPlayerID] > 1800 && curDistance < 3000)
                        || (curDistance < 2000 && (curShip.gold > 10000 || curShip.damageGiven > 100000 || curShip.bonus > 7500))
                        ) {
                    TrekTorpedo torpy = new TrekTorpedo(this.ship, TrekShip.TORPEDO_WARHEAD, curShip);
                    torpy.warpSpeed = Math.abs(gen.nextInt() % 21) / 10.0 + 11.0;
                    torpy.lifeTime = 90;
                    torpy.scanRange = 150;
                    torpy.strength = Math.abs(gen.nextInt() % 51) + 150;
                    ship.currentQuadrant.addObject(torpy);
                    specialTorpFireTimeout = Math.abs(gen.nextInt() % 8) + 3;

                    long connValue = curShip.parent.dbConnectionID;
                    if (connValue != 0) {
                        if (warnedShips.containsKey(new Long(connValue).toString())) {
                            return;
                        }
                        TrekServer.sendMsgToPlayer(curShip.scanLetter, "Strength is irrelevant.  Your culture will adapt to service ours.", torpy, false, false);
                        curShip.lastWarning = Calendar.getInstance().getTimeInMillis();
                        warnedShips.put(new Long(connValue).toString(), curShip);
                    }
                }
            }
        }
    }

    public void incrementPlayerCounters() {
        // keep track of which players keep attacking and adjust difficulty accordingly
        Vector nearbyShips = ship.currentQuadrant.getAllShipsInRange(ship, (int) ship.scanRange);
        for (Iterator iterator = nearbyShips.iterator(); iterator.hasNext(); ) {
            TrekShip curShip = (TrekShip) iterator.next();
            if (playerCounters[curShip.parent.dbPlayerID] > 0 || TrekMath.getDistance(ship, curShip) < 3000) {
                playerCounters[curShip.parent.dbPlayerID]++;
            }
            ;
        }
    }

    public void reducePlayerCounters() {
        for (int i = 0; i < 1024; i++) {
            if (playerCounters[i] > 0) {
                playerCounters[i]--;
            }
        }
    }


}
