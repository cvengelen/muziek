// Class to setup a ComboBox for maand

package muziek.gui;

import java.util.*;
import javax.swing.*;

public class MaandComboBox extends JComboBox {
    private static final String [ ] maandString = { "",
						    "Januari", "Februari", "Maart",
						    "April", "Mei", "Juni",
						    "Juli", "Augustus", "September",
						    "Oktober", "November", "December" };
    private Map maandMap = new HashMap( );

    public MaandComboBox( int selectedMaand ) {

	// Setup the Maand combo box
	for ( int monthIdx = 0; monthIdx <= 12; monthIdx++ ) {
	    // Store monthIdx in the map indexed by maanString
	    maandMap.put( maandString[ monthIdx ], new Integer( monthIdx ) );

	    // Add the maandString to the combo box
	    addItem( maandString[ monthIdx ] );

	    // Check if this is the selected maand
	    if ( monthIdx == selectedMaand ) {
		// Select this maand
		setSelectedItem( maandString[ monthIdx ] );
	    }
	}

	setMaximumRowCount( 13 );
    }

    public int getSelectedMaand( ) {
	String maandString = ( String )getSelectedItem( );

	if ( maandString == null ) return 0;

	// Check if empty string is selected
	if ( maandString.length( ) == 0 ) return 0;

	// Get the maand from the map
	if ( maandMap.containsKey( maandString ) ) {
	    return ( ( Integer )maandMap.get( maandString ) ).intValue( );
	}

	return 0;
    }
}
