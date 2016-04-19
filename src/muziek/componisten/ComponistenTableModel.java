// Class to setup a TableModel for records in componisten

package muziek.componisten;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;


public class ComponistenTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.componisten.ComponistenTableModel" );

    private Connection connection;
    private String[ ] headings = { "Id", "Componisten", "Persoon" };

    class ComponistenRecord {
	String	componistenString;
	String	persoonString;
	int	componistenId;
	int	persoonId;

	public ComponistenRecord( String componistenString,
				  String persoonString,
				  int    componistenId,
				  int    persoonId ) {
	    this.componistenString = componistenString;
	    this.persoonString = persoonString;
	    this.componistenId = componistenId;
	    this.persoonId = persoonId;
	}
    }

    ArrayList componistenRecordList = new ArrayList( 100 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public ComponistenTableModel( Connection connection ) {
	this.connection = connection;

	setupComponistenTableModel( null, 0 );
    }

    public void setupComponistenTableModel( String componistenFilterString,
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

	final ComponistenRecord componistenRecord =
	    ( ComponistenRecord )componistenRecordList.get( row );

	if ( column == 0 ) return new Integer( componistenRecord.componistenId );
	if ( column == 1 ) return componistenRecord.componistenString;
	if ( column == 2 ) return componistenRecord.persoonString;

	return "";
    }


    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= componistenRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final ComponistenRecord componistenRecord =
	    ( ComponistenRecord )componistenRecordList.get( row );

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

    public int getComponistenId( int row ) {
	final ComponistenRecord componistenRecord =
	    ( ComponistenRecord )componistenRecordList.get( row );

	return componistenRecord.componistenId;
    }

    public String getComponistenString( int row ) {
	if ( ( row < 0 ) || ( row >= componistenRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( ( ComponistenRecord )componistenRecordList.get( row ) ).componistenString;
    }
}
