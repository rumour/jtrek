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

import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

/**
 * Represents a planet in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekPlanet extends TrekObject {
    protected boolean givesAntimatterFlag;
    protected boolean givesTorpsFlag;
    protected boolean givesMinesFlag;
    protected boolean givesDronesFlag;
    protected boolean givesCrystalsFlag;
    protected boolean fixesCloakFlag;
    protected boolean fixesTransmitterFlag;
    protected boolean installsTranswarpFlag;
    protected boolean installsCloakFlag;
    protected boolean repairsLifeSupportFlag;
    protected boolean givesCorbomiteFlag;
    protected boolean givesIridiumFlag;
    protected boolean givesMagnabuoyFlag;
    protected boolean givesNeutronFlag;

    protected Hashtable attackers;
    protected int phaserRange;
    protected Vector losersInRange;

    protected boolean raceSpecific;
    protected String race;

    protected int totalAntiMatter = 50000;
    protected int gold;

    private int tick = 0;
    private int second = 0;

    protected boolean disabled;
    protected int phasers = 50;
    protected int minPhaserRange;
    protected int fireTimeout;
    protected int phaserDamage;

    public TrekPlanet(int x, int y, int z, String name, String scanletter, String codes, String whatRace, int phaserRangeIn, int phaserDamageIn, int fireDelay) {
        super(name, scanletter, x, y, z);
        type = OBJ_PLANET;

        race = whatRace;

        givesAntimatterFlag = (codes.indexOf("A ") != -1) ? true : false;
        givesTorpsFlag = (codes.indexOf("T ") != -1) ? true : false;
        givesMinesFlag = (codes.indexOf("M ") != -1) ? true : false;
        givesDronesFlag = (codes.indexOf("D ") != -1) ? true : false;
        givesCrystalsFlag = (codes.indexOf("C ") != -1) ? true : false;

        if (codes.indexOf("FC ") != -1) {
            fixesCloakFlag = true;
            installsCloakFlag = true;
        } else {
            fixesCloakFlag = false;
            installsCloakFlag = false;
        }

        fixesTransmitterFlag = (codes.indexOf("FR ") != -1) ? true : false;
        installsTranswarpFlag = (codes.indexOf("TW ") != -1) ? true : false;
        repairsLifeSupportFlag = (codes.indexOf("L ") != -1) ? true : false;

        // misc devices
        givesCorbomiteFlag = (codes.indexOf("CM ") != -1) ? true : false;
        givesIridiumFlag = (codes.indexOf("I ") != -1) ? true : false;
        givesMagnabuoyFlag = (codes.indexOf("MB ") != -1) ? true : false;
        givesNeutronFlag = (codes.indexOf("N ") != -1) ? true : false;

        gold = 20000;

        if (fixesCloakFlag) {
            if (!(this instanceof PlanetTeamPlay))
                givesCrystalsFlag = false;
        }

        if (givesCorbomiteFlag) {
            givesMinesFlag = false;
        }

        raceSpecific = (race.equals("")) ? false : true;
        damagePercent = 8;

        // For the losers that attack.
        attackers = TrekServer.dbInt.getObjectEnemyList(name);
        phaserRange = phaserRangeIn;
        minPhaserRange = phaserRangeIn;
        fireTimeout = fireDelay;
        phaserDamage = phaserDamageIn;
    }

    public boolean isRaceSpecific() {
        return raceSpecific;
    }

    public boolean isDisabled() {
        return disabled;
    }

    protected void doTick() {
        Random gen = new Random();
        tick++;

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

            totalAntiMatter++;

            if (totalAntiMatter > 50000)
                totalAntiMatter = 50000;

            if (gold > 20000)
                gold--;

            if (gold < 20000)
                gold++;

            tick = 0;

            // Repair damage that we received.
            double repairDamage = Math.abs(gen.nextInt() % 270);

            if (repairDamage < 45 && damage > 0) {  // reduced from 180 to 45
                damage--;
            }

            if (damage > 500)
                disabled = true;
            else
                disabled = false;
        }
    }

    protected int getAntiMatterChunk(int thisAmount) {
        if (totalAntiMatter <= 0) {
            return 0;
        }

        if (totalAntiMatter < thisAmount) {
            totalAntiMatter = 0;
            return totalAntiMatter;
        }

        totalAntiMatter -= thisAmount;
        return thisAmount;
    }

    protected void updateBattle(TrekDamageStat stat) {
        if (TrekUtilities.isObjectShip(stat.victor) && !isAttacker((TrekShip) stat.victor)) {
            addAttacker((TrekShip) stat.victor);
        }

        damage += stat.structuralDamage * .25;
        return;
    }

    public boolean givesAntimatter(TrekShip thisShip) {
        if (isRaceSpecific() && !thisShip.homePlanet.equals(race))
            return false;

        if (!givesAntimatterFlag)
            return false;

        return true;
    }

    public boolean fixesCloak(TrekShip thisShip) {
        if (!fixesCloakFlag)
            return false;

        return true;
    }

    public boolean givesTorps(TrekShip thisShip) {
        if (!givesTorpsFlag)
            return false;

        return true;
    }

    public boolean givesMines(TrekShip thisShip) {
        if (!givesMinesFlag)
            return false;

        return true;
    }

    public boolean givesDrones(TrekShip thisShip) {
        if (!givesDronesFlag)
            return false;

        return true;
    }

    public boolean givesCrystals(TrekShip thisShip) {
        if (!givesCrystalsFlag)
            return false;

        return true;
    }

    public boolean fixesTransmitter(TrekShip thisShip) {
        if (!fixesTransmitterFlag)
            return false;

        return true;
    }

    public boolean installsTranswarp(TrekShip thisShip) {
        if (!installsTranswarpFlag)
            return false;

        return true;
    }

    public boolean installsCloak(TrekShip thisShip) {
        if (!installsCloakFlag)
            return false;

        return true;
    }

    public boolean repairsLifeSupport(TrekShip thisShip) {
        if (!repairsLifeSupportFlag)
            return false;

        return true;
    }

    public boolean givesCorbomite(TrekShip thisShip) {
        if (!givesCorbomiteFlag) return false;

        return true;
    }

    public boolean givesIridium(TrekShip thisShip) {
        if (!givesIridiumFlag) return false;

        return true;
    }

    public boolean givesMagnabuoy(TrekShip thisShip) {
        if (!givesMagnabuoyFlag) return false;

        return true;
    }

    public boolean givesNeutron(TrekShip thisShip) {
        if (!givesNeutronFlag) return false;

        return true;
    }

    protected void firePhasers(TrekObject targetShip) {
        double maxDamage = phasers * 8;
        double percentOfRange = 0;
        double percentOfDamage = 0;

        percentOfRange = 100 - (((TrekMath.getDistance(this, targetShip)) / this.minPhaserRange) * 100);
        percentOfDamage = maxDamage * (percentOfRange / 100);

        if (percentOfDamage < 0) {
            percentOfDamage = 0;
        }

        TrekDamageStat stat = targetShip.doDamageInstant(this, phaserDamage, "phasers", true);
        TrekLog.logMessage(this.name + " doing " + stat.damageGiven + " to " + targetShip.name);
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
}