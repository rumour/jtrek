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

import java.util.*;

/**
 * Represents a ship in the game.
 *
 * @author Joe Hopkinson
 */
public abstract class TrekShip extends TrekObject {
    // A reference to the parent TrekPlayer.
    public TrekPlayer parent;

    public String fullClassName;
    public String shipType;

    // Various targets that we need to keep track of.
    public TrekObject scanTarget;
    public TrekObject interceptTarget;
    public TrekObject lockTarget;
    public TrekPlanet orbitTarget;
    public TrekStarbase dockTarget;
    public TrekShipDebris shipDebrisTarget;
    public TrekZone zoneTarget;
    public TrekZone pulsarTarget;
    public TrekZone quasarTarget;
    public TrekStar starTarget;
    public TrekPlanet planetTarget;
    public TrekZone asteroidTarget;
    public TrekZone nebulaTarget;
    public TrekObject tractorBeamTarget;

    // Flags.
    public boolean raisingShields;
    public boolean selfDestruct;
    public int selfDestructCountdown;
    public boolean shipDestroyed;
    public boolean cloaked;
    public boolean locked;
    public boolean doTranswarp;
    public boolean transwarpEngaged;
    public boolean lifeSupportFailing;
    public boolean loading;
    public boolean irreversableDestruction;
    public boolean organiaModifier;

    protected boolean torpCoolMsgSent = false;
    protected boolean phaserCoolMsgSent = false;
    protected boolean torpsOutMsgSent = false;
    protected boolean repairLSMsgSent = false;

    // For class display in the roster.
    public String classLetter = "?";

    // For intercepting coordinate data
    //public boolean interceptingCoord;
    public Trek3DPoint intCoordPoint;

    // Tracking where the last message came from
    public Trek3DPoint msgPoint;

    // Message jamming
    public boolean jamShips[];
    public boolean jamGlobal;

    // Timeouts and counters.
    public boolean wormholeTransfer;
    public int whTimeElapsed;
    public int transwarpCounter;
    public int warpenUsed = 0;
    public int asteroidTickCounter;

    // Orbiting and Docking related.
    public boolean orbiting;
    public boolean orbitable;
    public int orbitDuration;

    public boolean dockable;
    public boolean docked;
    public int dockDuration;

    public int orbitDmgCounter;

    // Crystal related.
    public int maxCrystalStorage;
    public int currentCrystalCount;
    public int crystalsReceived;

    // Overwarp / Damage warp related
    public double maxWarpSpeedInSecond;
    public int warpTickCounter;

    // General Ship
    public double warpSpeed;
    public int warpUsageRatio;
    public int damageWarp;
    public int crew;
    public String homePlanet;

    public int damageControl;
    public int warpEnergy;
    public int impulseEnergy;
    public int maxImpulseEnergy;
    public int lifeSupport;
    public int antiMatter;
    public int torpedoes;
    public int phasers;
    public double maxCruiseWarp;
    public double maxTurnWarp;
    public double maxDamageWarp;
    public String shipClass;
    public int maxWarpEnergy;
    public double preTranswarpSpeed;
    public boolean transwarp;
    public boolean transwarpTracking;
    public int transwarpDirection;
    public int visibility;

    // Cloak
    public boolean cloak;
    public int cloakTimeCurrent;
    public int cloakTime;
    public int cloakPower;
    public int cloakRegeneration;
    public boolean cloakBurnt;
    public int cloakCounter;

    // Weapons.
    public static final int PHASER_NORMAL = 1;
    public static final int PHASER_EXPANDINGSPHEREINDUCER = 2;
    public static final int PHASER_AGONIZER = 3;
    public static final int PHASER_TELEPORTER = 4;
    public static final int PHASER_DISRUPTOR = 5;

    public static final int PHASER_FIREWIDE = 1;
    public static final int PHASER_FIRENARROW = 2;

    public static final int TORPEDO_PHOTON = 1;
    public static final int TORPEDO_PLASMA = 2;
    public static final int TORPEDO_BOLTPLASMA = 3;
    public static final int TORPEDO_OBLITERATOR = 4;
    public static final int TORPEDO_VARIABLESPEEDPLASMA = 5;
    public static final int TORPEDO_NORMAL = 6;
    public static final int TORPEDO_WARHEAD = 7;
    public static final int TORPEDO_ONEHITWARHEAD = 8;

    public static final int MOVEMENT_FORWARD = 1;
    public static final int MOVEMENT_REVERSE = 2;

    public int phaserType;
    public int maxPhaserBanks;
    public int minPhaserRange;
    public int phaserFireType;

    public int torpedoType;
    public int torpedoCount;
    public int maxTorpedoBanks;
    public int maxTorpedoStorage;
    public int minTorpedoRange;
    public int maxTorpedoRange;

    public boolean drones;
    public int droneCount;
    public int maxDroneStorage;
    public int droneStrength;
    public boolean variableSpeed;
    public int droneSpeed;

    public boolean mines;
    public int mineCount;
    public int maxMineStorage;
    public int mineStrength;

    public int mineFireTimeout;
    public int droneFireTimeout;
    public int torpFireTimeout;
    public int cloakFireTimeout;
    public int phaserFireTimeout;
    public int phaserEnergyReturned;
    public int ramTimeout;
    public int buoyTimeout;
    public int saveTimeout;

    public int torpedoWarpSpeed;
    public boolean torpedoWarpSpeedAuto = true;

    // misc devices
    public boolean corbomite;
    public boolean iridium;
    public boolean lithium;
    public boolean magnabuoy;
    public boolean neutron;
    public boolean seeker;
    public int corbomiteTimeout;
    public int lithiumTimeout;
    public int magnabuoyTimeout;
    public int seekerTimeout;

    // Scoring
    public int gold;

    public int damageGiven;
    public int bonus;
    public int damageReceived;
    public int breakSaves;
    public int conflicts;
    public Hashtable activeConflicts;

    public int totalDamageGiven;
    public int totalDamageReceived;
    public int totalBonus;

    // Odometer / statistic stuff
    public double unitsTraveled;
    public int torpsFired;
    public int minesDropped;
    public int dronesFired;
    public int secondsPlayed;
    public long dateLaunched;

    // For the announcements.
    public long lastLogin;
    public long lastWarning;

    // For borg exposure
    public int borgExposureTime = 0;

    Random gen;

    // Preserving ship heading when pitch is -/+ 90.
    public int previousHeading = -1;

    // keeping track of last seen coordinates
    public HashMap scannedHistory = new HashMap(50);

    // unique shipID value - used to store/retrieve this ship to/from database
    public int dbShipID;
    public boolean dbShipAlive = true;

    // flag to indicate whether the ship was launched in teamplay mode or not
    protected boolean ctfShip = false;

    // escape pod
    private boolean escapeCountdownEngaged;
    private int escapeCountdown = 5;
    private static final String ESCAPE_MSG = "Launching escape pod in ";

    public TrekShip(TrekPlayer parentin, String scanLetter) {
        super(parentin.shipName, scanLetter, 1000, 1000, 1000);
        parent = parentin;
        scanTarget = null;
        lockTarget = null;
        interceptTarget = null;
        warpSpeed = 0;
        damageControl = 0;
        damage = 0;
        shields = 0;
        lifeSupport = 100;
        antiMatter = 5000;
        gold = 0;
        selfDestructCountdown = 27;
        phaserFireType = PHASER_FIRENARROW;
        type = OBJ_SHIP;
        wormholeTransfer = false;
        loading = false;
        crystalsReceived = 0;
        droneSpeed = 0;
        irreversableDestruction = false;
        // only define for lower-case ship slots for now
        jamShips = new boolean[62];
        jamGlobal = false;
        orbitDmgCounter = 0;
        dateLaunched = Calendar.getInstance().getTimeInMillis();

        // Initially set the ram timeout to 0, so you can ram as soon as you get into the game.
        ramTimeout = 0;

        // Default warp usage ratio is 1:1.  Specify other in extended classes.
        warpUsageRatio = 1;

        buoyTimeout = 0;
        seekerTimeout = 180;
        lithiumTimeout = 300;

        activeConflicts = new Hashtable();
        gen = new Random();

        if (TrekServer.isTeamPlayEnabled()) ctfShip = true;
    }

    /**
     * This method is called every 1/4 second.  A 'tick'.
     */
    public void doShipTickUpdate() {
        doClassSpecificTick();

        // clear any error message flags; this is so that a keymap won't send the same error
        // bazillions of times; i.e. there's a torpfiretimeout and keymap is TTTTTTTTt -- keeps
        // from sending torp cooling message 8 times; only sends it once instead
        if (torpCoolMsgSent) torpCoolMsgSent = false;
        if (phaserCoolMsgSent) phaserCoolMsgSent = false;
        if (torpsOutMsgSent) torpsOutMsgSent = false;

        // reset life support message flag if ship leaves dock/orbit
        if (!docked && !orbiting) {
            repairLSMsgSent = false;
        }

        clearIntercept();

        clearScanTarget();

        clearWeaponsLock();

        checkDisengageTrkXwarp();

        // Conflict Management
        manageConflicts();

        reduceAntimatter();

        checkOverwarpDmgWarp();

        // check for damage level, and trigger appropriate flags
        lifeSupportFailing = damage > 100;

        if (damage > 200) {
            doDestruction(true);
        }

        // clear intercept if speed greater than turn warp
        if (!transwarpEngaged && Math.abs(warpSpeed) > maxTurnWarp && (interceptTarget != null || intCoordPoint != null)) {
            interceptTarget = null;
            intCoordPoint = null;
        }

        if (lithiumTimeout <= 0) {
            if (!lithium) {
                lithium = true;
            }
        }

        if (seekerTimeout <= 0) {
            if (!seeker) {
                seeker = true;
            }
        }
    }

    private void checkOverwarpDmgWarp() {
        // check over/damage warp related logic
        if (transwarpEngaged) {
            if (Math.abs(warpSpeed) - 30 > maxWarpSpeedInSecond)
                maxWarpSpeedInSecond = Math.abs(warpSpeed) - 30;
        } else {
            if (Math.abs(warpSpeed) > maxWarpSpeedInSecond)
                maxWarpSpeedInSecond = warpSpeed;
        }

        warpTickCounter++;

        if (warpTickCounter % 4 == 0) {
            if (Math.abs(maxWarpSpeedInSecond) > maxCruiseWarp) {
                int totalNormalCrystals = new Double(Math.ceil(Math.abs(maxWarpSpeedInSecond) - maxCruiseWarp) + 1).intValue();
                burnCrystals(totalNormalCrystals);
            }

            if (Math.abs(maxWarpSpeedInSecond) > damageWarp) {
                int totalDamageWarpDamage = new Double((Math.abs(maxWarpSpeedInSecond) - damageWarp) * 5).intValue();
                damage += totalDamageWarpDamage;
            }

            warpTickCounter = 0;
            maxWarpSpeedInSecond = 0;
        }
    }

    private void reduceAntimatter() {
        // Antimatter reduction.
        if (getAvailablePower() < getWarpEnergy()) {
            double warpen = getWarpEnergy() - getAvailablePower();
            warpenUsed += warpen;
        }

        if (warpenUsed > 400) {
            antiMatter -= 1;
            warpenUsed -= 400;

            if (antiMatter < 0) {
                antiMatter = 0;
            }
        }
    }

    private void checkDisengageTrkXwarp() {
        // Check for transwarp tracking disengage.
        if (this.transwarp && this.transwarpEngaged && this.transwarpTracking) {
            // check for lower than warp 4 preXwarp speed - if less, then incur possible structural damage
            // 40 ticks in a 10 second transwarp trip ... total damage should be ~20 ints...
            // so randomize with a 40 % chance of incurring an int per tick... max damage would be 40, avg
            // damage would be 16
            if (Math.abs(preTranswarpSpeed) < 4) {
                parent.hud.sendTopMessage("Structural damage occurring to ship's hull");
                Random gen = new Random();
                int damageOdds = Math.abs(gen.nextInt() % 100);
                if (damageOdds <= 40)
                    damage++;
            }

            // verify something had been intercepted prior to transwarp engage
            if ((transwarpDirection == TrekShip.MOVEMENT_FORWARD) && ((interceptTarget != null) || (intCoordPoint != null))) {
                if (interceptTarget != null) {
                    double distance = TrekMath.getDistance(this, interceptTarget);
                    if (!TrekUtilities.isObjectBlackhole(interceptTarget) && TrekMath.getDistanceMoved(warpSpeed) > distance) {
                        //calculate the speed necessary to drop the ship near the target object
                        distance = gen.nextInt() % 50 + distance;
                        warpSpeed = TrekMath.getWarpToDistance(distance);
                    }
                    if (TrekMath.getDistance(this, interceptTarget) < 100) {
                        transwarpEngaged = false;
                        transwarpDirection = 0;
                        doTranswarp = false;
                        parent.hud.sendMessage("Transwarp deactivated.");
                        warpSpeed = preTranswarpSpeed;
                        transwarpCounter = 13;
                        interceptTarget = null;
                    }
                } else {
                    double distance = TrekMath.getDistance(this.point, intCoordPoint);
                    if (TrekMath.getDistanceMoved(warpSpeed) > distance) {
                        //calculate the speed necessary to drop the ship within 50 units or so of the target point
                        distance = gen.nextInt() % 50 + distance;
                        warpSpeed = TrekMath.getWarpToDistance(distance);
                    }

                    if (TrekMath.getDistance(this.point, intCoordPoint) < 100) {
                        transwarpEngaged = false;
                        transwarpDirection = 0;
                        doTranswarp = false;
                        parent.hud.sendMessage("Transwarp deactivated.");
                        warpSpeed = preTranswarpSpeed;
                        transwarpCounter = 13;
                        intCoordPoint = null;
                    }
                }
            }
        }
    }

