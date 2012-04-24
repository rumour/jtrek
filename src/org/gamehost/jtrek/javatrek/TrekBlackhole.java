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
import java.util.Random;
import java.util.Vector;


/**
 * Represents a black hole in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekBlackhole extends TrekObject {
    protected int effectRange = 500;
    protected int gravityRange = 10000;
    protected String[] possWarpObjs;

    public TrekBlackhole(int x, int y, int z, String name, String scanletter, String[] targetObjs) {
        super(name, scanletter, x, y, z);
        type = OBJ_BLACKHOLE;
        possWarpObjs = targetObjs;
    }

    protected void doTick() {
        // get all ships within gravity field, and pull them closer with a varying strength based on their distance from the hole
        Vector gravShips = currentQuadrant.getAllShipsInRange(this, gravityRange);

        double gravPullFactor = 0;
        double gravMvmt = 0;

        for (Enumeration e = gravShips.elements(); e.hasMoreElements(); ) {
            TrekShip curShip = (TrekShip) e.nextElement();
            // the closer they are, the larger the gravPullFactor will become; min = 4, max = (9.5 ^ 2 / 4) + 4 (= 26.56)
            gravPullFactor = (Math.pow((gravityRange - TrekMath.getDistance(this, curShip)) / gravityRange * 10, 1.5) / 2) + 3;
            gravMvmt = TrekMath.getDistanceMoved(gravPullFactor);
            //curShip.parent.hud.sendMessage("Blackhole pull @" + gravPullFactor);
            //StringBuffer result = new StringBuffer();

            Trek3DPoint holePoint = new Trek3DPoint(this.point);
            Trek3DPoint shipPoint = new Trek3DPoint(curShip.point);

            Trek3DVector vectorToHole = new Trek3DVector(holePoint.x - shipPoint.x, holePoint.y - shipPoint.y, holePoint.z - shipPoint.z);
            vectorToHole.normalize();
            vectorToHole.scaleUp(gravMvmt);
            curShip.point.add(vectorToHole);
            curShip.unitsTraveled += gravMvmt; // keep units traveled accurate
        }

        // get any ships that are within 1000 units, are excels, have the black hole as their intercept target, and are transwarping
        Vector ships = currentQuadrant.getAllShipsInRange(this, 1000);
        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip ship = (TrekShip) e.nextElement();
            if (ship instanceof ShipExcelsior && ship.transwarpEngaged && ship.interceptTarget == this) {
                ship.interceptTarget = null;
                ship.point = new Trek3DPoint(this.point);
            }

        }

        // get all ships within actual transportation field, and spit them near a random base
        ships = currentQuadrant.getAllShipsInRange(this, effectRange);

        // define the random generator outside the form loop, so it doesn't get instantiated each iteration
        Random baseChoice = new Random();

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip ship = (TrekShip) e.nextElement();

            // randomly send them near base 0-9
            int theBase = Math.abs(baseChoice.nextInt() % (possWarpObjs.length + 1));
            boolean quadTransfer = false;
            if (theBase == possWarpObjs.length) { // possibly send to another quadrant
                theBase = Math.abs(baseChoice.nextInt() % possWarpObjs.length);
                int theQuad = Math.abs(baseChoice.nextInt() % 4);
                String quad = "";
                switch (theQuad) {
                    case 1:
                        quad = "Beta Quadrant";
                        break;
                    case 2:
                        quad = "Gamma Quadrant";
                        break;
                    case 3:
                        quad = "Omega Quadrant";
                        break;
                    default:
                        quad = "Alpha Quadrant";
                }
                if (!(currentQuadrant.name.equals(quad))) {
                    quadTransfer = true;
                    ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
                    ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(quad);

                    ship.currentQuadrant.addShip(ship);
                    TrekLog.logMessage(name + ": Blackhole sending " + ship.name + " to " + quad);
                    ship.parent.hud.sendMessage("Black hole " + name + " sends you to " + quad + "...");
                }
            }
            TrekLog.logMessage(name + ": Blackhole sending " + ship.name + " to object " + possWarpObjs[theBase]);
            if (!quadTransfer) ship.parent.hud.sendMessage("Black hole " + name + " sends you across the quadrant...");

            TrekObject thisObj = currentQuadrant.getObjectByScanLetter(possWarpObjs[theBase]);

            // set them out at a random point around 2-3k from base
            int randomHeading = Math.abs(baseChoice.nextInt() % 360); //
            int randomPitch = baseChoice.nextInt() % 90;
            int randomDistance = Math.abs(baseChoice.nextInt() % 1000) + 2000;

            Trek3DPoint target = new Trek3DPoint();
            randomPitch = 90 - randomPitch;
            // calculate end points
            target.x = thisObj.point.x + (float) (randomDistance * Math.sin(Math.toRadians(randomPitch)) * Math.sin(Math.toRadians(randomHeading)));
            target.y = thisObj.point.y + (float) (randomDistance * Math.sin(Math.toRadians(randomPitch)) * Math.cos(Math.toRadians(randomHeading)));
            target.z = thisObj.point.z + (float) (randomDistance * Math.cos(Math.toRadians(randomPitch)));

            ship.point = target;

            // TODO: set heading and pitch back to what it was before entering the black hole
        }
    }

}