package muziek.ensemble;

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
 * Frame to show, insert and update records in the ensemble table in schema muziek.
 * @author Chris van Engelen
 */
public class EditEnsemble extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditEnsemble.class.getCanonicalName() );

    private JTextField ensembleFilterTextField;

    private EnsembleTableModel ensembleTableModel;
    private TableSorter ensembleTableSorter;

    public EditEnsemble( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit ensemble", true, true, true, true);

        // Get the container from the internal frame
        final Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	final GridBagConstraints constraints = new GridBagConstraints( );

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Ensemble Filter:" ), constraints );

	ensembleFilterTextField = new JTextField( 20 );

        constraints.insets = new Insets( 20, 5, 5, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( ensembleFilterTextField, constraints );

	ensembleFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the ensemble table
            ensembleTableSorter.clearSortingState();
            ensembleTableModel.setupEnsembleTableModel( ensembleFilterTextField.getText( ) );
        } );

        ensembleFilterTextField.addFocusListener( new FocusListener() {
            public void focusLost(FocusEvent focusEven) {
                // Setup the ensemble table
                ensembleTableSorter.clearSortingState();
                ensembleTableModel.setupEnsembleTableModel( ensembleFilterTextField.getText( ) );
            }

            public void focusGained(FocusEvent focusEven) {}
        } );

	// Create ensemble table from title table model
	ensembleTableModel = new EnsembleTableModel( connection, parentFrame );
	ensembleTableSorter = new TableSorter( ensembleTableModel );
	final JTable ensembleTable = new JTable( ensembleTableSorter );
	ensembleTableSorter.setTableHeader( ensembleTable.getTableHeader( ) );
	// ensembleTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	ensembleTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	ensembleTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	ensembleTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // Id
	ensembleTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 400 );  // ensemble

	// Set vertical size just enough for 20 entries
	ensembleTable.setPreferredScrollableViewportSize( new Dimension( 450, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	container.add( new JScrollPane( ensembleTable ), constraints );


	////////////////////////////////////////////////
	// Add, Delete, Close Buttons
	////////////////////////////////////////////////

	// Define the delete button because it is used by the list selection listener
	final JButton deleteEnsembleButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel ensembleListSelectionModel = ensembleTable.getSelectionModel( );

	class EnsembleListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

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

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add ensembleListSelectionListener object to the selection model of the musici table
	final EnsembleListSelectionListener ensembleListSelectionListener = new EnsembleListSelectionListener( );
	ensembleListSelectionModel.addListSelectionListener( ensembleListSelectionListener );

	// Class to handle button actions: uses ensembleListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
                    return;
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
			final String insertString = "INSERT INTO ensemble SET ensemble_id = " + ensembleId;
			logger.fine( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in ensemble" );
			    return;
			}
		    } catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( parentFrame,
                                                       "SQL exception: " + sqlException.getMessage(),
                                                       "EditEnsemble SQL exception",
                                                       JOptionPane.ERROR_MESSAGE );
			logger.severe( "SQLException: " + sqlException.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = ensembleListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen ensemble geselecteerd",
						       "Edit ensemble error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected ensemble id
		    int selectedEnsembleId = ensembleTableModel.getEnsembleId( selectedRow );

		    // Check if ensemble has been selected
		    if ( selectedEnsembleId == 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen ensemble geselecteerd",
						       "Edit ensemble error",
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
				JOptionPane.showMessageDialog( parentFrame,
							       "Tabel musici_ensemble heeft nog verwijzing naar '" + ensembleString + "'",
							       "Edit ensemble error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in select: " + sqlException.getMessage(),
                                                           "EditEnsemble SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( parentFrame,
							   "Delete '" + ensembleString + "' ?",
							   "Delete ensemble record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString  = "DELETE FROM ensemble WHERE ensemble_id = " + selectedEnsembleId;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = "Could not delete record with ensemble_id  = " + selectedEnsembleId + " in ensemble";
				JOptionPane.showMessageDialog( parentFrame,
							       errorString,
							       "Edit ensemble error",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditEnsemble SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
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
