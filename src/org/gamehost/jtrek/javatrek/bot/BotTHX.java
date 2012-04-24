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
package org.gamehost.jtrek.javatrek.bot;


import org.gamehost.jtrek.javatrek.*;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 * The THX bot retrieves gold from Gorn and takes it to a random starbase.
 *
 * @author Aaron P. Chalifoux
 */
public class BotTHX extends TrekBot {


    public static final String scoutLetter = "r";
    public static final String cv97Letter = "k";
    public static final String d10Letter = "v";
    public static final String rbopLetter = "e";
    public static final String warbirdLetter = "n";

    // added excel and dy letters b/c they have xwarp, and pose an extra threat

    public static final String excelLetter = "b";
    public static final String dyLetter = "d";

    Random gen = new Random();
    boolean initialCommand = false;
    boolean shipNearFlag = false;
    boolean dockFlag = false;
    boolean gotGoldFromGornFlag = false;

    String sbNumber;
    String destination = "g";

    /**
     * @param serverIn
     * @param shipNameIn
     */
    public BotTHX(TrekServer serverIn, String shipNameIn) {
        super(serverIn, shipNameIn);
        int thxNumber = 1000 + Math.abs(gen.nextInt() % 9000);
        this.shipName = "THX-" + thxNumber;
        try {
            out = new java.io.FileOutputStream("./data/" + shipName + ".log");
        } catch (java.io.FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }

    /* (non-Javadoc)
      * @see org.gamehost.jtrek.javatrek.TrekBot#loadInitialBot()
      */
    public void loadInitialBot() {
        ship = TrekUtilities.getShip("d", this);
        ship.setInitialDirection();
        ship.currentQuadrant.addShip(ship);

        this.drawHud(true);
    }

    /* (non-Javadoc)
      * @see org.gamehost.jtrek.javatrek.TrekBot#botSecondUpdate()
      */
    public void botSecondUpdate() {

        try {
            //	zap();

            if (!initialCommand) {
                ship.interceptTarget = ship.currentQuadrant.getObjectByScanLetter("g");
                ship.scanTarget = ship.interceptTarget;
                //runMacro("Ogi@4\rixccccccccccccccccccccccccccccCCCCCCC");
                runMacro("]");
                initialCommand = true;
            }
            if (shipNearFlag == true) {
                //	runMacro("mcsneartrue\r");
                return;
            } else if (dockFlag == true) {
                //	runMacro("mcdocktrue\r");
                if (gotGoldFromGornFlag == true)
                    ship.interceptTarget = ship.currentQuadrant.getObjectByScanLetter(sbNumber);
                else
                    ship.interceptTarget = ship.currentQuadrant.getObjectByScanLetter("g");

                ship.scanTarget = ship.interceptTarget;
            }

            if (ship.damage <= 20 && ship.getWarpEnergy() >= 100 && shipNearFlag == false && dockFlag == false) {

                if (gotGoldFromGornFlag == false) {
                    ship.interceptTarget = ship.currentQuadrant.getObjectByScanLetter("g");
                } else {
                    ship.interceptTarget = ship.currentQuadrant.getObjectByScanLetter(sbNumber);
                    //	runMacro("mb" + sbNumber + "\r");
                }
                ship.scanTarget = ship.interceptTarget;
                if (ship.warpSpeed < 11)
                    runMacro("\003");
                runMacro("}i]");
            }


            if (ship == null)
                return;

            if (!ship.raisingShields)
                runMacro("s");

            if (ship.doTranswarp || ship.transwarpEngaged)
                return;

            if (!ship.isIntercepting()) {
                if (shipNearFlag == false && ship.damage <= 20 && ship.getWarpEnergy() >= 100) {
                    runMacro("i");
                } else
                    return;
            }

            if (ship.dockTarget != null) {
                ship.gold = 0;
                ship.dockTarget.gold += 20000;
                runMacro("Qy");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* (non-Javadoc)
      * @see org.gamehost.jtrek.javatrek.TrekBot#botTickUpdate()
      */
    public void botTickUpdate() {

        zap();

        if (ship.doTranswarp || ship.transwarpEngaged) {
            if (ship.transwarpEngaged)
                runMacro("]");
            return;
        } else {
            scanForShips();
        }

        checkDamage();

        if (ship.damage >= 20 || ship.getWarpEnergy() < 100 || shipNearFlag)
            return;


        double curDistanceToDestination = TrekMath.getDistance(ship, ship.scanTarget);

        if (curDistanceToDestination > 100) {
            if (ship.warpSpeed < 11)
                runMacro("\003\005");

            runMacro("}ii]@11\r");
        }
        if (curDistanceToDestination < 50 && ship.warpSpeed > 4.9) {
            dockFlag = true;
            runMacro("\005@5\r");
        }

        if (curDistanceToDestination < 10 && ship.warpSpeed > 1.9)
            runMacro("\005@2\r");

        if (TrekMath.getDistance(ship, ship.scanTarget) < 2) {
            runMacro("\005@0\r...");
        }

        if (ship.orbitTarget != null) {
            if (ship.orbitTarget.name.equals("Gorn")) {
                dockFlag = false;
                gotGoldFromGornFlag = true;
                ship.gold = 350;
                sbNumber = new Integer(Math.abs(gen.nextInt() % 10)).toString();
                //runMacro( "O" + sbNumber + "ii\005@4\rxcccccccccccccccCCCCCCC");
                ship.interceptTarget = ship.currentQuadrant.getObjectByScanLetter(sbNumber);
                ship.scanTarget = ship.interceptTarget;
                runMacro("O" + sbNumber + "ii]");
            }
        }

        return;
    }

    public void zap() {
        TrekObject intHolder;
        intHolder = ship.scanTarget;
        TrekObject nearestObj = null;
        double nearest = 500;

        Vector objs = ship.currentQuadrant.getAllObjectsInRange(ship, ship.minPhaserRange);
        for (Enumeration e = objs.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();
            if (TrekUtilities.isObjectBuoy(curObj) ||
                    TrekUtilities.isObjectDrone(curObj) ||
                    TrekUtilities.isObjectMine(curObj) ||
                    TrekUtilities.isObjectMagnabuoy(curObj)) {

                double curObjDist = TrekMath.getDistance(ship, curObj);

                // if this object is the closest one encountered so far
                if (curObjDist < nearest) {
                    nearest = curObjDist;

                    // of these objects, thx will likely only fire drones, so double check it's an enemy drone
                    if (TrekUtilities.isObjectDrone(curObj)) {
                        TrekDrone drone = (TrekDrone) curObj;
                        if (!(drone.owner == ship)) {
                            nearestObj = drone;
                        }
                    } else {
                        nearestObj = curObj;
                    }
                }
            }
        }

        // shoot the nearest object
        if (nearestObj != null) {
            ship.phaserType = TrekShip.PHASER_NORMAL;
            ship.phaserFireType = TrekShip.PHASER_FIRENARROW;
            runMacro("CO" + nearestObj.scanLetter + "\014Pp\020");
            // instead of using scanner to lock on object, and losing the planet/base we're interecepting, could:
            /*

               ship.lockTarget = nearestObj;
               if (ship.lockTarget != null) {
                   runMacro("\005Pp\020");
               }

               */
        }

        ship.scanTarget = intHolder;
    }

    public void scanForShips() {
        int numShips = 0;
        shipNearFlag = false;

        Vector ships = ship.currentQuadrant.getAllVisibleShipsInRange(ship, ship.maxTorpedoRange);
        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip curShip = (TrekShip) e.nextElement();
            double curShipDist = TrekMath.getDistance(ship, curShip);
            // if this ship is the closest one encountered so far

            if (curShipDist <= 4000) {
                numShips++;

                if (numShips >= 2)
                    runMacro("\003@3\rHHHHHHHHHHHHHx");
            }

            if ((curShipDist <= (curShip.maxTorpedoRange) + 30) && curShipDist > 1000) {
                shipNearFlag = true;
                dockFlag = false;
                bobAndWeave(curShip, curShipDist);
                moveWithTarget(curShip, curShipDist);
            }
            if (curShipDist <= 1000) {
                shipNearFlag = true;
                dockFlag = false;
                shootEnemies();
                moveWithTarget(curShip, curShipDist);
            }
            if (curShipDist <= 325) {
                shipNearFlag = true;
                dockFlag = false;
                tPhaserEnemy(curShip);
                moveWithTarget(curShip, curShipDist);
            }
        }
    }

    public void tPhaserEnemy(TrekShip curShip) {

        ship.phaserType = TrekShip.PHASER_TELEPORTER;
        ship.phaserFireType = TrekShip.PHASER_FIRENARROW;
        ship.lockTarget = curShip;
        if (ship.lockTarget != null) {
            runMacro("\003\027PPPPp");
        }
    }

    public void moveWithTarget(TrekShip curShip, double curShipDist) {

        ship.interceptTarget = curShip;
        ship.scanTarget = curShip;

        if (curShip.warpSpeed >= 8 && curShip.warpSpeed <= 11) {
            runMacro("\003\005{i@" + curShip.warpSpeed + "\r");
        } else if (curShip.warpSpeed <= -8 && curShip.warpSpeed >= -11) {
            runMacro("\003\005}i]@" + curShip.warpSpeed + "\r");
            //		runMacro("b");
        } else if (curShip.warpSpeed > 11) {
            runMacro("\003\005{i[[");
        } else if (curShip.warpSpeed < -11) {
            runMacro("\003\005}i]");
        } else if (curShipDist <= 499) {
            runMacro("\003\005{i[");
        } else {
            runMacro("\003\005}i]");
        }
    }

    public void bobAndWeave(TrekShip curShip, double curDistance) {


        ship.interceptTarget = curShip;
        ship.scanTarget = curShip;
        if ((curDistance <= (curShip.maxTorpedoRange) + 30) && curShip.warpSpeed >= 0) {
            runMacro("\003}i]");
        } else if ((curDistance <= (curShip.maxTorpedoRange) + 30
        ) && curShip.warpSpeed < 0) {
            runMacro("\003{i[");
        }

        if (curShip.warpSpeed == 0)
            runMacro("\003}i]");

    }

    public void shootEnemies() {
        TrekShip phaserTarget = null;
        TrekShip torpTarget = null;
        double nearest = 500;
        int mostHurt = -1;

        Vector ships = ship.currentQuadrant.getAllVisibleShipsInRange(ship, ship.maxTorpedoRange);
        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip curShip = (TrekShip) e.nextElement();
            double curShipDist = TrekMath.getDistance(ship, curShip);
            int curShipDmg = curShip.damage;
            // if this ship is the closest one encountered so far
            if (curShipDist < nearest) {
                nearest = curShipDist;
                phaserTarget = curShip;
            }

            // torp the ship that's hurt most
            if (curShipDmg > mostHurt) {
                torpTarget = curShip;
            }
        }

        // shoot the nearest object
        if (nearest < ship.minPhaserRange && phaserTarget != null) {
            ship.phaserType = TrekShip.PHASER_NORMAL;
            ship.phaserFireType = TrekShip.PHASER_FIRENARROW;

            ship.lockTarget = phaserTarget;
            if (ship.lockTarget != null) {
                runMacro("\003PPPPp\020\020\020\020");
            }
        }

        // shoot the most hurt
        if (torpTarget != null) {
            ship.lockTarget = torpTarget;
            runMacro("\003\027CCTt\024");
        }
    }

    public void checkDamage() {
        if (ship.doTranswarp)
            return;

        // apply damage control if necessary
        if (ship.damage > 0 || ship.getWarpEnergy() < ship.maxWarpEnergy || ship.impulseEnergy < ship.maxImpulseEnergy) {
            runMacro("ccccccccccccccccccccc");
        } else
            runMacro("\003");

        TrekShip curShip = null;
        TrekShip torpTarget2 = null;
        double nearest2 = 2300;
        int maxDamageToTake = 70;
        int weToWarp = 50;

        Vector ships2 = ship.currentQuadrant.getAllVisibleShipsInRange(ship, ship.maxTorpedoRange);
        for (Enumeration e = ships2.elements(); e.hasMoreElements(); ) {
            curShip = (TrekShip) e.nextElement();
            double curShipDist = TrekMath.getDistance(ship, curShip);
            // if this ship is the closest one encountered so far
            if (curShipDist < nearest2) {
                nearest2 = curShipDist;
                torpTarget2 = curShip;


//				 if (curShip.classLetter.equals(scoutLetter) || curShip.classLetter.equals(cv97Letter) || curShip.classLetter.equals(d10Letter)){
//		                        maxDamageToTake = 40;
                //              		        weToWarp = 80;
                //            		}
                //          		else if (curShip.classLetter.equals(rbopLetter) || curShip.classLetter.equals(warbirdLetter)){
                //                		maxDamageToTake = 50;
                //              		weToWarp = 70;
                //    		}
            }

        }

        if (curShip != null) {

            if (curShip.classLetter.equals(scoutLetter) || curShip.classLetter.equals(cv97Letter) || curShip.classLetter.equals(d10Letter)) {
                maxDamageToTake = 40;
                weToWarp = 80;
            } else if (curShip.classLetter.equals(rbopLetter) || curShip.classLetter.equals(warbirdLetter)) {
                maxDamageToTake = 50;
                weToWarp = 70;
            }
//		runMacro("mcnotnull" + maxDamageToTake + "\r");
        }

        if ((ship.damage >= maxDamageToTake || ship.getWarpEnergy() <= weToWarp) && torpTarget2 != null) {
            shouldwarp();
        } else if ((shipNearFlag != true) && (ship.damage >= 20 || ship.getWarpEnergy() < ((ship.maxWarpEnergy) - 20))) {
            runMacro("\027cccccccccccccccccccccccccccccccccccccc");
        }
    }

    public void shouldwarp() {
        if (ship.damage >= 60 && ship.warpEnergy >= 30) {
            runMacro("\003@3\rHHHHHHHHHHHHHx");
            //	sbNumber = new Integer( Math.abs( gen.nextInt() % 10 ) ).toString();
        }
    }
}

