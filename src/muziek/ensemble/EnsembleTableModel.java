// Class to setup a TableModel for records in ensemble

package muziek.ensemble;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

class EnsembleTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( "muziek.ensemble.EnsembleTableModel" );

    private final Connection connection;
    private final JFrame parentFrame;

    private final String[ ] headings = { "Id", "Ensemble" };

    private class EnsembleRecord {
	int	ensembleId;
	String  ensembleString;

	EnsembleRecord( int     ensembleId,
                        String  ensembleString ) {
	    this.ensembleId = ensembleId;
	    this.ensembleString = ensembleString;
	}
    }

    private ArrayList<EnsembleRecord> ensembleRecordList = new ArrayList<>( 100 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    EnsembleTableModel( Connection connection, JFrame parentFrame ) {
	this.connection = connection;
        this.parentFrame = parentFrame;

	setupEnsembleTableModel( null );
    }

    void setupEnsembleTableModel( String ensembleFilterString ) {

	// Setup the table
	try {
	    String ensembleQueryString = "SELECT ensemble_id, ensemble FROM ensemble ";

	    if ( ( ensembleFilterString != null ) && ( ensembleFilterString.length( ) > 0 ) ) {
		// Matcher to find single quotes in ensembleFilterString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( ensembleFilterString );
		ensembleQueryString += "WHERE ensemble LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
	    }

	    ensembleQueryString += "ORDER BY ensemble";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( ensembleQueryString );

	    // Clear the list
	    ensembleRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		ensembleRecordList.add( new EnsembleRecord( resultSet.getInt( 1 ),
							    resultSet.getString( 2 ) ) );
	    }

	    ensembleRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in select: " + sqlException.getMessage(),
                                           "EnsembleTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return ensembleRecordList.size( ); }

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
	if ( ( row < 0 ) || ( row >= ensembleRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final EnsembleRecord ensembleRecord = ensembleRecordList.get( row );

	if ( column == 0 ) return ensembleRecord.ensembleId;
	if ( column == 1 ) return ensembleRecord.ensembleString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= ensembleRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final EnsembleRecord ensembleRecord = ensembleRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String ensembleString = ( String )object;
		if ( ( ( ensembleString == null ) || ( ensembleString.length( ) == 0 ) ) &&
		     ( ensembleRecord.ensembleString != null ) &&
		     ( ensembleRecord.ensembleString.length( ) != 0 ) ) {
		    updateString = "ensemble = NULL ";
		} else if ( ( ensembleString != null ) &&
			    ( !ensembleString.equals( ensembleRecord.ensembleString ) ) ) {
		    // Matcher to find single quotes in ensemble, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( ensembleString );
		    updateString = "ensemble = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		}
		ensembleRecord.ensembleString = ensembleString;
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
	ensembleRecordList.set( row, ensembleRecord );

	updateString = ( "UPDATE ensemble SET " + updateString +
			 " WHERE ensemble_id = " + ensembleRecord.ensembleId );

	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with ensemble_id " + ensembleRecord.ensembleId +
			       " in ensemble, nUpdate = " + nUpdate );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in update: " + sqlException.getMessage(),
                                           "EnsembleTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	fireTableCellUpdated( row, column );
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    int getNumberOfRecords( ) { return ensembleRecordList.size( ); }

    int getEnsembleId( int row ) {
        if ( ( row < 0 ) || ( row >= ensembleRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return ensembleRecordList.get( row ).ensembleId;
    }

    String getEnsembleString( int row ) {
	if ( ( row < 0 ) || ( row >= ensembleRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ensembleRecordList.get( row ).ensembleString;
    }
}
