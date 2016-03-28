// Class to setup a TableModel for records in subtype

package muziek.gui;

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


public class SubtypeTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.gui.SubtypeTableModel" );

    private Connection connection;
    private String[ ] headings = { "Id", "Subtype" };

    class SubtypeRecord {
	int	subtypeId;
	String	subtypeString;

	public SubtypeRecord( int    subtypeId,
			      String subtypeString ) {
	    this.subtypeId = subtypeId;
	    this.subtypeString = subtypeString;
	}
    }

    ArrayList subtypeRecordList = new ArrayList( 100 );

    private String subtypeFilterString = null;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public SubtypeTableModel( Connection connection ) {
	this.connection = connection;

	setupSubtypeTableModel( null );
    }	

    public void setupSubtypeTableModel( String subtypeFilterString ) {
	this.subtypeFilterString = subtypeFilterString;

	// Setup the table
	try {
	    String subtypeQueryString = "SELECT subtype_id, subtype FROM subtype ";

	    if ( ( subtypeFilterString != null ) && ( subtypeFilterString.length( ) > 0 ) ) {
		    subtypeQueryString += " WHERE subtype LIKE \"%" + subtypeFilterString + "%\" ";
	    }

	    subtypeQueryString += "ORDER BY subtype";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( subtypeQueryString );

	    // Clear the list
	    subtypeRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		subtypeRecordList.add( new SubtypeRecord( resultSet.getInt( 1 ),
							  resultSet.getString( 2 ) ) );
	    }

	    subtypeRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return subtypeRecordList.size( ); }

    public int getColumnCount( ) { return 2; }

    public String getColumnName( int column ) { return headings[ column ]; }

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
	if ( ( row < 0 ) || ( row >= subtypeRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final SubtypeRecord subtypeRecord = ( SubtypeRecord )subtypeRecordList.get( row );

	if ( column == 0 ) return new Integer( subtypeRecord.subtypeId );
	if ( column == 1 ) return subtypeRecord.subtypeString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= subtypeRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final SubtypeRecord subtypeRecord = ( SubtypeRecord )subtypeRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String subtypeString = ( String )object;
		if ( ( ( subtypeString == null ) || ( subtypeString.length( ) == 0 ) ) &&
		     ( subtypeRecord.subtypeString != null ) &&
		     ( subtypeRecord.subtypeString.length( ) != 0 ) ) {
		    updateString = "subtype = NULL ";
		    subtypeRecord.subtypeString = subtypeString;
		} else if ( ( subtypeString != null ) &&
			    ( !subtypeString.equals( subtypeRecord.subtypeString ) ) ) {
		    // Matcher to find single quotes in subtypeString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( subtypeString );
		    updateString = "subtype = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    subtypeRecord.subtypeString = subtypeString;
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

	updateString = ( "UPDATE subtype SET " + updateString +
			 " WHERE subtype_id = " + subtypeRecord.subtypeId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with subtype_id " + subtypeRecord.subtypeId );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	subtypeRecordList.set( row, subtypeRecord );

	fireTableCellUpdated( row, column );
    }

    public int getSubtypeId( int row ) {
	final SubtypeRecord subtypeRecord = ( SubtypeRecord )subtypeRecordList.get( row );

	return subtypeRecord.subtypeId;
    }

    public String getSubtypeString( int row ) {
	if ( ( row < 0 ) || ( row >= subtypeRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( ( SubtypeRecord )subtypeRecordList.get( row ) ).subtypeString;
    }
}
