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
 * Represents a chunk of gold in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekGold extends TrekObject {
    protected int amount;
    protected int goldTimeout = 0;
    protected int tick = 0;
    protected String ownerName;
    protected int dbPlayerID;

    public TrekGold(int amountin, TrekObject heWhoLeftIt) {
        super("gold", heWhoLeftIt.currentQuadrant.getObjectLetter(), heWhoLeftIt.point.x, heWhoLeftIt.point.y, heWhoLeftIt.point.z);
        amount = amountin;
        this.type = TrekObject.OBJ_GOLD;
        goldTimeout = 600;
        ownerName = heWhoLeftIt.name;
        if (heWhoLeftIt instanceof TrekShip) {
            TrekShip thePlayer = (TrekShip) heWhoLeftIt;
            dbPlayerID = thePlayer.parent.dbPlayerID;
        }
    }

    public TrekGold(int amountin, TrekObject heWhoLeftIt, float xin, float yin, float zin) {
        super("gold", heWhoLeftIt.currentQuadrant.getObjectLetter(), xin, yin, zin);
        amount = amountin;
        this.type = TrekObject.OBJ_GOLD;
        goldTimeout = 600;
        ownerName = heWhoLeftIt.name;
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

    protected void doTick() {
        tick++;

        if ((tick % 4) == 0) {
            if (goldTimeout > 0)
                goldTimeout--;

            if (goldTimeout <= 0)
                kill();
        }

        Vector ships = currentQuadrant.getAllVisibleShipsInRange(this, 1);

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip ship = (TrekShip) e.nextElement();

            // check to see if within range
            if (TrekMath.getDistance(ship, this) < 1) {
                ship.gold += this.amount;
                ship.parent.hud.sendMessage("Picked up " + amount + " gold ...");
                this.amount = 0;

                kill();
                break;
            }
        }
    }
}