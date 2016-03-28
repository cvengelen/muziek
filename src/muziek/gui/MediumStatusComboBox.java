// Class to setup a ComboBox for medium_status
// Simple version: no need to add or edit items

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MediumStatusComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.MediumStatusComboBox" );

    private Map mediumStatusMap = new HashMap( );

    public MediumStatusComboBox( Connection conn,
			         int        selectedMediumStatusId ) {

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	try {
	    // Fill the combo box and hash table
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT medium_status_id, medium_status " +
							  "FROM medium_status ORDER BY medium_status" );
	    while ( resultSet.next( ) ) {
		String mediumStatusString = resultSet.getString( 2 );

		// Store the medium_status_id in the map indexed by the mediumStatusString
		mediumStatusMap.put( mediumStatusString, resultSet.getObject( 1 ) );

		// Add the mediumStatusString to the combo box
		addItem( mediumStatusString );

		// Check if this is the selected mediumStatus
		if ( resultSet.getInt( 1 ) == selectedMediumStatusId ) {
		    // Select this mediumStatus
		    setSelectedItem( mediumStatusString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 10 );
    }

    public int getSelectedMediumStatusId( ) {
	return getMediumStatusId( ( String )getSelectedItem( ) );
    }

    public int getMediumStatusId( String mediumStatusString ) {
	if ( mediumStatusString == null ) return 0;

	// Check if empty string is selected
	if ( mediumStatusString.length( ) == 0 ) return 0;

	// Get the medium_status_id from the map
	if ( mediumStatusMap.containsKey( mediumStatusString ) ) {
	    return ( ( Integer )mediumStatusMap.get( mediumStatusString ) ).intValue( );
	}

	return 0;
    }
}
