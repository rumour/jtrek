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
import java.util.Random;
import java.util.Vector;

/**
 * Represents a non-instant torpedo in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekTorpedo extends TrekObject {
    public TrekObject owner;
    protected boolean intercepting;
    protected TrekObject interceptTarget;
    public int strength;
    protected int counter;
    protected String ownerName;
    protected boolean hasTarget;
    protected int tickCountLifetime;
    public int torpType;
    private boolean active = false;

    // this constructor is for the warring planets
    public TrekTorpedo(TrekObject ownerin, int typeIn, TrekObject targetIn, double torpSpeed, int torpLife, int torpStrength, int torpRange) {
        this(ownerin, typeIn, targetIn);

        strength = torpStrength;
        warpSpeed = torpSpeed;
        lifeTime = torpLife;
        scanRange = torpRange;
        interceptTarget = targetIn;
    }

    public TrekTorpedo(TrekObject ownerin, int typeIn, TrekObject targetIn) {
        super(TrekUtilities.getTorpedoTypeString(typeIn), ownerin.currentQuadrant.getObjectLetter(), ownerin.point.x, ownerin.point.y, ownerin.point.z);

        try {
            owner = ownerin;
            torpType = typeIn;
            TrekShip ownerShip = null;
            interceptTarget = targetIn;

            active = false;
            type = OBJ_TORPEDO;

            intercepting = false;

            counter = 2; // changed from 1 to 2, seems like plasma is getting an extra tick ...
            tickCountLifetime = 0;

            if (TrekUtilities.isObjectShip(owner)) {
                ownerShip = (TrekShip) owner;
                if (torpType == TrekShip.TORPEDO_WARHEAD || torpType == TrekShip.TORPEDO_ONEHITWARHEAD) {
                    intercepting = true;
                } else {
                    intercepting = false;
                    interceptTarget = null;
                }

                if (ownerShip.isWeaponsLocked()) {
                    Trek3DPoint current = new Trek3DPoint();
                    Trek3DPoint target = new Trek3DPoint();

                    target.x = ownerShip.lockTarget.point.x;
                    target.y = ownerShip.lockTarget.point.y;
                    target.z = ownerShip.lockTarget.point.z;

                    current.x = point.x;
                    current.y = point.y;
                    current.z = point.z;

                    if (current.equals(target)) {
                        vector = new Trek3DVector(0, 0, 1);
                    } else {
                        target.subtract(current);

                        vector.x = target.x;
                        vector.y = target.y;
                        vector.z = target.z;
                    }
                } else if (ownerShip.isIntercepting()) {
                    Trek3DPoint current = new Trek3DPoint();
                    Trek3DPoint target = new Trek3DPoint();

                    if (ownerShip.interceptTarget != null) {
                        target.x = ownerShip.interceptTarget.point.x;
                        target.y = ownerShip.interceptTarget.point.y;
                        target.z = ownerShip.interceptTarget.point.z;
                    } else if (ownerShip.intCoordPoint != null) {
                        target.x = ownerShip.intCoordPoint.x;
                        target.y = ownerShip.intCoordPoint.y;
                        target.z = ownerShip.intCoordPoint.z;
                    }

                    current.x = point.x;
                    current.y = point.y;
                    current.z = point.z;

                    if (current.equals(target)) {
                        vector = new Trek3DVector(0, 0, 1);
                    } else {
                        target.subtract(current);

                        vector.x = target.x;
                        vector.y = target.y;
                        vector.z = target.z;
                    }
                } else {
                    this.vector.x = owner.vector.x;
                    this.vector.y = owner.vector.y;
                    this.vector.z = owner.vector.z;

                }
            } else {
                intercepting = true;
                vector.x = targetIn.point.x;
                vector.y = targetIn.point.y;
                vector.z = targetIn.point.z;
            }

            if (torpType == TrekShip.TORPEDO_PLASMA) {
                Random gen = new Random();
                double warpModifier = Math.abs((gen.nextInt() % 2) * .2);
                TrekLog.logMessage("Plasma Torpedo for " + owner.name + " - Warp Modifier: " + warpModifier);

                warpSpeed = 9.2 + warpModifier;

                strength = 1500;
                lifeTime = 20;
                scanRange = 200;

                // if a bot launched it; add it to the bot weapons fire history
                if (TrekUtilities.isObjectShip(owner) && owner != null && owner instanceof TrekShip) {
                    TrekShip ts = (TrekShip) owner;
                    if (ts.parent instanceof BotPlayer) {
                        BotPlayer bp = (BotPlayer) ts.parent;
                        bp.addWeaponFire(this, BotPlayer.WEAPON_PLASMA);
                    }
                }
            }

            if (TrekUtilities.isObjectShip(owner) && torpType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {

                if (ownerShip.torpedoWarpSpeedAuto) {
                    warpSpeed = ownerShip.warpSpeed;

                    if (warpSpeed < 5) {
                        warpSpeed = 5;
                    }
                    if (warpSpeed > 10) {
                        warpSpeed = 10;
                    }
                } else {
                    warpSpeed = ownerShip.torpedoWarpSpeed;

                    if (warpSpeed < 5) {
                        warpSpeed = 5;
                    }
                    if (warpSpeed > 10) {
                        warpSpeed = 10;
                    }
                }

                strength = new Double(warpSpeed * 120).intValue();
                lifeTime = 20;
                scanRange = 200;
            }

            if (torpType == TrekShip.TORPEDO_NORMAL) {
                strength = 100;
                warpSpeed = 21;
                lifeTime = 4;
                scanRange = 1;
            }

            if (torpType == TrekShip.TORPEDO_WARHEAD || torpType == TrekShip.TORPEDO_ONEHITWARHEAD) {
                strength = 200;
                warpSpeed = 8;
                lifeTime = 120;
                scanRange = 200;
            }

            ownerName = owner.name;
            hasTarget = false;
        } catch (Exception e) {
            TrekLog.logError("Error in TrekTorpedo constructor.");
            TrekLog.logException(e);
            this.kill();
        }
    }

    protected void doTick() {
        // Added to force a 3 second activation timer on plasma torpedoes. (and the warring planet [one hit warhead] torps)
        if (torpType == TrekShip.TORPEDO_PLASMA || torpType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA || torpType == TrekShip.TORPEDO_ONEHITWARHEAD) {
            if (tickCountLifetime >= 15) {
                active = true;
            }
        } else {
            active = true;
        }

        // Decay the strength, if it is plasma.
        if (torpType == TrekShip.TORPEDO_PLASMA || torpType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
            if (lifeTime < 6) {
                strength -= (strength * .05);
            }
        }

        /*
           * Find out if we are close enough to hit something.  Hit the first ship
           * found.  But only if the torpedo is active.
           */
        if (isActive()) {
            // plasma should hit cloaked ships, so we need to get all ships within range... should we make
            // freighter torpedoes the same?  why not try it?
            //Vector visibleObjects = currentQuadrant.getVisibleShips(this);
            Vector allShips = currentQuadrant.getAllObjectsInRange(this, 201);
            //ShipsInRange(this, 201);
            boolean didHit = false;

            for (Enumeration e = allShips.elements(); e.hasMoreElements(); ) {

                TrekObject obj = (TrekObject) e.nextElement();

                if (TrekUtilities.isObjectShip(obj)) {

                    if (obj != owner) {

                        if (this.scanRange > TrekMath.getDistance(this, obj)) {

                            TrekShip ship = (TrekShip) obj;

                            TrekDamageStat newStat = null;

                            switch (torpType) {
                                case TrekShip.TORPEDO_PLASMA:
                                    newStat = ship.doDamage(this, "plasma", false);
                                    if (TrekUtilities.isObjectShip(owner) && owner != null && owner instanceof TrekShip) {
                                        TrekShip ts = (TrekShip) owner;
                                        if (ts.parent instanceof BotPlayer) {
                                            BotPlayer bp = (BotPlayer) ts.parent;
                                            bp.weaponReport(this, BotPlayer.WEAPON_PLASMA, true);
                                        }
                                    }
                                    break;
                                case TrekShip.TORPEDO_VARIABLESPEEDPLASMA:
                                    newStat = ship.doDamage(this, "plasma", false);
                                    break;
                                case TrekShip.TORPEDO_NORMAL:
                                    newStat = ship.doDamage(this, "torpedo", false);
                                    break;
                                case TrekShip.TORPEDO_WARHEAD:
                                    // keep it from hitting every tick
                                    if (tickCountLifetime % 4 == 0) {
                                        if (!obj.name.equals(owner.name)) // for the borg collective
                                            newStat = ship.doDamage(this, "warhead", false);
                                    }
                                    break;
                                case TrekShip.TORPEDO_ONEHITWARHEAD:
                                    newStat = ship.doDamage(this, "warhead", false);
                                    break;
                                default:
                                    newStat = ship.doDamage(this, "NEGAVERSE TORPEDO!!", false);
                                    break;
                            }

                            if (newStat != null) {
                                if (TrekUtilities.isObjectShip(owner)) {
                                    TrekShip ownerShip = (TrekShip) owner;
                                    ownerShip.parent.hud.drawDamageGivenStat(newStat);
                                }

                                didHit = true;
                            }

                        }
                    }
                } else {
                    if (torpType == TrekShip.TORPEDO_ONEHITWARHEAD && obj == interceptTarget) {
                        interceptTarget.doDamageInstant(owner, strength, "warhead", false);
                        didHit = true;
                    }
                }
            }

            if (didHit) {
                // warheads hit multiple times
                if (torpType != TrekShip.TORPEDO_WARHEAD)
                    kill();
            }
        }

        // Update the lifeTime...
        if (counter > 4) {
            lifeTime -= 1;
            counter = 1;
        }

        // If the lifeTime has expired, kill the object.
        if (lifeTime <= 0) {
            if (TrekUtilities.isObjectShip(owner) && owner != null && owner instanceof TrekShip) {
                TrekShip ts = (TrekShip) owner;
                if (ts.parent instanceof BotPlayer) {
                    BotPlayer bp = (BotPlayer) ts.parent;
                    bp.weaponReport(this, BotPlayer.WEAPON_PLASMA, false);
                }
            }
            kill();
        }

        counter++;
        tickCountLifetime++;
    }

    protected Trek3DVector getNewDirectionVector() {
        try {
            if (intercepting) {
                // omniscient warheads (always know an uncloaked enemy's location)
                if (TrekMath.getDistance(this, interceptTarget) < 250000) {
                    Trek3DPoint current = new Trek3DPoint(point);
                    Trek3DPoint target = new Trek3DPoint(interceptTarget.point);

                    if (current.equals(target)) {
                        vector = new Trek3DVector(0, 0, 1);
                    } else {
                        target.subtract(current);
                        vector.applyPoint(target);
                    }
                }
            }

            Trek3DVector returnVector = new Trek3DVector(vector);
            double distance = TrekMath.getDistanceMoved(warpSpeed);

            returnVector.normalize();
            returnVector.scaleUp(distance);

            return returnVector;
        } catch (Exception e) {
            TrekLog.logError("TrekTorpedo.getNewDirectionVector error.");
            TrekLog.logException(e);
        }

        return vector;
    }

    protected void kill() {
        currentQuadrant.removeObjectByScanLetter(scanLetter);
    }

    protected boolean isActive() {
        return active;
    }

    protected void updateBattle(TrekDamageStat stat) {
        if (torpType == TrekShip.TORPEDO_WARHEAD && stat.damageGiven > 0) {
            damage += stat.damageGiven / 8;
            stat.damageGiven = 0;
            stat.structuralDamage = 0;
            stat.bonus = 0;
            stat.shieldReduction = 0;
            if (damage > 100)
                kill();
        } else if (torpType == TrekShip.TORPEDO_ONEHITWARHEAD) {
            kill();
        }
    }
}
