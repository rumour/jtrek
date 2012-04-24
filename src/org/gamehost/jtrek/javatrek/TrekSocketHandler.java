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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * SocketHandler is a socket handler that handles communication between the
 * game and client.
 *
 * @author Joe Hopkinson
 */
public class TrekSocketHandler extends Thread {
    // A handle to the Java socket.
    private Socket socket;

    // Input and Output buffers.
    public String inputBuffer;
    public String outputBuffer;

    // Input and Output Streams.
    private InputStream input;
    private OutputStream output;

    // States
    public static final int STATE_RUNNING = 1;
    public static final int STATE_KILL = 2;
    public static final int STATE_DEAD = 3;
    public static final int STATE_RECEIVING = 4;
    public static final int STATE_SENDING = 5;

    public boolean dataWaiting = false;

    public int state;

    public boolean telnetNegotiation = false;
    public boolean handleTelnetNegotiation;
    public TrekTelnet telnet;

    public TrekSocketHandler(Socket thisSocket, boolean handleTelnetNegotiationIn) {
        try {
            socket = thisSocket;
            handleTelnetNegotiation = handleTelnetNegotiationIn;
            telnet = new TrekTelnet();

            TrekLog.logMessage("Initializing InputBuffer...");
            inputBuffer = "";

            TrekLog.logMessage("Initializing OutputBuffer...");
            outputBuffer = "";

            TrekLog.logMessage("Getting InputStream...");
            input = socket.getInputStream();

            TrekLog.logMessage("Getting OutputStream...");
            output = socket.getOutputStream();

            socket.setKeepAlive(true);
            socket.setSoTimeout(10);
        } catch (Exception e) {
            TrekLog.logMessage("Exception in SocketHandler constructor.");
            TrekLog.logException(e);
        }
    }

    public void run() {
        try {
            state = STATE_RUNNING;

            if (handleTelnetNegotiation) {
                TrekLog.logMessage("Sending telnet negotiation parameters...");

                byte[] commands = new byte[3];

                commands[0] = (byte) TrekTelnet.IAC;
                commands[1] = (byte) TrekTelnet.WILL;
                commands[2] = (byte) TrekTelnet.SUPPRESS_GOAHEAD;
                output.write(commands);
                output.flush();

                commands[0] = (byte) TrekTelnet.IAC;
                commands[1] = (byte) TrekTelnet.WILL;
                commands[2] = (byte) TrekTelnet.ECHO;
                output.write(commands);
                output.flush();

                commands[0] = (byte) TrekTelnet.IAC;
                commands[1] = (byte) TrekTelnet.DONT;
                commands[2] = (byte) TrekTelnet.LINEMODE;
                output.write(commands);
                output.flush();

                // For that bitch ass MicroFUCKINGSOFT Telnet client... cocksuckers.
                commands[0] = (byte) TrekTelnet.IAC;
                commands[1] = (byte) TrekTelnet.WONT;
                commands[2] = (byte) TrekTelnet.TIMING_MARK;
                output.write(commands);
                output.flush();

            }

            do {
                if (state == STATE_KILL)
                    break;

                try {
                    try {
                        int inputByte = input.read();

                        if (inputByte == -1) {
                            state = STATE_KILL;
                            TrekLog.logMessage("The connection was closed on the client end.");
                            break;
                        }

                        if (inputByte != 0) {
                            state = STATE_RECEIVING;

                            if (handleTelnetNegotiation) {
                                // Telnet negotiation capture.
                                if (inputByte == 255) {
                                    telnetNegotiation = true;
                                }

                                if (telnetNegotiation) {
                                    char[] response = telnet.negotiate((char) inputByte);
                                    if (response.length == 3) {
                                        if (response[0] != '\0') {
                                            byte[] responseBytes = new byte[3];

                                            for (int y = 0; y < response.length; y++) {
                                                responseBytes[y] = (byte) response[y];
                                            }

                                            output.write(responseBytes);
                                            output.flush();
                                        }

                                        telnetNegotiation = false;
                                    }

                                    if (inputByte < 240 && inputByte > 48)
                                        telnetNegotiation = false;

                                    inputByte = 0;
                                }

                            }

                            if (inputByte != 0 && !telnetNegotiation) {
                                inputBuffer += new Character((char) inputByte).toString();
                                dataWaiting = true;
                            }
                            state = STATE_RUNNING;
                        }

                    } catch (SocketTimeoutException ste) {
                        // Ignore socket timeouts.
                    }

                    // Expunge the output buffer if there is data waiting.
                    if (!outputBuffer.equals("")) {
                        state = STATE_SENDING;

                        output.write(outputBuffer.getBytes());
                        output.flush();

                        outputBuffer = "";

                        state = STATE_RUNNING;
                    }

                    Thread.sleep(1);
                } catch (InterruptedException ie) {
                    TrekLog.logMessage("Thread interrupted.  Ending session.");
                    break;
                }
            }
            while (state != STATE_KILL);

            TrekLog.logMessage("Shutting down InputStream...");
            input.close();

            TrekLog.logMessage("Shutting down OutputStream...");
            output.close();

            TrekLog.logMessage("Shutting down Socket...");
            socket.close();

            state = STATE_DEAD;
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    public void sendData(byte[] dataArray) {
        outputBuffer += new String(dataArray);
    }

    public byte[] getData() {
        byte[] returnData = inputBuffer.getBytes();
        inputBuffer = "";
        dataWaiting = false;

        return returnData;
    }

    public byte getDataSingleByte() {
        byte returnByte = inputBuffer.getBytes()[0];
        inputBuffer = inputBuffer.substring(1);

        if (inputBuffer.equals(""))
            dataWaiting = false;

        return returnByte;
    }

    public void endSession() {
        state = STATE_KILL;
        this.interrupt();
    }

    public String getIPAddress() {
        try {
            String addr = socket.getInetAddress().toString();
            return addr.substring(1);
        } catch (Exception e) {
            TrekLog.logException(e);
        }

        return "Unknown.";
    }
}
