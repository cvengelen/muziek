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

import muziek.gui.EditComponistenDialog;
import muziek.gui.PersoonComboBox;

import table.*;

/**
 * Frame to show, insert and update records in the componisten table in schema muziek.
 * @author Chris van Engelen
 */
public class EditComponisten extends JInternalFrame {
    private final Logger logger = Logger.getLogger(EditComponisten.class.getCanonicalName());

    private final Connection connection;
    private final JFrame parentFrame;

    private JTextField componistenFilterTextField;

    private PersoonComboBox persoonComboBox;
    private int selectedPersoonId = 0;

    private ComponistenTableModel componistenTableModel;
    private TableSorter componistenTableSorter;

    private class Componisten {
        int        id;
        String  string;

        Componisten( int    id,
                     String string ) {
            this.id = id;
            this.string = string;
        }

        boolean presentInTable( String tableString ) {
            // Check if componistenId is present in table
            try {
                Statement statement = connection.createStatement( );
                ResultSet resultSet = statement.executeQuery( "SELECT componisten_id FROM " + tableString +
                                                              " WHERE componisten_id = " + id );
                if ( resultSet.next( ) ) {
                    JOptionPane.showMessageDialog( parentFrame,
                                                   "Tabel " + tableString + " heeft nog verwijzing naar '" + string + "'",
                                                   "Edit componisten error",
                                                   JOptionPane.ERROR_MESSAGE );
                    return true;
                }
            } catch ( SQLException sqlException ) {
                JOptionPane.showMessageDialog( parentFrame,
                                               "SQL exception in select: " + sqlException.getMessage(),
                                               "EditComponisten SQL exception",
                                               JOptionPane.ERROR_MESSAGE );
                logger.severe( "SQLException: " + sqlException.getMessage( ) );
                return true;
            }
            return false;
        }
    }

    public EditComponisten( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit componisten", true, true, true, true);

        this.connection = connection;
        this.parentFrame = parentFrame;

        // put the controls the content pane
        Container container = getContentPane();

        // Set grid bag layout manager
        container.setLayout( new GridBagLayout( ) );
        GridBagConstraints constraints = new GridBagConstraints( );

        constraints.insets = new Insets( 20, 20, 5, 5 );
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Componisten Filter:" ), constraints );
        componistenFilterTextField = new JTextField( 30 );

        constraints.insets = new Insets( 20, 5, 5, 40 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( componistenFilterTextField, constraints );

        componistenFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the componisten table
            componistenTableSorter.clearSortingState();
            componistenTableModel.setupComponistenTableModel( componistenFilterTextField.getText( ),
                                                              selectedPersoonId );
        } );

        ////////////////////////////////////////////////
        // Persoon ComboBox
        ////////////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0d;
        constraints.fill = GridBagConstraints.NONE;
        container.add( new JLabel( "Persoon: " ), constraints );

        final JPanel persoonPanel = new JPanel( );
        final Border emptyBorder = new EmptyBorder( -5, -5, -5, -5 );
        persoonPanel.setBorder( emptyBorder );

        // Setup a JComboBox with the results of the query on persoon
        // Do not allow to enter new record in persoon
        persoonComboBox = new PersoonComboBox( connection, parentFrame, false );
        persoonComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected persoon ID from the combo box
            selectedPersoonId = persoonComboBox.getSelectedPersoonId( );

            // Setup the componisten table
            componistenTableSorter.clearSortingState();
            componistenTableModel.setupComponistenTableModel( componistenFilterTextField.getText( ),
                                                              selectedPersoonId );
        } );
        persoonPanel.add( persoonComboBox );

        JButton filterPersoonButton = new JButton( "Filter" );
        filterPersoonButton.setActionCommand( "filterPersoon" );
        filterPersoonButton.addActionListener( ( ActionEvent actionEvent ) -> persoonComboBox.filterPersoonComboBox( ) );
        persoonPanel.add( filterPersoonButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( persoonPanel, constraints );


        // Create componisten table from title table model
        componistenTableModel = new ComponistenTableModel( connection, parentFrame );
        componistenTableSorter = new TableSorter( componistenTableModel );
        JTable componistenTable = new JTable( componistenTableSorter );
        componistenTableSorter.setTableHeader( componistenTable.getTableHeader( ) );
        // componistenTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

        componistenTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        componistenTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

        componistenTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
        componistenTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 300 );  // componisten
        componistenTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 200 );  // persoon

        // Set vertical size just enough for 20 entries
        componistenTable.setPreferredScrollableViewportSize( new Dimension( 550, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
        container.add( new JScrollPane( componistenTable ), constraints );


        // Define the edit, delete button because it is used by the list selection listener
        final JButton editComponistenButton = new JButton( "Edit" );
        final JButton deleteComponistenButton = new JButton( "Delete" );

        // Get the selection model related to the rekening_mutatie table
        final ListSelectionModel componistenListSelectionModel = componistenTable.getSelectionModel( );

        class ComponistenListSelectionListener implements ListSelectionListener {
            private int selectedRow = -1;

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
                    setVisible( false );
                    dispose();
                    return;
                } else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
                    // Insert new componisten record
                    new EditComponistenDialog( connection, parentFrame,
                                               componistenFilterTextField.getText( ) );
                } else {
                    int selectedRow = componistenListSelectionListener.getSelectedRow( );
                    if ( selectedRow < 0 ) {
                        JOptionPane.showMessageDialog( parentFrame,
                                                       "Geen componisten geselecteerd",
                                                       "Edit componisten error",
                                                       JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    // Get the selected componisten id
                    int selectedComponistenId = componistenTableModel.getComponistenId( selectedRow );

                    // Check if componisten has been selected
                    if ( selectedComponistenId == 0 ) {
                        JOptionPane.showMessageDialog( parentFrame,
                                                       "Geen componisten geselecteerd",
                                                       "Edit componisten error",
                                                       JOptionPane.ERROR_MESSAGE );
                        return;
                    }

                    if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
                        // Do dialog
                        new EditComponistenDialog( connection, parentFrame, selectedComponistenId );

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
                            JOptionPane.showConfirmDialog( parentFrame,
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
                                JOptionPane.showMessageDialog( parentFrame,
                                                               errorString,
                                                               "Edit componisten error",
                                                               JOptionPane.ERROR_MESSAGE);
                                logger.severe( errorString );
                                return;
                            }
                        } catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditComponisten SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
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

        constraints.insets = new Insets( 5, 20, 20, 20 );
        constraints.gridx = 0;
        constraints.gridy = 3;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;
        container.add( buttonPanel, constraints );

        setSize( 610, 550 );
        setLocation( x, y );
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        setVisible(true);
    }
}
