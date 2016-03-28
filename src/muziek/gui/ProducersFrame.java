// frame to show and select records from producers

package muziek.gui;

import java.sql.Connection; 
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*; 
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.*;
import java.util.logging.*;

import table.*;


public class ProducersFrame {
    final Logger logger = Logger.getLogger( "muziek.gui.ProducersFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Producers" );

    JTextField producersFilterTextField;

    PersoonComboBox persoonComboBox;
    int selectedPersoonId = 0;

    ProducersTableModel producersTableModel;
    TableSorter producersTableSorter;
    JTable producersTable;


    class Producers {
	int	id;
	String  string;

	public Producers( int    id,
			  String string ) {
	    this.id = id;
	    this.string = string;
	}

	public boolean presentInTable( String tableString ) {
	    // Check if producersId is present in table
	    try {
		Statement statement = connection.createStatement( );
		ResultSet resultSet = statement.executeQuery( "SELECT producers_id FROM " + tableString +
							      " WHERE producers_id = " + id );
		if ( resultSet.next( ) ) {
		    JOptionPane.showMessageDialog( frame,
						   "Tabel " + tableString +
						   " heeft nog verwijzing naar '" + string + "'",
						   "Producers frame error",
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


    public ProducersFrame( final Connection connection ) {
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
	container.add( new JLabel( "Producers Filter:" ), constraints );
	producersFilterTextField = new JTextField( 30 );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( producersFilterTextField, constraints );

	class ProducersFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Setup the producers table
		producersTableModel.setupProducersTableModel( producersFilterTextField.getText( ),
							      selectedPersoonId );
	    }
	}
	producersFilterTextField.addActionListener( new ProducersFilterActionListener( ) );


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

		// Setup the producers table
		producersTableModel.setupProducersTableModel( producersFilterTextField.getText( ),
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


	// Create producers table from title table model
	producersTableModel = new ProducersTableModel( connection );
	producersTableSorter = new TableSorter( producersTableModel );
	producersTable = new JTable( producersTableSorter );
	producersTableSorter.setTableHeader( producersTable.getTableHeader( ) );
	// producersTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	producersTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	producersTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	producersTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	producersTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 300 );  // producers
	producersTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 200 );  // persoon

	// Set vertical size just enough for 20 entries
	producersTable.setPreferredScrollableViewportSize( new Dimension( 550, 320 ) );

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
	constraints.insets = new Insets( 10, 0, 10, 10 );
	container.add( new JScrollPane( producersTable ), constraints );


	// Define the edit, delete button because it is used by the list selection listener
	final JButton editProducersButton = new JButton( "Edit" );
	final JButton deleteProducersButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel producersListSelectionModel = producersTable.getSelectionModel( );

	class ProducersListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( producersListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    editProducersButton.setEnabled( false );
		    deleteProducersButton.setEnabled( false );
		    return;
		}

		int viewRow = producersListSelectionModel.getMinSelectionIndex( );
		selectedRow = producersTableSorter.modelIndex( viewRow );
		editProducersButton.setEnabled( true );
		deleteProducersButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add producersListSelectionListener object to the selection model of the musici table
	final ProducersListSelectionListener producersListSelectionListener = new ProducersListSelectionListener( );
	producersListSelectionModel.addListSelectionListener( producersListSelectionListener );

	// Class to handle button actions: uses producersListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new producers record
		    EditProducersDialog editProducersDialog =
			new EditProducersDialog( connection, frame,
						 producersFilterTextField.getText( ) );
		} else {
		    int selectedRow = producersListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen producers geselecteerd",
						       "Producers frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected producers id 
		    int selectedProducersId = producersTableModel.getProducersId( selectedRow );

		    // Check if producers has been selected
		    if ( selectedProducersId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen producers geselecteerd",
						       "Producers frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
			// Do dialog
			EditProducersDialog editProducersDialog =
			    new EditProducersDialog( connection, frame, selectedProducersId );

		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			final Producers producers = new Producers( producersTableModel.getProducersId( selectedRow ),
								   producersTableModel.getProducersString( selectedRow ) );

			// Check if producers ID is still used
			if ( producers.presentInTable( "producers_persoon" ) ) return;
			if ( producers.presentInTable( "opname" ) ) return;

			// Replace null or empty string by single space for messages
			if ( ( producers.string == null ) || ( producers.string.length( ) == 0  ) ) {
			    producers.string = " ";
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + producers.string + "' ?",
							   "Delete Producers record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM producers";
			deleteString += " WHERE producers_id = " + producers.id;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with producers_id  = " +
						       producers.id + " in producers" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Producers record",
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
		producersTableSorter.clearSortingState( );
		producersTableModel.setupProducersTableModel( producersFilterTextField.getText( ),
							      selectedPersoonId );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertProducersButton = new JButton( "Insert" );
	insertProducersButton.setActionCommand( "insert" );
	insertProducersButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertProducersButton );

	editProducersButton.setActionCommand( "edit" );
	editProducersButton.setEnabled( false );
	editProducersButton.addActionListener( buttonActionListener );
	buttonPanel.add( editProducersButton );

	deleteProducersButton.setActionCommand( "delete" );
	deleteProducersButton.setEnabled( false );
	deleteProducersButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteProducersButton );

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
