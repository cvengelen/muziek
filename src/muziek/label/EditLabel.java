package muziek.label;

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
 * Frame to show, insert and update records in the label table in schema muziek.
 * @author Chris van Engelen
 */
public class EditLabel extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditLabel.class.getCanonicalName() );

    private JTextField labelFilterTextField;

    private LabelTableModel labelTableModel;
    private TableSorter labelTableSorter;

    public EditLabel( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit label", true, true, true, true);

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
	container.add( new JLabel( "Label Filter:" ), constraints );
	labelFilterTextField = new JTextField( 12 );

        constraints.insets = new Insets( 20, 5, 5, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
	container.add( labelFilterTextField, constraints );

	labelFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the label table
            labelTableSorter.clearSortingState();
            labelTableModel.setupLabelTableModel( labelFilterTextField.getText( ) );
        } );

        labelFilterTextField.addFocusListener( new FocusListener() {
            public void focusLost(FocusEvent focusEven) {
                // Setup the label table
                labelTableSorter.clearSortingState();
                labelTableModel.setupLabelTableModel( labelFilterTextField.getText( ) );
            }

            public void focusGained(FocusEvent focusEven) {}
        } );

	// Create label table from title table model
	labelTableModel = new LabelTableModel( connection, parentFrame );
	labelTableSorter = new TableSorter( labelTableModel );
	final JTable labelTable = new JTable( labelTableSorter );
	labelTableSorter.setTableHeader( labelTable.getTableHeader( ) );
	// labelTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	labelTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	labelTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	labelTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // Id
	labelTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 220 );  // label

	// Set vertical size just enough for 20 entries
	labelTable.setPreferredScrollableViewportSize( new Dimension( 270, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
        container.add( new JScrollPane( labelTable ), constraints );


	// Define the delete button because it is used by the list selection listener
	final JButton deleteLabelButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel labelListSelectionModel = labelTable.getSelectionModel( );

	class LabelListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( labelListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    deleteLabelButton.setEnabled( false );
		    return;
		}

		int viewRow = labelListSelectionModel.getMinSelectionIndex( );
		selectedRow = labelTableSorter.modelIndex( viewRow );
		deleteLabelButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add labelListSelectionListener object to the selection model of the musici table
	final LabelListSelectionListener labelListSelectionListener = new LabelListSelectionListener( );
	labelListSelectionModel.addListSelectionListener( labelListSelectionListener );

	// Class to handle button actions: uses labelListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
		    dispose();
                    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new label record
		    try {
			Statement statement = connection.createStatement( );
			ResultSet resultSet = statement.executeQuery( "SELECT MAX( label_id ) FROM label" );
			if ( ! resultSet.next( ) ) {
			    logger.severe( "Could not get maximum for label_id in label" );
			    return;
			}

			int labelId = resultSet.getInt( 1 ) + 1;
			final String insertString = "INSERT INTO label SET label_id = " + labelId;
                        logger.fine( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in label" );
			    return;
			}
		    } catch ( SQLException sqlException ) {
                        JOptionPane.showMessageDialog( parentFrame,
                                                       "SQL exception: " + sqlException.getMessage(),
                                                       "EditLabel SQL exception",
                                                       JOptionPane.ERROR_MESSAGE );
			logger.severe( "SQLException: " + sqlException.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = labelListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen label geselecteerd",
						       "Edit label error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected label id
		    int selectedLabelId = labelTableModel.getLabelId( selectedRow );

		    // Check if label has been selected
		    if ( selectedLabelId == 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen label geselecteerd",
						       "Edit label error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    String labelString = labelTableModel.getLabelString( selectedRow );
		    // Replace null or empty string by single space for messages
		    if ( ( labelString == null ) || ( labelString.length( ) == 0  ) ) {
			labelString = " ";
		    }

		    if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			// Check if labelId is present in table musici_label
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet = statement.executeQuery( "SELECT label_id FROM medium" + " WHERE label_id = " + selectedLabelId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( parentFrame,
							       "Tabel medium heeft nog verwijzing naar '" + labelString + "'",
							       "Edit label error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in select: " + sqlException.getMessage(),
                                                           "EditLabel SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( parentFrame,
							   "Delete '" + labelString + "' ?",
							   "Delete Label record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM label WHERE label_id = " + selectedLabelId;
			logger.fine( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = "Could not delete record with label_id  = " + selectedLabelId + " in label";
				JOptionPane.showMessageDialog( parentFrame,
							       errorString,
							       "Delete Label record",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditLabel SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    }
		}

		// Records may have been modified: setup the table model again
		labelTableSorter.clearSortingState( );
		labelTableModel.setupLabelTableModel( labelFilterTextField.getText( ) );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertLabelButton = new JButton( "Insert" );
	insertLabelButton.setActionCommand( "insert" );
	insertLabelButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertLabelButton );

	deleteLabelButton.setActionCommand( "delete" );
	deleteLabelButton.setEnabled( false );
	deleteLabelButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteLabelButton );

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

	setSize( 330, 500 );
        setLocation( x, y );
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}
