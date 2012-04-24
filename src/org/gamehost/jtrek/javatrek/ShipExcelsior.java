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
 * A handler for EXCEL class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipExcelsior extends TrekShip {

    public ShipExcelsior(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "EXCEL";
        fullClassName = "Excelsior";
        shipType = "starship";

        classLetter = "b";
        damageWarp = 17;
        warpUsageRatio = 2;
        maxCruiseWarp = 12;
        maxTurnWarp = 10;
        warpEnergy = 130;
        impulseEnergy = 50;
        transwarp = false;
        transwarpTracking = true;
        transwarpCounter = 13;
        damagePercent = 45;
        shieldStrength = 17;
        visibility = 100;
        cloak = false;
        cloakTime = 0;
        cloakPower = 0;
        cloakRegeneration = 0;
        scanRange = 10000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 65;
        minPhaserRange = 600;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 6;
        maxTorpedoStorage = 54;
        minTorpedoRange = 500;
        maxTorpedoRange = 1200;

        drones = true;
        maxDroneStorage = 4;
        droneStrength = 400;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 5;
        mineStrength = 900;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 565;
        homePlanet = "Earth";
    }
}