    private void clearWeaponsLock() {
        // Clear a lock if cloaked or out of range.
        if (isWeaponsLocked()) {
            if (!currentQuadrant.isInQuadrant(lockTarget))
                lockTarget = null;
            else if (!TrekMath.canScanShip(this, lockTarget))
                lockTarget = null;
            else if (TrekMath.getDistance(this, lockTarget) > (float) maxTorpedoRange + .5)
                lockTarget = null;
        }
    }

    private void clearScanTarget() {
        // Clear the scanTarget...
        if (isScanning()) {
            if (!currentQuadrant.isInQuadrant(scanTarget))
                scanTarget = null;
            else if (!TrekMath.canScanShip(this, scanTarget))
                scanTarget = null;

            if (scanTarget == null)
                parent.hud.clearScanner();
        }
    }

    private void clearIntercept() {
        // Clear the interceptTarget...
        if (isIntercepting()) {
            if (!currentQuadrant.isInQuadrant(interceptTarget))
                interceptTarget = null;
            else if (TrekUtilities.isObjectShip(interceptTarget) && !TrekMath.canScanShip(this, interceptTarget))
                interceptTarget = null;

            if (interceptTarget != null) {
                Trek3DPoint current = new Trek3DPoint();
                Trek3DPoint target = new Trek3DPoint();

                target.x = interceptTarget.point.x;
                target.y = interceptTarget.point.y;
                target.z = interceptTarget.point.z;

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
            }
        }
    }

    /**
     * Given an amount, will burn the passed number of crystals.
     * <p/>
     * First from the ships current crystal count, then from the warp energy.
     *
     * @param amount int Amount of crystals to burn.
     */
    public void burnCrystals(int amount) {
        if (currentCrystalCount <= 0) {
            warpEnergy -= amount;
        } else {
            currentCrystalCount -= amount;
        }

        if (currentCrystalCount < 0) {
            warpEnergy += currentCrystalCount;
            currentCrystalCount = 0;
        }

        if (warpEnergy < 0) {
            warpEnergy = 0;
        }
    }

    /**
     * This method is called every one second.
     */
    public void doShipSecondUpdate() {
        if (whTimeElapsed < 60)
            whTimeElapsed++;

        handleTranswarp();

        handleCloak();

        decrementTimeouts();

        // Raise the shields.
        if (raisingShields) {
            raiseShields();
        }

        handleSelfDestruct();

        // Fail the life support.
        // 1% per second for damage over 100 up to 150.
        // 2% per second for damage over 150.
        // 3% for 200
        if (lifeSupportFailing) {
            lifeSupport--;
            totalDamageReceived++;

            if (damage > 150)
                lifeSupport--;

            if (damage == 200)
                lifeSupport--;

            if (lifeSupport <= 0) {
                lifeSupport = 0;
                doDestruction(false);
            }
        }

        handleDmgControl();

        handleOrbitDmg();

        handleOrbit();

        handleDock();

        bleedGold();

        secondsPlayed++;

        // exposure time increases at rate of 1 / tick, and decreases at 1 / sec.
        if (borgExposureTime > 0) borgExposureTime--;

        // check for removal from zone targets; in case of going through black hole or something
        // while being affected (for instance Xelor Asteroids)
        clearPulsarTarget();
        clearQuasarTarget();
        clearAsteroidTarget();
        clearNebulaTarget();

        handleEscapePod();
    }

    private void handleEscapePod() {
        if (escapeCountdownEngaged) {
            escapeCountdown--;
            if (escapeCountdown > 1) {
                parent.hud.sendMessage(ESCAPE_MSG + escapeCountdown + " seconds");
            } else if (escapeCountdown == 1) {
                parent.hud.sendMessage(ESCAPE_MSG + escapeCountdown + " second");
            } else {
                escapeCountdownEngaged = false;
                escapeCountdown = 5;
                doLaunchEscapePod();
            }
        }
    }

    private void clearNebulaTarget() {
        if (nebulaTarget != null) {
            if (currentQuadrant.getObjectByScanLetter(nebulaTarget.scanLetter) != null) {
                if (currentQuadrant == nebulaTarget.currentQuadrant) {
                    if (!(TrekMath.getDistance(this, nebulaTarget) <= nebulaTarget.effectRadius)) {
                        nebulaTarget.triggerZoneExit(this);
                        nebulaTarget = null;
                    }
                } else {
                    nebulaTarget.triggerZoneExit(this);
                    nebulaTarget = null;
                }
            } else {
                if (TrekMath.getDistance(this, nebulaTarget) > nebulaTarget.effectRadius) // adding this line, to try to preserve comet settings
                    nebulaTarget = null;  // i think this may be clearing the embedded zone within comets
            }
        }
    }

    private void clearAsteroidTarget() {
        if (asteroidTarget != null) {
            if (currentQuadrant.getObjectByScanLetter(asteroidTarget.scanLetter) != null) {
                if (currentQuadrant == asteroidTarget.currentQuadrant) {
                    if (!(TrekMath.getDistance(this, asteroidTarget) <= asteroidTarget.effectRadius)) {
                        asteroidTarget.triggerZoneExit(this);
                        asteroidTarget = null;
                    }
                } else {
                    asteroidTarget.triggerZoneExit(this);
                    asteroidTarget = null;
                }
            } else {
                asteroidTarget = null;
            }
        }
    }

    private void clearQuasarTarget() {
        if (quasarTarget != null) {
            if (currentQuadrant.getObjectByScanLetter(quasarTarget.scanLetter) != null) {
                if (currentQuadrant == quasarTarget.currentQuadrant) {
                    if (!(TrekMath.getDistance(this, quasarTarget) <= quasarTarget.effectRadius)) {
                        quasarTarget.triggerZoneExit(this);
                        quasarTarget = null;
                    }
                } else {
                    quasarTarget.triggerZoneExit(this);
                    quasarTarget = null;
                }
            } else {
                quasarTarget = null;
            }
        }
    }

    private void clearPulsarTarget() {
        if (pulsarTarget != null) {
            if (currentQuadrant.getObjectByScanLetter(pulsarTarget.scanLetter) != null) {
                if (currentQuadrant == pulsarTarget.currentQuadrant) {
                    if (!(TrekMath.getDistance(this, pulsarTarget) <= pulsarTarget.effectRadius)) {
                        pulsarTarget.triggerZoneExit(this);
                        pulsarTarget = null;
                    }
                } else {
                    pulsarTarget.triggerZoneExit(this);
                    pulsarTarget = null;
                }
            } else {
                pulsarTarget = null;
            }
        }
    }

    private void bleedGold() {
        // check to see if gold chunk should be dropped ... if ship should 'bleed'
        // set odds to 1 in 40
        if (damage > 100 && gold >= 1000) {
            Random goldDrop = new Random();
            if (Math.abs(goldDrop.nextInt()) % 40 == 1) {
                // drop a chunk from 4 to 8% of total ship gold
                double goldPercent = (Math.abs(goldDrop.nextInt() % 4) + 4) / 100.0;
                int randomHeading = Math.abs(goldDrop.nextInt() % 360); //
                int randomPitch = goldDrop.nextInt() % 90;
                int randomDistance = Math.abs(goldDrop.nextInt() % 150) + 150; // randomly make distance 150 to 300 units from ship
                int goldDropped = (int) Math.round(gold * goldPercent);

                Trek3DPoint target = new Trek3DPoint();
                randomPitch = 90 - randomPitch;
                // calculate end points
                target.x = point.x + (float) (randomDistance * Math.sin(Math.toRadians(randomPitch)) * Math.sin(Math.toRadians(randomHeading)));
                target.y = point.y + (float) (randomDistance * Math.sin(Math.toRadians(randomPitch)) * Math.cos(Math.toRadians(randomHeading)));
                target.z = point.z + (float) (randomDistance * Math.cos(Math.toRadians(randomPitch)));

                currentQuadrant.addObject(new TrekGold(goldDropped, this, target.x, target.y, target.z));

                gold -= goldDropped;
            }
        }
    }

    private void handleOrbitDmg() {
        if ((orbitable || dockable) && !(orbiting || docked)) {
            orbitDmgCounter++;

            if (orbitDmgCounter == 10) {
                parent.hud.sendMessageBeep("Captain, the hull is getting extremely hot ...");
            }

            if (orbitDmgCounter == 15) {
                if (orbitable) {
                    parent.hud.sendMessageBeep("Captain, if we don't orbit now, I can't guarantee the safety of the ship!");
                } else {
                    parent.hud.sendMessageBeep("Captain, if we don't dock now, I can't guarantee the safety of the ship!");
                }
            }

            // do damage after 20 seconds of not completing docking/orbiting procedure, and every 5 secs after
            if ((orbitDmgCounter == 20) || (orbitDmgCounter > 20 && ((orbitDmgCounter - 20) % 5 == 0))) {
                Random dmgRand = new Random();
                // start damage at around 20 ints per occurrence, increase to a max of about 40
                int dmgAmount = Math.abs(dmgRand.nextInt() % 5) + 15;
                if (orbitDmgCounter > 20) {
                    int extraDmg = Math.abs(dmgRand.nextInt() % (orbitDmgCounter - 20));
                    dmgAmount += extraDmg;
                }

                TrekDamageStat orbitDmg;
                if (orbitable) {
                    orbitDmg = this.doAtmosphereDamage(orbitTarget, dmgAmount);
                    parent.hud.sendMessageBeep("Atmosphere causing " + dmgAmount + " structural damage!");
                } else { // also, sometimes ramming the starbases would trigger their wrath...
                    orbitDmg = this.doAtmosphereDamage(dockTarget, dmgAmount);
                    parent.hud.sendMessageBeep(">>>CRrruUNnchh<<<  We've run into " + dockTarget.name + "! " + dmgAmount + " damage!");
                }

                parent.hud.drawDamageReceivedStat(orbitDmg);
            }
        }
    }

    private void handleDock() {
        if (docked) {
            // Pay a cease fire balance, if needed.
            if (dockTarget.isAttacker(this)) {
                dockTarget.payBalance(this);
            } else {
                if (!dockTarget.isDisabled()) {
                    dockDuration += 1;
                    TrekStarbase base = dockTarget;

                    int totalTorpedoCount = torpedoCount + torpedoes;

                    if (base.givesTorps && totalTorpedoCount < maxTorpedoStorage) {
                        if (dockDuration > 5 && totalTorpedoCount < maxTorpedoStorage) {
                            loading = true;
                            torpedoCount++;
                            if (gold > 1000) {
                                if (torpedoType == TrekShip.TORPEDO_PLASMA) {
                                    gold -= 3;
                                    dockTarget.gold += 3;
                                } else {
                                    gold--;
                                    dockTarget.gold++;
                                }
                            }

                            parent.hud.setLoadMessage("Torpedoes Loaded: " + torpedoCount + "(" + maxTorpedoStorage + ")");
                        }

                        if (torpedoCount >= maxTorpedoStorage) {
                            parent.hud.setLoadMessage("Torpedoes Loaded: done");
                            parent.hud.loadMessageWaiting = true;
                            parent.hud.loadMessageTimeout = 5;
                            loading = false;
                        }
                    }

                    if (base.givesDrones && drones && droneCount < maxDroneStorage) {
                        if (dockDuration > 8) {
                            parent.hud.setLoadMessage("Drones Loaded: " + (maxDroneStorage - droneCount));
                            parent.hud.loadMessageWaiting = true;
                            parent.hud.loadMessageTimeout = 5;

                            while (droneCount < maxDroneStorage) {
                                droneCount++;
                                if (gold > 1000) {
                                    gold--;
                                    dockTarget.gold++;
                                }
                            }
                        }
                    }

                    if (base.givesMines && mines && mineCount < maxMineStorage) {
                        if (dockDuration > 10) {
                            parent.hud.setLoadMessage("Mines Loaded: " + (maxMineStorage - mineCount));
                            parent.hud.loadMessageWaiting = true;
                            parent.hud.loadMessageTimeout = 5;

                            while (mineCount < maxMineStorage) {
                                mineCount++;
                                if (gold > 1000) {
                                    gold--;
                                    dockTarget.gold++;
                                }
                            }
                        }
                    }

                    if (base.fixesLifeSupport && lifeSupport < 100) {
                        if (dockDuration > 5) {
                            if (lifeSupport >= 99) {
                                parent.hud.sendMessage("Your life support has been repaired.");
                            } else {
                                if (!repairLSMsgSent) {
                                    parent.hud.sendMessage("Your life support is being repaired.");
                                    repairLSMsgSent = true;
                                }
                            }
                            lifeSupport++;
                            if (gold > 1000) {
                                gold--;
                                dockTarget.gold++;
                            }
                        }
                    }

                    // restore escape pod to full vessel
                    if (this instanceof ShipEscapePod) {
                        ShipEscapePod pod = (ShipEscapePod) this;
                        if (pod.oldShipClass != null && !pod.oldShipClass.equals(""))
                            parent.changeShipClass(((ShipEscapePod) this).oldShipClass);
                    }
                }
            }
        }
    }

