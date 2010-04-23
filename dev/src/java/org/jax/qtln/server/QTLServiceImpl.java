package org.jax.qtln.server;



import org.jax.qtln.regions.InvalidChromosomeException;
import org.jax.qtln.regions.QTLSet;
import org.jax.qtln.regions.Region;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.jax.qtln.client.QTLService;
import org.jax.qtln.client.SMSException;

/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class QTLServiceImpl extends RemoteServiceServlet implements
        QTLService {

    private String narrowingStatus = "Waiting";

    //  The next two variables should be passed in as parameters to the
    //  servlet config.
    public static final String snpDirName = "/Users/dow/Documents/workspace/QTLNarrowing/data/CGD/imputed";
    //  TODO: come up with a clever way of dealing with file name with changing
    //  version and build
    public static final String imputedSnpsBaseName =
            "chr(\\d\\d?|[XYxy])_.*\\.txt";

    private static HashMap<String, SNPFile> cgdSNPLookup;

    public void init() throws ServletException {
        System.out.println("######  DOING QTLNARROWING CUSTOM INITIALIZATION ######");
        cgdSNPLookup = new HashMap<String, SNPFile>();
      File snpDir = new File(snpDirName);
      if (! snpDir.exists())
          throw new UnavailableException("Cannot fine CGD SNP files.  " +
                  "Directory does not exist: " + snpDirName);
      if (! snpDir.isDirectory())
          throw new UnavailableException("Not a directory: " + snpDirName);

      String[] snpFileNames = snpDir.list();
      if (snpFileNames.length == 0)
          throw new UnavailableException("Directory empty: " + snpDirName);
      Pattern snpNamePattern = Pattern.compile(QTLServiceImpl.imputedSnpsBaseName);
      //  we expect files for Chr 1-19 and X
      int chr_count = 0;
      System.out.println("INIT: Cycle through files in dir, load snp files...");
      for (String snpFileName : snpFileNames) {
          File snpFile = new File(snpFileName);
          System.out.println("INIT: Creating SNPFile Object");
          SNPFile result = new SNPFile(snpDir, snpFile, snpNamePattern);
          if (result.valid()) {
              System.out.println("INIT: Loading SNP file for chromosome " + result.getChromosome());
              try {
                result.load();
              } catch (IOException ioe) {
                  System.out.println("INIT:  FAILURE DUE TO IO ISSUE");
                  ioe.printStackTrace();
                  throw new UnavailableException(
                          "Problem reading CGD Imputed SNP File: " +
                          ioe.getMessage());
              }
              chr_count += 1;
              System.out.println("INIT: Chromosome loaded " + chr_count);
              cgdSNPLookup.put(result.getChromosome(), result);
              System.out.println(result.getDetails());
          }
      }
      //if (chr_count < 1)
      //    throw new UnavailableException("Expected 20 Chromosome files in CGD " +
      //            "data directory (" + snpDirName + "), only found " + chr_count);
      System.out.println("Custom Initialization Complete!");
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


    public Map<String, List<Region>> narrowQTLs(List<List> qtls)
            throws SMSException
    {
        System.out.print("In narrowQTLs");
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
        System.out.print("...getting Smallest Common Regions");
        SmallestCommonRegion scr = new SmallestCommonRegion(qtlSet);
        Map<String, List<Region>> regions = scr.getRegions();

        //  Now do haplotype mapping
        this.narrowingStatus = "Doing haplotype analysis of Mouse regions...";
        System.out.print("...doing Haplotype Analysis");
        HaplotypeAnalyzer haplotypeAnalyzer = new HaplotypeAnalyzer(this.cgdSNPLookup);
        haplotypeAnalyzer.doAnalysis(regions);


        this.narrowingStatus = "Done!";
        System.out.print("Done in narrowQTLs, returning results!");

        Map<String, List<Region>> generic_results = (Map<String, List<Region>>)regions;
        return generic_results;

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