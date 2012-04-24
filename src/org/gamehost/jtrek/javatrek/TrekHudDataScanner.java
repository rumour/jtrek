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

/**
 * This class stores all the data represented on the right side of a ship's HUD (scanned ships and objects only,
 * ship specs, roster, etc. are handled differently, since they don't alter the ship's scanTarget); compares
 * differences against a previous tick's scan data, and returns only the differences
 */
public class TrekHudDataScanner {
    TrekObject theObj;
    protected int x, y, z = -1;
    protected int tgtX, tgtY, tgtZ = -1;
    protected String objName = "";
    protected String tgtName = "";
    protected String shipClass = "";
    protected int dist = -1;
    protected int we = -1; // warp energy
    protected int ie = -1; // impluse energy
    protected int pu = -1; // power unused
    protected double warp = 0; // warp speed
    protected int shields = -1; // shields
    protected int ph = -1; // phasers
    protected int dmg = -1; // damage
    protected int tp = -1; // torps
    protected int dmgctl = -1; // damage control
    protected int ls = -1; // life support
    protected int anti = -1; // anti-matter
    protected int drones = -1; // drones
    protected int mines = -1; // mines
    protected int cloak = -1; // cloak
    protected int heading = -1; // heading
    protected int pitch = -99; // pitch
    protected int gold = -1;
    protected String codes = "";
    protected String creator = "";
    protected int energy = -1;
    protected String locked = "";
    protected String remains = "";
    protected int payout = -1;


