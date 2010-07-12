/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import java.io.Serializable;
import java.util.ArrayList;
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

    public void setGenes(HashMap<Integer,Gene> genes) {
        this.genes = genes;
    }

    public void addGene(Integer cgd_gene_id, String mgi_accession_id) {
        if (this.genes == null) {
            this.genes = new HashMap<Integer,Gene>();
        }
        Gene gene = new Gene(cgd_gene_id, mgi_accession_id);
        this.genes.put(cgd_gene_id, gene);
    }

    public void addGene(int cgd_gene_id, String mgi_accession_id) {
        addGene(new Integer(cgd_gene_id), mgi_accession_id);
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

    public void addSnpDetails(List<List> details) {
        Integer gene_id;
        String mgi_id;
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
            mgi_id  = (String)row.get(4);
            snp.setCgdAssociatedGeneId(gene_id);
            addGene(gene_id, mgi_id);
        }
    }
}
