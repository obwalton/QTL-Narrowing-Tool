/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jax.qtln.mgisearch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.UnavailableException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;

/**
 *
 * @author dave
 */
public class QTLPhenotypeLoader {
    // Temporary variables for the mgi files to load.  these should be 
    // downloaded dynamically from MGI

    private String data_dir = "/Users/dave/QNT_Project/data";
    private String mgi_ftp_url = null;
    private String reports_dir = null;
    private String markers_file = "MGI_Coordinate.rpt";
    private String qtl2mp_file = "MGI_PhenoGenoMP.rpt";
    private String mp_file = "VOC_MammalianPhenotype.rpt";
    private String ref_file = "MRK_Reference.rpt";
    private SolrServer server = null;
    private ServletContext sc = null;
    
    public QTLPhenotypeLoader(SolrServer server, String url, String rep_dir,
            String markers_file, String qtl2mp_file, String mp_file, String ref_file) {
        this.server = server;
        this.mgi_ftp_url = url;
        this.reports_dir = rep_dir;
        this.markers_file = markers_file;
        this.qtl2mp_file = qtl2mp_file;
        this.mp_file = mp_file;
        this.ref_file = ref_file;
    }

    public SolrServer getLoadedServer(ServletContext sc)
            throws UnavailableException
    {
        this.sc = sc;
        FTPClient ftp = new FTPClient();
        try {
            int reply;
            ftp.connect(this.mgi_ftp_url);

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new UnavailableException("Problem connecting to MGI server! ");
            }
            //  TODO: replace this with a "QTL Narrowing" specific mail addr
            sc.log("logging in...");
            ftp.login("anonymous", "cbr-help@jax.org");
            sc.log("changing directory...");
            ftp.cwd(this.reports_dir);
            sc.log(ftp.printWorkingDirectory());

            sc.log("Fetch " + this.markers_file);
            InputStream inStream = ftp.retrieveFileStream(this.markers_file);
            if (inStream == null)
                throw new UnavailableException("marker file inputstream is null!");
            sc.log("created buffered reader");
            BufferedReader bufferedReader =
                    new BufferedReader(new InputStreamReader(inStream));

            // Get QTLs from markers_file
            sc.log("Get QTLs");
            HashMap qtls = getQTLs(bufferedReader);
            sc.log("found " + qtls.size() + " qtls");
            bufferedReader.close();
            inStream.close();
            ftp.logout();

            reply = 0;
            ftp.connect(this.mgi_ftp_url);

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new UnavailableException("Problem connecting to MGI server! ");
            }
            //  TODO: replace this with a "QTL Narrowing" specific mail addr
            ftp.login("anonymous", "cbr-help@jax.org");
            ftp.cwd(this.reports_dir);
            sc.log(ftp.printWorkingDirectory());


            InputStream mpInStream = ftp.retrieveFileStream(this.mp_file);
            if (mpInStream == null) {
                sc.log("mp file input stream is null! what gives?");
                System.out.println("TESTING ...1...2...3");
                throw new UnavailableException("mp file inputstream is null!");
            }
            bufferedReader =
                    new BufferedReader(new InputStreamReader(mpInStream));

            HashMap mpids = getMP(bufferedReader);
            sc.log("After getMP - " + mpids.size() + " MP Terms");
            bufferedReader.close();
            mpInStream.close();
            ftp.logout();

