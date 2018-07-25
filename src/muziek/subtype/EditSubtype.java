package muziek.subtype;

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
 * Frame to show, insert and update records in the subtype table in schema muziek.
 * @author Chris van Engelen
 */
public class EditSubtype extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditSubtype.class.getCanonicalName() );

    private JTextField subtypeFilterTextField;

    private SubtypeTableModel subtypeTableModel;
    private TableSorter subtypeTableSorter;

    public EditSubtype( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit subtype", true, true, true, true);

        // Get the container from the internal frame
        final Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
        constraints.gridwidth = 1;

	/////////////////////////////////
	// Subtype filter string
	/////////////////////////////////

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Subtype Filter:" ), constraints );

	subtypeFilterTextField = new JTextField( 15 );
	subtypeFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the subtype table
            subtypeTableSorter.clearSortingState();
            subtypeTableModel.setupSubtypeTableModel( subtypeFilterTextField.getText( ) );
        } );
        subtypeFilterTextField.addFocusListener( new FocusListener() {
            public void focusLost(FocusEvent focusEven) {
                // Setup the subtype table
                subtypeTableSorter.clearSortingState();
                subtypeTableModel.setupSubtypeTableModel( subtypeFilterTextField.getText( ) );
            }

            public void focusGained(FocusEvent focusEven) {}
        } );

        constraints.insets = new Insets( 20, 5, 5, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( subtypeFilterTextField, constraints );

	/////////////////////////////////
	// Subtype Table
	/////////////////////////////////

	// Create subtype table from title table model
	subtypeTableModel = new SubtypeTableModel( connection, parentFrame );
	subtypeTableSorter = new TableSorter( subtypeTableModel );
	final JTable subtypeTable = new JTable( subtypeTableSorter );
	subtypeTableSorter.setTableHeader( subtypeTable.getTableHeader( ) );
	// subtypeTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	subtypeTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	subtypeTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	subtypeTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	subtypeTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 300 );  // subtype

	// Set vertical size just enough for 20 entries
	subtypeTable.setPreferredScrollableViewportSize( new Dimension( 350, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
        constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	container.add( new JScrollPane( subtypeTable ), constraints );

	// Define the delete button because it is enabled/disabled by the list selection listener
	final JButton deleteSubtypeButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel subtypeListSelectionModel = subtypeTable.getSelectionModel( );

	class SubtypeListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( subtypeListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteSubtypeButton.setEnabled( false );
		    return;
		}

		int viewRow = subtypeListSelectionModel.getMinSelectionIndex( );
		selectedRow = subtypeTableSorter.modelIndex( viewRow );
		deleteSubtypeButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add subtypeListSelectionListener object to the selection model of the musici table
	final SubtypeListSelectionListener subtypeListSelectionListener = new SubtypeListSelectionListener( );
	subtypeListSelectionModel.addListSelectionListener( subtypeListSelectionListener );

	// Class to handle button actions: uses subtypeListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
		    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( subtype_id ) FROM subtype" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for subtype_id in subtype" );
			    return;
			}

			int subtypeId = resultSet.getInt( 1 ) + 1;
			final String insertString = "INSERT INTO subtype SET subtype_id = " + subtypeId;
                        logger.fine( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in subtype" );
			    return;
			}
		    } catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( parentFrame,
                                                       "SQL exception: " + sqlException.getMessage(),
                                                       "EditSubtype SQL exception",
                                                       JOptionPane.ERROR_MESSAGE );
			logger.severe( "SQLException: " + sqlException.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = subtypeListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen subtype geselecteerd",
						       "Edit subtype error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected subtype id
		    int selectedSubtypeId = subtypeTableModel.getSubtypeId( selectedRow );

		    // Check if subtype has been selected
		    if ( selectedSubtypeId == 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen subtype geselecteerd",
						       "Edit subtype error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the subtype
		    String subtypeString = subtypeTableModel.getSubtypeString( selectedRow );

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if subtype ID is still used
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT subtype_id FROM opus WHERE subtype_id = " + selectedSubtypeId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( parentFrame,
							       "Tabel opus heeft nog verwijzing naar '" +
							       subtypeString + "'",
							       "Edit subtype error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in select: " + sqlException.getMessage(),
                                                           "EditSubtype SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( parentFrame,
							   "Delete '" + subtypeString + "' ?",
							   "Delete subtype record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			final String deleteString = "DELETE FROM subtype WHERE subtype_id = " + selectedSubtypeId;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				final String errorString = "Could not delete record with subtype_id  = " + selectedSubtypeId + " in subtype";
				JOptionPane.showMessageDialog( parentFrame,
							       errorString,
							       "Edit subtype error",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditSubtype SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    } else {
			logger.severe( "Unimplemented command: " + actionEvent.getActionCommand( ) );
		    }
		}

		// Records may have been modified: setup the table model again
		subtypeTableSorter.clearSortingState( );
		subtypeTableModel.setupSubtypeTableModel( subtypeFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertSubtypeButton = new JButton( "Insert" );
	insertSubtypeButton.setActionCommand( "insert" );
	insertSubtypeButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertSubtypeButton );

	deleteSubtypeButton.setActionCommand( "delete" );
	deleteSubtypeButton.setEnabled( false );
	deleteSubtypeButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteSubtypeButton );

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

	setSize( 410, 500 );
        setLocation( x, y );
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}
