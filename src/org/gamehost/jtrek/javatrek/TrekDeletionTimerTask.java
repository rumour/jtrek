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

import java.util.TimerTask;

/**
 * @author Joe Hopkinson
 */
public class TrekDeletionTimerTask extends TimerTask {
    String shipToDelete;

    public TrekDeletionTimerTask(String thisShip) {
        shipToDelete = thisShip;
    }

    public void run() {
        /*	TrekLog.logMessage( "Timer expired for " + shipToDelete + "." );

          if( TrekServer.getPlayerShipByShipName( shipToDelete ) != null ) {
              TrekLog.logMessage(shipToDelete + " is currently playing.  Not deleting files.");
              return;
          }

          TrekDataInterface di = new TrekDataInterface();

          if( di.hasPlayerFile( shipToDelete ) ) {
              TrekLog.logMessage(shipToDelete + " has saved since the timer was created.  Not deleting files.");
              return;
          }

          di.removePlayer( shipToDelete, true );*/
    }
}
