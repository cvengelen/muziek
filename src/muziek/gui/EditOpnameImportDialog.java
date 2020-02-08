// Project:	muziek
// Package:	muziek.gui
// File:	EditOpnameDialog.java
// Description:	Dialog for inserting or updating a record in opname
// Author:	Chris van Engelen
// History:	2005/05/01: Initial version
//		2009/01/01: Add selection on Componist-Persoon
//              2016/05/20: Refactoring, and use of Java 7, 8 features

package muziek.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Logger;


public class EditOpnameImportDialog {
    final private Logger logger = Logger.getLogger( "muziek.gui.EditOpnameImportDialog" );

    private Connection conn;
    private Object parentObject;
    private JDialog dialog;

    private ArrayList<OpnameKey> opnameKeys;

    private ImportTypeComboBox importTypeComboBox;
    private int defaultImportTypeId;

    private JSpinner importDatumSpinner;

    private int nUpdate = 0;

    private final String insertOpnameActionCommand = "insertOpname";
    private final String updateOpnameActionCommand = "updateOpname";

    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

    // Constructor
    public EditOpnameImportDialog(Connection conn,
                                  Object     parentObject,
                                  int        defaultImportTypeId,
                                  ArrayList<OpnameKey> opnameKeys ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.defaultImportTypeId = defaultImportTypeId;
	this.opnameKeys = opnameKeys;

	setupOpnameImportDialog();
    }

