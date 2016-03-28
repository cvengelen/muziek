// Class to setup a ComboBox for selection of musici-ensemble

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


public class MusiciEnsembleComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.MusiciEnsembleComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map musiciEnsembleMap = new HashMap( );
    private int selectedMusiciEnsembleId = 0;
    private String musiciEnsembleFilterString = null;
    private String newMusiciEnsembleString = null;


    public MusiciEnsembleComboBox( Connection conn,
				   Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the musici-ensemble combo box
	setupMusiciEnsembleComboBox( );
    }


    public MusiciEnsembleComboBox( Connection conn,
				   Object     parentObject,
				   int        selectedMusiciEnsembleId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedMusiciEnsembleId = selectedMusiciEnsembleId;

	// Setup the musici-ensemble combo box
	setupMusiciEnsembleComboBox( );
    }


    public void setupMusiciEnsembleComboBox( int selectedMusiciEnsembleId ) {
	this.selectedMusiciEnsembleId = selectedMusiciEnsembleId;

	// Setup the musici-ensemble combo box
	setupMusiciEnsembleComboBox( );
    }


    public void setupMusiciEnsembleComboBox( ) {
	// Remove all existing items in the musici-ensemble combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( !musiciEnsembleMap.isEmpty( ) ) {
	    // Remove all items in the musici hash table
	    musiciEnsembleMap.clear( );
	}

	try {
	    // Fill the combo box and hash table

	    String musiciEnsembleQueryString = "SELECT DISTINCT ensemble.ensemble_id, ensemble.ensemble FROM musici ";
	    musiciEnsembleQueryString += "INNER JOIN musici_ensemble ON musici_ensemble.musici_id = musici.musici_id ";
	    musiciEnsembleQueryString += "INNER JOIN ensemble ON musici_ensemble.ensemble_id = ensemble.ensemble_id ";

	    // Check if an musici-ensemble filter is present
	    if  ( ( musiciEnsembleFilterString != null ) && ( musiciEnsembleFilterString.length( ) > 0 ) ) {
		// Add selection on musici-ensemble
		musiciEnsembleQueryString += "WHERE ensemble.ensemble LIKE '%" + musiciEnsembleFilterString + "%' ";
	    }

	    // Order on ensemble
	    musiciEnsembleQueryString += "ORDER BY ensemble";

	    logger.fine( "musiciEnsembleQueryString:\n" + musiciEnsembleQueryString );

	    // Execute the query
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( musiciEnsembleQueryString );

	    while ( resultSet.next( ) ) {
		String musiciEnsembleString = resultSet.getString( 2 );

		// Store the ensemble_id in the map indexed by the musiciEnsembleString
		musiciEnsembleMap.put( musiciEnsembleString, resultSet.getObject( 1 ) );

		// Add the musiciEnsembleString to the combo box
		addItem( musiciEnsembleString );

		// Check if this is the selected musici-ensemble
		if ( resultSet.getInt( 1 ) == selectedMusiciEnsembleId ) {
		    // Select this musici-ensemble
		    setSelectedItem( musiciEnsembleString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterMusiciEnsembleComboBox( ) {
	String newMusiciEnsembleFilterString = null;

	// Prompt for the musici-ensemble filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newMusiciEnsembleFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "musici-ensemble filter:",
						       "musici-ensemble filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       musiciEnsembleFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newMusiciEnsembleFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "musici-ensemble filter:",
						       "musici-ensemble filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       musiciEnsembleFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newMusiciEnsembleFilterString != null ) {
	    // Store the new musici filter
	    musiciEnsembleFilterString = newMusiciEnsembleFilterString;

	    // Setup the musici combo box with the musici filter
	    // Reset the selected musici ID in order to avoid immediate selection
	    setupMusiciEnsembleComboBox( 0 );
	}

	// Return current musici-ensemble filter string, also when dialog has been canceled
	return musiciEnsembleFilterString;
    }


    public String getSelectedMusiciEnsembleString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedMusiciEnsembleId( ) {
	String musiciEnsembleString = ( String )getSelectedItem( );

	if ( musiciEnsembleString == null ) return 0;

	// Check if empty string is selected
	if ( musiciEnsembleString.length( ) == 0 ) return 0;

	// Get the ensemble_id from the map
	if ( musiciEnsembleMap.containsKey( musiciEnsembleString ) ) {
	    return ( ( Integer )musiciEnsembleMap.get( musiciEnsembleString ) ).intValue( );
	}

	return 0;
    }


    public boolean newMusiciEnsembleSelected( ) {
	String musiciEnsembleString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( musiciEnsembleString == null ) return false;

	return musiciEnsembleString.equals( newMusiciEnsembleString );
    }
}
