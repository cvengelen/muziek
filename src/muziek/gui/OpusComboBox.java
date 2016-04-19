//
// Project:	muziek
// Component:	gui
// File:	OpusComboBox.java
// Description:	ComboBox for selection of record in opus table
// Author:	Chris van Engelen
// History:	2005/04/02: Initial version
//		2009/01/01: Add selection on Componist-Persoon
//

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;


public class OpusComboBox extends JComboBox< String > {
	private static final long serialVersionUID = 1L;

	final private Logger logger = Logger.getLogger( "muziek.gui.OpusComboBox" );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    // Storage for genre and componist pre-selected in selection dialog
    private int selectedComponistenPersoonId = 0;
    private int selectedComponistenId = 0;
    private int selectedGenreId = 0;
    private int selectedTypeId = 0;

    private Map< String, Object > opusMap = new HashMap< String, Object >( );
    private int selectedOpusId = 0;
    private String opusFilterString = null;
    private String newOpusString = null;


    public OpusComboBox( Connection conn,
			 Object     parentObject ) {
	this.conn = conn;
	this.parentObject = parentObject;

	// Setup the Opus combo box
	setupOpusComboBox( );
    }


    public OpusComboBox( Connection conn,
			 Object     parentObject,
			 String     opusFilterString,
			 int	    selectedComponistenPersoonId,
			 int        selectedComponistenId,
			 int        selectedGenreId,
			 int        selectedTypeId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.opusFilterString = opusFilterString;
	this.selectedComponistenPersoonId = selectedComponistenPersoonId;
	this.selectedComponistenId = selectedComponistenId;
	this.selectedGenreId = selectedGenreId;
	this.selectedTypeId = selectedTypeId;

	// Setup the Opus combo box
	setupOpusComboBox( );
    }


    public OpusComboBox( Connection conn,
			 Object     parentObject,
			 int        selectedOpusId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.selectedOpusId = selectedOpusId;

	// Setup the Opus combo box
	setupOpusComboBox( );
    }


    public void setupOpusComboBox( int selectedOpusId ) {
	this.selectedOpusId = selectedOpusId;

	// Setup the opus combo box
	setupOpusComboBox( );
    }


    void setupOpusComboBox( int selectedGenreId,
			    int selectedComponistenPersoonId,
			    int selectedComponistenId ) {
	this.selectedGenreId = selectedGenreId;
	this.selectedComponistenPersoonId = selectedComponistenPersoonId;
	this.selectedComponistenId = selectedComponistenId;

	// New selected componist: reset the selected opus
	this.selectedOpusId = 0;

	// Setup the opus combo box
	setupOpusComboBox( );
    }


    void setupOpusComboBox( String opusFilterString,
			    int    selectedComponistenPersoonId,
			    int    selectedComponistenId,
			    int    selectedGenreId,
			    int    selectedTypeId ) {
	this.opusFilterString = opusFilterString;
	this.selectedComponistenPersoonId = selectedComponistenPersoonId;
	this.selectedComponistenId = selectedComponistenId;
	this.selectedGenreId = selectedGenreId;
	this.selectedTypeId = selectedTypeId;

	// New selected componist: reset the selected opus
	this.selectedOpusId = 0;

	// Setup the opus combo box
	setupOpusComboBox( );
    }


