//
// Project:	muziek
// Component:	gui
// File:	OpnameFrame.java
// Description:	Frame to show all or selected records in opname table
// Author:	Chris van Engelen
// History:	2006/02/19: Initial version
//		2009/01/01: Add selection on Componist-Persoon
//              2011/08/13: Add selection on medium status, default selection available

package muziek.gui;

import java.sql.Connection; 

import java.awt.*; 
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.*;
import java.util.logging.*;

import table.*;


public class OpnameFrame {
    final Logger logger = Logger.getLogger( "muziek.gui.OpnameFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Opname");

    MediumComboBox mediumComboBox;
    int selectedMediumId = 0;

    // Select available CDs: MediumStatus 2
    MediumStatusComboBox mediumStatusComboBox;
    int selectedMediumStatusId = 2;

    JTextField opusFilterTextField;

    ComponistenPersoonComboBox componistenPersoonComboBox;
    int selectedComponistenPersoonId = 0;
    int selectedComponistenId = 0;

    GenreComboBox genreComboBox;
    int selectedGenreId = 0;

    TypeComboBox typeComboBox;
    int selectedTypeId = 0;

    MusiciPersoonComboBox musiciPersoonComboBox;
    int selectedPersoonAllMusiciId = 0;
    int selectedMusiciId = 0;

    MusiciEnsembleComboBox musiciEnsembleComboBox;
    int selectedMusiciEnsembleId = 0;

    OpnamePlaatsComboBox opnamePlaatsComboBox;
    int selectedOpnamePlaatsId = 0;

    OpnameDatumComboBox opnameDatumComboBox;
    int selectedOpnameDatumId = 0;

    ProducersComboBox producersComboBox;
    int selectedProducersId = 0;


    OpnameTableModel opnameTableModel;
    TableSorter opnameTableSorter;
    JTable opnameTable;

    public OpnameFrame( final Connection connection ) {
	this.connection = connection;

	final Border emptyBorder = new EmptyBorder( -5, -5, -5, -5 );

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 5, 10, 5, 10 );
	constraints.weighty = 0.0;


	/////////////////////////////////
	// Text filter action listener
	/////////////////////////////////

