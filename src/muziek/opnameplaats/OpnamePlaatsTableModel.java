// Class to setup a TableModel for records in opname_plaats

package muziek.opnameplaats;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;


import javax.swing.table.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;


public class OpnamePlaatsTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.opnameplaats.OpnamePlaatsTableModel" );

    private Connection connection;
    private String[ ] headings = { "Id", "Opname Plaats" };

    class OpnamePlaatsRecord {
	int	opnamePlaatsId;
	String	opnamePlaatsString;

	public OpnamePlaatsRecord( int    opnamePlaatsId,
				   String opnamePlaatsString ) {
	    this.opnamePlaatsId = opnamePlaatsId;
	    this.opnamePlaatsString = opnamePlaatsString;
	}
    }

    ArrayList opnamePlaatsRecordList = new ArrayList( 200 );

    private String opnamePlaatsFilterString = null;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public OpnamePlaatsTableModel( Connection connection ) {
	this.connection = connection;

	setupOpnamePlaatsTableModel( null );
    }

    public void setupOpnamePlaatsTableModel( String opnamePlaatsFilterString ) {
	this.opnamePlaatsFilterString = opnamePlaatsFilterString;

	// Setup the table
	try {
	    String opnamePlaatsQueryString =
		"SELECT opname_plaats_id, opname_plaats FROM opname_plaats ";

	    if ( ( opnamePlaatsFilterString != null ) && ( opnamePlaatsFilterString.length( ) > 0 ) ) {
		    opnamePlaatsQueryString += " WHERE opname_plaats LIKE \"%" + opnamePlaatsFilterString + "%\" ";
	    }

	    opnamePlaatsQueryString += "ORDER BY opname_plaats";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opnamePlaatsQueryString );

	    // Clear the list
	    opnamePlaatsRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		opnamePlaatsRecordList.add( new OpnamePlaatsRecord( resultSet.getInt( 1 ),
								    resultSet.getString( 2 ) ) );
	    }

	    opnamePlaatsRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return opnamePlaatsRecordList.size( ); }

    public int getColumnCount( ) { return 2; }

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
	case 0:	// id
	    return false;
	}
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= opnamePlaatsRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final OpnamePlaatsRecord opnamePlaatsRecord =
	    ( OpnamePlaatsRecord )opnamePlaatsRecordList.get( row );

	if ( column == 0 ) return new Integer( opnamePlaatsRecord.opnamePlaatsId );
	if ( column == 1 ) return opnamePlaatsRecord.opnamePlaatsString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= opnamePlaatsRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final OpnamePlaatsRecord opnamePlaatsRecord =
	    ( OpnamePlaatsRecord )opnamePlaatsRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String opnamePlaatsString = ( String )object;
		if ( ( ( opnamePlaatsString == null ) || ( opnamePlaatsString.length( ) == 0 ) ) &&
		     ( opnamePlaatsRecord.opnamePlaatsString != null ) &&
		     ( opnamePlaatsRecord.opnamePlaatsString.length( ) != 0 ) ) {
		    updateString = "opname_plaats = NULL ";
		    opnamePlaatsRecord.opnamePlaatsString = opnamePlaatsString;
		} else if ( ( opnamePlaatsString != null ) &&
			    ( !opnamePlaatsString.equals( opnamePlaatsRecord.opnamePlaatsString ) ) ) {
		    // Matcher to find single quotes in opnamePlaatsString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( opnamePlaatsString );
		    updateString = "opname_plaats = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    opnamePlaatsRecord.opnamePlaatsString = opnamePlaatsString;
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

	updateString = ( "UPDATE opname_plaats SET " + updateString +
			 " WHERE opname_plaats_id = " + opnamePlaatsRecord.opnamePlaatsId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with opname_plaats_id " + opnamePlaatsRecord.opnamePlaatsId );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	opnamePlaatsRecordList.set( row, opnamePlaatsRecord );

	fireTableCellUpdated( row, column );
    }

    public int getOpnamePlaatsId( int row ) {
	final OpnamePlaatsRecord opnamePlaatsRecord =
	    ( OpnamePlaatsRecord )opnamePlaatsRecordList.get( row );

	return opnamePlaatsRecord.opnamePlaatsId;
    }

    public String getOpnamePlaatsString( int row ) {
	if ( ( row < 0 ) || ( row >= opnamePlaatsRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( ( OpnamePlaatsRecord )opnamePlaatsRecordList.get( row ) ).opnamePlaatsString;
    }
}
