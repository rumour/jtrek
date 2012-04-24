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
 * A handler for Scout class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipScout extends TrekShip {

    public ShipScout(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "SCOUT";
        fullClassName = "Gorn Scout";
        shipType = "scout";

        classLetter = "r";
        damageWarp = 17;
        warpUsageRatio = 2;
        maxCruiseWarp = 13;
        maxTurnWarp = 12;
        warpEnergy = 120;
        impulseEnergy = 60;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 45;
        shieldStrength = 17;
        visibility = 90;
        cloak = false;
        cloakTime = 0;
        cloakPower = 0;
        cloakRegeneration = 0;
        scanRange = 11000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 30;
        minPhaserRange = 700;

        torpedoType = TORPEDO_BOLTPLASMA;
        maxTorpedoBanks = 1;
        maxTorpedoStorage = 35;
        minTorpedoRange = 300;
        maxTorpedoRange = 1100;

        drones = true;
        maxDroneStorage = 20;
        droneStrength = 400;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 2;
        mineStrength = 900;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 180;
        homePlanet = "Gorn";
    }
}