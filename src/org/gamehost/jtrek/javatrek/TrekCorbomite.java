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

import java.util.Random;

/**
 * Represents an all-powerful corbomite device in the game.
 *
 * @author Jay Ashworth
 */
public class TrekCorbomite extends TrekObject {
    protected TrekShip owner;
    protected String ownerName;

    protected int lifeCounter = 1;

    protected int energy;

    public TrekCorbomite(TrekShip ownerin) {
        super("corbomite", ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);
        owner = ownerin;
        lifeTime = 60;
        type = OBJ_CORBOMITE;
        ownerName = owner.name;
        energy = 1000;

        Trek3DVector vec = owner.vector;
        vec.normalize();
    }

    protected void doTick() {
        if (lifeCounter >= 6) {
            lifeTime--;
            lifeCounter = 1;
        }

        // umm, corbomite doesn't _actually_ do ANYTHING

        // for fun let's make corbomite look like it has oodles of energy, and change it every ... 5 seconds or so
        Random corbEngGen = new Random();
        if ((lifeTime > 0) && (lifeTime % 5 == 0) && (lifeCounter == 1)) {
            energy += Math.abs(corbEngGen.nextInt() % 8999) + 1000;
            // minimum 1000 'energy', max '9999'
        }


        if (lifeTime <= 0) {
            kill();
            return;
        }

        lifeCounter++;
    }

    protected void kill() {
        currentQuadrant.removeObjectByScanLetter(scanLetter);
    }

    protected void updateBattle(TrekDamageStat stat) {
        if (stat.damageGiven > 0) {
            stat.damageGiven = 0;
            stat.structuralDamage = 0;
            stat.bonus = 0;
            stat.shieldReduction = 0;
            kill();
        }
    }
}
