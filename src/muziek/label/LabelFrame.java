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
 * An instance of LabelFrame is created by class muziek.Main.
 *
 * @author Chris van Engelen
 */
public class LabelFrame {
    private final Logger logger = Logger.getLogger( LabelFrame.class.getCanonicalName() );

    private final JFrame frame = new JFrame( "Label" );

    private JTextField labelFilterTextField;

    private LabelTableModel labelTableModel;
    private TableSorter labelTableSorter;

    public LabelFrame( final Connection connection ) {

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );

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

	class LabelFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Setup the label table
		labelTableModel.setupLabelTableModel( labelFilterTextField.getText( ) );
	    }
	}
	labelFilterTextField.addActionListener( ( ActionEvent actionEvent ) -> {
            // Setup the label table
            labelTableSorter.clearSortingState();
            labelTableModel.setupLabelTableModel( labelFilterTextField.getText( ) );
        } );


	// Create label table from title table model
	labelTableModel = new LabelTableModel( connection );
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
		    frame.setVisible( false );
		    frame.dispose();
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
			String insertString = "INSERT INTO label SET label_id = " + labelId;

			logger.info( "insertString: " + insertString );
			if ( statement.executeUpdate( insertString ) != 1 ) {
			    logger.severe( "Could not insert in label" );
			    return;
			}
		    } catch ( SQLException ex ) {
			logger.severe( "SQLException: " + ex.getMessage( ) );
			return;
		    }
		} else {
		    int selectedRow = labelListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen label geselecteerd",
						       "Label frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected label id
		    int selectedLabelId = labelTableModel.getLabelId( selectedRow );

		    // Check if label has been selected
		    if ( selectedLabelId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen label geselecteerd",
						       "Label frame error",
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
			    ResultSet resultSet = statement.executeQuery( "SELECT label_id FROM medium" +
									  " WHERE label_id = " + selectedLabelId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel medium heeft nog verwijzing naar '" +
							       labelString + "'",
							       "Label frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + labelString + "' ?",
							   "Delete Label record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM label";
			deleteString += " WHERE label_id = " + selectedLabelId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with label_id  = " +
						       selectedLabelId + " in label" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete Label record",
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

	frame.setSize( 330, 500 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
