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

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.*;
import java.util.zip.Deflater;

/**
 * The purpose of this class is to provide a socket connection that will feed the game data in a raw format to
 * game clients.  Instead of screen grabbing for data, a client will parse the data feed and extract the
 * needed information.
 */
public class TrekRawDataInterface extends TrekPlayer {
    private Deflater compressor;

    public TrekRawDataInterface(Socket sck, TrekServer serverIn) {
        TrekLog.logMessage("Creating Raw TrekPlayer thread...");
        server = serverIn;
        state = WAIT_LOGIN;
        outputBuffer = new String();
        macros = new Hashtable();
        playerOptions = new TrekPlayerOptions(this);
        shipName = "";
        dbInt = new TrekJDBCInterface();
        messageQueue = new Vector();
        compressor = new Deflater();

        try {
            socket = sck;
            socket.setTcpNoDelay(true);
            in = sck.getInputStream();
            //out = sck.getOutputStream();
            //dOut = new DeflaterOutputStream(out);
            //bOut = new BufferedOutputStream(sck.getOutputStream());
            //cOut = new CompressedOutputStream(sck.getOutputStream());
            out = new DataOutputStream(new BufferedOutputStream(sck.getOutputStream()));

            try {
                int slashIndex = sck.getInetAddress().toString().indexOf('/');
                playerIP = sck.getInetAddress().toString().substring(slashIndex + 1);
                playerHostName = InetAddress.getByName(playerIP).toString();
            } catch (Exception e) {
                TrekLog.logException(e);
            }
        } catch (IOException ioe) {
            TrekLog.logMessage("Could not create Raw TrekPlayer object for " + sck.toString() + "!!");
        }
    }

    protected String formatOption(String shipLetter, String shipClass) {
        return "<option id=\"" + shipLetter + "\" name=\"" + shipClass + "\" />";
    }

    protected void sendError(String errMsg) {
        sendText("<error>" + errMsg + "</error>");
    }

    protected boolean doChooseShip() {
        state = WAIT_SHIPCHOICE;
        try {
            // format to client in plain text
            TrekLog.logMessage("Raw data user is choosing a ship...");

            boolean disconnect = false;
            String input = "";
            boolean passedCheck = false;
            StringBuffer outputData = new StringBuffer();

            outputData.append("<dataRequest id=\"shipClass\">");
            outputData.append(formatOption("a", "Constitution II-A"));
            outputData.append(formatOption("b", "Excelsior"));
            outputData.append(formatOption("c", "Larson"));
            outputData.append(formatOption("d", "Freighter DY-600"));
            outputData.append(formatOption("e", "Romulan Bird of Prey"));
            outputData.append(formatOption("f", "Klinogn EV-12"));
            outputData.append(formatOption("g", "Klingon PB-13"));
            outputData.append(formatOption("h", "Orion BR-5"));
            outputData.append(formatOption("i", "Orion BR-1000"));
            outputData.append(formatOption("j", "Gorn CL-13"));
            outputData.append(formatOption("k", "Gorn CV-97"));
            outputData.append(formatOption("l", "Cardassian CDA-180"));
            outputData.append(formatOption("m", "Cardassian CDA-120"));
            outputData.append(formatOption("n", "Romulan Warbird"));
            outputData.append(formatOption("o", "Klingon BOP"));
            outputData.append(formatOption("p", "Cardassian Galor"));
            outputData.append(formatOption("q", "Orion BR-2000"));
            outputData.append(formatOption("r", "Gorn Scout"));
            outputData.append(formatOption("s", "Romulan Interceptor"));
            outputData.append(formatOption("t", "Federation Defiant"));
            outputData.append(formatOption("u", "Ferengi"));
            outputData.append(formatOption("v", "Klingon D-10"));
            outputData.append("</dataRequest>");

            sendText(outputData.toString());

            do {
                if (disconnect)
                    return false;
                do {
                    // get user ship selection
                    input = getBlockedInput(false, 60000, false);
                    if (input == null) {
                        disconnect = true;
                        break;
                    }
                    if (input.charAt(0) == 3)  // ctrl-c abort
                        return false;
                }
                while (input.equals(""));

                if (disconnect)
                    return false;

                // in case data had CRLF appended
                input = input.replaceAll("\r", "");
                input = input.replaceAll("\n", "");
                // and trim off any spacing
                input = input.trim();
                // and force to lower case
                input = input.toLowerCase();
                if (input.equals("a") || input.equals("b") || input.equals("c") || input.equals("d") || input.equals("e") || input.equals("f") || input.equals("g") || input.equals("h") || input.equals("i") || input.equals("j") || input.equals("k") || input.equals("l") || input.equals("m") || input.equals("n") || input.equals("o") || input.equals("p") || input.equals("q") || input.equals("r") || input.equals("s") || input.equals("t") || input.equals("u") || input.equals("v")) {
                    // chose valid ship letter
                    ship = TrekUtilities.getShip(input, this);
                    ship.setInitialDirection();

                    // TODO: update the hud class to be able to generate 'raw' output
                    //hud = new TrekHud(this);
                    hud = new TrekRawHud(this);

                    passedCheck = true;
                    dbInt.loadTemplateKeymaps(this);
                    dbInt.saveNewShipRecord(this.ship);
                } else {
                    sendError("Illegal ship class.");
                    passedCheck = false;
                }
            }
            while (!passedCheck);
            TrekLog.logMessage("Raw data user finally chose a ship.");

            // display player specific login message
            String loginMessage = dbInt.getPlayerLoginMsg(dbPlayerID);
            if (loginMessage != null && !(loginMessage.equals(""))) {
                sendText("<loginMsg>" + loginMessage.replaceAll("\n", "\r\n") + "</loginMsg>");
                Thread.sleep(250);
                sendText("<dataRequest id=\"loginMsgPause\" />");
                input = getBlockedInput(false, 60000, false);
            }

            state = WAIT_SUCCESSFULLOGIN;
            return true;
        } catch (Exception e) {
            TrekLog.logException(e);
            return false;
        }
    }

    protected void doBoom() {
        if (ship.damage >= 200 || ship.irreversableDestruction) {
            sendText("<death id=\"boom\" />");
        } else {
            sendText("<death id=\"lifesupport\" />");
        }

        dbInt.updateShipRecord(ship);
        dbInt.setShipDestroyed(ship.dbShipID);
    }

    protected void drawHud(boolean firstDraw) {
        // shouldn't need to draw any initial HUD 'outline'; that's the client's job
    }

