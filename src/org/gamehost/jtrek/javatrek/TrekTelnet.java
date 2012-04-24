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
 * Telnet Stuff.
 *
 * @author Joe Hopkinson
 */
public class TrekTelnet {
    public static final int IAC = 255;
    public static final int WILL = 251;
    public static final int DONT = 254;
    public static final int WONT = 252;
    public static final int DO = 253;

    public static final int ECHO = 1;
    public static final int SUPPRESS_GOAHEAD = 3;
    public static final int TIMING_MARK = 6;
    public static final int LINEMODE = 34;
    public static final int TERMINALTYPE = 24;
    public static final int INTERRUPT_PROCESS = 244;

    private int[] negotiationParameters = new int[3];
    private int commandCount = 0;

    private boolean willSuppressGoAhead = false;
    private boolean wontEcho = false;

    public TrekTelnet() {
    }

    public char[] negotiate(int command) {
        char[] response;

        negotiationParameters[commandCount] = command;
        commandCount++;

        if (commandCount >= 3) {
            commandCount = 0;
            TrekLog.logMessage("<<< Telnet Negotiation Receive: " + getNegotiationString(negotiationParameters[0]) + " " + getNegotiationString(negotiationParameters[1]) + " " + getNegotiationString(negotiationParameters[2]));

            // ACCEPT or DECLICE Dos
            if (negotiationParameters[1] == DO) {
                if (negotiationParameters[2] == SUPPRESS_GOAHEAD) {
                    willSuppressGoAhead = true;
                    negotiationParameters = new int[3];
                    response = new char[]{(char) IAC, (char) WILL, (char) SUPPRESS_GOAHEAD};
                    outputResponse(response);
                    return response;
                } else if (negotiationParameters[2] == ECHO) {
                    wontEcho = true;
                    negotiationParameters = new int[3];
                    response = new char[]{(char) IAC, (char) WILL, (char) ECHO};
                    outputResponse(response);
                    return response;
                } else if (negotiationParameters[2] == LINEMODE) {
                    willSuppressGoAhead = true;
                    negotiationParameters = new int[3];
                    response = new char[]{(char) IAC, (char) WONT, (char) LINEMODE};
                    outputResponse(response);
                    return response;
                } else if (negotiationParameters[2] != 3 && negotiationParameters[2] != 1 && negotiationParameters[2] != 34) {
                    response = new char[]{(char) 255, (char) 254, (char) negotiationParameters[2]};
                    negotiationParameters = new int[3];
                    outputResponse(response);
                    return response;
                }
            }

            // Decline WILLS..
            if (negotiationParameters[1] == WILL) {
                if (negotiationParameters[2] == ECHO) {
                    willSuppressGoAhead = true;
                    negotiationParameters = new int[3];
                    response = new char[]{(char) IAC, (char) DO, (char) ECHO};
                    outputResponse(response);
                    return response;
                } else if (negotiationParameters[2] == SUPPRESS_GOAHEAD) {
                    willSuppressGoAhead = true;
                    negotiationParameters = new int[3];
                    response = new char[]{(char) IAC, (char) DO, (char) SUPPRESS_GOAHEAD};
                    outputResponse(response);
                    return response;
                } else if (negotiationParameters[2] == LINEMODE) {
                    negotiationParameters = new int[3];
                    response = new char[]{(char) IAC, (char) DONT, (char) LINEMODE};
                    outputResponse(response);
                    return response;
                } else if (negotiationParameters[2] != 3 && negotiationParameters[2] != 1 && negotiationParameters[2] != 34) {
                    response = new char[]{(char) IAC, (char) DONT, (char) negotiationParameters[2]};
                    negotiationParameters = new int[3];
                    outputResponse(response);
                    return response;
                }
            }

            // Decline WILLS..
            if (negotiationParameters[1] == WONT) {
                if (negotiationParameters[2] == LINEMODE) {
                    return new char[]{'\0', '\0', '\0'};
                } else {
                    return new char[]{'\0', '\0', '\0'};
                }
            }

        } else if (commandCount == 2) {
            if (negotiationParameters[2] == INTERRUPT_PROCESS) {
                commandCount = 0;

            }
        }

        return new char[0];
    }

