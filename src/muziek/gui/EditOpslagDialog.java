// Dialog for inserting or updating a record in opslag

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

public class EditOpslagDialog {
    Connection conn;
    Object parentObject;
    JDialog dialog;
    int opslagId;
    String opslagString;
    JTextField opslagTextField;
    int nUpdate = 0;
    final String insertOpslagActionCommand = "insertOpslag";
    final String updateOpslagActionCommand = "updateOpslag";

    // Constructor
    public EditOpslagDialog( Connection conn,
			     Object parentObject,
			     String opslagString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.opslagString = opslagString;
	setupOpslagDialog( "Insert opslag", "Insert",
			   insertOpslagActionCommand );
    }

    // Constructor
    public EditOpslagDialog( Connection conn,
			     Object parentObject,
			     int opslagId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.opslagId = opslagId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT opslag FROM opslag WHERE opslag_id = " +
							  opslagId );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get record for opslag_id " +
				    opslagId + " in opslag" );
		return;
	    }

	    opslagString = resultSet.getString( 1 );
	} catch ( SQLException ex ) {
	    System.err.println( "EditOpslagDialog.EditOpslagDialog SQLException:\n\t" +
				ex.getMessage( ) );
	}

	setupOpslagDialog( "Edit opslag", "Update", updateOpslagActionCommand );
    }

    // Setup opslag dialog
    void setupOpslagDialog( String dialogTitle,
			    String editOpslagButtonText,
			    String editOpslagButtonActionCommand ) {
	// Create modal dialog for editing opslag
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    System.err.println( "EditOpslagDialog.setupOpslagDialog, " +
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
	container.add( new JLabel( "Opslag: " ), constraints );

	opslagTextField = new JTextField( opslagString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( opslagTextField, constraints );

	class EditOpslagActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertOpslagActionCommand ) ) {
		    insertOpslag( );
		} else if ( ae.getActionCommand( ).equals( updateOpslagActionCommand ) ) {
		    updateOpslag( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JPanel buttonPanel = new JPanel( );

	JButton editOpslagButton = new JButton( editOpslagButtonText );
	editOpslagButton.setActionCommand( editOpslagButtonActionCommand );
	editOpslagButton.addActionListener( new EditOpslagActionListener( ) );
	buttonPanel.add( editOpslagButton );

	JButton cancelOpslagButton = new JButton( "Cancel" );
	cancelOpslagButton.setActionCommand( "cancelOpslag" );
	cancelOpslagButton.addActionListener( new EditOpslagActionListener( ) );
	buttonPanel.add( cancelOpslagButton );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize( 700, 150 );
	dialog.setVisible( true );
    }

    void insertOpslag( ) {
	opslagString = opslagTextField.getText( );

	// System.out.println( "EditOpslagDialog.insertOpslag, opslag: " + opslagString );

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( opslag_id ) FROM opslag" );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get maximum for opslag_id in opslag" );
		dialog.setVisible( false );
		return;
	    }
	    opslagId = resultSet.getInt( 1 ) + 1;

	    nUpdate = statement.executeUpdate( "INSERT INTO opslag SET " +
					       "opslag_id = " + opslagId +
					       ",  opslag = '" + opslagString + "'" );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not insert in opslag" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditOpslagDialog.insertOpslag SQLException:\n\t" + ex.getMessage( ) );
	}
    }

    void updateOpslag( ) {
	opslagString = opslagTextField.getText( );

	// System.out.println( "EditOpslagDialog.updateOpslag, opslag: " +
	//                     opslagString + ", with id " + opslagId );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( "UPDATE opslag SET opslag = '" + opslagString +
					       "' WHERE opslag_id = " + opslagId );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not update in opslag" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditOpslagDialog.updateOpslag SQLException:\n\t" +
				ex.getMessage( ) );
	}
    }

    public boolean opslagUpdated( ) { return nUpdate > 0; }

    public String getOpslagString( ) { return opslagString; }

    public int getOpslagId( ) { return opslagId; }
}
