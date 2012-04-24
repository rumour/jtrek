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
import java.util.TimerTask;
import java.util.Vector;

/**
 * A timer task setup to run every 250 milliseconds.  Calls TrekServer.doTick();
 *
 * @author Joe Hopkinson
 */
public class TrekTickTimerTask extends TimerTask {
    static int tickCount = 0;
    static int totalSeconds = 0;

    public TrekTickTimerTask() {

    }

    /* (non-Javadoc)
      * @see java.util.TimerTask#run()
      */
    public void run() {
        try {
            doTick();
        } catch (NullPointerException npe) {
            TrekLog.logException(npe);
        }
    }

    protected static void doTick() {
        tickCount++;

        updatePointLocations();
        doTickUpdate();

        // Do the second update... same as above.
        if (tickCount >= 4) {
            doSecondUpdate();
            tickCount = 0;
        }
    }

    private static void doTickUpdate() {

        for (Enumeration e = TrekServer.players.elements(); e.hasMoreElements(); ) {
            TrekPlayer activePlayer = (TrekPlayer) e.nextElement();

            if (activePlayer.state == TrekPlayer.WAIT_PLAYING) {
                activePlayer.doTickUpdate();
            }
        }
    }

    private static void doSecondUpdate() {
        totalSeconds++;
        int count = 0;

        if ((totalSeconds % 60) == 0) { // 1 minute tick
            // check all ships -- see if any are at 0,0,0 -- if so, set them to 1,1,1 (try to avoid negaverse bugs)
            for (Enumeration e = TrekServer.quadrants.elements(); e.hasMoreElements(); ) {
                TrekQuadrant q = (TrekQuadrant) e.nextElement();
                //System.out.println("*** On quadrant: " + q.name);
                Vector quadShips = q.getAllShips();
                //System.out.println("*** Contains " + quadShips.size() + " ships.");
                for (Enumeration e2 = quadShips.elements(); e2.hasMoreElements(); ) {
                    count++;
                    //System.out.println("*** On ship: " + count);
                    TrekShip curShip = (TrekShip) e2.nextElement();
                    //System.out.println("*** Ship name: " + curShip.name);
                    //if (curShip.point != null) System.out.println("*** Ship location: " + curShip.point.getXString() + ", " + curShip.point.getYString() + ", " + curShip.point.getZString());
                    if (curShip.point == null ||
                            (curShip.point.getXInt() == 0 && curShip.point.getYInt() == 0 && curShip.point.getZInt() == 0)) {
                        TrekLog.logMessage("Negaverse Check has found stranded ship: " + curShip.name);
                        curShip.point = new Trek3DPoint(1, 1, 1);
                    }
                }
            }
        }

        if ((totalSeconds % 300) == 0) { // 5 minutes tick
            if (TrekServer.isTeamPlayEnabled()) {
                TrekTeamStats team1 = TrekServer.teamStats[1];
                TrekTeamStats team2 = TrekServer.teamStats[2];
                if (team1.getGold() > team2.getGold()) { // announce winning team first
                    TrekServer.sendAnnouncement("Team 1: gold = " + team1.getGold() + " dmggvn = " + team1.getDamageGiven() +
                            " bonus = " + team1.getBonus() + " dmgrcvd = " + team1.getDamageReceived() + " conflicts = " +
                            team1.getConflicts(), false);
                    TrekServer.sendAnnouncement("Team 2: gold = " + team2.getGold() + " dmggvn = " + team2.getDamageGiven() +
                            " bonus = " + team2.getBonus() + " dmgrcvd = " + team2.getDamageReceived() + " conflicts = " +
                            team2.getConflicts(), false);
                } else {
                    TrekServer.sendAnnouncement("Team 2: gold = " + team2.getGold() + " dmggvn = " + team2.getDamageGiven() +
                            " bonus = " + team2.getBonus() + " dmgrcvd = " + team2.getDamageReceived() + " conflicts = " +
                            team2.getConflicts(), false);
                    TrekServer.sendAnnouncement("Team 1: gold = " + team1.getGold() + " dmggvn = " + team1.getDamageGiven() +
                            " bonus = " + team1.getBonus() + " dmgrcvd = " + team1.getDamageReceived() + " conflicts = " +
                            team1.getConflicts(), false);
                }
            }
        }

        if ((totalSeconds % TrekServer.getBotRespawnTime()) == 0) {
            TrekServer.launchBots();
        }

        if ((totalSeconds % 3600) == 0) {
            TrekLog.logMessage("Hourly tick.");
            totalSeconds = 0;
            if (TrekServer.isThxEnabled()) {
                TrekServer.launchBot("BotTHX");
            }
        }

        for (Enumeration e = TrekServer.players.elements(); e.hasMoreElements(); ) {
            TrekPlayer activePlayer = (TrekPlayer) e.nextElement();

            if (activePlayer.state == TrekPlayer.WAIT_PLAYING) {
                activePlayer.doSecondUpdate();
            }
        }
    }

    private static void updatePointLocations() {
        for (Enumeration e = TrekServer.quadrants.elements(); e.hasMoreElements(); ) {
            TrekQuadrant target = (TrekQuadrant) e.nextElement();
            target.updatePointLocations();
        }
    }

}
