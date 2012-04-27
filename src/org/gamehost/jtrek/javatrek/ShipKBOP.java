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

/**
 * A handler for KBOP class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipKBOP extends TrekShip {
    protected int torpedoRegen;

    public ShipKBOP(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "KBOP";
        fullClassName = "Klingon Bird of Prey";
        shipType = "starship";

        classLetter = "o";
        damageWarp = 16;
        warpUsageRatio = 2;
        maxCruiseWarp = 10;
        maxTurnWarp = 9;
        warpEnergy = 120;
        impulseEnergy = 60;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 45;
        shieldStrength = 17;
        visibility = 95;
        cloak = true;
        cloakTime = 60;
        cloakPower = 50;
        cloakRegeneration = 1;
        scanRange = 13000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 60;
        minPhaserRange = 1000;

        torpedoType = TORPEDO_OBLITERATOR;
        maxTorpedoBanks = 1;
        maxTorpedoStorage = 75;
        minTorpedoRange = 0;
        maxTorpedoRange = 2200;

        drones = true;
        maxDroneStorage = 20;
        droneStrength = 500;
        variableSpeed = false;

        mines = false;
        maxMineStorage = 0;
        mineStrength = 0;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 180;
        homePlanet = "Klinzhai";
        torpedoRegen = 0;
    }

    public void doShipSecondUpdate() {
        super.doShipSecondUpdate();

        torpedoRegen++;

        // Regenerate klingon torps.
        if ((torpedoCount < maxTorpedoStorage) && (torpedoRegen % 2 == 0)) {
            torpedoCount++;
        }
    }
}