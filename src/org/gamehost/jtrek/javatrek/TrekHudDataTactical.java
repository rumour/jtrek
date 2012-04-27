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

/**
 * This class stores all the data represented on the tactical of a ship's HUD (visible objects, heading, distance)
 * and compares data to another instance of the class; returning only elements that are different.
 */
public class TrekHudDataTactical {
    private boolean hasBeenCleared = false;

    protected static long uid = 0; // unique object id
    protected long myid;
    protected TrekObject theObj;
    protected String scanLtr;
    protected int type;
    protected String name = "";
    protected String bearing;
    protected int distance;
    protected boolean locked, scanning,
            active, mine = false;  // if tact objects are locked (ships), scanning (ships, obs devs),
    // active (pulsar, wh), or owned by the player (drones, mines, buoys, etc.)
    protected int teamNum = 0;

    public TrekHudDataTactical(TrekShip ship, TrekObject obj) {
        theObj = obj;
        scanLtr = theObj.scanLetter;
        type = theObj.type;
        bearing = ship.getBearingToObj(obj);
        distance = (int) TrekMath.getDistance(ship, obj);
        teamNum = ship.parent.teamNumber;

        myid = uid++;
    }

    public TrekHudDataTactical(TrekHudDataTactical thdt) {
        myid = thdt.myid;
        theObj = thdt.theObj;
        scanLtr = thdt.scanLtr;
        type = thdt.type;
        name = thdt.name;
        bearing = thdt.bearing;
        distance = thdt.distance;
        locked = thdt.locked;
        scanning = thdt.scanning;
        active = thdt.active;
        mine = thdt.mine;
        teamNum = thdt.teamNum;
    }

    protected void setName(String newName) {
        name = newName;
    }

    protected String returnDiff(TrekHudDataTactical thdt) {
        StringBuffer returnData = new StringBuffer();

        if (thdt == null)
            return writeInitial();

        if (theObj == thdt.theObj) {
            myid = thdt.myid;  // preserve the id value, so client can update the proper tactical object

            if (!scanLtr.equals(thdt.scanLtr))
                returnData.append("<scanltr>" + scanLtr + "</scanltr>");
            if (type != thdt.type)
                returnData.append("<type>" + type + "</type>");
            if (!name.equals(thdt.name))
                returnData.append("<name>" + name + "</name>");
            if (!bearing.equals(thdt.bearing))
                returnData.append("<bearing>" + bearing + "</bearing>");
            if (distance != thdt.distance)
                returnData.append("<distance>" + distance + "</distance>");
            if (locked != thdt.locked)
                returnData.append("<locked>" + locked + "</locked>");
            if (scanning != thdt.scanning)
                returnData.append("<scanning>" + scanning + "</scanning>");
            if (mine != thdt.mine)
                returnData.append("<mine>" + mine + "</mine>");
            if (active != thdt.active)
                returnData.append("<active>" + active + "</active>");
            if (TrekServer.isTeamPlayEnabled())
                if (teamNum != thdt.teamNum)
                    returnData.append("<team>" + teamNum + "</team>");

            if (returnData.length() > 0)
                return "<tactical id=\"" + myid + "\">" + returnData.toString() + "</tactical>";
            else
                return "";
        } else {
            return writeInitial();
        }
    }

    protected String writeInitial() {
        StringBuffer outTact = new StringBuffer();

        outTact.append("<tactical id=\"" + myid + "\">");

        outTact.append("<scanltr>" + scanLtr + "</scanltr>");
        outTact.append("<type>" + type + "</type>");
        outTact.append("<name>" + name + "</name>");
        outTact.append("<bearing>" + bearing + "</bearing>");
        outTact.append("<distance>" + distance + "</distance>");
        outTact.append("<locked>" + locked + "</locked>");
        outTact.append("<scanning>" + scanning + "</scanning>");
        outTact.append("<mine>" + mine + "</mine>");
        outTact.append("<active>" + active + "</active>");
        if (TrekServer.isTeamPlayEnabled())
            outTact.append("<team>" + teamNum + "</team>");

        outTact.append("</tactical>");

        return outTact.toString();
    }

    protected String clear() {
        if (!hasBeenCleared) {
            hasBeenCleared = true;
            return "<tactical id=\"" + myid + "\"><clear /></tactical>";
        } else {
            return "";
        }
    }
}
