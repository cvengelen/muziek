 // Class to setup a ComboBox for opslag

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

public class OpslagComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.OpslagComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map opslagMap = new HashMap( );
    private int selectedOpslagId = 0;
    private String opslagFilterString = null;
    private String newOpslagString = null;

    private boolean allowNewOpslag = true;


    public OpslagComboBox( Connection conn,
			   Object     parentObject,
			   boolean    allowNewOpslag ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewOpslag = allowNewOpslag;

	// Setup the opslag combo box
	setupOpslagComboBox( );
    }

    public OpslagComboBox( Connection conn,
			   Object     parentObject,
			   int        selectedOpslagId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedOpslagId = selectedOpslagId;

	// Setup the opslag combo box
	setupOpslagComboBox( );
    }


    public void setupOpslagComboBox( int selectedOpslagId ) {
	this.selectedOpslagId = selectedOpslagId;

	// Setup the opslag combo box
	setupOpslagComboBox( );
    }


    void setupOpslagComboBox( ) {
	// Remove all existing items in the opslag combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewOpslag ) {
	    // Add special item to insert new opslag
	    newOpslagString = "Nieuwe opslag ";
	    if ( ( opslagFilterString != null ) && ( opslagFilterString.length( ) > 0 ) ) {
		newOpslagString += opslagFilterString + " ";
	    }
	    newOpslagString += "toevoegen";
	    addItem( newOpslagString );
	}

	if ( !opslagMap.isEmpty( ) ) {
	    // Remove all items in the opslag hash table
	    opslagMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String opslagQueryString = "SELECT opslag_id, opslag FROM opslag ";

	    // Check for a opslag filter
	    if ( ( opslagFilterString != null ) && ( opslagFilterString.length( ) > 0 ) ) {
		// Add filter to query
		opslagQueryString += "WHERE opslag LIKE '%" + opslagFilterString + "%' ";
	    }

	    // Add order to query
	    opslagQueryString += "ORDER BY opslag";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opslagQueryString );

	    while ( resultSet.next( ) ) {
		String opslagString = resultSet.getString( 2 );

		// Store the opslag_id in the map indexed by the opslagString
		opslagMap.put( opslagString, resultSet.getObject( 1 ) );

		// Add the opslagString to the combo box
		addItem( opslagString );

		// Check if this is the selected opslag
		if ( resultSet.getInt( 1 ) == selectedOpslagId ) {
		    // Select this opslag
		    setSelectedItem( opslagString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterOpslagComboBox( ) {
	String newOpslagFilterString = null;

	// Prompt for the opslag filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newOpslagFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Opslag filter:",
						       "Opslag filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       opslagFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newOpslagFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Opslag filter:",
						       "Opslag filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       opslagFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newOpslagFilterString != null ) {
	    // Store the new opslag filter
	    opslagFilterString = newOpslagFilterString;

	    // Setup the opslag combo box with the opslag filter
	    // Reset the selected opslag ID in order to avoid immediate selection
	    setupOpslagComboBox( 0 );
	}

	// Return current opslag filter string, also when dialog has been canceled
	return opslagFilterString;
    }


    public String getSelectedOpslagString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedOpslagId( ) {
	return getOpslagId( ( String )getSelectedItem( ) );
    }


    public int getOpslagId( String opslagString ) {
	if ( opslagString == null ) return 0;

	// Check if empty string is selected
	if ( opslagString.length( ) == 0 ) return 0;

	// Get the opslag_id from the map
	if ( opslagMap.containsKey( opslagString ) ) {
	    return ( ( Integer )opslagMap.get( opslagString ) ).intValue( );
	}

	return 0;
    }


    public boolean newOpslagSelected( ) {
	String opslagString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( opslagString == null ) return false;

	return opslagString.equals( newOpslagString );
    }
}
