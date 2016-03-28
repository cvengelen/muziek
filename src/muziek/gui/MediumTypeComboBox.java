// Class to setup a ComboBox for medium_type
// Simple version: no need to add or edit items

package muziek.gui;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.logging.*;

public class MediumTypeComboBox extends JComboBox< String > {
    private Map< String, Object > mediumTypeMap = new HashMap< >( );

    public MediumTypeComboBox( Connection conn,
                               int        selectedMediumTypeId ) {

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        try {
            // Fill the combo box and hash table
            Statement statement = conn.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT medium_type_id, medium_type " +
                                                          "FROM medium_type ORDER BY medium_type" );
            while ( resultSet.next( ) ) {
                String mediumTypeString = resultSet.getString( 2 );

                // Store the medium_type_id in the map indexed by the mediumTypeString
                mediumTypeMap.put( mediumTypeString, resultSet.getObject( 1 ) );

                // Add the mediumTypeString to the combo box
                addItem( mediumTypeString );

                // Check if this is the selected mediumType
                if ( resultSet.getInt( 1 ) == selectedMediumTypeId ) {
                    // Select this mediumType
                    setSelectedItem( mediumTypeString );
                }
            }
        } catch ( SQLException sqlException ) {
            Logger.getLogger( "muziek.gui.MediumTypeComboBox" ).severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 10 );
    }

    public int getSelectedMediumTypeId( ) {
	return getMediumTypeId( ( String )getSelectedItem( ) );
    }

    public int getMediumTypeId( String mediumTypeString ) {
        if ( mediumTypeString == null ) return 0;

        // Check if empty string is selected
        if ( mediumTypeString.length( ) == 0 ) return 0;

        // Get the medium_type_id from the map
        if ( mediumTypeMap.containsKey( mediumTypeString ) ) {
            return ( ( Integer )mediumTypeMap.get( mediumTypeString ) ).intValue( );
        }

        return 0;
    }
}
