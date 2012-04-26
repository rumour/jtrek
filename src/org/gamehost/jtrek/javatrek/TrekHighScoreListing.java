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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.StringTokenizer;

/**
 * TrekHighScoreListing represents a single high score listing.
 *
 * @author Joe Hopkinson
 */
public class TrekHighScoreListing {

    protected String listing;
    protected String fileString;

    protected String name = "???";
    protected String className = "???";
    protected int gold = 0;
    protected int dmgGiven = 0;
    protected int bonus = 0;
    protected int dmgReceived = 0;
    protected int conflicts = 0;
    protected int breakSaves = 0;
    protected String saveDate = "??? ??";
    protected String starbaseLetter = "??";
    protected String quadrant = "?";

    String finalBuffer = "";

    public TrekHighScoreListing(String thisInput) {
        listing = parse(thisInput);
    }

    public TrekHighScoreListing(TrekPlayer player) {
        listing = parse(player);
    }

    public TrekHighScoreListing(TrekPlayer player, boolean whoList) {
        listing = parseWhoListing(player);
    }

    private String parse(String thisString) {
        try {
            if (thisString == null) {
                listing = "---------------- -------- ------ -------- ----- ------- --- --- ----- --";
                fileString = "Nobody~????~0~0~0-0~0~0~??? ?~?";
            }

            StringTokenizer tokens = new StringTokenizer(thisString, "~");

            name = tokens.nextToken();
            className = tokens.nextToken();
            if (className.equals("INTERCEPTOR")) {
                className = "INTRCPTR";
            }
            gold = new Integer(tokens.nextToken());
            dmgGiven = new Integer(tokens.nextToken());
            bonus = new Integer(tokens.nextToken());
            dmgReceived = new Integer(tokens.nextToken());
            conflicts = new Integer(tokens.nextToken());
            breakSaves = new Integer(tokens.nextToken());
            saveDate = tokens.nextToken();
            starbaseLetter = tokens.nextToken();

            setStrings();

            return finalBuffer;
        } catch (Exception e) {
            TrekLog.logException(e);
            setStrings();
            return finalBuffer;
        }
    }

    private String parse(TrekPlayer thisPlayer) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat();
            formatter.applyPattern("MMM dd");

            Calendar cal = Calendar.getInstance();

            name = thisPlayer.shipName;
            className = thisPlayer.ship.shipClass;
            if (className.equals("INTERCEPTOR")) {
                className = "INTRCPTR";
            }
            gold = thisPlayer.ship.gold;
            dmgGiven = thisPlayer.ship.totalDamageGiven;
            bonus = thisPlayer.ship.totalBonus;
            dmgReceived = thisPlayer.ship.totalDamageReceived;
            conflicts = thisPlayer.ship.conflicts;
            breakSaves = thisPlayer.ship.breakSaves;
            saveDate = formatter.format(cal.getTime());

            starbaseLetter = thisPlayer.ship.dockTarget.scanLetter;

            if (thisPlayer.ship.currentQuadrant.name.equals("Beta Quadrant")) {
                if (starbaseLetter.compareTo("0") > 0)
                    starbaseLetter = "1" + starbaseLetter;
                else
                    starbaseLetter = "20";
            }

            if (thisPlayer.ship.currentQuadrant.name.equals("Gamma Quadrant")) {
                if (starbaseLetter.compareTo("0") > 0)
                    starbaseLetter = "2" + starbaseLetter;
                else
                    starbaseLetter = "30";

            }

            if (thisPlayer.ship.currentQuadrant.name.equals("Omega Quadrant")) {
                if (starbaseLetter.compareTo("0") > 0)
                    starbaseLetter = "3" + starbaseLetter;
                else
                    starbaseLetter = "40";

            }

            setStrings();

            return finalBuffer;
        } catch (Exception e) {
            TrekLog.logException(e);
            TrekLog.logException(e);
            setStrings();
            return finalBuffer;
        }
    }

    private String parseWhoListing(TrekPlayer thisPlayer) {
        try {
            name = thisPlayer.shipName;
            className = thisPlayer.ship.shipClass;
            if (className.equals("INTERCEPTOR")) {
                className = "INTRCPTR";
            }
            gold = thisPlayer.ship.gold;
            dmgGiven = thisPlayer.ship.totalDamageGiven;
            bonus = thisPlayer.ship.totalBonus;
            dmgReceived = thisPlayer.ship.totalDamageReceived;
            conflicts = thisPlayer.ship.conflicts;
            breakSaves = thisPlayer.ship.breakSaves;
            saveDate = "";
            starbaseLetter = "";
            quadrant = thisPlayer.ship.currentQuadrant.name;

            setStringsWho();

            return finalBuffer;
        } catch (Exception e) {
            TrekLog.logException(e);
            TrekLog.logException(e);
            setStringsWho();
            return finalBuffer;
        }
    }

    private void setStrings() {
        finalBuffer = format(name, "", 17);
        finalBuffer += format(className, "", 8);
        finalBuffer += format("", "" + gold, 7);
        finalBuffer += format("", "" + dmgGiven, 9);
        finalBuffer += format("", "" + bonus, 6);
        finalBuffer += format("", "" + dmgReceived, 8);
        finalBuffer += format("", "" + conflicts, 4);
        finalBuffer += format("", "" + breakSaves, 4);
        finalBuffer += format("", saveDate, 7);
        finalBuffer += format("", starbaseLetter, 3);

        fileString = name + "~" +
                className + "~" +
                gold + "~" +
                dmgGiven + "~" +
                bonus + "~" +
                dmgReceived + "~" +
                conflicts + "~" +
                breakSaves + "~" +
                saveDate + "~" +
                starbaseLetter;

    }

    private void setStringsWho() {
        finalBuffer = format(name, "", 17);
        finalBuffer += format(className, "", 8);
        finalBuffer += format("", "" + gold, 7);
        finalBuffer += format("", "" + dmgGiven, 9);
        finalBuffer += format("", "" + bonus, 6);
        finalBuffer += format("", "" + dmgReceived, 8);
        finalBuffer += format("", "" + conflicts, 4);
        finalBuffer += format("", "" + breakSaves, 4);
        finalBuffer += format("", quadrant.substring(0, quadrant.indexOf(' ')), 9);
    }


    protected String format(String start, String end, int chars) {
        String buffer = "";

        for (int x = 0; x < chars - start.length() - end.length(); x++) {
            buffer += " ";
        }

        return start + buffer + end;
    }
}
