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
package org.gamehost.jtrek.javatrek.bot;

import org.gamehost.jtrek.javatrek.*;

import java.util.Enumeration;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Apr 8, 2004
 * Time: 8:48:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class BotDY extends BotPlayer {
    public BotDY(TrekServer serverIn, String name) {
        super(serverIn, name);

        shipName = name;
    }

    public void loadInitialBot() {
        super.loadInitialBot();

        ship = TrekUtilities.getShip("d", this);
        ship.setInitialDirection();
        ship.currentQuadrant.addShip(ship);

        quadrantName = ship.currentQuadrant.name;

        setCurObjs();

        this.drawHud(true);
    }

    public void botTickUpdate() {
        if (ship == null) return;

        if (ship.doTranswarp && ship.transwarpCounter <= 1) {
            // make sure we're set to warp
            runMacro("\005@3\r");
        } else {
            super.botTickUpdate();
        }

        // ship specific tick functions
    }

    public void botSecondUpdate() {
        if (ship == null) return;

        if (ship.doTranswarp && ship.transwarpCounter <= 1) {
            // make sure we're set to warp
            runMacro("\005@3\r");
        } else {
            super.botSecondUpdate();
        }

        // ship specific second functions
    }

    protected void doDefensiveMovement() {
        if (ship.checkPhaserCool()) {
            double nearest = ship.minPhaserRange;
            TrekObject nearObj = null;
            Vector shipsInRange = ship.currentQuadrant.getAllVisibleShipsInRange(ship, (int) nearest);

            for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
                TrekShip curShip = (TrekShip) e.nextElement();
                double curDist = TrekMath.getDistance(ship, curShip);
                if (curDist < nearest) {
                    nearObj = curShip;
                    nearest = curDist;
                }
            }

            Vector objsInRange = ship.currentQuadrant.getAllObjectsInRange(ship, (int) nearest);

            for (Enumeration e = objsInRange.elements(); e.hasMoreElements(); ) {
                TrekObject curObj = (TrekObject) e.nextElement();
                double curDist = TrekMath.getDistance(ship, curObj);
                if (curDist < nearest) {
                    nearObj = curObj;
                    nearest = curDist;
                }
            }

            if (nearObj != null) { // teleport away from near ship or object when doing defensive movement
                TrekObject preserveScan = ship.scanTarget;

                runMacro("\033\0201PP\033\016");
                if (nearObj instanceof TrekShip) {
                    runMacro("o");
                } else {
                    runMacro("O");
                }
                runMacro(nearObj.scanLetter + "\014p\020\020");

                ship.scanTarget = preserveScan;
            }
        }

        super.doDefensiveMovement();
    }

    protected void doEscapeMovement() {
        if (!ship.doTranswarp) {
            int rndHdng = Math.abs(gen.nextInt() % 360);
            int rndPitch = gen.nextInt() % 90;

            runMacro("\005@4\rx!" + rndHdng + "'" + rndPitch + "\r");
            if (!ship.doTranswarp) {  // not enough energy, or something!
                super.doEscapeMovement();
            }
        } else {
            runMacro("\027@4\r");
        }
    }

    protected void fireWeapons() {
        if (!ship.doTranswarp) {
            super.fireWeapons();
        }
    }
}