            reply = 0;
            ftp.connect(this.mgi_ftp_url);

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new UnavailableException("Problem connecting to MGI server! ");
            }
            //  TODO: replace this with a "QTL Narrowing" specific mail addr
            ftp.login("anonymous", "cbr-help@jax.org");
            ftp.cwd(this.reports_dir);
            sc.log(ftp.printWorkingDirectory());

            InputStream q2mInStream = ftp.retrieveFileStream(this.qtl2mp_file);
            bufferedReader =
                    new BufferedReader(new InputStreamReader(q2mInStream));
            qtls = getQTL2MP(bufferedReader, qtls, mpids);
            sc.log("after getQTL2MP - " + qtls.size() + " QTLS");
            bufferedReader.close();
            q2mInStream.close();

            ftp.logout();

            reply = 0;
            ftp.connect(this.mgi_ftp_url);

            // After connection attempt, you should check the reply code to verify
            // success.
            reply = ftp.getReplyCode();

            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect();
                throw new UnavailableException("Problem connecting to MGI server! ");
            }
            //  TODO: replace this with a "QTL Narrowing" specific mail addr
            ftp.login("anonymous", "cbr-help@jax.org");
            ftp.cwd(this.reports_dir);
            sc.log(ftp.printWorkingDirectory());


            InputStream refInStream = ftp.retrieveFileStream(this.ref_file);
            if (refInStream == null) {
                sc.log("ref file input stream is null! what gives?");
                System.out.println("TESTING ...1...2...3");
                throw new UnavailableException("ref file inputstream is null!");
            }
            bufferedReader =
                    new BufferedReader(new InputStreamReader(refInStream));

            qtls = getRefs(bufferedReader, qtls);
            sc.log("After getRefs - " + qtls.size() + " QTLs");
            bufferedReader.close();
            refInStream.close();
            ftp.logout();


            
            loadServer(qtls, sc);

        } catch (SolrServerException sse) {
            sc.log(sse.getMessage());
            StackTraceElement[] trace = sse.getStackTrace();
            for (StackTraceElement element: trace) {
                sc.log(element.toString());
            }

            throw new UnavailableException("Problem loading Solr Server: " 
                    + sse.getMessage());
        } catch (IOException ioe) {
            throw new UnavailableException("Problem getting MGI Data: "
                    + ioe.getMessage());
        } finally {
            try {
                ftp.disconnect();
            } catch (IOException ioe) {
                // do nothing
            }
        }

        return server;
    }

    private HashMap getQTLs(BufferedReader bufferedReader)
            throws IOException {
        HashMap qtls = new HashMap();
        /* 
        File qtlFile = new File(this.data_dir + File.separator
        + this.markers_file);
        if (!qtlFile.exists()) {
        throw new IOException("Cannot find QTL Data File.  "
        + "File does not exist: " + qtlFile.getAbsolutePath());
        }
        
        FileReader fileReader = new FileReader(qtlFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
         */

        // loop through lines of the file.  Separating each line
        // into a QTL.
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split("\t");
            if (cols[1].equals("QTL")) {
                ArrayList qtl = new ArrayList();
                // THis was the order of the old MGI_Coordinate report
                // 1=mgiid, 2=mgitype, 3=symbol, 4=name, 29=chr, 30=start,31=end
                qtl.add(cols[0].trim());
                qtl.add(cols[28].trim());
                qtl.add(cols[29].trim());
                qtl.add(cols[30].trim());
                qtl.add(cols[2].trim());
                qtl.add(cols[3].trim());
                // MGI Discontinued this report and replaced it with the
                // MGI_MRK_Coord.rpt.  Following are the columns:
                // 1= mgiid, 2=mgitype, 4=symbol, 5=name, 6=chr, 7=start, 8=end
                //qtl.add(cols[0].trim());
                //qtl.add(cols[5].trim());
                //qtl.add(cols[6].trim());
                //qtl.add(cols[7].trim());
                //qtl.add(cols[3].trim());
                //qtl.add(cols[4].trim());
                ArrayList mp_terms = new ArrayList();
                ArrayList ref_ids = new ArrayList();
                HashMap qtl_dict = new HashMap();
                qtl_dict.put("qtl", qtl);
                qtl_dict.put("mp", mp_terms);
                qtl_dict.put("ref", ref_ids);
                qtls.put(cols[0], qtl_dict);

            }

        }
        // Close the file readers, we're all done with them.
        //bufferedReader.close();
        //fileReader.close();

        return qtls;
    }

    private HashMap getMP(BufferedReader bufferedReader)
            throws IOException {

        /*File mpFile = new File(this.data_dir + File.separator
        + this.mp_file);
        if (!mpFile.exists()) {
        throw new IOException("Cannot find MP Data File.  "
        + "File does not exist: " + mpFile.getAbsolutePath());
        }
        
        FileReader fileReader = new FileReader(mpFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
         */

        HashMap mpids = new HashMap();
        // loop through lines of the file.  Separating each line
        // into a QTL.
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split("\t");

            String term_text = new String();
            for (String col : cols) {
                term_text += col + " ";
            }

            mpids.put(cols[0], term_text);
        }
        // Close the file readers, we're all done with them.
        //bufferedReader.close();
        //fileReader.close();

        return mpids;
    }

    private HashMap getQTL2MP(BufferedReader bufferedReader, HashMap qtls,
            HashMap mpids)
            throws IOException {

        /*File qtl2mpFile = new File(this.data_dir + File.separator
        + this.qtl2mp_file);
        if (!qtl2mpFile.exists()) {
        throw new IOException("Cannot find QTL2MP Data File.  "
        + "File does not exist: " + qtl2mpFile.getAbsolutePath());
        }
        
        FileReader fileReader = new FileReader(qtl2mpFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
         */
        // loop through lines of the file.  Separating each line
        // into a QTL.
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split("\t");
            if (cols.length < 6) {
                continue;
            }
            String qtlid = cols[5];

            String[] q_list = qtlid.split(",");
            for (String q : q_list) {
                if (qtls.containsKey(q)) {
                    HashMap qtl_dict = (HashMap) qtls.get(q);
                    if (qtl_dict != null) {
                        String mpid = cols[3];
                        if (mpids.containsKey(mpid)) {
                            ArrayList mp_terms = (ArrayList) qtl_dict.get("mp");
                            mp_terms.add((String) mpids.get(mpid));
                        }
                    }
                }
            }

        }
        // Close the file readers, we're all done with them.
        //bufferedReader.close();
        //fileReader.close();

        return qtls;
    }

    private HashMap getRefs(BufferedReader bufferedReader, HashMap qtls)
            throws IOException {

        // loop through lines of the file.  Separating each line
        // into a QTL.
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split("\t");
            if (cols.length < 5)
                continue;
            String qtlid = cols[0];

            if (qtls.containsKey(qtlid)) {
                HashMap qtl_dict = (HashMap) qtls.get(qtlid);
                String[] refs = cols[4].split("|");
                ArrayList ref_ids = (ArrayList) qtl_dict.get("ref");
                for (String ref : refs) {
                    ref_ids.add(ref); 
                }
                qtl_dict.put("ref",ref_ids);
            }

        }

        return qtls;
    }

    private void loadServer(HashMap qtls, ServletContext sc)
            throws SolrServerException, IOException {
        // Clean out the existing index first
        sc.log("Clear server");
        server.deleteByQuery("*:*");

        sc.log("cycle through qtls and create beans");
        Set keys = qtls.keySet();
        List<MGIQTL> beans = new ArrayList<MGIQTL>();
        //  Cycle through the qtls and create beans to load to the
        //  solr server.
        for (Iterator i = keys.iterator(); i.hasNext();) {
            String qtl = (String) i.next();
            HashMap qtl_dict = (HashMap) qtls.get(qtl);
            ArrayList qtl_detail = (ArrayList) qtl_dict.get("qtl");
            ArrayList mp_terms = (ArrayList) qtl_dict.get("mp");
            ArrayList ref_ids = (ArrayList) qtl_dict.get("ref");
            MGIQTL mgiqtl = new MGIQTL();
            mgiqtl.setId((String) qtl_detail.get(0));
            mgiqtl.setChromosome((String) qtl_detail.get(1));
            try {
                mgiqtl.setBp_start(new Integer((String) qtl_detail.get(2)));
                mgiqtl.setBp_end(new Integer((String) qtl_detail.get(3)));
            } catch (Exception e) {
                continue;
                // The start or end weren't numeric, skip
            }
            mgiqtl.setSymbol((String) qtl_detail.get(4));
            mgiqtl.setName((String) qtl_detail.get(5));

            String[] terms = new String[mp_terms.size()];
            for (int j = 0; j < mp_terms.size(); j++) {
                String term = (String) mp_terms.get(j);
                terms[j] = term;
            }
            mgiqtl.mpterms = terms;
            
            String[] refids = new String[ref_ids.size()];
            for (int j = 0; j < ref_ids.size(); j++) {
                String ref = (String) ref_ids.get(j);
                refids[j] = ref;
            }
            mgiqtl.refids = refids;
            beans.add(mgiqtl);
        }
        sc.log("Adding " + beans.size() + " QTLs to SOLr server!");
        this.server.addBeans(beans);
        sc.log("Beans added");
        this.server.commit();
        sc.log("server commited");

    }

    /*public static void main(String[] args) {
        // Main method for testing...
        PrintStream sc = System.out;
        //  Set up the Solr server
        String MGI_FTP_ADDR = "ftp.informatics.jax.org";
        String MGI_REPORTS_DIR = "pub/reports";
        String SOLR_HOME =
            "/Users/dow/Documents/workspace/QTLN/apache-solr-1.4.1/example/solr";
        String MGI_MARKERS_FILE = "MGI_Coordinate.rpt";
        String MGI_QTL2MP_FILE = "MGI_PhenoGenoMP.rpt";
        String MGI_MP_FILE = "VOC_MammalianPhenotype.rpt";
        SolrServer server;
        try {
            // Note that the following property could be set through JVM level arguments too
            System.setProperty("solr.solr.home", SOLR_HOME);
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            CoreContainer coreContainer = initializer.initialize();
            server = new EmbeddedSolrServer(coreContainer, "");
            QTLPhenotypeLoader loadMGIServer = new QTLPhenotypeLoader(server,
                    MGI_FTP_ADDR, MGI_REPORTS_DIR,
                    MGI_MARKERS_FILE, MGI_QTL2MP_FILE,
                    MGI_MP_FILE);
            server = loadMGIServer.getLoadedServer(sc);
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


    }*/
}
