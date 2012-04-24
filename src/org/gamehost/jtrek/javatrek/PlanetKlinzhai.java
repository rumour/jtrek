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

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Apr 2, 2005
 * Time: 8:17:57 AM
 * To change this template use File | Settings | File Templates.
 */
public class PlanetKlinzhai extends TrekPlanet implements IPlanetProvidesShipUpgrade {
    protected String upgradeClass = "A"; // A = vorcha
    protected String orbitMessage = "Lock weapons on planet and press $ to upgrade to VORCHA.  Cost is 5000.";
    protected int orbitDuration = 30;
    protected int upgradeCost = 5000;

    public PlanetKlinzhai(int x, int y, int z, String name, String scanletter, String codes, String whatRace, int phaserRangeIn, int phaserDamageIn, int fireDelay) {
        super(x, y, z, name, scanletter, codes, whatRace, phaserRangeIn, phaserDamageIn, fireDelay);
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
        if (curShip instanceof ShipKEV12) {
            return true;
        }

        return false;
    }

    public String getUpgradeClass() {
        return upgradeClass;
    }
}
