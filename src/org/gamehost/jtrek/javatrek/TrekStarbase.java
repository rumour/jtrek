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

import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

/**
 * TrekStarbase represensts a Starbase in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekStarbase extends TrekObject {
    public boolean givesTorps;
    public boolean givesMines;
    public boolean givesDrones;
    public boolean fixesLifeSupport;
    public int gold;
    protected int tick = 0;
    protected int second = 0;

    protected Hashtable attackers;
    protected int phaserRange;
    protected Vector losersInRange;

    private boolean sentDistressSignal = false;
    private boolean sentGoldSignal = false;
    private boolean sentDestructionSignal = false;

    protected boolean destruct = false;
    protected int destructionCountdown = 0;
    protected int phasers = 20;
    protected int minPhaserRange;
    protected boolean disabled;
    protected int fireTimeout;
    protected int phaserDamage;

    public TrekStarbase(int x, int y, int z, String name, String scanletter, String codes, int phaserRangeIn, int phaserDamageIn, int fireDelay) {
        super(name, scanletter, x, y, z);
        type = OBJ_STARBASE;

        givesTorps = (codes.indexOf("T ") != -1) ? true : false;
        givesMines = (codes.indexOf("M ") != -1) ? true : false;
        givesDrones = (codes.indexOf("D ") != -1) ? true : false;
        fixesLifeSupport = (codes.indexOf("L ") != -1) ? true : false;
        gold = 20000;
        damagePercent = 8;

        // For the losers that attack.
        attackers = TrekServer.dbInt.getObjectEnemyList(name);
        phaserRange = phaserRangeIn;
        minPhaserRange = phaserRangeIn;
        fireTimeout = fireDelay;
        phaserDamage = phaserDamageIn;
    }

    /**
     * This method is called when the Starbase is attacked.
     * Future home of Starbase AI.
     */
    protected void updateBattle(TrekDamageStat stat) {
        if (!isAttacker((TrekShip) stat.victor)) {
            addAttacker((TrekShip) stat.victor);
        }

        damage += stat.structuralDamage * .25;

        if (damage >= 100 && !sentDistressSignal) {
            TrekServer.sendMsgToAllPlayers("Distress Signal from " + name + ": Under attack, require assistance!", this, true, true);
            sentDistressSignal = true;
        }

        if (damage >= 150 && !sentGoldSignal) {
            TrekServer.sendMsgToAllPlayers("Distress Signal from " + name + ": Under attack, now losing gold!", this, true, true);
            sentGoldSignal = true;
        }

        if (damage >= 200 && !sentDestructionSignal && !destruct) {
            TrekServer.sendMsgToAllPlayers("Distress Signal from " + name + ": Under attack, imminent explosion!", this, true, true);
            sentDestructionSignal = true;
            destruct = true;
            destructionCountdown = 30;
        }

        return;
    }

    public boolean isDisabled() {
        return disabled;
    }

    protected void doTick() {
        Random gen = new Random();
        tick++;

        if (damage > 100)
            disabled = true;
        else
            disabled = false;

        if ((tick % 4) == 0) {
            second++;

            if ((second % fireTimeout) == 0) {
                // Fire at the attackers.
                losersInRange = currentQuadrant.getAllShipsInRange(this, phaserRange);

                for (int x = 0; x < losersInRange.size(); x++) {
                    TrekShip loser = (TrekShip) losersInRange.elementAt(x);

                    if (isAttacker(loser)) {
                        firePhasers(loser);
                    }
                }

                second = 0;
            }

            // If the starbase is destructing, countdown.
            if (destruct) {
                destructionCountdown--;

                // If the countdown is reached, blow it up, and remove the object.  Scatter some gold.  Do some damage.
                if (destructionCountdown <= 0) {
                    // Don't remove the base, just reset everything.
                    destruct = false;
                    damage = 0;
                    gold = 20000;
                    sentDistressSignal = false;
                    sentGoldSignal = false;
                    sentDestructionSignal = false;

                    // Add some damage to nearby ships.
                    Vector shipsInRange = currentQuadrant.getAllShipsInRange(this, 3000);

                    double maxDamage = 5000;

                    double percentOfRange = 0;
                    double percentOfDamage = 0;

                    for (int x = 0; x < shipsInRange.size(); x++) {
                        TrekShip boomTarget = (TrekShip) shipsInRange.elementAt(x);

                        percentOfRange = 100 - (((TrekMath.getDistance(this, boomTarget)) / 3000) * 100);
                        percentOfDamage = (maxDamage * (percentOfRange / 100)) / 2;

                        boomTarget.applyDamage(percentOfDamage, this, "starbase destruction", true);

                    }

                    // Scatter some gold.
                    int chunksToScatter = 5 + Math.abs(gen.nextInt() % 5);

                    for (int y = 0; y < chunksToScatter; y++) {
                        float newX = this.point.x + gen.nextInt() % 7000;
                        float newY = this.point.y + gen.nextInt() % 7000;
                        float newZ = this.point.z + gen.nextInt() % 7000;
                        int amountToLeave = 1000 + Math.abs(gen.nextInt() % 1000);

                        currentQuadrant.addObject(new TrekGold(amountToLeave, this, newX, newY, newZ));
                    }
                }
            }

            if (sentGoldSignal) {
                int goldLost = 300 + Math.abs(gen.nextInt() % 200);
                gold -= goldLost;

                if (gold < 0)
                    gold = 0;
            } else {
                if (gold > 20000)
                    gold--;

                if (gold < 20000)
                    gold++;
            }

            tick = 0;

            // Repair damage that we received.
            double repairDamage = Math.abs(gen.nextInt() % 270);

            if (repairDamage < 180 && damage > 0) {
                damage--;
            }

            if (damage < 150)
                sentGoldSignal = false;

            if (damage < 100)
                sentDistressSignal = false;
        }
    }

    public boolean isAttacker(TrekShip thisShip) {
        // only preserve hatred for a specific instance of a ship, if it dies, and is 'reborn'... all is forgiven

        if (!attackers.containsKey(new Integer(thisShip.dbShipID).toString())) {
            return false;
        }

        return true;
    }

    public void addAttacker(TrekShip thisShip) {
        attackers.put(new Integer(thisShip.dbShipID).toString(), new Integer(1000));
        TrekServer.dbInt.addEnemy(name, thisShip.dbShipID);
    }

    public void payBalance(TrekObject thisObj) {
        if (!isAttacker((TrekShip) thisObj))
            return;

        if (isDisabled())
            return;

        int balance = ((Integer) attackers.get(new Integer(((TrekShip) thisObj).dbShipID).toString())).intValue();

        if (balance > 0) {
            if (TrekUtilities.isObjectShip(thisObj)) {
                TrekShip idiot = (TrekShip) thisObj;

                if (idiot.gold <= balance) {
                    balance -= idiot.gold;
                    gold += idiot.gold;
                    idiot.gold = 0;
                } else {
                    idiot.gold -= balance;
                    gold += balance;
                    balance = 0;
                }

                attackers.remove(new Integer(idiot.dbShipID).toString());
                TrekServer.dbInt.removeEnemy(name, idiot.dbShipID);
            }

        }
    }

    protected void firePhasers(TrekObject targetShip) {
        // incrementally make phasers stronger as base takes more damage
        double maxDamage = 0;
        maxDamage = (damage > 10) ? phasers * 8 * (damage / 3) : 100;
        double percentOfRange = 0;
        double percentOfDamage = 0;

        if (damage > 10) {
            percentOfRange = 100 - (((TrekMath.getDistance(this, targetShip)) / this.minPhaserRange) * 100);
            percentOfDamage = maxDamage * (percentOfRange / 100);

            if (percentOfDamage < 0) {
                percentOfDamage = 0;
            }
        } else {
            percentOfDamage = 100;
        }

        TrekDamageStat stat = targetShip.doDamageInstant(this, percentOfDamage, "phasers", true);
        TrekLog.logMessage(this.name + " doing " + stat.damageGiven + " to " + targetShip.name);
    }
}