    private void handleOrbit() {
        // Handle orbiting of planets.
        if (orbiting) {
            if (orbitTarget.isAttacker(this)) {
                orbitTarget.payBalance(this);
            } else {
                if (!orbitTarget.isDisabled()) {
                    orbitDuration += 1;
                    TrekPlanet planet = orbitTarget;

                    int totalTorpedoCount = torpedoCount + torpedoes;

                    if (planet.givesTorps(this) && totalTorpedoCount < maxTorpedoStorage) {
                        if (orbitDuration > 5 && totalTorpedoCount < maxTorpedoStorage) {
                            loading = true;
                            torpedoCount++;
                            if (gold > 1000) {
                                if (torpedoType == TrekShip.TORPEDO_PLASMA) {
                                    gold -= 3;
                                    orbitTarget.gold += 3;
                                } else {
                                    gold--;
                                    orbitTarget.gold++;
                                }
                            }

                            parent.hud.setLoadMessage("Torpedoes Loaded: " + torpedoCount + "(" + maxTorpedoStorage + ")");
                        }

                        if (torpedoCount >= maxTorpedoStorage) {
                            parent.hud.setLoadMessage("Torpedoes Loaded: done");
                            parent.hud.loadMessageWaiting = true;
                            parent.hud.loadMessageTimeout = 5;
                            loading = false;
                        }
                    }

                    if (planet.givesDrones(this) && drones && droneCount < maxDroneStorage) {
                        if (orbitDuration > 8) {
                            parent.hud.setLoadMessage("Drones Loaded: " + (maxDroneStorage - droneCount));
                            parent.hud.loadMessageWaiting = true;
                            parent.hud.loadMessageTimeout = 5;

                            while (droneCount < maxDroneStorage) {
                                droneCount++;
                                if (gold > 1000) {
                                    gold--;
                                    orbitTarget.gold++;
                                }
                            }
                        }
                    }

                    if (planet.givesMines(this) && mines && mineCount < maxMineStorage) {
                        if (orbitDuration > 10) {
                            parent.hud.setLoadMessage("Mines Loaded: " + (maxMineStorage - mineCount));
                            parent.hud.loadMessageWaiting = true;
                            parent.hud.loadMessageTimeout = 5;

                            while (mineCount < maxMineStorage) {
                                mineCount++;
                                if (gold > 1000) {
                                    gold--;
                                    orbitTarget.gold++;
                                }
                            }
                        }
                    }

                    if (planet.givesAntimatter(this) && antiMatter < 5000) {
                        if (orbitDuration > 4) {
                            if ((5000 - antiMatter) < 100) {
                                antiMatter += orbitTarget.getAntiMatterChunk((5000 - antiMatter));
                            } else {
                                antiMatter += orbitTarget.getAntiMatterChunk(100);
                            }

                            if (gold > 1000) {
                                gold--;
                                orbitTarget.gold++;
                            }

                            if (antiMatter > 5000)
                                antiMatter = 5000;
                        }
                    }

                    if (planet.fixesCloak(this) && cloak && cloakBurnt) {
                        if (orbitDuration == 3) {
                            parent.hud.sendMessage("Your cloaking device is being repaired.");
                        }

                        if (orbitDuration > 20) {
                            parent.hud.sendMessage("Your cloaking device has been fixed.");

                            cloakBurnt = false;
                            cloakTimeCurrent = cloakTime;
                            if (gold > 1000) {
                                gold -= 10;
                                orbitTarget.gold += 10;
                            }
                        }
                    }

                    if (planet.fixesTransmitter(this) && parent.transmitterBurnt) {
                        if (orbitDuration == 3) {
                            if (gold >= 500) {
                                parent.hud.sendMessage("Your transmitter is being fixed.");
                            } else {
                                parent.hud.sendMessage("Repairs to transmitter require 500 gold, non-negotiable.");
                            }
                        }
                        if (orbitDuration > 10) {
                            if (gold >= 500) {
                                parent.transmitterBurnt = false;
                                parent.hud.sendMessage("Your transmitter has been fixed.");
                                gold -= 500;
                                orbitTarget.gold += 500;
                            }
                        }
                    }

                    if (planet.installsCloak(this) && shipClass.equals("II-A")) {
                        if (!cloak && orbitDuration > 3)
                            parent.hud.sendMessage("Your cloaking device is being installed.");

                        if (!cloak && orbitDuration > 20) {
                            cloak = true;
                            parent.hud.sendMessage("Your cloaking device has been installed.");
                        }
                    }

                    if (planet.installsTranswarp(this) && shipClass.equals("EXCEL")) {
                        if (!transwarp && orbitDuration > 4) {
                            transwarp = true;
                            parent.hud.sendMessage("Your transwarp drive has been installed.");
                        }
                    }

                    if (planet.givesCrystals(this) && getCrystalCount() > 0) {
                        if (orbitDuration > 5 && getCrystalCount() > 0) {
                            loading = true;

                            loadCrystals(1);
                            crystalsReceived++;
                            if (gold > 1000) {
                                gold--;
                                orbitTarget.gold++;
                            }

                            parent.hud.setLoadMessage("Receiving Crystals: " + crystalsReceived + "(" + getCrystalCount() + ")");

                        }

                        if (getCrystalCount() == 0) {
                            loading = false;
                            crystalsReceived = 0;
                            parent.hud.setLoadMessage("Receiving Crystals: done");
                            parent.hud.loadMessageWaiting = true;
                            parent.hud.loadMessageTimeout = 5;
                        }
                    }

                    if (planet.repairsLifeSupport(this) && lifeSupport < 100) {
                        if (orbitDuration > 5) {
                            if (lifeSupport >= 99) {
                                parent.hud.sendMessage("Your life support has been repaired.");
                            } else {
                                if (!repairLSMsgSent) {
                                    parent.hud.sendMessage("Your life support is being repaired.");
                                    repairLSMsgSent = true;
                                }
                            }
                            lifeSupport++;
                            if (gold > 1000) {
                                gold--;
                                orbitTarget.gold++;
                            }
                        }
                    }

                    // misc devices
                    if (planet.givesCorbomite(this) && !corbomite) {
                        if (orbitDuration >= 10) {
                            parent.hud.sendMessage("Loaded Corbomite device ...");
                            corbomite = true;
                        }
                    }

                    if (planet.givesIridium(this) && !iridium) {
                        if (orbitDuration >= 10) {
                            parent.hud.sendMessage("Loaded Iridium mine ...");
                            iridium = true;
                        }
                    }

                    if (planet.givesMagnabuoy(this) && !magnabuoy) {
                        if (orbitDuration >= 10) {
                            parent.hud.sendMessage("Loaded Magnabuoy ...");
                            magnabuoy = true;
                        }
                    }

                    if (planet.givesNeutron(this) && !neutron) {
                        if (orbitDuration >= 10) {
                            parent.hud.sendMessage("Loaded Neutron mine ...");
                            neutron = true;
                        }
                    }

                    handleSpecialPlanets(planet);
                }
            }
        } else {
            if (crystalsReceived > 0) crystalsReceived = 0;
        }
    }

    private void handleSpecialPlanets(TrekPlanet planet) {
        if (planet instanceof IPlanetProvidesShipUpgrade) {
            IPlanetProvidesShipUpgrade upgrade = (IPlanetProvidesShipUpgrade) planet;
            if (orbitDuration == upgrade.getOrbitDuration() && upgrade.isUpgradeAvailable(this)) {
                parent.hud.sendMessage(upgrade.getOrbitMessage());
            }
        }
    }

    private void handleDmgControl() {
        // Fix the ship.
        if (damageControl > 0) {
            double repairIE = Math.abs(gen.nextInt() % 540);
            double repairWEINT = Math.log(damageControl) / 15 * 100; //percentage chance of fixing
            double testWE = Math.abs(gen.nextInt() % 100) + 1;
            double testINT = Math.abs(gen.nextInt() % 100) + 1;

            if (repairIE < damageControl && impulseEnergy < maxImpulseEnergy) {
                impulseEnergy++;
            }

            if (testWE < repairWEINT && warpEnergy < maxWarpEnergy) {
                warpEnergy++;
            }

            if (testINT < repairWEINT && damage > 0) {
                damage--;
            }

            if (damage <= 100) {
                lifeSupportFailing = false;
            }
        }
    }

    private void handleSelfDestruct() {
        //Countdown Autodestruct.
        if (selfDestruct) {
            if (selfDestructCountdown == 27) {
                parent.hud.sendMessage("Captain entering auto-destruct code...");
            }

            if (selfDestructCountdown == 26) {
                parent.hud.sendMessage("First officer entering auto-destruct code...");
            }

            if (selfDestructCountdown > 3 && selfDestructCountdown < 26) {
                parent.hud.sendMessage("Countdown to auto-destruct: " + selfDestructCountdown + "...");
            }

            if (selfDestructCountdown == 10) {
                parent.hud.sendTopMessage("The crew looks at you nervously...");
            }

            if (selfDestructCountdown == 5) {
                irreversableDestruction = true;
                parent.hud.sendTopMessage("Only 5 seconds left and no power in the universe can stop it!");
            }

            if (selfDestructCountdown < 4 && selfDestructCountdown > 0) {
                parent.hud.clearMessage(2);
                parent.hud.sendMessage(selfDestructCountdown + "...");
            }

            if (selfDestructCountdown <= 0) {
                doDestruction(true);
            }

            selfDestructCountdown--;
        }
    }

    private void decrementTimeouts() {
        // Decrement the various timeouts.
        if (torpFireTimeout > 0) {
            torpFireTimeout--;
        }
        if (droneFireTimeout > 0) {
            droneFireTimeout--;
        }

        if (ramTimeout > 0) {
            ramTimeout--;
        }

        if (mineFireTimeout > 0) {
            mineFireTimeout--;
        }

        if (cloakFireTimeout > 0) {
            cloakFireTimeout--;
        }

        if (phaserFireTimeout > 0) {
            if (this.shipClass.equals("CL-13")) {
                phaserFireTimeout -= 3;
            } else if (this.shipClass.equals("WARBIRD")) {
                phaserFireTimeout--;
            } else {
                phaserFireTimeout -= 2;
            }

            if (phaserFireTimeout < 0) {
                phaserFireTimeout = 0;
            }
        }

        if (phaserEnergyReturned > 0) {
            phaserEnergyReturned -= 2;

            if (phaserEnergyReturned < 0) {
                phaserEnergyReturned = 0;
            }
        }

        if (buoyTimeout > 0) {
            buoyTimeout--;

            if (buoyTimeout < 0)
                buoyTimeout = 0;
        }

        if (saveTimeout > 0) {
            saveTimeout--;

            if (saveTimeout < 0)
                saveTimeout = 0;
        }

        corbomiteTimeout--;
        if (corbomiteTimeout <= 0) corbomiteTimeout = 0;

        lithiumTimeout--;
        if (lithiumTimeout <= 0) lithiumTimeout = 0;

        magnabuoyTimeout--;
        if (magnabuoyTimeout <= 0) magnabuoyTimeout = 0;

        seekerTimeout--;
        if (seekerTimeout <= 0) seekerTimeout = 0;
    }

    private void handleCloak() {
        // If the ship is cloaked.
        if (cloaked) {
            cloakTimeCurrent -= 1;
            if (cloakTimeCurrent == 10) {
                parent.hud.sendMessageBeep("WARNING!  Your cloaking device will burn out in 10 seconds!");
            }

            if (cloakTimeCurrent <= 0) {
                cloakTimeCurrent = 0;
                cloakBurnt = true;
                cloaked = false;
            }
        } else {
            if (cloakCounter == cloakRegeneration) {
                if (cloakBurnt) {
                    // nothing
                } else {
                    cloakTimeCurrent += 1;

                    if (cloakTimeCurrent > cloakTime) {
                        cloakTimeCurrent = cloakTime;
                    }

                    cloakCounter = 1;
                }
            } else {
                cloakCounter++;
            }
        }
    }

    private void handleTranswarp() {
        // Transwarp functionality.
        if (doTranswarp) {
            if (transwarpCounter > 10) {
                parent.hud.sendMessage("Transwarp will engage in " + (transwarpCounter - 10) + "...");
            }

            if ((transwarpCounter == 10) && (doTranswarp)) {
                if ((Math.abs(warpSpeed) < 3) || (getAvailablePower() < 30) || (warpEnergy < 30)) {
                    if (Math.abs(warpSpeed) < 3) {
                        parent.hud.sendMessage("Good Morning Captain.");
                    } else {
                        parent.hud.sendMessage("Insufficient energy - requires 30 Power Unused, and 30 Warp Energy.");
                    }

                    doTranswarp = false;

                } else {
                    parent.hud.sendMessage("Transwarp engaged ...");
                    transwarpEngaged = true;
                    preTranswarpSpeed = warpSpeed;

                    // disable tractor beam if active
                    if (tractorBeamTarget != null) doTractorBeam();

                    // clear intercept if tracking transwarp set to directly intercept a ship
                    if (interceptTarget != null) {
                        if (TrekUtilities.isObjectShip(interceptTarget)) {
                            interceptTarget = null;
                        }
                    }

                    // Fixed negative warps adding 30; now they subtract 30
                    if (warpSpeed >= 0) {
                        warpSpeed += 30;
                        transwarpDirection = TrekShip.MOVEMENT_FORWARD;
                    } else {
                        warpSpeed -= 30;
                        transwarpDirection = TrekShip.MOVEMENT_REVERSE;
                    }

                    // Fixed issue of repairing back to full xtals after xwarp.
                    if (currentCrystalCount > 0) {
                        currentCrystalCount -= 30;

                        if (currentCrystalCount < 0) {
                            warpEnergy += currentCrystalCount;
                            currentCrystalCount = 0;
                        }
                    } else {
                        warpEnergy -= 30;
                    }
                }
            }

            if (transwarpCounter <= 0) {
                transwarpEngaged = false;
                transwarpDirection = 0;
                doTranswarp = false;
                warpSpeed = preTranswarpSpeed;
                parent.hud.sendMessage("Transwarp deactivated.");

                transwarpCounter = 13;
            }

            transwarpCounter -= 1;
        }
    }

