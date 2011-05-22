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
import java.util.Map;
import java.util.TreeMap;


/**
 *
 * @author dow
 */
public interface Region extends Serializable {

    public void setChromosome(String chromosome);

    public void setEnd(int end);

    public void setStart(int start);

    public String getChromosome();

    public int getEnd();
 
    public int getStart();

    //TODO:  These three are not common to QTL and overlapping region...remove?
    public void setSnps(TreeMap<Integer,SNP> snps);
    
    public TreeMap<Integer,SNP> getSnps();

    public Map<Integer, Gene> getGenes();

    public void setTotalNumSNPsInRegion(int numsnps);

    public int getTotalNumSNPsInRegion();

}
