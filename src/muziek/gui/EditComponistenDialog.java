// Dialog for inserting or updating componisten, componisten_persoon and tekstschrijver_persoon

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class EditComponistenDialog {
    final private Logger logger = Logger.getLogger( "muziek.gui.EditComponistenDialog" );

    Connection connection;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    Object parentObject;

    JDialog dialog;

    int componistenId;
    String componistenString;
    JTextField componistenTextField;

    PersoonComboBox persoonComboBox;
    String persoonFilterString = null;

    ComponistenPersoonTableModel componistenPersoonTableModel;
    JTable componistenPersoonTable;

    TekstschrijverPersoonTableModel tekstschrijverPersoonTableModel;
    JTable tekstschrijverPersoonTable;

    int nUpdate = 0;
    final String insertComponistenActionCommand = "insertComponisten";
    final String updateComponistenActionCommand = "updateComponisten";

    // Pattern to find a single quote in componisten, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public EditComponistenDialog( Connection connection,
				  Object     parentObject,
				  String     componistenString ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.componistenString = componistenString;

	// Setup componisten_persoon table
	componistenPersoonTableModel = new ComponistenPersoonTableModel( connection );
	componistenPersoonTable = new JTable( componistenPersoonTableModel );

	// Setup tekstschrijver_persoon table
	tekstschrijverPersoonTableModel = new TekstschrijverPersoonTableModel( connection );
	tekstschrijverPersoonTable = new JTable( tekstschrijverPersoonTableModel );

	if ( componistenString == null ) componistenString = "";
	setupComponistenDialog( "Insert componisten: " + componistenString, "Insert",
				insertComponistenActionCommand );
    }

    // Constructor
    public EditComponistenDialog( Connection connection,
				  Object     parentObject,
				  int        componistenId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.componistenId = componistenId;

	// Setup componisten_persoon table
	componistenPersoonTableModel = new ComponistenPersoonTableModel( connection, componistenId );
	componistenPersoonTable = new JTable( componistenPersoonTableModel );

	// Setup tekstschrijver_persoon table
	tekstschrijverPersoonTableModel = new TekstschrijverPersoonTableModel( connection, componistenId );
	tekstschrijverPersoonTable = new JTable( tekstschrijverPersoonTableModel );

	try {
	    Statement statement = connection.createStatement();
	    ResultSet resultSet = statement.executeQuery( "SELECT componisten FROM componisten WHERE componisten_id = " +
							  componistenId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for componisten_id " +
			       componistenId + " in componisten" );
		return;
	    }
	    componistenString = resultSet.getString( 1 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupComponistenDialog( "Edit componisten: " + componistenString, "Update",
				updateComponistenActionCommand );
    }

    // Setup componisten dialog
    void setupComponistenDialog( String dialogTitle,
				 String editComponistenButtonText,
				 String editComponistenButtonActionCommand ) {
	// Create modal dialog for editing componisten
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
	constraints.insets = new Insets( 5, 5, 5, 5 );


	////////////////////////////////////////////////
	// Componisten text field
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	container.add( new JLabel( "Componisten:" ), constraints );

	componistenTextField = new JTextField( componistenString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 4;
	container.add( componistenTextField, constraints );


	////////////////////////////////////////////////
	// Persoon ComboBox, Add, Filter Persoon button
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
	// Componist-Persoon Table, Add, Replace, Remove Buttons
	////////////////////////////////////////////////

	// Setup a table with componisten_persoon
	componistenPersoonTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	componistenPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	componistenPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 300 );

	// Set vertical size just enough for 5 entries
	componistenPersoonTable.setPreferredScrollableViewportSize( new Dimension( 300, 80 ) );

	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	// Set gridheight to allow for three buttons next to the table
	constraints.gridheight = 3;
	container.add( new JScrollPane( componistenPersoonTable ), constraints );

	// Define Add button next to table
	JButton addPersoonInComponistTableButton = new JButton( "Add" );
	addPersoonInComponistTableButton.setActionCommand( "addPersoonInComponistTable" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridheight = 1;
	container.add( addPersoonInComponistTableButton, constraints );

	// Define Replace button next to table
	final JButton replacePersoonInComponistTableButton = new JButton( "Replace" );
	replacePersoonInComponistTableButton.setActionCommand( "replacePersoonInComponistTable" );
	replacePersoonInComponistTableButton.setEnabled( false );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridy = 3;
	container.add( replacePersoonInComponistTableButton, constraints );

	// Define Remove button next to table
	final JButton removePersoonFromComponistTableButton = new JButton( "Remove" );
	removePersoonFromComponistTableButton.setActionCommand( "removePersoonFromComponistTable" );
	removePersoonFromComponistTableButton.setEnabled( false );
	constraints.gridy = 4;
	container.add( removePersoonFromComponistTableButton, constraints );


	// Class to handle Add button
	class AddPersoonInComponistTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Componisten error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected row in table
		componistenPersoonTableModel.addRow( selectedPersoonId,
						     persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	addPersoonInComponistTableButton.addActionListener( new AddPersoonInComponistTableActionListener( ) );


	// Get the selection model related to the componisten table
	final ListSelectionModel persoonListSelectionModel = componistenPersoonTable.getSelectionModel( );

	// Class to handle row selection in componisten table
	class PersoonListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( persoonListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    replacePersoonInComponistTableButton.setEnabled( false );
		    removePersoonFromComponistTableButton.setEnabled( false );
		    return;
		}

		selectedRow = persoonListSelectionModel.getMinSelectionIndex( );
		replacePersoonInComponistTableButton.setEnabled( true );
		removePersoonFromComponistTableButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add persoonListSelectionListener object to the selection model of the componisten table
	final PersoonListSelectionListener persoonListSelectionListener = new PersoonListSelectionListener( );
	persoonListSelectionModel.addListSelectionListener( persoonListSelectionListener );


	// Class to handle Replace button: uses persoonListSelectionListener
	class ReplacePersoonInComponistTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Componisten error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Componisten error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Replace selected row in table
		componistenPersoonTableModel.replaceRow( selectedRow,
							 selectedPersoonId,
							 persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	replacePersoonInComponistTableButton.addActionListener( new ReplacePersoonInComponistTableActionListener( ) );


	// Class to handle Remove button: uses persoonListSelectionListener
	class RemovePersoonFromComponistTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Componisten error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		componistenPersoonTableModel.removeRow( selectedRow );
	    }
	}
	removePersoonFromComponistTableButton.addActionListener( new RemovePersoonFromComponistTableActionListener( ) );


	////////////////////////////////////////////////
	// Tekstschrijver-Persoon Table, Add, Replace, Remove Buttons
	////////////////////////////////////////////////

	// Setup a table with tekstschrijver_persoon
	tekstschrijverPersoonTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	tekstschrijverPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	tekstschrijverPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 300 );

	// Set vertical size just enough for 5 entries
	tekstschrijverPersoonTable.setPreferredScrollableViewportSize( new Dimension( 300, 80 ) );

	constraints.gridx = 1;
	constraints.gridy = 5;
	constraints.gridwidth = 1;
	// Set gridheight to allow for three buttons next to the table
	constraints.gridheight = 3;
	container.add( new JScrollPane( tekstschrijverPersoonTable ), constraints );

	// Define Add button next to table
	JButton addPersoonInTekstschrijverTableButton = new JButton( "Add" );
	addPersoonInTekstschrijverTableButton.setActionCommand( "addPersoonInTekstschrijverTable" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridheight = 1;
	container.add( addPersoonInTekstschrijverTableButton, constraints );

	// Define Replace button next to table
	final JButton replacePersoonInTekstschrijverTableButton = new JButton( "Replace" );
	replacePersoonInTekstschrijverTableButton.setActionCommand( "replacePersoonInTekstschrijverTable" );
	replacePersoonInTekstschrijverTableButton.setEnabled( false );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridy = 6;
	container.add( replacePersoonInTekstschrijverTableButton, constraints );

	// Define Remove button next to table
	final JButton removePersoonFromTektstschrijverTableButton = new JButton( "Remove" );
	removePersoonFromTektstschrijverTableButton.setActionCommand( "removePersoonFromTektstschrijverTable" );
	removePersoonFromTektstschrijverTableButton.setEnabled( false );
	constraints.gridy = 7;
	container.add( removePersoonFromTektstschrijverTableButton, constraints );


	// Class to handle Add button
	class AddPersoonInTekstschrijverTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Componisten error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected row in table
		tekstschrijverPersoonTableModel.addRow( selectedPersoonId,
							persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	addPersoonInTekstschrijverTableButton.addActionListener( new AddPersoonInTekstschrijverTableActionListener( ) );


	// Get the selection model related to the tekstschrijver table
	final ListSelectionModel tekstschrijverListSelectionModel = tekstschrijverPersoonTable.getSelectionModel( );

	// Class to handle row selection in componisten table
	class TekstschrijverListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( tekstschrijverListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    replacePersoonInTekstschrijverTableButton.setEnabled( false );
		    removePersoonFromTektstschrijverTableButton.setEnabled( false );
		    return;
		}

		selectedRow = tekstschrijverListSelectionModel.getMinSelectionIndex( );
		replacePersoonInTekstschrijverTableButton.setEnabled( true );
		removePersoonFromTektstschrijverTableButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add tekstschrijverListSelectionListener object to the selection model of the componisten table
	final TekstschrijverListSelectionListener tekstschrijverListSelectionListener = new TekstschrijverListSelectionListener( );
	tekstschrijverListSelectionModel.addListSelectionListener( tekstschrijverListSelectionListener );


	// Class to handle Replace button: uses tekstschrijverListSelectionListener
	class ReplacePersoonInTekstschrijverTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = tekstschrijverListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Componisten error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Componisten error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Replace selected row in table
		tekstschrijverPersoonTableModel.replaceRow( selectedRow,
							    selectedPersoonId,
							    persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	replacePersoonInTekstschrijverTableButton.addActionListener( new ReplacePersoonInTekstschrijverTableActionListener( ) );


	// Class to handle Remove button: uses tekstschrijverListSelectionListener
	class RemovePersoonFromTektstschrijverTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = tekstschrijverListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Componisten error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		tekstschrijverPersoonTableModel.removeRow( selectedRow );
	    }
	}
	removePersoonFromTektstschrijverTableButton.addActionListener( new RemovePersoonFromTektstschrijverTableActionListener( ) );


	////////////////////////////////////////////////
	// Update/Insert, Cancel Buttons
	////////////////////////////////////////////////

	JPanel buttonPanel = new JPanel( );

	class EditComponistenActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertComponistenActionCommand ) ) {
		    insertComponisten( );
		} else if ( ae.getActionCommand( ).equals( updateComponistenActionCommand ) ) {
		    updateComponisten( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editComponistenButton = new JButton( editComponistenButtonText );
	editComponistenButton.setActionCommand( editComponistenButtonActionCommand );
	editComponistenButton.addActionListener( new EditComponistenActionListener( ) );
	buttonPanel.add( editComponistenButton );

	JButton cancelComponistenButton = new JButton( "Cancel" );
	cancelComponistenButton.setActionCommand( "cancelComponisten" );
	cancelComponistenButton.addActionListener( new EditComponistenActionListener( ) );
	buttonPanel.add( cancelComponistenButton );

	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.gridwidth = 5;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	dialog.setSize( 620, 600 );
	dialog.setVisible( true );
    }

    void insertComponisten( ) {
	componistenString = componistenTextField.getText( );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( componisten_id ) FROM componisten" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for componisten_id in componisten" );
		dialog.setVisible( false );
		return;
	    }
	    componistenId = resultSet.getInt( 1 ) + 1;

	    // Matcher to find single quotes in componistenString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( componistenString );

	    // Insert a new row in the componisten table (which really is just a label)
	    nUpdate = statement.executeUpdate( "INSERT INTO componisten SET " +
					       "componisten_id = " + componistenId +
					       ",  componisten = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in componisten" );
		dialog.setVisible( false );
		return;
	    }

	    // Insert the rows in the componisten_persoon table using the new componisten ID
	    componistenPersoonTableModel.insertTable( componistenId );

	    // Insert the rows in the tekstschrijver_persoon table using the new componisten ID
	    tekstschrijverPersoonTableModel.insertTable( componistenId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updateComponisten( ) {
	componistenString = componistenTextField.getText( );

	// Matcher to find single quotes in componistenString, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	final Matcher quoteMatcher = quotePattern.matcher( componistenString );

	try {
	    Statement statement = connection.createStatement();

	    // Update the componisten table (which really is just a label)
	    nUpdate = statement.executeUpdate( "UPDATE componisten SET componisten = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) +
					       "' WHERE componisten_id = " + componistenId );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in componisten" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Update the rows in the componisten_persoon table
	componistenPersoonTableModel.updateTable( );

	// Update the rows in the tekstschrijver_persoon table
	tekstschrijverPersoonTableModel.updateTable( );
    }

    public boolean componistenUpdated( ) { return nUpdate > 0; }

    public String getComponisten( ) { return componistenString; }

    public int getComponistenId( ) { return componistenId; }
}
