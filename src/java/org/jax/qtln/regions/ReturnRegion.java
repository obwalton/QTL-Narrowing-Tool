/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import java.io.Serializable;
import java.util.List;

/**
 *
 * @author dow
 */
public class ReturnRegion implements Serializable {

    private String regionKey;
    private QTLSet qtls;
    //  This represents all selected SNPs in region
    private Integer numberSnps;
    //  This represents all SNPs in region
    private Integer totalNumberSnps;
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

    public int getTotalNumSNPsInRegion() {
        return this.totalNumberSnps;
    }

    public void setTotalNumSnps(Integer numberSnps) {
        this.totalNumberSnps = numberSnps;
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
