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
import java.util.Hashtable;
import java.util.Vector;

/**
 * Represents a single quadrant in the game.
 *
 * @author Joe Hopkinson
 */
public class TrekQuadrant {
    private Hashtable ships;
    private Hashtable objects;
    public String name;
    public static TrekServer server;
    private TrekFlag flag = null;

    public TrekQuadrant(TrekServer serverIn) {
        objects = new Hashtable();
        ships = new Hashtable();
        server = serverIn;
    }

    public synchronized boolean isInQuadrant(TrekObject thisObj) {
        if (thisObj == null) {
            return false;
        }

        TrekLog.logDebug("Server: Checking to see if " + thisObj.name + " is in quadrant.");

        if (TrekUtilities.isObjectShip(thisObj)) {
            TrekShip thisShip = (TrekShip) thisObj;

            if (thisShip.isPlaying()) {
                return ships.containsValue(thisObj);
            }
        } else {
            return objects.containsValue(thisObj);
        }

        return false;
    }

    public synchronized void addObject(TrekObject thisObj) {
        //TrekLog.logMessage(this.name + ": Adding object " + thisObj.name + " to quadrant.  Total Objects: " + objects.size());

        if (thisObj == null) {
            return;
        }

        if (thisObj.scanLetter.equals("")) {
            thisObj.scanLetter = this.getObjectLetter();
        }

        thisObj.currentQuadrant = this;

        objects.put(thisObj.scanLetter, thisObj);
    }

    public synchronized void addShip(TrekObject thisObj) {
        TrekLog.logMessage(this.name + ": Adding ship " + thisObj.name + " to quadrant.  Total Ships: " + ships.size());
        ships.put(thisObj.scanLetter, thisObj);
        thisObj.currentQuadrant = this;
    }

    protected synchronized void removeObjectByScanLetter(String thisLetter) {
        try {
            //TrekLog.logMessage(this.name + ": Removing object by scan letter " + thisLetter + ".  Total Objects: " + objects.size());

            if (objects.containsKey(thisLetter)) {
                objects.remove(thisLetter);
            }
        } catch (Exception e) {
            TrekLog.logError("Could not remove object by scanletter.");
        }
    }

    public synchronized void removeShipByScanLetter(String thisLetter) {
        TrekLog.logMessage(this.name + ": Removing ship by scan letter " + thisLetter + ".  Total Ships: " + ships.size());

        if (ships.containsKey(thisLetter)) {
            ships.remove(thisLetter);
        }
    }

    public synchronized TrekObject getObjectByScanLetter(String thisLetter) {
        TrekLog.logDebug("Server: Getting object by scan letter " + thisLetter + ".");

        if (objects.containsKey(thisLetter)) {
            return (TrekObject) objects.get(thisLetter);
        }

        return null;
    }

    public synchronized TrekObject getShipByScanLetter(String thisLetter) {
        TrekLog.logDebug("Server: Getting ship by scan letter " + thisLetter + ".");

        if (ships.containsKey(thisLetter)) {
            return (TrekObject) ships.get(thisLetter);
        }

        return null;
    }

