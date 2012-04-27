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

import java.util.Enumeration;
import java.util.Vector;


/**
 * Represents the remains of a destroyed ship.
 *
 * @author Joe Hopkinson
 */
public class TrekShipDebris extends TrekObject {
    protected int lifeTime;
    protected String whos;
    protected int lifeCounter = 1;
    public int gold;
    protected int dbPlayerID;

    public TrekShipDebris(TrekShip ownerin, int amountOfGold) {
        super("ship debris", ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);
        whos = ownerin.parent.shipName;
        dbPlayerID = ownerin.parent.dbPlayerID;
        lifeTime = 120;
        gold = amountOfGold;
        this.type = TrekObject.OBJ_SHIPDEBRIS;
    }

    protected void doLifetimeTick() {
        if (lifeCounter > 4) {
            lifeTime--;
            lifeCounter = 1;
        }

        lifeCounter++;
    }

    protected void doTick() {
        // get all ships within effect radius
        Vector ships = currentQuadrant.getAllShipsInRange(this, 500);
        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip ship = (TrekShip) e.nextElement();
            if (Math.abs(ship.warpSpeed) > 8) {
                ship.shipDebrisTarget = this;
                ship.applyDamage(Math.abs(ship.warpSpeed) * 3, this, "ship debris", false);
            }
        }

        doLifetimeTick();
        if (lifeTime < 1) {
            kill();
        }
    }

    protected void kill() {
        currentQuadrant.removeObjectByScanLetter(scanLetter);
    }
}