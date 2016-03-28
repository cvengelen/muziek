// Class to setup a ComboBox for subtype

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

public class SubtypeComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.SubtypeComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map subtypeMap = new HashMap( );
    int selectedSubtypeId = 0;
    private String subtypeFilterString = null;
    private String newSubtypeString = null;

    private boolean allowNewSubtype = true;


    public SubtypeComboBox( Connection conn,
			    Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the subtype combo box
	setupSubtypeComboBox( );
    }


    public SubtypeComboBox( Connection conn,
			    Object     parentObject,
			    boolean    allowNewSubtype ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewSubtype = allowNewSubtype;

	// Setup the subtype combo box
	setupSubtypeComboBox( );
    }


    public SubtypeComboBox( Connection conn,
			    Object     parentObject,
			    int        selectedSubtypeId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedSubtypeId = selectedSubtypeId;

	// Setup the subtype combo box
	setupSubtypeComboBox( );
    }


    public void setupSubtypeComboBox( int selectedSubtypeId ) {
	this.selectedSubtypeId = selectedSubtypeId;

	// Setup the subtype combo box
	setupSubtypeComboBox( );
    }


    void setupSubtypeComboBox( ) {
	// Remove all existing items in the subtype combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewSubtype ) {
	    // Add special item to insert new subtype
	    newSubtypeString = "Nieuwe subtype ";
	    if ( ( subtypeFilterString != null ) && ( subtypeFilterString.length( ) > 0 ) ) {
		newSubtypeString += subtypeFilterString + " ";
	    }
	    newSubtypeString += "toevoegen";
	    addItem( newSubtypeString );
	}

	if ( !subtypeMap.isEmpty( ) ) {
	    // Remove all items in the subtype hash table
	    subtypeMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String subtypeQueryString = "SELECT subtype_id, subtype FROM subtype ";

	    // Check for a subtype filter
	    if ( ( subtypeFilterString != null ) && ( subtypeFilterString.length( ) > 0 ) ) {
		// Add filter to query
		subtypeQueryString += "WHERE subtype LIKE '%" + subtypeFilterString + "%' ";
	    }

	    // Add order to query
	    subtypeQueryString += "ORDER BY subtype";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( subtypeQueryString );

	    while ( resultSet.next( ) ) {
		String subtypeString = resultSet.getString( 2 );

		// Store the subtype_id in the map indexed by the subtypeString
		subtypeMap.put( subtypeString, resultSet.getObject( 1 ) );

		// Add the subtypeString to the combo box
		addItem( subtypeString );

		// Check if this is the selected subtype
		if ( resultSet.getInt( 1 ) == selectedSubtypeId ) {
		    // Select this subtype
		    setSelectedItem( subtypeString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterSubtypeComboBox( ) {
	String newSubtypeFilterString = null;

	// Prompt for the subtype filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newSubtypeFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Subtype filter:",
						       "Subtype filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       subtypeFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newSubtypeFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Subtype filter:",
						       "Subtype filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       subtypeFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newSubtypeFilterString != null ) {
	    // Store the new subtype filter
	    subtypeFilterString = newSubtypeFilterString;

	    // Setup the subtype combo box with the subtype filter
	    // Reset the selected subtype ID in order to avoid immediate selection
	    setupSubtypeComboBox( 0 );
	}

	// Return current subtype filter string, also when dialog has been canceled
	return subtypeFilterString;
    }


    public String getSelectedSubtypeString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedSubtypeId( ) {
	return getSubtypeId( ( String )getSelectedItem( ) );
    }


    public int getSubtypeId( String subtypeString ) {
	if ( subtypeString == null ) return 0;

	// Check if empty string is selected
	if ( subtypeString.length( ) == 0 ) return 0;

	// Get the subtype_id from the map
	if ( subtypeMap.containsKey( subtypeString ) ) {
	    return ( ( Integer )subtypeMap.get( subtypeString ) ).intValue( );
	}

	return 0;
    }


    public boolean newSubtypeSelected( ) {
	String subtypeString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( subtypeString == null ) return false;

	return subtypeString.equals( newSubtypeString );
    }
}
