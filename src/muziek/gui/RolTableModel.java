// Class to setup a TableModel for records in rol

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


public class RolTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.gui.RolTableModel" );

    private Connection connection;
    private String[ ] headings = { "Id", "Rol" };

    class RolRecord {
	int	rolId;
	String	rolString;

	public RolRecord( int    rolId,
			  String rolString ) {
	    this.rolId = rolId;
	    this.rolString = rolString;
	}
    }

    ArrayList rolRecordList = new ArrayList( 100 );

    private String rolFilterString = null;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public RolTableModel( Connection connection ) {
	this.connection = connection;

	setupRolTableModel( null );
    }	

    public void setupRolTableModel( String rolFilterString ) {
	this.rolFilterString = rolFilterString;

	// Setup the table
	try {
	    String rolQueryString = "SELECT rol_id, rol FROM rol ";

	    if ( ( rolFilterString != null ) && ( rolFilterString.length( ) > 0 ) ) {
		    rolQueryString += " WHERE rol LIKE \"%" + rolFilterString + "%\" ";
	    }

	    rolQueryString += "ORDER BY rol";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( rolQueryString );

	    // Clear the list
	    rolRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		rolRecordList.add( new RolRecord( resultSet.getInt( 1 ),
						  resultSet.getString( 2 ) ) );
	    }

	    rolRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return rolRecordList.size( ); }

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
	if ( ( row < 0 ) || ( row >= rolRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final RolRecord rolRecord = ( RolRecord )rolRecordList.get( row );

	if ( column == 0 ) return new Integer( rolRecord.rolId );
	if ( column == 1 ) return rolRecord.rolString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= rolRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final RolRecord rolRecord = ( RolRecord )rolRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String rolString = ( String )object;
		if ( ( ( rolString == null ) || ( rolString.length( ) == 0 ) ) &&
		     ( rolRecord.rolString != null ) &&
		     ( rolRecord.rolString.length( ) != 0 ) ) {
		    updateString = "rol = NULL ";
		    rolRecord.rolString = rolString;
		} else if ( ( rolString != null ) &&
			    ( !rolString.equals( rolRecord.rolString ) ) ) {
		    // Matcher to find single quotes in rolString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( rolString );
		    updateString = "rol = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    rolRecord.rolString = rolString;
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

	updateString = ( "UPDATE rol SET " + updateString +
			 " WHERE rol_id = " + rolRecord.rolId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with rol_id " + rolRecord.rolId );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	rolRecordList.set( row, rolRecord );

	fireTableCellUpdated( row, column );
    }

    public int getRolId( int row ) {
	final RolRecord rolRecord = ( RolRecord )rolRecordList.get( row );

	return rolRecord.rolId;
    }

    public String getRolString( int row ) {
	if ( ( row < 0 ) || ( row >= rolRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return ( ( RolRecord )rolRecordList.get( row ) ).rolString;
    }
}
