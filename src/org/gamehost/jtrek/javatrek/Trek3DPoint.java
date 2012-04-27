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
 * Represents a single point in a 3 dimensional space.
 *
 * @author Joe Hopkinson
 */
public final class Trek3DPoint {
    protected float x, y, z;

    public Trek3DPoint() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Trek3DPoint(Trek3DPoint p) {
        x = p.x;
        y = p.y;
        z = p.z;
    }

    public Trek3DPoint(float xin, float yin, float zin) {
        x = xin;
        y = yin;
        z = zin;
    }

    public void add(Trek3DPoint p) {
        x += p.x;
        y += p.y;
        z += p.z;
    }

    public void add(Trek3DVector v) {
        x += v.x;
        y += v.y;
        z += v.z;
    }

    public void subtract(Trek3DPoint p) {
        x -= p.x;
        y -= p.y;
        z -= p.z;
    }

    public void subtract(Trek3DVector v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
    }

    public String getXString() {
        return new Integer(new Float(x).intValue()).toString();
    }

    public String getYString() {
        return new Integer(new Float(y).intValue()).toString();
    }

    public String getZString() {
        return new Integer(new Float(z).intValue()).toString();
    }

    public int getXInt() {
        return Math.round(x);
    }

    public int getYInt() {
        return Math.round(y);
    }

    public int getZInt() {
        return Math.round(z);
    }

    public String toString() {
        return new String("(" + getXString() + ", " + getYString() +
                ", " + getZString() + ")");
    }

    public boolean equals(Trek3DPoint targetPoint) {
        if (getXInt() == targetPoint.getXInt() && getYInt() == targetPoint.getYInt() && getZInt() == targetPoint.getZInt()) {
            return true;
        }
        return false;
    }
}