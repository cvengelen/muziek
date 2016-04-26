package muziek;

import muziek.gui.PasswordPanel;

import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Main program for schema muziek.
 * The first and only argument must specify the fully-qualified name of the class which should be started,
 * e.g.: java -cp ...  muziek.Main muziek.opname.OpnameFrame
 *
 * Created by cvengelen on 25-04-16.
 */
public class Main {
    private final static Logger logger = Logger.getLogger( muziek.Main.class.getCanonicalName() );

    public static void main( String[ ] args ) {
        if (args.length == 0 || args[0].length() == 0) {
            logger.severe("No class");
            System.err.println("Geef de naam van de class die gestart moet worden, bijvoorbeeld: muziek.opname.OpnameFrame");
            System.exit(1);
        }
        final String muziekClassName = args[0];
        logger.info( "Starting " + muziekClassName );

        try {
            // Load the MySQL JDBC driver
            Class.forName( "com.mysql.jdbc.Driver" );
        } catch ( ClassNotFoundException classNotFoundException ) {
            logger.severe( "ClassNotFoundException: " + classNotFoundException.getMessage() );
            System.exit( 1 );
        }

        try {
            // Get the password for the muziek account, which gives access to schema muziek.
            final PasswordPanel passwordPanel = new PasswordPanel();
            final String password = passwordPanel.getPassword();
            if (password == null) {
                logger.info("No password");
                System.err.println("Geen password gegeven");
                System.exit( 1 );
            }

            // Find the constructor of the class with name muziekClassName which has a Connection as parameter
            // See: https://docs.oracle.com/javase/tutorial/reflect/class/classNew.html
            // and: http://tutorials.jenkov.com/java-reflection/constructors.html
            final Constructor constructor = Class.forName(muziekClassName).getConstructor( Connection.class );

            // Open a connection to the muziek schema of the MySQL database, and create the frame with the MySQL connection as parameter.
            // No need to save a reference of the instance: when the frame is finished, the application is finished.
            constructor.newInstance( DriverManager.getConnection( "jdbc:mysql://localhost/muziek?user=muziek&password=" + password ) );
        } catch ( ClassNotFoundException classNotFoundException ) {
            logger.severe( "ClassNotFoundException: " + classNotFoundException.getMessage( ) );
            System.err.println("Class " + muziekClassName + " bestaat niet.\nControleer de naam van de class, bijvoorbeeld: muziek.opname.OpnameFrame");
            System.exit( 1 );
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
            System.exit( 1 );
        } catch ( Exception exception ) {
            logger.severe( "Exception: " + exception.getMessage( ) );
            System.exit( 1 );
        }
    }
}
