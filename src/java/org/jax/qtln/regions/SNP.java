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
    private char hrBaseValue;
    private char lrBaseValue;
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
        this.cgdSnpAnnotations = new ArrayList<Integer>();

    }
   
    public SNP(int position) {
        this();
        this.bpPosition = position;
    }

    public int getBPPosition() {
        return bpPosition;
    }

    public void setBPPosition(int position) {
        this.bpPosition = position;
    }

    public ArrayList<Integer> getSnpAnnotations() {
        return cgdSnpAnnotations;
    }

    public void setSnpAnnotations(ArrayList<Integer> cgdSnpAnnotations) {
        this.cgdSnpAnnotations = cgdSnpAnnotations;
    }

    public void addSnpAnnotation(Integer annotation) {
        if (! this.cgdSnpAnnotations.contains(annotation))
            this.cgdSnpAnnotations.add(annotation);
    }

    public char getHighRespondingBaseValue() {
        return hrBaseValue;
    }

    public void setHighRespondingBaseValue(char hrBaseValue) {
        this.hrBaseValue = hrBaseValue;
    }

    public char getLowRespondingBaseValue() {
        return lrBaseValue;
    }

    public void setLowRespondingBaseValue(char lrBaseValue) {
        this.lrBaseValue = lrBaseValue;
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
