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
package org.gamehost.jtrek.javatrek.bot;

import org.gamehost.jtrek.javatrek.*;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Apr 8, 2004
 * Time: 8:43:32 PM
 * To change this template use File | Settings | File Templates.
 */

public class BotExcel extends BotPlayer {
    private boolean transwarpDeviceAborted = false;
    private boolean transwarpDeviceLoaded = false;
    private int ticksToWaitForXwarp = 240;

    private String obsTrackingTarget = null;

    public BotExcel(TrekServer serverIn, String name) {
        super(serverIn, name);

        shipName = name;
    }

    public void loadInitialBot() {
        super.loadInitialBot();

        ship = TrekUtilities.getShip("b", this);
        ship.setInitialDirection();
        ship.currentQuadrant.addShip(ship);

        quadrantName = ship.currentQuadrant.name;

        setCurObjs();

        this.drawHud(true);
    }

    public void botTickUpdate() {
        if (!transwarpDeviceLoaded && !transwarpDeviceAborted) {
            if (ship != null) {
                runMacro("s");
                if (ship.currentQuadrant.name.equals("Alpha Quadrant")) {
                    destinationObj = ship.currentQuadrant.getObjectByScanLetter("e");  // earth we hope
                    doDock(TrekMath.getDistance(ship, destinationObj), true);
                    ticksToWaitForXwarp--;
                    if (ticksToWaitForXwarp < 0) {
                        transwarpDeviceAborted = true;
                    }
                    if (ship.transwarp) {
                        transwarpDeviceLoaded = true;
                    }

                }
            }
        } else {
            if (ship == null) return;

            if (ship.doTranswarp && ship.transwarpCounter <= 1) {
                // make sure we're set to warp
                if (ship.damage > 50) {
                    runMacro("\005@3\r");
                } else {
                    runMacro("\005@4\r");
                }
            } else {
                super.botTickUpdate();
            }
        }

        // ship specific tick functions
    }

    public void botSecondUpdate() {
        if (!transwarpDeviceLoaded && !transwarpDeviceAborted) {
            if (ship != null) {
                zap();
                dodgePlasma();
            }
        } else {
            if (ship == null) return;

            if (ship.doTranswarp && ship.transwarpCounter <= 1) {
                // make sure we're set to warp
                if (ship.damage > 50) {
                    runMacro("\005@3\r");
                } else {
                    runMacro("\005@4\r");
                }
            } else {
                super.botSecondUpdate();
            }
        }

        // ship specific second functions
    }

    protected void doEscapeMovement() {
        if (!ship.doTranswarp) {
            int rndHdng = Math.abs(gen.nextInt() % 360);
            int rndPitch = gen.nextInt() % 90;

            if (ship.isIntercepting()) runMacro("i");
            runMacro("\005@3\rx!" + rndHdng + "'" + rndPitch + "\r");
            if (!ship.doTranswarp) {  // not enough energy, or something!
                super.doEscapeMovement();
            }
        } else {
            runMacro("\005@3\r");
        }
    }

    protected void fireWeapons() {
        if (!ship.doTranswarp) {
            super.fireWeapons();
        }
    }

    protected void checkEnemies() {
        if (!ship.doTranswarp) {
            super.checkEnemies();

            // if no one is around, check obs devices for possible target
            if (targetShip == null) {
                TrekShip possTarget = null;
                int mostDamage = 0;
                this.obsTrackingTarget = null;

                Vector obsDevs = ship.currentQuadrant.getObserverDevices();
                for (Enumeration e = obsDevs.elements(); e.hasMoreElements(); ) {
                    TrekObserverDevice tod = (TrekObserverDevice) e.nextElement();
                    if (tod.isScanning()) {
                        if (tod.scanTarget.damage >= mostDamage) {
                            if (tod.scanTarget == ship) continue;

                            possTarget = tod.scanTarget;
                            obsTrackingTarget = tod.scanLetter;
                        }
                    }
                }

                // if there is a target, and bot ship is at full strength
                if (possTarget != null && ship.currentCrystalCount == ship.maxCrystalStorage) {
                    targetShip = possTarget;
                    targetTimeout = 60;
                    if (shipSubMode == NONE) shipSubMode = ATTACK;
                    logDebug("checkEnemies()   (BotExcel) new target found: " + targetShip.name);
                    // update coord in ship spotted database
                    if (ship.scannedHistory.containsKey(targetShip.scanLetter)) {
                        ship.scannedHistory.remove(targetShip.scanLetter);
                    }
                    ship.scannedHistory.put(targetShip.scanLetter, new TrekCoordHistory(targetShip.scanLetter, targetShip.name, new Trek3DPoint(targetShip.point)));
                    runMacro("}\033l" + targetShip.scanLetter);
                    if (TrekMath.getDistance(ship, targetShip) < ship.scanRange) {
                        runMacro("]");
                    } else {
                        runMacro("\005@4\rx");
                    }
                }

            }
        } else {
            // preparing to transwarp, check for updated coordinates on the target
            if (obsTrackingTarget != null) {
                TrekObserverDevice tod = (TrekObserverDevice) ship.currentQuadrant.getObjectByScanLetter(obsTrackingTarget);
                if (tod != null && tod.isScanning() && tod.scanTarget == targetShip) {
                    // update coord in ship spotted database
                    if (ship.scannedHistory.containsKey(targetShip.scanLetter)) {
                        ship.scannedHistory.remove(targetShip.scanLetter);
                    }
                    ship.scannedHistory.put(targetShip.scanLetter, new TrekCoordHistory(targetShip.scanLetter, targetShip.name, new Trek3DPoint(targetShip.point)));
                    runMacro("}\033l" + targetShip.scanLetter);
                    if (TrekMath.getDistance(ship, targetShip) < ship.scanRange) {
                        runMacro("]cccccccccccccccccccccccccccccccccccccc");  // tie up energy so xwarp fails
                    }
                }
            }
        }
    }

    protected void doOffensiveMovement() {
        if (!ship.doTranswarp)
            super.doOffensiveMovement();
    }
}
