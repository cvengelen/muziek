package muziek.opslag;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * Class to setup a TableModel for records in opslag
 */
class OpslagTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( OpslagTableModel.class.getCanonicalName() );

    private final Connection connection;
    private final JFrame parentFrame;

    private final String[ ] headings = { "Id", "Opslag" };

    private class OpslagRecord {
	int	opslagId;
	String	opslagString;

	OpslagRecord( int    opslagId,
                      String opslagString ) {
	    this.opslagId = opslagId;
	    this.opslagString = opslagString;
	}
    }

    private final ArrayList<OpslagRecord> opslagRecordList = new ArrayList<>( 500 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );

    // Constructor
    OpslagTableModel( Connection connection, JFrame parentFrame ) {
        this.connection = connection;
        this.parentFrame = parentFrame;

	setupOpslagTableModel( null );
    }

    void setupOpslagTableModel( String opslagFilterString ) {

	// Setup the table
	try {
	    String opslagQueryString = "SELECT opslag.opslag_id, opslag.opslag FROM opslag ";

	    if ( ( opslagFilterString != null ) && ( opslagFilterString.length( ) > 0 ) ) {
		// Matcher to find single quotes in opslagFilterString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( opslagFilterString );
		opslagQueryString += "WHERE opslag.opslag LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
	    }

	    opslagQueryString += "ORDER BY opslag.opslag";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opslagQueryString );

	    // Clear the list
	    opslagRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		opslagRecordList.add( new OpslagRecord( resultSet.getInt( 1 ),
							resultSet.getString( 2 ) ) );
	    }

	    opslagRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in select: " + sqlException.getMessage(),
                                           "OpslagTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return opslagRecordList.size( ); }

    public int getColumnCount( ) { return 2; }

    public Class getColumnClass( int column ) {
	switch ( column ) {
	case 0: // Id
	    return Integer.class;
	}
	return String.class;
    }

    public boolean isCellEditable( int row, int column ) {
	switch ( column ) {
	case 0: // Id
	    // Do not allow editing
	    return false;
	}
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= opslagRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final OpslagRecord opslagRecord = opslagRecordList.get( row );

	if ( column == 0 ) return opslagRecord.opslagId;
	if ( column == 1 ) return opslagRecord.opslagString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= opslagRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final OpslagRecord opslagRecord = opslagRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String opslagString = ( String )object;
		if ( ( ( opslagString == null ) || ( opslagString.length( ) == 0 ) ) &&
		     ( opslagRecord.opslagString != null ) &&
		     ( opslagRecord.opslagString.length( ) != 0 ) ) {
		    updateString = "opslag = NULL ";
		} else if ( ( opslagString != null ) &&
			    ( !opslagString.equals( opslagRecord.opslagString ) ) ) {
		    // Matcher to find single quotes in opslag, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( opslagString );
		    updateString = "opslag = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		}
		opslagRecord.opslagString = opslagString;
		break;

	    default:
		logger.severe( "Invalid column: " + column );
		return;
	    }
	} catch ( Exception exception ) {
	    logger.severe( "could not get value from " +
			   object + " for column " + column + " in row " + row );
	    return;
	}

	// Check if update is not necessary
	if ( updateString == null ) return;

	// Store record in list
	opslagRecordList.set( row, opslagRecord );

	updateString = ( "UPDATE opslag SET " + updateString +
			 " WHERE opslag_id = " + opslagRecord.opslagId );

	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with opslag_id " + opslagRecord.opslagId +
			       " in opslag, nUpdate = " + nUpdate );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in update: " + sqlException.getMessage(),
                                           "OpslagTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	fireTableCellUpdated( row, column );
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    int getNumberOfRecords( ) { return opslagRecordList.size( ); }

    int getOpslagId( int row ) {
        if ( ( row < 0 ) || ( row >= opslagRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return opslagRecordList.get( row ).opslagId;
    }

    String getOpslagString( int row ) {
	if ( ( row < 0 ) || ( row >= opslagRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return opslagRecordList.get( row ).opslagString;
    }
}
