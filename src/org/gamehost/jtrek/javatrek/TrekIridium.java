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
import java.util.Vector;


/**
 * Represents an iridium mine in the game.
 *
 * @author Jay Ashworth
 */
public class TrekIridium extends TrekObject {
    protected TrekShip owner;
    protected int strength;
    protected String ownerName;

    protected int lifeCounter = 1;

    public TrekIridium(TrekShip ownerin) {
        super("iridium mine", ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);
        owner = ownerin;
        lifeTime = 60;
        scanRange = 1000;
        type = OBJ_IRIDIUM;
        ownerName = owner.name;

        Trek3DVector vec = owner.vector;
        vec.normalize();

    }

    protected void doTick() {
        if (lifeCounter >= 6) {
            lifeTime--;
            lifeCounter = 1;
        }

        Vector visibleObjects = currentQuadrant.getAllShipsInRange(this, (int) scanRange);

        for (Enumeration e = visibleObjects.elements(); e.hasMoreElements(); ) {
            TrekObject obj = (TrekObject) e.nextElement();

            if (TrekUtilities.isObjectShip(obj)) {
                if (this.scanRange > TrekMath.getDistance(this, obj)) {
                    if (TrekUtilities.isObjectShip(obj)) {
                        TrekShip targetShip = (TrekShip) obj;
                        if (Math.abs(targetShip.warpSpeed) > 8) {
                            targetShip.applyDamage(Math.abs(targetShip.warpSpeed) * 1.875, this, "iridium mine", false);
                        }
                    }
                }
            }
        }

        if (lifeTime <= 0) {
            kill();
            return;
        }

        lifeCounter++;
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
}
