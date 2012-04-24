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
 * A handler for BR-1000 class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipBR1000 extends TrekShip {

    public ShipBR1000(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "BR-1000";
        fullClassName = "Orion BR-1000";
        shipType = "starship";

        classLetter = "i";
        maxCruiseWarp = 14;
        maxTurnWarp = 13;
        warpEnergy = 80;
        impulseEnergy = 60;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 60;
        shieldStrength = 16;
        visibility = 97;
        cloak = false;
        cloakTime = 0;
        cloakPower = 0;
        cloakRegeneration = 0;
        scanRange = 8000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 50;
        minPhaserRange = 1000;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 1;
        maxTorpedoStorage = 50;
        minTorpedoRange = 500;
        maxTorpedoRange = 2200;

        drones = true;
        maxDroneStorage = 8;
        droneStrength = 500;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 2;
        mineStrength = 1000;

        //Dilithium Crystals
        maxCrystalStorage = 40;

        crew = 17;
        homePlanet = "Orion";

        damageWarp = 14;
    }
}