/**
 * ComboBox for selection of a componisten record.
 *
 * @author Chris van Engelen
 *
 * History:     2005/03/28: Initial version
 *              2009/01/01: Add selection on Componist-Persoon
 *              2016/05/20: Add generics
 */

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import javax.swing.*;

public class ComponistenComboBox extends JComboBox< String > {
    final private Logger logger = Logger.getLogger( ComponistenComboBox.class.getCanonicalName() );

    private Connection conn;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    private Object parentObject;

    private Map< String, Object > componistenMap = new HashMap< >( );
    private int selectedComponistenPersoonId = 0;
    private int selectedComponistenId = 0;
    private String componistenFilterString = null;
    private String newComponistenString = null;

    private boolean allowNewComponisten = true;

    public ComponistenComboBox( Connection conn,
                                Object     parentObject,
                                boolean    allowNewComponisten ) {
        this.conn = conn;
        this.parentObject = parentObject;
        this.allowNewComponisten = allowNewComponisten;

        // Setup the componisten combo box
        setupComponistenComboBox( );
    }

    public ComponistenComboBox( Connection conn,
                                Object     parentObject,
                                int            selectedComponistenPersoonId,
                                int        selectedComponistenId ) {
        this.conn = conn;
        this.parentObject = parentObject;
        this.selectedComponistenPersoonId = selectedComponistenPersoonId;
        this.selectedComponistenId = selectedComponistenId;

        // Setup the componisten combo box
        setupComponistenComboBox( );
    }


    public void setupComponistenComboBox( int selectedComponistenId ) {
        this.selectedComponistenId = selectedComponistenId;

        // Setup the componisten combo box
        setupComponistenComboBox( );
    }


    public void setupComponistenComboBox( ) {
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

        if ( !componistenMap.isEmpty( ) ) {
            // Remove all items in the componisten hash table
            componistenMap.clear( );
        }

        try {
            // Fill the combo box and hash table
            String componistenQueryString = ( "SELECT componisten.componisten_id, " +
                                              "componisten.componisten, persoon.persoon " +
                                              "FROM componisten " +
                                              "LEFT JOIN componisten_persoon ON componisten.componisten_id = componisten_persoon.componisten_id " +
                                              "LEFT JOIN persoon ON componisten_persoon.persoon_id = persoon.persoon_id " );

            if ( ( ( componistenFilterString != null ) && ( componistenFilterString.length( ) > 0 ) ) ||
                 ( selectedComponistenPersoonId > 0 ) ) {
                componistenQueryString += "WHERE ";

                // Check for a componisten filter
                if ( ( componistenFilterString != null ) && ( componistenFilterString.length( ) > 0 ) ) {
                    // Add filter to query, both on componisten and persoon
                    componistenQueryString += ( "( componisten LIKE '%" + componistenFilterString +
                                                "%' ) || ( persoon LIKE '%" + componistenFilterString + "%' ) " );

                    if ( selectedComponistenPersoonId != 0 ) {
                        componistenQueryString += "AND ";
                    }
                }

                if ( selectedComponistenPersoonId != 0 ) {
                    componistenQueryString += "componisten_persoon.persoon_id = " + selectedComponistenPersoonId + " ";
                }
            }

            // Add order to query
            componistenQueryString += "ORDER BY persoon, componisten";

            Statement statement = conn.createStatement( );
            ResultSet resultSet = statement.executeQuery( componistenQueryString );

            while ( resultSet.next( ) ) {
                String componistString = resultSet.getString( 3 );
                if ( componistString == null ) {
                    componistString = "-";
                }

                String componistenString = resultSet.getString( 2 );
                if ( !( componistenString.equals( componistString ) ) ) {
                    componistString += " (" + componistenString + ")";
                }

                if ( componistString.length( ) > 70 ) {
                    componistString = componistString.substring( 0, 70 ) + "...";
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


    public String filterComponistenComboBox( ) {
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
            // Reset the selected componisten ID in order to avoid immediate selection
            setupComponistenComboBox( 0 );
        }

        // Return current componisten filter string, also when dialog has been canceled
        return componistenFilterString;
    }


    public int getSelectedComponistenId( ) {
        String componistenString = ( String )getSelectedItem( );

        if ( componistenString == null ) return 0;

        // Check if empty string is selected
        if ( componistenString.length( ) == 0 ) return 0;

        // Get the componisten_id from the map
        if ( componistenMap.containsKey( componistenString ) ) {
            return ( Integer )componistenMap.get( componistenString );
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
