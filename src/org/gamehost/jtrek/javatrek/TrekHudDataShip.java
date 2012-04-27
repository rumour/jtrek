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

import java.text.NumberFormat;

/**
 * This class stores all the data represented on the left side of a ship's HUD (the ship stats, WE, IE, etc.)
 * and compares data to another instance of the class; returning only elements that are different.  Also includes
 * data about ship status in space (such as zone effects, dock/orbit status, intercepting target, etc.)
 */
public class TrekHudDataShip {
    protected String shipClass; // ship class
    protected int we; // warp energy
    protected int ie; // impluse energy
    protected int pu; // power unused
    protected double warp; // warp speed
    protected int shields; // shields
    protected int ph; // phasers
    protected int dmg; // damage
    protected int tp; // torps
    protected int dmgctl; // damage control
    protected int ls; // life support
    protected int anti; // anti-matter
    protected int drones; // drones
    protected int mines; // mines
    protected int cloak; // cloak
    protected int heading; // heading
    protected int pitch; // pitch
    // ship x,y,z
    protected int x;
    protected int y;
    protected int z;
    protected boolean cloaked; // is the ship cloaked
    protected boolean xwarp; // is the ship transwarping
    protected String quad; // current quadrant name

    // these data values are set directly from the TrekRawHud and don't need initialization in the constructor
    // note: they must get set prior to a returnDiff() call

    // zone effects
    protected boolean neb = false; // nebula
    protected boolean pul = false; // pulsar
    protected boolean qua = false; // quasar
    protected boolean ast = false; // asteroids
    // dock/orbit status
    protected String doStat = "";
    // warp messages
    protected String warpMsg = "";
    // intercepting data
    protected String intData = "";
    // weapons lock data
    protected String lockData = "";
    // loading data
    protected String loadType = "";
    protected int load1 = -1; // current received count (for torps/xtals), or total received (drones/mines)
    protected int load2 = -1; // left to load (xtals), or max count (torps)


    public TrekHudDataShip(TrekShip theShip) {
        shipClass = theShip.shipClass;
        we = theShip.warpEnergy + theShip.currentCrystalCount;
        ie = theShip.impulseEnergy;
        pu = theShip.getAvailablePower();
        warp = theShip.warpSpeed;
        shields = theShip.shields;
        ph = theShip.phasers;
        dmg = theShip.damage;
        tp = theShip.torpedoes;
        dmgctl = theShip.damageControl;
        ls = theShip.lifeSupport;
        anti = theShip.antiMatter;
        drones = theShip.droneCount;
        mines = theShip.mineCount;
        cloak = (theShip.cloak) ? theShip.cloakTimeCurrent : 0;
        heading = new Integer(theShip.getHeading()).intValue();
        pitch = (int) theShip.vector.getPitch();
        //new Integer(theShip.getPitch()).intValue();
        cloaked = theShip.cloaked;
        xwarp = theShip.transwarpEngaged;
        quad = theShip.currentQuadrant.name;
        x = theShip.point.getXInt();
        y = theShip.point.getYInt();
        z = theShip.point.getZInt();
    }

    private String getFormattedSpeed(double warpSpeed) {
        NumberFormat numformat;
        numformat = NumberFormat.getInstance();
        numformat.setMaximumFractionDigits(1);
        numformat.setMinimumFractionDigits(1);
        String formattedWarp = numformat.format(warpSpeed);
        return formattedWarp;
    }

