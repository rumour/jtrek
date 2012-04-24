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
 * A handler for II-A class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipConstitutionIIA extends TrekShip {

    public ShipConstitutionIIA(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);
        shipClass = "II-A";
        fullClassName = "Constitution II-A";
        shipType = "starship";

        classLetter = "a";

        // Warp.
        maxCruiseWarp = 14;
        maxTurnWarp = 9;
        damageWarp = 18;
        warpUsageRatio = 2;

        // Energy.
        warpEnergy = 50;
        impulseEnergy = 40;

        transwarp = false;
        transwarpTracking = false;
        damagePercent = 45;
        shieldStrength = 17;
        visibility = 100;

        cloak = false;
        cloakTime = 90;
        cloakPower = 80;
        cloakRegeneration = 2;

        scanRange = 13000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 50;
        minPhaserRange = 700;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 4;
        maxTorpedoStorage = 40;
        minTorpedoRange = 500;
        maxTorpedoRange = 1800;

        drones = true;
        maxDroneStorage = 10;
        droneStrength = 400;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 8;
        mineStrength = 900;

        //Dilithium Crystals
        maxCrystalStorage = 80;

        crew = 485;
        homePlanet = "Earth";
    }
}