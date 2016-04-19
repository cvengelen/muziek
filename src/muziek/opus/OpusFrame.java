//
// Project:	muziek
// Component:	gui
// File:	OpnameFrame.java
// Description:	Frame to show all or selected records in the opus table
// Author:	Chris van Engelen
// History:	2006/02/26: Initial version
//		2009/01/01: Add selection on Componist-Persoon
//              2011/08/28: Add weightx, weighty, fill
//

package muziek.opus;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.Logger;

import muziek.gui.*;
import table.*;


public class OpusFrame {
    final Logger logger = Logger.getLogger( "muziek.opus.OpusFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Opus");

    JTextField opusTitelFilterTextField;

    JTextField opusNummerFilterTextField;

    ComponistenPersoonComboBox componistenPersoonComboBox;
    int selectedComponistenPersoonId = 0;
    int selectedComponistenId = 0;

    GenreComboBox genreComboBox;
    int selectedGenreId = 0;

    TijdperkComboBox tijdperkComboBox;
    int selectedTijdperkId = 0;

    TypeComboBox typeComboBox;
    int selectedTypeId = 0;

    SubtypeComboBox subtypeComboBox;
    int selectedSubtypeId = 0;

    OpusTableModel opusTableModel;
    TableSorter opusTableSorter;
    JTable opusTable;


