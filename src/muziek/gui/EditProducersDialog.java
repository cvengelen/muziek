// Dialog for inserting or updating producers and producers_persoon

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import java.util.logging.Logger;
import java.util.regex.*;


public class EditProducersDialog {
    final private Logger logger = Logger.getLogger( "muziek.gui.EditProducersDialog" );

    Connection connection;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    Object parentObject;

    JDialog dialog;

    int producersId;
    String producersString;
    JTextField producersTextField;

    PersoonComboBox persoonComboBox;
    String persoonFilterString = null;

    ProducersPersoonTableModel producersPersoonTableModel;
    JTable producersTable;

    int nUpdate = 0;
    final String insertProducersActionCommand = "insertProducers";
    final String updateProducersActionCommand = "updateProducers";

    // Pattern to find a single quote in producers, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public EditProducersDialog( Connection connection,
				Object     parentObject,
				String     producersString ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.producersString = producersString;

	// Setup producers_persoon table
	producersPersoonTableModel = new ProducersPersoonTableModel( connection );
	producersTable = new JTable( producersPersoonTableModel );

	if ( producersString == null ) producersString = "";
	setupProducersDialog( "Insert producers: " + producersString, "Insert",
			      insertProducersActionCommand );
    }

    // Constructor
    public EditProducersDialog( Connection connection,
				Object     parentObject,
				int        producersId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.producersId = producersId;

	// Setup producers_persoon table
	producersPersoonTableModel = new ProducersPersoonTableModel( connection, producersId );
	producersTable = new JTable( producersPersoonTableModel );

	try {
	    Statement statement = connection.createStatement();
	    ResultSet resultSet = statement.executeQuery( "SELECT producers FROM producers WHERE producers_id = " +
							  producersId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for producers_id " +
			       producersId + " in producers" );
		return;
	    }
	    producersString = resultSet.getString( 1 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupProducersDialog( "Edit producers: " + producersString, "Update",
			      updateProducersActionCommand );
    }

    // Setup producers dialog
    void setupProducersDialog( String dialogTitle,
			       String editProducersButtonText,
			       String editProducersButtonActionCommand ) {
	// Create modal dialog for editing producers
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    logger.severe( "Unexpected parent object class: " +
			   parentObject.getClass( ).getName( ) );
	    return;
	}

	// Set grid bag layout manager
	Container container = dialog.getContentPane();
	container.setLayout( new GridBagLayout( ) );

	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 5, 0, 5, 10 );


	////////////////////////////////////////////////
	// Producers text field
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Producers:" ), constraints );

	producersTextField = new JTextField( producersString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 4;
	container.add( producersTextField, constraints );


	////////////////////////////////////////////////
	// Persoon ComboBox, Add, Find Persoon buttons
	////////////////////////////////////////////////

	// Setup a JComboBox with the results of the query on persoon
	persoonComboBox = new PersoonComboBox( connection, dialog );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Persoon:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( persoonComboBox, constraints );

	class SelectPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a persoon record needs to be inserted
		if ( persoonComboBox.newPersoonSelected( ) ) {
		    // Insert new persoon record
		    EditPersoonDialog editPersoonDialog =
			new EditPersoonDialog( connection, parentObject, persoonFilterString );

		    // Check if a new persoon record has been inserted
		    if ( editPersoonDialog.persoonUpdated( ) ) {
			// Get the id of the new persoon record
			int selectedPersoonId = editPersoonDialog.getPersoonId( );

			// Setup the persoon combo box again
			persoonComboBox.setupPersoonComboBox( selectedPersoonId );
		    }
		}
	    }
	}
	persoonComboBox.addActionListener( new SelectPersoonActionListener( ) );

	JButton addPersoonInTableButton = new JButton( "Add" );
	addPersoonInTableButton.setActionCommand( "addPersoonInTable" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( addPersoonInTableButton, constraints );

	class AddPersoonInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Producers error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected row in table
		producersPersoonTableModel.addRow( selectedPersoonId,
						   persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	addPersoonInTableButton.addActionListener( new AddPersoonInTableActionListener( ) );

	JButton filterPersoonButton = new JButton( "Filter" );
	filterPersoonButton.setActionCommand( "filterPersoon" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterPersoonButton, constraints );

	class FilterPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		persoonFilterString = persoonComboBox.filterPersoonComboBox( );
	    }
	}
	filterPersoonButton.addActionListener( new FilterPersoonActionListener( ) );


	////////////////////////////////////////////////
	// Producers Table, Replace, Remove Buttons
	////////////////////////////////////////////////

	// Setup a table with producers_persoon
	producersTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	producersTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	producersTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 300 );

	// Set vertical size just enough for 5 entries
	producersTable.setPreferredScrollableViewportSize( new Dimension( 300, 80 ) );

	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	// Set gridheight to allow for two buttons next to the table
	constraints.gridheight = 2;
	container.add( new JScrollPane( producersTable ), constraints );

	// Define Replace button next to table
	final JButton replacePersoonInTableButton = new JButton( "Replace" );
	replacePersoonInTableButton.setActionCommand( "replacePersoonInTable" );
	replacePersoonInTableButton.setEnabled( false );
	constraints.gridheight = 1;
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( replacePersoonInTableButton, constraints );

	// Define Remove button next to table
	final JButton removePersoonFromTableButton = new JButton( "Remove" );
	removePersoonFromTableButton.setActionCommand( "removePersoonFromTable" );
	removePersoonFromTableButton.setEnabled( false );
	constraints.gridy = 3;
	container.add( removePersoonFromTableButton, constraints );


	// Get the selection model related to the producers table
	final ListSelectionModel persoonListSelectionModel = producersTable.getSelectionModel( );

	// Class to handle row selection in producers table
	class PersoonListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( persoonListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    replacePersoonInTableButton.setEnabled( false );
		    removePersoonFromTableButton.setEnabled( false );
		    return;
		}

		selectedRow = persoonListSelectionModel.getMinSelectionIndex( );
		replacePersoonInTableButton.setEnabled( true );
		removePersoonFromTableButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add persoonListSelectionListener object to the selection model of the producers table
	final PersoonListSelectionListener persoonListSelectionListener = new PersoonListSelectionListener( );
	persoonListSelectionModel.addListSelectionListener( persoonListSelectionListener );


	// Class to handle Replace button: uses persoonListSelectionListener
	class ReplacePersoonInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Producers error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Producers error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Replace selected row in table
		producersPersoonTableModel.replaceRow( selectedRow,
						       selectedPersoonId,
						       persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	replacePersoonInTableButton.addActionListener( new ReplacePersoonInTableActionListener( ) );


	// Class to handle Remove button: uses persoonListSelectionListener
	class RemovePersoonFromTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Producers error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		producersPersoonTableModel.removeRow( selectedRow );
	    }
	}
	removePersoonFromTableButton.addActionListener( new RemovePersoonFromTableActionListener( ) );


	////////////////////////////////////////////////
	// Update/Insert, Cancel Buttons
	////////////////////////////////////////////////

	JPanel buttonPanel = new JPanel( );

	class EditProducersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertProducersActionCommand ) ) {
		    insertProducers( );
		} else if ( ae.getActionCommand( ).equals( updateProducersActionCommand ) ) {
		    updateProducers( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editProducersButton = new JButton( editProducersButtonText );
	editProducersButton.setActionCommand( editProducersButtonActionCommand );
	editProducersButton.addActionListener( new EditProducersActionListener( ) );
	buttonPanel.add( editProducersButton );

	JButton cancelProducersButton = new JButton( "Cancel" );
	cancelProducersButton.setActionCommand( "cancelProducers" );
	cancelProducersButton.addActionListener( new EditProducersActionListener( ) );
	buttonPanel.add( cancelProducersButton );

	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 5;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	dialog.setSize( 600, 300 );
	dialog.setVisible( true );
    }

    void insertProducers( ) {
	producersString = producersTextField.getText( );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( producers_id ) FROM producers" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for producers_id in producers" );
		dialog.setVisible( false );
		return;
	    }
	    producersId = resultSet.getInt( 1 ) + 1;

	    // Matcher to find single quotes in producersString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( producersString );

	    // Insert a new row in the producers table (which really is just a label)
	    nUpdate = statement.executeUpdate( "INSERT INTO producers SET " +
					       "producers_id = " + producersId +
					       ",  producers = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in producers" );
		dialog.setVisible( false );
		return;
	    }

	    // Insert the rows in the producers_persoon table using the new producersId
	    producersPersoonTableModel.insertTable( producersId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updateProducers( ) {
	producersString = producersTextField.getText( );

	// Matcher to find single quotes in producersString, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	final Matcher quoteMatcher = quotePattern.matcher( producersString );

	try {
	    Statement statement = connection.createStatement();

	    // Update the producers table (which really is just a label)
	    nUpdate = statement.executeUpdate( "UPDATE producers SET producers = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) +
					       "' WHERE producers_id = " + producersId );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in producers" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Update the rows in the producers_persoon table
	producersPersoonTableModel.updateTable( );
    }

    public boolean producersUpdated( ) { return nUpdate > 0; }

    public String getProducersString( ) { return producersString; }

    public int getProducersId( ) { return producersId; }
}