    void setupOpusComboBox( ) {
	// Remove all existing items in the opus combo box
	removeAllItems( );

	// Add first empty item to force selection of non-empty item
	addItem( "" );

	// Add special item to insert new opus
	newOpusString = "Nieuw opus ";
	if ( ( opusFilterString != null ) && ( opusFilterString.length( ) > 0 ) ) {
	    newOpusString += opusFilterString + " ";
	}
	newOpusString += "toevoegen";
	addItem( newOpusString );

	if ( !opusMap.isEmpty( ) ) {
	    // Remove all items in the opus hash table
	    opusMap.clear( );
	}

	try {
	    // Fill the combo box and hash table
	    String opusQueryString =
		new String( "SELECT opus_id, persoon, opus_titel, opus_nummer, genre FROM opus " +
			    "LEFT JOIN genre ON opus.genre_id = genre.genre_id " +
			    "LEFT JOIN componisten ON opus.componisten_id = componisten.componisten_id " +
			    "LEFT JOIN componisten_persoon ON opus.componisten_id = componisten_persoon.componisten_id " +
			    "LEFT JOIN persoon ON componisten_persoon.persoon_id = persoon.persoon_id " +
			    "LEFT JOIN type ON opus.type_id = type.type_id " +
			    "LEFT JOIN subtype ON opus.subtype_id = subtype.subtype_id " );

	    // Check if an opus filter is present, or if a componist is selected
	    if ( ( ( opusFilterString != null ) && ( opusFilterString.length( ) > 0 ) ) ||
		 ( selectedComponistenPersoonId > 0 ) ||
		 ( selectedComponistenId > 0 ) ||
		 ( selectedGenreId > 0 ) ||
		 ( selectedTypeId > 0 ) ) {
		// Add WHERE clause
		opusQueryString += "WHERE ";

		// Check if a opus filter is present
		if ( ( opusFilterString != null ) && ( opusFilterString.length( ) > 0 ) ) {
		    // Add selection on opus titel or componist
		    opusQueryString +=
			"( opus_titel LIKE '%" + opusFilterString + "%' " +
			"OR persoon.persoon LIKE '%" + opusFilterString + "%' ) ";

		    // Check if also componist, genre or type is selected
		    if ( ( selectedComponistenPersoonId > 0 ) ||
			 ( selectedComponistenId > 0 ) ||
			 ( selectedGenreId > 0 ) ||
			 ( selectedTypeId > 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		// Check if componist-persoon is selected
		if ( selectedComponistenPersoonId > 0 ) {
		    // Add selection on componist-persoon
		    opusQueryString += "componisten_persoon.persoon_id = " + selectedComponistenPersoonId + " ";

		    // Check if also componisten, genre or type is selected
		    if ( ( selectedComponistenId > 0 ) ||
			 ( selectedGenreId > 0 ) ||
			 ( selectedTypeId > 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		// Check if componisten is selected
		if ( selectedComponistenId > 0 ) {
		    // Add selection on componisten
		    opusQueryString += "opus.componisten_id = " + selectedComponistenId + " ";

		    // Check if also genre or type is selected
		    if ( ( selectedGenreId > 0 ) ||
			 ( selectedTypeId > 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		// Check if genre is selected
		if ( selectedGenreId > 0 ) {
		    // Add selection on genre
		    opusQueryString += "opus.genre_id = " + selectedGenreId + " ";

		    // Check if also type is selected
		    if ( selectedTypeId > 0 ) {
			opusQueryString += "AND ";
		    }
		}

		// Check if type is selected
		if ( selectedTypeId > 0 ) {
		    // Add selection on type
		    opusQueryString += "type.type_id = " + selectedTypeId + " ";
		}
	    }

	    // Add order (ordering for type is on type_id, fixed order)
	    opusQueryString += "ORDER BY persoon, opus.type_id, opus.subtype_id, opus.opus_nummer, opus.opus_titel";

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opusQueryString );

	    while ( resultSet.next( ) ) {
		String opusString = null;

		// Start with componist, if present
		String componistString = resultSet.getString( 2 );
		if ( componistString != null ) {
		    opusString = componistString + ": ";
		}

		// Add composition title
		if ( opusString != null ) {
		    opusString += resultSet.getString( 3 );
		} else {
		    opusString = resultSet.getString( 3 );
		}

		String opusNummerString = resultSet.getString( 4 );
		if ( opusNummerString != null && opusNummerString.length( ) > 0 ) {
		    opusString += ", " + opusNummerString;
		}

		String genreString = resultSet.getString( 5 );
		if ( genreString != null && genreString.length( ) > 0 ) {
		    opusString += " (" + genreString + ")";
		}

		if ( opusString.length( ) > 100 ) {
		    opusString = opusString.substring( 0, 100 );
		}

		// Store the opus_id in the map indexed by the opusTitelString
		opusMap.put( opusString, resultSet.getObject( 1 ) );

		// Add the opusTitelString to the combo box
		addItem( opusString );

		// Check if this is the default opus
		if ( resultSet.getInt( 1 ) == selectedOpusId ) {
		    // Select this opus
		    setSelectedItem( opusString );
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setMaximumRowCount( 20 );
    }


    public String filterOpusComboBox( ) {
	String newOpusFilterString = null;

	// Prompt for the opus filter, using the current value as default
	if ( parentObject instanceof JFrame ) {
	    newOpusFilterString =
		( String )JOptionPane.showInputDialog( ( JFrame )parentObject,
						       "Opus filter:",
						       "Opus filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       opusFilterString );
	} else if ( parentObject instanceof JDialog ) {
	    newOpusFilterString =
		( String )JOptionPane.showInputDialog( ( JDialog )parentObject,
						       "Opus filter:",
						       "Opus filter dialog",
						       JOptionPane.QUESTION_MESSAGE,
						       null,
						       null,
						       opusFilterString );
	}

	// Check if dialog was completed successfully (i.e., not canceled)
	if ( newOpusFilterString != null ) {
	    // Store the new opus filter
	    opusFilterString = newOpusFilterString;

	    // Setup the opus combo box with the opus filter
	    // Reset the selected opus ID in order to avoid immediate selection
	    setupOpusComboBox( 0 );
	}

	// Return current opus filter string, also when dialog has been canceled
	return opusFilterString;
    }


    public String getSelectedOpusString( ) {
	return ( String )getSelectedItem( );
    }


    public int getSelectedOpusId( ) {
	String opusString = ( String )getSelectedItem( );

	if ( opusString == null ) return 0;

	// Check if empty string is selected
	if ( opusString.length( ) == 0 ) return 0;

	// Get the opus_id from the map
	if ( opusMap.containsKey( opusString ) ) {
	    return ( ( Integer )opusMap.get( opusString ) ).intValue( );
	}

	return 0;
    }


    public boolean newOpusSelected( ) {
	String opusString = ( String )getSelectedItem( );

	// Check if empty string is selected
	if ( opusString == null ) return false;

	return opusString.equals( newOpusString );
    }
}