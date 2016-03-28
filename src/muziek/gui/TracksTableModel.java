// Class to setup a TableModel for tracks

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class TracksTableModel extends AbstractTableModel {
    private Connection conn;
    private String[ ] headings            = { "Sub-medium", "Track #", "Track tijd", "Opus-deel #", "Opus-deel titel" };
    private String[ ] submediumString     = new String[ 60 ];
    private int[ ]    trackNummer         = new int[ 60 ];
    private String[ ] trackTijdString     = new String[ 60 ];
    private int[ ]    opusDeelNummer      = new int[ 60 ];
    private String[ ] opusDeelTitelString = new String[ 60 ];
    private int nTracks  = 0;
    private int mediumId = 0;
    private int opusId  = 0;

    // Constructor
    public TracksTableModel( Connection conn, int mediumId, int opusId ) {
	this.conn = conn;
	this.mediumId = mediumId;
	this.opusId = opusId;

	// No action id opus not set
	if ( opusId == 0 ) return;

	// Setup the table for the given opus ID
	try {
	    Statement tracksStatement = conn.createStatement( );
	    ResultSet tracksResultSet = tracksStatement.executeQuery( "SELECT tracks.submedium, " +
								      "tracks.track_nummer, tracks.tijd, " +
								      "tracks.opus_deel_nummer, " +
								      "opus_deel.opus_deel_titel " +
								      "FROM tracks " +
								      "LEFT JOIN opus_deel ON " +
								      "tracks.opus_id = opus_deel.opus_id AND " +
								      "tracks.opus_deel_nummer = opus_deel.opus_deel_nummer " +
								      "WHERE tracks.medium_id = " + mediumId + " AND "+
								      "tracks.opus_id = " + opusId );

	    nTracks = 0;
	    while ( tracksResultSet.next( ) ) {
		submediumString[ nTracks ]     = tracksResultSet.getString( 1 );
		trackNummer[ nTracks ]         = tracksResultSet.getInt( 2 );
		trackTijdString[ nTracks ]     = tracksResultSet.getString( 3 );
		opusDeelNummer[ nTracks ]      = tracksResultSet.getInt( 4 );
		opusDeelTitelString[ nTracks ] = tracksResultSet.getString( 5 );
		nTracks++;
	    }
	    System.out.println( "TracksTableModel.showTable, nTracks: " + nTracks );
	    fireTableDataChanged( );
	} catch ( SQLException ex ) {
	    System.err.println( "TracksTableModel.TracksTableModel SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }

    public int getRowCount( ) { return nTracks; }

    public int getColumnCount( ) { return 5; }

    public boolean isCellEditable( int row, int col ) {
	// Do not allow editing the opus-deel titel
	if ( col == 4 ) return false;

	// Anything else is OK
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( column == 0 ) return submediumString[ row ];
	if ( column == 1 ) return String.valueOf( trackNummer[ row ] );
	if ( column == 2 ) return trackTijdString[ row ];
	if ( column == 3 ) return String.valueOf( opusDeelNummer[ row ] );
	return opusDeelTitelString[ row ];
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public int getNumberOfTracks( ) { return nTracks; }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= nTracks ) ) {
	    System.err.println( "TracksTableModel.setValueAt, invalid row: " + row );
	    return;
	}

	if ( column >= 4 ) {
	    System.err.println( "TracksTableModel.setValueAt, invalid column: " + column );
	    return;
	}

	if ( column == 0 ) submediumString[ row ] = ( String )object;
	if ( column == 1 ) {
	    try {
		trackNummer[ row ] = Integer.parseInt( ( String )object );
	    } catch ( Exception exception ) {
		System.err.println( "TracksTableModel.setValueAt: could not get track-nummer from " +
				    object + " for row " + row );
	    }
	}
	if ( column == 2 ) trackTijdString[ row ] = ( String )object;
	if ( column == 3 ) {
	    try {
		opusDeelNummer[ row ] = Integer.parseInt( ( String )object );
	    } catch ( Exception exception ) {
		System.err.println( "TracksTableModel.setValueAt: could not get opus-deel-nummer from " +
				    object + " for row " + row );
	    }

	    try {
		// Find the opus deel titel for this opus deel nummer
		Statement tracksStatement = conn.createStatement( );
		ResultSet tracksResultSet = tracksStatement.executeQuery( "SELECT opus_deel_titel FROM opus_deel" +
									  " WHERE opus_id = " + opusId +
									  " AND opus_deel_nummer = " +
									  opusDeelNummer[ row ] );
		// Set the found value in the table (if any)
		if ( tracksResultSet.next( ) ) {
		    opusDeelTitelString[ row ] = tracksResultSet.getString( 1 );
		}
	    } catch ( SQLException ex ) {
		System.err.println( "TracksTableModel.setValueAt SQLException:\n\t" +
				    ex.getMessage( ) );
	    }
	}

	fireTableCellUpdated( row, column );
    }

    public void initTable( int mediumId, int opusId ) {
	boolean mediumIdChanged = ( mediumId != this.mediumId ); 
	boolean opusIdChanged   = ( opusId   != this.opusId ); 
	this.mediumId = mediumId;
	this.opusId = opusId;

	// Check for valid medium ID
	if ( mediumId == 0 ) {
	    // No valid medium ID: reset number of tracks and return
	    nTracks = 0;
	    return;
	}

	try {
	    int submedium = 1;
	    int initTrackNummer = 1;
	    Statement tracksStatement = conn.createStatement( );
	    ResultSet tracksResultSet;

	    // Find the maximum submedium used for this medium
	    tracksResultSet = tracksStatement.executeQuery( "SELECT MAX( submedium ) FROM tracks " +
							    "WHERE medium_id = " + mediumId );
	    if ( tracksResultSet.next( ) ) {
		submedium = tracksResultSet.getInt( 1 );
	    }

	    // Find the maximum track number used for this medium
	    tracksResultSet = tracksStatement.executeQuery( "SELECT MAX( track_nummer ) FROM tracks" +
							    " WHERE medium_id = " + mediumId +
							    " AND submedium = " + submedium );
	    if ( tracksResultSet.next( ) ) {
		initTrackNummer = tracksResultSet.getInt( 1 ) + 1;
	    }

	    // Find the set of opus deel nummers and titels for this opus
	    tracksResultSet = tracksStatement.executeQuery( "SELECT opus_deel_nummer, opus_deel_titel " +
							    "FROM opus_deel WHERE " +
							    "opus_id = " + opusId );

	    // Initialize the table starting from initTrackNummer, using opus_deel_nummer and opus_deel_titel
	    int currentNumberOfTracks = nTracks;
	    nTracks = 0;
	    while ( tracksResultSet.next( ) ) {
		// Only initialize/reset medium related data when
		// the medium changed, or when adding new tracks
		if ( mediumIdChanged || ( nTracks >= currentNumberOfTracks ) ) {
		    submediumString[ nTracks ]     = String.valueOf( submedium );
		    trackNummer[ nTracks ]         = initTrackNummer;
		    trackTijdString[ nTracks ]     = "00:00:00";
		}

		// Always increment the track nummer, even if not updated
		initTrackNummer++;

		// Only initialize/reset opus-deel related data when
		// the opus changed, or when adding new tracks
		if ( opusIdChanged || ( nTracks >= currentNumberOfTracks ) ) {
		    opusDeelNummer[ nTracks ]      = tracksResultSet.getInt( 1 );
		    opusDeelTitelString[ nTracks ] = tracksResultSet.getString( 2 );
		}
		nTracks++;
	    }

	    // Initialize at least one entry
	    if ( nTracks == 0 ) {
		// Only initialize/reset medium related data when
		// the medium changed, or when there are no tracks
		if ( mediumIdChanged || ( currentNumberOfTracks == 0 ) ) {
		    submediumString[ nTracks ]     = String.valueOf( submedium );
		    trackNummer[ nTracks ]         = initTrackNummer++;
		    trackTijdString[ nTracks ]     = "00:00:00";
		}

		// Only initialize/reset opus deel related data when
		// the opus changed, or when there are no tracks
		if ( opusIdChanged || ( currentNumberOfTracks == 0 ) ) {
		    opusDeelNummer[ nTracks ]      = 0;
		    opusDeelTitelString[ nTracks ] = "";
		}
		nTracks++;
	    }
	
	    // System.out.println( "TracksTableModel.showTable, nTracks: " + nTracks );
	    fireTableDataChanged( );
	} catch ( SQLException ex ) {
	    System.err.println( "TracksTableModel.initTable SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }


    public void insertRow( int row ) {
	if ( ( row < 0 ) || ( row >= nTracks ) ) {
	    System.err.println( "TracksTableModel.insertRow, invalid row: " + row );
	    return;
	}
	for ( int moveRow = nTracks; moveRow >= row; moveRow-- ) {
	    submediumString[ moveRow + 1 ]     = submediumString[ moveRow ];
	    trackNummer[ moveRow + 1 ]         = trackNummer[ moveRow ] + 1;
	    trackTijdString[ moveRow + 1 ]     = trackTijdString[ moveRow ];
	    opusDeelNummer[ moveRow + 1 ]      = opusDeelNummer[ moveRow ];
	    opusDeelTitelString[ moveRow + 1 ] = opusDeelTitelString[ moveRow ];
	}

	// Initialize all except the track nummer, which stays the same
	submediumString[ row ]     = "1";
	trackTijdString[ row ]     = "00:00:00";
	opusDeelNummer[ row ]      = 0;
	opusDeelTitelString[ row ] = "";

	// One track more
	nTracks++;

	fireTableDataChanged( );
    }


    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= nTracks ) ) {
	    System.err.println( "TracksTableModel.removeRow, invalid row: " + row );
	    return;
	}
	// Move all rows above row one down, except for the trackNummer column
	for ( int moveRow = row; moveRow < ( nTracks - 1 ); moveRow++ ) {
	    submediumString[ moveRow ]     = submediumString[ moveRow ];
	    trackTijdString[ moveRow ]     = trackTijdString[ moveRow ];
	    opusDeelNummer[ moveRow ]      = opusDeelNummer[ moveRow ];
	    opusDeelTitelString[ moveRow ] = opusDeelTitelString[ moveRow ];
	}

	// One track less
	nTracks--;

	fireTableDataChanged( );
    }


    public void addRow( ) {
	int initTrackNummer = 1;

	// Check if table is empty
	if ( nTracks == 0 ) {
	    try {
		Statement tracksStatement = conn.createStatement( );

		// Find the maximum track number used for this medium
		ResultSet tracksResultSet = tracksStatement.executeQuery( "SELECT MAX( track_nummer ) FROM tracks " +
									  "WHERE medium_id = " + mediumId );
		if ( tracksResultSet.next( ) ) {
		    initTrackNummer = tracksResultSet.getInt( 1 ) + 1;
		}
	    } catch ( SQLException ex ) {
		System.err.println( "TracksTableModel.addRow SQLException:\n\t" +
				    ex.getMessage( ) );
	    }
	} else {
	    // get track nummer from previous track
	    initTrackNummer = trackNummer[ nTracks - 1 ] + 1;
	}

	submediumString[ nTracks ]     = "1";
	trackNummer[ nTracks ]         = initTrackNummer;
	trackTijdString[ nTracks ]     = "00:00:00";
	opusDeelNummer[ nTracks ]      = 0;
	opusDeelTitelString[ nTracks ] = "";

	// One track more
	nTracks++;

	fireTableDataChanged( );
    }

    public void insertTable( ) {
	// Check for valid medium ID
	if ( mediumId == 0 ) {
	    System.err.println( "\nTracksTableMode.insertTable: medium not selected" );
	    return;
	}

	// Check for valid opus ID
	if ( opusId == 0 ) {
	    System.err.println( "\nTracksTableMode.insertTable: opus not selected" );
	    return;
	}

	try {
	    // Insert new rows in the tracks table
	    Statement tracksStatement = conn.createStatement( );
	    for ( int tracksIndex = 0; tracksIndex < nTracks; tracksIndex++ ) {
		int nUpdate = tracksStatement.executeUpdate( "INSERT INTO tracks SET " +
							     "medium_id = " + mediumId + ", opus_id = " + opusId +
							     ", submedium = '" + submediumString[ tracksIndex ] + "'" +
							     ", track_nummer = " + trackNummer[ tracksIndex ] +
							     ", tijd = '" + trackTijdString[ tracksIndex ] + "'" +
							     ", opus_deel_nummer = " + opusDeelNummer[ tracksIndex ] );
		if ( nUpdate == 0 ) {
		    System.err.println( "Could not insert in tracks" );
		    return;
		}
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "TracksTableModel.insertTable SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }

    public void updateTable( ) {
	try {
	    // Remove all existing rows with the current opus ID from the tracks table
	    Statement tracksStatement = conn.createStatement( );
	    tracksStatement.executeUpdate( "DELETE FROM tracks WHERE medium_id = " + mediumId +
					   " AND opus_id = " + opusId );
	} catch ( SQLException ex ) {
	    System.err.println( "TracksTableModel.updateTable SQLException:\n\t" +
				ex.getMessage( ) );
	}

	// Insert the table in tracks
	insertTable( );
    }
}
