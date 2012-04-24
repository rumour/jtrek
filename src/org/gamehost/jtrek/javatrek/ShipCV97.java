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
 * A handler for CV-97 class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipCV97 extends TrekShip {

    public ShipCV97(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "CV-97";
        fullClassName = "Gorn CV-97";
        shipType = "starship";

        classLetter = "k";
        damageWarp = 13;
        warpUsageRatio = 3;
        maxCruiseWarp = 11;
        maxTurnWarp = 9;
        warpEnergy = 130;
        impulseEnergy = 80;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 40;
        shieldStrength = 17;
        visibility = 100;
        cloak = true;
        cloakTime = 40;
        cloakPower = 80;
        cloakRegeneration = 2;
        scanRange = 10000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 75;
        minPhaserRange = 600;

        torpedoType = TORPEDO_BOLTPLASMA;
        maxTorpedoBanks = 1;
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

        crew = 235;
        homePlanet = "Gorn";
    }
}