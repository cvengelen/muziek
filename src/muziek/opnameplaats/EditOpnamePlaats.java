package muziek.opnameplaats;

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
 * Frame to show, insert and update records in the opname_plaats table in schema muziek.
 * @author Chris van Engelen
 */
public class EditOpnamePlaats extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditOpnamePlaats.class.getCanonicalName() );

    private JTextField opnamePlaatsFilterTextField;

    private OpnamePlaatsTableModel opnamePlaatsTableModel;
    private TableSorter opnamePlaatsTableSorter;

    public EditOpnamePlaats( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit opname plaats", true, true, true, true);

        // Get the container from the internal frame
        final Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	final GridBagConstraints constraints = new GridBagConstraints( );

	/////////////////////////////////
	// Opname plaats filter string
	/////////////////////////////////

        constraints.insets = new Insets( 20, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opname Plaats Filter:" ), constraints );

	opnamePlaatsFilterTextField = new JTextField( 20 );
	opnamePlaatsFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the opnamePlaats table
            opnamePlaatsTableSorter.clearSortingState();
            opnamePlaatsTableModel.setupOpnamePlaatsTableModel( opnamePlaatsFilterTextField.getText( ) );
        } );
        opnamePlaatsFilterTextField.addFocusListener( new FocusListener() {
            public void focusLost(FocusEvent focusEven) {
                // Setup the opnamePlaats table
                opnamePlaatsTableSorter.clearSortingState();
                opnamePlaatsTableModel.setupOpnamePlaatsTableModel( opnamePlaatsFilterTextField.getText( ) );
            }

            public void focusGained(FocusEvent focusEven) {}
        } );

        constraints.insets = new Insets( 20, 5, 5, 100 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( opnamePlaatsFilterTextField, constraints );

	/////////////////////////////////
	// OpnamePlaats Table
	/////////////////////////////////

	// Create opnamePlaats table from title table model
	opnamePlaatsTableModel = new OpnamePlaatsTableModel( connection, parentFrame );
	opnamePlaatsTableSorter = new TableSorter( opnamePlaatsTableModel );
	final JTable opnamePlaatsTable = new JTable( opnamePlaatsTableSorter );
	opnamePlaatsTableSorter.setTableHeader( opnamePlaatsTable.getTableHeader( ) );
	// opnamePlaatsTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	opnamePlaatsTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	opnamePlaatsTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	opnamePlaatsTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	opnamePlaatsTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 400 );  // opname plaats

	// Set vertical size just enough for 20 entries
	opnamePlaatsTable.setPreferredScrollableViewportSize( new Dimension( 450, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	container.add( new JScrollPane( opnamePlaatsTable ), constraints );


	// Define the delete button because it is enabled/disabled by the list selection listener
	final JButton deleteOpnamePlaatsButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel opnamePlaatsListSelectionModel = opnamePlaatsTable.getSelectionModel( );

	class OpnamePlaatsListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

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

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add opnamePlaatsListSelectionListener object to the selection model of the musici table
	final OpnamePlaatsListSelectionListener opnamePlaatsListSelectionListener = new OpnamePlaatsListSelectionListener( );
	opnamePlaatsListSelectionModel.addListSelectionListener( opnamePlaatsListSelectionListener );

	// Class to handle button actions: uses opnamePlaatsListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
                    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( opname_plaats_id ) FROM opname_plaats" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for opname_plaats_id in opname_plaats" );
			    return;
			}

			int opnamePlaatsId = resultSet.getInt( 1 ) + 1;
			final String insertString = "INSERT INTO opname_plaats SET opname_plaats_id = " + opnamePlaatsId;
                        logger.fine( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in opname_plaats" );
			    return;
			}
		    } catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( parentFrame,
                                                       "SQL exception: " + sqlException.getMessage(),
                                                       "EditOpnamePlaats SQL exception",
                                                       JOptionPane.ERROR_MESSAGE );
			logger.severe( "SQLException: " + sqlException.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = opnamePlaatsListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen opname plaats geselecteerd",
						       "Edit opname plaats error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected opnamePlaats id
		    int selectedOpnamePlaatsId = opnamePlaatsTableModel.getOpnamePlaatsId( selectedRow );

		    // Check if opnamePlaats has been selected
		    if ( selectedOpnamePlaatsId == 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen opname plaats geselecteerd",
						       "Edit opname plaats error",
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
				statement.executeQuery( "SELECT opname_plaats_id FROM opname WHERE opname_plaats_id = " + selectedOpnamePlaatsId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( parentFrame,
							       "Tabel opname heeft nog verwijzing naar '" + opnamePlaatsString + "'",
							       "Edit opname plaats error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in select: " + sqlException.getMessage(),
                                                           "EditOpnamePlaats SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( parentFrame,
							   "Delete '" + opnamePlaatsString + "' ?",
							   "Delete opname plaats record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString = "DELETE FROM opname_plaats WHERE opname_plaats_id = " + selectedOpnamePlaatsId;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = "Could not delete record with opname_plaats_id  = " + selectedOpnamePlaatsId + " in opname_plaats";
				JOptionPane.showMessageDialog( parentFrame,
							       errorString,
							       "Edit opname plaats error",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditOpnamePlaats SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    } else {
                        JOptionPane.showMessageDialog( parentFrame,
                                                       "Unimplemented command: " + actionEvent.getActionCommand( ),
                                                       "Edit opname plaats error",
                                                       JOptionPane.ERROR_MESSAGE );
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

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 2;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;
	container.add( buttonPanel, constraints );

        setSize( 510, 500 );
        setLocation( x, y );
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}
