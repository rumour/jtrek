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
 * Time: 8:50:48 PM
 * To change this template use File | Settings | File Templates.
 */
public class BotScout extends BotPlayer {
    public BotScout(TrekServer serverIn, String name) {
        super(serverIn, name);

        shipName = name;
    }

    public void loadInitialBot() {
        super.loadInitialBot();

        ship = TrekUtilities.getShip("r", this);
        ship.setInitialDirection();
        ship.currentQuadrant.addShip(ship);

        quadrantName = ship.currentQuadrant.name;

        setCurObjs();

        this.drawHud(true);

        ship.torpedoType = TrekShip.TORPEDO_BOLTPLASMA;
    }

    public void botTickUpdate() {
        super.botTickUpdate();

        // ship specific tick functions
    }

    public void botSecondUpdate() {
        super.botSecondUpdate();

        // ship specific second functions
    }

    protected void fireWeapons() {
        double tempSpeed = ship.warpSpeed;
        runMacro("\027");

        if (ship.getAvailablePower() >= 85 && ship.torpFireTimeout <= 0 &&
                ship.currentQuadrant.getCountVisibleShipsInRange(ship, 500) > 0) {
            TrekShip plasmaTarget = null;

            Vector nearShips = ship.currentQuadrant.getVisibleShips(ship);
            for (Enumeration e = nearShips.elements(); e.hasMoreElements(); ) {
                TrekShip curShip = (TrekShip) e.nextElement();
                double curDist = TrekMath.getDistance(ship, curShip);

                // skip self
                if (curShip == ship) continue;

                // skip ships over 500 away
                if (curDist > 500) continue;

                // skip peaced bots
                if (curShip.parent instanceof BotPlayer) {
                    BotPlayer curBot = (BotPlayer) curShip.parent;
                    if (curBot.botPeace && curBot.teamNumber == this.teamNumber) continue;
                }

                plasmaTarget = curShip;
            }

            // 1 in 3 chance of spitting out a plasma
            boolean doFire = Math.abs(gen.nextInt() % 3) < 1 ? true : false;

            if (plasmaTarget != null && doFire) {
                runMacro("\033\0240o" + plasmaTarget.scanLetter + "\014Tt\024\033\0241");
            }
        }

        // restore speed
        runMacro("@" + tempSpeed + "\r");

        super.fireWeapons();
    }

    protected void doOffensiveMovement() {
        super.doOffensiveMovement();

        // check for any plasma to intercept, if ships are near
        if (ship.currentQuadrant.getCountVisibleShipsInRange(ship, 350) > 0) {
            boolean doIntPlasma = false;
            TrekTorpedo curTorp = null;

            Vector nearObjs = ship.currentQuadrant.getAllObjectsInRange(ship, 500);
            for (Enumeration e = nearObjs.elements(); e.hasMoreElements(); ) {
                TrekObject curObj = (TrekObject) e.nextElement();
                if (TrekUtilities.isObjectTorpedo(curObj)) {
                    curTorp = (TrekTorpedo) curObj;
                    if ((TrekShip) (curTorp.owner) == ship) {
                        doIntPlasma = true;
                        break;
                    }
                }
            }

            if (doIntPlasma) {
                TrekObject preserveScan = ship.scanTarget;
                runMacro("O" + curTorp.scanLetter + "}ii]");
                if (TrekUtilities.isObjectShip(preserveScan)) {
                    runMacro("o");
                } else {
                    runMacro("O");
                }
                runMacro(preserveScan.scanLetter);
            }
        }

        // check for launching plasma at cloaked ships
        if (ship.intCoordPoint != null && ship.point != null && TrekMath.getDistance(ship.intCoordPoint, ship.point) < 800 &&
                TrekMath.getDistance(ship.intCoordPoint, ship.point) > 375) {
            double saveSpeed = ship.warpSpeed;
            runMacro("\005");

            // see if we already have an 'outstanding torp'
            if (plasmaResult == NONE || plasmaResult == HIT) {
                runMacro("\033\0240Tt\024\033\0241");
            }

            runMacro("@" + saveSpeed + "\r");

        }
    }
}
