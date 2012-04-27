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
 * A handler for RBOP class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipValdore extends TrekShip {

    public ShipValdore(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "VALDORE";
        fullClassName = "Romulan Valdore";
        shipType = "starship";

        classLetter = "z";
        damageWarp = 16;
        maxCruiseWarp = 11;
        maxTurnWarp = 10;
        warpEnergy = 140;
        impulseEnergy = 80;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 38;
        shieldStrength = 18;
        visibility = 98;
        cloak = true;
        cloakTime = 90;
        cloakPower = 85;
        cloakRegeneration = 3;
        scanRange = 11000;
        warpUsageRatio = 2;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 80;
        minPhaserRange = 800;

        torpedoType = TORPEDO_PLASMA;
        maxTorpedoBanks = 2;
        maxTorpedoStorage = 30;
        minTorpedoRange = 500;
        maxTorpedoRange = 2000;

        drones = false;
        maxDroneStorage = 0;
        droneStrength = 0;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 8;
        mineStrength = 900;

        //Dilithium Crystals
        maxCrystalStorage = 0;

        crew = 126;
        homePlanet = "Romulus";
    }
}