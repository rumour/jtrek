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

import org.gamehost.jtrek.javatrek.bot.BotPlayer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.*;
import java.text.NumberFormat;
import java.util.*;

/**
 * Represents a player.
 *
 * @author Joe Hopkinson
 */
public class TrekPlayer extends Thread {
    protected static final int INPUT_NORMAL = 0;
    protected static final int INPUT_SCANLETTER = 1;
    protected static final int INPUT_SCANLETTERSHIP = 2;
    protected static final int INPUT_QUIT = 3;
    protected static final int INPUT_ESCAPECOMMAND = 4;
    protected static final int INPUT_MACROLETTERADD = 5;
    protected static final int INPUT_MACROLETTERREMOVE = 6;  // unused
    protected static final int INPUT_MACROADD = 7;
    protected static final int INPUT_MACROREMOVE = 8;
    protected static final int INPUT_WARPAMOUNT = 9;
    protected static final int INPUT_MESSAGETARGET = 10;
    protected static final int INPUT_MESSAGE = 11;
    protected static final int INPUT_PHASERTYPE = 12;
    protected static final int INPUT_TORPEDOSPEED = 13;
    protected static final int INPUT_TORPEDOTYPE = 14;
    protected static final int INPUT_PASSWORD = 15;
    protected static final int INPUT_COORDINATES = 16;
    protected static final int INPUT_SHIPLETTERSCORE = 17;
    protected static final int INPUT_LOCKTARGET = 18;
    protected static final int INPUT_DRONESPEED = 19;
    protected static final int INPUT_DRONESPEEDSELECT = 20;
    protected static final int INPUT_HEADING = 21;
    protected static final int INPUT_INTERCEPTSHIPNOSCAN = 22;
    protected static final int INPUT_SHIPLETTERINTCOORD = 23;
    protected static final int INPUT_SHIPLETTERDIRECTION = 24;
    protected static final int INPUT_SHIPSTATS = 25;
    protected static final int INPUT_SHIPLETTERJAM = 26;
    protected static final int INPUT_OPTION = 27;
    protected static final int INPUT_HELPSCREEN = 28;
    protected static final int INPUT_ADMIRALCOMMAND = 29;
    protected static final int INPUT_DEVICELETTER = 30;
    protected static final int INPUT_ROSTER = 31;
    protected static final int INPUT_SEEKERTARGET = 32;
    protected static final int WHO_NORMAL = 1;
    protected static final int WHO_ADDRESS = 2;
    protected static final int WHO_INGAME = 3;
    protected boolean quitAfterPassword = false;
    public boolean serverShutdown = false;
    public String playerHostName = "";
    public String playerIP = "";
    // Transmitter stuff.
    protected boolean transmitterBurnt = false;
    protected int messageSendCount = 0;
    protected int messageSendTimeout = 0;
    protected static final int MESSAGETARGET_ALL = 1;
    protected static final int MESSAGETARGET_SHIP = 2;
    protected static final int MESSAGETARGET_CLOSEST = 3;
    protected static final int MESSAGETARGET_RACE = 4;
    protected static final int MESSAGETARGET_TEAM = 5;
    protected String messagePrompt = "";
    protected static TrekServer server;
    public static final int WAIT_LOGIN = 0;
    public static final int WAIT_SHIPNAME = 1;
    public static final int WAIT_PASSWORD = 2;
    public static final int WAIT_SHIPCHOICE = 3;
    public static final int WAIT_PLAYING = 4;
    public static final int WAIT_DEAD = 5;
    public static final int WAIT_HSOVERALL = 6;
    public static final int WAIT_HSCLASS = 7;
    public static final int WAIT_HSFLEET = 8;
    public static final int WAIT_SOCKETERROR = 9;
    public static final int WAIT_SUCCESSFULLOGIN = 10;
    public static final int WAIT_SOCKETTIMEDOUT = 11;
    public static final int WAIT_SPECIFYPLAYER = 12;
    protected int chosenMacro;
    // Buffers
    protected String outputBuffer;
    protected String actualOctalCode = "";
    protected String buffer = "";
    protected String passwordBuffer = "";
    protected String typeAheadBuffer = "";
    protected String input = "";
    // Prompts
    protected String inputPrompt;
    // Player objects.
    protected TrekTelnet telnet = new TrekTelnet();
    protected TrekShip ship;
    public TrekHud hud;
    protected TrekAnsi ansi;
    protected TrekPlayerOptions playerOptions;
    protected TrekObject[] hudObjects;
    protected int inputstate;
    protected int inputstatenext = INPUT_NORMAL;
    protected String macro = "";
    protected Hashtable macros;
    protected Vector messageQueue;
    protected int messageTarget;
    protected String messageTargetShip = "";
    protected boolean octalCode = false;
    protected int octalCount = 0;
    protected String shipName;

    // For team play
    public int teamNumber = 0;

    public int dbPlayerID;
    public long dbConnectionID;
    protected long timeShipLogin;
    protected long timeShipLogout;

    // Old socket declarations
    protected Socket socket;
    //protected PrintWriter out;
    protected OutputStream out;
    protected InputStream in;

    // New socket handler.
    //TrekSocketHandler handler;
    public int state;
    protected HashMap scannedHistory = new HashMap(25);

    // used to determine whether the ship was saved or not; in the event of an
    // exception, use this to determine
    // whether or not to erase the saved file if it exists (i.e. they
    // terminated connection to save themselves from being
    // blown to pieces)
    protected boolean thisPlayerSaved = false;
    protected boolean validEscCommand = false;
    protected boolean vectorDebug = false;
    protected TrekJDBCInterface dbInt;
    protected boolean isAnonymous;

    protected boolean isObserved = false;
    protected boolean isObserving = false;
    protected Hashtable theObservers = new Hashtable();

    public TrekPlayer(Socket sck, TrekServer serverIn) {
        TrekLog.logMessage("Creating TrekPlayer thread...");
        server = serverIn;
        state = WAIT_LOGIN;
        ansi = new TrekAnsi();
        outputBuffer = new String();
        macros = new Hashtable();
        playerOptions = new TrekPlayerOptions(this);
        shipName = "";
        dbInt = new TrekJDBCInterface();
        messageQueue = new Vector();

        try {
            socket = sck;
            socket.setTcpNoDelay(true);
            in = sck.getInputStream();
            out = sck.getOutputStream();
            try {
                int slashIndex = sck.getInetAddress().toString().indexOf('/');
                playerIP =
                        sck.getInetAddress().toString().substring(slashIndex + 1);
                playerHostName = InetAddress.getByName(playerIP).toString();
            } catch (Exception e) {
                TrekLog.logException(e);
            }
        } catch (IOException ioe) {
            TrekLog.logMessage(
                    "Could not create TrekPlayer object for "
                            + sck.toString()
                            + "!!");
        }
    }

    public TrekPlayer() {
    }

    /**
     * Adds a message to the message queue for the player. This method is called from the TrekServer object.
     *
     * @param thisMessage
     */
    protected void addMessageToQueue(String thisMessage) {
        messageQueue.add(thisMessage);
    }

    protected boolean bufferInput(int x, boolean echo) {
        // Intercept carriage returns.
        if (x == 13)
            return true;
        if ((x != 0 && x >= 32 && x <= 126) || x == 13 || x == 8 || x == 32 || x == 127) {
            if (x == 8 || x == 127) {
                try {
                    buffer = buffer.substring(0, buffer.length() - 1);
                    // Fixed backspace issue on Ship Name: prompt.
                    sendText(TrekAnsi.moveBackwards(1, this) + TrekAnsi.eraseToEndOfLine(this));
                } catch (StringIndexOutOfBoundsException oops) {
                    buffer = "";
                }
            } else {
                buffer += Character.toString((char) x);
                if (echo) {
                    sendText(Character.toString((char) x));
                }
            }
        }
        return false;
    }

