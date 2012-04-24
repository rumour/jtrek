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
 * Represents the wormhole.
 *
 * @author Joe Hopkinson
 */
public class TrekWormhole extends TrekObject {
    protected String targetQuadrant;
    //	protected int quadrantCount;
    protected int activeCountdown;
    protected boolean active;

    public TrekWormhole(int x, int y, int z, String name, String scanletter) {
        super(name, scanletter, x, y, z);
        targetQuadrant = "";
//		quadrantCount = 0;
        type = OBJ_WORMHOLE;
        activeCountdown = 180;
        active = false;
    }

    protected void doTick() {
        activeCountdown--;

        if (activeCountdown <= 0) {
            if (active == true) {
                active = false;
            } else {
                active = true;
            }

            Random gen = new Random();
            activeCountdown = Math.abs(gen.nextInt() % 140) + 40; // fluctuate for min 10 seconds, max 45
        }

        Vector shipsToTransfer = currentQuadrant.getShipsAffectedByWormhole(this, 500);

        if (shipsToTransfer.size() == 0) {
            return;
        }

        if (active) {

            for (Enumeration e = shipsToTransfer.elements(); e.hasMoreElements(); ) {
                TrekShip ship = (TrekShip) e.nextElement();
                TrekLog.logMessage("Wormhole transferring " + ship.parent.shipName + " to " + targetQuadrant);

                ship.wormholeTransfer = false;
                ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);

                targetQuadrant = getTargetQuadrant(ship);
                ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(targetQuadrant);

                ship.currentQuadrant.addShip(ship);

                // wormhole shouldn't change ship coords.
                //ship.point = new Trek3DPoint(1000, 1000, 1000);

                ship.whTimeElapsed = 0;
                ship.lockTarget = null;
                ship.scanTarget = null;
                ship.interceptTarget = null;
                ship.nebulaTarget = null;
                ship.shipDebrisTarget = null;
                ship.parent.hud.clearScanner();

                // If we are towing an observer device, transfer it as well.
                if (ship.tractorBeamTarget != null && TrekUtilities.isObjectObserverDevice(ship.tractorBeamTarget)) {
                    TrekLog.logMessage("Transferring observer device " + ship.tractorBeamTarget.name);
                    if (!((TrekObserverDevice) ship.tractorBeamTarget).destroyed) {
                        ship.tractorBeamTarget.currentQuadrant.removeObjectByScanLetter(ship.tractorBeamTarget.scanLetter);
                        ship.tractorBeamTarget.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(targetQuadrant);

                        ship.tractorBeamTarget.currentQuadrant.addObject(ship.tractorBeamTarget);
                    }
                }

                ship.parent.hud.sendTopMessage("Entering " + targetQuadrant + " ...");
            }

        }
    }

    protected String getTargetQuadrant(TrekShip targetShip) {
        String returnQuadrant = "";

        if (targetShip.warpSpeed < 0) {
            if (targetShip.currentQuadrant.name.equals("Alpha Quadrant"))
                returnQuadrant = "Omega Quadrant";

            if (targetShip.currentQuadrant.name.equals("Beta Quadrant"))
                returnQuadrant = "Alpha Quadrant";

            if (targetShip.currentQuadrant.name.equals("Gamma Quadrant"))
                returnQuadrant = "Beta Quadrant";

            if (targetShip.currentQuadrant.name.equals("Omega Quadrant"))
                returnQuadrant = "Gamma Quadrant";

        } else {
            if (targetShip.currentQuadrant.name.equals("Alpha Quadrant"))
                returnQuadrant = "Beta Quadrant";

            if (targetShip.currentQuadrant.name.equals("Beta Quadrant"))
                returnQuadrant = "Gamma Quadrant";

            if (targetShip.currentQuadrant.name.equals("Gamma Quadrant"))
                returnQuadrant = "Omega Quadrant";

            if (targetShip.currentQuadrant.name.equals("Omega Quadrant"))
                returnQuadrant = "Alpha Quadrant";
        }

        return returnQuadrant;
    }

}