/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author dow
 */
public class ReturnRegion implements Serializable {

    private String regionKey;
    private QTLSet qtls;
    private Integer numberSnps;
    private List<Gene> genes;

    public ReturnRegion() {
    }

    public ReturnRegion(String region_key, QTLSet qtls, Integer number_snps, 
            List<Gene> genes) {
        this.regionKey = region_key;
        this.qtls = qtls;
        this.numberSnps = number_snps;
        this.genes = genes;
    }

    public List<Gene> getGenes() {
        return genes;
    }

    public void setGenes(List<Gene> genes) {
        this.genes = genes;
    }

    public Integer getNumberSnps() {
        return numberSnps;
    }

    public void setNumberSnps(Integer numberSnps) {
        this.numberSnps = numberSnps;
    }

    public QTLSet getQtls() {
        return qtls;
    }

    public void setQtls(QTLSet qtls) {
        this.qtls = qtls;
    }

    public String getRegionKey() {
        return regionKey;
    }

    public void setRegionKey(String regionKey) {
        this.regionKey = regionKey;
    }

}
