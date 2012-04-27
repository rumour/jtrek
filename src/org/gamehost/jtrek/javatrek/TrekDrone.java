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
 * @author Joe Hopkinson
 */
public class TrekDrone extends TrekObject {
    public TrekShip owner;
    protected double warpSpeed;
    protected boolean intercepting;
    protected TrekObject interceptTarget;
    protected int strength;
    protected int counter;
    protected String ownerName;
    protected double oldWarpSpeed;
    protected int tickCounter;
    protected boolean active;

    public TrekDrone(String droneType, String ownerLtr, float ownx, float owny, float ownz) {
        super(droneType, ownerLtr, ownx, owny, ownz);
    }

    public TrekDrone(TrekShip ownerin, TrekObject target) {
        super("drone", ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);
        owner = ownerin;
        lifeTime = 60;  // seconds
        tickCounter = 0;
        active = false;
        currentQuadrant = owner.currentQuadrant;

        if (ownerin.variableSpeed) {
            warpSpeed = ownerin.droneSpeed;

            if (warpSpeed == 12) {
                strength = 400;
            } else {
                strength = ((12 - new Double(warpSpeed).intValue()) * 57) + 400;
            }

        } else {
            warpSpeed = 10;
            strength = owner.droneStrength;
        }

        oldWarpSpeed = warpSpeed;

        ownerName = owner.name;

        //scanRange = ownerin.scanRange;
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
        tickCounter++;

        if (tickCounter >= 16) { // 4 second activation delay
            active = true;
        }

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

            if (targetShip.cloaked) {
                warpSpeed = 0;
            } else {
                if (warpSpeed != oldWarpSpeed) {
                    warpSpeed = oldWarpSpeed;
                }
            }
        }

        if ((active) && (TrekMath.getDistance(this, interceptTarget) < 100)) {
            TrekDamageStat stat = interceptTarget.doDamage(this, "drone", false);
            owner.updateBattle(stat);
            kill();
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

    protected void kill() {
        currentQuadrant.removeObjectByScanLetter(scanLetter);
    }

    protected void updateBattle(TrekDamageStat stat) {
        // Since they hit the drone.  Kill it, and give 10 points. But, they have to have caused some damage.
        if (stat.damageGiven > 0) {
            stat.damageGiven = 0;
            stat.structuralDamage = 0;
            stat.bonus = 0;
            stat.shieldReduction = 0;
            kill();
        }
    }

    protected String getYaw() {
        return new Integer((int) Math.round(vector.getHeading())).toString();
    }

    protected String getPitch() {
        double pitch = vector.getPitch();
        String sign = (pitch < 0) ? "-" : "+";
        String pad = (Math.abs(Math.round(pitch)) < 10) ? "0" : "";

        return sign + pad + (new Integer(Math.abs((int) Math.round(pitch))).toString());
    }

}
