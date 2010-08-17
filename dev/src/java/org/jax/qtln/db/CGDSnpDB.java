/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jax.qtln.db;

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
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author dow
 */
public class CGDSnpDB {

    private String driver = "com.mysql.jdbc.Driver";
    private String protocol = "jdbc:mysql:";
    private String dbHost = "cgd.jax.org";
    private String database = "cgdsnpdb";
    private String user = "pup";
    private String password = "puppass";
    public static final String[] chr_array = {"1", "2", "3", "4", "5", "6", "7",
        "8", "9","10","11", "12", "13", "14", "15", "16", "17", "18", "19", "X",
        "Y"};


    private Connection connection;
    private List chromosomes;


    public CGDSnpDB() {
        loadDriver();
        this.chromosomes = Arrays.asList(chr_array);
    }

    public CGDSnpDB(String dbHost, String database, String user,
            String password)
    {
        super();
        this.dbHost = dbHost;
        this.database = database;
        this.user = user;
        this.password = password;

    }

    public Connection getConnection() 
        throws SQLException
    {
        if (this.connection == null) {
            try {
                Properties props = new Properties(); // connection properties
                props.put("user", this.user);
                props.put("password", this.password);
                this.connection = DriverManager.getConnection(this.protocol +
                        "//" + dbHost + ":3306/" + database, props);
            } catch (SQLException sql) {
                printSQLException(sql);
                throw sql;
            }

        }
        return this.connection;
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
        } catch (SQLException sqle) {
            printSQLException(sqle);
            throw sqle;
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

    }

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
        } catch (SQLException sqle) {
            printSQLException(sqle);
            throw sqle;
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

    }

    /**
     * getSNPDetail is intended to get all the SNPs on the given chromosome,
     * in the list of positions.  It uses the CGDSnpDB class
     * connection attribute.
     *
     * @return  A List of rows, where each row is a slist of columns.  The query
     * returns columns for: snpid, bp_position, _loc_func_key, gene_id, and
     * mgi_geneid (mgi accession id)
     */
    public List<List> getSNPDetails(String chromosome, List<Integer> bpPosList)
            throws SQLException
    {
        List<List> results = new ArrayList<List>();
        String detail_cmd = "select distinct s.snpid, s.bp_position, st._loc_func_key, " +
                "st.gene_id, mgi.mgi_geneid, mgi.marker_symbol, mgi.marker_name, " +
                "sa.accession_id, sas.source_name " +
                "from snp_main s, snp_chromosome c, snp_by_source ss, " +
                "snp_transcript st, cgd_genes_ensembl_mgi mgi,  " +
                "snp_accession sa, snp_source sas " +
                "where chromosome_name =  '" +
                chromosome + "' " +
                "and c.chromosome_id = s.chromosome_id " +
                "and s.bp_position in (";
        boolean first = true;
        for (Integer position: bpPosList) {
            if (first) {
                detail_cmd += position;
                first = false;
            }
            else
                detail_cmd += "," + position;

        }
        // snpid_type_id will only bring back RS numbers
        detail_cmd += ")" +
                "and s.snpid = ss.snpid " +
                "and ss.source_id = 16 " +
                "and s.snpid = st.snpid " +
                "and st.gene_id = mgi.gene_id " +
                "and s.snpid = sa.snpid " +
                "and sa.snpid_id_type = 1 " +
                "and sa.source_id = sas.source_id " +
                "order by s.bp_position, s.snpid";
        //System.out.println(detail_cmd);
        ResultSet rs = null;
        Statement statement = null;
        try {
            Connection conn = this.getConnection();
            statement = conn.createStatement();
            rs = statement.executeQuery(detail_cmd);
            while(rs.next()) {
                List these = new ArrayList();
                these.add(rs.getInt(1));    //  snpid
                these.add(rs.getInt(2));    //  bpPosition
                these.add(rs.getInt(3));    //  _loc_func_key
                these.add(rs.getInt(4));    //  gene_id
                these.add(rs.getString(5)); // mgi_geneid
                these.add(rs.getString(6)); // gene symbol
                these.add(rs.getString(7)); // gene name
                these.add(rs.getString(8)); // accession_id
                these.add(rs.getString(9)); // id source
                results.add(these);

            }
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


    public static void main (String[] args) {
        
        CGDSnpDB querySnpDB = new CGDSnpDB("cgd.jax.org", "cgdsnpdb",
                "pup", "puppass");

        System.exit(0);
    }

}
