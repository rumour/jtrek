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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Vector;

/**
 * TrekDataInterface is used to load and save various game data.
 *
 * @author Joe Hopkinson
 */
public class TrekDataInterface {
    public synchronized void createDirectoryStructure() {
        try {
            File dir = new File("./data/");
            dir.mkdir();
            dir = new File("./log/");
            dir.mkdir();
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    protected synchronized Vector loadHelpFile(String letter) {
        try {
            String buffer;
            int count = 0;
            Vector helpLines = new Vector();
            File helpFile = new File("./data/helpfile" + letter + ".txt");

            if (helpFile.exists()) {
                BufferedReader helpIn = new BufferedReader(new FileReader("./data/helpfile" + letter + ".txt"));

                do {
                    buffer = helpIn.readLine();

                    if (buffer == null)
                        break;

                    if (buffer.length() >= 20) {
                        helpLines.add(buffer.substring(0, 20));
                    } else {
                        helpLines.add(buffer);
                    }

                    count++;

                    if (count >= 13)
                        break;

                }
                while (buffer != null);

                helpIn.close();

            }

            return helpLines;
        } catch (Exception e) {
            TrekLog.logException(e);
            return new Vector();
        }
    }
}