// Project:	muziek
// Package:	muziek.opname
// File:	EditOpname.java
// Description:	Frame to show all or selected records in the opname table in schema muziek
// Author:	Chris van Engelen
// History:	2006/02/19: Initial version
//		2009/01/01: Add selection on Componist-Persoon
//              2011/08/13: Add selection on medium status, default selection available
//              2016/04/27: Refactoring, and use of Java 7, 8 features
//              2017/12/26: Application with internal frames

package muziek.opname;

import java.sql.Connection;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.util.logging.*;

import muziek.gui.*;
import table.*;

/**
 * Frame to show, insert and update records in the opname table in schema muziek.
 * @author Chris van Engelen
 */
public class EditOpname extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditOpname.class.getCanonicalName() );

    static private final Border emptyBorder = new EmptyBorder( -5, -5, -5, -5 );

    private MediumComboBox mediumComboBox;
    private int selectedMediumId = 0;

    // Select available mediums: MediumStatus 2
    private MediumStatusComboBox mediumStatusComboBox;
    private int selectedMediumStatusId = 2;

    private JTextField opusFilterTextField;

    private ComponistenPersoonComboBox componistenPersoonComboBox;
    private int selectedComponistenPersoonId = 0;
    private int selectedComponistenId = 0;

    private GenreComboBox genreComboBox;
    private int selectedGenreId = 0;

    private TypeComboBox typeComboBox;
    private int selectedTypeId = 0;

    private MusiciPersoonComboBox musiciPersoonComboBox;
    private int selectedPersoonAllMusiciId = 0;
    private int selectedMusiciId = 0;

    private MusiciEnsembleComboBox musiciEnsembleComboBox;
    private int selectedMusiciEnsembleId = 0;

    private OpnamePlaatsComboBox opnamePlaatsComboBox;
    private int selectedOpnamePlaatsId = 0;

    private OpnameDatumComboBox opnameDatumComboBox;
    private int selectedOpnameDatumId = 0;

    private ProducersComboBox producersComboBox;
    private int selectedProducersId = 0;

    private OpnameTableModel opnameTableModel;
    private TableSorter opnameTableSorter;

    public EditOpname( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit opname", true, true, true, true);

	// put the controls the content pane
	Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
        constraints.gridwidth = 1;

	// Text filter action listener
	ActionListener textFilterActionListener = ( ActionEvent actionEvent ) -> {
            // Setup the opname table
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                                                    selectedMediumStatusId,
                                                    opusFilterTextField.getText( ),
                                                    selectedComponistenPersoonId,
                                                    selectedComponistenId,
                                                    selectedGenreId,
                                                    selectedTypeId,
                                                    selectedPersoonAllMusiciId,
                                                    selectedMusiciId,
                                                    selectedMusiciEnsembleId,
                                                    selectedOpnameDatumId,
                                                    selectedOpnamePlaatsId,
                                                    selectedProducersId );
        };


	/////////////////////////////////
	// Medium Combo Box
	/////////////////////////////////

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Medium:" ), constraints );

	final JPanel mediumPanel = new JPanel( );
	mediumPanel.setBorder( emptyBorder );

	// Setup a JComboBox for medium with the selected medium ID
	mediumComboBox = new MediumComboBox( connection, parentFrame, false );
	mediumComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected medium ID from the combo box
            selectedMediumId = mediumComboBox.getSelectedMediumId( );

            // Setup the opname table for the selected medium
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                                                    selectedMediumStatusId,
                                                    opusFilterTextField.getText( ),
                                                    selectedComponistenPersoonId,
                                                    selectedComponistenId,
                                                    selectedGenreId,
                                                    selectedTypeId,
                                                    selectedPersoonAllMusiciId,
                                                    selectedMusiciId,
                                                    selectedMusiciEnsembleId,
                                                    selectedOpnameDatumId,
                                                    selectedOpnamePlaatsId,
                                                    selectedProducersId );
        } );
        mediumPanel.add( mediumComboBox );

	JButton filterMediumButton = new JButton( "Filter" );
	filterMediumButton.setActionCommand( "filterMedium" );
        filterMediumButton.addActionListener( ( ActionEvent actionEvent ) -> mediumComboBox.filterMediumComboBox( ) );
        mediumPanel.add( filterMediumButton );

        constraints.insets = new Insets( 20, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( mediumPanel, constraints );


	/////////////////////////////////
	// Medium Status Combo Box
	/////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Medium status:" ), constraints );

	final JPanel mediumStatusPanel = new JPanel( );
	mediumStatusPanel.setBorder( emptyBorder );

	// Setup a JComboBox for medium status with the selected medium ID
	mediumStatusComboBox = new MediumStatusComboBox( connection, selectedMediumStatusId );
	mediumStatusComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected medium status ID from the combo box
            selectedMediumStatusId = mediumStatusComboBox.getSelectedMediumStatusId( );

            // Setup the opname table for the selected medium
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                    selectedMediumStatusId,
                    opusFilterTextField.getText( ),
                    selectedComponistenPersoonId,
                    selectedComponistenId,
                    selectedGenreId,
                    selectedTypeId,
                    selectedPersoonAllMusiciId,
                    selectedMusiciId,
                    selectedMusiciEnsembleId,
                    selectedOpnameDatumId,
                    selectedOpnamePlaatsId,
                    selectedProducersId );
        } );
        mediumStatusPanel.add( mediumStatusComboBox );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( mediumStatusPanel, constraints );


	/////////////////////////////////
	// Opus filter string
	/////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opus Filter:" ), constraints );

	opusFilterTextField = new JTextField( 40 );
	opusFilterTextField.addActionListener( textFilterActionListener );

        constraints.insets = new Insets( 5, 5, 5, 600 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( opusFilterTextField, constraints );

        constraints.weightx = 0d;
        constraints.fill = GridBagConstraints.NONE;


	/////////////////////////////////
	// Componisten Combo Box
	/////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Componisten:" ), constraints );

	final JPanel componistenPanel = new JPanel( );
	componistenPanel.setBorder( emptyBorder );

	// Setup a JComboBox with the results of the query on componisten
	componistenPersoonComboBox = new ComponistenPersoonComboBox( connection, parentFrame, false );
	componistenPersoonComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected componisten-persoon ID and componisten ID from the combo box
            selectedComponistenPersoonId = componistenPersoonComboBox.getSelectedComponistenPersoonId( );
            selectedComponistenId = componistenPersoonComboBox.getSelectedComponistenId( );

            // Setup the opname table for the selected componisten
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                                                    selectedMediumStatusId,
                                                    opusFilterTextField.getText( ),
                                                    selectedComponistenPersoonId,
                                                    selectedComponistenId,
                                                    selectedGenreId,
                                                    selectedTypeId,
                                                    selectedPersoonAllMusiciId,
                                                    selectedMusiciId,
                                                    selectedMusiciEnsembleId,
                                                    selectedOpnameDatumId,
                                                    selectedOpnamePlaatsId,
                                                    selectedProducersId );
        } );
        componistenPanel.add( componistenPersoonComboBox );

	JButton filterComponistenButton = new JButton( "Filter" );
	filterComponistenButton.setActionCommand( "filterComponisten" );
	filterComponistenButton.addActionListener( ( ActionEvent actionEvent ) -> componistenPersoonComboBox.filterComponistenPersoonComboBox( ) );
        componistenPanel.add( filterComponistenButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( componistenPanel, constraints );


	/////////////////////////////////
	// GenreCombo Box
	/////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Genre:" ), constraints );

	// Setup a JComboBox for genre
	genreComboBox = new GenreComboBox( connection, selectedGenreId );
	genreComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected genre ID from the combo box
            selectedGenreId = genreComboBox.getSelectedGenreId( );

            // Setup the opname table for the selected genre
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                                                    selectedMediumStatusId,
                                                    opusFilterTextField.getText( ),
                                                    selectedComponistenPersoonId,
                                                    selectedComponistenId,
                                                    selectedGenreId,
                                                    selectedTypeId,
                                                    selectedPersoonAllMusiciId,
                                                    selectedMusiciId,
                                                    selectedMusiciEnsembleId,
                                                    selectedOpnameDatumId,
                                                    selectedOpnamePlaatsId,
                                                    selectedProducersId );
        } );

        constraints.insets = new Insets( 5, 5, 5, 20 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( genreComboBox, constraints );


	/////////////////////////////////
	// Type Combo Box
	/////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Type:" ), constraints );

	// Setup a JComboBox for type
	typeComboBox = new TypeComboBox( connection, selectedTypeId );
	typeComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected type ID from the combo box
            selectedTypeId = typeComboBox.getSelectedTypeId( );

            // Setup the opname table for the selected type
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                                                    selectedMediumStatusId,
                                                    opusFilterTextField.getText( ),
                                                    selectedComponistenPersoonId,
                                                    selectedComponistenId,
                                                    selectedGenreId,
                                                    selectedTypeId,
                                                    selectedPersoonAllMusiciId,
                                                    selectedMusiciId,
                                                    selectedMusiciEnsembleId,
                                                    selectedOpnameDatumId,
                                                    selectedOpnamePlaatsId,
                                                    selectedProducersId );
        } );

        constraints.insets = new Insets( 5, 5, 5, 20 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( typeComboBox, constraints );


        ////////////////////////////////////////////////
	// Musici Combo Box
	////////////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Musici:" ), constraints );

	final JPanel musiciPanel = new JPanel( );
	musiciPanel.setBorder( emptyBorder );

	// Setup a JComboBox for musici-persoon
	musiciPersoonComboBox = new MusiciPersoonComboBox( connection, parentFrame );
	musiciPersoonComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected persoon or musici ID from the combo box
            selectedPersoonAllMusiciId = musiciPersoonComboBox.getSelectedPersoonAllMusiciId( );
            selectedMusiciId  = musiciPersoonComboBox.getSelectedMusiciId( );

            // Setup the opname table for the selected musici
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                                                    selectedMediumStatusId,
                                                    opusFilterTextField.getText( ),
                                                    selectedComponistenPersoonId,
                                                    selectedComponistenId,
                                                    selectedGenreId,
                                                    selectedTypeId,
                                                    selectedPersoonAllMusiciId,
                                                    selectedMusiciId,
                                                    selectedMusiciEnsembleId,
                                                    selectedOpnameDatumId,
                                                    selectedOpnamePlaatsId,
                                                    selectedProducersId );
        } );
        musiciPanel.add( musiciPersoonComboBox );

	JButton filterMusiciPersoonButton = new JButton( "Filter" );
	filterMusiciPersoonButton.setActionCommand( "filterMusiciPersoon" );
	filterMusiciPersoonButton.addActionListener( ( ActionEvent actionEvent ) -> musiciPersoonComboBox.filterMusiciPersoonComboBox( ) );
        musiciPanel.add( filterMusiciPersoonButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( musiciPanel, constraints );


	////////////////////////////////////////////////
	// Ensemble Combo Box
	////////////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 7;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Ensemble:" ), constraints );

	final JPanel ensemblePanel = new JPanel( );
	ensemblePanel.setBorder( emptyBorder );

	// Setup a JComboBox for musici-ensemble
	musiciEnsembleComboBox = new MusiciEnsembleComboBox( connection, parentFrame );
	musiciEnsembleComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected musici-ensemble ID from the combo box
            selectedMusiciEnsembleId = musiciEnsembleComboBox.getSelectedMusiciEnsembleId( );

            // Setup the opname table for the selected musici-ensemble
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                                                    selectedMediumStatusId,
                                                    opusFilterTextField.getText( ),
                                                    selectedComponistenPersoonId,
                                                    selectedComponistenId,
                                                    selectedGenreId,
                                                    selectedTypeId,
                                                    selectedPersoonAllMusiciId,
                                                    selectedMusiciId,
                                                    selectedMusiciEnsembleId,
                                                    selectedOpnameDatumId,
                                                    selectedOpnamePlaatsId,
                                                    selectedProducersId );
        } );
        ensemblePanel.add( musiciEnsembleComboBox );

	JButton filterMusiciEnsembleButton = new JButton( "Filter" );
	filterMusiciEnsembleButton.setActionCommand( "filterMusiciEnsemble" );
	filterMusiciEnsembleButton.addActionListener( ( ActionEvent actionEvent ) -> musiciEnsembleComboBox.filterMusiciEnsembleComboBox( ) );
        ensemblePanel.add( filterMusiciEnsembleButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( ensemblePanel, constraints );


	//////////////////////////////////////////
	// OpnamePlaats Combo Box
	//////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opname plaats:" ), constraints );

	final JPanel opnamePlaatsPanel = new JPanel( );
	opnamePlaatsPanel.setBorder( emptyBorder );

	// Setup a JComboBox for opname_plaats
	opnamePlaatsComboBox = new OpnamePlaatsComboBox( connection, parentFrame, false );
	opnamePlaatsComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected opname plaats ID from the combo box
            selectedOpnamePlaatsId = opnamePlaatsComboBox.getSelectedOpnamePlaatsId( );

            // Setup the opname table for the selected opname plaats
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                    selectedMediumStatusId,
                    opusFilterTextField.getText( ),
                    selectedComponistenPersoonId,
                    selectedComponistenId,
                    selectedGenreId,
                    selectedTypeId,
                    selectedPersoonAllMusiciId,
                    selectedMusiciId,
                    selectedMusiciEnsembleId,
                    selectedOpnameDatumId,
                    selectedOpnamePlaatsId,
                    selectedProducersId );
        } );
        opnamePlaatsPanel.add( opnamePlaatsComboBox );

	JButton filterOpnamePlaatsButton = new JButton( "Filter" );
	filterOpnamePlaatsButton.setActionCommand( "filterOpnamePlaats" );
	filterOpnamePlaatsButton.addActionListener( ( ActionEvent actionEvent ) -> opnamePlaatsComboBox.filterOpnamePlaatsComboBox( ) );
        opnamePlaatsPanel.add( filterOpnamePlaatsButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( opnamePlaatsPanel, constraints );


        //////////////////////////////////////////
        // OpnameDatum Combo Box
        //////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
        constraints.gridx = 0;
        constraints.gridy = 9;
        constraints.anchor = GridBagConstraints.EAST;
        container.add( new JLabel( "Opname datum:" ), constraints );

        final JPanel opnameDatumPanel = new JPanel( );
        opnameDatumPanel.setBorder( emptyBorder );

        // Setup a JComboBox for opname datum
        opnameDatumComboBox = new OpnameDatumComboBox( connection, parentFrame, false );
        opnameDatumComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected opname datum ID from the combo box
            selectedOpnameDatumId = opnameDatumComboBox.getSelectedOpnameDatumId( );

            // Setup the opname table for the selected opname datum
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                    selectedMediumStatusId,
                    opusFilterTextField.getText( ),
                    selectedComponistenPersoonId,
                    selectedComponistenId,
                    selectedGenreId,
                    selectedTypeId,
                    selectedPersoonAllMusiciId,
                    selectedMusiciId,
                    selectedMusiciEnsembleId,
                    selectedOpnameDatumId,
                    selectedOpnamePlaatsId,
                    selectedProducersId );
        } );
        opnameDatumPanel.add( opnameDatumComboBox );

        JButton filterOpnameDatumButton = new JButton( "Filter" );
        filterOpnameDatumButton.setActionCommand( "filterOpnameDatum" );
        filterOpnameDatumButton.addActionListener( ( ActionEvent actionEvent ) -> opnameDatumComboBox.filterOpnameDatumComboBox( ) );
        opnameDatumPanel.add( filterOpnameDatumButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        container.add( opnameDatumPanel, constraints );


        //////////////////////////////////////////
	// Producers Combo Box
	//////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 10;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Producers:" ), constraints );

	final JPanel producersPanel = new JPanel( );
	producersPanel.setBorder( emptyBorder );

	// Setup a JComboBox for producers
	producersComboBox = new ProducersComboBox( connection, parentFrame, selectedProducersId );
	producersComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected producers ID from the combo box
            selectedProducersId = producersComboBox.getSelectedProducersId( );

            // Setup the opname table for the selected producers
            opnameTableSorter.clearSortingState();
            opnameTableModel.setupOpnameTableModel( selectedMediumId,
                                                    selectedMediumStatusId,
                                                    opusFilterTextField.getText( ),
                                                    selectedComponistenPersoonId,
                                                    selectedComponistenId,
                                                    selectedGenreId,
                                                    selectedTypeId,
                                                    selectedPersoonAllMusiciId,
                                                    selectedMusiciId,
                                                    selectedMusiciEnsembleId,
                                                    selectedOpnameDatumId,
                                                    selectedOpnamePlaatsId,
                                                    selectedProducersId );
        } );
        producersPanel.add( producersComboBox );

	JButton filterProducersButton = new JButton( "Filter" );
	filterProducersButton.setActionCommand( "filterProducers" );
	filterProducersButton.addActionListener( ( ActionEvent actionEvent ) -> producersComboBox.filterProducersComboBox( ) );
        producersPanel.add( filterProducersButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( producersPanel, constraints );


	// Create opname table from opname table model
	opnameTableModel = new OpnameTableModel( connection, parentFrame );
	opnameTableSorter = new TableSorter( opnameTableModel );
	final JTable opnameTable = new JTable( opnameTableSorter );
	opnameTableSorter.setTableHeader( opnameTable.getTableHeader( ) );
	// opnameTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	opnameTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	opnameTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 200 );  // medium
	opnameTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 200 );  // opus
	opnameTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 150 );  // componisten
	opnameTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth(  80 );  // genre
	opnameTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth(  80 );  // type
	opnameTable.getColumnModel( ).getColumn( 5 ).setPreferredWidth( 150 );  // Musici
	opnameTable.getColumnModel( ).getColumn( 6 ).setPreferredWidth( 140 );  // Opname datum
	opnameTable.getColumnModel( ).getColumn( 7 ).setPreferredWidth( 150 );  // Opname plaats
	opnameTable.getColumnModel( ).getColumn( 8 ).setPreferredWidth( 150 );  // producers

	// Set vertical size just enough for 10 entries
	opnameTable.setPreferredScrollableViewportSize( new Dimension( 1300, 240 ) );


        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 11;
	constraints.gridwidth = 2;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 1d;
	constraints.weighty = 1d;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( new JScrollPane( opnameTable ), constraints );


	// Define the open opname dialog button because it is used by the list selection listener
	final JButton openOpnameDialogButton = new JButton( "Open Dialog" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel opnameListSelectionModel = opnameTable.getSelectionModel( );

	class OpnameListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( opnameListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    openOpnameDialogButton.setEnabled( false );
		    return;
		}

		int viewRow = opnameListSelectionModel.getMinSelectionIndex( );
		selectedRow = opnameTableSorter.modelIndex( viewRow );
		openOpnameDialogButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add opnameListSelectionListener object to the selection model of the opname table
	final OpnameListSelectionListener opnameListSelectionListener = new OpnameListSelectionListener( );
	opnameListSelectionModel.addListSelectionListener( opnameListSelectionListener );

	// Class to handle button actions: uses opnameListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
		    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new opname record
		    new EditOpnameDialog( connection, parentFrame,
                                          selectedMediumId,
                                          opusFilterTextField.getText( ),
                                          selectedComponistenPersoonId,
                                          selectedComponistenId,
                                          selectedGenreId,
                                          selectedTypeId,
                                          selectedPersoonAllMusiciId,
                                          selectedMusiciId,
                                          selectedMusiciEnsembleId,
                                          selectedOpnameDatumId,
                                          selectedOpnamePlaatsId,
                                          selectedProducersId );
		} else {
		    int selectedRow = opnameListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen opname geselecteerd",
						       "Opname frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "openDialog" ) ) {
			// Get the selected opname key
			OpnameKey selectedOpnameKey = opnameTableModel.getSelectedOpnameKey( selectedRow );

			// Check if opname has been selected
			if ( selectedOpnameKey == new OpnameKey( ) ) {
			    JOptionPane.showMessageDialog( parentFrame,
							   "Geen opname geselecteerd",
							   "Opname frame error",
							   JOptionPane.ERROR_MESSAGE );
			    return;
			}

			// Do edit opname dialog
			new EditOpnameDialog( connection, parentFrame, selectedOpnameKey );

		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			logger.severe( "Delete not yet implemented" );
		    }
		}

		// Records may have been modified: setup the table model again
                opnameTableSorter.clearSortingState();
		opnameTableModel.setupOpnameTableModel( selectedMediumId,
		                                        selectedMediumStatusId,
							opusFilterTextField.getText( ),
							selectedComponistenPersoonId,
							selectedComponistenId,
							selectedGenreId,
							selectedTypeId,
							selectedPersoonAllMusiciId,
							selectedMusiciId,
							selectedMusiciEnsembleId,
							selectedOpnameDatumId,
							selectedOpnamePlaatsId,
							selectedProducersId );

		// Setup combo boxes again
		componistenPersoonComboBox.setupComponistenPersoonComboBox( selectedComponistenPersoonId,
									    selectedComponistenId );
		musiciPersoonComboBox.setupMusiciPersoonComboBox( selectedPersoonAllMusiciId,
								  selectedMusiciId );
		musiciEnsembleComboBox.setupMusiciEnsembleComboBox( selectedMusiciEnsembleId );
		opnameDatumComboBox.setupOpnameDatumComboBox( selectedOpnameDatumId );
		opnamePlaatsComboBox.setupOpnamePlaatsComboBox( selectedOpnamePlaatsId );
		producersComboBox.setupProducersComboBox( selectedProducersId );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertOpnameButton = new JButton( "Insert" );
	insertOpnameButton.setActionCommand( "insert" );
	insertOpnameButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertOpnameButton );


	openOpnameDialogButton.setActionCommand( "openDialog" );
	openOpnameDialogButton.setEnabled( false );
	openOpnameDialogButton.addActionListener( buttonActionListener );
	buttonPanel.add( openOpnameDialogButton );


	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 12;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;
	container.add( buttonPanel, constraints );

	setSize( 1360, 800 );
        setLocation(x, y );
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}
