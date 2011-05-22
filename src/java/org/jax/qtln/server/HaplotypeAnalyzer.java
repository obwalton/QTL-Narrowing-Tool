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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

import java.util.HashMap;
import org.jax.qtln.regions.SNPDoesNotMeetCriteriaException;
import org.jax.qtln.regions.OverlappingRegion;
import org.jax.qtln.regions.SNP;
import org.jax.qtln.regions.Region;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jax.qtln.db.CGDSnpDB;

/**
 * Used for doing the Haplotype Analysis step of the QTL Narrowing workflow
 * Depends on a few other classes to do the actual work such as SNPFile and
 * SNP.  The HaplotypeAnalyzer is also dependent on the CGD Imputed SNP
 * files being present, and loaded into memory using the SNPFile class.
 * It is assumed that this has already been done and a "lookup" by chromosome
 * of all the imputed snps is passed into the class.
 *
 * The algorithm used to do the haplotype analysis takes the regions of
 * interest on a chromosome, and searches the lookup data structure for SNPS
 * that fall in that region.  Once the snps are found, a comparison is done
 * of the base calls for all the high responding strains in the region and all
 * the low responding strains in the region.  The base call for all the high
 * responding strains must be the same, and the base calls for all the low
 * responding strains must be the same, but the base value between high and
 * low responding strains must be different.
 *
 * If a SNP is determined to pass this criteria, then the base calls for both
 * high and low responding strains are returned.  Additionally all other strains
 * with a base call that matches the high responding strain are added to the
 * high responding strain set.  Likewise, any additional strains with a base
 * call that matches the low responding is added to that set.
 *
 * @author dow
 */
public class HaplotypeAnalyzer {
    private List strains;
    private Map<String, SNPFile> cgdSNPLookup;
    private CGDSnpDB snpLookup;

    /**
     * Constructor for HaplotypeAnalyzer
     *
     * @param lookup This is a Map, by chromosome, of all the CGD Imputed SNPS
     */
    public HaplotypeAnalyzer () {
        this.snpLookup = new CGDSnpDB();
    }

    /**
     * Constructor for HaplotypeAnalyzer
     *
     * @param lookup This is a Map, by chromosome, of all the CGD Imputed SNPS
     *
     * This version uses an in memory lookup.  This was very memory intensive.
     * Rewriting (using default constructor) to use CGD Database.  If the DB 
     * performs well enough, we'll delete this constructor.
     */
    public HaplotypeAnalyzer (Map<String, SNPFile> lookup) {
        this.cgdSNPLookup = lookup;
    }

    /**
     * This method is used to actually execute the haplotype analysis.  See
     * the class header for an explaination of the analysis.
     *
     * @param regions  This is a map of regions of interest by chromosome.
     *    The regions object is updated with SNPS that are found.
     */
    public void doAnalysis(Map<String, List<Region>> regions)
    {
        System.out.println("In HaplotypeAnalyzer.doAnalysis...");
        //  The keys are chromsomes
        Set<String> keys = regions.keySet();
        Runtime rt = Runtime.getRuntime();
        //  for each chromsome
        for (String key : keys) {
            //  for each region on the chromosome
            System.out.println("Getting SNPs for Chromosome " + key);

            for (Region region : regions.get(key)) {
                region = getSnpsInRegion(key, (OverlappingRegion)region);
            }
        }

    }

    /**
     * Finds all of the SNPS in our region.
     *
     * @param chromosome  The chromosome of interest
     * @param region  The region we are finding SNPs in
     * @return  A new version of the region with the SNPs added.
     *
     * This version of the method uses the in memory store.  Used tons of
     * memory.  Rewriting to use db.  If that performs well enough, delete this
     * method.
     *
     */
    private Region getSnpsInRegion(String chromosome, OverlappingRegion region) {
        //  get two lists from region:
        //      High responding strains
        //      Low responding strains
        List highRespondingStrains = region.getHighRespondingStrains();
        List lowRespondingStrains = region.getLowRespondingStrains();
        HashMap<String, Integer>  diagnostics = new HashMap<String, Integer>();
        
        //  Get look-up for this chromosome
        SNPFile snpLookup = this.cgdSNPLookup.get(chromosome);

        //  Find the array of candidate SNPs in the region.
        int[] snpSubSet = snpLookup.findSnpPositionsInRange(region.getStart(),
                region.getEnd());
        region.setTotalNumSNPsInRegion(snpSubSet.length);

        // Get all the SNPs that meet the criteria and add them to the
        // region. (criteria defined in class header and the SNPFile class...)
        int snp_count = 0;
        int no_b_37_pos = 0;
        for (int snp_position:snpSubSet) {
            if (snp_position < 0) {
                //  If the snp_position is -1, this means that while there was
                //  a build 36 position, there was no build 37 position for this
                //  SNP.  Skip it.
                ++no_b_37_pos;
                continue;
            }
            SNP snp = null;
            try {
                snp = snpLookup.analyzeSNP(snp_position,
                        highRespondingStrains, lowRespondingStrains);
            }
            catch (SNPDoesNotMeetCriteriaException e) {
                //  TODO:  Consider logging snps that fail criteria and why
                if(diagnostics.containsKey(e.getMessage())) {
                    int value = diagnostics.get(e.getMessage()).intValue();
                    value++;
                    diagnostics.put(e.getMessage(), value);
                }
                else {
                    diagnostics.put(e.getMessage(), 1);
                }
                continue;
            }
            // If SNP kept add to our region
            if (snp != null)
                region.addSnp(snp);
                ++snp_count;

        }
        System.out.println("Kept " + snp_count + " for region");
        if (no_b_37_pos > 0) {
            System.out.println("There were " + no_b_37_pos + " SNPs skipped because there was no B 37 position.");
        }

        // Print out some diagnostics about rejected snps
        for (String key:diagnostics.keySet()) {
            System.out.println(key + " " + diagnostics.get(key));
        }
        return region;
    } 



}
