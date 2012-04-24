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


/**
 * Used for most mathematical operations.
 *
 * @author Joe Hopkinson
 */
public final class TrekMath {

    public TrekMath() {
    }

    /**
     * Gets the distance between two TrekObjects.
     *
     * @param obj1 The source TrekObject.
     * @param obj2 The destination TrekObject.
     * @return A double of the distance between the passed objects.
     */
    public static double getDistance(TrekObject obj1, TrekObject obj2) {
        float dx, dy, dz;

        if (obj1 != null && obj2 != null) {
            dx = obj1.point.x - obj2.point.x;
            dy = obj1.point.y - obj2.point.y;
            dz = obj1.point.z - obj2.point.z;

            return Math.sqrt(dx * dx + dy * dy + dz * dz);
        }

        return 0;
    }

    public static double getDistance(Trek3DPoint p1, Trek3DPoint p2) {
        float dx, dy, dz;

        dx = p1.x - p2.x;
        dy = p1.y - p2.y;
        dz = p1.z - p2.z;

        return Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }

    /**
     * <p>Gets the distance that would be moved at a given warp speed per tick.</p>
     *
     * @param warpSpeed The warp speed to calculate.
     * @return A double of the distance moved.
     */
    // Gets the distance moved at a given warp speed.  1/4 of a second.
    public static double getDistanceMoved(double warpSpeed) {
        double workingSpeed = Math.abs(warpSpeed);
        double moveUnits = Math.pow(((workingSpeed + 1) * 10), 3);

        if (warpSpeed < 0) {
            return ((moveUnits / 9050) / 4) * -1;
        } else {
            return (moveUnits / 9050) / 4;
        }

    }

    // Gets the warp speed necessary to move a specific number of units in one tick
    // Used in tracking transwarp for the last tick of transwarp
    public static double getWarpToDistance(double dist) {
        // units_per_second = ((warp + 1) * 10)^3 / 9050 / 4
        // 4 * 9050 * ups = ((warp + 1) * 10)^3
        // (36200 * ups)^(1/3) = (warp + 1) * 10
        // ((36200 * ups)^(1/3)) / 10 = warp + 1
        // warp = (((36200 * ups)^(1/3)/10 - 1)

        double warpSpeed = (Math.pow(36200.0 * dist, (1.0 / 3.0)) / 10.0) - 1.0;
        //TrekLog.logMessage("getWarpToDistance, dist: " + dist + " calculates warp " + warpSpeed);
        return warpSpeed;
    }

    public static double getShieldReduction(double rawDamage, int shieldStrength) {
        return rawDamage / shieldStrength;
    }

    public static double getInternalDamage(double rawDamage, int damagePercent) {
        double result = ((rawDamage) * (damagePercent)) / 3000;
        if (result <= 0) {
            result = 0;
        }
        return result;
    }

    public static double getScanningRange(double scanRange, int energyUsed, int visibility) {
        return (scanRange + 10 * energyUsed) * visibility / 100;
    }

    public static double getShieldDrop(double rawDamage, int shieldStrength) {
        return rawDamage / shieldStrength;
    }

    public static double getDamageDifference(double shieldDrop, int shieldStrength) {
        return (100 - shieldDrop) * shieldStrength;
    }

    /**
     * Given a source and target ship, returns whether you can scan an object
     *
     * @param source The source ship
     * @param obj    The target object
     * @return boolean Whether or not an object can be scanned.
     */
    public static boolean canScanShip(TrekShip source, TrekObject obj) {
        double scanRange;
        int targetVisibility;
        double actualScanRange;

        if (source.nebulaTarget != null) {
            scanRange = 1000;
        } else {
            scanRange = source.scanRange;
        }

        // Special Objects that can always be scanned.
        if (TrekUtilities.isObjectBuoy(obj) ||
                TrekUtilities.isObjectGold(obj) ||
                TrekUtilities.isObjectObserverDevice(obj) ||
                TrekUtilities.isObjectPlanet(obj) ||
                TrekUtilities.isObjectShipDebris(obj) ||
                TrekUtilities.isObjectStarbase(obj) ||
                TrekUtilities.isObjectWormhole(obj) ||
                TrekUtilities.isObjectMagnabuoy(obj) ||
                TrekUtilities.isObjectStar(obj) ||
                TrekUtilities.isObjectComet(obj) ||
                TrekUtilities.isObjectBlackhole(obj) ||
                TrekUtilities.isObjectZone(obj)) {
            return true;
        }

        if (TrekUtilities.isObjectShip(obj)) {
            TrekShip targetShip = (TrekShip) obj;

            if (!targetShip.isPlaying())
                return false;

            if (targetShip.cloaked) {
                if (targetShip.quasarTarget != null) return true;
                if (targetShip.pulsarTarget != null && targetShip.pulsarTarget.active) return true;
                return false;
            }

            if (targetShip.nebulaTarget != null) {
                targetVisibility = targetShip.visibility / 3;
            } else {
                targetVisibility = targetShip.visibility;
            }

            actualScanRange = TrekMath.getScanningRange(scanRange, (targetShip.getWarpEnergy() + targetShip.impulseEnergy) - targetShip.getAvailablePower(), targetVisibility);
        } else {
            actualScanRange = scanRange;
        }

        double distanceFromObject = TrekMath.getDistance(source, obj);

        if (distanceFromObject < actualScanRange) {
            return true;
        }

        return false;
    }

    public static int getDecimalFromOctal(String octalCode) {
        try {
            int code1 = new Integer(octalCode.substring(0, 1)) * 64;
            int code2 = new Integer(octalCode.substring(1, 2)) * 8;
            int code3 = new Integer(octalCode.substring(2, 3));
            return code1 + code2 + code3;
        } catch (java.lang.NumberFormatException nfe) {
            TrekLog.logMessage("Could not parse octal: " + octalCode);
            return 0;
        }
    }
}