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

import java.io.File;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

// TODO: if keymap has zZ, need to trigger loss of scan
// DONE? // TODO: when checking in, need to apply any changes to Ship/Player to the 'raw' classes

/**
 * TrekServer is the main class for the game server, and is responsible for
 * player management.
 *
 * @author Joe Hopkinson
 */
public final class TrekServer extends Thread {
    protected static Vector players;
    public static Hashtable quadrants;
    protected static Timer tickTimer;
    protected static Vector deletionTimers;
    protected static long startTime;
    protected static boolean serverShuttingDown = false;
    private static boolean teamPlayEnabled = false;
    protected static TrekTeamStats[] teamStats;
    protected static TrekJDBCInterface dbInt;
    protected static TrekMonitor serverMon;

    private String consoleCommand = "";

    private static boolean botRespawnEnabled = true;
    private static int maxBotCount = 8;
    private static boolean logBotHud = false;
    // should be < 3600; time to wait until checking to spawn bots, in seconds
    private static int botRespawnTime = 60;
    public static int botPlayerId = -1;
    private static boolean enableAnonymousShips = false;
    private static boolean autoSpawnThx = false;

    private static TrekPropertyReader tpr = TrekPropertyReader.getInstance();

    public TrekServer() {
    }

    /**
     * Get a new instance of TrekServer.
     *
     * @param debug "true" or "false" - Whether or not to log Debug messages.
     */
    public TrekServer(boolean debug) {
        // GNU GPL Copyright Notice.
        System.out.println("JavaTrek, Copyright (C) 2003-2007 Joseph Hopkinson, Jay Ashworth");
        System.out.println("JavaTrek comes with ABSOLUTELY NO WARRANTY.  This is free");
        System.out.println("software, and you are welcome to redistribute it under certain");
        System.out.println("conditions; for details please refer to the COPYING file");
        System.out.println("provided with this distribution.");
        System.out.println("");
        System.out.println("");
        System.out.println("");
        // END GNU GPL Copyright Notice.


        // Initialize the quadrants hashtable.
        quadrants = new Hashtable();
        deletionTimers = new Vector();

        Calendar cal = Calendar.getInstance();
        startTime = cal.getTimeInMillis();

        // Open the log.
        TrekLog.openLogFile(debug);

        // initiate an interface to the database
        dbInt = new TrekJDBCInterface();

        serverMon = new TrekMonitor();

        // check properties, override server bot defaults
        botRespawnEnabled = tpr.getValue("bot.doRespawn").equals("true");
        maxBotCount = new Integer(tpr.getValue("bot.maxBots"));
        botRespawnTime = new Integer(tpr.getValue("bot.respawnTime"));
        logBotHud = Boolean.valueOf(tpr.getValue("bot.drawHud"));
        botPlayerId = new Integer(tpr.getValue("bot.playerId"));

        // check properties to enable anonymous play
        enableAnonymousShips = Boolean.valueOf(tpr.getValue("server.anonymousPlay"));

        // check to see if thx robot ship should be auto launched
        autoSpawnThx = Boolean.valueOf(tpr.getValue("thx.autoSpawn"));
    }

