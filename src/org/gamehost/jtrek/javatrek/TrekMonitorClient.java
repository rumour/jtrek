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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Timer;

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Mar 8, 2004
 * Time: 12:16:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrekMonitorClient extends Thread {
    protected Socket socket;
    private OutputStream out;
    private InputStream in;
    private String buffer = "";
    private boolean doDisconnect = false;
    private boolean adminMon = false;
    private boolean loggedIn = false;
    protected String monPlayer;

    private String outputBuffer = "";

    public TrekMonitorClient(Socket sck) {

        try {
            socket = sck;
            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);

            in = socket.getInputStream();
            out = socket.getOutputStream();

            System.out.println("TrekMonitorClient created.");

        } catch (IOException ioe) {
            TrekLog.logError("Could not create TrekMonitorClient object for " + socket.toString() + "!");
        }
    }

    public void run() {
        String curCmd = "";

        while (!doDisconnect) {
            try {
                if (socket.isClosed()) {
                    doDisconnect = true;
                } else {
                    Thread.sleep(100);
                    curCmd = getBlockedInput(true);

                    if (curCmd != null && !(curCmd.equals(""))) {
                        if (loggedIn) {
                            //System.out.print(curCmd);
                            processCommand(curCmd);
                        } else {
                            login(curCmd);
                        }
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            curCmd = "";
            buffer = "";
        }

        kill();
    }

    protected void addToOutputBuffer(String thisOutput) {
        synchronized (this) {
            outputBuffer += thisOutput;
        }
    }

    protected synchronized void sendOutputBuffer() {
        try {
            if (!outputBuffer.equals("")) {
                outputBuffer = outputBuffer.replace('\010', '\000');
                Timer timeToDie = new Timer("MonitorClient-" + monPlayer);
                timeToDie.schedule(new TrekDeadThreadKiller(this), 1000);  // keep socket locks from freezing app

                out.write(outputBuffer.getBytes());
                out.flush();

                // if you made it here in less than 1 second, you're still alive, otherwise... goodbye, you are the weakest link
                timeToDie.cancel();
            }
        } catch (java.net.SocketException se) {
            TrekLog.logError(se.getMessage());
            kill();
        } catch (Exception e) {
            TrekLog.logException(e);
            kill();
        } finally {
            outputBuffer = "";
        }
    }

    private boolean bufferInput(int x) {
        // Intercept carriage returns.
        if (x == 13)
            return true;
        if ((x != 0 && x >= 32 && x <= 126) || x == 13 || x == 32) {
            buffer += Character.toString((char) x);
        }
        return false;
    }

    private String getBlockedInput(boolean waitForCarriageReturn) {
        int x = 0;

        try {
            do {
                try {
                    // Wait for timeout milliseconds, before attempting to read again.
                    //socket.setSoTimeout(timeout);

                    // Get input from the socket.
                    x = in.read();

                    if (x == 10)
                        x = 0;

                    // If the input is -1 then the socket is probably bad.
                    if (x == -1)
                        return null;

                    if (!waitForCarriageReturn && x != 0)
                        return new Character((char) x).toString();

                    if (bufferInput(x))
                        break;

                } catch (SocketTimeoutException ste) {
                    //TrekLog.logMessage("Socket timed out.  Disconnecting.");
                    return null;
                } catch (SocketException se) {
                    socket.close();
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
            TrekLog.logError(socket.getInetAddress().toString() + " Socket Exception: " + se.getMessage());
            kill();
            return null;
        } catch (IOException ioe) {
            TrekLog.logError(socket.getInetAddress().toString() + " IOException: " + ioe.getMessage());
            kill();
            return null;
        } catch (Exception re) {
            TrekLog.logException(re);
            return null;
        }
    }

    private void login(String command) {
        if (command.indexOf("USER:") != -1) {
            int colonLoc = command.indexOf(":");
            int tildeLoc = command.indexOf("~");

            if (tildeLoc != -1) {
                monPlayer = command.substring(colonLoc + 1, tildeLoc);
                String password = command.substring(tildeLoc + 1);

                TrekJDBCInterface dbInt = new TrekJDBCInterface();
                boolean result = dbInt.doesPlayerPasswordMatch(monPlayer, password);

                if (result) {
                    loggedIn = true;
                    addToOutputBuffer("USER:ACK\r");

                    if (dbInt.isPlayerAdmin(monPlayer))
                        adminMon = true;
                } else {
                    addToOutputBuffer("USER:FAIL_NO_MATCHING_USER(" + monPlayer + "~" + password + ")\r");
                }
            } else {
                addToOutputBuffer("USER:FAIL_NO_TILDE(username~password)\r");
            }

        }
    }

    // handle client requests
    private void processCommand(String command) {
        if (command.equalsIgnoreCase("DISC")) {
            // disconnect
            addToOutputBuffer("DISC:ACK\r\n");
            doDisconnect = true;
        } else if (command.equalsIgnoreCase("SHIPS")) {
            // retrieve ships
            sendShips();
        } else if (command.equalsIgnoreCase("MON")) {
            // retrieve list of people monitoring
            sendMonitors();
        } else if (command.equalsIgnoreCase("COUNT")) {
            // retrieve ship count
            sendShipCount();
        } else if (command.equalsIgnoreCase("BOT")) {
            // retrieve bot count
            sendBotCount();
        } else if (command.equalsIgnoreCase("START")) {
            // send initial ships, monitors, player count, and bots
            sendShips();
            sendMonitors();
            sendShipCount();
            sendBotCount();
        }
    }

    protected void kill() {
        TrekLog.logMessage("Removing client monitor connection from: " + socket.getInetAddress().toString());
        TrekServer.serverMon.removeClient(this);

        try {
            if (socket.isConnected()) {
                addToOutputBuffer("DISC:ACK\r");
                out.flush();
            }
            socket.close();
        } catch (IOException e) {
            TrekLog.logException(e);
        }
    }

    protected void sendShips() {
        addToOutputBuffer("SHIPS:" + TrekServer.serverMon.getShipNames() + "\r");
    }

    protected void sendMonitors() {
        addToOutputBuffer("MON:" + TrekServer.serverMon.getMonitorNames() + "\r");
    }

    protected void sendShipCount() {
        addToOutputBuffer("COUNT:" + TrekServer.serverMon.getPlayerCount() + "\r");
    }

    protected void sendBotCount() {
        addToOutputBuffer("BOT:" + TrekServer.serverMon.getBotCount() + "\r");
    }

    protected boolean isAdmin() {
        return adminMon;
    }

    protected boolean isLoggedIn() {
        return loggedIn;
    }

}
