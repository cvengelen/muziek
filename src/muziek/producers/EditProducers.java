package muziek.producers;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.util.logging.*;

import muziek.gui.EditProducersDialog;
import muziek.gui.PersoonComboBox;
import table.*;

/**
 * Frame to show, insert and update records in the producers table in schema muziek.
 * @author Chris van Engelen
 */
public class EditProducers extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditProducers.class.getCanonicalName() );

    private final Connection connection;
    private final JFrame parentFrame;

    private JTextField producersFilterTextField;

    private PersoonComboBox persoonComboBox;
    private int selectedPersoonId = 0;

    private ProducersTableModel producersTableModel;
    private TableSorter producersTableSorter;

    private class Producers {
	int	id;
	String  string;

	Producers( int    id,
                   String string ) {
	    this.id = id;
	    this.string = string;
	}

	boolean presentInTable( String tableString ) {
	    // Check if producersId is present in table
	    try {
		Statement statement = connection.createStatement( );
		ResultSet resultSet = statement.executeQuery( "SELECT producers_id FROM " + tableString +
							      " WHERE producers_id = " + id );
		if ( resultSet.next( ) ) {
		    JOptionPane.showMessageDialog( parentFrame,
						   "Tabel " + tableString + " heeft nog verwijzing naar '" + string + "'",
						   "Edit producers error",
						   JOptionPane.ERROR_MESSAGE );
		    return true;
		}
	    } catch ( SQLException sqlException ) {
                JOptionPane.showMessageDialog( parentFrame,
                                               "SQL exception in select: " + sqlException.getMessage(),
                                               "EditProducers SQL exception",
                                               JOptionPane.ERROR_MESSAGE );
		logger.severe( "SQLException: " + sqlException.getMessage( ) );
		return true;
	    }
	    return false;
	}
    }

    public EditProducers( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit producers", true, true, true, true);

        this.connection = connection;
        this.parentFrame = parentFrame;

        // Get the container from the internal frame
        final Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
        constraints.gridwidth = 1;

	constraints.insets = new Insets( 20, 20, 5, 5 );
        constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Producers Filter:" ), constraints );

	producersFilterTextField = new JTextField( 30 );
        producersFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the producers table
            producersTableSorter.clearSortingState();
            producersTableModel.setupProducersTableModel( producersFilterTextField.getText( ),
                                                          selectedPersoonId );
        } );
        producersFilterTextField.addFocusListener( new FocusListener() {
            public void focusLost(FocusEvent focusEven) {
                // Setup the producers table
                producersTableSorter.clearSortingState();
                producersTableModel.setupProducersTableModel( producersFilterTextField.getText( ),
                                                              selectedPersoonId );
            }

            public void focusGained(FocusEvent focusEven) {}
        } );

        constraints.insets = new Insets( 20, 5, 5, 100 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( producersFilterTextField, constraints );
        constraints.weightx = 0d;
        constraints.fill = GridBagConstraints.NONE;

	////////////////////////////////////////////////
	// Persoon ComboBox
	////////////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 1;
        constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Persoon: " ), constraints );

	final JPanel persoonPanel = new JPanel( );
	final Border emptyBorder = new EmptyBorder( -5, -5, -5, -5 );
	persoonPanel.setBorder( emptyBorder );

	// Setup a JComboBox with the results of the query on persoon
	// Do not allow to enter new record in persoon
	persoonComboBox = new PersoonComboBox( connection, parentFrame, false );
        persoonComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected persoon ID from the combo box
            selectedPersoonId = persoonComboBox.getSelectedPersoonId( );

            // Setup the producers table
            producersTableSorter.clearSortingState();
            producersTableModel.setupProducersTableModel( producersFilterTextField.getText( ),
                    selectedPersoonId );
        } );
	persoonPanel.add( persoonComboBox );

	JButton filterPersoonButton = new JButton( "Filter" );
	filterPersoonButton.setActionCommand( "filterPersoon" );
        filterPersoonButton.addActionListener( ( ActionEvent ae ) -> persoonComboBox.filterPersoonComboBox( ) );
	persoonPanel.add( filterPersoonButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( persoonPanel, constraints );

	// Create producers table from title table model
	producersTableModel = new ProducersTableModel( connection, parentFrame );
	producersTableSorter = new TableSorter( producersTableModel );
	final JTable producersTable = new JTable( producersTableSorter );
	producersTableSorter.setTableHeader( producersTable.getTableHeader( ) );
	// producersTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	producersTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	producersTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	producersTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	producersTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 300 );  // producers
	producersTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 200 );  // persoon

	// Set vertical size just enough for 20 entries
	producersTable.setPreferredScrollableViewportSize( new Dimension( 550, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	container.add( new JScrollPane( producersTable ), constraints );
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;

	// Define the edit, delete button because it is used by the list selection listener
	final JButton editProducersButton = new JButton( "Edit" );
	final JButton deleteProducersButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel producersListSelectionModel = producersTable.getSelectionModel( );

	class ProducersListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

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

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add producersListSelectionListener object to the selection model of the musici table
	final ProducersListSelectionListener producersListSelectionListener = new ProducersListSelectionListener( );
	producersListSelectionModel.addListSelectionListener( producersListSelectionListener );

	// Class to handle button actions: uses producersListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
		    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new producers record
		    new EditProducersDialog( connection, parentFrame,
                                             producersFilterTextField.getText( ) );
		} else {
		    int selectedRow = producersListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen producers geselecteerd",
						       "Edit producers error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected producers id
		    int selectedProducersId = producersTableModel.getProducersId( selectedRow );

		    // Check if producers has been selected
		    if ( selectedProducersId == 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen producers geselecteerd",
						       "Edit producers error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
			// Do dialog
			new EditProducersDialog( connection, parentFrame, selectedProducersId );
		    }
                    else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			final Producers producers = new Producers( producersTableModel.getProducersId( selectedRow ),
								   producersTableModel.getProducersString( selectedRow ) );

			// Check if producers ID is still used
			if ( producers.presentInTable( "producers_persoon" ) ) return;
			if ( producers.presentInTable( "opname" ) ) return;

			// Replace null or empty string by single space for messages
			if ( ( producers.string == null ) || ( producers.string.length( ) == 0  ) ) {
			    producers.string = " ";
			}

			int result = JOptionPane.showConfirmDialog( parentFrame,
                                                                    "Delete '" + producers.string + "' ?",
                                                                    "Delete producers record",
                                                                    JOptionPane.YES_NO_OPTION,
                                                                    JOptionPane.QUESTION_MESSAGE,
                                                                    null );
			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString = "DELETE FROM producers WHERE producers_id = " + producers.id;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				final String errorString = "Could not delete record with producers_id  = " + producers.id + " in producers";
				JOptionPane.showMessageDialog( parentFrame,
							       errorString,
							       "Edit producers error",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditProducers SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
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

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 3;
	container.add( buttonPanel, constraints );

	setSize( 610, 550 );
        setLocation( x, y );
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}
