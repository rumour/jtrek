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

import java.util.Random;

/**
 * TrekUtilities is a holder class for static uncategorized methods.
 *
 * @author Joe Hopkinson
 */
public final class TrekUtilities {

    public TrekUtilities() {
    }

    public static TrekShip getShip(String chosenShip, TrekPlayer player) {
        TrekLog.logMessage("Getting ship: " + chosenShip);
        TrekShip newShip = null;
        String shipLetter = "";
        if (player instanceof TrekBot && player.shipName.equals("borg")) {
            shipLetter = TrekServer.getBotScanLetter();
        } else {
            shipLetter = TrekServer.getScanLetter();
        }

        if (chosenShip.equals("a") || chosenShip.equals("II-A")) {
            newShip = new ShipConstitutionIIA(player, shipLetter);
        } else if (chosenShip.equals("b") || chosenShip.equals("EXCEL")) {
            newShip = new ShipExcelsior(player, shipLetter);
        } else if (chosenShip.equals("c") || chosenShip.equals("LARSON")) {
            newShip = new ShipLarson(player, shipLetter);
        } else if (chosenShip.equals("d") || chosenShip.equals("DY-600")) {
            newShip = new ShipDY600(player, shipLetter);
        } else if (chosenShip.equals("e") || chosenShip.equals("RBOP")) {
            newShip = new ShipRBOP(player, shipLetter);
        } else if (chosenShip.equals("n") || chosenShip.equals("WARBIRD")) {
            newShip = new ShipWarbird(player, shipLetter);
        } else if (chosenShip.equals("f") || chosenShip.equals("KEV-12")) {
            newShip = new ShipKEV12(player, shipLetter);
        } else if (chosenShip.equals("g") || chosenShip.equals("KPB-13")) {
            newShip = new ShipKPB13(player, shipLetter);
        } else if (chosenShip.equals("h") || chosenShip.equals("BR-5")) {
            newShip = new ShipBR5(player, shipLetter);
        } else if (chosenShip.equals("i") || chosenShip.equals("BR-1000")) {
            newShip = new ShipBR1000(player, shipLetter);
        } else if (chosenShip.equals("j") || chosenShip.equals("CL-13")) {
            newShip = new ShipCL13(player, shipLetter);
        } else if (chosenShip.equals("k") || chosenShip.equals("CV-97")) {
            newShip = new ShipCV97(player, shipLetter);
        } else if (chosenShip.equals("l") || chosenShip.equals("CDA-180")) {
            newShip = new ShipCDA180(player, shipLetter);
        } else if (chosenShip.equals("m") || chosenShip.equals("CDA-120")) {
            newShip = new ShipCDA120(player, shipLetter);
        } else if (chosenShip.equals("o") || chosenShip.equals("KBOP")) {
            newShip = new ShipKBOP(player, shipLetter);
        } else if (chosenShip.equals("p") || chosenShip.equals("GALOR")) {
            newShip = new ShipGalor(player, shipLetter);
        } else if (chosenShip.equals("q") || chosenShip.equals("BR-2000")) {
            newShip = new ShipBR2000(player, shipLetter);
        } else if (chosenShip.equals("r") || chosenShip.equals("SCOUT")) {
            newShip = new ShipScout(player, shipLetter);
        } else if (chosenShip.equals("s") || chosenShip.equals("INTERCEPTOR")) {
            newShip = new ShipInterceptor(player, shipLetter);
        } else if (chosenShip.equals("t") || chosenShip.equals("DEFIANT")) {
            newShip = new ShipDefiant(player, shipLetter);
        } else if (chosenShip.equals("u") || chosenShip.equals("FERENGI")) {
            newShip = new ShipFerengi(player, shipLetter);
        } else if (chosenShip.equals("v") || chosenShip.equals("D-10")) {
            newShip = new ShipD10(player, shipLetter);
        } else if (chosenShip.equals("x") || chosenShip.equals("XP-1")) {
            newShip = new ShipEscapePod(player, shipLetter);
        } else if (chosenShip.equals("y") || chosenShip.equals("AVG IX")) {
            newShip = new ShipNorm(player, shipLetter);
        } else if (chosenShip.equals("z") || chosenShip.equals("VALDORE")) {
            newShip = new ShipValdore(player, shipLetter);
        } else if (chosenShip.equals("A") || chosenShip.equals("VORCHA")) {
            newShip = new ShipVorcha(player, shipLetter);
        } else if (chosenShip.equals("Q")) {
            newShip = new ShipQ(player, shipLetter);
        }

        newShip.maxImpulseEnergy = newShip.impulseEnergy;
        newShip.maxWarpEnergy = newShip.warpEnergy;
        newShip.cloakBurnt = false;
        newShip.cloakTimeCurrent = newShip.cloakTime;
        newShip.mineCount = newShip.maxMineStorage;
        newShip.droneCount = newShip.maxDroneStorage;
        newShip.torpedoCount = newShip.maxTorpedoStorage;
        newShip.currentCrystalCount = 0;

        if (chosenShip.equals("l") || chosenShip.equals("m") || chosenShip.equals("p") || chosenShip.equals("u")) {
            newShip.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get("Gamma Quadrant");
        } else {
            newShip.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get("Alpha Quadrant");
        }

        return newShip;
    }

