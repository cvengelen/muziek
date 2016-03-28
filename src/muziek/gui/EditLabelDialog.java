// Dialog for inserting or updating a record in label

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

public class EditLabelDialog {
    Connection conn;
    Object parentObject;
    JDialog dialog;
    int labelId;
    String labelString;
    JTextField labelTextField;
    int nUpdate = 0;
    final String insertLabelActionCommand = "insertLabel";
    final String updateLabelActionCommand = "updateLabel";

    // Constructor
    public EditLabelDialog( Connection conn,
			    Object parentObject,
			    String labelString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.labelString = labelString;
	setupLabelDialog( "Insert label", "Insert",
			  insertLabelActionCommand );
    }

    // Constructor
    public EditLabelDialog( Connection conn,
			    Object parentObject,
			    int labelId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.labelId = labelId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT label FROM label WHERE label_id = " +
							  labelId );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get record for label_id " +
				    labelId + " in label" );
		return;
	    }

	    labelString = resultSet.getString( 1 );
	} catch ( SQLException ex ) {
	    System.err.println( "EditLabelDialog.EditLabelDialog SQLException:\n\t" + ex.getMessage( ) );
	}

	setupLabelDialog( "Edit label", "Update", updateLabelActionCommand );
    }

    // Setup label dialog
    void setupLabelDialog( String dialogTitle,
			   String editLabelButtonText,
			   String editLabelButtonActionCommand ) {
	// Create modal dialog for editing label
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, dialogTitle, true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, dialogTitle, true );
	} else {
	    System.err.println( "EditLabelDialog.setupLabelDialog, " +
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
	container.add( new JLabel( "Label: " ), constraints );

	labelTextField = new JTextField( labelString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( labelTextField, constraints );

	class EditLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertLabelActionCommand ) ) {
		    insertLabel( );
		} else if ( ae.getActionCommand( ).equals( updateLabelActionCommand ) ) {
		    updateLabel( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JPanel buttonPanel = new JPanel( );

	JButton editLabelButton = new JButton( editLabelButtonText );
	editLabelButton.setActionCommand( editLabelButtonActionCommand );
	editLabelButton.addActionListener( new EditLabelActionListener( ) );
	buttonPanel.add( editLabelButton );

	JButton cancelLabelButton = new JButton( "Cancel" );
	cancelLabelButton.setActionCommand( "cancelLabel" );
	cancelLabelButton.addActionListener( new EditLabelActionListener( ) );
	buttonPanel.add( cancelLabelButton );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize( 700, 150 );
	dialog.setVisible( true );
    }

    void insertLabel( ) {
	labelString = labelTextField.getText( );

	// System.out.println( "EditLabelDialog.insertLabel, label: " + label );

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( label_id ) FROM label" );
	    if ( ! resultSet.next( ) ) {
		System.err.println( "Could not get maximum for label_id in label" );
		dialog.setVisible( false );
		return;
	    }
	    labelId = resultSet.getInt( 1 ) + 1;

	    nUpdate = statement.executeUpdate( "INSERT INTO label SET " +
					       "label_id = " + labelId +
					       ",  label = '" + labelString + "'" );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not insert in label" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditLabelDialog.insertLabel SQLException:\n\t" + ex.getMessage( ) );
	}
    }

    void updateLabel( ) {
	labelString = labelTextField.getText( );

	// System.out.println( "EditLabelDialog.updateLabel, label: " +
	//                     labelString + ", with id " + labelId );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( "UPDATE label SET label = '" + labelString +
					       "' WHERE label_id = " + labelId );
	    if ( nUpdate != 1 ) {
		System.err.println( "Could not update in label" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "EditLabelComboBox.updateLabel SQLException:\n\t" + ex.getMessage( ) );
	}
    }

    public boolean labelUpdated( ) { return nUpdate > 0; }

    public String getLabelString( ) { return labelString; }

    public int getLabelId( ) { return labelId; }
}
