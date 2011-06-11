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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.xml.sax.SAXException;

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
    String mp_file = "VOC_MammalianPhenotype.rpt.txt";
 
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
            Set keys = qtls.keySet();
            List<MGIQTL> beans = new ArrayList<MGIQTL>();
            for (Iterator i = keys.iterator(); i.hasNext(); ) {
                String qtl = (String)i.next();
                HashMap qtl_dict = (HashMap)qtls.get(qtl);
                ArrayList qtl_detail = (ArrayList)qtl_dict.get("qtl");
                ArrayList mp_terms = (ArrayList)qtl_dict.get("mp");
                MGIQTL mgiqtl = new MGIQTL();
                mgiqtl.id = (String)qtl_detail.get(0);
                mgiqtl.setChromosome((String)qtl_detail.get(1));
                mgiqtl.centimorgans = new Float((String)qtl_detail.get(2));
                mgiqtl.symbol = (String)qtl_detail.get(3);
                mgiqtl.name = (String)qtl_detail.get(4);
                
                String[] terms = new String[mp_terms.size()];
                for (int j = 0; j < mp_terms.size(); j++) {
                    String term = (String)mp_terms.get(j);
                    terms[j] = term;
                }
                mgiqtl.mpterms = terms;
                //  Skip all QTLs with a cM < 0.0 (ie. -1.0)
                if (mgiqtl.centimorgans >= 0.0)
                    beans.add(mgiqtl);
            }
            server.addBeans(beans);
            server.commit();
            
        } catch (SolrServerException sse) {
            sse.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
            
            String term_text = new String();
            for (String col : cols) {
                term_text += col + " ";
            }
            
            mpids.put(cols[0], term_text);
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
                    mp_terms.add((String)mpids.get(mpid));
                }
            }

        }
        // Close the file readers, we're all done with them.
        bufferedReader.close();
        fileReader.close();

        return qtls;
    }
    
    public static void main (String[] args) {
        SolrServer server;
        try {
            // Note that the following property could be set through JVM level arguments too
            System.setProperty("solr.solr.home", "/Users/dave/QNT_Project/apache-solr-1.4.1/example/solr");
            System.out.println("solr.solr.home system variable set");
            CoreContainer.Initializer initializer = new CoreContainer.Initializer();
            System.out.println("initializer created");
            CoreContainer coreContainer = initializer.initialize();
            System.out.println("core container created");
            server = new EmbeddedSolrServer(coreContainer, "");            //server = new CommonsHttpSolrServer( solrUrl );
            LoadData loadMGIServer = new LoadData(server);
            //SolrQuery solrQuery = new SolrQuery().setQuery("name:neoplasm, terms:neoplasms");
            SolrQuery solrQuery = new SolrQuery().setQuery("name:increased lung adenoma, terms:increased lung adenoma").
                    addSortField("chr_num",SolrQuery.ORDER.asc).addSortField("cm",SolrQuery.ORDER.asc).
                    setFacet(true).setRows(1000).
                    setFacetMinCount(1).
                    setFacetLimit(8).addFacetField("id").
                    addFacetField("symbol").
                    addFacetField("name").addFacetField("terms");
            QueryResponse rsp = server.query(solrQuery);
            //Iterator<SolrDocument> iter = rsp.getResults().iterator();
            List<MGIQTL> beans = rsp.getBeans(MGIQTL.class);

            //while (iter.hasNext()) {
            if (beans.size() > 0) {
                for (Iterator<MGIQTL> iter = beans.iterator(); iter.hasNext();) {
                    MGIQTL resultDoc = iter.next();

                    String id = (String) resultDoc.getId();
                    String chr = (String) resultDoc.getChromosome();
                    Float cm = (Float) resultDoc.getCentimorgans();
                    String symbol = (String) resultDoc.getSymbol();
                    String name = (String) resultDoc.getName();
                    System.out.print(id + "," + chr + "," + cm + "," + symbol + "," + name + "\n");
                    String[] terms = resultDoc.getMpterms();
                    if (terms != null && terms.length > 0) {
                        for (String term : terms) {
                            System.out.println("\t" + term);
                        }
                    }

                }
                System.out.println("Returned first " + beans.size() + " limited to 1000");

            }
            else {
                System.out.println("Nothing found");
            }
        } catch (SolrServerException sse) {
            sse.printStackTrace();
            System.out.println("Failed doing test query of solr instance");
        /*} catch (MalformedURLException mue) {
            mue.printStackTrace();
            System.out.println("Failed to create server");*/
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.print("Problem building our embedded server");
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (SAXException saxe) {
            saxe.printStackTrace();
        } catch (Exception e) {
            System.out.println("Catch all for the rest of the exceptions");
            e.printStackTrace();
        }

    }
}
