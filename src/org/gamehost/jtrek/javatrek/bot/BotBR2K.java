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
package org.gamehost.jtrek.javatrek.bot;

import org.gamehost.jtrek.javatrek.TrekServer;
import org.gamehost.jtrek.javatrek.TrekUtilities;

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Apr 8, 2004
 * Time: 8:49:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class BotBR2K extends BotPlayer {
    public BotBR2K(TrekServer serverIn, String name) {
        super(serverIn, name);

        shipName = name;
    }

    public void loadInitialBot() {
        super.loadInitialBot();

        ship = TrekUtilities.getShip("q", this);
        ship.setInitialDirection();
        ship.currentQuadrant.addShip(ship);

        quadrantName = ship.currentQuadrant.name;

        setCurObjs();

        this.drawHud(true);
    }

    public void botTickUpdate() {
        super.botTickUpdate();

        // ship specific tick functions
    }

    public void botSecondUpdate() {
        super.botSecondUpdate();

        // ship specific second functions
    }
}
