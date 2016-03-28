// Class to setup a TableModel for opus_deel

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.logging.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class OpusDeelTableModel extends AbstractTableModel {
    final private Logger logger = Logger.getLogger( "muziek.gui.OpusDeelTableModel" );

    private Connection conn;
    private String[ ] headings            = { "Opus-deel Nummer", "Opus-deel Titel" };
    private int[ ]    opusDeelNummer      = new int[ 50 ];
    private String[ ] opusDeelTitelString = new String[ 50 ];
    private int nOpusDeel = 0;
    private int opusId = 0;

    // Pattern to find a single quote in the opusdeel titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final private Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public OpusDeelTableModel( Connection conn, int opusId ) {
	this.conn = conn;
	this.opusId = opusId;

	// Setup the table for the given opus ID
	try {
	    Statement opusDeelStatement = conn.createStatement( );
	    ResultSet opusDeelResultSet = opusDeelStatement.executeQuery( "SELECT opus_deel_nummer, opus_deel_titel " +
									  "FROM opus_deel " +
									  "WHERE opus_deel.opus_id = " + opusId );

	    nOpusDeel = 0;
	    while ( opusDeelResultSet.next( ) ) {
		opusDeelNummer[ nOpusDeel ]      = opusDeelResultSet.getInt( 1 );
		opusDeelTitelString[ nOpusDeel ] = opusDeelResultSet.getString( 2 );
		nOpusDeel++;
	    }
	    logger.finer( "nOpusDeel: " + nOpusDeel );

	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return nOpusDeel; }

    public int getColumnCount( ) { return 2; }

    public boolean isCellEditable( int row, int col ) {
	// Do not allow editing the opus deel nummer
	if ( col == 0 ) return false;

	// Anything else is OK
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( column == 0 ) return String.valueOf( opusDeelNummer[ row ] );
	return opusDeelTitelString[ row ];
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= nOpusDeel ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	if ( column >= 2 ) {
	    logger.severe( "Invalid column: " + column );
	    return;
	}

	if ( column == 0 ) {
	    try {
		opusDeelNummer[ row ] = Integer.parseInt( ( String )object );
	    } catch ( Exception exception ) {
		logger.severe( "Could not get opus deel nummer from " +
			       object + " for row " + row );
	    }
	}

	if ( column == 1 ) opusDeelTitelString[ row ] = ( String )object;

	fireTableCellUpdated( row, column );
    }


    public void addRow( ) {
	this.opusDeelNummer[ nOpusDeel ] = nOpusDeel + 1;
	this.opusDeelTitelString[ nOpusDeel ] = "";
	this.nOpusDeel++;
	fireTableDataChanged( );
    }

    public void insertRow( int row ) {
	if ( ( row < 0 ) || ( row >= nOpusDeel ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	// Move the opus deel titel for all rows above row one up
	for ( int moveRow = nOpusDeel; moveRow >= row; moveRow-- ) {
	    opusDeelTitelString[ moveRow + 1 ] = opusDeelTitelString[ moveRow ];
	}

	// Initialize the inserted opus deel titel to an empty string
	opusDeelTitelString[ row ] = "";

	// Set the opus deel nummer for the last entry in the table
	opusDeelNummer[ nOpusDeel ] = nOpusDeel + 1;

	// Increment the number of entries in the table
	nOpusDeel++;

	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= nOpusDeel ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	// Move the opus deel titel for all rows above row one down
	for ( int copyRow = row; copyRow < ( nOpusDeel - 1 ); copyRow++ ) {
	    opusDeelTitelString[ copyRow ] = opusDeelTitelString[ copyRow + 1 ];
	}

	nOpusDeel--;

	fireTableDataChanged( );
    }

    // When inserting for a new opus record, the opusId still must be set:
    // only allow inserting in the table for other classes when the opus ID is specified.
    public void insertTable( int opusId ) {
	this.opusId = opusId;

	insertTable( );
    }

    private void insertTable( ) {
	// Check for valid opus ID
	if ( opusId == 0 ) {
	    logger.severe( "Opus not selected" );
	    return;
	}

	try {
	    // Insert new rows in the opus-deel table
	    Statement opusDeelStatement = conn.createStatement( );
	    for ( int opusDeelIndex = 0; opusDeelIndex < nOpusDeel; opusDeelIndex++ ) {
		// Matcher to find single quotes in opusDeelTitelString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		final Matcher quoteMatcher = quotePattern.matcher( opusDeelTitelString[ opusDeelIndex ] );

		String insertOpusDeelString =
		    "INSERT INTO opus_deel SET opus_id = " + opusId +
		    ", opus_deel_nummer = " + opusDeelNummer[ opusDeelIndex ] +
		    ", opus_deel_titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";

		logger.fine( "insertOpusDeelString: " + insertOpusDeelString );

		int nUpdate = opusDeelStatement.executeUpdate( insertOpusDeelString );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in opus_deel" );
		    return;
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void updateTable( ) {
	try {
	    // Remove all existing rows with the current opus ID from the opus_deel table
	    Statement opusDeelStatement = conn.createStatement( );
	    opusDeelStatement.executeUpdate( "DELETE FROM opus_deel WHERE opus_id = " + opusId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in opus_deel
	insertTable( );
    }
}
