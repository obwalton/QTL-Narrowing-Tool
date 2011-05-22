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

package org.jax.qtln.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import org.jax.qtln.regions.SNP;
import org.jax.qtln.regions.SNPDoesNotMeetCriteriaException;
//import org.devbrat.util.WAHBitSet;


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
    /** The column where the Build 37 Position can be found */
    public static final int BUILD_37_COL = 3;
    /** The column where the SNP Source can be found */
    public static final int SOURCE_COL = 5;
    /** The column where the First Strain can be found */
    public static final int FIRST_STRAIN_COL = 6;

    private File physicalFile;
    private String chromosome;
    private String[] strain_array;
    //private Map<String, WAHBitSet> strainBaseCalls;
    private Map<String, BitSet> strainBaseCalls;
    private char[] baseCall1_array;
    private char[] baseCall0_array;
    private String[] snpIds_array;
    private String[] rsNums_array;
    private int[] b37Positions_array;
    //private int[] b36Positions_array;
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
        if (matcher.matches()) {
            this.chromosome = matcher.group(1);
        }

        this.physicalFile = new File(snpDir.getAbsolutePath() + 
                File.separator + snpFile.getName());
        //this.strainBaseCalls = new HashMap<String, WAHBitSet>();
        this.strainBaseCalls = new HashMap<String, BitSet>();
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
        //  This object is used as a staging area.  We read in the entire file,
        //  Drop all rows that have no Build 37 position, sort the keys on
        //  Build 37 position, and then iterate through the ordered rows,
        //  and build our SNPFile structure.
        TreeMap<Integer, String> staging = new TreeMap<Integer, String>();

        //  These are the objects that store our data during the load.
        //  Most will be converted to arrays of primitives for long term
        //  storage.
        ArrayList<String> strains = new ArrayList<String>();
        ArrayList<String> snpIds = new ArrayList<String>();
        ArrayList<String> rsNums = new ArrayList<String>();
        ArrayList<Integer> b37Positions = new ArrayList<Integer>();
        ArrayList<Short> sources = new ArrayList<Short>();
        ArrayList<Character> baseCall1 = new ArrayList<Character>();
        ArrayList<Character> baseCall0 = new ArrayList<Character>();


        FileReader fileReader = new FileReader(this.physicalFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        // loop through lines of the file.  Separating each line
        // into a row of the dataset object.
        String line = "";
        boolean firstLine = true;
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split(",");

            // For the first line we need to gather all the strain names
            if (firstLine) {
                firstLine = false;
                //  Get all strain names...
                //this.strainBaseCalls = new HashMap<String, WAHBitSet>();
                this.strainBaseCalls = new HashMap<String, BitSet>();
                for (int i =
                        SNPFile.FIRST_STRAIN_COL; i < cols.length; i++) {
                    //this.strainBaseCalls.put(cols[i], new WAHBitSet());
                    this.strainBaseCalls.put(cols[i], new BitSet());
                    strains.add(cols[i].trim());
                }
                continue;
            } 
            //  for all other lines we load the data into our staging area...
            else {
                //  First we must determine if there is a B37 Position
                String b37pos = cols[SNPFile.BUILD_37_COL];
                Integer b37posI;
                if (b37pos.equals("NA"))
                    //  We are skipping all rows without a B37 position
                    continue;
                else
                    b37posI = new Integer(b37pos);

                staging.put(b37posI, line);
            }
        }
        // Close the file readers, we're all done with them.
        bufferedReader.close();
        fileReader.close();


        // Now get the keys in ascending order (the NavigableKeySet from
        // a TreeMap is ordered)
        Set<Map.Entry<Integer,String>> positions = staging.entrySet();
        int row = 0;
        short next_map_value = (short) 0;
        for(Map.Entry<Integer, String> position: positions) {
            String[] cols = position.getValue().split(",");

            //snpIds.add(cols[SNPFile.SNP_ID_COL]);
            //rsNums.add(cols[SNPFile.RS_NUM_COL]);
            //b36Positions.add(new Integer(cols[SNPFile.BUILD_36_COL]));

            b37Positions.add(position.getKey());
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
                //  set it now.
                else if (!isZeroSet) {
                    baseCall0.add(cur_basecall);
                    isZeroSet = true;
                    this.strainBaseCalls.get(strains.get(i)).set(row, false);
                } else {
                    this.strainBaseCalls.get(strains.get(i)).set(row, false);
                }

            }

            //  if the two master lists of basecalls are not of
            //  equal length, that means there were no base calls
            //  for this SNP that were a miss match.  Add a null
            //  row to baseCall0
            if (baseCall0.size() != baseCall1.size()) {
                baseCall0.add(null);
            }

            ++row;
        }
        //  All done with our staging area
        staging = null;
        positions = null;

        // Now copy all temporary Collection based data structures into less
        // memory consuming arrays
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
        //this.b36Positions_array = new int[b36Positions.size()];
        //for (int i = 0; i < b36Positions.size(); i++)
        //    this.b36Positions_array[i] = b36Positions.get(i);
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
     * @param start  The starting position of the range.
     * @param end  The ending position of the range.
     * @return  An array containing all of the positions found in the range
     *      as "java.util.Integer" values.
     */
    public int[] findSnpPositionsInRange(int start, int end) {
        //  TODO: Consider throwing an exception if the "load()" method has not
        //  been called.

        //  The list of all SNP positions for this chromosome.  Will be
        //  populated with either b36 or b37, depending on user parameter
        int[] positionLookup = null;
        //if (build.toUpperCase().equals("36"))
        //    positionLookup = this.b36Positions_array;
        //  Because we only support 36 and 37, we'll assume anything not
        //  b36 is b37.
        //else
        positionLookup = this.b37Positions_array;

        System.out.println("for positions " + start + " to " + end);
        //  This will be the first position in the array of positions in the
        //  Lookup that maps to our start position.
        int start_index = -1;
        //  This will be the last position in the array of positions in the
        //  Lookup that maps to our end position.
        int end_index = -1;

        //  If start and end position exist in the position lookup, this
        //  saves us time from looping through
        start_index = Arrays.binarySearch(positionLookup, start);
        if (start_index < -1) start_index = -1;

        end_index = Arrays.binarySearch(positionLookup, end);
        if (end_index < -1) end_index = -1;

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
                        if (position == end) {
                            end_index = Arrays.binarySearch(positionLookup, position);
                        }
                        else {
                            end_index = Arrays.binarySearch(positionLookup, last_position);
                        }

                    }
                }
                last_position = position;
                if (start_index >= 0 && end_index >= 0) break;
            }
            if (end_index <= -1) {
                end_index = Arrays.binarySearch(positionLookup, last_position);
                System.out.println("No positions greater than end " + end + " Last position before end was: " + last_position + " index of " + end_index);
            }
        }
        // Now take the start and end indicies and get a subList of positions
        // to return.
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
     * @param highRespondingStrains  List of high responding strains.
     * @param lowRespondingStrains  List of low responding strains.
     * @return  SNP object is returned containing basecall value, position in
     *   build 37, all high responding strains from the complete
     *   set of strains, all low responding strains, rs number and source.
     * @throws SNPDoesNotMeetCriteriaException  If for any reason the SNP
     *   fails to pass the criteria this exception is thrown with an
     *   explaination message.
     */
    public SNP analyzeSNP(Integer snp_position, 
                        List<String> highRespondingStrains,
                        List<String> lowRespondingStrains)
        throws SNPDoesNotMeetCriteriaException
    {
        boolean debug = false;
        if (snp_position == 9274546)
            debug = true;

        if (debug) System.out.println("Analyzing SNP Position " + snp_position);
        //  The list of all SNP positions for this chromosome.  Will be
        //  populated with either b36 or b37, depending on user parameter
        int[] positionLookup = null;
        //if (build.toUpperCase().equals("36"))
        //    positionLookup = this.b36Positions_array;
        //  Because we only support 36 and 37, we'll assume anything not
        //  b36 is b37.
        //else
        positionLookup = this.b37Positions_array;

        int snp_index = Arrays.binarySearch(positionLookup, snp_position);
        if (snp_index < 0) {
            String msg = "Not in the list of SNPs in this region.";
            if (snp_position == -1)
                msg += " position is -1...";
            throw new SNPDoesNotMeetCriteriaException(msg);
        }
        //int snp_index = positionLookup.indexOf(snp_position);

        //  if zero basecall is 'N' throw exception, all strains cannot have
        //  same base value and meet criteria.  'N' is to indicate no second
        //  base was found.
        if (this.baseCall0_array[snp_index] == 'N') {
            String msg = "All base calls are the same.";
            if (debug) System.out.println(msg);
            throw new SNPDoesNotMeetCriteriaException(msg);
        }

        SNP snp = new SNP(snp_position);

        // Keep SNP if All high responding strains have same base value ...
        char hrCall = 'N';
        boolean high = false;
        boolean first = true;
        for (String strain : highRespondingStrains) {
            //  invalid strain throw exception
            if (Arrays.binarySearch(this.strain_array, strain) < 0) {
                String msg = "High responding strain " +
                        strain + " is not a valid strain!";
                if (debug) System.out.println(msg);
                throw new SNPDoesNotMeetCriteriaException(msg);
            }
            //WAHBitSet calls = strainBaseCalls.get(strain);
            BitSet calls = strainBaseCalls.get(strain);
            if (first) {
                high = calls.get(snp_index);
                if (high)
                    hrCall = this.baseCall1_array[snp_index];
                else
                    hrCall = this.baseCall0_array[snp_index];

                if (debug) System.out.println("Basecall for strain " + strain + " 'High' value " + high + " at index " + snp_index);
                first = false;
            } else {
                //  Mismatch between two high responding strains.
                //  throw an exception
                if (high != calls.get(snp_index)) {
                    String msg = "Nonmatching high responding strain base values.";
                    if (debug) System.out.println(msg);
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
            }
        }
        snp.setHighRespondingBaseValue(hrCall);

        // ... && All low responding strains have the same base value
        char lrCall = 'N';
        boolean low = false;
        first = true;
        for (String strain : lowRespondingStrains) {
            //  invalid strain throw exception
            if (Arrays.binarySearch(this.strain_array, strain) < 0) {
                    String msg = "Low responding strain " +
                            strain + " is not a valid strain!";
                if (debug) System.out.println(msg);
                throw new SNPDoesNotMeetCriteriaException(msg);
            }
            //WAHBitSet calls = strainBaseCalls.get(strain);
            BitSet calls = strainBaseCalls.get(strain);
            if (first) {
                low = calls.get(snp_index);
                if (low)
                    lrCall = this.baseCall1_array[snp_index];
                else
                    lrCall = this.baseCall0_array[snp_index];
                if (debug) System.out.println("Basecall for strain " + strain + " 'Low' value " + low + " at index " + snp_index);
                first = false;
            } else {
                //  Mismatch between two low responding strains. 
                //  throw an exception
                if (low != calls.get(snp_index)) {
                    String msg = "Nonmatching low responding strain base values.";
                    if (debug) System.out.println(msg);
                    throw new SNPDoesNotMeetCriteriaException(msg);
                }
            }
        }
        snp.setLowRespondingBaseValue(lrCall);
        
        
        //  ... && high responding base != low responding base
        if (high == low) {
            String msg = 
                    "High and low responding strain base calls are the same. ";
            if (debug) System.out.println(msg);
            if (debug) System.out.println(this.baseCall1_array[snp_index] + "/" + this.baseCall0_array[snp_index]);
            throw new SNPDoesNotMeetCriteriaException(msg);
        }

        // TODO:  Add all of these back in when I work out my memory problems
        //snp.setSnpId(this.snpIds_array[snp_index]);
        //snp.setRsNumber(this.rsNums_array[snp_index]);
        //snp.setSource(this.sourceMap.get(this.sources_array[snp_index]));

        return snp;
    }

    public String[] getStrains() {
        return this.strain_array;
    }
}
