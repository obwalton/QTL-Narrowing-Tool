/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

import org.jax.qtln.regions.SNPDoesNotMeetCriteriaException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.BitSet;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.devbrat.util.WAHBitSet;
import org.jax.qtln.regions.SNP;


/** Class that stores a CGD Imputed SNP data for a single chromosome.
 *
 * The goal of this class is to store the CGD Imputed SNP data for a chromosome
 * in a format that conserves memory.  The class currently leverages a
 * list of java.util.BitSet objects for storing all the basecalls.
 *
 * @author dow
 */
public class SNPFile {
    //  This class assumes the format of the CGD Imputed SNP files to have
    //  the following columns.
    /** The column where the SNP ID can be found */
    public static final int SNP_ID_COL = 0;
    /** The column where the RS Number can be found */
    public static final int RS_NUM_COL = 1;
    /** The column where the Build 36 Position can be found */
    public static final int BUILD_36_COL = 4;
    /** The column where the Build 37 Position can be found */
    public static final int BUILD_37_COL = 3;
    /** The column where the SNP Source can be found */
    public static final int SOURCE_COL = 5;
    /** The column where the First Strain can be found */
    public static final int FIRST_STRAIN_COL = 6;

    private File physicalFile;
    private String chromosome;
    private String[] strain_array;
    private Map<String, WAHBitSet> strainBaseCalls;
    private char[] baseCall1_array;
    private char[] baseCall0_array;
    private String[] snpIds_array;
    private String[] rsNums_array;
    private int[] b37Positions_array;
    private int[] b36Positions_array;
    private Map<Short, String> sourceMap;
    private Map<String, Short> revSourceMap;
    private short[] sources_array;

    /** Constructor for the SNPFile class.
     *  Requires that the user provides both the SNPFile as a "java.io.File"
     * object, and that the user provide a regex pattern that represents the
     * file name format.  It is assumed that the first "Group" in the pattern
     * will be the chromosome the imputed SNP file represents.
     * @param snpFile  A CGD Imputed SNP file for a single chromosome.
     * @param nameFormat  A regex pattern representing a valid file name
     *      which must include the chromosome as the first "Group" to be
     *      harvested by a regex match.
     */
    public SNPFile(File snpDir, File snpFile, Pattern nameFormat) {
        String fileName = snpFile.getName();
        Matcher matcher = nameFormat.matcher(fileName);
        System.out.println("Check for Match for file " + fileName + " and pattern: " + nameFormat.pattern());
        if (matcher.matches()) {
            System.out.println("Matches!  Get Chromosome");
            this.chromosome = matcher.group(1);
        }

        this.physicalFile = new File(snpDir.getAbsolutePath() + 
                File.separator + snpFile.getName());
        this.strainBaseCalls = new HashMap<String, WAHBitSet>();
        this.sourceMap = new HashMap<Short, String>();
        this.revSourceMap = new HashMap<String, Short>();

    }

    /**  Confirms whether or not the SNPFile was a valid snp file based on the
     *   file name.  It does not confirm based on the content of the file.
     *
     * @return true if the file has a valid name (and the chromosome was
     *      therefore set).  False if the file name was invalid.
     */
    public boolean valid () {
        if (this.chromosome != null) {
            return true;
        } else {
            return false;
        }
    }

    /** Returns the current chromosome.
     *
     * @return a String representation of the chromosome value.  Will return
     *      null if the SNPFile was invalid and no chromosome value could be
     *      pulled from the file name.
     */
    public String getChromosome() {
        return this.chromosome;
    }

    /**
     * Returns a String representation of the size and state of all
     *   internal structures.  For diagnostic purposes only!
     * @return  String representation of all internal structures (summary)
     */
    public String getDetails () {
        String results = "";
        if (! this.valid())
            results = "Nothing to report, invalid SNPFile";
        else {
            results = "Summary contents for CGD Imputed SNP File: " +
                    physicalFile.getName() + "\n";
            results += "\tChromosome = " + getChromosome() + "\n";
            if (strain_array != null)
                results += "\tNumber of strains = " + strain_array.length + "\n";
            if (strainBaseCalls != null)
                results += "\tNumber of strains with base calls = " + strainBaseCalls.size() + "\n";
            if (baseCall1_array != null)
                results += "\tNumber of positions with base calls = " + baseCall1_array.length + "\n";

            if (this.snpIds_array != null)
                results += "\tNumber of snp ids found = " + snpIds_array.length + "\n";

            if (this.rsNums_array != null)
                results += "\tNumber of rs numbers found = " + rsNums_array.length + "\n";

            if (sourceMap != null)
                results += "\tNumber of sources found = " + sourceMap.size() + "\n";

        }
        return results;
    }

