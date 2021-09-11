// Class to setup a ComboBox for jaar
// Simple version: no need to add or edit items

package muziek.gui;

import javax.swing.*;

public class JaarComboBox extends JComboBox {

    public JaarComboBox( int selectedJaar ) {
	// Add empty item for when jaar is not applicable
	addItem( "" );

	for ( int year = 1900; year <= 2030; year++ ) {
	    // Add the jaarString to the combo box
	    addItem( String.valueOf( year ) );

	    // Check if this is the selected jaar
	    if ( year == selectedJaar ) {
		// Select this jaar
		setSelectedItem( String.valueOf( year ) );
	    }
	}

	setMaximumRowCount( 20 );
    }
}
