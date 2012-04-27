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

import java.util.Random;

/**
 * TrekObject is an abstract class that represents an object in a quadrant of the game.
 *
 * @author Joe Hopkinson
 */
public abstract class TrekObject {
    public Trek3DPoint point;
    public Trek3DVector vector;

    public String name;
    public String scanLetter;
    public String objectDesc;
    public TrekQuadrant currentQuadrant;

    public int type;
    public int lifeTime;
    public double scanRange;
    public int shields;
    public int shieldStrength;
    public int damagePercent;
    public int damage;

    public double warpSpeed;

    public TrekZone zoneEffect; // primarily to make it so ships outside the nebula can't see all objects within the nebula

    public static final int OBJ_OBJECT = 0;
    public static final int OBJ_PLANET = 1;
    public static final int OBJ_STARBASE = 2;
    public static final int OBJ_SHIP = 3;
    public static final int OBJ_DRONE = 4;
    public static final int OBJ_MINE = 5;
    public static final int OBJ_TORPEDO = 6;
    public static final int OBJ_BUOY = 7;
    public static final int OBJ_WORMHOLE = 8;
    public static final int OBJ_ASTEROIDBELT = 9;
    public static final int OBJ_SHIPDEBRIS = 10;
    public static final int OBJ_STAR = 11;
    public static final int OBJ_NEBULA = 12;
    public static final int OBJ_COMET = 13;
    public static final int OBJ_ZONE = 14;
    public static final int OBJ_BLACKHOLE = 15;
    public static final int OBJ_GOLD = 16;
    public static final int OBJ_QUASAR = 18;
    public static final int OBJ_PULSAR = 19;
    public static final int OBJ_ORGANIA = 20;
    public static final int OBJ_OBSERVERDEVICE = 21;
    public static final int OBJ_CORBOMITE = 22;
    public static final int OBJ_IRIDIUM = 23;
    public static final int OBJ_MAGNABUOY = 24;
    public static final int OBJ_NEUTRON = 25;

    public TrekObject(String namein, String scanletterin, float xin, float yin, float zin) {
        this(namein, scanletterin, xin, yin, zin, "");
    }

    // add new constructor to prepare for implementing object descriptions (ESC shift-d objLtr command) -
    // since this could take a couple of check-ins, just overload the constructor to initialize to a blank
    // description, if I haven't updated the specific object instance creation call yet...
    public TrekObject(String namein, String scanletterin, float xin, float yin, float zin, String desc) {
        name = namein;
        scanLetter = scanletterin;
        objectDesc = desc;

        point = new Trek3DPoint(xin, yin, zin);
        vector = new Trek3DVector();

        type = OBJ_OBJECT;
        lifeTime = 999;
        scanRange = 0;
        warpSpeed = 0;
    }

    // Methods to be over-ridden in child classes.
    protected void updateSecond() {
        return;
    }

    protected void updateBattle(TrekDamageStat stat) {
        return;
    }

    /**
     * Methods that are in every object.
     */
    protected TrekDamageStat doDamage(TrekObject obj, String type, boolean showAttacker) {
        int damageCaused = 0;

        if (TrekUtilities.isObjectMine(obj)) {
            TrekMine mine = (TrekMine) obj;
            damageCaused = mine.strength;
        }

        if (TrekUtilities.isObjectDrone(obj)) {
            TrekDrone drone = (TrekDrone) obj;
            damageCaused = drone.strength;
        }

        if (TrekUtilities.isObjectTorpedo(obj)) {
            TrekTorpedo torp = (TrekTorpedo) obj;
            damageCaused = torp.strength;
        }

        return applyDamage(damageCaused, obj, type, showAttacker);
    }

    protected TrekDamageStat applyDamage(double rawDamage, TrekObject from, String type, boolean showAttacker) {
        double shielddrop = TrekMath.getShieldDrop(rawDamage, this.shieldStrength);
        double intDamage = 0;
        double shieldsafterdrop = 0;

        boolean nebulaEffect = false;
        if (TrekUtilities.isObjectShip(this)) {
            TrekShip ship = (TrekShip) this;
            if (ship.nebulaTarget != null)
                nebulaEffect = true;
        }

        // Some value tweaking.
        if (rawDamage <= 0) {
            rawDamage = 0;
        }

        // Check our shields.. see if we need to take them to zero first.
        if ((shields > 0) && (!nebulaEffect)) {
            // Get our shield modifier.
            double difference = shielddrop - shields;

            // Apply the shield drop.
            shieldsafterdrop = shields - shielddrop;
            if (shieldsafterdrop <= 0) {
                shieldsafterdrop = 0;
            }
            shields = new Double(shieldsafterdrop).intValue();

            // If we have no shields..
            if (shields <= 0) {
                if (shields <= 0) {
                    shields = 0;
                }
                shielddrop = 0;

                intDamage = TrekMath.getInternalDamage(difference * this.shieldStrength, this.damagePercent);
                this.damage += intDamage;
            }
        } else {
            intDamage = TrekMath.getInternalDamage(rawDamage, this.damagePercent);
            this.damage += intDamage;
        }

        // apply remainder amount of damage (non integer portion), and randomly see if another int will be added
        int dmgRemainder = (int) Math.round((intDamage - new Double(intDamage).intValue()) * 100);
        //TrekLog.logMessage("The dmgRemainder = " + dmgRemainder);
        Random randomNumGen = new Random();
        if ((Math.abs(randomNumGen.nextInt()) % 100) < dmgRemainder) {
            intDamage++;
            this.damage++;
        }
        TrekDamageStat stat = new TrekDamageStat(new Double(rawDamage).intValue(), new Double(intDamage).intValue(), new Double((shieldsafterdrop <= 0) ? shieldsafterdrop : 100 - shieldsafterdrop).intValue(), 0, this, from, type, showAttacker);

        updateBattle(stat);

        return stat;
    }

    protected TrekDamageStat doDamageInstant(TrekObject from, double rawDamage, String type, boolean showAttacker) {
        return applyDamage(rawDamage, from, type, showAttacker);
    }

    protected TrekDamageStat doRamDamage(TrekObject from, int ramDmg) {

        this.damage += ramDmg;

        TrekDamageStat stat = new TrekDamageStat(ramDmg * 10, ramDmg, 0, 0, this, from, "ram", true);
        stat.rammed = true;
        updateBattle(stat);
        //TrekLog.logMessage("Ram damage: " + ramDmg + ", " + this.damage + "  -" + this.name);

        return stat;
    }

    protected TrekDamageStat doAtmosphereDamage(TrekObject from, int dmgAmount) {

        this.damage += dmgAmount;

        TrekDamageStat stat = new TrekDamageStat(dmgAmount * 10, dmgAmount, 0, 0, this, from, "Orbit/Dock Damage", false);
        stat.rammed = true; // treat it similar to ram damage

        return stat;
    }

    protected void doTick() {
        return;
    }

    protected Trek3DVector getNewDirectionVector() {
        return null;
    }
}