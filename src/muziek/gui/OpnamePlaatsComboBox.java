// Class to setup a ComboBox for opname_plaats

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


public class OpnamePlaatsComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.OpnamePlaatsComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map opnamePlaatsMap = new HashMap( );
    int selectedOpnamePlaatsId = 0;
    private String opnamePlaatsFilterString = null;
    private String newOpnamePlaatsString = null;

    boolean allowNewOpnamePlaats = true;


    public OpnamePlaatsComboBox( Connection conn,
				 Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the opname plaats combo box
	setupOpnamePlaatsComboBox( );
    }


    public OpnamePlaatsComboBox( Connection conn,
				 Object     parentObject,
				 boolean    allowNewOpnamePlaats ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewOpnamePlaats = allowNewOpnamePlaats;

	// Setup the opname plaats combo box
	setupOpnamePlaatsComboBox( );
    }


    public OpnamePlaatsComboBox( Connection conn,
				 Object     parentObject,
				 int        selectedOpnamePlaatsId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedOpnamePlaatsId = selectedOpnamePlaatsId;

	// Setup the opname plaats combo box
	setupOpnamePlaatsComboBox( );
    }


    public void setupOpnamePlaatsComboBox( int selectedOpnamePlaatsId ) {
	this.selectedOpnamePlaatsId = selectedOpnamePlaatsId;

	// Setup the opname plaats combo box
	setupOpnamePlaatsComboBox( );
    }


    void setupOpnamePlaatsComboBox( ) {
	// Remove all existing items in the opname plaats combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewOpnamePlaats ) {
	    // Add special item to insert new opname plaats
	    newOpnamePlaatsString = "Nieuwe opname plaats ";
	    if ( ( opnamePlaatsFilterString != null ) && ( opnamePlaatsFilterString.length( ) > 0 ) ) {
		newOpnamePlaatsString += opnamePlaatsFilterString + " ";
	    }
	    newOpnamePlaatsString += "toevoegen";
	    addItem( newOpnamePlaatsString );
	}

	if ( !opnamePlaatsMap.isEmpty( ) ) {
	    // Remove all items in the opname plaats hash table
	    opnamePlaatsMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String opnamePlaatsQueryString = "SELECT opname_plaats_id, opname_plaats FROM opname_plaats ";

	    // Check for a opname plaats filter
	    if ( ( opnamePlaatsFilterString != null ) && ( opnamePlaatsFilterString.length( ) > 0 ) ) {
		// Add filter to query on opname plaats
		opnamePlaatsQueryString += "WHERE opname_plaats LIKE '%" + opnamePlaatsFilterString + "%' ";
	    }

	    // Add order to query
	    opnamePlaatsQueryString += "ORDER BY opname_plaats";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opnamePlaatsQueryString );

	    while ( resultSet.next( ) ) {
		String opnamePlaatsString = resultSet.getString( 2 );

		// Store the opname plaats ID in the map indexed by the opnamePlaatsString
		opnamePlaatsMap.put( opnamePlaatsString, resultSet.getObject( 1 ) );

		// Add the opnamePlaatsString to the combo box
		addItem( opnamePlaatsString );

		// Check if this is the selected opname plaats
		if ( resultSet.getInt( 1 ) == selectedOpnamePlaatsId ) {
		    // Select this opname plaats
		    setSelectedItem( opnamePlaatsString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterOpnamePlaatsComboBox( ) {
	String newOpnamePlaatsFilterString = null;

	// Prompt for the opname plaats filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newOpnamePlaatsFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Opname plaats filter:",
						       "Opname plaats filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       opnamePlaatsFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newOpnamePlaatsFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Opname plaats filter:",
						       "Opname plaats filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       opnamePlaatsFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newOpnamePlaatsFilterString != null ) {
	    // Store the new opname plaats filter
	    opnamePlaatsFilterString = newOpnamePlaatsFilterString;

	    // Setup the opnamePlaats combo box with the opname plaats filter
	    // Reset the selected opnamePlaats ID in order to avoid immediate selection
	    setupOpnamePlaatsComboBox( 0 );
	}

	// Return current opname plaats filter string, also when dialog has been canceled
	return opnamePlaatsFilterString;
    }


    public int getSelectedOpnamePlaatsId( ) {
	String opnamePlaatsString = ( String )getSelectedItem( );

	if ( opnamePlaatsString == null ) return 0;

	// Check if empty string is selected
	if ( opnamePlaatsString.length( ) == 0 ) return 0;

	// Get the opname plaats id from the map
	if ( opnamePlaatsMap.containsKey( opnamePlaatsString ) ) {
	    return ( ( Integer )opnamePlaatsMap.get( opnamePlaatsString ) ).intValue( );
	}

	return 0;
    }


    public boolean newOpnamePlaatsSelected( ) {
	String opnamePlaatsString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( opnamePlaatsString == null ) return false;

	return opnamePlaatsString.equals( newOpnamePlaatsString );
    }
}
