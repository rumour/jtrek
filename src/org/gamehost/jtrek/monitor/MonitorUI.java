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
package org.gamehost.jtrek.monitor;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class MonitorUI extends JFrame {
	private static final long serialVersionUID = 4991642289515404201L;
	MigLayout layout = new MigLayout();
    JMenuBar menuBar = new JMenuBar();
    JScrollPane shipScrollPane = new JScrollPane();
    JScrollPane playerScrollPane = new JScrollPane();
    JLabel shipLabel = new JLabel();
    JLabel playerLabel = new JLabel();
    JList shipList = new JList();
    JList playerList = new JList();
    JScrollPane messageScrollPane = new JScrollPane();
    JLabel messageLabel = new JLabel();
    JTextArea messageTextArea = new JTextArea();
    JLabel shipCountLabel = new JLabel();
    JMenu editMenu = new JMenu();
    JMenuItem optionsMI = new JMenuItem();
    JMenu fileMenu = new JMenu();
    JMenuItem exitMI = new JMenuItem();
    OptionsDialog dlg;

    private String buffer = "";
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private boolean isConnected;
    private boolean loggedIn = false;
    private boolean getInitialData = false;

    private String hostName;
    private String portNumber;
    private String loginUser;
    private String loginPwd;

    public MonitorUI() {
        try {
            initUI();
            dlg  = new OptionsDialog(this);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void show() {
        super.show();

        launchOptions();
        doConnect();
    }

    private void initUI() throws Exception {
        JPanel panel = new JPanel(new MigLayout());
        panel.setMinimumSize(new Dimension(680, 275));
        getContentPane().setLayout(layout);

        shipLabel.setText("Active Ships");
        playerLabel.setText("Players Monitoring");
        messageLabel.setText("Messages");

        messageTextArea.setToolTipText("");
        messageTextArea.setEditable(false);
        messageTextArea.setText("");
        messageTextArea.setFont(new java.awt.Font("Courier New", 0, 10));

        shipCountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        shipCountLabel.setText("Disconnected");

        fileMenu.setText("File");
        editMenu.setText("Edit");
        exitMI.setText("Exit");
        optionsMI.setText("Options ...");

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
                doExit();
            }
        });

        exitMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                doExit();
            }
        });

        optionsMI.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                launchOptions();
            }
        });

        menuBar.add(fileMenu);
        //menuBar.add(editMenu);
        editMenu.add(optionsMI);
        fileMenu.add(exitMI);

        getContentPane().add(menuBar);
        this.getContentPane().add(shipCountLabel);
        this.getContentPane().add(shipLabel);
        this.getContentPane().add(shipScrollPane);
        this.getContentPane().add(playerLabel);
        this.getContentPane().add(playerScrollPane);
        this.getContentPane().add(messageLabel);
        this.getContentPane().add(messageScrollPane);

        messageScrollPane.getViewport().add(messageTextArea, null);
        playerScrollPane.getViewport().add(playerList, null);
        shipScrollPane.getViewport().add(shipList, null);

        setTitle("JTrek Monitor");
        setResizable(false);

        pack();
    }

    private boolean bufferInput(int x) {
		// Intercept carriage returns.
		if (x == 13)
			return true;
		if ((x != 0 && x >= 32 && x <= 126) || x == 32) {
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
                    if (!socket.isClosed()) x = in.read();

                    if (x == 10)
                        x = 0;

                    // If the input is -1 then the socket is probably bad.
                    if (x == -1)
                        return null;

                    if (!waitForCarriageReturn && x != 0)
                        return Character.toString((char) x);

                    if (bufferInput(x))
                        break;

                }
                catch (SocketTimeoutException ste) {
                    return null;
                }
                catch (SocketException se) {
                    se.printStackTrace();
                    socket.close();
                    this.dispose();
                }
                catch (Exception e) {
                    e.printStackTrace();
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
        }
        catch (SocketException se) {
            se.printStackTrace();
            return null;
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
            return null;
        }
        catch (Exception re) {
            re.printStackTrace();
            return null;
        }
    }

    public void processData(String data) {
        //System.out.println(data);
        if (data.indexOf("DISC:ACK") != -1) {
            // disconnected from server
            isConnected = false;
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else if (data.indexOf("SHIPS:") != -1) {
            // parse ship data
            int colonLoc = data.indexOf(":");
            int tildeLoc = data.indexOf("~");

            if (tildeLoc != -1) {  // at least 1 ship
                String ships = data.substring(colonLoc + 1);
                String[] shipArray = ships.split("~");
                shipList = new JList(shipArray);
            } else {
                shipList = new JList();
            }

            shipScrollPane.getViewport().add(shipList, null);
        }
        else if (data.indexOf("MON:") != -1) {
            // parse people monitoring
            int colonLoc = data.indexOf(":");
            int tildeLoc = data.indexOf("~");

            if (tildeLoc != -1) {  // at least 1 player
                String players = data.substring(colonLoc + 1);
                String[] playerArray = players.split("~");
                playerList = new JList(playerArray);
            } else {
                playerList = new JList();
            }

            playerScrollPane.getViewport().add(playerList, null);
        }
        else if (data.indexOf("COUNT:") != -1) {
            // parse ship count
            int colonLoc = data.indexOf(":");
            int ships = new Integer(data.substring(colonLoc + 1)).intValue();
            if (ships == 1) {
                shipCountLabel.setText("" + ships + " Active Ship");
            } else {
                shipCountLabel.setText("" + ships + " Active Ships");
            }
        }
        else if (data.indexOf("BOT:") != -1) {
            // parse bot count
            int colonLoc = data.indexOf(":");
            int bots = new Integer(data.substring(colonLoc + 1)).intValue();
            if (bots == 1) {
                shipCountLabel.setText(shipCountLabel.getText() + " (" + bots + " bot)");
            } else {
                shipCountLabel.setText(shipCountLabel.getText() + " (" + bots + " bots)");
            }
        }
        else if (data.indexOf("MSG:") != -1) {
            // parse message
            int colonLoc = data.indexOf(":");
            String msgStr = data.substring(colonLoc + 1);
            messageTextArea.append(msgStr + "\r\n");
        }

    }

    private void login(String data) {
        //System.out.println(data);
        if (data.equals("USER:ACK")) {
            loggedIn = true;
        } else {
            isConnected = false;
            messageTextArea.append("*** Problem logging into the monitor server with the specified username and password. ***\r\n" + data + "\r\n");
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void doExit() {
        if (isConnected) {
            doDisconnect();
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    private void doDisconnect() {
        try {
            out.write("DISC\r".getBytes());
            out.flush();
        } catch (SocketException se) {
            se.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            isConnected = false;
            loggedIn = false;
            getInitialData = false;

            shipCountLabel.setText("Disconnected");
            shipList = new JList();
            shipScrollPane.getViewport().add(shipList, null);
            playerList = new JList();
            playerScrollPane.getViewport().add(playerList, null);
        }
    }

    private void doConnect() {
        hostName = dlg.addressField.getText();
        portNumber = dlg.portField.getText();
        loginUser = dlg.playerField.getText();
        loginPwd = new String(dlg.passwordField.getPassword());

        if (hostName == null || portNumber == null || loginUser == null || loginPwd == null ||
                hostName.equals("") || portNumber.equals("") || loginUser.equals("") || loginPwd.equals("")) {
            messageTextArea.append("*** ERROR: need to fill out all fields on options screen before connecting. ***\r\n");
            return;
        }

        try {
            socket = new Socket(hostName, new Integer(portNumber).intValue());

            while (!socket.isConnected()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            isConnected = true;
            shipCountLabel.setText("Connected");

            socket.setTcpNoDelay(true);
            socket.setKeepAlive(true);
            socket.setSoTimeout(500);

            in = socket.getInputStream();
            out = socket.getOutputStream();

            String data = "";

            // send login data
            out.write(("USER:"+loginUser+"~"+loginPwd+"\r").getBytes());


            while (isConnected) {
                if (loggedIn && !getInitialData) {
                    out.write("START\r".getBytes());
                    getInitialData = true;
                }

                if (in.available() != -1) {
                    data = getBlockedInput(true);
                }
                if (data != null && !(data.equals("")) ) {
                    if (loggedIn) {
                        processData(data);
                    } else {
                        login(data);
                    }

                    data = "";
                    buffer="";
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }

            }
        } catch (UnknownHostException u) {
            u.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    private void launchOptions() {
        Dimension dlgSize = dlg.getPreferredSize();
        Dimension frmSize = getSize();
        Point loc = getLocation();
        dlg.setLocation((frmSize.width - dlgSize.width) / 2 + loc.x, (frmSize.height - dlgSize.height) / 2 + loc.y);
        dlg.setModal(true);
        dlg.pack();
        dlg.show();
    }
}

