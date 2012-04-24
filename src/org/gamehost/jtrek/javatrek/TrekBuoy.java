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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Represents a Buoy in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekBuoy extends TrekObject {
    public TrekShip owner;
    Hashtable infidels;
    int tick = 0;
    int lifeTime = 180;
    long buoyConn;
    Vector brandedThisTick;

    public TrekBuoy(float x, float y, float z, String name, String scanletter, TrekShip ownerShip) {
        super(name, scanletter, x, y, z);
        owner = ownerShip;
        infidels = new Hashtable();
        type = TrekObject.OBJ_BUOY;
        // tie a buoy to a specific connection, so if owner logs out and back in, they will no longer receive notifications
        buoyConn = owner.parent.dbConnectionID;
    }

    protected void kill() {
        currentQuadrant.removeObjectByScanLetter(scanLetter);
    }

    protected void updateBattle(TrekDamageStat stat) {
        if (stat.damageGiven > 0) {
            stat.damageGiven = 0;
            stat.structuralDamage = 0;
            stat.bonus = 0;
            stat.shieldReduction = 0;
            kill();
        }
    }

    protected void doTick() {
        brandedThisTick = new Vector();
        tick++;

        if ((tick % 4) == 0) {
            lifeTime--;

            if (lifeTime <= 0)
                kill();

            tick = 0;
        }

        Vector shipsInRange = currentQuadrant.getAllShipsInRange(this, 3000);

        // Brand or update the current infidels.
        for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
            TrekShip infidel = (TrekShip) e.nextElement();

            if (!infidel.homePlanet.equals(name)) {
                brandTheInfidel(infidel);
            }
        }

        // Remove those not branded this tick.
        for (Enumeration losers = infidels.keys(); losers.hasMoreElements(); ) {
            String theLoser = (String) losers.nextElement();

            if (!brandedThisTick.contains(theLoser)) {
                infidels.remove(theLoser);
            }
        }
    }

    private void brandTheInfidel(TrekShip thisShip) {
        if (thisShip.parent.dbConnectionID == buoyConn)
            return;

        brandedThisTick.add(thisShip.name);

        double distance = TrekMath.getDistance(this, thisShip);

        if (distance > 2000) {
            if (infidels.containsKey(thisShip.name))
                infidels.remove(thisShip.name);

            return;
        }

        if (!infidels.containsKey(thisShip.name)) {
            infidels.put(thisShip.name, "0");
        }

        if (infidels.get(thisShip.name).equals("1"))
            return;

        if (distance <= 2000) {
            if (!infidels.get(thisShip.name).equals("1")) {
                int pointX = new Float(thisShip.point.x).intValue();
                int pointY = new Float(thisShip.point.y).intValue();
                int pointZ = new Float(thisShip.point.z).intValue();

                if (owner.scanLetter.equals(TrekServer.getShipLetterByConn(buoyConn))) {
                    TrekServer.sendMsgToPlayer(owner.scanLetter, "Buoy triggered by " + thisShip.name + " [" + thisShip.scanLetter + "] at (" + pointX + ", " + pointY + ", " + pointZ + ")", this, true, false);
                }
                TrekServer.sendMsgToPlayer(thisShip.scanLetter, "Triggering a buoy.", this, false, false);

                // update coord in ship spotted database
                if (owner.scannedHistory.containsKey(thisShip.scanLetter)) {
                    owner.scannedHistory.remove(thisShip.scanLetter);
                }
                owner.scannedHistory.put(thisShip.scanLetter, new TrekCoordHistory(thisShip.scanLetter, thisShip.name, new Trek3DPoint(thisShip.point)));

                infidels.put(thisShip.name, "1");
            }
            return;
        }
    }
}