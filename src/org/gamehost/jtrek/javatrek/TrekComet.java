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
import java.util.Random;
import java.util.Vector;


/**
 * Represents a coment in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekComet extends TrekObject {
    protected Random randomGen = new Random();
    protected TrekZone internalZone;
    protected Trek3DPoint focalPoint;
    protected int effectRadius = 3000;
    //protected double warpSpeed;
    protected int centerX, centerY, centerZ;
    protected int minX, maxX, minY, maxY;
    protected boolean xIsMajorAxis;
    protected int majorAxisLength;
    protected int minorAxisLength;
    protected boolean xIncreasing;
    protected boolean yIncreasing;
    protected boolean zIncreasing;

    // for now just have it use an intercepting target and go back and forth
    // from vulcan to wrigley
    TrekObject interceptTarget;

    // to create a Comet with an elliptical orbit, we need a center point, major axis size, minor axis size
    // and we need two additional points actually on the path of the ellipse to define the plane in space
    public TrekComet(int x, int y, int z, String name, String scanletter) {
        super(name, scanletter, x, y, z);
        type = OBJ_COMET;
        internalZone = new TrekZone(x, y, z, "", "", effectRadius, TrekObject.OBJ_NEBULA);
        warpSpeed = 10;
    }

    // quicky 2d attempt, orbit on xy axis
    public TrekComet(int centX, int centY, int z, int minorLength, int majorLength, boolean xAxis, String name, String scanletter) {
        super(name, scanletter, centX, centY, z);
        type = OBJ_COMET;

        centerX = centX;
        centerY = centY;

        // shift starting point, and define variables based on which axis is the major one
        xIsMajorAxis = xAxis;

        if (xIsMajorAxis) {
            point = new Trek3DPoint(majorLength, centY, z);
            minX = centerX - majorAxisLength;
            maxX = centerX + majorAxisLength;
            minY = centerY - minorAxisLength;
            maxY = centerY + minorAxisLength;
            xIncreasing = false;
            yIncreasing = false;
        } else {
            point = new Trek3DPoint(centX, majorLength, z);
            minX = centerX - minorAxisLength;
            maxX = centerX + minorAxisLength;
            minY = centerY - majorAxisLength;
            maxY = centerY + majorAxisLength;
            xIncreasing = true;
            yIncreasing = false;
        }

        // setup internal nebula
        internalZone = new TrekZone(point.getXInt(), point.getYInt(), point.getZInt(), "", "", effectRadius, TrekObject.OBJ_NEBULA);

        // define orbit focus point (what the comet orbits around)
        double eccentricity = Math.sqrt(Math.pow(majorAxisLength, 2) - Math.pow(minorAxisLength, 2)) / majorAxisLength;
        if (xIsMajorAxis) {
            focalPoint = new Trek3DPoint((float) (centerX + eccentricity * majorAxisLength), centerY, z);
        } else {
            focalPoint = new Trek3DPoint(centerX, (float) (centerY + eccentricity * majorAxisLength), z);
        }

        warpSpeed = 10;

    }

    protected void doTick() {
        Vector ships = currentQuadrant.getAllShipsInRange(this, effectRadius + 3100);

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip ship = (TrekShip) e.nextElement();
            if (TrekMath.getDistance(ship, this) <= effectRadius) {
                if (ship.nebulaTarget != internalZone) {
                    ship.nebulaTarget = internalZone;
                    ship.parent.hud.sendMessage("Now affected by " + name + ".  Visibility reduced, shields ineffective.");
                }
            } else {
                if (ship.nebulaTarget == internalZone) {
                    ship.nebulaTarget = null;
                    ship.parent.hud.sendMessage("No longer affected by " + name + ".");
                }
            }
        }
    }

    protected void updateOrbitPoint() {
        if (interceptTarget == null) {
            interceptTarget = currentQuadrant.getObjectByScanLetter("p");
            warpSpeed = 10;
        }

        double curDistance = TrekMath.getDistance(this, interceptTarget);

        if (curDistance > 10000) {
            // 16 to 17.5
            warpSpeed = ((double) Math.abs(randomGen.nextInt() % 16) / 10) + 16;
        } else if (curDistance <= 10000 && curDistance > 5000) {
            // 12 to 13.5
            warpSpeed = ((double) Math.abs(randomGen.nextInt() % 16) / 10) + 12;
        } else if (curDistance <= 5000 && curDistance > 500) {
            // 10 to 11.5
            warpSpeed = ((double) Math.abs(randomGen.nextInt() % 16) / 10) + 10;
        } else if (curDistance <= 500) {
            // 7.5 to 9
            warpSpeed = ((double) Math.abs(randomGen.nextInt() % 16) / 10) + 7.5;
            if (curDistance <= 20) {
                if (interceptTarget == currentQuadrant.getObjectByScanLetter("p")) {
                    interceptTarget = currentQuadrant.getObjectByScanLetter("v");
                } else if (interceptTarget == currentQuadrant.getObjectByScanLetter("v")) {
                    interceptTarget = currentQuadrant.getObjectByScanLetter("h");
                } else {
                    interceptTarget = currentQuadrant.getObjectByScanLetter("p");
                }
            }
        }

        Trek3DPoint current = new Trek3DPoint(point);
        Trek3DPoint target = new Trek3DPoint(interceptTarget.point);

        if (current.equals(target)) {
            vector = new Trek3DVector(0, 0, 1);
        } else {
            target.subtract(current);
            vector.applyPoint(target);
        }

        Trek3DVector returnVector = new Trek3DVector(vector);
        double distance = TrekMath.getDistanceMoved(warpSpeed);

        returnVector.normalize();
        returnVector.scaleUp(distance);

        point.add(returnVector);

        // keep linked nebula zone in-synch with comet movement
        internalZone.point = point;
    }

    protected String getYaw() {
        //return new Integer((int) Math.round(vector.getHeading())).toString();
        double tmp1 = vector.getHeading();
        int tmp2 = (int) Math.round(tmp1);
        Integer tmp3 = new Integer(tmp2);
        String tmp4 = tmp3.toString();
        return tmp4;
    }

    protected String getPitch() {
        double pitch = vector.getPitch();
        String sign = (pitch < 0) ? "-" : "+";
        String pad = (Math.abs(Math.round(pitch)) < 10) ? "0" : "";

        return sign + pad + (new Integer(Math.abs((int) Math.round(pitch))).toString());
    }

    protected double getSlope() {
        double returnValue = 0;
        returnValue = Math.pow(majorAxisLength, 2) * point.y;
        if (returnValue != 0) {
            returnValue = (0 - Math.pow(minorAxisLength, 2) * point.x) / returnValue;
        } else {
            returnValue = -1;  // need to differentiate between x = 0, and y = 0
        }
        return returnValue;
    }

}