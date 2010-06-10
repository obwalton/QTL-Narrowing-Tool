package org.jax.qtln.server;



import org.jax.qtln.regions.InvalidChromosomeException;
import org.jax.qtln.regions.QTLSet;
import org.jax.qtln.regions.Region;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.jax.qtln.client.QTLService;
import org.jax.qtln.client.SMSException;
import org.jax.qtln.db.CGDSnpDB;
import org.jax.qtln.regions.OverlappingRegion;
import org.jax.qtln.regions.SNP;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class QTLServiceImpl extends RemoteServiceServlet implements
        QTLService {

    private String narrowingStatus = "Waiting";

    //  This is temporary until I determine if we are better off with
    //  in-memory snps or database storage for snps (memory vs. performance
    //  trade-off
    public static final String SNP_STORAGE = "DB";

    //  These are all for the database stored version of the CGD SNPS
    private String driver = "com.mysql.jdbc.Driver";

    //  This is a shared database connection for DB Storage of SNPS
    //private static Connection conn;

    //  The next two variables should be passed in as parameters to the
    //  servlet config.
    public static final String snpDirName = "/Users/dow/Documents/workspace/QTLNarrowing/data/CGD/imputed";
    //  TODO: come up with a clever way of dealing with file name with changing
    //  version and build
    public static final String imputedSnpsBaseName =
            "chr(\\d\\d?|[XYxy])_.*\\.txt";

    private static HashMap<String, SNPFile> cgdSNPLookup;
    private static Map<Integer,String> snpLocFuncs;

    public void init() throws ServletException {
        ServletContext context = this.getServletContext();
        String status = (String)context.getAttribute("SNP_INIT_STATUS");
        System.out.println("INITIALIZATION = " + status);
        cgdSNPLookup = (HashMap<String, SNPFile>)context.getAttribute("snpLookup");
        //  Get our location function lookup
        CGDSnpDB snpDb = new CGDSnpDB();
        try {
            if (this.snpLocFuncs == null) {
                this.snpLocFuncs = snpDb.getSNPLocFuncList();
            }
        } catch (SQLException sqle) {
            throw new ServletException(sqle.getMessage());
        }
    }


    public List<String[]> readQTLFile()
            throws SMSException {

        System.out.println("IN readQTLFile");
        // Check the HttpSession to see if a sequence file was uploaded
        HttpSession session = this.getSession();
        HttpServletRequest hsr = this.getThreadLocalRequest();
        System.out.println("Got session info");

        //  The actual logic for running the analysis is in a separate
        //  class from the actual servlet.  I did this because the servlet
        //  class is shared by all users, and I wanted to use class attributes
        //  that were not shared.
        QTLFileReader qfr = new QTLFileReader();
        System.out.println("Got reader, now read");
        return qfr.readQTLFile(session, hsr);

    }


    public Map<String, Map<String,Integer>> narrowQTLs(List<List> qtls)
            throws SMSException
    {
        System.out.println("In narrowQTLs");
        //  The actual logic for running the analysis is in a separate
        //  class from the actual servlet.  I did this because the servlet
        //  class is shared by all users, and I wanted to use class attributes
        //  that were not shared.
        this.narrowingStatus = "Initializing...";

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
        this.narrowingStatus = "Getting smallest common regions in mouse...";
        System.out.println("...getting Smallest Common Regions");
        SmallestCommonRegion scr = new SmallestCommonRegion(qtlSet);
        Map<String, List<Region>> regions = scr.getRegions();

        //  Now do haplotype mapping
        this.narrowingStatus = "Doing haplotype analysis of Mouse regions...";
        System.out.println("...doing Haplotype Analysis");
        HaplotypeAnalyzer haplotypeAnalyzer = new HaplotypeAnalyzer(this.cgdSNPLookup);
        try {
            haplotypeAnalyzer.doAnalysis(regions);
            System.out.println("after doAnalysis");
        } catch (Exception e) {
            System.out.println("Caught an exception ");
            e.printStackTrace();
            throw new SMSException(e.getMessage());
        }
        this.narrowingStatus = "Getting SNP Annotations...";

        // Now get annotations to the SNPs
        // Need a CGDSnpDB object...
        CGDSnpDB snpDb = new CGDSnpDB();

        //Map<String, List<Region>> generic_results = (Map<String, List<Region>>)regions;
        Map<String, List<Region>> generic_results = regions;
        Map<String, Map<String, Integer>> ret_results = new HashMap<String, Map<String, Integer>>();
        System.out.println("results have " + generic_results.keySet().size() + " chromosomes");
        for (String chr:generic_results.keySet()) {
            List<Region> myRegions = generic_results.get(chr);
            Map regionMap = new HashMap<String, Integer>();
            System.out.println(chr + " has " + myRegions.size() + " regions.");
            for (Region region: myRegions) {
                String region_key = "" + region.getStart() + "-" + region.getEnd();
                Integer snp_count = new Integer(0);
                if (region.getSnps() != null) {
                    snp_count = new Integer(region.getSnps().size());
                    List<Integer> snps = new ArrayList<Integer>();
                    Set<Map.Entry<Integer,SNP>> snp_positions = region.getSnps().entrySet();
                    for (Map.Entry<Integer,SNP> snp:snp_positions) {
                        snps.add(snp.getValue().getBPPosition());
                    }
                    try {
                        List<List> details =
                            snpDb.getSNPDetails(region.getChromosome(), snps);
                        OverlappingRegion oRegion = (OverlappingRegion)region;
                        oRegion.addSnpDetails(details);
                    } catch (SQLException sqle) {
                        throw new SMSException(sqle.getMessage());
                    }
                }
                regionMap.put(region_key, snp_count);
            }
            ret_results.put(chr, regionMap);
        }
        this.narrowingStatus = "Caching results...";
        HttpSession session = this.getSession();
        session.setAttribute("REGIONS", generic_results);

        this.narrowingStatus = "Done!";
        System.out.println("Done in narrowQTLs, returning results! ");

        return ret_results;

    }

    public String getNarrowingStatus() {
        return this.narrowingStatus;
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