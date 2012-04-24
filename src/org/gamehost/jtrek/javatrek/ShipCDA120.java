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

import java.util.Enumeration;
import java.util.Vector;

/**
 * A handler for CDA-120 class ships.
 *
 * @author Joe Hopkinson
 */
public class ShipCDA120 extends TrekShip {

    public ShipCDA120(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "CDA-120";
        fullClassName = "Cardassian CDA-120";
        shipType = "starship";

        classLetter = "m";
        maxCruiseWarp = 14;
        maxTurnWarp = 12;
        warpEnergy = 100;
        impulseEnergy = 50;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 55;
        shieldStrength = 16;
        visibility = 90;
        cloak = false;
        cloakTime = 0;
        cloakPower = 0;
        cloakRegeneration = 0;
        scanRange = 8000;

        phaserType = PHASER_EXPANDINGSPHEREINDUCER;
        maxPhaserBanks = 40;
        minPhaserRange = 500;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 4;
        maxTorpedoStorage = 40;
        minTorpedoRange = 500;
        maxTorpedoRange = 1300;

        drones = true;
        maxDroneStorage = 15;
        droneStrength = 800;
        variableSpeed = true;
        droneSpeed = 12;

        mines = false;
        maxMineStorage = 0;
        mineStrength = 0;

        //Dilithium Crystals
        maxCrystalStorage = 40;

        crew = 74;
        homePlanet = "Cardassia";

        damageWarp = 16;
    }

    public void firePhasers() {
        if (organiaModifier)
            return; // no weapons allowed in organia space

        if (!checkCloak()) {
            return;
        }

        if (phasers <= 0) {
            parent.hud.sendMessage("You must load your phasers before you can fire.");
            return;
        }

        double maxDamage = phasers * 30;
        double percentOfRange = 0;
        double percentOfDamage = 0;

        Vector objectsInRange = currentQuadrant.getAllObjectsInRange(this, this.minPhaserRange);

        for (Enumeration e = objectsInRange.elements(); e.hasMoreElements(); ) {
            TrekObject testRange = (TrekObject) e.nextElement();

            percentOfRange = 100 - (((TrekMath.getDistance(this, testRange)) / this.minPhaserRange) * 100);
            percentOfDamage = (maxDamage * (percentOfRange / 100));

            if (percentOfDamage < 0) {
                percentOfDamage = 0;
            }

            if (this.phaserType == PHASER_EXPANDINGSPHEREINDUCER) {
                //System.out.println(" ESI Distance: " + this.name + "->" + testRange.name + "  = " + TrekMath.getDistance(this, testRange));
                if (TrekMath.getDistance(this, testRange) >= .5) {
                    if (currentQuadrant.getCountShipsInRange(this, this.minPhaserRange) == 0) {
                        // nothing - apply full phaser blast to planet/sb/objects in range ...
                    } else {
                        percentOfDamage = percentOfDamage / currentQuadrant.getCountShipsInRange(this, this.minPhaserRange);
                    }

                    TrekDamageStat stat = testRange.doDamageInstant(this, percentOfDamage, "phasers", true);
                    parent.hud.drawDamageGivenStat(stat);

                } else {
                    percentOfDamage = 0;
                }
            }

        }

        // Return our energy, set some timeouts, etc.
        phaserFireTimeout += phasers;
        //phaserEnergyReturned += phasers;
        phasers = 0;
    }
}
