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
import java.util.Vector;

/**
 * Represents Planet Megadon in the game.
 *
 * @author Joe Hopkinson
 */
public class PlanetMegadon extends TrekPlanet {
    private int megadonTick = 0;
    private boolean doOnce = true;
    Vector infidels;
    private int weaponZoneRange = 500;
    private String entryMsg = "Megadon Defense Zone.  Planetary defenses activated.";
    private String exitMsg = "Leaving Megadon Defense Zone.";

    public PlanetMegadon(int x, int y, int z, String name, String scanletter, String codes, String whatRace, int phaserRangeIn, int phaserDamageIn, int fireTimeoutIn) {
        super(x, y, z, name, scanletter, codes, whatRace, phaserRangeIn, phaserDamageIn, fireTimeoutIn);
    }

    protected void doTick() {
        super.doTick();

        if (doOnce) {
            if (this.currentQuadrant.name.equals("Delta Quadrant")) {
                weaponZoneRange = 1500;
                entryMsg = "Borg Homeworld.  Non-assimilated vessel detected.  Defenses activated.";
                exitMsg = "Leaving the Borg Homeworld defense zone.";
            }

            doOnce = false;
        }

        megadonTick++;

        Vector shipsInRange = currentQuadrant.getAllShipsInRange(this, weaponZoneRange + 200);

        for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
            TrekShip targetShip = (TrekShip) e.nextElement();

            // Megadon has entered into an uneasy alliance with the borg
            if (targetShip.name.equals("borg")) {
                continue;
            }

            if (TrekMath.getDistance(this, targetShip) <= weaponZoneRange) {
                if (targetShip.planetTarget != this) {
                    targetShip.parent.hud.sendMessage(entryMsg);
                    targetShip.planetTarget = this;
                }
            } else {
                if (targetShip.planetTarget == this) {
                    targetShip.parent.hud.sendMessage(exitMsg);
                    targetShip.planetTarget = null;
                }
            }

            if ((megadonTick % 20) == 0) {
                megadonTick = 0;

                if (TrekMath.getDistance(this, targetShip) <= weaponZoneRange)
                    currentQuadrant.addObject(new TrekTorpedo(this, TrekShip.TORPEDO_WARHEAD, targetShip));
            }
        }

    }

}
