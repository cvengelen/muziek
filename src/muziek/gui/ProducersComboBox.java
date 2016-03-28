// Class to setup a ComboBox for selection of producers

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


public class ProducersComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.ProducersComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map producersMap = new HashMap( );
    private int selectedProducersId = 0;
    private String producersFilterString = null;
    private String newProducersString = null;

    boolean allowNewProducers = true;


    public ProducersComboBox( Connection conn,
			      Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the producers combo box
	setupProducersComboBox( );
    }


    public ProducersComboBox( Connection conn,
			      Object     parentObject,
			      boolean    allowNewProducers ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewProducers = allowNewProducers;

	// Setup the producers combo box
	setupProducersComboBox( );
    }


    public ProducersComboBox( Connection conn,
			      Object     parentObject,
			      int        selectedProducersId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedProducersId = selectedProducersId;

	// Setup the producers combo box
	setupProducersComboBox( );
    }


    public void setupProducersComboBox( int selectedProducersId ) {
	this.selectedProducersId = selectedProducersId;

	// Setup the producers combo box
	setupProducersComboBox( );
    }


    public void setupProducersComboBox( ) {
	// Remove all existing items in the producers combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewProducers ) {
	    // Add special item to insert new producers
	    newProducersString = "Nieuwe producers ";
	    if ( ( producersFilterString != null ) && ( producersFilterString.length( ) > 0 ) ) {
		newProducersString += producersFilterString + " ";
	    }
	    newProducersString += "toevoegen";
	    addItem( newProducersString );
	}

	if ( !producersMap.isEmpty( ) ) {
	    // Remove all items in the producers hash table
	    producersMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String producersQueryString = ( "SELECT producers.producers_id, " +
					    "producers.producers, persoon.persoon " +
					    "FROM producers " +
					    "LEFT JOIN producers_persoon ON producers.producers_id = producers_persoon.producers_id " +
					    "LEFT JOIN persoon ON producers_persoon.persoon_id = persoon.persoon_id " );

	    // Check for a producers filter
	    if ( ( producersFilterString != null ) && ( producersFilterString.length( ) > 0 ) ) {
		// Add filter to query, both on producers and persoon
		producersQueryString += ( "WHERE ( producers LIKE '%" + producersFilterString +
					  "%' ) || ( persoon LIKE '%" + producersFilterString + "%' ) " );
	    }

	    // Add order to query
	    producersQueryString += "ORDER BY persoon, producers";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( producersQueryString );

	    while ( resultSet.next( ) ) {
		String producersString = null;

		String producerString = resultSet.getString( 3 );
		if ( producerString != null ) {
		    producersString = producerString + ": ";
		}

		if ( producersString == null ) {
		    producersString = resultSet.getString( 2 );
		} else {
		    producersString += resultSet.getString( 2 );
		}

		// Store the producers_id in the map indexed by the producersString
		producersMap.put( producersString, resultSet.getObject( 1 ) );

		// Add the producersString to the combo box
		addItem( producersString );

		// Check if this is the selected producers
		if ( resultSet.getInt( 1 ) == selectedProducersId ) {
		    // Select this producers
		    setSelectedItem( producersString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterProducersComboBox( ) {
	String newProducersFilterString = null;

	// Prompt for the producers filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newProducersFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Producers filter:",
						       "Producers filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       producersFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newProducersFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Producers filter:",
						       "Producers filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       producersFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newProducersFilterString != null ) {
	    // Store the new producers filter
	    producersFilterString = newProducersFilterString;

	    // Setup the producers combo box with the producers filter
	    // Reset the selected producers ID in order to avoid immediate selection
	    setupProducersComboBox( 0 );
	}

	// Return current producers filter string, also when dialog has been canceled
	return producersFilterString;
    }


    public int getSelectedProducersId( ) {
	String producersString = ( String )getSelectedItem( );

	if ( producersString == null ) return 0;

	// Check if empty string is selected
	if ( producersString.length( ) == 0 ) return 0;

	// Get the producers_id from the map
	if ( producersMap.containsKey( producersString ) ) {
	    return ( ( Integer )producersMap.get( producersString ) ).intValue( );
	}

	return 0;
    }


    public boolean newProducersSelected( ) {
	String producersString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( producersString == null ) return false;

	return producersString.equals( newProducersString );
    }
}
