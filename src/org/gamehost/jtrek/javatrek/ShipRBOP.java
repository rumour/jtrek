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
 * A handler for RBOP class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipRBOP extends TrekShip {

    public ShipRBOP(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "RBOP";
        fullClassName = "Romulan Bird of Prey";
        shipType = "starship";

        classLetter = "e";
        damageWarp = 18;
        maxCruiseWarp = 14;
        maxTurnWarp = 12;
        warpEnergy = 80;
        impulseEnergy = 50;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 50;
        shieldStrength = 18;
        visibility = 95;
        cloak = true;
        cloakTime = 90;
        cloakPower = 80;
        cloakRegeneration = 2;
        scanRange = 8000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 25;
        minPhaserRange = 500;

        torpedoType = TORPEDO_PLASMA;
        maxTorpedoBanks = 1;
        maxTorpedoStorage = 15;
        minTorpedoRange = 500;
        maxTorpedoRange = 2000;

        drones = false;
        maxDroneStorage = 0;
        droneStrength = 0;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 6;
        mineStrength = 900;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 12;
        homePlanet = "Romulus";
    }
}