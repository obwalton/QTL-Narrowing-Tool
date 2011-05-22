/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

import java.util.List;

/**
 *
 * @author dow
 */
public class ArrayUtils {

    public static int[] toArrayInt(List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;

    }

    public static int[] toArrayInt(List<Integer> list, int nullDefault) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null)
                array[i] = nullDefault;
            else
                array[i] = list.get(i);
        }
        return array;

    }

    public static double[] toArrayDouble(List<Double> list) {
        double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;

    }

    public static double[] toArrayDouble(List<Double> list, double nullDefault) {
         double[] array = new double[list.size()];
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) == null)
                array[i] = nullDefault;
            else
                array[i] = list.get(i);
        }
        return array;

    }
}
