/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

import org.jax.qtln.regions.SNPDoesNotMeetCriteriaException;
import org.jax.qtln.regions.OverlappingRegion;
import org.jax.qtln.regions.SNP;
import org.jax.qtln.regions.Region;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Constructor for HaplotypeAnalyzer
     *
     * @param lookup This is a Map, by chromosome, of all the CGD Imputed SNPS
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
    public void doAnalysis(Map<String, List<Region>> regions) {
        System.out.println("In HaplotypeAnalyzer.doAnalysis...");
        //  The keys are chromsomes
        Set<String> keys = regions.keySet();

        //  for each chromsome
        for (String key : keys) {
            //  for each region on the chromosome
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
     */
    private Region getSnpsInRegion(String chromosome, OverlappingRegion region) {
        //  get two lists from region:
        //      High responding strains
        //      Low responding strains
        List highRespondingStrains = region.getHighRespondingStrains();
        List lowRespondingStrains = region.getLowRespondingStrains();
        
        //  Get look-up for this chromosome
        SNPFile snpLookup = this.cgdSNPLookup.get(chromosome);

        //  Find the array of candidate SNPs in the region.
        int[] snpSubSet = snpLookup.findSnpsPositionsInRange(region.getBuild(),
                region.getStart(), region.getEnd());

        // Get all the SNPs that meet the criteria and add them to the
        // region. (criteria defined in class header and the SNPFile class...)
        for (int snp_position:snpSubSet) {
            SNP snp = null;
            try {
                snp = snpLookup.analyzeSNP(snp_position, region.getBuild(),
                        highRespondingStrains, lowRespondingStrains);
                if (snp != null) {
                    System.out.println("Kept SNP " + snp.getSnpId() + " " + snp.getBuild36Position());
                }
            }
            catch (SNPDoesNotMeetCriteriaException e) {
                //  TODO:  Consider logging snps that fail criteria and why
                continue;
            }
            // If SNP kept add to our region
            if (snp != null)
                region.addSnp(snp);
        }

        return region;
    }


}
