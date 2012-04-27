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
 * A handler for II-A class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipDefiant extends TrekShip {

    public ShipDefiant(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);
        shipClass = "DEFIANT";
        fullClassName = "Defiant";
        shipType = "starship";

        classLetter = "t";

        // Warp.
        maxCruiseWarp = 12;
        maxTurnWarp = 11;
        damageWarp = 17;
        warpUsageRatio = 2;

        // Energy.
        warpEnergy = 120;
        impulseEnergy = 50;

        transwarp = false;
        transwarpTracking = false;
        damagePercent = 40;
        shieldStrength = 17;
        visibility = 90;

        cloak = true;
        cloakTime = 75;
        cloakPower = 90;
        cloakRegeneration = 2;

        scanRange = 13000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 60;
        minPhaserRange = 800;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 5;
        maxTorpedoStorage = 45;
        minTorpedoRange = 600;
        maxTorpedoRange = 1700;

        drones = true;
        maxDroneStorage = 20;
        droneStrength = 400;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 8;
        mineStrength = 1000;

        //Dilithium Crystals
        maxCrystalStorage = 20;

        crew = 185;
        homePlanet = "Earth";
    }
}