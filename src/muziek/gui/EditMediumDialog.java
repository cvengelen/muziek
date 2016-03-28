// Dialog for inserting or updating a record in medium

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

public class EditMediumDialog {
    final Logger logger = Logger.getLogger( "muziek.gui.EditMediumDialog" );

    Connection conn;
    Object parentObject;
    JDialog dialog;

    int mediumId = 0;

    String defaultMediumTitelString = "";
    JTextField mediumTitelTextField;

    String defaultUitvoerendenString = "";
    JTextField uitvoerendenTextField;

    MediumTypeComboBox mediumTypeComboBox;
    int defaultMediumTypeId = 0;

    MediumStatusComboBox mediumStatusComboBox;
    int defaultMediumStatusId = 0;

    int defaultAantal = 0;
    JSpinner aantalSpinner;

    LabelComboBox labelComboBox;
    int defaultLabelId = 0;
    String labelFilterString = null;

    String defaultLabelNummerString = "";
    JTextField labelNummerTextField;

    Date defaultAankoopDatumDate;
    JSpinner aankoopDatumSpinner;

    GenreComboBox genreComboBox;
    int defaultGenreId = 0;

    SubgenreComboBox subgenreComboBox;
    int defaultSubgenreId = 0;

    OpslagComboBox opslagComboBox;
    int defaultOpslagId = 0;
    String opslagFilterString = null;

    String defaultOpmerkingenString = "";
    JTextField opmerkingenTextField;

    int nUpdate = 0;

    final String insertMediumActionCommand = "insertMedium";
    final String updateMediumActionCommand = "updateMedium";
 
    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor for inserting a record in medium
    public EditMediumDialog( Connection conn,
			     Object     parentObject,
			     String     defaultMediumTitelString,
			     String	defaultUitvoerendenString,
			     int        defaultGenreId,
			     int        defaultSubgenreId,
			     int        defaultMediumTypeId,
			     int        defaultMediumStatusId,
			     int	defaultLabelId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.defaultMediumTitelString = defaultMediumTitelString;
	this.defaultUitvoerendenString = defaultUitvoerendenString;
	this.defaultGenreId = defaultGenreId;
	this.defaultSubgenreId = defaultSubgenreId;
	this.defaultMediumTypeId = defaultMediumTypeId;
	this.defaultMediumStatusId = defaultMediumStatusId;
	this.defaultLabelId = defaultLabelId;

	setupMediumDialog( "Insert medium", "Insert", insertMediumActionCommand );
    }

    // Constructor for updating an existing record in medium
    public EditMediumDialog( Connection conn,
			     Object     parentObject,
			     int        mediumId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.mediumId = mediumId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT medium_titel, uitvoerenden, " +
							  "medium_type_id, aantal, label_id, label_nummer, " +
							  "aankoop_datum, genre_id, subgenre_id, opslag_id, " +
							  "opmerkingen, medium_status_id " +
							  "FROM medium WHERE medium_id = " + mediumId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for medium_id " + mediumId + " in medium" );
		return;
	    }