    /* (non-Javadoc)
      * @see java.lang.Runnable#run()
      */
    public void run() {
        try {
            // Create our directory structure, if it does not exist.
            TrekDataInterface di = new TrekDataInterface();
            di.createDirectoryStructure();

            teamStats = new TrekTeamStats[10];

            for (int x = 0; x < 10; x++) {
                teamStats[x] = new TrekTeamStats();
            }

            /*
                * TODO: Move data into files, rather than in constructor.
                */
            players = new Vector();

            TrekQuadrant quadrant = new TrekQuadrant(this);

            quadrant.name = "Alpha Quadrant";
            quadrant.addObject(new TrekStarbase(-6150, -13432, -300, "Spacelab Regula 1", "1", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(7803, -10300, -6540, "Starbase 2", "2", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-6000, 10020, 900, "Starbase 3", "3", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-11400, 17000, -800, "Starbase 4", "4", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-18400, 20730, 5020, "Babylon 5", "5", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-29600, 17000, -400, "Starbase 6", "6", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(21800, 16000, -3000, "Space Station K-7", "7", "L M D ", 3500, 250, 3));
            quadrant.addObject(new TrekStarbase(28000, 5000, 400, "Starbase 8", "8", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(33900, -10500, 8030, "Starbase 9", "9", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(24000, -24000, 800, "Starbase 10", "0", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekZone(-18000, -12500, -3000, "Asteroid Field", "a", 5000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekStar(2000, -3000, 20500, "Betelgeuse Star", "b"));
            quadrant.addObject(new TrekBlackhole(-34052, -17084, 2112, "Cygnus X-1", "c",
                    new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "d",
                            "e", "f", "g", "j", "k", "n", "o", "p", "q", "r", "t", "v", "w"}));
            quadrant.addObject(new TrekPlanet(-20320, -21344, -1200, "Delta Vega", "d", "C M D ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(2000, 2025, 220, "Earth", "e", "A TW ", "Earth", 2000, 100, 1));
            quadrant.addObject(new TrekZone(-18000, 4000, 6000, "Organia", "f", 2000, TrekZone.OBJ_ORGANIA));
            quadrant.addObject(new TrekPlanet(-10000, -36020, -4060, "Gorn", "g", "A ", "Gorn", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(139519, -115152, 111346, "Hyperion", "h", "C A ", "", 2000, 100, 1));
            //quadrant.addObject(new TrekComet(100000, 100000, 100000, "Comet Icarus", "i"));
            quadrant.addObject(new TrekComet(8100, 0, 40, 10000, 20341, false, "Comet Icarus 4", "i"));
            quadrant.addObject(new TrekPlanet(-21200, -23040, -4020, "Deneva", "j", "T ", "", 2000, 100, 1));
            quadrant.addObject(new PlanetKlinzhai(36030, -18080, 3000, "Klinzhai", "k", "A ", "Klinzhai", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(139988, -114758, 111123, "Lusus Minor", "l", "L T ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(136030, -118080, 113000, "Magrathea", "m", "M D ", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(-6003, -10300, -6540, "Mutara Nebula", "n", 5000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekPlanet(-39700, 16341, 7000, "Orion", "o", "A ", "Orion", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(8100, -20341, 40, "Wrigley", "p", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-17700, -9341, 3400, "Rigel XII", "q", "C ", "", 2000, 100, 1));
            quadrant.addObject(new PlanetRomulus(20100, 29200, -3230, "Romulus", "r", "A FC ", "Romulus", 2000, 100, 1));
            quadrant.addObject(new TrekZone(136500, -119962, 123574, "Sargasso Nebula", "s", 3500, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekZone(-40003, 6032, -2030, "Tholean Space", "t", 2000, TrekZone.OBJ_ZONE));
            quadrant.addObject(new TrekStarbase(134820, -117517, 109320, "Ursus Outpost", "u", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(6053, 20747, 2980, "Vulcan", "v", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekWormhole(3, -7, 2, "Worm Hole", "w"));
            Random gen = new Random();
            quadrant.addObject(new TrekSpatialAnomaly(gen.nextInt() % 100000, gen.nextInt() % 100000, gen.nextInt() % 100000, "Spatial Anomaly", "W"));

            // Observer Devices
            // place them in random orbits around key areas: earth, dv, wrigley, vulcan, rigel, sb3
            quadrant.addObject(new TrekObserverDevice("Mars Observer", "M", -17700 + randomInt(3000), -9341 + randomInt(3000), 3400 + randomInt(5000)));
            quadrant.addObject(new TrekObserverDevice("Hubble Telescope", "H", 3000 + randomInt(500), -2000 + randomInt(500), 1000 + randomInt(1000)));
            quadrant.addObject(new TrekObserverDevice("Voyager-" + (100 + Math.abs(gen.nextInt() % 899)), "V", 6053 + randomInt(2000), 20747 + randomInt(2000), 2980 + randomInt(3000)));
            quadrant.addObject(new TrekObserverDevice("Pioneer-" + (10 + Math.abs(gen.nextInt() % 89)), "P", 8100 + randomInt(2500), -20341 + randomInt(2500), 40 + randomInt(2500)));
            quadrant.addObject(new TrekObserverDevice("Skylab", "S", -20320 + randomInt(3500), -21344 + randomInt(3500), -1200 + randomInt(3000)));
            quadrant.addObject(new TrekObserverDevice("Uldarian Lens", "U", -6000 + randomInt(3500), 10020 + randomInt(3500), 900 + randomInt(3500)));

            quadrants.put("Alpha Quadrant", quadrant);

            quadrant = new TrekQuadrant(this);
            quadrant.name = "Beta Quadrant";
            quadrant.addObject(new TrekStarbase(4016, 13407, -8936, "Starbase 11", "1", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(26200, 9680, 6470, "Starbase 12", "2", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(8150, -10940, -29460, "Starbase 13", "3", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(1340, -24700, 28970, "Starbase 14", "4", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-5900, -12870, -27910, "Starbase 15", "5", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-19792, -15170, -2900, "Starbase 16", "6", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-25935, 2440, 2135, "Starbase 17", "7", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-18913, 11802, -16036, "Starbase 18", "8", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-16402, 22795, 9968, "Starbase 19", "9", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(207, 26501, 7573, "Starbase 20", "0", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-15350, 2440, 6080, "Arrakis", "a", "C ", "", 2000, 100, 1));
            quadrant.addObject(new PlanetWarring(-8970, 25670, -23180, "Brachistochrone", "b", "T N I MB ", "", 2000, 100, 1));
            quadrant.addObject(new TrekBlackhole(-22600, -27410, -6530, "Cygnus X-2", "c",
                    new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "d",
                            "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
                            "u", "v", "w", "x", "y", "z"}));
            quadrant.addObject(new TrekPlanet(1280, 24120, 21070, "Dana Planet", "d", "A ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-2050, -5460, -17900, "Encinitas", "e", "None ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(18810, -1990, 5720, "Fomalhaut", "f", "None ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-602, 8647, -1772, "Gauss", "g", "T ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-19340, -18190, -20150, "Hawserion", "h", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(20850, 7120, -1080, "Ion", "i", "FC ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-4290, -7570, 9020, "Jovia", "j", "None ", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(29700, 22220, -10670, "Kali Asteroid Zone", "k", 3000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekPlanet(-7800, -6240, -21610, "Lethargy", "l", "", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-23450, 27700, 26460, "Malibu Beta 6", "m", "A ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(2512, 5004, -3198, "Newtonium", "n", "M A ", "Klinzhai", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-16420, -1630, 12290, "Orion Outpost", "o", "None ", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(28620, -15180, 10970, "Pleasure Planet", "p", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(6330, 21880, -14960, "Quasar Epsilon", "q", 4000, TrekZone.OBJ_QUASAR));
            quadrant.addObject(new TrekZone(1743, -26267, -16435, "Rama Pulsar", "r", 4000, TrekZone.OBJ_PULSAR));
            quadrant.addObject(new TrekZone(12460, 27570, -8110, "Shiva Nebula", "s", 3500, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new PlanetWarring(-10000, 21670, -10250, "Tautochrone", "t", "C M D ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(17230, -6660, 8420, "Uhura", "u", "None ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(4459, 9388, -362, "Vishnu", "v", "C D ", "", 5000, 100, 1));
            quadrant.addObject(new TrekWormhole(12, -19, 5, "Worm Hole", "w"));
            quadrant.addObject(new TrekPlanet(-20940, 7900, -28990, "Planet X-Beta", "x", "None ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-24160, -7080, 19630, "Yaxci", "y", "None ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(20750, -12450, 7020, "Planet Z-Beta", "z", "None ", "", 2000, 100, 1));

            // set warring planets' target; brach hates taut, taut hates brach
            PlanetWarring pw = (PlanetWarring) quadrant.getObjectByScanLetter("b");
            pw.setEnemy(quadrant.getObjectByScanLetter("t"));
            pw = (PlanetWarring) quadrant.getObjectByScanLetter("t");
            pw.setEnemy(quadrant.getObjectByScanLetter("b"));

            quadrants.put("Beta Quadrant", quadrant);

            quadrant = new TrekQuadrant(this);
            quadrant.name = "Gamma Quadrant";
            quadrant.addObject(new TrekStarbase(-10860, 9250, -5420, "Starbase 21", "1", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(26850, 29350, 4140, "Starbase 22", "2", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(31150, 37900, 3890, "Starbase 23", "3", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-20590, 1960, -17960, "Starbase 24", "4", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(9860, 12250, 6840, "Starbase 25", "5", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(27410, 20000, -15710, "Starbase 26", "6", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(26040, 1450, -1600, "Starbase 27", "7", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(4650, -19120, -28250, "Starbase 28", "8", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(3600, -11770, -21750, "Starbase 29", "9", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(34050, -10640, 900, "Starbase 30", "0", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(7841, 27048, 17639, "Alpha Centauri", "a", "M D ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(10974, 30019, 21137, "Bajor", "b", "T ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(13710, 10000, -7950, "Cardassia", "c", "A ", "Cardassia", 2000, 100, 1));
            quadrant.addObject(new TrekZone(-22840, 11790, -6390, "Deneb Nebula", "d", 4000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekZone(-18930, 11290, -9010, "Encaladus Quasar", "e", 4000, TrekZone.OBJ_QUASAR));
            quadrant.addObject(new TrekPlanet(35450, 33250, 2420, "Ferrigulum", "f", "C A FR CM ", "Ferrigulum", 2000, 100, 1));
            quadrant.addObject(new TrekZone(23600, 24140, -16930, "Galileo Asteroids", "g", 3000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekComet(17542, 5798, 17348, "Halley's Comet", "h"));
            quadrant.addObject(new TrekPlanet(20030, 2320, -21220, "Irata Carinae", "i", "", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(-21250, 5950, -12210, "Juno Asteroid Zone", "j", 5000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekComet(31064, -10048, 2841, "Kahoutek Comet", "k"));
            quadrant.addObject(new TrekZone(32950, 35750, 2190, "Lagoon Nebula", "l", 4000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new PlanetMegadon(30040, -4600, -350, "Megadon", "m", "C M D T A ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-23950, 12560, -4790, "Nebulon", "n", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(7050, -13570, -25250, "Orion Nebula", "o", 6000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekPlanet(4450, -7720, -17970, "Praxis", "p", "C A ", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(33050, -11640, 1130, "Quasar 3C 273", "q", 4000, TrekZone.OBJ_QUASAR));
            quadrant.addObject(new TrekZone(8410, 11860, 9890, "Rosette Nebula", "r", 2500, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekPlanet(-14310, -8550, 10290, "Sagittarius", "s", "M ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(11795, 25850, 15998, "Triangulum", "t", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(24970, -21080, -35270, "Uranus Cygni", "u", "", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-6060, 19150, 5540, "Vidicon Beta", "v", "FC ", "", 2000, 100, 1));
            quadrant.addObject(new TrekWormhole(0, 0, 1, "Worm Hole", "w"));
            quadrant.addObject(new TrekZone(29200, 31050, 4740, "Xakaar Pulsar", "x", 4000, TrekZone.OBJ_PULSAR));
            quadrant.addObject(new TrekZone(1900, -15820, -24800, "Yxk Asteroid Zone", "y", 5000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekPlanet(-15810, 35290, -16740, "Zwicky", "z", "", "", 2000, 100, 1));
            quadrant.addObject(new TrekBlackhole(-34052, -17084, 2112, "Cygnus X-3", "C",
                    new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d",
                            "e", "f", "g", "i", "j", "l", "m", "n", "o", "p", "q", "r", "s", "t",
                            "u", "v", "w", "x", "y", "z"}));
            quadrants.put("Gamma Quadrant", quadrant);

            quadrant = new TrekQuadrant(this);
            quadrant.name = "Omega Quadrant";
            quadrant.addObject(new TrekStarbase(2399, -5793, 15137, "Starbase 31", "1", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(0, -11585, 11585, "Starbase 32", "2", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(11585, 0, 11585, "Starbase 33", "3", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(13985, -5793, 6270, "Starbase 34", "4", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(5793, 13985, 6270, "Starbase 35", "5", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-5793, 13985, -6270, "Starbase 36", "6", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-13985, -5793, -6270, "Starbase 37", "7", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-11585, 0, -11585, "Starbase 38", "8", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(0, -11585, -11585, "Starbase 39", "9", "L M D ", 2000, 100, 1));
            quadrant.addObject(new TrekStarbase(-2399, -5793, -15137, "Starbase 40", "0", "T ", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(5793, -2399, -15137, "Aay'diemm III-A", "a", "", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-5793, -2399, 15137, "Beta Carotene", "b", "FC ", "", 2000, 100, 1));
            quadrant.addObject(new TrekBlackhole(-15137, 4356, 6270, "Cygnus X-4", "c",
                    new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "d",
                            "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
                            "u", "v", "w", "x", "y", "z"}));
            quadrant.addObject(new TrekPlanet(-9448, -9448, 8928, "Dandelion Outpost", "d", "A ", "Romulus", 2000, 100, 1));
            quadrant.addObject(new TrekZone(10703, 4433, 2450, "Elysian Asteroids", "e", 3500, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekPlanet(0, -15760, 3135, "Frogstar Outpost", "f", "A", "Gorn", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(11144, -11144, -3135, "Gagaran Outpost", "g", "A ", "Earth", 2000, 100, 1));
            quadrant.addObject(new TrekZone(285, -7379, 6270, "Helix Nebula", "h", 3500, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekPlanet(768, 5206, -1320, "Iridium", "i", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(2399, 4836, 15137, "Jalapeno Nebula", "j", 3000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekPlanet(11144, 11144, 3135, "Khittomer Outpost", "k", "A ", "Klinzhai", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(6313, -9448, 11585, "Lucifer", "l", "", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(13329, -7605, -9980, "Markarian 205", "m", 2000, TrekZone.OBJ_PULSAR));
            quadrant.addObject(new TrekPlanet(0, 15760, -3135, "Nimbus Outpost", "n", "A ", "Orion", 2000, 100, 1));
            quadrant.addObject(new TrekZone(13985, -8564, -6270, "Omega Nebula", "o", 4000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekPlanet(0, 0, 16384, "Polaris", "p", "C A ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(13682, -6523, -10174, "Quetzalcoatl", "q", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(11585, 11585, 0, "Rura Penthe", "r", "", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-6313, 9448, -11585, "Ssyz Planetoid", "s", "M ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-11585, -11585, 0, "Tantalus V", "t", "FR ", "", 2000, 100, 1));
            quadrant.addObject(new TrekPlanet(-7641, -3416, 8039, "Ursa Minor Beta", "u", "C ", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(-3524, 3623, -4000, "Veil Nebula", "v", 3000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekWormhole(1, 4, 9, "Worm Hole", "w"));
            quadrant.addObject(new TrekZone(-16243, 4175, 4922, "Xelor Asteroids", "x", 5000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekPlanet(-9448, 9448, -8928, "Yonada Outpost", "y", "A ", "", 2000, 100, 1));
            quadrant.addObject(new TrekZone(-776, 2647, 513, "Vela Pulsar", "z", 2000, TrekZone.OBJ_PULSAR));
            quadrants.put("Omega Quadrant", quadrant);

            quadrant = new TrekQuadrant(this);
            quadrant.name = "Nu Quadrant";
            quadrant.addObject(new PlanetTeamPlay(-6313, 9448, -11585, "Team Base 1", "1", "M D T C TW FC ", "", 2000, 100, 1, 1));
            quadrant.addObject(new TrekZone(-6303, 9438, -11575, "Nebula R", "r", 3000, TrekZone.OBJ_NEBULA));

            quadrant.addObject(new PlanetTeamPlay(+6313, -9448, +11585, "Team Base 2", "2", "M D T C TW FC ", "", 2000, 100, 1, 2));
            quadrant.addObject(new TrekZone(+6303, -9438, +11575, "Nebula B", "b", 3000, TrekZone.OBJ_NEBULA));

            quadrant.addObject(new TrekPlanet(4145, 2795, 0, "Crystallum", "c", "A C ", "", 3000, 250, 3));
            quadrant.addObject(new TrekPlanet(3651, 3109, -546, "Torpetia", "t", "T L ", "", 3000, 250, 3));
            quadrant.addObject(new TrekPlanet(4247, 2219, 545, "Dronze", "d", "D M ", "", 3000, 250, 3));
            quadrant.addObject(new TrekPlanet(-4145, -2795, 0, "Miscelly IV", "m", "CM I N MB ", "", 3000, 250, 3));
            quadrant.addObject(new TrekStarbase(0, 0, 0, "The Flag", "f", "", 2000, 100, 1));
            quadrants.put("Nu Quadrant", quadrant);

            quadrant = new TrekQuadrant(this);
            quadrant.name = "Delta Quadrant";
            quadrant.addObject(new TrekSpatialAnomaly(gen.nextInt() % 50000, gen.nextInt() % 50000, gen.nextInt() % 50000, "Spatial Anomaly", "W"));
            quadrant.addObject(new TrekPlanet(7841, 27048, 17639, "Assimilated Planet", "a", "M D ", "", 2000, 500, 1));
            quadrant.addObject(new TrekPlanet(10974, 30019, 21137, "Assimilated Planet", "b", "T ", "", 2000, 500, 1));
            quadrant.addObject(new TrekPlanet(13710, 10000, -7950, "Assimilated Planet", "c", "A ", "", 2000, 500, 1));
            quadrant.addObject(new TrekZone(-22840, 11790, -6390, "Nebula", "d", 4000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekZone(-18930, 11290, -9010, "Quasar", "e", 4000, TrekZone.OBJ_QUASAR));
            quadrant.addObject(new TrekPlanet(35450, 33250, 2420, "Assimilated Planet", "f", "C A FR CM ", "", 2000, 500, 1));
            quadrant.addObject(new TrekZone(23600, 24140, -16930, "Asteroids", "g", 3000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekPlanet(20030, 2320, -21220, "Assimilated Planet", "i", "", "", 2000, 500, 1));
            quadrant.addObject(new TrekZone(-21250, 5950, -12210, "Asteroid Belt", "j", 5000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekZone(32950, 35750, 2190, "Nebula", "l", 4000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new PlanetMegadon(30040, -4600, -350, "Borg Homeworld", "m", "C M D T A ", "", 2000, 500, 1));
            quadrant.addObject(new TrekPlanet(-23950, 12560, -4790, "Assimilated Planet", "n", "C ", "", 2000, 500, 1));
            quadrant.addObject(new TrekZone(7050, -13570, -25250, "Nebula", "o", 6000, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekPlanet(4450, -7720, -17970, "Assimilated Planet", "p", "C A ", "", 2000, 500, 1));
            quadrant.addObject(new TrekZone(33050, -11640, 1130, "Quasar", "q", 4000, TrekZone.OBJ_QUASAR));
            quadrant.addObject(new TrekZone(8410, 11860, 9890, "Nebula", "r", 2500, TrekZone.OBJ_NEBULA));
            quadrant.addObject(new TrekPlanet(-14310, -8550, 10290, "Assimilated Planet", "s", "M ", "", 2000, 500, 1));
            quadrant.addObject(new PlanetTechPlanet(11795, 25850, 15998, "Tech Planet", "t", "C ", "", 2000, 500, 1));
            quadrant.addObject(new TrekPlanet(24970, -21080, -35270, "Assimilated Planet", "u", "", "", 2000, 500, 1));
            quadrant.addObject(new TrekPlanet(-6060, 19150, 5540, "Assimilated Planet", "v", "FC ", "", 2000, 500, 1));
            quadrant.addObject(new TrekZone(29200, 31050, 4740, "Pulsar", "x", 4000, TrekZone.OBJ_PULSAR));
            quadrant.addObject(new TrekZone(1900, -15820, -24800, "Asteroid Zone", "y", 5000, TrekZone.OBJ_ASTEROIDBELT));
            quadrant.addObject(new TrekObserverDevice("Observer-" + (100 + Math.abs(gen.nextInt() % 899)), "V", 6053 + randomInt(2000), 20747 + randomInt(2000), 2980 + randomInt(3000)));
            quadrant.addObject(new TrekObserverDevice("Observer-" + (10 + Math.abs(gen.nextInt() % 89)), "P", 8100 + randomInt(2500), -20341 + randomInt(2500), 40 + randomInt(2500)));
            quadrants.put("Delta Quadrant", quadrant);

            TrekLog.logMessage("Setting up Tick Timer...");
            tickTimer = new Timer(true);
            tickTimer.scheduleAtFixedRate(new TrekTickTimerTask(), 0, 250);

            TrekLog.logMessage("Server is up and running.");

            while (true) {
                try {
                    // Sleep for 10 ms, so we don't take up processing time.
                    Thread.sleep(1);

                    // Capture console commands.
                    if (System.in.available() != 0) {
                        int consoleCharacter = System.in.read();
                        Character theChar = (char) consoleCharacter;

                        System.out.write(theChar.toString().getBytes());
                        System.out.flush();

                        if (consoleCharacter != 13 && consoleCharacter != 10) {
                            consoleCommand += Character.toString((char) consoleCharacter);
                        } else {
                            doConsoleCommand(consoleCommand);
                        }
                    }

                    // Send all of the user's output buffers.
                    doOutputBuffers();
                } catch (InterruptedException ie) {
                    TrekLog.logMessage("Server interrupted.");
                    break;
                } catch (Exception e) {
                    TrekLog.logException(e);
                    break;
                }
            }

            serverShuttingDown = true;

            for (int x = 0; x < players.size(); x++) {
                TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);
                activePlayer.serverShutdown = true;
                activePlayer.interrupt();
            }

            serverMon.shutdownConnections();

            while (players.size() != 0) {
                TrekLog.logMessage("Waiting for all players threads to stop...");
                Thread.sleep(10000);
            }

            TrekLog.logMessage("Server Shutdown complete.");

            System.exit(0);
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    /**
     * Adds a player to the server.
     *
     * @param sckClientIn The Socket of the player.
     * @param serverIn    An instance of the server.
     */
    protected static synchronized void add(Socket sckClientIn, TrekServer serverIn) {
        try {
            if (serverShuttingDown) {
                OutputStream out = sckClientIn.getOutputStream();
                out.write("The server is rebooting.  Wait a few minutes and reconnect.".getBytes());
                out.flush();
                return;
            }

            TrekPlayer newPlayer = new TrekPlayer(sckClientIn, serverIn);
            newPlayer.socket.setKeepAlive(true);
            players.addElement(newPlayer);
            newPlayer.start();
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    /**
     * Removes the passed player from the server.
     *
     * @param player The TrekPlayer object to remove.
     */
    protected static void removePlayer(TrekPlayer player) {
        try {
            // Remove the ship from the quadrant.
            if (player.state == TrekPlayer.WAIT_PLAYING) {
                player.ship.currentQuadrant.removeShipByScanLetter(player.ship.scanLetter);
                TrekLog.logMessage("Removed " + player.shipName + " from " + player.ship.currentQuadrant + ".");
            }

            // Close the socket, or the file if it is a Bot.
            if (player instanceof TrekBot)
                player.out.close();
            else
                player.socket.close();
            //player.handler.interrupt();

            // Remove the player from the players vector.
            for (int x = 0; x < players.size(); x++) {
                TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

                if (activePlayer.equals(player)) {
                    TrekLog.logMessage("Removed " + player.shipName + " from game.");
                    players.removeElementAt(x);
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // nothing
        } catch (Exception f) {
            TrekLog.logException(f);
        }
    }

    /**
     * Adds a player to the server.
     *
     * @param sckClientIn The Socket of the player.
     * @param serverIn    An instance of the server.
     */
    protected static synchronized void addRaw(Socket sckClientIn, TrekServer serverIn) {
        try {
            if (serverShuttingDown) {
                OutputStream out = sckClientIn.getOutputStream();
                out.write("<error>The server is rebooting.  Wait a few minutes and reconnect.</error>".getBytes());
                out.flush();
                return;
            }

            TrekRawDataInterface newPlayer = new TrekRawDataInterface(sckClientIn, serverIn);
            newPlayer.socket.setKeepAlive(true);
            players.addElement(newPlayer);
            newPlayer.start();
        } catch (Exception e) {
            TrekLog.logException(e);
        }
    }

    /**
     * Adds a monitoring connection to the server.
     *
     * @param sckClientIn The Socket of the monitor client.
     */
    protected static synchronized void addMonitor(Socket sckClientIn) {
        try {
            if (serverShuttingDown) {
                OutputStream out = sckClientIn.getOutputStream();
                out.write("The server is rebooting.  Wait a few minutes and reconnect.".getBytes());
                out.flush();
                return;
            }

            serverMon.addClient(sckClientIn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sends a global message.
     *
     * @param thisMessage The message to send.
     * @param sender      TrekPlayer of who it is from.
     */
    protected static void sendMsgToAllPlayers(String thisMessage, TrekObject sender, boolean beep, boolean sendReceivingMessage) {

        if (TrekUtilities.isObjectShip(sender)) {
            serverMon.addToOutputBuffer("MSG:(" + sender.name + ") " + thisMessage + "\r");
        } else {
            serverMon.addToOutputBuffer("MSG:(SB Distress) " + thisMessage + "\r");
        }

        String actualMessage = "";
        String receiveMessage = "";

        if (sendReceivingMessage)
            receiveMessage = "Receiving subspace transmission from " + sender.name + " ...";

        if (beep)
            actualMessage += "\007";

        if (TrekUtilities.isObjectShip(sender)) {
            TrekShip messagingShip = (TrekShip) sender;

            // Log the message, because we are nosey.
            TrekLog.logTextMessage("Global: (" + messagingShip.scanLetter + "): " + messagingShip.name + ": " + thisMessage);

            messagingShip.parent.messageSendCount++;
            if (messagingShip.nebulaTarget != null) {
                //garble outbound message
                thisMessage = TrekUtilities.garbledOutboundMsg(thisMessage);
            }

            if (messagingShip.parent.messageSendCount > 3 && messagingShip.parent.messageSendTimeout != 0) {
                //	messaged too frequently, time to fry the radio transmitter
                if (thisMessage.length() > 65) {
                    thisMessage = thisMessage.substring(0, 64); // truncate to append burn-out message
                }

                actualMessage += "(" + sender.scanLetter + ") " + thisMessage + "&^%$#@! ...";
            } else {
                actualMessage += "(" + sender.scanLetter + ") " + thisMessage;
            }
        } else {
            actualMessage += thisMessage;
        }

        // Loop through our player vector, sending messages to each player.
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            // If we encounter a null player or a non-playing player, continue with the next player.
            if (activePlayer == null || activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (!activePlayer.ship.jamGlobal) {
                // check to see if current player has messaging ship jammed
                if (TrekUtilities.isObjectShip(sender)) {
                    if (activePlayer.ship.jamShips[TrekUtilities.returnArrayIndex(sender.scanLetter)]) {
                        continue;
                    }
                    // Add the message to the queue.  Garble it if the receiving ship is affected by message distortion zone.
                    if (activePlayer.ship.nebulaTarget != null) {
                        activePlayer.addMessageToQueue(TrekUtilities.garbledInboundMsg(actualMessage));
                    } else {
                        activePlayer.addMessageToQueue(actualMessage);
                    }

                    // If we are to send the receiving message.
                    if (sendReceivingMessage)
                        activePlayer.hud.sendTopMessage(receiveMessage, 4);

                    if (!activePlayer.ship.scanLetter.equals(sender.scanLetter) && activePlayer.ship.currentQuadrant == sender.currentQuadrant)
                        activePlayer.ship.updateMsgPoint(sender.point);

                    // if it's a bot, send it the message data, so that it can queue 'em up, and potentially respond to them
                    if (activePlayer instanceof BotPlayer) {
                        BotPlayer curBot = (BotPlayer) activePlayer;
                        curBot.recvPublicMessage(sender.scanLetter + actualMessage);  // first character will always be sending ship's letter
                    }
                } else {
                    if (activePlayer.ship.nebulaTarget != null) {
                        activePlayer.addMessageToQueue(TrekUtilities.garbledInboundMsg(actualMessage));
                    } else {
                        activePlayer.addMessageToQueue(actualMessage);
                    }

                    if (sendReceivingMessage)
                        activePlayer.hud.sendTopMessage(receiveMessage, 4);
                }
            }
        }
    }

    public static void sendMsgToAllPlayersInQuadrant(String message, TrekObject sender, TrekQuadrant quad, boolean beep, boolean sendRecMsg) {
        Vector allShips = quad.getAllShips();
        for (Object ship : allShips) {
            TrekShip curShip = (TrekShip) ship;
            sendMsgToPlayer(curShip.scanLetter, message, sender, beep, sendRecMsg);
        }
    }

    protected static void sendAnnouncement(String thisMessage, boolean beep) {
        serverMon.addToOutputBuffer("MSG:(Announce) " + thisMessage + "\r");

        String actualMessage = "";

        if (beep)
            actualMessage += "\007";

        actualMessage += thisMessage;

        // Loop through our player vector, sending messages to each player.
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            // If we encounter a null player or a non-playing player, continue with the next player.
            if (activePlayer == null || activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (!activePlayer.ship.jamGlobal) {
                activePlayer.addMessageToQueue(actualMessage);
            }
        }
    }

    /**
     * Sends a message to ships from a particular home planet.
     *
     * @param thisMessage The message to send.
     * @param sender      TrekPlayer of who it is from.
     */
    protected static void sendMsgToRace(String thisMessage, TrekObject sender, String homePlanet, boolean beep, boolean sendReceivingMessage) {
        String actualMessage = "";
        String receiveMessage = "";

        if (sendReceivingMessage)
            receiveMessage = "Receiving subspace transmission from " + sender.name + " ...";

        if (beep)
            actualMessage += "\007";

        if (TrekUtilities.isObjectShip(sender)) {
            TrekShip messagingShip = (TrekShip) sender;

            // Log the message, because we are nosey.
            TrekLog.logTextMessage("Race: (" + messagingShip.scanLetter + "): " + messagingShip.name + ": " + thisMessage);
            serverMon.addToAdminOutputBuffer("MSG:(" + homePlanet + ") " + messagingShip.name + ": " + thisMessage + "\r");

            messagingShip.parent.messageSendCount++;
            if (messagingShip.nebulaTarget != null) {
                //garble outbound message
                thisMessage = TrekUtilities.garbledOutboundMsg(thisMessage);
            }

            if (messagingShip.parent.messageSendCount > 3 && messagingShip.parent.messageSendTimeout != 0) {
                //	messaged too frequently, time to fry the radio transmitter
                if (thisMessage.length() > 65) {
                    thisMessage = thisMessage.substring(0, 64); // truncate to append burn-out message
                }

                actualMessage += "{" + sender.scanLetter + "} " + thisMessage + "&^%$#@! ...";
            } else {
                actualMessage += "{" + sender.scanLetter + "} " + thisMessage;
            }
        } else {
            actualMessage += thisMessage;
        }

        // Loop through our player vector, sending messages to each player.
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            if (activePlayer == null)
                continue;

            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (!activePlayer.ship.homePlanet.equals(homePlanet))
                continue;

            if (TrekUtilities.isObjectShip(sender)) {
                if (activePlayer.ship.jamShips[TrekUtilities.returnArrayIndex(sender.scanLetter)]) {
                    continue;
                }

                // Garble message if the receiving ship is affected by message distortion zone.
                if (activePlayer.ship.nebulaTarget != null) {
                    activePlayer.addMessageToQueue(TrekUtilities.garbledInboundMsg(actualMessage));
                } else {
                    activePlayer.addMessageToQueue(actualMessage);
                }

                if (sendReceivingMessage)
                    activePlayer.hud.sendTopMessage(receiveMessage, 4);

                if (activePlayer.state == TrekPlayer.WAIT_PLAYING) {
                    if (!activePlayer.ship.scanLetter.equals(sender.scanLetter) && activePlayer.ship.currentQuadrant == sender.currentQuadrant) {
                        activePlayer.ship.updateMsgPoint(sender.point);
                    }
                }

                // if it's a bot, send it the message data, so that it can queue 'em up, and potentially respond to them
                if (activePlayer instanceof BotPlayer) {
                    BotPlayer curBot = (BotPlayer) activePlayer;
                    curBot.recvPublicMessage(sender.scanLetter + actualMessage);  // first character will always be sending ship's letter
                }
            } else {
                if (activePlayer.ship.nebulaTarget != null) {
                    activePlayer.addMessageToQueue(TrekUtilities.garbledInboundMsg(actualMessage));
                } else {
                    activePlayer.addMessageToQueue(actualMessage);
                }

                if (sendReceivingMessage)
                    activePlayer.hud.sendTopMessage(receiveMessage, 4);
            }
        }
    }

    protected static void sendMsgToTeam(String thisMessage, TrekShip sender, boolean beep, boolean sendReceivingMessage) {
        String actualMessage = "";
        String receiveMessage = "";

        if (sendReceivingMessage)
            receiveMessage = "Receiving subspace transmission from " + sender.name + " ...";

        if (beep)
            actualMessage += "\007";

        if (TrekUtilities.isObjectShip(sender)) {
            // Log the message, because we are nosey.
            TrekLog.logTextMessage("Team: (" + sender.scanLetter + "): " + sender.name + ": " + thisMessage);
            serverMon.addToAdminOutputBuffer("MSG:(Team " + sender.parent.teamNumber + ") " + thisMessage + "\r");

            sender.parent.messageSendCount++;
            if (sender.nebulaTarget != null) {
                //garble outbound message
                thisMessage = TrekUtilities.garbledOutboundMsg(thisMessage);
            }

            if (sender.parent.messageSendCount > 3 && sender.parent.messageSendTimeout != 0) {
                //	messaged too frequently, time to fry the radio transmitter
                if (thisMessage.length() > 65) {
                    thisMessage = thisMessage.substring(0, 64); // truncate to append burn-out message
                }

                actualMessage += "{" + sender.scanLetter + "} " + thisMessage + "&^%$#@! ...";
            } else {
                actualMessage += "{" + sender.scanLetter + "} " + thisMessage;
            }
        }

        // Loop through our player vector, sending messages to each player on the same team
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            if (activePlayer == null)
                continue;

            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (!(activePlayer.ship.parent.teamNumber == sender.parent.teamNumber))
                continue;

            if (TrekUtilities.isObjectShip(sender)) {
                if (activePlayer.ship.jamShips[TrekUtilities.returnArrayIndex(sender.scanLetter)]) {
                    continue;
                }

                // Garble message if the receiving ship is affected by message distortion zone.
                if (activePlayer.ship.nebulaTarget != null) {
                    activePlayer.addMessageToQueue(TrekUtilities.garbledInboundMsg(actualMessage));
                } else {
                    activePlayer.addMessageToQueue(actualMessage);
                }

                if (sendReceivingMessage)
                    activePlayer.hud.sendTopMessage(receiveMessage, 4);

                if (activePlayer.state == TrekPlayer.WAIT_PLAYING) {
                    if (!activePlayer.ship.scanLetter.equals(sender.scanLetter)) {
                        activePlayer.ship.updateMsgPoint(sender.point);
                    }
                }

                // if it's a bot, send it the message data, so that it can queue 'em up, and potentially respond to them
                if (activePlayer instanceof BotPlayer) {
                    BotPlayer curBot = (BotPlayer) activePlayer;
                    curBot.recvPublicMessage(sender.scanLetter + actualMessage);  // first character will always be sending ship's letter
                }
            } else {
                if (activePlayer.ship.nebulaTarget != null) {
                    activePlayer.addMessageToQueue(TrekUtilities.garbledInboundMsg(actualMessage));
                } else {
                    activePlayer.addMessageToQueue(actualMessage);
                }

                if (sendReceivingMessage)
                    activePlayer.hud.sendTopMessage(receiveMessage, 4);
            }
        }
    }

    public static void sendMsgToPlayer(String thisShip, String thisMessage, TrekObject sender, boolean beep, boolean sendReceivingMessage) {
        String actualMessage = "";
        String receiveMessage = "";

        if (sendReceivingMessage)
            receiveMessage = "Receiving subspace transmission from " + sender.name + " ...";

        if (beep)
            actualMessage += "\007";

        if (TrekUtilities.isObjectShip(sender)) {
            actualMessage += "[" + sender.scanLetter + "] " + thisMessage;
        } else {
            actualMessage += thisMessage;
        }

        // Loop through our player vector, sending messages to each player.
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (activePlayer.ship == null)
                continue;

            if (activePlayer.ship.scanLetter == null)
                continue;

            if (!activePlayer.ship.scanLetter.equals(thisShip))
                continue;

            if (TrekUtilities.isObjectShip(sender)) {
                TrekShip senderShip = (TrekShip) sender;

                // Log the message, because we are nosey.
                TrekLog.logTextMessage("Private:  Recipient: (" + activePlayer.ship.scanLetter + "): " + activePlayer.ship.name + "  Sender: (" + senderShip.scanLetter + "): " + senderShip.name + ": " + thisMessage);
                serverMon.addToAdminOutputBuffer("MSG:(Private) " + sender.name + " to " + activePlayer.shipName + ": " + thisMessage + "\r");

                if (activePlayer.ship.jamShips[TrekUtilities.returnArrayIndex(sender.scanLetter)]) {
                    senderShip.parent.hud.sendMessage("Frequency IGNORED");
                } else {
                    senderShip.parent.messageSendCount++;

                    if (senderShip.nebulaTarget != null) {
                        //garble outbound message
                        thisMessage = TrekUtilities.garbledOutboundMsg(thisMessage);
                    }

                    if (senderShip.parent.messageSendCount > 3 && senderShip.parent.messageSendTimeout != 0) {
                        //	messaged too frequently, time to fry the radio transmitter
                        if (actualMessage.length() > 65) {
                            actualMessage = actualMessage.substring(0, 64); // truncate to append burn-out message
                        }

                        actualMessage += "&^%$#@! ...";
                    }

                    if (activePlayer.ship.nebulaTarget != null) {
                        actualMessage = TrekUtilities.garbledInboundMsg(actualMessage);
                    }

                    activePlayer.addMessageToQueue(actualMessage);

                    if (sendReceivingMessage)
                        activePlayer.hud.sendTopMessage(receiveMessage, 4);

                    if (activePlayer.state == TrekPlayer.WAIT_PLAYING) {
                        if (!activePlayer.ship.scanLetter.equals(sender.scanLetter) && activePlayer.ship.currentQuadrant == sender.currentQuadrant) {
                            activePlayer.ship.updateMsgPoint(sender.point);
                        }
                    }

                    // if it's a bot, send it the message data, so that it can queue 'em up, and potentially respond to them
                    if (activePlayer instanceof BotPlayer) {
                        BotPlayer curBot = (BotPlayer) activePlayer;
                        curBot.recvPrivateMessage(sender.scanLetter + thisMessage);  // first character will always be sending ship's letter
                    }

                }
            } else {
                if (activePlayer.ship.scanLetter.equals(thisShip)) {
                    if (activePlayer.ship.nebulaTarget != null) {
                        //actualMessage = TrekUtilities.garbledInboundMsg(actualMessage);
                        // *** Commenting out to see if it fixes garbled 'Torpedoes Miss' style messages for ship in nebula
                    }
                    activePlayer.addMessageToQueue(actualMessage);

                    if (sendReceivingMessage)
                        activePlayer.hud.sendTopMessage(receiveMessage, 4);
                }
            }
        }
    }

    protected static void sendMsgToClosestPlayer(String thisMessage, TrekPlayer sender, boolean beep, boolean sendReceivingMessage) {
        String preGarbledMsg = thisMessage;
        String actualMessage = "";
        String receiveMessage;
        receiveMessage = "Receiving subspace transmission from " + sender.ship.name + " ...";

        // Log the message, because we are nosey.
        TrekLog.logTextMessage("Closest: (" + sender.ship.scanLetter + "): " + sender.ship.name + ": " + thisMessage);

        if (beep)
            actualMessage += "\007";

        actualMessage += "[" + sender.ship.scanLetter + "] " + thisMessage;

        double closestDistance = 10000000;
        TrekPlayer closest = null;

        // Loop through our player vector, sending messages to each player.
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            if (activePlayer == null)
                continue;

            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (TrekMath.getDistance(sender.ship, activePlayer.ship) < closestDistance && activePlayer != sender) {
                closest = activePlayer;
            }
        }

        if (closest != null) {
            if (closest.ship.jamShips[TrekUtilities.returnArrayIndex(sender.ship.scanLetter)]) {
                sender.hud.sendMessage("Frequency IGNORED");
            } else {

                if (sendReceivingMessage)
                    closest.hud.sendTopMessage(receiveMessage, 4);

                sender.messageSendCount++;

                if (sender.ship.nebulaTarget != null) {
                    //garble outbound message
                    thisMessage = TrekUtilities.garbledOutboundMsg(thisMessage);
                }

                if (sender.messageSendCount > 3 && sender.messageSendTimeout != 0) {
                    //	messaged too frequently, time to fry the radio transmitter
                    if (actualMessage.length() > 65) {
                        actualMessage = actualMessage.substring(0, 64); // truncate to append burn-out message
                    }

                    actualMessage += "&^%$#@! ...";
                }

                if (closest.ship.nebulaTarget != null) {
                    actualMessage = TrekUtilities.garbledInboundMsg(actualMessage);
                }
                closest.addMessageToQueue(actualMessage);
                closest.ship.updateMsgPoint(sender.ship.point);

                // if it's a bot, send it the message data, so that it can queue 'em up, and potentially respond to them
                if (sender instanceof BotPlayer) {
                    BotPlayer curBot = (BotPlayer) sender;
                    curBot.recvPrivateMessage(sender.ship.scanLetter + thisMessage);  // first character will always be sending ship's letter
                }

                serverMon.addToAdminOutputBuffer("MSG:(Closest) " + sender.shipName + " to " + closest.shipName + ": " + preGarbledMsg + "\r");
            }
        }
    }

    protected static void sendMsgToAdmins(String thisMessage, TrekObject sender, boolean beep, boolean sendReceivingMessage) {
        serverMon.addToAdminOutputBuffer("MSG:(Admin) " + thisMessage + "\r");

        String actualMessage = "";
        String receiveMessage = "";

        if (sendReceivingMessage)
            receiveMessage = "Receiving subspace transmission from " + sender.name + " ...";

        if (beep)
            actualMessage += "\007";

        actualMessage += thisMessage;

        // Loop through our player vector, sending messages to each player.
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            // If we encounter a null player or a non-playing player, continue with the next player.
            if (activePlayer == null || activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (activePlayer.ship instanceof ShipQ) {
                // Add the message to the queue.  Garble it if the receiving ship is affected by message distortion zone.
                activePlayer.addMessageToQueue(actualMessage);
            }

            // If we are to send the receiving message.
            if (sendReceivingMessage)
                activePlayer.hud.sendTopMessage(receiveMessage, 4);
        }
    }

    private static void doOutputBuffers() {
        try {
            for (Enumeration e = players.elements(); e.hasMoreElements(); ) {
                TrekPlayer activePlayer = (TrekPlayer) e.nextElement();

                if (activePlayer.state == TrekPlayer.WAIT_PLAYING) {
                    activePlayer.sendOutputBuffer();
                }
            }

            serverMon.doOutputBuffers();
        } catch (NoSuchElementException nsee) {
            TrekLog.logError(nsee.getMessage());
        }
    }

    protected static boolean shipAlreadyExists(String thisName) {
        for (Enumeration e = players.elements(); e.hasMoreElements(); ) {
            TrekPlayer activePlayer = (TrekPlayer) e.nextElement();

            if (activePlayer.shipName.equals(thisName)) {
                return true;
            }
        }

        return false;
    }

    protected static String getScanLetter() {
        TrekLog.logMessage("Getting ship scanletter...");

        char[] letters = new char[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0'};

        if (players.size() > letters.length) {
            TrekLog.logMessage("Server is full.  Kick ass.");
            return null;
        }

        // Remove all used scan letters from the array.
        for (Enumeration e = players.elements(); e.hasMoreElements(); ) {
            TrekPlayer player = (TrekPlayer) e.nextElement();

            if (player.ship != null) {
                for (int x = 0; x < letters.length; x++) {
                    if (player.ship.scanLetter.equals(Character.toString(letters[x]))) {
                        letters[x] = '\0';
                        break;
                    }
                }
            }
        }

        // Get the first available scanletter from the letters array.
        for (char letter : letters) {
            if (letter != '\0') {
                TrekLog.logMessage("Available ship scan letter found: " + letter);
                return Character.toString(letter);
            }
        }

        TrekLog.logMessage("Scan letter not found!");
        return null;
    }

    protected synchronized static void addBot(TrekPlayer thisBotPlayer) {
        // If we do not have room for a bot, then do nothing.
        if (players.size() >= 62)
            return;

        players.add(thisBotPlayer);
        thisBotPlayer.start();
    }

    protected static TrekServer getInstance() {
        return new TrekServer();
    }

    protected static TrekShip getPlayerShipByScanLetter(String scanLetter) {
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            if (activePlayer == null)
                continue;

            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (activePlayer.ship.scanLetter.equals(scanLetter))
                return activePlayer.ship;
        }

        return null;
    }

    protected static String getShipLetterByConn(long connID) {
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            if (activePlayer == null)
                continue;

            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (activePlayer.dbConnectionID == connID)
                return activePlayer.ship.scanLetter;
        }

        return null;
    }

    protected static int getNumberOfActivePlayers() {
        int playerCount = 0;

        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            if (activePlayer == null)
                continue;

            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            playerCount++;
        }

        return playerCount;
    }

    protected static TrekShip getPlayerShipByShipName(String shipName) {
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

            if (activePlayer == null)
                continue;

            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;

            if (activePlayer.ship.name.equals(shipName))
                return activePlayer.ship;
        }

        return null;
    }

    protected static boolean doBotHuds() {
        return logBotHud;
    }

    public static void launchBot(String botType) {
        try {
            String botString = "org.gamehost.jtrek.javatrek.bot." + botType;
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
            } catch (MalformedURLException mue) {
                TrekLog.logException(mue);
            }
        } catch (ClassNotFoundException cnfe) {
            TrekLog.logException(cnfe);
        } catch (NoSuchMethodException nsme) {
            TrekLog.logException(nsme);
        } catch (IllegalAccessException iae) {
            TrekLog.logException(iae);
        } catch (InstantiationException ie) {
            TrekLog.logException(ie);
        } catch (InvocationTargetException ite) {
            TrekLog.logException(ite);
        }
    }

    public static void launchBots() {
        if (botRespawnEnabled) {
            Random gen = new Random();

            if (getCurrentBotPlayerCount() < maxBotCount) {
                TrekLog.logMessage("Launching bot.");

                String botShipName;

                // TODO: determine whether to start a new ship or unsave an existing one here
                // for now; choose a new ship each time, cycle through names until a non-allocated name is selected
                boolean shipExists = false;

                boolean doNameLoop;
                do {
                    botShipName = BotPlayer.nameChoices[Math.abs(gen.nextInt() % BotPlayer.nameChoices.length)];
                    // check to see if name already is saved
                    doNameLoop = (TrekServer.shipAlreadyExists(botShipName) || dbInt.isShipPasswordProtected(botShipName));
                } while (doNameLoop);

                // if new ship, then pick a class
                if (!shipExists) {
                    int shipInt = Math.abs(gen.nextInt() % 22) + 97;  // 97 = a, 22 = number of unique classes of ship
                    String botStr = "";

                    switch (shipInt) {
//                        case 97:  // con
//                            botStr = "BotCON";
//                        break;

                        case 98:  // excel
                            botStr = "BotExcel";
                            break;

                        case 99:  // larson
                            botStr = "BotLarson";
                            break;

                        case 100: // dy
                            botStr = "BotDY";
                            break;
//
//                        case 101: // rbop
//                            botStr = "BotRBOP";
//                        break;
//
//                        case 102: // kev
//                            botStr = "BotKEV";
//                        break;

                        case 103: // kpb
                            botStr = "BotKPB";
                            break;
//
//                        case 104: // br5
//                            botStr = "BotBR5";
//                        break;

                        case 105: // br1k
                            botStr = "BotBR1K";
                            break;

//                        case 106: // cl-13
//                            botStr = "BotCL13";
//                        break;
//
//                        case 107: // cv-97
//                            botStr = "BotCV97";
//                        break;

                        case 108: // cda-180
                            botStr = "BotCDA180";
                            break;

                        case 109: // cda-120
                            botStr = "BotCDA120";
                            break;
//
//                        case 110: // warbird
//                            botStr = "BotWarbird";
//                        break;
//
//                        case 111: // kbop
//                            botStr = "BotKBOP";
//                        break;
//
                        case 112: // galor
                            botStr = "BotGalor";
                            break;

                        case 113: // br2k
                            botStr = "BotBR2K";
                            break;

                        case 114: // scout
                            botStr = "BotScout";
                            break;
//
//                        case 115: // interceptor
//                            botStr = "BotIntrcptr";
//                        break;
//
//                        case 116: // defiant
//                            botStr = "BotDefiant";
//                        break;
//
                        case 117: // ferengi
                            botStr = "BotFerengi";
                            break;

                        case 118: // d10
                            botStr = "BotD10";
                            break;

                        default:
                            switch (Math.abs(gen.nextInt() % 12)) {
                                case 0:
                                    botStr = "BotExcel";
                                    break;
                                case 1:
                                    botStr = "BotLarson";
                                    break;
                                case 2:
                                    botStr = "BotDY";
                                    break;
                                case 3:
                                    botStr = "BotKPB";
                                    break;
                                case 4:
                                    botStr = "BotBR1K";
                                    break;
                                case 5:
                                    botStr = "BotCDA180";
                                    break;
                                case 6:
                                    botStr = "BotCDA120";
                                    break;
                                case 7:
                                    botStr = "BotGalor";
                                    break;
                                case 8:
                                    botStr = "BotBR2K";
                                    break;
                                case 9:
                                    botStr = "BotScout";
                                    break;
                                case 10:
                                    botStr = "BotFerengi";
                                    break;
                                case 11:
                                    botStr = "BotD10";
                                    break;
                            }
                    }

                    try {
                        String botString = "org.gamehost.jtrek.javatrek.bot." + botStr;
                        TrekBot botPlayer;
                        Class botClass;
                        Class[] constArgs = new Class[]{TrekServer.class, String.class};
                        Object[] actualArgs = new Object[]{TrekServer.getInstance(), botShipName};
                        Constructor botConst;
                        try {
                            ClassLoader botLoader = new URLClassLoader(new URL[]{new File("lib/.").toURL()});
                            botClass = botLoader.loadClass(botString);
                            botConst = botClass.getConstructor(constArgs);
                            botPlayer = (TrekBot) botConst.newInstance(actualArgs);
                            TrekServer.addBot(botPlayer);
                            TrekLog.logMessage("Spawned new " + botStr + " bot.");
                        } catch (MalformedURLException mue) {
                            TrekLog.logException(mue);
                            TrekLog.logMessage("Failed to load new instance of bot class. MalformedURLException");
                        }
                    } catch (ClassNotFoundException cnfe) {
                        TrekLog.logException(cnfe);
                        TrekLog.logMessage("Failed to spawn new bot. ClassNotFoundException");
                    } catch (NoSuchMethodException nsme) {
                        TrekLog.logException(nsme);
                        TrekLog.logMessage("Failed to spawn new bot. NoSuchMethodException");
                    } catch (IllegalAccessException iae) {
                        TrekLog.logException(iae);
                        TrekLog.logMessage("Failed to spawn new bot. IllegalAccessException");
                    } catch (InstantiationException ie) {
                        TrekLog.logException(ie);
                        TrekLog.logMessage("Failed to spawn new bot. InstantiationException");
                    } catch (InvocationTargetException ite) {
                        TrekLog.logException(ite);
                        TrekLog.logMessage("Failed to spawn new bot. InvocationTargetException");
                    }
                }
            }
        }
    }

    protected void showPlayers() {
        if (players.size() == 0) {
            System.out.println("No active players.");
        } else {
            System.out.println(" #  Remote Socket Address     Player Name                    State");
            for (int x = 0; x < players.size(); x++) {
                TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);

                System.out.print(TrekUtilities.format("", "" + x, 2) + "  ");
                //System.out.print(TrekUtilities.format(activePlayer.handler.getIPAddress(), "", 26));
                if (activePlayer instanceof TrekBot) {
                    System.out.print(TrekUtilities.format("TrekBot", "", 26));
                } else {
                    System.out.print(TrekUtilities.format(activePlayer.socket.getRemoteSocketAddress().toString(), "", 26));
                }
                System.out.print(TrekUtilities.format(activePlayer.shipName, "", 31));
                System.out.print(activePlayer.getCurrentStateString() + "\n");
            }

            System.out.print("\nTotal Players: " + players.size() + "\n\n");
        }
    }

    protected void removeUnknown() {
        int removeAttempts = 0;

        if (players.size() == 0) {
            System.out.println("No active players.");
        } else {
            for (int count = 0; count < players.size(); count++) {
                TrekPlayer curPlayer = (TrekPlayer) players.elementAt(count);
                if (curPlayer.getCurrentStateString().equalsIgnoreCase("** unknown")) {
                    System.out.println("Trying to remove player on socket: " + curPlayer.socket.getRemoteSocketAddress().toString() + " name: " + curPlayer.shipName);
                    removePlayer(curPlayer);
                    removeAttempts++;
                }
            }
        }

        if (removeAttempts == 0) {
            System.out.println("No players in UNKNOWN state.");
        }
    }

    private void doConsoleCommand(String thisCommand) {
        try {
            if (thisCommand.equals(""))
                return;

            if (thisCommand.equalsIgnoreCase("show players")) {
                showPlayers();
                return;
            }

            if (thisCommand.equalsIgnoreCase("remove unknown")) {
                removeUnknown();
                return;
            }

            if (thisCommand.equalsIgnoreCase("shutdown")) {
                TrekLog.logMessage("Shutting down server...");

                setBotRespawnEnabled(false);
                setMaxBots(0);

                this.interrupt();

                return;
            }

            if (thisCommand.equalsIgnoreCase("reboot")) {
                TrekLog.logMessage("Rebooting server...");
                this.interrupt();

                return;
            }

            if (thisCommand.equalsIgnoreCase("show playerids")) {
                TrekLog.logMessage("Outputting player ids...");

                dbInt.logPlayerIDs();

                return;
            }
        } catch (Exception e) {
            TrekLog.logException(e);
        } finally {
            consoleCommand = "";
        }
    }

    protected static void addDeletionTimer(String thisShipName) {
        TrekLog.logMessage("Adding a deletion timer for " + thisShipName);

        Calendar cal = Calendar.getInstance();
        Date deleteTime = cal.getTime();
        deleteTime.setTime(cal.getTimeInMillis() + 900000);

        TrekDeletionTimerTask deleter = new TrekDeletionTimerTask(thisShipName);
        tickTimer.schedule(deleter, deleteTime);

        deletionTimers.add(deleter);
    }

    public static String getUptime() {
        Calendar cal = Calendar.getInstance();
        long now = cal.getTimeInMillis();
        long uptimeInSeconds = (now - startTime) / 1000;

        int hours = new Long(uptimeInSeconds / 3600).intValue();
        int minutes = new Long((uptimeInSeconds / 60) - (hours * 60)).intValue();

        return hours + "h:" + minutes + "m";
    }

    protected static boolean isTeamPlayEnabled() {
        return teamPlayEnabled;
    }

    protected static void setTeamPlayEnabled(boolean teamPlay) {
        TrekServer.teamPlayEnabled = teamPlay;
    }

    protected static TrekQuadrant getQuadrantByName(String quadName) {
        TrekQuadrant returnValue;
        returnValue = (TrekQuadrant) quadrants.get(quadName);

        return returnValue;
    }

    protected static boolean isBotRespawnEnabled() {
        return botRespawnEnabled;
    }

    protected static void setBotRespawnEnabled(boolean b) {
        botRespawnEnabled = b;
    }

    protected static void setMaxBots(int i) {
        maxBotCount = i;
    }

    // returns the number of 'BotPlayer's on the server
    protected static int getCurrentBotPlayerCount() {
        int currentBotCount = 0;
        for (int x = 0; x < players.size(); x++) {
            TrekPlayer activePlayer = (TrekPlayer) players.elementAt(x);
            if (activePlayer == null)
                continue;
            if (activePlayer.state != TrekPlayer.WAIT_PLAYING)
                continue;
            if (activePlayer instanceof BotPlayer) {
                currentBotCount++;
            }
        }

        return currentBotCount;
    }

    protected static int getBotRespawnTime() {
        return botRespawnTime;
    }

    protected static void setBotRespawnTime(int i) {
        if (i > 0 && i < 3600) {
            botRespawnTime = i;
        }
    }

    public static synchronized TrekObject getShipByScanLetter(String thisLetter) {
        TrekLog.logDebug("Server: Getting ship by scan letter " + thisLetter + ".");

        TrekObject theShip = null;

        for (Enumeration e = quadrants.elements(); e.hasMoreElements(); ) {
            TrekQuadrant quad = (TrekQuadrant) e.nextElement();
            if (quad.getShipByScanLetter(thisLetter) != null) {
                theShip = quad.getShipByScanLetter(thisLetter);
                break;
            }
        }

        return theShip;
    }

    private int randomInt(int x) {
        Random gen = new Random();
        return (gen.nextInt() % x);
    }

    protected static boolean isAnonymousPlayEnabled() {
        return enableAnonymousShips;
    }

    protected static boolean isThxEnabled() {
        return autoSpawnThx;
    }

    protected static void toggleThx() {
        autoSpawnThx = !autoSpawnThx;
    }

    public static String getBotScanLetter() {
        TrekLog.logMessage("Getting bot scan letter...");

        // we want that darn borg to start at 'A'
        char[] letters = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

        if (players.size() > letters.length) {
            TrekLog.logMessage("Server is full.  Kick ass.");
            return null;
        }

        // Remove all used scan letters from the array.
        for (Enumeration e = players.elements(); e.hasMoreElements(); ) {
            TrekPlayer player = (TrekPlayer) e.nextElement();

            if (player.ship != null) {
                for (int x = 0; x < letters.length; x++) {
                    if (player.ship.scanLetter.equals(Character.toString(letters[x]))) {
                        letters[x] = '\0';
                        break;
                    }
                }
            }
        }

        // Get the first available scan letter from the letters array.
        for (char letter : letters) {
            if (letter != '\0') {
                TrekLog.logMessage("Available ship scan letter found: " + letter);
                return Character.toString(letter);
            }
        }

        TrekLog.logMessage("Scan letter not found!");
        return null;
    }
}