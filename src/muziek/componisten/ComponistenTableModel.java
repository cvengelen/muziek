package muziek.componisten;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * Class to setup a TableModel for records in componisten
 */
class ComponistenTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( ComponistenTableModel.class.getCanonicalName() );

    private Connection connection;
    private final String[ ] headings = { "Id", "Componisten", "Persoon" };

    private class ComponistenRecord {
	String	componistenString;
	String	persoonString;
	int	componistenId;
	int	persoonId;

	ComponistenRecord( String componistenString,
                           String persoonString,
                           int    componistenId,
                           int    persoonId ) {
	    this.componistenString = componistenString;
	    this.persoonString = persoonString;
	    this.componistenId = componistenId;
	    this.persoonId = persoonId;
	}
    }

    private ArrayList<ComponistenRecord> componistenRecordList = new ArrayList<>( 100 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    ComponistenTableModel( Connection connection ) {
	this.connection = connection;

	setupComponistenTableModel( null, 0 );
    }

    void setupComponistenTableModel( String componistenFilterString,
                                     int selectedPersoonId ) {

	// Setup the table
	try {
	    String componistenQueryString =
		"SELECT componisten.componisten, persoon.persoon, " +
		"componisten.componisten_id, componisten_persoon.persoon_id " +
		"FROM componisten " +
		"LEFT JOIN componisten_persoon ON componisten_persoon.componisten_id = componisten.componisten_id " +
		"LEFT JOIN persoon ON persoon.persoon_id = componisten_persoon.persoon_id ";

	    if ( ( ( componistenFilterString != null ) && ( componistenFilterString.length( ) > 0 ) ) ||
		 ( selectedPersoonId != 0 ) ) {
		componistenQueryString += "WHERE ";

		if ( ( componistenFilterString != null ) && ( componistenFilterString.length( ) > 0 ) ) {
		    componistenQueryString += "componisten.componisten LIKE \"%" + componistenFilterString + "%\" ";

		    if ( selectedPersoonId != 0 ) {
			componistenQueryString += "AND ";
		    }
		}

		if ( selectedPersoonId != 0 ) {
		    componistenQueryString += "componisten_persoon.persoon_id = " + selectedPersoonId + " ";
		}
	    }

	    componistenQueryString += "ORDER BY persoon.persoon";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( componistenQueryString );

	    // Clear the list
	    componistenRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		componistenRecordList.add( new ComponistenRecord( resultSet.getString( 1 ),
								  resultSet.getString( 2 ),
								  resultSet.getInt( 3 ),
								  resultSet.getInt( 4 ) ) );
	    }

	    componistenRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return componistenRecordList.size( ); }

    public int getColumnCount( ) { return 3; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

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
	case 2: // Persoon
	    // Do not allow editing
	    return false;
	}
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= componistenRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final ComponistenRecord componistenRecord = componistenRecordList.get( row );

	if ( column == 0 ) return componistenRecord.componistenId;
	if ( column == 1 ) return componistenRecord.componistenString;
	if ( column == 2 ) return componistenRecord.persoonString;

	return "";
    }


    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= componistenRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final ComponistenRecord componistenRecord = componistenRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String componistenString = ( String )object;
		if ( ( ( componistenString == null ) || ( componistenString.length( ) == 0 ) ) &&
		     ( componistenRecord.componistenString != null ) &&
		     ( componistenRecord.componistenString.length( ) != 0 ) ) {
		    updateString = "componisten = NULL ";
		    componistenRecord.componistenString = componistenString;
		} else if ( ( componistenString != null ) &&
			    ( !componistenString.equals( componistenRecord.componistenString ) ) ) {
		    // Matcher to find single quotes in componisten, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( componistenString );
		    updateString = "componisten = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    componistenRecord.componistenString = componistenString;
		}

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

	updateString = ( "UPDATE componisten SET " + updateString +
			 " WHERE componisten_id = " + componistenRecord.componistenId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with componisten_id " + componistenRecord.componistenId +
			       " in componisten, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	componistenRecordList.set( row, componistenRecord );

	fireTableCellUpdated( row, column );
    }

    int getComponistenId( int row ) {
        if ( ( row < 0 ) || ( row >= componistenRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return componistenRecordList.get( row ).componistenId;
    }

    String getComponistenString( int row ) {
	if ( ( row < 0 ) || ( row >= componistenRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return componistenRecordList.get( row ).componistenString;
    }
}
