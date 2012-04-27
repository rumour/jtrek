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
 * A handler for Interceptor class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipInterceptor extends TrekShip {

    public ShipInterceptor(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "INTERCEPTOR";
        fullClassName = "Romulan Interceptor";
        shipType = "scout";

        classLetter = "s";
        damageWarp = 17;
        maxCruiseWarp = 15;
        maxTurnWarp = 14;
        warpEnergy = 80;
        impulseEnergy = 50;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 50;
        shieldStrength = 16;
        visibility = 98;
        cloak = true;
        cloakTime = 75;
        cloakPower = 75;
        cloakRegeneration = 2;
        scanRange = 8000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 35;
        minPhaserRange = 1000;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 3;
        maxTorpedoStorage = 30;
        minTorpedoRange = 500;
        maxTorpedoRange = 1300;

        drones = false;
        maxDroneStorage = 0;
        droneStrength = 0;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 4;
        mineStrength = 900;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 25;
        homePlanet = "Romulus";
    }
}