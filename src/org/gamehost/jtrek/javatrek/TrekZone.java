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
import java.util.Random;
import java.util.Vector;

/**
 * The TrekZone class defines a specific zone in space.  i.e. The Neutral Zone, Asteroid Field, etc.
 *
 * @author Joe Hopkinson
 */
public final class TrekZone extends TrekObject {
    protected int effectRadius;
    protected boolean active = false;
    protected int stateChangeTicks = 0;

    public TrekZone(int x, int y, int z, String name, String scanletter, int radius, int ztype) {
        super(name, scanletter, x, y, z);
        effectRadius = radius;
        type = ztype; // TrekObject type
    }

    protected void doTick() {
        Vector ships = currentQuadrant.getAllShipsInRange(this, effectRadius + 3100);

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip ship = (TrekShip) e.nextElement();

            // check to see if within effect range
            switch (this.type) {
                case TrekObject.OBJ_ASTEROIDBELT:
                    if (TrekMath.getDistance(ship, this) <= effectRadius) {
                        if (ship.asteroidTarget != this) {
                            ship.asteroidTarget = this;
                            triggerZoneEntry(ship);
                            ship.asteroidTickCounter = 0;
                        }
                        ship.asteroidTickCounter++;

                        if (ship.asteroidTickCounter % 4 == 0) {
                            applyZoneEffects(ship);
                        }
                    } else {
                        if (ship.asteroidTarget == this) {
                            ship.asteroidTarget = null;
                            triggerZoneExit(ship);
                        }
                    }
                    break;
                case TrekObject.OBJ_ORGANIA:
                    if (TrekMath.getDistance(ship, this) <= effectRadius) {
                        if (ship.zoneTarget != this) {
                            ship.zoneTarget = this;
                            triggerZoneEntry(ship);
                        }
                    } else {
                        if (ship.zoneTarget == this) {
                            ship.zoneTarget = null;
                            triggerZoneExit(ship);
                        }
                    }
                    break;
                case TrekObject.OBJ_NEBULA:
                    if (TrekMath.getDistance(ship, this) <= effectRadius) {
                        if (ship.nebulaTarget != this) {
                            ship.nebulaTarget = this;
                            triggerZoneEntry(ship);
                        }
                    } else {
                        if (ship.nebulaTarget == this) {
                            ship.nebulaTarget = null;
                            triggerZoneExit(ship);
                        }
                    }
                    break;
                case TrekObject.OBJ_PULSAR:
                    if (TrekMath.getDistance(ship, this) <= effectRadius) {
                        if (ship.pulsarTarget != this) {
                            ship.pulsarTarget = this;
                            triggerZoneEntry(ship);
                        }
                    } else {
                        if (ship.pulsarTarget == this) {
                            ship.pulsarTarget = null;
                            triggerZoneExit(ship);
                        }
                    }
                    break;
                case TrekObject.OBJ_QUASAR:
                    if (TrekMath.getDistance(ship, this) <= effectRadius) {
                        if (ship.quasarTarget != this) {
                            ship.quasarTarget = this;
                            triggerZoneEntry(ship);
                        }
                    } else {
                        if (ship.quasarTarget == this) {
                            ship.quasarTarget = null;
                            triggerZoneExit(ship);
                        }
                    }
                    break;
                default:
            }

            // tholean space jiggle
            if (name.equals("Tholean Space") && TrekMath.getDistance(this, ship) <= effectRadius) {
                Random gen = new Random();
                int xDisp = gen.nextInt() % 3 + 1;  // 1 to 3 units
                int yDisp = gen.nextInt() % 3 + 1;
                int zDisp = gen.nextInt() % 3 + 1;

                ship.point.x += xDisp;
                ship.point.y += yDisp;
                ship.point.z += zDisp;
            }
        }

        Vector objects = currentQuadrant.getAllObjectsInRange(this, effectRadius + 500);
        for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();

