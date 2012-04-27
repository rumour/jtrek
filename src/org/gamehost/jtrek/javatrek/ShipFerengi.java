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
 * A handler for CDA-120 class ships.
 *
 * @author Joe Hopkinson
 */
public class ShipFerengi extends TrekShip {

    public ShipFerengi(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "FERENGI";
        fullClassName = "Ferengi";
        shipType = "starship";

        classLetter = "u";
        maxCruiseWarp = 13;
        maxTurnWarp = 11;
        warpEnergy = 110;
        impulseEnergy = 50;
        transwarp = false;
        transwarpTracking = false;
        damagePercent = 50;
        shieldStrength = 17;
        visibility = 95;
        cloak = false;
        cloakTime = 0;
        cloakPower = 0;
        cloakRegeneration = 0;
        scanRange = 8000;

        phaserType = PHASER_EXPANDINGSPHEREINDUCER;
        maxPhaserBanks = 50;
        minPhaserRange = 500;

        torpedoType = TORPEDO_PHOTON;
        maxTorpedoBanks = 5;
        maxTorpedoStorage = 40;
        minTorpedoRange = 500;
        maxTorpedoRange = 1300;

        drones = true;
        maxDroneStorage = 20;
        droneStrength = 600;
        variableSpeed = false;
        droneSpeed = 10;

        mines = false;
        maxMineStorage = 0;
        mineStrength = 0;

        //Dilithium Crystals
        maxCrystalStorage = 30;

        crew = 74;
        homePlanet = "Ferrigulum";

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

    protected void doScore(TrekStarbase base) {
        if (damageGiven > 0 || bonus > 0) {
            int totalGold = 0;

            // give ferengi class an added gold earning advantage
            totalGold = (int) Math.round((damageGiven + bonus) * .15);

            gold += totalGold;
            base.gold -= totalGold;
            if (TrekServer.isTeamPlayEnabled()) TrekServer.teamStats[parent.teamNumber].addGold(totalGold);

            damageGiven = 0;
            bonus = 0;
            damageReceived = 0;

            parent.hud.sendMessageBeep("Received " + totalGold + " gold pressed latinum bars!!");
        }
    }
}