    public TrekHudDataScanner(TrekPlayer player, TrekObject scanObj) {
        theObj = scanObj;

        x = scanObj.point.getXInt();
        y = scanObj.point.getYInt();
        z = scanObj.point.getZInt();
        objName = scanObj.name;

        double actualScanRange = player.ship.scanRange;
        boolean canSeeFull = false;

        if (player.ship.nebulaTarget != null)
            actualScanRange = 1000;

        if (TrekMath.getDistance(player.ship, scanObj) < (actualScanRange * .90))
            canSeeFull = true;

        switch (scanObj.type) {
            case TrekObject.OBJ_SHIP:
                TrekShip theShip = (TrekShip) scanObj;
                shipClass = theShip.shipClass;
                we = theShip.warpEnergy + theShip.currentCrystalCount;
                ;
                ie = theShip.impulseEnergy;
                pu = theShip.getAvailablePower();
                warp = theShip.warpSpeed;
                if (canSeeFull)
                    shields = theShip.shields;
                else
                    shields = -1;
                ph = theShip.phasers;
                dmg = theShip.damage;
                tp = theShip.torpedoes;
                dmgctl = theShip.damageControl;
                if (canSeeFull)
                    ls = theShip.lifeSupport;
                else
                    ls = -1;
                anti = theShip.antiMatter;

                heading = new Integer(theShip.getHeading()).intValue();
                pitch = (int) theShip.vector.getPitch();
                //new Integer(theShip.getPitch()).intValue();
                gold = theShip.gold;
                break;

            case TrekObject.OBJ_OBSERVERDEVICE:
                TrekObserverDevice tod = (TrekObserverDevice) scanObj;

                dist = (int) TrekMath.getDistance(player.ship, tod);
                warp = tod.warpSpeed;

                if (tod.isScanning()) {
                    TrekShip scannedShip = tod.scanTarget;

                    we = scannedShip.warpEnergy + scannedShip.currentCrystalCount;
                    ie = scannedShip.impulseEnergy;
                    pu = scannedShip.getAvailablePower();
                    warp = scannedShip.warpSpeed;
                    shields = scannedShip.shields;
                    dmg = scannedShip.damage;
                    tp = scannedShip.torpedoCount;
                    drones = scannedShip.droneCount;
                    mines = scannedShip.mineCount;
                    dmgctl = scannedShip.damageControl;
                    ls = scannedShip.lifeSupport;
                    anti = scannedShip.antiMatter;
                    tgtX = scannedShip.point.getXInt();
                    tgtY = scannedShip.point.getYInt();
                    tgtZ = scannedShip.point.getZInt();
                    tgtName = scannedShip.name;
                    shipClass = scannedShip.shipClass;
                    if (shipClass.equals("INTERCEPTOR"))
                        shipClass = "INTRCPTR";

                    heading = new Integer(scannedShip.getHeading()).intValue();
                    pitch = (int) scannedShip.vector.getPitch();

                    // update the observed ship's coord in ship spotted database
                    if (player.ship.scannedHistory.containsKey(scannedShip.scanLetter)) {
                        player.ship.scannedHistory.remove(scannedShip.scanLetter);
                    }

                    player.ship.scannedHistory.put(scannedShip.scanLetter, new TrekCoordHistory(scannedShip.scanLetter,
                            scannedShip.name, new Trek3DPoint(scannedShip.point)));
                }
                break;

            case TrekObject.OBJ_ASTEROIDBELT:
            case TrekObject.OBJ_BLACKHOLE:
            case TrekObject.OBJ_NEBULA:
            case TrekObject.OBJ_PULSAR:
            case TrekObject.OBJ_QUASAR:
            case TrekObject.OBJ_STAR:
            case TrekObject.OBJ_WORMHOLE:
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                break;

            case TrekObject.OBJ_COMET:
                TrekComet tc = (TrekComet) scanObj;
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                warp = scanObj.warpSpeed;
                heading = new Integer(tc.getYaw()).intValue();
                pitch = (int) tc.vector.getPitch();
                //new Integer(tc.getPitch()).intValue();
                break;

            case TrekObject.OBJ_PLANET:
            case TrekObject.OBJ_STARBASE:
                dist = (int) TrekMath.getDistance(player.ship, scanObj);

                if (scanObj.type == TrekObject.OBJ_PLANET) {
                    TrekPlanet planet = (TrekPlanet) scanObj;

                    StringBuffer buffer1 = new StringBuffer();

                    if (planet.givesCrystals(player.ship)) buffer1.append("C ");
                    if (planet.repairsLifeSupport(player.ship)) buffer1.append("L ");
                    if (planet.givesMines(player.ship)) buffer1.append("M ");
                    if (planet.givesDrones(player.ship)) buffer1.append("D ");
                    if (planet.givesTorps(player.ship)) buffer1.append("T ");
                    if (planet.givesAntimatter(player.ship)) buffer1.append("A ");
                    if (planet.fixesCloak(player.ship)) buffer1.append("FC ");
                    if (planet.fixesTransmitter(player.ship)) buffer1.append("FR ");
                    if (planet.installsTranswarp(player.ship)) buffer1.append("TW ");
                    if (planet.givesCorbomite(player.ship)) buffer1.append("CM ");
                    if (planet.givesNeutron(player.ship)) buffer1.append("N ");
                    if (planet.givesIridium(player.ship)) buffer1.append("I ");
                    if (planet.givesMagnabuoy(player.ship)) buffer1.append("MB ");

                    codes = buffer1.toString();
                    if (canSeeFull) {
                        dmg = planet.damage;
                        gold = planet.gold;
                    } else {
                        dmg = -1;
                        gold = -1;
                    }

                } else {
                    TrekStarbase base = (TrekStarbase) scanObj;
                    StringBuffer buffer1 = new StringBuffer();

                    if (base.fixesLifeSupport) buffer1.append("L ");
                    if (base.givesMines) buffer1.append("M ");
                    if (base.givesDrones) buffer1.append("D ");
                    if (base.givesTorps) buffer1.append("T ");

                    codes = buffer1.toString();

                    if (canSeeFull) {
                        dmg = base.damage;
                        gold = base.gold;
                    } else {
                        dmg = -1;
                        gold = -1;
                    }

                }
                break;

            case TrekObject.OBJ_BUOY:
            case TrekObject.OBJ_IRIDIUM:
            case TrekObject.OBJ_NEUTRON:
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                if (scanObj.type == TrekObject.OBJ_BUOY) {
                    TrekBuoy buoy = (TrekBuoy) scanObj;
                    creator = buoy.owner.name;
                } else if (scanObj.type == TrekObject.OBJ_IRIDIUM) {
                    TrekIridium iridium = (TrekIridium) scanObj;
                    creator = iridium.ownerName;
                } else {
                    TrekNeutron neutron = (TrekNeutron) scanObj;
                    creator = neutron.ownerName;
                }
                break;

            case TrekObject.OBJ_TORPEDO:
                TrekTorpedo torp = (TrekTorpedo) scanObj;
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                if (torp.torpType == TrekShip.TORPEDO_WARHEAD)
                    dmg = torp.damage;
                energy = torp.strength;
                warp = torp.warpSpeed;
                creator = torp.ownerName;
                break;

            case TrekObject.OBJ_MINE:
            case TrekObject.OBJ_CORBOMITE:
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                if (scanObj.type == TrekObject.OBJ_MINE) {
                    TrekMine mine = (TrekMine) scanObj;
                    energy = mine.strength;
                    creator = mine.owner.name;
                } else {
                    TrekCorbomite corbomite = (TrekCorbomite) scanObj;
                    energy = corbomite.energy;
                    creator = corbomite.ownerName;
                }
                break;

            case TrekObject.OBJ_DRONE:
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                TrekDrone drone = (TrekDrone) scanObj;
                energy = drone.strength;
                warp = drone.warpSpeed;
                creator = drone.ownerName;
                heading = new Integer(drone.getYaw()).intValue();
                pitch = (int) drone.vector.getPitch();
                //new Integer(drone.getPitch()).intValue();
                break;

            case TrekObject.OBJ_MAGNABUOY:
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                TrekMagnabuoy magnabuoy = (TrekMagnabuoy) scanObj;
                creator = magnabuoy.ownerName;
                locked = magnabuoy.target.name;
                break;

            case TrekObject.OBJ_SHIPDEBRIS:
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                TrekShipDebris debris = (TrekShipDebris) scanObj;
                gold = debris.gold;
                remains = debris.whos;
                break;

            case TrekObject.OBJ_GOLD:
                dist = (int) TrekMath.getDistance(player.ship, scanObj);
                TrekGold goldObj = (TrekGold) scanObj;
                gold = goldObj.amount;
                creator = goldObj.ownerName;
                break;

            default:
                System.out.println("*R*A*W* ::: Unexpected object type: " + theObj.type);
        }

        if (TrekUtilities.isObjectFlag(scanObj)) {
            dist = (int) TrekMath.getDistance(player.ship, scanObj);
            TrekFlag flag = player.ship.scanTarget.currentQuadrant.getFlag();
            payout = flag.getPayoutSeconds();
        }

    }

