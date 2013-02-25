/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jax.qtln.mgisearch;

import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author dave
 */
public class MGIQTL {
    
    @Field
    String id;

    @Field("chr")
    String chromosome;

    @Field("chr_num")
    Integer chromosomeNumber;

    @Field("start")
    Integer bp_start;

    @Field("end")
    Integer bp_end;

    @Field("symbol")
    String symbol;

    @Field
    String name;

    @Field("terms")
    String[] mpterms;

    @Field("refs")
    String[] refids;

    public Integer getBp_start() {
        return bp_start;
    }

    public void setBp_start(Integer start) {
        this.bp_start = start;
    }

    public Integer getBp_end() {
        return bp_end;
    }

    public void setBp_end(Integer end) {
        this.bp_end = end;
    }

    public String getChromosome() {
        return chromosome;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
        if (chromosome.equals("X")) 
            this.chromosomeNumber = 20;
        else if (chromosome.equals("Y"))
            this.chromosomeNumber = 21;
        else
            try {
            this.chromosomeNumber = new Integer(chromosome);
            } catch (NumberFormatException nfe) {
                this.chromosomeNumber = -1;
            }
            
        
    }

    public String getChromosomeNumber() {
        return chromosome;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String[] getMpterms() {
        return mpterms;
    }

    public void setMpterms(String[] mpterms) {
        this.mpterms = mpterms;
    }

    public String[] getRefids() {
        return refids;
    }

    public void setRefids(String[] refids) {
        this.refids = refids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

  }