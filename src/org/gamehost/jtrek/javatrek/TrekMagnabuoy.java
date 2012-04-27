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
 * Represents a Magnabuoy in the game.
 *
 * @author Jay Ashworth
 */
public class TrekMagnabuoy extends TrekObject {
    protected TrekShip owner;
    protected TrekShip target;

    protected String ownerName;

    protected int lifeCounter = 1;

    public TrekMagnabuoy(TrekShip ownerin, TrekShip targetin) {
        super("magnabuoy", ownerin.currentQuadrant.getObjectLetter(), targetin.point.x, targetin.point.y, targetin.point.z);
        owner = ownerin;
        target = targetin;
        lifeTime = 20;
        type = OBJ_MAGNABUOY;
        ownerName = owner.name;

        Trek3DVector vec = targetin.vector;
        vec.normalize();
    }

    protected void doTick() {
        if (lifeCounter >= 6) {
            lifeTime--;
            lifeCounter = 1;
        }

        TrekObject thisObj = currentQuadrant.getShipByScanLetter(target.scanLetter);

        if ((TrekUtilities.isObjectShip(thisObj)) && ((TrekShip) thisObj == target)) {
            point = new Trek3DPoint(target.point);
            vector = new Trek3DVector(target.vector);
            vector.normalize();
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
