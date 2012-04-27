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

import java.util.Enumeration;
import java.util.Vector;

/**
 * A handler for Galor class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipGalor extends TrekShip {
    int mineRegenTick = 0;

    public ShipGalor(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);
        warpUsageRatio = 1;
        shipClass = "GALOR";
        fullClassName = "Cardassian Galor";
        shipType = "starship";

        classLetter = "p";
        maxCruiseWarp = 13;
        maxTurnWarp = 12;
        warpEnergy = 100;
        impulseEnergy = 50;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 40;
        shieldStrength = 17;
        visibility = 95;
        cloak = false;
        cloakTime = 0;
        cloakPower = 0;
        cloakRegeneration = 0;
        scanRange = 13000;

        phaserType = PHASER_EXPANDINGSPHEREINDUCER;
        maxPhaserBanks = 50;
        minPhaserRange = 500;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 5;
        maxTorpedoStorage = 40;
        minTorpedoRange = 500;
        maxTorpedoRange = 1450;

        drones = true;
        maxDroneStorage = 20;
        droneStrength = 800;
        variableSpeed = true;
        droneSpeed = 12;

        mines = true;
        maxMineStorage = 1;
        mineStrength = 1000;

        //Dilithium Crystals
        maxCrystalStorage = 40;

        crew = 180;
        homePlanet = "Cardassia";

        damageWarp = 17;
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
        //		phaserEnergyReturned += phasers;
        phasers = 0;
    }

    public void doClassSpecificTick() {
        mineRegenTick++;

        if ((mineRegenTick % 120) == 0) {  // regen a mine every 30 seconds
            mineRegenTick = 0;
            if (this.mineCount != 1) mineCount = 1;
        }

    }

    public void dropMine() {
        if (organiaModifier)
            return; // no weapons allowed in organia space

        if (saveTimeout > 0) {
            parent.hud.sendMessage("You must wait " + saveTimeout + " second(s) to drop a mine.");
            return;
        }

        if (this.mineCount <= 0) {
            parent.hud.sendMessage("You have no mines.");
            return;
        }

        if (mineFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + mineFireTimeout + " second(s) to drop a mine.");
            return;
        }

        currentQuadrant.addObject(new TrekMine(this));

        minesDropped++;
        mineCount -= 1;
        mineRegenTick = 0;

        if (mineCount <= 0) {
            mineCount = 0;
        }

        mineFireTimeout = 10;
    }

}
