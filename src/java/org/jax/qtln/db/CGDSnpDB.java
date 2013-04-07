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

package org.jax.qtln.db;

import com.jolbox.bonecp.BoneCP;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 *
 * @author dow
 */
public class CGDSnpDB {

    private String driver = "com.mysql.jdbc.Driver";
    private String protocol = "jdbc:mysql:";
    private String dbHost = "cgddb.jax.org";
    private String dbPort = "44444";
    private String database = "cgdsnpdb";
    private String user = "pup";
    private String password = "puppass";
    public static final String[] chr_array = {"1", "2", "3", "4", "5", "6", "7",
        "8", "9","10","11", "12", "13", "14", "15", "16", "17", "18", "19", "X",
        "Y"};

    private BoneCP pool;
    //private Connection connection;
    private List chromosomes;


    public CGDSnpDB() {
        loadDriver();
        this.chromosomes = Arrays.asList(chr_array);
    }

    public CGDSnpDB(String dbHost, String dbPort, String database, String user,
            String password)
    {
        this();
        this.dbHost = dbHost;
        this.dbPort = dbPort;
        this.database = database;
        this.user = user;
        this.password = password;

    }

    public Connection getConnection() 
        throws SQLException, ClassNotFoundException
    {
        if (this.pool == null) {
            try {
                Properties props = new Properties(); // connection properties
                props.put("user", this.user);
                props.put("password", this.password);
                String dbUrl = this.protocol + "//" + dbHost + ":" + dbPort + "/" + database;
                //this.connection = DriverManager.getConnection(this.protocol +
                //        "//" + dbHost + ":" + dbPort + "/" + database, props);
                
                this.pool = ConnectionPoolFactory.createConnectionPool(driver,
                        dbUrl, user, password);
            } catch (SQLException sql) {
                printSQLException(sql);
                throw sql;
            } catch (ClassNotFoundException cnfe) {
                System.out.println("Cound not file class in ConnectionPoolFactory");
                throw cnfe;
            }

        }
        Connection c = pool.getConnection();

        return c;
    }
    
    public void shutdownConnectionPool() {
        ConnectionPoolFactory.shutdownDatabase(pool);
    }

    public static void executeSQL(Connection conn, String sql) 
        throws SQLException
    {
        Statement st = conn.createStatement();
        st.execute(sql);
        st.close();
    }
    
    

    
    /**
     * Loads the appropriate JDBC driver for this environment/framework. For
     * example, if we are in an embedded environment, we load Derby's
     * embedded Driver, <code>org.apache.derby.jdbc.EmbeddedDriver</code>.
     */
    private void loadDriver() {
        /*
         *  The JDBC driver is loaded by loading its class.
         *  If you are using JDBC 4.0 (Java SE 6) or newer, JDBC drivers may
         *  be automatically loaded, making this code optional.
         *
         *  In an embedded environment, this will also start up the Derby
         *  engine (though not any databases), since it is not already
         *  running. In a client environment, the Derby engine is being run
         *  by the network server framework.
         *
         *  In an embedded environment, any static Derby system properties
         *  must be set before loading the driver to take effect.
         */
        boolean success = false;
        try {
            Class.forName(driver).newInstance();
            System.out.println("Loaded the appropriate driver");
            success = true;
        } catch (ClassNotFoundException cnfe) {
            System.err.println("\nUnable to load the JDBC driver " + driver);
            System.err.println("Please check your CLASSPATH.");
            cnfe.printStackTrace(System.err);
        } catch (InstantiationException ie) {
            System.err.println(
                        "\nUnable to instantiate the JDBC driver " + driver);
            ie.printStackTrace(System.err);
        } catch (IllegalAccessException iae) {
            System.err.println(
                        "\nNot allowed to access the JDBC driver " + driver);
            iae.printStackTrace(System.err);
        }

        if (! success) {
            System.exit(1);
        }
    }

    /**
     * Prints details of an SQLException chain to <code>System.err</code>.
     * Details included are SQL State, Error code, Exception message.
     *
     * @param e the SQLException from which to print details.
     */
    public static void printSQLException(SQLException e)
    {
        e.printStackTrace();
        // Unwraps the entire exception chain to unveil the real cause of the
        // Exception.
        while (e != null)
        {
            System.err.println("\n----- SQLException -----");
            System.err.println("  SQL State:  " + e.getSQLState());
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  Message:    " + e.getMessage());
            // for stack traces, refer to derby.log or uncomment this:
            //e.printStackTrace(System.err);
            e = e.getNextException();
        }
    }

