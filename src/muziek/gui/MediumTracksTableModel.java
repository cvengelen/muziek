// Class to setup a TableModel for all tracks on a submedium

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
import java.util.logging.*;

public class MediumTracksTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.gui.MediumTracksTableModel" );

    private Connection conn;
    private String[ ] headings            = { "Track", "Track tijd",
					      "Opus titel", "Deel", "Deel titel",
					      "Componisten", "Opus type", "Opus subtype" };
    private int[ ]    trackNummer         = new int[ 60 ];
    private String[ ] trackTijdString     = new String[ 60 ];
    private String[ ] opusTitelString     = new String[ 60 ];
    private int[ ]    opusDeelNummer      = new int[ 60 ];
    private String[ ] opusDeelTitelString = new String[ 60 ];
    private String[ ] componistenString   = new String[ 60 ];
    private String[ ] opusTypeString      = new String[ 60 ];
    private String[ ] opusSubtypeString   = new String[ 60 ];

    private int nTracks = 0;

    private int mediumId = 0;
    private String submediumString = null;


    // Constructor
    public MediumTracksTableModel( Connection conn,
				   int        mediumId,
				   String     submediumString ) {
	this.conn = conn;
	this.mediumId = mediumId;
	this.submediumString = submediumString;

	setupMediumTracksTableModel( );
    }

    public void setupMediumTracksTableModel( String submediumString ) {
	this.submediumString = submediumString;

	setupMediumTracksTableModel( );
    }

    private void setupMediumTracksTableModel( ) {
	// Setup the table for the given medium, submedium
	try {
	    String mediumQueryString =
		"SELECT tracks.track_nummer, tracks.tijd, opus.opus_titel, " +
		"tracks.opus_deel_nummer, opus_deel.opus_deel_titel, " +
		"componisten.componisten, type.type, subtype.subtype " +
		"FROM tracks " +
		"LEFT JOIN opus ON opus.opus_id = tracks.opus_id " +
		"LEFT JOIN opus_deel ON " +
		"opus_deel.opus_id = tracks.opus_id AND " +
		"opus_deel.opus_deel_nummer = tracks.opus_deel_nummer " +
		"LEFT JOIN componisten ON componisten.componisten_id = opus.componisten_id " +
		"LEFT JOIN type ON type.type_id = opus.type_id " +
		"LEFT JOIN subtype ON subtype.subtype_id = opus.subtype_id " +
		"WHERE tracks.medium_id = " + mediumId + " AND "+
		"tracks.submedium = '" + submediumString + "' " +
		"ORDER BY tracks.track_nummer";

	    Statement tracksStatement = conn.createStatement( );
	    ResultSet tracksResultSet = tracksStatement.executeQuery( mediumQueryString );

	    nTracks = 0;
	    while ( tracksResultSet.next( ) ) {
		trackNummer[ nTracks ]         = tracksResultSet.getInt( 1 );
		trackTijdString[ nTracks ]     = tracksResultSet.getString( 2 );
		opusTitelString[ nTracks ]     = tracksResultSet.getString( 3 );
		opusDeelNummer[ nTracks ]      = tracksResultSet.getInt( 4 );
		opusDeelTitelString[ nTracks ] = tracksResultSet.getString( 5 );
		componistenString[ nTracks ]   = tracksResultSet.getString( 6 );
		opusTypeString[ nTracks ]      = tracksResultSet.getString( 7 );
		opusSubtypeString[ nTracks ]   = tracksResultSet.getString( 8 );
		nTracks++;
	    }
	    logger.fine( "nTracks: " + nTracks );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException ex ) {
	    logger.severe( "SQLException: " + ex.getMessage( ) );
	}
    }

    public int getRowCount( ) { return nTracks; }

    public int getColumnCount( ) { return 8; }

    public boolean isCellEditable( int row, int col ) {
	// Do not allow editing
	return false;
    }

    public Object getValueAt( int row, int column ) {
	if ( column == 0 ) return String.valueOf( trackNummer[ row ] );
	if ( column == 1 ) return trackTijdString[ row ];
	if ( column == 2 ) return opusTitelString[ row ];
	if ( column == 3 ) return String.valueOf( opusDeelNummer[ row ] );
	if ( column == 4 ) return opusDeelTitelString[ row ];
	if ( column == 5 ) return componistenString[ row ];
	if ( column == 6 ) return opusTypeString[ row ];
	if ( column == 7 ) return opusSubtypeString[ row ];

	return "";
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public int getNumberOfTracks( ) { return nTracks; }

    public String getTotalTrackTime( ) {
	int totalTime = 0;
	int hours = 0;
	int minutes = 0;
	int seconds = 0;

	for ( int track = 0; track < nTracks; track++ ) {
	    // Get the number of hours from characters 0-1
	    try {
		hours = Integer.parseInt( trackTijdString[ track ].substring( 0, 2 ) );
	    } catch ( Exception exception ) {
		hours = 0;
	    }

	    // Get the number of minutes from characters 3-4
	    try {
		minutes = Integer.parseInt( trackTijdString[ track ].substring( 3, 5 ) );
	    } catch ( Exception exception ) {
		minutes = 0;
	    }

	    // Get the number of seconsd from characters 6-7
	    try {
		seconds = Integer.parseInt( trackTijdString[ track ].substring( 6 ) );
	    } catch ( Exception exception ) {
		seconds = 0;
	    }

	    // Increment the total time in seconds
	    totalTime += seconds + minutes * 60 + hours * 3600;
	}

	// Revert the total time in seconds to time string hh:mm:ss

	// Get the number of hours, e.g., 3723 / 3600 -> 1
	hours = totalTime / 3600;

	// Get the number of remaining seconds, e.g., 3723 % 3600 -> 123
	totalTime = totalTime % 3600;

	// Get the number of remaining minutes, e.g., 123 / 60 -> 2
	minutes = totalTime / 60;

	// Get the number of remaining seconds, e.g., 123 % 60 -> 3
	seconds = totalTime % 60;

	// Use format to get output prefixed with 0 if less than 10
	DecimalFormat decimalFormat = new DecimalFormat( "00" );

	// Return the time string as hh:mm:ss
	return ( decimalFormat.format( hours ) + ":" +
		 decimalFormat.format( minutes ) + ":" +
		 decimalFormat.format( seconds ) );
    }
}
