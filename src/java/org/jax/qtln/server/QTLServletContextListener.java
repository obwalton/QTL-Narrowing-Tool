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


package org.jax.qtln.server;

import java.io.*;
import java.net.MalformedURLException;
import java.util.*;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;

import org.jax.qtln.mgisearch.QTLPhenotypeLoader;
import org.xml.sax.SAXException;
import org.jax.qtln.snpsets.SangerSNPFile;
import org.jax.qtln.snpsets.UNCSNPFile;

/**
 *
 * @author dow
 */
public class QTLServletContextListener implements ServletContextListener {

    //  the name of the application's properties file.
    private static final String propertiesFile = "qnt.properties";
    
    //  These are all for the database stored version of the CGD SNPS
    private String driver = "com.mysql.jdbc.Driver";

    //  This is a shared database connection for DB Storage of SNPS
    //private static Connection conn;

    //  The next two variables should be passed in as parameters to the
    //  servlet config.
    private String snpDirName = "/Users/dow/workspace/QTLNarrowing/data/CGD/imputed";
    
    private String sangerSnpFile = "/Users/dow/workspace/cgd/RV/test_data/20110602-final-snps.vcf.gz";
    private String uncSnpFile = "/Users/dow/workspace/cgd/RV/test_data/UNC_88_Combined.txt.gz";
    //  TODO: come up with a clever way of dealing with file name with changing
    //  version and build
    private String imputedSnpsBaseName =
            "chr(\\d\\d?|[XYxy])_.*\\.txt";

    //  These are the files containing the default Lung Experiment for the
    //  expression analysis.  Again:
    //  These are only default values for testing on my mac, actual values
    //  should be pulled from the qnt.propertie file
    private String LUNG_RMA =
            "/Users/dow/workspace/QTLNarrowing/data/GEX/Lung/lung_rma.dat";
    private String LUNG_DESIGN =
            "/Users/dow/workspace/QTLNarrowing/data/GEX/Lung/lung_design.txt";
    private String LIVER_RMA =
            "/Users/dow/workspace/QTLNarrowing/data/GEX/Liver/liver_rma.dat";
    private String LIVER_DESIGN =
            "/Users/dow/workspace/QTLNarrowing/data/GEX/Liver/liver_design.txt";
    private String LIVER_LF_DESIGN =
            "/Users/dow/workspace/QTLNarrowing/data/GEX/Liver/liver_lf_design.txt";
    private String LIVER_HF_DESIGN =
            "/Users/dow/workspace/QTLNarrowing/data/GEX/Liver/liver_hf_design.txt";
    private String SKINGRAFT_ALOPECIA_AREATA_RMA = "/Users/dow/workspace/QTLNarrowing/data/GEX/AlopeciaAreata/SkinGraft_vs_Sham_by_Weeks_rma_expr_norm.txt";
    private String SKINGRAFT_ALOPECIA_AREATA_DESIGN = "/Users/dow/workspace/QTLNarrowing/data/GEX/AlopeciaAreata/SkinGraft_vs_Sham_by_Weeks_design.txt";
    private String SPONTANEOUS_ALOPECIA_AREATA_RMA = "/Users/dow/workspace/QTLNarrowing/data/GEX/AlopeciaAreata/Spontaneous_vs_2007_Normal_rma_expr_norm.txt";
    private String SPONTANEOUS_ALOPECIA_AREATA_DESIGN = "/Users/dow/workspace/QTLNarrowing/data/GEX/AlopeciaAreata/Spontaneous_vs_2007_Normal_design.txt";
    private String STRAIN_XREF =
            "/Users/dow/workspace/QTLNarrowing/data/GEX/StrainXRef.txt";
    private String MGI_FTP_ADDR = "ftp.informatics.jax.org";
    private String MGI_REPORTS_DIR = "pub/reports";
    private String MGI_AFFY_430A_2_0_FILE = "Affy_430A_2.0_mgi.rpt";
    private String MGI_AFFY_430_2_0_FILE = "Affy_430_2.0_mgi.rpt";
    private String MGI_AFFY_U74_FILE = "Affy_U74_mgi.rpt";
    private String MGI_AFFY_V1_0_FILE = "Affy_1.0_ST_mgi.rpt";
    private String SOLR_HOME =
            "/Users/dow/workspace/QTLN/apache-solr-1.4.1/example/solr";
    private String MGI_MARKERS_FILE = "MGI_Coordinate.rpt";
    private String MGI_QTL2MP_FILE = "MGI_PhenoGenoMP.rpt";
    private String MGI_MP_FILE = "VOC_MammalianPhenotype.rpt";
    private static final int MGI_PROBE_FILE_HEADER_SIZE = 5;

