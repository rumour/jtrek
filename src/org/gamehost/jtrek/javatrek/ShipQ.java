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
 * Q Ship for administrators.
 *
 * @author Joe Hopkinson
 */
public class ShipQ extends TrekShip {

    boolean autoFix = false;
    TrekObject followTarget = null;
    TrekObject beamTarget = null;
    TrekShip observeShip = null;

    public ShipQ(TrekPlayer playerin, String scanLetter) {
        super(playerin, scanLetter);

        shipClass = "Q";
        fullClassName = "God-Like";
        shipType = "entity";

        classLetter = "?";

        maxCruiseWarp = 18;
        maxTurnWarp = 16;
        warpEnergy = 440;
        impulseEnergy = 160;
        transwarp = true;
        transwarpTracking = true;
        damagePercent = 1;
        shieldStrength = 100;
        visibility = 100;

        cloak = true;
        cloakTime = 999;
        cloakPower = 0;
        cloakRegeneration = 1;

        scanRange = 15000;

        phaserType = PHASER_NORMAL;
        maxPhaserBanks = 200;
        minPhaserRange = 20000;

        maxTorpedoBanks = 1;
        maxTorpedoStorage = 999;
        minTorpedoRange = 0;
        maxTorpedoRange = 20000;
        torpedoType = TrekShip.TORPEDO_PLASMA;

        drones = true;
        maxDroneStorage = 50;
        droneStrength = 5000;
        variableSpeed = false;

        mines = true;
        maxMineStorage = 50;
        mineStrength = 3000;

        //Dilithium Crystals
        maxCrystalStorage = 80;

        crew = 1;
        homePlanet = "Earth";

        damageWarp = 30;
    }

    public void doShipSecondUpdate() {
        super.doShipSecondUpdate();

        if (torpedoCount >= maxTorpedoStorage) {
            torpedoCount = maxTorpedoStorage;
            return;
        }

        // Regenerate klingon torps.
        torpedoCount += 5;
    }


    public void fireDrone() {
        if (!checkLock()) {
            parent.hud.sendMessage("Your weapons must be locked to fire a drone.");
            return;
        }

        TrekDrone QDrone = new TrekDrone(this, lockTarget);
        QDrone.name = "Q-drone";
        QDrone.oldWarpSpeed = 20;
        QDrone.warpSpeed = 20;

        currentQuadrant.addObject(QDrone);

        parent.hud.sendTopMessage("Drone released..... *boom*");

        dronesFired++;
    }

    public void firePhasers() {
        super.firePhasers();
        phaserFireTimeout = 0;
    }

    public void fireTorpedoes() {
        TrekTorpedo QTorp = new TrekTorpedo(this, TrekShip.TORPEDO_PLASMA, null);
        QTorp.name = "Q-plasma";
        QTorp.strength = 3000;
        QTorp.warpSpeed = 20;

        parent.hud.sendTopMessage("Torpedo fired, poor schmuck.");

        currentQuadrant.addObject(QTorp);
        torpsFired++;
        torpedoes = 0;
    }

    public void dropMine() {
        TrekMine QMine = new TrekMine(this);
        QMine.name = "Q-mine";

        parent.hud.sendTopMessage("Mine dropped.  *kaboom*");

        currentQuadrant.addObject(QMine);

        minesDropped++;
    }

    public void doClassSpecificTick() {
        // neverending cloak
        if (cloaked && cloakTimeCurrent < 999) cloakTimeCurrent++;

        // autofix any damage
        if (autoFix) damage = 0;

        if (followTarget != null) {
            point.x = followTarget.point.x;
            point.y = followTarget.point.y;
            point.z = followTarget.point.z;
        }

        if (beamTarget != null) {
            beamTarget.point.x = point.x;
            beamTarget.point.y = point.y;
            beamTarget.point.z = point.z;
        }

        // in case we're observing a ship that quits
        if (parent.isObserving && observeShip == null) {
            parent.isObserving = false;
            parent.drawHud(false);
        }
    }
}
