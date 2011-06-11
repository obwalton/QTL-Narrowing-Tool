/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jax.qtln.mgisearch;

import java.util.HashMap;
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

    @Field("cm")
    Float centimorgans;
    
    @Field("symbol")
    String symbol;

    @Field
    String name;

    @Field("terms")
    String[] mpterms;

    public Float getCentimorgans() {
        return centimorgans;
    }

    public void setCentimorgans(Float centimorgans) {
        this.centimorgans = centimorgans;
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