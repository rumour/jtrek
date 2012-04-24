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
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Jan 11, 2005
 * Time: 9:31:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrekLithium extends TrekMine {
    protected int lifeCounter = 1;
    protected TrekShip tower;
    protected Trek3DPoint delta;

    public TrekLithium(TrekShip ownerin) {
        super("lithium mine", ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);
        owner = ownerin;
        lifeTime = 20;
        scanRange = 5000;
        strength = 500;
        type = OBJ_MINE;
        ownerName = owner.name;

        Trek3DVector vec = owner.vector;
        vec.normalize();

    }

    protected void doTick() {
        if (lifeCounter >= 6) {
            lifeTime--;
            lifeCounter = 1;
        }

        if (tower != null && !tower.isPlaying())
            tower = null;

        if (tower != null)
            warpSpeed = tower.warpSpeed;

        Vector visibleObjects = currentQuadrant.getAllShipsInRange(this, (int) scanRange);

        if (lifeCounter % 4 == 0 && warpSpeed != 0 && tower == null) {
            if (warpSpeed > 0) {
                warpSpeed -= 1;
            } else {
                warpSpeed += 1;
            }

            if (Math.abs(warpSpeed) <= 1)
                warpSpeed = 0;
        }

        if (lifeTime <= 0) {
            for (Enumeration e = visibleObjects.elements(); e.hasMoreElements(); ) {
                TrekObject obj = (TrekObject) e.nextElement();

                if (TrekUtilities.isObjectShip(obj)) {
                    if (this.scanRange > TrekMath.getDistance(this, obj)) {
                        if (TrekUtilities.isObjectShip(obj)) {
                            TrekShip targetShip = (TrekShip) obj;
                            double damage = strength - (TrekMath.getDistance(this, targetShip) * .95);
                            if (damage > 0)
                                targetShip.applyDamage(damage, this, "lithium mine", false);
                        }
                    }
                }
            }
            kill();
            return;
        }

        lifeCounter++;
    }

    protected Trek3DVector getNewDirectionVector() {
        try {
            vector.normalize();
            Trek3DVector returnVector = new Trek3DVector();

            double distance = TrekMath.getDistanceMoved((tower != null) ? tower.warpSpeed : warpSpeed);

            returnVector.x = vector.x;
            returnVector.y = vector.y;
            returnVector.z = vector.z;

            returnVector.normalize();
            returnVector.scaleUp(distance);

            return returnVector;
        } catch (Exception e) {
            TrekLog.logError("TrekObserverDevice.getNewDirectionVector error.");
            TrekLog.logException(e);
        }

        return vector;
    }

    protected void kill() {
        tower = null;
        currentQuadrant.removeObjectByScanLetter(scanLetter);
    }

    protected void updateBattle(TrekDamageStat stat) {
        if (stat.damageGiven > 0) {
            strength += (stat.damageGiven / 2);
            stat.damageGiven = 0;
            stat.structuralDamage = 0;
            stat.bonus = 0;
            stat.shieldReduction = 0;
        }
    }

}
