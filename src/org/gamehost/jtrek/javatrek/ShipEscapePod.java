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
 * Time: 7:32:59 AM
 * To change this template use File | Settings | File Templates.
 */
public class ShipEscapePod extends TrekShip {
    protected String oldShipClass = "";

    public ShipEscapePod(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "XP-1";
        fullClassName = "ESCAPE XP-1";
        shipType = "escape pod";

        classLetter = "x";
        damageWarp = 20;
        warpUsageRatio = 1;
        maxCruiseWarp = 17;
        maxTurnWarp = 10;
        warpEnergy = 60;
        impulseEnergy = 45;
        transwarp = false;
        transwarpTracking = false;
        transwarpCounter = 13;

        damagePercent = 80;
        shieldStrength = 5;
        visibility = 100;
        cloak = true;
        cloakTime = 10;
        cloakPower = 60;
        cloakRegeneration = 2;
        scanRange = 8000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 10;
        minPhaserRange = 800;

        torpedoType = TORPEDO_NORMAL;
        maxTorpedoBanks = 1;
        maxTorpedoStorage = 20;
        minTorpedoRange = 0;
        maxTorpedoRange = 1000;

        drones = true;
        maxDroneStorage = 2;
        droneStrength = 400;
        variableSpeed = false;

        mines = false;
        maxMineStorage = 0;
        mineStrength = 0;

        //Dilithium Crystals
        maxCrystalStorage = 0;

        crew = 2;
        homePlanet = "Earth";
    }
}
