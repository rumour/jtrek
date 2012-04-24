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


/**
 * Represents the options a player has set.
 *
 * @author Joe Hopkinson
 */
public class TrekPlayerOptions {
    TrekPlayer player;
    protected int[] options;

    protected static final int OPTION_BEEP = 1;
    protected static final int OPTION_ROSTER = 2;
    protected static final int OPTION_OBJECTRANGE = 3;
    protected static final int OPTION_BEARINGUPDATE = 4;
    protected static final int OPTION_RANGEUPDATE = 5;
    protected static final int OPTION_XYZUPDATE = 6;
    protected static final int OPTION_UNKNOWN = 7;
    protected static final int OPTION_DAMAGEREPORT = 8;

    public TrekPlayerOptions(TrekPlayer playerIn) {
        player = playerIn;
        options = new int[9];
    }

    protected void incrementOption(int thisOption) {
        switch (thisOption) {
            case OPTION_BEEP:
                if (options[OPTION_BEEP] == 0)
                    options[OPTION_BEEP] = 1;
                else
                    options[OPTION_BEEP] = 0;

                break;
            case OPTION_ROSTER:
                options[OPTION_ROSTER]++;

                if (options[OPTION_ROSTER] > 2)
                    options[OPTION_ROSTER] = 0;

                break;
            case OPTION_OBJECTRANGE:
                options[OPTION_OBJECTRANGE] += 1000;

                if (options[OPTION_OBJECTRANGE] > 5000)
                    options[OPTION_OBJECTRANGE] = 0;

                break;
            case OPTION_BEARINGUPDATE:
                if (options[OPTION_BEARINGUPDATE] == 0)
                    options[OPTION_BEARINGUPDATE] = 1;
                else if (options[OPTION_BEARINGUPDATE] == -1)
                    options[OPTION_BEARINGUPDATE] = 0;
                else
                    options[OPTION_BEARINGUPDATE] *= 2;

                if (options[OPTION_BEARINGUPDATE] > 20)
                    options[OPTION_BEARINGUPDATE] = -1;

                break;
            case OPTION_RANGEUPDATE:
                if (options[OPTION_RANGEUPDATE] == 0)
                    options[OPTION_RANGEUPDATE] = 1;
                else
                    options[OPTION_RANGEUPDATE] *= 2;

                if (options[OPTION_RANGEUPDATE] > 20)
                    options[OPTION_RANGEUPDATE] = 0;

                break;
            case OPTION_XYZUPDATE:
                if (options[OPTION_XYZUPDATE] == 0)
                    options[OPTION_XYZUPDATE] = 1;
                else
                    options[OPTION_XYZUPDATE] *= 2;

                if (options[OPTION_XYZUPDATE] > 20)
                    options[OPTION_XYZUPDATE] = 0;

                break;
            case OPTION_UNKNOWN:
                break;
            case OPTION_DAMAGEREPORT:
                if (options[OPTION_DAMAGEREPORT] == 0)
                    options[OPTION_DAMAGEREPORT] = 1;
                else
                    options[OPTION_DAMAGEREPORT] = 0;
            default:
                break;
        }
    }

    protected int getOption(int option) {
        try {
            return options[option];
        } catch (Exception e) {
            return 0;
        }
    }

    protected String getOptionString(int option) {
        switch (option) {
            case OPTION_BEEP:
                if (options[OPTION_BEEP] == 0)
                    return "enable";
                else
                    return "disable";
            case OPTION_ROSTER:
                switch (options[OPTION_ROSTER]) {
                    case 0:
                        return "gold";
                    case 1:
                        return "class";
                    case 2:
                        return "quadrant";
                    default:
                        return "unknown";
                }
            case OPTION_BEARINGUPDATE:
                if (options[OPTION_BEARINGUPDATE] == 0)
                    return "max";
                else if (options[OPTION_BEARINGUPDATE] == -1)
                    return "off";
                else
                    return new Double(options[OPTION_BEARINGUPDATE] * .25).toString();
            case OPTION_RANGEUPDATE:
                if (options[OPTION_RANGEUPDATE] == 0)
                    return "max";
                else
                    return new Double(options[OPTION_RANGEUPDATE] * .25).toString();
            case OPTION_XYZUPDATE:
                if (options[OPTION_XYZUPDATE] == 0)
                    return "max";
                else
                    return new Double(options[OPTION_XYZUPDATE] * .25).toString();
            case OPTION_DAMAGEREPORT:
                if (options[OPTION_DAMAGEREPORT] == 0)
                    return "verbose";
                else
                    return "terse";
            case OPTION_OBJECTRANGE:
                if (options[OPTION_OBJECTRANGE] == 0)
                    return "max";
                else
                    return new Integer(options[OPTION_OBJECTRANGE]).toString();
            default:
                return "???";
        }
    }

}