    private void doLaunchEscapePod() {
        // deduct half the gold from the ship
        gold = gold / 2;

        // create a ship debris with 1k + 10% of half the gold
        TrekShipDebris debris = new TrekShipDebris(this, 1000 + gold / 10);
        currentQuadrant.addObject(debris);

        // launch the escape pod
        parent.changeShipClass("x");
    }

    /**
     * Increase the warp speed of the ship by the given amount.
     *
     * @param amount The amount to increase the warp speed.
     */
    public void increaseSpeed(double amount) {
        setWarp(warpSpeed + amount);
    }

    /**
     * Decrease the warp speed of the ship by the given amount.
     *
     * @param amount The amount to decrease the warp speed.
     */
    public void decreaseSpeed(double amount) {
        setWarp(warpSpeed + amount);
    }

    public void damageControlOff() {
        damageControl = 0;
    }

    public void holdShieldLevel() {
        raisingShields = false;
    }

    public Trek3DVector getNewDirectionVector() {
        try {

            if (isIntercepting()) {
                /*
                     * Shortly after release for beta, the NegaVerse was coined.
                     * A bug existed below that would send you to X:0 Y:0 Z:0, aptly named the NegaVerse.
                     * You could not move.  And you could see everything.
                     * The direction vector would hit NaN as soon as you got within 0 distance, hence the
                     * if statement below.
                     *
                     * Long live the NegaVerse.
                     *
                     */

                Trek3DPoint current;
                Trek3DPoint target;

                if (interceptTarget != null) {
                    if (TrekMath.getDistance(this, interceptTarget) < scanRange && new Double(TrekMath.getDistance(this, interceptTarget)).intValue() != 0) {
                        current = new Trek3DPoint(point);
                        target = new Trek3DPoint(interceptTarget.point);
                        target.subtract(current);
                        vector.applyPoint(target);
                    }
                } else if (intCoordPoint != null) {
                    if ((TrekMath.getDistance(point, intCoordPoint) < scanRange) && (new Double(TrekMath.getDistance(point, intCoordPoint)).intValue() != 0)) {
                        current = new Trek3DPoint(point);
                        target = new Trek3DPoint(intCoordPoint);
                        target.subtract(current);
                        vector.applyPoint(target);
                    }
                }
            }

            Trek3DVector returnVector = new Trek3DVector(vector);
            double distance = TrekMath.getDistanceMoved(warpSpeed);
            unitsTraveled += Math.abs(distance);
            returnVector.normalize();
            returnVector.scaleUp(distance);

            return returnVector;
        } catch (Exception e) {
            TrekLog.logException(e);
            return new Trek3DVector(0, 0, 1);
        }
    }