    // Setup opname import dialog
    private void setupOpnameImportDialog( ) {
	// Create modal dialog for editing opname record
	if ( parentObject instanceof JFrame ) {
	    dialog = new JDialog( ( JFrame )parentObject, "Opname Import", true );
	} else if ( parentObject instanceof JDialog ) {
	    dialog = new JDialog( ( JDialog )parentObject, "Opname Import", true );
	} else {
	    logger.severe( "Unexpected parent object class: " +
			   parentObject.getClass( ).getName( ) );
	    return;
	}

	// Set grid bag layout manager
	Container container = dialog.getContentPane( );
	container.setLayout( new GridBagLayout( ) );
        GridBagConstraints constraints = new GridBagConstraints( );

        //////////////////////////////////////////
        // Import Type Combo Box, Import Datum
        //////////////////////////////////////////

        constraints.anchor = GridBagConstraints.EAST;
        constraints.insets = new Insets( 5, 20, 5, 5 );
        constraints.gridx = 0;
        constraints.gridy = 1;
        constraints.gridwidth = 1;
        container.add( new JLabel( "Import:" ), constraints );

        // Setup a JComboBox for ImportType
        importTypeComboBox = new ImportTypeComboBox( conn, defaultImportTypeId );
        importTypeComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            if (importTypeComboBox.getSelectedImportTypeId( ) == 0) {
                importDatumSpinner.setEnabled(false);
            }
            else {
                importDatumSpinner.setEnabled(true);
            }
        } );

        final JPanel importPanel = new JPanel();
        importPanel.add( importTypeComboBox );

        // Import datum
        GregorianCalendar calendar = new GregorianCalendar( );
        Date defaultImportDatumDate = calendar.getTime( );

        calendar.add( Calendar.YEAR, -50 );
        Date earliestDate = calendar.getTime( );
        calendar.add( Calendar.YEAR, 100 );
        Date latestDate = calendar.getTime( );
        SpinnerDateModel importDatumSpinnerDatemodel = new SpinnerDateModel(defaultImportDatumDate,
                                                                            earliestDate,
                                                                            latestDate,
                                                                            Calendar.DAY_OF_MONTH );
        importDatumSpinner = new JSpinner( importDatumSpinnerDatemodel );
        importDatumSpinner.setEditor( new JSpinner.DateEditor( importDatumSpinner, "dd-MM-yyyy" ) );
        importPanel.add( importDatumSpinner );

        // Set the import datum to enabled or disabled according to the selected medium type ID
        if (importTypeComboBox.getSelectedImportTypeId( ) == 0) {
            importDatumSpinner.setEnabled(false);
        }
        else {
            importDatumSpinner.setEnabled(true);
        }

        constraints.insets = new Insets( 5, 0, 5, 20 );
        constraints.anchor = GridBagConstraints.WEST;
        constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.gridwidth = 2;
        container.add( importPanel, constraints );


	//////////////////////////////////////////
	// Update/Insert, Cancel buttons
	//////////////////////////////////////////

	JPanel buttonPanel = new JPanel( );

	ActionListener buttonPanelActionListener = ( ActionEvent actionEvent ) -> {
            boolean result = true;

            if ( actionEvent.getActionCommand( ).equals( "updateOpnameImport" ) ) {
                result = updateOpnameImport( );
            }

            // Any other actionCommand, including cancel, has no action
            if ( result ) {
                dialog.setVisible( false );
            }
        };

	JButton editOpnameButton = new JButton( "Update Import" );
	editOpnameButton.setActionCommand( "updateOpnameImport" );
	editOpnameButton.addActionListener( buttonPanelActionListener );
	buttonPanel.add( editOpnameButton );

	JButton cancelOpnameButton = new JButton( "Cancel" );
	cancelOpnameButton.setActionCommand( "cancelOpnameImport" );
	cancelOpnameButton.addActionListener( buttonPanelActionListener );
	buttonPanel.add( cancelOpnameButton );

        constraints.anchor = GridBagConstraints.CENTER;
        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 14;
	constraints.gridwidth = 3;
	container.add( buttonPanel, constraints );

        // Default dialog size
        final Dimension dialogSize = new Dimension( 400, 150 );
	dialog.setSize( dialogSize );
	dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	dialog.setVisible( true );
    }

    private String updateString = null;

    private void addToUpdateString( String additionalUpdateString ) {
	if ( updateString == null ) {
	    updateString = additionalUpdateString;
	} else {
	    updateString += ", " + additionalUpdateString;
	}
    }

    private boolean updateOpnameImport( ) {
	// Initialise string holding the update query
	updateString = null;

        int importTypeId = importTypeComboBox.getSelectedImportTypeId( );
        // Check if importTypeId changed to a non-zero value
        if ( importTypeId != 0 ) {
            if (importTypeId != defaultImportTypeId) {
                addToUpdateString("import_type_id = " + importTypeId);
            }

            Date importDatumDate = (Date)importDatumSpinner.getValue();
            String importDatumString = dateFormat.format((Date)importDatumSpinner.getValue());
            if (importDatumString != null) {
                if (importDatumString.length() > 0) {
                    addToUpdateString("import_datum = '" + importDatumString + "'");
                }
            }
        }
        else {
            // Import type ID is 0: check for change
            if (0 != defaultImportTypeId) {
                addToUpdateString("import_type_id = NULL");
                addToUpdateString("import_datum = NULL");
            }
        }

	// Check if any update is necessary at all
	if ( updateString == null ) {
	    logger.info( "no update necessary" );
	    return true;
	}

	updateString  = "UPDATE opname SET " + updateString;

        for (OpnameKey opnameKey: opnameKeys) {
            String updateOpnameString = updateString + " WHERE medium_id = " + opnameKey.getMediumId();
            updateOpnameString += " AND opus_id = " + opnameKey.getOpusId();
            updateOpnameString += " AND opname_nummer = " + opnameKey.getOpnameNummer();
            updateOpnameString += " AND musici_id = " + opnameKey.getMusiciId();

            logger.fine("updateOpnameString: " + updateOpnameString);

            try {
                Statement statement = conn.createStatement();
                nUpdate = statement.executeUpdate(updateOpnameString);
                if (nUpdate != 1) {
                    logger.severe("Could not update in opname");
                    return false;
                }
            } catch (SQLException sqlException) {
                logger.severe("SQLException: " + sqlException.getMessage());
                return false;
            }
        }

        return true;
    }

    public boolean opnameUpdated( ) { return nUpdate > 0; }
}