    /**  This method will actually load the SNP file for the given chromosome
     *   from disk and store it in memory.  We store the base calls as
     *   "java.util.BitSet" objects to save memory.
     *   Nothing is returned, the structure of the object is updated, and
     *   then the physical file is closed.
     */
    public void load()
        throws IOException
    {
        ArrayList<String> strains = new ArrayList<String>();
        ArrayList<String> snpIds = new ArrayList<String>();
        ArrayList<String> rsNums = new ArrayList<String>();
        ArrayList<Integer> b37Positions = new ArrayList<Integer>();
        ArrayList<Integer> b36Positions = new ArrayList<Integer>();
        ArrayList<Short> sources = new ArrayList<Short>();
        ArrayList<Character> baseCall1 = new ArrayList<Character>();
        ArrayList<Character> baseCall0 = new ArrayList<Character>();


        FileReader fileReader = new FileReader(this.physicalFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        String line = "";
        short next_map_value = (short) 0;
        // loop through lines of the file.  Separating each line
        // into a row of the dataset object.
        boolean firstLine = true;
        int row = 0;
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split(",");
            // For the first line we need to gather all the strain names
            if (firstLine) {
                firstLine = false;
                //  Get all strain names...
                this.strainBaseCalls = new HashMap<String, WAHBitSet>();
                for (int i =
                        SNPFile.FIRST_STRAIN_COL; i < cols.length; i++) {
                    this.strainBaseCalls.put(cols[i], new WAHBitSet());
                    strains.add(cols[i]);
                }
                continue;
            } //  for all other lines we process the data...
            else {
                //snpIds.add(cols[SNPFile.SNP_ID_COL]);
                //rsNums.add(cols[SNPFile.RS_NUM_COL]);
                b36Positions.add(new Integer(cols[SNPFile.BUILD_36_COL]));
                String b37pos = cols[SNPFile.BUILD_37_COL];
                Integer b37posI;
                if (b37pos.equals("NA"))
                    b37posI = null;
                else
                    b37posI = new Integer(b37pos);
                b37Positions.add(b37posI);
                String source = cols[SNPFile.SOURCE_COL];
                // We store the actual source values in a lookup, since
                // there are a limited number, that way we save more space
                if (!this.sourceMap.containsValue(source)) {
                    Short nmv = new Short(next_map_value);
                    this.sourceMap.put(nmv, source);
                    this.revSourceMap.put(source, nmv);
                    ++next_map_value;
                }
                //sources.add(this.revSourceMap.get(source));

                // Now we are going to create the BitSets that represent
                // the base calls

                // We will set the "One" value or first base call with the
                // first strain every time.  We need to keep track of
                // whether or not the "Zero" value is set, so we can assign
                // the second base call.  We assume for a given SNP there
                // are only 2 possible base call values.
                boolean isZeroSet = false;
                //  TODO: think how logic can change to carry through upper
                //  and lower case base calls to show did between normal
                //  and imputed values.
                char cur_primary_basecall =
                        cols[SNPFile.FIRST_STRAIN_COL].toUpperCase().trim().
                        charAt(0);
                baseCall1.add(cur_primary_basecall);
                // This sets the bit for the primary call to true.
                this.strainBaseCalls.get(strains.get(0)).set(row);
                // Cycle through the rest of the strain columns
                for (int i = 1; i < strains.size(); i++) {
                    char cur_basecall = cols[SNPFile.FIRST_STRAIN_COL + i].toUpperCase().trim().charAt(0);
                    // if this base is the same as the primary base, set
                    // the associated bit to true
                    if (cur_basecall == cur_primary_basecall) {
                        this.strainBaseCalls.get(strains.get(i)).set(row);
                    } //  otherwise, if the zero value has not yet been set
                    //  set it now.  No need to set a bit as any bit not
                    //  "true" is assumed to be false.
                    else if (!isZeroSet) {
                        baseCall0.add(cur_basecall);
                    }
                }

                //  if the two master lists of basecalls are not of
                //  equal length, that means there were no base calls
                //  for this SNP that were a miss match.  Add a null
                //  row to baseCall0
                if (baseCall0.size() != baseCall1.size()) {
                    baseCall0.add(null);
                }

            }
        }
        bufferedReader.close();
        fileReader.close();

        this.strain_array = strains.toArray(new String[0]);
        //this.snpIds_array = snpIds.toArray(new String[0]);
        //this.rsNums_array = rsNums.toArray(new String[0]);
        this.b37Positions_array = new int[b37Positions.size()];
        for (int i = 0; i < b37Positions.size(); i++) {
            if (b37Positions.get(i) == null)
                this.b37Positions_array[i] = -1;
            else
                this.b37Positions_array[i] = b37Positions.get(i);
        }
        this.b36Positions_array = new int[b36Positions.size()];
        for (int i = 0; i < b36Positions.size(); i++)
            this.b36Positions_array[i] = b36Positions.get(i);
        //this.sources_array = new short[sources.size()];
        //for (int i = 0; i < sources.size(); i++)
        //    this.sources_array[i] = sources.get(i);
        this.baseCall1_array = new char[baseCall1.size()];
        for (int i = 0; i < baseCall1.size(); i++)
            this.baseCall1_array[i] = baseCall1.get(i);
        this.baseCall0_array = new char[baseCall0.size()];
        for (int i = 0; i < baseCall0.size(); i++) {
            if (baseCall0.get(i) == null)
                this.baseCall0_array[i] = 'N';
            else
                this.baseCall0_array[i] = baseCall0.get(i);
        }

    }

