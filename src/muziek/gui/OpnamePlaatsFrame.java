// frame to show and select records from opname_plaats

package muziek.gui;

import java.sql.Connection; 
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*; 
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.*;
import java.util.logging.Logger;

import table.*;


public class OpnamePlaatsFrame {
    final Logger logger = Logger.getLogger( "muziek.gui.OpnamePlaatsFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Opname Plaats");

    JTextField opnamePlaatsFilterTextField;

    OpnamePlaatsTableModel opnamePlaatsTableModel;
    TableSorter opnamePlaatsTableSorter;
    JTable opnamePlaatsTable;


    public OpnamePlaatsFrame( final Connection connection ) {
	this.connection = connection;

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 0, 0, 10, 10 );


	/////////////////////////////////
	// Text filter action listener
	/////////////////////////////////

	class TextFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Setup the opnamePlaats table
		opnamePlaatsTableModel.setupOpnamePlaatsTableModel( opnamePlaatsFilterTextField.getText( ) );
	    }
	}
	TextFilterActionListener textFilterActionListener = new TextFilterActionListener( );


	/////////////////////////////////
	// Opname plaats filter string
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opname Plaats Filter:" ), constraints );

	opnamePlaatsFilterTextField = new JTextField( 20 );
	opnamePlaatsFilterTextField.addActionListener( textFilterActionListener );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( opnamePlaatsFilterTextField, constraints );


	/////////////////////////////////
	// OpnamePlaats Table
	/////////////////////////////////

	// Create opnamePlaats table from title table model
	opnamePlaatsTableModel = new OpnamePlaatsTableModel( connection );
	opnamePlaatsTableSorter = new TableSorter( opnamePlaatsTableModel );
	opnamePlaatsTable = new JTable( opnamePlaatsTableSorter );
	opnamePlaatsTableSorter.setTableHeader( opnamePlaatsTable.getTableHeader( ) );
	// opnamePlaatsTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	opnamePlaatsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	opnamePlaatsTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	opnamePlaatsTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	opnamePlaatsTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 400 );  // opname plaats

	// Set vertical size just enough for 20 entries
	opnamePlaatsTable.setPreferredScrollableViewportSize( new Dimension( 450, 320 ) );


	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.gridwidth = 3;
	constraints.anchor = GridBagConstraints.CENTER;
	constraints.insets = new Insets( 10, 0, 10, 10 );
	container.add( new JScrollPane( opnamePlaatsTable ), constraints );


	// Define the delete button because it is enabled/disabled by the list selection listener
	final JButton deleteOpnamePlaatsButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel opnamePlaatsListSelectionModel = opnamePlaatsTable.getSelectionModel( );

	class OpnamePlaatsListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( opnamePlaatsListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteOpnamePlaatsButton.setEnabled( false );
		    return;
		}

		int viewRow = opnamePlaatsListSelectionModel.getMinSelectionIndex( );
		selectedRow = opnamePlaatsTableSorter.modelIndex( viewRow );
		deleteOpnamePlaatsButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add opnamePlaatsListSelectionListener object to the selection model of the musici table
	final OpnamePlaatsListSelectionListener opnamePlaatsListSelectionListener = new OpnamePlaatsListSelectionListener( );
	opnamePlaatsListSelectionModel.addListSelectionListener( opnamePlaatsListSelectionListener );

	// Class to handle button actions: uses opnamePlaatsListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( opname_plaats_id ) FROM opname_plaats" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for opname_plaats_id in opname_plaats" );
			    return;
			}
			int opnamePlaatsId = resultSet.getInt( 1 ) + 1;
			String insertString = "INSERT INTO opname_plaats SET opname_plaats_id = " + opnamePlaatsId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in opname_plaats" );
			    return;
			}
		    } catch ( SQLException ex ) {
			logger.severe( "SQLException: " + ex.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = opnamePlaatsListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen opname plaats geselecteerd",
						       "Opname Plaats frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected opnamePlaats id 
		    int selectedOpnamePlaatsId = opnamePlaatsTableModel.getOpnamePlaatsId( selectedRow );

		    // Check if opnamePlaats has been selected
		    if ( selectedOpnamePlaatsId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen opname plaats geselecteerd",
						       "Opname Plaats frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the opname plaats
		    String opnamePlaatsString = opnamePlaatsTableModel.getOpnamePlaatsString( selectedRow );

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if opname_plaats ID is still used
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT opname_plaats_id FROM opname WHERE opname_plaats_id = " +
							selectedOpnamePlaatsId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel opname heeft nog verwijzing naar '" +
							       opnamePlaatsString + "'",
							       "Opname Plaats frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + opnamePlaatsString + "' ?",
							   "Delete Opname_Plaats record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM opname_plaats";
			deleteString += " WHERE opname_plaats_id = " + selectedOpnamePlaatsId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with opname_plaats_id  = " +
						       selectedOpnamePlaatsId + " in opname_plaats" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Opname Plaats record",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    } else {
			logger.severe( "Unimplemented command: " + actionEvent.getActionCommand( ) );
		    }
		}

		// Records may have been modified: setup the table model again
		opnamePlaatsTableSorter.clearSortingState( );
		opnamePlaatsTableModel.setupOpnamePlaatsTableModel( opnamePlaatsFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertOpnamePlaatsButton = new JButton( "Insert" );
	insertOpnamePlaatsButton.setActionCommand( "insert" );
	insertOpnamePlaatsButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertOpnamePlaatsButton );

	deleteOpnamePlaatsButton.setActionCommand( "delete" );
	deleteOpnamePlaatsButton.setEnabled( false );
	deleteOpnamePlaatsButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteOpnamePlaatsButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

	constraints.gridx = 0;
	constraints.gridy = 7;
	constraints.insets = new Insets( 10, 0, 0, 10 );
	container.add( buttonPanel, constraints );

	frame.setSize( 500, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