    private String getFormattedSpeed(double warpSpeed) {
        NumberFormat numformat;
        numformat = NumberFormat.getInstance();
        numformat.setMaximumFractionDigits(1);
        numformat.setMinimumFractionDigits(1);
        String formattedWarp = numformat.format(warpSpeed);
        return formattedWarp;
    }

    protected String returnDiff(TrekHudDataScanner prevData) {
        StringBuffer returnData = new StringBuffer();

        if (theObj == prevData.theObj) {
            if (!objName.equals(prevData.objName))
                returnData.append("<name>" + objName + "</name>");
            if (theObj.type != prevData.theObj.type)
                returnData.append("<type>" + theObj.type + "</type>");
            if (!shipClass.equals(prevData.shipClass))
                returnData.append("<class>" + shipClass + "</class>");
            if (dist != prevData.dist)
                returnData.append("<dist>" + dist + "</dist>");
            if (we != prevData.we)
                returnData.append("<we>" + we + "</we>");
            if (ie != prevData.ie)
                returnData.append("<ie>" + ie + "</ie>");
            if (pu != prevData.pu)
                returnData.append("<pu>" + pu + "</pu>");
            if (warp != prevData.warp)
                returnData.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
            if (shields != prevData.shields)
                if (shields == -1)
                    returnData.append("<sh>---</sh>");
                else
                    returnData.append("<sh>" + shields + "</sh>");
            if (ph != prevData.ph)
                returnData.append("<ph>" + ph + "</ph>");
            if (dmg != prevData.dmg)
                if (dmg == -1)
                    returnData.append("<dmg>---</dmg>");
                else
                    returnData.append("<dmg>" + dmg + "</dmg>");
            if (tp != prevData.tp)
                returnData.append("<tp>" + tp + "</tp>");
            if (dmgctl != prevData.dmgctl)
                returnData.append("<dc>" + dmgctl + "</dc>");
            if (ls != prevData.ls)
                if (ls == -1)
                    returnData.append("<ls>---</ls>");
                else
                    returnData.append("<ls>" + ls + "</ls>");
            if (anti != prevData.anti)
                returnData.append("<anti>" + anti + "</anti>");
            if (drones != prevData.drones)
                if (drones == -1)
                    returnData.append("<dr>---</dr>");
                else
                    returnData.append("<dr>" + drones + "</dr>");
            if (mines != prevData.mines)
                if (mines == -1)
                    returnData.append("<mn>---</mn>");
                else
                    returnData.append("<mn>" + mines + "</mn>");
            if (cloak != prevData.cloak)
                if (cloak == -1)
                    returnData.append("<cl>---</cl>");
                else
                    returnData.append("<cl>" + cloak + "</cl>");
            if (heading != prevData.heading)
                returnData.append("<hdg>" + heading + "</hdg>");
            if (pitch != prevData.pitch)
                returnData.append("<pch>" + pitch + "</pch>");
            if (gold != prevData.gold)
                if (gold == -1)
                    returnData.append("<gold>---</gold>");
                else
                    returnData.append("<gold>" + gold + "</gold>");
            if (!codes.equals(prevData.codes))
                returnData.append("<codes>" + codes + "</codes>");
            if (!creator.equals(prevData.creator))
                returnData.append("<owner>" + creator + "</owner>");
            if (x != prevData.x)
                returnData.append("<x>" + x + "</x>");
            if (y != prevData.y)
                returnData.append("<y>" + y + "</y>");
            if (z != prevData.z)
                returnData.append("<z>" + z + "</z>");
            if (tgtX != prevData.tgtX)
                returnData.append("<tgtx>" + tgtX + "</tgtx>");
            if (tgtY != prevData.tgtY)
                returnData.append("<tgty>" + tgtY + "</tgty>");
            if (tgtZ != prevData.tgtZ)
                returnData.append("<tgtz>" + tgtZ + "</tgtz>");
            if (!tgtName.equals(prevData.tgtName))
                returnData.append("<tgtname>" + tgtName + "</tgtname>");
            if (energy != prevData.energy)
                returnData.append("<energy>" + energy + "</energy>");
            if (payout != prevData.payout)
                returnData.append("<payout>" + payout + "</payout>");
            if (!locked.equals(prevData.locked))
                returnData.append("<locked>" + locked + "</locked>");
            if (!remains.equals(prevData.remains))
                returnData.append("<remains>" + remains + "</remains>");
        } else {
            // different object, send clear signal, and append 'initial' write for that object type
            return "<clearscanner />" + writeInitial();
        }

        if (returnData.length() > 0)
            return "<scandata>" + returnData.toString() + "</scandata>";

        return "";
    }

