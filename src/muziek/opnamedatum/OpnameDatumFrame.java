package muziek.opnamedatum;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.Logger;

import muziek.gui.EditOpnameDatumDialog;

import table.*;

/**
 * Frame to show, insert and update records in the opname_datum table in schema muziek.
 * An instance of OpnameDatumFrame is created by class muziek.Main.
 *
 * @author Chris van Engelen
 */
public class OpnameDatumFrame {
    private final Logger logger = Logger.getLogger( OpnameDatumFrame.class.getCanonicalName() );

    private final JFrame frame = new JFrame( "Opname Datum");

    private JTextField opnameDatumFilterTextField;

    private OpnameDatumTableModel opnameDatumTableModel;
    private TableSorter opnameDatumTableSorter;

    public OpnameDatumFrame( final Connection connection ) {

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );

	/////////////////////////////////
	// Opname datum filter string
	/////////////////////////////////

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opname Datum Filter:" ), constraints );

	opnameDatumFilterTextField = new JTextField( 20 );
	opnameDatumFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the opnameDatum table
            opnameDatumTableSorter.clearSortingState();
            opnameDatumTableModel.setupOpnameDatumTableModel( opnameDatumFilterTextField.getText( ) );
        } );

        constraints.insets = new Insets( 20, 5, 5, 200 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( opnameDatumFilterTextField, constraints );

	/////////////////////////////////
	// OpnameDatum Table
	/////////////////////////////////

	// Create opnameDatum table from title table model
	opnameDatumTableModel = new OpnameDatumTableModel( connection );
	opnameDatumTableSorter = new TableSorter( opnameDatumTableModel );
	final JTable opnameDatumTable = new JTable( opnameDatumTableSorter );
	opnameDatumTableSorter.setTableHeader( opnameDatumTable.getTableHeader( ) );
	// opnameDatumTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	opnameDatumTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	opnameDatumTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	opnameDatumTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	opnameDatumTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth(  80 );  // jaar 1
	opnameDatumTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth(  80 );  // maand 1
	opnameDatumTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth(  80 );  // jaar 2
	opnameDatumTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth(  80 );  // maand 2
	opnameDatumTable.getColumnModel( ).getColumn( 5 ).setPreferredWidth( 200 );  // opname datum

	// Set vertical size just enough for 20 entries
	opnameDatumTable.setPreferredScrollableViewportSize( new Dimension( 570, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	container.add( new JScrollPane( opnameDatumTable ), constraints );


	// Define the delete button because it is enabled/disabled by the list selection listener
	final JButton deleteOpnameDatumButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel opnameDatumListSelectionModel = opnameDatumTable.getSelectionModel( );

	class OpnameDatumListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( opnameDatumListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteOpnameDatumButton.setEnabled( false );
		    return;
		}

		int viewRow = opnameDatumListSelectionModel.getMinSelectionIndex( );
		selectedRow = opnameDatumTableSorter.modelIndex( viewRow );
		deleteOpnameDatumButton.setEnabled( true );
	    }

            int getSelectedRow ( ) { return selectedRow; }
	}

	// Add opnameDatumListSelectionListener object to the selection model of the musici table
	final OpnameDatumListSelectionListener opnameDatumListSelectionListener = new OpnameDatumListSelectionListener( );
	opnameDatumListSelectionModel.addListSelectionListener( opnameDatumListSelectionListener );

	// Class to handle button actions: uses opnameDatumListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
                    frame.dispose();
                    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new opnameDatum record
		    new EditOpnameDatumDialog( connection, frame,
                                               opnameDatumFilterTextField.getText( ) );

		    // Records may have been modified: setup the table model again
		    opnameDatumTableModel.setupOpnameDatumTableModel( opnameDatumFilterTextField.getText( ) );
		} else {
		    int selectedRow = opnameDatumListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen opnameDatum geselecteerd",
						       "OpnameDatum frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected opnameDatum id
		    int selectedOpnameDatumId = opnameDatumTableModel.getOpnameDatumId( selectedRow );

		    // Check if opnameDatum has been selected
		    if ( selectedOpnameDatumId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen opnameDatum geselecteerd",
						       "OpnameDatum frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the opname datum
		    String opnameDatumString = opnameDatumTableModel.getOpnameDatumString( selectedRow );

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if opname_datum ID is still used
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT opname_datum_id FROM opname WHERE opname_datum_id = " +
							selectedOpnameDatumId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel opname heeft nog verwijzing naar '" +
							       opnameDatumString + "'",
							       "Opname Datum frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + opnameDatumString + "' ?",
							   "Delete Opname_Datum record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM opname_datum";
			deleteString += " WHERE opname_datum_id = " + selectedOpnameDatumId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with opname_datum_id  = " +
						       selectedOpnameDatumId + " in opname_datum" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Opname Datum record",
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
		opnameDatumTableSorter.clearSortingState( );
		opnameDatumTableModel.setupOpnameDatumTableModel( opnameDatumFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertOpnameDatumButton = new JButton( "Insert" );
	insertOpnameDatumButton.setActionCommand( "insert" );
	insertOpnameDatumButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertOpnameDatumButton );

	deleteOpnameDatumButton.setActionCommand( "delete" );
	deleteOpnameDatumButton.setEnabled( false );
	deleteOpnameDatumButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteOpnameDatumButton );


	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 2;
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

	frame.setSize( 630, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
