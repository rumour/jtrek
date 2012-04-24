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
 * The TrekAnsi class holds all methods for controlling the users terminal with
 * ANSI control sequences.
 *
 * @author Joe Hopkinson
 */
public final class TrekAnsi {
    public TrekAnsi() {
    }

    protected static String clearScreen(TrekPlayer thisPlayer) {
        return new String(((char) 27) + ("[2J") + TrekAnsi.locate(1, 1, thisPlayer));
    }

    protected static String locate(int row, int col, TrekPlayer thisPlayer) {
        String srow = new Integer(row).toString();
        String scol = new Integer(col).toString();

        return new String(((char) 27) + ("[" + srow + ";" + scol + "H"));
    }

    protected static String clearRow(int row, TrekPlayer thisPlayer) {
        String srow = new Integer(row).toString();
        return new String(((char) 27) + ("[" + srow + ";1H") + ((char) 27) + ("[0K"));
    }

    protected static String clearRow(TrekPlayer thisPlayer) {
        return new String(((char) 27) + "[0K");
    }

    protected static String deleteLines(int lines, TrekPlayer thisPlayer) {
        return new String(((char) 27) + "[" + lines + "M");
    }

    protected static String insertBlankSpaces(int numberOfSpaces, TrekPlayer thisPlayer) {
        return new String(((char) 27) + "[" + numberOfSpaces + "@");
    }

    protected static String eraseToEndOfLine(TrekPlayer thisPlayer) {
        return new String(((char) 27) + "[0K");
    }

    protected static String deleteCharacters(int number, TrekPlayer thisPlayer) {
        return new String(((char) 27) + "[" + number + "P");
    }

    protected static String disableAutoWrap(TrekPlayer thisPlayer) {
        return new String(((char) 27) + "[?71");
    }

    public static String moveBackwards(int spaces, TrekPlayer thisPlayer) {
        return new String(((char) 27) + "[" + spaces + "D");
    }
}