    /** Get all the SNP position values in the given range for the given build
     *
     * @param build The build number for the position values, currently support
     *      b36 and b37
     * @param start  The starting position of the range.
     * @param end  The ending position of the range.
     * @return  An array containing all of the positions found in the range
     *      as "java.util.Integer" values.
     */
    public int[] findSnpsPositionsInRange(String build, int start, int end) {
        //  TODO: Consider throwing an exception if the "load()" method has not
        //  been called.

        //  The list of all SNP positions for this chromosome.  Will be
        //  populated with either b36 or b37, depending on user parameter
        int[] positionLookup = null;
        if (build.toUpperCase().equals("36"))
            positionLookup = this.b36Positions_array;
        //  Because we only support 36 and 37, we'll assume anything not
        //  b36 is b37.
        else
            positionLookup = this.b37Positions_array;

        //  This will be the first position in the array of positions in the
        //  Lookup that maps to our start position.
        int start_index = -1;
        //  This will be the last position in the array of positions in the
        //  Lookup that maps to our end position.
        int end_index = -1;
        //  Convert start and end to "java.util.Integer" values
        //Integer startI = new Integer(start);
        //Integer endI = new Integer(end);
        // first lets see if we can find the start and end positions exactly
        // and avoid looping below.
        //if (positionLookup.contains(startI)) {
        //    start_index = positionLookup.indexOf(startI);
        //}
        //if (positionLookup.contains(endI)) {
        //    end_index = positionLookup.indexOf(endI);
        //}
        start_index = Arrays.binarySearch(positionLookup, start);

        end_index = Arrays.binarySearch(positionLookup, end);

        //  If we haven't found both start and end index yet, then we'll
        //  loop through the positionLookup to find them.
        if (start_index < 0 || end_index < 0) {
            int last_position = 0;
            for (int position:positionLookup) {
                if (start_index < 0) {
                    if (position >= start) {
                        start_index = Arrays.binarySearch(positionLookup, position);
                    }
                }

                if (end_index < 0) {
                    if (position >= end) {
                        if (position == end)
                            end_index = Arrays.binarySearch(positionLookup, position);
                        else
                            end_index = Arrays.binarySearch(positionLookup, last_position);

                    }
                }
                last_position = position;
                if (start_index >= 0 && end_index >= 0) break;
            }
        }
        // Now take the start and end indicies and get a subList of positions
        // to return.
        System.out.println("for positions " + start + " to " + end);
        System.out.println("selecting snps from " + start_index + " to " + end_index);

        int[] positions = new int[0];
        //  If end_index is less than start_index, that means we didn't find
        //  any snps.
        if (end_index >= start_index) {
            positions = new int[end_index - start_index + 1];
            System.arraycopy(positionLookup, start_index, positions, 0,
                    end_index - start_index + 1);
        }
        //       positionLookup.subList(start_index, end_index);
        //Integer[] l = new Integer[0];
        //l = positions.toArray(l);
        return positions;
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
     * @param snp_position  The position of the SNP on the chromosome
     * @param build The build (B36 or B37) of the position.
     * @param highRespondingStrains  List of high responding strains.
     * @param lowRespondingStrains  List of low responding strains.
     * @return  SNP object is returned containing basecall value, position in
     *   both build 36 and 37, all high responding strains from the complete
     *   set of strains, all low responding strains, rs number and source.
     * @throws SNPDoesNotMeetCriteriaException  If for any reason the SNP
     *   fails to pass the criteria this exception is thrown with an
     *   explaination message.
     */
    public SNP analyzeSNP(Integer snp_position, String build,
                        List<String> highRespondingStrains,
                        List<String> lowRespondingStrains)
        throws SNPDoesNotMeetCriteriaException
    {
        //  The list of all SNP positions for this chromosome.  Will be
        //  populated with either b36 or b37, depending on user parameter
        int[] positionLookup = null;
        if (build.toUpperCase().equals("36"))
            positionLookup = this.b36Positions_array;
        //  Because we only support 36 and 37, we'll assume anything not
        //  b36 is b37.
        else
            positionLookup = this.b37Positions_array;

        int snp_index = Arrays.binarySearch(positionLookup, snp_position);
        if (snp_index < 0)
            throw new SNPDoesNotMeetCriteriaException(snp_position.toString() +
                    " is not in the list of SNPs in this region.");

        //int snp_index = positionLookup.indexOf(snp_position);

        //  if zero basecall is null throw exception, all strains cannot have
        //  same base value and meet criteria
        if (this.baseCall0_array[snp_index] < 0)
            throw new SNPDoesNotMeetCriteriaException("For " +
                    snp_position.toString() + " all base calls are the " +
                    "same.");

        SNP snp = new SNP(snp_position, build);
        // Keep SNP if All high responding strains have same base value ...
        boolean high = false;
        boolean first = true;
        for (String strain : highRespondingStrains) {
            //  invalid strain throw exception
            if (Arrays.binarySearch(this.strain_array, strain) < 0)
                throw new SNPDoesNotMeetCriteriaException("For " +
                        snp_position.toString() + " high responding strain " +
                        strain + " is not a valid strain!");
            WAHBitSet calls = strainBaseCalls.get(strain);
            if (first) {
                high = calls.get(snp_index);
                first = false;
            } else {
                //  Mismatch between two high responding strains.
                //  throw an exception
                if (high != calls.get(snp_index))
                    throw new SNPDoesNotMeetCriteriaException("For " +
                        snp_position.toString() + " nonmatching high " +
                        " responding strain base values.");
            }
        }

        // ... && All low responding strains have the same base value
        boolean low = false;
        first = true;
        for (String strain : lowRespondingStrains) {
            //  invalid strain throw exception
            if (Arrays.binarySearch(this.strain_array, strain) < 0)
                throw new SNPDoesNotMeetCriteriaException("For " +
                        snp_position.toString() + " low responding strain " +
                        strain + " is not a valid strain!");
            WAHBitSet calls = strainBaseCalls.get(strain);
            if (first) {
                low = calls.get(snp_index);
                first = false;
            } else {
                //  Mismatch between two low responding strains. 
                //  throw an exception
                if (low != calls.get(snp_index)) 
                    throw new SNPDoesNotMeetCriteriaException("For " +
                        snp_position.toString() + " nonmatching low " +
                        " responding strain base values.");
            }
        }
        
        
        //  ... && high responding base != low responding base
        if (high == low)
            throw new SNPDoesNotMeetCriteriaException("For " +
                        snp_position.toString() + " high and low" +
                        "responding strain base calls are the same.");

        // This is a keeper, start populating the SNP to be returned
        if (build.toUpperCase().equals("36"))
            snp.setBuild37Position(this.b37Positions_array[snp_index]);
        else
            snp.setBuild36Position(this.b36Positions_array[snp_index]);
        snp.setSnpId(this.snpIds_array[snp_index]);
        snp.setRsNumber(this.rsNums_array[snp_index]);
        snp.setSource(this.sourceMap.get(this.sources_array[snp_index]));


        //  collect all other strains with HR base
        //  collect all other strains with LR base
        boolean hi_call_set = false;
        boolean lo_call_set = false;

        //  For each strain, add it to either the high or low responding set
        for (String strain:this.strain_array) {
            int index = Arrays.binarySearch(this.strain_array, strain);
            // We've stored all base values in the bitset as a true or a false,
            // essentially a 1 or a 0.  We've then stored the actual base call
            // values as chars, as for a snp there can only be two values from
            // a, c, t, g
            boolean base = this.strainBaseCalls.get(strain).get(index);
            char call;
            if (base)
                call = this.baseCall1_array[index];
            else
                call = this.baseCall0_array[index];

            //  If the base (t or f) is the same as high (t or f), add
            //  to the high responding strains.  Otherwise add to the
            //  low responding group.  Set the base call if it hasn't been
            //  set yet.
            if (high == base) {
                snp.addHighRepsondingStrain(strain);
                if (! hi_call_set) {
                    snp.setHighRespondingBaseValue(call);
                    hi_call_set = true;
                }
            } else {
                snp.addLowRespondingStrain(strain);
                if (! lo_call_set) {
                    snp.setLowRespondingBaseValue(call);
                    lo_call_set = true;
                }
            }
        }

        return snp;
    }
}