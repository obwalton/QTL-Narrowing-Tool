/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dow
 */
public class Gene implements Serializable {
    private static final long serialVersionUID =
            4741175800356479306L;

    private List<SNP> associatedSNPs;
    private double hrMeanIntensity;
    private double lrMeanIntensity;
    private List<String> hrStrains;
    private List<String> lrStrains;
    private double pValue;
    private int cgdGeneId;
    private String mgiAccessionId;
    private String symbol;
    private String name;
    private List<String> probeSets;

    public Gene () {
        this.hrStrains = new ArrayList<String>();
        this.lrStrains = new ArrayList<String>();
        this.associatedSNPs = new ArrayList<SNP>();
        this.probeSets = new ArrayList<String>();
        this.hrMeanIntensity = Double.NaN;
        this.lrMeanIntensity = Double.NaN;
        this.pValue = Double.NaN;
    }

    public Gene(int cgdId, String mgi) {
        this();
        setCgdGeneId(cgdId);
        setMgiId(mgi);
    }

    public Gene(int cgdId, String mgi, String symbol, String name) {
        this(cgdId, mgi);
        setSymbol(symbol);
        setName(name);
    }

    public int getCgdGeneId() {
        return cgdGeneId;
    }

    public void setCgdGeneId(int cgdGeneId) {
        this.cgdGeneId = cgdGeneId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMgiId() {
        return mgiAccessionId;
    }

    public void setMgiId(String id) {
        this.mgiAccessionId = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public List<String> getProbeSetIds() {
        return probeSets;
    }

    public void setProbeSetIds(List<String> probeSetIds) {
        this.probeSets = probeSetIds;
    }

    public void addProbeSetId(String probeSetId) {
        this.probeSets.add(probeSetId);
    }

    public List<SNP> getAssociatedSnps() {
        return associatedSNPs;
    }

    public void setAssociatedSnps(List<SNP> snps) {
        this.associatedSNPs = snps;
    }

    public void addAssociatedSnp(SNP snp) {
        this.associatedSNPs.add(snp);
    }


    public double getHighRespondingMeanIntensity() {
        return hrMeanIntensity;
    }

    public void setHighRespondingMeanIntensity(double hrMeanIntensity) {
        this.hrMeanIntensity = hrMeanIntensity;
    }

    public List<String> getHighRespondingStrains() {
        return hrStrains;
    }

    public void setHighRespondingStrains(List<String> hrStrains) {
        this.hrStrains = hrStrains;
    }

    public void addHighRespondingStrain(String strain) {
        this.hrStrains.add(strain);
    }

    public double getLowRespondingMeanIntensity() {
        return lrMeanIntensity;
    }

    public void setLowRespondingMeanIntensity(double lrMeanIntensity) {
        this.lrMeanIntensity = lrMeanIntensity;
    }

    public List<String> getLowRespondingStrains() {
        return lrStrains;
    }

    public void setLowRespondingStrains(List<String> lrStrains) {
        this.lrStrains = lrStrains;
    }

    public void addLowRespondingStrain(String strain) {
        this.lrStrains.add(strain);
    }

    public double getPValue() {
        return pValue;
    }

    public void setPValue(double pValue) {
        this.pValue = pValue;
    }

}
