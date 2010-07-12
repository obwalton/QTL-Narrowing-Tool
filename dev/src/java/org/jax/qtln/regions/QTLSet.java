/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author dow
 */
public class QTLSet implements Serializable {
    private static final long serialVersionUID =
            4741175800356479306L;

    private ArrayList<QTL> qtls;
    private String chromosome;
    private int minCoordinate = 0;
    private int maxCoordinate = 0;
    private boolean unsetMin = true;

    public QTLSet () {
        this.chromosome = "";
        this.qtls = new ArrayList();
    }

    public QTLSet (String chr) {
        this.chromosome = chr;
        this.qtls = new ArrayList();
    }

    public void addQTL(List qtl)
        throws InvalidChromosomeException
    {
        //  Need to add a couple checks to this method to make sure there
        //  are the right number of columns and that the appropriate columns
        //  are numeric.
        QTL newQtl = new QTL();
        newQtl.setQtlID(((String)qtl.get(0)).trim());
        newQtl.setPhenotype(((String)qtl.get(1)).trim());
        newQtl.setSpecies(((String)qtl.get(2)).trim());
        newQtl.setHighResponder(((String)qtl.get(3)).trim());
        newQtl.setLowResponder(((String)qtl.get(4)).trim());
        newQtl.setChromosome(((String)qtl.get(5)).trim());
        newQtl.setStart((new Integer((String)qtl.get(6))).intValue());
        newQtl.setEnd((new Integer((String)qtl.get(7))).intValue());
        
        this.addQTL(newQtl);
    }

    public void addQTL(QTL qtl) 
        throws InvalidChromosomeException
    {
        if (! this.chromosome.equals("") &&
                ! qtl.getChromosome().equals(this.chromosome))
            throw new InvalidChromosomeException("Chromosome of QTL does not "+
                    " match chromosome for QTLSet");

        qtls.add(qtl);
        if (this.unsetMin || qtl.getStart() < this.minCoordinate) {
            this.minCoordinate = qtl.getStart();
            this.unsetMin = false;
        }

        if (qtl.getEnd() > this.maxCoordinate)
            this.maxCoordinate = qtl.getEnd();
    }

    public String getChromosome() {
        return chromosome;
    }

    public int getMaxCoordinate() {
        return maxCoordinate;
    }

    public int getMinCoordinate() {
        return minCoordinate;
    }

    public List<QTL> asList() {
        return (List<QTL>)qtls;
    }

    public QTLSet getByChromosome(String chromosome) {
        QTLSet subSet = new QTLSet(chromosome);
        for (Object qtl : this.qtls) {
            if (((QTL)qtl).getChromosome().equals(chromosome)) {
                try {
                    subSet.addQTL((QTL)qtl);
                }
                catch (InvalidChromosomeException ice) {
                    //  Do nothing here as we've set the chromosome of
                    //  the QTLSet, and are only adding qtls with the
                    //  correct Chromsome
                }
            }
        }

        return subSet;
    }

    public Set<String> getChromosomesInSet() {
        HashSet<String> chromosomes = new HashSet();
        for (Object qtl : this.qtls) {
            chromosomes.add(((QTL)qtl).getChromosome());
        }
        return (Set<String>)chromosomes;
    }


    public void orderByStart() {
        Collections.sort(this.qtls);
    }
}
