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

import org.gamehost.jtrek.javatrek.bot.BotPlayer;

import java.util.Enumeration;
import java.util.Vector;


/**
 * Represents a mine in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekMine extends TrekObject {
    public TrekShip owner;
    protected int strength;
    protected String ownerName;
    protected boolean hasBeenBeamedUp = false;

    protected int lifeCounter = 1;

    public TrekMine(String mineType, String ownerLtr, float ownx, float owny, float ownz) {
        super(mineType, ownerLtr, ownx, owny, ownz);
    }

    public TrekMine(TrekShip ownerin) {
        super("mine", ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);
        owner = ownerin;
        lifeTime = 30;
        scanRange = 100;
        type = OBJ_MINE;
        strength = owner.mineStrength;
        ownerName = owner.name;

        Trek3DVector vec = owner.vector;
        vec.normalize();

        if (owner.warpSpeed < 0) {
            TrekLog.logDebug("MINE: " + owner.name + " is going warp " + owner.warpSpeed + ".  Scaling vector up by 150.");

            vec.scaleUp(150);
            point.add(vec);
        } else {
            TrekLog.logDebug("MINE: " + owner.name + " is going warp " + owner.warpSpeed + ".  Scaling vector up by -150.");

            vec.scaleUp(150);
            point.subtract(vec);
        }

        // if a bot launched it; add it to the bot weapons fire history
        if (TrekUtilities.isObjectShip(owner) && owner != null && owner instanceof TrekShip) {
            TrekShip ts = owner;
            if (ts.parent instanceof BotPlayer) {
                BotPlayer bp = (BotPlayer) ts.parent;
                bp.addWeaponFire(this, BotPlayer.WEAPON_MINE);
            }
        }

    }

    protected void doTick() {
        if (lifeCounter >= 6) {
            lifeTime--;
            lifeCounter = 1;
        }

        Vector visibleObjects = currentQuadrant.getAllShipsInRange(this, 100);

        for (Enumeration e = visibleObjects.elements(); e.hasMoreElements(); ) {
            TrekObject obj = (TrekObject) e.nextElement();

            if (TrekUtilities.isObjectShip(obj)) {
                if (this.scanRange > TrekMath.getDistance(this, obj)) {
                    if (TrekUtilities.isObjectShip(obj)) {
                        TrekShip ship = (TrekShip) obj;
                        TrekDamageStat stat = ship.doDamage(this, "mine", false);
                        owner.updateBattle(stat);
                        hit(true);
                        kill();
                    }
                }
            }
        }

        if (lifeTime <= 0) {
            hit(false);
            kill();
            return;
        }

        lifeCounter++;
    }

    protected void kill() {
        currentQuadrant.removeObjectByScanLetter(scanLetter);
    }

    protected void updateBattle(TrekDamageStat stat) {
        // Since they hit the mine.  Kill it, and give 10 points. But, they have to have caused some damage.
        if (stat.damageGiven > 0) {
            stat.damageGiven = 0;
            stat.structuralDamage = 0;
            stat.bonus = 0;
            stat.shieldReduction = 0;
            kill();
        }
    }

    protected void hit(boolean b) {
        // if a bot launched it, update the owner's weapons list with the success
        if (TrekUtilities.isObjectShip(owner) && owner != null && owner instanceof TrekShip) {
            TrekShip ts = owner;
            if (ts.parent instanceof BotPlayer) {
                BotPlayer bp = (BotPlayer) ts.parent;
                bp.weaponReport(this, BotPlayer.WEAPON_MINE, b);
            }
        }
    }
}
