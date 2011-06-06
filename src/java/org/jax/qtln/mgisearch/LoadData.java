/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jax.qtln.mgisearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

/**
 *
 * @author dave
 */
public class LoadData {
    // Temporary variables for the mgi files to load.  these should be 
    // downloaded dynamically from MGI
    String data_dir = "/Users/dave/QNT_Project/data";
    String markers_file = "MRK_Dump2.rpt.txt";
    String qtl2mp_file = "MGI_PhenoGenoMP.rpt.txt";
    String mp_file = "MGI_PhenoGenoMP.rpt.txt";
 
    public LoadData (SolrServer server)
    {
        try {
            // Clean out the existing index first
            server.deleteByQuery( "*:*" );
            
            // Get QTLs from markers_file
            String error_message = "";
            HashMap qtls = getQTLs();
            HashMap mpids = getMP();
            qtls = getQTL2MP(qtls, mpids);
            Collection<SolrInputDocument> docs = new ArrayList<SolrInputDocument>();
        } catch (SolrServerException sse) {
        } catch (IOException ioe) {
        }

   }
    
    
    private HashMap getQTLs()
            throws IOException {

        HashMap qtls = new HashMap();
        File qtlFile = new File(this.data_dir + File.separator
                + this.markers_file);
        if (!qtlFile.exists()) {
            throw new IOException("Cannot find QTL Data File.  "
                    + "File does not exist: " + qtlFile.getAbsolutePath());
        }

        FileReader fileReader = new FileReader(qtlFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        // loop through lines of the file.  Separating each line
        // into a QTL.
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split("\t");
            if (cols[5].equals("QTL")) {
                ArrayList qtl = new ArrayList();
                qtl.add(cols[0]);
                qtl.add(cols[4]);
                qtl.add(cols[3].trim());
                qtl.add(cols[1].trim());
                qtl.add(cols[2].trim());
                ArrayList mp_terms = new ArrayList();
                HashMap qtl_dict = new HashMap();
                qtl_dict.put("qtl", qtl);
                qtl_dict.put("mp", mp_terms);
                qtls.put(cols[0], qtl_dict);

            }

        }
        // Close the file readers, we're all done with them.
        bufferedReader.close();
        fileReader.close();

        return qtls;
    }

     private HashMap getMP()
            throws IOException {

        File mpFile = new File(this.data_dir + File.separator
                + this.mp_file);
        if (!mpFile.exists()) {
            throw new IOException("Cannot find MP Data File.  "
                    + "File does not exist: " + mpFile.getAbsolutePath());
        }

        FileReader fileReader = new FileReader(mpFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        HashMap mpids = new HashMap();
        // loop through lines of the file.  Separating each line
        // into a QTL.
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split("\t");
            String[] fields = new String[3];
            fields[0] = cols[0];
            fields[1] = cols[1];
            fields[2] = cols[2];
            mpids.put(cols[0], fields);
        }
        // Close the file readers, we're all done with them.
        bufferedReader.close();
        fileReader.close();

        return mpids;
    }
   
    private HashMap getQTL2MP(HashMap qtls, HashMap mpids)
            throws IOException {

        File qtl2mpFile = new File(this.data_dir + File.separator
                + this.qtl2mp_file);
        if (!qtl2mpFile.exists()) {
            throw new IOException("Cannot find QTL2MP Data File.  "
                    + "File does not exist: " + qtl2mpFile.getAbsolutePath());
        }

        FileReader fileReader = new FileReader(qtl2mpFile);
        BufferedReader bufferedReader = new BufferedReader(fileReader);

        // loop through lines of the file.  Separating each line
        // into a QTL.
        String line = "";
        while ((line = bufferedReader.readLine()) != null) {
            String[] cols = line.split("\t");
            String qtlid = cols[5];
            
            if (qtls.containsKey(qtlid)) {
                HashMap qtl_dict = (HashMap)qtls.get(qtlid);
                String mpid = cols[3];
                if (mpids.containsKey(mpid)) {
                    ArrayList mp_terms = (ArrayList)qtl_dict.get("mp");
                    mp_terms.add((String[])mpids.get(mpid));
                }
            }

        }
        // Close the file readers, we're all done with them.
        bufferedReader.close();
        fileReader.close();

        return qtls;
    }
}