    protected boolean doChoosePlayer() {
        state = WAIT_SPECIFYPLAYER;
        buffer = "";

        String input = "";
        String playerName = "";

        sendText("<dataRequest id=\"player\" />");

        // get the player name
        input = getBlockedInput(false, 60000, true);

        // If input is null, it's probably a socket error. Disconnect.
        if (input == null || input.length() == 0)
            return false;

        if (input.charAt(0) == 3) {
            // CTRL-C for abort.
            return false;
        }

        // in case the client returns the name w/ CRLF
        input = input.replaceAll("\r", "");
        input = input.replaceAll("\n", "");

        // trim any trailing spaces
        input = input.trim();

        playerName = input;

        // does the player exist?
        if (dbInt.doesPlayerExist(playerName) == 0) {
            TrekLog.logMessage("Invalid player found for " + shipName + ", tried player: " + playerName);
            sendError("Player not found.");
            return false;
        }

        // verify the player password
        String password = "";
        sendText("<dataRequest id=\"password\" />");

        // get the password
        buffer = "";
        input = getBlockedInput(false, 60000, true);

        // If input is null, it's probably a socket error. Disconnect.
        if (input == null || input.length() == 0)
            return false;

        if (input.charAt(0) == 3) {
            // CTRL-C for abort.
            return false;
        }

        // in case the client returns the password w/ CRLF
        input = input.replaceAll("\r", "");
        input = input.replaceAll("\n", "");

        // trim any trailing spaces
        input = input.trim();

        password = input;

        if (!dbInt.doesPlayerPasswordMatch(playerName, password)) {
            TrekLog.logMessage("Invalid password attempt creating " + shipName + " with player " + playerName + "!");
            sendError("Wrong password.");
            return false;
        }
        dbPlayerID = dbInt.doesPlayerExist(playerName);
        state = WAIT_SHIPCHOICE;
        buffer = "";
        return true;
    }

    protected boolean doLogin() {
        state = TrekPlayer.WAIT_LOGIN;
        buffer = "";
        String validChars = " `!@#$%^&*()-=!@#$%^&*()_+[]\\{}|;:'\",./<>?1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

        TrekLog.logMessage("Raw data user is choosing name...");

        String input = "";
        String inputPrompt = "<dataRequest id=\"shipName\" />";
        boolean passedCheck = true;

        do {
            passedCheck = true;
            input = "";

            sendText(inputPrompt);

            // get ship name
            input = getBlockedInput(false, 60000, true);

            // If input is null, it's probably a socket error. Disconnect.
            if (input == null || input.length() == 0)
                return false;

            if (input.charAt(0) == 3) {
                // ctrl-c abort
                return false;
            }

            // trim off any CRLF that client may have inadvertantly sent
            input = input.replaceAll("\r", "");
            input = input.replaceAll("\n", "");

            // and trim trailing white space
            input = input.trim();
            StringBuffer inputSB = new StringBuffer(input);
            for (int i = 0; i < inputSB.length(); i++) {
                if (validChars.indexOf(new Character(inputSB.charAt(i)).toString()) == -1) {
                    inputSB.setCharAt(i, '_');
                }
            }
            input = inputSB.toString();

            if (input.length() > 16) {
                sendError("Ship name cannot be greater than 16 characters.");
                passedCheck = false;
            } else if (input.equals("")) {
                sendError("Illegal ship name.");
                passedCheck = false;
            } else if (TrekServer.shipAlreadyExists(input)) {
                sendError("Ship name already in use.");
                passedCheck = false;
            }
            // TODO: determine whether to allow who & who -l via this interface
            else if (input.equalsIgnoreCase("who")) {
                printShipRoster(WHO_NORMAL);
                passedCheck = false;
            } else if (input.equalsIgnoreCase("who -l")) {
                printShipRoster(WHO_ADDRESS);
                passedCheck = false;
            } else if (input.equals("borg")) {
                sendError("Reserved ship name; select another.");
                passedCheck = false;
            } else if (input.indexOf('~') != -1) {
                sendError("The '~' character is reserved; please select a different ship name.");
                passedCheck = false;
            }
        } while (!passedCheck);

        TrekLog.logMessage("Raw data user chose ship name: " + input);

        // Set the thread name.
        super.setName("Thread-" + input);
        shipName = input;
        return true;
    }

    public boolean doPassword() {
        state = WAIT_PASSWORD;
        buffer = "";

        String input = "";
        String password = "";

        boolean usePreservedPwd = false;
        boolean evaluatePwd = false;

        if (!dbInt.doesShipExist(shipName)) {
            sendText("<dataRequest id=\"oldPassword\" />");
            usePreservedPwd = true;
        } else {
            sendText("<dataRequest id=\"password\" />");
        }

        // get the password
        input = getBlockedInput(false, 60000, true);

        // If input is null, it's probably a socket error. Disconnect.
        if (input == null || input.length() == 0)
            return false;

        if (input.charAt(0) == 3) {
            // ctrl-c for abort
            return false;
        }

        // in case the client returns the password w/ CRLF
        input = input.replaceAll("\r", "");
        input = input.replaceAll("\n", "");

        // trim any trailing spaces
        input = input.trim();

        password = input;

        if (usePreservedPwd) {
            evaluatePwd = dbInt.doesPreservedShipPasswordMatch(shipName, password);
        } else {
            evaluatePwd = dbInt.doesShipPasswordMatch(shipName, password);
        }

        // check for password match
        if (!evaluatePwd) {
            // password does not match
            TrekLog.logMessage("Invalid password attempt for " + shipName + "!");
            sendError("Wrong password.");
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
            sendText("<loginMsg>" + loginMessage.replaceAll("\n", "\r\n") + "</loginMsg>");
            sendText("<dataRequest id=\"loginMsgPause\" />");
            input = getBlockedInput(false, 60000, false);
        }

        state = WAIT_SUCCESSFULLOGIN;
        timeShipLogin = Calendar.getInstance().getTimeInMillis();
        buffer = "";
        input = "";

        return true;
    }

    protected void printShipRoster(int whoListing) {
        int playerCount = 0;
        int humanPlyrCnt = 0;
        StringBuffer plyrOutput = new StringBuffer();
        StringBuffer rosterOutput = new StringBuffer();

        for (int x = 0; x < TrekServer.players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) TrekServer.players.elementAt(x);
            if (activePlayer == null)
                continue;
            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;
            if (activePlayer == this && activePlayer.ship == null)
                continue;

            // regardless of the who listing type, we'll give them the same data, and the client can pick and choose what to display
            plyrOutput.append("<ship id=\"" + playerCount + "\">");
            plyrOutput.append("<name>" + activePlayer.shipName + "</name>");
            plyrOutput.append("<class>"
                    + (activePlayer.ship.shipClass.equals("INTERCEPTOR") ? "INTRCPTR" : activePlayer.ship.shipClass)
                    + "</class>");
            plyrOutput.append("<slot>" + activePlayer.ship.scanLetter + "</slot>");
            plyrOutput.append("<gold>" + activePlayer.ship.gold + "</gold>");
            plyrOutput.append("<dmggvn>" + activePlayer.ship.totalDamageGiven + "</dmggvn");
            plyrOutput.append("<dmgrcvd>" + activePlayer.ship.totalDamageReceived + "</dmgrcvd>");
            plyrOutput.append("<bonus>" + activePlayer.ship.totalBonus + "</bonus>");
            plyrOutput.append("<cfl>" + activePlayer.ship.conflicts + "</cfl>");
            plyrOutput.append("<breaks>" + activePlayer.ship.breakSaves + "</breaks>");
            plyrOutput.append("<quadrant>" + activePlayer.ship.currentQuadrant.name + "</quadrant>");
            plyrOutput.append("<ip>" +
                    ((!(activePlayer instanceof TrekBot)) ? activePlayer.playerHostName.substring(0, 7) : "BotShip") +
                    "</ip>");
            plyrOutput.append("</ship>");

            playerCount++;
            if (!(activePlayer instanceof TrekBot)) {
                humanPlyrCnt++;
            }
        }

