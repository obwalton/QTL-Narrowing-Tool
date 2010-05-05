/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.ArrayList;

/**
 *
 * @author dow
 */
public class SNP implements IsSerializable {
    private static final long serialVersionUID =
            4741175800356479306L;

    private String snpID;
    private String rsNumber;
    private int build36Position;
    private int build37Position;
    private char hrBaseValue;
    private char lrBaseValue;
    private double hrMeanIntensity;
    private double lrMeanIntensity;
    private ArrayList<String> hrStrains;
    private ArrayList<String> lrStrains;
    private double pValue;
    private double qValue;
    private ArrayList<String> dbSNPAnnotations;
    private String source;

    public SNP () {
        this.hrStrains = new ArrayList<String>();
        this.lrStrains = new ArrayList<String>();
        this.dbSNPAnnotations = new ArrayList<String>();

    }
   
    public SNP(int position, String build) {
        this();
        if (build.toUpperCase().equals("36"))
            this.build36Position = position;
        else
            this.build37Position = position;
    }

    public int getBuild36Position() {
        return build36Position;
    }

    public void setBuild36Position(int build36Position) {
        this.build36Position = build36Position;
    }

    public int getBuild37Position() {
        return build37Position;
    }

    public void setBuild37Position(int build37Position) {
        this.build37Position = build37Position;
    }

    public ArrayList<String> getDbSNPAnnotations() {
        return dbSNPAnnotations;
    }

    public void setDbSNPAnnotations(ArrayList<String> dbSNPAnnotations) {
        this.dbSNPAnnotations = dbSNPAnnotations;
    }

    public void addDbSNPAnnotation(String annotation) {
        this.dbSNPAnnotations.add(annotation);
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
