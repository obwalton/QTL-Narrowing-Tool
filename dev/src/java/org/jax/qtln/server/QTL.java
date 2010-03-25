/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

/**
 *
 * @author dow
 */
public class QTL implements Region, Comparable {
    
    private String qtlID;
    private String phenotype;
    private String species;
    private String highResponder;
    private String lowResponder;
    private String chromosome;
    private int qtlStart;
    private int qtlEnd;
    private String build;

    public QTL() {
        super();
    }

    public QTL(String qtl) {
        this.qtlID = qtl;
    }

    public QTL(String qtl, String pheno, String species, String hi, String lo,
            String chr, int start, int end, String build) {
        this.qtlID = qtl;
        this.phenotype = pheno;
        this.species = species;
        this.highResponder = hi;
        this.lowResponder = lo;
        this.chromosome = chr;
        this.qtlStart = start;
        this.qtlEnd = end;
        this.build = build;
    }

    public void setBuild(String build) {
        this.build = build;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public void setHighResponder(String highResponder) {
        this.highResponder = highResponder;
    }

    public void setLowResponder(String lowResponder) {
        this.lowResponder = lowResponder;
    }

    public void setPhenotype(String phenotype) {
        this.phenotype = phenotype;
    }

    public void setEnd(int qtlEnd) {
        this.qtlEnd = qtlEnd;
    }

    public void setQtlID(String qtlID) {
        this.qtlID = qtlID;
    }

    public void setStart(int qtlStart) {
        this.qtlStart = qtlStart;
    }

    public void setSpecies(String species) {
        this.species = species;
    }

    public String getBuild() {
        return build;
    }

    public String getChromosome() {
        return chromosome;
    }

    public String getHighResponder() {
        return highResponder;
    }

    public String getLowResponder() {
        return lowResponder;
    }

    public String getPhenotype() {
        return phenotype;
    }

    public int getEnd() {
        return qtlEnd;
    }

    public String getQtlID() {
        return qtlID;
    }

    public int getStart() {
        return qtlStart;
    }

    public String getSpecies() {
        return species;
    }

    public int compareTo (Object o) {
        QTL other = (QTL)o;
        int ret_val = 0;
        if (this.getStart() < other.getStart()) {
            ret_val = -1;
        }
        else if (this.getStart() > other.getStart()) {
            ret_val = 1;
        }
        return ret_val;
    }

}
