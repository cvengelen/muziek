//
// Project:	muziek
// Component:	gui
// File:	EditOpusDialog.java
// Description:	Dialog for inserting or updating a record in opus
// Author:	Chris van Engelen
// History:	2005/04/02: Initial version
//		2009/01/01: Add selection on Componist-Persoon
//		2009/11/23: Add tekstschrijver_persoon table
//

package muziek.gui;

import muziek.componisten.ComponistenPersoonTableModel;
import muziek.componisten.EditComponistenDialog;
import muziek.componisten.TekstschrijverPersoonTableModel;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.logging.Logger;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

public class EditOpusDialog {
    final private Logger logger = Logger.getLogger( "muziek.gui.EditOpusDialog" );

    Connection connection;
    Object parentObject;
    JDialog dialog;

    int opusId = 0;

    String defaultOpusTitelString = "";
    JTextField opusTitelTextField;

    String defaultOpusNummerString = "";
    JTextField opusNummerTextField;

    ComponistenComboBox componistenComboBox;
    ComponistenPersoonTableModel componistenPersoonTableModel;
    JTable componistenPersoonTable;
    TekstschrijverPersoonTableModel tekstschrijverPersoonTableModel;
    JTable tekstschrijverPersoonTable;
    String defaultComponistenString;
    String componistenFilterString = null;
    int defaultComponistenPersoonId = 0;
    int defaultComponistenId = 0;

    GenreComboBox genreComboBox;
    int defaultGenreId = 0;

    TijdperkComboBox tijdperkComboBox;
    int defaultTijdperkId = 0;

    TypeComboBox typeComboBox;
    int defaultTypeId = 0;

    SubtypeComboBox subtypeComboBox;
    int defaultSubtypeId = 0;
    String subtypeFilterString = null;

    // JSpinner opusDeelNummerSpinner;

    OpusDeelTableModel opusDeelTableModel;
    JTable opusDeelTable;

    int nUpdate = 0;

    final String insertOpusActionCommand = "insertOpus";
    final String updateOpusActionCommand = "updateOpus";

    // Pattern to find a single quote in the opus titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor for inserting a record in opus
    public EditOpusDialog( Connection connection,
                           Object     parentObject,
                           int        selectedGenreId,
                           int	      selectedComponistenPersoonId,
                           int        selectedComponistenId,
                           String     defaultOpusTitelString ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.defaultGenreId = selectedGenreId;
        this.defaultComponistenPersoonId = selectedComponistenPersoonId;
        this.defaultComponistenId = selectedComponistenId;
        this.defaultOpusTitelString = defaultOpusTitelString;

        // Setup componisten table with connection
        componistenPersoonTableModel = new ComponistenPersoonTableModel( connection );

        // Setup tekstschrijver table with connection
        tekstschrijverPersoonTableModel = new TekstschrijverPersoonTableModel( connection );

        setupOpusDialog( "Insert opus", "Insert", insertOpusActionCommand );
    }


    // Constructor for inserting a record in opus
    public EditOpusDialog( Connection connection,
                           Object     parentObject,
                           String     defaultOpusTitelString,
                           int	      selectedComponistenPersoonId,
                           int        selectedComponistenId,
                           int        selectedGenreId,
                           int        selectedTypeId ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.defaultOpusTitelString = defaultOpusTitelString;
        this.defaultComponistenPersoonId = selectedComponistenPersoonId;
        this.defaultComponistenId = selectedComponistenId;
        this.defaultGenreId = selectedGenreId;
        this.defaultTypeId = selectedTypeId;

        // Setup componisten table with connection
        componistenPersoonTableModel = new ComponistenPersoonTableModel( connection );

        // Setup tekstschrijver table with connection
        tekstschrijverPersoonTableModel = new TekstschrijverPersoonTableModel( connection );

        setupOpusDialog( "Insert opus", "Insert", insertOpusActionCommand );
    }


