/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.stat.descriptive.moment.Mean;
import org.apache.commons.math.stat.inference.TTest;
import org.apache.commons.math.stat.inference.TTestImpl;
import org.jax.qtln.regions.Gene;
import org.jax.qtln.regions.OverlappingRegion;


/**
 *
 * @author dow
 */
public class ExpressionAnalyzer {

    // MGIAccession ID -> Probe ID
    private Map<String, List<String>> probeSetLookup;
    // MGIAccession ID -> {"symbol"->symbol,"name"->name}
    private Map<String, Map<String,String>> mgiLookup;
    // "samples", "probes", "intensities"
    // samples -> List of sample names
    // probes  -> List of probe ids
    // intensities -> matrix rows/columns = probes/samples
    private Map lungIntensityLookup;
    private List<String> samples;
    private List<String> probes;
    private double[][] intensityMatrix;
    // Strain -> [Sample names...]
    private Map<String, List<String>> lungStrainLookup;

    /**
     * This constructor is used for the case where the supporting lookups are
     * not yet generated.
     */
    public ExpressionAnalyzer () {

    }

    public ExpressionAnalyzer (Map<String, List<String>> probeSetLookup,
                Map<String,Map<String,String>> mgiLookup,
                Map lungIntensityLookup,
                Map<String, List<String>> lungStrainLookup) {
        this.probeSetLookup = probeSetLookup;
        this.mgiLookup = mgiLookup;
        this.lungIntensityLookup = lungIntensityLookup;
        this.probes = (List<String>)this.lungIntensityLookup.get("probes");
        this.samples = (List<String>)this.lungIntensityLookup.get("samples");
        this.intensityMatrix =
                (double[][])this.lungIntensityLookup.get("intensities");
        this.lungStrainLookup = lungStrainLookup;
    }

    public Map parseRMA(String rmaFileName, ServletContext sc)
            throws IOException, FileNotFoundException
    {
        HashMap lungIntensities = new HashMap();
        File rmaFile = new File(rmaFileName);
        if (!rmaFile.exists()) {
            throw new FileNotFoundException("Cannot find Lung RMA file.  " +
                    "File does not exist: " + rmaFile);
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        List<String> samples = new ArrayList<String>();
        List<String> probes = new ArrayList<String>();
        double[][] matrix = new double[0][0];
        try {
            fileReader = new FileReader(rmaFile);
            bufferedReader = new BufferedReader(fileReader);

            // loop through lines of the file.  Create a data structure for
            // the "samples", the "probes" and a matrix of the intensity values
            String line = "";
            boolean firstLine = true;
            List<double[]> rows = new ArrayList<double[]>();
            int matrix_width = 0;
            while ((line = bufferedReader.readLine()) != null) {
                String[] cols = line.split("\t");

                // For the first line we need to gather all the strain names
                if (firstLine) {
                    firstLine = false;
                    //  get all of our sample names starting at column 1
                    for (int i = 1; i < cols.length; i++) {
                        String sample = cols[i].trim();
                        if (sample.toUpperCase().endsWith(".CEL")) {
                            sample = sample.replace(".CEL", "");
                        }
                        samples.add(sample);
                    }
                    matrix_width = samples.size();
                    continue;
                } else {
                    probes.add(cols[0]);
                    double[] row = new double[matrix_width];
                    for (int i = 1; i < cols.length; i++) {
                        row[i - 1] = new Double(cols[i]).doubleValue();
                    }
                    rows.add(row);
                }
            }
            bufferedReader.close();
            fileReader.close();
            matrix = rows.toArray(matrix);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileReader != null) {
                    fileReader = null;
                }
            } catch (IOException ioe) {
                //do nothing
            }
        }

