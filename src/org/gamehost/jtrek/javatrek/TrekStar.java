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
 * Represents a star in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekStar extends TrekObject {
    public TrekStar(int x, int y, int z, String name, String scanletter) {
        super(name, scanletter, x, y, z);
        type = OBJ_STAR;
    }

    protected void doTick(Vector ships) {
        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip ship = (TrekShip) e.nextElement();
            ship.starTarget = this;
        }
    }

    protected void doTick() {
        Vector ships = currentQuadrant.getAllShipsInRange(this, 100);

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip ship = (TrekShip) e.nextElement();

            // check to see if within effect range
            if (TrekMath.getDistance(ship, this) <= 20) {
                // randomly push the ship out 300-500 units
                Random starRnd = new Random();

                int randomHeading = Math.abs(starRnd.nextInt() % 360); //
                int randomPitch = starRnd.nextInt() % 90;
                int randomDistance = Math.abs(starRnd.nextInt() % 200) + 300; // make distance 300 to 500 units from star

                Trek3DPoint target = new Trek3DPoint();
                randomPitch = 90 - randomPitch;
                // calculate end points
                target.x = point.x + (float) (randomDistance * Math.sin(Math.toRadians(randomPitch)) * Math.sin(Math.toRadians(randomHeading)));
                target.y = point.y + (float) (randomDistance * Math.sin(Math.toRadians(randomPitch)) * Math.cos(Math.toRadians(randomHeading)));
                target.z = point.z + (float) (randomDistance * Math.cos(Math.toRadians(randomPitch)));

                ship.point = target;
            }
        }
    }
}