/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 *
 * @author dow
 */
public class SmallestCommonRegion {

    private QTLSet qtlSet;

    public SmallestCommonRegion (QTLSet q) {
        this.qtlSet = q;
    }

    public List<Region> getRegions() {
        ArrayList<Region> regions = new ArrayList<Region>();
        HashMap<String, QTLSet> chromosomeGroups =
                new HashMap<String, QTLSet>();

        Set<String> chromosomes = this.qtlSet.getChromosomesInSet();

        for (String chr : chromosomes) {
            chromosomeGroups.put(chr, this.qtlSet.getByChromosome(chr));
        }

        // Cycle through our QTLs on this
        for (String chr : chromosomeGroups.keySet()) {
            QTLSet qtls = chromosomeGroups.get(chr);
            for (QTL qtl : qtls.asList()) {
                //  TODO: Cycle through each QTL in the set.  Look for other qtls
                //  that overlap, create a new region... for one chr there
                //  could be multiple regions and some qtls with no overlaps,
                //  that are therefore regions of their own.
            }
        }



        return (List<Region>)regions;
    }

}
