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

import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * TrekMain is the main application class for the game, and takes care of
 * starting the server thread.
 *
 * @author Joe Hopkinson
 */
public final class TrekMain {
    protected static int port;
    protected static int rawPort;
    protected static int monPort;
    protected static TrekPropertyReader tpr = TrekPropertyReader.getInstance();

    public static void main(String args[]) {
        // get port from properties file
        try {
            port = new Integer(tpr.getValue("game.port"));
            rawPort = new Integer(tpr.getValue("data.port"));
            monPort = new Integer(tpr.getValue("monitor.port"));
        } catch (Exception e) {
            showUsage();
        }

        // Load up the server, man.
        try {
            // main server port; typically 1701
            ServerSocket sckServer = new ServerSocket(port);
            Socket sckClient;
            sckServer.setSoTimeout(1000);

            // raw server port; typically 1702
            ServerSocket rawServer = new ServerSocket(rawPort);
            Socket rawClient;
            rawServer.setSoTimeout(1000);

            // add another socket for 'monitor' application; typically 1710
            ServerSocket monServer = new ServerSocket(monPort);
            Socket monClient;
            monServer.setSoTimeout(1000);

            // Create our server.
            boolean doDebug = false;
            if (tpr.getValue("game.debug").equals("true"))
                doDebug = true;
            TrekServer server = new TrekServer(doDebug);
            server.start();

            System.out.println("JavaTrek Server bound to port " + port + ".");

            do {
                try {
                    sckClient = sckServer.accept(); // wait for connection
                    TrekServer.add(sckClient, server); // when connection established add it to the interactive service
                    TrekLog.logConnection("Connection established: " + sckClient.toString()); // output to log file
                } catch (java.net.SocketTimeoutException s) {
                    // nothing
                }

                try {
                    rawClient = rawServer.accept();
                    TrekServer.addRaw(rawClient, server);
                    TrekLog.logConnection("Raw connection established: " + rawClient.toString());
                } catch (java.net.SocketTimeoutException s) {
                    // nothing
                }

                try {
                    monClient = monServer.accept();
                    TrekServer.addMonitor(monClient);
                    TrekLog.logConnection("Monitor established: " + monClient.toString());
                } catch (java.net.SocketTimeoutException s) {
                    // nothing
                }
            }
            while (true); // infinite loop accepting connections forever
        } catch (BindException be) {
            System.out.println("There was an error binding to port " + port + ".  Port already in use.");
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    protected static void showUsage() {
        System.out.println("JavaTrek Usage: java org.gamehost.jtrek.javatrek.TrekMain");
        System.exit(0);
    }
}