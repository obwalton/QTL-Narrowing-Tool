/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.client;

import java.io.Serializable;


/**
 *
 * @author dow
 */
public class GWTRegion implements Serializable {
    private String build;
    private String chromosome;
    private int end;
    private int start;

    public void setBuild(String build) {
        this.build = build;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public String getBuild() {
        return build;
    }

    public String getChromosome() {
        return chromosome;
    }

    public int getEnd() {
        return end;
    }
 
    public int getStart() {
        return start;
    }

}