    public void setWarp(double amount) {
        try {
            TrekLog.logDebug(parent.shipName + ": Setting warp to " + amount);

            // If we are transwarping don't do anything with warp.
            if (transwarpEngaged) {
                return;
            }

            amount = new Double(parent.hud.getFormattedWarpSpeed(amount));

            // If it's an all stop..
            if (amount == 0) {
                warpSpeed = 0;
                return;
            }

            // Set our warp speed to zero, and give back the power.
            warpSpeed = 0;

            // Now, get our amount, and how much power it will take to do it.
            double energyUsed = Math.abs(amount) * (5 * warpUsageRatio);

            // If we have enough energy, set it, and forget it...
            if (energyUsed <= getAvailablePower()) {
                warpSpeed = amount;
            } else {
                if (amount < 0)
                    warpSpeed = (getAvailablePower() / warpUsageRatio) * -.2;
                else
                    warpSpeed = (getAvailablePower() / warpUsageRatio) * .2;
            }

            // Check for loss of intercept.
            if (Math.abs(warpSpeed) > maxTurnWarp) {
                interceptTarget = null;
                intCoordPoint = null;
            }

            // If the warpspeed is not 0, then lose orbit and dock.
            if (warpSpeed != 0) {
                orbiting = false;
                orbitable = false;
                orbitTarget = null;
                orbitDuration = 0;

                docked = false;
                dockable = false;
                dockTarget = null;
                dockDuration = 0;

                parent.hud.setLoadMessage("");
            }

        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    public void setTurnWarp(int direction) {
        double targetWarp;
        if (direction == MOVEMENT_FORWARD) {
            targetWarp = maxTurnWarp;
        } else {
            targetWarp = maxTurnWarp * -1;
        }

        if (transwarpEngaged) {
            preTranswarpSpeed = targetWarp;

            switch (transwarpDirection) {
                case TrekShip.MOVEMENT_FORWARD:
                    warpSpeed = 30 + targetWarp;
                    break;
                case TrekShip.MOVEMENT_REVERSE:
                    warpSpeed = -30 + targetWarp;
                    break;
            }
        } else {
            setWarp(targetWarp);
        }
    }

    public void setCruiseWarp(int direction) {
        double targetWarp;
        if (direction == MOVEMENT_FORWARD) {
            targetWarp = maxCruiseWarp;
        } else {
            targetWarp = maxCruiseWarp * -1;
        }

        if (transwarpEngaged) {
            preTranswarpSpeed = targetWarp;

            switch (this.transwarpDirection) {
                case TrekShip.MOVEMENT_FORWARD:
                    warpSpeed = 30 + targetWarp;
                    break;
                case TrekShip.MOVEMENT_REVERSE:
                    warpSpeed = -30 + targetWarp;
                    break;
            }
        } else {
            setWarp(targetWarp);
        }
    }

    public void alterHeading(int hdgAmount, int pchAmount) {
        int newHeading = Integer.valueOf(getHeading());

        newHeading += hdgAmount;
        newHeading %= 360;

        String pitchStr = getPitch();

        if (pitchStr.indexOf('+') != -1) {
            pitchStr = pitchStr.substring(1, pitchStr.length());
        }

        int newPitch = Integer.valueOf(pitchStr);

        newPitch += pchAmount;

        if (newPitch > 90)
            newPitch = 90;

        if (newPitch < -90)
            newPitch = -90;


        changeHeading(newHeading, newPitch);
    }

    public void scan(String thisLetter) {
        if (parent.hud.showPlayerPage > 0) {
            parent.hud.clearScanner();
            parent.hud.showPlayerPage = 0;
        }

        TrekObject thisObj = currentQuadrant.getObjectByScanLetter(thisLetter);

        if (thisObj == null || TrekUtilities.isObjectShip(thisObj)) {
            parent.hud.sendMessage("Unknown object.");
            return;
        }

        if (!TrekMath.canScanShip(this, thisObj)) {
            parent.hud.sendMessage("Unknown object.");
            return;
        }

        if (parent.hud.showStats) {
            parent.hud.showStats = false;
            parent.hud.targetShip = null;
        }

        if (parent.hud.showOdometer) {
            parent.hud.showOdometer = false;
        }

        if (parent.hud.showingHelpScreen) {
            parent.hud.showHelpScreen = false;
            parent.hud.helpScreenLetter = "";
            parent.hud.showingHelpScreen = false;
        }

        if (parent.hud.showInventory) {
            parent.hud.showInventory = false;
        }

        // If the requested object is not what we are currently scanning, change the target.
        if (scanTarget != thisObj) {
            parent.hud.clearScanner();
            scanTarget = thisObj;
        }

        parent.hud.updateScanner();
    }

    public void showShipStats(String shipLetter) {
        TrekShip thisShip = TrekServer.getPlayerShipByScanLetter(shipLetter);

        if (thisShip == null)
            return;

        if (parent.hud.showStats && parent.hud.targetShip == thisShip) {
            return;
        }

        parent.hud.showShipStats(thisShip);

    }

    public void showOdometerPage() {
        parent.hud.showOdometerPage();
    }

    public void scanShip(String thisLetter) {
        if (parent.hud.showPlayerPage > 0) {
            parent.hud.clearScanner();
            parent.hud.showPlayerPage = 0;
        }

        if (parent.hud.showStats) {
            parent.hud.clearScanner();
            parent.hud.showStats = false;
        }

        if (parent.hud.showOdometer) {
            parent.hud.clearScanner();
            parent.hud.showOdometer = false;
        }

        if (parent.hud.showingHelpScreen) {
            parent.hud.showHelpScreen = false;
            parent.hud.helpScreenLetter = "";
            parent.hud.showingHelpScreen = false;
        }

        if (parent.hud.showStats) {
            parent.hud.showStats = false;
            parent.hud.targetShip = null;
        }

        if (parent.hud.showingHelpScreen) {
            parent.hud.showInventory = false;
        }

        if (parent.hud.showInventory) {
            parent.hud.showInventory = false;
        }

        TrekShip thisObj = (TrekShip) currentQuadrant.getShipByScanLetter(thisLetter);

        if (scanLetter.equals(thisLetter)) {
            parent.hud.sendMessage("Here's looking at you, kid...");
            return;
        }

        if (thisObj == null || !TrekUtilities.isObjectShip(thisObj)) {
            parent.hud.sendMessage("Unknown ship letter.");
            return;
        }

        if (TrekMath.canScanShip(this, thisObj)) {
            if (scanTarget != thisObj) {
                parent.hud.clearScanner();
                scanTarget = thisObj;
            }

            parent.hud.updateScanner();
        } else {
            parent.hud.sendMessage("Ship " + thisLetter + " not in the area.");
        }
    }

    public void stopScanning() {
        parent.hud.showPlayerPage = 0;
        scanTarget = null;
        parent.hud.clearScanner();
        if (parent.hud.showStats) {
            parent.hud.showStats = false;
            parent.hud.targetShip = null;
        }
        if (parent.hud.showOdometer) {
            parent.hud.showOdometer = false;
        }
        if (parent.hud.showingHelpScreen) {
            parent.hud.showHelpScreen = false;
            parent.hud.helpScreenLetter = "";
            parent.hud.showingHelpScreen = false;
        }
        if (parent.hud.showInventory) {
            parent.hud.showInventory = false;
        }
    }

    public void intercept() {
        try {
            double startHdg = vector.getHeading();
            double startPch = vector.getPitch();

            previousHeading = -1;

            // If the ship is in transwarp, do not allow modification of course.
            if (transwarpEngaged)
                return;

            if (isIntercepting()) {
                interceptTarget = null;
                intCoordPoint = null;
                return;
            }

            if (scanTarget == null) {
                parent.hud.sendMessage("You must be scanning an object to intercept.");
                interceptTarget = null;
                intCoordPoint = null;
                return;
            }

            interceptTarget = scanTarget;

            Trek3DPoint current = new Trek3DPoint();
            Trek3DPoint target = new Trek3DPoint();

            target.x = interceptTarget.point.x;
            target.y = interceptTarget.point.y;
            target.z = interceptTarget.point.z;

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

                if (Math.abs(warpSpeed) > maxTurnWarp) {
                    // check for high speed damage
                    applyTurnDamage(startHdg, startPch);
                }
            }
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    public void interceptCoords(Trek3DPoint cPoint) {
        interceptCoords(cPoint.x, cPoint.y, cPoint.z);
    }

    public void interceptCoords(float cX, float cY, float cZ) {
        try {
            double startHdg = vector.getHeading();
            double startPch = vector.getPitch();

            previousHeading = -1;

            if (transwarpEngaged) {
                return;
            }

            if (isIntercepting()) {
                interceptTarget = null;
                intCoordPoint = null;
            }

            intCoordPoint = new Trek3DPoint(cX, cY, cZ);

            Trek3DPoint current = new Trek3DPoint();
            Trek3DPoint target = new Trek3DPoint();

            target.x = cX;
            target.y = cY;
            target.z = cZ;

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

            // handle turn damage
            if (Math.abs(warpSpeed) > maxTurnWarp) {
                applyTurnDamage(startHdg, startPch);
            }
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    public void interceptMsg() {
        try {
            double startHdg = vector.getHeading();
            double startPch = vector.getPitch();

            previousHeading = -1;

            if (transwarpEngaged) {
                return;
            }

            if (isIntercepting()) {
                interceptTarget = null;
                intCoordPoint = null;
            }

            Trek3DPoint current = new Trek3DPoint();
            Trek3DPoint target = new Trek3DPoint();

            target.x = msgPoint.x;
            target.y = msgPoint.y;
            target.z = msgPoint.z;

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

            if (Math.abs(warpSpeed) > maxTurnWarp) {
                // do high speed turn damage
                applyTurnDamage(startHdg, startPch);
            }
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    public void interceptShipLastCoord(String targetLetter) {
        if (scanLetter.equals(targetLetter)) {
            parent.hud.sendMessage("Ever seen a dog chase its tail?");
            return;
        }

        TrekObject targetObj = currentQuadrant.getShipByScanLetter(targetLetter);

        if ((targetObj == null) || (!TrekUtilities.isObjectShip(targetObj))) {
            parent.hud.sendMessage("Unknown ship letter.");
            return;
        }

        TrekCoordHistory targetLastSeen = (TrekCoordHistory) scannedHistory.get(targetLetter);

        if ((targetLastSeen == null) || (!targetLastSeen.shipName.equalsIgnoreCase(targetObj.name))) {
            parent.hud.sendMessage("You haven't seen that ship on your scanner.");
            return;
        }

        interceptCoords(targetLastSeen.shipCoord);

    }

    public void interceptShipNoScan(String thisLetter) {
        TrekObject thisObj = currentQuadrant.getShipByScanLetter(thisLetter);

        if (scanLetter.equals(thisLetter)) {
            return;
        }

        if (thisObj == null || !TrekUtilities.isObjectShip(thisObj)) {
            parent.hud.sendMessage("Unknown ship letter.");
            return;
        }

        if (TrekMath.getDistance(this, thisObj) > this.scanRange) {
            return;
        }

        TrekShip ship = (TrekShip) thisObj;

        if (!ship.cloaked) {
            interceptCoords(ship.point.x, ship.point.y, ship.point.z);
        }
    }

    public void showShipLastCoord(String targetLetter) {
        if (scanLetter.equals(targetLetter)) {
            parent.hud.sendMessage("You are where you are, and you last saw yourself there.");
            return;
        }

        TrekObject targetObj = currentQuadrant.getShipByScanLetter(targetLetter);

        if ((targetObj == null) || (!TrekUtilities.isObjectShip(targetObj))) {
            parent.hud.sendMessage("Unknown ship letter.");
            return;
        }

        TrekCoordHistory targetLastSeen = (TrekCoordHistory) scannedHistory.get(targetLetter);

        if ((targetLastSeen == null) || (!targetLastSeen.shipName.equalsIgnoreCase(targetObj.name))) {
            parent.hud.sendMessage("You haven't seen that ship on your scanner.");
            return;
        }

        parent.hud.sendMessage("Ship " + targetLastSeen.shipName + " last seen at: " + targetLastSeen.shipCoord.toString());
    }

    public void applyTurnDamage(double hdg1, double pch1) {
        double startDmg = damage;

        double speedDiff;
        if (transwarpEngaged) {
            if (warpSpeed >= 30) {
                // positive transwarp
                speedDiff = warpSpeed - 30 - maxTurnWarp;
                // warp 12 ...  2
                // warp 9  ... -1
                // warp 3  ... -7
                if (speedDiff <= 0)
                    return;
            } else {
                // braking effect
                speedDiff = warpSpeed - 30 + maxTurnWarp;
                // warp -12 ... -2
                // warp  -9 ...  1
                // warp  -3 ...  7
                if (speedDiff >= 0)
                    return;

                speedDiff *= -1; // turn it positive
            }
        } else {
            // we know warpspeed > maxturnwarp, or we wouldn't be in this method
            speedDiff = Math.abs(warpSpeed) - maxTurnWarp;
        }

        speedDiff = Math.ceil(speedDiff);

        double hdg2 = vector.getHeading();
        double pch2 = vector.getPitch();

        double hdgDiff = Math.abs(hdg1 - hdg2);
        if (hdgDiff > 180) hdgDiff = 360 - hdgDiff;

        double pchDiff = Math.abs(pch1 - pch2);

        int pointsChanged = (int) Math.round(hdgDiff + pchDiff);

        // let's say for up to every 5 degrees of change, you have a 50% chance of taking 1 damage per 1.0 warp over turn
        Random gen = new Random();
        if (pointsChanged > 0) {
            do {
                if (Math.abs(gen.nextInt() % 2) < 1) {
                    double damageChange = (speedDiff / (maxCruiseWarp - maxTurnWarp));
                    if (Math.abs(gen.nextInt() % 3) < 1) {
                        damageChange = Math.ceil(damageChange);
                    }

                    damage += damageChange;
                }
                pointsChanged -= 5;
            } while (pointsChanged > 0);
        }

        if (damage - startDmg > 0) {
            parent.hud.sendTopMessage("Receiving " + (int) (damage - startDmg) + " damage from high speed turn.");
        }
    }

    public void increaseDamageControl(int amount) {
        if (getAvailablePower() < amount) {
            return;
        }
        damageControl += amount;
    }

    public void decreaseDamageControl(int amount) {
        if (damageControl <= 0) {
            damageControl = 0;
            return;
        }

        damageControl -= amount;
    }

    public void raiseShields() {
        if (!raisingShields) {
            return;
        }

        if (shields >= 100) {
            return;
        }

        // only need to check for power on every 5th shield point
        if ((getAvailablePower() <= 0) && (shields > 4) && ((shields + 1) % 5 == 0)) {
            deallocateEnergy();
        }

        // if more power has been made available, continue raising shields
        if ((getAvailablePower() > 0) || (shields < 5) || ((shields + 1) % 5 != 0)) {
            shields += 1;

            if (shields > 100) {
                shields = 100;
            }
        }
    }

    public void lowerShields(int amount) {
        shields -= amount;
        raisingShields = false;
        if (shields < 0) {
            shields = 0;
        }
    }

    public void clearEnergyUse() {
        phasers = 0;
        torpedoCount += torpedoes;
        torpedoes = 0;
        if (!transwarpEngaged)
            warpSpeed = 0;
        damageControl = 0;
    }

    // look for systems that have energy applied to them; reduce that energy to allow shields to rise to 100%
    public void deallocateEnergy() {
        int calcEnergy = getAvailablePower();
        if (raisingShields && shields != 100) {
            // reduce in order: dmg ctl, torps, phasers, cloak
            while (calcEnergy <= 0 && this.damageControl > 0) {
                damageControl -= 5;
                calcEnergy += 5;
            }

            while (calcEnergy <= 0 && this.torpedoes > 0) {
                torpedoes -= 1;
                if (this.torpedoType == TrekShip.TORPEDO_PLASMA || this.torpedoType == TrekShip.TORPEDO_BOLTPLASMA) {
                    calcEnergy += 85;
                } else { // variable speed plasma, oblits, and photons only cost 10 per tube
                    calcEnergy += 10;
                }
                parent.hud.sendMessageBeep("Warning! Rising shields, unloading torpedo tube!");
            }

            while (calcEnergy <= 0 && this.phasers > 0) {
                if (this.phaserType == TrekShip.PHASER_AGONIZER) {
                    phasers -= 40;
                    calcEnergy += 40;
                }

                phasers -= 5;
                calcEnergy += 5;
                parent.hud.sendMessageBeep("Warning! Rising shields, unloading phasers!");
            }

            while (calcEnergy <= .1 && this.warpSpeed != 0) {
                if (warpSpeed > 0) {
                    warpSpeed -= .2;
                } else {
                    warpSpeed += .2;
                }

                calcEnergy += warpUsageRatio;
            }

            while (calcEnergy <= 0 && this.cloaked) {
                decloak();
                calcEnergy += cloakPower;
                parent.hud.sendMessageBeep("Warning! Rising shields, cloak has been deactivated!");
            }
        }
    }

    public void firePhasers() {
        if (organiaModifier)
            return; // naughty boy, you cannot fire those nasty phasers here

        if (saveTimeout > 0) {
            parent.hud.sendMessage("You must wait " + saveTimeout + " second(s) to fire phasers.");
            return;
        }

        if (!checkCloak()) {
            return;
        }

        if (phasers <= 0) {
            parent.hud.sendMessage("You must load your phasers before you can fire.");
            return;
        }

        if (phaserFireType == TrekShip.PHASER_FIRENARROW && !checkLock()) {
            parent.hud.sendMessage("Phasers must be locked.");
            return;
        }

        double maxDamage = phasers * 25;
        double percentOfRange;
        double percentOfDamage;

        // Figure out our actual raw damage.
        if (lockTarget != null) {
            if (phaserType != PHASER_AGONIZER && phaserType != TrekShip.PHASER_EXPANDINGSPHEREINDUCER) {
                double currentDistance = TrekMath.getDistance(lockTarget, this);

                if (currentDistance > minPhaserRange) {
                    TrekServer.sendMsgToPlayer(scanLetter, "Phasers miss.", null, false, false);
                    phaserFireTimeout += phasers;
                    phasers = 0;
                    return;
                }

                percentOfRange = 100 - (((TrekMath.getDistance(this, lockTarget)) / this.minPhaserRange) * 100);
                percentOfDamage = maxDamage * (percentOfRange / 100);

                if (percentOfDamage < 0) {
                    percentOfDamage = 0;
                }

                TrekDamageStat stat = lockTarget.doDamageInstant(this, percentOfDamage, "phasers", true);
                parent.hud.drawDamageGivenStat(stat);
            }
        } else {
            Vector objectsInRange = currentQuadrant.getAllObjectsInRange(this, this.minPhaserRange);

            if (objectsInRange.size() == 0) {
                phaserFireTimeout += phasers;
                phasers = 0;
                return;
            }

            for (Enumeration e = objectsInRange.elements(); e.hasMoreElements(); ) {
                TrekObject testRange = (TrekObject) e.nextElement();

                if (TrekUtilities.isObjectDrone(testRange))
                    continue;

                if (TrekUtilities.isObjectBuoy(testRange))
                    continue;

                if (TrekUtilities.isObjectMine(testRange))
                    continue;

                percentOfRange = 100 - (((TrekMath.getDistance(this, testRange)) / this.minPhaserRange) * 100);
                percentOfDamage = (maxDamage * (percentOfRange / 100)) / 2;

                if (percentOfDamage < 0) {
                    percentOfDamage = 0;
                }

                TrekDamageStat stat = testRange.doDamageInstant(this, percentOfDamage, "phasers", true);
                parent.hud.drawDamageGivenStat(stat);
            }
        }

        // Return our energy, set some timeouts, etc.
        phaserFireTimeout += phasers;
        phasers = 0;
    }

    /**
     * Normal phaser operation. Override in ship class for different phaser types.
     */
    public void loadPhasers(int amount) {
        if (this.checkPhaserCool()) {
            if (phasers >= maxPhaserBanks) {
                phasers = maxPhaserBanks;
                return;
            }

            if (getAvailablePower() < amount) {
                return;
            }

            phasers += amount;
        }
    }

    public void unloadPhasers(int amount) {
        if (phasers <= 0) {
            phasers = 0;
            return;
        }

        phasers -= amount;
    }

    public void loadTorpedo() {
        if (torpedoCount <= 0) {
            if (!torpsOutMsgSent) {
                parent.hud.sendMessage("You are out of torpedoes.");
                torpsOutMsgSent = true;
            }
            return;
        }

        if (torpFireTimeout > 0) {
            if (!torpCoolMsgSent) {
                parent.hud.sendMessage("Tubes must cool for " + torpFireTimeout + " second(s).");
                torpCoolMsgSent = true;
            }
            return;
        }

        if (torpedoType == TORPEDO_PLASMA || torpedoType == TORPEDO_BOLTPLASMA) {
            if (getAvailablePower() < 85) {
                return;
            }
        } else {
            if (getAvailablePower() < 10) {
                return;
            }
        }

        if (torpedoes >= maxTorpedoBanks) {
            return;
        }

        torpedoes += 1;
        torpedoCount -= 1;

        if (torpedoCount <= 0) {
            torpedoCount = 0;
        }
    }

    public void unloadTorpedo() {
        if (torpedoes <= 0) {
            return;
        }

        torpedoes -= 1;
        torpedoCount += 1;

        if (torpedoes <= 0) {
            torpedoes = 0;
        }
    }

    public void fireTorpedoes() {
        if (organiaModifier)
            return; // no no, you cannot fire your torpies here

        // If there is an unsave timeout.
        if (saveTimeout > 0) {
            parent.hud.sendMessage("You must wait " + saveTimeout + " second(s) to fire torpedoes.");
            return;
        }

        // No torpedoes loaded.
        if (torpedoes <= 0) {
            parent.hud.sendMessage("Tubes are empty.");
            return;
        }

        // Cloaked.
        if (cloaked) {
            parent.hud.sendMessage("You cannot fire torpedoes while cloaked.");
            return;
        }

        // Just fired.
        if (torpFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + torpFireTimeout + " second(s) to fire torpedoes.");
            return;
        }

        // Uncloaked.
        if (cloakFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + cloakFireTimeout + " second(s) to fire.");
            return;
        }

        if (torpedoes > 1)
            parent.hud.sendTopMessage("Firing " + torpedoes + " torpedoes.");
        else
            parent.hud.sendTopMessage("Firing " + torpedoes + " torpedo.");

        TrekDamageStat stat = null;

        // All torps that need a lock.
        if (torpedoType != TrekShip.TORPEDO_PLASMA && torpedoType != TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
            if (torpedoType == TrekShip.TORPEDO_NORMAL && lockTarget == null) {
                currentQuadrant.addObject(new TrekTorpedo(this, torpedoType, null));
                torpsFired++;
                torpedoes = 0;
                torpFireTimeout = 1;
                return;
            }

            if (lockTarget == null) {
                TrekServer.sendMsgToPlayer(scanLetter, "Torpedoes miss.", null, false, false);
                torpFireTimeout = 1;
                torpsFired += torpedoes;
                torpedoes = 0;
                return;
            }

            // Get the current range to the lock target.
            double currentRange = TrekMath.getDistance(this, lockTarget);

            // add, and subtract .5 to get the rounding... so if a ship is 1400.49 away in larson, and tact says 1400, that
            // weapons lock should go ahead and succeed.
            //			System.out.println("currentRange: " + currentRange);
            if ((currentRange < this.minTorpedoRange - .5) || (currentRange >= (float) this.maxTorpedoRange + .5)) {
                TrekServer.sendMsgToPlayer(scanLetter, "Torpedoes miss.", null, false, false);
                torpFireTimeout = 1;
                torpsFired += torpedoes;
                torpedoes = 0;
                return;
            }

            if (this.torpedoType == TrekShip.TORPEDO_OBLITERATOR) {
                stat = lockTarget.doDamageInstant(this, torpedoes * 100, "obliterator", true);
                torpsFired += torpedoes;
            }

            if (this.torpedoType == TrekShip.TORPEDO_PHOTON) {
                double rangeDifference = this.maxTorpedoRange - this.minTorpedoRange;
                double maxDamage = torpedoes * 175;
                double percentOfRange = (((TrekMath.getDistance(this, lockTarget) - this.minTorpedoRange) / rangeDifference) * 100);
                double percentOfDamage = maxDamage * (percentOfRange / 100);
                torpsFired += torpedoes;

                if (TrekMath.getDistance(this, lockTarget) < this.minTorpedoRange) {
                    stat = lockTarget.doDamageInstant(this, 0, "photon", true);
                } else {
                    stat = lockTarget.doDamageInstant(this, percentOfDamage, "photon", true);
                }
            }

            if (this.torpedoType == TrekShip.TORPEDO_BOLTPLASMA) {
                stat = lockTarget.doDamageInstant(this, 750, "bolt plasma", true);
                torpsFired++;
            }

            if (this.torpedoType == TrekShip.TORPEDO_NORMAL) {
                stat = lockTarget.doDamageInstant(this, 100, "torpedo", true);
                torpsFired++;
            }

            parent.hud.drawDamageGivenStat(stat);
        } else if (torpedoType == TrekShip.TORPEDO_NORMAL || torpedoType == TrekShip.TORPEDO_PLASMA || torpedoType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
            if (this.torpedoType == TrekShip.TORPEDO_NORMAL) {
                currentQuadrant.addObject(new TrekTorpedo(this, torpedoType, null));
                torpsFired++;
            } else if (this.torpedoType == TrekShip.TORPEDO_PLASMA || this.torpedoType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                for (int i = 0; i < torpedoes; i++) {
                    currentQuadrant.addObject(new TrekTorpedo(this, torpedoType, null));
                    torpsFired++;
                }
            }
        } else {
            parent.hud.sendMessage("Your weapons must be locked to fire torpedoes.");
            return;
        }

        // Set our torpedo fire timeout.
        if (torpedoType == TrekShip.TORPEDO_PLASMA || torpedoType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA || torpedoType == TrekShip.TORPEDO_BOLTPLASMA) {
            torpFireTimeout = 3;
        } else {
            torpFireTimeout = 1;
        }

        torpedoes = 0;
    }

    public void lockWeapons() {
        if (scanTarget == null) {
            parent.hud.sendMessage("You must be scanning something to lock weapons.");
            return;
        }

        double distanceToTarget = TrekMath.getDistance(this, scanTarget);

        if (distanceToTarget >= (float) this.maxTorpedoRange + .5) {
            parent.hud.sendMessage("Cannot lock weapons, out of range.");
            locked = false;
            lockTarget = null;
            return;
        }

        if (distanceToTarget < (float) this.maxTorpedoRange + .5) {
            lockTarget = scanTarget;
            locked = true;
        }

    }

    public void lockWeaponsOnScanletter(String thisLetter) {
        TrekShip lockObject;

        lockObject = (TrekShip) currentQuadrant.getShipByScanLetter(thisLetter);

        if (lockObject == null)
            return;

        if (lockObject == this)
            return;

        // Fixed bug that allowed to lock onto cloaked ships.
        if (!TrekMath.canScanShip(this, lockObject))
            return;

        double distanceToTarget = TrekMath.getDistance(this, lockObject);

        // round up to match what tact displays
        if (distanceToTarget >= (float) this.maxTorpedoRange + .5) {
            parent.hud.sendMessage("Cannot lock weapons, out of range.");
            locked = false;
            lockTarget = null;
            return;
        }

        if (distanceToTarget < (float) this.maxTorpedoRange + .5) {
            lockTarget = lockObject;
            locked = true;
        }
    }

    public void unlockWeapons() {
        if (locked) {
            locked = false;
            lockTarget = null;
        }
    }

    public void fireDrone() {
        if (organiaModifier)
            return; // no weapons fire allowed within organia space

        if (saveTimeout > 0) {
            parent.hud.sendMessage("You must wait " + saveTimeout + " second(s) to fire a drone.");
            return;
        }

        if (this.droneCount <= 0) {
            droneCount = 0;
            parent.hud.sendMessage("You have no drones.");
            return;
        }

        if (!checkLock()) {
            parent.hud.sendMessage("Your weapons must be locked to fire a drone.");
            return;
        }

        if (droneFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + droneFireTimeout + " seconds to fire a drone.");
            return;
        }

        if (!TrekUtilities.isObjectShip(lockTarget)) {
            // only allow firing drones at ships
            return;
        }

        currentQuadrant.addObject(new TrekDrone(this, lockTarget));

        parent.hud.sendTopMessage("Drone released.");

        dronesFired++;
        droneCount -= 1;

        if (droneCount <= 0) {
            droneCount = 0;
        }

        droneFireTimeout = 10;
    }

    public void dropMine() {
        if (organiaModifier)
            return; // no weapons allowed in organia space

        if (saveTimeout > 0) {
            parent.hud.sendMessage("You must wait " + saveTimeout + " second(s) to drop a mine.");
            return;
        }

        if (this.mineCount <= 0) {
            parent.hud.sendMessage("You have no mines.");
            return;
        }

        if (mineFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + mineFireTimeout + " second(s) to drop a mine.");
            return;
        }

        currentQuadrant.addObject(new TrekMine(this));

        minesDropped++;
        mineCount -= 1;

        if (mineCount <= 0) {
            mineCount = 0;
        }

        mineFireTimeout = 10;
    }

    public void ram() {
        if (saveTimeout > 0) {
            parent.hud.sendMessage("You must wait " + saveTimeout + " second(s) to ram.");
            return;
        }

        boolean actuallyRammedSomething = false;

        if (ramTimeout > 0) {
            parent.hud.sendMessage("You must wait " + ramTimeout + " second(s) to ram again.");
            return;
        }

        Vector objs = currentQuadrant.getAllShipsInRange(this, 2);
        TrekDamageStat stat;

        for (int x = 0; x < objs.size(); x++) {
            TrekObject obj = (TrekObject) objs.elementAt(x);

            if (TrekUtilities.isObjectShip(obj)) {
                if (TrekMath.getDistance(this, obj) < 1.5) {
                    Random gen = new Random();

                    int ramDmg = Math.abs(gen.nextInt() % 4) + 22; // generate random ram damage for ramming ship
                    int ramDm2 = Math.abs(gen.nextInt() % 4) + 22; // generate random ram damage for rammed ship

                    stat = obj.doRamDamage(this, ramDmg);
                    parent.hud.drawDamageGivenStat(stat);

                    stat = this.doRamDamage(obj, ramDm2);
                    TrekShip ts = (TrekShip) obj;
                    ts.parent.hud.drawDamageGivenStat(stat);

                    actuallyRammedSomething = true;
                }
            }
        }

        // If we rammed something then, reset the timeout.
        if (actuallyRammedSomething) {
            ramTimeout = 30;
        } else {
            parent.hud.sendMessage("There is nothing to ram.");
        }
    }

    public void dock() {
        try {
            if (!orbitable && !dockable) {
                return;
            }

            previousHeading = -1;

            if (orbitable) {
                TrekPlanet planet = currentQuadrant.getClosestPlanet(this);

                if (planet != null) {
                    setWarp(0);
                    orbiting = true;
                    orbitable = false;
                    orbitTarget = planet;
                    orbitDmgCounter = 0;

                    point.x = planet.point.x;
                    point.y = planet.point.y;
                    point.z = planet.point.z;

                    vector = new Trek3DVector(0, 0, 1);
                }
            }

            if (dockable) {
                TrekStarbase base = currentQuadrant.getClosestStarbase(this);

                if (base != null) {
                    setWarp(0);
                    docked = true;
                    dockable = false;
                    dockTarget = base;
                    orbitDmgCounter = 0;

                    point.x = base.point.x;
                    point.y = base.point.y;
                    point.z = base.point.z;

                    vector = new Trek3DVector(0, 0, 1);

                    // Do the scoring thing.
                    doScore(base);
                }
            }
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    protected void doScore(TrekStarbase base) {
        if (damageGiven > 0 || bonus > 0) {
            int totalGold;

            if (currentQuadrant.name.equals("Gamma Quadrant")) {
                totalGold = (int) Math.round((damageGiven + bonus) * .11);
            } else {
                totalGold = (int) Math.round((damageGiven + bonus) * .10);
            }

            gold += totalGold;
            base.gold -= totalGold;
            if (TrekServer.isTeamPlayEnabled()) TrekServer.teamStats[parent.teamNumber].addGold(totalGold);

            damageGiven = 0;
            bonus = 0;
            damageReceived = 0;

            parent.hud.sendMessageBeep("Received " + totalGold + " gold pressed latinum bars!!");
        }
    }

    public void dropBuoy() {
        if (buoyTimeout != 0) {
            parent.hud.sendMessage("You must wait " + buoyTimeout + " seconds to drop a buoy.");
            return;
        }

        currentQuadrant.addObject(new TrekBuoy(point.x, point.y, point.z, "buoy", currentQuadrant.getObjectLetter(), this));

        buoyTimeout = 600;
    }

    public void dropCorbomite() {
        if (!corbomite) return;

        // i can't remember if corbomite device has a timeout or not...
        if (corbomiteTimeout != 0) {
            parent.hud.sendMessage("You must wait " + corbomiteTimeout + " seconds to deploy the corbomite device.");
            return;
        }

        currentQuadrant.addObject(new TrekCorbomite(this));

        corbomite = false;
        corbomiteTimeout = 600;
    }

    public void dropIridium() {
        if (!iridium) return;

        // iridium mines are mines, and should share the same timeout; can only fire 1 mine device every 10 seconds
        if (mineFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + mineFireTimeout + " second(s) to drop an iridium mine.");
            return;
        }

        currentQuadrant.addObject(new TrekIridium(this));
        minesDropped++;
        iridium = false;
        mineFireTimeout = 10;
    }

    public void dropLithium() {
        if (lithiumTimeout > 0) {
            parent.hud.sendMessage("You must wait " + lithiumTimeout + " second(s) to drop a lithium mine.");
            return;
        }

        currentQuadrant.addObject(new TrekLithium(this));

        lithium = false;
        lithiumTimeout = 300;
    }

    public void dropMagnabuoy() {
        if (!magnabuoy) return;

        if ((lockTarget == null) || (!TrekUtilities.isObjectShip(lockTarget))) {
            parent.hud.sendMessage("Must have weapons locked on a ship.");
            return;
        }

        if (magnabuoyTimeout != 0) {
            parent.hud.sendMessage("You must wait " + magnabuoyTimeout + " seconds to launch the Magnabuoy.");
            return;
        }

        currentQuadrant.addObject(new TrekMagnabuoy(this, (TrekShip) lockTarget));

        magnabuoy = false;
        magnabuoyTimeout = 600;
    }

    public void dropNeutron() {
        if (!neutron) return;

        // neutron mines are mines, and should share the same timeout; can only fire 1 mine device every 10 seconds
        if (mineFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + mineFireTimeout + " second(s) to drop a neutron mine.");
            return;
        }

        currentQuadrant.addObject(new TrekNeutron(this));
        minesDropped++;
        neutron = false;
        mineFireTimeout = 10;
    }

    public void fireSeekerProbe(String targetShipLtr) {
        if (seekerTimeout > 0) {
            parent.hud.sendMessage("You must wait " + seekerTimeout + " second(s) to fire a seeker probe.");
            return;
        }

        TrekShip target = (TrekShip) TrekServer.getShipByScanLetter(targetShipLtr);
        if (target != null) {
            if (target.currentQuadrant == currentQuadrant) {
                TrekSeeker seekerProbe = new TrekSeeker(this, target);
                currentQuadrant.addObject(seekerProbe);

                seeker = false;
                seekerTimeout = 180;
            } else {
                parent.hud.sendMessage("Specified ship is not in this quadrant.");
            }

        } else {
            parent.hud.sendMessage("Ship does not exist.");
        }
    }

    public void toggleSelfDestruct() {
        if (selfDestruct) {
            if (!irreversableDestruction) {
                selfDestruct = false;
                selfDestructCountdown = 27;
                parent.hud.sendMessage("Auto-destruct sequence cancelled.");
            }
        } else {
            selfDestruct = true;
            selfDestructCountdown = 27;
        }
    }

    public void cloak() {
        if (!cloak) {
            parent.hud.sendMessage("You do not have cloak capabilities.");
            return;
        }

        if (cloaked) {
            parent.hud.sendMessage("You are already cloaked.");
            return;
        }

        if (cloakBurnt) {
            parent.hud.sendMessageBeep("Your cloaking device has burnt out.  You cannot cloak.");
            return;
        }

        if (getAvailablePower() < cloakPower) {
            parent.hud.sendMessageBeep("You need " + cloakPower + " unused power to cloak.");
            return;
        }

        if (cloakFireTimeout > 0) {
            parent.hud.sendMessage("You must wait " + cloakFireTimeout + " seconds to cloak.");
            return;
        }

        TrekLog.logDebug("Cloaked!");
        cloaked = true;
    }

    public void decloak() {
        if (!cloaked) {
            parent.hud.sendMessageBeep("You are not cloaked.");
            return;
        }

        cloaked = false;
        cloakFireTimeout = 3;
    }

    public String getBearingToObj(TrekObject to) {
        StringBuilder result = new StringBuilder();

        Trek3DPoint targetObjPoint = new Trek3DPoint(to.point);
        Trek3DPoint currentPoint = new Trek3DPoint(this.point);

        Trek3DVector bearingVector = new Trek3DVector(targetObjPoint.x - currentPoint.x, targetObjPoint.y - currentPoint.y, targetObjPoint.z - currentPoint.z);

        int head = (int) Math.round(bearingVector.getHeading());
        String headPad = (head < 10) ? "  " : (head < 100) ? " " : "";

        result.append(headPad).append(head);
        result.append("'");

        double pitch = bearingVector.getPitch();

        String sign = (pitch < 0) ? "-" : "+";
        String pitchPad = (Math.abs(Math.round(pitch)) < 10) ? "0" : "";

        result.append(sign).append(pitchPad);
        result.append(Integer.toString(Math.abs((int) Math.round(pitch))));

        return result.toString();
    }

    public String getBearingToMsg() {
        StringBuilder result = new StringBuilder();

        Trek3DVector bearingVector = new Trek3DVector(msgPoint.x - point.x, msgPoint.y - point.y, msgPoint.z - point.z);

        result.append(Integer.toString((int) Math.round(bearingVector.getHeading())));
        result.append("'");

        double pitch = bearingVector.getPitch();

        String sign = (pitch < 0) ? "" : "+";

        result.append(sign);
        result.append(Integer.toString((int) Math.round(pitch)));

        return result.toString();
    }

    public String getHeading() {
        String returnString;

        if (previousHeading != -1) {
            returnString = Integer.toString(Math.abs(previousHeading));
        } else {
            returnString = Integer.toString((int) Math.round(vector.getHeading()));
        }

        return returnString;
    }

    public String getPitch() {
        double pitch = vector.getPitch();
        String sign = (pitch < 0) ? "-" : "+";
        String pad = (Math.abs(Math.round(pitch)) < 10) ? "0" : "";

        return sign + pad + (Integer.toString(Math.abs((int) Math.round(pitch))));
    }

    public void changeHeading(int hdg, int pch) {
        double startHdg = vector.getHeading();
        double startPch = vector.getPitch();

        if (pch == -90 || pch == 90) {
            previousHeading = hdg;
        } else {
            if (previousHeading != -1) {
                hdg = previousHeading;
                previousHeading = -1;
            }
        }

        // clear any intercept that is set
        if (isIntercepting()) {
            interceptTarget = null;
            intCoordPoint = null;
        }

        final int distance = 100000; // const value somewhere in deep space used as endpoint for the vector

        Trek3DPoint target = new Trek3DPoint();

        pch = 90 - pch;

        // calculate end points
        target.x = (float) (distance * Math.sin(Math.toRadians(pch)) * Math.sin(Math.toRadians(hdg)));
        target.y = (float) (distance * Math.sin(Math.toRadians(pch)) * Math.cos(Math.toRadians(hdg)));
        target.z = (float) (distance * Math.cos(Math.toRadians(pch)));

        vector.x = target.x;
        vector.y = target.y;
        vector.z = target.z;

        if (Math.abs(warpSpeed) > maxTurnWarp)
            applyTurnDamage(startHdg, startPch);
    }

    public void setInitialDirection() {
        Trek3DPoint current = new Trek3DPoint();
        Trek3DPoint target = new Trek3DPoint();
        Random gen = new Random();

        // Set our home planet. (If it exists...)
        TrekPlanet home = currentQuadrant.getHomePlanet(homePlanet);

        if (home != null) {
            // Random start point from home planet.
            TrekLog.logMessage("Setting home planet to " + home.name + ".");

            int randomHeading = Math.abs(gen.nextInt() % 360); //
            int randomPitch = gen.nextInt() % 90;
            int randomDistance = Math.abs(gen.nextInt() % 2000) + 3000; // make distance 3000 to 5000 units from planet

            randomPitch = 90 - randomPitch;
            // calculate end points
            point.x = home.point.x + (float) (randomDistance * Math.sin(Math.toRadians(randomPitch)) * Math.sin(Math.toRadians(randomHeading)));
            point.y = home.point.y + (float) (randomDistance * Math.sin(Math.toRadians(randomPitch)) * Math.cos(Math.toRadians(randomHeading)));
            point.z = home.point.z + (float) (randomDistance * Math.cos(Math.toRadians(randomPitch)));
        } else {
            // Random start point at earth?.
            TrekLog.logMessage(parent.shipName + ": Could not find home planet!");
            point.x = gen.nextInt() % 1000;
            point.y = gen.nextInt() % 1000;
            point.z = gen.nextInt() % 1000;
        }

        // Random start direction..
        target.x = gen.nextInt() % 3000;
        target.y = gen.nextInt() % 3000;
        target.z = gen.nextInt() % 3000;

        current.x = point.x;
        current.y = point.y;
        current.z = point.z;

        target.subtract(current);

        vector.x = target.x;
        vector.y = target.y;
        vector.z = target.z;
    }

    public void resetInitialDirection() {
        Trek3DPoint current = new Trek3DPoint();
        Trek3DPoint target = new Trek3DPoint();
        Random gen = new Random();

        // Random start direction..
        target.x = gen.nextInt() % 3000;
        target.y = gen.nextInt() % 3000;
        target.z = gen.nextInt() % 3000;

        current.x = point.x;
        current.y = point.y;
        current.z = point.z;

        target.subtract(current);

        vector.x = target.x;
        vector.y = target.y;
        vector.z = target.z;
    }

    public boolean checkCloak() {
        if (cloaked) {
            parent.hud.sendMessage("You cannot fire phasers while cloaked.");
            return false;
        } else {
            if (cloakFireTimeout > 0) {
                parent.hud.sendMessage("You must wait " + cloakFireTimeout + " seconds to fire.");
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean checkLock() {
        if (lockTarget == null) {
            parent.hud.sendMessage("Your weapons must be locked to fire.");
            return false;
        } else {
            return true;
        }
    }

    public void transwarpEngage() {
        if (doTranswarp) {
            // if transwarp countdown is already started, just return; cannot toggle transwarp off
            return;
        }

        if (!this.transwarp) {
            parent.hud.sendMessage("You do not have transwarp capabilities.");
            return;
        }

        if (transwarpEngaged) {
            return;
        }

        if (Math.abs(warpSpeed) < 3) {
            parent.hud.sendMessage("You must be travelling at warp 3 or greater to engage transwarp.");
            return;
        }

        if (warpEnergy < 30) {
            parent.hud.sendMessage("Insufficient energy to transwarp; requires 30 Warp Energy.");
            return;
        }

        if (getAvailablePower() < 30) {
            parent.hud.sendMessage("Insufficient free energy to transwarp; requires 30 Power Unused.");
            return;
        }

        // If we don't have transwarp tracking, clear our intercept target.
        if (!transwarpTracking) {
            interceptTarget = null;
        }

        transwarpCounter = 13;
        doTranswarp = true;
    }

    /**
     * Takes care of destroying a ship, and leaving debris.
     *
     * @param explosion Whether or not to cause damage when destroying.
     */
    public void doDestruction(boolean explosion) {
        dbShipAlive = false;
        if (!shipDestroyed) {
            int goldAmount = 0;

            parent.state = TrekPlayer.WAIT_DEAD;

            if (explosion)
                TrekLog.logDebug("Detonating " + parent.shipName + " with damage.");
            else
                TrekLog.logDebug("Detonating " + parent.shipName + " without damage.");

            currentQuadrant.removeShipByScanLetter(this.scanLetter);

            if (this.gold > 0) {
                if (this.gold <= 1000) {
                    goldAmount = this.gold;
                } else {
                    goldAmount = new Double(((this.gold - 1000) * .1) + 1000).intValue();
                }
            }

            // Create the ship debris object, only if we blew up.
            if (explosion) {
                currentQuadrant.addObject(new TrekShipDebris(this, goldAmount));
            } else {
                if (goldAmount > 0) {
                    currentQuadrant.addObject(new TrekGold(goldAmount, this));
                }
            }

            // If we are supposed to explode with damage...
            if (explosion) {
                // Add some damage to nearby ships.
                Vector shipsInRange = currentQuadrant.getAllShipsInRange(this, 3000);

                double maxDamage = (antiMatter / 50) * 25;

                double percentOfRange;
                double percentOfDamage;

                for (int x = 0; x < shipsInRange.size(); x++) {
                    TrekShip boomTarget = (TrekShip) shipsInRange.elementAt(x);

                    percentOfRange = 100 - (((TrekMath.getDistance(this, boomTarget)) / 3000) * 100);
                    percentOfDamage = (maxDamage * (percentOfRange / 100)) / 2;

                    boomTarget.applyDamage(percentOfDamage, this, "ship destruction", true);

                }
            }

            shipDestroyed = true;
        }
    }

    public String getMtrekStylePhaserString() {
        // for use with esc-p command
        switch (this.phaserType) {
            case PHASER_AGONIZER:
                return "AGONIZER";
            case PHASER_EXPANDINGSPHEREINDUCER:
                return "ESI";
            case PHASER_NORMAL:
                return "PHASER";
            case PHASER_TELEPORTER:
                return "TELEPORTER";
            case PHASER_DISRUPTOR:
                return "DISRUPTOR";
            default:
                return "UNKNOWN";
        }
    }

    public String getMtrekStyleTorpString() {
        // for use with esc-t command
        switch (this.torpedoType) {
            case TORPEDO_BOLTPLASMA:
                return "BOLT PLASMA";
            case TORPEDO_NORMAL:
                return "TORPEDO";
            case TORPEDO_OBLITERATOR:
                return "OBLITERATOR";
            case TORPEDO_PHOTON:
                return "PHOTON";
            case TORPEDO_PLASMA:
                return "PLASMA";
            case TORPEDO_VARIABLESPEEDPLASMA:
                return "VPLASMA";
            default:
                return "UNKNOWN";
        }
    }

    public synchronized void updateBattle(TrekDamageStat stat) {
        int warpEnergyDamage;
        Random gen = new Random();

        if (stat.victor instanceof TrekMine) {
            TrekMine theMine = (TrekMine) stat.victor;
            stat.victor = theMine.owner;
        }

        if (stat.victor instanceof TrekDrone) {
            TrekDrone theDrone = (TrekDrone) stat.victor;
            stat.victor = theDrone.owner;
        }

        if (stat.victor instanceof TrekTorpedo) {
            TrekTorpedo theTorp = (TrekTorpedo) stat.victor;
            stat.victor = theTorp.owner;
        }

        if (stat.victor instanceof TrekNeutron) {
            TrekNeutron theMine = (TrekNeutron) stat.victor;
            stat.victor = theMine.owner;
        }

        if (stat.victim != this) {
            parent.hud.drawDamageGivenStat(stat);
        } else {
            parent.hud.drawDamageReceivedStat(stat);
        }

        // Add a conflict if it is a ship.
        if (TrekUtilities.isObjectShip(stat.victim) && TrekUtilities.isObjectShip(stat.victor)) {
            ((TrekShip) stat.victor).addConflict((TrekShip) stat.victim);
        }

        if (stat.victim == this) {
            int intsReceived = new Double(stat.structuralDamage).intValue();

            // If structural damage occurred, lower our warp energy.
            if (intsReceived > 0 && !stat.rammed) {
                // 100% of ints minus a random 25% deduction of total ints.
                int modifier = new Double(intsReceived * .25).intValue();

                if (modifier == 0) {
                    warpEnergyDamage = intsReceived;
                } else {
                    warpEnergyDamage = intsReceived - (Math.abs(gen.nextInt() % modifier));
                }

                // If we have dilithium crystal, diminish them first.
                if (currentCrystalCount > 0) {
                    currentCrystalCount -= warpEnergyDamage;

                    // If the crystals are in the negative, take it from warpen.
                    if (currentCrystalCount < 0) {
                        warpEnergy += currentCrystalCount;
                        currentCrystalCount = 0;
                    }
                } else {
                    warpEnergy -= warpEnergyDamage;
                }

                for (int i = 0; i < intsReceived; i++) {
                    double damageIE = Math.abs(gen.nextInt() % 20);

                    if (damageIE < 1) {
                        impulseEnergy--;
                    }
                }
            }
        }

        if (damage > 100) {
            lifeSupportFailing = true;
        }

        if (damage > 200) {
            doDestruction(true);
        }

        if (warpEnergy < 0) {
            warpEnergy = 0;
        }
        if (impulseEnergy < 0) {
            impulseEnergy = 0;
        }
        if (currentCrystalCount < 0) {
            currentCrystalCount = 0;
        }
    }

    public int getAvailablePower() {
        // First let's get our current available power from the energy.
        // Warp Energy + Impulse Energy = Total Energy Available
        // If no anti-matter, then warp energy is not available.
        int totalEnergy;

        if (antiMatter <= 0) {
            totalEnergy = this.impulseEnergy;
        } else {
            totalEnergy = this.getWarpEnergy() + this.impulseEnergy;
        }

        // Subtract our cloak.
        if (cloaked)
            totalEnergy -= cloakPower;

        // Subtract our warp modifier.
        if (!transwarpEngaged) {
            totalEnergy -= Math.abs(warpSpeed) / .2 * warpUsageRatio;
        } else {
            if (transwarpCounter <= 9) {
                //totalEnergy -= (Math.abs(warpSpeed) - 30) / .2 * warpUsageRatio;
                totalEnergy -= Math.abs(preTranswarpSpeed) / .2 * warpUsageRatio;
            } else {
                totalEnergy -= Math.abs(warpSpeed) / .2 * warpUsageRatio;
            }
        }

        // Subtract our damage control.
        totalEnergy -= this.damageControl;

        // Subtract our shields.
        totalEnergy -= (this.shields / 5);

        // Subtract our phasers.
        if (this.phaserType == TrekShip.PHASER_AGONIZER) {
            if (this.phasers > 0) {
                totalEnergy -= 40;
            }

            totalEnergy -= this.phaserEnergyReturned;
        } else {
            totalEnergy -= this.phaserFireTimeout;
            totalEnergy -= this.phasers;
        }

        // Subtract our torpedoes.
        if (this.torpedoType == TrekShip.TORPEDO_PLASMA || this.torpedoType == TrekShip.TORPEDO_BOLTPLASMA) {
            totalEnergy -= (torpedoes * 85);
        } else { // variable speed plasma, oblits, and photons only cost 10 per tube
            totalEnergy -= (torpedoes * 10);
        }

        if (totalEnergy < 0)
            totalEnergy = adjustAvailablePower(totalEnergy);

        return totalEnergy;
    }

    public int adjustAvailablePower(int calcEnergy) {
        // on each HUD update, check to see if power unused is negative ... if so, reduce the systems in the following order
        // until positive (or 0) power value can be obtained.
        // order: 1) damage control, 2) weapons - torpedoes, 3) weapons - phasers, 4) speed, 5) cloak, 6) shields
        while (calcEnergy < 0 && this.damageControl > 0) {
            damageControl -= 5;
            calcEnergy += 5;
        }

        while (calcEnergy < 0 && this.torpedoes > 0) {
            torpedoes -= 1;
            if (this.torpedoType == TrekShip.TORPEDO_PLASMA || this.torpedoType == TrekShip.TORPEDO_BOLTPLASMA) {
                calcEnergy += 85;
            } else { // variable speed plasma, oblits, and photons only cost 10 per tube
                calcEnergy += 10;
            }
            parent.hud.sendMessageBeep("Warning! Power shortage, unloading torpedo tube!");
        }

        while (calcEnergy < 0 && this.phasers > 0) {
            if (this.phaserType == TrekShip.PHASER_AGONIZER) {
                phasers -= 40;
                calcEnergy += 40;
            }

            phasers -= 5;
            calcEnergy += 5;
            parent.hud.sendMessageBeep("Warning! Power shortage, unloading phasers!");
        }

        while (calcEnergy < 0 && this.warpSpeed != 0) {
            if (transwarpEngaged && (transwarpCounter < 10)) {
                if (preTranswarpSpeed > 0) {
                    preTranswarpSpeed -= .2;
                    warpSpeed -= .2;
                } else {
                    preTranswarpSpeed += .2;
                    warpSpeed += .2;
                }
            } else {
                if (warpSpeed > 0) {
                    warpSpeed -= .2;
                } else {
                    warpSpeed += .2;
                }
            }

            calcEnergy += warpUsageRatio;
        }

        while (calcEnergy < 0 && this.cloaked) {
            decloak();
            calcEnergy += cloakPower;
            parent.hud.sendMessageBeep("Warning! Power shortage, cloak has been deactivated!");
        }

        while (calcEnergy < 0 && this.shields > 4) {
            shields -= 5;
            calcEnergy += 1;
        }

        return calcEnergy;
    }

    public int getCrystalCount() {
        // The number of dilithium crystals to get.
        return (maxWarpEnergy - warpEnergy) + (maxImpulseEnergy - impulseEnergy) + (maxCrystalStorage - currentCrystalCount);
    }

    public int getWarpEnergy() {
        if ((warpEnergy + currentCrystalCount) > antiMatter) {
            return antiMatter;
        } else {
            return warpEnergy + currentCrystalCount;
        }
    }

    public boolean checkPhaserCool() {
        // If our phasers are hot.
        if (phaserFireTimeout != 0) {
            if (phasers > (maxPhaserBanks - phaserFireTimeout)) {
                if (!phaserCoolMsgSent) {
                    parent.hud.sendMessage("You must let your phasers cool.");
                    phaserCoolMsgSent = true;
                }
                return false;
            }
        }

        return true;
    }

    public void beamGold() {
        if (cloaked) return;
        if (scanTarget == null) {
            return;
        }
        if (!checkLock()) {
            return;
        }
        // mines can only be beamed up at <=110, but gold can be picked up @200
        if (TrekMath.getDistance(this, lockTarget) > 200) {
            return;
        }

        // Beam in gold chunks.
        if (TrekUtilities.isObjectGold(lockTarget)) {
            TrekGold goldTarget = (TrekGold) scanTarget;
            this.gold += goldTarget.amount;
            if (goldTarget.dbPlayerID == parent.dbPlayerID) {
                String outputString = "*** Poss Bank: " + parent.shipName + " / " + goldTarget.ownerName + " / " + goldTarget.amount +
                        ", pID: " + parent.dbPlayerID;
                TrekLog.logMessage(outputString);
                TrekServer.sendMsgToAdmins(outputString, goldTarget, true, false);
            }
            if (TrekServer.isTeamPlayEnabled()) TrekServer.teamStats[parent.teamNumber].addGold(goldTarget.amount);
            goldTarget.amount = 0;

            currentQuadrant.removeObjectByScanLetter(goldTarget.scanLetter);
            return;
        }

        // Beam in mines.
        if (TrekUtilities.isObjectMine(lockTarget)) {
            if (TrekMath.getDistance(this, lockTarget) <= 110) {
                if ((this.mines) && (this.mineCount < this.maxMineStorage)) {
                    if (shipClass.equals("BR-1000"))
                        return; // dunno why, but mtrek didn't allow br1k's to beam up mines
                    TrekMine theMine = (TrekMine) lockTarget;
                    if (theMine.hasBeenBeamedUp)
                        return;
                    this.mineCount++;
                    theMine.hasBeenBeamedUp = true;
                    currentQuadrant.removeObjectByScanLetter(scanTarget.scanLetter);
                }
            }

            return;
        }

        // Beam in gold from ship debris.
        if (TrekUtilities.isObjectShipDebris(lockTarget)) {
            TrekShipDebris shipDebris = (TrekShipDebris) lockTarget;

            if (shipDebris.gold > 0) {
                gold += shipDebris.gold;
                parent.hud.sendMessage("Beamed aboard " + shipDebris.gold + " gold.");
                if (parent.dbPlayerID == shipDebris.dbPlayerID) {
                    String outputString = "*** Poss Bank: " + parent.shipName + " / " + shipDebris.whos + " (debris) / " + shipDebris.gold +
                            ", pID: " + parent.dbPlayerID;
                    TrekLog.logMessage(outputString);
                    TrekServer.sendMsgToAdmins(outputString, shipDebris, true, false);
                }
                if (TrekServer.isTeamPlayEnabled()) TrekServer.teamStats[parent.teamNumber].addGold(shipDebris.gold);
                shipDebris.gold = 0;
            }

            return;
        }

        // Handle ship upgrades
        if (TrekUtilities.isObjectPlanet(lockTarget) && lockTarget instanceof IPlanetProvidesShipUpgrade) {
            IPlanetProvidesShipUpgrade upgrade = (IPlanetProvidesShipUpgrade) lockTarget;
            if (orbitDuration > upgrade.getOrbitDuration() && gold > upgrade.getUpgradeCost() && upgrade.isUpgradeAvailable(this)) {
                gold -= upgrade.getUpgradeCost();
                parent.changeShipClass(upgrade.getUpgradeClass());
            }
        }
    }

    public void loadCrystals(int amount) {
        if (impulseEnergy < maxImpulseEnergy) {
            impulseEnergy += amount;

            if (impulseEnergy > maxImpulseEnergy) {
                impulseEnergy = maxImpulseEnergy;
            }

            return;
        }

        if (warpEnergy < maxWarpEnergy) {
            warpEnergy += amount;

            if (warpEnergy > maxWarpEnergy) {
                warpEnergy = maxWarpEnergy;
            }

            return;
        }

        if (currentCrystalCount < maxCrystalStorage) {
            currentCrystalCount += amount;

            if (currentCrystalCount > maxCrystalStorage) {
                currentCrystalCount = maxCrystalStorage;
            }
        }
    }

    public void updateMsgPoint(Trek3DPoint p) {
        msgPoint = new Trek3DPoint(p);
    }

    public boolean isPlaying() {
        return parent.state == TrekPlayer.WAIT_PLAYING;
    }

    public void toggleJamShipSlot(String shipLetter) {
        if (scanLetter.equals(shipLetter)) {
            parent.hud.sendMessage("You could ignore yourself, but then who would you have to talk to?");
            return;
        }

        int arrayIndex = TrekUtilities.returnArrayIndex(shipLetter);
        if (jamShips[arrayIndex]) {
            jamShips[arrayIndex] = false;
            parent.hud.sendMessage("Ship [" + shipLetter + "] is no longer ignored.");
        } else {
            jamShips[arrayIndex] = true;
            parent.hud.sendMessage("Ship [" + shipLetter + "] ignored.");
        }
    }

    public void toggleJamGlobal() {
        if (jamGlobal) {
            jamGlobal = false;
            parent.hud.sendMessage("Stopped ignoring global messages.");
        } else {
            jamGlobal = true;
            parent.hud.sendMessage("Global messages ignored.");
        }
    }

    public void reportJammedSlots() {
        int jamCount = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("You are now ignoring: ");
        for (int x = 0; x < jamShips.length; x++) {
            if (jamShips[x]) {
                sb.append(TrekUtilities.convertArrayIndexToShipLetter(x)).append(" ");
                jamCount++;
            }
        }

        if (jamCount > 0) {
            parent.hud.sendMessage(sb.toString());
        } else {
            parent.hud.sendMessage("All frequencies open.");
        }
    }

    public int getCurrentVisibility() {
        if (nebulaTarget != null)
            return visibility / 3;
        else
            return visibility;
    }

    public void doTractorBeam() {
        if (tractorBeamTarget != null) {
            if (TrekUtilities.isObjectObserverDevice(tractorBeamTarget)) {
                TrekObserverDevice tractorObs = (TrekObserverDevice) tractorBeamTarget;
                if (tractorObs.tower == this) {
                    tractorObs.tower = null;
                }
            } else {
                TrekLithium tractorLith = (TrekLithium) tractorBeamTarget;
                if (tractorLith.tower == this) {
                    tractorLith.tower = null;
                }
            }

            tractorBeamTarget = null;
            parent.hud.sendMessage("Released tractor beam.");
            return;
        }

        if (lockTarget == null) {
            parent.hud.sendMessage("Weapons not locked.");
            return;
        }

        if (!TrekUtilities.isObjectObserverDevice(lockTarget) && !TrekUtilities.isObjectMine(lockTarget))
            return;

        if (TrekUtilities.isObjectMine(lockTarget) && !lockTarget.name.equals("lithium mine"))
            return;

        if (TrekUtilities.isObjectObserverDevice(lockTarget)) {
            TrekObserverDevice observer = (TrekObserverDevice) lockTarget;

            if (observer.tower != null) {
                parent.hud.sendMessage(observer.tower.name + " is already towing it.");
                return;
            }

            parent.hud.sendMessage("You are now towing " + observer.name + ".");
            tractorBeamTarget = observer;
            observer.tower = this;
            observer.delta = new Trek3DPoint(point.x - observer.point.x, point.y - observer.point.y, point.z - observer.point.z);
        } else {
            TrekLithium lithium = (TrekLithium) lockTarget;

            if (lithium.tower != null) {
                parent.hud.sendMessage(lithium.tower.name + " is already towing it.");
                return;
            }

            parent.hud.sendMessage("You are now towing a lithium mine.");
            tractorBeamTarget = lithium;
            lithium.tower = this;
            lithium.delta = new Trek3DPoint(point.x - lithium.point.x, point.y - lithium.point.y, point.z - lithium.point.z);
        }
    }

    /**
     * Adds a ship to the active conflict list, if not already there.
     *
     * @param thisShip The ship that attacked.
     */
    public void addConflict(TrekShip thisShip) {
        if (activeConflicts.containsKey(Long.toString(thisShip.parent.dbConnectionID))) {
            return;
        }

        TrekLog.logMessage(name + ": Adding conflict - " + thisShip.name);

        activeConflicts.put(Long.toString(thisShip.parent.dbConnectionID), thisShip);
        //only add a conflict if you have inflicted damage on the enemy... if you're just a target, you don't get one
        //thisShip.addConflict(this);

        conflicts++;
        if (TrekServer.isTeamPlayEnabled()) {
            TrekServer.teamStats[parent.teamNumber].addConflict();
        }
    }

    /**
     * Manages the active conflict list.
     */
    public void manageConflicts() {
        if (activeConflicts.size() == 0)
            return;

        for (Enumeration e = activeConflicts.elements(); e.hasMoreElements(); ) {
            TrekShip conflictPlayer = (TrekShip) e.nextElement();

            // If the ship is null, remove the active conflict.
            if (conflictPlayer == null) {
                continue;
            }

            // If the player is gone, remove the active conflict.
            if (conflictPlayer.parent.state != TrekPlayer.WAIT_PLAYING) {
                activeConflicts.remove(Long.toString(conflictPlayer.parent.dbConnectionID));
                continue;
            }

            // If the greater of the two scan ranges is exceeded, remove the active conflict.
            double actualScanRange = (conflictPlayer.scanRange > scanRange) ? conflictPlayer.scanRange : scanRange;

            if (TrekMath.getDistance(this, conflictPlayer) > actualScanRange) {
                activeConflicts.remove(Long.toString(conflictPlayer.parent.dbConnectionID));
                continue;
            }
        }
    }

    public boolean isIntercepting() {
        if (interceptTarget == null && intCoordPoint == null)
            return false;

        if (transwarpEngaged && !transwarpTracking)
            return false;

        return true;
    }

    public boolean isScanning() {
        return scanTarget != null;
    }

    public boolean isWeaponsLocked() {
        return lockTarget != null;
    }

    public void doClassSpecificTick() {
        // this method will be called every tick, and can be overridden in the individual ship class Classes to handle
        // unique skills that the ship may have; this allows for ship specific skills, without overriding the entire
        // doShipTickUpdate method
    }

    public void launchEscapePod() {
        if (this instanceof ShipEscapePod)
            return;

        if (escapeCountdownEngaged) {
            escapeCountdown = 5;
            escapeCountdownEngaged = false;
            parent.hud.sendMessage("Escape pod launch aborted.");
            return;
        }

        if (gold > 10000) {
            escapeCountdownEngaged = true;
        } else {
            parent.hud.sendMessage("Escape pod only available with more than 10k gold.");
        }
    }
}