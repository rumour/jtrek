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
 * A handler for KEV-12 class ships.
 *
 * @author Joe Hopkinson
 */
public final class ShipD10 extends TrekShip {

    public ShipD10(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "D-10";
        fullClassName = "Klingon D-10";
        shipType = "starship";

        classLetter = "v";
        damageWarp = 15;
        warpUsageRatio = 2;
        maxCruiseWarp = 12;
        maxTurnWarp = 10;
        warpEnergy = 120;
        impulseEnergy = 60;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 50;
        shieldStrength = 17;
        visibility = 95;

        cloak = false;
        cloakTime = 50;
        cloakPower = 50;
        cloakRegeneration = 1;

        scanRange = 8000;

        phaserType = PHASER_DISRUPTOR;
        maxPhaserBanks = 60;
        minPhaserRange = 500;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 8;
        maxTorpedoStorage = 112;
        minTorpedoRange = 500;
        maxTorpedoRange = 1000;

        drones = true;
        maxDroneStorage = 15;
        droneStrength = 600;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 8;
        mineStrength = 900;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 150;
        homePlanet = "Klinzhai";
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

        if (this.phaserFireType == PHASER_FIRENARROW) {
            if (!checkLock()) {
                return;
            }

            double maxDamage = 100 * phasers / 5;

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

                double maxDamage = 100 * phasers / 5 / 4; // normal phasers would be / 2 - make dis / 4

                TrekDamageStat stat = target.doDamageInstant(this, maxDamage, "phasers", true);
                parent.hud.drawDamageGivenStat(stat);

            }

        }

        // Return our energy, set some timeouts, etc.
        phaserFireTimeout += phasers;
        phasers = 0;
    }


}