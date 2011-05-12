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
