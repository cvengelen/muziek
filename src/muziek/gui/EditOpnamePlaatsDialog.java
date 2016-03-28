// Dialog for inserting or updating a record in opnamePlaats

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

public class EditOpnamePlaatsDialog {
    final private Logger logger = Logger.getLogger( "muziek.gui.EditOpnamePlaatsDialog" );

    Connection conn;
    Object parentObject;
    JDialog dialog;
    int opnamePlaatsId;
    String opnamePlaatsString;
    JTextField opnamePlaatsTextField;
    int nUpdate = 0;

    final String insertOpnamePlaatsActionCommand = "insertOpnamePlaats";
    final String updateOpnamePlaatsActionCommand = "updateOpnamePlaats";

    // Pattern to find a single quote in persoon, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final private Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public EditOpnamePlaatsDialog( Connection conn,
				   Object     parentObject,
				   String     opnamePlaatsString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.opnamePlaatsString = opnamePlaatsString;
	setupOpnamePlaatsDialog( "Insert Opname Plaats", "Insert",
				 insertOpnamePlaatsActionCommand );
    }

    // Constructor
    public EditOpnamePlaatsDialog( Connection conn,
				   Object     parentObject,
				   int        opnamePlaatsId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.opnamePlaatsId = opnamePlaatsId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT opname_plaats FROM opname_plaats WHERE opname_plaats_id = " +
							  opnamePlaatsId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for opname_plaats_id " +
			       opnamePlaatsId + " in opname_plaats" );
		return;
	    }

	    opnamePlaatsString = resultSet.getString( 1 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupOpnamePlaatsDialog( "Edit Opname Plaats", "Update",
				 updateOpnamePlaatsActionCommand );
    }

    // Setup opnamePlaats dialog
    void setupOpnamePlaatsDialog( String dialogTitle,
				  String editOpnamePlaatsButtonText,
				  String editOpnamePlaatsButtonActionCommand ) {
	// Create modal dialog for editing opnamePlaats
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
	Container container = dialog.getContentPane( );
	container.setLayout( new GridBagLayout( ) );

	GridBagConstraints constraints = new GridBagConstraints( );

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 0, 0, 5, 5 );
	container.add( new JLabel( "Opname plaats: " ), constraints );

	opnamePlaatsTextField = new JTextField( opnamePlaatsString, 40 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( opnamePlaatsTextField, constraints );

	class EditOpnamePlaatsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertOpnamePlaatsActionCommand ) ) {
		    insertOpnamePlaats( );
		} else if ( ae.getActionCommand( ).equals( updateOpnamePlaatsActionCommand ) ) {
		    updateOpnamePlaats( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JPanel buttonPanel = new JPanel( );

	JButton editOpnamePlaatsButton = new JButton( editOpnamePlaatsButtonText );
	editOpnamePlaatsButton.setActionCommand( editOpnamePlaatsButtonActionCommand );
	editOpnamePlaatsButton.addActionListener( new EditOpnamePlaatsActionListener( ) );
	buttonPanel.add( editOpnamePlaatsButton );

	JButton cancelOpnamePlaatsButton = new JButton( "Cancel" );
	cancelOpnamePlaatsButton.setActionCommand( "cancelOpnamePlaats" );
	cancelOpnamePlaatsButton.addActionListener( new EditOpnamePlaatsActionListener( ) );
	buttonPanel.add( cancelOpnamePlaatsButton );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize( 700, 150 );
	dialog.setVisible( true );
    }

    void insertOpnamePlaats( ) {
	opnamePlaatsString = opnamePlaatsTextField.getText( );

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( opname_plaats_id ) FROM opname_plaats" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for opname_plaats_id in opnamePlaats" );
		dialog.setVisible( false );
		return;
	    }
	    opnamePlaatsId = resultSet.getInt( 1 ) + 1;

	    // Matcher to find single quotes in opnamePlaatsString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( opnamePlaatsString );

	    nUpdate = statement.executeUpdate( "INSERT INTO opname_plaats SET " +
					       "opname_plaats_id = " + opnamePlaatsId +
					       ",  opname_plaats = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in opname_plaats" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    void updateOpnamePlaats( ) {
	opnamePlaatsString = opnamePlaatsTextField.getText( );

	// Matcher to find single quotes in opnamePlaatsString, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	final Matcher quoteMatcher = quotePattern.matcher( opnamePlaatsString );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( "UPDATE opname_plaats SET opname_plaats = '" +
					       quoteMatcher.replaceAll( "\\\\'" ) +
					       "' WHERE opname_plaats_id = " + opnamePlaatsId );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in opname_plaats" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public boolean opnamePlaatsUpdated( ) { return nUpdate > 0; }

    public String getOpnamePlaatsString( ) { return opnamePlaatsString; }

    public int getOpnamePlaatsId( ) { return opnamePlaatsId; }
}
