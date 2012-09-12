package org.jax.util.db;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
//  THIS CLASS WAS ADDED TO SUPPORT CONNECTION POOLING.  COMMENTED OUT FOR NOW
//  TODO:  Uncomment, include BoneCP in libraries, add code to thread cgdsnp lookups.
//import com.jolbox.bonecp.BoneCP;
//import com.jolbox.bonecp.BoneCPConfig;

/**
 * A database connection factory.  Based on a connection factory written by
 * Keith Sheppard
 * @author <A HREF="mailto:david.o.walton">Dave Walton</A>
 */
public class ConnectionPoolFactory {
    private static final Logger LOG = Logger.getLogger(
            ConnectionPoolFactory.class.getName());
    
        
    /**
     * Create a new connection pool
     * @param db_host   The server where the database resides
     * @param db_port   The data base port on the db_host
     * @param db_name   The name of the database on db_host
     * @param user      User account to be used to log into db
     * @param pass      The password for login
     * @return the connection pool
     * @throws SQLException if there's some problem creating the connection
     * @throws ClassNotFoundException
     */
     /*public static BoneCP createConnectionPool(
            String databaseDriver, String databaseUrl, String user,
            String pass)
            throws SQLException, ClassNotFoundException {
        Class.forName(databaseDriver);
        
        //final String databaseUrl = ConnectionPoolFactory.DB_PROTOCOL + "//" + 
        //        db_host + ":" + db_port + "/" + db_name;
        BoneCPConfig config = new BoneCPConfig();
        config.setDefaultAutoCommit(false);
        config.setJdbcUrl(databaseUrl);
        config.setUsername(user);
        config.setPassword(pass);
        config.setMaxConnectionAge(7, TimeUnit.HOURS);
        config.setMaxConnectionsPerPartition(10);
        

        LOG.info("Initializing Connection Pool with URL: " + databaseUrl);
        BoneCP boneCP = new BoneCP(config);
        // With the connection
        //connection.setAutoCommit(false);
        return boneCP;
    }*/
   
    
    /**
     * Shutdown the database driver
     * @param connection the connection to use when shutting down
     */
    /*public static void shutdownDatabase(BoneCP connectionPool) {
        try {
            connectionPool.shutdown();
        } catch (Exception ex) {
            LOG.log(Level.WARNING,
                    "Datatbase connection pool did not shut down normally",
                    ex);
        }
    }*/
}
