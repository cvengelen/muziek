// Class to setup a TableModel for tekstschrijver_persoon

package muziek.componisten;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.table.*;

import java.util.ArrayList;
import java.util.logging.*;
import java.util.regex.*;


public class TekstschrijverPersoonTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.componisten.TekstschrijverPersoonTableModel" );

    private Connection connection;
    private String[ ] headings = { "Tekstschrijver-Persoon" };

    class TekstschrijverRecord {
	int	persoonId;
	String  persoonString;

	public TekstschrijverRecord( int    persoonId,
				     String persoonString ) {
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	}
    }

    ArrayList tekstschrijverRecordList = new ArrayList( 10 );
    private int componistenId = 0;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public TekstschrijverPersoonTableModel( Connection connection ) {
	this.connection = connection;
    }

    // Constructor
    public TekstschrijverPersoonTableModel( Connection connection, int componistenId ) {
	this.connection = connection;
	showTable( componistenId );
    }

    public int getRowCount( ) { return tekstschrijverRecordList.size( ); }

    public int getColumnCount( ) { return 1; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public boolean isCellEditable( int row, int column ) { return true; }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= tekstschrijverRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}
	return ( ( TekstschrijverRecord )tekstschrijverRecordList.get( row ) ).persoonString;
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= tekstschrijverRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final TekstschrijverRecord tekstschrijverRecord =
	    ( TekstschrijverRecord )tekstschrijverRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 0:
		String persoonString = ( String )object;
		if ( ( ( persoonString == null ) || ( persoonString.length( ) == 0 ) ) &&
		     ( tekstschrijverRecord.persoonString != null ) &&
		     ( tekstschrijverRecord.persoonString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		    tekstschrijverRecord.persoonString = persoonString;
		} else if ( ( persoonString != null ) &&
			    ( !persoonString.equals( tekstschrijverRecord.persoonString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( persoonString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    tekstschrijverRecord.persoonString = persoonString;
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

	updateString = ( "UPDATE persoon SET " + updateString +
			 " WHERE persoon_id = " + tekstschrijverRecord.persoonId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with persoon_id " + tekstschrijverRecord.persoonId +
			       " in persoon, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	tekstschrijverRecordList.set( row, tekstschrijverRecord );

	fireTableCellUpdated( row, column );
    }

    public void showTable( int componistenId ) {
	this.componistenId = componistenId;
	showTable( );
    }

    public void showTable( ) {
	// Clear the tekstschrijver list
	tekstschrijverRecordList.clear( );

	try {
	    Statement tekstschrijverStmt = connection.createStatement( );
	    ResultSet tekstschrijverResultSet = tekstschrijverStmt.executeQuery( "SELECT persoon.persoon_id, persoon.persoon " +
										 "FROM tekstschrijver_persoon " +
										 "LEFT JOIN persoon ON persoon.persoon_id = tekstschrijver_persoon.persoon_id " +
										 "WHERE tekstschrijver_persoon.componisten_id = " + componistenId );

	    while ( tekstschrijverResultSet.next( ) ) {
		tekstschrijverRecordList.add( new TekstschrijverRecord( tekstschrijverResultSet.getInt( 1 ),
									tekstschrijverResultSet.getString( 2 ) ) );
	    }

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getPersoonId( int row ) {
	if ( ( row < 0 ) || ( row >= tekstschrijverRecordList.size( ) ) ) {
	    logger.severe( "invalid row: " + row );
	    return 0;
	}

	return ( ( TekstschrijverRecord )tekstschrijverRecordList.get( row ) ).persoonId;
    }

    public void addRow( int persoonId, String persoonString ) {
	tekstschrijverRecordList.add( new TekstschrijverRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void replaceRow( int row, int persoonId, String persoonString ) {
	if ( ( row < 0 ) || ( row >= tekstschrijverRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	tekstschrijverRecordList.set( row,
				 new TekstschrijverRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= tekstschrijverRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	tekstschrijverRecordList.remove( row );
	fireTableDataChanged( );
    }

    // When inserting for a new tekstschrijver record, the componistenId still must be set:
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
	    // Insert new rows in the tekstschrijver_persoon table
	    Statement statement = connection.createStatement( );
	    for ( int tekstschrijverIndex = 0; tekstschrijverIndex < tekstschrijverRecordList.size( ); tekstschrijverIndex++ ) {
		int persoonId = ( ( TekstschrijverRecord )tekstschrijverRecordList.get( tekstschrijverIndex ) ).persoonId;
		int nUpdate = statement.executeUpdate( "INSERT INTO tekstschrijver_persoon SET " +
						       "componisten_id = " + componistenId +
						       ", persoon_id = " + persoonId );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in tekstschrijver_persoon for index " + tekstschrijverIndex );
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
	    // Remove all existing rows with the current componisten ID from the tekstschrijver_persoon table
	    Statement statement = connection.createStatement( );
	    statement.executeUpdate( "DELETE FROM tekstschrijver_persoon WHERE componisten_id = " + componistenId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in tekstschrijver_persoon
	insertTable( );
    }
}
