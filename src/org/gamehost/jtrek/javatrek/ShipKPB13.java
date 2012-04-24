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
 * A handler for KPB-13 class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipKPB13 extends TrekShip {
    protected int torpedoRegen;

    public ShipKPB13(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "KPB-13";
        fullClassName = "Klingon KPB-13";
        shipType = "starship";

        classLetter = "g";
        damageWarp = 17;
        warpUsageRatio = 2;
        maxCruiseWarp = 13;
        maxTurnWarp = 10;
        warpEnergy = 120;
        impulseEnergy = 60;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 40;
        shieldStrength = 17;
        visibility = 100;
        cloak = false;
        cloakTime = 0;
        cloakPower = 0;
        cloakRegeneration = 0;
        scanRange = 8000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 35;
        minPhaserRange = 800;

        torpedoType = TORPEDO_OBLITERATOR;
        maxTorpedoBanks = 5;
        maxTorpedoStorage = 50;
        minTorpedoRange = 300;
        maxTorpedoRange = 1200;

        drones = true;
        maxDroneStorage = 20;
        droneStrength = 400;
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