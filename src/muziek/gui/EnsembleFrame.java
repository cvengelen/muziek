// frame to show and select records from ensemble

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
import java.util.logging.*;

import table.*;


public class EnsembleFrame {
    final Logger logger = Logger.getLogger( "muziek.gui.EnsembleFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Ensemble" );

    JTextField ensembleFilterTextField;

    EnsembleTableModel ensembleTableModel;
    TableSorter ensembleTableSorter;
    JTable ensembleTable;


    public EnsembleFrame( final Connection connection ) {
	this.connection = connection;

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.insets = new Insets( 0, 0, 10, 10 );

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Ensemble Filter:" ), constraints );
	ensembleFilterTextField = new JTextField( 20 );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( ensembleFilterTextField, constraints );

	class EnsembleFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Setup the ensemble table
		ensembleTableModel.setupEnsembleTableModel( ensembleFilterTextField.getText( ) );
	    }
	}
	ensembleFilterTextField.addActionListener( new EnsembleFilterActionListener( ) );


	// Create ensemble table from title table model
	ensembleTableModel = new EnsembleTableModel( connection );
	ensembleTableSorter = new TableSorter( ensembleTableModel );
	ensembleTable = new JTable( ensembleTableSorter );
	ensembleTableSorter.setTableHeader( ensembleTable.getTableHeader( ) );
	// ensembleTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	ensembleTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	ensembleTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	ensembleTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // Id
	ensembleTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 400 );  // ensemble

	// Set vertical size just enough for 20 entries
	ensembleTable.setPreferredScrollableViewportSize( new Dimension( 450, 320 ) );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
	constraints.insets = new Insets( 10, 0, 10, 10 );
	container.add( new JScrollPane( ensembleTable ), constraints );


	////////////////////////////////////////////////
	// Add, Delete, Close Buttons
	////////////////////////////////////////////////

	// Define the delete button because it is used by the list selection listener
	final JButton deleteEnsembleButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel ensembleListSelectionModel = ensembleTable.getSelectionModel( );

	class EnsembleListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( ensembleListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteEnsembleButton.setEnabled( false );
		    return;
		}

		int viewRow = ensembleListSelectionModel.getMinSelectionIndex( );
		selectedRow = ensembleTableSorter.modelIndex( viewRow );
		deleteEnsembleButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add ensembleListSelectionListener object to the selection model of the musici table
	final EnsembleListSelectionListener ensembleListSelectionListener = new EnsembleListSelectionListener( );
	ensembleListSelectionModel.addListSelectionListener( ensembleListSelectionListener );

	// Class to handle button actions: uses ensembleListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new ensemble record
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( ensemble_id ) FROM ensemble" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for ensemble_id in ensemble" );
			    return;
			}
			int ensembleId = resultSet.getInt( 1 ) + 1;
			String insertString = "INSERT INTO ensemble SET ensemble_id = " + ensembleId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in ensemble" );
			    return;
			}
		    } catch ( SQLException ex ) {
			logger.severe( "SQLException: " + ex.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = ensembleListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen ensemble geselecteerd",
						       "Ensemble frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected ensemble id 
		    int selectedEnsembleId = ensembleTableModel.getEnsembleId( selectedRow );

		    // Check if ensemble has been selected
		    if ( selectedEnsembleId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen ensemble geselecteerd",
						       "Ensemble frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }


		    String ensembleString = ensembleTableModel.getEnsembleString( selectedRow );
		    // Replace null or empty string by single space for messages
		    if ( ( ensembleString == null ) || ( ensembleString.length( ) == 0  ) ) {
			ensembleString = " ";
		    }

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if ensembleId is present in table musici_ensemble
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet = statement.executeQuery( "SELECT ensemble_id FROM musici_ensemble" +
									  " WHERE ensemble_id = " + selectedEnsembleId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel musici_ensemble heeft nog verwijzing naar '" +
							       ensembleString + "'",
							       "Ensemble frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + ensembleString + "' ?",
							   "Delete Ensemble record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM ensemble";
			deleteString += " WHERE ensemble_id = " + selectedEnsembleId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with ensemble_id  = " +
						       selectedEnsembleId + " in ensemble" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Ensemble record",
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
		ensembleTableSorter.clearSortingState( );
		ensembleTableModel.setupEnsembleTableModel( ensembleFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertEnsembleButton = new JButton( "Insert" );
	insertEnsembleButton.setActionCommand( "insert" );
	insertEnsembleButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertEnsembleButton );

	deleteEnsembleButton.setActionCommand( "delete" );
	deleteEnsembleButton.setEnabled( false );
	deleteEnsembleButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteEnsembleButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.insets = new Insets( 10, 0, 0, 10 );
	container.add( buttonPanel, constraints );

	frame.setSize( 520, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
