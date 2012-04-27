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
 * A handler for WARBIRD class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipWarbird extends TrekShip {

    public ShipWarbird(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "WARBIRD";
        fullClassName = "Romulan Warbird";
        shipType = "starship";

        classLetter = "n";
        damageWarp = 17;
        maxCruiseWarp = 13;
        maxTurnWarp = 12;
        warpEnergy = 100;
        impulseEnergy = 40;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 45;
        shieldStrength = 16;
        visibility = 96;
        cloak = true;
        cloakTime = 60;
        cloakPower = 75;
        cloakRegeneration = 2;
        scanRange = 8000;

        phaserType = PHASER_AGONIZER;
        maxPhaserBanks = 40;
        minPhaserRange = 500;

        torpedoType = TORPEDO_VARIABLESPEEDPLASMA;
        maxTorpedoBanks = 1;
        maxTorpedoStorage = 35;
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

        crew = 43;
        homePlanet = "Romulus";
    }

    public void loadPhasers(int amount) {
        if (phasers == 40) return;  // if already loaded, don't use up another torpedo

        if (this.checkPhaserCool()) {
            if (phaserType == PHASER_AGONIZER) {
                if (this.torpedoCount <= 0) {
                    parent.hud.sendMessage("Cannot load Agonizer phaser.  Out of torpedoes.");
                    return;
                }

                phasers = 40;
                this.torpedoCount--;
                return;
            }
        }
    }

    public void unloadPhasers(int amount) {
        if (phasers == 0)
            return;

        phasers = 0;
        torpedoCount++;
        return;
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

        if (phaserFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + phaserFireTimeout + " seconds to fire.");
            return;
        }

        if (this.phaserFireType == PHASER_FIRENARROW) {
            if (!checkLock()) {
                return;
            }

            double maxDamage = 400;

            if (TrekMath.getDistance(this, lockTarget) <= minPhaserRange) {
                TrekDamageStat stat = lockTarget.doDamageInstant(this, maxDamage, "phasers", true);
                parent.hud.drawDamageGivenStat(stat);
            } else {
                parent.hud.sendMessage("Phasers miss.");
            }
        } else {
            Vector objectsInRange = currentQuadrant.getAllObjectsInRange(this, minPhaserRange);

            for (Enumeration e = objectsInRange.elements(); e.hasMoreElements(); ) {
                TrekObject target = (TrekObject) e.nextElement();

                if (TrekUtilities.isObjectDrone(target))
                    continue;

                if (TrekUtilities.isObjectBuoy(target))
                    continue;

                if (TrekUtilities.isObjectMine(target))
                    continue;

                double maxDamage = 200;

                TrekDamageStat stat = target.doDamageInstant(this, maxDamage, "phasers", true);
                parent.hud.drawDamageGivenStat(stat);

            }

        }

        // Return our energy, set some timeouts, etc.
        phaserFireTimeout = 3;
        phaserEnergyReturned += 0;
        phasers = 0;
    }

}