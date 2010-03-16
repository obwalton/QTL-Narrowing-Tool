/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author dow
 */
public class QTLSet {

    private ArrayList qtls;

    public QTLSet () {
        this.qtls = new ArrayList();
    }

    public void addQTL(List qtl) {
        //  Need to add a couple checks to this method to make sure there
        //  are the right number of columns and that the appropriate columns
        //  are numeric.
        QTL newQtl = new QTL();
        newQtl.setQtlID((String)qtl.get(0));
        newQtl.setPhenotype((String)qtl.get(1));
        newQtl.setSpecies((String)qtl.get(2));
        newQtl.setHighResponder((String)qtl.get(3));
        newQtl.setLowResponder((String)qtl.get(4));
        newQtl.setChromosome((String)qtl.get(5));
        newQtl.setQtlStart(((Integer)qtl.get(6)).intValue());
        newQtl.setQtlEnd(((Integer)qtl.get(7)).intValue());
        newQtl.setBuild((String)qtl.get(8));
        qtls.add(newQtl);
    }

    public void addQTL(QTL qtl) {
        qtls.add(qtl);
    }

    public List<QTL> asList() {
        return (List<QTL>)qtls;
    }

    public QTLSet getByChromosome(String chromosome) {
        QTLSet subSet = new QTLSet();
        for (Object qtl : this.qtls) {
            if (((QTL)qtl).getChromosome().equals(chromosome)) {
                subSet.addQTL((QTL)qtl);
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

}
