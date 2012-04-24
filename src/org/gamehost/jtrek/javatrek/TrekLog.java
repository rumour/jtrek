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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Main logging class for the game server.
 *
 * @author Joe Hopkinson
 */
public final class TrekLog {
    private static File logFile;
    private static File connectLog;
    private static File messageLog;

    private static FileWriter logOut;
    private static FileWriter connectOut;
    private static FileWriter messageOut;

    private static boolean debug;

    public TrekLog() {
    }

    /**
     * Logs a message to System.out, and to the open log file.
     *
     * @param thisMessage The message to be logged.
     */
    public synchronized static void logMessage(String thisMessage) {
        try {
            thisMessage = getDateString() + thisMessage;
            System.out.println(thisMessage);
            logOut.write(thisMessage + "\r\n");
            logOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs a message to System.out, and to the open log file.
     *
     * @param thisMessage The message to be logged.
     */
    public synchronized static void logDebug(String thisMessage) {
        try {
            if (!debug)
                return;

            System.out.println("DEBUG: " + thisMessage);
            logOut.write("DEBUG: " + thisMessage + "\r\n");
            logOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Logs an error to System.out and to the open log file.
     *
     * @param thisError The error to be logged.
     */
    protected synchronized static void logError(String thisError) {
        try {
            TrekLog.logMessage("ERROR: " + thisError);
            logOut.write("ERROR: " + thisError + "\r\n");
            logOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Logs an Exception to System.out and to the open log file.
     *
     * @param thisException The exception to be logged.
     */
    public synchronized static void logException(Exception thisException) {
        try {
            thisException.printStackTrace();

            StringWriter sWriter = new StringWriter();
            thisException.printStackTrace(new PrintWriter(sWriter));
            logMessage(sWriter.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the log file.
     */
    protected synchronized static void openLogFile(boolean debugin) {
        try {
            // Get our next logfile index.
            int x = 1000;

            // make sure log directory exists
            File logDir = new File("./log");
            if (!logDir.exists()) {
                boolean result = logDir.mkdirs();
                if (!result) {
                    throw new RuntimeException("Unable to create log directory in current folder.");
                }
            }

            do {
                logFile = new File("./log/" + x + ".log");

                if (!logFile.exists())
                    break;

                x++;
            } while (logFile.exists());

            connectLog = new File("./log/" + x + "_connections.log");
            messageLog = new File("./log/" + x + "_messages.log");

            logFile.createNewFile();
            connectLog.createNewFile();
            messageLog.createNewFile();


            System.out.println("Opened log file: " + logFile.getAbsolutePath());
            System.out.println("Opened connect log file: " + connectLog.getAbsolutePath());
            System.out.println("Opened message log file: " + messageLog.getAbsolutePath());

            // Setup the file writers.
            logOut = new FileWriter(logFile, true);
            connectOut = new FileWriter(connectLog, true);
            messageOut = new FileWriter(messageLog, true);

            // Set our debug flag.
            if (debugin)
                debug = true;
            else
                debug = false;
        } catch (Exception e) {
            System.out.println("Error opening log.");
            e.printStackTrace();
        }
    }

    protected static synchronized String getDateString() {
        Calendar cal = Calendar.getInstance();
        Date date = cal.getTime();
        SimpleDateFormat format = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss': '");
        return format.format(date);
    }

    /**
     * Closes the log file.
     */
    protected synchronized static void closeLogFile() {
        try {
            logOut.close();
            connectOut.close();
            messageOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void logTextMessage(String thisMessage) {
        try {
            TrekLog.logMessage(thisMessage);
            messageOut.write(getDateString() + ": " + thisMessage + "\n\r");
            messageOut.flush();
        } catch (IOException e) {
            TrekLog.logException(e);
        }
    }

    public static void logConnection(String thisMessage) {
        try {
            TrekLog.logMessage(thisMessage);
            connectOut.write(getDateString() + ": " + thisMessage + "\n\r");
            connectOut.flush();
        } catch (IOException e) {
            TrekLog.logException(e);
        }
    }
}
