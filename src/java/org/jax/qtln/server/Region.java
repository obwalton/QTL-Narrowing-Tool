/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;


/**
 *
 * @author dow
 */
public interface Region {

    public void setBuild(String build);

    public void setChromosome(String chromosome);

    public void setEnd(int end);

    public void setStart(int start);

    public String getBuild();

    public String getChromosome();

    public int getEnd();
 
    public int getStart();

}
