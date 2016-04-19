/**
 * Frame to show and select records from componisten.
 *
 * @author Chris van Engelen
 */

package muziek.componisten;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.logging.*;

import muziek.gui.PersoonComboBox;

import table.*;


public class ComponistenFrame {
    final Logger logger = Logger.getLogger( "muziek.componisten.ComponistenFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Componisten" );

    JTextField componistenFilterTextField;

    PersoonComboBox persoonComboBox;
    int selectedPersoonId = 0;

    ComponistenTableModel componistenTableModel;
    TableSorter componistenTableSorter;
    JTable componistenTable;


    class Componisten {
        int        id;
        String  string;

        public Componisten( int    id,
                            String string ) {
            this.id = id;
            this.string = string;
        }

        public boolean presentInTable( String tableString ) {
            // Check if componistenId is present in table
            try {
                Statement statement = connection.createStatement( );
                ResultSet resultSet = statement.executeQuery( "SELECT componisten_id FROM " + tableString +
                                                              " WHERE componisten_id = " + id );
                if ( resultSet.next( ) ) {
                    JOptionPane.showMessageDialog( frame,
                                                   "Tabel " + tableString +
                                                   " heeft nog verwijzing naar '" + string + "'",
                                                   "Componisten frame error",
                                                   JOptionPane.ERROR_MESSAGE );
                    return true;
                }
            } catch ( SQLException sqlException ) {
                logger.severe( "SQLException: " + sqlException.getMessage( ) );
                return true;
            }
            return false;
        }
    }


