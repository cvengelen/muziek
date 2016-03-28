// frame to show and select records from subtype

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


public class SubtypeFrame {
    final Logger logger = Logger.getLogger( "muziek.gui.SubtypeFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Subtype");

    JTextField subtypeFilterTextField;

    SubtypeTableModel subtypeTableModel;
    TableSorter subtypeTableSorter;
    JTable subtypeTable;


    public SubtypeFrame( final Connection connection ) {
	this.connection = connection;

	// put the contsubtypes the content pane
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
		// Setup the subtype table
		subtypeTableModel.setupSubtypeTableModel( subtypeFilterTextField.getText( ) );
	    }
	}
	TextFilterActionListener textFilterActionListener = new TextFilterActionListener( );


	/////////////////////////////////
	// Subtype filter string
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Subtype Filter:" ), constraints );

	subtypeFilterTextField = new JTextField( 15 );
	subtypeFilterTextField.addActionListener( textFilterActionListener );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( subtypeFilterTextField, constraints );


	/////////////////////////////////
	// Subtype Table
	/////////////////////////////////

	// Create subtype table from title table model
	subtypeTableModel = new SubtypeTableModel( connection );
	subtypeTableSorter = new TableSorter( subtypeTableModel );
	subtypeTable = new JTable( subtypeTableSorter );
	subtypeTableSorter.setTableHeader( subtypeTable.getTableHeader( ) );
	// subtypeTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	subtypeTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	subtypeTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	subtypeTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	subtypeTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 300 );  // subtype

	// Set vertical size just enough for 20 entries
	subtypeTable.setPreferredScrollableViewportSize( new Dimension( 350, 320 ) );


	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.gridwidth = 3;
	constraints.anchor = GridBagConstraints.CENTER;
	constraints.insets = new Insets( 10, 0, 10, 10 );
	container.add( new JScrollPane( subtypeTable ), constraints );


	// Define the delete button because it is enabled/disabled by the list selection listener
	final JButton deleteSubtypeButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel subtypeListSelectionModel = subtypeTable.getSelectionModel( );

	class SubtypeListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( subtypeListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteSubtypeButton.setEnabled( false );
		    return;
		}

		int viewRow = subtypeListSelectionModel.getMinSelectionIndex( );
		selectedRow = subtypeTableSorter.modelIndex( viewRow );
		deleteSubtypeButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add subtypeListSelectionListener object to the selection model of the musici table
	final SubtypeListSelectionListener subtypeListSelectionListener = new SubtypeListSelectionListener( );
	subtypeListSelectionModel.addListSelectionListener( subtypeListSelectionListener );

	// Class to handle button actions: uses subtypeListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( subtype_id ) FROM subtype" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for subtype_id in subtype" );
			    return;
			}
			int subtypeId = resultSet.getInt( 1 ) + 1;
			String insertString = "INSERT INTO subtype SET subtype_id = " + subtypeId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in subtype" );
			    return;
			}
		    } catch ( SQLException ex ) {
			logger.severe( "SQLException: " + ex.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = subtypeListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen subtype geselecteerd",
						       "Subtype frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected subtype id 
		    int selectedSubtypeId = subtypeTableModel.getSubtypeId( selectedRow );

		    // Check if subtype has been selected
		    if ( selectedSubtypeId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen subtype geselecteerd",
						       "Subtype frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the subtype
		    String subtypeString = subtypeTableModel.getSubtypeString( selectedRow );

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if subtype ID is still used
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT subtype_id FROM opus WHERE subtype_id = " +
							selectedSubtypeId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel opus heeft nog verwijzing naar '" +
							       subtypeString + "'",
							       "Subtype frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + subtypeString + "' ?",
							   "Delete Subtype record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM subtype";
			deleteString += " WHERE subtype_id = " + selectedSubtypeId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with subtype_id  = " +
						       selectedSubtypeId + " in subtype" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Subtype record",
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
		subtypeTableSorter.clearSortingState( );
		subtypeTableModel.setupSubtypeTableModel( subtypeFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertSubtypeButton = new JButton( "Insert" );
	insertSubtypeButton.setActionCommand( "insert" );
	insertSubtypeButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertSubtypeButton );

	deleteSubtypeButton.setActionCommand( "delete" );
	deleteSubtypeButton.setEnabled( false );
	deleteSubtypeButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteSubtypeButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

	constraints.gridx = 0;
	constraints.gridy = 7;
	constraints.insets = new Insets( 10, 0, 0, 10 );
	container.add( buttonPanel, constraints );

	frame.setSize( 430, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