    protected String returnDiff(TrekHudDataShip prevData) {
        StringBuffer returnData = new StringBuffer();

        if (we != prevData.we)
            returnData.append("<we>" + we + "</we>");
        if (ie != prevData.ie)
            returnData.append("<ie>" + ie + "</ie>");
        if (pu != prevData.pu)
            returnData.append("<pu>" + pu + "</pu>");
        if (warp != prevData.warp)
            returnData.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
        if (shields != prevData.shields)
            returnData.append("<sh>" + shields + "</sh>");
        if (ph != prevData.ph)
            returnData.append("<ph>" + ph + "</ph>");
        if (dmg != prevData.dmg)
            returnData.append("<dmg>" + dmg + "</dmg>");
        if (tp != prevData.tp)
            returnData.append("<tp>" + tp + "</tp>");
        if (dmgctl != prevData.dmgctl)
            returnData.append("<dc>" + dmgctl + "</dc>");
        if (ls != prevData.ls)
            returnData.append("<ls>" + ls + "</ls>");
        if (anti != prevData.anti)
            returnData.append("<anti>" + anti + "</anti>");
        if (drones != prevData.drones)
            returnData.append("<dr>" + drones + "</dr>");
        if (mines != prevData.mines)
            returnData.append("<mn>" + mines + "</mn>");
        if (cloak != prevData.cloak)
            returnData.append("<cl>" + cloak + "</cl>");
        if (heading != prevData.heading)
            returnData.append("<hdg>" + heading + "</hdg>");
        if (pitch != prevData.pitch)
            returnData.append("<pch>" + pitch + "</pch>");
        if (cloaked != prevData.cloaked)
            returnData.append("<cloaked>" + cloaked + "</cloaked>");
        if (xwarp != prevData.xwarp)
            returnData.append("<xwarp>" + xwarp + "</xwarp>");
        if (!quad.equals(prevData.quad))
            returnData.append("<quad>" + quad + "</quad>");
        if (x != prevData.x)
            returnData.append("<x>" + x + "</x>");
        if (y != prevData.y)
            returnData.append("<y>" + y + "</y>");
        if (z != prevData.z)
            returnData.append("<z>" + z + "</z>");
        if (ast != prevData.ast)
            returnData.append("<ast>" + ast + "</ast>");
        if (neb != prevData.neb)
            returnData.append("<neb>" + neb + "</neb>");
        if (pul != prevData.pul)
            returnData.append("<pul>" + pul + "</pul>");
        if (qua != prevData.qua)
            returnData.append("<qua>" + qua + "</qua>");
        if (!doStat.equals(prevData.doStat))
            returnData.append("<dockorbit>" + doStat + "</dockorbit>");
        if (!warpMsg.equals(prevData.warpMsg))
            returnData.append("<warpmsg>" + warpMsg + "</warpmsg>");
        if (!intData.equals(prevData.intData))
            returnData.append("<intercept>" + intData + "</intercept>");
        if (!lockData.equals(prevData.lockData))
            returnData.append("<lock>" + lockData + "</lock>");
        if (!loadType.equals(prevData.loadType)) {
            returnData.append("<loading>" + prevData.loadType + "</loading>");
            //System.out.println("loadType = " + loadType + "  - prevData.loadType = " + prevData.loadType);
        }
        if (load1 != prevData.load1) {
            returnData.append("<load1>" + prevData.load1 + "</load1>");
            //System.out.println("load1 = " + load1 + "  - prevData.load1 = " + prevData.load1);
        }
        if (load2 != prevData.load2) {
            returnData.append("<load2>" + prevData.load2 + "</load2>");
            //System.out.println("load2 = " + load2 + "  - prevData.load2 = " + prevData.load2);
        }
        if (returnData.length() > 0) {
            return "<shipdata>" + returnData.toString() + "</shipdata>";
        }

        return "";
    }

    protected String writeInitial() {
        StringBuffer outHud = new StringBuffer();

        outHud.append("<shipdata>");

        // try adding a couple extra values for the initial dump
        outHud.append("<class>" + shipClass + "</class>");
        // skipping 'name' for now; could also add player stats

        // normal stuff
        outHud.append("<we>" + we + "</we>");
        outHud.append("<ie>" + ie + "</ie>");
        outHud.append("<pu>" + pu + "</pu>");
        outHud.append("<warp>" + getFormattedSpeed(warp) + "</warp>");
        outHud.append("<sh>" + shields + "</sh>");
        outHud.append("<ph>" + ph + "</ph>");
        outHud.append("<dmg>" + dmg + "</dmg>");
        outHud.append("<tp>" + tp + "</tp>");
        outHud.append("<dc>" + dmgctl + "</dc>");
        outHud.append("<ls>" + ls + "</ls>");
        outHud.append("<anti>" + anti + "</anti>");
        outHud.append("<dr>" + drones + "</dr>");
        outHud.append("<mn>" + mines + "</mn>");
        outHud.append("<cl>" + cloak + "</cl>");
        outHud.append("<hdg>" + heading + "</hdg>");
        outHud.append("<pch>" + pitch + "</pch>");
        outHud.append("<cloaked>" + cloaked + "</cloaked>");
        outHud.append("<xwarp>" + xwarp + "</xwarp>");
        outHud.append("<quad>" + quad + "</quad>");
        outHud.append("<x>" + x + "</x>");
        outHud.append("<y>" + y + "</y>");
        outHud.append("<z>" + z + "</z>");
        outHud.append("<ast>" + ast + "</ast>");
        outHud.append("<neb>" + neb + "</neb>");
        outHud.append("<pul>" + pul + "</pul>");
        outHud.append("<qua>" + qua + "</qua>");
        outHud.append("<dockorbit>" + doStat + "</dockorbit>");
        outHud.append("<warpmsg>" + warpMsg + "</warpmsg>");
        outHud.append("<intercept>" + intData + "</intercept>");
        outHud.append("<lock>" + lockData + "</lock>");
        outHud.append("<loading>" + loadType + "</loading>");
        outHud.append("<load1>" + load1 + "</load1>");
        outHud.append("<load2>" + load2 + "</load2>");

        outHud.append("</shipdata>");

        return outHud.toString();
    }
}
