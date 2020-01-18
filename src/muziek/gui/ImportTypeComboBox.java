// Class to setup a ComboBox for import_type
// Simple version: no need to add or edit items

package muziek.gui;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ImportTypeComboBox extends JComboBox< String > {
    private Map< String, Object > importTypeMap = new HashMap< >( );

    public ImportTypeComboBox(Connection conn,
                              int        selectedImportTypeId ) {

        // Add first empty item to force selection of non-empty item
        addItem( "" );

        try {
            // Fill the combo box and hash table
            Statement statement = conn.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT import_type_id, import_type " +
                                                          "FROM import_type ORDER BY import_type" );
            while ( resultSet.next( ) ) {
                String importTypeString = resultSet.getString( 2 );

                // Store the import_type_id in the map indexed by the importTypeString
                importTypeMap.put( importTypeString, resultSet.getObject( 1 ) );

                // Add the importTypeString to the combo box
                addItem( importTypeString );

                // Check if this is the selected importType
                if ( resultSet.getInt( 1 ) == selectedImportTypeId ) {
                    // Select this importType
                    setSelectedItem( importTypeString );
                }
            }
        } catch ( SQLException sqlException ) {
            Logger.getLogger( "muziek.gui.ImportTypeComboBox" ).severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setMaximumRowCount( 10 );
    }

    public int getSelectedImportTypeId( ) {
	return getImportTypeId( ( String )getSelectedItem( ) );
    }

    public int getImportTypeId( String importTypeString ) {
        if ( importTypeString == null ) return 0;

        // Check if empty string is selected
        if ( importTypeString.length( ) == 0 ) return 0;

        // Get the import_type_id from the map
        if ( importTypeMap.containsKey( importTypeString ) ) {
            return ( ( Integer )importTypeMap.get( importTypeString ) ).intValue( );
        }

        return 0;
    }
}
