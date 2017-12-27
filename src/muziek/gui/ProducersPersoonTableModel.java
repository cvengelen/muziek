// Class to setup a TableModel for producers_persoon

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


public class ProducersPersoonTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.gui.ProducersPersoonTableModel" );

    private Connection connection;
    private String[ ] headings      = { "Producers-Persoon" };

    class ProducerRecord {
	int	persoonId;
	String  persoonString;

	public ProducerRecord( int    persoonId,
			       String persoonString ) {
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	}
    }

    ArrayList producerRecordList = new ArrayList( 10 );
    private int producersId = 0;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public ProducersPersoonTableModel( Connection connection ) {
	this.connection = connection;
    }

    // Constructor
    public ProducersPersoonTableModel( Connection connection, int producersId ) {
	this.connection = connection;
	showTable( producersId );
    }

    public int getRowCount( ) { return producerRecordList.size( ); }

    public int getColumnCount( ) { return 1; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public boolean isCellEditable( int row, int column ) { return true; }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= producerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}
	return ( ( ProducerRecord )producerRecordList.get( row ) ).persoonString;
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= producerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final ProducerRecord producerRecord =
	    ( ProducerRecord )producerRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 0:
		String persoonString = ( String )object;
		if ( ( ( persoonString == null ) || ( persoonString.length( ) == 0 ) ) &&
		     ( producerRecord.persoonString != null ) &&
		     ( producerRecord.persoonString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		    producerRecord.persoonString = persoonString;
		} else if ( ( persoonString != null ) &&
			    ( !persoonString.equals( producerRecord.persoonString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( persoonString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    producerRecord.persoonString = persoonString;
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

	updateString = "UPDATE persoon SET " + updateString + " WHERE persoon_id = " + producerRecord.persoonId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with persoon_id " + producerRecord.persoonId +
			       " in persoon, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	producerRecordList.set( row, producerRecord );

	fireTableCellUpdated( row, column );
    }

    public void showTable( int producersId ) {
	this.producersId = producersId;
	showTable( );
    }

    public void showTable( ) {
	// Clear the producers list
	producerRecordList.clear( );

	try {
	    Statement producersStmt = connection.createStatement( );
	    ResultSet producersResultSet = producersStmt.executeQuery( "SELECT persoon.persoon_id, persoon.persoon " +
								       "FROM producers_persoon " +
								       "LEFT JOIN persoon ON persoon.persoon_id = producers_persoon.persoon_id " +
								       "WHERE producers_persoon.producers_id = " + producersId );

	    while ( producersResultSet.next( ) ) {
		producerRecordList.add( new ProducerRecord( producersResultSet.getInt( 1 ),
							    producersResultSet.getString( 2 ) ) );
	    }

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getPersoonId( int row ) {
	if ( ( row < 0 ) || ( row >= producerRecordList.size( ) ) ) {
	    logger.severe( "invalid row: " + row );
	    return 0;
	}

	return ( ( ProducerRecord )producerRecordList.get( row ) ).persoonId;
    }

    public void addRow( int persoonId, String persoonString ) {
	producerRecordList.add( new ProducerRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void replaceRow( int row, int persoonId, String persoonString ) {
	if ( ( row < 0 ) || ( row >= producerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	producerRecordList.set( row,
				new ProducerRecord( persoonId, persoonString ) );
	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= producerRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	producerRecordList.remove( row );
	fireTableDataChanged( );
    }


    // When inserting for a new producers record, the producersId still must be set:
    // only allow inserting in the table for other classes when the producers ID is specified.
    public void insertTable( int producersId ) {
	this.producersId = producersId;

	insertTable( );
    }

    private void insertTable( ) {
	// Check for valid producers ID
	if ( producersId == 0 ) {
	    logger.severe( "Producers not selected" );
	    return;
	}

	try {
	    // Insert new rows in the producers_persoon table
	    Statement statement = connection.createStatement( );
	    for ( int producerIndex = 0; producerIndex < producerRecordList.size( ); producerIndex++ ) {
		int persoonId = ( ( ProducerRecord )producerRecordList.get( producerIndex ) ).persoonId;
		int nUpdate = statement.executeUpdate( "INSERT INTO producers_persoon SET " +
						       "producers_id = " + producersId +
						       ", persoon_id = " + persoonId );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in producers_persoon for index " + producerIndex );
		    return;
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void updateTable( ) {
	// Check for valid producers ID
	if ( producersId == 0 ) {
	    logger.severe( "producers not selected" );
	    return;
	}

	try {
	    // Remove all existing rows with the current producers ID from the producers_persoon table
	    Statement statement = connection.createStatement( );
	    statement.executeUpdate( "DELETE FROM producers_persoon WHERE producers_id = " + producersId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in producers_persoon
	insertTable( );
    }
}
