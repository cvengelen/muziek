// Class to setup a TableModel for records in producers

package muziek.producers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;


public class ProducersTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.producers.ProducersTableModel" );

    private Connection connection;
    private String[ ] headings = { "Id", "Producers", "Persoon" };

    class ProducersRecord {
	String	producersString;
	String	persoonString;
	int	producersId;
	int	persoonId;

	public ProducersRecord( String producersString,
				String persoonString,
				int    producersId,
				int    persoonId ) {
	    this.producersString = producersString;
	    this.persoonString = persoonString;
	    this.producersId = producersId;
	    this.persoonId = persoonId;
	}
    }

    ArrayList producersRecordList = new ArrayList( 100 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public ProducersTableModel( Connection connection ) {
	this.connection = connection;

	setupProducersTableModel( null, 0 );
    }

    public void setupProducersTableModel( String producersFilterString,
					    int selectedPersoonId ) {

	// Setup the table
	try {
	    String producersQueryString =
		"SELECT producers.producers, persoon.persoon, " +
		"producers.producers_id, producers_persoon.persoon_id " +
		"FROM producers " +
		"LEFT JOIN producers_persoon ON producers_persoon.producers_id = producers.producers_id " +
		"LEFT JOIN persoon ON persoon.persoon_id = producers_persoon.persoon_id ";

	    if ( ( ( producersFilterString != null ) && ( producersFilterString.length( ) > 0 ) ) ||
		 ( selectedPersoonId != 0 ) ) {
		producersQueryString += "WHERE ";

		if ( ( producersFilterString != null ) && ( producersFilterString.length( ) > 0 ) ) {
		    producersQueryString += "producers.producers LIKE \"%" + producersFilterString + "%\" ";

		    if ( selectedPersoonId != 0 ) {
			producersQueryString += "AND ";
		    }
		}

		if ( selectedPersoonId != 0 ) {
		    producersQueryString += "producers_persoon.persoon_id = " + selectedPersoonId + " ";
		}
	    }

	    producersQueryString += "ORDER BY persoon.persoon";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( producersQueryString );

	    // Clear the list
	    producersRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		producersRecordList.add( new ProducersRecord( resultSet.getString( 1 ),
							      resultSet.getString( 2 ),
							      resultSet.getInt( 3 ),
							      resultSet.getInt( 4 ) ) );
	    }

	    producersRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return producersRecordList.size( ); }

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
	if ( ( row < 0 ) || ( row >= producersRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final ProducersRecord producersRecord =
	    ( ProducersRecord )producersRecordList.get( row );

	if ( column == 0 ) return new Integer( producersRecord.producersId );
	if ( column == 1 ) return producersRecord.producersString;
	if ( column == 2 ) return producersRecord.persoonString;

	return "";
    }


    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= producersRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final ProducersRecord producersRecord =
	    ( ProducersRecord )producersRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String producersString = ( String )object;
		if ( ( ( producersString == null ) || ( producersString.length( ) == 0 ) ) &&
		     ( producersRecord.producersString != null ) &&
		     ( producersRecord.producersString.length( ) != 0 ) ) {
		    updateString = "producers = NULL ";
		    producersRecord.producersString = producersString;
		} else if ( ( producersString != null ) &&
			    ( !producersString.equals( producersRecord.producersString ) ) ) {
		    // Matcher to find single quotes in producers, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( producersString );
		    updateString = "producers = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    producersRecord.producersString = producersString;
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

	updateString = ( "UPDATE producers SET " + updateString +
			 " WHERE producers_id = " + producersRecord.producersId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with producers_id " + producersRecord.producersId +
			       " in producers, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	producersRecordList.set( row, producersRecord );

	fireTableCellUpdated( row, column );
    }

    public int getProducersId( int row ) {
	final ProducersRecord producersRecord =
	    ( ProducersRecord )producersRecordList.get( row );

	return producersRecord.producersId;
    }

    public String getProducersString( int row ) {
	if ( ( row < 0 ) || ( row >= producersRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( ( ProducersRecord )producersRecordList.get( row ) ).producersString;
    }
}
