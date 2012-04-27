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
 * A handler for CL-13 class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipCL13 extends TrekShip {

    public ShipCL13(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);
        damageWarp = 13;

        shipClass = "CL-13";
        fullClassName = "Gorn CL-13";
        shipType = "starship";

        classLetter = "j";
        maxCruiseWarp = 11;
        maxTurnWarp = 10;
        maxDamageWarp = 13;
        warpEnergy = 100;
        impulseEnergy = 50;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 50;
        shieldStrength = 18;
        visibility = 95;
        cloak = true;
        cloakTime = 120;
        cloakPower = 50;
        cloakRegeneration = 1;
        scanRange = 7500;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 100;
        minPhaserRange = 1000;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 3;
        maxTorpedoStorage = 30;
        minTorpedoRange = 500;
        maxTorpedoRange = 1000;

        drones = true;
        maxDroneStorage = 10;
        droneStrength = 400;
        variableSpeed = false;

        mines = false;
        maxMineStorage = 0;
        mineStrength = 0;

        //Dilithium Crystals
        maxCrystalStorage = 40;

        crew = 71;
        homePlanet = "Gorn";
    }
}