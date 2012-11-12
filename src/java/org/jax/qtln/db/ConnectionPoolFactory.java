package org.jax.qtln.db;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;

/**
 * A connection factory for
 * @author <A HREF="mailto:keith.sheppard@jax.org">Keith Sheppard</A>
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
     public static BoneCP createConnectionPool(
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
        config.setMinConnectionsPerPartition(5);
        config.setMaxConnectionsPerPartition(10);
        config.setPartitionCount(1);

        

        LOG.info("Initializing Connection Pool with URL: " + databaseUrl);
        BoneCP boneCP = new BoneCP(config);
        // With the connection
        //connection.setAutoCommit(false);
        return boneCP;
    }
   
    
    /**
     * Shutdown the database driver
     * @param connection the connection to use when shutting down
     */
    public static void shutdownDatabase(BoneCP connectionPool) {
        try {
            connectionPool.shutdown();
        } catch (Exception ex) {
            LOG.log(Level.WARNING,
                    "Datatbase connection pool did not shut down normally",
                    ex);
        }
    }
}