        lungIntensities.put("samples", samples);
        lungIntensities.put("probes", probes);
        lungIntensities.put("intensities", matrix);
        this.lungIntensityLookup = lungIntensities;
        return lungIntensities;
    }

    public Map<String, List<String>> parseDesign(String designFileName, ServletContext sc)
            throws IOException, FileNotFoundException, InvalidDesignFileException
    {
        // Strain -> [Sample Names...]
        Map<String, List<String>> results = new HashMap<String,List<String>>();

        File designFile = new File(designFileName);
        if (!designFile.exists()) {
            throw new FileNotFoundException("Cannot find Lung Design file.  " +
                    "File does not exist: " + designFile);
        }

        FileReader fileReader = null;
        BufferedReader bufferedReader = null;
        try {
            fileReader = new FileReader(designFile);
            bufferedReader = new BufferedReader(fileReader);

            // loop through lines of the file.  Add new samples to the Map
            // as they are found.  Add new strains to the list associated with
            // each sample
            String line = "";
            boolean firstLine = true;
            int sample_column = -1;
            int strain_column = -1;
            while ((line = bufferedReader.readLine()) != null) {
                String[] cols = line.split("\t");

                // For the first line we need to gather all the strain names
                if (firstLine) {
                    firstLine = false;
                    List<String> header = Arrays.asList(cols);
                    //  Find the sample and strain columns
                    for (int i = 0; i < cols.length; i++) {
                        String col_name = cols[i].toUpperCase();
                        if (col_name.equals("ARRAY")) {
                            sample_column = i;
                        }
                        else if (col_name.equals("STRAIN")) {
                            strain_column = i;
                        }
                    }

                    //  Without both the sample and strain columns we cannot
                    //  proceed.
                    if (sample_column == -1 || strain_column == -1)
                        throw new InvalidDesignFileException("Missing Column:" +
                                "Must include Array and Strain columns.");
                    continue;
                } else {
                    String sample = cols[sample_column];
                    String strain = cols[strain_column];
                    if (results.containsKey(strain)) {
                        List<String> samples = results.get(strain);
                        samples.add(sample);
                        results.put(strain, samples);
                    } else {
                        List<String> samples = new ArrayList<String>();
                        samples.add(sample);
                        results.put(strain, samples);
                    }
                }
            }
            bufferedReader.close();
            fileReader.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw ioe;
        } finally {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (fileReader != null) {
                    fileReader = null;
                }
            } catch (IOException ioe) {
                //do nothing
            }
        }
        this.lungStrainLookup = results;
        return results;
    }

    public void analyzeRegion(OverlappingRegion region) 
            throws MathRuntimeException
    {
        Map<Integer, Gene> genes = region.getGenes();
        List<String> probeIds;
        Set<Map.Entry<Integer,Gene>> geneSet = genes.entrySet();

        for (Map.Entry<Integer,Gene> geneEntry:geneSet) {

            //CGD database ID
            Integer cgdId = geneEntry.getKey();
            Gene gene = geneEntry.getValue();
            //MGI accession ID
            String mgiId = gene.getMgiId();
            // If the gene symbol is not yet set for the gene, set it and the
            // name using the ones pulled from the MGI probe list.  This is
            // a better source than using the CGD SNP Database.
            if (this.mgiLookup.containsKey(mgiId)) {
                if (gene.getSymbol() == null) {
                    gene.setSymbol(this.mgiLookup.get(mgiId).get("symbol"));
                    gene.setName(this.mgiLookup.get(mgiId).get("name"));
                }
            }
            probeIds = new ArrayList<String>();
            if (this.probeSetLookup.containsKey(mgiId)) {
                probeIds = this.probeSetLookup.get(mgiId);
                gene.setProbeSetIds(probeIds);

                //  Object tracks the mean intensity of the high responding strains
                Mean hrMean = new Mean();
                //  Object tracks the mean intensity of the low responding strains
                Mean lrMean = new Mean();
                //  These two variables are storing the intensity values to be
                //  passed to a TTest class to calculate the P-Value.
                List<Double> hrIntensities = new ArrayList<Double>();
                List<Double> lrIntensities = new ArrayList<Double>();
                for (String probe : probeIds) {
                    int probe_pos = this.probes.indexOf(probe);
                    for (String strain : region.getHighRespondingStrains()) {
                        List<String> strainSamples =
                                this.lungStrainLookup.get(strain);
                        if (strainSamples == null) {
                            System.out.println("For MGI ID: " + mgiId + " probe: " + probe + " HR strain " + strain + " there were no associated samples!");
                            System.out.println("skipping");
                            continue;
                        }
                        for (String sample : strainSamples) {
                            int sample_pos = this.samples.indexOf(sample);
                            if (sample_pos < 0 || probe_pos < 0) {
                                System.out.println(probe + ":" + sample + " does not have a matching intensity. Positions " + probe_pos + "x" + sample_pos);
                                continue;
                            } else {
                                hrIntensities.add(this.intensityMatrix[probe_pos][sample_pos]);
                                hrMean.increment(this.intensityMatrix[probe_pos][sample_pos]);
                            }
                        }
                    }
                    for (String strain : region.getLowRespondingStrains()) {
                        List<String> strainSamples =
                                this.lungStrainLookup.get(strain);
                        if (strainSamples == null) {
                            System.out.println("For MGI ID: " + mgiId + " probe: " + probe + " LR strain " + strain + " there were no associated samples!");
                            System.out.println("skipping");
                            continue;
                        }
                        for (String sample : strainSamples) {
                            int sample_pos = this.samples.indexOf(sample);
                            if (sample_pos < 0 || probe_pos < 0) {
                                System.out.println(probe + ":" + sample + " does not have a matching intensity. Positions " + probe_pos + "x" + sample_pos);
                                continue;
                            } else {
                                lrIntensities.add(this.intensityMatrix[probe_pos][sample_pos]);
                                lrMean.increment(this.intensityMatrix[probe_pos][sample_pos]);
                            }
                        }
                    }
                }
                double[] hrArray = new double[0];
                if (hrIntensities.size() > 0) {
                    hrArray = ArrayUtils.toArray(hrIntensities);
                    gene.setHighRespondingMeanIntensity(hrMean.getResult());

                } else {
                    // If there are no intensities, set to NaN
                    gene.setHighRespondingMeanIntensity(Double.NaN);
                }
                double[] lrArray = new double[0];
                if (lrIntensities.size() > 0) {
                    lrArray = ArrayUtils.toArray(lrIntensities);
                    gene.setLowRespondingMeanIntensity(lrMean.getResult());

                } else {
                    // If there are no intensities, set to NaN
                    gene.setLowRespondingMeanIntensity(Double.NaN);
                }


                TTest tTester = new TTestImpl();
                try {
                    if (hrArray.length < 2 || lrArray.length < 2) {
                        System.out.println("No pvalue calculated for Gene " + mgiId + " insufficient data for t statistic, needs at least 2");
                        gene.setPValue(Double.NaN);

                    } else {
                        gene.setPValue(tTester.tTest(hrArray, lrArray));
                    }
                } catch (MathException me) {
                    //  If we get a math exception, log it, and set PValue to NAN
                    me.printStackTrace();
                    gene.setPValue(Double.NaN);
                } catch (MathRuntimeException mre) {
                    mre.printStackTrace();
                    gene.setPValue(Double.NaN);
                }

            }

        }
    }
}
