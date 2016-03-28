// Class to setup a ComboBox for selection of an ensemble record

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

public class EnsembleComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.EnsembleComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map ensembleMap = new HashMap( );
    int selectedEnsembleId = 0;
    private String ensembleFilterString = null;
    private String newEnsembleString = null;

    private boolean allowNewEnsemble = true;


    public EnsembleComboBox( Connection conn,
			     Object     parentObject,
			     boolean    allowNewEnsemble ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewEnsemble = allowNewEnsemble;

	// Setup the ensemble combo box
	setupEnsembleComboBox( );
    }

    public EnsembleComboBox( Connection conn,
			     Object     parentObject,
			     int        selectedEnsembleId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedEnsembleId = selectedEnsembleId;

	// Setup the ensemble combo box
	setupEnsembleComboBox( );
    }


    public void setupEnsembleComboBox( int selectedEnsembleId ) {
	this.selectedEnsembleId = selectedEnsembleId;

	// Setup the ensemble combo box
	setupEnsembleComboBox( );
    }


    void setupEnsembleComboBox( ) {
	// Remove all existing items in the ensemble combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewEnsemble ) {
	    // Add special item to insert new ensemble
	    newEnsembleString = "Nieuwe ensemble ";
	    if ( ( ensembleFilterString != null ) && ( ensembleFilterString.length( ) > 0 ) ) {
		newEnsembleString += ensembleFilterString + " ";
	    }
	    newEnsembleString += "toevoegen";
	    addItem( newEnsembleString );
	}

	if ( !ensembleMap.isEmpty( ) ) {
	    // Remove all items in the ensemble hash table
	    ensembleMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String ensembleQueryString = "SELECT ensemble_id, ensemble FROM ensemble ";

	    // Check for a ensemble filter
	    if ( ( ensembleFilterString != null ) && ( ensembleFilterString.length( ) > 0 ) ) {
		// Add filter to query
		ensembleQueryString += "WHERE ensemble LIKE '%" + ensembleFilterString + "%' ";
	    }

	    // Add order to query
	    ensembleQueryString += "ORDER BY ensemble";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( ensembleQueryString );

	    while ( resultSet.next( ) ) {
		String ensembleString = resultSet.getString( 2 );

		// Store the ensemble_id in the map indexed by the ensembleString
		ensembleMap.put( ensembleString, resultSet.getObject( 1 ) );

		// Add the ensembleString to the combo box
		addItem( ensembleString );

		// Check if this is the selected ensemble
		if ( resultSet.getInt( 1 ) == selectedEnsembleId ) {
		    // Select this ensemble
		    setSelectedItem( ensembleString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterEnsembleComboBox( ) {
	String newEnsembleFilterString = null;

	// Prompt for the ensemble filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newEnsembleFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Ensemble filter:",
						       "Ensemble filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       ensembleFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newEnsembleFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Ensemble filter:",
						       "Ensemble filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       ensembleFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newEnsembleFilterString != null ) {
	    // Store the new ensemble filter
	    ensembleFilterString = newEnsembleFilterString;

	    // Setup the ensemble combo box with the ensemble filter
	    // Reset the selected ensemble ID in order to avoid immediate selection
	    setupEnsembleComboBox( 0 );
	}

	// Return current ensemble filter string, also when dialog has been canceled
	return ensembleFilterString;
    }


    String getSelectedEnsembleString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedEnsembleId( ) {
	String ensembleString = ( String )getSelectedItem( );

	if ( ensembleString == null ) return 0;

	// Check if empty string is selected
	if ( ensembleString.length( ) == 0 ) return 0;

	// Get the ensemble_id from the map
	if ( ensembleMap.containsKey( ensembleString ) ) {
	    return ( ( Integer )ensembleMap.get( ensembleString ) ).intValue( );
	}

	return 0;
    }


    public boolean newEnsembleSelected( ) {
	String ensembleString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( ensembleString == null ) return false;

	return ensembleString.equals( newEnsembleString );
    }
}
