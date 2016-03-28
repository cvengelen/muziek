// Dialog for inserting or updating a record in ensemble

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

public class EditEnsembleDialog {
    Connection conn;
    Object parentObject;
    JDialog dialog;
    int ensembleId;
    String ensembleString;
    JTextField ensembleTextField;
    int nUpdate = 0;

    final String insertEnsembleActionCommand = "insertEnsemble";
    final String updateEnsembleActionCommand = "updateEnsemble";

    // Pattern to find a single quote in persoon, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final private Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public EditEnsembleDialog( Connection conn,
			       Object parentObject,
			       String ensembleString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.ensembleString = ensembleString;
	setupEnsembleDialog( "Insert ensemble", "Insert",
			     insertEnsembleActionCommand );
    }

    // Constructor
    public EditEnsembleDialog( Connection conn,
			       Object parentObject,
			       int ensembleId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.ensembleId = ensembleId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT ensemble FROM ensemble WHERE ensemble_id = " +
							  ensembleId );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get record for ensemble_id " +
				    ensembleId + " in ensemble" );
		return;
	    }

	    ensembleString = resultSet.getString( 1 );
	} catch ( SQLException ex ) {
	    System.err.println( "EditEnsembleDialog.EditEnsembleDialog SQLException:\n\t" +
				ex.getMessage( ) );
	}

	setupEnsembleDialog( "Edit ensemble", "Update",
			    updateEnsembleActionCommand );
    }

    // Setup ensemble dialog
    void setupEnsembleDialog( String dialogTitle,
			      String editEnsembleButtonText,
			      String editEnsembleButtonActionCommand ) {
	// Create modal dialog for editing ensemble
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    System.err.println( "EditEnsembleDialog.setupEnsembleDialog, " +
				"unexpected parent object class: " +
				parentObject.getClass( ).getName( ) );
	    return;
	}

	// Set grid bag layout manager
	Container container = dialog.getContentPane( );
	container.setLayout( new GridBagLayout( ) );

	GridBagConstraints constraints = new GridBagConstraints( );

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 0, 0, 5, 5 );
	container.add( new JLabel( "Ensemble: " ), constraints );

	ensembleTextField = new JTextField( ensembleString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( ensembleTextField, constraints );

	class EditEnsembleActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertEnsembleActionCommand ) ) {
		    insertEnsemble( );
		} else if ( ae.getActionCommand( ).equals( updateEnsembleActionCommand ) ) {
		    updateEnsemble( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JPanel buttonPanel = new JPanel( );

	JButton editEnsembleButton = new JButton( editEnsembleButtonText );
	editEnsembleButton.setActionCommand( editEnsembleButtonActionCommand );
	editEnsembleButton.addActionListener( new EditEnsembleActionListener( ) );
	buttonPanel.add( editEnsembleButton );

	JButton cancelEnsembleButton = new JButton( "Cancel" );
	cancelEnsembleButton.setActionCommand( "cancelEnsemble" );
	cancelEnsembleButton.addActionListener( new EditEnsembleActionListener( ) );
	buttonPanel.add( cancelEnsembleButton );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize( 700, 150 );
	dialog.setVisible( true );
    }

    void insertEnsemble( ) {
	ensembleString = ensembleTextField.getText( );

	// System.out.println( "EditEnsembleDialog.insertEnsemble, ensemble: " + ensembleString );

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( ensemble_id ) FROM ensemble" );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get maximum for ensemble_id in ensemble" );
		dialog.setVisible( false );
		return;
	    }
	    ensembleId = resultSet.getInt( 1 ) + 1;

	    // Matcher to find single quotes in persoonString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( ensembleString );

	    nUpdate = statement.executeUpdate( "INSERT INTO ensemble SET " +
					       "ensemble_id = " + ensembleId +
					       ",  ensemble = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not insert in ensemble" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditEnsembleDialog.insertEnsemble SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }

    void updateEnsemble( ) {
	ensembleString = ensembleTextField.getText( );

	// System.out.println( "EditEnsembleDialog.updateEnsemble, ensemble: " +
	//                     ensembleString + ", with id " + ensembleId );

	try {
	    Statement statement = conn.createStatement( );

	    // Matcher to find single quotes in persoonString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( ensembleString );

	    nUpdate = statement.executeUpdate( "UPDATE ensemble SET ensemble = '" + quoteMatcher.replaceAll( "\\\\'" ) +
					       "' WHERE ensemble_id = " + ensembleId );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not update in ensemble" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditEnsembleDialog.updateEnsemble SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }

    public boolean ensembleUpdated( ) { return nUpdate > 0; }

    public String getEnsembleString( ) { return ensembleString; }

    public int getEnsembleId( ) { return ensembleId; }
}
