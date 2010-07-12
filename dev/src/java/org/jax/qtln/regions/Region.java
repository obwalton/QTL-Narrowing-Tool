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
public interface Region extends Serializable {

    public void setChromosome(String chromosome);

    public void setEnd(int end);

    public void setStart(int start);

    public String getChromosome();

    public int getEnd();
 
    public int getStart();

    //TODO:  These three are not common to QTL and overlapping region...remove?
    public void setSnps(TreeMap<Integer,SNP> snps);
    
    public TreeMap<Integer,SNP> getSnps();

    public Map<Integer, Gene> getGenes();

}
