/*
 *  Copyright (C) 2014  Alfons Wirtz  
 *   website www.freerouting.net
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License at <http://www.gnu.org/licenses/> 
 *   for more details.
 *
 * IntVector.java
 *
 * Created on 1. Februar 2003, 14:47
 */
package net.freerouting.freeroute.geometry.planar;

import static com.google.common.math.IntMath.gcd;
import net.freerouting.freeroute.datastructures.Signum;

/**
 *
 * Implementation of the interface Vector via a tuple of integers
 *
 * @author Alfons Wirtz
 */
@SuppressWarnings("serial")
public final class IntVector extends Vector {

    /**
     * the x coordinate of this vector
     */
    public final int x;
    /**
     * the y coordinate of this vector
     */
    public final int y;

    /**
     * creates an IntVector from two integer coordinates
     */
    public IntVector(int p_x, int p_y) {
        // range check ommitet for performance reasons
        x = p_x;
        y = p_y;
    }

    /**
     * returns true, if this IntVector is equal to p_ob
     */
    @Override
    public boolean equals(Object p_ob) {
        if (this == p_ob) {
            return true;
        }
        if (p_ob == null) {
            return false;
        }
        if (getClass() != p_ob.getClass()) {
            return false;
        }
        IntVector other = (IntVector) p_ob;
        return (x == other.x && y == other.y);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.x;
        hash = 37 * hash + this.y;
        return hash;
    }

    /**
     * returns true, if both coordinates of this vector are 0
     */
    @Override
    public boolean is_zero() {
        return x == 0 && y == 0;
    }

    /**
     * returns the Vector such that this plus this.minus() is zero
     */
    @Override
    public Vector negate() {
        return new IntVector(-x, -y);
    }

    @Override
    public boolean is_orthogonal() {
        return (x == 0 || y == 0);
    }

    @Override
    public boolean is_diagonal() {
        return (Math.abs(x) == Math.abs(y));
    }

    /**
     * Calculates the determinant of the matrix consisting of this Vector and
     * p_other.
     */
    public long determinant(IntVector p_other) {
        return (long) x * p_other.y - (long) y * p_other.x;
    }

    @Override
    public Vector turn_90_degree(int p_factor) {
        int n = p_factor;
        while (n < 0) {
            n += 4;
        }
        while (n >= 4) {
            n -= 4;
        }
        int new_x;
        int new_y;
        switch (n) {
            case 0: // 0 degree
                new_x = x;
                new_y = y;
                break;
            case 1: // 90 degree
                new_x = -y;
                new_y = x;
                break;
            case 2: // 180 degree
                new_x = -x;
                new_y = -y;
                break;
            case 3: // 270 degree
                new_x = y;
                new_y = -x;
                break;
            default:
                new_x = 0;
                new_y = 0;
        }
        return new IntVector(new_x, new_y);
    }

    @Override
    public Vector mirror_at_y_axis() {
        return new IntVector(-this.x, this.y);
    }

    @Override
    public Vector mirror_at_x_axis() {
        return new IntVector(this.x, -this.y);
    }

    @Override
    public Vector add(IntVector p_other) {
        return new IntVector(x + p_other.x, y + p_other.y);
    }

    @Override
    public Vector add(RationalVector p_other) {
        return p_other.add(this);
    }

    /**
     * returns the Point, which results from adding this vector to p_point
     */
    @Override
    public Point add_to(IntPoint p_point) {
        return new IntPoint(p_point.x + x, p_point.y + y);
    }

    @Override
    public Point add_to(RationalPoint p_point) {
        return p_point.translate_by(this);
    }

    /**
     * Let L be the line from the Zero Vector to p_other. The function returns
     * Side.ON_THE_LEFT, if this Vector is on the left of L Side.ON_THE_RIGHT,
     * if this Vector is on the right of L and Side.COLLINEAR, if this Vector is
     * collinear with L.
     */
    @Override
    public Side side_of(IntVector p_other) {
        double determinant = (double) p_other.x * y - (double) p_other.y * x;
        return Side.of(determinant);
    }

    @Override
    public Side side_of(RationalVector p_other) {
        Side tmp = p_other.side_of(this);
        return tmp.negate();
    }

    /**
     * converts this vector to a PointFloat.
     */
    @Override
    public FloatPoint to_float() {
        return new FloatPoint(x, y);
    }

    @Override
    public Vector change_length_approx(double p_length) {
        FloatPoint new_point = this.to_float().change_size(p_length);
        return new_point.round().difference_by(PointUtils.ZERO);
    }

    @Override
    public Direction to_normalized_direction() {
        int dx = x;
        int dy = y;

        int gcd_positive = gcd(Math.abs(dx), Math.abs(dy));
        if (gcd_positive > 1) {
            dx /= gcd_positive;
            dy /= gcd_positive;
        }
        return new IntDirection(dx, dy);
    }

    /**
     * The function returns Signum.POSITIVE, if the scalar product of this
     * vector and p_other > 0, Signum.NEGATIVE, if the scalar product Vector is
     * < 0, and Signum.ZERO, if the scalar product is equal 0.
     */
    @Override
    public Signum projection(IntVector p_other) {
        double tmp = (double) x * p_other.x + (double) y * p_other.y;
        return Signum.of(tmp);
    }

    @Override
    public Signum projection(RationalVector p_other) {
        return p_other.projection(this);
    }

    @Override
    public double scalar_product(IntVector p_other) {
        return (double) x * p_other.x + (double) y * p_other.y;
    }

    @Override
    public double scalar_product(RationalVector p_other) {
        return p_other.scalar_product(this);
    }
}