	    defaultMediumTitelString = resultSet.getString( 1 );
	    defaultUitvoerendenString = resultSet.getString( 2 );
	    defaultMediumTypeId = resultSet.getInt( 3 );
	    defaultAantal = resultSet.getInt( 4 );
	    defaultLabelId = resultSet.getInt( 5 );
	    defaultLabelNummerString = resultSet.getString( 6 );
	    defaultAankoopDatumDate = resultSet.getDate( 7 );
	    defaultGenreId = resultSet.getInt( 8 );
	    defaultSubgenreId = resultSet.getInt( 9 );
	    defaultOpslagId = resultSet.getInt( 10 );
	    defaultOpmerkingenString = resultSet.getString( 11 );
	    defaultMediumStatusId = resultSet.getInt( 12 );
	} catch ( SQLException ex ) {
	    logger.severe( "SQLException: " + ex.getMessage( ) );
	}

	setupMediumDialog( "Edit medium", "Update", updateMediumActionCommand );
    }

    // Setup medium dialog
    private void setupMediumDialog( String dialogTitle,
				    String editMediumButtonText,
				    String editMediumButtonActionCommand ) {
	// Create modal dialog for editing medium record
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
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 5, 5, 5, 5 );

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Medium titel:" ), constraints );

	mediumTitelTextField = new JTextField( defaultMediumTitelString, 50 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( mediumTitelTextField, constraints );

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Uitvoerenden:" ), constraints );

	uitvoerendenTextField = new JTextField( defaultUitvoerendenString, 50 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( uitvoerendenTextField, constraints );

	// Setup a JComboBox for mediumType
	mediumTypeComboBox = new MediumTypeComboBox( conn, defaultMediumTypeId );
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Medium type:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( mediumTypeComboBox, constraints );

	// Setup a JComboBox for mediumStatus
	mediumStatusComboBox = new MediumStatusComboBox( conn, defaultMediumStatusId );
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Medium status:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( mediumStatusComboBox, constraints );

	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Aantal:" ), constraints );
	SpinnerNumberModel aantalSpinnerNumberModel = new SpinnerNumberModel( defaultAantal, 0, 100, 1 );
	aantalSpinner = new JSpinner( aantalSpinnerNumberModel );
	JFormattedTextField aantalSpinnerTextField = ( ( JSpinner.DefaultEditor )aantalSpinner.getEditor( ) ).getTextField( );
	if ( aantalSpinnerTextField != null ) {
	    aantalSpinnerTextField.setColumns( 3 );
	}
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( aantalSpinner, constraints );

	// Setup a JComboBox for label
	labelComboBox = new LabelComboBox( conn, dialog, defaultLabelId );
	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Label:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( labelComboBox, constraints );

	class SelectLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a label record needs to be inserted
		if ( labelComboBox.newLabelSelected( ) ) {
		    // Insert new label record
		    EditLabelDialog editLabelDialog =
			new EditLabelDialog( conn, parentObject, labelFilterString );

		    // Check if a new label record has been inserted
		    if ( editLabelDialog.labelUpdated( ) ) {
			// Get the id of the new label record
			int selectedLabelId = editLabelDialog.getLabelId( );

			// Setup the label combo box again
			labelComboBox.setupLabelComboBox( selectedLabelId );
		    }
		}
	    }
	}
	labelComboBox.addActionListener( new SelectLabelActionListener( ) );

	JButton filterLabelButton = new JButton( "Filter" );
	filterLabelButton.setActionCommand( "filterLabel" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterLabelButton, constraints );

	class FilterLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		labelFilterString = labelComboBox.filterLabelComboBox( );
	    }
	}
	filterLabelButton.addActionListener( new FilterLabelActionListener( ) );

	JButton editLabelButton = new JButton( "Edit" );
	editLabelButton.setActionCommand( "editLabel" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( editLabelButton, constraints );

	class EditLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		// Get the selected Label ID
		int selectedLabelId = labelComboBox.getSelectedLabelId( );

		// Check if label has been selected
		if ( selectedLabelId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen Label geselecteerd",
						   "Edit Medium error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditLabelDialog editLabelDialog = new EditLabelDialog( conn,
								       dialog,
								       selectedLabelId );

		if ( editLabelDialog.labelUpdated( ) ) {
		    // Setup the label combo box again
		    labelComboBox.setupLabelComboBox( );
		}
	    }
	}
	editLabelButton.addActionListener( new EditLabelActionListener( ) );


	// Setup a text field for Label nummer
	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Label nummer:" ), constraints );

	labelNummerTextField = new JTextField( defaultLabelNummerString, 16 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( labelNummerTextField, constraints );


	// Aankoop datum
	GregorianCalendar calendar = new GregorianCalendar( );
	if ( defaultAankoopDatumDate == null ) {
	    defaultAankoopDatumDate = calendar.getTime( );
	}
	calendar.add( Calendar.YEAR, -50 );
	Date earliestDate = calendar.getTime( );
	calendar.add( Calendar.YEAR, 100 );
	Date latestDate = calendar.getTime( );
	SpinnerDateModel aankoopDatumSpinnerDatemodel = new SpinnerDateModel( defaultAankoopDatumDate,
									      earliestDate,
									      latestDate,
									      Calendar.DAY_OF_MONTH );
	aankoopDatumSpinner = new JSpinner( aankoopDatumSpinnerDatemodel );
	aankoopDatumSpinner.setEditor( new JSpinner.DateEditor( aankoopDatumSpinner, "dd-MM-yyyy" ) );
	constraints.gridx = 0;
	constraints.gridy = 7;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Aankoop datum:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( aankoopDatumSpinner, constraints );


	// Setup a JComboBox for genre
	genreComboBox = new GenreComboBox( conn, defaultGenreId );
	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Genre:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( genreComboBox, constraints );


	// Setup a JComboBox for subgenre
	subgenreComboBox = new SubgenreComboBox( conn, defaultSubgenreId );
	constraints.gridx = 0;
	constraints.gridy = 9;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Subgenre:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( subgenreComboBox, constraints );


	// Setup a JComboBox for opslag
	opslagComboBox = new OpslagComboBox( conn, dialog, defaultOpslagId );
	constraints.gridx = 0;
	constraints.gridy = 10;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opslag:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( opslagComboBox, constraints );

	class SelectOpslagActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a opslag record needs to be inserted
		if ( opslagComboBox.newOpslagSelected( ) ) {
		    // Insert new opslag record
		    EditOpslagDialog editOpslagDialog =
			new EditOpslagDialog( conn, parentObject, opslagFilterString );

		    // Check if a new opslag record has been inserted
		    if ( editOpslagDialog.opslagUpdated( ) ) {
			// Get the id of the new opslag record
			int selectedOpslagId = editOpslagDialog.getOpslagId( );

			// Setup the opslag combo box again
			opslagComboBox.setupOpslagComboBox( selectedOpslagId );
		    }
		}
	    }
	}
	opslagComboBox.addActionListener( new SelectOpslagActionListener( ) );

	JButton filterOpslagButton = new JButton( "Filter" );
	filterOpslagButton.setActionCommand( "filterOpslag" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterOpslagButton, constraints );

	class FilterOpslagActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		opslagFilterString = opslagComboBox.filterOpslagComboBox( );
	    }
	}
	filterOpslagButton.addActionListener( new FilterOpslagActionListener( ) );

	JButton editOpslagButton = new JButton( "Edit" );
	editOpslagButton.setActionCommand( "editOpslag" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( editOpslagButton, constraints );

	class EditOpslagActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		// Get the selected Opslag ID
		int selectedOpslagId = opslagComboBox.getSelectedOpslagId( );

		// Check if opslag has been selected
		if ( selectedOpslagId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen Opslag geselecteerd",
						   "Edit Medium error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditOpslagDialog editOpslagDialog = new EditOpslagDialog( conn,
									  dialog,
									  selectedOpslagId );

		if ( editOpslagDialog.opslagUpdated( ) ) {
		    // Setup the opslag combo box again
		    opslagComboBox.setupOpslagComboBox( );
		}
	    }
	}
	editOpslagButton.addActionListener( new EditOpslagActionListener( ) );

	constraints.gridx = 0;
	constraints.gridy = 11;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opmerkingen:" ), constraints );

	opmerkingenTextField = new JTextField( defaultOpmerkingenString, 50 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( opmerkingenTextField, constraints );



	// Insert/cancel buttons
	JPanel buttonPanel = new JPanel( );

	class EditMediumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		boolean result = true;

		if ( actionEvent.getActionCommand( ).equals( insertMediumActionCommand ) ) {
		    result = insertMedium( );
		} else if ( actionEvent.getActionCommand( ).equals( updateMediumActionCommand ) ) {
		    result = updateMedium( );
		}

		// Any other actionCommand, including cancel, has no action
		if ( result ) {
		    dialog.setVisible( false );
		}
	    }
	}

	JButton editMediumButton = new JButton( editMediumButtonText );
	editMediumButton.setActionCommand( editMediumButtonActionCommand );
	editMediumButton.addActionListener( new EditMediumActionListener( ) );
	buttonPanel.add( editMediumButton );

	JButton cancelMediumButton = new JButton( "Cancel" );
	cancelMediumButton.setActionCommand( "cancelMedium" );
	cancelMediumButton.addActionListener( new EditMediumActionListener( ) );
	buttonPanel.add( cancelMediumButton );

	constraints.gridx = 1;
	constraints.gridy = 12;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );


	dialog.setSize( 900, 600 );
	dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	dialog.setVisible( true );
    }


    private boolean insertMedium( ) {
	String mediumTitelString = mediumTitelTextField.getText( );
	if ( mediumTitelString == null || mediumTitelString.length( ) == 0 ) {
	    JOptionPane.showMessageDialog( dialog,
					   "Medium titel niet ingevuld",
					   "Insert Medium error",
					   JOptionPane.ERROR_MESSAGE );
	    return false;
	}

	// Matcher to find single quotes in mediumTitelString, in order to replace these
	// with escaped quotes (the quadruple slashes are really necessary)
	Matcher quoteMatcher = quotePattern.matcher( mediumTitelString );
	String insertString = ( "INSERT INTO medium SET medium_titel = '" +
				quoteMatcher.replaceAll( "\\\\'" ) + "'" );

	String uitvoerendenString = uitvoerendenTextField.getText( );
	if ( uitvoerendenString != null ) {
	    if ( uitvoerendenString.length( ) > 0 ) {
		// Matcher to find single quotes in uitvoerendenString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		quoteMatcher = quotePattern.matcher( uitvoerendenString );
		insertString += ", uitvoerenden = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	    }
	}

	int mediumTypeId = mediumTypeComboBox.getSelectedMediumTypeId( );
	if ( mediumTypeId != 0 ) insertString += ", medium_type_id = " + mediumTypeId;

	int mediumStatusId = mediumStatusComboBox.getSelectedMediumStatusId( );
	if ( mediumStatusId == 0 ) {
	    JOptionPane.showMessageDialog( dialog,
					   "Medium status niet ingevuld",
					   "Insert Medium error",
					   JOptionPane.ERROR_MESSAGE );
	    return false;
	}
	insertString += ", medium_status_id = " + mediumStatusId;

	int aantal = ( ( Integer )aantalSpinner.getValue( ) ).intValue( );
	if ( aantal != 0 ) insertString += ", aantal = " + aantal;

	int labelId = labelComboBox.getSelectedLabelId( );
	if ( labelId != 0 ) insertString += ", label_id = " + labelId;

	String labelNummerString = labelNummerTextField.getText( );
	if ( labelNummerString != null ) {
	    if ( labelNummerString.length( ) > 0 ) {
		insertString += ", label_nummer = '" + labelNummerString + "'";
	    }
	}

	SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
	String aankoopDatumString = dateFormat.format( ( Date )aankoopDatumSpinner.getValue( ) );
	if ( aankoopDatumString != null ) {
	    if ( aankoopDatumString.length( ) > 0 ) {
		insertString += ", aankoop_datum = '" + aankoopDatumString + "'";
	    }
	}

	int genreId = genreComboBox.getSelectedGenreId( );
	if ( genreId != 0 ) insertString += ", genre_id = " + genreId;

	int subgenreId = subgenreComboBox.getSelectedSubgenreId( );
	if ( subgenreId != 0 ) insertString += ", subgenre_id = " + subgenreId;

	int selectedOpslagId = opslagComboBox.getSelectedOpslagId( );
	if ( selectedOpslagId != 0 ) insertString += ", opslag_id = " + selectedOpslagId;

	String opmerkingenString = opmerkingenTextField.getText( );
	if ( opmerkingenString != null ) {
	    if ( opmerkingenString.length( ) > 0 ) {
		// Matcher to find single quotes in opmerkingenString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		quoteMatcher = quotePattern.matcher( opmerkingenString );
		insertString += ", opmerkingen = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	    }
	}

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( medium_id ) FROM medium" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for medium_id in medium" );
		return false;
	    }
	    mediumId = resultSet.getInt( 1 ) + 1;
	    insertString += ", medium_id = " + mediumId;

	    logger.fine( "insertString: " + insertString );
	    nUpdate = statement.executeUpdate( insertString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not insert in medium" );
	    	return false;
	    }
	} catch ( SQLException ex ) {
	    logger.severe( "SQLException: " + ex.getMessage( ) );
	    return false;
	}

	return true;
    }

    String updateString = null;

    private void addToUpdateString( String additionalUpdateString ) {
	if ( updateString == null ) {
	    updateString = additionalUpdateString;
	} else {
	    updateString += ", " + additionalUpdateString;
	}
    }

    private boolean updateMedium( ) {
	// Initialise string holding the update query
	updateString = null;

	String mediumTitelString = mediumTitelTextField.getText( );
	// Check if medium titel changed
	if ( !mediumTitelString.equals( defaultMediumTitelString ) ) {
	    if ( mediumTitelString == null || mediumTitelString.length( ) == 0 ) {
		JOptionPane.showMessageDialog( dialog,
					       "Medium titel niet ingevuld",
					       "Insert Medium error",
					       JOptionPane.ERROR_MESSAGE );
		return false;
	    }

	    // Matcher to find single quotes in mediumTitelString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    Matcher quoteMatcher = quotePattern.matcher( mediumTitelString );
	    addToUpdateString( "medium_titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	String uitvoerendenString = uitvoerendenTextField.getText( );
	// Check if uitvoerenden changed (allow for empty string)
	// (do not update when default was NULL and text field is empty)
	if ( ( ( defaultUitvoerendenString != null ) || ( uitvoerendenString.length( ) > 0 ) ) &&
	     ( !uitvoerendenString.equals( defaultUitvoerendenString ) ) ) {
	    if ( uitvoerendenString.length( ) == 0 ) {
		addToUpdateString( "uitvoerenden = NULL" );
	    } else {
		// Matcher to find single quotes in uitvoerendenString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( uitvoerendenString );
		addToUpdateString( "uitvoerenden = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    }
	}

	int mediumTypeId = mediumTypeComboBox.getSelectedMediumTypeId( );
	// Check if mediumTypeId changed to a non-zero value
	if ( ( mediumTypeId != 0 ) && ( mediumTypeId != defaultMediumTypeId ) ) {
	    addToUpdateString( "medium_type_id = " + mediumTypeId );
	}

	int mediumStatusId = mediumStatusComboBox.getSelectedMediumStatusId( );
	// Check if mediumStatusId changed to a non-zero value
	if ( ( mediumStatusId != 0 ) && ( mediumStatusId != defaultMediumStatusId ) ) {
	    addToUpdateString( "medium_status_id = " + mediumStatusId );
	}

	int aantal = ( ( Integer )aantalSpinner.getValue( ) ).intValue( );
	// Check if aantal changed
	if ( aantal != defaultAantal ) {
	    if ( aantal == 0 ) {
		addToUpdateString( "aantal = NULL" );
	    } else {
		addToUpdateString( "aantal = " + aantal );
	    }
	}

	int labelId = labelComboBox.getSelectedLabelId( );
	// Check if labelId changed (allow for a zero value)
	if ( labelId != defaultLabelId ) {
	    if ( labelId == 0 ) {
		addToUpdateString( "label_id = NULL" );
	    } else {
		addToUpdateString( "label_id = " + labelId );
	    }
	}

	String labelNummerString = labelNummerTextField.getText( );
	// Check if label nummer changed (allow for empty string)
	// (do not update when default was NULL and text field is empty)
	if ( ( ( defaultLabelNummerString != null ) || ( labelNummerString.length( ) > 0 ) ) &&
	     ( !labelNummerString.equals( defaultLabelNummerString ) ) ) {
	    if ( labelNummerString.length( ) == 0 ) {
		addToUpdateString( "label_nummer = NULL" );
	    } else {
		addToUpdateString( "label_nummer = '" + labelNummerString + "'" );
	    }
	}

	Date aankoopDatumDate = ( Date )aankoopDatumSpinner.getValue( );
	// Check if aankoopDatumDate changed
	if ( !aankoopDatumDate.equals( defaultAankoopDatumDate ) ) {
	    SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );
	    String aankoopDatumString = dateFormat.format( ( Date )aankoopDatumSpinner.getValue( ) );
	    if ( aankoopDatumString != null ) {
		if ( aankoopDatumString.length( ) > 0 ) {
		    addToUpdateString( "aankoop_datum = '" + aankoopDatumString + "'" );
		}
	    }
	}

	int selectedGenreId = genreComboBox.getSelectedGenreId( );
	// Check if genreId changed to a non-zero value
	if ( ( selectedGenreId != defaultGenreId ) && ( selectedGenreId != 0 ) ) {
	    addToUpdateString( "genre_id = " + selectedGenreId );
	}

	int selectedSubgenreId = subgenreComboBox.getSelectedSubgenreId( );
	// Check if subgenreId changed to a non-zero value
	if ( ( selectedSubgenreId != defaultSubgenreId ) && ( selectedSubgenreId != 0 ) ) {
	    addToUpdateString( "subgenre_id = " + selectedSubgenreId );
	}

	int selectedOpslagId = opslagComboBox.getSelectedOpslagId( );
	// Check if opslagId changed to a non-zero value
	if ( ( selectedOpslagId != defaultOpslagId ) && ( selectedOpslagId != 0 ) ) {
	    addToUpdateString( "opslag_id = " + selectedOpslagId );
	}

	String opmerkingenString = opmerkingenTextField.getText( );
	// Check if opmerkingen changed (allow for empty string)
	// (do not update when default was NULL and text field is empty)
	if ( ( ( defaultOpmerkingenString != null ) || ( opmerkingenString.length( ) > 0 ) ) &&
	     ( !opmerkingenString.equals( defaultOpmerkingenString ) ) ) {
	    if ( opmerkingenString.length( ) == 0 ) {
		addToUpdateString( "opmerkingen = NULL" );
	    } else {
		// Matcher to find single quotes in opmerkingenString, in order to replace these
		// with escaped quotes (the quadruple slashes are really necessary)
		Matcher quoteMatcher = quotePattern.matcher( opmerkingenString );
		addToUpdateString( "opmerkingen = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	    }
	}

	// Check if any update is necessary at all
	if ( updateString == null ) {
	    logger.info( "No update necessary" );
	    return true;
	}

	updateString  = "UPDATE medium SET " + updateString;
	updateString += " WHERE medium_id = " + mediumId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update in medium" );
	    	return false;
	    }
	} catch ( SQLException ex ) {
	    logger.severe( "SQLException: " + ex.getMessage( ) );
	    return false;
	}

	return true;
    }

    public boolean mediumUpdated( ) { return nUpdate > 0; }

    public String getMediumTitelString( ) { return mediumTitelTextField.getText( ); };

    public int getMediumId( ) { return mediumId; }
}
