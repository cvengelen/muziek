// frame to show and select records from opslag

package muziek.opslag;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.*;

import table.*;


public class OpslagFrame {
    final Logger logger = Logger.getLogger( "muziek.opslag.OpslagFrame" );

    final Connection connection;
    final JFrame frame = new JFrame( "Opslag" );

    OpslagTableModel opslagTableModel;
    TableSorter opslagTableSorter;
    JTable opslagTable;

    class Opslag {
	int	id;
	String  name;

	public Opslag( int    id,
			String name ) {
	    this.id = id;
	    this.name = name;
	}

	public boolean presentInTable( String tableString ) {
	    // Check if opslagId is present in table
	    try {
		Statement statement = connection.createStatement( );
		ResultSet resultSet = statement.executeQuery( "SELECT opslag_id FROM " + tableString +
							      " WHERE opslag_id = " + id );
		if ( resultSet.next( ) ) {
		    JOptionPane.showMessageDialog( frame,
						   "Tabel " + tableString +
						   " heeft nog verwijzing naar '" + name + "'",
						   "Opslag frame error",
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


    public OpslagFrame( final Connection connection ) {
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
	container.add( new JLabel( "Opslag Filter:" ), constraints );
	final JTextField opslagFilterTextField = new JTextField( 15 );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( opslagFilterTextField, constraints );

	class OpslagFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Setup the opslag table
		opslagTableModel.setupOpslagTableModel( opslagFilterTextField.getText( ) );
	    }
	}
	opslagFilterTextField.addActionListener( new OpslagFilterActionListener( ) );


	// Create opslag table from title table model
	opslagTableModel = new OpslagTableModel( connection );
	opslagTableSorter = new TableSorter( opslagTableModel );
	opslagTable = new JTable( opslagTableSorter );
	opslagTableSorter.setTableHeader( opslagTable.getTableHeader( ) );
	// opslagTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	opslagTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	opslagTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // Id
	opslagTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 250 );  // opslag

	// Set vertical size just enough for 20 entries
	opslagTable.setPreferredScrollableViewportSize( new Dimension( 300, 320 ) );

	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 5;
	constraints.insets = new Insets( 10, 0, 10, 10 );
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( new JScrollPane( opslagTable ), constraints );


	////////////////////////////////////////////////
	// Add, Delete, Close Buttons
	////////////////////////////////////////////////

	// Define the delete button because it is used by the list selection listener
	final JButton deleteOpslagButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel opslagListSelectionModel = opslagTable.getSelectionModel( );

	class OpslagListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( opslagListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteOpslagButton.setEnabled( false );
		    return;
		}

		int viewRow = opslagListSelectionModel.getMinSelectionIndex( );
		selectedRow = opslagTableSorter.modelIndex( viewRow );
		deleteOpslagButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add opslagListSelectionListener object to the selection model of the musici table
	final OpslagListSelectionListener opslagListSelectionListener = new OpslagListSelectionListener( );
	opslagListSelectionModel.addListSelectionListener( opslagListSelectionListener );

	// Class to handle button actions: uses opslagListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( opslag_id ) FROM opslag" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for opslag_id in opslag" );
			    return;
			}
			int opslagId = resultSet.getInt( 1 ) + 1;
			String insertString = "INSERT INTO opslag SET opslag_id = " + opslagId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in opslag" );
			    return;
			}
		    } catch ( SQLException ex ) {
			logger.severe( "SQLException: " + ex.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = opslagListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen Opslag geselecteerd",
						       "Opslag frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected opslag id
		    int selectedOpslagId = opslagTableModel.getOpslagId( selectedRow );

		    // Check if opslag has been selected
		    if ( selectedOpslagId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen opslag geselecteerd",
						       "Opslag frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }


		    // Replace null or empty string by single space for messages
		    String selectedOpslagString = opslagTableModel.getOpslagString( selectedRow );
		    if ( ( selectedOpslagString == null ) || ( selectedOpslagString.length( ) == 0  ) ) {
			selectedOpslagString = " ";
		    }

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			final Opslag opslag = new Opslag( selectedOpslagId,
							     opslagTableModel.getOpslagString( selectedRow ) );

			// Check if opslag ID is still used in table medium
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet = statement.executeQuery( "SELECT opslag_id FROM medium " +
									  " WHERE opslag_id = " + selectedOpslagId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel medium heeft nog verwijzing naar '" +
							       selectedOpslagString + "'",
							       "Opslag frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + selectedOpslagString + "' ?",
							   "Delete Opslag record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM opslag";
			deleteString += " WHERE opslag_id = " + opslag.id;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with opslag_id  = " +
						       opslag.id + " in opslag" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Opslag record",
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
		opslagTableSorter.clearSortingState( );
		opslagTableModel.setupOpslagTableModel( opslagFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertOpslagButton = new JButton( "Insert" );
	insertOpslagButton.setActionCommand( "insert" );
	insertOpslagButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertOpslagButton );

	deleteOpslagButton.setActionCommand( "delete" );
	deleteOpslagButton.setEnabled( false );
	deleteOpslagButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteOpslagButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.gridwidth = 3;
	constraints.insets = new Insets( 10, 0, 0, 10 );
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	frame.setSize( 400, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
