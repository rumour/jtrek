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
 * A handler for BR-5 class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipBR5 extends TrekShip {

    public ShipBR5(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "BR-5";
        fullClassName = "Orion BR-5";
        shipType = "starship";

        classLetter = "h";
        maxCruiseWarp = 16;
        maxTurnWarp = 14;
        warpEnergy = 110;
        impulseEnergy = 40;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 60;
        shieldStrength = 15;
        visibility = 96;
        cloak = true;
        cloakTime = 90;
        cloakPower = 80;
        cloakRegeneration = 1;
        scanRange = 7500;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 40;
        minPhaserRange = 500;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 2;
        maxTorpedoStorage = 30;
        minTorpedoRange = 500;
        maxTorpedoRange = 1000;

        drones = true;
        maxDroneStorage = 6;
        droneStrength = 400;
        variableSpeed = false;

        mines = false;
        maxMineStorage = 0;
        mineStrength = 0;

        //Dilithium Crystals
        maxCrystalStorage = 40;

        crew = 25;
        homePlanet = "Orion";

        damageWarp = 18;
    }
}