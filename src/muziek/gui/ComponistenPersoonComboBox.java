//
// Project:	muziek
// Component:	gui
// File:	ComponistenPersoonComboBox.java
// Description:	ComboBox for selection of either a composer or a group of composers (componisten)
// Author:	Chris van Engelen
// History:	2008/12/27: Initial version
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

public class ComponistenPersoonComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.ComponistenPersoonComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    // Map of number of composers in componisten group, indexed by componisten_id
    private Map nComponistenMap = new HashMap( );

    // Map of persoon_id indexed by composer (a single person)
    private Map componistenPersoonMap = new HashMap( );
    private int selectedComponistenPersoonId = 0;

    // Map of componisten_id indexed by the label of the group of composers (componisten)
    private Map componistenMap = new HashMap( );
    private int selectedComponistenId = 0;

    private String componistenFilterString = null;
    private String newComponistenString = null;

    private boolean allowNewComponisten = true;


    public ComponistenPersoonComboBox( Connection conn,
				       Object     parentObject,
				       boolean    allowNewComponisten ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewComponisten = allowNewComponisten;

	// Setup the componisten combo box
	setupComponistenPersoonComboBox( );
    }


    public ComponistenPersoonComboBox( Connection conn,
				       Object     parentObject,
				       int        selectedComponistenPersoonId,
				       int        selectedComponistenId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedComponistenPersoonId = selectedComponistenPersoonId;
	this.selectedComponistenId = selectedComponistenId;

	// Setup the componisten combo box
	setupComponistenPersoonComboBox( );
    }


    public void setupComponistenPersoonComboBox( int selectedComponistenPersoonId,
						 int selectedComponistenId ) {
	this.selectedComponistenPersoonId = selectedComponistenPersoonId;
	this.selectedComponistenId = selectedComponistenId;

	// Setup the componisten combo box
	setupComponistenPersoonComboBox( );
    }


    public void setupComponistenPersoonComboBox( ) {
	// Remove all existing items in the componisten combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewComponisten ) {
	    // Add special item to insert new componisten
	    newComponistenString = "Nieuwe componisten ";
	    if ( ( componistenFilterString != null ) && ( componistenFilterString.length( ) > 0 ) ) {
		newComponistenString += componistenFilterString + " ";
	    }
	    newComponistenString += "toevoegen";
	    addItem( newComponistenString );
	}

	if ( !nComponistenMap.isEmpty( ) ) {
	    // Remove all items in the nComponisten hash table
	    nComponistenMap.clear( );
	}

	// Get the number of groups of composers a composer belongs to
	try {
	    String nComponistenQueryString = ( "SELECT componisten_persoon.persoon_id, COUNT(*) AS nComp " +
					       "FROM componisten_persoon " +
					       "GROUP BY componisten_persoon.persoon_id" );
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( nComponistenQueryString );

	    while ( resultSet.next( ) ) {
		// Store the number of groups of composers in , indexed by persoon_id
		nComponistenMap.put( resultSet.getObject( 1 ), resultSet.getObject( 2 ) );
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Check if componistenPersoonMap is empty
	if ( !componistenPersoonMap.isEmpty( ) ) {
	    // Remove all items in the componistenPersoon hash table
	    componistenPersoonMap.clear( );
	}

	// Check if componistenMap is empty
	if ( !componistenMap.isEmpty( ) ) {
	    // Remove all items in the componisten hash table
	    componistenMap.clear( );
	}

	// Fill the combo box and hash table
	try {
	    String componistenQueryString = ( "SELECT componisten.componisten_id, " +
					      "componisten.componisten, persoon.persoon_id, persoon.persoon " +
					      "FROM componisten " +
					      "LEFT JOIN componisten_persoon ON componisten.componisten_id = componisten_persoon.componisten_id " +
					      "LEFT JOIN persoon ON componisten_persoon.persoon_id = persoon.persoon_id " );

	    // Check for a componisten filter
	    if ( ( componistenFilterString != null ) && ( componistenFilterString.length( ) > 0 ) ) {
		// Add filter to query, both on componisten and persoon
		componistenQueryString += ( "WHERE ( componisten LIKE '%" + componistenFilterString +
					    "%' ) || ( persoon LIKE '%" + componistenFilterString + "%' ) " );
	    }

	    // Add order to query
	    componistenQueryString += "ORDER BY persoon, componisten";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( componistenQueryString );

	    while ( resultSet.next( ) ) {
		String componistString = resultSet.getString( 4 );
		if ( componistString == null ) {
		    componistString = "-";
		}

		// If the composer is a member of more than one group of composers,
		// then add a separate entry in the combobox for the composer as a person
		if ( nComponistenMap.containsKey( resultSet.getObject( 3 ) ) ) {
		    try {
			long nComponisten = ( ( Long )nComponistenMap.get( resultSet.getObject( 3 ) ) ).longValue( );
			if ( nComponisten > 1 ) {
			    String componistenPersoonString = componistString + " (*)";

			    // Check if the componistenPersoonMap does not already contains this entry
			    if ( !componistenPersoonMap.containsKey( componistenPersoonString ) ) {
				// Store the persoon_id in the componistenPersoonMap indexed by the componistString
				componistenPersoonMap.put( componistenPersoonString, resultSet.getObject( 3 ) );

				// Add the componistString to the combo box
				addItem( componistenPersoonString );

				// Check if this is the selected componisten
				if ( resultSet.getInt( 3 ) == selectedComponistenPersoonId ) {
				    // Select this composer
				    setSelectedItem( componistenPersoonString );
				}
			    }
			}
		    }
		    catch ( Exception exception ) {
			    logger.severe( "nComponistenMap exception for key " + resultSet.getString( 3 ) + 
					   ": " + exception.getMessage( ) );
		    }
		}
		else {
		    logger.severe( "nComponistenMap does not contain key " + resultSet.getString( 3 ) );
		}

		String componistenString = resultSet.getString( 2 );
		if ( !( componistenString.equals( componistString ) ) ) {
		    componistString += " (" + componistenString + ")";
		}

		if ( componistString.length( ) > 57 ) {
		    componistString = componistString.substring( 0, 57 ) + "...";
		}

		// Store the componisten_id in the map indexed by the componistenString
		componistenMap.put( componistString, resultSet.getObject( 1 ) );

		// Add the componistString to the combo box
		addItem( componistString );

		// Check if this is the selected componisten
		if ( resultSet.getInt( 1 ) == selectedComponistenId ) {
		    // Select this componisten
		    setSelectedItem( componistString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterComponistenPersoonComboBox( ) {
	String newComponistenFilterString = null;

	// Prompt for the componisten filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newComponistenFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Componisten filter:",
						       "Componisten filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       componistenFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newComponistenFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Componisten filter:",
						       "Componisten filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       componistenFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newComponistenFilterString != null ) {
	    // Store the new componisten filter
	    componistenFilterString = newComponistenFilterString;

	    // Setup the componisten combo box with the componisten filter
	    // Reset the selected componisten-persoon ID and componisten ID in order to avoid immediate selection
	    setupComponistenPersoonComboBox( 0, 0 );
	}

	// Return current componisten filter string, also when dialog has been canceled
	return componistenFilterString;
    }


    public int getSelectedComponistenPersoonId( ) {
	String componistenPersoonString = ( String )getSelectedItem( );

	if ( componistenPersoonString == null ) return 0;

	// Check if empty string is selected
	if ( componistenPersoonString.length( ) == 0 ) return 0;

	// Get the persoon_id from the map
	if ( componistenPersoonMap.containsKey( componistenPersoonString ) ) {
	    return ( ( Integer )componistenPersoonMap.get( componistenPersoonString ) ).intValue( );
	}

	return 0;
    }


    public int getSelectedComponistenId( ) {
	String componistenString = ( String )getSelectedItem( );

	if ( componistenString == null ) return 0;

	// Check if empty string is selected
	if ( componistenString.length( ) == 0 ) return 0;

	// Get the componisten_id from the map
	if ( componistenMap.containsKey( componistenString ) ) {
	    return ( ( Integer )componistenMap.get( componistenString ) ).intValue( );
	}

	return 0;
    }


    public boolean newComponistenSelected( ) {
	String componistenString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( componistenString == null ) return false;

	return componistenString.equals( newComponistenString );
    }
}
