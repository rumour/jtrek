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

//import java.io.FileOutputStream;

import java.io.FileOutputStream;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Abstract.  An interface for creating a bot to run in the game.
 *
 * @author Joe Hopkinson
 */
public abstract class TrekBot extends TrekPlayer {

    /**
     * An abstract class for Bot functionality.
     *
     * @param serverIn
     * @param shipNameIn
     */
    public TrekBot(TrekServer serverIn, String shipNameIn) {
        try {
            state = WAIT_PLAYING;
            ansi = new TrekAnsi();
            outputBuffer = "";
            macros = new Hashtable();
            playerOptions = new TrekPlayerOptions(this);
            shipName = shipNameIn;
            messageQueue = new Vector();
            hud = new TrekHud(this);

            in = null;
            out = new FileOutputStream("./data/" + shipName + ".log");
            super.setName("Thread-" + shipName);
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    public void run() {
        try {
            TrekLog.logMessage("Bot running!");

            loadInitialBot();

            do {
                if (state == TrekPlayer.WAIT_HSOVERALL)
                    break;

                if (state == TrekPlayer.WAIT_HSCLASS)
                    break;

                if (state == TrekPlayer.WAIT_DEAD)
                    break;

                Thread.sleep(1);
            }
            while (true);
        } catch (java.lang.InterruptedException ie) {
            TrekLog.logError(ie.getMessage());
        } catch (Exception e) {
            TrekLog.logException(e);
        } finally {
            TrekServer.removePlayer(this);
            TrekLog.logMessage("Bot dead!");
        }
    }

    protected void doSecondUpdate() {
        super.doSecondUpdate();
        botSecondUpdate();
    }

    protected void doTickUpdate() {
        super.doTickUpdate();
        botTickUpdate();
    }

    protected void kill() {
        state = TrekPlayer.WAIT_DEAD;
    }

    protected abstract void loadInitialBot();

    protected abstract void botSecondUpdate();

    protected abstract void botTickUpdate();

    public void runMacro(String theMacro) {
        if (!theMacro.equals("")) {
            char[] commands = theMacro.toCharArray();

            TrekLog.logDebug(this.shipName + ": Running macro: " + new String(commands));
            //System.out.println(this.shipName + ": Running macro: " + new String(commands).toString());

            for (int m = 0; m < commands.length; m++) {

                // If there is a backslash, then process the octal code.
                if (Character.toString(commands[m]).equals("\\")) {
                    octalCode = true;
                }

                if (octalCode) {
                    if (!Character.toString(commands[m]).equals("\\")) {

                        if (commands[m] >= 48 && commands[m] <= 57 || commands[m] == 114) {
                            this.actualOctalCode += Character.toString(commands[m]);
                            this.octalCount++;
                        } else {
                            actualOctalCode = "";
                            octalCount = 4;
                        }
                    }

                    if (actualOctalCode.equals("r")) {
                        actualOctalCode = "015";
                        octalCount = 4;
                    }

                    if (octalCount >= 3) {
                        if (!actualOctalCode.equals("")) {
                            // Convert the octal to a decimal.
                            int decimalCommand = TrekMath.getDecimalFromOctal(actualOctalCode);

                            doCommand(decimalCommand);
                        }

                        this.octalCount = 0;
                        this.octalCode = false;
                        actualOctalCode = "";
                    }
                } else {
                    TrekLog.logDebug("Not an octal. Command: " + commands[m]);

                    if (commands[m] != 0) {
                        doCommand(commands[m]);
                        actualOctalCode = "";
                        this.octalCount = 0;
                        this.octalCode = false;
                    }
                }
            }
        }
    }
}
