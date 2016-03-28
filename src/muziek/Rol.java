// Main program to insert or update in rol
// Start with muziek.Rol

package muziek;

import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException; 
import java.util.logging.Logger;

import muziek.gui.RolFrame;


public class Rol {
    final static Logger logger = Logger.getLogger( "muziek.Rol" );

    public static void main( String[ ] args ) {
        try {
            // The newInstance() call is a work around for some broken Java implementations
            // Class.forName("com.mysql.jdbc.Driver").newInstance();
            Class.forName( "com.mysql.jdbc.Driver" );
        } catch ( Exception exception ) {
	    logger.severe( "Exception: " + exception.getMessage( ) );
	    return;
        }

	final Connection connection;
	try {
            logger.info( "Opening db connection" );
            connection = DriverManager.getConnection( "jdbc:mysql://localhost/muziek?user=cvengelen&password=cve123" );
	    RolFrame rolFrame = new RolFrame( connection );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }
    }
}