            if (!TrekUtilities.isObjectShip(curObj) && !TrekUtilities.isGalacticObject(curObj)) { // skip all ships, planets, sbs, etc.
                if (TrekMath.getDistance(this, curObj) <= effectRadius) {
                    if (curObj.zoneEffect != this) {
                        curObj.zoneEffect = this;
                    }
                } else {
                    if (curObj.zoneEffect == this) {
                        curObj.zoneEffect = null;
                    }
                }
            }

        }

        if (type == TrekObject.OBJ_PULSAR) {
            if (stateChangeTicks == 0) {
                if (active) {
                    active = false;
                } else {
                    active = true;
                }

                Random newTime = new Random();
                stateChangeTicks = Math.abs(newTime.nextInt() % 48) + 12;  // 3 to 15 seconds
            } else {
                stateChangeTicks--;
            }

            if (this.name.equals("Markarian 205")) active = true;
        }
    }

    protected void triggerZoneEntry(TrekShip targetShip) {
        switch (this.type) {
            case TrekObject.OBJ_ORGANIA:
                targetShip.parent.hud.sendMessage("Weapons ineffective within Organia space.");
                targetShip.organiaModifier = true;
                break;
            case TrekObject.OBJ_ASTEROIDBELT:
                targetShip.parent.hud.sendMessage("Now entering " + this.name + " ...");
                targetShip.parent.hud.asteroidIndicator(true);
                break;
            case TrekObject.OBJ_NEBULA:
                targetShip.parent.hud.sendMessage("Now entering the " + this.name + ".  Visibility reduced, shields ineffective.");
                targetShip.parent.hud.nebulaIndicator(true);
                break;
            case TrekObject.OBJ_PULSAR:
                if (this.name.equals("Markarian 205")) {
                    targetShip.parent.hud.sendMessage("Now affected by " + this.name + ".  Cloaking is ineffective.");
                } else {
                    targetShip.parent.hud.sendMessage("Now affected by the " + this.name + ".  Cloaking is periodically ineffective.");
                }
                targetShip.parent.hud.pulsarIndicator(true);
                break;
            case TrekObject.OBJ_QUASAR:
                targetShip.parent.hud.sendMessage("Now affected by the " + this.name + ".  Cloaking is ineffective.");
                targetShip.parent.hud.quasarIndicator(true);
                break;
            default:

        }
    }

    protected void triggerZoneExit(TrekShip targetShip) {
        switch (this.type) {
            case TrekObject.OBJ_ORGANIA:
                targetShip.parent.hud.sendMessage("Now leaving Organian space.");
                targetShip.organiaModifier = false;
                break;
            case TrekObject.OBJ_ASTEROIDBELT:
                targetShip.parent.hud.sendMessage("Now exiting " + this.name + ".");
                targetShip.parent.hud.asteroidIndicator(false);
                break;
            case TrekObject.OBJ_NEBULA:
                targetShip.parent.hud.sendMessage("Now leaving the " + this.name + ".");
                targetShip.parent.hud.nebulaIndicator(false);
                break;
            case TrekObject.OBJ_PULSAR:
                if (this.name.equals("Markarian 205")) {
                    targetShip.parent.hud.sendMessage("No longer affected by " + this.name + ".");
                } else {
                    targetShip.parent.hud.sendMessage("No longer affected by the " + this.name + ".");
                }
                targetShip.parent.hud.pulsarIndicator(false);
                break;
            case TrekObject.OBJ_QUASAR:
                targetShip.parent.hud.sendMessage("No longer affected by the " + this.name + ".");
                targetShip.parent.hud.quasarIndicator(false);
                break;
            default:

        }
    }

    protected void applyZoneEffects(TrekShip targetShip) {
        switch (this.type) {
            case TrekObject.OBJ_ASTEROIDBELT:
                if (Math.abs(targetShip.warpSpeed) > 8) {
                    targetShip.applyDamage(Math.abs(targetShip.warpSpeed) * 7.5, this, "asteroids", false);  // trying new formula
                }
                break;
            default:
        }
    }
}