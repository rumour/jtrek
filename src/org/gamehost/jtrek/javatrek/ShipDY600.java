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
 * A handler for DY-600 class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipDY600 extends TrekShip {

    public ShipDY600(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "DY-600";
        fullClassName = "DY-600";
        shipType = "freighter";

        classLetter = "d";
        damageWarp = 13;
        warpUsageRatio = 2;
        maxCruiseWarp = 11;
        maxTurnWarp = 8;
        warpEnergy = 120;
        impulseEnergy = 60;
        transwarp = true;
        transwarpTracking = false;
        transwarpCounter = 13;

        damagePercent = 30;
        shieldStrength = 25;
        visibility = 100;
        cloak = false;
        cloakTime = 0;
        cloakPower = 0;
        cloakRegeneration = 0;
        scanRange = 8000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 20;
        minPhaserRange = 500;

        torpedoType = TORPEDO_NORMAL;
        maxTorpedoBanks = 1;
        maxTorpedoStorage = 200;
        minTorpedoRange = 0;
        maxTorpedoRange = 1000;

        drones = true;
        maxDroneStorage = 30;
        droneStrength = 400;
        variableSpeed = false;

        mines = false;
        maxMineStorage = 0;
        mineStrength = 0;

        //Dilithium Crystals
        maxCrystalStorage = 0;

        crew = 5;
        homePlanet = "Earth";
    }

    public void firePhasers() {
        if (organiaModifier)
            return; // no weapons allowed in organia space

        if (this.phaserFireType == TrekShip.PHASER_FIRENARROW && !checkLock()) {
            parent.hud.sendMessage("Phasers must be locked.");
            return;
        }

        if (!checkCloak()) {
            return;
        }

        if (phasers <= 0) {
            parent.hud.sendMessage("You must load your phasers before you can fire.");
            return;
        }

        double maxDamage = phasers * 25;
        double percentOfRange = 0;
        double percentOfDamage = 0;

        // Handle the teleporter phaser type.
        if (phaserType == PHASER_TELEPORTER) {
            if (lockTarget != null) {
                percentOfRange = 100 - (((TrekMath.getDistance(this, lockTarget)) / this.minPhaserRange) * 100);
                percentOfDamage = maxDamage * (percentOfRange / 100);

                if (percentOfDamage < 0) {
                    percentOfDamage = 0;
                }

                if (percentOfDamage > 0) {
                    float teleportDistance = new Float(percentOfDamage * 3.2).floatValue();

                    this.point.add(new Trek3DVector(teleportDistance, teleportDistance, teleportDistance));
                }
            } else {
                Vector objectsInRange = currentQuadrant.getAllObjectsInRange(this, this.minPhaserRange);

                for (Enumeration e = objectsInRange.elements(); e.hasMoreElements(); ) {
                    TrekObject testRange = (TrekObject) e.nextElement();

                    if (TrekUtilities.isObjectShip(testRange)) {
                        percentOfRange = 100 - (((TrekMath.getDistance(this, testRange)) / this.minPhaserRange) * 100);
                        percentOfDamage = (maxDamage * (percentOfRange / 100)) / 2;

                        if (percentOfDamage < 0) {
                            percentOfDamage = 0;
                        }


                        if (percentOfDamage > 0) {
                            float teleportDistance = new Float(percentOfDamage * 3.2).floatValue();

                            this.point.add(new Trek3DVector(teleportDistance, teleportDistance, teleportDistance));
                            break;
                        }
                    }
                }
            }

            // Return our energy, set some timeouts, etc.
            phaserFireTimeout += phasers;
            // phaserEnergyReturned += phasers;
            phasers = 0;

        } else {
            super.firePhasers();
        }
    }
}
