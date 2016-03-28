// Class to setup a ComboBox for genre
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

public class GenreComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.GenreComboBox" );

    private Map genreMap = new HashMap( );

    public GenreComboBox( Connection conn,
			  int        selectedGenreId ) {

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	try {
	    // Fill the combo box and hash table
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT genre_id, genre " +
							  "FROM genre ORDER BY genre" );
	    while ( resultSet.next( ) ) {
		String genreString = resultSet.getString( 2 );

		// Store the genre_id in the map indexed by the genreString
		genreMap.put( genreString, resultSet.getObject( 1 ) );

		// Add the genreString to the combo box
		addItem( genreString );

		// Check if this is the selected genre
		if ( resultSet.getInt( 1 ) == selectedGenreId ) {
		    // Select this genre
		    setSelectedItem( genreString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 10 );
    }

    public int getSelectedGenreId( ) {
	return getGenreId( ( String )getSelectedItem( ) );
    }

    public int getGenreId( String genreString ) {
	if ( genreString == null ) return 0;

	// Check if empty string is selected
	if ( genreString.length( ) == 0 ) return 0;

	// Get the genre_id from the map
	if ( genreMap.containsKey( genreString ) ) {
	    return ( ( Integer )genreMap.get( genreString ) ).intValue( );
	}

	return 0;
    }
}
