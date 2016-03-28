// Class to setup a ComboBox for rol

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

public class RolComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.RolComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map rolMap = new HashMap( );
    int selectedRolId = 0;
    private String rolFilterString = null;
    private String newRolString = null;

    private boolean allowNewRol = true;


    public RolComboBox( Connection conn,
			Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the rol combo box
	setupRolComboBox( );
    }


    public RolComboBox( Connection conn,
			Object     parentObject,
			boolean    allowNewRol ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewRol = allowNewRol;

	// Setup the rol combo box
	setupRolComboBox( );
    }


    public RolComboBox( Connection conn,
			Object     parentObject,
			int        selectedRolId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedRolId = selectedRolId;

	// Setup the rol combo box
	setupRolComboBox( );
    }


    public void setupRolComboBox( int selectedRolId ) {
	this.selectedRolId = selectedRolId;

	// Setup the rol combo box
	setupRolComboBox( );
    }


    void setupRolComboBox( ) {
	// Remove all existing items in the rol combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewRol ) {
	    // Add special item to insert new rol
	    newRolString = "Nieuwe rol ";
	    if ( ( rolFilterString != null ) && ( rolFilterString.length( ) > 0 ) ) {
		newRolString += rolFilterString + " ";
	    }
	    newRolString += "toevoegen";
	    addItem( newRolString );
	}

	if ( !rolMap.isEmpty( ) ) {
	    // Remove all items in the rol hash table
	    rolMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String rolQueryString = "SELECT rol_id, rol FROM rol ";

	    // Check for a rol filter
	    if ( ( rolFilterString != null ) && ( rolFilterString.length( ) > 0 ) ) {
		// Add filter to query
		rolQueryString += "WHERE rol LIKE '%" + rolFilterString + "%' ";
	    }

	    // Add order to query
	    rolQueryString += "ORDER BY rol";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( rolQueryString );

	    while ( resultSet.next( ) ) {
		String rolString = resultSet.getString( 2 );

		// Store the rol_id in the map indexed by the rolString
		rolMap.put( rolString, resultSet.getObject( 1 ) );

		// Add the rolString to the combo box
		addItem( rolString );

		// Check if this is the selected rol
		if ( resultSet.getInt( 1 ) == selectedRolId ) {
		    // Select this rol
		    setSelectedItem( rolString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterRolComboBox( ) {
	String newRolFilterString = null;

	// Prompt for the rol filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newRolFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Rol filter:",
						       "Rol filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       rolFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newRolFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Rol filter:",
						       "Rol filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       rolFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newRolFilterString != null ) {
	    // Store the new rol filter
	    rolFilterString = newRolFilterString;

	    // Setup the rol combo box with the rol filter
	    // Reset the selected rol ID in order to avoid immediate selection
	    setupRolComboBox( 0 );
	}

	// Return current rol filter string, also when dialog has been canceled
	return rolFilterString;
    }


    public String getSelectedRolString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedRolId( ) {
	return getRolId( ( String )getSelectedItem( ) );
    }


    public int getRolId( String rolString ) {
	if ( rolString == null ) return 0;

	// Check if empty string is selected
	if ( rolString.length( ) == 0 ) return 0;

	// Get the rol_id from the map
	if ( rolMap.containsKey( rolString ) ) {
	    return ( ( Integer )rolMap.get( rolString ) ).intValue( );
	}

	return 0;
    }


    public boolean newRolSelected( ) {
	String rolString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( rolString == null ) return false;

	return rolString.equals( newRolString );
    }
}