        rosterOutput.append("<wholist>");
        rosterOutput.append("<playercount>" + playerCount + "</playercount>");
        rosterOutput.append("<humancount>" + humanPlyrCnt + "</humancount>");
        rosterOutput.append("<ships>");
        rosterOutput.append(plyrOutput);
        rosterOutput.append("</ships>");
        rosterOutput.append("</wholist>");

        sendText(rosterOutput.toString());
    }

    protected void doHighScoresOverall() {
        if (state == WAIT_SOCKETERROR)
            return;

        state = WAIT_HSOVERALL;

        StringBuffer outputScores = new StringBuffer();

        outputScores.append("<scores id=\"global\">");

        //sendText("   Ship Name        Class      Gold DmgGiven Bonus DmgRcvd Cfl Brk Saved  SB\r\n");
        Vector overall = dbInt.getOverallHighScores(TrekJDBCInterface.ORDER_BY_GOLD);
        for (int x = 0; x < overall.size(); x++) {
            TrekHighScoreListing hs = (TrekHighScoreListing) overall.elementAt(x);
            outputScores.append("<score id=\"" + (x + 1) + "\">");
            outputScores.append("<ship>" + hs.name + "</ship>");
            outputScores.append("<class>" + hs.className + "</class>");
            outputScores.append("<gold>" + hs.gold + "</gold>");
            outputScores.append("<dmggvn>" + hs.dmgGiven + "</dmggvn>");
            outputScores.append("<bonus>" + hs.bonus + "</bonus>");
            outputScores.append("<dmgrcvd>" + hs.dmgReceived + "</dmgrcvd>");
            outputScores.append("<cfl>" + hs.conflicts + "</cfl>");
            outputScores.append("<brk>" + hs.breakSaves + "</brk>");
            outputScores.append("<saved>" + hs.saveDate + "</saved>");
            outputScores.append("<base>" + hs.starbaseLetter + "</base>");
            outputScores.append("</score>");
        }

        outputScores.append("</scores>");
        sendText(outputScores.toString());

        sendText("<dataRequest id=\"mainScorePause\" />");
    }

    protected void doHighScoresClass() {
        if (state == WAIT_SOCKETERROR)
            return;

        state = WAIT_HSCLASS;
        StringBuffer outputScores = new StringBuffer();

        outputScores.append("<scores id=\"class\">");

        Vector overall = dbInt.getClassHighScores(ship.shipClass, TrekJDBCInterface.ORDER_BY_GOLD);
        for (int x = 0; x < overall.size(); x++) {
            TrekHighScoreListing hs = (TrekHighScoreListing) overall.elementAt(x);
            outputScores.append("<score id=\"" + (x + 1) + "\">");
            outputScores.append("<ship>" + hs.name + "</ship>");
            outputScores.append("<class>" + hs.className + "</class>");
            outputScores.append("<gold>" + hs.gold + "</gold>");
            outputScores.append("<dmggvn>" + hs.dmgGiven + "</dmggvn>");
            outputScores.append("<bonus>" + hs.bonus + "</bonus>");
            outputScores.append("<dmgrcvd>" + hs.dmgReceived + "</dmgrcvd>");
            outputScores.append("<cfl>" + hs.conflicts + "</cfl>");
            outputScores.append("<brk>" + hs.breakSaves + "</brk>");
            outputScores.append("<saved>" + hs.saveDate + "</saved>");
            outputScores.append("<base>" + hs.starbaseLetter + "</base>");
            outputScores.append("</score>");
        }

        outputScores.append("</scores>");
        sendText(outputScores.toString());

        sendText("<dataRequest id=\"classScorePause\" />");
    }

    protected void doHighScoresFleet() {
        if (state == WAIT_SOCKETERROR)
            return;

        state = WAIT_HSFLEET;
        StringBuffer outputScores = new StringBuffer();

        outputScores.append("<scores id=\"fleet\">");

        Vector overall = dbInt.getFleetHighScores(dbPlayerID, TrekJDBCInterface.ORDER_BY_GOLD);
        for (int x = 0; x < overall.size(); x++) {
            TrekHighScoreListing hs = (TrekHighScoreListing) overall.elementAt(x);
            outputScores.append("<score id=\"" + (x + 1) + "\">");
            outputScores.append("<ship>" + hs.name + "</ship>");
            outputScores.append("<class>" + hs.className + "</class>");
            outputScores.append("<gold>" + hs.gold + "</gold>");
            outputScores.append("<dmggvn>" + hs.dmgGiven + "</dmggvn>");
            outputScores.append("<bonus>" + hs.bonus + "</bonus>");
            outputScores.append("<dmgrcvd>" + hs.dmgReceived + "</dmgrcvd>");
            outputScores.append("<cfl>" + hs.conflicts + "</cfl>");
            outputScores.append("<brk>" + hs.breakSaves + "</brk>");
            outputScores.append("<saved>" + hs.saveDate + "</saved>");
            outputScores.append("<base>" + hs.starbaseLetter + "</base>");
            outputScores.append("</score>");
        }

        outputScores.append("</scores>");
        sendText(outputScores.toString());

        sendText("<dataRequest id=\"fleetScorePause\" />");
    }

    protected synchronized void doTickUpdate() {
        try {
            // If there is no ship, don't do anything.
            if (ship == null || state != WAIT_PLAYING || hud == null) {
                return;
            }
            // Call the ship update.
            ship.doShipTickUpdate();
            // Update the hud
            hud.updateHud();
            // Clear the message queue.
            for (int x = 0; x < messageQueue.size(); x++) {
                String theMessage = (String) messageQueue.elementAt(x);
                hud.sendMessage(theMessage);
            }
            messageQueue.clear();
            // Send any queued output
            sendOutputBuffer();
        } catch (Exception e) {
            TrekLog.logException(e);
            kill();
        }
    }

    public void run() {
        buffer = "";

        try {
            Thread.sleep(500);

            // Ouput Welcome Message...
            StringBuffer sb = new StringBuffer();
            sb.append("<login id=\"welcomeMsg\">");
            try {
                File loginMsg = new File("./welcome.txt");
                String tmpStr;
                BufferedReader br = new BufferedReader(new FileReader(loginMsg));

                sb.append("<msgtext>");

                while ((tmpStr = br.readLine()) != null) {
                    sb.append(tmpStr);
                }

                sb.append("</msgtext>");
            } catch (FileNotFoundException fnfe) {
                TrekLog.logError("Missing welcome screen text file - welcome.txt");
                sb.append("<msgtext>Welcome to JTrek</msgtext>");
            } catch (IOException ioe) {
                TrekLog.logException(ioe);
            } finally {
                sb.append("<players>" + TrekServer.getNumberOfActivePlayers() + "</players>");
                sb.append("<uptime>" + TrekServer.getUptime() + "</uptime>");
                sb.append("<teamplay>" + TrekServer.isTeamPlayEnabled() + "</teamplay>");
                sb.append("</login>");
            }
            sendText(sb.toString());
            sb = null; // free it for garbage collection

            Thread.sleep(250);

            // If the server is full...
            if (TrekServer.players.size() >= 62) {
                sendError("We're sorry.  The server is full.  Try again later.");
                sendText("<dataRequest id=\"serverFullMsgPause\" />");
                getBlockedInput(false, 60000, true);
                state = WAIT_HSCLASS;
                return;
            }

            int clientInput = 0;
            TrekLog.logMessage("Getting raw user ship name.");

            do {
                // Reset the ship name.
                shipName = "";

                if (!doLogin()) {
                    TrekLog.logMessage("Raw data user disconnected.");
                    kill();
                    return;
                }

                if (dbInt.isShipPasswordProtected(shipName)) {
                    if (!doPassword()) {
                        TrekLog.logMessage("PASSWORD FAILURE: " + shipName + " failed password check (raw data).");
                    } else {
                        if (!dbInt.doesShipExist(shipName)) {
                            if (!doChooseShip()) {
                                TrekLog.logMessage("Failed getting raw data ship class.");
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
                        TrekLog.logMessage("Failed to link new ship to raw data player.");
                    } else {
                        if (!doChooseShip()) {
                            TrekLog.logMessage("Failed getting raw data ship class.");
                        } else {
                            ship.dbShipID = dbInt.getNewShipID(shipName);
                            timeShipLogin = Calendar.getInstance().getTimeInMillis();
                            dbConnectionID = dbInt.createNewConnection(this);
                        }
                    }
                }

                if (state == WAIT_SOCKETTIMEDOUT) {
                    TrekLog.logMessage("Socket timeout. Raw data user disconnected.");
                    kill();
                    return;
                }

                if (state == WAIT_SOCKETERROR) {
                    TrekLog.logMessage("Socket error. Raw data user disconnected.");
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
                TrekLog.logMessage("Drawing raw user initial HUD.");
                drawHud(true); // TODO: do this?

                TrekLog.logMessage("Raw data ship " + shipName + " is now active.");

                state = WAIT_PLAYING;

                ship.currentQuadrant.addShip(ship);
            } else {
                TrekLog.logMessage("BANNED - attempted raw data login of " + ship.name + " - playerID: " + dbPlayerID);
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
                            TrekLog.logDebug(shipName + ": Input received " + clientInput);
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
                                    TrekLog.logMessage(shipName + " closed the session.");
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
                                TrekLog.logMessage(shipName + " closed the session.");
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

            if (state == WAIT_DEAD) {
                doBoom();
                ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
                //input = getBlockedInput(false, 60000, true);
            } else {
                if (ship != null)
                    ship.currentQuadrant.removeShipByScanLetter(ship.scanLetter);
            }

            timeShipLogout = Calendar.getInstance().getTimeInMillis();
            dbInt.updateConnection(this);

            if (state != TrekPlayer.WAIT_SOCKETERROR) {
                doHighScoresOverall();
                input = getBlockedInput(false, 60000, true);

                doHighScoresClass();
                input = getBlockedInput(false, 60000, true);

                doHighScoresFleet();
                input = getBlockedInput(false, 60000, true);

                sendText("<disconnect />");
            }

            TrekServer.removePlayer(this);

            TrekLog.logMessage("Raw data player " + shipName + " thread exiting.");
        } catch (Exception e) {
            TrekLog.logException(e);
            kill();
        }
    }

    protected String getBlockedInput(boolean echo, int timeout, boolean waitForCarriageReturn) {
        int x = 0;

        try {
            do {
                try {
                    // Wait for timeout milliseconds, before attempting to read again.
                    if (!socket.isConnected()) return "";
                    if (socket.isClosed()) return "";

                    socket.setSoTimeout(timeout);

                    // Get input from the socket.
                    x = in.read();

                    if (x == 10)
                        x = 0;

                    // If the input is -1 then the socket is probably bad.
                    if (x == -1)
                        return null;

                    if (!waitForCarriageReturn && x != 0)
                        return new Character((char) x).toString();

                    if (bufferInput(x, echo))
                        break;

                } catch (SocketTimeoutException ste) {
                    state = WAIT_SOCKETTIMEDOUT;
                    //sendText("<disconnect />");
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

    protected void sendText(String thisText) {
        if (state == TrekPlayer.WAIT_PLAYING) addToOutputBuffer(thisText);
        else {
            synchronized (this) {
                try {
                    thisText = thisText.replace('\010', '\000');
                    if (state != WAIT_PLAYING) {
                        Timer timeToDie = new Timer();

                        byte output[] = new byte[2048];
                        compressor = new Deflater();
                        compressor.setInput(thisText.getBytes());
                        compressor.finish();
                        int compressedSize = compressor.deflate(output);
                        byte sentData[] = new byte[compressedSize];
                        for (int x = 0; x < compressedSize; x++) {
                            sentData[x] = output[x];
                        }
                        //System.out.println("st: compressed to " + compressedSize + " bytes.  Adler: " + adlerCrc + "  -- Wrote: " + new String(sentData));
                        out.write(sentData);
                        out.flush();

                        timeToDie.cancel();

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

    protected void sendOutputBuffer() {
        synchronized (this) {
            try {
                if (!outputBuffer.equals("")) {
                    outputBuffer = outputBuffer.replace('\010', '\000');

                    Timer timeToDie = new Timer();

                    timeToDie.schedule(new TrekDeadThreadKiller(this), 1000);  // you have 1 second to comply

                    /*dOut = new DeflaterOutputStream(bOut);
                    dataOut = new DataOutputStream(dOut);
                    dataOut.writeUTF(outputBuffer);
                    System.out.println("wrote data(2): " + outputBuffer);
                    dOut.finish();*/

                    //cOut.write(outputBuffer.getBytes());
                    //cOut.flush();
                    byte output[] = new byte[2048];
                    compressor = new Deflater();
                    compressor.setInput(outputBuffer.getBytes());
                    compressor.finish();
                    int compressedSize = compressor.deflate(output);
                    byte sentData[] = new byte[compressedSize];
                    for (int x = 0; x < compressedSize; x++) {
                        sentData[x] = output[x];
                    }
                    //System.out.println("sob: compressed to " + compressedSize + " bytes.  Adler: " + adlerCrc + "  -- Wrote: " + new String(sentData));
                    out.write(sentData);
                    out.flush();

                    //bOut.write(outputBuffer.getBytes());
                    //bOut.flush();

                    // if you made it here in less than 1 second, you're still alive, otherwise... goodbye, you are the weakest link
                    timeToDie.cancel();
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

    protected void doCommand(int command) {
        if (state != WAIT_PLAYING)
            return;
        // do regular commands unless there is an inputstatenext already set;
        // this will prevent normal commands from intercepting user input
        // that should be handled by the inputstatenext blocks
        if (inputstatenext == INPUT_NORMAL) {
            TrekLog.logDebug(shipName + ": RAW doCommand(" + command + ");");
            /*
                *
                * ****** CATCH INPUT STATES ******
                *
                */
            // kicked off by shift-i command
            if (inputstate == INPUT_COORDINATES) {
                // ctrl-c
                if (command == 3) {
                    inputstate = INPUT_NORMAL;
                    buffer = "";
                    command = 0;
                }
                // backspace / ctrl-h
                if (isValidEraseCharacter(command)) {
                    if (buffer.length() > 0) {
                        buffer = buffer.substring(0, buffer.length() - 1);
                        //sendText("<dataRequest id=\"intXYZ\"><current>" + buffer + "</current></dataRequest>");
                    }
                }
                // limit characters to numbers, negative sign, and space
                if ((command >= 48 && command <= 57) || command == 45 || command == 32) {
                    buffer += Character.toString((char) command);
                    //sendText("<dataRequest id=\"intXYZ\"><current>" + buffer + "</current></dataRequest>");
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
                            buffer = buffer.trim();
                            // get rid of any leading/trailing spaces
                            float coordX = new Float(buffer.substring(0, buffer.indexOf(' ')).trim()).floatValue();
                            buffer = buffer.substring(buffer.indexOf(' ') + 1, buffer.length());
                            float coordY = new Float(buffer.substring(0, buffer.indexOf(' ')).trim()).floatValue();
                            float coordZ = new Float(buffer.substring(buffer.indexOf(' ') + 1, buffer.length()).trim()).floatValue();
                            ship.interceptCoords(coordX, coordY, coordZ);
                        } else {
                            sendError("Badly formed coordinates.  Enter as: X Y Z");
                        }
                    } catch (Exception badnumber) {
                    } finally {
                        inputstate = INPUT_NORMAL;
                        buffer = "";
                        command = 0;
                    }
                }
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

                if (command != 115) {
                    inputstate = INPUT_NORMAL;
                }
                buffer = "";
                command = 0;
            }

            // ! command
            if (inputstate == INPUT_HEADING) {
                if (command == 3) {
                    inputstate = INPUT_NORMAL;
                    buffer = "";
                    command = 0;
                }
                if (command == 8) {
                    if (buffer.length() > 0) {
                        buffer = buffer.substring(0, buffer.length() - 1);
                        //sendText("<dataRequest id=\"heading\"><current>" + buffer + "</current></dataRequest>");
                    }
                }
                // limit characters to numeric, minus sign, single quote
                if ((command >= 48 && command <= 57) || (command == 45) || (command == 39)) {
                    buffer += Character.toString((char) command);
                    //sendText("<dataRequest id=\"heading\"><current>" + buffer + "</current></dataRequest>");
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
                                sendError("Badly formed heading, use xxx'zz.  i.e. 123'45, or 123'-45.");
                            }
                        } catch (Exception e) {
                            sendError("Badly formed heading, use xxx'zz.  i.e. 123'45, or 123'-45.");
                        } finally {
                            inputstate = INPUT_NORMAL;
                            buffer = "";
                            command = 0;
                        }
                    } else {
                        sendError("Badly formed heading, use xxx'zz.  i.e. 123'45, or 123'-45.");
                        inputstate = INPUT_NORMAL;
                        buffer = "";
                        command = 0;
                    }
                }
            }

            if (inputstate == INPUT_HELPSCREEN) {
                hud.showHelpScreen = true;
                hud.showingHelpScreen = false;
                hud.helpScreenLetter = new Character((char) command).toString();
                TrekLog.logMessage("User chose help screen: " + hud.helpScreenLetter);
                inputstate = INPUT_NORMAL;
                command = 0;
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
                        }
                    } else {
                        if (TrekUtilities.isValidShipChar(command))
                            ship.interceptShipNoScan(Character.toString((char) command));
                    }
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
            }

            if (inputstate == INPUT_MACROLETTERADD) {
                chosenMacro = (char) command;
                inputstate = INPUT_NORMAL;
                inputstatenext = INPUT_MACROADD;
                //sendText("<dataRequest id=\"macroAdd\" />");
                command = 0;
            }

            if (inputstate == INPUT_MACROLETTERREMOVE) {
                chosenMacro = (char) command;
                inputstate = INPUT_NORMAL;
                inputstatenext = INPUT_MACROREMOVE;
                command = 0;
            }

            if (inputstate == INPUT_MESSAGETARGET) {
                buffer = "";
                if ((TrekUtilities.isValidShipChar(command)) || command == 42 || command == 46 || command == 125) {
                    if (command == 42) {
                        this.messageTarget = TrekPlayer.MESSAGETARGET_ALL;
                        this.inputstatenext = TrekPlayer.INPUT_MESSAGE;
                        //sendText("dataRequest id=\"msgAll\"><current>" + buffer + "</current></dataRequest>");
                        inputstatenext = INPUT_MESSAGE;
                    } else if (command == 46) {
                        this.messageTarget = TrekPlayer.MESSAGETARGET_CLOSEST;
                        this.inputstatenext = TrekPlayer.INPUT_MESSAGE;
                        //sendText("dataRequest id=\"msgClosest\"><current>" + buffer + "</current></dataRequest>");
                        inputstatenext = INPUT_MESSAGE;
                    } else if (command == 125) {
                        if (TrekServer.isTeamPlayEnabled()) {
                            messageTarget = TrekPlayer.MESSAGETARGET_TEAM;
                            //messagePrompt = "msgTeam";
                        } else {
                            this.messageTarget = TrekPlayer.MESSAGETARGET_RACE;
                            //messagePrompt = "msgRace";
                        }
                        this.inputstatenext = TrekPlayer.INPUT_MESSAGE;
                        //sendText("<dataRequest id=\"" + messagePrompt + "\"><current>" + buffer + "</current></dataRequest>");
                        inputstatenext = INPUT_MESSAGE;
                    } else {
                        if (TrekServer.getPlayerShipByScanLetter(new Character((char) command).toString()) == null) {
                            //messagePrompt = "msgNoShip";
                        } else {
                            //messagePrompt = "msgShip";
                        }
                        if (TrekUtilities.isValidShipChar(command)) {
                            this.messageTargetShip = Character.toString((char) command);
                            this.messageTarget = TrekPlayer.MESSAGETARGET_SHIP;
                            this.inputstatenext = TrekPlayer.INPUT_MESSAGE;
                            //sendText("<dataRequest id=\"" + messagePrompt + "\"><current>" + buffer + "</current></dataRequest>");
                            inputstatenext = INPUT_MESSAGE;
                        } else {
                            inputstatenext = INPUT_NORMAL;
                        }
                    }
                    inputstate = INPUT_NORMAL;
                } else {
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                }
                command = 0;
            }
            if (inputstate == INPUT_QUIT) {
                if (command == 121) {
                    if (ship.damage <= 150) {
                        doQuit();
                    } else {
                        hud.sendMessage("Sorry, you have too much damage to quit.");
                    }
                }
                inputstate = INPUT_NORMAL;
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
            }

            if (inputstate == INPUT_SEEKERTARGET) {
                if (command != 0) {
                    if (TrekUtilities.isValidShipChar(command)) {
                        ship.fireSeekerProbe(Character.toString((char) command));
                    }
                    command = 0;
                    inputstate = INPUT_NORMAL;
                    sendText(TrekAnsi.clearRow(19, this));
                }
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
            }

            if (inputstate == INPUT_WARPAMOUNT) {
                // CTRL+C
                if (command == 3) {
                    buffer = "";
                }
                if (isValidEraseCharacter(command)) {
                    if (buffer.length() > 0) {
                        buffer = buffer.substring(0, buffer.length() - 1);
                        //sendText("<dataRequest id=\"warp\"><current>" + buffer + "</current></dataRequest>");
                    }
                }
                if ((command >= 48 && command <= 57) || command == 46 || command == 45) {
                    buffer += Character.toString((char) command);
                    //sendText("<dataRequest id=\"warp\"><current>" + buffer + "</current></dataRequest>");
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
                        inputstate = INPUT_NORMAL;
                        buffer = "";
                    }
                }
                command = 0;
                return;
            }

            if (inputstate == INPUT_ADMIRALCOMMAND) {
                switch (command) {
                    case 3: // CTRL-C to cancel.
                        buffer = "";
                        inputstate = INPUT_NORMAL;
                        break;
                    case 13: // Enter to issue the command.
                        doAdmiralCommand(buffer);
                        buffer = "";
                        inputstate = INPUT_NORMAL;
                        break;
                    default:
                        if (isValidEraseCharacter(command)) {
                            if (buffer.length() > 0) {
                                buffer = buffer.substring(0, buffer.length() - 1);
                                //sendText("<dataRequest id=\"admiral\"><current>" + buffer + "</current></dataRequest>");
                            }
                        } else {
                            if (isValidInput(command)) {
                                buffer += new Character((char) command).toString();
                                //sendText("<dataRequest id=\"admiral\"><current>" + buffer + "</current></dataRequest>");
                            }
                        }
                        break;
                }
            }

            /*
                *
                * ****** ESCAPE COMMANDS ******
                *
                */

            if (inputstate == INPUT_ESCAPECOMMAND) {
                validEscCommand = false;
                // escape pod; esc-ctrl-e
                if (command == 5) {
                    validEscCommand = true;
                    ship.launchEscapePod();
                }
                // lock weapons; ESC-ctrl-l-<ship>
                if (command == 12) {
                    validEscCommand = true;
                    sendText("<dataRequest id=\"shipLtrLock\" />");
                    inputstatenext = INPUT_LOCKTARGET;
                }
                // ESC - Ctrl - M : intercept heading of last message
                if (command == 13) {
                    validEscCommand = true;
                    if (ship.msgPoint != null) {
                        ship.interceptMsg();
                    } else {
                        hud.sendMessage("You've received no messages to track.");
                    }
                }
                if (command == 14) {
                    validEscCommand = true;
                    hud.sendMessage("Phasers set to fire narrow.");
                    ship.phaserFireType = TrekShip.PHASER_FIRENARROW;
                }
                if (command == 16) {
                    validEscCommand = true;
                    if (this.ship.shipClass != "DY-600") {
                        hud.sendMessage("You cannot change phaser types.");
                    } else {
                        sendText("<dataRequest id=\"phType\" />");
                        inputstatenext = INPUT_PHASERTYPE;
                    }
                }
                if (command == 19) {
                    validEscCommand = true;
                    hud.sendMessage("Scan Range: " + new Double(ship.scanRange).intValue());
                }
                if (command == 20) {
                    validEscCommand = true;
                    if (this.ship.shipClass != "CV-97" && this.ship.shipClass != "SCOUT") {
                        hud.sendMessage("You cannot change your torpedo type.");
                    } else {
                        sendText("<dataRequest id=\"torpType\" />");
                        inputstatenext = INPUT_TORPEDOTYPE;
                    }
                }
                if (command == 23) {
                    validEscCommand = true;
                    hud.sendMessage("Phasers set to fire wide.");
                    ship.phaserFireType = TrekShip.PHASER_FIREWIDE;
                }
                if (command == 35) {
                    validEscCommand = true;
                    hud.sendMessage("Your are designated as ship " + ship.scanLetter + ".");
                }
                if (command == 64) {
                    // experimental command; ESC-@
                    // show the 'who' roster
                    validEscCommand = true;
                    // inputstatenext = INPUT_ROSTER; // shouldn't be needed for raw interface, since play doesn't need to be
                    // suspended in order to display this data
                    printShipRoster(WHO_INGAME);
                }
                // esc-L-ship letter -- display coords where last seen
                if (command == 76) {
                    validEscCommand = true;
                    inputstatenext = INPUT_SHIPLETTERDIRECTION;
                    sendText("<dataRequest id=\"shipLtrLastSeenCoords\" />");
                }
                // esc-M -- display heading of last message received
                if (command == 77) {
                    validEscCommand = true;
                    if (ship.msgPoint != null) {
                        hud.sendMessage("Last message received from: " + ship.getBearingToMsg());
                    } else {
                        hud.sendMessage("You've received no messages to track.");
                    }
                }
                // ESC - O : odometer
                if (command == 79) {
                    validEscCommand = true;
                    ship.showOdometerPage();
                }
                // Capture the password command.
                if (command == 80) {
                    //validEscCommand = true;
                    //sendText(TrekAnsi.clearRow(19, this) + "New Ship Password: ");
                    //inputstatenext = INPUT_PASSWORD;
                }
                // Capture show jammed frequencies comamnd.
                if (command == 82) {
                    validEscCommand = true;
                    ship.reportJammedSlots();
                }
                // Capture save command
                if (command == 83) {
                    validEscCommand = true;
                    if (!(ship instanceof ShipQ)) {
                        if (!this.ship.docked) {
                            hud.sendMessage("You can only save if you are docked at a starbase.");
                        } else {
                            doSave();
                        }
                    } else {
                        doSave();
                    }
                }
                if (command == 84) {
                    validEscCommand = true;
                    if (this.ship.torpedoType != TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                        hud.sendMessage("You cannot change your torpedo speed.");
                    } else {
                        sendText("<dataRequest id=\"torpSpeed\" />");
                        inputstatenext = INPUT_TORPEDOSPEED;
                    }
                }
                // ESC+W - Max torpedo and phaser capabilities
                if (command == 87) {
                    validEscCommand = true;
                    String phaserString = "";
                    String torpedoString = "Torpedoes: " + ship.torpedoCount + "(" + ship.maxTorpedoStorage + ") " + ship.minTorpedoRange + "-" + ship.maxTorpedoRange + " " + ship.getMtrekStyleTorpString();
                    if (ship.homePlanet.equals("Cardassia")) {
                        phaserString = "Phasers: " + ship.maxPhaserBanks + " 1-" + ship.minPhaserRange + " " + ship.getMtrekStylePhaserString();
                    } else {
                        phaserString = "Phasers: " + ship.maxPhaserBanks + " 0-" + ship.minPhaserRange + " " + ship.getMtrekStylePhaserString();
                    }
                    hud.sendMessage(phaserString + "  " + torpedoString);
                }
                if (command == 97) {
                    validEscCommand = true;
                    if (this.ship.torpedoType != TrekShip.TORPEDO_VARIABLESPEEDPLASMA) {
                        hud.sendMessage("You cannot change your torpedo speed.");
                    } else {
                        this.ship.torpedoWarpSpeedAuto = true;
                        hud.sendMessage("Torpedo speed will be set automatically.");
                    }
                }
                // Esc - b -- buoy availability
                if (command == 98) {
                    validEscCommand = true;
                    if (ship.buoyTimeout > 0)
                        hud.sendMessage("Next buoy will be ready in " + ship.buoyTimeout + " second(s).");
                    else
                        hud.sendMessage("A buoy is ready for deployment.");
                }
                // ESC - c - <ship letter> -- shows score of <ship letter>
                if (command == 99) {
                    validEscCommand = true;
                    inputstatenext = INPUT_SHIPLETTERSCORE;
                    sendText("<dataRequest id\"shipLtrScore\" />");
                }
                // ESC - d : show drone status
                if (command == 100) {
                    validEscCommand = true;
                    if (ship.homePlanet.equals("Cardassia")) {
                        hud.sendMessage("Drones: " + ship.droneCount + "(" + ship.maxDroneStorage + ")  Strength: variable");
                    } else {
                        if (ship.drones) {
                            hud.sendMessage("Drones: " + ship.droneCount + "(" + ship.maxDroneStorage + ")  Strength: " + ship.droneStrength);
                        } else {
                            hud.sendMessage("This type of ship can't carry any drones.");
                        }
                    }
                }
                // ESC - i : misc device inventory
                if (command == 105) {
                    hud.showInventoryScreen();
                }
                if (command == 108) {
                    validEscCommand = true;
                    inputstatenext = INPUT_SHIPLETTERINTCOORD;
                    sendText("<dataRequest id=\"shipLtrIntLastCoords\" />");
                }
                if (command == 109) {
                    validEscCommand = true;
                    if (ship.mines) {
                        hud.sendMessage("Mines: " + ship.mineCount + "(" + ship.maxMineStorage + ")  Strength: " + ship.mineStrength);
                    } else {
                        hud.sendMessage("This type of ship can't carry any mines.");
                    }
                }
                // esc-o : option page
                // TODO: options screen handling
                if (command == 111) {
                    validEscCommand = true;
                    if (hud.showOptionScreen) {
                        hud.showOptionScreen = false;
                        inputstate = INPUT_NORMAL;
                        inputstatenext = INPUT_NORMAL;
                    } else {
                        ship.stopScanning();
                        hud.showOptionScreen = true;
                        inputstatenext = INPUT_OPTION;
                    }
                }
                if (command == 112) {
                    validEscCommand = true;
                    if (ship.homePlanet.equals("Cardassia")) {
                        hud.sendMessage("Phasers: " + ship.maxPhaserBanks + "  Range: 1-" + ship.minPhaserRange + "  Phaser type: " + ship.getMtrekStylePhaserString());
                    } else {
                        hud.sendMessage("Phasers: " + ship.maxPhaserBanks + "  Range: 0-" + ship.minPhaserRange + "  Phaser type: " + ship.getMtrekStylePhaserString());
                    }
                }
                if (command == 114) {
                    validEscCommand = true;
                    if (ship.homePlanet.equals("Cardassia")) {
                        hud.sendMessage("Phaser Range: 1-" + ship.minPhaserRange + "  Torpedo Range: " + ship.minTorpedoRange + "-" + ship.maxTorpedoRange + ".");
                    } else {
                        hud.sendMessage("Phaser Range: 0-" + ship.minPhaserRange + "  Torpedo Range: " + ship.minTorpedoRange + "-" + ship.maxTorpedoRange + ".");
                    }
                }
                if (command == 115) {
                    validEscCommand = true;
                    hud.showScore(this);
                }
                if (command == 116) {
                    validEscCommand = true;
                    hud.sendMessage("Torpedoes: " + ship.torpedoCount + "(" + ship.maxTorpedoStorage + ")  Range: " + ship.minTorpedoRange + "-" + ship.maxTorpedoRange + "  Torpedo type: " + ship.getMtrekStyleTorpString());
                }
                if (command == 118) {
                    validEscCommand = true;
                    hud.sendMessage("Normal Visibility: " + ship.visibility + " - Current Visibility: " + ship.getCurrentVisibility());
                }
                if (command == 119) {
                    validEscCommand = true;
                    hud.sendMessage("Max Turn Warp: " + ship.maxTurnWarp + "  Max Cruise Warp: " + ship.maxCruiseWarp + "  Max Damage Warp: " + ship.damageWarp + ".0");
                }
                if (command == 122) {
                    validEscCommand = true;
                    if (ship.cloak) {
                        if (ship.cloakBurnt) {
                            hud.sendMessage("Your cloaking device has burnt out.");
                        } else {
                            hud.sendMessage("Your cloaking device is operating normally.");
                        }
                    } else {
                        hud.sendMessage("You have no cloaking device.");
                    }
                }
                if (!validEscCommand) {
                    // nothing
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
                            sendText("<dataRequest id=\"admiral\" />");
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
                            sendText("<dataRequest id=\"droneSpeed\" />");
                            inputstatenext = INPUT_DRONESPEED;
                        } else {
                            hud.sendMessage("You cannot change your drone speed.");
                            inputstatenext = INPUT_NORMAL;
                            inputstate = INPUT_NORMAL;
                        }
                        command = 0;
                        break;
                    case 5: // CTRL-E - Clear energy usage. (unloads weapons,
                        // stops movement, turns off dmg ctl)
                        ship.clearEnergyUse();
                        break;
                    case 9: // CTRL-I <ship>; Intercept ship.
                        sendText("<dataRequest id=\"shipLtrIntNoScan\" />");
                        inputstate = INPUT_INTERCEPTSHIPNOSCAN;
                        break;
                    case 12: // CTRL-L; Lock weapons.
                        ship.lockWeapons();
                        break;
                    case 16: // CTRL-P; Unload phasers.
                        ship.unloadPhasers(5);
                        break;
                    case 18: // CTRL-R; Redraw screen.
                        // drawHud(false);
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
                        sendText("<dataRequest id=\"escCmd\" />");
                        inputstate = INPUT_ESCAPECOMMAND;
                        break;
                    case 33: // ! ; Set heading.
                        //	allow modify heading as long as they aren't
                        // exceeding turn warp
                        if (!(Math.abs(ship.warpSpeed) > ship.maxCruiseWarp)) {
                            sendText("<dataRequest id=\"heading\" />");
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
                        sendText("<dataRequest id=\"shipLtrShipSpecs\" />");
                        inputstate = INPUT_SHIPSTATS;
                        break;
                    case 40: // ) ; Add macro.
                        sendText("<dataRequest id=\"macroAdd\" />");
                        inputstate = INPUT_MACROLETTERADD;
                        break;
                    case 41: // ( ; Remove macro.
                        sendText("<dataRequest id=\"macroRemove\" />");
                        inputstate = INPUT_MACROLETTERREMOVE;
                        break;
                    case 46: // Dock or orbit '.'
                        ship.dock();
                        break;
                    case 63: // ? - Help screens.
                        sendText("<dataRequest id=\"helpScreen\" />");
                        inputstate = INPUT_HELPSCREEN;
                        break;
                    case 64: // Capture set warp. @
                        inputstate = INPUT_WARPAMOUNT;
                        sendText("<dataRequest id=\"warp\" />");
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
                            sendText("<dataRequest id=\"intXYZ\" />");
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
                        sendText("<dataRequest id=\"objLtrScan\" />");
                        break;
                    case 80: // Capture phaser fire. P
                        ship.loadPhasers(5);
                        break;
                    case 81: // Capture quit. Q
                        inputstate = INPUT_QUIT;
                        sendText("<dataRequest id=\"quit\" />");
                        break;
                    case 82: // Capture jam message command. R
                        inputstate = INPUT_SHIPLETTERJAM;
                        sendText("<dataRequest id=\"shipLtrJam\" />");
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
                        sendText("<dataRequest id=\"dropInvObj\" />");
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
                            sendText("<dataRequest id=\"msgTarget\" />");
                            inputstate = INPUT_MESSAGETARGET;
                        }
                        command = 0;
                        break;
                    case 111: // Capture ship scan. o
                        inputstate = INPUT_SCANLETTERSHIP;
                        sendText("<dataRequest id=\"shipLtrScan\" />");
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
                        // nothing
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
            }

            if (inputstatenext == INPUT_MACROADD) {
                switch (command) {
                    case 3:
                        buffer = "";
                        chosenMacro = 0;
                        inputstatenext = INPUT_NORMAL;
                        break;
                    case 13:
                        setMacro(chosenMacro, buffer);
                        buffer = "";
                        chosenMacro = 0;
                        inputstatenext = INPUT_NORMAL;
                        break;
                    default:
                        if (command > 31 && command < 127) {
                            if (buffer.length() < 100) {
                                buffer = buffer + Character.toString((char) command);
                                //sendText("<dataRequest id=\"macroAdd\"><current>" + buffer + "</current></dataRequest>");
                            }
                        }
                        if (isValidEraseCharacter(command)) {
                            try {
                                buffer = buffer.substring(0, buffer.length() - 1);
                            } catch (StringIndexOutOfBoundsException soobe) {
                                buffer = "";
                            }
                            //sendText("<dataRequest id=\"macroAdd\"><current>" + buffer + "</current></dataRequest>");
                        }
                        break;
                }
                command = 0;
            }

            if (inputstatenext == INPUT_MACROREMOVE) {
                removeMacro(chosenMacro);
                command = 0;
                inputstatenext = INPUT_NORMAL;
            }

            if (inputstatenext == INPUT_MESSAGE) {
                switch (command) {
                    case 3:
                        inputstatenext = INPUT_NORMAL;
                        buffer = "";
                        break;
                    case 13:
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
                                //sendText("<dataRequest id=\"msgData\"><current>" + buffer + "</current></dataRequest>");
                                command = 0;
                            }
                        }
                        if (isValidEraseCharacter(command)) {
                            try {
                                buffer = buffer.substring(0, buffer.length() - 1);
                            } catch (StringIndexOutOfBoundsException siobe) {
                                buffer = "";
                            }
                            //sendText("<dataRequest id=\"msgData\"><current>" + buffer + "</current></dataRequest>");
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
            }

            if (inputstatenext == INPUT_PASSWORD) {
                switch (command) {
                    case 3:
                        inputstatenext = INPUT_NORMAL;
                        buffer = "";
                        break;
                    case 13:
                        if (passwordBuffer.equals("")) {
                            hud.sendMessage("You cannot have a blank password.");
                            buffer = "";
                            passwordBuffer = "";
                            inputstatenext = INPUT_NORMAL;
                        } else {
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
                            // TODO: when implementing password update to player account w/ raw interface send out
                            // appropriate data here
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
                            // TODO: and here!
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
                        // nothing
                    }
                    hud.sendMessage("Phaser type is now " + ship.getMtrekStylePhaserString() + ".");
                    command = 0;
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                }
            }

            // wait for a keystroke and return to normal HUD
            // unused here
            if (inputstatenext == INPUT_ROSTER) {
                if (command != 0) {
                    hud.disabled = false;
                    //drawHud(false);
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                    command = 0;
                }
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
            }

            // get ship for ESC-l command
            if (inputstatenext == INPUT_SHIPLETTERINTCOORD) {
                // CTRL+C
                if (command == 3) {
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
            }

            // get letter of ship for ESC-c command, and display score if valid
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
                        // nothing
                    }

                    hud.sendMessage("Torpedo type is set to " + ship.getMtrekStyleTorpString() + ".");
                    command = 0;
                    inputstate = INPUT_NORMAL;
                    inputstatenext = INPUT_NORMAL;
                }
            }
        }
    }
}