    // Constructor for inserting a record in opus
    public EditOpusDialog( Connection connection,
                           Object     parentObject,
                           String     defaultOpusTitelString,
                           String     defaultOpusNummerString,
                           int	      selectedComponistenPersoonId,
                           int        selectedComponistenId,
                           int        selectedGenreId,
                           int        selectedTijdperkId,
                           int        selectedTypeId,
                           int        selectedSubtypeId ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.defaultOpusTitelString = defaultOpusTitelString;
        this.defaultOpusNummerString = defaultOpusNummerString;
        this.defaultComponistenPersoonId = selectedComponistenPersoonId;
        this.defaultComponistenId = selectedComponistenId;
        this.defaultGenreId = selectedGenreId;
        this.defaultTijdperkId = selectedTijdperkId;
        this.defaultTypeId = selectedTypeId;
        this.defaultSubtypeId = selectedSubtypeId;

        // Setup componisten table with connection
        componistenPersoonTableModel = new ComponistenPersoonTableModel( connection );

        // Setup tekstschrijver table with connection
        tekstschrijverPersoonTableModel = new TekstschrijverPersoonTableModel( connection );

        setupOpusDialog( "Insert opus", "Insert", insertOpusActionCommand );
    }


    // Constructor for updating an existing record in opus
    public EditOpusDialog( Connection connection,
                           Object     parentObject,
                           int        opusId ) {
        this.connection = connection;
        this.parentObject = parentObject;
        this.opusId = opusId;

        // Setup componisten table with connection
        componistenPersoonTableModel = new ComponistenPersoonTableModel( connection );

        // Setup tekstschrijver table with connection
        tekstschrijverPersoonTableModel = new TekstschrijverPersoonTableModel( connection );

        try {
            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT opus_titel, opus_nummer, " +
                                                          "opus.componisten_id, componisten.componisten, " +
                                                          "genre_id, tijdperk_id, type_id, subtype_id " +
                                                          "FROM opus " +
                                                          "LEFT JOIN componisten ON opus.componisten_id = componisten.componisten_id " +
                                                          "WHERE opus_id = " + opusId );
            if ( ! resultSet.next( ) ) {
                logger.severe( "Could not get record for opus_id " + opusId + " in opus" );
                return;
            }

            defaultOpusTitelString = resultSet.getString( 1 );
            defaultOpusNummerString = resultSet.getString( 2 );
            defaultComponistenId = resultSet.getInt( 3 );
            defaultComponistenString = resultSet.getString( 4 );
            defaultGenreId = resultSet.getInt( 5 );
            defaultTijdperkId = resultSet.getInt( 6 );
            defaultTypeId = resultSet.getInt( 7 );
            defaultSubtypeId = resultSet.getInt( 8 );
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
        }

