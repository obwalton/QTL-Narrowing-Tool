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



import org.jax.qtln.regions.InvalidChromosomeException;
import org.jax.qtln.regions.QTLSet;
import org.jax.qtln.regions.Region;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.math.MathRuntimeException;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.jax.qtln.client.QTLService;
import org.jax.qtln.client.SMSException;
import org.jax.qtln.db.CGDSnpDB;
import org.jax.qtln.mgisearch.MGIQTL;
import org.jax.qtln.regions.Gene;
import org.jax.qtln.regions.OverlappingRegion;
import org.jax.qtln.regions.QTL;
import org.jax.qtln.regions.ReturnRegion;
import org.jax.qtln.regions.SNP;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class QTLServiceImpl extends RemoteServiceServlet implements
        QTLService {

    private String narrowingStatus = "Waiting";

    //  These are all for the database stored version of the CGD SNPS
    private String driver = "com.mysql.jdbc.Driver";
    private static String db_protocol = "jdbc:mysql:";
    private static String db_host = "cgd.jax.org";
    private static String db_name = "cgdsnpdb";
    private static String db_user = "pup";
    private static String db_password = "puppass";

    //  Various Lookups that are initialized at the time the webapp is
    //  deployed (in QTLServletContextListener).  The variables are actually
    //  assigned in the servlet init() below.  They are stored in the servlet
    //  context.
    // Chromosome -> SNP detail
    private static Map<String, SNPFile> cgdSNPLookup;
    // MGIAccession ID -> Probe ID
    private static Map<String, List<String>> probeSetLookup;
    // MGIAccession ID -> {"symbol"->symbol,"name"->name}
    private static Map<String, Map<String,String>> mgiLookup;
    // "samples", "probes", "intensities"
    // samples -> List of sample names
    // probes  -> List of probe ids
    // intensities -> matrix rows/columns = probes/samples
    private static Map lungIntensityLookup;
    private static Map liverIntensityLookup;
    // Strain -> [Sample names...]
    private static Map<String, List<String>> lungStrainLookup;
    private static Map<String, List<String>> liverStrainLookup;
    private static Map<String, List<String>> liverLowFatStrainLookup;
    private static Map<String, List<String>> liverHighFatStrainLookup;

    //  This lookup is initialized in the Servlet "init()" method below.
    // cgdsnpdb _loc_func_key -> location/function description
    private static Map<Integer,String> snpLocFuncs;
    private static SolrServer solrServer;

    /** init
     * Runs inititalizations that must occur before methods of servlet are run.
     * This method doesn't appear to run until the first method of the servlet
     * is called.  Initializations done here are kept to items that are not
     * time consuming.  Should only run once during life of servlet.  Variables
     * initialized here are shared by all users of the servlet.
     * <p>
     * Time consuming initializations are done in the
     * @see org.jax.qtln.server.QTLServletContextListener
     * class.
     * @throws ServletException
     */
    public void init() throws ServletException {
        ServletContext context = this.getServletContext();
        String status = (String)context.getAttribute("SNP_INIT_STATUS");
        System.out.println("SNP INITIALIZATION = " + status);
        QTLServiceImpl.cgdSNPLookup =
                (Map<String, SNPFile>)context.getAttribute("snpLookup");
        System.out.println("PROBE INITIALIZATION = " + 
                (String)context.getAttribute("PROBE_INIT_STATUS"));
        QTLServiceImpl.probeSetLookup =
                (Map<String, List<String>>)context.getAttribute("probeLookup");
        QTLServiceImpl.mgiLookup =
                (Map<String, Map<String,String>>)context.getAttribute("mgiLookup");
        System.out.println("GEX INITIALIZATION = " +
                (String)context.getAttribute("GEX_INIT_STATUS"));
        QTLServiceImpl.lungIntensityLookup =
                (Map)context.getAttribute("lungIntensityLookup");
        QTLServiceImpl.lungStrainLookup =
                (Map<String, List<String>>)context.getAttribute("lungStrainLookup");
        QTLServiceImpl.liverIntensityLookup =
                (Map)context.getAttribute("liverIntensityLookup");
        QTLServiceImpl.liverStrainLookup =
                (Map<String, List<String>>)context.getAttribute("liverStrainLookup");
        QTLServiceImpl.liverLowFatStrainLookup =
                (Map<String, List<String>>)context.getAttribute("liverLowFatStrainLookup");
        QTLServiceImpl.liverHighFatStrainLookup =
                (Map<String, List<String>>)context.getAttribute("liverHighFatStrainLookup");
         QTLServiceImpl.solrServer =
                (SolrServer)context.getAttribute("solrServer");
       
        
        //  If any of these were not provided with user properties, we'll
        //  use the default values instead
        String driver = (String)context.getAttribute("db_driver");
        if (driver != null) this.driver = driver;
        String db_protocol = (String)context.getAttribute("db_protocol");
        if (db_protocol != null) QTLServiceImpl.db_protocol = db_protocol;
        String db_host = (String)context.getAttribute("db_host");
        if (db_host != null)  QTLServiceImpl.db_host = db_host;
        String db_name = (String)context.getAttribute("db_name");
        if (db_name != null) QTLServiceImpl.db_name = db_name;
        String db_user = (String)context.getAttribute("db_user");
        if (db_user != null) QTLServiceImpl.db_user = db_user;
        String db_password = (String)context.getAttribute("db_password");
        if (db_password != null) QTLServiceImpl.db_password = db_password;

        //  Get our location function lookup
        /*  Must disable until port is opened in firewall
         *  WAITIN ON IT....
        CGDSnpDB snpDb = new CGDSnpDB(QTLServiceImpl.db_host,
                QTLServiceImpl.db_name, QTLServiceImpl.db_user,
                QTLServiceImpl.db_password);
        try {
            if (QTLServiceImpl.snpLocFuncs == null) {
                QTLServiceImpl.snpLocFuncs = snpDb.getSNPLocFuncList();
            }
        } catch (SQLException sqle) {
            throw new ServletException(sqle.getMessage());
        }
         * 
         */

    }

    /**
     * readQTLFile is used to read in the users uploaded QTL Input File.
     * This method assumes that the file was uploaded using the
     * @see org.jax.qtln.server.QTLFileUploadServlet and uses the
     * @see org.jax.qtln.server.QTLFileReader to actually read the file.
     * @return A List of String arrays where each string array represents
     * a tokenized line from the data file.
     * @throws SMSException
     */
    public List<String[]> readQTLFile()
            throws SMSException {

        // Check the HttpSession to see if a sequence file was uploaded
        HttpSession session = this.getSession();
        HttpServletRequest hsr = this.getThreadLocalRequest();

        //  The actual logic for running the analysis is in a separate
        //  class from the actual servlet.  I did this because the servlet
        //  class is shared by all users, and I wanted to use class attributes
        //  that were not shared.
        QTLFileReader qfr = new QTLFileReader();
        return qfr.readQTLFile(session, hsr);

    }

    /**
     * getStrains is used to fetch the list of strains created at initialization
     * 
     * @see org.jax.qtln.server.SNPFile to see actual source of strains.
     * @return A array of Strings representing the list of valid strains we can
     * work with.
     * @throws SMSException
     */
    public String[] getStrains()
            throws SMSException
    {
        System.out.println ("IN getStrains");
        String[] strains = new String[0];
        if (QTLServiceImpl.cgdSNPLookup != null) {
            //  Only need one SNPFile as the strain list
            //  should be the same in all
            Set keys = this.cgdSNPLookup.keySet();
            for (Iterator i = keys.iterator(); i.hasNext();) {
                String key = (String)i.next();
                System.out.println("getting strains from " + key);
                SNPFile snpf = this.cgdSNPLookup.get(key);
                strains = snpf.getStrains();
                break;
            }
        } else {
            System.out.println("NO LOOKUP AVAILABLE");
            throw new SMSException("No strains found.  Initialization must " +
                    "have been unsuccessful.");
        }
        System.out.println("Returning " + strains.length + " strains");
        return strains;

    }


    /**
     * getStrains is used to fetch the list of strains created at initialization
     *
     * @see org.jax.qtln.server.SNPFile to see actual source of strains.
     * @return A array of Strings representing the list of valid strains we can
     * work with.
     * @throws SMSException
     */
    public Map<Integer,String> getSnpAnnotLookup()
            throws SMSException
    {
        System.out.println ("IN getSnpAnnotLookup");
        Map<Integer, String> annotations = new HashMap<Integer, String>();
        if (snpLocFuncs != null) {
            annotations = snpLocFuncs;
        } else {
            System.out.println("NO LOOKUP AVAILABLE");
            throw new SMSException("No SNP Annotation Lookup found.  " +
                    "Initialization must have been unsuccessful.");
        }
        System.out.println("Returning " + annotations.size() + " lookup entries");
        return annotations;
    }

    /**
     * This method is the main logic for the QTLServiceImpl servlet.
     * The purpose of this method is to run the analysis workflow for doing
     * QTL Narrowing.<P>
     * There are serveral steps included in this process including:
     * <UL>
     * <LI> Finding Smallest Common Regions of the QTL inputset</LI>
     * <LI> Haplotype Analysis of regions</LI>
     * <LI> Acquisition of SNP Annotations (location, function, assoicated gene)</LI>
     * <LI> Microarray analysis of resulting set of genes</LI>
     * </UL>
     * The results of this analysis are quite sizable and are stored in the
     * classes' session object.  It is expected that the user will then fetch
     * only fetch the portions of the results needed for display at any given
     * point in time.
     * @param qtls
     * @return A trimmed down, flattened version of the results are returned
     *   instead of the entire data structure.  The real object representing
     *   the full results is stored in the session object.
     * @throws SMSException
     */
    public Map<String,List<Map<String,Object>>> narrowQTLs(List<List> qtls,
            boolean doGEX, String gexExp)
            throws SMSException {
        try {
            System.out.println("In narrowQTLs");
            //  The actual logic for running the analysis are in a separate
            //  classes from the actual servlet.  I did this because the servlet
            //  class is shared by all users, and I wanted to use class
            //  attributes that were not shared.
            this.setNarrowingStatus("Initializing...");

            // Take the 2D list of values and convert to a QTLSet object
            QTLSet qtlSet = new QTLSet();
            for (List qtl : qtls) {
                try {
                    qtlSet.addQTL(qtl);
                } catch (InvalidChromosomeException e) {
                    throw new SMSException(e.getMessage());
                }
            }

            //  Now get the smallest common regions in our qtlSet
            this.setNarrowingStatus("Getting smallest common regions in mouse...");
            System.out.println("...getting Smallest Common Regions");
            SmallestCommonRegion scr = new SmallestCommonRegion(qtlSet);
            Map<String, List<Region>> regions = scr.getRegions();

            //  Now do haplotype mapping
            this.setNarrowingStatus("Doing haplotype analysis of Mouse regions...");
            System.out.println("...doing Haplotype Analysis");
            HaplotypeAnalyzer haplotypeAnalyzer = new HaplotypeAnalyzer(
                    this.cgdSNPLookup);
            try {
                haplotypeAnalyzer.doAnalysis(regions);
                System.out.println("after doAnalysis");
            } catch (Exception e) {
                e.printStackTrace();
                throw new SMSException(e.getMessage());
            }
            this.setNarrowingStatus("SNP Annotations and GEX...");

            // Now get annotations to the SNPs
            // Need a CGDSnpDB object...
            CGDSnpDB snpDb = new CGDSnpDB(QTLServiceImpl.db_host,
                QTLServiceImpl.db_name, QTLServiceImpl.db_user,
                QTLServiceImpl.db_password);

            // This loop serves two purposes:
            // 1) use the cgd snp db to pull the annotations for snps per region
            //    including location/function, and mgi gene accession ids
            // 2) Create a "trim" data structure to return to the user interface.
            Map<String, List<Region>> generic_results = regions;
            Map<String, List<ReturnRegion>> ret_results =
                    new HashMap<String, List<ReturnRegion>>();
            System.out.println("results have " + generic_results.keySet().size() +
                    " chromosomes");
            //  Get an ExpressionAnalyzer object for the gene experession analysis
            //  we'll do in this loop.
            ExpressionAnalyzer analyzeGEX = null;
            if (doGEX) {
                //  If LUNG Experiment
                if (gexExp.equals("lung"))
                    analyzeGEX = new ExpressionAnalyzer(probeSetLookup, mgiLookup,
                        lungIntensityLookup, lungStrainLookup);
                //  If LIVER Experiment
                else if (gexExp.equals("liver"))
                    analyzeGEX = new ExpressionAnalyzer(probeSetLookup, mgiLookup,
                        liverIntensityLookup, liverStrainLookup);
                else if (gexExp.equals("liverlf"))
                    analyzeGEX = new ExpressionAnalyzer(probeSetLookup, mgiLookup,
                        liverIntensityLookup, liverLowFatStrainLookup);
                else if (gexExp.equals("liverhf"))
                    analyzeGEX = new ExpressionAnalyzer(probeSetLookup, mgiLookup,
                        liverIntensityLookup, liverHighFatStrainLookup);
            }
            for (String chr : generic_results.keySet()) {
                List<Region> myRegions = generic_results.get(chr);
                // list of lists, where each row contains:
                //   region_key, qtls, num snps, genes
                List<ReturnRegion> regionReturn = new ArrayList<ReturnRegion>();
                System.out.println(chr + " has " + myRegions.size() + " regions.");
                DecimalFormat fmt = new DecimalFormat("#,##0");
                for (Region region : myRegions) {
                    ReturnRegion oneRegion = new ReturnRegion();
                    String region_key = new String();
                    region_key = fmt.format(region.getStart() * 1.0);
                    region_key += "-";
                    region_key += fmt.format(region.getEnd() * 1.0);
                    //String region_key = "" + region.getStart() + "-" +
                    //       region.getEnd();
                    oneRegion.setRegionKey(region_key);
                    oneRegion.setQtls(((OverlappingRegion) region).getQtls());
                    Integer snp_count = new Integer(0);
                    if (region.getSnps() != null) {
                        //  We return the regions and a count of the snps in region
                        snp_count = new Integer(region.getSnps().size());
                        List<Integer> snps = new ArrayList<Integer>();
                        Set<Map.Entry<Integer, SNP>> snp_positions =
                                region.getSnps().entrySet();
                        for (Map.Entry<Integer, SNP> snp : snp_positions) {
                            snps.add(snp.getValue().getBPPosition());
                        }
                        try {
                            this.setNarrowingStatus("Chr " + chr + ":" + region_key +
                                    " get SNP detail...");

                            // Pull SNP "details" from CGD SNP DB
                            List<List> details = snpDb.getSNPDetails(
                                    region.getChromosome(), snps);
                            OverlappingRegion oRegion =
                                    (OverlappingRegion) region;
                            //  Add details to our underlying data structure
                            oRegion.addSnpDetails(details);
                        } catch (SQLException sqle) {
                            throw new SMSException(sqle.getMessage());
                        }
                        // Once we have the SNP details for a region, then we can
                        // take the genes found, and use them to do expression
                        // anlysis
                        // TODO:  In the future add an "If" to determine if we are
                        // using one of our default experiments or a user uploaded
                        // experiment.
                        if (doGEX && region.getGenes() != null) {
                            try {
                                this.setNarrowingStatus("Do GEX analysis for chr " +
                                        chr + ":" + region_key + "...");
                                analyzeGEX.analyzeRegion((OverlappingRegion) region);
                            } catch (MathRuntimeException mre) {
                                mre.printStackTrace();
                                throw new SMSException(mre.getMessage());
                            }
                        }

                    }
                    oneRegion.setNumberSnps(snp_count);
                    oneRegion.setTotalNumSnps(region.getTotalNumSNPsInRegion());
                    Map<Integer, Gene> geneMap = region.getGenes();
                    List<Gene> genes = new ArrayList<Gene>();
                    if (geneMap != null && geneMap.size() > 0) {
                        Set<Map.Entry<Integer, Gene>> geneEntries = geneMap.entrySet();
                        for (Map.Entry<Integer, Gene> geneEntry : geneEntries) {
                            genes.add(geneEntry.getValue());
                        }
                    }
                    oneRegion.setGenes(genes);
                    regionReturn.add(oneRegion);
                }
                ret_results.put(chr, regionReturn);
            }
            // TODO:  Bag this if we determine there is no reason to come
            // back down to server to get more data..
            // Put our results in the session object so the user can get this
            // information back piecemeal with future calls.
            this.setNarrowingStatus("Caching results...");
            HttpSession session = this.getSession();
            //session.setAttribute("REGIONS", generic_results);
            session.setAttribute("REGIONS", ret_results);
            Set<Map.Entry<String,List<ReturnRegion>>> chrs = ret_results.entrySet();
            Map<String,List<Map<String,Object>>> results =
                    new HashMap<String,List<Map<String,Object>>>();
            for (Map.Entry<String,List<ReturnRegion>> chr:chrs) {
                List<ReturnRegion> chrRegions = (List<ReturnRegion>)chr.getValue();
                List<Map<String,Object>> chrRegionList =
                        new ArrayList<Map<String,Object>>();
                for (ReturnRegion region : chrRegions) {
                    Map<String,Object> regionMap = new HashMap<String, Object>();
                    regionMap.put("range", (String)region.getRegionKey());
                    QTLSet regQtlSet = (QTLSet) region.getQtls();
                    List<QTL> regQtls = regQtlSet.asList();
                    List<String> qtlNames = new ArrayList<String>();
                    for (QTL qtl: regQtls) {
                        qtlNames.add(qtl.getQtlID());
                    }
                    regionMap.put("qtls", qtlNames);
                    regionMap.put("totalSnpsInRegion",
                            (Integer)region.getTotalNumSNPsInRegion());
                    regionMap.put("selectedSnpCount",
                            (Integer) region.getNumberSnps());
                    List<Gene> genes = (List<Gene>) region.getGenes();
                    regionMap.put("geneCount", genes.size());
                    chrRegionList.add(regionMap);
                }
                results.put(chr.getKey(), chrRegionList);
            }

            this.setNarrowingStatus("Done!  Returning results...");
            System.out.println("Done in narrowQTLs, returning results! ");

            return results;
        } catch (SMSException sms) {
            throw sms;
        } catch (Exception e) {
            e.printStackTrace();
            throw new SMSException(e);
        }

    }

    /**
     * This is a call back method used to give the user interface the ability
     * to poll the status of the Narrowing analysis.
     * @return  A simple status string.
     */
    public void setNarrowingStatus(String status) {
        HttpSession session = this.getSession();
        session.setAttribute("NARROWING_STATUS", status);
    }

    /**
     * This is a call back method used to give the user interface the ability
     * to poll the status of the Narrowing analysis.
     * @return  A simple status string.
     */
    public String getNarrowingStatus() {
        HttpSession session = this.getSession();
        String status = (String)session.getAttribute("NARROWING_STATUS");
        if (status != null)
            return status;
        else
            return this.narrowingStatus;
    }

    /**
     * Get a region by Chromosome and range.
     *
     * This method has been written to reduce the amount of data flowing to
     * the client at any one time.  The data set can be very large for a single
     * analysis and cause problems with the browser.
     *
     */
    public ReturnRegion getRegion(String chromosome, String rangeKey) {
        ReturnRegion retRegion = null;
        HttpSession session = this.getSession();

        Map<String, List<ReturnRegion>> results =
                (Map<String, List<ReturnRegion>>)session.getAttribute("REGIONS");
        List<ReturnRegion> regions = results.get(chromosome);

        for (ReturnRegion region: regions) {
            if (region.getRegionKey().equals(rangeKey))
                retRegion = region;
        }

        return retRegion;

    }

    /**
     * This method is used to clear the "REGIONS" attribute in the session,
     * if the user has already run analysis.  This is the case where they
     * want to start a new analysis in the same session
     */
    public Boolean clearAnalysis() {
        System.out.println("Clearing session info");
        HttpSession session = this.getSession();
        Object old_results = session.getAttribute("REGIONS");

        session.setAttribute("NARROWING_STATUS", "Waiting...");
        if (old_results != null) {
            System.out.println("clearing regions attribute");
            session.removeAttribute("REGIONS");
            return new Boolean(true);
        } else {
            System.out.println("There was no REGION attribute");
            return new Boolean(false);
        }
    }


    /**
     * Get a region by Chromosome and range.
     *
     * This method has been written to reduce the amount of data flowing to
     * the client at any one time.  The data set can be very large for a single
     * analysis and cause problems with the browser.
     *
     */
    public List<Map<String,String>> searchPhenotypesForQTLs(String searchString) {
        List<Map<String,String>> results = null;
        try {
            System.out.println("In searchPhenotypesForQTLs");
            //Do a test query and make sure it's working
            SolrQuery solrQuery = new SolrQuery().setQuery("name:" + searchString + ", terms:" + searchString).
                    addSortField("chr_num",SolrQuery.ORDER.asc).addSortField("start",SolrQuery.ORDER.asc).
                    setFacet(true).setRows(10).
                    setFacetMinCount(1).setIncludeScore(true).
                    setFacetLimit(8).addFacetField("id").
                    addFacetField("symbol").
                    addFacetField("name").addFacetField("terms");
            System.out.println("built query " + solrQuery.toString());
            QueryResponse rsp = this.solrServer.query(solrQuery);
            System.out.println("Back from query");
            //Iterator<SolrDocument> iter = rsp.getResults().iterator();
            List<MGIQTL> beans = rsp.getBeans(MGIQTL.class);

            System.out.println("cycle through " + beans.size() + " results...");
            //while (iter.hasNext()) {
            if (beans.size() > 0) {
                results = new ArrayList<Map<String,String>>();
                
                for (Iterator<MGIQTL> iter = beans.iterator(); iter.hasNext();) {
                    MGIQTL resultDoc = iter.next();
                    Map qtl = new HashMap<String, String>();
                    
                    String id = (String) resultDoc.getId();
                    qtl.put("qtlid", id);
                    String chr = (String) resultDoc.getChromosome();
                    qtl.put("chr",chr);
                    Integer bp_start = (Integer) resultDoc.getBp_start();
                    qtl.put("start", bp_start);
                    Integer bp_end = (Integer) resultDoc.getBp_end();
                    qtl.put("end", bp_end);
                    String symbol = (String) resultDoc.getSymbol();
                    qtl.put("symbol", symbol);
                    String name = (String) resultDoc.getName();
                    qtl.put("name", name);
                    System.out.println(id + "," + chr + "," + bp_start + "-" + bp_end + "," + symbol + "," + name + "\n");
                    String[] terms = resultDoc.getMpterms();
                    String term_out = "";
                    if (terms != null && terms.length > 0) {
                        boolean first = true;
                        for (String term : terms) {
                            if (first)
                                first = false;
                            else
                                term_out = "\n" + term_out;
                            term_out += term;
                        }
                    }
                    qtl.put("terms",term_out);
                    results.add(qtl);
                }
                System.out.println("Returned first " + beans.size() + " limited to 10");

            }
            else {
                System.out.println("Nothing found");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("FAILURE DOING SOLR QUERY!");
            
        }

        return results;

    }

    /**
     * Returns the current session
     *
     * @return  The current Session
     */
    private HttpSession getSession() {
        // Get the current request and then return its session
        return this.getThreadLocalRequest().getSession();
    }

}