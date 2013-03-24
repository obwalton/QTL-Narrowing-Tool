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
public class NIEHSSNPFile extends TabixSearcher {
    
    public static final int STRAIN_START_COLUMN = 4;
    
    private List<String> strains;
    /**
     * 
     * @param tabGz UNC Tab formatted GZ file
     * 
     */
    public NIEHSSNPFile(String tabGz) {
        super(tabGz);
        try {
            // Determine what strains are available in this SNP GZ file
            this.strains = getValidSamples(tabGz);
                        
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public List<String> getValidSamples(String tabGz) 
            throws FileNotFoundException, IOException
    {
        System.out.println("In NIEHSSNPFile.getValidSamples()");
        File tabFile = new File(tabGz);
        System.out.println("Open gz file: " + tabGz);
        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(tabFile));
        BufferedReader br = new BufferedReader(new InputStreamReader(gis));
        System.out.println(br);
        List<String> validStrains = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            String[] tokens = line.split("\t");
            for (int i = NIEHSSNPFile.STRAIN_START_COLUMN; i < tokens.length; i++) {
                validStrains.add(tokens[i]);
            }
            break;
        }
        return validStrains;       
    }
    
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
            throws SNPDoesNotMeetCriteriaException
    {
        String[] tokens = s.split("\t");
        String pos = tokens[2].trim();
        //  init the ref and alt to blank
        char ref = ' ';
        char alt = ' ';
        
        String hr_call = "";
        boolean first = true;
        SNP snp = new SNP(Integer.parseInt(pos));
        for (String strain : hrstrains) {
            //  invalid strain throw exception
            int strain_idx = this.strains.indexOf(strain);
            if (strain_idx < 0) {
                  String msg = "High responding strain " +
                        strain + " is not a valid strain!";
                throw new SNPDoesNotMeetCriteriaException(msg);
            }
            strain_idx = (strain_idx) + NIEHSSNPFile.STRAIN_START_COLUMN;
            String value = tokens[strain_idx];
            
            if (first) {
                if (!value.toUpperCase().equals("N") && 
                        !value.toUpperCase().equals("H")) {
                    hr_call = value.toUpperCase();
                } 
                else {
                    String msg = "High responding strain " + strain + 
                            " has either an ambigous base call: " + value;
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
                first = false;
            } else {
                String tmp_call = "";
                if (!value.toUpperCase().equals("N") && 
                        !value.toUpperCase().equals("H")) {
                    tmp_call = value.toUpperCase();
                } 
                //  All high responding strains must have the same call
                if (!hr_call.equals(tmp_call)) {
                    String msg = "Nonmatching high responding strain base values.";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
            }
        }
        snp.setHighRespondingBaseValue(hr_call.charAt(0));
        
        String lr_call = "";
        first = true;
        for (String strain : lrstrains) {
            //  invalid strain throw exception
            int strain_idx = this.strains.indexOf(strain);
            if (strain_idx < 0) {
                  String msg = "Low responding strain " +
                        strain + " is not a valid strain!";
                throw new SNPDoesNotMeetCriteriaException(msg);
            }
            strain_idx = strain_idx + NIEHSSNPFile.STRAIN_START_COLUMN;
            String value = tokens[strain_idx];
            
            if (first) {
                if (!value.toUpperCase().equals("N") && 
                        !value.toUpperCase().equals("H")) {
                    lr_call = value.toUpperCase();
                } 
                else {
                    String msg = "Low responding strain " + strain + 
                            " has either an ambigous base call: " + value ;
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }

                if (lr_call.equals(hr_call)) {
                    String msg = "High and low responding strain base calls are the same. ";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
            } else {
                String tmp_call = "";
                if (!value.toUpperCase().equals("N") && 
                        !value.toUpperCase().equals("H")) {
                    tmp_call = value.toUpperCase();
                } 
                //  All low responding strains must have the same call
                if (!lr_call.equals(tmp_call)) {
                    String msg = "Nonmatching low responding strain base values.";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
            }
        }
        snp.setLowRespondingBaseValue(lr_call.charAt(0));

        return snp;   
    }
    
    public List<String> getStrains() {
        return this.strains;
    }
    
    public TabixReader.Iterator search(String region) {
        String[] tokens = region.split(":");
        // In NIEHS file Chromosomes less than 10 start with 0.
        if (tokens[0].length() == 1 && !tokens[0].equals("X")) {
            region = "0" + region;
        }
        return this.reader.query(region); 
    }
    
    public TabixReader.Iterator search(String chr, int start, int end) {
        System.out.println("Searching " + chr + ":" + start + "-" + end);
        //  TODO: Fix bug that causes an out of bounds error if start is 0,
        //  for now reset to 1
        if (start == 0)
            start = 1;
        // In NIEHS file Chromosomes less than 10 start with 0.
        if (chr.length() == 1 && !chr.equals("X")) {
            chr = "0" + chr;
        }
        return this.search(chr + ":" + start + "-" + end);
    }

}
