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

package org.jax.qtln.regions;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author dow
 */
public class QTL implements Region, Comparable, Serializable {
    private static final long serialVersionUID =
            4741175800356479306L;

    
    private String qtlID;
    private String phenotype;
    private String species;
    private String highResponder;
    private String lowResponder;
    private String chromosome;
    private int qtlStart;
    private int qtlEnd;
    private TreeMap<Integer,SNP> snps;
    private int totalSnps = 0;


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

    public TreeMap<Integer,SNP> getSnps() {
        return this.snps;
    }

    public void setSnps(TreeMap<Integer,SNP> snps) {
        this.snps = snps;
    }

    public Map<Integer,Gene> getGenes() {
        return null;
    }

    public void setTotalNumSNPsInRegion(int numsnps) {
        this.totalSnps = numsnps;
    }

    public int getTotalNumSNPsInRegion() {
        return this.totalSnps;
    }


}
