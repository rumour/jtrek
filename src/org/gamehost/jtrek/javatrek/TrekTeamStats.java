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

/**
 * Created by IntelliJ IDEA.
 * User: Jay
 * Date: Feb 18, 2004
 * Time: 12:38:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class TrekTeamStats {
    protected int damageGiven;
    protected int bonus;
    protected int damageReceived;
    protected int breakSaves;
    protected int conflicts;
    protected int gold;

    public TrekTeamStats() {
        damageGiven = 0;
        bonus = 0;
        damageReceived = 0;
        breakSaves = 0;
        conflicts = 0;
        gold = 0;
    }

    public int getDamageGiven() {
        return damageGiven;
    }

    public void addDmgGvn(int damageGiven) {
        this.damageGiven += damageGiven;
    }

    public int getBonus() {
        return bonus;
    }

    public void addBonus(int bonus) {
        this.bonus += bonus;
    }

    public int getDamageReceived() {
        return damageReceived;
    }

    public void addDmgRcvd(int damageReceived) {
        this.damageReceived += damageReceived;
    }

    public int getBreakSaves() {
        return breakSaves;
    }

    public void addBreakSaves(int breakSaves) {
        this.breakSaves += breakSaves;
    }

    public int getConflicts() {
        return conflicts;
    }

    public void addConflict() {
        this.conflicts++;
    }

    public int getGold() {
        return gold;
    }

    public void addGold(int gold) {
        this.gold += gold;
    }
}
