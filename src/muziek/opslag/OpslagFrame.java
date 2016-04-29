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

/**
 * Frame to show, insert and update records in the opslag table in schema muziek.
 * An instance of OpslagFrame is created by class muziek.Main.
 *
 * @author Chris van Engelen
 */
public class OpslagFrame {
    private final Logger logger = Logger.getLogger( OpslagFrame.class.getCanonicalName() );

    private final JFrame frame = new JFrame( "Opslag" );

    private OpslagTableModel opslagTableModel;
    private TableSorter opslagTableSorter;

    public OpslagFrame( final Connection connection ) {

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
        constraints.gridwidth = 1;

	constraints.insets = new Insets( 20, 20, 5, 5 );
        constraints.gridx = 0;
	constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opslag Filter:" ), constraints );

	final JTextField opslagFilterTextField = new JTextField( 15 );
        opslagFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the opslag table
            opslagTableSorter.clearSortingState();
            opslagTableModel.setupOpslagTableModel( opslagFilterTextField.getText( ) );
        } );

        constraints.insets = new Insets( 20, 5, 5, 100 );
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( opslagFilterTextField, constraints );

	// Create opslag table from title table model
	opslagTableModel = new OpslagTableModel( connection );
	opslagTableSorter = new TableSorter( opslagTableModel );
	final JTable opslagTable = new JTable( opslagTableSorter );
	opslagTableSorter.setTableHeader( opslagTable.getTableHeader( ) );
	// opslagTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	opslagTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	opslagTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // Id
	opslagTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 250 );  // opslag

	// Set vertical size just enough for 20 entries
	opslagTable.setPreferredScrollableViewportSize( new Dimension( 300, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
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
	    private int selectedRow = -1;

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

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add opslagListSelectionListener object to the selection model of the musici table
	final OpslagListSelectionListener opslagListSelectionListener = new OpslagListSelectionListener( );
	opslagListSelectionModel.addListSelectionListener( opslagListSelectionListener );

	// Class to handle button actions: uses opslagListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
                    frame.dispose();
		    return;
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
			deleteString += " WHERE opslag_id = " + selectedOpslagId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with opslag_id  = " +
                                                        selectedOpslagId + " in opslag" );
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

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;
	container.add( buttonPanel, constraints );

        // Add a window listener to close the connection when the frame is disposed
        frame.addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    // Close the connection to the MySQL database
                    connection.close( );
                } catch (SQLException sqlException) {
                    logger.severe( "SQL exception closing connection: " + sqlException.getMessage() );
                }
            }
        } );

	frame.setSize( 360, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