    public ComponistenFrame( final Connection connection ) {
        this.connection = connection;

        // put the controls the content pane
        Container container = frame.getContentPane();

        // Set grid bag layout manager
        container.setLayout( new GridBagLayout( ) );
        GridBagConstraints constraints = new GridBagConstraints( );
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets( 0, 0, 10, 10 );

        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Componisten Filter:" ), constraints );
        componistenFilterTextField = new JTextField( 30 );

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( componistenFilterTextField, constraints );

        class ComponistenFilterActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Setup the componisten table
                componistenTableModel.setupComponistenTableModel( componistenFilterTextField.getText( ),
                                                                  selectedPersoonId );
            }
        }
        componistenFilterTextField.addActionListener( new ComponistenFilterActionListener( ) );


        ////////////////////////////////////////////////
        // Persoon ComboBox
        ////////////////////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Persoon: " ), constraints );

        final JPanel persoonPanel = new JPanel( );
        final Border emptyBorder = new EmptyBorder( -5, -5, -5, -5 );
        persoonPanel.setBorder( emptyBorder );

        // Setup a JComboBox with the results of the query on persoon
        // Do not allow to enter new record in persoon
        persoonComboBox = new PersoonComboBox( connection, frame, false );
        persoonPanel.add( persoonComboBox );

        class SelectPersoonActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Get the selected persoon ID from the combo box
                selectedPersoonId = persoonComboBox.getSelectedPersoonId( );

                // Setup the componisten table
                componistenTableModel.setupComponistenTableModel( componistenFilterTextField.getText( ),
                                                                  selectedPersoonId );
            }
        }
        persoonComboBox.addActionListener( new SelectPersoonActionListener( ) );

        JButton filterPersoonButton = new JButton( "Filter" );
        filterPersoonButton.setActionCommand( "filterPersoon" );
        persoonPanel.add( filterPersoonButton );

        class FilterPersoonActionListener implements ActionListener {
            public void actionPerformed( ActionEvent ae ) {
                persoonComboBox.filterPersoonComboBox( );
            }
        }
        filterPersoonButton.addActionListener( new FilterPersoonActionListener( ) );

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( persoonPanel, constraints );


        // Create componisten table from title table model
        componistenTableModel = new ComponistenTableModel( connection );
        componistenTableSorter = new TableSorter( componistenTableModel );
        componistenTable = new JTable( componistenTableSorter );
        componistenTableSorter.setTableHeader( componistenTable.getTableHeader( ) );
        // componistenTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

        componistenTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        componistenTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

        componistenTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
        componistenTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 300 );  // componisten
        componistenTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 200 );  // persoon

        // Set vertical size just enough for 20 entries
        componistenTable.setPreferredScrollableViewportSize( new Dimension( 550, 320 ) );

        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets( 10, 0, 10, 10 );
        container.add( new JScrollPane( componistenTable ), constraints );


        // Define the edit, delete button because it is used by the list selection listener
        final JButton editComponistenButton = new JButton( "Edit" );
        final JButton deleteComponistenButton = new JButton( "Delete" );

        // Get the selection model related to the rekening_mutatie table
        final ListSelectionModel componistenListSelectionModel = componistenTable.getSelectionModel( );

        class ComponistenListSelectionListener implements ListSelectionListener {
            int selectedRow = -1;

            public void valueChanged( ListSelectionEvent listSelectionEvent ) {
                // Ignore extra messages.
                if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

                // Ignore if nothing is selected
                if ( componistenListSelectionModel.isSelectionEmpty( ) ) {
                    selectedRow = -1;
                    editComponistenButton.setEnabled( false );
                    deleteComponistenButton.setEnabled( false );
                    return;
                }

                int viewRow = componistenListSelectionModel.getMinSelectionIndex( );
                selectedRow = componistenTableSorter.modelIndex( viewRow );
                editComponistenButton.setEnabled( true );
                deleteComponistenButton.setEnabled( true );
            }

            public int getSelectedRow ( ) { return selectedRow; }
        }

        // Add componistenListSelectionListener object to the selection model of the musici table
        final ComponistenListSelectionListener componistenListSelectionListener = new ComponistenListSelectionListener( );
        componistenListSelectionModel.addListSelectionListener( componistenListSelectionListener );

        // Class to handle button actions: uses componistenListSelectionListener
        class ButtonActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
                    frame.setVisible( false );
                    System.exit( 0 );
                } else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
                    // Insert new componisten record
                    EditComponistenDialog editComponistenDialog =
                        new EditComponistenDialog( connection, frame,
                                                   componistenFilterTextField.getText( ) );
                } else {
                    int selectedRow = componistenListSelectionListener.getSelectedRow( );
                    if ( selectedRow < 0 ) {
                        JOptionPane.showMessageDialog( frame,
                                                       "Geen componisten geselecteerd",
                                                       "Componisten frame error",
                                                       JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    // Get the selected componisten id
                    int selectedComponistenId = componistenTableModel.getComponistenId( selectedRow );

                    // Check if componisten has been selected
                    if ( selectedComponistenId == 0 ) {
                        JOptionPane.showMessageDialog( frame,
                                                       "Geen componisten geselecteerd",
                                                       "Componisten frame error",
                                                       JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
                        // Do dialog
                        EditComponistenDialog editComponistenDialog =
                            new EditComponistenDialog( connection, frame, selectedComponistenId );

                    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
                        final Componisten componisten = new Componisten( componistenTableModel.getComponistenId( selectedRow ),
                                                                         componistenTableModel.getComponistenString( selectedRow ) );

                        // Check if componisten ID is still used
                        if ( componisten.presentInTable( "componisten_persoon" ) ) return;
                        if ( componisten.presentInTable( "opus" ) ) return;

                        // Replace null or empty string by single space for messages
                        if ( ( componisten.string == null ) || ( componisten.string.length( ) == 0  ) ) {
                            componisten.string = " ";
                        }

                        int result =
                            JOptionPane.showConfirmDialog( frame,
                                                           "Delete '" + componisten.string + "' ?",
                                                           "Delete Componisten record",
                                                           JOptionPane.YES_NO_OPTION,
                                                           JOptionPane.QUESTION_MESSAGE,
                                                           null );

                        if ( result != JOptionPane.YES_OPTION ) return;

                        String deleteString  = "DELETE FROM componisten";
                        deleteString += " WHERE componisten_id = " + componisten.id;

                        logger.info( "deleteString: " + deleteString );

                        try {
                            Statement statement = connection.createStatement( );
                            int nUpdate = statement.executeUpdate( deleteString );
                            if ( nUpdate != 1 ) {
                                String errorString = ( "Could not delete record with componisten_id  = " +
                                                       componisten.id + " in componisten" );
                                JOptionPane.showMessageDialog( frame,
                                                               errorString,
                                                               "Delete Componisten record",
                                                               JOptionPane.ERROR_MESSAGE);
                                logger.severe( errorString );
                                return;
                            }
                        } catch ( SQLException sqlException ) {
                            logger.severe( "SQLException: " + sqlException.getMessage( ) );
                            return;
                        }
                    }
                }

                // Records may have been modified: setup the table model again
                componistenTableSorter.clearSortingState( );
                componistenTableModel.setupComponistenTableModel( componistenFilterTextField.getText( ),
                                                                  selectedPersoonId );
            }
        }
        final ButtonActionListener buttonActionListener = new ButtonActionListener( );

        JPanel buttonPanel = new JPanel( );

        final JButton insertComponistenButton = new JButton( "Insert" );
        insertComponistenButton.setActionCommand( "insert" );
        insertComponistenButton.addActionListener( buttonActionListener );
        buttonPanel.add( insertComponistenButton );

        editComponistenButton.setActionCommand( "edit" );
        editComponistenButton.setEnabled( false );
        editComponistenButton.addActionListener( buttonActionListener );
        buttonPanel.add( editComponistenButton );

        deleteComponistenButton.setActionCommand( "delete" );
        deleteComponistenButton.setEnabled( false );
        deleteComponistenButton.addActionListener( buttonActionListener );
        buttonPanel.add( deleteComponistenButton );

        final JButton closeButton = new JButton( "Close" );
        closeButton.setActionCommand( "close" );
        closeButton.addActionListener( buttonActionListener );
        buttonPanel.add( closeButton );

        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.insets = new Insets( 10, 0, 0, 10 );
        container.add( buttonPanel, constraints );

        frame.setSize( 630, 550 );
        frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        frame.setVisible(true);
    }
}
