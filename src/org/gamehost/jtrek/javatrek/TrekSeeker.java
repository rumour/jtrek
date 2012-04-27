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

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Jan 11, 2005
 * Time: 10:43:04 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrekSeeker extends TrekDrone {

    public TrekSeeker(TrekShip ownerin, TrekObject target) {
        super("seeker probe", ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);
        owner = ownerin;
        strength = 0;
        lifeTime = 10;  // seconds
        active = false;
        currentQuadrant = owner.currentQuadrant;

        warpSpeed = 20;

        ownerName = owner.name;

        // allow omniscient drones
        scanRange = 250000;
        interceptTarget = target;
        type = OBJ_DRONE;

        intercepting = true;
        getNewDirectionVector();
        counter = 1;


        shields = 0;
        shieldStrength = 1;
        damage = 0;
    }

    protected void doTick() {
        if (TrekUtilities.isObjectShip(interceptTarget)) {
            TrekShip targetShip = (TrekShip) interceptTarget;
            TrekShip shipExists = (TrekShip) currentQuadrant.getShipByScanLetter(targetShip.scanLetter);

            if ((shipExists == null) || (!shipExists.name.equals(targetShip.name)) ||
                    (shipExists.parent.state != TrekPlayer.WAIT_PLAYING)) {

                intercepting = false;
                warpSpeed = 0;
                if (counter >= 4) {
                    lifeTime -= 1;
                    counter = 1;
                }

                if (lifeTime <= 0) {
                    kill();
                }

                counter++;

                return;
            }
        }

        if (counter >= 4) {
            lifeTime -= 1;
            counter = 1;
        }

        if (lifeTime <= 0) {
            kill();
        }

        counter++;
    }

    protected Trek3DVector getNewDirectionVector() {
        if (intercepting) {
            if (TrekMath.getDistance(this, interceptTarget) < scanRange) {
                Trek3DPoint current = new Trek3DPoint(point);
                Trek3DPoint target = new Trek3DPoint(interceptTarget.point);

                if (current.equals(target)) {
                    vector = new Trek3DVector(0, 0, 1);
                } else {
                    target.subtract(current);
                    vector.applyPoint(target);
                }
            }
        }

        Trek3DVector returnVector = new Trek3DVector(vector);
        double distance = TrekMath.getDistanceMoved(warpSpeed);

        returnVector.normalize();
        returnVector.scaleUp(distance);

        return returnVector;

    }

}
