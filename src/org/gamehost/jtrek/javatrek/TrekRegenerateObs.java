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
import java.util.TimerTask;


public class TrekRegenerateObs extends TimerTask {
    TrekObserverDevice regenObs;

    public TrekRegenerateObs(TrekObserverDevice obs) {
        regenObs = obs;
    }

    public void run() {
        regenObs.destroyed = false;
        regenObs.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get("Alpha Quadrant");

        // have them regenerate near earth
        Random newPoint = new Random();
        int offsetX = newPoint.nextInt() % 5000 + 2000;  // random point from 2000 to 7000 distance
        int offsetY = newPoint.nextInt() % 5000 + 2000;
        int offsetZ = newPoint.nextInt() % 5000 + 2000;

        TrekPlanet planetEarth = (TrekPlanet) regenObs.currentQuadrant.getObjectByScanLetter("e");
        regenObs.point.x = planetEarth.point.x + offsetX;
        regenObs.point.y = planetEarth.point.y + offsetY;
        regenObs.point.z = planetEarth.point.z + offsetZ;

        regenObs.warpSpeed = 8;
        regenObs.setInitialDirection();
        // get old object letter, see if it's null, if so, use it... otherwise get the next available one
        synchronized (this) {
            if (regenObs.scanLetter != null && !(regenObs.scanLetter.equals(""))) {
                TrekObject testObject = regenObs.currentQuadrant.getObjectByScanLetter(regenObs.scanLetter);
                if (testObject == null) {
                    // desired object letter seems free, add it to the quadrant
                    regenObs.currentQuadrant.addObject(regenObs);
                } else {
                    // object slot already taken, find a new letter
                    regenObs.scanLetter = regenObs.currentQuadrant.getObjectLetter();
                    if (regenObs.scanLetter.equals("")) {
                        // quadrant full?
                        TrekLog.logMessage("Trying to respawn obs dev: " + regenObs.name + " -- " + regenObs.currentQuadrant.name + " full?");
                        // set it up to try again in a 5 minutes
                        Timer recreateTimer = new Timer();
                        recreateTimer.schedule(new TrekRegenerateObs(regenObs), 300000); // 5 minutes
                    } else {
                        // add it to the quadrant
                        regenObs.currentQuadrant.addObject(regenObs);
                    }
                }
            } else {
                // obs dev scan letter is peculiar, try to get another
                regenObs.scanLetter = regenObs.currentQuadrant.getObjectLetter();
                if (regenObs.scanLetter.equals("")) {
                    // quadrant full?
                    TrekLog.logMessage("Trying to respawn obs dev(2): " + regenObs.name + " -- " + regenObs.currentQuadrant.name + " full?");
                    // set it up to try again in a 5 minutes
                    Timer recreateTimer = new Timer();
                    recreateTimer.schedule(new TrekRegenerateObs(regenObs), 300000); // 5 minutes
                } else {
                    // add it to the quadrant
                    regenObs.currentQuadrant.addObject(regenObs);
                }
            }
        }
    }

}