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
 * @author Chris van Engelen
 */
public class EditOpnameDatum extends JInternalFrame {
        private final Logger logger = Logger.getLogger( EditOpnameDatum.class.getCanonicalName() );

    private JTextField opnameDatumFilterTextField;

    private OpnameDatumTableModel opnameDatumTableModel;
    private TableSorter opnameDatumTableSorter;

    public EditOpnameDatum( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit opname datum", true, true, true, true);

        // Get the container from the internal frame
        final Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	final GridBagConstraints constraints = new GridBagConstraints( );

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
	opnameDatumFilterTextField.addFocusListener( new FocusListener() {
            public void focusLost(FocusEvent focusEven) {
                // Setup the opnameDatum table
                opnameDatumTableSorter.clearSortingState();
                opnameDatumTableModel.setupOpnameDatumTableModel( opnameDatumFilterTextField.getText( ) );
            }

            public void focusGained(FocusEvent focusEven) {}
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
	opnameDatumTableModel = new OpnameDatumTableModel( connection, parentFrame );
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
		    setVisible( false );
                    dispose();
                    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new opnameDatum record
		    new EditOpnameDatumDialog( connection, parentFrame,
                                               opnameDatumFilterTextField.getText( ) );

		    // Records may have been modified: setup the table model again
		    opnameDatumTableModel.setupOpnameDatumTableModel( opnameDatumFilterTextField.getText( ) );
		} else {
		    int selectedRow = opnameDatumListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen opnameDatum geselecteerd",
						       "Edit opname datum error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected opnameDatum id
		    int selectedOpnameDatumId = opnameDatumTableModel.getOpnameDatumId( selectedRow );

		    // Check if opnameDatum has been selected
		    if ( selectedOpnameDatumId == 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen opnameDatum geselecteerd",
						       "Edit opname datum error",
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
				statement.executeQuery( "SELECT opname_datum_id FROM opname WHERE opname_datum_id = " + selectedOpnameDatumId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( parentFrame,
							       "Tabel opname heeft nog verwijzing naar '" + opnameDatumString + "'",
							       "Edit opname datum error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in select: " + sqlException.getMessage(),
                                                           "EditOpnameDatum SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( parentFrame,
							   "Delete '" + opnameDatumString + "' ?",
							   "Delete opname datum record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString = "DELETE FROM opname_datum WHERE opname_datum_id = " + selectedOpnameDatumId;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = "Could not delete record with opname_datum_id  = " + selectedOpnameDatumId + " in opname_datum";
				JOptionPane.showMessageDialog( parentFrame,
							       errorString,
							       "Edit opname datum error",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditOpnameDatum SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    } else {
                        JOptionPane.showMessageDialog( parentFrame,
                                                       "Unimplemented command: " + actionEvent.getActionCommand( ),
                                                       "Edit opname datum error",
                                                       JOptionPane.ERROR_MESSAGE );
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

	setSize( 630, 500 );
        setLocation( x, y );
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}
