/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jax.qtln.mgisearch;

import org.apache.solr.client.solrj.beans.Field;

/**
 *
 * @author dave
 */
public class MGIQTL {
    
    @Field
    String id;

    @Field("chr")
    String chromosome;

    @Field("cm")
    String centimorgans;
    
    @Field("sym")
    String symbol;

    @Field
    String name;

    @Field("mp")
    String[][] mpterms;

  }