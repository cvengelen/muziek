// Dialog for inserting or updating a record in subtype

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

public class EditSubtypeDialog {
    Connection conn;
    Object parentObject;
    JDialog dialog;
    int subtypeId;
    String subtypeString;
    JTextField subtypeTextField;
    int nUpdate = 0;
    final String insertSubtypeActionCommand = "insertSubtype";
    final String updateSubtypeActionCommand = "updateSubtype";

    // Constructor
    public EditSubtypeDialog( Connection conn,
			      Object parentObject,
			      String subtypeString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.subtypeString = subtypeString;
	setupSubtypeDialog( "Insert subtype", "Insert",
			     insertSubtypeActionCommand );
    }

    // Constructor
    public EditSubtypeDialog( Connection conn,
			      Object parentObject,
			      int subtypeId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.subtypeId = subtypeId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT subtype FROM subtype WHERE subtype_id = " +
							  subtypeId );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get record for subtype_id " +
				    subtypeId + " in subtype" );
		return;
	    }

	    subtypeString = resultSet.getString( 1 );
	} catch ( SQLException ex ) {
	    System.err.println( "EditSubtypeDialog.EditSubtypeDialog SQLException:\n\t" +
				ex.getMessage( ) );
	}

	setupSubtypeDialog( "Edit subtype", "Update",
			    updateSubtypeActionCommand );
    }

    // Setup subtype dialog
    void setupSubtypeDialog( String dialogTitle,
			     String editSubtypeButtonText,
			     String editSubtypeButtonActionCommand ) {
	// Create modal dialog for editing subtype
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    System.err.println( "EditSubtypeDialog.setupSubtypeDialog, " +
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
	container.add( new JLabel( "Subtype: " ), constraints );

	subtypeTextField = new JTextField( subtypeString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( subtypeTextField, constraints );

	class EditSubtypeActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertSubtypeActionCommand ) ) {
		    insertSubtype( );
		} else if ( ae.getActionCommand( ).equals( updateSubtypeActionCommand ) ) {
		    updateSubtype( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JPanel buttonPanel = new JPanel( );

	JButton editSubtypeButton = new JButton( editSubtypeButtonText );
	editSubtypeButton.setActionCommand( editSubtypeButtonActionCommand );
	editSubtypeButton.addActionListener( new EditSubtypeActionListener( ) );
	buttonPanel.add( editSubtypeButton );

	JButton cancelSubtypeButton = new JButton( "Cancel" );
	cancelSubtypeButton.setActionCommand( "cancelSubtype" );
	cancelSubtypeButton.addActionListener( new EditSubtypeActionListener( ) );
	buttonPanel.add( cancelSubtypeButton );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize( 700, 150 );
	dialog.setVisible( true );
    }

    void insertSubtype( ) {
	subtypeString = subtypeTextField.getText( );

	// System.out.println( "EditSubtypeDialog.insertSubtype, subtype: " + subtypeString );

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( subtype_id ) FROM subtype" );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get maximum for subtype_id in subtype" );
		dialog.setVisible( false );
		return;
	    }
	    subtypeId = resultSet.getInt( 1 ) + 1;

	    nUpdate = statement.executeUpdate( "INSERT INTO subtype SET " +
					       "subtype_id = " + subtypeId +
					       ",  subtype = '" + subtypeString + "'" );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not insert in subtype" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditSubtypeDialog.insertSubtype SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }

    void updateSubtype( ) {
	subtypeString = subtypeTextField.getText( );

	// System.out.println( "EditSubtypeDialog.updateSubtype, subtype: " +
	//                     subtypeString + ", with id " + subtypeId );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( "UPDATE subtype SET subtype = '" + subtypeString +
					       "' WHERE subtype_id = " + subtypeId );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not update in subtype" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditSubtypeDialog.updateSubtype SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }

    public boolean subtypeUpdated( ) { return nUpdate > 0; }

    public String getSubtypeString( ) { return subtypeString; }

    public int getSubtypeId( ) { return subtypeId; }
}
