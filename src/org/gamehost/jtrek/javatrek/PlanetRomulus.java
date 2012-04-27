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
import java.util.Hashtable;
import java.util.Vector;

/**
 * Represents planet Romulus in the game.
 *
 * @author Joe Hopkinson
 */
public class PlanetRomulus extends TrekPlanet implements IPlanetProvidesShipUpgrade {
    protected Hashtable infidels;
    protected String upgradeClass = "z"; // z = valdore
    protected String orbitMessage = "Lock weapons on planet and press $ to upgrade to VALDORE.  Cost is 5000.";
    protected int orbitDuration = 30;
    protected int upgradeCost = 5000;

    public PlanetRomulus(int x, int y, int z, String name, String scanletter, String codes, String whatRace, int phaserRangeIn, int phaserDamageIn, int fireTimeoutIn) {
        super(x, y, z, name, scanletter, codes, whatRace, phaserRangeIn, phaserDamageIn, fireTimeoutIn);
        infidels = new Hashtable();
    }

    protected void doTick() {
        super.doTick();


        Vector shipsInRange = currentQuadrant.getAllShipsInRange(this, 10000);

        for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
            TrekShip infidel = (TrekShip) e.nextElement();

            if (!infidel.homePlanet.equals(name)) {
                brandTheInfidel(infidel);
            }
        }
    }

    private void brandTheInfidel(TrekShip thisShip) {
        double distance = TrekMath.getDistance(this, thisShip);

        if (distance > 7500) {
            if (infidels.containsKey(thisShip.name))
                infidels.remove(thisShip.name);

            return;
        }

        if (!infidels.containsKey(thisShip.name)) {
            infidels.put(thisShip.name, "0");
        }

        if (infidels.get(thisShip.name).equals("1"))
            return;

        if (distance < 2500) {
            if (!infidels.get(thisShip.name).equals("1")) {
                TrekServer.sendMsgToRace(thisShip.name + ", ship [" + thisShip.scanLetter + "] has entered Romulan Space.", this, "Romulus", true, false);
                TrekServer.sendMsgToPlayer(thisShip.scanLetter, "You have entered Romulan space.", this, false, false);
                infidels.put(thisShip.name, "1");
            }
            return;
        }

        if (distance < 7500) {
            if (!infidels.get(thisShip.name).equals("2")) {
                TrekServer.sendMsgToRace(thisShip.name + ", ship [" + thisShip.scanLetter + "] has entered the Romulan Neutral Zone.", this, "Romulus", true, false);
                TrekServer.sendMsgToPlayer(thisShip.scanLetter, "You have entered the Romulan Neutral Zone.", this, false, false);
                infidels.put(thisShip.name, "2");
            }

            return;
        }


    }


    public String getUpgradeClass() {
        return upgradeClass;
    }

    public String getOrbitMessage() {
        return orbitMessage;
    }

    public int getOrbitDuration() {
        return orbitDuration;
    }

    public int getUpgradeCost() {
        return upgradeCost;
    }

    public boolean isUpgradeAvailable(TrekShip curShip) {
        if (curShip instanceof ShipRBOP) {
            return true;
        }

        return false;
    }
}
