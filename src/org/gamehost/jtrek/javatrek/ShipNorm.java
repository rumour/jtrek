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
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Mar 7, 2005
 * Time: 7:42:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class ShipNorm extends TrekShip {
    public ShipNorm(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "AVG IX";
        fullClassName = "BORG AVG IX";
        shipType = "starship";

        classLetter = "y";
        damageWarp = 16;
        warpUsageRatio = 2;
        maxCruiseWarp = 13;
        maxTurnWarp = 11;
        warpEnergy = 110;
        impulseEnergy = 50;
        transwarp = false;
        transwarpTracking = false;
        transwarpCounter = 13;

        damagePercent = 45;
        shieldStrength = 16;
        visibility = 98;
        cloak = true;
        cloakTime = 35;
        cloakPower = 30;
        cloakRegeneration = 2;
        scanRange = 9500;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 50;
        minPhaserRange = 600;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 3;
        maxTorpedoStorage = 54;
        minTorpedoRange = 450;
        maxTorpedoRange = 1450;

        drones = true;
        maxDroneStorage = 12;
        droneStrength = 400;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 3;
        mineStrength = 500;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 25;
        homePlanet = "Borg";
    }
}
