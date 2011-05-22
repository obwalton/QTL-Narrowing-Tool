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
