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
 * Represents a single Vector in a 3 dimensional space.
 *
 * @author Joe Hopkinson
 */
public final class Trek3DVector {
    protected float x, y, z;

    public Trek3DVector(float xin, float yin, float zin) {
        x = xin;
        y = yin;
        z = zin;
    }

    public Trek3DVector() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Trek3DVector(Trek3DVector v) {
        x = v.x;
        y = v.y;
        z = v.z;
    }

    protected double magnitude() {
        return Math.sqrt((x * x + y * y + z * z));
    }

    public void scaleDown(double d) {
        x /= d;
        y /= d;
        z /= d;
    }

    public void scaleUp(double d) {
        x *= d;
        y *= d;
        z *= d;
    }

    public void subtract(Trek3DVector v) {
        x -= v.x;
        y -= v.y;
        z -= v.z;
    }

    public static float dot(Trek3DVector v1, Trek3DVector v2) {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z;
    }

    public static Trek3DVector cross(Trek3DVector v1, Trek3DVector v2) {
        return new Trek3DVector((v1.y * v2.z) - (v2.y * v1.z), (v1.z * v2.x) - (v2.z * v1.x), (v1.x * v2.y) - (v2.x * v1.y));
    }

    public void normalize() {
        scaleDown(magnitude());
    }

    public void applyPoint(Trek3DPoint p) {
        x = p.x;
        y = p.y;
        z = p.z;
    }

    public int getXInt() {
        return (Math.round(this.x));
    }

    public int getYInt() {
        return (Math.round(this.y));
    }

    public int getZInt() {
        return (Math.round(this.z));
    }

    // return the heading value represented by this vector
    public double getHeading() {
        double result = 0;

        if (getXInt() != 0) {
            //protect against division by zero
            result = 90 - Math.toDegrees(Math.atan(y / x));

            // adjust heading based on what quadrant it's in
            if ((getYInt() < 0) && (getXInt() < 0)) {
                result += 180;
            }

            if ((getYInt() > 0) && (getXInt() < 0)) {
                result += 180;
            }

            if (getYInt() == 0) {
                // handle vectors where y is 0
                if (getXInt() > 0) {
                    result = 90;
                }

                if (getXInt() < 0) {
                    result = 270;
                }
            }

        } else {
            // handle vectors where x is 0
            if (getYInt() < 0) {
                result = 180;
            }

            if (getYInt() > 0) {
                result = 0;
            }

            if (getYInt() == 0) {
                // current location
                result = 0;
            }
        }

        return result;

    }

    // return the pitch -90 to +90 represented by this vector
    public double getPitch() {
        double ex = Math.pow(x, 2);
        double ey = Math.pow(y, 2);
        double ez = Math.pow(z, 2);

        double distance = Math.sqrt(ex + ey + ez);
        double result = 0;

        // trap potential divide by zero
        if (distance != 0) {
            result = Math.toDegrees(Math.asin(z / distance));
        } else {
            result = 90;
        }

        return result;
    }

    public String toString() {
        return new String("( " + x + ", " + y + ", " + z + " )");
    }
}
