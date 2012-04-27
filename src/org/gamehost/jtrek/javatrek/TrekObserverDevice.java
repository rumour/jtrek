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

import java.util.Random;
import java.util.Timer;
import java.util.Vector;

/**
 * Represents an observer device in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekObserverDevice extends TrekObject {
    public TrekShip scanTarget;
    protected Trek3DPoint delta;
    int scanTimeout = 0;
    int tick = 0;
    int second = 0;
    int scanRange;
    double warpSpeed;
    TrekShip tower;
    boolean destroyed = false;
    int regenTime = 3600000;  // 1 hour in milliseconds
    int timeToReset = 7200;  // 30 minutes in ticks
    Trek3DPoint homePoint;

    public TrekObserverDevice(String namein, String scanletterin, float xin, float yin, float zin) {
        super(namein, scanletterin, xin, yin, zin);
        homePoint = new Trek3DPoint(point);

        scanRange = 5000;
        warpSpeed = 8.0;

        type = OBJ_OBSERVERDEVICE;

        setInitialDirection();
    }

    public boolean isScanning() {
        if (scanTarget != null) {
            if (scanTarget.currentQuadrant == this.currentQuadrant) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    protected void doTick() {
        tick++;
        timeToReset--;

        if (isScanning()) {
            if (scanTarget != null && !scanTarget.isPlaying()) {
                scanTarget = null;
                scanTimeout = 6;
            }
        }

        if (tower != null && !tower.isPlaying())
            tower = null;

        if (tower != null) {
            warpSpeed = tower.warpSpeed;
            timeToReset = 7200; // reset this
        }

        if (timeToReset <= 0) {
            // time to move back to home point
            double curDist = TrekMath.getDistance(this.point, homePoint);
            if (curDist > 10000) {
                // if it is more than 10k from it's home point, start moving back
                if (curDist > 1000000) {
                    warpSpeed = 40;
                } else if (curDist > 100000) {
                    warpSpeed = 20;
                } else if (curDist > 40000) {
                    warpSpeed = 16;
                } else if (curDist > 20000) {
                    warpSpeed = 13;
                } else if (curDist < 11000) {
                    warpSpeed = 12;
                } else
                    timeToReset = 7200; // reset the counter
            }

            // set course towards home point
            interceptCoords(homePoint);
        }

        if ((tick % 4) == 0) {
            second++;

            if ((second % 2) == 0) {
                second = 0;

                if (warpSpeed > 0) {
                    if (warpSpeed > 16) { // if it's being flung at high speed,  accelerate the deceleration
                        warpSpeed -= 1;
                    } else {
                        warpSpeed -= .2;
                    }
                }

                if (warpSpeed < 0)
                    warpSpeed = 0;
            }

            if (scanTimeout > 0) {
                scanTimeout--;

                if (scanTimeout == 0) {
                    if (scanTarget != null) {
                        scanTarget = null;
                    }
                } else {
                    return;
                }
            }

            Vector shipsInRange = currentQuadrant.getAllShipsInRange(this, scanRange);

            if (shipsInRange.size() == 0)
                return;

            Random gen = new Random();

            int shipToScan = Math.abs(gen.nextInt() % shipsInRange.size());

            TrekShip closestShip = (TrekShip) shipsInRange.elementAt(shipToScan);

            if (closestShip == null)
                return;

            if (TrekMath.getDistance(this, closestShip) > scanRange)
                return;

            scanTarget = closestShip;
            scanTimeout = Math.abs(gen.nextInt() % 240) + 60;  // random timeout from 1 to 5 minutes
        }

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

    protected void setInitialDirection() {
        Random gen = new Random();

        vector.x = gen.nextInt() % 3000;
        vector.y = gen.nextInt() % 3000;
        vector.z = gen.nextInt() % 3000;

        vector.normalize();
    }

    protected void kill() {
        tower = null;
        destroyed = true;
        currentQuadrant.removeObjectByScanLetter(scanLetter);
        Timer recreateTimer = new Timer("Observer-" + name);
        recreateTimer.schedule(new TrekRegenerateObs(this), regenTime);
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

    protected void interceptCoords(Trek3DPoint destPoint) {
        try {
            Trek3DPoint current = new Trek3DPoint();
            Trek3DPoint target = new Trek3DPoint();

            target.x = destPoint.x;
            target.y = destPoint.y;
            target.z = destPoint.z;

            current.x = point.x;
            current.y = point.y;
            current.z = point.z;

            if (current.equals(target)) {
                vector = new Trek3DVector(0, 0, 1);
            } else {
                target.subtract(current);

                vector.x = target.x;
                vector.y = target.y;
                vector.z = target.z;
            }
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }
}