    protected synchronized void updatePointLocations() {
        try {

            if (TrekServer.isTeamPlayEnabled() && flag != null) {
                flag.doTick();
            }

            for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
                TrekObject activeObject = (TrekObject) e.nextElement();

                // If the magnitude of the vector is 0, then skip the object.
                if (activeObject.vector.magnitude() != 0) {

                    if (TrekUtilities.isObjectShip(activeObject)) {
                        TrekShip testShip = (TrekShip) activeObject;

                        if (testShip.isPlaying()) {
                            if (testShip.warpSpeed != 0) {
                                Trek3DVector dir = testShip.getNewDirectionVector();
                                activeObject.point.add(dir);
                                // activeObject.point.add(activeObject.vector);
                            }

                            try {
                                // Check for orbitability..
                                TrekPlanet planet = getClosestPlanet(testShip);

                                if (planet != null) {
                                    int distanceFromPlanet = new Double(TrekMath.getDistance(testShip, planet)).intValue();

                                    if (distanceFromPlanet <= 1 && testShip.warpSpeed == 0) {
                                        testShip.orbitable = true;
                                        testShip.orbitTarget = planet;
                                    } else {
                                        testShip.orbitable = false;
                                        testShip.orbitDuration = 0;
                                        testShip.orbiting = false;
                                        testShip.orbitTarget = null;
                                        testShip.orbitDmgCounter = 0;
                                    }
                                }

                                TrekStarbase base = getClosestStarbase(testShip);

                                if (base != null) {
                                    int distanceFromBase = new Double(TrekMath.getDistance(testShip, base)).intValue();

                                    if (distanceFromBase <= 1 && testShip.warpSpeed == 0) {
                                        testShip.dockable = true;
                                        testShip.dockTarget = base;
                                    } else {
                                        //					  TrekLog.logMessage( base.name );
                                        testShip.dockable = false;
                                        testShip.docked = false;
                                        testShip.dockTarget = null;
                                        testShip.dockDuration = 0;
                                        testShip.orbitDmgCounter = 0;
                                    }
                                }
                            } catch (Exception ec) {
                                TrekLog.logException(ec);
                            }
                        }
                    }
                }
            }

            // Update locations for objects.
            Trek3DVector dir;

            for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
                TrekObject activeObject = (TrekObject) e.nextElement();

                if (!TrekUtilities.isObjectPlanet(activeObject) && !TrekUtilities.isObjectStarbase(activeObject)) {
                    // If the magnitude of the vector is 0, then skip the object.
                    if (activeObject.vector.magnitude() != 0 || TrekUtilities.isObjectComet(activeObject) ||
                            (TrekUtilities.isObjectMine(activeObject) && activeObject.name.equals("lithium mine"))) {
                        if (TrekUtilities.isObjectDrone(activeObject)) {
                            TrekDrone activeDrone = (TrekDrone) activeObject;
                            if (TrekUtilities.isObjectShip(activeDrone.interceptTarget)) {
                                if (TrekUtilities.shipVisibleAndActive(activeDrone.interceptTarget, this) || activeDrone.name.equals("seeker probe")) {
                                    // if it's a seeker drone, we need to make sure the ship is still active before trying to get the new vector
                                    if (activeDrone.name.equals("seeker probe")) {
                                        TrekShip doesShipExist = (TrekShip) (getShipByScanLetter(activeDrone.interceptTarget.scanLetter));

                                        if ((doesShipExist == null) || (!doesShipExist.name.equals(activeDrone.interceptTarget.name)) ||
                                                (doesShipExist.parent.state != TrekPlayer.WAIT_PLAYING)) {
                                            // do nothing
                                        } else {
                                            // otherwise update vector on cloaked ship
                                            dir = activeObject.getNewDirectionVector();
                                            activeObject.point.add(dir);
                                        }
                                    } else {
                                        dir = activeObject.getNewDirectionVector();
                                        activeObject.point.add(dir);
                                        //activeObject.point.add(activeObject.vector);
                                    }
                                }
                            }
                        }

                        if (TrekUtilities.isObjectTorpedo(activeObject)) {
                            TrekTorpedo torpy = (TrekTorpedo) activeObject;
                            if (!torpy.intercepting ||
                                    (TrekUtilities.shipVisibleAndActive(torpy.interceptTarget, this))) {
                                dir = activeObject.getNewDirectionVector();
                                activeObject.point.add(dir);
                                //activeObject.point.add(activeObject.vector);
                            }
                        }

                        if (TrekUtilities.isObjectObserverDevice(activeObject)) {
                            TrekObserverDevice tod = (TrekObserverDevice) activeObject;
                            if (tod.tower == null) {
                                if (tod.warpSpeed != 0.0) {
                                    dir = activeObject.getNewDirectionVector();
                                    activeObject.point.add(dir);
                                    activeObject.point.add(activeObject.vector);
                                }
                            } else {
                                tod.vector = new Trek3DVector(tod.tower.vector);
                                tod.point = new Trek3DPoint(tod.tower.point);
                                tod.point.subtract(tod.delta);
                            }
                        }

                        if (TrekUtilities.isObjectComet(activeObject)) {
                            ((TrekComet) activeObject).updateOrbitPoint();
                        }

                        if (TrekUtilities.isObjectMine(activeObject)) {
                            TrekLithium lithium = (TrekLithium) activeObject;
                            if (lithium.tower == null) {
                                if (lithium.warpSpeed != 0.0) {
                                    dir = activeObject.getNewDirectionVector();
                                    activeObject.point.add(dir);
                                    activeObject.point.add(activeObject.vector);
                                }
                            } else {
                                lithium.vector = new Trek3DVector(lithium.tower.vector);
                                lithium.point = new Trek3DPoint(lithium.tower.point);
                                lithium.point.subtract(lithium.delta);
                            }
                        }

                    }
                }

                activeObject.doTick();

            }

        } catch (Exception e) {
            TrekLog.logException(e);
        }

    }

    public synchronized Vector getVisibleObjects(TrekObject thisObject) {
        TrekLog.logDebug("Server: Getting visible objects to " + thisObject.name);

        Vector returnVector = new Vector();
        returnVector.setSize(13);
        int x = 0;
        double scanRange = 0;

        if (TrekUtilities.isObjectShip(thisObject)) {
            TrekShip shipObj = (TrekShip) thisObject;
            int objectRange = shipObj.parent.playerOptions.getOption(TrekPlayerOptions.OPTION_OBJECTRANGE);

            if (objectRange == 0) {
                scanRange = thisObject.scanRange;
            } else {
                scanRange = objectRange;
            }

            if (shipObj.nebulaTarget != null) {
                scanRange = 500;
            }
        } else {
            scanRange = thisObject.scanRange;
        }

        double objInZoneRange = 2000;

        for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            TrekObject activeObject = (TrekObject) e.nextElement();

            if (!thisObject.equals(activeObject)) {

                if (TrekMath.getDistance(thisObject, activeObject) < scanRange) {
                    // if an object is in a zone (nebula), potentially further reduce visible range
                    // primarily to keep ships outside the zone from seeing all objects within
                    if (activeObject.zoneEffect != null) {
                        if (TrekMath.getDistance(thisObject, activeObject) < objInZoneRange) {
                            returnVector.insertElementAt(activeObject, x);
                            x++;
                        }
                    } else {
                        returnVector.insertElementAt(activeObject, x);
                        x++;
                    }
                }
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    // basically same functionality as getVisibleObjects, however it gets anything within normal scan range (unless in nebula)
// rather than using the ship options setting
    public synchronized Vector getScannableObjects(TrekObject thisObject) {
        TrekLog.logDebug("Server: Getting objects within " + thisObject.name + "'s scan range.");

        Vector returnVector = new Vector();
        returnVector.setSize(13);
        int x = 0;
        double scanRange = 0;

        if (TrekUtilities.isObjectShip(thisObject)) {
            TrekShip shipObj = (TrekShip) thisObject;

            if (shipObj.nebulaTarget != null) {
                scanRange = 500;
            } else {
                scanRange = thisObject.scanRange;
            }
        } else {
            scanRange = thisObject.scanRange;
        }

        for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            TrekObject activeObject = (TrekObject) e.nextElement();

            if (!thisObject.equals(activeObject)) {

                if (TrekMath.getDistance(thisObject, activeObject) < scanRange) {

                    returnVector.insertElementAt(activeObject, x);
                    x++;
                }
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    public synchronized Vector getVisibleShips(TrekObject thisObject) {
        TrekLog.logDebug("Server: Getting visible ships to " + thisObject.name);

        Vector returnVector = new Vector();
        double scanRange = 0;
        int targetVisibility = 0;

        boolean cloaked = false;

        scanRange = thisObject.scanRange;

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekObject activeObject = (TrekObject) e.nextElement();

            cloaked = false;
            double actualScanRange = 0;

            if (!thisObject.equals(activeObject)) {
                if (TrekUtilities.isObjectShip(activeObject)) {
                    TrekShip targetShip = (TrekShip) activeObject;

                    if (targetShip.isPlaying()) {
                        cloaked = targetShip.cloaked;

                        if (targetShip.nebulaTarget != null) {
                            targetVisibility = targetShip.visibility / 3;
                        } else {
                            targetVisibility = targetShip.visibility;
                        }

                        actualScanRange = TrekMath.getScanningRange(scanRange, (targetShip.getWarpEnergy() + targetShip.impulseEnergy) - targetShip.getAvailablePower(), targetVisibility);

                        if (TrekUtilities.isObjectShip(thisObject)) {
                            TrekShip sourceShip = (TrekShip) thisObject;

                            if (sourceShip.nebulaTarget != null)
                                actualScanRange = actualScanRange / 3;
                        }

                        if (TrekMath.getDistance(thisObject, targetShip) < actualScanRange) {
                            if ((targetShip.quasarTarget != null && cloaked) ||
                                    (targetShip.pulsarTarget != null && cloaked && targetShip.pulsarTarget.active) ||
                                    (!cloaked)) {
                                returnVector.addElement(activeObject);
                            }
                        }
                    }
                }
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    protected synchronized String getObjectLetter() {
        TrekLog.logDebug("Server: Getting object scan letter.");

        String finalLetter = "";

        // For new objects... first lowercase.
        for (int x = 97; x < 123; x++) {
            if (!objects.containsKey(Character.toString((char) x))) {
                finalLetter = Character.toString((char) x);
                break;
            }
        }

        if (finalLetter.equals("")) {
            for (int x = 65; x < 91; x++) {
                if (!objects.containsKey(Character.toString((char) x))) {
                    finalLetter = Character.toString((char) x);
                    break;
                }
            }
        }

        return finalLetter;
    }

    /**
     * Returns the closest ship to an object.
     *
     * @param thisObj The source object that will be processed.
     * @return A TrekObject of the closest ship.
     */
    protected synchronized TrekObject getClosestShip(TrekObject thisObj, boolean visibilityImpaired) {
        TrekLog.logDebug("Server: Getting closest ship to " + thisObj.name + ".");

        Vector visObjs;

        if (visibilityImpaired)
            visObjs = getVisibleShips(thisObj);
        else
            visObjs = getAllShips();

        double closestDistance = 100000;
        double switchDistance = 0;

        TrekObject closestShip = null;

        for (int x = 0; x < visObjs.size(); x++) {
            TrekObject curObj = (TrekObject) visObjs.elementAt(x);

            if (curObj != null) {
                if (TrekUtilities.isObjectShip(curObj)) {
                    TrekShip testShip = (TrekShip) curObj;

                    if (testShip.isPlaying()) {
                        if (testShip != thisObj) {
                            switchDistance = TrekMath.getDistance(thisObj, curObj);

                            if (switchDistance < closestDistance) {
                                closestShip = curObj;
                                closestDistance = switchDistance;
                            }
                        }
                    }
                }
            }
        }

        return closestShip;
    }

    public synchronized TrekObject getClosestObject(TrekObject thisObj) {
        TrekLog.logDebug("Server: Getting closest objects to " + thisObj.name + ".");

        //Vector visObjs = getVisibleObjects(thisObj);
        Vector visObjs = getScannableObjects(thisObj);
        double closestDistance = 100000;
        double switchDistance = 0;

        TrekObject closestShip = null;

        for (int x = 0; x < visObjs.size(); x++) {
            TrekObject curObj = (TrekObject) visObjs.elementAt(x);

            if (curObj != null) {
                if (!TrekUtilities.isObjectShip(curObj)) {
                    switchDistance = TrekMath.getDistance(thisObj, curObj);

                    if (switchDistance < closestDistance) {
                        closestShip = curObj;
                        closestDistance = switchDistance;
                    }
                }
            }
        }

        return closestShip;
    }

    protected synchronized TrekPlanet getClosestPlanet(TrekObject thisObj) {
        TrekLog.logDebug("Server: Getting closest planet to " + thisObj.name + ".");

        Vector visObjs = getVisibleObjects(thisObj);
        double closestDistance = 100000;
        double switchDistance = 0;

        TrekObject closestShip = null;

        for (int x = 0; x < visObjs.size(); x++) {
            TrekObject curObj = (TrekObject) visObjs.elementAt(x);

            if (curObj != null) {
                if (TrekUtilities.isObjectPlanet(curObj)) {
                    switchDistance = TrekMath.getDistance(thisObj, curObj);

                    if (switchDistance < closestDistance) {
                        closestShip = curObj;
                        closestDistance = switchDistance;
                    }
                }
            }
        }

        return (TrekPlanet) closestShip;
    }

    protected synchronized TrekStarbase getClosestStarbase(TrekObject thisObj) {
        TrekLog.logDebug("Server: Getting closest starbase to " + thisObj.name + ".");

        Vector visObjs = getVisibleObjects(thisObj);
        double closestDistance = 100000;
        double switchDistance = 0;

        TrekObject closestShip = null;

        for (int x = 0; x < visObjs.size(); x++) {
            TrekObject curObj = (TrekObject) visObjs.elementAt(x);

            if (curObj != null) {
                if (TrekUtilities.isObjectStarbase(curObj)) {
                    switchDistance = TrekMath.getDistance(thisObj, curObj);

                    if (switchDistance < closestDistance) {
                        closestShip = curObj;
                        closestDistance = switchDistance;
                    }
                }
            }
        }

        return (TrekStarbase) closestShip;
    }

    public synchronized Vector getAllShipsInRange(TrekObject thisObject, int range) {
        TrekLog.logDebug("Server: Getting ships in " + range + " distance of " + thisObject.name + ".");

        Vector returnVector = new Vector();
        double scanRange = range;

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeObject = (TrekShip) e.nextElement();

            if (activeObject != null) {
                if (activeObject != thisObject && activeObject.isPlaying()) {
                    if (TrekMath.getDistance(thisObject, activeObject) < scanRange) {
                        //            TrekLog.logMessage( "Adding: " + activeObject.parent.shipName );
                        returnVector.addElement(activeObject);
                    }
                }
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    public synchronized Vector getAllVisibleShipsInRange(TrekObject thisObject, int range) {
        TrekLog.logDebug("Server: Getting visible ships in " + range + " distance of " + thisObject.name + ".");

        Vector returnVector = new Vector();
        double scanRange = range;
        int targetVisibility = 0;

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeShip = (TrekShip) e.nextElement();

            if (activeShip != null) {
                if (activeShip != thisObject && activeShip.isPlaying()) {
                    if ((activeShip.quasarTarget != null && activeShip.cloaked) ||
                            (activeShip.pulsarTarget != null && activeShip.cloaked && activeShip.pulsarTarget.active) ||
                            (!activeShip.cloaked)) {
                        if (!TrekUtilities.isObjectShip(thisObject) || !((TrekShip) thisObject).equals(activeShip)) {
                            if (activeShip.nebulaTarget != null) {
                                targetVisibility = activeShip.visibility / 3;
                            } else {
                                targetVisibility = activeShip.visibility;
                            }

                            double actualScanRange = TrekMath.getScanningRange(scanRange, (activeShip.getWarpEnergy() + activeShip.impulseEnergy) - activeShip.getAvailablePower(), targetVisibility);

                            if (TrekMath.getDistance(thisObject, activeShip) < actualScanRange) {
                                returnVector.addElement(activeShip);
                            }
                        }
                    }
                }
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    public synchronized Vector getAllVisibleShipsInTorpRange(TrekShip thisShip, int range) {
        TrekLog.logDebug("Server: Getting visible ships in " + range + " distance (torps) of " + thisShip.name + ".");

        Vector returnVector = new Vector();
        double scanRange = thisShip.scanRange;
        int targetVisibility = 0;

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeShip = (TrekShip) e.nextElement();

            if (activeShip != null && activeShip != thisShip && activeShip.isPlaying()) {
                if ((activeShip.quasarTarget != null && activeShip.cloaked) ||
                        (activeShip.pulsarTarget != null && activeShip.cloaked && activeShip.pulsarTarget.active) ||
                        (!activeShip.cloaked)) {
                    if (activeShip.nebulaTarget != null) {
                        targetVisibility = activeShip.visibility / 3;
                    } else {
                        targetVisibility = activeShip.visibility;
                    }

                    double actualScanRange = TrekMath.getScanningRange(scanRange, (activeShip.getWarpEnergy() + activeShip.impulseEnergy) - activeShip.getAvailablePower(), targetVisibility);

                    if (TrekMath.getDistance(thisShip, activeShip) < actualScanRange &&
                            TrekMath.getDistance(thisShip, activeShip) <= range) {
                        returnVector.addElement(activeShip);
                    }
                }
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    // primarily used for borg
    public synchronized Vector getAllVisibleShipsInExactRange(TrekObject thisObject, int range) {
        TrekLog.logDebug("Server: Getting visible ships within exactly " + range + " distance of " + thisObject.name + ".");

        Vector returnVector = new Vector();

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeShip = (TrekShip) e.nextElement();

            if (activeShip != null) {
                if (activeShip != thisObject && activeShip.isPlaying()) {
                    if ((activeShip.quasarTarget != null && activeShip.cloaked) ||
                            (activeShip.pulsarTarget != null && activeShip.cloaked && activeShip.pulsarTarget.active) ||
                            (!activeShip.cloaked)) {
                        if (TrekMath.getDistance(thisObject, activeShip) < range) {
                            returnVector.addElement(activeShip);
                        }
                    }
                }
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    protected synchronized Vector getAllShips() {
        Vector returnVector = new Vector();
        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeObject = (TrekShip) e.nextElement();

            if ((activeObject != null) && (activeObject.isPlaying())) {
                returnVector.addElement(activeObject);
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    public synchronized Vector getAllObjectsInRange(TrekObject thisObject, int range) {
        TrekLog.logDebug("Server: Getting all objects in " + range + " distance of " + thisObject.name + ".");

        Vector returnVector = new Vector();
        double scanRange = range;

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeObject = (TrekShip) e.nextElement();

            if (activeObject != null && activeObject.isPlaying()) {
                if (activeObject != thisObject) {
                    if (TrekMath.getDistance(thisObject, activeObject) < scanRange) {
                        returnVector.addElement(activeObject);
                    }
                }
            }
        }

        for (Enumeration e2 = objects.elements(); e2.hasMoreElements(); ) {
            TrekObject activeObject = (TrekObject) e2.nextElement();

            if (activeObject != null) {
                if (activeObject != thisObject) {
                    if (TrekMath.getDistance(thisObject, activeObject) < scanRange) {
                        returnVector.addElement(activeObject);
                    }
                }
            }

        }

        returnVector.trimToSize();
        return returnVector;
    }

    public synchronized int getCountShipsInRange(TrekObject thisObject, int range) {
        TrekLog.logDebug("Server: Getting ships in " + range + " distance of " + thisObject.name + ".");

        double scanRange = range;
        int count = 0;

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeObject = (TrekShip) e.nextElement();

            if (activeObject != null && activeObject.isPlaying()) {
                if (activeObject != thisObject && activeObject.isPlaying()) {
                    if (TrekMath.getDistance(thisObject, activeObject) < scanRange) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    public synchronized int getCountVisibleShipsInRange(TrekObject thisObject, int range) {
        TrekLog.logDebug("Server: Getting count of visible ships in " + range + " distance of " + thisObject.name + ".");

        double scanRange = range;
        int count = 0;

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeObject = (TrekShip) e.nextElement();

            if (activeObject != null && activeObject.isPlaying()) {
                if (activeObject != thisObject && activeObject.isPlaying()) {
                    if (TrekMath.getDistance(thisObject, activeObject) < scanRange &&
                            TrekUtilities.shipVisibleAndActive(activeObject, this)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    public synchronized TrekPlanet getHomePlanet(String thisPlanet) {
        TrekLog.logMessage(this.name + ": Getting home planet...");

        // Loop through the object hash table, looking for our planet.
        for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            TrekObject obj = (TrekObject) e.nextElement();

            // If it is not a planet, skip it.
            if (obj.type == TrekObject.OBJ_PLANET) {
                TrekPlanet planet = (TrekPlanet) obj;

                if (planet.name.equals(thisPlanet)) {
                    return planet;
                }
            }
        }

        return null;
    }

    protected synchronized Vector getShipsAffectedByWormhole(TrekObject thisObject, int range) {
        // duplicates much of the functionality of getAllVisibleShipInRange with additional check for decloak
        // timeout countdown required before a decloaking ship can enter the wormhole
        // also prevent ships from going through wormhole at rate faster than once per minute.
        Vector returnVector = new Vector();
        double scanRange = range;

        for (Enumeration e = ships.elements(); e.hasMoreElements(); ) {
            TrekShip activeShip = (TrekShip) e.nextElement();

            if (activeShip != null) {
                if (activeShip != thisObject && activeShip.isPlaying()) {
                    if (TrekMath.getDistance(thisObject, activeShip) <= scanRange) {
                        // make sure the ship isn't cloaked, and that if it recently decloaked that the
                        // 3 seconds have elapsed before allowing it into the wormhole.
                        if (!activeShip.cloaked && activeShip.cloakFireTimeout <= 0 && activeShip.whTimeElapsed >= 60)
                            returnVector.addElement(activeShip);
                    }
                }
            }
        }

        returnVector.trimToSize();
        return returnVector;
    }

    public synchronized Vector getObserverDevices() {
        Vector returnVector = new Vector();
        for (Enumeration e = objects.elements(); e.hasMoreElements(); ) {
            TrekObject curObj = (TrekObject) e.nextElement();
            if (TrekUtilities.isObjectObserverDevice(curObj)) {
                returnVector.addElement(curObj);
            }
        }

        return returnVector;
    }

    protected TrekFlag getFlag() {
        return flag;
    }

    protected void setFlag(TrekFlag flag) {
        this.flag = flag;
    }

    protected void clearFlag() {
        flag = null;
    }

}
