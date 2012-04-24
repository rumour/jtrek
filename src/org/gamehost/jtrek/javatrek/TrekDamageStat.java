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
 * Represents a damage statistic generated when damage occurs.
 *
 * @author Joe Hopkinson
 */
public class TrekDamageStat {
    protected int damageGiven;
    protected int structuralDamage;
    protected int shieldReduction;
    protected int bonus;
    protected TrekObject victim;
    protected TrekObject victor;
    protected boolean rammed;
    protected String type;
    protected boolean showAttacker;

    public TrekDamageStat(int dmgGvnin, int intDmgGvnin, int shieldReductionin, int bonusin, TrekObject victimin, TrekObject victorin, String typein, boolean showAttackerIn) {
        damageGiven = dmgGvnin;
        structuralDamage = intDmgGvnin;
        shieldReduction = shieldReductionin;
        bonus = bonusin;
        victim = victimin;
        victor = victorin;
        rammed = false;
        type = typein;
        showAttacker = showAttackerIn;
    }

    protected void dumpStatToConsole() {
        System.out.println("DamageGiven: " + damageGiven);
        System.out.println("StructuralDamage: " + structuralDamage);
        System.out.println("ShieldReduction: " + shieldReduction);
        System.out.println("Bonus: " + bonus);
        System.out.println("Victim: " + victim.name);
        System.out.println("Victor: " + victor.name);
        System.out.println("Rammed: " + rammed);
        System.out.println("Type: " + type);
    }

}