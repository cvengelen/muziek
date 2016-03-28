// Class to setup a ComboBox for tijdperk
// Simple version: no need to add or edit items

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.Logger;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class TijdperkComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.TijdperkComboBox" );

    private Map tijdperkMap = new HashMap( );

    public TijdperkComboBox( Connection connection,
			     int        selectedTijdperkId ) {

	// Add first empty item
	addItem( "" );

	try {
	    // Fill the combo box and hash table
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT tijdperk_id, tijdperk " +
							  "FROM tijdperk ORDER BY tijdperk" );
	    while ( resultSet.next( ) ) {
		String tijdperkString = resultSet.getString( 2 );

		// Store the tijdperk_id in the map indexed by the tijdperkString
		tijdperkMap.put( tijdperkString, resultSet.getObject( 1 ) );

		// Add the tijdperkString to the combo box
		addItem( tijdperkString );

		// Check if this is the selected tijdperk
		if ( resultSet.getInt( 1 ) == selectedTijdperkId ) {
		    // Select this tijdperk
		    setSelectedItem( tijdperkString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 10 );
    }

    public int getSelectedTijdperkId( ) {
	return getTijdperkId( ( String )getSelectedItem( ) );
    }

    public int getTijdperkId( String tijdperkString ) {
	if ( tijdperkString == null ) return 0;

	// Check if empty string is selected
	if ( tijdperkString.length( ) == 0 ) return 0;

	// Get the tijdperk_id from the map
	if ( tijdperkMap.containsKey( tijdperkString ) ) {
	    return ( ( Integer )tijdperkMap.get( tijdperkString ) ).intValue( );
	}

	return 0;
    }
}