    protected String writeInitial() {
        StringBuffer outScan = new StringBuffer();

        outScan.append("<scandata>");

        // add common elements
        outScan.append("<x>" + x + "</x>");
        outScan.append("<y>" + y + "</y>");
        outScan.append("<z>" + z + "</z>");
        outScan.append("<name>" + objName + "</name>");
        outScan.append("<type>" + theObj.type + "</type>");

        switch (theObj.type) {
            case TrekObject.OBJ_SHIP:
                outScan.append("<class>" + shipClass + "</class>");
                outScan.append("<we>" + we + "</we>");
                outScan.append("<ie>" + ie + "</ie>");
                outScan.append("<pu>" + pu + "</pu>");
                outScan.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
                if (shields == -1)
                    outScan.append("<sh>---</sh>");
                else
                    outScan.append("<sh>" + shields + "</sh>");
                outScan.append("<ph>" + ph + "</ph>");
                outScan.append("<dmg>" + dmg + "</dmg>");
                outScan.append("<tp>" + tp + "</tp>");
                outScan.append("<dc>" + dmgctl + "</dc>");
                if (ls == -1)
                    outScan.append("<ls>---</ls>");
                else
                    outScan.append("<ls>" + ls + "</ls>");
                outScan.append("<anti>" + anti + "</anti>");
                outScan.append("<dr>---</dr>");
                outScan.append("<mn>---</mn>");
                outScan.append("<cl>---</cl>");
                outScan.append("<hdg>" + heading + "</hdg>");
                outScan.append("<pch>" + pitch + "</pch>");
                outScan.append("<gold>" + gold + "</gold>");
                break;

            case TrekObject.OBJ_OBSERVERDEVICE:
                outScan.append("<dist>" + dist + "</dist>");
                if (tgtName.equals("")) {
                    // not scanning
                    outScan.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
                } else {
                    // scanning
                    outScan.append("<tgtx>" + tgtX + "</tgtx>");
                    outScan.append("<tgty" + tgtY + "</tgty>");
                    outScan.append("<tgtz>" + tgtZ + "</tgtz>");
                    outScan.append("<tgtname>" + tgtName + "</tgtname>");
                    outScan.append("<class>" + shipClass + "</class>");
                    outScan.append("<we>" + we + "</we>");
                    outScan.append("<ie>" + ie + "</ie>");
                    outScan.append("<pu>" + pu + "</pu>");
                    outScan.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
                    outScan.append("<sh>" + shields + "</sh>");
                    outScan.append("<dmg>" + dmg + "</dmg>");
                    outScan.append("<tp>" + tp + "</tp>");
                    outScan.append("<ls>" + ls + "</ls>");
                    outScan.append("<anti>" + anti + "</anti>");
                    outScan.append("<dr>" + drones + "</dr>");
                    outScan.append("<mn>" + mines + "</mn>");
                    outScan.append("<hdg>" + heading + "</hdg>");
                    outScan.append("<pch>" + pitch + "</pch>");
                }
                break;

            case TrekObject.OBJ_ASTEROIDBELT:
            case TrekObject.OBJ_BLACKHOLE:
            case TrekObject.OBJ_NEBULA:
            case TrekObject.OBJ_PULSAR:
            case TrekObject.OBJ_QUASAR:
            case TrekObject.OBJ_STAR:
            case TrekObject.OBJ_WORMHOLE:
                outScan.append("<dist>" + dist + "</dist>");
                break;

            case TrekObject.OBJ_COMET:
                outScan.append("<dist>" + dist + "</dist>");
                outScan.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
                outScan.append("<hdg>" + heading + "</hdg>");
                outScan.append("<pch>" + pitch + "</pch>");
                break;

            case TrekObject.OBJ_PLANET:
            case TrekObject.OBJ_STARBASE:
                outScan.append("<dist>" + dist + "</dist>");
                if (dmg == -1)
                    outScan.append("<dmg>---</dmg>");
                else
                    outScan.append("<dmg>" + dmg + "</dmg>");
                if (gold == -1)
                    outScan.append("<gold>---</gold>");
                else
                    outScan.append("<gold>" + gold + "</gold>");
                outScan.append("<codes>" + codes + "</codes>");
                break;

            case TrekObject.OBJ_BUOY:
            case TrekObject.OBJ_IRIDIUM:
            case TrekObject.OBJ_NEUTRON:
                outScan.append("<dist>" + dist + "</dist>");
                outScan.append("<owner>" + creator + "</owner>");
                break;

            case TrekObject.OBJ_TORPEDO:
                outScan.append("<dist>" + dist + "</dist>");
                outScan.append("<owner>" + creator + "</owner>");
                outScan.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
                outScan.append("<energy>" + energy + "</energy>");
                break;

            case TrekObject.OBJ_MINE:
            case TrekObject.OBJ_CORBOMITE:
                outScan.append("<dist>" + dist + "</dist>");
                outScan.append("<owner>" + creator + "</owner>");
                outScan.append("<energy>" + energy + "</energy>");
                break;

            case TrekObject.OBJ_DRONE:
                outScan.append("<dist>" + dist + "</dist>");
                outScan.append("<owner>" + creator + "</owner>");
                outScan.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
                outScan.append("<energy>" + energy + "</energy>");
                outScan.append("<hdg>" + heading + "</hdg>");
                outScan.append("<pch>" + pitch + "</pch>");
                break;

            case TrekObject.OBJ_MAGNABUOY:
                outScan.append("<dist>" + dist + "</dist>");
                outScan.append("<owner>" + creator + "</owner>");
                outScan.append("<locked>" + locked + "</locked>");
                break;

            case TrekObject.OBJ_SHIPDEBRIS:
                outScan.append("<dist>" + dist + "</dist>");
                outScan.append("<gold>" + gold + "</gold>");
                outScan.append("<remains>" + remains + "</remains>");
                break;

            case TrekObject.OBJ_GOLD:
                outScan.append("<dist>" + dist + "</dist>");
                outScan.append("<gold>" + gold + "</gold>");
                outScan.append("<owner>" + creator + "</owner>");
                break;

            default:
                System.out.println("*R*A*W* ::: unexpected object type: " + theObj.type);
        }

        outScan.append("</scandata>");

        return outScan.toString();
    }
}
