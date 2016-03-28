// Class to setup a ComboBox for selection of musici

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


public class MusiciComboBox extends JComboBox {
    final private Logger logger = Logger.getLogger( "muziek.gui.MusiciComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map musiciMap = new HashMap( );
    private int selectedMusiciId = 0;
    private int selectedMusiciPersoonId = 0;
    private int selectedMusiciEnsembleId = 0;
    private String musiciFilterString = null;
    private String newMusiciString = null;

    private boolean allowNewMusici = true;


    public MusiciComboBox( Connection conn,
			   Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the musici combo box
	setupMusiciComboBox( );
    }


    public MusiciComboBox( Connection conn,
			   Object     parentObject,
			   boolean    allowNewMusici ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.allowNewMusici = allowNewMusici;

	// Setup the musici combo box
	setupMusiciComboBox( );
    }


    public MusiciComboBox( Connection conn,
			   Object     parentObject,
			   int        selectedMusiciId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedMusiciId = selectedMusiciId;

	// Setup the musici combo box
	setupMusiciComboBox( );
    }


    public MusiciComboBox( Connection conn,
			   Object     parentObject,
			   int        selectedMusiciPersoonId,
			   int        selectedMusiciEnsembleId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedMusiciPersoonId  = selectedMusiciPersoonId;
	this.selectedMusiciEnsembleId = selectedMusiciEnsembleId;

	// Setup the musici combo box
	setupMusiciComboBox( );
    }


    public void setupMusiciComboBox( int selectedMusiciId ) {
	this.selectedMusiciId = selectedMusiciId;

	// Setup the musici combo box
	setupMusiciComboBox( );
    }


    public void setupMusiciComboBox( ) {
	// Remove all existing items in the musici combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	if ( allowNewMusici ) {
	    // Add special item to insert new musici
	    newMusiciString = "Nieuwe musici ";
	    if ( ( musiciFilterString != null ) && ( musiciFilterString.length( ) > 0 ) ) {
		newMusiciString += musiciFilterString + " ";
	    }
	    newMusiciString += "toevoegen";
	    addItem( newMusiciString );
	}

	if ( !musiciMap.isEmpty( ) ) {
	    // Remove all items in the musici hash table
	    musiciMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String musiciQueryString;

	    // Check if a musici-persoon is selected
	    if ( selectedMusiciPersoonId != 0 ) {
		// Selection on musici-persoon, and possibly also musici-ensemble
		musiciQueryString = "SELECT musici.musici_id, musici.musici, persoon.persoon ";
		musiciQueryString += "FROM musici ";
		musiciQueryString += "LEFT JOIN musici_persoon ON musici_persoon.musici_id = musici.musici_id ";
		musiciQueryString += "LEFT JOIN musici_ensemble ON musici_ensemble.musici_id = musici.musici_id ";
		musiciQueryString += "LEFT JOIN persoon ON musici_persoon.persoon_id = persoon.persoon_id ";
		musiciQueryString += "WHERE musici_persoon.persoon_id =" + selectedMusiciPersoonId + " ";

		// Check if a musici-ensemble is selected
		if  ( selectedMusiciEnsembleId != 0 ) {
		    // Add selection on musici-ensemble
		    musiciQueryString += "AND musici_ensemble.ensemble_id =" + selectedMusiciEnsembleId + " ";
		}

		musiciQueryString += "ORDER BY persoon, musici";

	    } else if ( selectedMusiciEnsembleId != 0 ) {
		// Selection only on ensemble
		musiciQueryString = "SELECT musici.musici_id, musici.musici, ensemble.ensemble ";
		musiciQueryString += "FROM musici ";
		musiciQueryString += "LEFT JOIN musici_ensemble ON musici_ensemble.musici_id = musici.musici_id ";
		musiciQueryString += "LEFT JOIN ensemble ON musici_ensemble.ensemble_id = ensemble.ensemble_id ";
		musiciQueryString += "WHERE ensemble.ensemble_id =" + selectedMusiciEnsembleId + " ";
		musiciQueryString += "ORDER BY ensemble, musici";

	    } else {
		// No selection on musici-persoon or musici-ensemble

		// Get a list of sorted musici, with or without ensemble: left join with musici_persoon,
		// then add a list of musici for which entry in musici_ensemble must exist: inner join,
		// and sort everything on the persoon column (which holds either the persoon or the ensemble), 
		// and sort on the musici for which neither a persoon or ensemble is present in musici_persoon,
		// musici_ensemble, respectively.

		musiciQueryString = "(SELECT musici.musici_id, musici.musici, persoon.persoon ";
		musiciQueryString += "FROM musici ";
		musiciQueryString += "LEFT JOIN musici_persoon ON musici_persoon.musici_id = musici.musici_id ";
		musiciQueryString += "LEFT JOIN persoon ON musici_persoon.persoon_id = persoon.persoon_id ";

		// Check if a musici filter is present
		if  ( ( musiciFilterString != null ) && ( musiciFilterString.length( ) > 0 ) ) {
		    // Add selection on musici titel
		    musiciQueryString +=
			"WHERE ( persoon.persoon LIKE '%" + musiciFilterString + "%' " +
			"OR musici.musici LIKE '%" + musiciFilterString + "%' ) ";
		}

		musiciQueryString += ") UNION ";
		musiciQueryString += "(SELECT musici.musici_id, musici.musici, ensemble.ensemble ";
		musiciQueryString += "FROM musici ";
		musiciQueryString += "INNER JOIN musici_ensemble ON musici.musici_id = musici_ensemble.musici_id ";
		musiciQueryString += "LEFT JOIN ensemble ON musici_ensemble.ensemble_id = ensemble.ensemble_id ";

		// Check if a musici filter is present
		if ( ( musiciFilterString != null ) && ( musiciFilterString.length( ) > 0 ) ) {
		    // Add selection on musici titel
		    musiciQueryString +=
			"WHERE ( ensemble.ensemble LIKE '%" + musiciFilterString + "%' " +
			"OR musici.musici LIKE '%" + musiciFilterString + "%' ) ";
		}

		// Order on the persoon or ensemble name, or on musici if these are NULL
		musiciQueryString += ") ORDER BY persoon, musici";
	    }

	    logger.fine( "musiciQueryString:\n" + musiciQueryString );

	    // Execute the query
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( musiciQueryString );

	    while ( resultSet.next( ) ) {
		String musiciString = null;

		String persoonEnsembleString = resultSet.getString( 3 );
		if ( persoonEnsembleString != null ) {
		    musiciString = persoonEnsembleString + " (";
		}

		if ( musiciString == null ) {
		    musiciString = resultSet.getString( 2 );
		} else {
		    musiciString += resultSet.getString( 2 );
		}

		if ( musiciString.length( ) > 90 ) {
		    musiciString = musiciString.substring( 0, 90 );
		}

		if ( persoonEnsembleString != null ) {
		    musiciString += ")";
		}

		// Store the musici_id in the map indexed by the musiciString
		musiciMap.put( musiciString, resultSet.getObject( 1 ) );

		// Add the musiciString to the combo box
		addItem( musiciString );

		// Check if this is the selected musici
		if ( resultSet.getInt( 1 ) == selectedMusiciId ) {
		    // Select this musici
		    setSelectedItem( musiciString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterMusiciComboBox( ) {
	String newMusiciFilterString = null;

	// Prompt for the musici filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newMusiciFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Musici filter:",
						       "Musici filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       musiciFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newMusiciFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Musici filter:",
						       "Musici filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       musiciFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled) 
	if ( newMusiciFilterString != null ) {
	    // Store the new musici filter
	    musiciFilterString = newMusiciFilterString;

	    // Reset the default musici-persoon and musici-ensemble, overruled by filter
	    selectedMusiciPersoonId = 0;
	    selectedMusiciEnsembleId = 0;

	    // Setup the musici combo box with the musici filter
	    // Reset the selected musici ID in order to avoid immediate selection
	    setupMusiciComboBox( 0 );
	}

	// Return current musici filter string, also when dialog has been canceled
	return musiciFilterString;
    }


    public String getSelectedMusiciString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedMusiciId( ) {
	String musiciString = ( String )getSelectedItem( );

	if ( musiciString == null ) return 0;

	// Check if empty string is selected
	if ( musiciString.length( ) == 0 ) return 0;

	// Get the musici_id from the map
	if ( musiciMap.containsKey( musiciString ) ) {
	    return ( ( Integer )musiciMap.get( musiciString ) ).intValue( );
	}

	return 0;
    }


    public boolean newMusiciSelected( ) {
	String musiciString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( musiciString == null ) return false;

	return musiciString.equals( newMusiciString );
    }
}