        setupOpusDialog( "Edit opus", "Update", updateOpusActionCommand );
    }


    // Setup opus dialog
    private void setupOpusDialog( String dialogTitle,
                                  String editOpusButtonText,
                                  String editOpusButtonActionCommand ) {
        // Create modal dialog for editing opus record
        if ( parentObject instanceof JFrame ) {
            dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
        } else if ( parentObject instanceof JDialog ) {
            dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
        } else {
            logger.severe( "Unexpected parent object class: " +
                           parentObject.getClass( ).getName( ) );
            return;
        }

        // Setup the componisten table model with defaults
        componistenPersoonTableModel.showTable( defaultComponistenId );

        // Setup the tekstschrijver table model with defaults
        tekstschrijverPersoonTableModel.showTable( defaultComponistenId );

        // Set grid bag layout manager
        Container container = dialog.getContentPane( );
        container.setLayout( new GridBagLayout( ) );

        GridBagConstraints constraints = new GridBagConstraints( );
        constraints.anchor = GridBagConstraints.WEST;
        constraints.insets = new Insets( 5, 5, 5, 5 );

        constraints.gridx = 0;
        constraints.gridy = 0;
        container.add( new JLabel( "Opus titel:" ), constraints );

        opusTitelTextField = new JTextField( defaultOpusTitelString, 60 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 5;
        container.add( opusTitelTextField, constraints );

        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        container.add( new JLabel( "OpusNummer:" ), constraints );

        opusNummerTextField = new JTextField( defaultOpusNummerString, 20 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        container.add( opusNummerTextField, constraints );


        // Setup a JComboBox with the results of the query on componisten
        componistenComboBox = new ComponistenComboBox( connection, dialog,
                                                       defaultComponistenPersoonId,
                                                       defaultComponistenId );
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Componisten:" ), constraints );

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 3;
        container.add( componistenComboBox, constraints );

        class SelectComponistenActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                int selectedComponistenId = 0;

                // Check if a componisten record needs to be inserted
                if ( componistenComboBox.newComponistenSelected( ) ) {
                    // Insert new componisten record
                    EditComponistenDialog editComponistenDialog =
                        new EditComponistenDialog( connection, parentObject, componistenFilterString );

                    // Check if a new componisten record has been inserted
                    if ( editComponistenDialog.componistenUpdated( ) ) {
                        // Get the id of the new componisten record
                        selectedComponistenId = editComponistenDialog.getComponistenId( );

                        // Setup the componisten combo box again
                        componistenComboBox.setupComponistenComboBox( selectedComponistenId );
                    }
                } else {
                    // Get the selected componisten ID from the combo box
                    selectedComponistenId = componistenComboBox.getSelectedComponistenId( );
                }

                // Show the selected componisten and tekstschrijvers
                componistenPersoonTableModel.showTable( selectedComponistenId );
                tekstschrijverPersoonTableModel.showTable( selectedComponistenId );
            }
        }
        componistenComboBox.addActionListener( new SelectComponistenActionListener( ) );

        JButton filterComponistenButton = new JButton( "Filter" );
        filterComponistenButton.setActionCommand( "filterComponisten" );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        container.add( filterComponistenButton, constraints );

        class FilterComponistenActionListener implements ActionListener {
            public void actionPerformed( ActionEvent ae ) {
                componistenFilterString = componistenComboBox.filterComponistenComboBox( );
            }
        }
        filterComponistenButton.addActionListener( new FilterComponistenActionListener( ) );

        JButton editComponistenButton = new JButton( "Edit" );
        editComponistenButton.setActionCommand( "editComponisten" );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        container.add( editComponistenButton, constraints );

        class EditComponistenActionListener implements ActionListener {
            public void actionPerformed( ActionEvent ae ) {
                // Get the selected Componisten ID
                int selectedComponistenId = componistenComboBox.getSelectedComponistenId( );

                // Check if componisten has been selected
                if ( selectedComponistenId == 0 ) {
                    JOptionPane.showMessageDialog( dialog,
                                                   "Geen componisten geselecteerd",
                                                   "Edit Opus error",
                                                   JOptionPane.ERROR_MESSAGE );
                    return;
                }

                // Do dialog
                EditComponistenDialog editComponistenDialog =
                    new EditComponistenDialog( connection, dialog, selectedComponistenId );

                if ( editComponistenDialog.componistenUpdated( ) ) {
                    // Show the selected componisten and tekstschrijvers
                    componistenPersoonTableModel.showTable( selectedComponistenId );
                    tekstschrijverPersoonTableModel.showTable( selectedComponistenId );

                    // Setup the componisten combo box again
                    componistenComboBox.setupComponistenComboBox( );
                }
            }
        }
        editComponistenButton.addActionListener( new EditComponistenActionListener( ) );

        constraints.gridx = 0;
        constraints.gridy = 4;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Componisten tabel:" ), constraints );

        // Setup a table with componisten
        componistenPersoonTable = new JTable( componistenPersoonTableModel );
        componistenPersoonTable.setRowSelectionAllowed( false );
        componistenPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        componistenPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 500 );
        // Set vertical size just enough for 3 entries
        componistenPersoonTable.setPreferredScrollableViewportSize( new Dimension( 500, 48 ) );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 3;
        container.add( new JScrollPane( componistenPersoonTable ), constraints );

        constraints.gridx = 0;
        constraints.gridy = 5;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Tekstschrijvers tabel:" ), constraints );

        // Setup a table with tekstschrijvers
        tekstschrijverPersoonTable = new JTable( tekstschrijverPersoonTableModel );
        tekstschrijverPersoonTable.setRowSelectionAllowed( false );
        tekstschrijverPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        tekstschrijverPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 500 );
        // Set vertical size just enough for 4 entries
        tekstschrijverPersoonTable.setPreferredScrollableViewportSize( new Dimension( 500, 64 ) );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 3;
        container.add( new JScrollPane( tekstschrijverPersoonTable ), constraints );


        // Setup a JComboBox for genre
        genreComboBox = new GenreComboBox( connection, defaultGenreId );
        constraints.gridx = 0;
        constraints.gridy = 6;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Genre:" ), constraints );

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 3;
        container.add( genreComboBox, constraints );


        // Setup a JComboBox for tijdperk
        tijdperkComboBox = new TijdperkComboBox( connection, defaultTijdperkId );
        constraints.gridx = 0;
        constraints.gridy = 7;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Tijdperk:" ), constraints );

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 3;
        container.add( tijdperkComboBox, constraints );


        // Setup a JComboBox for type
        typeComboBox = new TypeComboBox( connection, defaultTypeId );
        constraints.gridx = 0;
        constraints.gridy = 8;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Type:" ), constraints );

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 3;
        container.add( typeComboBox, constraints );


        // Setup a JComboBox for subtype
        subtypeComboBox = new SubtypeComboBox( connection, dialog, defaultSubtypeId );
        constraints.gridx = 0;
        constraints.gridy = 9;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Subtype:" ), constraints );

        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        container.add( subtypeComboBox, constraints );

        class SelectSubtypeActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Check if a subtype record needs to be inserted
                if ( subtypeComboBox.newSubtypeSelected( ) ) {
                    // Insert new subtype record
                    EditSubtypeDialog editSubtypeDialog =
                        new EditSubtypeDialog( connection, parentObject, subtypeFilterString );

                    // Check if a new subtype record has been inserted
                    if ( editSubtypeDialog.subtypeUpdated( ) ) {
                        // Get the id of the new subtype record
                        int selectedSubtypeId = editSubtypeDialog.getSubtypeId( );

                        // Setup the subtype combo box again
                        subtypeComboBox.setupSubtypeComboBox( selectedSubtypeId );
                    }
                }
            }
        }
        subtypeComboBox.addActionListener( new SelectSubtypeActionListener( ) );

        JButton filterSubtypeButton = new JButton( "Filter" );
        filterSubtypeButton.setActionCommand( "filterSubtype" );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        container.add( filterSubtypeButton, constraints );

        class FilterSubtypeActionListener implements ActionListener {
            public void actionPerformed( ActionEvent ae ) {
                subtypeFilterString = subtypeComboBox.filterSubtypeComboBox( );
            }
        }
        filterSubtypeButton.addActionListener( new FilterSubtypeActionListener( ) );

        JButton editSubtypeButton = new JButton( "Edit" );
        editSubtypeButton.setActionCommand( "editSubtype" );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 1;
        container.add( editSubtypeButton, constraints );

        class EditSubtypeActionListener implements ActionListener {
            public void actionPerformed( ActionEvent ae ) {
                // Get the selected Subtype ID
                int selectedSubtypeId = subtypeComboBox.getSelectedSubtypeId( );

                // Check if subtype has been selected
                if ( selectedSubtypeId == 0 ) {
                    JOptionPane.showMessageDialog( dialog,
                                                   "Geen Subtype geselecteerd",
                                                   "Edit Opus error",
                                                   JOptionPane.ERROR_MESSAGE );
                    return;
                }

                // Do dialog
                EditSubtypeDialog editSubtypeDialog = new EditSubtypeDialog( connection,
                                                                             dialog,
                                                                             selectedSubtypeId );

                if ( editSubtypeDialog.subtypeUpdated( ) ) {
                    // Setup the subtype combo box again
                    subtypeComboBox.setupSubtypeComboBox( );
                }
            }
        }
        editSubtypeButton.addActionListener( new EditSubtypeActionListener( ) );


        ////////////////////////////////////////////////
        // Opus-deel Table, Add, Insert, Remove buttons
        ////////////////////////////////////////////////

        constraints.gridx = 0;
        constraints.gridy = 10;
        constraints.gridwidth = 1;
        // Set gridheigth to allow for three buttons next to the table
        constraints.gridheight = 3;
        container.add( new JLabel( "Opus-deel tabel:" ), constraints );

        // Create opus_deel table model with connection and current opus_id
        opusDeelTableModel = new OpusDeelTableModel( connection, opusId );

        // Create opus_deel table from opus_deel table model
        opusDeelTable = new JTable( opusDeelTableModel );

        // Setup a table with opus_deel records for this opus
        opusDeelTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        opusDeelTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
        opusDeelTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 120 );
        opusDeelTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 400 );
        // Set vertical size just enough for 10 entries
        opusDeelTable.setPreferredScrollableViewportSize( new Dimension( 540, 160 ) );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 3;
        container.add( new JScrollPane( opusDeelTable ), constraints );

        // Define Add button next to table
        final JButton addOpusDeelToTableButton = new JButton( "Add" );
        addOpusDeelToTableButton.setActionCommand( "addOpusDeelToTable" );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        container.add( addOpusDeelToTableButton, constraints );

        // Class to handle Add button
        class AddOpusDeelToTableActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                // Add row to table
                opusDeelTableModel.addRow( );
            }
        }
        addOpusDeelToTableButton.addActionListener( new AddOpusDeelToTableActionListener( ) );

        // Define Insert button next to table
        final JButton insertOpusDeelInTableButton = new JButton( "Insert" );
        insertOpusDeelInTableButton.setActionCommand( "insertOpusDeelInTable" );
        insertOpusDeelInTableButton.setEnabled( false );
        constraints.gridy = 11;
        container.add( insertOpusDeelInTableButton, constraints );

        // Define Remove button next to table
        final JButton removeOpusDeelFromTableButton = new JButton( "Remove" );
        removeOpusDeelFromTableButton.setActionCommand( "removeOpusDeelFromTable" );
        removeOpusDeelFromTableButton.setEnabled( false );
        constraints.gridy = 12;
        constraints.gridwidth = 1;
        container.add( removeOpusDeelFromTableButton, constraints );


        // Get the selection model related to the opus_deel table
        final ListSelectionModel opusDeelListSelectionModel = opusDeelTable.getSelectionModel( );

        class OpusDeelListSelectionListener implements ListSelectionListener {
            int selectedRow = -1;

            public void valueChanged( ListSelectionEvent listSelectionEvent ) {
                // Ignore extra messages.
                if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

                // Ignore if nothing is selected
                if ( opusDeelListSelectionModel.isSelectionEmpty( ) ) {
                    selectedRow = -1;
                    insertOpusDeelInTableButton.setEnabled( false );
                    removeOpusDeelFromTableButton.setEnabled( false );
                    return;
                }

                selectedRow = opusDeelListSelectionModel.getMinSelectionIndex( );
                insertOpusDeelInTableButton.setEnabled( true );
                removeOpusDeelFromTableButton.setEnabled( true );
            }

            public int getSelectedRow ( ) { return selectedRow; }
        }

        // Add opusDeelListSelectionListener object to the selection model of the opus_deel table
        final OpusDeelListSelectionListener opusDeelListSelectionListener = new OpusDeelListSelectionListener( );
        opusDeelListSelectionModel.addListSelectionListener( opusDeelListSelectionListener );


        // Class to handle Insert button: uses opusDeelListSelectionListener
        class InsertOpusDeelInTableActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                int selectedRow = opusDeelListSelectionListener.getSelectedRow( );
                if ( selectedRow < 0 ) {
                    JOptionPane.showMessageDialog( dialog,
                                                   "Geen opus deel geselecteerd",
                                                   "Edit opus error",
                                                   JOptionPane.ERROR_MESSAGE );
                    return;
                }

                // Insert selected row in table
                opusDeelTableModel.insertRow( selectedRow );
            }
        }
        insertOpusDeelInTableButton.addActionListener( new InsertOpusDeelInTableActionListener( ) );


        // Class to handle Remove button: uses opusDeelListSelectionListener
        class RemoveOpusDeelFromTableActionListener implements ActionListener {
            public void actionPerformed( ActionEvent ae ) {
                int selectedRow = opusDeelListSelectionListener.getSelectedRow( );
                if ( selectedRow < 0 ) {
                    JOptionPane.showMessageDialog( dialog,
                                                   "Geen opus deel geselecteerd",
                                                   "Edit opus error",
                                                   JOptionPane.ERROR_MESSAGE );
                    return;
                }

                // Remove selected row in table
                opusDeelTableModel.removeRow( selectedRow );
            }
        }
        removeOpusDeelFromTableButton.addActionListener( new RemoveOpusDeelFromTableActionListener( ) );


        ////////////////////////////////////////////////
        // Update/Insert, Cancel Buttons
        ////////////////////////////////////////////////

        JPanel buttonPanel = new JPanel( );

        class EditOpusActionListener implements ActionListener {
            public void actionPerformed( ActionEvent actionEvent ) {
                boolean result = true;

                if ( actionEvent.getActionCommand( ).equals( insertOpusActionCommand ) ) {
                    result = insertOpus( );
                } else if ( actionEvent.getActionCommand( ).equals( updateOpusActionCommand ) ) {
                    result = updateOpus( );
                }

                // Any other actionCommand, including cancel, has no action
                if ( result ) {
                    dialog.setVisible( false );
                }
            }
        }

        JButton editOpusButton = new JButton( editOpusButtonText );
        editOpusButton.setActionCommand( editOpusButtonActionCommand );
        editOpusButton.addActionListener( new EditOpusActionListener( ) );
        buttonPanel.add( editOpusButton );

        JButton cancelOpusButton = new JButton( "Cancel" );
        cancelOpusButton.setActionCommand( "cancelOpus" );
        cancelOpusButton.addActionListener( new EditOpusActionListener( ) );
        buttonPanel.add( cancelOpusButton );

        constraints.gridx = 1;
        constraints.gridy = 13;
        constraints.gridwidth = 2;
        container.add( buttonPanel, constraints );


        dialog.setSize( 1000, 710 );
        dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
        dialog.setVisible( true );
    }


    private boolean insertOpus( ) {
        String opusTitelString = opusTitelTextField.getText( );
        if ( opusTitelString == null || opusTitelString.length( ) == 0 ) {
            JOptionPane.showMessageDialog( dialog,
                                           "Opus titel niet ingevuld",
                                           "Insert Opus error",
                                           JOptionPane.ERROR_MESSAGE );
            return false;
        }

        // Matcher to find single quotes in opusTitelString, in order to replace these
        // with escaped quotes (the quadruple slashes are really necessary)
        final Matcher quoteMatcher = quotePattern.matcher( opusTitelString );
        String insertString =
            "INSERT INTO opus SET opus_titel = '" +
            quoteMatcher.replaceAll( "\\\\'" ) + "'";

        String opusNummerString = opusNummerTextField.getText( );
        if ( opusNummerString != null ) {
            if ( opusNummerString.length( ) > 0 ) {
                insertString += ", opus_nummer = '" + opusNummerString + "'";
            }
        }

        int selectedComponistenId = componistenComboBox.getSelectedComponistenId( );
        if ( selectedComponistenId != 0 ) insertString += ", componisten_id = " + selectedComponistenId;

        int genreId = genreComboBox.getSelectedGenreId( );
        if ( genreId != 0 ) insertString += ", genre_id = " + genreId;

        int tijdperkId = tijdperkComboBox.getSelectedTijdperkId( );
        if ( tijdperkId != 0 ) insertString += ", tijdperk_id = " + tijdperkId;

        int typeId = typeComboBox.getSelectedTypeId( );
        if ( typeId == 0 ) {
                insertString += ", type_id = null";
        } else {
                insertString += ", type_id = " + typeId;
        }

        int selectedSubtypeId = subtypeComboBox.getSelectedSubtypeId( );
        if ( selectedSubtypeId != 0 ) insertString += ", subtype_id = " + selectedSubtypeId;

        try {
            Statement statement = connection.createStatement( );
            ResultSet resultSet = statement.executeQuery( "SELECT MAX( opus_id ) FROM opus" );
            if ( ! resultSet.next( ) ) {
                logger.severe( "Could not get maximum for opus_id in opus" );
                return false;
            }
            opusId = resultSet.getInt( 1 ) + 1;
            insertString += ", opus_id = " + opusId;

            logger.fine( "insertString: " + insertString );

            nUpdate = statement.executeUpdate( insertString );
            if ( nUpdate != 1 ) {
                logger.severe( "Could not insert in opus" );
                return false;
            }

            // Use new opus ID for inserting in opus-deel table
            opusDeelTableModel.insertTable( opusId );
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
            return false;
        }

        return true;
    }

    String updateString = null;

    private void addToUpdateString( String additionalUpdateString ) {
        if ( updateString == null ) {
            updateString = additionalUpdateString;
        } else {
            updateString += ", " + additionalUpdateString;
        }
    }

    private boolean updateOpus( ) {
        // Initialise string holding the update query
        updateString = null;

        String opusTitelString = opusTitelTextField.getText( );
        if ( opusTitelString != null )
        {
            // Check if opus titel changed
            if ( !opusTitelString.equals( defaultOpusTitelString ) ) {
                if ( opusTitelString.length( ) == 0 ) {
                    JOptionPane.showMessageDialog( dialog,
                            "Opus titel niet ingevuld",
                            "Insert Opus error",
                            JOptionPane.ERROR_MESSAGE );
                    return false;
                }
            }

            // Matcher to find single quotes in opusTitelString, in order to replace these
            // with escaped quotes (the quadruple slashes are really necessary)
            final Matcher quoteMatcher = quotePattern.matcher( opusTitelString );
            addToUpdateString( "opus_titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
        }

        String opusNummerString = opusNummerTextField.getText( );
        // Check if opusNummer changed (allow for empty string)
        // (do not update when default was NULL and text field is empty)
        if ( ( ( defaultOpusNummerString != null ) || ( opusNummerString.length( ) > 0 ) ) &&
             ( !opusNummerString.equals( defaultOpusNummerString ) ) ) {
            if ( opusNummerString.length( ) == 0 ) {
                addToUpdateString( "opus_nummer = NULL" );
            } else {
                addToUpdateString( "opus_nummer = '" + opusNummerString + "'" );
            }
        }

        int selectedComponistenId = componistenComboBox.getSelectedComponistenId( );
        // Check if componistenId changed to a non-zero value
        if ( ( selectedComponistenId != defaultComponistenId ) && ( selectedComponistenId != 0 ) ) {
            addToUpdateString( "componisten_id = " + selectedComponistenId );
        }

        int selectedGenreId = genreComboBox.getSelectedGenreId( );
        // Check if genreId changed to a non-zero value
        if ( ( selectedGenreId != defaultGenreId ) && ( selectedGenreId != 0 ) ) {
            addToUpdateString( "genre_id = " + selectedGenreId );
        }

        int selectedTijdperkId = tijdperkComboBox.getSelectedTijdperkId( );
        // Check if tijdperkId changed to a non-zero value
        if ( ( selectedTijdperkId != defaultTijdperkId ) && ( selectedTijdperkId != 0 ) ) {
            addToUpdateString( "tijdperk_id = " + selectedTijdperkId );
        }

        int selectedTypeId = typeComboBox.getSelectedTypeId( );
        // Check if typeId changed to a non-zero value
        if ( selectedTypeId != defaultTypeId ) {
            if ( selectedTypeId != 0 ) {
                addToUpdateString( "type_id = " + selectedTypeId );
            } else {
                addToUpdateString( "type_id = null" );
            }
        }

        int selectedSubtypeId = subtypeComboBox.getSelectedSubtypeId( );
        // Check if subtypeId changed to a non-zero value
        if ( selectedSubtypeId != defaultSubtypeId ) {
            if ( selectedSubtypeId != 0 ) {
                addToUpdateString( "subtype_id = " + selectedSubtypeId );
            } else {
                addToUpdateString( "subtype_id = null" );
            }
        }

        opusDeelTableModel.updateTable( );

        // Check if any update is necessary at all
        if ( null == updateString ) {
            logger.info( "No update necessary" );
            return true;
        }

        updateString  = "UPDATE opus SET " + updateString;
        updateString += " WHERE opus_id = " + opusId;

        logger.fine( "updateString: " + updateString );

        try {
            Statement statement = connection.createStatement( );
            nUpdate = statement.executeUpdate( updateString );
            if ( nUpdate != 1 ) {
                logger.severe( "Could not update in opus" );
                return false;
            }
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
            return false;
        }

        return true;
    }

    public boolean opusUpdated( ) { return nUpdate > 0; }

    public String getOpusTitelString( ) { return opusTitelTextField.getText( ); }

    public int getOpusId( ) { return opusId; }
}