    protected boolean doChooseShip() {
        state = WAIT_SHIPCHOICE;
        try {
            //TODO: Load ship data from a file?
            this.state = TrekPlayer.WAIT_SHIPCHOICE;
            TrekLog.logMessage("User is choosing a ship...");
            boolean disconnect = false;
            String input = "";
            boolean passedCheck = false;
            String buffer1, buffer2 = "";
            sendText("\r\n\r\nShip Classes:\r\n");
            buffer1 = TrekUtilities.format("  a", "Constitution II-A", 24);
            buffer2 = TrekUtilities.format("h", "Orion BR-5", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            buffer1 = TrekUtilities.format("  b", "Excelsior", 24);
            buffer2 = TrekUtilities.format("i", "Orion BR-1000", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            buffer1 = TrekUtilities.format("  c", "Larson", 24);
            buffer2 = TrekUtilities.format("j", "Gorn CL-13", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            buffer1 = TrekUtilities.format("  d", "Freighter DY-600", 24);
            buffer2 = TrekUtilities.format("k", "Gorn CV-97", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            buffer1 = TrekUtilities.format("  e", "Romulan Bird of Prey", 24);
            buffer2 = TrekUtilities.format("l", "Cardassian CDA-180", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            buffer1 = TrekUtilities.format("  f", "Klingon EV-12", 24);
            buffer2 = TrekUtilities.format("m", "Cardassian CDA-120", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            buffer1 = TrekUtilities.format("  g", "Klingon PB-13", 24);
            buffer2 = TrekUtilities.format("n", "Romulan Warbird", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            sendText("\r\n");
            sendText("Experimental Vessels:\r\n");
            buffer1 = TrekUtilities.format("  o", "Klingon BOP", 24);
            buffer2 = TrekUtilities.format("r", "Gorn Scout", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            buffer1 = TrekUtilities.format("  p", "Cardassian Galor", 24);
            buffer2 = TrekUtilities.format("s", "Romulan Interceptor", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            buffer1 = TrekUtilities.format("  q", "Orion BR-2000", 24);
            buffer2 = TrekUtilities.format("t", "Federation Defiant", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");
            sendText("\r\n");
            sendText("Classic Vessels:\r\n");
            buffer1 = TrekUtilities.format("  u", "Ferengi", 24);
            buffer2 = TrekUtilities.format("v", "Klingon D-10", 22);
            sendText(buffer1 + "     " + buffer2 + "\r\n");

            do {
                if (disconnect)
                    return false;
                sendText("\r\nEnter Class: ");
                do {
                    //input = getCharacterInput(false, true, false);
                    input = getBlockedInput(true, 60000, false);
                    if (input == null) {
                        disconnect = true;
                        break;
                    }
                    if (input.charAt(0) == 3)
                        return false;
                }
                while (input.equals(""));
                if (disconnect)
                    return false;
                input = input.toLowerCase();
                if (input.equals("a") || input.equals("b") || input.equals("c") || input.equals("d") || input.equals("e") || input.equals("f") || input.equals("g") || input.equals("h") || input.equals("i") || input.equals("j") || input.equals("k") || input.equals("l") || input.equals("m") || input.equals("n") || input.equals("o") || input.equals("p") || input.equals("q") || input.equals("r") || input.equals("s") || input.equals("t") || input.equals("u") || input.equals("v")) {
                    ship = TrekUtilities.getShip(input, this);
                    ship.setInitialDirection();
                    hud = new TrekHud(this);
                    passedCheck = true;
                    dbInt.loadTemplateKeymaps(this);
                    dbInt.saveNewShipRecord(this.ship);
                } else {
                    sendText("\r\nIllegal ship class.");
                    passedCheck = false;
                }
            }
            while (!passedCheck);
            TrekLog.logMessage("User finally chose a ship.");

            // display player specific login message
            String loginMessage = dbInt.getPlayerLoginMsg(dbPlayerID);
            if (loginMessage != null && !(loginMessage.equals(""))) {
                sendText("\r\n\r\n" + loginMessage.replaceAll("\n", "\r\n") + "\r\n");

                sendText("\r\nPress a key to proceed.");
                input = getBlockedInput(true, 60000, false);
            }

            state = WAIT_SUCCESSFULLOGIN;
            return true;
        } catch (Exception e) {
            TrekLog.logException(e);
            return false;
        }
    }

    protected void doCommand(int command) {
        if (state != WAIT_PLAYING)
            return;
        // do regular commands unless there is an inputstatenext already set;
        // this will prevent normal commands from intercepting user input
        // that should be handled by the inputstatenext blocks
        if (inputstatenext == INPUT_NORMAL) {
            TrekLog.logDebug(shipName + ": doCommand(" + command + ");");
            /*
                *
                * ****** CATCH INPUT STATES ******
                *
                */
            // kicked off by shift-i command
            if (inputstate == INPUT_COORDINATES) {
                // ctrl-c
                if (command == 3) {
                    sendText(TrekAnsi.clearRow(19, this));
                    inputstate = INPUT_NORMAL;
                    buffer = "";
                    command = 0;
                }
                // backspace / ctrl-h
                if (isValidEraseCharacter(command)) {
                    if (buffer.length() > 0) {
                        buffer = buffer.substring(0, buffer.length() - 1);
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText(TrekAnsi.locate(19, 1, this));
                        sendText("Intercept (X Y Z): " + buffer);
                    }
                }
                // limit characters to numbers, negative sign, and space
                if ((command >= 48 && command <= 57) || command == 45 || command == 32) {
                    buffer += Character.toString((char) command);
                    sendText(TrekAnsi.clearRow(19, this));
                    sendText(TrekAnsi.locate(19, 1, this));
                    sendText("Intercept (X Y Z): " + buffer);
                }
                if (command == 13) {
                    try {
                        // check for two spaces, otherwise don't bother
                        // processing the data
                        int spaceCount = 0;
                        for (int bufferLoop = 0; bufferLoop < buffer.length(); bufferLoop++) {
                            if (buffer.charAt(bufferLoop) == ' ')
                                spaceCount++;
                        }
                        if (spaceCount >= 2) {
                            //TrekLog.logMessage("Intercept data entered as: "
                            // + buffer);
                            buffer = buffer.trim();
                            // get rid of any leading/trailing spaces
                            float coordX = new Float(buffer.substring(0, buffer.indexOf(' ')).trim()).floatValue();
                            buffer = buffer.substring(buffer.indexOf(' ') + 1, buffer.length());
                            float coordY = new Float(buffer.substring(0, buffer.indexOf(' ')).trim()).floatValue();
                            float coordZ = new Float(buffer.substring(buffer.indexOf(' ') + 1, buffer.length()).trim()).floatValue();
                            //TrekLog.logMessage("Intercept data parsed as: "
                            // + coordX + " " + coordY + " " + coordZ);
                            ship.interceptCoords(coordX, coordY, coordZ);
                        } else {
                            sendText(TrekAnsi.clearRow(19, this));
                            hud.sendMessage("Badly formed coordinates.  Enter as: X Y Z");
                        }
                    } catch (Exception badnumber) {
                    } finally {
                        sendText(TrekAnsi.clearRow(19, this));
                        inputstate = INPUT_NORMAL;
                        buffer = "";
                        command = 0;
                    }
                }
                return;
            }

            // f command (drop misc device)
            if (inputstate == INPUT_DEVICELETTER) {
                switch (command) {
                    case 98: // b - buoy
                        ship.dropBuoy();
                        break;
                    case 99: // c - corbomite device
                        ship.dropCorbomite();
                        break;
                    case 105: // i - iridium mine
                        ship.dropIridium();
                        break;
                    case 108: // l - lithium mine
                        ship.dropLithium();
                        break;
                    case 109: // m - magnabuoy
                        ship.dropMagnabuoy();
                        break;
                    case 110: // n - neutron mine
                        ship.dropNeutron();
                        break;
                    case 115: // s - seeker probe
                        inputstate = INPUT_SEEKERTARGET;
                        break;
                } // end switch

                buffer = "";

                sendText(TrekAnsi.clearRow(19, this));
                if (command == 115) {
                    sendText(TrekAnsi.locate(19, 1, this));
                    sendText("Which ship? ");
                } else {
                    inputstate = INPUT_NORMAL;
                }
                command = 0;
                return;
            }

            // ! command
            if (inputstate == INPUT_HEADING) {
                if (command == 3) {
                    sendText(TrekAnsi.clearRow(19, this));
                    inputstate = INPUT_NORMAL;
                    buffer = "";
                    command = 0;
                }
                if (command == 8) {
                    if (buffer.length() > 0) {
                        buffer = buffer.substring(0, buffer.length() - 1);
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText(TrekAnsi.locate(19, 1, this));
                        sendText("Heading (xxx'zz): " + buffer);
                    }
                }
                // limit characters to numeric, minus sign, single quote
                if ((command >= 48 && command <= 57) || (command == 45) || (command == 39)) {
                    buffer += Character.toString((char) command);
                    sendText(TrekAnsi.clearRow(19, this));
                    sendText(TrekAnsi.locate(19, 1, this));
                    sendText("Heading (xxx'zz): " + buffer);
                }
                if (command == 13) {
                    // check for number, single quote, number
                    if (buffer.indexOf('\'') != -1) {
                        try {
                            int heading = new Integer(buffer.substring(0, buffer.indexOf('\'')).trim()).intValue();
                            int pitch = new Integer(buffer.substring(buffer.indexOf('\'') + 1, buffer.length()).trim()).intValue();
                            heading = Math.abs(heading % 360);
                            // regardless of the number, break it down into 360
                            // degree format
                            if ((pitch >= -90) && (pitch <= 90)) {
                                //TrekLog.logMessage("Set heading: " + heading + "'" + pitch);
                                ship.changeHeading(heading, pitch);
                            } else {
                                hud.sendMessage("Badly formed heading, use xxx'zz.  i.e. 123'45, or 123'-45.");
                            }
                        } catch (Exception e) {
                            hud.sendMessage("Badly formed heading, use xxx'zz.  i.e. 123'45, or 123'-45.");
                        } finally {
                            sendText(TrekAnsi.clearRow(19, this));
                            inputstate = INPUT_NORMAL;
                            buffer = "";
                            command = 0;
                        }
                    } else {
                        hud.sendMessage("Badly formed heading, use xxx'zz.  i.e. 123'45, or 123'-45.");
                        sendText(TrekAnsi.clearRow(19, this));
                        inputstate = INPUT_NORMAL;
                        buffer = "";
                        command = 0;
                    }
                }
                return;
            }

            if (inputstate == INPUT_HELPSCREEN) {
                sendText(TrekAnsi.clearRow(19, this));
                hud.showHelpScreen = true;
                hud.showingHelpScreen = false;
                hud.helpScreenLetter = new Character((char) command).toString();
                TrekLog.logMessage("User chose help screen: " + hud.helpScreenLetter);
                inputstate = INPUT_NORMAL;
                command = 0;
                return;
            }

            // get letter of ship for ESC-ctrl-i, and intercept if in scanning
            // range
            if (inputstate == INPUT_INTERCEPTSHIPNOSCAN) {
                if (command != 0) {
                    if (command == 46) {
                        TrekObject closestShip = ship.currentQuadrant.getClosestShip(ship, true);
                        if (closestShip != null) {
                            if (closestShip instanceof TrekShip) {
                                ship.interceptShipNoScan(closestShip.scanLetter);
                            }
                        } else {
                            hud.clearMessage(19);
                            //sendText("No ships in the area.");
                        }
                    } else {
                        if (TrekUtilities.isValidShipChar(command))
                            ship.interceptShipNoScan(Character.toString((char) command));
                    }
                    sendText(TrekAnsi.clearRow(19, this));
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
                return;
            }

            if (inputstate == INPUT_MACROLETTERADD) {
                chosenMacro = (char) command;
                inputstate = INPUT_NORMAL;
                inputstatenext = INPUT_MACROADD;
                sendText(TrekAnsi.clearRow(19, this) + "Macro: ");
                command = 0;
                return;
            }

            if (inputstate == INPUT_MESSAGETARGET) {
                buffer = "";
                if ((TrekUtilities.isValidShipChar(command)) || command == 42 || command == 46 || command == 125) {
                    if (command == 42) {
                        messagePrompt = ") ";
                        this.messageTarget = TrekPlayer.MESSAGETARGET_ALL;
                        this.inputstatenext = TrekPlayer.INPUT_MESSAGE;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText(messagePrompt + buffer);
                        inputstatenext = INPUT_MESSAGE;
                    } else if (command == 46) {
                        messagePrompt = "] ";
                        this.messageTarget = TrekPlayer.MESSAGETARGET_CLOSEST;
                        this.inputstatenext = TrekPlayer.INPUT_MESSAGE;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText(messagePrompt + buffer);
                        inputstatenext = INPUT_MESSAGE;
                    } else if (command == 125) {
                        messagePrompt = "} ";
                        if (TrekServer.isTeamPlayEnabled()) {
                            messageTarget = TrekPlayer.MESSAGETARGET_TEAM;
                        } else {
                            this.messageTarget = TrekPlayer.MESSAGETARGET_RACE;
                        }
                        this.inputstatenext = TrekPlayer.INPUT_MESSAGE;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText(messagePrompt + buffer);
                        inputstatenext = INPUT_MESSAGE;
                    } else {
                        if (TrekServer.getPlayerShipByScanLetter(new Character((char) command).toString()) == null) {
                            messagePrompt = "? ";
                        } else {
                            messagePrompt = "] ";
                        }
                        if (TrekUtilities.isValidShipChar(command)) {
                            this.messageTargetShip = Character.toString((char) command);
                            this.messageTarget = TrekPlayer.MESSAGETARGET_SHIP;
                            this.inputstatenext = TrekPlayer.INPUT_MESSAGE;
                            sendText(TrekAnsi.clearRow(19, this));
                            sendText(messagePrompt + buffer);
                            inputstatenext = INPUT_MESSAGE;
                        } else {
                            sendText(TrekAnsi.clearRow(19, this));
                            inputstatenext = INPUT_NORMAL;
                        }
                    }
                    inputstate = INPUT_NORMAL;
                } else {
                    sendText(TrekAnsi.clearRow(19, this));
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                }
                command = 0;
                return;
            }

            if (inputstate == INPUT_QUIT) {
                if (command == 121) {
                    if (ship.damage <= 150) {
                        doQuit();
                    } else {
                        hud.sendMessage("Sorry, you have too much damage to quit.");
                    }
                }
                sendText(TrekAnsi.clearRow(19, this));
                inputstate = INPUT_NORMAL;
                return;
            }

            // If scanning something..
            if (inputstate == INPUT_SCANLETTER) {
                if (command == 46) {
                    TrekObject closestShip = ship.currentQuadrant.getClosestObject(ship);
                    if (closestShip != null) {
                        if (!(closestShip instanceof TrekShip)) {
                            ship.scan(closestShip.scanLetter);
                        }
                    } else {
                        //hud.sendMessage("No objects in the area.");
                    }
                } else {
                    if (TrekUtilities.isValidObjChar(command))
                        ship.scan(Character.toString((char) command));
                }
                inputstate = INPUT_NORMAL;
                command = 0;
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            if (inputstate == INPUT_SCANLETTERSHIP) {
                if (command == 46) {
                    TrekObject closestShip = ship.currentQuadrant.getClosestShip(ship, true);
                    if (closestShip != null) {
                        if (closestShip instanceof TrekShip) {
                            ship.scanShip(closestShip.scanLetter);
                        }
                    } else {
                        //hud.sendMessage("No ships in the area.");
                    }
                } else {
                    if (TrekUtilities.isValidShipChar(command))
                        ship.scanShip(Character.toString((char) command));
                }
                inputstate = INPUT_NORMAL;
                command = 0;
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            // seeker probe target letter
            if (inputstate == INPUT_SEEKERTARGET) {
                if (command != 0) {
                    if (TrekUtilities.isValidShipChar(command)) {
                        ship.fireSeekerProbe(Character.toString((char) command));
                    }
                    command = 0;
                    inputstate = INPUT_NORMAL;
                    sendText(TrekAnsi.clearRow(19, this));
                }
                return;
            }

            if (inputstate == INPUT_SHIPLETTERJAM) {
                if (command == 46) {
                    TrekObject closestShip = ship.currentQuadrant.getClosestShip(ship, false);
                    if (closestShip != null) {
                        if (closestShip instanceof TrekShip) {
                            ship.scanShip(closestShip.scanLetter);
                        }
                    } else {
                        //hud.sendMessage("No ships in the area.");
                    }
                } else {
                    if (TrekUtilities.isValidShipChar(command))
                        ship.toggleJamShipSlot(Character.toString((char) command));
                    if (command == 42) // jam globals
                        ship.toggleJamGlobal();
                }
                inputstate = INPUT_NORMAL;
                command = 0;
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            if (inputstate == INPUT_SHIPSTATS) {
                if (command == 46) {
                    TrekObject closestShip = ship.currentQuadrant.getClosestShip(ship, false);
                    if (closestShip != null) {
                        if (closestShip instanceof TrekShip) {
                            ship.showShipStats(closestShip.scanLetter);
                        }
                    } else {
                        //hud.sendMessage("No ships in the area.");
                    }
                } else {
                    if (TrekUtilities.isValidShipChar(command))
                        ship.showShipStats(Character.toString((char) command));
                }
                inputstate = INPUT_NORMAL;
                command = 0;
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            if (inputstate == INPUT_WARPAMOUNT) {
                // CTRL+C
                if (command == 3) {
                    sendText(TrekAnsi.clearRow(19, this));
                    inputstate = INPUT_NORMAL;
                    buffer = "";
                }
                if (isValidEraseCharacter(command)) {
                    if (buffer.length() > 0) {
                        buffer = buffer.substring(0, buffer.length() - 1);
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Enter warp speed: " + buffer);
                    }
                }
                if ((command >= 48 && command <= 57) || command == 46 || command == 45) {
                    buffer += Character.toString((char) command);
                    sendText(TrekAnsi.clearRow(19, this));
                    sendText("Enter warp speed: " + buffer);
                }
                if (command == 13) {
                    try {
                        double warpSpeed = new Double(buffer).doubleValue();
                        if (Math.abs(warpSpeed) > ship.damageWarp) {
                            hud.sendMessage("Warp too high.");
                        } else {
                            ship.setWarp(warpSpeed);
                        }
                    } catch (Exception badnumber) {
                        ship.setWarp(0);
                    } finally {
                        sendText(TrekAnsi.clearRow(19, this));
                        inputstate = INPUT_NORMAL;
                        buffer = "";
                    }
                }
                command = 0;
                return;
            }

            if (inputstate == INPUT_ADMIRALCOMMAND) {
                inputPrompt = "Admiral command: ";
                switch (command) {
                    case 3: // CTRL-C to cancel.
                        buffer = "";
                        inputstate = INPUT_NORMAL;
                        sendText(TrekAnsi.clearRow(19, this));
                        break;
                    case 13: // Enter to issue the command.
                        doAdmiralCommand(buffer);
                        buffer = "";
                        inputstate = INPUT_NORMAL;
                        sendText(TrekAnsi.clearRow(19, this));
                        break;
                    default:
                        if (isValidEraseCharacter(command)) {
                            if (buffer.length() > 0) {
                                buffer = buffer.substring(0, buffer.length() - 1);
                                sendText(TrekAnsi.locate(19, inputPrompt.length() + buffer.length() + 1, this) + TrekAnsi.eraseToEndOfLine(this));
                            }
                        } else {
                            if (isValidInput(command)) {
                                buffer += new Character((char) command).toString();
                                sendText(TrekAnsi.locate(19, inputPrompt.length() + buffer.length(), this) + new Character((char) command).toString());
                            }
                        }
                        break;
                }
                return;
            }
            /*
                *
                * ****** ESCAPE COMMANDS ******
                *
                */
            if (inputstate == INPUT_ESCAPECOMMAND) {
                sendText(TrekAnsi.clearRow(19, this));
                switch (command) {
                    case 5:
                        // escape pod; esc-ctrl-e
                        ship.launchEscapePod();
                        break;
                    case 12:
                        // lock weapons; esc-ctrl-l-<ship>
                        sendText("Which object or ship: ");
                        inputstatenext = INPUT_LOCKTARGET;
                        break;
                    case 13:
                        // intercept heading of last message; esc-ctrl-m
                        if (ship.msgPoint != null) {
                            ship.interceptMsg();
                        } else {
                            hud.sendMessage("You've received no messages to track.");
                        }
                        break;
                    case 14:
                        // phasers to narrow; esc-ctrl-n
                        hud.sendMessage("Phasers set to fire narrow.");
                        ship.phaserFireType = TrekShip.PHASER_FIRENARROW;
                        break;
                    case 16:
                        // dy600 - change phaser type; esc-ctrl-p
                        if (!(this.ship instanceof ShipDY600)) {
                            hud.sendMessage("You cannot change phaser types.");
                        } else {
                            sendText(TrekAnsi.clearRow(2, this));
                            sendText("Phaser Type: 0) PHASER  1) TELEPORTER");
                            inputstatenext = INPUT_PHASERTYPE;
                        }
                        break;
                    case 19:
                        // scan range; esc-ctrl-s
                        hud.sendMessage("Scan Range: " + new Double(ship.scanRange).intValue());
                        break;
                    case 20:
                        // cv & scout - change torp type; esc-ctrl-t
                        if (!(this.ship instanceof ShipCV97) && !(this.ship instanceof ShipScout)) {
                            hud.sendMessage("You cannot change your torpedo type.");
                        } else {
                            sendText(TrekAnsi.clearRow(2, this));
                            sendText("Torpedo Type: 0) PLASMA  1) BOLT PLASMA");
                            inputstatenext = INPUT_TORPEDOTYPE;
                        }
                        break;
                    case 23:
                        // set phasers to fire wide; esc-ctrl-w
                        hud.sendMessage("Phasers set to fire wide.");
                        ship.phaserFireType = TrekShip.PHASER_FIREWIDE;
                        break;
                    case 35:
                        // ship roster letter; esc-  ???
                        hud.sendMessage("Your are designated as ship " + ship.scanLetter + ".");
                        break;
                    case 64:
                        // show 'who' roster - esc-@
                        inputstatenext = INPUT_ROSTER;
                        sendText(TrekAnsi.clearScreen(this));
                        hud.disabled = true;
                        printShipRoster(WHO_INGAME);
                        break;
                    case 76:
                        // display last seen coords; esc-L-<ship>
                        inputstatenext = INPUT_SHIPLETTERDIRECTION;
                        sendText("Which ship? ");
                        break;
                    case 77:
                        // display bearing to last message received; esc-M-<ship>
                        if (ship.msgPoint != null) {
                            hud.sendMessage("Last message received from: " + ship.getBearingToMsg());
                        } else {
                            hud.sendMessage("You've received no messages to track.");
                        }
                        break;
                    case 79:
                        // show odometer; esc-O
                        ship.showOdometerPage();
                        break;
                    case 80:
                        // set new password; esc-P
                        sendText("New Ship Password: ");
                        inputstatenext = INPUT_PASSWORD;
                        break;
                    case 82:
                        // show jammed frequencies; esc-R
                        ship.reportJammedSlots();
                        break;
                    case 83:
                        // save; esc-S
                        if (!(ship instanceof ShipQ)) {
                            if (!this.ship.docked) {
                                hud.sendMessage("You can only save if you are docked at a starbase.");
                            } else {
                                doSave();
                            }
                        } else {
                            doSave();
                        }
                        break;
                    case 84:
                        // change torp speed; esc-T
                        if (this.ship.torpedoType != TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                            hud.sendMessage("You cannot change your torpedo speed.");
                        } else {
                            sendText(TrekAnsi.clearRow(2, this));

                            sendText("Speed: a) auto 5) 5.0 6) 6.0 7) 7.0 8) 8.0 9) 9.0 0) 10.0");
                            inputstatenext = INPUT_TORPEDOSPEED;
                        }
                        break;
                    case 87:
                        // show weapons ranges and storage; esc-W
                        String phaserString = "";
                        String torpedoString = "Torpedoes: " + ship.torpedoCount + "(" + ship.maxTorpedoStorage + ") " + ship.minTorpedoRange + "-" + ship.maxTorpedoRange + " " + ship.getMtrekStyleTorpString();
                        if (ship.homePlanet.equals("Cardassia")) {
                            phaserString = "Phasers: " + ship.maxPhaserBanks + " 1-" + ship.minPhaserRange + " " + ship.getMtrekStylePhaserString();
                        } else {
                            phaserString = "Phasers: " + ship.maxPhaserBanks + " 0-" + ship.minPhaserRange + " " + ship.getMtrekStylePhaserString();
                        }
                        hud.sendMessage(phaserString + "  " + torpedoString);
                        break;
                    case 97:
                        // set variable speed plasma to auto; esc-a
                        if (this.ship.torpedoType != TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                            hud.sendMessage("You cannot change your torpedo speed.");
                        } else {
                            this.ship.torpedoWarpSpeedAuto = true;
                            hud.sendMessage("Torpedo speed will be set automatically.");
                        }
                        break;
                    case 98:
                        // check buoy availability; esc-b
                        if (ship.buoyTimeout > 0)
                            hud.sendMessage("Next buoy will be ready in " + ship.buoyTimeout + " second(s).");
                        else
                            hud.sendMessage("A buoy is ready for deployment.");
                        break;
                    case 99:
                        // show ship score; esc-c-<ship>
                        inputstatenext = INPUT_SHIPLETTERSCORE;
                        sendText("Which ship? ");
                        break;
                    case 100:
                        // show drone status; esc-d
                        if (ship.homePlanet.equals("Cardassia")) {
                            hud.sendMessage("Drones: " + ship.droneCount + "(" + ship.maxDroneStorage + ")  Strength: variable");
                        } else {
                            if (ship.drones) {
                                hud.sendMessage("Drones: " + ship.droneCount + "(" + ship.maxDroneStorage + ")  Strength: " + ship.droneStrength);
                            } else {
                                hud.sendMessage("This type of ship can't carry any drones.");
                            }
                        }
                        break;
                    case 105:
                        // show inventory; esc-i
                        hud.showInventoryScreen();
                        break;
                    case 108:
                        // intercept ship coords; esc-l-<ship>
                        inputstatenext = INPUT_SHIPLETTERINTCOORD;
                        sendText("Which ship? ");
                        break;
                    case 109:
                        // show mine status; esc-m
                        if (ship.mines) {
                            hud.sendMessage("Mines: " + ship.mineCount + "(" + ship.maxMineStorage + ")  Strength: " + ship.mineStrength);
                        } else {
                            hud.sendMessage("This type of ship can't carry any mines.");
                        }
                        break;
                    case 111:
                        // show options page; esc-o
                        if (hud.showOptionScreen) {
                            hud.showOptionScreen = false;
                            inputstate = INPUT_NORMAL;
                            inputstatenext = INPUT_NORMAL;
                        } else {
                            ship.stopScanning();
                            hud.showOptionScreen = true;
                            inputstatenext = INPUT_OPTION;
                        }
                        break;
                    case 112:
                        // show phaser range and type; esc-p
                        if (ship.homePlanet.equals("Cardassia")) {
                            hud.sendMessage("Phasers: " + ship.maxPhaserBanks + "  Range: 1-" + ship.minPhaserRange + "  Phaser type: " + ship.getMtrekStylePhaserString());
                        } else {
                            hud.sendMessage("Phasers: " + ship.maxPhaserBanks + "  Range: 0-" + ship.minPhaserRange + "  Phaser type: " + ship.getMtrekStylePhaserString());
                        }
                        break;
                    case 114:
                        // show weapons ranges; esc-r
                        if (ship.homePlanet.equals("Cardassia")) {
                            hud.sendMessage("Phaser Range: 1-" + ship.minPhaserRange + "  Torpedo Range: " + ship.minTorpedoRange + "-" + ship.maxTorpedoRange + ".");
                        } else {
                            hud.sendMessage("Phaser Range: 0-" + ship.minPhaserRange + "  Torpedo Range: " + ship.minTorpedoRange + "-" + ship.maxTorpedoRange + ".");
                        }
                        break;
                    case 115:
                        // show score; esc-s
                        hud.showScore(this);
                        break;
                    case 116:
                        // show torp count and range; esc-t
                        hud.sendMessage("Torpedoes: " + ship.torpedoCount + "(" + ship.maxTorpedoStorage + ")  Range: " + ship.minTorpedoRange + "-" + ship.maxTorpedoRange + "  Torpedo type: " + ship.getMtrekStyleTorpString());
                        break;
                    case 118:
                        // show visibility; esc-v
                        hud.sendMessage("Normal Visibility: " + ship.visibility + " - Current Visibility: " + ship.getCurrentVisibility());
                        break;
                    case 119:
                        // show warp speeds for class; esc-w
                        hud.sendMessage("Max Turn Warp: " + ship.maxTurnWarp + "  Max Cruise Warp: " + ship.maxCruiseWarp + "  Max Damage Warp: " + ship.damageWarp + ".0");
                        break;
                    case 122:
                        // show cloak status; esc-z
                        if (ship.cloak) {
                            if (ship.cloakBurnt) {
                                hud.sendMessage("Your cloaking device has burnt out.");
                            } else {
                                hud.sendMessage("Your cloaking device is operating normally.");
                            }
                        } else {
                            hud.sendMessage("You have no cloaking device.");
                        }
                        break;
                    default:
                        sendText(TrekAnsi.clearRow(19, this));
                }

                inputstate = INPUT_NORMAL;
                command = 0;
                return;
            }
            /*
                *
                * NORMAL COMMANDS
                *
                */
            if (inputstate == INPUT_NORMAL) {
                switch (command) {
                    case 1: // CTRL-A - Admiral commands.
                        if (ship instanceof ShipQ) {
                            inputstate = INPUT_ADMIRALCOMMAND;
                            sendText(TrekAnsi.clearRow(19, this) + "Admiral command: ");
                        } else {
                            hud.sendMessage("'aj ra'? nuq 'aj ra'?");
                            // Klingon for "Admiral command? What admiral
                            // command?"
                        }
                        break;
                    case 3: // CTRL-C - Turn off damage control.
                        ship.damageControlOff();
                        break;
                    case 4: // CTRL-D - Set drone speed.
                        if (ship.variableSpeed) {
                            sendText(TrekAnsi.clearRow(2, this));
                            sendText("Speed: 1) 12.0 2) 11.0 3) 10.0 4) 9.0 5) 8.0 6) 7.0 7) 6.0 8) 5.0 9) 4.0");
                            inputstatenext = INPUT_DRONESPEED;
                        } else {
                            hud.sendMessage("You cannot change your drone speed.");
                            inputstatenext = INPUT_NORMAL;
                            inputstate = INPUT_NORMAL;
                        }
                        command = 0;
                        sendText(TrekAnsi.clearRow(19, this));
                        break;
                    case 5: // CTRL-E - Clear energy usage. (unloads weapons,
                        // stops movement, turns off dmg ctl)
                        ship.clearEnergyUse();
                        break;
                    case 9: // CTRL-I <ship>; Intercept ship.
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Which ship: ");
                        inputstate = INPUT_INTERCEPTSHIPNOSCAN;
                        break;
                    case 12: // CTRL-L; Lock weapons.
                        ship.lockWeapons();
                        break;
                    case 16: // CTRL-P; Unload phasers.
                        ship.unloadPhasers(5);
                        break;
                    case 18: // CTRL-R; Redraw screen.
                        drawHud(false);
                        break;
                    case 19: // Capture hold shield level. ^S
                        ship.holdShieldLevel();
                        break;
                    case 20: // Capture torpedo unload. ^T
                        ship.unloadTorpedo();
                        break;
                    case 21: // Capture Unlock weapons. ^U
                        ship.unlockWeapons();
                        break;
                    case 23: // Capture all stop. ^W
                        ship.setWarp(0);
                        break;
                    case 24: // Capture clearing of messages. ^X
                        hud.clearMessages();
                        break;
                    case 27: // ESC ; Escape commands.
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Command: ");
                        inputstate = INPUT_ESCAPECOMMAND;
                        break;
                    case 33: // ! ; Set heading.
                        //	allow modify heading as long as they aren't
                        // exceeding turn warp
                        if (!(Math.abs(ship.warpSpeed) > ship.maxCruiseWarp)) {
                            sendText(TrekAnsi.clearRow(19, this));
                            sendText("Heading (xxx'zz): ");
                            inputstate = INPUT_HEADING;
                        }
                        command = 0;
                        break;
                    case 35: // Capture player listing. #
                        hud.showPlayerPage++;
                        break;
                    case 36: // Gold beaming. $
                        ship.beamGold();
                        break;
                    case 37: // % - Tractor beam.
                        ship.doTractorBeam();
                        break;
                    case 38: // Ship stats. &
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Which ship? ");
                        inputstate = INPUT_SHIPSTATS;
                        break;
                    case 40: // ) ; Add macro.
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Map which key? ");
                        inputstate = INPUT_MACROLETTERADD;
                        break;
                    case 41: // ( ; Remove macro.
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Remove which macro? ");
                        command = 0;
                        inputstatenext = INPUT_MACROREMOVE;
                        break;
                    case 46: // Dock or orbit '.'
                        ship.dock();
                        break;
                    case 63: // ? - Help screens.
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Which help screen? [a-l]:");
                        inputstate = INPUT_HELPSCREEN;
                        break;
                    case 64: // Capture set warp. @
                        inputstate = INPUT_WARPAMOUNT;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Enter warp speed: ");
                        break;
                    case 67: // Capture damage control down. C
                        ship.decreaseDamageControl(5);
                        break;
                    case 68: // Self destruct. D
                        ship.toggleSelfDestruct();
                        break;
                    case 72: // Capture -5 to heading. H
                        ship.alterHeading(-5, 0);
                        break;
                    case 73: // Capture intercept coord. I
                        // allow intercepting coords as long as they aren't
                        // exceeding turn warp
                        if (!(Math.abs(ship.warpSpeed) > ship.maxCruiseWarp)) {
                            sendText(TrekAnsi.clearRow(19, this));
                            sendText("Intercept (X Y Z): ");
                            inputstate = INPUT_COORDINATES;
                        }
                        command = 0;
                        break;
                    case 74: // Capture -5 to picth. J
                        ship.alterHeading(0, -5);
                        break;
                    case 75: // Capture +5 to pitch. K
                        ship.alterHeading(0, 5);
                        break;
                    case 76: // Capture +5 to heading. L
                        ship.alterHeading(5, 0);
                        break;
                    case 77: // Drop a mine. M
                        ship.dropMine();
                        break;
                    case 79: // Capture Object scan. O
                        inputstate = INPUT_SCANLETTER;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Which object? ");
                        break;
                    case 80: // Capture phaser fire. P
                        ship.loadPhasers(5);
                        break;
                    case 81: // Capture quit. Q
                        inputstate = INPUT_QUIT;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Really quit? ");
                        break;
                    case 82: // Capture jam message command. R
                        inputstate = INPUT_SHIPLETTERJAM;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Block messages from what ship frequency (slot letter): ");
                        break;
                    case 83: // Capture shield down 10%. S
                        ship.lowerShields(10);
                        break;
                    case 84: // Capture torpedo load. T
                        ship.loadTorpedo();
                        break;
                    case 87: // Capture down of warp speed. W
                        ship.decreaseSpeed(-0.2d);
                        break;
                    case 88: // Capture clear scanner. X
                        ship.stopScanning();
                        break;
                    case 90: // Decloak. Z
                        ship.decloak();
                        break;
                    case 98: // Capture b for dropping a buoy.
                        ship.dropBuoy();
                        break;
                    case 91: // Capture maximum cruise warp. [ (reverse)
                        ship.setCruiseWarp(TrekShip.MOVEMENT_REVERSE);
                        break;
                    case 93: // Capture maximum cruise warp. ] (forward)
                        ship.setCruiseWarp(TrekShip.MOVEMENT_FORWARD);
                        break;
                    case 99: // Capture damage control up. c
                        ship.increaseDamageControl(5);
                        break;
                    case 100: // Fire a drone. d
                        ship.fireDrone();
                        break;
                    case 102: // Use misc device. f
                        inputstate = INPUT_DEVICELETTER;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Which device? ");
                        break;
                    case 104: // Capture -1 to heading. h
                        ship.alterHeading(-1, 0);
                        break;
                    case 105: // Capture intercept. i
                        ship.intercept();
                        break;
                    case 106: // Capture -1 to pitch. j
                        ship.alterHeading(0, -1);
                        break;
                    case 107: // Capture +1 to pitch. k
                        ship.alterHeading(0, 1);
                        break;
                    case 108: // Capture +1 to heading. l
                        ship.alterHeading(1, 0);
                        break;
                    case 109: // Capture sending a message. m
                        if (transmitterBurnt) {
                            hud.sendMessageBeep("You need to fix your transmitter.");
                        } else {
                            sendText(TrekAnsi.clearRow(19, this));
                            sendText("Which ship? ");
                            inputstate = INPUT_MESSAGETARGET;
                        }
                        command = 0;
                        break;
                    case 111: // Capture ship scan. o
                        inputstate = INPUT_SCANLETTERSHIP;
                        sendText(TrekAnsi.clearRow(19, this));
                        sendText("Which ship? ");
                        break;
                    case 112: // Capture phaser up. p
                        ship.firePhasers();
                        break;
                    case 114: // Ram the fucker. r
                        ship.ram();
                        break;
                    case 115: // Capture shield up. s
                        ship.raisingShields = true;
                        break;
                    case 116: // Capture torpedo fire. t
                        ship.fireTorpedoes();
                        break;
                    case 117: // Capture Unlock weapons. u
                        ship.unlockWeapons();
                        break;
                    case 119: // Capture up of warp speed. w
                        ship.increaseSpeed(0.2d);
                        break;
                    case 120: // Capture transwarp. x
                        ship.transwarpEngage();
                        break;
                    case 122: // Cloak. z
                        ship.cloak();
                        break;
                    case 123: // Capture turn reverse warp. {
                        ship.setTurnWarp(TrekShip.MOVEMENT_REVERSE);
                        break;
                    case 125: // Capture cruise forward warp. }
                        ship.setTurnWarp(TrekShip.MOVEMENT_FORWARD);
                        break;
                    case 126: // Vector debugging. ~
                        if (ship instanceof ShipQ) {
                            doAdmiralCommand("spawn bot [BotTHX]");
                        }
                        break;
                    default:
                        break;
                }
            } // end normal commands
        }

        if (inputstatenext != INPUT_NORMAL) {
            /*
                *
                * ****** CATCH INPUT STATE NEXT COMMANDS ******
                *
                */
            if (inputstatenext == INPUT_DRONESPEED) {
                if (command != 0) {
                    if (command < 49 || command > 57) {
                        sendText(TrekAnsi.clearRow(19, this));
                    } else {
                        switch (command) {
                            case 49:
                                ship.droneSpeed = 12;
                                break;
                            case 50:
                                ship.droneSpeed = 11;
                                break;
                            case 51:
                                ship.droneSpeed = 10;
                                break;
                            case 52:
                                ship.droneSpeed = 9;
                                break;
                            case 53:
                                ship.droneSpeed = 8;
                                break;
                            case 54:
                                ship.droneSpeed = 7;
                                break;
                            case 55:
                                ship.droneSpeed = 6;
                                break;
                            case 56:
                                ship.droneSpeed = 5;
                                break;
                            case 57:
                                ship.droneSpeed = 4;
                                break;
                        }
                        hud.sendMessage("Drone warp speed set to " + ship.droneSpeed + ".");
                    }
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            //	get letter of ship for ESC-^L and lock on the specific target if
            // in range.
            if (inputstatenext == INPUT_LOCKTARGET) {
                if (command != 0) {
                    if (TrekUtilities.isValidShipChar(command))
                        ship.lockWeaponsOnScanletter(new Character((char) command).toString());
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            if (inputstatenext == INPUT_MACROADD) {
                switch (command) {
                    case 3:
                        sendText(TrekAnsi.clearRow(19, this));
                        buffer = "";
                        chosenMacro = 0;
                        inputstatenext = INPUT_NORMAL;
                        break;
                    case 13:
                        sendText(TrekAnsi.clearRow(19, this));
                        setMacro(chosenMacro, buffer);
                        buffer = "";
                        chosenMacro = 0;
                        inputstatenext = INPUT_NORMAL;
                        break;
                    default:
                        if (command > 31 && command < 127) {
                            if (buffer.length() < 100) {
                                buffer = buffer + Character.toString((char) command);
                                sendText(TrekAnsi.locate(19, buffer.length() + 7, this) + new Character((char) command).toString());
                            }
                        }
                        if (isValidEraseCharacter(command)) {
                            try {
                                buffer = buffer.substring(0, buffer.length() - 1);
                            } catch (StringIndexOutOfBoundsException soobe) {
                                buffer = "";
                            }
                            sendText(TrekAnsi.locate(19, buffer.length() + 8, this) + TrekAnsi.eraseToEndOfLine(this));
                        }
                        break;
                }
                command = 0;
                return;
            }

            if (inputstatenext == INPUT_MACROREMOVE) {
                if (command > 0) {
                    removeMacro((char) command);
                    sendText(TrekAnsi.clearRow(19, this));
                    command = 0;
                    inputstatenext = INPUT_NORMAL;
                }
                return;
            }

            if (inputstatenext == INPUT_MESSAGE) {
                switch (command) {
                    case 3:
                        sendText(TrekAnsi.clearRow(19, this));
                        inputstatenext = INPUT_NORMAL;
                        buffer = "";
                        break;
                    case 13:
                        sendText(TrekAnsi.clearRow(19, this));
                        inputstatenext = INPUT_NORMAL;
                        if (!buffer.equals("")) {
                            switch (messageTarget) {
                                case TrekPlayer.MESSAGETARGET_ALL:
                                    TrekServer.sendMsgToAllPlayers(buffer, ship, true, true);
                                    break;
                                case TrekPlayer.MESSAGETARGET_CLOSEST:
                                    TrekServer.sendMsgToClosestPlayer(buffer, this, true, true);
                                    break;
                                case TrekPlayer.MESSAGETARGET_SHIP:
                                    TrekServer.sendMsgToPlayer(this.messageTargetShip, buffer, ship, true, true);
                                    break;
                                case TrekPlayer.MESSAGETARGET_RACE:
                                    TrekServer.sendMsgToRace(buffer, ship, ship.homePlanet, true, true);
                                    break;
                                case TrekPlayer.MESSAGETARGET_TEAM:
                                    TrekServer.sendMsgToTeam(buffer, ship, true, true);
                                    break;
                            }
                        }
                        if (messageSendCount > 3 && messageSendTimeout != 0) {
                            hud.sendMessageBeep("You have burnt out your radio transmitter!");
                            transmitterBurnt = true;
                        }
                        messageSendTimeout = 2;
                        buffer = "";
                        break;
                    default:
                        if (command >= 32 && command <= 126) {
                            if (buffer.length() < 75) {
                                buffer += Character.toString((char) command);
                                sendText(TrekAnsi.locate(19, buffer.length() + 2, this) + new Character((char) command).toString());
                                command = 0;
                            }
                        }
                        if (isValidEraseCharacter(command)) {
                            try {
                                buffer = buffer.substring(0, buffer.length() - 1);
                            } catch (StringIndexOutOfBoundsException siobe) {
                                buffer = "";
                            }
                            sendText(TrekAnsi.locate(19, buffer.length() + 3, this) + TrekAnsi.eraseToEndOfLine(this));
                        }
                        break;
                }
                inputstate = INPUT_NORMAL;
                command = 0;
                return;
            }

            if (inputstatenext == INPUT_OPTION) {
                switch (command) {
                    case 0:
                        break;
                    case 49:
                        playerOptions.incrementOption(TrekPlayerOptions.OPTION_BEEP);
                        break;
                    case 50:
                        playerOptions.incrementOption(TrekPlayerOptions.OPTION_ROSTER);
                        break;
                    case 51:
                        playerOptions.incrementOption(TrekPlayerOptions.OPTION_OBJECTRANGE);
                        break;
                    case 52:
                        playerOptions.incrementOption(TrekPlayerOptions.OPTION_BEARINGUPDATE);
                        break;
                    case 53:
                        playerOptions.incrementOption(TrekPlayerOptions.OPTION_RANGEUPDATE);
                        break;
                    case 54:
                        playerOptions.incrementOption(TrekPlayerOptions.OPTION_XYZUPDATE);
                        break;
                    case 55:
                        playerOptions.incrementOption(TrekPlayerOptions.OPTION_UNKNOWN);
                        break;
                    case 56:
                        playerOptions.incrementOption(TrekPlayerOptions.OPTION_DAMAGEREPORT);
                        break;
                    default:
                        hud.showOptionScreen = false;
                        hud.clearScanner();
                        inputstate = INPUT_NORMAL;
                        inputstatenext = INPUT_NORMAL;
                        command = 0;
                        break;
                }
                return;
            }

            if (inputstatenext == INPUT_PASSWORD) {
                switch (command) {
                    case 3:
                        sendText(TrekAnsi.clearRow(19, this));
                        inputstatenext = INPUT_NORMAL;
                        buffer = "";
                        break;
                    case 13:
                        if (passwordBuffer.equals("")) {
                            sendText(TrekAnsi.clearRow(19, this));
                            hud.sendMessage("You cannot have a blank password.");
                            buffer = "";
                            passwordBuffer = "";
                            inputstatenext = INPUT_NORMAL;
                        } else {
                            sendText(TrekAnsi.clearRow(19, this));
                            inputstatenext = INPUT_NORMAL;
                            // TODO: update the player password, based on the
                            // passwordBuffer value
                            if (quitAfterPassword) {
                                doSave();
                            } else {
                                hud.sendMessage("Password set.");
                                buffer = "";
                                passwordBuffer = "";
                            }
                        }
                        break;
                    default:
                        if (command >= 32 && command <= 126) {
                            buffer += "*";
                            passwordBuffer += Character.toString((char) command);
                            sendText(TrekAnsi.locate(19, buffer.length() + 19, this) + "*");
                            command = 0;
                        }
                        if (isValidEraseCharacter(command)) {
                            try {
                                buffer = buffer.substring(0, buffer.length() - 1);
                                passwordBuffer = passwordBuffer.substring(0, buffer.length() - 1);
                            } catch (StringIndexOutOfBoundsException siobe) {
                                buffer = "";
                                passwordBuffer = "";
                            }
                            sendText(TrekAnsi.locate(19, buffer.length() + 20, this) + TrekAnsi.eraseToEndOfLine(this));
                        }
                        break;
                }
                inputstate = INPUT_NORMAL;
                command = 0;
                return;
            }

            if (inputstatenext == INPUT_PHASERTYPE) {
                if (command != 0) {
                    if (command == 48 || command == 49) {
                        if (command == 48) {
                            this.ship.phaserType = TrekShip.PHASER_NORMAL;
                        }
                        if (command == 49) {
                            this.ship.phaserType = TrekShip.PHASER_TELEPORTER;
                        }
                    } else {
                        sendText(TrekAnsi.clearRow(19, this));
                    }
                    hud.sendMessage("Phaser type is now " + ship.getMtrekStylePhaserString() + ".");
                    command = 0;
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                }
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            // wait for a keystroke and return to normal HUD
            if (inputstatenext == INPUT_ROSTER) {
                if (command != 0) {
                    hud.disabled = false;
                    drawHud(false);
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
                return;
            }

            // get ship for ESC-L command
            if (inputstatenext == INPUT_SHIPLETTERDIRECTION) {
                if (command != 0) {
                    if (TrekUtilities.isValidShipChar(command))
                        ship.showShipLastCoord(new Character((char) command).toString());
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            // get ship for ESC-l command
            if (inputstatenext == INPUT_SHIPLETTERINTCOORD) {
                // CTRL+C
                if (command == 3) {
                    sendText(TrekAnsi.clearRow(19, this));
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
                if (command != 0) {
                    if (TrekUtilities.isValidShipChar(command))
                        ship.interceptShipLastCoord(new Character((char) command).toString());
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            //	get letter of ship for ESC-c command, and display score if valid
            // ship letter
            if (inputstatenext == INPUT_SHIPLETTERSCORE) {
                if (command != 0) {
                    if (TrekUtilities.isValidShipChar(command)) {
                        TrekShip targetShip = TrekServer.getPlayerShipByScanLetter(Character.toString((char) command));
                        if (targetShip == null)
                            inputstate = INPUT_NORMAL;
                        else
                            hud.showScore(targetShip.parent);
                    }
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            if (inputstatenext == INPUT_TORPEDOSPEED) {
                if (command != 0) {
                    if (command == 48 || command >= 53 && command <= 57) {
                        if (command == 48) {
                            ship.torpedoWarpSpeed = 10;
                        } else {
                            ship.torpedoWarpSpeed = command - 48;
                        }

                        this.ship.torpedoWarpSpeedAuto = false;
                    } else {
                        if (command == 97) {
                            this.ship.torpedoWarpSpeedAuto = true;
                        }
                    }

                    if (this.ship.torpedoWarpSpeedAuto) {
                        hud.sendMessage("Torpedo speed is set to auto.");
                    } else {
                        hud.sendMessage("Torpedo speed is set to warp " + this.ship.torpedoWarpSpeed + ".");
                    }

                    command = 0;
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                }

                sendText(TrekAnsi.clearRow(19, this));
                return;
            }

            if (inputstatenext == INPUT_TORPEDOTYPE) {
                if (command != 0) {
                    if (command == 48 || command == 49) {
                        if (command == 48) {
                            this.ship.torpedoType = TrekShip.TORPEDO_PLASMA;
                        }
                        if (command == 49) {
                            this.ship.torpedoType = TrekShip.TORPEDO_BOLTPLASMA;
                        }
                    } else {
                        sendText(TrekAnsi.clearRow(19, this));
                    }
                    hud.sendMessage("Torpedo type is set to " + ship.getMtrekStyleTorpString() + ".");
                    command = 0;
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                }
                sendText(TrekAnsi.clearRow(19, this));
                return;
            }
        }
    }

    protected boolean doLogin() {
        this.state = TrekPlayer.WAIT_LOGIN;
        TrekLog.logMessage("User is choosing name...");
        String validChars = " `!@#$%^&*()-=!@#$%^&*()_+[]\\{}|;:'\",./<>?1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String input = "";
        String shipNameChosen = "";
        String inputPrompt = "\r\nShip name: ";
        boolean passedCheck = true;
        boolean inputDone = false;
        do {
            passedCheck = true;
            input = "";
            shipNameChosen = "";
            inputDone = false;
            sendText(inputPrompt);
            do {
                //input = getCharacterInput(false, false, false);
                input = getBlockedInput(true, 60000, false);
                // If input is null, it's probably a socket error. Disconnect.
                if (input == null)
                    return false;
                if (!input.equals("")) {
                    switch (input.charAt(0)) {
                        case 3: // CTRL-C for disconnection.
                            return false;
                        case 13: // Capture enter.
                            inputDone = true;
                            break;
                        default: // Backspace and buffering.
                            if (isValidEraseCharacter(input.charAt(0))) {
                                if (shipNameChosen.length() > 0) {
                                    sendText(TrekAnsi.moveBackwards(1, this) + TrekAnsi.deleteCharacters(1, this));
                                    shipNameChosen = shipNameChosen.substring(0, shipNameChosen.length() - 1);
                                }
                            } else {
                                // limit to valid characters
                                if (validChars.indexOf(input) != -1) {
                                    sendText(input);
                                    shipNameChosen += input;
                                }
                            }
                            break;
                    }
                }
            }
            while (!inputDone);
            input = shipNameChosen.trim();
            if (input.indexOf("___ kill ship:") != -1) {
                laggedShipKiller(input);
                buffer = "";
                input = "";
                passedCheck = false;
            } else if (input.length() > 16) {
                sendText("\r\nShip name cannot be greater than 16 characters.\r\n");
                buffer = "";
                input = "";
                passedCheck = false;
            } else if (input.equals("")) {
                sendText("\r\nIllegal ship name.\r\n");
                buffer = "";
                input = "";
                passedCheck = false;
            } else if (TrekServer.shipAlreadyExists(input)) {
                sendText("\r\nShip name already in use.\r\n");
                buffer = "";
                input = "";
                passedCheck = false;
            } else if (input.equals("")) {
                sendText("\r\nPlease enter a ship name.\r\n");
                buffer = "";
                input = "";
                passedCheck = false;
            } else if (input.equalsIgnoreCase("who")) {
                printShipRoster(WHO_NORMAL);
                buffer = "";
                input = "";
                passedCheck = false;
            } else if (input.equalsIgnoreCase("who -l")) {
                printShipRoster(WHO_ADDRESS);
                buffer = "";
                input = "";
                passedCheck = false;
            } else if (input.equals("borg")) {
                sendText("\r\nReserved ship name; select another.\r\n");
                buffer = "";
                input = "";
                passedCheck = false;
            } else if (input.indexOf('~') != -1) {
                sendText("\r\nThe '~' character is reserved; please select a different ship name.\r\n");
                buffer = "";
                input = "";
                passedCheck = false;
            }
        }
        while (!passedCheck);
        TrekLog.logMessage("User chose ship name: " + input);
        // Set the thread name.
        super.setName("Thread-" + input);
        shipName = input;
        return true;
    }

    public boolean doPassword() {
        state = WAIT_PASSWORD;
        String input = "";
        String passwordChosen = "";
        String passwordEntered = "";
        boolean inputDone = false;
        boolean usePreservedPwd = false;
        boolean evaluatePwd = false;
        if (!dbInt.doesShipExist(shipName)) {
            sendText("\r\nOld Ship Password: ");
            usePreservedPwd = true;
        } else {
            sendText("\r\n Password: ");
        }
        // get the password
        do {
            //input = getCharacterInput(false, false, false);
            input = getBlockedInput(true, 60000, false);
            // If input is null, it's probably a socket error. Disconnect.
            if (input == null)
                return false;
            switch (input.charAt(0)) {
                case 3: // CTRL-C for disconnection.
                    return false;
                case 13: // Capture enter.
                    inputDone = true;
                    break;
                default: // Backspace and buffering.
                    if (isValidEraseCharacter(input.charAt(0))) {
                        if (passwordChosen.length() > 0) {
                            passwordChosen = passwordChosen.substring(0, passwordChosen.length() - 1);
                        }
                    } else {
                        passwordChosen += input;
                    }
                    break;
            }
        }
        while (!inputDone);
        passwordEntered = passwordChosen;
        if (usePreservedPwd) {
            evaluatePwd = dbInt.doesPreservedShipPasswordMatch(shipName, passwordEntered);
        } else {
            evaluatePwd = dbInt.doesShipPasswordMatch(shipName, passwordEntered);
        }
        // Are they the same, grasshopper?
        if (!evaluatePwd) {
            // GET THE HELL OUT OF HERE!! HaX0R!!
            TrekLog.logMessage("Invalid password attempt for " + shipName + "!");
            sendText("\r\nWrong password.\r\n");
            return false;
        }
        // if using a preserved ship, copy the player id into the new ship
        if (usePreservedPwd) {
            dbPlayerID = dbInt.getPreservedShipPlayer(shipName);
        } else { // using an existing ship
            dbPlayerID = dbInt.getPlayerID(shipName);
        }
        // Load the saved ship.
        dbInt.loadShipRecord(this);

        // display player specific login message
        String loginMessage = dbInt.getPlayerLoginMsg(dbPlayerID);
        if (loginMessage != null && !(loginMessage.equals(""))) {
            sendText("\r\n\r\n" + loginMessage.replaceAll("\n", "\r\n") + "\r\n");

            sendText("\r\nPress a key to proceed.");
            input = getBlockedInput(true, 60000, false);
        }

        state = WAIT_SUCCESSFULLOGIN;
        timeShipLogin = Calendar.getInstance().getTimeInMillis();
        //dbConnectionID = dbInt.createNewConnection(this);
        return true;
    }

    protected boolean doChoosePlayer() {
        state = WAIT_SPECIFYPLAYER;
        String input = "";
        String playerChosen = "";
        String playerEntered = "";
        boolean inputDone = false;
        String password1, password2;
        // boolean result;

        sendText("\r\n   Player: ");
        // get the player name
        do {
            //input = getCharacterInput(false, false,false);
            input = getBlockedInput(true, 60000, false);
            // If input is null, it's probably a socket error. Disconnect.
            if (input == null)
                return false;
            switch (input.charAt(0)) {
                case 3: // CTRL-C for abort.
                    return false;
                case 13: // Capture enter.
                    inputDone = true;
                    break;
                default: // Backspace and buffering.
                    if (isValidEraseCharacter(input.charAt(0))) {
                        if (playerChosen.length() > 0) {
                            sendText(TrekAnsi.moveBackwards(1, this) + TrekAnsi.deleteCharacters(1, this));
                            playerChosen = playerChosen.substring(0, playerChosen.length() - 1);
                        }
                    } else {
                        sendText(input);
                        playerChosen += input;
                    }
                    break;
            }
        }
        while (!inputDone);

        playerEntered = playerChosen;

        // does the player exist?
        if (dbInt.doesPlayerExist(playerEntered) == 0) {
            TrekLog.logMessage("Invalid player found for " + shipName + ", tried player: " + playerEntered);
            sendText("\r\nNew player!  Welcome to JTrek!\r\n");

            do {
                do {
                    input = "";
                    buffer = "";

                    sendText("New Password: ");
                    password1 = getBlockedInput(false, 60000, true);

                    if (password1 == null)
                        return false;
                    else if (password1.equals(""))
                        sendText("\r\nPassword cannot be blank!\r\n");
                    else
                        break;

                } while (true);

                do {
                    buffer = "";
                    input = "";

                    sendText("\r\nRe-enter password: ");
                    password2 = getBlockedInput(false, 60000, true);

                    if (password2 == null)
                        return false;
                    else if (password2.equals(""))
                        sendText("\r\nPasswords do not match!\r\n");
                    else
                        break;
                } while (true);

                if (!password1.equals(password2))
                    sendText("\r\nPasswords do not match!\r\n");
                else
                    break;
            } while (true);

            // Password matched, so create the player account here.
            boolean result = dbInt.saveNewPlayer(playerEntered, password1);
            TrekLog.logMessage("Create Player Result: " + result);

            input = "";
            buffer = "";

        } else {
            // verify the player password
            String passwordEntered = "";
            String passwordChosen = "";
            inputDone = false;
            sendText("\r\n Password: ");
            // get the password
            do {
                //input = getCharacterInput(false, false, false);
                input = getBlockedInput(true, 60000, false);
                // If input is null, it's probably a socket error. Disconnect.
                if (input == null)
                    return false;
                switch (input.charAt(0)) {
                    case 3: // CTRL-C to abort.
                        return false;
                    case 13: // Capture enter.
                        inputDone = true;
                        break;
                    default: // Backspace and buffering.
                        if (isValidEraseCharacter(input.charAt(0))) {
                            if (passwordChosen.length() > 0) {
                                passwordChosen = passwordChosen.substring(0, passwordChosen.length() - 1);
                            }
                        } else {
                            passwordChosen += input;
                        }
                        break;
                }
            }
            while (!inputDone);
            passwordEntered = passwordChosen;
            if (!dbInt.doesPlayerPasswordMatch(playerEntered, passwordEntered)) {
                TrekLog.logMessage("Invalid password attempt creating " + shipName + " with player " + playerEntered + "!");
                sendText("\r\nWrong password.\r\n");
                return false;
            }
        }

        dbPlayerID = dbInt.doesPlayerExist(playerEntered);

        state = WAIT_SHIPCHOICE;
        return true;
    }

    protected void printShipRoster(int whoListing) {
        int playerCount = 0;
        int humanPlyrCnt = 0;
        String output = "";
        sendText("\r\n\r\n");
        for (int x = 0; x < TrekServer.players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) TrekServer.players.elementAt(x);
            if (activePlayer == null)
                continue;
            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;
            if (activePlayer == this && activePlayer.ship == null)
                continue;
            switch (whoListing) {
                case WHO_NORMAL:
                    TrekHighScoreListing scoreListing = new TrekHighScoreListing(activePlayer, true);
                    output += scoreListing.listing + "\r\n";
                    break;
                case WHO_ADDRESS:
                    output += TrekUtilities.format(activePlayer.shipName, "", 17);
                    if (!activePlayer.playerHostName.equals(""))
                        if (TrekServer.isAnonymousPlayEnabled() && activePlayer.isAnonymous) {
                            output += "xxx.xxx.      xxxxxx";
                        } else {
                            output += activePlayer.playerHostName.substring(0, 8);
                            output += "      " + dbInt.getPlayerName(activePlayer.dbPlayerID);
                        }
                    else if (!(activePlayer instanceof TrekBot)) {
                        output += activePlayer.playerIP.substring(0, 8);
                        output += "      " + dbInt.getPlayerName(activePlayer.dbPlayerID);
                    } else {
                        if (activePlayer instanceof BotPlayer)
                            output += "xxx.xxx.      xxxxxx";  //make 'botplayers' appear like normal human players
                    }
                    output += "\r\n";
                    break;
                case WHO_INGAME:
                    output += "[" + activePlayer.ship.scanLetter + "] " + (new TrekHighScoreListing(activePlayer, true).listing) + "\r\n";
                    break;
                default:
                    output += "Unknown.";
                    break;
            }
            playerCount++;
            if (!(activePlayer instanceof TrekBot) || (activePlayer instanceof BotPlayer)) {  // want botplayers counted as humans again
                humanPlyrCnt++;
            }
        }
        if (playerCount != 0) {
            switch (whoListing) {
                case WHO_NORMAL:
                    sendText("Ship Name        Class      Gold DmgGiven Bonus DmgRcvd Cfl Brk Quadrant\r\n");
                    break;
                case WHO_INGAME:
                    sendText("Ltr Ship Name        Class      Gold DmgGiven Bonus DmgRcvd Cfl Brk Quadrant\r\n");
                    break;
                case WHO_ADDRESS:
                    sendText("Ship Name        Hostname      Player\r\n");
                    break;
                default:
                    sendText("Ship Name        Class      Gold DmgGiven Bonus DmgRcvd Cfl Brk Quadrant\r\n");
                    break;
            }
            sendText(output);
            sendText("\r\nActive Players: " + playerCount + " (" + humanPlyrCnt + ")\r\n\r\n");
            if (whoListing == WHO_INGAME) {
                sendText("\r\nPress a key to return to the game.\r\n* YOUR SHIP IS ACTIVE AND COULD BE UNDER ATTACK. *\r\n");
            }
        } else {
            sendText("There are no active players.\r\n\r\n");
        }
    }

    protected void doQuit() {
        this.state = WAIT_HSOVERALL;
        int goldAmount = 0;
        if (ship.gold > 0) {
            if (ship.gold <= 1000) {
                goldAmount = ship.gold;
            } else {
                goldAmount = new Double(((ship.gold - 1000) * .1) + 1000).intValue();
            }
            ship.currentQuadrant.addObject(new TrekGold(goldAmount, ship));
        }
        ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
        if (!(this instanceof TrekBot)) {
            dbInt.updateShipRecord(ship);
            dbInt.setShipDestroyed(ship.dbShipID);
        }
    }

    protected void doSecondUpdate() {
        try {
            if (this.ship == null || this.state != TrekPlayer.WAIT_PLAYING || hud == null) {
                return;
            }
            // Call the update on the ship.
            ship.doShipSecondUpdate();
            // Count down our messaging timeout.
            messageSendTimeout--;
            if (messageSendTimeout <= 0) {
                messageSendTimeout = 0;
                messageSendCount = 0;
            }
            // Clear messages.
            if (hud.messageWaiting) {
                hud.messageTimeout--;
                if (hud.messageTimeout <= 0) {
                    hud.messageWaiting = false;
                    hud.clearMessage(2);
                }
            }
            if (hud.topMessageWaiting) {
                hud.topMessageTimeout--;
                if (hud.topMessageTimeout <= 0) {
                    hud.topMessageWaiting = false;
                    hud.clearTopMessage();
                }
            }
            if (hud.talkMessageWaiting) {
                hud.talkMessagesTimeout--;
                if (hud.talkMessagesTimeout <= 0) {
                    hud.talkMessageWaiting = false;
                    hud.clearMessages();
                }
            }
            if (hud.damageRcvdWaiting) {
                hud.dmgRvdTimeout--;
                if (hud.dmgRvdTimeout <= 0) {
                    hud.damageRcvdWaiting = false;
                    hud.clearMessage(20);
                    hud.clearMessage(21);
                }
            }
            if (hud.loadMessageWaiting) {
                hud.loadMessageTimeout--;
                if (hud.loadMessageTimeout <= 0) {
                    hud.loadMessageWaiting = false;
                    hud.setLoadMessage("");
                }
            }
        } catch (Exception e) {
            TrekLog.logException(e);
            kill();
        }
    }

    protected void doTickUpdate() {
        try {
            // If there is no ship, don't do anything.
            if (ship == null || state != WAIT_PLAYING || hud == null) {
                return;
            }
            // Call the ship update.
            ship.doShipTickUpdate();
            // Update the Heads Up Display.
            hud.updateHud();
            // Clear the message queue.
            for (int x = 0; x < messageQueue.size(); x++) {
                String theMessage = (String) messageQueue.elementAt(x);
                this.sendMessage(theMessage);
            }
            messageQueue.clear();
        } catch (Exception e) {
            TrekLog.logException(e);
            kill();
        }

        sendOutputBuffer();

    }

    protected void drawHud(boolean firstDraw) {
        String buffer = "";
        NumberFormat numformat;
        numformat = NumberFormat.getInstance();
        numformat.setMaximumFractionDigits(1);
        numformat.setMinimumFractionDigits(1);
        //sendText(TrekAnsi.disableAutoWrap(this));
        sendText(TrekAnsi.clearScreen(this));
        buffer = shipName + " ";
        for (int x = buffer.length(); x < 21; x++) {
            buffer += "-";
        }
        sendText(TrekAnsi.locate(3, 1, this) + buffer + "+----------tactical display----------+\r\n");
        sendText(TrekAnsi.locate(4, 1, this) + "Warp Energy:         |" + TrekAnsi.locate(4, 59, this) + "|");
        sendText(TrekAnsi.locate(5, 1, this) + "Impulse Energy:      |" + TrekAnsi.locate(5, 59, this) + "|");
        sendText(TrekAnsi.locate(6, 1, this) + "Power Unused:        |" + TrekAnsi.locate(6, 59, this) + "|");
        sendText(TrekAnsi.locate(7, 1, this) + "Warp:                |" + TrekAnsi.locate(7, 59, this) + "|");
        sendText(TrekAnsi.locate(8, 1, this) + "Shields:           % |" + TrekAnsi.locate(8, 59, this) + "|");
        sendText(TrekAnsi.locate(9, 1, this) + "Ph:      Damage:     |" + TrekAnsi.locate(9, 59, this) + "|");
        sendText(TrekAnsi.locate(10, 1, this) + "Tp:      DmgCtl:     |" + TrekAnsi.locate(10, 59, this) + "|");
        sendText(TrekAnsi.locate(11, 1, this) + "Life Support:      % |" + TrekAnsi.locate(11, 59, this) + "|");
        sendText(TrekAnsi.locate(12, 1, this) + "Anti-Matter:         |" + TrekAnsi.locate(12, 59, this) + "|");
        sendText(TrekAnsi.locate(13, 1, this) + "X:                   |" + TrekAnsi.locate(13, 59, this) + "|");
        sendText(TrekAnsi.locate(14, 1, this) + "Y:                   |" + TrekAnsi.locate(14, 59, this) + "|");
        sendText(TrekAnsi.locate(15, 1, this) + "Z:        HD:        |" + TrekAnsi.locate(15, 59, this) + "|");
        sendText(TrekAnsi.locate(16, 1, this) + "---------------------+------------------------------------+");
        if (hud == null)
            System.out.println("HUD IS NULL!");
        hud.reset(firstDraw);
    }

    protected String getMacro(int thisLetter) {
        //    TrekLog.logMessage( "Looking up macro: " + thisLetter );
        String macro = (String) macros.get(new Integer(thisLetter).toString());
        if (macro != null) {
            return macro;
        } else {
            return "";
        }
    }

    protected void kill() {
        if (ship == null) {
            TrekLog.logMessage("kill(): ship was null");
            //handler.interrupt();
            TrekServer.removePlayer(this);
            return;
        }
        // 8/26: adding 'break save' traps - ship will be removed on
        // exceptions...
        if (!thisPlayerSaved) {
            thisPlayerSaved = true; // set true to abort future kill() runs
            TrekLog.logMessage("kill(): !thisPlayerSaved");
            if (ship.gold < 1000 || ship.damage > 50) {
                if (ship.gold < 1000) {
                    if (ship.gold > 0) {
                        ship.currentQuadrant.addObject(new TrekGold(ship.gold, ship));
                        TrekLog.logMessage(ship.name + " lost connection and was marked as 'dead'.  damage: " + ship.damage);
                    }
                } else {
                    int goldLeftBehind = 1000 + (int) Math.round(.10 * (ship.gold - 1000));
                    ship.currentQuadrant.addObject(new TrekGold(goldLeftBehind, ship));
                    TrekLog.logMessage(ship.name + " lost connection and was marked as 'dead'.  damage: " + ship.damage);
                }
                dbInt.updateShipRecord(ship);
                dbInt.setShipDestroyed(ship.dbShipID);
            } else {
                // saves the ship and reduces gold by 25%, also leaves 40% of
                // that gold behind as a chunk....
                ship.dockTarget = null;
                int goldLost = (int) Math.round(ship.gold * .25);
                int goldLeftBehind = (int) Math.round(goldLost * .40);
                TrekGold tg = new TrekGold(goldLeftBehind, ship);
                ship.currentQuadrant.addObject(tg);
                ship.breakSaves++;
                ship.gold -= goldLost;
                dbInt.updateShipRecord(ship);
                TrekLog.logMessage(ship.name + " lost connection and was saved.");
            }

            // to prevent problems when saving while observing a ship
            if (isObserving) {
                ShipQ thisShip = (ShipQ) ship;
                thisShip.observeShip.parent.theObservers.remove(ship.name);
                if (thisShip.observeShip.parent.theObservers.size() == 0) {
                    thisShip.observeShip.parent.isObserved = false;
                }

                isObserving = false;
            }

            TrekServer.removePlayer(this);
        }
        //TrekLog.logMessage("kill(): end of kill()");

    }

    protected void removeMacro(int thisLetter) {
        macros.remove(new Integer(thisLetter).toString());
        hud.sendMessage("Macro removed.");
    }

    public void run() {
        buffer = "";

        try {
            Thread.sleep(1000);

            this.sendTelnetNegotiationParameters();

            // Ouput Welcome Message...
            try {
                File loginMsg = new File("./welcome.txt");
                String tmpStr;
                BufferedReader br = new BufferedReader(new FileReader(loginMsg));
                while ((tmpStr = br.readLine()) != null) {
                    sendText(tmpStr + "\r\n");
                }
            } catch (FileNotFoundException fnfe) {
                TrekLog.logError("Missing welcome screen text file - welcome.txt");
                sendText("Welcome to JTrek\r\n");
            } catch (IOException ioe) {
                TrekLog.logException(ioe);
            } finally {
                sendText("Active Players: " + TrekServer.getNumberOfActivePlayers() + "   Uptime: " + TrekServer.getUptime() + "\r\n");
                if (TrekServer.isTeamPlayEnabled()) {
                    sendText("[  TEAM GAME IN PROGRESS ]\r\n");
                }
            }

            // If the server is full...
            if (TrekServer.players.size() >= 62) {
                sendText("We're sorry.  The server is full.  Try again later.\r\n");
                sendText("[Press enter to disconnect.]");
                //getCharacterInput(true, false, false);
                getBlockedInput(false, 60000, true);
                state = WAIT_HSCLASS;
                return;
            }

            int clientInput = 0;
            TrekLog.logMessage("Getting ship name.");

            do {
                // Reset the ship name.
                shipName = "";

                if (!doLogin()) {
                    TrekLog.logMessage("User disconnected.");
                    kill();
                    return;
                }

                if (dbInt.isShipPasswordProtected(shipName)) {
                    if (!doPassword()) {
                        TrekLog.logMessage("PASSWORD FAILURE: " + shipName + " failed password check.");
                    } else {
                        if (!dbInt.doesShipExist(shipName)) {
                            if (!doChooseShip()) {
                                TrekLog.logMessage("Failed getting ship class.");
                            } else {
                                ship.dbShipID = dbInt.getNewShipID(shipName);
                                timeShipLogin = Calendar.getInstance().getTimeInMillis();
                                dbConnectionID = dbInt.createNewConnection(this);
                            }
                        } else {
                            dbConnectionID = dbInt.createNewConnection(this);
                        }
                    }
                } else {
                    // new ship - establish which player it belongs to, and
                    // prompt for password confirmation before allowing them to
                    // select ship class
                    if (!doChoosePlayer()) {
                        TrekLog.logMessage("Failed to link new ship to player.");
                    } else {
                        if (!doChooseShip()) {
                            TrekLog.logMessage("Failed getting ship class.");
                        } else {
                            ship.dbShipID = dbInt.getNewShipID(shipName);
                            timeShipLogin = Calendar.getInstance().getTimeInMillis();
                            dbConnectionID = dbInt.createNewConnection(this);
                        }
                    }
                }

                if (state == WAIT_SOCKETTIMEDOUT) {
                    TrekLog.logMessage("Socket timeout. User disconnected.");
                    kill();
                    return;
                }

                if (state == WAIT_SOCKETERROR) {
                    TrekLog.logMessage("Socket error. User disconnected.");
                    kill();
                    return;
                }
            }
            while (state != WAIT_SUCCESSFULLOGIN);

            // Send the announcement message.
            long currentTime = Calendar.getInstance().getTimeInMillis();

            if ((ship.totalDamageGiven > 250000 || ship.gold > 50000) && (currentTime - ship.lastLogin) > 7200000)
                if (ship.dockTarget != null) {
                    TrekServer.sendAnnouncement(ship.dockTarget.name + ": " + getRankText(ship) + " " + ship.name + " has logged in.", true);
                }

            ship.lastLogin = currentTime;

            dbInt.updatePlayerLastLogin(this.dbPlayerID, ship.lastLogin);

            // check for player ban
            boolean isBanned = dbInt.isPlayerAccountLocked(dbPlayerID);

            // automatically assign to a team
            if (new String("acegikmoqsuwyACEGIKMOQSUWY").indexOf(ship.scanLetter) != -1) {
                teamNumber = 1;
            } else {
                teamNumber = 2;
            }

            // if team play; zap them to the Nu quadrant near their base
            if (TrekServer.isTeamPlayEnabled()) {
                ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
                ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get("Nu Quadrant");
                ship.currentQuadrant.addShip(ship);

                // put it near it's base
                Random gen = new Random();
                TrekObject baseObj = ship.currentQuadrant.getObjectByScanLetter(new Integer(teamNumber).toString());
                ship.point = new Trek3DPoint(baseObj.point.x + (gen.nextInt() % 300), baseObj.point.y + (gen.nextInt() % 300),
                        baseObj.point.z + (gen.nextInt() % 300));
            }

            if (!isBanned) {
                // set anonymous flag
                isAnonymous = dbInt.isPlayerAnonymous(dbPlayerID);

                // Draw the inital hud.
                TrekLog.logMessage("Drawing initial HUD.");
                drawHud(true);

                TrekLog.logMessage("Ship " + shipName + " is now active.");

                state = WAIT_PLAYING;

                ship.currentQuadrant.addShip(ship);
            } else {
                TrekLog.logMessage("BANNED - attempted login of " + ship.name + " - playerID: " + dbPlayerID);
                state = WAIT_HSCLASS;
            }

            // check for possible multiship
            for (Enumeration e = TrekServer.players.elements(); e.hasMoreElements(); ) {
                TrekPlayer curPlyr = (TrekPlayer) e.nextElement();

                if (curPlyr == this) continue;
                if (curPlyr.state != WAIT_PLAYING) continue;  // in case they're sitting at a HS list, etc.
                if (curPlyr.ship instanceof ShipQ) continue;  // admin

                if (curPlyr.playerIP.equals(this.playerIP)) {
                    // TODO: output possible multi to log
                    TrekLog.logMessage("--------------------    Possible Multi Report   --------------------");
                    String outputString = "*** Poss Multi: " + shipName + " from player " + dbPlayerID +
                            " and " + curPlyr.shipName + " from player " + curPlyr.dbPlayerID;
                    TrekLog.logMessage(outputString);
                    TrekServer.sendMsgToAdmins(outputString, ship.currentQuadrant.getObjectByScanLetter("1"), true, false);
                    TrekLog.logMessage("*** connectionIDs: " + dbConnectionID + " and " + curPlyr.dbConnectionID + ".");
                    TrekLog.logMessage("*** shipIDs: " + ship.dbShipID + " and " + curPlyr.ship.dbShipID + ".");
                    TrekLog.logMessage("*** current gold: " + ship.gold + " and " + curPlyr.ship.gold + ".");
                    TrekLog.logMessage("--------------------------------------------------------------------");
                }
            }

            while (state != WAIT_DEAD && state != WAIT_HSOVERALL && state != WAIT_HSCLASS) {
                try {
                    // Get the input from the client.
                    if (in.available() > 0) {
                        clientInput = in.read();
                        // If it is valid input.
                        if (clientInput != 0 && clientInput != 10) {
                            TrekLog.logDebug(
                                    shipName + ": Input received " + clientInput);
                            interpretCommand(clientInput);
                            clientInput = 0;
                        }
                    } else { // If we received no input from the client.
                        try {
                            int timeoutInput = 0;
                            try {
                                // Attempt to read from the socket for a period of 10 milliseconds.
                                socket.setSoTimeout(10);
                                timeoutInput = in.read();
                                if (timeoutInput == -1) {
                                    TrekLog.logMessage(
                                            shipName + " closed the session.");
                                    kill();
                                    break;
                                }
                                if (state == WAIT_DEAD) {
                                    clientInput = 0;
                                    break;
                                } else if (state == WAIT_HSOVERALL) {
                                    clientInput = 0;
                                    break;
                                } else if (state == WAIT_HSCLASS) {
                                    clientInput = 0;
                                    break;
                                }
                                if (timeoutInput != 0 && timeoutInput != 10) {
                                    interpretCommand(timeoutInput);
                                }
                            } catch (SocketTimeoutException ste) {
                                /* Ignore */
                            } catch (InterruptedIOException iioe) { /* Ignore */
                            } catch (Exception e) {
                                TrekLog.logException(e);
                                kill();
                                break;
                            }
                            // If the input is -1, then the client disconnected without quiting the game.
                            if (timeoutInput == -1) {
                                TrekLog.logMessage(
                                        shipName + " closed the session.");
                                kill();
                                break;
                            }
                            // Otherwise sleep the thread so we don't take up processing time.
                            Thread.sleep(50);
                        } catch (InterruptedException ie) {
                            TrekLog.logMessage(
                                    this.getName() + ": Interrupted by server.");
                            if (!serverShutdown)
                                kill();
                            break;
                        } catch (Exception ue) {
                            kill();
                            TrekLog.logException(ue);
                            break;
                        }
                    }
                } catch (java.io.IOException ioe) {
                    TrekLog.logMessage(
                            this.getName() + ": Interrupted by server.");
                    if (!serverShutdown)
                        kill();
                    break;
                } catch (Exception oe) {
                    TrekLog.logException(oe);
                    kill();
                    break;
                }
            }

            input = "";

            if (state == WAIT_DEAD) {
                doBoom();
                ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
                //input = getCharacterInput(false, false, false);
                input = getBlockedInput(false, 60000, true);
            } else {
                if (ship != null)
                    ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
            }

            timeShipLogout = Calendar.getInstance().getTimeInMillis();
            dbInt.updateConnection(this);

            if (state != TrekPlayer.WAIT_SOCKETERROR) {
                doHighScoresOverall();
                //input = getCharacterInput(false, false, false);
                input = getBlockedInput(false, 60000, true);

                doHighScoresClass();
                //input = getCharacterInput(false, false, false);
                input = getBlockedInput(false, 60000, true);

                doHighScoresFleet();
                input = getBlockedInput(false, 60000, true);

                sendText("\r\n");
            }

            //handler.interrupt();
            TrekServer.removePlayer(this);

            TrekLog.logMessage("Player " + shipName + " thread exiting.");
        } catch (Exception e) {
            TrekLog.logException(e);
            kill();
        }
    }

    protected void sendMessage(String thisMessage) {
        synchronized (this) {
            hud.sendTextMessage(thisMessage);
        }
    }

    /**
     * Sends a string to the Socket WITHOUT carriage returns.
     *
     * @param thisText The text you want to send.
     */
    protected void sendText(String thisText) {
        if (state == TrekPlayer.WAIT_PLAYING) addToOutputBuffer(thisText);
        else {
            //handler.sendData(thisText.getBytes());
            synchronized (this) {
                try {
                    thisText = thisText.replace('\010', '\000');
                    if (state != WAIT_PLAYING) {
                        Timer timeToDie = new Timer();

                        // if it's a bot, don't spawn new threads
                        if (this instanceof TrekBot) {
                            if (TrekServer.doBotHuds()) {
                                out.write(thisText.getBytes());
                                out.flush();
                            }

                            if (isObserved) {
                                for (Enumeration e = theObservers.elements(); e.hasMoreElements(); ) {
                                    TrekPlayer curObs = (TrekPlayer) e.nextElement();
                                    if (curObs == null) continue;
                                    if (curObs.state != TrekPlayer.WAIT_PLAYING) {
                                        theObservers.remove(curObs.ship.name);
                                        if (theObservers.size() == 0) isObserved = false;
                                        continue;
                                    }
                                    timeToDie = new Timer();
                                    //System.out.println("*** OBSERVED: " + ship.name + ", by: " + curObs.ship.name);

                                    timeToDie.schedule(new TrekDeadThreadKiller(curObs), 1000);

                                    curObs.out.write(outputBuffer.getBytes());
                                    curObs.out.flush();

                                    timeToDie.cancel();
                                }
                            }
                        } else {
                            if (!isObserving) {
                                timeToDie.schedule(new TrekDeadThreadKiller(this), 1000);

                                out.write(thisText.getBytes());
                                out.flush();

                                timeToDie.cancel();
                            }

                            if (isObserved) {
                                for (Enumeration e = theObservers.elements(); e.hasMoreElements(); ) {
                                    TrekPlayer curObs = (TrekPlayer) e.nextElement();
                                    if (curObs == null) continue;  // how to remove a null object from the hashtable?

                                    if (curObs.state != TrekPlayer.WAIT_PLAYING) {
                                        theObservers.remove(curObs.ship.name);
                                        if (theObservers.size() == 0) isObserved = false;
                                        continue;
                                    }

                                    timeToDie = new Timer();

                                    timeToDie.schedule(new TrekDeadThreadKiller(curObs), 1000);

                                    curObs.out.write(thisText.getBytes());
                                    curObs.out.flush();

                                    timeToDie.cancel();
                                }
                            }
                        }

                    } else {
                        if (playerOptions.getOption(TrekPlayerOptions.OPTION_BEEP) != 0) {
                            thisText = thisText.replace('\007', '\000');
                        }
                        addToOutputBuffer(thisText);
                    }
                } catch (java.net.SocketException se) {
                    TrekLog.logError(se.getMessage());
                    state = WAIT_SOCKETERROR;
                } catch (java.io.IOException ioe) {
                    TrekLog.logError(ioe.getMessage());
                    state = WAIT_SOCKETERROR;
                } catch (Exception e) {
                    TrekLog.logException(e);
                    state = WAIT_SOCKETERROR;
                }
            }
        }
    }

    protected void setMacro(int thisCharacter, String thisBody) {
        TrekLog.logMessage("Setting macro " + thisCharacter + " - " + thisBody);
        macros.put(new Integer(thisCharacter).toString(), thisBody);
        hud.sendMessage("Macro added.");
    }

    protected void doSave() {
        state = WAIT_HSOVERALL;
        // to prevent problems with Q's (and others?) not unsaving properly when they've done weird stuff
        if (ship.dockTarget != null) {
            if (!(ship.dockTarget.point.equals(ship.point)) ||
                    (ship.dockTarget != ship.currentQuadrant.getObjectByScanLetter(ship.dockTarget.scanLetter))) {
                ship.dockTarget = null;
                ship.docked = false;
            }
        }
        // to prevent problems when saving while observing a ship
        if (isObserving) {
            ShipQ thisShip = (ShipQ) ship;
            thisShip.observeShip.parent.theObservers.remove(ship.name);
            if (thisShip.observeShip.parent.theObservers.size() == 0) {
                thisShip.observeShip.parent.isObserved = false;
            }

            isObserving = false;
        }

        // to ensure cease fires get cleared if the player docks / saves at an angry base
        if (ship.dockTarget != null && ship.dockTarget.isAttacker(this.ship)) {
            ship.dockTarget.payBalance(this.ship);
        }

        // and make sure that escape pods are restored to their rightful class
        if (ship instanceof ShipEscapePod) {
            ShipEscapePod pod = (ShipEscapePod) ship;
            if (pod.oldShipClass != null && !pod.oldShipClass.equals(""))
                changeShipClass(((ShipEscapePod) ship).oldShipClass);
        }

        // and finally save the ship
        if (!(this instanceof BotPlayer)) dbInt.updateShipRecord(ship);
        thisPlayerSaved = true;
    }

    protected void doHighScoresOverall() {
        if (state == WAIT_SOCKETERROR)
            return;
        state = WAIT_HSOVERALL;
        for (int x = 0; x < 26; x++) {
            sendText("\r\n");
        }
        sendText(TrekAnsi.clearScreen(this));
        sendText(TrekAnsi.locate(1, 1, this));
        sendText("High Scores:\r\n");
        sendText("   Ship Name        Class      Gold DmgGiven Bonus DmgRcvd Cfl Brk Saved  SB\r\n");
        Vector overall = dbInt.getOverallHighScores(TrekJDBCInterface.ORDER_BY_GOLD);
        for (int x = 0; x < overall.size(); x++) {
            TrekHighScoreListing hs = (TrekHighScoreListing) overall.elementAt(x);
            sendText(hud.format("", "" + (x + 1) + " ", 3) + hs.listing + "\r\n");
        }
        sendText(TrekAnsi.locate(23, 1, this));
        sendText("[Press enter to continue.]");
    }

    protected void doHighScoresClass() {
        if (state == WAIT_SOCKETERROR)
            return;
        state = WAIT_HSCLASS;
        sendText(TrekAnsi.clearScreen(this));
        sendText(TrekAnsi.locate(1, 1, this));
        sendText(ship.fullClassName + " High Scores:\r\n");
        sendText("   Ship Name        Class      Gold DmgGiven Bonus DmgRcvd Cfl Brk Saved  SB\r\n");
        Vector overall = dbInt.getClassHighScores(ship.shipClass, TrekJDBCInterface.ORDER_BY_GOLD);
        for (int x = 0; x < overall.size(); x++) {
            TrekHighScoreListing hs = (TrekHighScoreListing) overall.elementAt(x);
            sendText(hud.format("", "" + (x + 1) + " ", 3) + hs.listing + "\r\n");
        }
        sendText(TrekAnsi.locate(23, 1, this));
        sendText("[Press enter to continue.]");
    }

    protected void doHighScoresFleet() {
        if (state == WAIT_SOCKETERROR)
            return;
        state = WAIT_HSFLEET;
        sendText(TrekAnsi.clearScreen(this));
        sendText(TrekAnsi.locate(1, 1, this));
        sendText("Your High Scores:\r\n");
        sendText("   Ship Name        Class      Gold DmgGiven Bonus DmgRcvd Cfl Brk Saved  SB\r\n");
        Vector overall = dbInt.getFleetHighScores(dbPlayerID, TrekJDBCInterface.ORDER_BY_GOLD);
        for (int x = 0; x < overall.size(); x++) {
            TrekHighScoreListing hs = (TrekHighScoreListing) overall.elementAt(x);
            sendText(hud.format("", "" + (x + 1) + " ", 3) + hs.listing + "\r\n");
        }
        sendText(TrekAnsi.locate(23, 1, this));
        sendText("[Press enter to continue.]");
    }

    protected boolean interpretCommand(int inputCharacter) {
        if (state != WAIT_PLAYING)
            return true;
        try {
            // Hehe, macros.. I rock...
            if (inputstate == INPUT_NORMAL && inputstatenext == INPUT_NORMAL) {
                String theMacro = getMacro(inputCharacter);
                if (!theMacro.equals("")) {
                    char[] commands = theMacro.toCharArray();
                    TrekLog.logDebug(this.shipName + ": Running macro: " + new String(commands).toString());
                    for (int m = 0; m < commands.length; m++) {
                        // If there is a backslash, then process the octal
                        // code.
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
                } else {
                    TrekLog.logDebug(shipName + ": Couldn't find macro. Executing command " + new Character((char) inputCharacter).toString());
                    doCommand(inputCharacter);
                }
            } else {
                TrekLog.logDebug(shipName + ": Failed macro check.  Running command: " + new Character((char) inputCharacter).toString());
                doCommand(inputCharacter);
            }
            return true;
        } catch (Exception e) {
            TrekLog.logError("interpretCommand Error!");
            TrekLog.logException(e);
            return false;
        }
    }

    protected void addToOutputBuffer(String thisOutput) {
        synchronized (this) {
            outputBuffer += thisOutput;
        }
    }

    protected void sendOutputBuffer() {
        synchronized (this) {
            try {
                if (!outputBuffer.equals("")) {
                    outputBuffer = outputBuffer.replace('\010', '\000');

                    Timer timeToDie = new Timer();

                    if (this instanceof TrekBot) {
                        if (TrekServer.doBotHuds()) {
                            out.write(outputBuffer.getBytes());
                            out.flush();
                        }

                        if (isObserved) {
                            for (Enumeration e = theObservers.elements(); e.hasMoreElements(); ) {
                                TrekPlayer curObs = (TrekPlayer) e.nextElement();
                                if (curObs == null) continue;
                                if (curObs.state != TrekPlayer.WAIT_PLAYING) {
                                    theObservers.remove(curObs.ship.name);
                                    if (theObservers.size() == 0) isObserved = false;
                                    continue;
                                }
                                timeToDie = new Timer();

                                timeToDie.schedule(new TrekDeadThreadKiller(curObs), 1000);

                                curObs.out.write(outputBuffer.getBytes());
                                curObs.out.flush();

                                timeToDie.cancel();
                            }
                        }
                    } else {
                        if (!isObserving) {
                            timeToDie.schedule(new TrekDeadThreadKiller(this), 1000);  // you have 1 second to comply, bitch

                            out.write(outputBuffer.getBytes());
                            out.flush();

                            // if you made it here in less than 1 second, you're still alive, otherwise... goodbye, you are the weakest link
                            timeToDie.cancel();
                        }

                        if (isObserved) {
                            for (Enumeration e = theObservers.elements(); e.hasMoreElements(); ) {
                                TrekPlayer curObs = (TrekPlayer) e.nextElement();
                                if (curObs == null) continue;
                                if (curObs.state != TrekPlayer.WAIT_PLAYING) {
                                    theObservers.remove(curObs.ship.name);
                                    if (theObservers.size() == 0) isObserved = false;
                                    continue;
                                }
                                timeToDie = new Timer();

                                timeToDie.schedule(new TrekDeadThreadKiller(curObs), 1000);

                                curObs.out.write(outputBuffer.getBytes());
                                curObs.out.flush();

                                timeToDie.cancel();
                            }
                        }
                    }
                }
            } catch (java.net.SocketException se) {
                TrekLog.logError(se.getMessage());
                state = WAIT_SOCKETERROR;
            } catch (java.io.IOException ioe) {
                TrekLog.logError(ioe.getMessage());
                state = WAIT_SOCKETERROR;
            } catch (Exception e) {
                TrekLog.logException(e);
                state = WAIT_SOCKETERROR;
            } finally {
                outputBuffer = "";
            }
        }
    }

    protected void doBoom() {
        if (ship.damage >= 200 || ship.irreversableDestruction) {
            Random gen = new Random();
            for (int x = 0; x < 100; x++) {
                int xPoint = Math.abs(gen.nextInt() % 78);
                int yPoint = Math.abs(gen.nextInt() % 23);
                sendText(TrekAnsi.locate(yPoint, xPoint, this));
                sendText("*");
            }
        } else {
            sendText(TrekAnsi.clearRow(22, this));
            sendText("Your crew has died.");
        }
        sendText(TrekAnsi.clearRow(23, this));
        sendText("[Press enter to continue.]");
        dbInt.updateShipRecord(ship);
        dbInt.setShipDestroyed(ship.dbShipID);
    }

    public static boolean isValidBackspaceCharacter(int x) {
        if (x == 8 || x == 127)
            return true;
        else
            return false;
    }

    public String getCurrentStateString() {
        switch (state) {
            case WAIT_LOGIN:
                return "LOGIN";
            case WAIT_PASSWORD:
                return "PASSWORD";
            case WAIT_PLAYING:
                return "PLAYING";
            case WAIT_SHIPCHOICE:
                return "SHIPCHOICE";
            case WAIT_SHIPNAME:
                return "SHIPNAME";
            case WAIT_DEAD:
                return "DEAD";
            case WAIT_HSOVERALL:
                return "HIGHSCORESOVERALL";
            case WAIT_HSCLASS:
                return "HIGHSCORESCLASS";
            case WAIT_SOCKETERROR:
                return "SOCKETERROR";
            default:
                return "** UNKNOWN";
        }
    }

    /**
     * Given a character, returns whether or not it is a valid erase character.
     *
     * @param thisChar The character to check.
     * @return boolean Whether or not it is a valied erase character.
     */
    public boolean isValidEraseCharacter(int thisChar) {
        if (thisChar == 8 || thisChar == 127)
            return true;
        return false;
    }

    /**
     * Given a character, returns whether or not it is valid alphanumeric input.
     *
     * @param thisChar The character to check.
     * @return boolean Whether or not it is a valid input character.
     */
    public boolean isValidInput(int thisChar) {
        if (thisChar >= 32 && thisChar <= 126)
            return true;
        return false;
    }

    /**
     * Performs a command from the Admirals command prompt.
     *
     * @param admiralCommand The command to perform.
     */
    public void doAdmiralCommand(String admiralCommand) {
        if (admiralCommand.equals(""))
            return;
        if (admiralCommand.indexOf("create gold") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc == -1) {
                ship.currentQuadrant.addObject(new TrekGold(100, ship));
            } else {
                int closingBrackLoc = admiralCommand.indexOf("]");
                if (closingBrackLoc != -1) {
                    String goldStr = admiralCommand.substring(brackLoc + 1, closingBrackLoc);
                    int goldAmt = new Integer(goldStr).intValue();
                    ship.currentQuadrant.addObject(new TrekGold(goldAmt, ship, ship.point.x + 100, ship.point.y + 100, ship.point.z + 100));
                } else {
                    hud.sendMessage("Poorly formed gold command, missing closing bracket: create gold [xxx]");
                }
            }
            return;
        }
        if (admiralCommand.equalsIgnoreCase("create ship debris with gold")) {
            ship.currentQuadrant.addObject(new TrekShipDebris(ship, 100));
            return;
        }
        if (admiralCommand.equalsIgnoreCase("autofix")) {
            ShipQ adminShip = (ShipQ) ship;
            if (adminShip.autoFix) {
                adminShip.autoFix = false;
                hud.sendMessage("AutoFix disabled.");
            } else {
                adminShip.autoFix = true;
                hud.sendMessage("AutoFix enabled.");
            }
            return;
        }
        if (admiralCommand.indexOf("show db data") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String dbShipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip dbShip = TrekServer.getPlayerShipByScanLetter(dbShipStr);
                if (dbShip != null) {
                    hud.sendMessage("For: " + dbShip.name + ", dbPlayerID: " + dbShip.parent.dbPlayerID + "   dbShipID: " + dbShip.dbShipID +
                            "   dbConnectionID: " + dbShip.parent.dbConnectionID);
                } else {
                    hud.sendMessage("Ship letter does not exist.");
                }
            }
            return;
        }
        // show xyz [c]
        if (admiralCommand.indexOf("show xyz") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String xyzShipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip xyzShip = TrekServer.getPlayerShipByScanLetter(xyzShipStr);
                if (xyzShip != null) {
                    hud.sendMessage("Ship (" + xyzShip.name + ") located at: " + xyzShip.point.toString());
                    // update the observed ship's coord in ship spotted database
                    if (ship.scannedHistory.containsKey(xyzShip.scanLetter)) {
                        ship.scannedHistory.remove(xyzShip.scanLetter);
                    }

                    ship.scannedHistory.put(xyzShip.scanLetter, new TrekCoordHistory(xyzShip.scanLetter, xyzShip.name, new Trek3DPoint(xyzShip.point)));

                } else {
                    hud.sendMessage("Ship letter does not exist.");
                }
            } else {
                hud.sendMessage("Poorly formed XYZ Admiral command, use: show xyz [shipletter]");
            }
            return;
        }
        if (admiralCommand.indexOf("move to") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String xyzShipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip xyzShip = TrekServer.getPlayerShipByScanLetter(xyzShipStr);
                if (xyzShip != null && xyzShip.currentQuadrant.name.equals(ship.currentQuadrant.name)) {
                    ship.point = new Trek3DPoint(xyzShip.point);
                } else {
                    hud.sendMessage("Ship letter does not exist.");
                }
            } else {
                hud.sendMessage("Poorly formed Move Admiral command, use: move to [shipletter]");
            }
            return;
        }
        if (admiralCommand.indexOf("change quad") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                char quadLtr = admiralCommand.substring(brackLoc + 1, brackLoc + 2).charAt(0);
                if ((quadLtr != 'a') && (quadLtr != 'b') && (quadLtr != 'd') && (quadLtr != 'g') && (quadLtr != 'o') && (quadLtr != 'n')) {
                    hud.sendMessage("Poorly formed Quad command, use: change quad [quadletter] <- a,b,d,g,o,n");
                } else {
                    ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);

                    String targetQuadrant = "";
                    switch (quadLtr) {
                        case 'a':
                            targetQuadrant = "Alpha Quadrant";
                            break;
                        case 'b':
                            targetQuadrant = "Beta Quadrant";
                            break;
                        case 'd':
                            targetQuadrant = "Delta Quadrant";
                            break;
                        case 'g':
                            targetQuadrant = "Gamma Quadrant";
                            break;
                        case 'o':
                            targetQuadrant = "Omega Quadrant";
                            break;
                        case 'n':
                            targetQuadrant = "Nu Quadrant";
                    }

                    ship.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(targetQuadrant);
                    ship.currentQuadrant.addShip(ship);
                }
            }
            return;
        }
        if (admiralCommand.equalsIgnoreCase("give all")) {
            ship.buoyTimeout = 0;
            ship.corbomite = true;
            ship.corbomiteTimeout = 0;
            ship.iridium = true;
            ship.lithium = true;
            ship.lithiumTimeout = 0;
            ship.magnabuoy = true;
            ship.magnabuoyTimeout = 0;
            ship.neutron = true;
            ship.seeker = true;
            ship.seekerTimeout = 0;
            return;
        }
        if (admiralCommand.indexOf("send msg:") != -1) {
            String msgContents = admiralCommand.substring(admiralCommand.indexOf(":") + 1, admiralCommand.length());
            TrekServer.sendAnnouncement(msgContents, true);
            return;
        }
        if (admiralCommand.indexOf("change name") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                int closingBrackLoc = admiralCommand.indexOf("]");
                if (closingBrackLoc != -1) {
                    String newName = admiralCommand.substring(brackLoc + 1, closingBrackLoc);
                    if (newName.length() <= 16) { // max characters per ship name
                        shipName = newName;
                        ship.name = newName;
                    } else {
                        hud.sendMessage("New name is too long!  Keep it at or under 16 characters.");
                    }

                } else {
                    hud.sendMessage("Poorly formed Change Name Admiral command, use: change name [newshipname]");
                }
            } else {
                hud.sendMessage("Poorly formed Change Name Admiral command, use: change name [newshipname]");
            }
            return;
        }

        if (admiralCommand.equals("toggle follow")) {
            ShipQ admiralShip = (ShipQ) ship;
            if (admiralShip.followTarget != null) {
                admiralShip.followTarget = null;
                hud.sendMessage("Following is off.");
            } else {
                if (admiralShip.scanTarget == null) {
                    hud.sendMessage("For the Follow Admiral command, you need to be scanning an object.");
                } else {
                    admiralShip.followTarget = admiralShip.scanTarget;
                    hud.sendMessage("Now following " + admiralShip.followTarget.name);
                }
            }
            return;
        }
        if (admiralCommand.indexOf("heal ship") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String xyzShipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip xyzShip = TrekServer.getPlayerShipByScanLetter(xyzShipStr);
                if (xyzShip != null) {
                    hud.sendMessage("Clearing " + xyzShip.name + "'s damage and restoring shields.");
                    xyzShip.damage = 0;
                    xyzShip.shields = 100;
                    xyzShip.lifeSupport = 100;
                    if (xyzShip.lifeSupportFailing)
                        xyzShip.lifeSupportFailing = false;

                } else {
                    hud.sendMessage("Ship letter does not exist.");
                }
            } else {
                hud.sendMessage("Poorly formed Heal Admiral command, use: heal ship [shipletter]");
            }
            return;
        }
        if (admiralCommand.indexOf("energize ship") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String xyzShipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip xyzShip = TrekServer.getPlayerShipByScanLetter(xyzShipStr);
                if (xyzShip != null) {
                    hud.sendMessage("Restoring " + xyzShip.name + "'s energy levels.");
                    xyzShip.warpEnergy = xyzShip.maxWarpEnergy;
                    xyzShip.impulseEnergy = xyzShip.maxImpulseEnergy;
                    xyzShip.currentCrystalCount = xyzShip.maxCrystalStorage;
                } else {
                    hud.sendMessage("Ship letter does not exist.");
                }
            } else {
                hud.sendMessage("Poorly formed Energize Admiral command, use: energize ship [shipletter]");
            }
            return;
        }
        if (admiralCommand.indexOf("bring ship") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String shipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip theShip = TrekServer.getPlayerShipByScanLetter(shipStr);
                if (theShip != null && theShip.currentQuadrant.name.equals(ship.currentQuadrant.name)) {
                    theShip.point = new Trek3DPoint(this.ship.point);
                    hud.sendMessage("Teleported ship " + theShip.name + " to current point.");
                } else {
                    hud.sendMessage("Ship letter does not exist.");
                }
            } else {
                hud.sendMessage("Poorly formed Move Admiral command, use: move to [shipletter]");
            }
            return;
        }
        if (admiralCommand.equals("tractor beam")) {
            ShipQ admiralShip = (ShipQ) ship;
            if (admiralShip.beamTarget != null) {
                admiralShip.beamTarget = null;
                hud.sendMessage("Deactivated tractor beam.");
            } else {
                if (admiralShip.scanTarget == null) {
                    hud.sendMessage("For the Tractor Beam Admiral command, you need to be scanning an object.");
                } else {
                    admiralShip.beamTarget = admiralShip.scanTarget;
                    hud.sendMessage("Locking Q tractor beam on " + admiralShip.beamTarget.name);
                }
            }
            return;
        }
        // TODO:possibly enhance command to get the bot name as well as class, thx currently defines it's name in the class though
        if (admiralCommand.indexOf("spawn bot") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                int closingBrackLoc = admiralCommand.indexOf("]");
                if (closingBrackLoc != -1) {
                    String botStr = admiralCommand.substring(brackLoc + 1, closingBrackLoc);
                    try {
                        String botString = "org.gamehost.jtrek.javatrek.bot." + botStr;
                        TrekBot botPlayer;
                        Class botClass;
                        Class[] constArgs = new Class[]{TrekServer.class, String.class};
                        Object[] actualArgs = new Object[]{TrekServer.getInstance(), "test"};
                        Constructor botConst;
                        try {
                            ClassLoader botLoader = new URLClassLoader(new URL[]{new File("lib/.").toURL()});
                            botClass = botLoader.loadClass(botString);
                            botConst = botClass.getConstructor(constArgs);
                            botPlayer = (TrekBot) botConst.newInstance(actualArgs);
                            TrekServer.addBot(botPlayer);
                            hud.sendMessage("Spawned new " + botStr + " bot.");
                        } catch (MalformedURLException mue) {
                            TrekLog.logException(mue);
                            hud.sendMessage("Failed to load new instance of bot class. MalformedURLException");
                        }
                    } catch (ClassNotFoundException cnfe) {
                        TrekLog.logException(cnfe);
                        hud.sendMessage("Failed to spawn new bot. ClassNotFoundException");
                    } catch (NoSuchMethodException nsme) {
                        TrekLog.logException(nsme);
                        hud.sendMessage("Failed to spawn new bot. NoSuchMethodException");
                    } catch (IllegalAccessException iae) {
                        TrekLog.logException(iae);
                        hud.sendMessage("Failed to spawn new bot. IllegalAccessException");
                    } catch (InstantiationException ie) {
                        TrekLog.logException(ie);
                        hud.sendMessage("Failed to spawn new bot. InstantiationException");
                    } catch (InvocationTargetException ite) {
                        TrekLog.logException(ite);
                        hud.sendMessage("Failed to spawn new bot. InvocationTargetException");
                    }
                } else {
                    hud.sendMessage("Poorly formed Bot Admiral command, use: spawn bot [classname]");
                }
            } else {
                hud.sendMessage("Poorly formed Bot Admiral command, use: spawn bot [classname]");
            }
            return;
        }
        if (admiralCommand.indexOf("kill ship") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String shipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip theShip = TrekServer.getPlayerShipByScanLetter(shipStr);
                if (theShip != null && theShip.currentQuadrant.name.equals(ship.currentQuadrant.name)) {
                    theShip.parent.kill();
                }
            } else {
                hud.sendMessage("Poorly formed Kill Admiral command, use: kill ship [shipletter]");
            }
            return;
        }
        if (admiralCommand.indexOf("save ship") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String shipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip theShip = TrekServer.getPlayerShipByScanLetter(shipStr);
                if (theShip != null) {
                    theShip.parent.doSave();
                }
            } else {
                hud.sendMessage("Poorly formed Save Admiral command, use: save ship [shipletter]");
            }
            return;
        }
        if (admiralCommand.equals("save all ships")) {
            for (int x = 0; x < 5; x++) {
                TrekQuadrant quadrant = (TrekQuadrant) TrekServer.quadrants.get("Alpha Quadrant");
                switch (x) {
                    case 0:
                        // set to alpha by default
                        //quadrant = (TrekQuadrant) TrekServer.quadrants.get("Alpha Quadrant");
                        break;
                    case 1:
                        quadrant = (TrekQuadrant) TrekServer.quadrants.get("Beta Quadrant");
                        break;
                    case 2:
                        quadrant = (TrekQuadrant) TrekServer.quadrants.get("Gamma Quadrant");
                        break;
                    case 3:
                        quadrant = (TrekQuadrant) TrekServer.quadrants.get("Omega Quadrant");
                        break;
                    case 4:
                        quadrant = (TrekQuadrant) TrekServer.quadrants.get("Nu Quadrant");
                }
                Vector curQuadShips = quadrant.getAllShips();
                for (Enumeration e = curQuadShips.elements(); e.hasMoreElements(); ) {
                    TrekShip theShip = (TrekShip) e.nextElement();
                    if (theShip != null) {
                        theShip.parent.doSave();
                    }
                }
            }
            return;
        }

        if (admiralCommand.indexOf("observe ship") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String shipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip theShip = TrekServer.getPlayerShipByScanLetter(shipStr);
                if (theShip != null) {
                    if (theShip.parent instanceof TrekRawDataInterface || ship.parent instanceof TrekRawDataInterface) {
                        hud.sendMessage("Cannot use observe with raw data streams.");
                    } else {
                        ShipQ thisShip = (ShipQ) ship;
                        if (isObserving) { // remove current observation
                            thisShip.observeShip.parent.theObservers.remove(ship.name);
                            if (thisShip.observeShip.parent.theObservers.size() == 0)
                                thisShip.observeShip.parent.isObserved = false;

                            isObserving = false;
                            thisShip.observeShip = null;
                        }
                        if (theShip == this.ship) {
                            // refresh screen
                            drawHud(false);
                        } else {
                            theShip.parent.theObservers.put(ship.name, this);
                            theShip.parent.isObserved = true;

                            isObserving = true;
                            thisShip.observeShip = theShip;
                            theShip.parent.drawHud(false);
                        }
                    }
                } else {
                    hud.sendMessage("Ship letter does not exist.");
                }
            }
            return;
        }

        if (admiralCommand.indexOf("send quad") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                char quadLtr = admiralCommand.substring(brackLoc + 1, brackLoc + 2).charAt(0);
                if ((quadLtr != 'a') && (quadLtr != 'b') && (quadLtr != 'd') && (quadLtr != 'g') && (quadLtr != 'o') && (quadLtr != 'n')) {
                    hud.sendMessage("Poorly formed Send Admiral command, use: send quad [quadletter,ship]");
                } else {
                    int commaLoc = admiralCommand.indexOf(",");
                    if (commaLoc != -1) {
                        String shipLtr = admiralCommand.substring(commaLoc + 1, commaLoc + 2);
                        TrekShip theShip = TrekServer.getPlayerShipByScanLetter(shipLtr);
                        if (theShip != null) {
                            theShip.currentQuadrant.removeShipByScanLetter(theShip.scanLetter);

                            String targetQuadrant = "";
                            switch (quadLtr) {
                                case 'a':
                                    targetQuadrant = "Alpha Quadrant";
                                    break;
                                case 'b':
                                    targetQuadrant = "Beta Quadrant";
                                    break;
                                case 'd':
                                    targetQuadrant = "Delta Quadrant";
                                    break;
                                case 'g':
                                    targetQuadrant = "Gamma Quadrant";
                                    break;
                                case 'o':
                                    targetQuadrant = "Omega Quadrant";
                                    break;
                                case 'n':
                                    targetQuadrant = "Nu Quadrant";
                            }

                            theShip.currentQuadrant = (TrekQuadrant) TrekServer.quadrants.get(targetQuadrant);
                            theShip.currentQuadrant.addShip(theShip);
                        } else {
                            hud.sendMessage("Ship letter does not exist.");
                        }
                    } else {
                        hud.sendMessage("Poorly formed Send Admiral command, use: send quad [quadletter,ship]");
                    }
                }
            } else {
                hud.sendMessage("Poorly formed Send Admiral command, use: send quad [quadletter,ship]");
            }
            return;
        }

        if (admiralCommand.indexOf("send obj") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String objLtr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekObject theObj = ship.currentQuadrant.getObjectByScanLetter(objLtr);
                if (theObj == null) {
                    hud.sendMessage("Object letter doesn't exist.");
                } else {
                    int commaLoc = admiralCommand.indexOf(",");
                    if (commaLoc != -1) {
                        String shipLtr = admiralCommand.substring(commaLoc + 1, commaLoc + 2);
                        TrekShip theShip = TrekServer.getPlayerShipByScanLetter(shipLtr);
                        if (theShip != null) {
                            theShip.point = new Trek3DPoint(theObj.point);
                        } else {
                            hud.sendMessage("Ship letter does not exist.");
                        }
                    } else {
                        hud.sendMessage("Poorly formed Send Admiral command, use: send obj [objletter,ship]");
                    }
                }
            } else {
                hud.sendMessage("Poorly formed Send Admiral command, use: send obj [objletter,ship]");
            }
            return;
        }

        if (admiralCommand.equals("toggle bots")) {
            if (TrekServer.isBotRespawnEnabled()) {
                TrekServer.setBotRespawnEnabled(false);

                hud.sendMessage("Server bot spawning is off.");
            } else {
                TrekServer.setBotRespawnEnabled(true);
                hud.sendMessage("Server bot spawning enabled.");
            }
            return;
        }

        if (admiralCommand.indexOf("set bot max") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                int closingBrackLoc = admiralCommand.indexOf("]");
                if (closingBrackLoc != -1) {
                    int numBots = new Integer(admiralCommand.substring(brackLoc + 1, closingBrackLoc)).intValue();
                    TrekServer.setMaxBots(numBots);
                    hud.sendMessage("Max bots set to " + numBots + ".");
                }
            }
            return;
        }

        if (admiralCommand.indexOf("set bot timeout") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                int closingBrackLoc = admiralCommand.indexOf("]");
                if (closingBrackLoc != -1) {
                    int reTime = new Integer(admiralCommand.substring(brackLoc + 1, closingBrackLoc)).intValue();
                    TrekServer.setBotRespawnTime(reTime);
                    hud.sendMessage("Respawn time set to " + reTime / 60 + " minutes.");
                }
            }
            return;
        }

        if (admiralCommand.indexOf("debug bot") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            String shipStr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
            TrekShip botShip = TrekServer.getPlayerShipByScanLetter(shipStr);
            if (botShip != null) {
                if (botShip.parent instanceof BotPlayer) {
                    BotPlayer theBot = (BotPlayer) botShip.parent;
                    hud.sendMessage("BOT: " + theBot.shipName + ", MODE: " + theBot.shipMode + ", SUB: " + theBot.shipSubMode + ", SUP: " +
                            theBot.supplyNeed + ", BP: " + theBot.botPeace + ", TM: " + theBot.botTeam + ", SK: " + theBot.skillLevel);
                }
            }
            return;
        }

        if (admiralCommand.indexOf("toggle bot debug") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String shipLtr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip botShip = TrekServer.getPlayerShipByScanLetter(shipLtr);
                if (botShip != null && botShip.parent instanceof BotPlayer) {
                    BotPlayer bp = (BotPlayer) botShip.parent;
                    bp.toggleDebug();

                    if (bp.getDebugStatus()) hud.sendMessage("Enabled bot debugging for " + botShip.name + ".");
                    else hud.sendMessage("Disabled bot debugging for " + botShip.name + ".");
                }
            }
            return;
        }

        if (admiralCommand.indexOf("set bot skill") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                int skillLevel = new Integer(admiralCommand.substring(brackLoc + 1, brackLoc + 2)).intValue();
                if (skillLevel < 1 || skillLevel > 5) {
                    hud.sendMessage("Valid levels are 1 - 5.");
                } else {
                    int commaLoc = admiralCommand.indexOf(",");
                    if (commaLoc != -1) {
                        String shipLtr = admiralCommand.substring(commaLoc + 1, commaLoc + 2);
                        TrekShip theShip = TrekServer.getPlayerShipByScanLetter(shipLtr);
                        if (theShip != null) {
                            if (theShip.parent instanceof BotPlayer) {
                                BotPlayer theBot = (BotPlayer) theShip.parent;
                                theBot.skillLevel = skillLevel;
                                hud.sendMessage("Set " + theBot.shipName + " to skill level " + skillLevel);
                            } else {
                                hud.sendMessage("Specified ship isn't a BotPlayer.");
                            }
                        } else {
                            hud.sendMessage("Ship letter does not exist.");
                        }
                    } else {
                        hud.sendMessage("Poorly formed bot skill command, use: set bot skill [skill,ship]");
                    }
                }
            } else {
                hud.sendMessage("Poorly formed bot skill command, use: set bot skill [skill,ship]");
            }
            return;
        }

        if (admiralCommand.equals("toggle teamplay")) {
            if (TrekServer.isTeamPlayEnabled()) {
                TrekServer.setTeamPlayEnabled(false);
                TrekServer.getQuadrantByName("Alpha Quadrant").clearFlag();
                //TrekServer.getQuadrantByName("Beta Quadrant").clearFlag();
                //TrekServer.getQuadrantByName("Gamma Quadrant").clearFlag();
                //TrekServer.getQuadrantByName("Omega Quadrant").clearFlag();

                hud.sendMessage("Teamplay is off.");
            } else {
                TrekServer.setTeamPlayEnabled(true);
                hud.sendMessage("Teamplay enabled.");
                // set nu flag enabled
                TrekServer.getQuadrantByName("Nu Quadrant").setFlag(new TrekFlag("Nu Quadrant", "f"));
            }
            return;
        }
        if (admiralCommand.indexOf("set team") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                int teamNum = new Integer(admiralCommand.substring(brackLoc + 1, brackLoc + 2)).intValue();
                if (teamNum < 0 || teamNum > 9) {
                    hud.sendMessage("Valid Teams are 1 - 9, or 0 for no team.");
                } else {
                    int commaLoc = admiralCommand.indexOf(",");
                    if (commaLoc != -1) {
                        String shipLtr = admiralCommand.substring(commaLoc + 1, commaLoc + 2);
                        TrekShip theShip = TrekServer.getPlayerShipByScanLetter(shipLtr);
                        if (theShip != null) {
                            theShip.parent.teamNumber = teamNum;
                        } else {
                            hud.sendMessage("Ship letter does not exist.");
                        }
                    } else {
                        hud.sendMessage("Poorly formed Set Team Admiral command, use: set team [teamnum,ship]");
                    }
                }
            } else {
                hud.sendMessage("Poorly formed Set Team Admiral command, use: set team [teamnum,ship]");
            }
            return;
        }

        if (admiralCommand.equals("toggle thx")) {
            TrekServer.toggleThx();
            if (TrekServer.isThxEnabled()) {
                hud.sendMessage("THX enabled.");
            } else {
                hud.sendMessage("THX disabled.");
            }
            return;
        }

        if (admiralCommand.indexOf("resupply ship") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String shipLtr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                TrekShip theShip = (TrekShip) TrekServer.getShipByScanLetter(shipLtr);
                if (theShip != null) {
                    theShip.torpedoCount = theShip.maxTorpedoStorage;
                    theShip.droneCount = theShip.maxDroneStorage;
                    theShip.mineCount = theShip.maxMineStorage;
                    theShip.antiMatter = 5000;
                    theShip.buoyTimeout = 0;
                    theShip.lithium = true;
                    theShip.lithiumTimeout = 0;
                    theShip.seeker = true;
                    theShip.seekerTimeout = 0;
                    theShip.corbomite = true;
                    theShip.corbomiteTimeout = 0;
                    theShip.iridium = true;
                    theShip.magnabuoy = true;
                    theShip.magnabuoyTimeout = 0;
                    theShip.neutron = true;
                    if (theShip.cloak) {
                        theShip.cloakTimeCurrent = theShip.cloakTime;
                        if (theShip.cloakBurnt)
                            theShip.cloakBurnt = false;
                    }
                }
            } else {
                hud.sendMessage("Poorly formed resupply command, use: resupply ship [shipLtr]");
            }
            return;
        }

        if (admiralCommand.indexOf("change class") != -1) {
            int brackLoc = admiralCommand.indexOf("[");
            if (brackLoc != -1) {
                String classLtr = admiralCommand.substring(brackLoc + 1, brackLoc + 2);
                final String validClasses = "abcdefghijklmnopqrstuvxyzAQ";
                if (validClasses.indexOf(classLtr) == -1) {
                    hud.sendMessage("Not a valid ship class: " + classLtr + ", valid are: " + validClasses);
                    return;
                }
                int commaLoc = admiralCommand.indexOf(",");
                if (commaLoc != -1) {
                    String shipLtr = admiralCommand.substring(commaLoc + 1, commaLoc + 2);
                    TrekShip oldShip = (TrekShip) TrekServer.getShipByScanLetter(shipLtr);
                    TrekPlayer thePlayer = oldShip.parent;
                    thePlayer.changeShipClass(classLtr);
                } else {
                    hud.sendMessage("Poorly formed change class cmd, use: change class [class,shipLtr]");
                }
            } else {
                hud.sendMessage("Poorly formed change class cmd, use: change class [class,shipLtr]");
            }
        }
        return;
    }

    protected void changeShipClass(String newClass) {
        int tmpDmgGvn = ship.damageGiven;
        int tmpBonus = ship.bonus;
        int tmpTotalDmgGvn = ship.totalDamageGiven;
        int tmpTotalDmgRcvd = ship.totalDamageReceived;
        int tmpTotalBonus = ship.totalBonus;
        int tmpBreakSaves = ship.breakSaves;
        int tmpGold = ship.gold;
        int tmpSecondsPlayed = ship.secondsPlayed;
        int tmpConflicts = ship.conflicts;
        int tmpTorpsFired = ship.torpsFired;
        int tmpMinesFired = ship.minesDropped;
        int tmpDronesFired = ship.dronesFired;
        double tmpUnits = ship.unitsTraveled;
        Trek3DPoint tmpPoint = new Trek3DPoint(ship.point);
        TrekQuadrant tmpQuad = ship.currentQuadrant;
        String tmpScanLetter = ship.scanLetter;
        int tmpShipID = ship.dbShipID;
        String tmpShipClassLetter = ship.classLetter;

        ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
        ship = null;

        TrekShip newShip = TrekUtilities.getShip(newClass, this);

        newShip.damageGiven = tmpDmgGvn;
        newShip.bonus = tmpBonus;
        newShip.totalDamageGiven = tmpTotalDmgGvn;
        newShip.totalDamageReceived = tmpTotalDmgRcvd;
        newShip.totalBonus = tmpTotalBonus;
        newShip.gold = tmpGold;
        newShip.secondsPlayed = tmpSecondsPlayed;
        newShip.conflicts = tmpConflicts;
        newShip.breakSaves = tmpBreakSaves;
        newShip.torpsFired = tmpTorpsFired;
        newShip.minesDropped = tmpMinesFired;
        newShip.dronesFired = tmpDronesFired;
        newShip.unitsTraveled = tmpUnits;
        newShip.point = tmpPoint;
        newShip.currentQuadrant = tmpQuad;
        newShip.scanLetter = tmpScanLetter;
        newShip.dbShipID = tmpShipID;

        ship = newShip;
        ship.currentQuadrant.addShip(ship);
        hud.reset(false);
        drawHud(false);

        // if an escape pod, start the shields @ 100, and positioned 500 units away from previous location
        if (newClass == "x") {
            ship.shields = 100;
            ship.raisingShields = true;
            Random r = new Random();
            ship.point.x = ship.point.x + (r.nextInt() % 500);
            ship.point.y = ship.point.y + (r.nextInt() % 500);
            ship.point.z = ship.point.z + (r.nextInt() % 500);
            ((ShipEscapePod) ship).oldShipClass = tmpShipClassLetter;
        }

        // if it's an excel, give them transwarp
        if (newClass == "b") {
            ship.transwarpTracking = true;
            ship.transwarp = true;
        }

        // if it's a con, give it cloak
        if (newClass == "a") {
            ship.cloak = true;
        }

        ship.resetInitialDirection();
    }

    public String getRankText(TrekShip thisShip) {
        if (thisShip.totalDamageGiven < 10000)
            return "Ensign";
        if (thisShip.totalDamageGiven < 20000)
            return "Lieutenant JG";
        if (thisShip.totalDamageGiven < 40000)
            return "Lieutenant";
        if (thisShip.totalDamageGiven < 80000)
            return "Lt. Commander";
        if (thisShip.totalDamageGiven < 200000)
            return "Commander";
        if (thisShip.totalDamageGiven < 400000)
            return "Captain";
        if (thisShip.totalDamageGiven < 1000000)
            return "Admiral";
        return "Fleet Admiral";
    }

    protected void laggedShipKiller(String killCommand) {
        String shipName = killCommand.substring(killCommand.indexOf(":") + 1);
        TrekPlayer dieShipDie = TrekServer.getPlayerShipByShipName(shipName).parent;
        dieShipDie.kill();
        try {
            dieShipDie.socket.close();
        } catch (IOException ioe) {
            TrekLog.logError(ioe.getMessage());
        }
    }

    protected String getBlockedInput(boolean echo, int timeout, boolean waitForCarriageReturn) {
        int x = 0;
        boolean telnetNegotiation = false;

        try {
            do {
                try {
                    // Wait for timeout milliseconds, before attempting to read again.
                    socket.setSoTimeout(timeout);

                    // Get input from the socket.
                    x = in.read();

                    if (x == 10)
                        x = 0;

                    // If the input is -1 then the socket is probably bad.
                    if (x == -1)
                        return null;

                    // If 255, then it's a telnet negotiation command.
                    // Telnet negotiation capture.
                    if (x == 255) {
                        TrekLog.logMessage("Received Telnet IAC...");
                        telnetNegotiation = true;
                    }

                    if (x < 240 && x > 48)
                        telnetNegotiation = false;

                    if (telnetNegotiation) {
                        char[] response = telnet.negotiate(x);
                        if (response.length == 3) {
                            if (response[0] != '\0') {
                                byte[] responseBytes = new byte[3];

                                for (int y = 0; y < response.length; y++) {
                                    responseBytes[y] = (byte) response[y];
                                }

                                out.write(responseBytes);
                                out.flush();
                            }

                            telnetNegotiation = false;
                        }

                        x = 0;
                    }

                    if (!waitForCarriageReturn && x != 0)
                        return new Character((char) x).toString();

                    if (bufferInput(x, echo))
                        break;

                } catch (SocketTimeoutException ste) {
                    state = WAIT_SOCKETTIMEDOUT;
                    TrekLog.logMessage("Socket timed out.  Disconnecting.");
                    return null;
                } catch (Exception e) {
                    TrekLog.logException(e);
                    return null;
                }

                if (x == -1) {
                    return null;
                }

                x = 0;

                Thread.sleep(50);
            }
            while (in.available() != -1);
            return buffer;

        } catch (SocketException se) {
            TrekLog.logError(
                    shipName + " Socket Exception: " + se.getMessage());
            kill();
            return null;
        } catch (IOException ioe) {
            TrekLog.logError(shipName + " IOException: " + ioe.getMessage());
            kill();
            return null;
        } catch (Exception re) {
            TrekLog.logException(re);
            return null;
        }
    }

    protected void sendTelnetNegotiationParameters() {
        try {
            byte[] commands = new byte[3];

            commands[0] = (byte) TrekTelnet.IAC;
            commands[1] = (byte) TrekTelnet.WILL;
            commands[2] = (byte) TrekTelnet.SUPPRESS_GOAHEAD;
            out.write(commands);

            commands[0] = (byte) TrekTelnet.IAC;
            commands[1] = (byte) TrekTelnet.WILL;
            commands[2] = (byte) TrekTelnet.ECHO;
            out.write(commands);

            commands[0] = (byte) TrekTelnet.IAC;
            commands[1] = (byte) TrekTelnet.DONT;
            commands[2] = (byte) TrekTelnet.LINEMODE;
            out.write(commands);

            // For that bitch ass MicroFUCKINGSOFT Telnet client... cocksuckers.
            commands[0] = (byte) TrekTelnet.IAC;
            commands[1] = (byte) TrekTelnet.WONT;
            commands[2] = (byte) TrekTelnet.TIMING_MARK;
            out.write(commands);
        } catch (Exception e) {
            TrekLog.logException(e);
            return;
        }
    }

}