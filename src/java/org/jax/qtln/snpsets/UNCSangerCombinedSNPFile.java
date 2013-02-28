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
public class UNCSangerCombinedSNPFile extends TabixSearcher {
    
    public static final int STRAIN_START_COLUMN = 5;
    
    private List<String> strains;
    /**
     * 
     * @param tabGz CGD UNC/Sanger Tab formatted GZ file
     * 
     */
    public UNCSangerCombinedSNPFile(String tabGz) {
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
        System.out.println("In UNCSangerCombined.getValidSamples()");
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
            // Once we start parsing strains every other column is a strain,
            // The others are a confidence.  For now not keeping the conf val
            boolean strain = true;
            for (int i = STRAIN_START_COLUMN; i < tokens.length; i++) {
                // Because we've begun at 5, and every other is a strain,
                // followed by a confidence, we should skip all that are not
                // divisible by 2
                if (strain) {
                    validStrains.add(tokens[i]);
                    strain = false;
                } else {
                    strain = true;
                }
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
            //  Index needs to be times 2 because every other is a confidence score
            strain_idx = (strain_idx*2) + UNCSangerCombinedSNPFile.STRAIN_START_COLUMN;
            String value = tokens[strain_idx];
            String confidence = tokens[strain_idx + 1];
            if (value.equals("0") || value.equals("1") || value.equals("2")) {
                System.out.println("BUG!!! " + tokens[0] + ", " + tokens[1] + ", " + tokens[2] + ", " + tokens[3] + ", " + tokens[4]);
            }
            if (first) {
                if ((confidence.equals("1") || confidence.equals("2")) && 
                     !value.equals("N") && !value.equals("H")) {
                    hr_call = value;
                } 
                else {
                    String msg = "High responding strain " + strain + 
                            " has either an ambigous base call: " + value + 
                            " or a low confidence score: '" + confidence + "'";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
                first = false;
            } else {
                String tmp_call = "";
                if ((confidence.equals("1") || confidence.equals("2")) && 
                     !value.equals("N") && !value.equals("H")) {
                    tmp_call = value;
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
            strain_idx = (strain_idx *2) + UNCSangerCombinedSNPFile.STRAIN_START_COLUMN;
            String value = tokens[strain_idx];
            String confidence = tokens[strain_idx + 1];
            
            if (first) {
                if ((confidence.equals("1") || confidence.equals("2")) && 
                     !value.equals("N") && !value.equals("H")) {
                    lr_call = value;
                } 
                else {
                    String msg = "Low responding strain " + strain + 
                            " has either an ambigous base call: " + value + 
                            " or a low confidence score: '" + confidence + "'";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }

                if (lr_call.equals(hr_call)) {
                    String msg = "High and low responding strain base calls are the same. ";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
            } else {
                String tmp_call = "";
                if ((confidence.equals("1") || confidence.equals("2")) && 
                     !value.equals("N") && !value.equals("H")) {
                    tmp_call = value;
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
        java.util.Collections.sort(this.strains);
        return this.strains;
    }
}
