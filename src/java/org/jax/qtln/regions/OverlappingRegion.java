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

package org.jax.qtln.regions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author dow
 */
public class OverlappingRegion implements Region, Serializable {
    private static final long serialVersionUID =
            4741175800356479306L;

    private QTLSet qtls;
    private String chromosome;
    private int start;
    private int end;
    private ArrayList<String> highRespondingStrains;
    private ArrayList<String> lowRespondingStrains;
    private TreeMap<Integer, SNP> snps;
    private HashMap<Integer, Gene> genes;
    private int totalSnps = 0;

    public OverlappingRegion () {

    }

    public OverlappingRegion (QTL qtl) {
        this(qtl.getChromosome());
        this.addQtl(qtl);
        this.setStart(qtl.getStart());
        this.setEnd(qtl.getEnd());
    }
    
    public OverlappingRegion (String chr) {
        this.setChromosome(chr);
        this.qtls = new QTLSet(chr);
    }
    
    public String getChromosome() {
        return chromosome;
    }

    public int getEnd() {
        return end;
    }

    public QTLSet getQtls() {
        return qtls;
    }

    public void addQtl(QTL qtl) {
        try {
            this.qtls.addQTL(qtl);
            this.addHighRespondingStrain(qtl.getHighResponder());
            this.addLowRespondingStrain(qtl.getLowResponder());
        } catch (InvalidChromosomeException ice) {
            //  Should we throw this out???
        }
    }

    public int getStart() {
        return start;
    }

    public void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public ArrayList<String> getHighRespondingStrains() {
        return highRespondingStrains;
    }

    public void setHighRespondingStrains(ArrayList<String> highRespondingStrains) {
        this.highRespondingStrains = highRespondingStrains;
    }

    public void addHighRespondingStrain(String strain) {
        if (this.highRespondingStrains == null) {
            this.highRespondingStrains = new ArrayList<String>();
        }
        this.highRespondingStrains.add(strain);
    }

    public ArrayList<String> getLowRespondingStrains() {
        return lowRespondingStrains;
    }

    public void setLowRespondingStrains(ArrayList<String> lowRespondingStrains) {
        this.lowRespondingStrains = lowRespondingStrains;
    }

    public void addLowRespondingStrain(String strain) {
        if (this.lowRespondingStrains == null) {
            this.lowRespondingStrains = new ArrayList<String>();
        }
        this.lowRespondingStrains.add(strain);
    }

    public void setTotalNumSNPsInRegion(int numsnps) {
        this.totalSnps = numsnps;
    }

    public int getTotalNumSNPsInRegion() {
        return this.totalSnps;
    }


    public TreeMap<Integer, SNP> getSnps() {
        return snps;
    }

    public void setSnps(TreeMap<Integer,SNP> snps) {
        this.snps = snps;
    }

    public void addSnp(SNP snp) {
        if (this.snps == null) {
            this.snps = new TreeMap<Integer,SNP>();
        }
        this.snps.put(snp.getBPPosition(), snp);
    }

    public Map<Integer, Gene> getGenes() {
        return genes;
    }

    public Gene getGene(Integer cgd_gene_id) {
        return genes.get(cgd_gene_id);
    }

    public Gene getGene(int cgd_gene_id) {
        return getGene(new Integer(cgd_gene_id));
    }

    public void setGenes(HashMap<Integer,Gene> genes) {
        this.genes = genes;
    }

    public void addGene(Integer cgd_gene_id, String mgi_accession_id,
            String symbol, String name) {
        if (this.genes == null) {
            this.genes = new HashMap<Integer,Gene>();
        }
        if (! this.genes.containsKey(cgd_gene_id)) {
            Gene gene = new Gene(cgd_gene_id, mgi_accession_id);
            if (! "".equals(symbol))
                gene.setSymbol(symbol);
            if (! "".equals(name))
                gene.setName(name);
            this.genes.put(cgd_gene_id, gene);
        }
    }

    public void addGene(int cgd_gene_id, String mgi_accession_id, String symbol,
            String name) {
        addGene(new Integer(cgd_gene_id), mgi_accession_id, symbol, name);
    }