	class TextFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Setup the opname table
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
	    }
	}
	TextFilterActionListener textFilterActionListener = new TextFilterActionListener( );


	/////////////////////////////////
	// Medium Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Medium:" ), constraints );

	final JPanel mediumPanel = new JPanel( );
	mediumPanel.setBorder( emptyBorder );

	// Setup a JComboBox for medium with the selected medium ID
	mediumComboBox = new MediumComboBox( connection, frame, false );
	mediumPanel.add( mediumComboBox );

	class SelectMediumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected medium ID from the combo box
		selectedMediumId = mediumComboBox.getSelectedMediumId( );

		// Setup the opname table for the selected medium
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
	    }
	}
	mediumComboBox.addActionListener( new SelectMediumActionListener( ) );

	JButton filterMediumButton = new JButton( "Filter" );
	filterMediumButton.setActionCommand( "filterMedium" );
	mediumPanel.add( filterMediumButton );

	class FilterMediumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		mediumComboBox.filterMediumComboBox( );
	    }
	}
	filterMediumButton.addActionListener( new FilterMediumActionListener( ) );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( mediumPanel, constraints );


	/////////////////////////////////
	// Medium Status Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Medium status:" ), constraints );

	final JPanel mediumStatusPanel = new JPanel( );
	mediumStatusPanel.setBorder( emptyBorder );

	// Setup a JComboBox for medium status with the selected medium ID
	mediumStatusComboBox = new MediumStatusComboBox( connection, selectedMediumStatusId );
	mediumStatusPanel.add( mediumStatusComboBox );

	class SelectMediumStatusActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected medium status ID from the combo box
		selectedMediumStatusId = mediumStatusComboBox.getSelectedMediumStatusId( );

		// Setup the opname table for the selected medium
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
	    }
	}
	mediumStatusComboBox.addActionListener( new SelectMediumStatusActionListener( ) );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( mediumStatusPanel, constraints );


	/////////////////////////////////
	// Opus filter string
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opus Filter:" ), constraints );

	opusFilterTextField = new JTextField( 40 );
	opusFilterTextField.addActionListener( textFilterActionListener );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( opusFilterTextField, constraints );


	/////////////////////////////////
	// Componisten Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = 1;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Componisten:" ), constraints );

	final JPanel componistenPanel = new JPanel( );
	componistenPanel.setBorder( emptyBorder );

	// Setup a JComboBox with the results of the query on componisten
	componistenPersoonComboBox = new ComponistenPersoonComboBox( connection, frame, false );
	componistenPanel.add( componistenPersoonComboBox );

	class SelectComponistenActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected componisten-persoon ID and componisten ID from the combo box
		selectedComponistenPersoonId = componistenPersoonComboBox.getSelectedComponistenPersoonId( );
		selectedComponistenId = componistenPersoonComboBox.getSelectedComponistenId( );

		// Setup the opname table for the selected componisten
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
	    }
	}
	componistenPersoonComboBox.addActionListener( new SelectComponistenActionListener( ) );

	JButton filterComponistenButton = new JButton( "Filter" );
	filterComponistenButton.setActionCommand( "filterComponisten" );
	componistenPanel.add( filterComponistenButton );

	class FilterComponistenActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		componistenPersoonComboBox.filterComponistenPersoonComboBox( );
	    }
	}
	filterComponistenButton.addActionListener( new FilterComponistenActionListener( ) );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( componistenPanel, constraints );


	/////////////////////////////////
	// GenreCombo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Genre:" ), constraints );

	// Setup a JComboBox for genre
	genreComboBox = new GenreComboBox( connection, selectedGenreId );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( genreComboBox, constraints );

	class SelectGenreActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected genre ID from the combo box
		selectedGenreId = genreComboBox.getSelectedGenreId( );

		// Setup the opname table for the selected genre
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
	    }
	}
	genreComboBox.addActionListener( new SelectGenreActionListener( ) );


	/////////////////////////////////
	// Type Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Type:" ), constraints );

	// Setup a JComboBox for type
	typeComboBox = new TypeComboBox( connection, selectedTypeId );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( typeComboBox, constraints );

	class SelectTypeActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected type ID from the combo box
		selectedTypeId = typeComboBox.getSelectedTypeId( );

		// Setup the opname table for the selected type
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
	    }
	}
	typeComboBox.addActionListener( new SelectTypeActionListener( ) );


	////////////////////////////////////////////////
	// Musici Combo Box
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Musici:" ), constraints );

	final JPanel musiciPanel = new JPanel( );
	musiciPanel.setBorder( emptyBorder );

	// Setup a JComboBox for musici-persoon
	musiciPersoonComboBox = new MusiciPersoonComboBox( connection, frame );
	musiciPanel.add( musiciPersoonComboBox );

	class SelectMusiciPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected persoon or musici ID from the combo box
		selectedPersoonAllMusiciId = musiciPersoonComboBox.getSelectedPersoonAllMusiciId( );
		selectedMusiciId  = musiciPersoonComboBox.getSelectedMusiciId( );

		// Setup the opname table for the selected musici
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
	    }
	}
	musiciPersoonComboBox.addActionListener( new SelectMusiciPersoonActionListener( ) );

	JButton filterMusiciPersoonButton = new JButton( "Filter" );
	filterMusiciPersoonButton.setActionCommand( "filterMusiciPersoon" );
	musiciPanel.add( filterMusiciPersoonButton );

	class FilterMusiciPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		musiciPersoonComboBox.filterMusiciPersoonComboBox( );
	    }
	}
	filterMusiciPersoonButton.addActionListener( new FilterMusiciPersoonActionListener( ) );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( musiciPanel, constraints );


	////////////////////////////////////////////////
	// Ensemble Combo Box
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 7;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Ensemble:" ), constraints );

	final JPanel ensemblePanel = new JPanel( );
	ensemblePanel.setBorder( emptyBorder );

	// Setup a JComboBox for musici-ensemble
	musiciEnsembleComboBox = new MusiciEnsembleComboBox( connection, frame );
	ensemblePanel.add( musiciEnsembleComboBox );

	class SelectMusiciEnsembleActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected musici-ensemble ID from the combo box
		selectedMusiciEnsembleId = musiciEnsembleComboBox.getSelectedMusiciEnsembleId( );

		// Setup the opname table for the selected musici
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
	    }
	}
	musiciEnsembleComboBox.addActionListener( new SelectMusiciEnsembleActionListener( ) );

	JButton filterMusiciEnsembleButton = new JButton( "Filter" );
	filterMusiciEnsembleButton.setActionCommand( "filterMusiciEnsemble" );
	ensemblePanel.add( filterMusiciEnsembleButton );

	class FilterMusiciEnsembleActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		musiciEnsembleComboBox.filterMusiciEnsembleComboBox( );
	    }
	}
	filterMusiciEnsembleButton.addActionListener( new FilterMusiciEnsembleActionListener( ) );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( ensemblePanel, constraints );


	//////////////////////////////////////////
	// OpnameDatum Combo Box
	//////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opname datum:" ), constraints );

	final JPanel opnameDatumPanel = new JPanel( );
	opnameDatumPanel.setBorder( emptyBorder );

	// Setup a JComboBox for opname datum
	opnameDatumComboBox = new OpnameDatumComboBox( connection, frame, false );
	opnameDatumPanel.add( opnameDatumComboBox );

	class SelectOpnameDatumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected opname datum ID from the combo box
		selectedOpnameDatumId = opnameDatumComboBox.getSelectedOpnameDatumId( );

		// Setup the opname table for the selected opname datum
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
	    }
	}
	opnameDatumComboBox.addActionListener( new SelectOpnameDatumActionListener( ) );

	JButton filterOpnameDatumButton = new JButton( "Filter" );
	filterOpnameDatumButton.setActionCommand( "filterOpnameDatum" );
	opnameDatumPanel.add( filterOpnameDatumButton );

	class FilterOpnameDatumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		opnameDatumComboBox.filterOpnameDatumComboBox( );
	    }
	}
	filterOpnameDatumButton.addActionListener( new FilterOpnameDatumActionListener( ) );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( opnameDatumPanel, constraints );


	//////////////////////////////////////////
	// OpnamePlaats Combo Box
	//////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 9;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opname plaats:" ), constraints );

	final JPanel opnamePlaatsPanel = new JPanel( );
	opnamePlaatsPanel.setBorder( emptyBorder );

	// Setup a JComboBox for opname_plaats
	opnamePlaatsComboBox = new OpnamePlaatsComboBox( connection, frame, false );
	opnamePlaatsPanel.add( opnamePlaatsComboBox );

	class SelectOpnamePlaatsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected opname plaats ID from the combo box
		selectedOpnamePlaatsId = opnamePlaatsComboBox.getSelectedOpnamePlaatsId( );

		// Setup the opname table for the selected opname plaats
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
	    }
	}
	opnamePlaatsComboBox.addActionListener( new SelectOpnamePlaatsActionListener( ) );

	JButton filterOpnamePlaatsButton = new JButton( "Filter" );
	filterOpnamePlaatsButton.setActionCommand( "filterOpnamePlaats" );
	opnamePlaatsPanel.add( filterOpnamePlaatsButton );

	class FilterOpnamePlaatsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		opnamePlaatsComboBox.filterOpnamePlaatsComboBox( );
	    }
	}
	filterOpnamePlaatsButton.addActionListener( new FilterOpnamePlaatsActionListener( ) );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( opnamePlaatsPanel, constraints );


	//////////////////////////////////////////
	// Producers Combo Box
	//////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 10;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Producers:" ), constraints );

	final JPanel producersPanel = new JPanel( );
	producersPanel.setBorder( emptyBorder );

	// Setup a JComboBox for producers
	producersComboBox = new ProducersComboBox( connection, frame, selectedProducersId );
	producersPanel.add( producersComboBox );

	class SelectProducersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected producers ID from the combo box
		selectedProducersId = producersComboBox.getSelectedProducersId( );

		// Setup the opname table for the selected opname datum
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
	    }
	}
	producersComboBox.addActionListener( new SelectProducersActionListener( ) );

	JButton filterProducersButton = new JButton( "Filter" );
	filterProducersButton.setActionCommand( "filterProducers" );
	producersPanel.add( filterProducersButton );

	class FilterProducersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		producersComboBox.filterProducersComboBox( );
	    }
	}
	filterProducersButton.addActionListener( new FilterProducersActionListener( ) );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( producersPanel, constraints );


	// Create opname table from opname table model
	opnameTableModel = new OpnameTableModel( connection );
	opnameTableSorter = new TableSorter( opnameTableModel );
	opnameTable = new JTable( opnameTableSorter );
	opnameTableSorter.setTableHeader( opnameTable.getTableHeader( ) );
	// opnameTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	opnameTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	opnameTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 200 );  // medium
	opnameTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 200 );  // opus
	opnameTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 150 );  // componisten
	opnameTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth(  80 );  // genre
	opnameTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth(  80 );  // type
	opnameTable.getColumnModel( ).getColumn( 5 ).setPreferredWidth( 150 );  // Musici
	opnameTable.getColumnModel( ).getColumn( 6 ).setPreferredWidth( 150 );  // Opname datum
	opnameTable.getColumnModel( ).getColumn( 7 ).setPreferredWidth( 150 );  // Opname plaats
	opnameTable.getColumnModel( ).getColumn( 8 ).setPreferredWidth( 150 );  // producers

	// Set vertical size just enough for 10 entries
	opnameTable.setPreferredScrollableViewportSize( new Dimension( 900, 240 ) );


	constraints.gridx = 0;
	constraints.gridy = 11;
	constraints.gridwidth = 3;
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 1.0;
	constraints.weighty = 1.0;
	constraints.anchor = GridBagConstraints.CENTER;
	constraints.insets = new Insets( 10, 5, 5, 5 );
	container.add( new JScrollPane( opnameTable ), constraints );


	// Define the open opname dialog button because it is used by the list selection listener
	final JButton openOpnameDialogButton = new JButton( "Open Dialog" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel opnameListSelectionModel = opnameTable.getSelectionModel( );

	class OpnameListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

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

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add opnameListSelectionListener object to the selection model of the opname table
	final OpnameListSelectionListener opnameListSelectionListener = new OpnameListSelectionListener( );
	opnameListSelectionModel.addListSelectionListener( opnameListSelectionListener );

	// Class to handle button actions: uses opnameListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new opname record
		    EditOpnameDialog editOpnameDialog =
			new EditOpnameDialog( connection, frame,
					      selectedMediumId,
					      opusFilterTextField.getText( ),
					      selectedComponistenPersoonId,
					      selectedComponistenId,
					      ( String )( componistenPersoonComboBox.getSelectedItem( ) ),
					      selectedGenreId,
					      ( String )( genreComboBox.getSelectedItem( ) ),
					      selectedTypeId,
					      ( String )( typeComboBox.getSelectedItem( ) ),
					      selectedPersoonAllMusiciId,
					      selectedMusiciId,
					      selectedMusiciEnsembleId,
					      selectedOpnameDatumId,
					      selectedOpnamePlaatsId,
					      selectedProducersId );
		} else {
		    int selectedRow = opnameListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
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
			    JOptionPane.showMessageDialog( frame,
							   "Geen opname geselecteerd",
							   "Opname frame error",
							   JOptionPane.ERROR_MESSAGE );
			    return;
			}

			// Do dialog
			EditOpnameDialog editOpnameDialog =
			    new EditOpnameDialog( connection, frame, selectedOpnameKey );

		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			logger.severe( "Delete not yet implemented" );
		    }
		}

		// Records may have been modified: setup the table model again
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

	constraints.gridx = 0;
	constraints.gridy = 12;
        constraints.weightx = 0.0;
        constraints.weighty = 0.0;
	container.add( buttonPanel, constraints );

	frame.setSize( 980, 800 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
