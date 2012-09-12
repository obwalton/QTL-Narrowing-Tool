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
public class SangerSNPFile {
    
    public static final int STRAIN_START_COLUMN = 9;
    
    private List<String> strains;
    private TabixReader reader;
    /**
     * 
     * @param vcf
     * 
     */
    public SangerSNPFile(String vcfGz) {
        
        try {
            // Determine what strains are available in this SNP GZ file
            this.strains = getValidSamples(vcfGz);
                        
            this.reader = new TabixReader(vcfGz);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    private List<String> getValidSamples(String vcfGz) 
            throws FileNotFoundException, IOException
    {
        System.out.println("In SanterSNPFile.getValidSamples()");
        File vcfFile = new File(vcfGz);
        System.out.println("Open gz file: " + vcfGz);
        GZIPInputStream gis = new GZIPInputStream(new FileInputStream(vcfFile));
        BufferedReader br = new BufferedReader(new InputStreamReader(gis));
        System.out.println(br);
        List<String> validStrains = new ArrayList<String>();
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            String[] tokens = line.split("\t");
            String firstCol = tokens[0];
            if (firstCol.startsWith("##"))
                continue;
            else if (firstCol.contains("#")) {
                for (int i = 9; i < tokens.length; i++) {
                    validStrains.add(tokens[i]);
                }
                break;
            }
        }
        return validStrains;       
    }
    
    public boolean snpInStrains(String s, Map<String, Integer> strains) {
        String[] tokens = s.split("\t");
        // TODO: If any of the strains start with 1/1, 1/0 or 0/1 there
        // is a snp
        Set<String> keys = strains.keySet();
        for (String key: keys) {
            int position = strains.get(key);
            String value = tokens[position];
            if (value.startsWith("1/1") || value.startsWith("0/1") || 
                    value.startsWith("1/0")) {
                return true;
            }
        }
        
        return false;
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
     *   explaination message.
     */
    public SNP analyzeSNP(String s, List<String> hrstrains, 
            List<String> lrstrains) 
            throws SNPDoesNotMeetCriteriaException
    {
        String[] tokens = s.split("\t");
        String pos = tokens[1];
        String id  = tokens[2];
        char ref = tokens[3].charAt(0);
        char alt = tokens[4].charAt(0);
        // TODO: If any of the strains start with 1/1, 1/0 or 0/1 there
        // is a snp
        String hr_call = "";
        boolean first = true;
        SNP snp = new SNP(Integer.parseInt(tokens[1]));
        for (String strain : hrstrains) {
            //  invalid strain throw exception
            int strain_idx = this.strains.indexOf(strain);
            if (strain_idx < 0) {
                  String msg = "High responding strain " +
                        strain + " is not a valid strain!";
                throw new SNPDoesNotMeetCriteriaException(msg);
            }
            strain_idx = strain_idx + SangerSNPFile.STRAIN_START_COLUMN;
            String value = tokens[strain_idx];
            
            if (first) {
                if (value.startsWith("1/1") || value.startsWith("0/1") || 
                    value.startsWith("1/0")) {
                    hr_call = "1";
                } else {
                    hr_call = "0";
                }
                first = false;
            } else {
                if ((hr_call.equals("0")  && (value.startsWith("1/1") || 
                        value.startsWith("0/1") || 
                    value.startsWith("1/0"))) || (hr_call.equals("1") && 
                        value.startsWith("0/0"))) {
                    String msg = "Nonmatching high responding strain base values.";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                   
                }
            }
        }
        if (hr_call.equals("1"))
            snp.setHighRespondingBaseValue(alt);
        else
            snp.setHighRespondingBaseValue(ref);
        
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
            strain_idx = strain_idx + SangerSNPFile.STRAIN_START_COLUMN;
            String value = tokens[strain_idx];
            
            if (first) {
                if (value.startsWith("1/1") || value.startsWith("0/1") || 
                    value.startsWith("1/0")) {
                    lr_call = "1";
                } else {
                    lr_call = "0";
                }
                if (lr_call.equals(hr_call)) {
                    String msg = "High and low responding strain base calls are the same. ";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
            } else {
                if ((lr_call.equals("0")  && (value.startsWith("1/1") || 
                        value.startsWith("0/1") || 
                    value.startsWith("1/0"))) || (lr_call.equals("1") && 
                        value.startsWith("0/0"))) {
                    String msg = "Nonmatching low responding strain base values.";
                    throw new SNPDoesNotMeetCriteriaException(msg);
                   
                }
            }
        }
        if (lr_call.equals("1"))
            snp.setLowRespondingBaseValue(alt);
        else
            snp.setLowRespondingBaseValue(ref);

        return snp;   
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
     
    public List<String> getStrains() {
        return this.strains;
    }

}
