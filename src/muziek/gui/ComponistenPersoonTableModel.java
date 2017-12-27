// Class to setup a TableModel for componisten_persoon

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.table.*;

import java.util.ArrayList;
import java.util.logging.*;
import java.util.regex.*;


public class ComponistenPersoonTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.gui.ComponistenPersoonTableModel" );

    private Connection connection;
    private String[ ] headings = { "Componisten-Persoon" };

    class ComponistRecord {
	int	persoonId;
	String  persoonString;

	public ComponistRecord( int    persoonId,
				String persoonString ) {
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	}
    }

    ArrayList componistRecordList = new ArrayList( 10 );
    private int componistenId = 0;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public ComponistenPersoonTableModel( Connection connection ) {
	this.connection = connection;
    }

    // Constructor
    public ComponistenPersoonTableModel( Connection connection, int componistenId ) {
	this.connection = connection;
	showTable( componistenId );
    }

    public int getRowCount( ) { return componistRecordList.size( ); }

    public int getColumnCount( ) { return 1; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public boolean isCellEditable( int row, int column ) { return true; }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= componistRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}
	return ( ( ComponistRecord )componistRecordList.get( row ) ).persoonString;
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= componistRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final ComponistRecord componistRecord =
	    ( ComponistRecord )componistRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 0:
		String persoonString = ( String )object;
		if ( ( ( persoonString == null ) || ( persoonString.length( ) == 0 ) ) &&
		     ( componistRecord.persoonString != null ) &&
		     ( componistRecord.persoonString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		    componistRecord.persoonString = persoonString;
		} else if ( ( persoonString != null ) &&
			    ( !persoonString.equals( componistRecord.persoonString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( persoonString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    componistRecord.persoonString = persoonString;
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

	updateString = "UPDATE persoon SET " + updateString + " WHERE persoon_id = " + componistRecord.persoonId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with persoon_id " + componistRecord.persoonId +
			       " in persoon, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	componistRecordList.set( row, componistRecord );

	fireTableCellUpdated( row, column );
    }

    public void showTable( int componistenId ) {
	this.componistenId = componistenId;
	showTable( );
    }

    public void showTable( ) {
	// Clear the componisten list
	componistRecordList.clear( );

	try {
	    Statement componistenStmt = connection.createStatement( );
	    ResultSet componistenResultSet = componistenStmt.executeQuery( "SELECT persoon.persoon_id, persoon.persoon " +
									   "FROM componisten_persoon " +
									   "LEFT JOIN persoon ON persoon.persoon_id = componisten_persoon.persoon_id " +
									   "WHERE componisten_persoon.componisten_id = " + componistenId );

	    while ( componistenResultSet.next( ) ) {
		componistRecordList.add( new ComponistRecord( componistenResultSet.getInt( 1 ),
							      componistenResultSet.getString( 2 ) ) );
	    }

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getPersoonId( int row ) {
	if ( ( row < 0 ) || ( row >= componistRecordList.size( ) ) ) {
	    logger.severe( "invalid row: " + row );
	    return 0;
	}

	return ( ( ComponistRecord )componistRecordList.get( row ) ).persoonId;
    }

    public void addRow( int persoonId, String persoonString ) {
	componistRecordList.add( new ComponistRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void replaceRow( int row, int persoonId, String persoonString ) {
	if ( ( row < 0 ) || ( row >= componistRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	componistRecordList.set( row,
				 new ComponistRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= componistRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	componistRecordList.remove( row );
	fireTableDataChanged( );
    }

    // When inserting for a new componisten record, the componistenId still must be set:
    // only allow inserting in the table for other classes when the componisten ID is specified.
    public void insertTable( int componistenId ) {
	this.componistenId = componistenId;

	insertTable( );
    }

    private void insertTable( ) {
	// Check for valid componisten ID
	if ( componistenId == 0 ) {
	    logger.severe( "Componisten not selected" );
	    return;
	}

	try {
	    // Insert new rows in the componisten_persoon table
	    Statement statement = connection.createStatement( );
	    for ( int componistIndex = 0; componistIndex < componistRecordList.size( ); componistIndex++ ) {
		int persoonId = ( ( ComponistRecord )componistRecordList.get( componistIndex ) ).persoonId;
		int nUpdate = statement.executeUpdate( "INSERT INTO componisten_persoon SET " +
						       "componisten_id = " + componistenId +
						       ", persoon_id = " + persoonId );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in componisten_persoon for index " + componistIndex );
		    return;
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void updateTable( ) {
	// Check for valid componisten ID
	if ( componistenId == 0 ) {
	    logger.severe( "componisten not selected" );
	    return;
	}

	try {
	    // Remove all existing rows with the current componisten ID from the componisten_persoon table
	    Statement statement = connection.createStatement( );
	    statement.executeUpdate( "DELETE FROM componisten_persoon WHERE componisten_id = " + componistenId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in componisten_persoon
	insertTable( );
    }
}
