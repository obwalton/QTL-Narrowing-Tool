/*
 * Copyright (c) 2010 The Jackson Laboratory
 * 
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jax.qtln.server;

import org.jax.qtln.regions.QTL;
import org.jax.qtln.regions.QTLSet;
import org.jax.qtln.regions.OverlappingRegion;
import org.jax.qtln.regions.Region;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author dow
 */
public class SmallestCommonRegion {

    private QTLSet qtlSet;

    public SmallestCommonRegion (QTLSet qtlSet) {
        this.qtlSet = qtlSet;
    }

    /**
     * This method will get the smallest common regions in a set of QTLs
     * using the following algorithm:
     *
     * 1) Group the QTLs by Chromosome
     * 2) On each Chromosome, sort the QTLs by start position
     * 3) loop through the QTLs
     * 4) If QTL start is > "maxStart" && QTL start < "minEnd" set maxStart
     *      a) If above and If QTL end is < "minEnd" set minEnd
     *      b) Add this QTL to current contributing QTL list
     *
     * 5) If QTL start > "minEnd"...
     *      a) Add new region to list of regions, including all contributing qtls
     *      b) Remove from contributing QTLs list, all QTLs with an end < current
     *         QTL start
     *      c) Add this QTL to contributing QTLs
     *      d) from list of contributing QTLs get maxStart & minEnd
     * 6) Continue
     * @return a Map keyed by chromosome, each key pointing to a list of
     *    regions on that chromosome.
     */
    public Map<String, List<Region>> getRegions() {
        HashMap<String, List<Region>> chrRegionMap =
                new HashMap<String, List<Region>>();

        Set<String> chromosomes = qtlSet.getChromosomesInSet();

        // Cycle through our QTLs on this
        for (String chr : chromosomes) {
            QTLSet tmpQtls = qtlSet.getByChromosome(chr);
            tmpQtls.orderByStart();
            ArrayList<Region> regions = new ArrayList<Region>();
            int maxStart = 0;
            int minEnd   = 0;
            ArrayList<QTL> contributingQTLs = new ArrayList<QTL>();

            for (QTL qtl : tmpQtls.asList()) {
                //  This is the first QTL in the region, just add it
                if (minEnd == 0) {
                    maxStart = qtl.getStart();
                    minEnd = qtl.getEnd();
                    contributingQTLs.add(qtl);
                }
                else if (qtl.getStart() > maxStart && qtl.getStart() < minEnd){
                    maxStart = qtl.getStart();
                    if (qtl.getEnd() < minEnd)
                        minEnd = qtl.getEnd();
                    contributingQTLs.add(qtl);
                }
                else if (qtl.getStart() > minEnd) {
                    OverlappingRegion region = new OverlappingRegion(chr);
                    region.setStart(maxStart);
                    region.setEnd(minEnd);
                    // set minEnd to this QTL's end, temporarily
                    minEnd = qtl.getEnd();
                    ArrayList<QTL> markForRemoval = new ArrayList();
                    for (Iterator iter = contributingQTLs.iterator(); iter.hasNext();) {
                        QTL contribQtl = (QTL)iter.next();
                        region.addQtl(contribQtl);
                        
                        if (contribQtl.getEnd() < qtl.getStart()) {
                            markForRemoval.add(contribQtl);
                            //contributingQTLs.remove(contribQtl);
                        }
                        else if (contribQtl.getEnd() < minEnd) {
                            minEnd = contribQtl.getEnd();
                        }
                    }
                    regions.add(region);
                    for (QTL remQtl:markForRemoval)
                        contributingQTLs.remove(remQtl);

                    maxStart = qtl.getStart();
                    contributingQTLs.add(qtl);
                }
            }
            // Process last region
            OverlappingRegion region = new OverlappingRegion(chr);
            region.setStart(maxStart);
            region.setEnd(minEnd);
            for (Iterator iter = contributingQTLs.iterator(); iter.hasNext();) {
                QTL contribQTL = (QTL)iter.next();
                region.addQtl(contribQTL);
            }
            regions.add(region);
            chrRegionMap.put(chr, regions);
        }


        return (Map<String,List<Region>>)chrRegionMap;
    }


}
