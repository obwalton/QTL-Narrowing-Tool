/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.jax.qtln.server.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dow
 */
public class OverlappingRegion implements Region, IsSerializable {
    private static final long serialVersionUID =
            4741175800356479306L;

    private QTLSet qtls;
    private String chromosome;
    private int start;
    private int end;
    private String build;
    private ArrayList<String> highRespondingStrains;
    private ArrayList<String> lowRespondingStrains;
    private ArrayList<SNP> snps;

    public OverlappingRegion () {

    }

    public OverlappingRegion (QTL qtl) {
        this(qtl.getChromosome(), qtl.getBuild());
        this.addQtl(qtl);
        this.setStart(qtl.getStart());
        this.setEnd(qtl.getEnd());
    }
    
    public OverlappingRegion (String chr, String build) {
        this.setChromosome(chr);
        this.setBuild(build);
        this.qtls = new QTLSet(chr);
    }
    
    public String getBuild() {
        return build;
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

    public void setBuild(String build) {
        this.build = build;
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

    public List<SNP> getSnps() {
        return snps;
    }

    public void setSnps(List<SNP> snps) {
        this.snps = (ArrayList)snps;
    }

    public void addSnp(SNP snp) {
        if (this.snps == null) {
            this.snps = new ArrayList<SNP>();
        }
        this.snps.add(snp);
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
            overlap = new OverlappingRegion(this.getChromosome(),
                    this.getBuild());
            overlap.setStart(other.getStart());
            overlap.setBuild(this.getBuild());
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
            overlap = new OverlappingRegion(this.getChromosome(),
                    this.getBuild());
            overlap.setStart(this.getStart());
            overlap.setBuild(this.getBuild());
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

}
