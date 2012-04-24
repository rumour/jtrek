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
 * @author Administrator
 *         <p/>
 *         To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Generation - Code and Comments
 */
public class PlanetTeamPlay extends TrekPlanet {
    public int teamNumber = 0;

    /**
     * @param x
     * @param y
     * @param z
     * @param name
     * @param scanletter
     * @param codes
     * @param whatRace
     * @param phaserRangeIn
     * @param phaserDamageIn
     * @param fireDelay
     */
    public PlanetTeamPlay(
            int x,
            int y,
            int z,
            String name,
            String scanletter,
            String codes,
            String whatRace,
            int phaserRangeIn,
            int phaserDamageIn,
            int fireDelay,
            int teamNumberIn) {
        super(
                x,
                y,
                z,
                name,
                scanletter,
                codes,
                whatRace,
                phaserRangeIn,
                phaserDamageIn,
                fireDelay);
        teamNumber = teamNumberIn;
    }

}
