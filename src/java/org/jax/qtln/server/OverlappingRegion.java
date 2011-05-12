/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

/**
 *
 * @author dow
 */
public class OverlappingRegion implements Region {
    private QTLSet qtls;
    private String chromosome;
    private int start;
    private int end;
    private String build;

    public OverlappingRegion (QTL qtl) {
        this(qtl.getChromosome(), qtl.getBuild());
        try {
            this.qtls.addQTL(qtl);
        } catch (InvalidChromosomeException ice) {
            //  We can do nothing as we just set the qtlSet with the 
            //  appropriate chromosome value
        }

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