    // Chromosome -> SNP detail
    private Map<String, SNPFile> cgdSNPLookup;

    // MGIAccession ID -> Probe ID
    private Map<String, List<String>> probeLookup;

    // MGIAccession ID -> {'Symbol':symbol, 'Name':name}
    private Map<String, Map<String, String>> mgiLookup;

    // "samples", "probes", "intensities"
    // samples -> List of sample names
    // probes  -> List of probe ids
    // intensities -> matrix rows/columns = probes/samples
    private Map lungExpIntensities;
    private Map liverExpIntensities;
    private Map spontaneousAlopeciaAreataIntensities;
    private Map skinGraftAlopeciaAreataIntensities;
    // Sample Name -> [Strains...]
    private Map<String, List<String>> lungExpStrains;
    private Map<String, List<String>> liverExpStrains;
    private Map<String, List<String>> liverLowFatExpStrains;
    private Map<String, List<String>> liverHighFatExpStrains;
    private Map<String, List<String>> spontaneousAlopeciaAreataStrains;
    private Map<String, List<String>> skinGraftAlopeciaAreataStrains;
    
    public void contextInitialized(ServletContextEvent event) {
        ServletContext sc = event.getServletContext();

        sc.log("######  DOING QTLNARROWING CUSTOM INITIALIZATION in Listener ######");
        sc.setAttribute("SNP_INIT_STATUS", "INIT");
        
        //  Let's start by loading our properties.  Without these, we don't know
        //  where to look for all of our data files and db info
        Properties myProperties = null;

        //  First see if the default properties file is present
        File propFile = new File(QTLServletContextListener.propertiesFile);
        try {
            // If this properties file exists in the local directory
            if (propFile.exists()) {
                sc.log("Using properties local dir");
                FileInputStream fis = new FileInputStream(propFile);
                myProperties = new Properties();
                myProperties.load(fis);
            } else {
                // If it does not, next we'll try to load from the root of the
                // class path
                InputStream propIS =
                        this.getClass().getClassLoader().getResourceAsStream(
                        "/org/jax/qtln/" + QTLServletContextListener.propertiesFile);
                if (propIS != null) {
                    sc.log("Using properties from classpath.");
                    myProperties = new Properties();
                    myProperties.load(propIS);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (myProperties != null) {
            assignProperties(myProperties, sc);
        } else {
            //  If properties are null, log that we are using default TEST Values
            sc.log("NO PROPERTIES FILE FOUND, USING TEST DEFAULT VALUES");
        }

        //  Set up the Solr server
        SolrServer server;
        try {
            sc.log("creating solr dataset");
            // Note that the following property could be set through JVM level arguments too
            System.setProperty("solr.solr.home", this.SOLR_HOME);
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            CoreContainer coreContainer = initializer.initialize();
            server = new EmbeddedSolrServer(coreContainer, "");
            QTLPhenotypeLoader loadMGIServer = new QTLPhenotypeLoader(server, 
                    this.MGI_FTP_ADDR, this.MGI_REPORTS_DIR,
                    this.MGI_MARKERS_FILE, this.MGI_QTL2MP_FILE,
                    this.MGI_MP_FILE);
            server = loadMGIServer.getLoadedServer(sc);
            sc.log("setting solrServer attribute");
            sc.setAttribute("solrServer", server);
        } catch (MalformedURLException mue) {
            sc.log("Failed to create server - MalformedURLException");
            sc.log(mue.getMessage());
            sc.log(mue.getStackTrace().toString());
        } catch (IOException ioe) {
            sc.log("Problem building our embedded server");
            sc.log(ioe.getMessage());
            sc.log(ioe.getStackTrace().toString());
        } catch (ParserConfigurationException pce) {
            sc.log("ParserConfigurationException");
            sc.log(pce.getMessage());
            sc.log(pce.getStackTrace().toString());
        } catch (SAXException saxe) {
            sc.log("SAXException");
            sc.log(saxe.getMessage());
            sc.log(saxe.getStackTrace().toString());
        } catch (Exception e) {
            sc.log("Catch all for the rest of the exceptions");
            sc.log(e.getMessage());
            StackTraceElement[] trace = e.getStackTrace();
            for (StackTraceElement element: trace) {
                sc.log(element.toString());
            }
        }
        
        
        //  Set up the Sanger SNP File for searching Sanger SNPs
        try {
            SangerSNPFile ssnpf = initSangerSNP(sc);
            sc.setAttribute("sangerSNPs", ssnpf);
            sc.setAttribute("SANGER_INIT_STATUS","SUCCESS");
        } catch (Exception e) {
            sc.log("Failure setting up Sanger SNPs...");
            sc.log(e.getMessage());
            sc.setAttribute("SANGER_INIT_STATUS","FAIL");
        }
        
        //  Set up the UNC SNP File for searching UNC SNPs
        try {
            UNCSNPFile usnpf = initUNCSNP(sc);
            sc.setAttribute("uncSNPs", usnpf);
            sc.setAttribute("UNC_INIT_STATUS","SUCCESS");
        } catch (Exception e) {
            sc.log("Failure setting up UNC SNPs...");
            sc.log(e.getMessage());
            sc.setAttribute("UNC_INIT_STATUS","FAIL");
        }

        //  Set up the SNP Database

        //boolean success = true;
        boolean success = false;
        /*try {
            this.cgdSNPLookup = initSnpDB(sc);
        } catch (ServletException se) {
            sc.log(se.getMessage());
            success = false;
        }*/
        if (!success) {
            sc.setAttribute("SNP_INIT_STATUS", "FAIL");
        } else {
            sc.setAttribute("SNP_INIT_STATUS", "SUCCESS");
            sc.setAttribute("snpLookup", this.cgdSNPLookup);
        }
        sc.log("SNPDB INITIALIZED");
        
        

        try {
            initProbeSetLookup(sc);
            sc.setAttribute("PROBE_INIT_STATUS", "SUCCESS");
            sc.setAttribute("probeLookup", this.probeLookup);
            sc.log("MGI LOOKUP CONTAINS: " + this.mgiLookup.size() + " mappings");
            sc.setAttribute("mgiLookup", this.mgiLookup);
        } catch (ServletException se) {
            sc.log(se.getMessage());
            sc.setAttribute("PROBE_INIT_STATUS", "FAIL");
        }
        sc.log("PROBELOOKUP INITIALIZED");
        
        try {
            initDefaultExpressionLookups(sc);
            sc.setAttribute("GEX_INIT_STATUS", "SUCCESS");
        } catch (ServletException se) {
            sc.log(se.getMessage());
            sc.setAttribute("GEX_INIT_STATUS", "FAIL");
        }
        
        try {
            Map<String, Map<String, String>> xref = getStrainXRef(sc);
            sc.setAttribute("GEX_INIT_STATUS", "SUCCESS");
            sc.setAttribute("strainXRef", xref);
        } catch (ServletException se) {
            sc.log(se.getMessage());
            sc.setAttribute("GEX_INIT_STATUS", "FAIL");
        }
        
        sc.log("EXPRESSION LOOKUPS INITIALIZED");

    }

    public void contextDestroyed(ServletContextEvent event) {
    }

    private Map<String,Map<String, String>> getStrainXRef(ServletContext sc) 
            throws ServletException
    {
        Map<String,Map<String,String>> xref = new HashMap<String, Map<String,String>>();
        try {
            FileReader fileReader = new FileReader(this.STRAIN_XREF);
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            // loop through lines of the file.  Separating each line
            // into a row of the dataset object.
            String line = "";
            boolean firstLine = true;
            String[] header = new String[0];
            String[] cols;
            while ((line = bufferedReader.readLine()) != null) {
                cols = line.split("\t");

                // For the first line we need to gather all the strain names
                if (firstLine) {
                    header = cols;
                    firstLine = false;
                    //  Get all snp set names...
                    for (int i = 0; i < header.length; i++) {
                        //this.strainBaseCalls.put(cols[i], new WAHBitSet());
                        xref.put(header[i].trim(), new HashMap<String, String>());
                    }
                    continue;
                } //  Create a HashMap lookup for each column other than 1st
                else {
                    //  Get the master strain name from expression sets
                    String master = cols[0];
                    // for each line add an entry in appropriate Map for rows where 
                    // "other" col has an entry
                    for (int i = 1; i < cols.length; i++) {
                        if (!("".equals(cols[i].trim()))) {
                            Map<String, String> map = xref.get(header[i]);
                            // Add each of the maps to the xref by column name
                            map.put(cols[i].trim(), master);
                            xref.put(header[i], map);   // not really necessary
                        }
                    }
                }
            }
            // Close the file readers, we're all done with them.
            bufferedReader.close();
            fileReader.close();
        } catch (IOException ioe) {
            throw new UnavailableException("Problem getting Strain Cross Reference: "
                    + ioe.getMessage());
        }
        return xref;
    }
    
    private SangerSNPFile initSangerSNP(ServletContext sc) {
        SangerSNPFile sangerSNPs = new SangerSNPFile(this.sangerSnpFile);
        sc.log("***** CONSTRUCTED SangerSNPFile. Has " + sangerSNPs.getStrains().size() + " Strains associated with it!");
        return sangerSNPs;
    }

    private UNCSNPFile initUNCSNP(ServletContext sc) {
        UNCSNPFile uncSNPs = new UNCSNPFile(this.uncSnpFile);
        sc.log("***** CONSTRUCTED UNCSNPFile. Has " + uncSNPs.getStrains().size() + " Strains associated with it!");
        return uncSNPs;
    }

    private HashMap<String, SNPFile> initSnpDB(ServletContext sc)
            throws ServletException
    {
        HashMap<String, SNPFile> snpLookup = new HashMap<String, SNPFile>();
        File snpDir = new File(this.snpDirName);
        if (!snpDir.exists()) {
            throw new UnavailableException("Cannot fine CGD SNP files.  " +
                    "Directory does not exist: " + this.snpDirName);
        }
        if (!snpDir.isDirectory()) {
            throw new UnavailableException("Not a directory: " + this.snpDirName);
        }

        String[] snpFileNames = snpDir.list();
        if (snpFileNames.length == 0) {
            throw new UnavailableException("Directory empty: " + this.snpDirName);
        }
        Pattern snpNamePattern = Pattern.compile(this.imputedSnpsBaseName);
        //  we expect files for Chr 1-19 and X
        int chr_count = 0;
        sc.log("INIT: Cycle through files in dir, load snp files...");
        for (String snpFileName : snpFileNames) {
            File snpFile = new File(snpFileName);
            sc.log("INIT: Creating SNPFile Object");
            SNPFile result = new SNPFile(snpDir, snpFile, snpNamePattern);
            if (result.valid()) {
                sc.log("INIT: Loading SNP file for chromosome " +
                        result.getChromosome());
                try {
                    result.load();
                } catch (IOException ioe) {
                    sc.log("INIT:  FAILURE DUE TO IO ISSUE");
                    ioe.printStackTrace();
                    throw new UnavailableException(
                            "Problem reading CGD Imputed SNP File: " +
                            ioe.getMessage());
                }
                chr_count += 1;
                sc.log("INIT: Chromosome loaded " + chr_count);
                snpLookup.put(result.getChromosome(), result);
                sc.log(result.getDetails());
            }
        }
        //if (chr_count < 1)
        //    throw new UnavailableException("Expected 20 Chromosome files in CGD " +
        //            "data directory (" + snpDirName + "), only found " + chr_count);
        sc.log("Custom Initialization Complete!");
        return snpLookup;
    }

    private void initProbeSetLookup(ServletContext sc)
            throws ServletException
    {
        this.probeLookup = new HashMap<String, List<String>>();
        this.mgiLookup = new HashMap<String, Map<String, String>>();
        sc.log("Open FTP Connection to MGI");
        FTPClient ftp = new FTPClient();
        try {
            String[] reportNames = new String[2];
            //InputStream[] inStreams = new InputStream[3];  //If we add 3rd affy
            reportNames[1] = MGI_AFFY_430_2_0_FILE;
            reportNames[0] = MGI_AFFY_V1_0_FILE;
            //inStreams[2] = ftp.retrieveFileStream(MGI_AFFY_U74_FILE);
            sc.log("iterate through files...");
            for (String filename : reportNames) {
                sc.log("Make connection to fetch " + filename);
                ftp = new FTPClient();
                int reply;
                sc.log("Connecting...");
                ftp.connect(MGI_FTP_ADDR);

                // After connection attempt, you should check the reply code to verify
                // success.
                reply = ftp.getReplyCode();
                sc.log("Reply code = " + reply);

                if (!FTPReply.isPositiveCompletion(reply)) {
                    ftp.disconnect();
                    sc.log("FTP server refused connection.");
                    throw new UnavailableException("Problem connecting to MGI server! ");
                }
                //  TODO: replace this with a "QTL Narrowing" specific mail addr
                sc.log("Logging in as anonymous...");
                ftp.login("anonymous", "cbr-help@jax.org");
                ftp.cwd(MGI_REPORTS_DIR);
                sc.log("Reading " + filename + " from MGI Report server");
                InputStream inStream = ftp.retrieveFileStream(filename);

                BufferedReader bufferedReader =
                        new BufferedReader(new InputStreamReader(inStream));
                //File has a 5 line header, remove it...
                int head_count = 0;
                String line;
                // probeset_id = col 0
                // seq id = col 1
                // mgi id = col 2
                // symbol = col 3
                // name   = col 4
                sc.log("Start reading Probe mapping from MGI...");
                while ((line = bufferedReader.readLine()) != null) {
                    if (head_count < (MGI_PROBE_FILE_HEADER_SIZE - 1)) {
                        ++head_count;
                        continue;
                    }
                    String[] tokens = line.split("\t");
                    // Skip probes that don't have our 5 required columns
                    if (tokens.length < 5) {
                        continue;
                    }
                    // Add probe to lookup by MGI Accession ID
                    // Because 1 gene may be associated with multiple probes,
                    // add it as a list of probes.
                    List<String> probes = new ArrayList<String>();
                    if (this.probeLookup.containsKey(tokens[2])) {
                        probes = this.probeLookup.get(tokens[2]);
                    }
                    probes.add(tokens[0]);
                    this.probeLookup.put(tokens[2], probes);
                    if (!this.mgiLookup.containsKey(tokens[2])) {
                        Map<String, String> geneMap = new HashMap<String, String>();
                        geneMap.put("symbol", tokens[3]);
                        geneMap.put("name", tokens[4]);
                        this.mgiLookup.put(tokens[2], geneMap);
                    }

                }
                sc.log("Done reading Probe mapping from MGI!");
                bufferedReader.close();
                inStream.close();
                ftp.logout();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new UnavailableException("Problem getting MGI ProbeSet Mappings: "
                    + ioe.getMessage());
        } finally {
            try {
                ftp.disconnect();
            } catch (IOException ioe) {
                // do nothing
            }
        }
    }

    private void initDefaultExpressionLookups(ServletContext sc)
            throws ServletException
    {
        ExpressionAnalyzer ea = new ExpressionAnalyzer();
        try {
            sc.log("INIT: Reading RMA File");
            this.lungExpIntensities = ea.parseRMA(this.LUNG_RMA, sc);
            sc.setAttribute("lungIntensityLookup", this.lungExpIntensities);
            this.liverExpIntensities = ea.parseRMA(this.LIVER_RMA, sc);
            sc.setAttribute("liverIntensityLookup", this.liverExpIntensities);
            this.spontaneousAlopeciaAreataIntensities = ea.parseRMA(this.SPONTANEOUS_ALOPECIA_AREATA_RMA, sc);
            sc.setAttribute("spontaneousAlopeciaAreataIntensityLookup", this.spontaneousAlopeciaAreataIntensities);
            this.skinGraftAlopeciaAreataIntensities = ea.parseRMA(this.SKINGRAFT_ALOPECIA_AREATA_RMA, sc);
            sc.setAttribute("skinGraftAlopeciaAreataIntensityLookup", this.skinGraftAlopeciaAreataIntensities);
            sc.log("INIT: Reading Design File");
            this.lungExpStrains = ea.parseDesign(this.LUNG_DESIGN, sc);
            sc.setAttribute("lungStrainLookup", this.lungExpStrains);
            this.liverExpStrains = ea.parseDesign(this.LIVER_DESIGN, sc);
            sc.setAttribute("liverStrainLookup", this.liverExpStrains);
            this.liverLowFatExpStrains = ea.parseDesign(this.LIVER_LF_DESIGN, sc);
            sc.setAttribute("liverLowFatStrainLookup", this.liverLowFatExpStrains);
            this.liverHighFatExpStrains = ea.parseDesign(this.LIVER_HF_DESIGN, sc);
            sc.setAttribute("liverHighFatStrainLookup", this.liverHighFatExpStrains);
            this.spontaneousAlopeciaAreataStrains = ea.parseDesign(this.SPONTANEOUS_ALOPECIA_AREATA_DESIGN, sc);
            sc.setAttribute("spontaneousAlopeciaAreataStrainLookup", this.spontaneousAlopeciaAreataStrains);
            this.skinGraftAlopeciaAreataStrains = ea.parseDesign(this.SKINGRAFT_ALOPECIA_AREATA_DESIGN, sc);
            sc.setAttribute("skinGraftAlopeciaAreataStrainLookup", this.skinGraftAlopeciaAreataStrains);

        } catch (Throwable e) {
            sc.log("INIT:  FAILURE DUE TO ISSUE: ");
            e.printStackTrace();
            throw new UnavailableException(e.getMessage());
        }

    }

    private void assignProperties(Properties p, ServletContext sc) {

        sc.log("Assigning Properties...");

        if (p.containsKey("threaded")) {
            sc.setAttribute("threaded", p.getProperty("threaded"));
        } else {
            sc.setAttribute("threaded", "0");
        }
        if (p.containsKey("db_driver")) {
            this.driver = p.getProperty("db_driver");
            sc.setAttribute("db_driver", this.driver);
        }
        if (p.containsKey("db_protocol")) {
            sc.setAttribute("db_protocol", p.getProperty("db_protocol"));
        }
        if (p.containsKey("db_host")) {
            sc.setAttribute("db_host", p.getProperty("db_host"));
        }
        if (p.containsKey("db_port")) {
            sc.setAttribute("db_port", p.getProperty("db_port"));
        }
        if (p.containsKey("db_name")) {
            sc.setAttribute("db_name", p.getProperty("db_name"));
        }
        if (p.containsKey("db_user")) {
            sc.setAttribute("db_user", p.getProperty("db_user"));
        }
        if (p.containsKey("db_password")) {
            sc.setAttribute("db_password", p.getProperty("db_password"));
        }

        if (p.containsKey("snp_base_file_name")) {
            this.imputedSnpsBaseName =
                    p.getProperty("snp_base_file_name");
        }
        
        if (p.containsKey("sanger_snp_file_name")){
            this.sangerSnpFile =
                    p.getProperty("sanger_snp_file_name");
        }
        
        if (p.containsKey("unc_snp_file_name")){
            this.uncSnpFile =
                    p.getProperty("unc_snp_file_name");
        }
        
        if (p.containsKey("data_directory")) {
            String data_dir = p.getProperty("data_directory");
            if (p.containsKey("snp_data_dir")) {
                this.snpDirName = data_dir + File.separator
                        + p.getProperty("snp_data_dir");
            }
            if (p.containsKey("lung_rma")) {
                this.LUNG_RMA = data_dir + File.separator
                        + p.getProperty("lung_rma");
            }
            if (p.containsKey("lung_design")) {
                this.LUNG_DESIGN = data_dir + File.separator
                        + p.getProperty("lung_design");
            }
            if (p.containsKey("liver_rma")) {
                this.LIVER_RMA = data_dir + File.separator
                        + p.getProperty("liver_rma");
            }
            if (p.containsKey("liver_design")) {
                this.LIVER_DESIGN = data_dir + File.separator
                        + p.getProperty("liver_design");
            }
            if (p.containsKey("liver_lf_design")) {
                this.LIVER_LF_DESIGN = data_dir + File.separator
                        + p.getProperty("liver_lf_design");
            }
            if (p.containsKey("liver_hf_design")) {
                this.LIVER_HF_DESIGN = data_dir + File.separator
                        + p.getProperty("liver_hf_design");
            }
            if (p.containsKey("spontaneous_alopecia_areata_rma")) {
                this.SPONTANEOUS_ALOPECIA_AREATA_RMA = data_dir + File.separator
                        + p.getProperty("spontaneous_alopecia_areata_rma");
            }
            if (p.containsKey("spontaneous_alopecia_areata_design")) {
                this.SPONTANEOUS_ALOPECIA_AREATA_DESIGN = data_dir + File.separator
                        + p.getProperty("spontaneous_alopecia_areata_design");
            }
            if (p.containsKey("skingraft_alopecia_areata_rma")) {
                this.SKINGRAFT_ALOPECIA_AREATA_RMA = data_dir + File.separator
                        + p.getProperty("skingraft_alopecia_areata_rma");
            }
            if (p.containsKey("skingraft_alopecia_areata_design")) {
                this.SKINGRAFT_ALOPECIA_AREATA_DESIGN = data_dir + File.separator
                        + p.getProperty("skingraft_alopecia_areata_design");
            }
            if (p.containsKey("strain_xref")) {
                this.STRAIN_XREF = data_dir + File.separator
                        + p.getProperty("strain_xref");
            }
        }

        if (p.containsKey("solr_home")) {
            this.SOLR_HOME = p.getProperty("solr_home");
        }
        if (p.containsKey("mgi_ftp_addr"))
            this.MGI_FTP_ADDR = p.getProperty("mgi_ftp_addr");
        if (p.containsKey("mgi_reports_dir"))
            this.MGI_REPORTS_DIR = p.getProperty("mgi_reports_dir");
        if (p.containsKey("mgi_affy_430_2_0_file"))
            this.MGI_AFFY_430_2_0_FILE = p.getProperty("mgi_affy_430_2_0_file");
        if (p.containsKey("mgi_affy_u74_file"))
            this.MGI_AFFY_U74_FILE = p.getProperty("mgi_affy_u74_file");
        if (p.containsKey("mgi_affy_v1_0_file"))
            this.MGI_AFFY_V1_0_FILE = p.getProperty("mgi_affy_v1_0_file");
        if (p.containsKey("mgi_markers"))
            this.MGI_MARKERS_FILE = p.getProperty("mgi_markers");
        if (p.containsKey("mgi_qtl2mp"))
            this.MGI_QTL2MP_FILE = p.getProperty("mgi_qtl2mp");
        if (p.containsKey("mgi_mpterms"))
            this.MGI_MP_FILE = p.getProperty("mgi_mpterms");
    }


}