    /*
    public List<String> getImputedStrainList()
            throws SQLException
    {
        List<String> results = new ArrayList<String>();
        ResultSet rs = null;
        PreparedStatement query = null;
        //  Assumes source "Imputed" exists
        String imputed_strains = "select distinct st.strain_name " +
                "from snp_strain st, snp_strain_by_source ss, snp_source so " +
                "where st.strain_id = ss.strain_id " +
                "and ss.source_id = so.source_id " +
                "and so.source_name = 'Imputed'";
        try {
            Connection conn = getConnection();
            query = conn.prepareStatement(imputed_strains);
            rs = query.executeQuery();
            while(rs.next()) {
                results.add(rs.getString(1));

            }
            conn.close();
        } catch (SQLException sqle) {
            printSQLException(sqle);
            throw sqle;
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (query != null) {
                    query.close();
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }
        }
        return results;

    }*/

    public Map<Integer,String> getSNPLocFuncList() 
            throws SQLException
    {
        Map<Integer,String> results = new HashMap<Integer,String>();
        ResultSet rs = null;
        PreparedStatement query = null;
        String cmd = "SELECT _loc_func_key, description FROM snp_loc_func";
        try {
            Connection conn = getConnection();
            query = conn.prepareStatement(cmd);
            rs = query.executeQuery();
            while(rs.next()) {
                results.put(rs.getInt(1), rs.getString(2));
            }
            conn.close();
        } catch (SQLException sqle) {
            printSQLException(sqle);
            throw sqle;
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
        }  finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (query != null) {
                    query.close();
                }
            } catch (SQLException sqle) {
                printSQLException(sqle);
            }
        }
        return results;

    }

    public static class SNPDetailFetcher
            implements Callable {

        private Connection connection;
        private String chromosome;
        private List<Integer> bpPosList;
        private String source_code;

        public SNPDetailFetcher(Connection conn, String chromosome, List<Integer> bpPosList, String source_code) {
            this.source_code = source_code;
            this.connection = conn;
            this.chromosome = chromosome;
            //System.out.println("Contructing for chromosome " + chromosome);
            this.bpPosList = bpPosList;
        }

        /*public List<List> call1() throws SQLException {
            List<List> results = new ArrayList<List>();
            String tempTable = "create temporary table _bp_pos ( bp_position INTEGER NOT NULL PRIMARY KEY );";
            String detail_cmd = "select distinct s.snpid, sp.bp_position, "
                    + "st._loc_func_key, st.gene_id, g.gene_start, g.gene_end, "
                    + "(select mgi_geneid from cgd_genes_ensembl_mgi where gene_id = st.gene_id) mgi_geneid, "
                    + "(select marker_symbol from cgd_genes_ensembl_mgi where gene_id = st.gene_id) marker_symbol, "
                    + "(select marker_name from cgd_genes_ensembl_mgi where gene_id = st.gene_id) marker_name, "
                    + "sa.accession_id as rs_number, sa2.accession_id as provider_id, "
                    + "sas.source_name as provider "
                    + "from _bp_pos bp, snp_main s LEFT JOIN (snp_accession sa) ON "
                    + "(s.snpid = sa.snpid and sa.snpid_id_type = 1) "
                    + "LEFT JOIN (snp_accession sa2, snp_source sas) ON "
                    + "(s.snpid = sa2.snpid and sa2.snpid_id_type = 3 "
                    + "and sa2.source_id = sas.source_id), "
                    + "snp_position sp, snp_chromosome c, snp_by_source ss, "
                    + "snp_transcript st, cgd_genes g "
                    + "where chromosome_name = ? "
                    + "and c.chromosome_id = sp.chromosome_id "
                    + "and sp.bp_position = bp.bp_position "
                    + "and sp.snpid = s.snpid "
                    + "and s.snpid = ss.snpid "
                    + "and ss.source_id in (15, 21) "
                    + "and s.snpid = st.snpid "
                    + "and st.gene_id = g.gene_id "
                    + "order by sp.bp_position, s.snpid";
            System.out.println(detail_cmd);
            ResultSet rs = null;
            Statement statement = null;
            try {
                System.out.println("Creating connection");

                System.out.println("Creating temp table");
                executeSQL(connection, tempTable);

                System.out.println("Creating statement for insert");
                // insert data into temp table
                String sql = "insert into _bp_pos (bp_position) VALUE(?)";
                //PreparedStatement pstmt = conn.prepareStatement(sql);
                //for (Integer position: bpPosList) {
                //    pstmt.setInt(1, position);
                //    pstmt.executeUpdate();
                //}
                connection.setAutoCommit(false);
                PreparedStatement st = connection.prepareStatement(sql);

                System.out.println("Creating insert statements and adding to batch...");
                for (Integer position : bpPosList) {
                    st.setInt(1, position);
                    st.addBatch();
                }
                System.out.println("Executing " + bpPosList.size() + " Inserts.");
                long start = System.currentTimeMillis();
                st.executeBatch();
                //conn.commit();
                long second = System.currentTimeMillis();
                statement = connection.createStatement();
                rs = statement.executeQuery("select count(1) from _bp_pos");
                rs.next();
                int count = rs.getInt(1);
                System.out.println("inserted " + count + " rows");
                System.out.println("Inserts took: " + ((second - start) / 1000) + " seconds");
                //System.out.println("Executing batch inserts...");
                //int[] numInserted = st.executeBatch();
                //System.out.println("numInserted = " + numInserted.length);

                System.out.println("creating statement for query");
                PreparedStatement statement2 = connection.prepareStatement(detail_cmd);
                statement2.setFetchSize(Integer.MIN_VALUE);//statement.setFetchSize(10000);
                System.out.println("add chromosome to query " + chromosome);
                statement2.setString(1, chromosome);

                System.out.println("START: " + now_formatted());
                rs = statement2.executeQuery();
                System.out.println("END: " + now_formatted());
                long last = System.currentTimeMillis();
                System.out.println("Data added to result set in " + ((last - second) / 1000) + " seconds");
                System.out.println("done with Query, now parse results...");
                while (rs.next()) {
                    List these = new ArrayList();
                    these.add(rs.getInt(1));    //  snpid
                    these.add(rs.getInt(2));    //  bpPosition
                    these.add(rs.getInt(3));    //  _loc_func_key
                    these.add(rs.getInt(4));    //  gene_id
                    these.add(rs.getInt(5));    //  gene_start
                    these.add(rs.getInt(6));    //  gene_end
                    these.add(rs.getString(7)); // mgi_geneid
                    these.add(rs.getString(8)); // gene symbol
                    these.add(rs.getString(9)); // gene name
                    these.add(rs.getString(10)); // rs number
                    these.add(rs.getString(11)); // provider id
                    these.add(rs.getString(12)); // provider
                    results.add(these);

                }
                System.out.println("RESULT SIZE: " + results.size());
                Statement dropStmt = connection.createStatement();
                dropStmt.executeUpdate("drop temporary table _bp_pos");
                connection.close();
            } catch (SQLException sqle) {
                printSQLException(sqle);
                throw sqle;
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException sqle) {
                    printSQLException(sqle);
                }
            }
            return results;
        }*/
        
        public List<List> call() throws SQLException {
            List<List> results = new ArrayList<List>();
            String detail_cmd = "select distinct s.snpid, sp.bp_position, "
                    + "st._loc_func_key, st.gene_id, g.gene_start, g.gene_end, "
                    + //"mgi.mgi_geneid, mgi.marker_symbol, mgi.marker_name, " +
                    "(select mgi_geneid from cgd_genes_ensembl_mgi where gene_id = st.gene_id) mgi_geneid, "
                    + "(select marker_symbol from cgd_genes_ensembl_mgi where gene_id = st.gene_id) marker_symbol, "
                    + "(select marker_name from cgd_genes_ensembl_mgi where gene_id = st.gene_id) marker_name, "
                    + "sa.accession_id as rs_number, sa2.accession_id as provider_id, "
                    + "sas.source_name as provider "
                    + "from snp_main s LEFT JOIN (snp_accession sa) ON "
                    + "(s.snpid = sa.snpid and sa.snpid_id_type = 1) "
                    + "LEFT JOIN (snp_accession sa2, snp_source sas) ON "
                    + "(s.snpid = sa2.snpid and sa2.snpid_id_type = 3 "
                    + "and sa2.source_id = sas.source_id), "
                    + "snp_position sp, snp_chromosome c, snp_by_source ss, "
                    + "snp_transcript st, cgd_genes_ensembl_mgi mgi, cgd_genes g "
                    + "where chromosome_name =  '"
                    + this.chromosome + "' "
                    + "and c.chromosome_id = sp.chromosome_id "
                    + "and sp.bp_position in (";
            boolean first = true;
            for (Integer position : bpPosList) {
                if (first) {
                    detail_cmd += position;
                    first = false;
                } else {
                    detail_cmd += "," + position;
                }

            }
            // snpid_type_id will only bring back RS numbers
            detail_cmd += ")"
                    + "and sp.snpid = s.snpid "
                    + "and s.snpid = ss.snpid ";
            if (this.source_code.startsWith("(")) {
                detail_cmd += "and ss.source_id in " + this.source_code;
            } else {
                detail_cmd += "and ss.source_id = " + this.source_code;
            }
            detail_cmd += " and s.snpid = st.snpid "
                    + "and st.gene_id = mgi.gene_id "
                    + "and st.gene_id = g.gene_id "
                    + "order by sp.bp_position, s.snpid";
            //System.out.println("Query has " + bpPosList.size() + " positions: " + detail_cmd);
            ResultSet rs = null;
            Statement statement = null;
            try {
                statement = connection.createStatement();
                long start = System.currentTimeMillis();
                rs = statement.executeQuery(detail_cmd);
                long second = System.currentTimeMillis();
                //System.out.println("Query took: " + ((second - start) / 1000) + " seconds");
                while (rs.next()) {
                    List these = new ArrayList();
                    these.add(rs.getInt(1));    //  snpid
                    these.add(rs.getInt(2));    //  bpPosition
                    these.add(rs.getInt(3));    //  _loc_func_key
                    these.add(rs.getInt(4));    //  gene_id
                    these.add(rs.getInt(5));    //  gene_start
                    these.add(rs.getInt(6));    //  gene_end
                    these.add(rs.getString(7)); // mgi_geneid
                    these.add(rs.getString(8)); // gene symbol
                    these.add(rs.getString(9)); // gene name
                    these.add(rs.getString(10)); // rs number
                    these.add(rs.getString(11)); // provider id
                    these.add(rs.getString(12)); // provider
                    results.add(these);

                }
                long last = System.currentTimeMillis();
                connection.close();

            } catch (SQLException sqle) {
                printSQLException(sqle);
                throw sqle;
            } finally {
                try {
                    if (rs != null) {
                        rs.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                } catch (SQLException sqle) {
                    printSQLException(sqle);
                }
            }
            return results;
        }

        
    }
    
    String source_code = "1";
    /**
     * getSNPDetail is intended to get all the SNPs on the given chromosome,
     * in the list of positions.  It uses the CGDSnpDB class
     * connection attribute.
     *
     * @return  A List of rows, where each row is a slist of columns.  The query
     * returns columns for: snpid, bp_position, _loc_func_key, gene_id, and
     * mgi_geneid (mgi accession id)
     */
    public List<List> getSNPDetails(String chromosome, List<Integer> bpPosList, String snpset)
            throws SQLException
    {
        if (snpset.equals("UNC")) this.source_code = "21";
        else if (snpset.equals("SANGER")) this.source_code = "15";
        else if (snpset.equals("NIEHS"))  this.source_code = "1";
        else if (snpset.equals("UNC_SANGER")) this.source_code = "(15, 21)";
        
        System.out.println("Begin processing for CHROMOSOME = " + chromosome);
        List<List<Integer>> bins = new ArrayList<List<Integer>>();
        List<List> results = new ArrayList<List>();
        List<Integer> bin = new ArrayList<Integer>();
        for (int i = 0; i < bpPosList.size(); i++) {
            if (( i % 1000) == 0 && i > 0) {
                bins.add(bin);
                bin = new ArrayList<Integer>();
            }
            bin.add(bpPosList.get(i));
        }
        bins.add(bin);

        ExecutorService executor = Executors.newFixedThreadPool(10);
        Set<Future<List<List>>> set = new HashSet<Future<List<List>>>();
        try {
            int numConOpen = 0;
            for (List<Integer> subSet : bins) {
                //  for each subset regions
                Connection conn = this.getConnection();
                Callable<List<List>> callable = new SNPDetailFetcher(conn, 
                        chromosome, subSet, this.source_code);
                Future<List<List>> future = executor.submit(callable);
                set.add(future);
                //conn.close();
                //--numConOpen;
            }
            for (Future<List<List>> future : set) {
                results.addAll(future.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // This will make the executor accept no new threads
        // and finish all existing threads in the queue
        executor.shutdown();
        // Wait until all threads are finish
        while (!executor.isTerminated()) {
        }
        System.out.println("Finished all threads");

        return results;
    }




    private static final String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";

    private static String now_formatted() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
        return sdf.format(cal.getTime());
    }


    private static Date now() {
        Calendar cal = Calendar.getInstance();
        return cal.getTime();
    }


    public static void main(String[] args) {
        try {
            CGDSnpDB querySnpDB = new CGDSnpDB("cgd-dev.jax.org", "3306", "cgd_snpdb",
                    "cssc", "sp00nm3");
            List<Integer> bpPosList = new ArrayList<Integer>();


            FileInputStream fstream = new FileInputStream("/Users/dow/snps.csv");
            // Get the object of DataInputStream
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            //Read File Line By Line
            while ((strLine = br.readLine()) != null) {
                // Print the content on the console
                bpPosList.add(Integer.parseInt(strLine));
            }
            //Close the input stream
            in.close();




            bpPosList = new ArrayList<Integer>(bpPosList.subList(70000, 80000));



            querySnpDB.getSNPDetails("1", bpPosList, "SANGER");
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}