    public static boolean isObjectShip(TrekObject obj) {
        if (obj == null) {
            return false;
        }
        if (obj.type == TrekObject.OBJ_SHIP)
            return true;
        else
            return false;
    }

    public static boolean isObjectPulsar(TrekObject obj) {
        if (obj == null) {
            return false;
        }
        if (obj.type == TrekObject.OBJ_PULSAR)
            return true;
        else
            return false;
    }

    public static boolean isObjectPlanet(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_PLANET)
            return true;
        else
            return false;
    }

    public static boolean isObjectStarbase(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_STARBASE)
            return true;
        else
            return false;
    }

    public static boolean isObjectDrone(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_DRONE)
            return true;
        else
            return false;
    }

    public static boolean isObjectMine(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_MINE)
            return true;
        else
            return false;
    }

    public static boolean isObjectObserverDevice(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_OBSERVERDEVICE)
            return true;
        else
            return false;
    }

    public static boolean isObjectTorpedo(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_TORPEDO)
            return true;
        else
            return false;
    }

    public static boolean isObjectBuoy(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_BUOY)
            return true;
        else
            return false;
    }

    public static boolean isObjectGold(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_GOLD)
            return true;
        else
            return false;
    }

    public static boolean isObjectWormhole(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_WORMHOLE)
            return true;
        else
            return false;
    }

    public static boolean isObjectCorbomite(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_CORBOMITE)
            return true;
        else
            return false;
    }

    public static boolean isObjectIridium(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_IRIDIUM)
            return true;
        else
            return false;
    }

    public static boolean isObjectMagnabuoy(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_MAGNABUOY)
            return true;
        else
            return false;
    }

    public static boolean isObjectNeutron(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_NEUTRON)
            return true;
        else
            return false;
    }

    public static boolean isObjectShipDebris(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_SHIPDEBRIS)
            return true;
        else
            return false;
    }

    public static boolean isObjectStar(TrekObject obj) {
        if (obj == null) {
            return false;
        }

        if (obj.type == TrekObject.OBJ_STAR)
            return true;
        else
            return false;
    }

    public static boolean isObjectComet(TrekObject obj) {
        if (obj == null)
            return false;

        if (obj.type == TrekObject.OBJ_COMET)
            return true;
        else
            return false;
    }

    public static boolean isObjectBlackhole(TrekObject obj) {
        if (obj == null)
            return false;

        if (obj.type == TrekObject.OBJ_BLACKHOLE)
            return true;
        else
            return false;
    }

    public static boolean isObjectZone(TrekObject obj) {
        if (obj == null)
            return false;

        if ((obj.type == TrekObject.OBJ_ASTEROIDBELT) ||
                (obj.type == TrekObject.OBJ_NEBULA) ||
                (obj.type == TrekObject.OBJ_ORGANIA) ||
                (obj.type == TrekObject.OBJ_PULSAR) ||
                (obj.type == TrekObject.OBJ_QUASAR) ||
                (obj.type == TrekObject.OBJ_ZONE))
            return true;
        else
            return false;
    }

    public static boolean isGalacticObject(TrekObject obj) {
        if (obj == null) return false;

        if (isObjectZone(obj)) return true;
        if (isObjectPlanet(obj)) return true;
        if (isObjectStarbase(obj)) return true;
        if (isObjectWormhole(obj)) return true;
        if (isObjectStar(obj)) return true;
        if (isObjectComet(obj)) return true;
        if (isObjectBlackhole(obj)) return true;
        if (isObjectObserverDevice(obj)) return true;

        return false;
    }

    public static boolean isObjectFlag(TrekObject obj) {
        if (obj.currentQuadrant.getFlag() != null &&
                obj.currentQuadrant.getFlag().getFlagObject() == obj) return true;
        return false;
    }

    public static String getTorpedoTypeString(int type) {
        switch (type) {
            case TrekShip.TORPEDO_BOLTPLASMA:
                return "bolt plasma";
            case TrekShip.TORPEDO_NORMAL:
                return "torpedo";
            case TrekShip.TORPEDO_OBLITERATOR:
                return "obliterator";
            case TrekShip.TORPEDO_PHOTON:
                return "photon";
            case TrekShip.TORPEDO_PLASMA:
                return "plasma";
            case TrekShip.TORPEDO_VARIABLESPEEDPLASMA:
                return "plasma";
            case TrekShip.TORPEDO_WARHEAD:
                return "warhead";
            case TrekShip.TORPEDO_ONEHITWARHEAD:
                return "warhead";
            default:
                return "a penis";
        }
    }

    public static String getPhaserTypeString(int type) {
        switch (type) {
            case TrekShip.PHASER_AGONIZER:
                return "Agonizer";
            case TrekShip.PHASER_EXPANDINGSPHEREINDUCER:
                return "Expanding Sphere Inducer";
            case TrekShip.PHASER_NORMAL:
                return "Normal";
            case TrekShip.PHASER_TELEPORTER:
                return "Teleporter";
            case TrekShip.PHASER_DISRUPTOR:
                return "Disruptor";
            default:
                return "a vagina";
        }
    }

    public static Trek3DPoint parsePoint(String s) {
        // passed string in format: (x,y,z)
        float x, y, z;
        x = new Float(s.substring(1, s.indexOf(','))).floatValue();
        s = s.substring(s.indexOf(',') + 1, s.length());
        y = new Float(s.substring(0, s.indexOf(','))).floatValue();
        z = new Float(s.substring(s.indexOf(',') + 1, s.length() - 1)).floatValue();

        return new Trek3DPoint(x, y, z);
    }

    public static boolean isValidShipChar(int i) {
        if (i < 65)
            return false;
        if (i >= 65 && i <= 90)
            return true;
        if (i >= 91 && i <= 96)
            return false;
        if (i >= 97 && i <= 122)
            return true; //lower-case letters
        if (i > 122)
            return false;

        return false;
    }

    public static boolean isValidObjChar(int i) {
        if (i >= 48 && i <= 57)
            return true; // numbers
        if (i >= 65 && i <= 90)
            return true; // upper-case letters
        if (i >= 97 && i <= 122)
            return true; // lower-case letters

        return false;
    }

    public static String format(String start, String end, int chars) {
        String buffer = "";

        for (int x = 0; x < chars - start.length() - end.length(); x++) {
            buffer += " ";
        }

        return start + buffer + end;
    }

    public static int returnArrayIndex(int i) {
        // passed a 'command' int representing char 1 to 0, A to Z, or a to z want to
        // to return as an index of an array (for tracking which ship slots to jam messages from)

        if (i >= 48 && i <= 57) {
            return (i - 48);
        } else if (i >= 65 && i <= 90) {
            return (i - 65 + 10);
        } else if (i >= 97 && i <= 122) {
            return (i - 97 + 10 + 26);
        }

        return 0;
    }

    public static int returnArrayIndex(String shipLetter) {
        // return same index position as previous method when passed a ship letter
        char c;
        c = shipLetter.charAt(0);
        return returnArrayIndex(c);
    }

    public static String convertArrayIndexToShipLetter(int i) {
        // in the array, 123...890 takes up the first 10, ABC...XYZ, the next 26, and abc...xyz the final 26
        if (i >= 0 && i <= 9) {
            i += 48;
        } else if (i >= 10 && i <= 35) {
            i += 65;
        } else if (i >= 36 && i <= 51) {
            i += 97;
        }
        return (new Character((char) i).toString());
    }

    public static String garbledOutboundMsg(String thisMsg) {
        // randomly (1 in 6 chance) replace characters in an outbound message, when sender is influenced by message distorting zone
        Random msgGen = new Random();
        StringBuffer sb = new StringBuffer();

        for (int x = 0; x < thisMsg.length(); x++) {
            if ((Math.abs(msgGen.nextInt() % 6) == 0) && (x > 4)) {
                sb.append("@");
            } else {
                sb.append(thisMsg.charAt(x));
            }
        }

        return sb.toString();
    }

    public static String garbledInboundMsg(String thisMsg) {
        // randomly (1 in 6 chance) replace characters in an inboung message when receiving ship is affected by distortion zone
        Random msgGen = new Random();
        StringBuffer sb = new StringBuffer();

        for (int x = 0; x < thisMsg.length(); x++) {
            if ((Math.abs(msgGen.nextInt() % 6) == 0) && (x > 4)) {
                sb.append("#");
            } else {
                sb.append(thisMsg.charAt(x));
            }
        }

        return sb.toString();
    }

    public static boolean shipVisibleAndActive(TrekObject targetObj, TrekQuadrant curQuad) {
        if (TrekUtilities.isObjectShip(targetObj)) {  // check for visibility if target is a ship
            TrekShip targetShip = (TrekShip) targetObj;

            if (targetShip.cloaked) {
                if ((targetShip.quasarTarget == null) &&
                        ((targetShip.pulsarTarget == null) || (!targetShip.pulsarTarget.active)))
                    return false;
            }

            TrekShip doesShipExist = (TrekShip) (curQuad.getShipByScanLetter(targetShip.scanLetter));

            if ((doesShipExist == null) || (!doesShipExist.name.equals(targetShip.name)) ||
                    (doesShipExist.parent.state != TrekPlayer.WAIT_PLAYING)) {
                return false;
            }
        }

        return true;
    }
}