// Class to setup a ComboBox for opname_datum

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


public class OpnameDatumComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.OpnameDatumComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map opnameDatumMap = new HashMap( );
    int selectedOpnameDatumId = 0;
    private String opnameDatumFilterString = null;
    private String newOpnameDatumString = null;

    boolean allowNewOpnameDatum = true;


    public OpnameDatumComboBox( Connection conn,
				Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the opname datum combo box
	setupOpnameDatumComboBox( );
    }


    public OpnameDatumComboBox( Connection conn,
				Object     parentObject,
				boolean    allowNewOpnameDatum ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewOpnameDatum = allowNewOpnameDatum;

	// Setup the opname datum combo box
	setupOpnameDatumComboBox( );
    }


    public OpnameDatumComboBox( Connection conn,
				Object     parentObject,
				int        selectedOpnameDatumId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedOpnameDatumId = selectedOpnameDatumId;

	// Setup the opname datum combo box
	setupOpnameDatumComboBox( );
    }


    public void setupOpnameDatumComboBox( int selectedOpnameDatumId ) {
	this.selectedOpnameDatumId = selectedOpnameDatumId;

	// Setup the opname datum combo box
	setupOpnameDatumComboBox( );
    }


    void setupOpnameDatumComboBox( ) {
	// Remove all existing items in the opname datum combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewOpnameDatum ) {
	    // Add special item to insert new opname datum
	    newOpnameDatumString = "Nieuwe opname datum ";
	    if ( ( opnameDatumFilterString != null ) && ( opnameDatumFilterString.length( ) > 0 ) ) {
		newOpnameDatumString += opnameDatumFilterString + " ";
	    }
	    newOpnameDatumString += "toevoegen";
	    addItem( newOpnameDatumString );
	}

	if ( !opnameDatumMap.isEmpty( ) ) {
	    // Remove all items in the opname datum hash table
	    opnameDatumMap.clear( );
	}

	if ( !opnameDatumMap.isEmpty( ) ) {
	    // Remove all items in the opname datum hash table
	    opnameDatumMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String opnameDatumQueryString = ( "SELECT opname_datum_id, opname_datum, " +
					       "opname_jaar_1, opname_maand_1, " +
					       "opname_jaar_2, opname_maand_2 " +
					       "FROM opname_datum " );

	    // Check for a opname datum filter
	    if ( ( opnameDatumFilterString != null ) && ( opnameDatumFilterString.length( ) > 0 ) ) {
		// Add filter to query on opname datum
		opnameDatumQueryString += "WHERE opname_datum LIKE '%" + opnameDatumFilterString + "%' ";
	    }

	    // Add order to query
	    opnameDatumQueryString += "ORDER BY opname_jaar_1, opname_maand_1, opname_jaar_2, opname_maand_2";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opnameDatumQueryString );

	    while ( resultSet.next( ) ) {
		String opnameDatumString = resultSet.getString( 2 );

		// Store the opname datum ID in the map indexed by the opnameDatumString
		opnameDatumMap.put( opnameDatumString, resultSet.getObject( 1 ) );

		// Add the opnameDatumString to the combo box
		addItem( opnameDatumString );

		// Check if this is the selected opname datum
		if ( resultSet.getInt( 1 ) == selectedOpnameDatumId ) {
		    // Select this opname datum
		    setSelectedItem( opnameDatumString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterOpnameDatumComboBox( ) {
	String newOpnameDatumFilterString = null;

	// Prompt for the opname datum filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newOpnameDatumFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Opname datum filter:",
						       "Opname datum filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       opnameDatumFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newOpnameDatumFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Opname datum filter:",
						       "Opname datum filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       opnameDatumFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newOpnameDatumFilterString != null ) {
	    // Store the new opname datum filter
	    opnameDatumFilterString = newOpnameDatumFilterString;

	    // Setup the opnameDatum combo box with the opname datum filter
	    // Reset the selected opnameDatum ID in order to avoid immediate selection
	    setupOpnameDatumComboBox( 0 );
	}

	// Return current opname datum filter string, also when dialog has been canceled
	return opnameDatumFilterString;
    }


    public int getSelectedOpnameDatumId( ) {
	String opnameDatumString = ( String )getSelectedItem( );

	if ( opnameDatumString == null ) return 0;

	// Check if empty string is selected
	if ( opnameDatumString.length( ) == 0 ) return 0;

	// Get the opname datum id from the map
	if ( opnameDatumMap.containsKey( opnameDatumString ) ) {
	    return ( ( Integer )opnameDatumMap.get( opnameDatumString ) ).intValue( );
	}

	return 0;
    }


    public boolean newOpnameDatumSelected( ) {
	String opnameDatumString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( opnameDatumString == null ) return false;

	return opnameDatumString.equals( newOpnameDatumString );
    }
}
