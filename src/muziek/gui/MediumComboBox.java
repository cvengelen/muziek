// Class to setup a ComboBox for selection of medium record

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


public class MediumComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.MediumComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private int selectedGenreId = 0;
    private int selectedMediumTypeId = 0;

    private Map mediumMap = new HashMap( );
    private int selectedMediumId = 0;
    private String mediumFilterString = null;
    private String newMediumString = null;

    private boolean allowNewMedium = true;


    public MediumComboBox( Connection conn,
			   Object     parentObject,
			   boolean    allowNewMedium ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewMedium = allowNewMedium;

	// Setup the Medium combo box
	setupMediumComboBox( );
    }


    public MediumComboBox( Connection conn,
			   Object     parentObject,
			   String     mediumFilterString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.mediumFilterString = mediumFilterString;

	// Setup the Medium combo box
	setupMediumComboBox( );
    }


    public MediumComboBox( Connection conn,
			   Object     parentObject,
			   int        selectedMediumId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedMediumId = selectedMediumId;

	// Setup the Medium combo box
	setupMediumComboBox( );
    }


    public void setupMediumComboBox( int selectedMediumId ) {
	this.selectedMediumId = selectedMediumId;

	// Setup the medium combo box
	setupMediumComboBox( );
    }


    public void setupMediumComboBox( int selectedGenreId, int selectedMediumTypeId ) {
	this.selectedGenreId = selectedGenreId;
	this.selectedMediumTypeId = selectedMediumTypeId;

	// Setup the medium combo box
	setupMediumComboBox( );
    }


    void setupMediumComboBox( ) {
	// Remove all existing items in the medium combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewMedium ) {
	    // Add special item to insert new medium
	    newMediumString = "Nieuw medium ";
	    if ( ( mediumFilterString != null ) && ( mediumFilterString.length( ) > 0 ) ) {
		newMediumString += mediumFilterString + " ";
	    }
	    newMediumString += "toevoegen";
	    addItem( newMediumString );
	}

	if ( !mediumMap.isEmpty( ) ) {
	    // Remove all items in the medium hash table
	    mediumMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String mediumQueryString =
		"SELECT medium_id, genre, medium_type, medium_titel, uitvoerenden FROM medium " +
		"LEFT JOIN genre ON medium.genre_id = genre.genre_id " +
		"LEFT JOIN subgenre ON medium.subgenre_id = subgenre.subgenre_id " +
		"LEFT JOIN medium_type ON medium.medium_type_id = medium_type.medium_type_id " +
		"LEFT JOIN opslag ON medium.opslag_id = opslag.opslag_id ";

	    // Check if a medium filter is present, or genre is selected, or medium type is selected
	    if ( ( ( mediumFilterString != null ) && ( mediumFilterString.length( ) > 0 ) ) ||
		 ( selectedGenreId > 0 ) ||
		 ( selectedMediumTypeId > 0 ) ) {
		// Add WHERE clause
		mediumQueryString += "WHERE ";

		// Check if a medium filter is present
		if ( ( mediumFilterString != null ) && ( mediumFilterString.length( ) > 0 ) ) {
		    // Add selection on medium titel
		    mediumQueryString +=
			"( medium_titel LIKE '%" + mediumFilterString + "%' OR " +
			"uitvoerenden LIKE '%" + mediumFilterString + "%' ) ";

		    // Check if also genre or medium_type is selected
		    if ( ( selectedGenreId > 0 ) || ( selectedMediumTypeId > 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

		// Check if genre is selected
		if ( selectedGenreId > 0 ) {
		    // Add selection on genre
		    mediumQueryString += "medium.genre_id = " + selectedGenreId + " ";

		    // Check if also medium_type is selected
		    if ( selectedMediumTypeId > 0 ) {
			mediumQueryString += "AND ";
		    }
		}

		// Check if medium_type is selected
		if ( selectedMediumTypeId > 0 ) {
		    // Add selection on medium_type
		    mediumQueryString += "medium.medium_type_id = " + selectedMediumTypeId + " ";
		}
	    }

	    mediumQueryString += "ORDER BY genre, opslag, subgenre, medium_titel";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( mediumQueryString );

	    while ( resultSet.next( ) ) {
		String mediumString = ( resultSet.getString( 2 ) +
					" (" + resultSet.getString( 3 ) +
					"): " + resultSet.getString( 4 ) );

		String uitvoerendenString = resultSet.getString( 5 );
		if ( ( uitvoerendenString != null ) && ( uitvoerendenString.length( ) > 0 ) ) {
		    mediumString += "; " + uitvoerendenString;
		}

		// Limit the medium string length
		if ( mediumString.length( ) > 90 ) {
		    mediumString = mediumString.substring( 0, 90 );
		}

		// Store the medium_id in the map indexed by the mediumTitelString
		mediumMap.put( mediumString, resultSet.getObject( 1 ) );

		// Add the mediumTitelString to the combo box
		addItem( mediumString );

		// Check if this is the default medium
		if ( resultSet.getInt( 1 ) == selectedMediumId ) {
		    // Select this medium
		    setSelectedItem( mediumString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterMediumComboBox( ) {
	String newMediumFilterString = null;

	// Prompt for the medium filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newMediumFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Medium filter:",
						       "Medium filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       mediumFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newMediumFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Medium filter:",
						       "Medium filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       mediumFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newMediumFilterString != null ) {
	    // Store the new medium filter
	    mediumFilterString = newMediumFilterString;

	    // Setup the medium combo box with the medium filter
	    // Reset the selected medium ID in order to avoid immediate selection
	    setupMediumComboBox( 0 );
	}

	// Return current medium filter string, also when dialog has been canceled
	return mediumFilterString;
    }


    public String getSelectedMediumString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedMediumId( ) {
	String mediumString = ( String )getSelectedItem( );

	if ( mediumString == null ) return 0;

	// Check if empty string is selected
	if ( mediumString.length( ) == 0 ) return 0;

	// Get the medium_id from the map
	if ( mediumMap.containsKey( mediumString ) ) {
	    return ( ( Integer )mediumMap.get( mediumString ) ).intValue( );
	}

	return 0;
    }


    public boolean newMediumSelected( ) {
	String mediumString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( mediumString == null ) return false;

	return mediumString.equals( newMediumString );
    }
}
