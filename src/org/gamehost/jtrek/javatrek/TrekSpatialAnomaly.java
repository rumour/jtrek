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
import java.util.Timer;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Feb 2, 2005
 * Time: 6:58:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrekSpatialAnomaly extends TrekWormhole {
    int regenTime = 300000; // 5 minutes in millis
    private int counterStart;

    public TrekSpatialAnomaly(int x, int y, int z, String name, String scanletter) {
        super(x, y, z, name, scanletter);
        targetQuadrant = "";
        activeCountdown = 180;
        counterStart = activeCountdown;
        active = true;
    }

    protected void doTick() {
        activeCountdown--;

        // don't send through in the first 10 seconds
        if (counterStart - activeCountdown < 40)
            return;

        Vector shipsToTransfer = currentQuadrant.getShipsAffectedByWormhole(this, 1000);

        if (shipsToTransfer.size() > 0) {
            for (Enumeration e = shipsToTransfer.elements(); e.hasMoreElements(); ) {
                TrekShip ship = (TrekShip) e.nextElement();

                if (ship.name.equals("borg")) // borg are immune to spatial anomalies
                    continue;

                TrekLog.logMessage("Spatial anomaly transferring " + ship.parent.shipName + " to " + targetQuadrant);

                ship.wormholeTransfer = false;
                ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);

                targetQuadrant = getTargetQuadrant(ship);
                ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(targetQuadrant);

                ship.currentQuadrant.addShip(ship);

                ship.whTimeElapsed = 0;
                ship.lockTarget = null;
                ship.scanTarget = null;
                ship.interceptTarget = null;
                ship.nebulaTarget = null;
                ship.shipDebrisTarget = null;
                ship.parent.hud.clearScanner();

                ship.parent.hud.sendTopMessage("Entering " + targetQuadrant + " ...");
            }
        }

        if (activeCountdown <= 0) {
            Random gen = new Random();
            activeCountdown = Math.abs(gen.nextInt() % 1200) + 1200;  // active time from 5 to 10 minutes
            counterStart = activeCountdown;
            Timer recreateTimer = new Timer("SpatialAnomaly-" + currentQuadrant);
            recreateTimer.schedule(new TrekAnomalyTimer(this), regenTime);
            currentQuadrant.removeObjectByScanLetter(scanLetter);
            regenTime = Math.abs(gen.nextInt() % 900000) + 300000;  // from 5 to 20 minutes to respawn
        }

    }

    protected String getTargetQuadrant(TrekShip targetShip) {
        String returnQuadrant = "";

        if (targetShip.currentQuadrant.name.equals("Alpha Quadrant"))
            returnQuadrant = "Delta Quadrant";

        if (targetShip.currentQuadrant.name.equals("Delta Quadrant"))
            returnQuadrant = "Alpha Quadrant";

        return returnQuadrant;
    }

}
