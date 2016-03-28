//
// Project:	muziek
// File:	MusiciPersoonComboBox.java
// Description:	Class to setup a ComboBox for either a musician, or a group of musicians (musici)
// Author:	Chris van Engelen
// History:	2006/12/16: Initial version
//		2008/12/25: Rework for selection of musician (persoon) or group of musicians (musici)
//

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


public class MusiciPersoonComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.MusiciPersoonComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    // Map of persoon.persoon_id index indexed by string "persoon.persoon: *"
    private Map persoonAllMusiciMap = new HashMap( );
    // Currently selected musician in the combo box (0 if a musici group is selected)
    private int selectedPersoonId = 0;

    // Map of musici.persoon_id index indexed by string "persoon.persoon: musici.musici"
    private Map persoonMusiciMap = new HashMap( );
    // Currently selected musici group in the combo box (0 if a musician is selected)
    private int selectedMusiciId = 0;

    private String musiciPersoonFilterString = null;
    private String newMusiciPersoonString = null;


    public MusiciPersoonComboBox( Connection conn,
				  Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the musici-persoon combo box
	setupMusiciPersoonComboBox( );
    }


    public MusiciPersoonComboBox( Connection conn,
				  Object     parentObject,
				  int        selectedPersoonId,
				  int        selectedMusiciId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedPersoonId = selectedPersoonId;
	this.selectedMusiciId  = selectedMusiciId;

	// Setup the musici-persoon combo box
	setupMusiciPersoonComboBox( );
    }


    public void setupMusiciPersoonComboBox( int selectedPersoonId,
					    int selectedMusiciId ) {
	this.selectedPersoonId = selectedPersoonId;
	this.selectedMusiciId  = selectedMusiciId;

	// Setup the musici-persoon combo box
	setupMusiciPersoonComboBox( );
    }


    public void setupMusiciPersoonComboBox( ) {
	// Remove all existing items in the musici-persoon combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( !persoonAllMusiciMap.isEmpty( ) ) {
	    // Remove all items in the musici hash table
	    persoonAllMusiciMap.clear( );
	}

	if ( !persoonMusiciMap.isEmpty( ) ) {
	    // Remove all items in the musici hash table
	    persoonAllMusiciMap.clear( );
	}

	try {
	    // Fill the combo box and hash tables

	    String musiciPersoonQueryString =
		"SELECT DISTINCT persoon.persoon_id, persoon.persoon, musici.musici_id, musici.musici " +
                "FROM musici " + 
		"INNER JOIN musici_persoon ON musici_persoon.musici_id = musici.musici_id " +
		"INNER JOIN persoon ON musici_persoon.persoon_id = persoon.persoon_id ";

	    // Check if an musici-persoon filter is present
	    if  ( ( musiciPersoonFilterString != null ) && ( musiciPersoonFilterString.length( ) > 0 ) ) {
		// Add selection on musici-persoon
		musiciPersoonQueryString += "WHERE persoon.persoon LIKE '%" + musiciPersoonFilterString + "%' ";
	    }

	    // Order on persoon
	    musiciPersoonQueryString += "ORDER BY persoon";

	    logger.fine( "musiciPersoonQueryString:\n" + musiciPersoonQueryString );

	    // Execute the query
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( musiciPersoonQueryString );

	    while ( resultSet.next( ) ) {
		// Get the last name of the musician
		String persoonString = resultSet.getString( 2 );

		// Make string to use as index in persoonAllMusiciMap
		String persoonAllMusiciString = persoonString + ": *";

		// Check if the persoonAllMusiciMap does not already contains this musician entry
		if ( !persoonAllMusiciMap.containsKey( persoonAllMusiciString ) ) {
		    // Store the persoon_id in persoonAllMusiciMap indexed by the persoonAllMusiciString
		    persoonAllMusiciMap.put( persoonAllMusiciString, resultSet.getObject( 1 ) );

		    // Add the entry for this musician to the combo box
		    addItem( persoonAllMusiciString );

		    // Check if this entry is the selected musician
		    if ( resultSet.getInt( 1 ) == selectedPersoonId ) {
			// Select this musician
			setSelectedItem( persoonAllMusiciString );
		    }
		}

		// Get the name of the group of musicians
		String musiciString = resultSet.getString( 4 );
		if ( musiciString.length( ) > 50 ) {
		    musiciString = musiciString.substring( 0, 50 ) + "...";
		}

		// Add the group of musicians to the musician name
		String persoonMusiciString = persoonString + ": " + musiciString;

		// Store the musici_id in the persoonMusiciMap indexed by the persoonMusiciString
		persoonMusiciMap.put( persoonMusiciString, resultSet.getObject( 3 ) );

		// Add the entry for this group of musicians to the combo box
		addItem( persoonMusiciString );

		// Check if this entry is the currenty selected group of musicians
		if ( resultSet.getInt( 3 ) == selectedMusiciId ) {
		    // Select this group of musicians
		    setSelectedItem( persoonMusiciString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterMusiciPersoonComboBox( ) {
	String newMusiciPersoonFilterString = null;

	// Prompt for the musici-persoon filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newMusiciPersoonFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Musici-persoon filter:",
						       "Musici-persoon filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       musiciPersoonFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newMusiciPersoonFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Musici-persoon filter:",
						       "Musici-persoon filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       musiciPersoonFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newMusiciPersoonFilterString != null ) {
	    // Store the new musici filter
	    musiciPersoonFilterString = newMusiciPersoonFilterString;

	    // Setup the musici combo box with the musici filter
	    // Reset the selected musici ID in order to avoid immediate selection
	    setupMusiciPersoonComboBox( 0, 0 );
	}

	// Return current musici-persoon filter string, also when dialog has been canceled
	return musiciPersoonFilterString;
    }


    public String getSelectedMusiciPersoonString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedPersoonAllMusiciId( ) {
	String musiciPersoonString = ( String )getSelectedItem( );

	if ( musiciPersoonString == null ) return 0;

	// Check if empty string is selected
	if ( musiciPersoonString.length( ) == 0 ) return 0;

	// Get the persoon_id from the persoonAllMusiciMap
	if ( persoonAllMusiciMap.containsKey( musiciPersoonString ) ) {
	    return ( ( Integer )persoonAllMusiciMap.get( musiciPersoonString ) ).intValue( );
	}

	return 0;
    }


    public int getSelectedMusiciId( ) {
	String musiciPersoonString = ( String )getSelectedItem( );

	if ( musiciPersoonString == null ) return 0;

	// Check if empty string is selected
	if ( musiciPersoonString.length( ) == 0 ) return 0;

	// Get the musici_id from the persoonMusiciMap
	if ( persoonMusiciMap.containsKey( musiciPersoonString ) ) {
	    return ( ( Integer )persoonMusiciMap.get( musiciPersoonString ) ).intValue( );
	}

	return 0;
    }


    public boolean newMusiciPersoonSelected( ) {
	String musiciPersoonString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( musiciPersoonString == null ) return false;

	return musiciPersoonString.equals( newMusiciPersoonString );
    }
}
