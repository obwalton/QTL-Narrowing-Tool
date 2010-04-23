/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.regions;

import com.google.gwt.user.client.rpc.IsSerializable;
import java.util.List;


/**
 *
 * @author dow
 */
public interface Region extends IsSerializable {

    public void setBuild(String build);

    public void setChromosome(String chromosome);

    public void setEnd(int end);

    public void setStart(int start);

    public String getBuild();

    public String getChromosome();

    public int getEnd();
 
    public int getStart();

    public void setSnps(List<SNP> snps);
    
    public List<SNP> getSnps();

}
