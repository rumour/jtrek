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

import java.net.Socket;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Mar 8, 2004
 * Time: 11:37:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class TrekMonitor {
    private Hashtable clients;
    private int playerCount;
    private int botCount;
    private String shipNames = "";
    private String playerNames = "";
    private boolean updateShips = false;
    private boolean updatePlayers = false;


    public TrekMonitor() {
        clients = new Hashtable();
    }

    protected void addClient(Socket client) {
        //if (clients.containsKey(client.getInetAddress().toString())) {
        // only 1 listener client per IP?
        //    return;
        //}

        TrekMonitorClient tmc = new TrekMonitorClient(client);
        clients.put(client.getInetAddress().toString(), tmc);
        tmc.start();
    }

    protected void removeClient(TrekMonitorClient client) {
        if (clients.containsKey(client.socket.getInetAddress().toString())) {
            clients.remove(client.socket.getInetAddress().toString());
        }
    }

    public void updatePlayerData() {
        int count = 0;
        playerCount = 0;
        botCount = 0;
        StringBuffer sb = new StringBuffer();

        for (java.util.Enumeration e = TrekServer.players.elements(); e.hasMoreElements(); ) {
            TrekPlayer curPlyr = (TrekPlayer) e.nextElement();
            if (curPlyr.state != TrekPlayer.WAIT_PLAYING) continue;
            playerCount++;

            if (curPlyr instanceof TrekBot) {
                botCount++;
            }

            sb.append(curPlyr.shipName + "~");
            count++;
        }

        if (sb.toString().equals(shipNames)) {
            updateShips = false;
        } else {
            updateShips = true;
            shipNames = sb.toString();
        }

        sb = new StringBuffer();
        for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
            TrekMonitorClient curClient = (TrekMonitorClient) e.nextElement();

            if (curClient.isLoggedIn()) {
                sb.append(curClient.monPlayer + "~");
            }
        }

        if (sb.toString().equals(playerNames)) {
            updatePlayers = false;
        } else {
            updatePlayers = true;
            playerNames = sb.toString();
        }
    }

    public String getShipNames() {
        return shipNames;
    }

    public String getMonitorNames() {
        return playerNames;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getBotCount() {
        return botCount;
    }

    public boolean doUpdateShips() {
        return updateShips;
    }

    public boolean doUpdatePlayers() {
        return updatePlayers;
    }

    protected void doOutputBuffers() {
        updatePlayerData();

        for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
            TrekMonitorClient curClient = (TrekMonitorClient) e.nextElement();

            if (doUpdateShips()) {
                if (curClient.isLoggedIn()) {
                    curClient.sendShips();
                    curClient.sendShipCount();
                    curClient.sendBotCount();
                }
            }

            if (doUpdatePlayers()) {
                if (curClient.isLoggedIn()) {
                    curClient.sendMonitors();
                }
            }

            curClient.sendOutputBuffer();
        }

        updateShips = false;
    }

    protected void addToOutputBuffer(String s) {
        for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
            TrekMonitorClient curClient = (TrekMonitorClient) e.nextElement();

            if (curClient.isLoggedIn()) curClient.addToOutputBuffer(s);
        }
    }

    protected void addToAdminOutputBuffer(String s) {
        for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
            TrekMonitorClient curClient = (TrekMonitorClient) e.nextElement();
            if (!curClient.isAdmin()) continue;

            if (curClient.isLoggedIn()) curClient.addToOutputBuffer(s);
        }
    }

    public void shutdownConnections() {
        for (Enumeration e = clients.elements(); e.hasMoreElements(); ) {
            TrekMonitorClient tmc = (TrekMonitorClient) e.nextElement();

            tmc.kill();
        }
    }
}
