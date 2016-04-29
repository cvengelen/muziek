package muziek.rol;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.Logger;

import table.*;

/**
 * Frame to show, insert and update records in the rol table in schema muziek.
 * An instance of RolFrame is created by class muziek.Main.
 *
 * @author Chris van Engelen
 */
public class RolFrame {
    private final Logger logger = Logger.getLogger( RolFrame.class.getCanonicalName() );

    private final JFrame frame = new JFrame( "Rol");

    private JTextField rolFilterTextField;

    private RolTableModel rolTableModel;
    private TableSorter rolTableSorter;


    public RolFrame( final Connection connection ) {

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
        constraints.gridwidth = 1;

	/////////////////////////////////
	// rol filter string
	/////////////////////////////////

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Rol Filter:" ), constraints );

	rolFilterTextField = new JTextField( 15 );
	rolFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the rol table
            rolTableSorter.clearSortingState();
            rolTableModel.setupRolTableModel( rolFilterTextField.getText( ) );
        });

        constraints.insets = new Insets( 20, 5, 5, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( rolFilterTextField, constraints );

	/////////////////////////////////
	// Rol Table
	/////////////////////////////////

	// Create rol table from title table model
	rolTableModel = new RolTableModel( connection );
	rolTableSorter = new TableSorter( rolTableModel );
	final JTable rolTable = new JTable( rolTableSorter );
	rolTableSorter.setTableHeader( rolTable.getTableHeader( ) );
	// rolTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	rolTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	rolTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	rolTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	rolTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 200 );  // rol

	// Set vertical size just enough for 20 entries
	rolTable.setPreferredScrollableViewportSize( new Dimension( 250, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
        constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	container.add( new JScrollPane( rolTable ), constraints );

	// Define the delete button because it is enabled/disabled by the list selection listener
	final JButton deleteRolButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel rolListSelectionModel = rolTable.getSelectionModel( );

	class RolListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( rolListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteRolButton.setEnabled( false );
		    return;
		}

		int viewRow = rolListSelectionModel.getMinSelectionIndex( );
		selectedRow = rolTableSorter.modelIndex( viewRow );
		deleteRolButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add rolListSelectionListener object to the selection model of the musici table
	final RolListSelectionListener rolListSelectionListener = new RolListSelectionListener( );
	rolListSelectionModel.addListSelectionListener( rolListSelectionListener );

	// Class to handle button actions: uses rolListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
                    frame.dispose();
		    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( rol_id ) FROM rol" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for rol_id in rol" );
			    return;
			}
			int rolId = resultSet.getInt( 1 ) + 1;
			String insertString = "INSERT INTO rol SET rol_id = " + rolId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in rol" );
			    return;
			}
		    } catch ( SQLException ex ) {
			logger.severe( "SQLException: " + ex.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = rolListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen rol geselecteerd",
						       "Rol frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected rol id
		    int selectedRolId = rolTableModel.getRolId( selectedRow );

		    // Check if rol has been selected
		    if ( selectedRolId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen rol geselecteerd",
						       "Rol frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the rol
		    String rolString = rolTableModel.getRolString( selectedRow );

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if rol ID is still used
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT rol_id FROM musici_persoon WHERE rol_id = " +
							selectedRolId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel musici_persoon heeft nog verwijzing naar '" +
							       rolString + "'",
							       "Rol frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + rolString + "' ?",
							   "Delete Rol record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM rol";
			deleteString += " WHERE rol_id = " + selectedRolId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with rol_id  = " +
						       selectedRolId + " in rol" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Rol record",
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
		rolTableSorter.clearSortingState( );
		rolTableModel.setupRolTableModel( rolFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertRolButton = new JButton( "Insert" );
	insertRolButton.setActionCommand( "insert" );
	insertRolButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertRolButton );

	deleteRolButton.setActionCommand( "delete" );
	deleteRolButton.setEnabled( false );
	deleteRolButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteRolButton );

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

	frame.setSize( 310, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
