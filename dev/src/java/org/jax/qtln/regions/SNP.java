/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author dow
 */
public class SNP implements Serializable {
    private static final long serialVersionUID =
            4741175800356479306L;

    private String snpID;
    private String rsNumber;
    private int bpPosition;
    private int build37Position;
    private char hrBaseValue;
    private char lrBaseValue;
    private double hrMeanIntensity;
    private double lrMeanIntensity;
    private ArrayList<String> hrStrains;
    private ArrayList<String> lrStrains;
    private double pValue;
    private double qValue;
    private ArrayList<Integer> cgdSnpAnnotations;
    private String source;
    private int cgdSnpId;
    private int cgdAssociatedGeneId;

    public int getCgdAssociatedGeneId() {
        return cgdAssociatedGeneId;
    }

    public void setCgdAssociatedGeneId(int cgdAssociatedGeneId) {
        this.cgdAssociatedGeneId = cgdAssociatedGeneId;
    }

    public int getCgdSnpId() {
        return cgdSnpId;
    }

    public void setCgdSnpId(int cgdSnpId) {
        this.cgdSnpId = cgdSnpId;
    }

    public SNP () {
        this.hrStrains = new ArrayList<String>();
        this.lrStrains = new ArrayList<String>();
        this.cgdSnpAnnotations = new ArrayList<Integer>();

    }
   
    public SNP(int position) {
        this();
        this.build37Position = position;
        this.bpPosition = position;
    }

    public int getBPPosition() {
        return bpPosition;
    }

    public void setBPPosition(int position) {
        this.bpPosition = position;
    }

    public int getBuild37Position() {
        return build37Position;
    }

    public void setBuild37Position(int build37Position) {
        this.build37Position = build37Position;
    }

    public ArrayList<Integer> getSnpAnnotations() {
        return cgdSnpAnnotations;
    }

    public void setSnpAnnotations(ArrayList<Integer> cgdSnpAnnotations) {
        this.cgdSnpAnnotations = cgdSnpAnnotations;
    }

    public void addSnpAnnotation(Integer annotation) {
        this.cgdSnpAnnotations.add(annotation);
    }

    public char getHighRespondingBaseValue() {
        return hrBaseValue;
    }

    public void setHighRespondingBaseValue(char hrBaseValue) {
        this.hrBaseValue = hrBaseValue;
    }

    public double getHighRespondingMeanIntensity() {
        return hrMeanIntensity;
    }

    public void setHighRespondingMeanIntensity(double hrMeanIntensity) {
        this.hrMeanIntensity = hrMeanIntensity;
    }

    public ArrayList<String> getHighRespondingStrains() {
        return hrStrains;
    }

    public void setHighRespondingStrains(ArrayList<String> hrStrains) {
        this.hrStrains = hrStrains;
    }

    public void addHighRepsondingStrain(String strain) {
        this.hrStrains.add(strain);
    }

    public char getLowRespondingBaseValue() {
        return lrBaseValue;
    }

    public void setLowRespondingBaseValue(char lrBaseValue) {
        this.lrBaseValue = lrBaseValue;
    }

    public double getLowRespondingMeanIntensity() {
        return lrMeanIntensity;
    }

    public void setLowRespondingMeanIntensity(double lrMeanIntensity) {
        this.lrMeanIntensity = lrMeanIntensity;
    }

    public ArrayList<String> getLowRespondingStrains() {
        return lrStrains;
    }

    public void setLowRespondingStrains(ArrayList<String> lrStrains) {
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

    public double getQValue() {
        return qValue;
    }

    public void setQValue(double qvalue) {
        this.qValue = qvalue;
    }

    public String getSnpId() {
        return snpID;
    }

    public void setSnpId(String snpId) {
        this.snpID = snpId;
    }

    public String getRsNumber() {
        return rsNumber;
    }

    public void setRsNumber(String rsNumber) {
        this.rsNumber = rsNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

}
