// Dialog for showing all tracks for a medium, submedium

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

public class ShowMediumTracksDialog {
    final Logger logger = Logger.getLogger( "muziek.gui.ShowMediumTracksDialog" );

    Connection conn;
    Object parentObject;
    JDialog dialog;

    int mediumId = 0;
    String submediumString = null;

    String mediumTitelString = null;
    String uitvoerendenString = null;
    String mediumTypeString = null;

    // Constructor
    public ShowMediumTracksDialog( Connection conn,
				   Object     parentObject,
				   int        mediumId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.mediumId = mediumId;

	try {
	    String mediumQueryString =
		"SELECT medium_titel, uitvoerenden, medium_type, aantal FROM medium " +
		"LEFT JOIN medium_type ON medium_type.medium_type_id = medium.medium_type_id " +
		"WHERE medium_id = " + mediumId;

	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( mediumQueryString );

	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for medium_id " + mediumId + " in medium" );
		return;
	    }

	    mediumTitelString = resultSet.getString( 1 );
	    uitvoerendenString = resultSet.getString( 2 );
	    mediumTypeString = resultSet.getString( 3 );
	    int aantal = resultSet.getInt( 4 );
	    if ( aantal > 1 ) {
		mediumTypeString += " (" + aantal + ")";
	    }
	} catch ( SQLException ex ) {
	    logger.severe( "SQLException: " + ex.getMessage( ) );
	}

	// Create modal dialog
	final String dialogTitle = "Show Medium Tracks";
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
	final Container container = dialog.getContentPane( );
	container.setLayout( new GridBagLayout( ) );

	final GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 5, 5, 5, 5 );


	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Medium titel:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 5;
	container.add( new JLabel( mediumTitelString ), constraints );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Uitvoerenden:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 5;
	container.add( new JLabel( uitvoerendenString ), constraints );

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Medium type:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( new JLabel( mediumTypeString ), constraints );

	// Initialize combo box for submedium (may not be necessary)
	final JComboBox submediumComboBox = new JComboBox( );

	// Check if there is more than one submedium for the medium
	try {
	    final String submediumQueryString =
		"SELECT DISTINCT submedium FROM tracks WHERE medium_id = " + mediumId;

	    final Statement statement = conn.createStatement( );
	    final ResultSet resultSet = statement.executeQuery( submediumQueryString );

	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get submedium for medium_id " +
			       mediumId + " in medium" );
		return;
	    }
	    submediumString = resultSet.getString( 1 );
	    submediumComboBox.addItem( submediumString );

	    // Check if there is more than one record
	    while ( resultSet.next( ) ) {
		submediumComboBox.addItem( resultSet.getString( 1 ) );
	    }
	} catch ( SQLException ex ) {
	    logger.severe( "SQLException: " + ex.getMessage( ) );
	}

	// Create tracks table model with connection and medium, submedium
	final MediumTracksTableModel mediumTracksTableModel =
	    new MediumTracksTableModel( conn, mediumId, submediumString );

	// Create tracks table from tracks table model
	final JTable mediumTracksTable = new JTable( mediumTracksTableModel );

	final JLabel totalTimeLabel = new JLabel( mediumTracksTableModel.getTotalTrackTime( ) );

	constraints.gridwidth = 1;
	container.add( new JLabel( "  Submedium:" ), constraints );

	// Setup submedium combo box if necessary
	if ( submediumComboBox.getItemCount( ) > 1 ) {
	    // Show the submedium combo box
	    container.add( submediumComboBox, constraints );

	    class SelectSubmediumActionListener implements ActionListener {
		public void actionPerformed( ActionEvent actionEvent ) {
		    // Get the submedium
		    submediumString = ( String )submediumComboBox.getSelectedItem( );
		    mediumTracksTableModel.setupMediumTracksTableModel( submediumString );
		    totalTimeLabel.setText( mediumTracksTableModel.getTotalTrackTime( ) );
		}
	    }
	    submediumComboBox.addActionListener( new SelectSubmediumActionListener( ) );
	} else {
	    // Submedium fixed
	    container.add( new JLabel( submediumString ), constraints );
	}

	container.add( new JLabel( "  Total time:" ), constraints );
	container.add( totalTimeLabel, constraints );


	////////////////////////////////////////////////
	// Medium-Tracks Table
	////////////////////////////////////////////////

	// Setup a table with tracks records for this medium, submedium
	mediumTracksTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	mediumTracksTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 50 );  // track #
	mediumTracksTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 70 );  // tijd
	mediumTracksTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 250 ); // opus titel
	mediumTracksTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth( 40 );  // opus-deel #
	mediumTracksTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth( 150 ); // opus-deel titel
	mediumTracksTable.getColumnModel( ).getColumn( 5 ).setPreferredWidth( 110 ); // componisten
	mediumTracksTable.getColumnModel( ).getColumn( 6 ).setPreferredWidth( 90 );  // opus type
	mediumTracksTable.getColumnModel( ).getColumn( 7 ).setPreferredWidth( 80 );  // opus subtype

	// Set vertical size just enough for 20 entries
	mediumTracksTable.setPreferredScrollableViewportSize( new Dimension( 840, 320 ) );
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = 8;
	container.add( new JScrollPane( mediumTracksTable ), constraints );


	class closeActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		dialog.setVisible( false );
	    }
	}

	JButton closeButton = new JButton( "Close" );
	closeButton.addActionListener( new closeActionListener( ) );
	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.anchor = GridBagConstraints.CENTER;
	container.add( closeButton, constraints );

	dialog.setSize( 950, 550 );
	dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	dialog.setVisible( true );
    }
}
