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
package org.gamehost.jtrek.javatrek.bot;

import org.gamehost.jtrek.javatrek.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Mar 29, 2004
 * Time: 9:01:22 AM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BotPlayer extends TrekBot {
    public static String[] nameChoices = {"El Mariachi", "Zorro", "Foamfollower", "Hood", "Fener", "Shadowthrone", "Bannor", "Goldeneyes",
            "Red Eagle", "Manetheren", "Terisa", "Geraden", "The Congery", "Imager", "Adept", "The Adept",
            "Evolution", "Lightspeed", "Whiskeyjack", "Quick Ben", "Jaghut", "Tool", "Conjurer", "Sorceror",
            "Crippled God", "The Chained One", "The First", "Queen of Dreams", "Cha0s", "Quicksilver",
            "TRUTH", "Silverfox", "Echo", "Elektra", "Black Cat", "Sandman", "The Sandman", "Vulture",
            "The Vulture", "Duck & Cover", "Run Away!", "SMASH!", "Hellboy", ".incoming.", "Ydrazil",
            "Allanon", "Don Quixote", "Synapsis", "Red Five", "Red Two", "Red One", "H2O", "Buffy", "Spike",
            "Willow", "T3rr0r", "Heboric", "Whirlwind", "Vision Quest", "Venom", "Proton", "Neutron", "Electron",
            "Byte", "Paran", "Tattersail", "Stormy", "Hedge", "Pitchwife", "Sandgorgon", "Nom", "na-Mhoram",
            "Fatal Revenant", "Eremis", "Barsonage", "The Tor", "The Fayle", "The Domne", "Myste", "Kragen",
            "Havelock", "Azath", "Malazan", "Fiddler", "Anomander Rake", "Caladan Brood", "Crichton", "Ka D'argo",
            "Aeryn Sun", "Zotoh Zhaan", "Chiana", "Scorpius", "Stark", "Cpl Punishment", "Mr. Bater",
            "Cpn Crunch", "George W.", "Apollo", "Starbuck", "Cylon", "One-Eye"
    };
    // modes
    public static final int PATROL = 0;
    public static final int SUPPLY = 1;
    public static final int HIDE = 2;
    public static final int SAVE = 3;

    // sub modes
    public static final int NONE = 0;
    public static final int ATTACK = 1;
    public static final int FLEE = 2;
    public static final int HEAL = 3;

    // supply needs
    public static final int ANTI = 1;
    public static final int TORPS = 2;
    public static final int DRONES = 3;
    public static final int MINES = 4;
    public static final int XTALS = 5;
    public static final int LIFE = 6;

    // weapons messages
    public static final int WEAPON_PLASMA = 1;
    public static final int WEAPON_MINE = 2;

    public static final int RESET = 1;
    public static final int HIT = 2;
    public static final int MISS = 3;

    protected Vector weaponsFired = new Vector();

    protected int plasmaResult = 0;
    protected int mineResult = 0;

    public int shipMode;
    public int shipSubMode;
    public int supplyNeed;

    public TrekObject destinationObj;
    public TrekShip targetShip;

    public static final String[] alphaObjs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "b", "c", "d", "e", "f", "g", "j", "k", "n", "o", "p",
            "q", "r", "t", "v", "w"};
    public static final String[] betaObjs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a", "b", "c", "d", "g", "h", "i", "m", "n", "o",
            "p", "t", "u", "v", "w"};
    public static final String[] gammaObjs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a", "b", "c", "f", "n", "p", "q", "s", "t", "v",
            "w", "C"};
    public static final String[] omegaObjs = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "a", "b", "c", "d", "f", "g", "i", "k", "l", "m",
            "n", "p", "q", "s", "t", "u", "v", "w", "x", "y", "z"};

    protected String[] curObjs;
    protected String quadrantName;
    protected String receivedMsgs;

    // flags
    protected boolean pointReached = false;
    public boolean botPeace = false;  // whether it will be peaceful with other bots (only applies if other bots are also peaceful)
    protected boolean distanceFlag = false;  // try to keep them 100k from worm hole
    protected boolean cloakWait = false;  // to intercept cloaked coordinates, or dock to resupply if near base/planet

    public int botTeam = 0;  // so that even if a bot has botPeace, it won't be completely safe .. may have an opposing team of bots to deal with
    public int skillLevel = 5;  // some skill level setting; 1 most proficient, 5 least proficient

    // timeouts
    protected int targetTimeout = 0;
    protected int fleeTimeout = 0;  // to help keep ships from getting bunched up at the same spot
    protected int dodgeTimeout = 0; // to dodge, and then prevent moving immediately back into plasma
    protected int overwarpTimeout = 0; // to enable overwarping for a full second, instead of only a tick
    protected int phaserTestTimeout = 0; // check to see if cardassian ship should fire phasers around a cloaked ship
    protected int msgTimeout = 0;
    protected int resetSupplyState = 300; // to reset supply needs that go awry, force a re-evaluation
    protected int backOutCount = 0;

    protected Random gen = new Random();

    private boolean doDebug;
    private FileOutputStream debugFile;

    private long tickCount = 0;

    public BotPlayer(TrekServer serverIn, String shipNameIn) {
        super(serverIn, shipNameIn);

        try {
            out = new java.io.FileOutputStream("./data/" + shipName + ".log");
        } catch (java.io.FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

        dbPlayerID = TrekServer.botPlayerId;
        botTeam = Math.abs(gen.nextInt() % 3) + 1;  // spawns as team 1, 2, or 3 ... can be incorporated in team play also

        doDebug = false;

        receivedMsgs = "";
    }

    public void loadInitialBot() {
        // common load tasks
        if ((Math.abs(gen.nextInt() % 3)) == 0) botPeace = true;  // 1 in 3 chance of being bot-peaceful
        skillLevel = Math.abs(gen.nextInt() % 5) + 1;
        shipMode = PATROL;
        shipSubMode = NONE;
        supplyNeed = NONE;
    }

    public void botTickUpdate() {
        tickCount++;
        logDebug("==================== start tick " + tickCount + " ====================");
        if (ship != null) {
            // equalizing factor here, to randomly miss executing a ticks actions
            if (Math.abs(gen.nextInt() % 6) < skillLevel) {
                // nothing; a 'skipped' beat, so to speak -- with skill level 5 (lowest), could miss as many as 83% of the ticks
            } else {

                try {
                    // common tick tasks
                    zap();

                    evaluateMode();
                    evaluateSubMode();

                    dodgePlasma();
                    if (!ship.doTranswarp) doHeal();
                    else runMacro("\003");

                    overwarpTimeout--;

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        logDebug("==================== end tick " + tickCount + " ====================");
    }

    public void botSecondUpdate() {
        logDebug("==================== start second " + tickCount / 4 + " ====================");
        if (ship != null) {
            try {
                // TODO: need a function to check for chasing ships for eternity; maybe a counter that increases the longer you're attacking
                // a specific ship; or check to see if > xxxx units from the worm hole
                if (!distanceFlag && TrekMath.getDistance(ship, ship.currentQuadrant.getObjectByScanLetter("w")) > 100000) {
                    // getting too far out; try clearing all modes
                    destinationObj = null;
                    shipMode = PATROL;
                    shipSubMode = NONE;
                    supplyNeed = NONE;

                    distanceFlag = true;
                }

                if (distanceFlag && TrekMath.getDistance(ship, ship.currentQuadrant.getObjectByScanLetter("w")) < 100000) {
                    distanceFlag = false;
                }

                // common second tasks
                if (!ship.raisingShields) {
                    runMacro("s");
                }

                if (ship.orbitable || ship.dockable) {
                    runMacro(".");
                }

                targetTimeout--;
                fleeTimeout--;
                dodgeTimeout--;
                msgTimeout--;
                resetSupplyState--;
                phaserTestTimeout--;

                if (resetSupplyState <= 0) {
                    resetSupplyState = 300;
                    supplyNeed = 0;
                    pointReached = false;
                    if (shipMode == SUPPLY) shipMode = PATROL;
                }

                if (backOutCount > 0 && Math.abs(gen.nextInt() % 10) < 1) {
                    backOutCount--;
                }

                // just sitting around
                if (shipMode == PATROL && shipSubMode == NONE && supplyNeed == NONE && ship.warpSpeed == 0) {
                    runMacro("X");
                    destinationObj = null;
                }

                // more sitting around
                if (shipMode == SUPPLY) {
                    switch (supplyNeed) {
                        case NONE:
                            break;
                        case ANTI:
                            if (ship.antiMatter == 5000) {
                                supplyNeed = NONE;
                                shipMode = PATROL;
                            }
                            break;
                        case TORPS:
                            if (ship.torpedoCount == ship.maxTorpedoStorage) {
                                supplyNeed = NONE;
                                shipMode = PATROL;
                            }
                            break;
                        case DRONES:
                            if (ship.droneCount == ship.maxDroneStorage) {
                                supplyNeed = NONE;
                                shipMode = PATROL;
                            }
                        case MINES:
                            if (ship.mineCount == ship.maxMineStorage) {
                                supplyNeed = NONE;
                                shipMode = PATROL;
                            }
                            break;
                        case XTALS:
                            if (ship.currentCrystalCount == ship.maxCrystalStorage) {
                                supplyNeed = NONE;
                                shipMode = PATROL;
                            }
                            break;
                        case LIFE:
                            if (ship.lifeSupport == 100) {
                                supplyNeed = NONE;
                                shipMode = PATROL;
                            }
                            break;

                    }
                }

                // respond to messages
                sendMessage();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        logDebug("==================== end second " + tickCount / 4 + " ====================");
    }

    protected void zap() {
        // make sure ship can fire, before bothering to scan all the objects in phaser range
        if (ship.checkPhaserCool() && !ship.organiaModifier && !ship.cloaked && (ship.cloakFireTimeout <= 0) && (ship.saveTimeout <= 0)) {
            TrekObject intHolder;
            intHolder = ship.scanTarget;
            TrekObject nearestObj = null;
            double nearest = ship.minPhaserRange;

            Vector objs = ship.currentQuadrant.getAllObjectsInRange(ship, ship.minPhaserRange);
            for (Enumeration e = objs.elements(); e.hasMoreElements(); ) {
                TrekObject curObj = (TrekObject) e.nextElement();
                if (TrekUtilities.isObjectBuoy(curObj) ||
                        TrekUtilities.isObjectDrone(curObj) ||
                        TrekUtilities.isObjectMine(curObj) ||
                        TrekUtilities.isObjectMagnabuoy(curObj)) {
                    // could also check for neutron, iridium, and corbomite (i guess)

                    double curObjDist = TrekMath.getDistance(ship, curObj);

                    // if this object is the closest one encountered so far
                    if (curObjDist < nearest) {
                        nearest = curObjDist;

                        // don't shoot owned objects
                        if (TrekUtilities.isObjectDrone(curObj)) {
                            TrekDrone drone = (TrekDrone) curObj;
                            if (!(drone.owner == ship)) {
                                nearestObj = drone;
                            }
                        } else if (TrekUtilities.isObjectMine(curObj)) {
                            TrekMine mine = (TrekMine) curObj;
                            if (!(mine.owner == ship)) nearestObj = mine;
                        } else if (TrekUtilities.isObjectBuoy(curObj)) {
                            TrekBuoy buoy = (TrekBuoy) curObj;
                            if (!(buoy.owner == ship)) nearestObj = buoy;
                        } else {
                            nearestObj = curObj;
                        }
                    }
                }
            }

            // shoot the nearest object
            if (nearestObj != null) {
                runMacro("CO" + nearestObj.scanLetter + "\033\016\014P");

                // check for ESI and accid. base/sb shooting
                if (ship.phaserType == TrekShip.PHASER_EXPANDINGSPHEREINDUCER) {
                    Vector objsInPhRange = ship.currentQuadrant.getAllObjectsInRange(ship, ship.minPhaserRange);
                    boolean shoot = true;
                    for (Enumeration e = objsInPhRange.elements(); e.hasMoreElements(); ) {
                        TrekObject curObj = (TrekObject) e.nextElement();
                        if (TrekUtilities.isObjectPlanet(curObj) || TrekUtilities.isObjectStarbase(curObj)) {
                            if (TrekMath.getDistance(ship, curObj) < .5) continue;  // ESI won't hit it
                            shoot = false;
                        }
                    }

                    if (shoot) runMacro("p\020");
                    else runMacro("\020");
                } else {
                    runMacro("p\020");
                }

                logDebug("zap()   shooting: " + nearestObj.name);
            }

            if (ship.scanTarget != intHolder) ship.scanTarget = intHolder;
            //if (ship.scanTarget != null) logDebug("zap():   scanning: " + ship.scanTarget.name);
        }
    }

    protected void checkSupplies() {
        if (!pointReached) {
            // priority: antimatter, torps, mines/drones, crystals, life support
            boolean foundSupply = false;
            if (ship.antiMatter < 1000) {
                if (shipMode != SUPPLY || supplyNeed != ANTI) {
                    // replenish fuel
                    shipMode = SUPPLY;
                    supplyNeed = ANTI;
                    double nearest = 200000;
                    // find a planet in current quadrant to give this ship antimatter
                    for (int x = 0; x < curObjs.length; x++) {
                        TrekObject curObj = ship.currentQuadrant.getObjectByScanLetter(curObjs[x]);
                        if (TrekUtilities.isObjectPlanet(curObj)) {
                            TrekPlanet curPlanet = (TrekPlanet) curObj;
                            if (curPlanet.givesAntimatter(ship)) {
                                if (TrekMath.getDistance(ship, curPlanet) < nearest) {
                                    destinationObj = curPlanet;
                                    nearest = TrekMath.getDistance(ship, curPlanet);
                                    foundSupply = true;
                                }
                            }
                        }
                    }

                    if (!foundSupply) {
                        runMacro("Qy");
                    }
                    logDebug("checkSupplies()   need anti: " + destinationObj.name);
                }
            } else if (ship.torpedoType != TrekShip.TORPEDO_OBLITERATOR && ship.torpedoCount < ship.maxTorpedoStorage / 3) { // less than 33% of torps left
                if (shipMode != SUPPLY || supplyNeed != TORPS) {
                    // replenish torps
                    shipMode = SUPPLY;
                    supplyNeed = TORPS;
                    double nearest = 300000;
                    // find nearest torp provider
                    for (int x = 0; x < curObjs.length; x++) {
                        TrekObject curObj = ship.currentQuadrant.getObjectByScanLetter(curObjs[x]);
                        if (TrekUtilities.isObjectPlanet(curObj)) {
                            TrekPlanet curPlanet = (TrekPlanet) curObj;
                            if (curPlanet.givesTorps(ship)) {
                                if (TrekMath.getDistance(ship, curPlanet) < nearest) {
                                    destinationObj = curPlanet;
                                    nearest = TrekMath.getDistance(ship, curPlanet);
                                    foundSupply = true;
                                }
                            }
                        } else if (TrekUtilities.isObjectStarbase(curObj)) {
                            TrekStarbase curBase = (TrekStarbase) curObj;
                            if (curBase.givesTorps && (TrekMath.getDistance(ship, curBase) < nearest)) {
                                destinationObj = curBase;
                                nearest = TrekMath.getDistance(ship, curBase);
                                foundSupply = true;
                            }
                        }
                    }

                    if (!foundSupply) {
                        runMacro("Qy");
                    }
                    logDebug("checkSupplies()   need torps: " + destinationObj.name);
                }
            } else if (ship.drones && (ship.droneCount <= 1)) {
                if (shipMode != SUPPLY || supplyNeed != DRONES) {
                    shipMode = SUPPLY;
                    supplyNeed = DRONES;
                    double nearest = 300000;
                    // find nearest drone provider
                    for (int x = 0; x < curObjs.length; x++) {
                        TrekObject curObj = ship.currentQuadrant.getObjectByScanLetter(curObjs[x]);
                        if (TrekUtilities.isObjectPlanet(curObj)) {
                            TrekPlanet curPlanet = (TrekPlanet) curObj;
                            if (curPlanet.givesDrones(ship) && (TrekMath.getDistance(ship, curPlanet) < nearest)) {
                                destinationObj = curPlanet;
                                nearest = TrekMath.getDistance(ship, curPlanet);
                                foundSupply = true;
                            }
                        } else if (TrekUtilities.isObjectStarbase(curObj)) {
                            TrekStarbase curBase = (TrekStarbase) curObj;
                            if (curBase.givesDrones && (TrekMath.getDistance(ship, curBase) < nearest)) {
                                destinationObj = curBase;
                                nearest = TrekMath.getDistance(ship, curBase);
                                foundSupply = true;
                            }
                        }
                    }

                    if (!foundSupply) {
                        runMacro("Qy");
                    }
                    logDebug("checkSupplies()   need drones: " + destinationObj.name);
                }
            } else if (ship.mines && (ship.mineCount <= ship.maxMineStorage / 4) && !(ship.classLetter.equals("p"))) {
                // Galor has regenerating mines
                if (shipMode != SUPPLY || supplyNeed != MINES) {
                    shipMode = SUPPLY;
                    supplyNeed = MINES;
                    double nearest = 300000;
                    // find nearest mine provider
                    for (int x = 0; x < curObjs.length; x++) {
                        TrekObject curObj = ship.currentQuadrant.getObjectByScanLetter(curObjs[x]);
                        if (TrekUtilities.isObjectPlanet(curObj)) {
                            TrekPlanet curPlanet = (TrekPlanet) curObj;
                            if (curPlanet.givesMines(ship) && (TrekMath.getDistance(ship, curPlanet) < nearest)) {
                                destinationObj = curPlanet;
                                nearest = TrekMath.getDistance(ship, curPlanet);
                                foundSupply = true;
                            }
                        } else if (TrekUtilities.isObjectStarbase(curObj)) {
                            TrekStarbase curBase = (TrekStarbase) curObj;
                            if (curBase.givesMines && (TrekMath.getDistance(ship, curBase) < nearest)) {
                                destinationObj = curBase;
                                nearest = TrekMath.getDistance(ship, curBase);
                                foundSupply = true;
                            }
                        }
                    }

                    if (!foundSupply) {
                        runMacro("Qy");
                    }
                    logDebug("checkSupplies()   need mines: " + destinationObj.name);
                }
            } else if (ship.lifeSupport < 85) {
                if (shipMode != SUPPLY || supplyNeed != LIFE) {
                    shipMode = SUPPLY;
                    supplyNeed = LIFE;
                    double nearest = 300000;
                    TrekObject lsFixer = null;

                    // find nearest odd starbase, or other life support fixer-upper
                    for (int x = 0; x < curObjs.length; x++) {
                        TrekObject curObj = ship.currentQuadrant.getObjectByScanLetter(curObjs[x]);
                        double curDist = TrekMath.getDistance(ship, curObj);
                        if (curDist < nearest) {
                            if (TrekUtilities.isObjectPlanet(curObj)) {
                                TrekPlanet curPlanet = (TrekPlanet) curObj;
                                if (curPlanet.repairsLifeSupport(ship)) {
                                    lsFixer = curPlanet;
                                    nearest = curDist;
                                }
                            } else if (TrekUtilities.isObjectStarbase(curObj)) {
                                TrekStarbase curBase = (TrekStarbase) curObj;
                                if (curBase.fixesLifeSupport) {
                                    lsFixer = curBase;
                                    nearest = curDist;
                                }
                            }
                        }
                    }

                    if (lsFixer != null) {
                        destinationObj = lsFixer;
                    } else {
                        runMacro("Qy"); // ship is out there pretty far, or something weird, just quit
                    }
                }
            } else if (ship.currentCrystalCount < (ship.maxCrystalStorage - 20) && shipSubMode != ATTACK && shipSubMode != FLEE) {
                if (shipMode != SUPPLY || supplyNeed != XTALS) {
                    shipMode = SUPPLY;
                    supplyNeed = XTALS;

                    // wrigley gets too crowded with current overwhelming kpb presence

                    double nearest = 300000;
                    // find nearest xtal provider
                    for (int x = 0; x < curObjs.length; x++) {
                        TrekObject curObj = ship.currentQuadrant.getObjectByScanLetter(curObjs[x]);
                        if (TrekUtilities.isObjectPlanet(curObj)) {
                            TrekPlanet curPlanet = (TrekPlanet) curObj;
                            if (curPlanet.givesCrystals(ship) && (TrekMath.getDistance(ship, curPlanet) < nearest)) {
                                destinationObj = curPlanet;
                                nearest = TrekMath.getDistance(ship, curPlanet);
                                foundSupply = true;
                            }
                        }
                    }

                    if (!foundSupply) {
                        runMacro("Qy");
                    }
                    logDebug("checkSupplies()   need xtals: " + destinationObj.name);
                }
            } else if (shipMode == SUPPLY && supplyNeed == NONE) shipMode = PATROL;
        }
    }

    protected void checkDamage() {
        if (ship.damage > 0 || ship.impulseEnergy < ship.maxImpulseEnergy || ship.warpEnergy < ship.maxWarpEnergy) {
            if (shipSubMode != ATTACK && shipSubMode != FLEE) {
                if (shipSubMode != HEAL) {
                    shipSubMode = HEAL;
                    logDebug("checkDamage()   changing shipSubMode to HEAL");
                }
            } else {
                runMacro("cccccccccccccccccccc");
                if (ship.damage > 75 && shipSubMode == ATTACK) {
                    int lurkingShips = ship.currentQuadrant.getCountShipsInRange(ship, 3000);
                    if (lurkingShips > 1 || ship.damage >= 90) {
                        shipSubMode = FLEE;
                        fleeTimeout = 30;
                    } else if ((lurkingShips <= 1 && shipSubMode == FLEE && fleeTimeout <= 0) ||
                            (shipSubMode == ATTACK && (targetShip != null && targetShip.damage < ship.damage)))
                        shipSubMode = HEAL;
                }
            }

            if (ship.lifeSupportFailing) {
                // check for nearby base to save if possible
                if (shipMode != SAVE &&
                        (ship.damage > 110 ||
                                (ship.warpEnergy + ship.currentCrystalCount + ship.impulseEnergy < 40 * ship.warpUsageRatio))) {
                    TrekStarbase saveBase = null;
                    int curMaxSpeed = ship.getAvailablePower() / 5 * ship.warpUsageRatio;
                    double nearest = 3000 + (1000 * curMaxSpeed / 2);  // assume they can make it to at least 3000 + a variable distance based
                    // on ships speed; i.e. warp 16 * 1000 / 2, another 8000 units
                    // warp 10 * 1000 / 2, another 5000
                    for (int x = 0; x < 10; x++) {
                        TrekStarbase curBase = (TrekStarbase) ship.currentQuadrant.getObjectByScanLetter(curObjs[x]);
                        if (TrekMath.getDistance(ship, curBase) < nearest) {
                            nearest = TrekMath.getDistance(ship, curBase);
                            saveBase = curBase;
                        }
                    }

                    if (saveBase != null) {
                        shipMode = SAVE;
                        destinationObj = saveBase;
                        logDebug("checkDamage()   changing shipMode to SAVE");
                    }
                    shipSubMode = HEAL;

                }
            }
        } else {
            if (shipSubMode == HEAL) {
                shipSubMode = NONE;
                runMacro("\003");
                logDebug("checkDamage()   changing shipSubMode to NONE");
            }

            if (ship.damageControl > 0)
                runMacro("\003");
        }
    }

    protected void checkEnemies() {
        // set sub mode to attack if enemies have been spotted, check for teams
        int shipsAround = getEnemyShipsInRange(ship, 2500);

        if (shipsAround > skillLevel && shipSubMode != FLEE) {  // overwhelming odds
            // possible team
            shipSubMode = FLEE;
            targetTimeout = 0;
            fleeTimeout = 30;
            logDebug("checkEnemies()   changing shipSubMode to FLEE");
            return;
        } else {
            if (shipsAround <= skillLevel) {
                if (shipSubMode == FLEE && fleeTimeout <= 0) {
                    shipSubMode = NONE;  // enemies are down to potentially managable numbers
                    logDebug("checkEnemies()   changing shipSubMode to NONE from FLEE");
                }
                if (shipsAround == 0 && shipSubMode == ATTACK) {
                    // increase range and look for targets
                    if (ship.currentQuadrant.getCountVisibleShipsInRange(ship, (int) ship.scanRange) == 0 && targetShip == null) {
                        shipSubMode = NONE;
                        logDebug("checkEnemies()   changing shipSubMode to NONE from ATTACK");
                    }
                }
            }
        }

        /*  Potentially call for help in battle situation, if being teamed / vultured  */
        if (botPeace) {
            Vector humanShips = ship.currentQuadrant.getAllVisibleShipsInRange(ship, 2999);
            int humanShipCount = 0;
            int mostDamage = 0;
            TrekShip curShip = null;
            TrekShip enemyShip = null;

            for (Enumeration e = humanShips.elements(); e.hasMoreElements(); ) {
                curShip = (TrekShip) e.nextElement();
                if (curShip.parent instanceof BotPlayer) {
                    // treat enemy bots as 'humans'

                    //BotPlayer bp = (BotPlayer) curShip.parent;
                    //if (bp.botPeace && bp.teamNumber == teamNumber)
                    continue;
                    //else
                    //    humanShipCount++;
                } else if (curShip.lockTarget == ship || curShip.scanTarget == ship) {
                    humanShipCount++;
                    if (curShip.damage >= mostDamage) {
                        mostDamage = curShip.damage;
                        enemyShip = curShip;
                    }
                }
            }

            logDebug("checkEnemies()   damage: " + ship.damage + "  -- humanShipCount: " + humanShipCount);
            if (humanShipCount > 1 || (ship.damage >= 50 && humanShipCount > 0)) {
                // call for a couple temporary allies
                int alliesCalled = 0;
                Vector nearShips = ship.currentQuadrant.getAllShipsInRange(ship, 100000);
                for (Enumeration e = nearShips.elements(); e.hasMoreElements(); ) {
                    if (alliesCalled > 2) break;
                    TrekShip curBotShip = (TrekShip) e.nextElement();

                    if (curBotShip == ship) continue;

                    if (curBotShip.parent instanceof BotPlayer) {
                        BotPlayer curPlyr = (BotPlayer) curBotShip.parent;
                        if (curPlyr.botPeace && curPlyr.teamNumber == this.teamNumber && curBotShip.damage < 25 &&
                                (curPlyr.shipMode == PATROL || (curPlyr.shipMode == SUPPLY && curPlyr.supplyNeed != TORPS))) {
                            // TODO: need to set the ally bot to head to the ship under attack
                            // pretend the caller sent the enemy coords via msg
                            curPlyr.destinationObj = null;
                            curPlyr.targetShip = enemyShip;
                            curPlyr.shipSubMode = ATTACK;
                            curBotShip.setWarp(curBotShip.maxTurnWarp);
                            curBotShip.interceptCoords(curShip.point);
                            curBotShip.setWarp(curBotShip.maxCruiseWarp);
                            alliesCalled++;
                            logDebug("checkEnemies()   " + ship.name + " called " + curBotShip.name + " for help against " + enemyShip.name);
                        }
                    }
                }
            }
        }

        if (targetTimeout > 0 && (targetShip == null || targetShip.parent.state != TrekPlayer.WAIT_PLAYING)) {
            targetTimeout = 0;  // find a new target; old one saved, quit, died or something...
            logDebug("checkEnemies()   finding new target, old one is null");
        }

        if (targetTimeout <= 0) { // try to find a target
            targetShip = null;  // clear current target

            // reset weapons results
            plasmaResult = NONE;
            mineResult = NONE;

            // base scanning range for enemies on ship mode
            double nearest = 0;
            switch (shipMode) {
                case (PATROL):
                    nearest = ship.scanRange + 3000;
                    break;

                case (SUPPLY):
                    if (supplyNeed != ANTI && supplyNeed != TORPS) {
                        nearest = ship.scanRange;
                    } else {
                        nearest = ship.minPhaserRange;
                    }
                    break;

                case (HIDE):
                case (SAVE):
                    nearest = ship.minPhaserRange / 2;
                    break;
            }

            Vector visibleShips = ship.currentQuadrant.getVisibleShips(ship);
            TrekShip possTarget = null;

            for (Enumeration e = visibleShips.elements(); e.hasMoreElements(); ) {
                TrekShip curShip = (TrekShip) e.nextElement();

                if (curShip == ship) continue;

                // check to see if it's a 'friendly'
                if (botPeace) {
                    if (curShip.parent instanceof BotPlayer) {
                        BotPlayer curPlayer = (BotPlayer) curShip.parent;
                        if (curPlayer.botPeace && curPlayer.botTeam == botTeam) continue;
                    }
                }

                if (TrekMath.getDistance(ship, curShip) < nearest) {
                    possTarget = curShip;
                    nearest = TrekMath.getDistance(ship, curShip);
                }
            }

            if (possTarget != null) {  // there are one or more visible ships
                if (shipMode == SUPPLY) {
                    // check to see if anyone locking
                    logDebug("checkEnemies()   possTarget != null && shipMode == SUPPLY");
                    boolean possTargetLocked = false;
                    for (Enumeration e = visibleShips.elements(); e.hasMoreElements(); ) {
                        TrekShip cur = (TrekShip) e.nextElement();

                        if (cur == ship) continue;

                        if (cur.lockTarget == ship || cur.scanTarget == ship) {  // if they're scanning they're prob. hostile
                            if (cur.scanTarget == ship) {
                                if (TrekMath.getDistance(ship, cur) < cur.maxTorpedoRange + 100)
                                    possTargetLocked = true;
                            } else
                                possTargetLocked = true;
                            break;  // once we've identified at least one hostile ship in the area
                        }
                    }

                    if (possTargetLocked) {
                        // there are potential hostiles around, check to find the most damaged
                        int mostDamage = 0;
                        for (Enumeration e = visibleShips.elements(); e.hasMoreElements(); ) {
                            TrekShip curLock = (TrekShip) e.nextElement();
                            if ((curLock.lockTarget == ship ||
                                    (curLock.scanTarget == ship && (TrekMath.getDistance(ship, curLock) < ship.maxTorpedoRange ||
                                            TrekMath.getDistance(ship, curLock) < curLock.maxTorpedoRange))) && curLock.damage >= mostDamage) {
                                mostDamage = curLock.damage;
                                possTarget = curLock;
                            }
                        }

                        targetShip = possTarget;
                        targetTimeout = 20;  // shorter timeout when in supply mode
                        if (shipSubMode == NONE) shipSubMode = ATTACK;
                        logDebug("checkEnemies()   new target during supply found: " + targetShip.name);
                    }

                } else {
                    targetShip = possTarget;
                    targetTimeout = 45;
                    if (shipSubMode == NONE) shipSubMode = ATTACK;
                    logDebug("checkEnemies()   new target found: " + targetShip.name);
                }
            } else {
                if (shipSubMode == ATTACK) shipSubMode = NONE;
            }
        }
    }

    protected void evaluateMode() {
        if (destinationObj != null || targetShip != null) {
            if (shipSubMode == NONE) {
                if (ship.scanTarget != destinationObj) ship.scanTarget = destinationObj;
            }
            if (shipSubMode == ATTACK) {
                if (TrekMath.canScanShip(ship, targetShip) && ship.scanTarget != targetShip)
                    ship.scanTarget = targetShip;
            }
        }

        if (!quadrantName.equals(ship.currentQuadrant.name)) {
            quadrantName = ship.currentQuadrant.name;
            setCurObjs();
        }

        if (shipMode == PATROL) {
            // check for getting near destination
            if (destinationObj != null) {

                checkForGold();

                // if intercepting ship debris for gold, and someone else snags it ... clear destination
                if (TrekUtilities.isObjectShipDebris(destinationObj) && ((TrekShipDebris) destinationObj).gold == 0) {
                    destinationObj = null;
                }

                double curDistToDO = TrekMath.getDistance(ship, destinationObj);
                if (shipSubMode != ATTACK)
                    doDock(curDistToDO, false);

                checkSupplies();
                checkDamage();
                checkEnemies();

                if (TrekMath.getDistance(ship, destinationObj) < 200) {
                    if (TrekUtilities.isObjectGold(destinationObj) || TrekUtilities.isObjectShipDebris(destinationObj)) {
                        runMacro("O" + destinationObj.scanLetter + "\014$");
                    }

                    // check to see if destination provides anything needed; good opportunity to 'top off'

                    if (TrekUtilities.isObjectPlanet(destinationObj)) {

                        TrekPlanet curPlanet = (TrekPlanet) destinationObj;
                        if (ship.antiMatter < 3000 && curPlanet.givesAntimatter(ship)) {
                            shipMode = SUPPLY;
                            supplyNeed = ANTI;
                            pointReached = false;
                        } else if (ship.torpedoCount < ship.maxTorpedoStorage && curPlanet.givesTorps(ship)) {
                            shipMode = SUPPLY;
                            supplyNeed = TORPS;
                            pointReached = false;
                        } else if (ship.drones && ship.droneCount < ship.maxDroneStorage && curPlanet.givesDrones(ship)) {
                            shipMode = SUPPLY;
                            supplyNeed = DRONES;
                            pointReached = false;
                        } else if (ship.mines && ship.mineCount < ship.maxMineStorage && curPlanet.givesMines(ship)) {
                            shipMode = SUPPLY;
                            supplyNeed = MINES;
                            pointReached = false;
                        } else if ((ship.currentCrystalCount < ship.maxCrystalStorage || ship.warpEnergy < ship.maxWarpEnergy) && curPlanet.givesCrystals(ship)) {
                            shipMode = SUPPLY;
                            supplyNeed = XTALS;
                            pointReached = false;
                        } else if (ship.lifeSupport < 100 && curPlanet.repairsLifeSupport(ship)) {
                            shipMode = SUPPLY;
                            supplyNeed = LIFE;
                            pointReached = false;
                        } else {
                            destinationObj = null;
                        }

                    } else if (TrekUtilities.isObjectStarbase(destinationObj)) {

                        TrekStarbase curBase = (TrekStarbase) destinationObj;
                        if (ship.torpedoCount < ship.maxTorpedoStorage && curBase.givesTorps) {
                            shipMode = SUPPLY;
                            supplyNeed = TORPS;
                            pointReached = false;
                        } else if (ship.drones && ship.droneCount < ship.maxDroneStorage && curBase.givesDrones) {
                            shipMode = SUPPLY;
                            supplyNeed = DRONES;
                            pointReached = false;
                        } else if (ship.mines && ship.mineCount < ship.maxMineStorage && curBase.givesMines) {
                            shipMode = SUPPLY;
                            supplyNeed = MINES;
                            pointReached = false;
                        } else if (ship.lifeSupport < 100 && curBase.fixesLifeSupport) {
                            shipMode = SUPPLY;
                            supplyNeed = LIFE;
                            pointReached = false;
                        } else {
                            destinationObj = null;
                        }

                    } else {
                        destinationObj = null;
                    }
                }
            } else {
                destinationObj = ship.currentQuadrant.getObjectByScanLetter(curObjs[Math.abs(gen.nextInt() % curObjs.length)]);
            }
        } else if (shipMode == SUPPLY) {
            checkSupplies();
            checkDamage();
            checkEnemies();

            if (destinationObj == null || ship.currentQuadrant.getObjectByScanLetter(destinationObj.scanLetter) == null ||
                    ship.currentQuadrant.getObjectByScanLetter(destinationObj.scanLetter) != destinationObj) {
                shipMode = PATROL;
                logDebug("evaluateMode()   current destinationObj was null, or ship moved to another quad - reset to PATROL");
                return;
            }

            // divert for gold, if around
            if (supplyNeed != ANTI) {
                checkForGold();
            }

            if ((TrekMath.getDistance(ship, destinationObj) < 200) &&
                    (TrekUtilities.isObjectGold(destinationObj) || TrekUtilities.isObjectShipDebris(destinationObj))) {
                runMacro("O" + destinationObj.scanLetter + "\014$");
                destinationObj = null;
                shipMode = PATROL;
                return;
            }

            double curDistanceToDestination = TrekMath.getDistance(ship, destinationObj);
            doDock(curDistanceToDestination, false);
            if (pointReached) {
                switch (supplyNeed) {
                    case NONE:
                        shipMode = PATROL;
                        break;
                    case ANTI:
                        if (ship.antiMatter == 5000) {
                            pointReached = false;
                            shipMode = PATROL;
                            supplyNeed = NONE;
                            destinationObj = null;
                        }
                        break;
                    case TORPS:
                        if (ship.torpedoCount == ship.maxTorpedoStorage) {
                            pointReached = false;
                            shipMode = PATROL;
                            supplyNeed = NONE;
                            destinationObj = null;
                        }
                        break;
                    case DRONES:
                        if (ship.droneCount == ship.maxDroneStorage) {
                            pointReached = false;
                            shipMode = PATROL;
                            supplyNeed = NONE;
                            destinationObj = null;
                        }
                        break;
                    case MINES:
                        if (ship.mineCount == ship.maxMineStorage) {
                            pointReached = false;
                            shipMode = PATROL;
                            supplyNeed = NONE;
                            destinationObj = null;
                        }
                        break;
                    case XTALS:
                        if (ship.currentCrystalCount == ship.maxCrystalStorage) {
                            pointReached = false;
                            shipMode = PATROL;
                            supplyNeed = NONE;
                            destinationObj = null;
                        }
                        break;
                    case LIFE:
                        if (ship.lifeSupport == 100) {
                            pointReached = false;
                            shipMode = PATROL;
                            supplyNeed = NONE;
                            destinationObj = null;
                        }
                        // end select
                }
            }
        } else if (shipMode == HIDE) {
            // not sure if this is going to get used... 'flee' submode, may take care of what i was envisioning for this
        } else if (shipMode == SAVE) {
            double curDistanceToDestination = TrekMath.getDistance(ship, destinationObj);
            doDock(curDistanceToDestination, true);

            if (ship.damage == 0) shipMode = PATROL;
        }
    }

    protected void evaluateSubMode() {
        switch (shipSubMode) {
            case NONE:
                break;
            case ATTACK:
                if (dodgeTimeout <= 0) doOffensiveMovement();
                fireWeapons();
                break;
            case FLEE:
                if (getEnemyShipsInRange(ship, 10000) < 1) {
                    shipSubMode = NONE;
                    logDebug("evaluateSubMode()   forcing shipSubMode from FLEE to NONE");
                }
                if (dodgeTimeout <= 0) doEscapeMovement();
                fireWeapons();
                break;
            case HEAL:
                if (getEnemyShipsInRange(ship, 2500) <= 1) {
                    if (shipMode == SUPPLY && ship.damage <= 25 && ship.warpEnergy > .75 * ship.maxWarpEnergy) {
                        runMacro("ccccccccccccccccccccccccccccccccc");
                        // the line following may be a problem for the healing con; requiring that it can move at cruise warp w/ shields
                    } else if (ship.damage > 100 ||
                            ((ship.warpEnergy + ship.impulseEnergy < ship.warpUsageRatio * 5 * ship.maxCruiseWarp + 20) && ship.antiMatter > 500)) {
                        if (dodgeTimeout <= 0) runMacro("\005ccccccccccccccccccccccccccccccccccccccccccc");
                    }

                    Vector shipsInRange = ship.currentQuadrant.getAllVisibleShipsInRange(ship, 2500);
                    for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
                        TrekShip theShip = (TrekShip) e.nextElement();

                        if (theShip == ship) continue;

                        if ((theShip.lockTarget == ship || (theShip.scanTarget == ship && TrekMath.getDistance(ship, theShip) < 500))
                                && dodgeTimeout <= 0) {
                            if (ship.damage > 75 && ship.damage < 100) doDefensiveMovement();
                            shipSubMode = ATTACK;
                            targetShip = theShip;
                        }
                    }
                } else {
                    if (dodgeTimeout <= 0) doDefensiveMovement();
                }
                fireWeapons();
                break;
        }
    }

    protected void setCurObjs() {
        if (quadrantName.equals("Alpha Quadrant")) {
            curObjs = alphaObjs;
        } else if (quadrantName.equals("Beta Quadrant")) {
            curObjs = betaObjs;
        } else if (quadrantName.equals("Gamma Quadrant")) {
            curObjs = gammaObjs;
        } else if (quadrantName.equals("Omega Quadrant")) {
            curObjs = omegaObjs;
        }
    }

    protected void checkForGold() {
        // check for any gold objects in the area
        Vector visObjs = ship.currentQuadrant.getVisibleObjects(ship);
        double nearest = ship.scanRange;
        for (Enumeration e = visObjs.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();
            if (TrekUtilities.isObjectGold(curObj)) {
                double curDist = TrekMath.getDistance(ship, curObj);
                if (curDist < nearest) {
                    destinationObj = curObj;
                    nearest = curDist;
                }
            } else if (TrekUtilities.isObjectShipDebris(curObj)) {
                TrekShipDebris tsd = (TrekShipDebris) curObj;
                double curDist = TrekMath.getDistance(ship, curObj);
                if (tsd.gold > 0 && curDist < nearest) {
                    destinationObj = curObj;
                    nearest = curDist;
                }
            }
        }
    }

    protected void doDock(double dist, boolean save) {
        //logDebug("doDock()   dist: " + dist + ", dest: " + destinationObj.name + ", reached: " + pointReached);
        if (TrekUtilities.isObjectShip(ship.scanTarget)) return;

        if (dodgeTimeout <= 0) {
            if (pointReached && destinationObj != null) {
                if (TrekMath.getDistance(ship, destinationObj) >= 2) {
                    pointReached = false;
                }
            }

            if (destinationObj != null && !pointReached) {

                // clear energy consumption
                if (!ship.orbitable && !ship.dockable) runMacro("\005");

                setIntercept(destinationObj);
                ship.scanTarget = destinationObj;

                // if we're getting close, and need supplies; check to see if enemies 'hold the destination'
                if (shipMode == SUPPLY && supplyNeed == TORPS && TrekMath.getDistance(ship, destinationObj) < ship.scanRange) {
                    TrekShip theShip = null;
                    boolean changeDest = false;
                    int enemyShipsNearDest = 0;
                    Vector theShips = ship.currentQuadrant.getAllShipsInRange(destinationObj, 501);
                    for (Enumeration e = theShips.elements(); e.hasMoreElements(); ) {
                        theShip = (TrekShip) e.nextElement();
                        if (theShip == ship) continue; // ignore current ship
                        if (botPeace && theShip.parent instanceof BotPlayer) {
                            BotPlayer theBot = (BotPlayer) theShip.parent;
                            if (theBot.botPeace && theBot.teamNumber == teamNumber) {
                                continue;
                            } else {
                                enemyShipsNearDest++;
                            }
                        } else {
                            enemyShipsNearDest++;
                        }
                    }

                    if (enemyShipsNearDest <= 1) {  // try to take on / bluff a single enemy
                        // avoid oblit ships if out of torps
                        if (enemyShipsNearDest > 0 && theShip.torpedoType == TrekShip.TORPEDO_OBLITERATOR) {
                            changeDest = true;
                        } else {
                            if (enemyShipsNearDest == 0) {
                                changeDest = false;
                            } else {
                                shipSubMode = ATTACK;
                                targetShip = theShip;
                            }
                        }

                    } else {
                        changeDest = true;
                    }

                    if (!changeDest) {
                    } else {
                        // find a new destination that provides the supply in need
                        TrekObject oldDest = destinationObj;
                        boolean foundSupply = false;
                        double nearest = 300000;
                        // find nearest torp provider
                        for (int x = 0; x < curObjs.length; x++) {
                            TrekObject curObj = ship.currentQuadrant.getObjectByScanLetter(curObjs[x]);
                            if (TrekUtilities.isObjectPlanet(curObj)) {
                                TrekPlanet curPlanet = (TrekPlanet) curObj;
                                if (curPlanet.givesTorps(ship)) {
                                    if (TrekMath.getDistance(ship, curPlanet) < nearest && TrekUtilities.isObjectPlanet(oldDest) && curPlanet != (TrekPlanet) oldDest) {
                                        destinationObj = curPlanet;
                                        nearest = TrekMath.getDistance(ship, curPlanet);
                                        foundSupply = true;
                                    }
                                }
                            } else if (TrekUtilities.isObjectStarbase(curObj)) {
                                TrekStarbase curBase = (TrekStarbase) curObj;
                                if (curBase.givesTorps && (TrekMath.getDistance(ship, curBase) < nearest && TrekUtilities.isObjectStarbase(oldDest) && curBase != (TrekStarbase) oldDest)) {
                                    destinationObj = curBase;
                                    nearest = TrekMath.getDistance(ship, curBase);
                                    foundSupply = true;
                                }
                            }
                        }

                        if (!foundSupply) {
                            destinationObj = oldDest;
                        }

                        setIntercept(destinationObj);
                        ship.scanTarget = destinationObj;
                        logDebug("doDock()   changing dest to: " + destinationObj.name);
                    }
                }

                if (dist > 100) {
                    if (Math.abs(ship.warpSpeed) < ship.maxCruiseWarp) {
                        runMacro("}ii]");
                        logDebug("doDock()   *** setting speed to max cruise");
                    }
                }
                if (dist > 11 && dist <= 100 && ship.warpSpeed != 8) {
                    runMacro("@8\r");
                }

                if (dist > 5 && dist <= 11 && ship.warpSpeed != 4) {
                    runMacro("@4\r");
                }

                if (dist < 5 && dist >= 2) {
                    runMacro("@3\r");
                }

                if (dist < 2) {
                    if (TrekUtilities.isObjectStarbase(destinationObj) && !ship.docked) {
                        if (ship.dockable) {
                            runMacro(".");
                            if (save) runMacro("\033S");  // stop, dock, and maybe save
                            pointReached = true;
                        } else {
                            runMacro("\027");
                        }
                    } else if (TrekUtilities.isObjectPlanet(destinationObj) && !ship.orbiting) {
                        if (ship.orbitable) {
                            runMacro(".");
                            pointReached = true;
                        } else {
                            runMacro("\027");
                        }
                    }
                }

                if (shipSubMode == HEAL || shipSubMode == FLEE) {
                    runMacro("ccccccccccccc");
                }

                if (slowZone()) runMacro("@8\r");
            }
        }
    }

    protected void doOffensiveMovement() {
        if (targetShip != null) {
            logDebug("doOffensiveMovement()   start speed: " + ship.warpSpeed);

            boolean doOverwarp = false;

            if (overwarpTimeout <= 0) runMacro("\005");

            if (ship.scanTarget == null || ship.scanTarget != targetShip) {
                logDebug("doOffensiveMovement()   scanning ship: " + targetShip.name);
                runMacro("o" + targetShip.scanLetter);
                if (targetShip == ship) logDebug("doOffensiveMovement()   *** trying to scan self");
            }

            if (cloakWait && !targetShip.cloaked) {
                cloakWait = false;
            }

            if (targetShip.cloaked && TrekMath.getDistance(ship, targetShip) < ship.scanRange) {

                // check for observer devices in the quad, and see if they have the target ship on scanner
                Vector obsDevs = ship.currentQuadrant.getObserverDevices();
                for (Enumeration e = obsDevs.elements(); e.hasMoreElements(); ) {
                    TrekObserverDevice curObs = (TrekObserverDevice) e.nextElement();
                    if (curObs.isScanning() && curObs.scanTarget == targetShip) {
                        // update coord in ship spotted database
                        if (ship.scannedHistory.containsKey(targetShip.scanLetter)) {
                            ship.scannedHistory.remove(targetShip.scanLetter);
                        }
                        ship.scannedHistory.put(targetShip.scanLetter, new TrekCoordHistory(targetShip.scanLetter, targetShip.name, new Trek3DPoint(targetShip.point)));
                        runMacro("}\033l" + targetShip.scanLetter);

                        // check for mine range, and drop one on last known coords ...
                        double theCurDist = TrekMath.getDistance(ship, targetShip);

                        if (ship.mines && ship.mineCount > 0 && ship.mineFireTimeout <= 0 &&
                                (mineResult == NONE || mineResult == HIT)) {

                            if (theCurDist >= 50 && theCurDist <= 250) {
                                double preserveCurSpeed = ship.warpSpeed;
                                runMacro("@-.1\rM");
                                runMacro("@" + preserveCurSpeed + "\r");
                            }
                        }

                        // check to see if we should throw a wide phaser blast in
                        if (ship.warpEnergy >= (ship.maxWarpEnergy * .85)) {
                            if (ship.checkPhaserCool() && theCurDist <= (ship.minPhaserRange / 3)) {
                                boolean doFire = true;
                                Vector objsInRange = ship.currentQuadrant.getAllObjectsInRange(ship, ship.minPhaserRange);
                                for (Enumeration en = objsInRange.elements(); en.hasMoreElements(); ) {
                                    TrekObject possBase = (TrekObject) en.nextElement();
                                    if (TrekUtilities.isObjectPlanet(possBase) || TrekUtilities.isObjectStarbase(possBase)) {
                                        doFire = false;
                                        break;
                                    }
                                }

                                if (doFire) {
                                    runMacro("\033\027PPPPPp\033\016");
                                }
                            }
                        }

                        break;
                    }
                }

                if (!cloakWait) {
                    runMacro("}\033l" + targetShip.scanLetter);
                    cloakWait = true;
                    //destinationObj = null;
                }

                // see if the enemy stopped near enough a base where supplies could be refueled
                //if (destinationObj == null) {
                if (cloakWait) {
                    boolean foundObj = false;
                    int checkDist = Math.abs(gen.nextInt() % 2500) + 500; // random distance from 500 to 3k
                    Vector nearbyObjs = ship.currentQuadrant.getAllObjectsInRange(ship, checkDist);
                    for (Enumeration e = nearbyObjs.elements(); e.hasMoreElements(); ) {
                        TrekObject curObj = (TrekObject) e.nextElement();
                        // if it's a base, or an xtal/torp planet
                        if (TrekUtilities.isObjectStarbase(curObj) ||
                                (TrekUtilities.isObjectPlanet(curObj) &&
                                        (((TrekPlanet) curObj).givesCrystals(ship) || ((TrekPlanet) curObj).givesTorps(ship)))) {
                            if (TrekUtilities.isObjectStarbase(curObj)) {
                                TrekStarbase theBase = (TrekStarbase) curObj;
                                if ((theBase.givesTorps && ship.torpedoCount < ship.maxTorpedoStorage) ||
                                        (theBase.givesDrones && ship.droneCount < ship.maxDroneStorage) ||
                                        (theBase.givesMines && ship.mineCount < ship.maxMineStorage) ||
                                        (theBase.fixesLifeSupport && ship.lifeSupport < 100)) {
                                    destinationObj = curObj;
                                    doDock(TrekMath.getDistance(ship, destinationObj), false);
                                    foundObj = true;
                                    break;
                                }
                            } else if (TrekUtilities.isObjectPlanet(curObj)) {
                                TrekPlanet thePlanet = (TrekPlanet) curObj;
                                if ((thePlanet.givesCrystals(ship) && ship.currentCrystalCount < ship.maxCrystalStorage) ||
                                        (thePlanet.givesTorps(ship) && ship.torpedoCount < ship.maxTorpedoStorage) ||
                                        (thePlanet.givesDrones(ship) && ship.droneCount < ship.maxDroneStorage) ||
                                        (thePlanet.givesMines(ship) && ship.mineCount < ship.maxMineStorage) ||
                                        (thePlanet.repairsLifeSupport(ship) && ship.lifeSupport < 100)) {
                                    destinationObj = curObj;
                                    doDock(TrekMath.getDistance(ship, destinationObj), false);
                                    foundObj = true;
                                    break;
                                }
                            }
                        }
                    }

                    if (!foundObj) {
                        runMacro("}\033l" + targetShip.scanLetter);
                    }

                } else {
                    doDock(TrekMath.getDistance(ship, destinationObj), false);
                }

                // close enough to their last seen position
                if (ship.intCoordPoint != null && ship.point != null && TrekMath.getDistance(ship.intCoordPoint, ship.point) < 50) {
                    runMacro("\027");

                    if (ship.phaserType == TrekShip.PHASER_EXPANDINGSPHEREINDUCER) {
                        Vector objsInPhRange = ship.currentQuadrant.getAllObjectsInRange(ship, ship.minPhaserRange);
                        boolean shoot = true;
                        for (Enumeration e = objsInPhRange.elements(); e.hasMoreElements(); ) {
                            if (!shoot) break;  // once we establish not to shoot, break out of the for loop
                            TrekObject curObj = (TrekObject) e.nextElement();
                            if (TrekUtilities.isObjectPlanet(curObj) || TrekUtilities.isObjectStarbase(curObj) || TrekUtilities.isObjectDrone(curObj)) {
                                if (TrekUtilities.isObjectDrone(curObj)) {
                                    TrekDrone curDrone = (TrekDrone) curObj;
                                    if (curDrone.owner == ship) shoot = false;  // try to avoid killing their own drones
                                }
                                if (TrekMath.getDistance(ship, curObj) < .5) continue;  // ESI won't hit it
                                shoot = false;
                            }
                        }

                        if (shoot) {
                            // no objects around; need to check a small phaser burst, and see if enemy is still nearby, before firing full
                            if (phaserTestTimeout <= 0) {
                                int prevDmg = ship.totalDamageGiven;
                                runMacro("Pp");

                                int diff = ship.totalDamageGiven - prevDmg;  // doesn't take into account what ship is getting shot ... this could suck
                                shoot = (diff >= 15) ? true : false;
                                phaserTestTimeout = 1;

                                if (shoot) runMacro("PPPPPPPPPPPPPPPPPPPp");
                                logDebug("doOffensiveMovement()   firing ESI phasers");
                            }
                        }
                    }
                }

                if (ship.damage > 0 || ship.warpEnergy < ship.maxWarpEnergy) {
                    runMacro("cccccccccccccccccccc");
                }
                return;
            } else {

                // move toward an enemy ship
                // if ship weapons range > enemy range, maintain outside influence
                // if enemies range is greater: if < (random 30 - 100) units out of range, do torp wasting maneuvers;
                // or do normal warp in; or do overwarp in

                double dist = TrekMath.getDistance(ship, targetShip);
                logDebug("doOffensiveMovement()   distance to target: " + dist);

                // if overwarping, and still out of range, don't recalc strategy, just keep on going in until overwarp timeout is 0
                if (dist > ship.maxTorpedoRange && overwarpTimeout > 0) return;

                if (ship.scanTarget != null && (ship.interceptTarget == null || ship.interceptTarget != targetShip)) {
                    if (ship.warpSpeed < 0) {
                        runMacro("{i[");
                    } else {
                        runMacro("}i]");
                    }
                }

                double desiredDist = 0;

                if (dist > targetShip.maxTorpedoRange) {
                    // currently outside the enemy ship's weapons range
                    if (ship.maxTorpedoRange > targetShip.maxTorpedoRange && dist <= ship.maxTorpedoRange) {
                        // bot ship has greater range than enemy ship
                        if (ship.torpedoCount > 0) {
                            // try to maintain that difference; staying outside of enemy weapons range while smacking them around
                            //ship.warpSpeed = targetShip.warpSpeed;
                            //ship.changeHeading(new Integer(targetShip.getHeading()).intValue(), new Integer(targetShip.getPitch().substring(1)).intValue());
                            desiredDist = ship.maxTorpedoRange - 50;
                            logDebug("doOffensiveMovement()   extended range: in torp range, and have torps");
                        } else {
                            // out of torpedoes, move in, if they're wounded, and our torps don't regenerate
                            if (ship.torpedoType != TrekShip.TORPEDO_OBLITERATOR) { // && targetShip.damage > 50) {
                                desiredDist = 50;
                                logDebug("doOffensiveMovement()   extended range: in torp range, but no torps");
                            }
                        }
                    } else {
                        // ship still out of enemy's range, but enemy has > or = bot ship's weapons range; try to make them waste some
                        int wasteDist = Math.abs(gen.nextInt() % 70) + 1;

                        if (wasteDist > dist - targetShip.maxTorpedoRange) {
                            int action = Math.abs(gen.nextInt() % 5);
                            logDebug("doOffensiveMovement()   backOutCount: " + backOutCount);
                            switch (action) {
                                case 0:
                                    // check current energy levels, compare to enemy levels
                                    // check enemy range, vs current range (torp type, etc.)
                                    // check enemy max speed vs current max speed
                                    // determine whether or not to overwarp in based on this data
                                    if (targetShip.maxCruiseWarp >= ship.maxCruiseWarp && Math.abs(targetShip.warpSpeed) >= Math.abs(ship.warpSpeed) &&
                                            ship.warpEnergy >= (ship.maxWarpEnergy * .85) && dist >= ship.maxTorpedoRange) {
                                        desiredDist = ship.maxTorpedoRange;
                                        // base overwarp on ship's skill level
                                        // a ship with skill 1, has a 33% chance
                                        // a ship with skill 5, has a 9% chance
                                        if (Math.abs(gen.nextInt() % (skillLevel * 2 + 1)) < 1) {
                                            doOverwarp = true;
                                            overwarpTimeout = 4;
                                        }
                                    }
                                    break;
                                case 1:
                                    // back away
                                    if (backOutCount < 5) {
                                        runMacro("[");
                                        backOutCount++;
                                    } else
                                        runMacro("]");
                                    break;
                                case 2:
                                    // cloak if able, or normal in
                                    if (ship.cloak) {
                                        runMacro("\005z");
                                    }
                                    desiredDist = ship.maxTorpedoRange;
                                    break;
                                case 3:
                                    // normal in
                                    desiredDist = ship.maxTorpedoRange;
                                    break;
                                case 4:
                                    // if enemy speed is greater, then use brief negative overwarp
                                    if (Math.abs(targetShip.warpSpeed) > Math.abs(ship.maxCruiseWarp) && (dist - targetShip.maxTorpedoRange) <= 25) {
                                        doOverwarp = true;
                                        overwarpTimeout = 1;

                                        if (backOutCount < 5) {
                                            desiredDist = targetShip.maxTorpedoRange + 50;
                                            backOutCount++;
                                        } else {
                                            desiredDist = ship.maxTorpedoRange - 50;
                                            if (ship.warpUsageRatio <= targetShip.warpUsageRatio && ship.warpEnergy >= (ship.maxWarpEnergy * .85)) {
                                                // nothing; it's ok to overwarp
                                            } else {
                                                // override overwarp; not a good time to do it
                                                overwarpTimeout = 0;
                                                doOverwarp = false;
                                            }
                                        }

                                    } else {
                                        // normal back-out
                                        if (backOutCount < 5) {
                                            runMacro("[");
                                            backOutCount++;
                                        } else
                                            runMacro("]");
                                    }
                            }

                        } else {
                            desiredDist = ship.maxTorpedoRange - 50;

                            if (Math.abs(targetShip.warpSpeed) == ship.maxCruiseWarp && ship.torpedoCount > 0 &&
                                    ship.warpEnergy >= (ship.maxWarpEnergy * .85) && dist <= 2000 && dist >= ship.maxTorpedoRange) {
                                // target ship is running
                                if (Math.abs(gen.nextInt() % (skillLevel * 10)) < 1) { // 1 in 10 chance of overwarping for skillLevel 1
                                    doOverwarp = true;
                                    overwarpTimeout = 4;
                                } else {
                                    // normal in
                                }
                            } else {
                                // normal in
                            }
                        }
                    }
                    // already in enemy torp range
                } else { // in enemy torp range
                    // see if in bot torp range
                    logDebug("doOffensiveMovement()   --- > dist: " + dist);
                    if (dist > ship.maxTorpedoRange) {
                        // check current energy levels, compare to enemy levels
                        // check enemy range, vs current range (torp type, etc.)
                        // check enemy max speed vs current max speed
                        // determine whether or not to overwarp in based on this data
                        desiredDist = ship.maxTorpedoRange - 50;

                        logDebug("doOffensiveMovement()   --- > tg: " + targetShip.warpSpeed + ", ship: " + ship.warpSpeed +
                                ", en: " + ship.warpEnergy);

                        if (Math.abs(dist - targetShip.maxTorpedoRange) < 100 && targetShip.torpedoType == TrekShip.TORPEDO_PHOTON &&
                                Math.abs(gen.nextInt()) % 4 < 1) {
                            // 1 in 4 chance, back out, try to make them waste some torps
                            desiredDist = targetShip.maxTorpedoRange + 55;

                            if (Math.abs(targetShip.warpSpeed) >= Math.abs(ship.warpSpeed) && Math.abs(gen.nextInt()) % (skillLevel * 2) < 1) {
                                // 1 in 2 chance for level 1, if speed is being matched, to overwarp out
                                doOverwarp = true;
                                overwarpTimeout = 2;
                            }

                        } else if (Math.abs(targetShip.warpSpeed) >= Math.abs(ship.warpSpeed) &&
                                ship.warpEnergy >= (ship.maxWarpEnergy * .85)) {
                            logDebug("doOffensiveMovement()   --- > out of range, checking for overwarp");
                            // 1 in 4 chance of overwarping for skillLevel 1
                            if (Math.abs(gen.nextInt() % (skillLevel * 4)) < 1) {
                                doOverwarp = true;
                                overwarpTimeout = 2;
                                logDebug("doOffensiveMovement()   --- > out of range, do it!");
                            } else {
                                // normal in
                                logDebug("doOffensiveMovement()   --- > out of range, nah...");
                            }
                        } else {
                            // normal in
                        }
                    } else if (dist < ship.maxTorpedoRange && dist > ship.minTorpedoRange && (ship.torpedoType == TrekShip.TORPEDO_OBLITERATOR || ship.torpedoCount > 0)) {
                        // determine whether to use overwarp
                        if (Math.abs(targetShip.warpSpeed) >= Math.abs(ship.warpSpeed) &&
                                ship.warpEnergy >= (ship.maxWarpEnergy * .85) &&
                                ship.maxTorpedoRange > targetShip.maxTorpedoRange) {

                            // 1 in 4 chance of overwarping for skillLevel 1
                            if (Math.abs(gen.nextInt() % (skillLevel * 4)) < 1) {
                                doOverwarp = true;
                                overwarpTimeout = 2;
                            }

                            logDebug("doOffensiveMovement()   check for ship overwarp: " + doOverwarp);
                        }

                        // torp range; try to optimize
                        switch (ship.torpedoType) {
                            // photon firing bot
                            case (TrekShip.TORPEDO_PHOTON):

                                // TODO: tweak photon ranges based on enemy torp types
                                switch (targetShip.torpedoType) {
                                    case (TrekShip.TORPEDO_PHOTON):
                                        desiredDist = ship.maxTorpedoRange - 50;
                                        logDebug("doOffensiveMovement()   photon vs photon: maintain outside presence");
                                        break;

                                    case (TrekShip.TORPEDO_OBLITERATOR):
                                    case (TrekShip.TORPEDO_NORMAL):
                                    case (TrekShip.TORPEDO_BOLTPLASMA):
                                        if (targetShip.shipClass.equals("KEV-12") && dist < 1000 && dist > 600) {
                                            // overwarp strategy versus kev -- deliver as much pain as quickly as possible
                                            if (ship.warpEnergy > (ship.maxWarpEnergy * .7)) {
                                                if (Math.abs(gen.nextInt()) % (skillLevel * 3) < 1) {
                                                    // 1 in 3 of overwarping out for skill level 1
                                                    doOverwarp = true;
                                                    overwarpTimeout = 4;
                                                }
                                            }
                                        }
                                        desiredDist = ship.maxTorpedoRange - 50;
                                        logDebug("doOffensiveMovement()   photon vs oblit/norm/bp: maintain outside presence");
                                        break;

                                    case (TrekShip.TORPEDO_PLASMA):
                                    case (TrekShip.TORPEDO_VARIABLESPEEDPLASMA):
                                        desiredDist = ship.maxTorpedoRange - 50;
                                        logDebug("doOffensiveMovement()   photon vs plasma: maintain outside presence");
                                        break;
                                }
                                break;

                            // oblit, normal (freighter), or boltplasma firing bot
                            case (TrekShip.TORPEDO_OBLITERATOR):
                            case (TrekShip.TORPEDO_NORMAL):
                            case (TrekShip.TORPEDO_BOLTPLASMA):
                                switch (targetShip.torpedoType) {
                                    case (TrekShip.TORPEDO_PHOTON):
                                        if (ship.maxTorpedoRange > targetShip.maxTorpedoRange) {
                                            // maintain outside presence
                                            desiredDist = ship.maxTorpedoRange - ((ship.maxTorpedoRange - targetShip.maxTorpedoRange) / 2);
                                            logDebug("doOffensiveMovement()   bolt-plasma/oblit/norm: maintain outside presence");
                                        } else if (ship.minTorpedoRange >= targetShip.minTorpedoRange) {
                                            desiredDist = ship.minTorpedoRange + 50;
                                            logDebug("doOffensiveMovement()   bolt-plasma/oblit/norm: maintain inside presence");
                                        } else {
                                            desiredDist = targetShip.minTorpedoRange + 50;
                                            logDebug("doOffensiveMovement()   bolt-plasma/oblit/norm: maintain inside presence, in enemy min torp range");
                                        }
                                        break;

                                    case (TrekShip.TORPEDO_OBLITERATOR):
                                    case (TrekShip.TORPEDO_NORMAL):
                                    case (TrekShip.TORPEDO_BOLTPLASMA):
                                        if (ship.maxTorpedoRange > targetShip.maxTorpedoRange) {
                                            // maintain outside presence
                                            desiredDist = ship.maxTorpedoRange - ((ship.maxTorpedoRange - targetShip.maxTorpedoRange) / 2);
                                            // use overwarp to take advantage of longer torp rangee
                                            if (ship.maxCruiseWarp == targetShip.maxCruiseWarp && ship.warpEnergy >= (ship.maxWarpEnergy * .85))
                                                doOverwarp = true;
                                            logDebug("doOffensiveMovement()   bp/oblit/norm vs bp/oblit/norm: maintain outside presence");
                                        } else if (ship.minTorpedoRange <= targetShip.minTorpedoRange) {
                                            if (ship.minTorpedoRange == targetShip.minTorpedoRange) {
                                                desiredDist = ship.minTorpedoRange + 50;  // lurk on just inside of minimum range
                                                logDebug("doOffensiveMovement()   bp/oblit/norm vs bp/oblit/norm: maintain inside presence");
                                            } else {
                                                desiredDist = ship.minTorpedoRange + ((targetShip.minTorpedoRange - ship.minTorpedoRange) / 2);
                                                logDebug("doOffensiveMovement()   bp/oblit/norm vs bp/oblit/norm: maintain middle presence");
                                            }
                                        } else {
                                            // ship has a smaller max torp range, and a larger min torp range ... that sucks; lurk
                                            desiredDist = targetShip.minTorpedoRange + 50;
                                            logDebug("doOffensiveMovement()   bp/oblit/norm vs bp/oblit/norm: maintain sucky presence");
                                        }
                                        break;

                                    case (TrekShip.TORPEDO_PLASMA):
                                    case (TrekShip.TORPEDO_VARIABLESPEEDPLASMA):
                                        desiredDist = (ship.maxTorpedoRange - ship.minTorpedoRange);
                                        logDebug("doOffensiveMovement()   bp/oblit/norm vs plasma: maintain mid-range presence");
                                        break;
                                }
                                break;

                            // plasma firing bot
                            case (TrekShip.TORPEDO_PLASMA):
                            case (TrekShip.TORPEDO_VARIABLESPEEDPLASMA):
                                break;
                        }

                        if (Math.abs(dist - desiredDist) < 50) doOverwarp = false;

                    } else {
                        // move in to phaser/mine range
                        if (ship.mines && ship.mineCount > 0 && ship.mineFireTimeout <= 0) {
                            if (targetShip.shipClass.equals("KEV-12") && dist < 900 && dist > 550) {
                                if (ship.warpEnergy > (ship.maxWarpEnergy * .7) && Math.abs(gen.nextInt()) % (skillLevel * 2) < 1) {
                                    // 1 in 2 chance of overwarping in for skillLevel 1
                                    doOverwarp = true;
                                    overwarpTimeout = 4;
                                }
                            }
                            desiredDist = 125;
                            logDebug("doOffensiveMovement()   phaser/mine");
                        } else {
                            if (ship.damage > targetShip.damage && targetShip.torpedoType == TrekShip.TORPEDO_PHOTON) {
                                desiredDist = ((targetShip.minTorpedoRange + targetShip.minPhaserRange) / 2);
                                logDebug("doOffensiveMovement()   min enemy torp range");
                            } else {
                                if (ship.torpedoCount > 0 && targetShip.torpedoType == TrekShip.TORPEDO_NORMAL) {
                                    desiredDist = ship.maxTorpedoRange - 50;
                                    logDebug("doOffensiveMovement()   phaser: moving back out to torp range");
                                } else {
                                    desiredDist = 0;
                                    logDebug("doOffensiveMovement()   phaser: point-blank");
                                }
                            }
                        }
                    }
                }

                logDebug("doOffensiveMovement()   dist: " + dist + " - desiredDist: " + desiredDist + " = " + (dist - desiredDist));
                if (Math.abs(dist - desiredDist) < 50) {
                    // pretty close to target range, call it good -- match speed / heading
                    if (Math.abs(targetShip.warpSpeed) > Math.abs(ship.warpSpeed)) {
                        // can't keep up; just go as fast as possible
                        //ship.changeHeading(new Integer(targetShip.getHeading()).intValue(), new Integer(targetShip.getPitch().substring(1)).intValue());
                        if (targetShip.warpSpeed > 0) {
                            runMacro("\005ii");
                            if (doOverwarp) {
                                runMacro("@" + ship.damageWarp + "\r");
                                overwarpTimeout = 2;
                            } else
                                runMacro("]");
                            if (ship.damage > 0 || ship.warpEnergy < ship.maxWarpEnergy)
                                runMacro("cccccccccccccccccccc");
                        } else {
                            runMacro("\005ii");
                            if (doOverwarp) {
                                runMacro("@-" + ship.damageWarp + "\r");
                                overwarpTimeout = 2;
                            } else
                                runMacro("[");
                            if (ship.damage > 0 || ship.warpEnergy < ship.maxWarpEnergy)
                                runMacro("cccccccccccccccccccc");
                        }
                    } else {
                        ship.warpSpeed = 0;
                        ship.changeHeading(new Integer(targetShip.getHeading()).intValue(), new Integer(targetShip.getPitch().substring(1)).intValue());
                        ship.warpSpeed = targetShip.warpSpeed;
                    }
                } else {
                    if (dist > desiredDist) {
                        runMacro("\005ii");
                        if (doOverwarp) {
                            runMacro("@" + ship.damageWarp + "\r");
                            overwarpTimeout = 2;
                            logDebug("doOffensiveMovement()   distance correction : overwarp in");
                        } else {
                            runMacro("]");
                            logDebug("doOffensiveMovement()   distance correction : normal in");
                        }
                        if (ship.damage > 0 || ship.warpEnergy < ship.maxWarpEnergy)
                            runMacro("cccccccccccccccccccc");
                    } else {
                        runMacro("\005ii");
                        if (doOverwarp) {
                            runMacro("@-" + ship.damageWarp + "\r");
                            //ship.warpSpeed = -ship.damageWarp;

                            overwarpTimeout = 2;
                            logDebug("doOffensiveMovement()   distance correction : overwarp out -- warp speed: " + ship.warpSpeed);
                        } else {
                            runMacro("[");
                            logDebug("doOffensiveMovement()   distance correction : normal out");
                        }
                        if (ship.damage > 0 || ship.warpEnergy < ship.maxWarpEnergy)
                            runMacro("cccccccccccccccccccc");
                    }
                }

                logDebug("doOffensiveMovement()   desiredDist: " + desiredDist);
            }
        }

        if (slowZone() && Math.abs(ship.warpSpeed) > 8) {
            if (ship.warpSpeed < 0) runMacro("@-8\r");
            else runMacro("@8\r");
        }

        if (pointReached && ship.warpSpeed != 0) pointReached = false;

        if (ship.damage > 0 || ship.warpEnergy < ship.maxWarpEnergy)
            runMacro("ccccccccccccccccccccccc");

        logDebug("doOffensiveMovement()   end speed: " + ship.warpSpeed);
    }

    protected void doDefensiveMovement() {
        runMacro("\005o.ii[cccccccccccccccccccccc");

        if (ship.mines && ship.mineCount > 0 && ship.mineFireTimeout <= 0 && getEnemyShipsInRange(ship, 350) > 0 && Math.abs(ship.warpSpeed) > 8)
            runMacro("M");

        if (slowZone()) {
            if (ship.warpSpeed < 0) runMacro("@-8\r");
            else runMacro("@8\r");
        }

        if (pointReached && ship.warpSpeed != 0) pointReached = false;
    }

    protected void doEscapeMovement() {
        runMacro("\005o.ii[cccccccccccccccccccccc");

        if (slowZone()) {
            if (ship.warpSpeed < 0) runMacro("@-8\r");
            else runMacro("@8\r");
        }

        if (pointReached && ship.warpSpeed != 0) pointReached = false;
    }

    protected void dodgePlasma() {
        if (dodgeTimeout <= 0) {
            double nearest = 500;
            TrekTorpedo dodgePlasma = null;
            Vector plasmaObjs = ship.currentQuadrant.getVisibleObjects(ship);

            for (Enumeration e = plasmaObjs.elements(); e.hasMoreElements(); ) {
                TrekObject curObj = (TrekObject) e.nextElement();
                if (TrekUtilities.isObjectTorpedo(curObj) && TrekMath.getDistance(ship, curObj) < nearest) {
                    TrekTorpedo curTorp = (TrekTorpedo) curObj;
                    // ignore plasma that is owned by 'us'
                    if (TrekUtilities.isObjectShip(curTorp.owner) && (TrekShip) (curTorp.owner) == ship) continue;

                    if (curTorp.torpType == TrekShip.TORPEDO_PLASMA || curTorp.torpType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                        dodgePlasma = curTorp;
                        nearest = TrekMath.getDistance(ship, curTorp);
                    }
                }
            }

            if (dodgePlasma != null) {
                TrekObject preserveScan = ship.scanTarget;

                runMacro("\005O" + dodgePlasma.scanLetter + "i");

                boolean negPitch = ship.getPitch().substring(0, 1).equals("-");
                int pitch = new Integer(ship.getPitch().substring(1)).intValue();

                if (pitch >= 75) {
                    if (negPitch) {
                        runMacro("KKKKKKKKKKKKKK]");
                    } else {
                        runMacro("JJJJJJJJJJJJJJ]");
                    }
                } else {
                    runMacro("LLLLLLLLLLLLLLLLLL]");
                }

                dodgeTimeout = 3;
                if (preserveScan != null && preserveScan instanceof TrekShip) {
                    runMacro("o" + preserveScan.scanLetter);
                } else {
                    runMacro("O" + preserveScan.scanLetter);
                }
            }
        }

        if (slowZone()) {
            if (ship.warpSpeed < 0) runMacro("@-8\r");
            else runMacro("@8\r");
        }

        if (pointReached && ship.warpSpeed != 0) pointReached = false;
    }

    protected boolean slowZone() {
        // TODO: improve method so that it can allow a bot to go faster in asteroids if it's to get in weapons range
        // or escape enemy weapon range
        boolean slowZone = false;

        if (ship.asteroidTarget != null) slowZone = true;

        Vector objs = ship.currentQuadrant.getAllObjectsInRange(ship, 1000);
        for (Enumeration e = objs.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();
            double curDist = TrekMath.getDistance(ship, curObj);
            if (TrekUtilities.isObjectIridium(curObj)) slowZone = true;
            if (TrekUtilities.isObjectShipDebris(curObj) && curDist <= 500) slowZone = true;

            if (slowZone) break;  // only need to identify one object that causes ship to slow
        }

        if (slowZone && Math.abs(ship.warpSpeed) > 8) {
            slowZone = true;
        } else {
            slowZone = false;
        }

        return slowZone;
    }

    protected void fireWeapons() {
        // handles firing of all weapons EXCEPT that doOffensiveMovement will drop mines and fire ESI / wide on a cloaked enemy vessel
        if (!ship.organiaModifier && !ship.cloaked && (ship.cloakFireTimeout <= 0) && (ship.saveTimeout <= 0)) {
            Vector shipsInRange = ship.currentQuadrant.getAllVisibleShipsInTorpRange(ship, ship.maxTorpedoRange);

            TrekShip torpTarget = null;
            TrekShip phaserTarget = null;
            double preserveSpeed = ship.warpSpeed;
            logDebug("fireWeapons()   preserve speed: " + preserveSpeed);

            if (ship.torpedoCount > 0 && ship.torpFireTimeout <= 0) {
                if (ship.torpedoType == TrekShip.TORPEDO_PHOTON) {  // try to select the best target based on range
                    double maxRange = 0;

                    for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
                        TrekShip curShip = (TrekShip) e.nextElement();

                        // check to see if it's another botplayer, and if the two ships have peace
                        if (botPeace && curShip.parent instanceof BotPlayer) {
                            BotPlayer curBot = (BotPlayer) curShip.parent;
                            if (curBot.botPeace && curBot.botTeam == botTeam) continue;
                        }

                        double curDistance = TrekMath.getDistance(ship, curShip);
                        if (curDistance >= maxRange && curDistance <= ship.maxTorpedoRange) {
                            maxRange = TrekMath.getDistance(ship, curShip);
                            torpTarget = curShip;
                        }
                    }

                    // if we've found a target, and it's within 80% of maximum torp range, then fire
                    // make an exception with br1k versus br5... allow it to fire torps down to 900 distance.
                    if ((torpTarget != null) &&
                            (((ship.maxTorpedoRange - TrekMath.getDistance(ship, torpTarget)) < (ship.maxTorpedoRange * .20)) ||
                                    ((ship.shipClass.equals("BR-1000")) &&
                                            (ship.maxTorpedoRange - TrekMath.getDistance(ship, torpTarget)) < (ship.maxTorpedoRange * .60)))) {
                        runMacro("\005o" + torpTarget.scanLetter + "\014");
                        if (torpTarget == ship) logDebug("fireWeapons()   *** trying to scan self #1");
                        for (int tc = 0; tc < ship.maxTorpedoBanks; tc++) {
                            runMacro("T");
                            if (ship.torpedoCount == 0) break;
                        }
                        runMacro("t");

                        if (ship.drones && ship.droneFireTimeout <= 0 && ship.droneCount > 0) {
                            if (ship.variableSpeed) {
                                ship.droneSpeed = Math.abs(gen.nextInt() % 5) + 8;  // warp 8 to 12
                            }
                            runMacro("d");
                        }

                        runMacro("@" + preserveSpeed + "\r");
                    }
                } else if (ship.torpedoType == TrekShip.TORPEDO_OBLITERATOR || ship.torpedoType == TrekShip.TORPEDO_BOLTPLASMA || ship.torpedoType == TrekShip.TORPEDO_NORMAL) {
                    // fire if there's an enemy in range; pick the most wounded if there are multiples
                    int mostHurt = 0;

                    for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
                        TrekShip curShip = (TrekShip) e.nextElement();

                        // check to see if it's another botplayer, and if the two ships have peace
                        if (botPeace && curShip.parent instanceof BotPlayer) {
                            BotPlayer curBot = (BotPlayer) curShip.parent;
                            if (curBot.botPeace && curBot.botTeam == botTeam) continue;
                        }

                        double curDistance = TrekMath.getDistance(ship, curShip);
                        int curDmg = curShip.damage;
                        if (curDmg >= mostHurt && curDistance <= ship.maxTorpedoRange && curDistance >= ship.minTorpedoRange) {
                            mostHurt = curDmg;
                            torpTarget = curShip;
                        }
                    }

                    if (torpTarget != null) {
                        runMacro("\005o" + torpTarget.scanLetter + "\014");
                        if (torpTarget == ship) logDebug("fireWeapons()   *** trying to scan self #2");
                        for (int tc = 0; tc < ship.maxTorpedoBanks; tc++) {
                            runMacro("T");
                            if (ship.torpedoCount == 0) break;
                        }
                        runMacro("t");

                        if (ship.drones && ship.droneFireTimeout <= 0 && ship.droneCount > 0) {
                            if (ship.variableSpeed) {
                                ship.droneSpeed = Math.abs(gen.nextInt() % 5) + 8;  // warp 8 to 12
                            }
                            runMacro("d");
                        }

                        runMacro("@" + preserveSpeed + "\r");
                    }
                } else if (ship.torpedoType == TrekShip.TORPEDO_PLASMA || ship.torpedoType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                    // fire torp routine for plasma ships
                }
                logDebug("fireWeapons()   ship speed after torps: " + ship.warpSpeed);
            }

            // see if there's anything to phaser
            if (ship.checkPhaserCool()) {
                double maxDist = ship.minPhaserRange;

                for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
                    TrekShip curShip = (TrekShip) e.nextElement();

                    // check to see if it's another botplayer, and if the two ships have peace
                    if (botPeace && curShip.parent instanceof BotPlayer) {
                        BotPlayer curBot = (BotPlayer) curShip.parent;
                        if (curBot.botPeace && curBot.botTeam == botTeam) continue;
                    }

                    double curDist = (TrekMath.getDistance(ship, curShip));

                    if ((ship.phaserType == TrekShip.PHASER_EXPANDINGSPHEREINDUCER && curDist >= .5 && curDist <= maxDist) ||
                            (ship.phaserType != TrekShip.PHASER_EXPANDINGSPHEREINDUCER && curDist <= maxDist)) {
                        maxDist = curDist;
                        phaserTarget = curShip;
                    }
                }

                if (phaserTarget != null) {
                    if (shipSubMode == HEAL) {
                        // determine whether or not to fire; consider the enemy's damage compared to ours...
                        // may be best to conserve energy, back out, flee, etc., if possible

                        if (phaserTarget.torpedoType == TrekShip.TORPEDO_PLASMA || phaserTarget.torpedoType == TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                            // try to keep enough energy to go warp 8 (plasma dodge)
                            int energyToFire = ship.getAvailablePower() - (8 * 5 * ship.warpUsageRatio);
                            logDebug("fireWeapons()   energyToFire: " + energyToFire);
                            if (energyToFire >= 5) {
                                runMacro("\005o" + phaserTarget.scanLetter + "\014");
                                if (phaserTarget == ship) logDebug("fireWeapons()   *** trying to scan self #3");
                                for (int x = energyToFire; x >= 5; x -= 5) {
                                    runMacro("P");
                                }

                                if (ship.phaserType == TrekShip.PHASER_EXPANDINGSPHEREINDUCER) {
                                    Vector objsInPhRange = ship.currentQuadrant.getAllObjectsInRange(ship, ship.minPhaserRange);
                                    boolean shoot = true;
                                    for (Enumeration e = objsInPhRange.elements(); e.hasMoreElements(); ) {
                                        if (!shoot) break;  // once we establish not to shoot, break out of the for loop
                                        TrekObject curObj = (TrekObject) e.nextElement();
                                        if (TrekUtilities.isObjectPlanet(curObj) || TrekUtilities.isObjectStarbase(curObj)) {
                                            if (TrekMath.getDistance(ship, curObj) < .5) continue;  // ESI won't hit it
                                            shoot = false;
                                        }
                                    }

                                    if (shoot) runMacro("p");
                                    else runMacro("PPPPPPPPPPPPPPPPPPPP");
                                } else {
                                    runMacro("p");
                                }
                            }
                        }
                    } else if (maxDist <= ship.minPhaserRange / 2 || ship.phaserType == TrekShip.PHASER_DISRUPTOR) {  // only fire if it's worthwhile...
                        runMacro("\005o" + phaserTarget.scanLetter + "\014");
                        if (phaserTarget == ship) logDebug("fireWeapons()   *** trying to scan self #4");
                        for (int pc = 0; pc < ship.maxPhaserBanks / 5; pc++) {
                            runMacro("P");
                        }

                        if (ship.phaserType == TrekShip.PHASER_EXPANDINGSPHEREINDUCER) {
                            Vector objsInPhRange = ship.currentQuadrant.getAllObjectsInRange(ship, ship.minPhaserRange);
                            boolean shoot = true;
                            for (Enumeration e = objsInPhRange.elements(); e.hasMoreElements(); ) {
                                if (!shoot) break;  // once we establish not to shoot, break out of the for loop
                                TrekObject curObj = (TrekObject) e.nextElement();
                                if (TrekUtilities.isObjectPlanet(curObj) || TrekUtilities.isObjectStarbase(curObj) || TrekUtilities.isObjectDrone(curObj)) {
                                    if (TrekUtilities.isObjectDrone(curObj)) {
                                        TrekDrone curDrone = (TrekDrone) curObj;
                                        if (curDrone.owner == ship)
                                            shoot = false;  // try to avoid killing their own drones
                                    }
                                    if (TrekMath.getDistance(ship, curObj) < .5) continue;  // ESI won't hit it
                                    shoot = false;
                                }
                            }

                            if (shoot) runMacro("p");
                            else runMacro("PPPPPPPPPPPPP");
                            logDebug("fireWeapons()   firing ESI phasers");

                        } else {
                            runMacro("p");
                            logDebug("fireWeapons()   firing phasers");
                        }
                    }

                    if (ship.drones && ship.droneFireTimeout <= 0 && ship.droneCount > 0) {
                        if (ship.variableSpeed) {
                            ship.droneSpeed = Math.abs(gen.nextInt() % 5) + 8;  // warp 8 to 12
                        }
                        runMacro("d");
                    }

                    // see if we should throw in a mine
                    if (ship.mines && ship.mineFireTimeout <= 0 && ship.mineCount > 0) {
                        double curDist = TrekMath.getDistance(ship, phaserTarget);
                        if (curDist >= 65 && curDist <= 235) {
                            runMacro("\027o" + phaserTarget.scanLetter + "ii@-.2\rM");
                            if (phaserTarget == ship) logDebug("fireWeapons()   *** trying to scan self #5");
                        }
                    }

                    runMacro("@" + preserveSpeed + "\r");
                }
                logDebug("fireWeapons()   ship speed after phasers: " + ship.warpSpeed);
            }
        }

        if (shipSubMode == HEAL) runMacro("cccccccccccccccccccccccccc");
    }

    protected int getEnemyShipsInRange(TrekObject obj, int dist) {
        int enemyShips = 0;

        Vector shipsInRange = ship.currentQuadrant.getAllVisibleShipsInRange(obj, dist);
        for (Enumeration e = shipsInRange.elements(); e.hasMoreElements(); ) {
            TrekShip curShip = (TrekShip) e.nextElement();

            if (curShip == ship) continue;

            if (botPeace && curShip.parent instanceof BotPlayer) {
                BotPlayer curBot = (BotPlayer) curShip.parent;
                if (curBot.botPeace && curBot.botTeam == botTeam) continue;
            }

            if (curShip instanceof ShipQ) continue;

            enemyShips++;
        }

        return enemyShips;
    }

    protected void logDebug(String s) {
        if (doDebug) {
            s = s + "\r\n";
            try {
                debugFile.write(s.getBytes());
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public void toggleDebug() {
        if (!doDebug) {
            doDebug = true;
            try {
                debugFile = new FileOutputStream("./data/debugBot.log", true);
                debugFile.write(("Enabling debug for " + ship.name + "\r\n").getBytes());
            } catch (FileNotFoundException fnfe) {
                fnfe.printStackTrace();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            doDebug = false;
            try {
                debugFile.write(("Disabling debug for " + ship.name + "\r\n").getBytes());
                debugFile.flush();
                debugFile.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }
    }

    public boolean getDebugStatus() {
        return doDebug;
    }

    public void recvPrivateMessage(String s) {
        receivedMsgs += (s + "\r\n");
        logDebug("recvPrivateMessage()   received message: " + s);
    }

    public void recvPublicMessage(String s) {
        // TODO: add ability to move toward a message if in PATROL mode, with no sub mode, and if message sender is in same quad
        logDebug("recvPublicMessage()   received message: " + s);
    }

    protected void sendMessage() {
        if (msgTimeout <= 0 && receivedMsgs.length() > 1 && shipSubMode == NONE && shipMode == PATROL) {
            String messageReceived = receivedMsgs.substring(1, receivedMsgs.indexOf("\r\n"));
            String messageSender = receivedMsgs.substring(0, 1);
            logDebug("sendMessage()   responding to message: " + messageReceived);

            receivedMsgs = receivedMsgs.substring(receivedMsgs.indexOf("\r\n") + 2);  // trim off the current message from the queue

            TrekShip msgShip = (TrekShip) TrekServer.getShipByScanLetter(messageSender);
            if (msgShip != null) {
                // default message response
                String responseMsg = "I have received your transmission.\r";
                boolean trackMsg = false;

                // parse message, and send more intelligent response
                if (messageReceived.indexOf("die") != -1) {
                    int randomMsgVal = Math.abs(gen.nextInt() % 5);
                    switch (randomMsgVal) {
                        case 0:
                            responseMsg = "Don't you think it'd be swell if you died instead?\r";
                            break;
                        case 1:
                            responseMsg = "  ... ... ... I'll be back ... ... ... \r";
                            break;
                        case 2:
                            responseMsg = "Try not to break your arm patting yourself on the back.\r";
                            break;
                        case 3:
                            responseMsg = "Well, what do you know ... MIRACLES can happen.\r";
                            break;
                        case 4:
                            responseMsg = "Technically I'm a robotic construct, incapable of acheiving 'death'.\r";
                            break;
                    }

                } else if ((messageReceived.indexOf("fuck") != -1) || (messageReceived.indexOf("shit") != -1) || (messageReceived.indexOf("asshole") != -1)) {
                    int randomMsgVal = Math.abs(gen.nextInt() % 6);
                    switch (randomMsgVal) {
                        case 0:
                            responseMsg = "Don't try to sweet talk me...\r";
                            break;
                        case 1:
                            responseMsg = "Sticks and stones will ... blah blah blah\r";
                            break;
                        case 2:
                            responseMsg = "I salute you... with my middle finger.\r";
                            break;
                        case 3:
                            responseMsg = "uh -- I know you are, but what am I?\r";
                            break;
                        case 4:
                            responseMsg = "Your intelligence is shining through!\r";
                            break;
                        case 5:
                            responseMsg = "Your mother would be SO proud!\r";
                            break;
                    }

                } else if (messageReceived.indexOf("lame") != -1 || messageReceived.indexOf("cheat") != -1 || messageReceived.indexOf("stupid") != -1) {
                    int randomMsgVal = Math.abs(gen.nextInt() % 5);
                    switch (randomMsgVal) {
                        case 0:
                            responseMsg = "You sound like an mtrek player ... cheese with that?\r";
                            break;
                        case 1:
                            responseMsg = "gripe gripe gripe whine whine whine\r";
                            break;
                        case 2:
                            responseMsg = " \"You knew what I was when you picked me up.\" \r";
                            break;
                        case 3:
                            responseMsg = "_whatever_ -- nice to blame something else for YOUR problems ...\r";
                            break;
                        case 4:
                            responseMsg = "is that envy in your 'voice'?\r";
                            break;
                    }
                } else if (messageReceived.equalsIgnoreCase("hi") || messageReceived.indexOf("hi ") != -1 || messageReceived.indexOf("hello") != -1 ||
                        messageReceived.indexOf("heya") != -1 || messageReceived.indexOf("greetings") != -1 || messageReceived.indexOf("wave") != -1) {
                    int randomMsgVal = Math.abs(gen.nextInt() % 5);
                    switch (randomMsgVal) {
                        case 0:
                            responseMsg = "hey there\r";
                            break;
                        case 1:
                            responseMsg = "what's up?\r";
                            break;
                        case 2:
                            responseMsg = "*yawn*\r";
                            break;
                        case 3:
                            responseMsg = "what?\r";
                            break;
                        case 4:
                            responseMsg = "hi yourself\r";
                            break;
                    }
                } else if (messageReceived.indexOf("human") != -1 || messageReceived.indexOf("bot") != -1) {
                    int randomMsgVal = Math.abs(gen.nextInt() % 5);
                    switch (randomMsgVal) {
                        case 0:
                            responseMsg = "huh?\r";
                            break;
                        case 1:
                            responseMsg = "what?\r";
                            break;
                        case 2:
                            responseMsg = "oh\r";
                            break;
                        case 3:
                            responseMsg = "uhhh ...\r";
                            break;
                        case 4:
                            responseMsg = "are you?\r";
                            break;
                    }
                } else if (messageReceived.indexOf("ping") != -1 || messageReceived.indexOf("where") != -1) {
                    int randomMsgVal = Math.abs(gen.nextInt() % 3);
                    switch (randomMsgVal) {
                        case 0:
                            responseMsg = "ping\r";
                            break;
                        case 1:
                            responseMsg = "pong!\r";
                            break;
                        case 2:
                            responseMsg = "I'm headed toward " + destinationObj.name + "\r";
                            break;
                    }
                } else if (messageReceived.indexOf("fight") != -1 || messageReceived.indexOf("coward") != -1 ||
                        messageReceived.indexOf("chicken") != -1 || messageReceived.indexOf("puss") != -1) {
                    trackMsg = true;
                }

                // check to see if the target ship is null, is a bot, or jams this ship letter before bothering to send the msg
                TrekShip sendingShip = (TrekShip) TrekServer.getShipByScanLetter(messageSender);
                if (sendingShip != null && !(sendingShip.parent instanceof BotPlayer) &&
                        !(sendingShip.jamShips[TrekUtilities.returnArrayIndex(ship.scanLetter)])) {
                    if (trackMsg) {
                        if (sendingShip.currentQuadrant == ship.currentQuadrant) {
                            responseMsg = "If it's a fight you want, then a fight you shall have, knave!\r";
                            TrekObject nearestEnemyObj = sendingShip.currentQuadrant.getClosestObject(sendingShip);
                            destinationObj = nearestEnemyObj;
                        } else {
                            responseMsg = "I'd blow you away if you were in " + ship.currentQuadrant.name + "\r";
                        }
                    }
                    runMacro("m" + messageSender + responseMsg);
                    msgTimeout = Math.abs(gen.nextInt() % 8) + 3;  // random 3 to 10 second pause before sending another message
                }
            }

            if (receivedMsgs.length() > 1000) receivedMsgs = "";  // if it gets too long, just dump it
        }
    }

    protected void doHeal() {
        int powerUnused = ship.getAvailablePower();
        if (powerUnused >= 5 && (ship.warpEnergy < ship.maxWarpEnergy || ship.impulseEnergy < ship.maxImpulseEnergy || ship.damage > 0)) {
            for (int x = powerUnused; x >= 5; x -= 5) {
                runMacro("c");
            }
        }
    }

    public void weaponReport(TrekObject theWeapon, int weaponType, boolean weaponSuccess) {
        switch (weaponType) {
            case WEAPON_PLASMA:
                if (weaponSuccess) plasmaResult = HIT;
                else plasmaResult = MISS;
                break;
            case WEAPON_MINE:
                if (weaponSuccess) mineResult = HIT;
                else mineResult = MISS;
                break;
            default:
        }

        weaponsFired.removeElement(theWeapon);

    }

    public void addWeaponFire(TrekObject theWeapon, int weaponType) {
        switch (weaponType) {
            case WEAPON_PLASMA:
                plasmaResult = RESET;
                break;
            case WEAPON_MINE:
                mineResult = RESET;
                break;
            default:

        }

        weaponsFired.addElement(theWeapon);
    }

    protected void setIntercept(TrekObject destObj) {
        // set intercept and avoid high speed turn damage
        double preserveSpeed = 0;

        if (Math.abs(ship.warpSpeed) > ship.maxTurnWarp) {
            ship.warpSpeed = 0;
        }

        ship.interceptTarget = destObj;

        if (preserveSpeed != 0) {
            ship.warpSpeed = preserveSpeed;
        }
    }
}