/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Pattern;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;

/**
 *
 * @author dow
 */
public class QTLServletContextListener implements ServletContextListener {

    //  This is temporary until I determine if we are better off with
    //  in-memory snps or database storage for snps (memory vs. performance
    //  trade-off
    public static final String SNP_STORAGE = "MEM";

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

    private HashMap<String, SNPFile> cgdSNPLookup;

    
    public void contextInitialized(ServletContextEvent event) {
        ServletContext sc = event.getServletContext();

        sc.log("######  DOING QTLNARROWING CUSTOM INITIALIZATION in Listener ######");
        sc.setAttribute("SNP_INIT_STATUS", "INIT");

        if (QTLServletContextListener.SNP_STORAGE.equals("DB")) {
            //  Set up a connection pool or something....
            boolean success = false;
            try {
                Class.forName(driver).newInstance();
                sc.log("Loaded the appropriate driver");
                success = true;
            } catch (ClassNotFoundException cnfe) {
                sc.log("\nUnable to load the JDBC driver " + driver);
                sc.log("Please check your CLASSPATH.");
                cnfe.printStackTrace(System.err);
            } catch (InstantiationException ie) {
                sc.log("\nUnable to instantiate the JDBC driver " + driver);
                ie.printStackTrace(System.err);
            } catch (IllegalAccessException iae) {
                sc.log("\nNot allowed to access the JDBC driver " + driver);
                iae.printStackTrace(System.err);
            }

            if (!success)
                sc.setAttribute("SNP_INIT_STATUS", "FAIL");
            else
                sc.setAttribute("SNP_INIT_STATUS", "SUCCESS");

        } else {
            boolean success = true;
            try {
                this.cgdSNPLookup = initSnpDB(sc);
            } catch (ServletException se) {
                sc.log(se.getMessage());
                success = false;
            }
            if (! success)
                sc.setAttribute("SNP_INIT_STATUS", "FAIL");
            else {
                sc.setAttribute("SNP_INIT_STATUS", "SUCCESS");
                sc.setAttribute("snpLookup", this.cgdSNPLookup);
            }

        }

    }

    public void contextDestroyed(ServletContextEvent event) {
    }


    private HashMap<String, SNPFile> initSnpDB(ServletContext sc)
            throws ServletException
    {
        HashMap<String, SNPFile> snpLookup = new HashMap<String, SNPFile>();
        File snpDir = new File(snpDirName);
        if (!snpDir.exists()) {
            throw new UnavailableException("Cannot fine CGD SNP files.  " +
                    "Directory does not exist: " + snpDirName);
        }
        if (!snpDir.isDirectory()) {
            throw new UnavailableException("Not a directory: " + snpDirName);
        }

        String[] snpFileNames = snpDir.list();
        if (snpFileNames.length == 0) {
            throw new UnavailableException("Directory empty: " + snpDirName);
        }
        Pattern snpNamePattern = Pattern.compile(QTLServiceImpl.imputedSnpsBaseName);
        //  we expect files for Chr 1-19 and X
        int chr_count = 0;
        sc.log("INIT: Cycle through files in dir, load snp files...");
        for (String snpFileName : snpFileNames) {
            File snpFile = new File(snpFileName);
            sc.log("INIT: Creating SNPFile Object");
            SNPFile result = new SNPFile(snpDir, snpFile, snpNamePattern);
            if (result.valid()) {
                sc.log("INIT: Loading SNP file for chromosome " + result.getChromosome());
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

}
