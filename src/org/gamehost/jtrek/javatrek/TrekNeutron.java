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
 * Represents a neutron mine in the game.
 *
 * @author Jay Ashworth
 */
public class TrekNeutron extends TrekObject {
    protected TrekShip owner;
    protected int dmgAmount;
    protected int effectRadius;
    protected String ownerName;

    protected int lifeCounter = 1;
    protected int shieldReduceCounter = 0;

    public TrekNeutron(TrekShip ownerin) {
        super("neutron mine", ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);
        owner = ownerin;
        lifeTime = 60;
        scanRange = 100;    //detonation radius
        effectRadius = 500; //shield neutralizing radius
        dmgAmount = 3;
        type = OBJ_NEUTRON;
        ownerName = owner.name;

        Trek3DVector vec = owner.vector;
        vec.normalize();

        if (owner.warpSpeed < 0) {
            vec.scaleUp(150);
            point.add(vec);
        } else {
            vec.scaleUp(150);
            point.subtract(vec);
        }

    }

    protected void doTick() {
        if (lifeCounter >= 6) {
            lifeTime--;
            lifeCounter = 1;
        }

        shieldReduceCounter++;

        Vector visibleObjects = currentQuadrant.getAllShipsInRange(this, effectRadius);

        for (Enumeration e = visibleObjects.elements(); e.hasMoreElements(); ) {
            TrekObject obj = (TrekObject) e.nextElement();

            if (TrekUtilities.isObjectShip(obj)) {
                TrekShip targetShip = (TrekShip) obj;

                if ((targetShip.shields != 100) && (targetShip.raisingShields)) {
                    if (shieldReduceCounter % 4 == 0) targetShip.shields--;  // prevents shields from rising
                }

                if (this.scanRange > TrekMath.getDistance(this, obj)) {
                    TrekDamageStat stat = new TrekDamageStat(dmgAmount * 10, dmgAmount, 0, 0, targetShip, this, "neutron mine", false);
                    stat.rammed = true; // treat it as ram damage (i.e. no energy loss due to damage incurred)
                    owner.updateBattle(stat);
                    targetShip.damage += dmgAmount;
                    kill();
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