    public OpusFrame( final Connection connection ) {
	this.connection = connection;

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.insets = new Insets( 15, 5, 5, 5 );
	constraints.weighty = 0.0;


	/////////////////////////////////
	// Opus Titel filter string
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.weightx = 0.0;
	constraints.gridwidth = 1;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opus Titel Filter:" ), constraints );
	opusTitelFilterTextField = new JTextField( 40 );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.gridwidth = 3;
	container.add( opusTitelFilterTextField, constraints );

	class OpusTitelFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Number of rows may have changed: reset the table sorter
		opusTableSorter.clearSortingState( );

		// Setup the opus table
		opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
						    opusNummerFilterTextField.getText( ),
						    selectedComponistenPersoonId,
						    selectedComponistenId,
						    selectedGenreId,
						    selectedTijdperkId,
						    selectedTypeId,
						    selectedSubtypeId );
	    }
	}
	opusTitelFilterTextField.addActionListener( new OpusTitelFilterActionListener( ) );


	/////////////////////////////////
	// Opus nummer filter string
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.weightx = 0.0;
	constraints.gridwidth = 1;
	constraints.insets = new Insets( 5, 5, 5, 5 );
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opus Nummer Filter:" ), constraints );
	opusNummerFilterTextField = new JTextField( 40 );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.gridwidth = 3;
	container.add( opusNummerFilterTextField, constraints );

	class OpusNummerFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Number of rows may have changed: reset the table sorter
		opusTableSorter.clearSortingState( );

		// Setup the opus table
		opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
						    opusNummerFilterTextField.getText( ),
						    selectedComponistenPersoonId,
						    selectedComponistenId,
						    selectedGenreId,
						    selectedTijdperkId,
						    selectedTypeId,
						    selectedSubtypeId );
	    }
	}
	opusNummerFilterTextField.addActionListener( new OpusNummerFilterActionListener( ) );


	/////////////////////////////////
	// Componisten Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Componisten:" ), constraints );

	// Setup a JComboBox with the results of the query on componisten
	componistenPersoonComboBox = new ComponistenPersoonComboBox( connection, frame, false );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( componistenPersoonComboBox, constraints );

	class SelectComponistenActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Number of rows may have changed: reset the table sorter
		opusTableSorter.clearSortingState( );

		// Get the selected componisten-persoon ID and componisten ID from the combo box
		selectedComponistenPersoonId = componistenPersoonComboBox.getSelectedComponistenPersoonId( );
		selectedComponistenId = componistenPersoonComboBox.getSelectedComponistenId( );

		// Setup the opus table for the selected componisten
		opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
						    opusNummerFilterTextField.getText( ),
						    selectedComponistenPersoonId,
						    selectedComponistenId,
						    selectedGenreId,
						    selectedTijdperkId,
						    selectedTypeId,
						    selectedSubtypeId );
	    }
	}
	componistenPersoonComboBox.addActionListener( new SelectComponistenActionListener( ) );

	JButton filterComponistenButton = new JButton( "Filter" );
	filterComponistenButton.setActionCommand( "filterComponisten" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.weightx = 1.0;
	constraints.gridwidth = 1;
	container.add( filterComponistenButton, constraints );

	class FilterComponistenActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		componistenPersoonComboBox.filterComponistenPersoonComboBox( );
	    }
	}
	filterComponistenButton.addActionListener( new FilterComponistenActionListener( ) );


	// Setup a JComboBox for genre
	genreComboBox = new GenreComboBox( connection, selectedGenreId );
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Genre:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( genreComboBox, constraints );

	class SelectGenreActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Number of rows may have changed: reset the table sorter
		opusTableSorter.clearSortingState( );

		// Get the selected genre ID from the combo box
		selectedGenreId = genreComboBox.getSelectedGenreId( );

		// Setup the opus table for the selected genre
		opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
						    opusNummerFilterTextField.getText( ),
						    selectedComponistenPersoonId,
						    selectedComponistenId,
						    selectedGenreId,
						    selectedTijdperkId,
						    selectedTypeId,
						    selectedSubtypeId );
	    }
	}
	genreComboBox.addActionListener( new SelectGenreActionListener( ) );


	// Setup a JComboBox for tijdperk
	tijdperkComboBox = new TijdperkComboBox( connection, selectedTijdperkId );
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Tijdperk:" ), constraints );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( tijdperkComboBox, constraints );

	class SelectTijdperkActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Number of rows may have changed: reset the table sorter
		opusTableSorter.clearSortingState( );

		// Get the selected tijdperk ID from the combo box
		selectedTijdperkId = tijdperkComboBox.getSelectedTijdperkId( );

		// Setup the opus table for the selected tijdperk
		opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
						    opusNummerFilterTextField.getText( ),
						    selectedComponistenPersoonId,
						    selectedComponistenId,
						    selectedGenreId,
						    selectedTijdperkId,
						    selectedTypeId,
						    selectedSubtypeId );
	    }
	}
	tijdperkComboBox.addActionListener( new SelectTijdperkActionListener( ) );


	// Setup a JComboBox for type
	typeComboBox = new TypeComboBox( connection, selectedTypeId );
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Type:" ), constraints );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( typeComboBox, constraints );

	class SelectTypeActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Number of rows may have changed: reset the table sorter
		opusTableSorter.clearSortingState( );

		// Get the selected type ID from the combo box
		selectedTypeId = typeComboBox.getSelectedTypeId( );

		// Setup the opus table for the selected type
		opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
						    opusNummerFilterTextField.getText( ),
						    selectedComponistenPersoonId,
						    selectedComponistenId,
						    selectedGenreId,
						    selectedTijdperkId,
						    selectedTypeId,
						    selectedSubtypeId );
	    }
	}
	typeComboBox.addActionListener( new SelectTypeActionListener( ) );


	// Setup a JComboBox for subtype
	subtypeComboBox = new SubtypeComboBox( connection, frame, selectedSubtypeId );
	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Subtype:" ), constraints );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( subtypeComboBox, constraints );

	class SelectSubtypeActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Number of rows may have changed: reset the table sorter
		opusTableSorter.clearSortingState( );

		// Get the selected subtype ID from the combo box
		selectedSubtypeId = subtypeComboBox.getSelectedSubtypeId( );

		// Setup the opus table for the selected subtype
		opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
						    opusNummerFilterTextField.getText( ),
						    selectedComponistenPersoonId,
						    selectedComponistenId,
						    selectedGenreId,
						    selectedTijdperkId,
						    selectedTypeId,
						    selectedSubtypeId );
	    }
	}
	subtypeComboBox.addActionListener( new SelectSubtypeActionListener( ) );


	// Define the open dialog, enable edit, cancel, save, and delete buttons.
	// These are enabled/disabled by the table model and the list selection listener.
	final JButton openOpusDialogButton = new JButton( "Open Dialog" );
	final JButton enableRowEditButton = new JButton( "Edit" );
	final JButton cancelRowEditButton = new JButton( "Cancel" );
	final JButton saveRowEditButton = new JButton( "Save" );
	final JButton deleteOpusButton = new JButton( "Delete" );

	// Create opus table from opus table model
	opusTableModel = new OpusTableModel( connection,
					     cancelRowEditButton,
					     saveRowEditButton );
	opusTableSorter = new TableSorter( opusTableModel );
	opusTable = new JTable( opusTableSorter );
	opusTableSorter.setTableHeader( opusTable.getTableHeader( ) );
	// opusTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	opusTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	opusTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	opusTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 250 );  // opus titel
	opusTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 180 );  // componist
	opusTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth(  80 );  // opus nummer
	opusTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth(  70 );  // genre
	opusTable.getColumnModel( ).getColumn( 5 ).setPreferredWidth( 150 );  // tijdperk
	opusTable.getColumnModel( ).getColumn( 6 ).setPreferredWidth( 100 );  // type
	opusTable.getColumnModel( ).getColumn( 7 ).setPreferredWidth( 100 );  // subtype

	final DefaultCellEditor genreDefaultCellEditor =
	    new DefaultCellEditor( new GenreComboBox( connection, 0 ) );
	opusTable.getColumnModel( ).getColumn( 4 ).setCellEditor( genreDefaultCellEditor );

	final DefaultCellEditor tijdperkDefaultCellEditor =
	    new DefaultCellEditor( new TijdperkComboBox( connection, 0 ) );
	opusTable.getColumnModel( ).getColumn( 5 ).setCellEditor( tijdperkDefaultCellEditor );

	final DefaultCellEditor typeDefaultCellEditor =
	    new DefaultCellEditor( new TypeComboBox( connection, 0 ) );
	opusTable.getColumnModel( ).getColumn( 6 ).setCellEditor( typeDefaultCellEditor );

	final DefaultCellEditor subtypeDefaultCellEditor =
	    new DefaultCellEditor( new SubtypeComboBox( connection, null, false ) );
	opusTable.getColumnModel( ).getColumn( 7 ).setCellEditor( subtypeDefaultCellEditor );

	// Set vertical size just enough for 10 entries
	opusTable.setPreferredScrollableViewportSize( new Dimension( 900, 240 ) );


	constraints.gridx = 0;
	constraints.gridy = 9;
	// Setting weighty and fill is necessary for proper filling the frame when resized.
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 1.0;
	constraints.weighty = 1.0;
	constraints.anchor = GridBagConstraints.CENTER;
	constraints.gridwidth = 3;
	constraints.insets = new Insets( 10, 15, 5, 15 );
	container.add( new JScrollPane( opusTable ), constraints );


	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel opusListSelectionModel = opusTable.getSelectionModel( );

	class OpusListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Check if current row has modified values
		if ( opusTableModel.getRowModified( ) ) {
		    if ( selectedRow == -1 ) {
			logger.severe( "Invalid selected row" );
		    } else {
			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Data zijn gewijzigd: modificaties opslaan?",
							   "Record is gewijzigd",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result == JOptionPane.YES_OPTION ) {
			    // Save the changes in the table model, and in the database
			    if ( !( opusTableModel.saveEditRow( selectedRow ) ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Error: row not saved",
							       "Save opus record error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} else {
			    // Cancel any edits in the selected row
			    opusTableModel.cancelEditRow( selectedRow );
			}
		    }
		}

		// Ignore if nothing is selected
		if ( opusListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;

		    openOpusDialogButton.setEnabled( false );
		    enableRowEditButton.setEnabled( false );
		    cancelRowEditButton.setEnabled( false );
		    saveRowEditButton.setEnabled( false );
		    deleteOpusButton.setEnabled( false );

		    return;
		}

		// Disable row edit
		opusTableModel.unsetEditRow( );

		// Get the selected row
		int viewRow = opusListSelectionModel.getMinSelectionIndex( );
		selectedRow = opusTableSorter.modelIndex( viewRow );

		// Enable the open dialog and enable edit buttons
		openOpusDialogButton.setEnabled( true );
		enableRowEditButton.setEnabled( true );

		// Disable the cancel and save buttons (these will be enabled
		// when any data in the row is actually modified)
		cancelRowEditButton.setEnabled( false );
		saveRowEditButton.setEnabled( false );

		// Enable the delete button
		deleteOpusButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add opusListSelectionListener object to the selection model of the musici table
	final OpusListSelectionListener opusListSelectionListener = new OpusListSelectionListener( );
	opusListSelectionModel.addListSelectionListener( opusListSelectionListener );

	// Class to handle button actions: uses opusListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new opus record
		    EditOpusDialog editOpusDialog =
			new EditOpusDialog( connection, frame,
					    opusTitelFilterTextField.getText( ),
					    opusNummerFilterTextField.getText( ),
					    selectedComponistenPersoonId,
					    selectedComponistenId,
					    selectedGenreId,
					    selectedTijdperkId,
					    selectedTypeId,
					    selectedSubtypeId );

		    // Number of rows may have changed: reset the table sorter
		    opusTableSorter.clearSortingState( );

		    // Records may have been modified: setup the table model again
		    opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
							opusNummerFilterTextField.getText( ),
							selectedComponistenPersoonId,
							selectedComponistenId,
							selectedGenreId,
							selectedTijdperkId,
							selectedTypeId,
							selectedSubtypeId );

		    // Setup combo boxes again
		    componistenPersoonComboBox.setupComponistenPersoonComboBox( selectedComponistenPersoonId,
										selectedComponistenId );
		} else {
		    int selectedRow = opusListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen opus geselecteerd",
						       "Opus frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected opus id
		    int selectedOpusId = opusTableModel.getOpusId( selectedRow );

		    // Check if opus has been selected
		    if ( selectedOpusId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen opus geselecteerd",
						       "Opus frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "openDialog" ) ) {
			// Do dialog
			EditOpusDialog editOpusDialog =
			    new EditOpusDialog( connection, frame, selectedOpusId );

			// Number of rows may have changed: reset the table sorter
			opusTableSorter.clearSortingState( );

			// Records may have been modified: setup the table model again
			opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
							    opusNummerFilterTextField.getText( ),
							    selectedComponistenPersoonId,
							    selectedComponistenId,
							    selectedGenreId,
							    selectedTijdperkId,
							    selectedTypeId,
							    selectedSubtypeId );

			// Setup combo boxes again
			componistenPersoonComboBox.setupComponistenPersoonComboBox( selectedComponistenPersoonId,
										    selectedComponistenId );
		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			String selectedOpusTitelString = opusTableModel.getOpusTitelString( selectedRow );

			// Replace null or empty string by single space for messages
			if ( ( selectedOpusTitelString == null ) ||
			     ( selectedOpusTitelString.length( ) == 0  ) ) {
			    selectedOpusTitelString = " ";
			}

			// Check if selectedOpusId is present in opname table
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT opus_id FROM opname WHERE opus_id = " +
							selectedOpusId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel opname heeft nog verwijzing naar '" +
							       selectedOpusTitelString + "'",
							       "Opus frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + selectedOpusTitelString + "' ?",
							   "Delete Opus record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM opus";
			deleteString += " WHERE opus_id = " + selectedOpusId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with opus_id  = " +
						       selectedOpusId + " in opus" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete opus record",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			// Number of rows may have changed: reset the table sorter
			opusTableSorter.clearSortingState( );

			// Records may have been modified: setup the table model again
			opusTableModel.setupOpusTableModel( opusTitelFilterTextField.getText( ),
							    opusNummerFilterTextField.getText( ),
							    selectedComponistenPersoonId,
							    selectedComponistenId,
							    selectedGenreId,
							    selectedTijdperkId,
							    selectedTypeId,
							    selectedSubtypeId );

			// Setup combo boxes again
			componistenPersoonComboBox.setupComponistenPersoonComboBox( selectedComponistenPersoonId,
										    selectedComponistenId );
		    } else if ( actionEvent.getActionCommand( ).equals( "enableRowEdit" ) ) {
			// Allow to edit the selected row
			opusTableModel.setEditRow( selectedRow );

			// Disable the enable edit button
			enableRowEditButton.setEnabled( false );
		    } else if ( actionEvent.getActionCommand( ).equals( "cancelRowEdit" ) ) {
			// Cancel any edits in the selected row
			opusTableModel.cancelEditRow( selectedRow );

			// Remove the capability to edit the row
			opusTableModel.unsetEditRow( );

			// Enable the enable edit button, so that the user can select edit again
			enableRowEditButton.setEnabled( true );

			// Disable the cancel and save buttons
			cancelRowEditButton.setEnabled( false );
			saveRowEditButton.setEnabled( false );
		    } else if ( actionEvent.getActionCommand( ).equals( "saveRowEdit" ) ) {
			// Save the changes in the table model, and in the database
			if ( !( opusTableModel.saveEditRow( selectedRow ) ) ) {
			    JOptionPane.showMessageDialog( frame,
							   "Error: row not saved",
							   "Save opus record error",
							   JOptionPane.ERROR_MESSAGE );
			    return;
			}

			// Remove the capability to edit the row
			opusTableModel.unsetEditRow( );

			// Enable the enable edit button, so that the user can select edit again
			enableRowEditButton.setEnabled( true );

			// Disable the cancel and save buttons
			cancelRowEditButton.setEnabled( false );
			saveRowEditButton.setEnabled( false );
		    }
		}
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertOpusButton = new JButton( "Insert" );
	insertOpusButton.setActionCommand( "insert" );
	insertOpusButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertOpusButton );

	openOpusDialogButton.setActionCommand( "openDialog" );
	openOpusDialogButton.setEnabled( false );
	openOpusDialogButton.addActionListener( buttonActionListener );
	buttonPanel.add( openOpusDialogButton );

	enableRowEditButton.setActionCommand( "enableRowEdit" );
	enableRowEditButton.setEnabled( false );
	enableRowEditButton.addActionListener( buttonActionListener );
	buttonPanel.add( enableRowEditButton );

	cancelRowEditButton.setActionCommand( "cancelRowEdit" );
	cancelRowEditButton.setEnabled( false );
	cancelRowEditButton.addActionListener( buttonActionListener );
	buttonPanel.add( cancelRowEditButton );

	saveRowEditButton.setActionCommand( "saveRowEdit" );
	saveRowEditButton.setEnabled( false );
	saveRowEditButton.addActionListener( buttonActionListener );
	buttonPanel.add( saveRowEditButton );

	deleteOpusButton.setActionCommand( "delete" );
	deleteOpusButton.setEnabled( false );
	deleteOpusButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteOpusButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

	constraints.gridx = 0;
	constraints.gridy = 10;
	constraints.weighty = 0;
	constraints.gridwidth = 3;
	constraints.anchor = GridBagConstraints.CENTER;
	constraints.insets = new Insets( 5, 5, 15, 5 );
	container.add( buttonPanel, constraints );

	frame.setSize( 1000, 700 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
