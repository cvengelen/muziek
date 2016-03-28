// Class to setup a ComboBox for subgenre
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

public class SubgenreComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.SubgenreComboBox" );

    private Map subgenreMap = new HashMap( );

    public SubgenreComboBox( Connection conn,
			     int        selectedSubgenreId ) {

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	try {
	    // Fill the combo box and hash table
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT subgenre_id, subgenre " +
							  "FROM subgenre ORDER BY subgenre" );
	    while ( resultSet.next( ) ) {
		String subgenreString = resultSet.getString( 2 );

		// Store the subgenre_id in the map indexed by the subgenreString
		subgenreMap.put( subgenreString, resultSet.getObject( 1 ) );

		// Add the subgenreString to the combo box
		addItem( subgenreString );

		// Check if this is the selected subgenre
		if ( resultSet.getInt( 1 ) == selectedSubgenreId ) {
		    // Select this subgenre
		    setSelectedItem( subgenreString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 10 );
    }

    public int getSelectedSubgenreId( ) {
	return getSubgenreId( ( String )getSelectedItem( ) );
    }

    public int getSubgenreId( String subgenreString ) {
	if ( subgenreString == null ) return 0;

	// Check if empty string is selected
	if ( subgenreString.length( ) == 0 ) return 0;

	// Get the subgenre_id from the map
	if ( subgenreMap.containsKey( subgenreString ) ) {
	    return ( ( Integer )subgenreMap.get( subgenreString ) ).intValue( );
	}

	return 0;
    }
}
