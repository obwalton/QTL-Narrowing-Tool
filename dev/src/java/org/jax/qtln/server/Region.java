/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

/**
 *
 * @author dow
 */
public class Region {
    private QTLSet qtls;
    private String chromosome;
    private int start;
    private int end;
    private String build;

    public Region () {
        this.qtls = new QTLSet();
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

    public QTLSet getQtls() {
        return qtls;
    }

    public int getStart() {
        return start;
    }

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



}