    public Region getOverlappingRegion(OverlappingRegion other)
        throws NoOverlapException
    {
        if (! this.getChromosome().equals(other.getChromosome())) {
            //  These cannot overlap as they are on different Chromosomes
            throw new NoOverlapException("Regions do not overlap. " +
                    "On different chromosomes.");
        }
        OverlappingRegion overlap = null;
        if (this.getStart() <= other.getStart() &&
                other.getStart() <= this.getEnd()) {
            overlap = new OverlappingRegion(this.getChromosome());
            overlap.setStart(other.getStart());
            overlap.setChromosome(this.getChromosome());
            QTLSet qtls = overlap.getQtls();
            try {
                qtls.addQTL((this.getQtls()).asList());
                qtls.addQTL((other.getQtls()).asList());
            }
            catch (InvalidChromosomeException ice) {
                //  Can be ignored, as we've already confirmed the two
                //  regions are on the same chromosome
            }
            if(this.getEnd() <= other.getEnd())
                overlap.setEnd(this.getEnd());
            else
                overlap.setEnd(other.getEnd());
        }
        else if (this.getStart() >= other.getStart() &&
                this.getStart() <= other.getEnd()) {
            overlap = new OverlappingRegion(this.getChromosome());
            overlap.setStart(this.getStart());
            overlap.setChromosome(this.getChromosome());
            QTLSet qtls = overlap.getQtls();
            try {
                qtls.addQTL((this.getQtls()).asList());
                qtls.addQTL((other.getQtls()).asList());
            }
            catch (InvalidChromosomeException ice) {
                //  Can be ignored, as we've already confirmed the two
                //  regions are on the same chromosome
            }
            if(other.getEnd() <= this.getEnd())
                overlap.setEnd(other.getEnd());
            else
                overlap.setEnd(this.getEnd());
        }
        else
            throw new NoOverlapException("Regions do not overlap");
        return overlap;
    }

    /**
     * This is both the location where SNP Details are loaded to the region
     * and it's SNPs, but also where the Gene objects are added to the
     * region, and where the snps within a gene are associated with that Gene.
     * @param details  This is a list of lists which includes columns for:
     *    <UL>
     *    <LI>CGD SNP Id</LI>
     *    <LI>???</LI>
     *    <LI>SNP Annotation</LI>
     *    <LI>CGD Gene ID</LI>
     *    <LI>MGI Accession ID</LI>
     *    </UL>
     *
     */
    public void addSnpDetails(List<List> details) {
        Integer gene_id;
        Integer gene_start;
        Integer gene_end;
        String mgi_id;
        String symbol;
        String name;
        // Order of presidence of IDs to show:
        // Sanger, Imputed Wang, NIEHS, Diversity array, Celera, Broad, GNF, Wild-Derived
        String[] otherIdOrder = {"15","21", "1", "17", "13", "2", "6", "19"};
        for (List row:details) {
            Integer position = (Integer)row.get(1);
            SNP snp = this.snps.get(position);
            if (snp == null) {
                System.err.println("Snp position " + position + " was missing " +
                        "from our region!");
                continue;
            }
            snp.setCgdSnpId((Integer)row.get(0));

            snp.addSnpAnnotation((Integer)row.get(2));
            gene_id = (Integer)row.get(3);
            gene_start = (Integer)row.get(4);
            gene_end = (Integer)row.get(5);
            mgi_id  = (String)row.get(6);
            symbol  = (String)row.get(7);
            name  = (String)row.get(8);
            snp.setRsNumber((String)row.get(9));

            String otherId = (String)row.get(10);
            String otherSrc = (String)row.get(11);
            String curId = snp.getSnpId();
            String curSrc = snp.getSource();
            if (curId == null) {
                snp.setSnpId(otherId);
                snp.setSource(otherSrc);
            }
            else {
                //  There is an order to which we want to see Other Ids
                int curIdx = Arrays.binarySearch(otherIdOrder, curId);
                int newIdx = Arrays.binarySearch(otherIdOrder, otherId);
                //  If the new ID has a lower index than the current, but is
                //  not less than 0 (meaning not found), we use it.
                if (curIdx > newIdx && newIdx > -1) {
                    snp.setSnpId(otherId);
                    snp.setSource(otherSrc);
                }
            }
            snp.setCgdAssociatedGeneId(gene_id);
            addGene(gene_id, mgi_id, symbol, name);
            Gene gene = getGene(gene_id);
            gene.setStart(gene_start);
            gene.setEnd(gene_end);
            gene.addAssociatedSnp(snp);
        }
    }
}
