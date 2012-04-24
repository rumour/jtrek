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
 * A handler for KEV-12 class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipKEV12 extends TrekShip {
    protected int torpedoRegen;

    public ShipKEV12(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "KEV-12";
        fullClassName = "Klingon KEV-12";
        shipType = "starship";

        classLetter = "f";
        damageWarp = 12;
        warpUsageRatio = 2;
        maxCruiseWarp = 10;
        maxTurnWarp = 8;
        warpEnergy = 120;
        impulseEnergy = 60;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 40;
        shieldStrength = 17;
        visibility = 100;

        cloak = true;
        cloakTime = 50;
        cloakPower = 50;
        cloakRegeneration = 1;

        scanRange = 13000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 60;
        minPhaserRange = 1000;

        torpedoType = TORPEDO_OBLITERATOR;
        maxTorpedoBanks = 8;
        maxTorpedoStorage = 60;
        minTorpedoRange = 500;
        maxTorpedoRange = 1200;

        drones = true;
        maxDroneStorage = 15;
        droneStrength = 600;
        variableSpeed = false;

        mines = false;
        maxMineStorage = 0;
        mineStrength = 0;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 150;
        homePlanet = "Klinzhai";
        torpedoRegen = 0;
    }

    public void doShipSecondUpdate() {
        super.doShipSecondUpdate();

        torpedoRegen++;

        // Regenerate klingon torps.
        if ((torpedoCount + torpedoes) < maxTorpedoStorage && torpedoRegen >= 2) {
            torpedoCount++;
        }

        if (torpedoRegen >= 2) {
            torpedoRegen = 0;
        }
    }
}