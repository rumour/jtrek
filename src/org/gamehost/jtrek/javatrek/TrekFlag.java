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
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Feb 11, 2004
 * Time: 9:49:55 PM
 * To change this template use Options | File Templates.
 */
public class TrekFlag {
    private TrekObject flagObject = null;
    private static final int zoneRange = 3000;
    private static final int maxPayoutTime = 720;
    private int payoutCountdown = maxPayoutTime; // payoff every 720 ticks (3 minutes)


    public TrekFlag(TrekObject host) {
        flagObject = host;
    }

    public TrekFlag(String quadrant, String objLetter) {
        TrekQuadrant flagQuad = TrekServer.getQuadrantByName(quadrant);
        if (flagQuad != null) {
            flagObject = flagQuad.getObjectByScanLetter(objLetter);
        }
    }

    protected void doTick() {
        int shipsInRange = flagObject.currentQuadrant.getCountShipsInRange(flagObject, TrekFlag.zoneRange);
        if (shipsInRange > 0) {
            boolean flagOwned = true;
            int shipCount = 0;
            int teamNum = 0;
            Vector ships = flagObject.currentQuadrant.getAllShipsInRange(flagObject, TrekFlag.zoneRange);
            for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
                TrekShip curShip = (TrekShip) e.nextElement();

                if (curShip.parent.teamNumber == 0) continue;  // non-team player (perhaps observing admin ship)

                if (shipCount == 0) {
                    teamNum = curShip.parent.teamNumber;
                    shipCount++;
                } else {
                    if (curShip.parent.teamNumber != teamNum) {
                        flagOwned = false;
                        break;
                    }
                }
            }

            if (flagOwned && teamNum != 0) {
                payoutCountdown--;
            } else {
                if (!flagOwned)
                    payoutCountdown++;
                if (payoutCountdown > maxPayoutTime) resetCountdown();
            }

            if (flagOwned && (payoutCountdown % 240 == 0 || payoutCountdown == 120 || payoutCountdown == 40)) {
                if (payoutCountdown == maxPayoutTime) {
                    // nothing; we don't want it to print a message after every reset
                } else {
                    if (payoutCountdown == 0) {
                        TrekServer.sendAnnouncement(flagObject.name + " has paid out for team " + teamNum + "!  Counter reset to 3 minutes.", false);
                    } else {
                        TrekServer.sendAnnouncement("Team " + teamNum + " has control of " + flagObject.name + " - payout in " + payoutCountdown / 4 + " seconds.", true);
                    }
                }
            }

            if (payoutCountdown <= 0) {
                Random gen = new Random();
                int goldChunks = Math.abs(gen.nextInt() % 5) + 1;
                for (int x = 0; x < goldChunks; x++) {
                    flagObject.currentQuadrant.addObject(new TrekGold(Math.abs(gen.nextInt() % 900) + 100, flagObject,
                            (gen.nextInt() % 3000) + flagObject.point.x, (gen.nextInt() % 3000) + flagObject.point.y,
                            (gen.nextInt() % 3000) + flagObject.point.z));
                }
                resetCountdown();
            }
        }
    }

    private void resetCountdown() {
        payoutCountdown = maxPayoutTime;
    }

    protected TrekObject getFlagObject() {
        return flagObject;
    }

    protected int getPayoutSeconds() {
        return (payoutCountdown / 4);
    }
}
