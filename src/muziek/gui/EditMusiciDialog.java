// Dialog for inserting or updating musici, musici_persoon and musici_ensemble

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.regex.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class EditMusiciDialog {
    Connection connection;

    // The parent can be either a JFrame or a JDialog:
    // No common ancestor other than Object, so store as Object
    Object parentObject;

    JDialog dialog;

    int musiciId = 0;
    String musiciString;
    JTextField musiciTextField;

    PersoonComboBox persoonComboBox;
    String persoonFilterString = null;

    MusiciPersoonTableModel musiciPersoonTableModel;
    JTable musiciPersoonTable;

    EnsembleComboBox ensembleComboBox;
    String ensembleFilterString = null;

    MusiciEnsembleTableModel musiciEnsembleTableModel;
    JTable musiciEnsembleTable;

    int nUpdate = 0;
    final String insertMusiciActionCommand = "insertMusici";
    final String updateMusiciActionCommand = "updateMusici";

    // Pattern to find a single quote in the opus titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );

    // Constructor
    public EditMusiciDialog( Connection connection,
			     Object     parentObject,
			     String     musiciString ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.musiciString = musiciString;

	// Setup musici_persoon table
	musiciPersoonTableModel = new MusiciPersoonTableModel( connection );
	musiciPersoonTable = new JTable( musiciPersoonTableModel );

	// Setup musici_ensemble table
	musiciEnsembleTableModel = new MusiciEnsembleTableModel( connection );
	musiciEnsembleTable = new JTable( musiciEnsembleTableModel );

	setupMusiciDialog( "Insert musici: " + musiciString, "Insert",
			   insertMusiciActionCommand );
    }

    // Constructor
    public EditMusiciDialog( Connection connection,
			     Object     parentObject,
			     int        musiciId ) {
	this.connection = connection;
	this.parentObject = parentObject;
	this.musiciId = musiciId;

	// Setup musici_persoon table
	musiciPersoonTableModel = new MusiciPersoonTableModel( connection, musiciId );
	musiciPersoonTable = new JTable( musiciPersoonTableModel );

	// Setup musici_ensemble table
	musiciEnsembleTableModel = new MusiciEnsembleTableModel( connection, musiciId );
	musiciEnsembleTable = new JTable( musiciEnsembleTableModel );

	try {
	    Statement statement = connection.createStatement();
	    ResultSet resultSet = statement.executeQuery( "SELECT musici FROM musici WHERE musici_id = " +
							  musiciId );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get record for musici_id " +
				    musiciId + " in musici" );
		return;
	    }
	    musiciString = resultSet.getString( 1 );
	} catch ( SQLException ex ) {
	    System.err.println( "EditMusiciDialog.EditMusiciDialog SQLException:\n\t" +
				ex.getMessage( ) );
	}

	setupMusiciDialog( "Edit musici: " + musiciString, "Update",
			   updateMusiciActionCommand );
    }

    // Setup musici dialog
    void setupMusiciDialog( String dialogTitle,
			    String editMusiciButtonText,
			    String editMusiciButtonActionCommand ) {
	// Create modal dialog for editing musici
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    System.err.println( "EditMusiciDialog.setupMusiciDialog, " +
				"unexpected parent object class: " +
				parentObject.getClass( ).getName( ) );
	    return;
	}

	// Set grid bag layout manager
	Container container = dialog.getContentPane();
	container.setLayout( new GridBagLayout( ) );

	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 10, 5, 10, 5 );


	////////////////////////////////////////////////
	// Musici text field
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	container.add( new JLabel( "Musici:" ), constraints );

	musiciTextField = new JTextField( musiciString, 50 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 4;
	container.add( musiciTextField, constraints );


	////////////////////////////////////////////////
	// Persoon ComboBox, Add, Filter Persoon button
	////////////////////////////////////////////////

	// Setup a JComboBox with the results of the query on persoon
	persoonComboBox = new PersoonComboBox( connection, dialog );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	constraints.insets = new Insets( 10, 5, 5, 5 );
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

	// Define Add button next to persoon combo box
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
						   "Edit Musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected persoon in table
		musiciPersoonTableModel.addRow( selectedPersoonId,
						persoonComboBox.getSelectedPersoonString( ) );
	    }
	}
	addPersoonInTableButton.addActionListener( new AddPersoonInTableActionListener( ) );

	JButton filterPersoonButton = new JButton( "Filter" );
	filterPersoonButton.setActionCommand( "filterPersoon" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterPersoonButton, constraints );

	class FilterPersoonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		persoonFilterString = persoonComboBox.filterPersoonComboBox( );
	    }
	}
	filterPersoonButton.addActionListener( new FilterPersoonActionListener( ) );


	////////////////////////////////////////////////
	// Musici-persoon Table, Add, Replace, Remove Buttons
	////////////////////////////////////////////////

	// Setup a table with musici_persoon
	musiciPersoonTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	musiciPersoonTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	musiciPersoonTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 210 );
	musiciPersoonTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 210 );

	// Set vertical size just enough for 10 entries
	musiciPersoonTable.setPreferredScrollableViewportSize( new Dimension( 420, 160 ) );

	final DefaultCellEditor rolDefaultCellEditor =
	    new DefaultCellEditor( new RolComboBox( connection, null, false ) );
	musiciPersoonTable.getColumnModel( ).getColumn( 1 ).setCellEditor( rolDefaultCellEditor );

	constraints.gridx = 1;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	// Set gridheight to allow for two buttons next to the table
	constraints.gridheight = 2;
	container.add( new JScrollPane( musiciPersoonTable ), constraints );

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


	// Get the selection model related to the musici_persoon table
	final ListSelectionModel persoonListSelectionModel = musiciPersoonTable.getSelectionModel( );

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

	// Add persoonListSelectionListener object to the selection model of the musici table
	final PersoonListSelectionListener persoonListSelectionListener = new PersoonListSelectionListener( );
	persoonListSelectionModel.addListSelectionListener( persoonListSelectionListener );


	// Class to handle Replace button: uses persoonListSelectionListener
	class ReplacePersoonInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = persoonListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen persoon geselecteerd",
						   "Edit Musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		int selectedPersoonId = persoonComboBox.getSelectedPersoonId( );
		if ( selectedPersoonId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Persoon niet ingevuld",
						   "Edit Musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected persoon in table
		musiciPersoonTableModel.replaceRow( selectedRow,
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
						   "Edit Musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		musiciPersoonTableModel.removeRow( selectedRow );
	    }
	}
	removePersoonFromTableButton.addActionListener( new RemovePersoonFromTableActionListener( ) );


	////////////////////////////////////////////////
	// Ensemble ComboBox, Add, Filter Ensemble button
	////////////////////////////////////////////////

	// Setup a JComboBox with the results of the query on ensemble
	ensembleComboBox = new EnsembleComboBox( connection, dialog, true );
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Ensemble:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( ensembleComboBox, constraints );

	class SelectEnsembleActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a ensemble record needs to be inserted
		if ( ensembleComboBox.newEnsembleSelected( ) ) {
		    // Insert new ensemble record
		    EditEnsembleDialog editEnsembleDialog =
			new EditEnsembleDialog( connection, parentObject, ensembleFilterString );

		    // Check if a new ensemble record has been inserted
		    if ( editEnsembleDialog.ensembleUpdated( ) ) {
			// Get the id of the new ensemble record
			int selectedEnsembleId = editEnsembleDialog.getEnsembleId( );

			// Setup the ensemble combo box again
			ensembleComboBox.setupEnsembleComboBox( selectedEnsembleId );
		    }
		}
	    }
	}
	ensembleComboBox.addActionListener( new SelectEnsembleActionListener( ) );

	JButton addEnsembleInTableButton = new JButton( "Add" );
	addEnsembleInTableButton.setActionCommand( "addEnsembleInTable" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( addEnsembleInTableButton, constraints );

	class AddEnsembleInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {

		int selectedEnsembleId = ensembleComboBox.getSelectedEnsembleId( );
		if ( selectedEnsembleId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Ensemble niet ingevuld",
						   "Edit Musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Add selected ensemble in table
		musiciEnsembleTableModel.addRow( selectedEnsembleId,
						 ensembleComboBox.getSelectedEnsembleString( ) );
	    }
	}
	addEnsembleInTableButton.addActionListener( new AddEnsembleInTableActionListener( ) );

	JButton filterEnsembleButton = new JButton( "Filter" );
	filterEnsembleButton.setActionCommand( "filterEnsemble" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterEnsembleButton, constraints );

	class FilterEnsembleActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		ensembleFilterString = ensembleComboBox.filterEnsembleComboBox( );
	    }
	}
	filterEnsembleButton.addActionListener( new FilterEnsembleActionListener( ) );


	/////////////////////////////////////////////////
	// Musici-ensemble Table, Replace, Remove Buttons
	/////////////////////////////////////////////////

	// Setup a table with musici_ensemble
	musiciEnsembleTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	musiciEnsembleTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	musiciEnsembleTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 420 );

	// Set vertical size just enough for 5 entries
	musiciEnsembleTable.setPreferredScrollableViewportSize( new Dimension( 420, 80 ) );

	constraints.gridx = 1;
	constraints.gridy = 6;
	constraints.gridwidth = 1;
	// Set gridheight to allow for two buttons next to the table
	constraints.gridheight = 2;
	container.add( new JScrollPane( musiciEnsembleTable ), constraints );

	// Define Replace button next to table
	final JButton replaceEnsembleInTableButton = new JButton( "Replace" );
	replaceEnsembleInTableButton.setActionCommand( "replaceEnsembleInTable" );
	replaceEnsembleInTableButton.setEnabled( false );
	constraints.gridwidth = 1;
	constraints.gridheight = 1;
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( replaceEnsembleInTableButton, constraints );

	// Define Remove button next to table
	final JButton removeEnsembleFromTableButton = new JButton( "Remove" );
	removeEnsembleFromTableButton.setActionCommand( "removeEnsembleFromTable" );
	removeEnsembleFromTableButton.setEnabled( false );
	constraints.gridwidth = 1;
	constraints.gridy = 7;
	container.add( removeEnsembleFromTableButton, constraints );


	// Get the selection model related to the musici_ensemble table
	final ListSelectionModel ensembleListSelectionModel = musiciEnsembleTable.getSelectionModel( );

	// Class to handle row selection in musici_ensemble table
	class EnsembleListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( ensembleListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    replaceEnsembleInTableButton.setEnabled( false );
		    removeEnsembleFromTableButton.setEnabled( false );
		    return;
		}

		selectedRow = ensembleListSelectionModel.getMinSelectionIndex( );
		replaceEnsembleInTableButton.setEnabled( true );
		removeEnsembleFromTableButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add ensembleListSelectionListener object to the selection model of the musici_ensemble table
	final EnsembleListSelectionListener ensembleListSelectionListener = new EnsembleListSelectionListener( );
	ensembleListSelectionModel.addListSelectionListener( ensembleListSelectionListener );


	// Class to handle Replace button: uses ensembleListSelectionListener
	class ReplaceEnsembleInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = ensembleListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen ensemble geselecteerd",
						   "Edit Musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		int selectedEnsembleId = ensembleComboBox.getSelectedEnsembleId( );
		if ( selectedEnsembleId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Ensemble niet ingevuld",
						   "Edit Musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Replace selected row in table
		musiciEnsembleTableModel.replaceRow( selectedRow,
						     selectedEnsembleId,
						     ensembleComboBox.getSelectedEnsembleString( ) );
	    }
	}
	replaceEnsembleInTableButton.addActionListener( new ReplaceEnsembleInTableActionListener( ) );


	// Class to handle Remove button: uses ensembleListSelectionListener
	class RemoveEnsembleFromTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		int selectedRow = ensembleListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen ensemble geselecteerd",
						   "Edit Musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		musiciEnsembleTableModel.removeRow( selectedRow );
	    }
	}
	removeEnsembleFromTableButton.addActionListener( new RemoveEnsembleFromTableActionListener( ) );


	////////////////////////////////////////////////
	// Update/Insert, Cancel Buttons
	////////////////////////////////////////////////

	JPanel buttonPanel = new JPanel( );

	class EditMusiciActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertMusiciActionCommand ) ) {
		    insertMusici( );
		} else if ( ae.getActionCommand( ).equals( updateMusiciActionCommand ) ) {
		    updateMusici( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JButton editMusiciButton = new JButton( editMusiciButtonText );
	editMusiciButton.setActionCommand( editMusiciButtonActionCommand );
	editMusiciButton.addActionListener( new EditMusiciActionListener( ) );
	buttonPanel.add( editMusiciButton );

	JButton cancelMusiciButton = new JButton( "Cancel" );
	cancelMusiciButton.setActionCommand( "cancelMusici" );
	cancelMusiciButton.addActionListener( new EditMusiciActionListener( ) );
	buttonPanel.add( cancelMusiciButton );

	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.gridwidth = 5;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( buttonPanel, constraints );

	dialog.setSize( 720, 580 );
	dialog.setVisible( true );
    }

    void insertMusici( ) {
	musiciString = musiciTextField.getText( );

	// System.out.println( "EditMusiciDialog.insertMusici, musici: " +
	//                     musiciString );

	try {
	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( musici_id ) FROM musici" );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get maximum for musici_id in musici" );
		dialog.setVisible( false );
		return;
	    }
	    musiciId = resultSet.getInt( 1 ) + 1;

	    // Matcher to find single quotes in musiciString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( musiciString );

	    // Insert a new row in the musici table (which really is just a label)
	    nUpdate = statement.executeUpdate( "INSERT INTO musici SET " +
					       "musici_id = " + musiciId +
					       ",  musici = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not insert in musici" );
		dialog.setVisible( false );
		return;
	    }

	    // Insert the rows in the musici_persoon table using the new musici ID
	    musiciPersoonTableModel.insertTable( musiciId );

	    // Insert the rows in the musici_ensemble table using the new musici ID
	    musiciEnsembleTableModel.insertTable( musiciId );
	} catch ( SQLException ex ) {
	    System.err.println( "EditMusiciDialog.insertMusici SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }

    void updateMusici( ) {
	musiciString = musiciTextField.getText( );

	// System.out.println( "EditMusiciDialog.updateMusici, musici: " +
	//		       musiciString + ", with id " + musiciId );

	try {
	    Statement statement = connection.createStatement();

	    // Matcher to find single quotes in musiciString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( musiciString );

	    // Update the musici table (which really is just a label)
	    nUpdate = statement.executeUpdate( "UPDATE musici SET musici = '" + quoteMatcher.replaceAll( "\\\\'" ) +
					       "' WHERE musici_id = " + musiciId );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not update in musici" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditMusiciDialog.updateMusici SQLException:\n\t" +
				ex.getMessage( ) );
	}

	// Update the rows in the musici_persoon table
	musiciPersoonTableModel.updateTable( );

	// Update the rows in the musici_ensemble table
	musiciEnsembleTableModel.updateTable( );
    }

    public boolean musiciUpdated( ) { return nUpdate > 0; }

    public String getMusiciString( ) { return musiciString; }

    public int getMusiciId( ) { return musiciId; }
}
