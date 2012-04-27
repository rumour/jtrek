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
 * PlanetWarring provides for the Beta twin planets (Brach/Taut) which fire warheads at one another
 *
 * @author Jay Ashworth
 */
public class PlanetWarring extends TrekPlanet {
    private TrekObject target = null;

    public PlanetWarring(int x, int y, int z, String name, String scanletter, String codes, String whatRace, int phaserRangeIn, int phaserDamageIn, int fireDelay) {
        super(x, y, z, name, scanletter, codes, whatRace, phaserRangeIn, phaserDamageIn, fireDelay);
    }

    // need to do this outside of the constructor, since the target may not yet have been instantiated
    protected void setEnemy(TrekObject theTarget) {
        target = theTarget;
    }

    protected void doTick() {
        super.doTick();

        int curTorpCount = 0;
        Vector quadObjs = currentQuadrant.getAllObjectsInRange(this, 20000);
        for (Enumeration e = quadObjs.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();
            if (TrekUtilities.isObjectTorpedo(curObj)) {
                TrekTorpedo curTorp = (TrekTorpedo) curObj;
                if (curTorp.owner == this) curTorpCount++;
            }
        }

        // allow warring planet to have up to 3 warheads active
        if (curTorpCount < 3 && target != null) {
            Random gen = new Random();

            // add some random element to torp firing
            if (Math.abs(gen.nextInt() % 240) < 1) {
                // fire a warhead at the enemy
                currentQuadrant.addObject(new TrekTorpedo(this, TrekShip.TORPEDO_ONEHITWARHEAD, target, 8, 1200, 1000, 200));
            }
        }

    }
}
