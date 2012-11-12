/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jax.qtln.snpsets;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.jax.qtln.regions.SNP;
import org.jax.qtln.regions.SNPDoesNotMeetCriteriaException;

/**
 *
 * @author dow
 */
public abstract class TabixSearcher implements SNPFileIfc {
    
    TabixReader reader;
    /**
     * 
     * @param vcf
     * 
     */
    public TabixSearcher(String tabGz){
        
        try {
            this.reader = new TabixReader(tabGz);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TabixReader.Iterator search(String region) {
        return this.reader.query(region); 
    }
    
    public TabixReader.Iterator search(String chr, int start, int end) {
        System.out.println("Searching " + chr + ":" + start + "-" + end);
        //  TODO: Fix bug that causes an out of bounds error if start is 0,
        //  for now reset to 1
        if (start == 0)
            start = 1;
        return this.search(chr + ":" + start + "-" + end);
    }

}
