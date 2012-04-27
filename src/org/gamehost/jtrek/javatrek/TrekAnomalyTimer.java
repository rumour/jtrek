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

import java.util.Random;
import java.util.TimerTask;

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Feb 2, 2005
 * Time: 7:49:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrekAnomalyTimer extends TimerTask {
    TrekSpatialAnomaly anomaly;

    public TrekAnomalyTimer(TrekSpatialAnomaly anom) {
        anomaly = anom;
    }

    public void run() {
        Random newPoint = new Random();

        anomaly.point.x = newPoint.nextInt() % 100000;
        anomaly.point.y = newPoint.nextInt() % 100000;
        anomaly.point.z = newPoint.nextInt() % 100000;

        anomaly.currentQuadrant.addObject(anomaly);
        TrekLog.logMessage("Activating an anomaly in " + anomaly.currentQuadrant.name + " at " + anomaly.point.toString() + ".");
        TrekServer.sendMsgToAllPlayersInQuadrant("An anomaly has been detected at " + anomaly.point + ".", anomaly, anomaly.currentQuadrant, false, false);
    }

}
