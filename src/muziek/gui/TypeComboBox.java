// Class to setup a ComboBox for type
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

public class TypeComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.TypeComboBox" );

    private Map typeMap = new HashMap( );

    public TypeComboBox( Connection conn,
			 int        selectedTypeId ) {

	// Add empty item for when type is not applicable
	addItem( "" );

	try {
	    // Fill the combo box and hash table
	    Statement statement = conn.createStatement( );

	    // Note that type records must be ordered by type_id.
	    ResultSet resultSet = statement.executeQuery( "SELECT type_id, type " +
							  "FROM type ORDER BY type_id" );

	    while ( resultSet.next( ) ) {
		String typeString = resultSet.getString( 2 );

		// Store the type_id in the map indexed by the typeString
		typeMap.put( typeString, resultSet.getObject( 1 ) );

		// Add the typeString to the combo box
		addItem( typeString );

		// Check if this is the selected type
		if ( resultSet.getInt( 1 ) == selectedTypeId ) {
		    // Select this type
		    setSelectedItem( typeString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 10 );
    }

    public int getSelectedTypeId( ) {
	return getTypeId( ( String )getSelectedItem( ) );
    }

    public int getTypeId( String typeString ) {
	if ( typeString == null ) return 0;

	// Check if empty string is selected
	if ( typeString.length( ) == 0 ) return 0;

	// Get the type_id from the map
	if ( typeMap.containsKey( typeString ) ) {
	    return ( ( Integer )typeMap.get( typeString ) ).intValue( );
	}

	return 0;
    }
}
