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
public interface SNPFileIfc {
        

    List<String> getValidSamples(String tabGz) 
            throws FileNotFoundException, IOException;
        
    /**
     * Do the analysis of a SNP with respect to whether or not it meets our
     * criteria to be kept.
     *
     * The criteria requires that:
     *   <ul>
     *   <li>all of the "high responding strains" passed in, share the same base
     *       call value.</li>
     *   <li>all of the "low responding strains" passed in, share the same base
     *       call value.</li>
     *   <li>base call for high and low responding strains are different.</li>
     *   </ul>
     * @param s  The string of information representing the SNP position
     * @param hrstrains  List of high responding strains.
     * @param lrstrains  List of low responding strains.
     * @return  SNP object is returned containing basecall value, position in
     *   build 37, all high responding strains from the complete
     *   set of strains, all low responding strains, rs number and source.
     * @throws SNPDoesNotMeetCriteriaException  If for any reason the SNP
     *   fails to pass the criteria this exception is thrown with an
     *   explanation message.
     */
    public SNP analyzeSNP(String s, List<String> hrstrains, 
            List<String> lrstrains) 
            throws SNPDoesNotMeetCriteriaException;

    
    public List<String> getStrains();

}