    public void outputResponse(char[] responseIn) {
        TrekLog.logMessage(">>> Telnet Negotiation Send: " + getNegotiationString(responseIn[0]) + " " + getNegotiationString(responseIn[1]) + " " + getNegotiationString(responseIn[2]));
    }

    private String getNegotiationString(int command) {
        switch (command) {
            case 255:
                return "IAC";
            case 254:
                return "DONT";
            case 253:
                return "DO";
            case 252:
                return "WONT";
            case 251:
                return "WILL";
            case 250:
                return "SB";
            case 249:
                return "GOAHEAD";
            case 248:
                return "ERASELINE";
            case 247:
                return "ERASECHARACTER";
            case 246:
                return "AREYOUTHERE";
            case 245:
                return "ABORTOUTPUT";
            case 244:
                return "INTERRUPTPROCESS";
            case 243:
                return "BREAK";
            case 242:
                return "DATAMARK";
            case 241:
                return "NOP";
            case 240:
                return "SE";
            case 0:
                return "BINARYTRANSMISSION";
            case 1:
                return "ECHO";
            case 2:
                return "RECONNECTION";
            case 3:
                return "SUPPRESSGOAHEAD";
            case 4:
                return "APPOXMSGSIZE";
            case 5:
                return "STATUS";
            case 6:
                return "TIMINGMARK";
            case 7:
                return "REMOTECONTROLTRANSECHO";
            case 8:
                return "OUTPUTLINEWIDTH";
            case 9:
                return "OUTPUTPAGESIZE";
            case 10:
                return "CRDISPOSITION";
            case 11:
                return "HORIZTABSTOPS";
            case 12:
                return "HORIZTABDISPOSITION";
            case 13:
                return "FORMFEEDDISPOSITION";
            case 14:
                return "VERTTABSTOPS";
            case 15:
                return "OUTPUTVERTICALTABDISPOSITION";
            case 16:
                return "OUTPUTLINEFEEDDISPOSITION";
            case 17:
                return "EXTENDEDASCII";
            case 18:
                return "LOGOUT";
            case 19:
                return "BYTEMACRO";
            case 20:
                return "DATAENTRYTERMINAL";
            case 21:
                return "SUPDUP";
            case 22:
                return "SUPDUPOUTPUT";
            case 23:
                return "SENDLOCATION";
            case 24:
                return "TERMINALTYPE";
            case 25:
                return "ENDOFRECORD";
            case 26:
                return "TACACSUSERIDENTIFICATION";
            case 27:
                return "OUTPUTMARKING";
            case 28:
                return "TERMINALLOCATIONNUMBER";
            case 29:
                return "TELNET3270REGIME";
            case 30:
                return "X.3PAD";
            case 31:
                return "NEGOTIATEABOUTWINDOWSIZE";
            case 32:
                return "TERMINALSPEED";
            case 33:
                return "REMOTEFLOWCONTROL";
            case 34:
                return "LINEMODE";
            case 35:
                return "XDISPLAYLOCATION";
            case 36:
                return "TELNETENVIRONMENTOPTION";
            case 37:
                return "AUTHENTICATIONOPTION";
            case 39:
                return "TELNETENVIRONMENTOPTION";
            case 40:
                return "TN3270ENHANCEMENTS";
            case 41:
                return "TELNETXAUTH";
            case 42:
                return "TELNETCHARSET";
            case 43:
                return "TELNETREMOTESERIALPORT";
            case 44:
                return "TELNETCOMPORTCONTROL";
            case 45:
                return "TELNETSUPPRESSLOCALECHO";
            case 46:
                return "TELNETSTARTTLS";
            case 47:
                return "TELNETKERMIT";
            case 48:
                return "SENDURL";

            default:
                return "UNKNOWNCOMMAND:" + command;
        }
    }

    public boolean clientChecksOut() {
        if (willSuppressGoAhead && wontEcho)
            return true;
        else
            return false;
    }
}
