// Class to setup a TableModel for musici_ensemble

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.ArrayList;
import java.util.logging.*;
import java.util.regex.*;


public class MusiciEnsembleTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "boeken.gui.MusiciEnsembleTableModel" );

    private Connection connection;

    private String[ ] headings = { "Ensemble" };

    class EnsembleRecord {
	int	ensembleId;
	String  ensembleString;

	public EnsembleRecord( int    ensembleId,
			       String ensembleString ) {
	    this.ensembleId = ensembleId;
	    this.ensembleString = ensembleString;
	}
    }

    ArrayList ensembleRecordList = new ArrayList( 10 );
    private int musiciId = 0;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public MusiciEnsembleTableModel( Connection connection ) {
	this.connection = connection;
    }

    // Constructor
    public MusiciEnsembleTableModel( Connection connection, int musiciId ) {
	this.connection = connection;
	showTable( musiciId );
    }

    public int getRowCount( ) { return ensembleRecordList.size( ); }

    public int getColumnCount( ) { return 1; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public boolean isCellEditable( int row, int column ) { return true; }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= ensembleRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}
	return ( ( EnsembleRecord )ensembleRecordList.get( row ) ).ensembleString;
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= ensembleRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final EnsembleRecord ensembleRecord =
	    ( EnsembleRecord )ensembleRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 0:
		String ensembleString = ( String )object;
		if ( ( ( ensembleString == null ) || ( ensembleString.length( ) == 0 ) ) &&
		     ( ensembleRecord.ensembleString != null ) &&
		     ( ensembleRecord.ensembleString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		    ensembleRecord.ensembleString = ensembleString;
		} else if ( ( ensembleString != null ) &&
			    ( !ensembleString.equals( ensembleRecord.ensembleString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( ensembleString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    ensembleRecord.ensembleString = ensembleString;
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
			 " WHERE persoon_id = " + ensembleRecord.ensembleId );
	logger.info( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with persoon_id " + ensembleRecord.ensembleId +
			       " in persoon, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	ensembleRecordList.set( row, ensembleRecord );

	fireTableCellUpdated( row, column );
    }

    public void showTable( int musiciId ) {
	this.musiciId = musiciId;
	showTable( );
    }

    public void showTable( ) {
	// Clear the ensemble list
	ensembleRecordList.clear( );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT ensemble.ensemble_id, ensemble.ensemble " +
							  "FROM musici_ensemble " +
							  "LEFT JOIN ensemble ON ensemble.ensemble_id = musici_ensemble.ensemble_id " +
							  "WHERE musici_ensemble.musici_id = " + musiciId );

	    while ( resultSet.next( ) ) {
		ensembleRecordList.add( new EnsembleRecord( resultSet.getInt( 1 ),
							    resultSet.getString( 2 ) ) );
	    }

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void addRow( int ensembleId, String ensembleString ) {
	ensembleRecordList.add( new EnsembleRecord( ensembleId, ensembleString ) );
	fireTableDataChanged( );
    }

    public void replaceRow( int row, int ensembleId, String ensembleString ) {
	if ( ( row < 0 ) || ( row >= ensembleRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	ensembleRecordList.set( row,
				new EnsembleRecord( ensembleId, ensembleString ) );
	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= ensembleRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	ensembleRecordList.remove( row );
	fireTableDataChanged( );
    }


    // When inserting for a new musici record, the musiciId still must be set:
    // only allow inserting in the table for other classes when the musici ID is specified.
    public void insertTable( int musiciId ) {
	this.musiciId = musiciId;

	insertTable( );
    }

    private void insertTable( ) {
	// Check for valid musici ID
	if ( musiciId == 0 ) {
	    logger.severe( "musici not selected" );
	    return;
	}

	try {
	    // Insert new rows in the musici_ensemble table
	    Statement statement = connection.createStatement( );
	    for ( int ensembleIndex = 0; ensembleIndex < ensembleRecordList.size( ); ensembleIndex++ ) {
		int ensembleId = ( ( EnsembleRecord )ensembleRecordList.get( ensembleIndex ) ).ensembleId;
		int nUpdate = statement.executeUpdate( "INSERT INTO musici_ensemble SET musici_id = " + musiciId +
						       ", ensemble_id = " + ensembleId );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in musici_persoon for index " + ensembleIndex );
		    return;
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void updateTable( ) {
	// Check for valid musici ID
	if ( musiciId == 0 ) {
	    logger.severe( "musici not selected" );
	    return;
	}

	try {
	    // Remove all existing rows with the current musici ID from the musici_ensemble table
	    Statement statement = connection.createStatement( );
	    statement.executeUpdate( "DELETE FROM musici_ensemble WHERE musici_id = " + musiciId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in musici_ensemble
	insertTable( );
    }
}
