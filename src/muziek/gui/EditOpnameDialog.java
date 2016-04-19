//
// Project:	muziek
// Component:	gui
// File:	EditOpnameDialog.java
// Description:	Dialog for inserting or updating a record in opname
// Author:	Chris van Engelen
// History:	2005/05/01: Initial version
//		2009/01/01: Add selection on Componist-Persoon
//

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
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.text.*;


public class EditOpnameDialog {
    final private Logger logger = Logger.getLogger( "muziek.gui.EditOpnameDialog" );

    Connection conn;
    Object parentObject;
    JDialog dialog;

    int selectedComponistenPersoonId = 0;
    int selectedComponistenId = 0;
    String selectedComponistenString = null;

    int selectedGenreId = 0;
    String selectedGenreString = null;

    int selectedTypeId = 0;
    String selectedTypeString = null;

    OpnameKey opnameKey = new OpnameKey( );

    MediumComboBox mediumComboBox;
    int defaultMediumId = 0;
    String mediumFilterString = null;

    OpusComboBox opusComboBox;
    int defaultOpusId = 0;
    String opusFilterString = null;

    int defaultOpnameNummer = 0;
    JSpinner opnameNummerSpinner;

    TracksTableModel tracksTableModel;
    JTable tracksTable;

    MusiciComboBox musiciComboBox;
    int defaultMusiciId = 0;
    int defaultMusiciPersoonId = 0;
    int defaultMusiciEnsembleId = 0;
    String musiciFilterString = null;

    OpnamePlaatsComboBox opnamePlaatsComboBox;
    int defaultOpnamePlaatsId = 0;
    String opnamePlaatsFilterString = null;

    OpnameDatumComboBox opnameDatumComboBox;
    int defaultOpnameDatumId = 0;
    String opnameDatumFilterString = null;

    ProducersComboBox producersComboBox;
    int defaultProducersId = 0;
    String producersFilterString = null;

    String defaultOpnameTechniekString = null;
    String opnameTechniekString = null;

    String defaultMixTechniekString = null;
    String mixTechniekString = null;

    int nUpdate = 0;

    final String insertOpnameActionCommand = "insertOpname";
    final String updateOpnameActionCommand = "updateOpname";

    // Constructor for inserting a record in opname
    public EditOpnameDialog( Connection conn,
			     Object     parentObject,
			     int	selectedMediumId,
			     String	opusFilterString,
			     int	selectedComponistenPersoonId,
			     int        selectedComponistenId,
			     String     selectedComponistenString,
			     int        selectedGenreId,
			     String     selectedGenreString,
			     int        selectedTypeId,
			     String     selectedTypeString,
			     int        selectedMusiciPersoonId,
			     int        selectedMusiciId,
			     int        selectedMusiciEnsembleId,
			     int	selectedOpnameDatumId,
			     int	selectedOpnamePlaatsId,
			     int	selectedProducersId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.defaultMediumId = selectedMediumId;
	this.opusFilterString = opusFilterString;
	this.selectedComponistenPersoonId = selectedComponistenPersoonId;
	this.selectedComponistenId = selectedComponistenId;
	this.selectedComponistenString = selectedComponistenString;
	this.selectedGenreId = selectedGenreId;
	this.selectedGenreString = selectedGenreString;
	this.selectedTypeId = selectedTypeId;
	this.selectedTypeString = selectedTypeString;
	this.defaultMusiciPersoonId = selectedMusiciPersoonId;
	this.defaultMusiciId = selectedMusiciId;
	this.defaultMusiciEnsembleId = selectedMusiciEnsembleId;
	this.defaultOpnameDatumId = selectedOpnameDatumId;
	this.defaultOpnamePlaatsId = selectedOpnamePlaatsId;
	this.defaultProducersId = selectedProducersId;

	setupOpnameDialog( "Insert opname", "Insert", insertOpnameActionCommand );
    }


    // Constructor for updating an existing record in opname
    public EditOpnameDialog( Connection conn,
			     Object     parentObject,
			     OpnameKey  opnameKey ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.opnameKey = opnameKey;
	this.defaultMediumId = opnameKey.getMediumId( );
	this.defaultOpusId = opnameKey.getOpusId( );
	this.defaultOpnameNummer = opnameKey.getOpnameNummer( );
	this.defaultMusiciId = opnameKey.getMusiciId( );

	// Reset default musici persoon, ensemble, for selection in musici combo box
	this.defaultMusiciPersoonId = 0;
	this.defaultMusiciEnsembleId = 0;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT opname_plaats_id, opname_datum_id, " +
							  "producers_id, opname_techniek, mix_techniek " +
							  "FROM opname " +
							  "WHERE medium_id = " + defaultMediumId +
							  " AND opus_id = " + defaultOpusId +
							  " AND opname_nummer = " + defaultOpnameNummer +
							  " AND musici_id = " + defaultMusiciId );

	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for opname key " + opnameKey + " in opname" );
		return;
	    }

	    defaultOpnamePlaatsId = resultSet.getInt( 1 );
	    defaultOpnameDatumId = resultSet.getInt( 2 );
	    defaultProducersId = resultSet.getInt( 3 );
	    defaultOpnameTechniekString = resultSet.getString( 4 );
	    defaultMixTechniekString = resultSet.getString( 5 );

	    // Make copies of default opname techniek and default mix techniek, so that if not touched,
	    // these will be equal to the default. Note: do not copy variables, but really make new objects,
	    // otherwise changing the copy would result in also changing the default!
	    if ( defaultOpnameTechniekString != null ) {
		opnameTechniekString = new String( defaultOpnameTechniekString );
	    }
	    if ( defaultMixTechniekString != null ) {
		mixTechniekString = new String( defaultMixTechniekString );
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupOpnameDialog( "Edit opname", "Update", updateOpnameActionCommand );
    }

    // Setup opname dialog
    private void setupOpnameDialog( String dialogTitle,
				    String editOpnameButtonText,
				    String editOpnameButtonActionCommand ) {
	// Create modal dialog for editing opname record
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


	//////////////////////////////////////////
	// Medium Combo Box, Filter, Edit buttons
	//////////////////////////////////////////

	// Setup a JComboBox for medium with the selected medium ID
	mediumComboBox = new MediumComboBox( conn, dialog, defaultMediumId );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Medium:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( mediumComboBox, constraints );

	class SelectMediumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		int selectedMediumId = 0;

		// Check if a medium record needs to be inserted
		if ( mediumComboBox.newMediumSelected( ) ) {
		    // Insert new medium record
		    EditMediumDialog editMediumDialog =
			new EditMediumDialog( conn, parentObject,
					      mediumFilterString,
					      null,
					      selectedGenreId,
					      0, 0, 0, 0 );

		    // Check if a new medium record has been inserted
		    if ( editMediumDialog.mediumUpdated( ) ) {
			// Get the id of the new medium record
			selectedMediumId = editMediumDialog.getMediumId( );

			// Setup the medium combo box again
			mediumComboBox.setupMediumComboBox( selectedMediumId );
		    }
		} else {
		    // Get the selected medium ID from the combo box
		    selectedMediumId = mediumComboBox.getSelectedMediumId( );
		}

		// Setup the tracks table
		tracksTableModel.initTable( selectedMediumId,
					    opusComboBox.getSelectedOpusId( ) );
	    }
	}
	mediumComboBox.addActionListener( new SelectMediumActionListener( ) );

	JButton filterMediumButton = new JButton( "Filter" );
	filterMediumButton.setActionCommand( "filterMedium" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterMediumButton, constraints );

	class FilterMediumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		mediumFilterString = mediumComboBox.filterMediumComboBox( );
	    }
	}
	filterMediumButton.addActionListener( new FilterMediumActionListener( ) );

	JButton editMediumButton = new JButton( "Edit" );
	editMediumButton.setActionCommand( "editMedium" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( editMediumButton, constraints );

	class EditMediumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Medium ID
		int selectedMediumId = mediumComboBox.getSelectedMediumId( );

		// Check if medium has been selected
		if ( selectedMediumId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen medium geselecteerd",
						   "Edit opname error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditMediumDialog editMediumDialog =
		    new EditMediumDialog( conn, dialog, selectedMediumId );

		if ( editMediumDialog.mediumUpdated( ) ) {
		    // Setup the medium combo box again
		    mediumComboBox.setupMediumComboBox( );

		    // Setup the tracks table
		    tracksTableModel.initTable( mediumComboBox.getSelectedMediumId( ),
						opusComboBox.getSelectedOpusId( ) );
		}
	    }
	}
	editMediumButton.addActionListener( new EditMediumActionListener( ) );


	///////////////////////////////////////////////////
	// Opus pre-selection on genre, type, componisten
	///////////////////////////////////////////////////

	if ( ( selectedComponistenPersoonId != 0 ) ||
	     ( selectedComponistenId != 0 ) ||
	     ( selectedGenreId != 0 ) ||
	     ( selectedTypeId != 0 ) ) {
	    constraints.gridx = 0;
	    constraints.gridy = 1;
	    constraints.gridwidth = 1;
	    container.add( new JLabel( "Opus selection:" ), constraints );

	    JPanel opusSelectionPanel = new JPanel( );

	    final JLabel componistenSelectionLabel = new JLabel( "" );
	    if ( ( selectedComponistenPersoonId != 0 ) || ( selectedComponistenId != 0 ) ) {
		componistenSelectionLabel.setText( "Componisten: " + selectedComponistenString );
		opusSelectionPanel.add( componistenSelectionLabel );
	    }

	    final JLabel genreSelectionLabel = new JLabel( "" );
	    if ( selectedGenreId != 0 ) {
		genreSelectionLabel.setText( "Genre: " + selectedGenreString + "  " );
		opusSelectionPanel.add( genreSelectionLabel );
	    }

	    final JLabel typeSelectionLabel = new JLabel( "" );
	    if ( selectedTypeId != 0 ) {
		typeSelectionLabel.setText( "Type: " + selectedTypeString );
		opusSelectionPanel.add( typeSelectionLabel );
	    }

	    constraints.gridx = GridBagConstraints.RELATIVE;
	    container.add( opusSelectionPanel, constraints );

	    JButton resetOpusSelectionButton = new JButton( "Reset" );
	    resetOpusSelectionButton.setActionCommand( "resetOpusSelection" );
	    constraints.gridx = GridBagConstraints.RELATIVE;
	    constraints.gridwidth = 1;
	    container.add( resetOpusSelectionButton, constraints );

	    class ResetOpusSelectionActionListener implements ActionListener {
		public void actionPerformed( ActionEvent actionEvent ) {
		    opusFilterString = null;
		    selectedComponistenPersoonId = 0;
		    selectedComponistenId = 0;
		    selectedGenreId = 0;
		    selectedTypeId = 0;
		    componistenSelectionLabel.setText( "" );
		    genreSelectionLabel.setText( "" );
		    typeSelectionLabel.setText( "" );
		    opusComboBox.setupOpusComboBox( opusFilterString,
						    selectedComponistenPersoonId,
						    selectedComponistenId,
						    selectedGenreId,
						    selectedTypeId );
		}
	    }
	    resetOpusSelectionButton.addActionListener( new ResetOpusSelectionActionListener( ) );
	}


	//////////////////////////////////////////
	// Opus Combo Box, Filter, Edit buttons
	//////////////////////////////////////////

	// Setup a JComboBox for opus
	if ( editOpnameButtonActionCommand.equals( insertOpnameActionCommand ) ) {
	    opusComboBox = new OpusComboBox( conn, dialog,
					     opusFilterString,
					     selectedComponistenPersoonId,
					     selectedComponistenId,
					     selectedGenreId,
					     selectedTypeId );
	} else {
	    opusComboBox = new OpusComboBox( conn, dialog, defaultOpusId );
	}
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opus:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( opusComboBox, constraints );

	class SelectOpusActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		int selectedOpusId = 0;

		// Check if a opus record needs to be inserted
		if ( opusComboBox.newOpusSelected( ) ) {
		    // Insert new opus record
		    EditOpusDialog editOpusDialog =
			new EditOpusDialog( conn, parentObject, 
					    opusFilterString,
					    selectedComponistenPersoonId,
					    selectedComponistenId,
					    selectedGenreId,
					    selectedTypeId );

		    // Check if a new opus record has been inserted
		    if ( editOpusDialog.opusUpdated( ) ) {
			// Get the id of the new opus record
			selectedOpusId = editOpusDialog.getOpusId( );

			// Setup the opus combo box again
			opusComboBox.setupOpusComboBox( selectedOpusId );
		    }
		} else {
		    // Get the selected opus ID from the combo box
		    selectedOpusId = opusComboBox.getSelectedOpusId( );
		}

		// Setup the tracks table
		tracksTableModel.initTable( mediumComboBox.getSelectedMediumId( ),
					    selectedOpusId );
	    }
	}
	opusComboBox.addActionListener( new SelectOpusActionListener( ) );

	JButton filterOpusButton = new JButton( "Filter" );
	filterOpusButton.setActionCommand( "filterOpus" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterOpusButton, constraints );

	class FilterOpusActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		opusFilterString = opusComboBox.filterOpusComboBox( );
	    }
	}
	filterOpusButton.addActionListener( new FilterOpusActionListener( ) );

	JButton editOpusButton = new JButton( "Edit" );
	editOpusButton.setActionCommand( "editOpus" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( editOpusButton, constraints );

	class EditOpusActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Opus ID
		int selectedOpusId = opusComboBox.getSelectedOpusId( );

		// Check if opus has been selected
		if ( selectedOpusId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen opus geselecteerd",
						   "Edit opname error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditOpusDialog editOpusDialog =
		    new EditOpusDialog( conn, dialog, selectedOpusId );

		if ( editOpusDialog.opusUpdated( ) ) {
		    // Setup the opus combo box again
		    opusComboBox.setupOpusComboBox( );

		    // Setup the tracks table
		    tracksTableModel.initTable( mediumComboBox.getSelectedMediumId( ),
						opusComboBox.getSelectedOpusId( ) );
		}
	    }
	}
	editOpusButton.addActionListener( new EditOpusActionListener( ) );


	//////////////////////////////////////////
	// Opname nummer spinner
	//////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opname nummer:" ), constraints );

	SpinnerNumberModel opnameNummerSpinnerNumberModel = new SpinnerNumberModel( defaultOpnameNummer, 0, 9, 1 );
	opnameNummerSpinner = new JSpinner( opnameNummerSpinnerNumberModel );
	JFormattedTextField opnameNummerSpinnerTextField = ( ( JSpinner.DefaultEditor )opnameNummerSpinner.getEditor( ) ).getTextField( );
	if ( opnameNummerSpinnerTextField != null ) {
	    opnameNummerSpinnerTextField.setColumns( 1 );
	}
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( opnameNummerSpinner, constraints );


	////////////////////////////////////////////////
	// tracks Table, Replace, Remove Buttons
	////////////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 1;
	// Set gridheigth to allow for two buttons next to the table
	constraints.gridheight = 3;
	container.add( new JLabel( "Tracks tabel:" ), constraints );

	// Create tracks table model with connection and current opus id
	tracksTableModel = new TracksTableModel( conn,
						 opnameKey.getMediumId( ),
						 opnameKey.getOpusId( ) );

	// Create tracks table from tracks table model
	tracksTable = new JTable( tracksTableModel );

	// Setup a table with tracks records for this opus
	// tracksTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	tracksTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );
	tracksTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth( 90 );  // submedium
	tracksTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 60 );  // track #
	tracksTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 70 );  // tijd
	tracksTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth( 90 );  // opus-deel #
	tracksTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth( 340 ); // opus-deel titel
	// Set vertical size just enough for 10 entries
	tracksTable.setPreferredScrollableViewportSize( new Dimension( 670, 160 ) );

	// Create time formatter
	try {
	    MaskFormatter timeMaskFormatter = new MaskFormatter( "##:##:##" );
	    timeMaskFormatter.setPlaceholderCharacter( '0' );
	    timeMaskFormatter.setAllowsInvalid( false );
	    timeMaskFormatter.setOverwriteMode( true );

	    // Set renderer for String objects
	    class TimeStringRenderer extends JFormattedTextField implements TableCellRenderer {
		TimeStringRenderer( MaskFormatter maskFormatter ) {
		    super( maskFormatter );
		}

		public Component getTableCellRendererComponent( JTable table,
								Object object,
								boolean isSelected,
								boolean hasFocus,
								int row, int column ) {
		    switch ( column ) {
		    case 2:		// Track time
			this.setText( ( String )object );
			break;

		    default:	// Unexpected column: just return the string related to the object
			logger.severe( "Unexpected column: " + column );
			this.setText( object.toString( ) );
		    }

		    return this;
		}
	    }
	    TimeStringRenderer timeStringRenderer = new TimeStringRenderer( timeMaskFormatter );
	    final Border emptyBorder = BorderFactory.createEmptyBorder( );
	    timeStringRenderer.setBorder( emptyBorder );
	    tracksTable.getColumnModel( ).getColumn( 2 ).setCellRenderer( timeStringRenderer );
	} catch( ParseException parseException ) {
	    logger.severe( "time parse exception: " + parseException.getMessage( ) );
	}

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( new JScrollPane( tracksTable ), constraints );

	// Define Add button next to table
	final JButton addTrackToTableButton = new JButton( "Add" );
	addTrackToTableButton.setActionCommand( "addTrackToTable" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 2;
	constraints.gridheight = 1;
	container.add( addTrackToTableButton, constraints );

	// Class to handle Add button: uses tracksListSelectionListener
	class AddTrackToTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Add row to table
		tracksTableModel.addRow( );
	    }
	}
	addTrackToTableButton.addActionListener( new AddTrackToTableActionListener( ) );

	// Define Insert button next to table
	final JButton insertTrackInTableButton = new JButton( "Insert" );
	insertTrackInTableButton.setActionCommand( "insertTrackInTable" );
	insertTrackInTableButton.setEnabled( false );
	constraints.gridy = 5;
	container.add( insertTrackInTableButton, constraints );

	// Define Remove button next to table
	final JButton removeTrackFromTableButton = new JButton( "Remove" );
	removeTrackFromTableButton.setActionCommand( "removeTrackFromTable" );
	removeTrackFromTableButton.setEnabled( false );
	constraints.gridy = 6;
	container.add( removeTrackFromTableButton, constraints );


	// Get the selection model related to the tracks table
	final ListSelectionModel tracksListSelectionModel = tracksTable.getSelectionModel( );

	class TracksListSelectionListener implements ListSelectionListener {
	    int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( tracksListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    insertTrackInTableButton.setEnabled( false );
		    removeTrackFromTableButton.setEnabled( false );
		    return;
		}

		selectedRow = tracksListSelectionModel.getMinSelectionIndex( );
		insertTrackInTableButton.setEnabled( true );
		removeTrackFromTableButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add tracksListSelectionListener object to the selection model of the tracks table
	final TracksListSelectionListener tracksListSelectionListener = new TracksListSelectionListener( );
	tracksListSelectionModel.addListSelectionListener( tracksListSelectionListener );


	// Class to handle Insert button: uses tracksListSelectionListener
	class InsertTrackInTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		int selectedRow = tracksListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen track geselecteerd",
						   "Edit opname error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Insert selected row in table
		tracksTableModel.insertRow( selectedRow );
	    }
	}
	insertTrackInTableButton.addActionListener( new InsertTrackInTableActionListener( ) );


	// Class to handle Remove button: uses tracksListSelectionListener
	class RemoveTrackFromTableActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		int selectedRow = tracksListSelectionListener.getSelectedRow( );
		if ( selectedRow < 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen tracks geselecteerd",
						   "Edit opname error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Remove selected row in table
		tracksTableModel.removeRow( selectedRow );
	    }
	}
	removeTrackFromTableButton.addActionListener( new RemoveTrackFromTableActionListener( ) );


	//////////////////////////////////////////
	// Musici Combo Box, Filter, Edit buttons
	//////////////////////////////////////////

	// Setup a JComboBox for musici
	if ( defaultMusiciId != 0 ) {
	    musiciComboBox = new MusiciComboBox( conn, dialog,
						 defaultMusiciId );
	} else if ( ( defaultMusiciPersoonId != 0 ) || ( defaultMusiciEnsembleId != 0 ) ) {
	    musiciComboBox = new MusiciComboBox( conn, dialog,
						 defaultMusiciPersoonId,
						 defaultMusiciEnsembleId );
	} else {
	    musiciComboBox = new MusiciComboBox( conn, dialog );
	}
	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.gridheight = 1;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Musici:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( musiciComboBox, constraints );

	class SelectMusiciActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a musici record needs to be inserted
		if ( musiciComboBox.newMusiciSelected( ) ) {
		    // Insert new musici record
		    EditMusiciDialog editMusiciDialog =
			new EditMusiciDialog( conn, parentObject, musiciFilterString );

		    // Check if a new musici record has been inserted
		    if ( editMusiciDialog.musiciUpdated( ) ) {
			// Get the id of the new musici record
			int selectedMusiciId = editMusiciDialog.getMusiciId( );

			// Setup the musici combo box again
			musiciComboBox.setupMusiciComboBox( selectedMusiciId );
		    }
		}
	    }
	}
	musiciComboBox.addActionListener( new SelectMusiciActionListener( ) );

	JButton filterMusiciButton = new JButton( "Filter" );
	filterMusiciButton.setActionCommand( "filterMusici" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( filterMusiciButton, constraints );

	class FilterMusiciActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		musiciFilterString = musiciComboBox.filterMusiciComboBox( );
	    }
	}
	filterMusiciButton.addActionListener( new FilterMusiciActionListener( ) );

	JButton editMusiciButton = new JButton( "Edit" );
	editMusiciButton.setActionCommand( "editMusici" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( editMusiciButton, constraints );

	class EditMusiciActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Musici ID
		int selectedMusiciId = musiciComboBox.getSelectedMusiciId( );

		// Check if musici has been selected
		if ( selectedMusiciId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen musici geselecteerd",
						   "Edit opname error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditMusiciDialog editMusiciDialog =
		    new EditMusiciDialog( conn, dialog, selectedMusiciId );

		if ( editMusiciDialog.musiciUpdated( ) ) {
		    // Setup the musici combo box again
		    musiciComboBox.setupMusiciComboBox( );
		}
	    }
	}
	editMusiciButton.addActionListener( new EditMusiciActionListener( ) );


	//////////////////////////////////////////
	// OpnamePlaats Combo Box, Filter, Edit buttons
	//////////////////////////////////////////

	// Setup a JComboBox for opname_plaats
	opnamePlaatsComboBox = new OpnamePlaatsComboBox( conn, dialog, defaultOpnamePlaatsId );
	constraints.gridx = 0;
	constraints.gridy = 9;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opname plaats:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( opnamePlaatsComboBox, constraints );

	class SelectOpnamePlaatsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a opname plaats record needs to be inserted
		if ( opnamePlaatsComboBox.newOpnamePlaatsSelected( ) ) {
		    // Insert new opname plaats record
		    EditOpnamePlaatsDialog editOpnamePlaatsDialog =
			new EditOpnamePlaatsDialog( conn, parentObject, opnamePlaatsFilterString );

		    // Check if a new opname plaats record has been inserted
		    if ( editOpnamePlaatsDialog.opnamePlaatsUpdated( ) ) {
			// Get the id of the new opname plaats record
			int selectedOpnamePlaatsId = editOpnamePlaatsDialog.getOpnamePlaatsId( );

			// Setup the opname plaats combo box again
			opnamePlaatsComboBox.setupOpnamePlaatsComboBox( selectedOpnamePlaatsId );
		    }
		}
	    }
	}
	opnamePlaatsComboBox.addActionListener( new SelectOpnamePlaatsActionListener( ) );

	JButton filterOpnamePlaatsButton = new JButton( "Filter" );
	filterOpnamePlaatsButton.setActionCommand( "filterOpnamePlaats" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterOpnamePlaatsButton, constraints );

	class FilterOpnamePlaatsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		opnamePlaatsFilterString = opnamePlaatsComboBox.filterOpnamePlaatsComboBox( );
	    }
	}
	filterOpnamePlaatsButton.addActionListener( new FilterOpnamePlaatsActionListener( ) );


	JButton editOpnamePlaatsButton = new JButton( "Edit" );
	editOpnamePlaatsButton.setActionCommand( "editOpnamePlaats" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( editOpnamePlaatsButton, constraints );

	class EditOpnamePlaatsActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected opname_plaats ID
		int selectedOpnamePlaatsId = opnamePlaatsComboBox.getSelectedOpnamePlaatsId( );

		// Check if opname plaats has been selected
		if ( selectedOpnamePlaatsId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen opname plaats geselecteerd",
						   "Edit opname error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditOpnamePlaatsDialog editOpnamePlaatsDialog =
		    new EditOpnamePlaatsDialog( conn, dialog, selectedOpnamePlaatsId );

		if ( editOpnamePlaatsDialog.opnamePlaatsUpdated( ) ) {
		    // Setup the opname_plaats combo box again
		    opnamePlaatsComboBox.setupOpnamePlaatsComboBox( );
		}
	    }
	}
	editOpnamePlaatsButton.addActionListener( new EditOpnamePlaatsActionListener( ) );


	//////////////////////////////////////////
	// OpnameDatum Combo Box, Filter, Edit buttons
	//////////////////////////////////////////

	// Setup a JComboBox for opname datum
	opnameDatumComboBox = new OpnameDatumComboBox( conn, dialog, defaultOpnameDatumId );
	constraints.gridx = 0;
	constraints.gridy = 10;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opname datum:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( opnameDatumComboBox, constraints );

	class SelectOpnameDatumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a opname datum record needs to be inserted
		if ( opnameDatumComboBox.newOpnameDatumSelected( ) ) {
		    // Insert new opname datum record
		    EditOpnameDatumDialog editOpnameDatumDialog =
			new EditOpnameDatumDialog( conn, parentObject, opnameDatumFilterString );

		    // Check if a new opname datum record has been inserted
		    if ( editOpnameDatumDialog.opnameDatumUpdated( ) ) {
			// Get the id of the new opname datum record
			int selectedOpnameDatumId = editOpnameDatumDialog.getOpnameDatumId( );

			// Setup the opname datum combo box again
			opnameDatumComboBox.setupOpnameDatumComboBox( selectedOpnameDatumId );
		    }
		}
	    }
	}
	opnameDatumComboBox.addActionListener( new SelectOpnameDatumActionListener( ) );

	JButton filterOpnameDatumButton = new JButton( "Filter" );
	filterOpnameDatumButton.setActionCommand( "filterOpnameDatum" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterOpnameDatumButton, constraints );

	class FilterOpnameDatumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		opnameDatumFilterString = opnameDatumComboBox.filterOpnameDatumComboBox( );
	    }
	}
	filterOpnameDatumButton.addActionListener( new FilterOpnameDatumActionListener( ) );

	JButton editOpnameDatumButton = new JButton( "Edit" );
	editOpnameDatumButton.setActionCommand( "editOpnameDatum" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( editOpnameDatumButton, constraints );

	class EditOpnameDatumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected opname_datum ID
		int selectedOpnameDatumId = opnameDatumComboBox.getSelectedOpnameDatumId( );

		// Check if opname datum has been selected
		if ( selectedOpnameDatumId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen opname datum geselecteerd",
						   "Edit opname error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditOpnameDatumDialog editOpnameDatumDialog =
		    new EditOpnameDatumDialog( conn, dialog, selectedOpnameDatumId );

		if ( editOpnameDatumDialog.opnameDatumUpdated( ) ) {
		    // Setup the opname datum combo box again
		    opnameDatumComboBox.setupOpnameDatumComboBox( );
		}
	    }
	}
	editOpnameDatumButton.addActionListener( new EditOpnameDatumActionListener( ) );


	//////////////////////////////////////////
	// Producers Combo Box, Filter, Edit buttons
	//////////////////////////////////////////

	// Setup a JComboBox for producers
	producersComboBox = new ProducersComboBox( conn, dialog, defaultProducersId );
	constraints.gridx = 0;
	constraints.gridy = 11;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Producers:" ), constraints );

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 1;
	container.add( producersComboBox, constraints );

	class SelectProducersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Check if a producers record needs to be inserted
		if ( producersComboBox.newProducersSelected( ) ) {
		    // Insert new producers record
		    EditProducersDialog editProducersDialog =
			new EditProducersDialog( conn, parentObject, producersFilterString );

		    // Check if a new producers record has been inserted
		    if ( editProducersDialog.producersUpdated( ) ) {
			// Get the id of the new producers record
			int selectedProducersId = editProducersDialog.getProducersId( );

			// Setup the producers combo box again
			producersComboBox.setupProducersComboBox( selectedProducersId );
		    }
		}
	    }
	}
	producersComboBox.addActionListener( new SelectProducersActionListener( ) );

	JButton filterProducersButton = new JButton( "Filter" );
	filterProducersButton.setActionCommand( "filterProducers" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( filterProducersButton, constraints );

	class FilterProducersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		producersFilterString = producersComboBox.filterProducersComboBox( );
	    }
	}
	filterProducersButton.addActionListener( new FilterProducersActionListener( ) );

	JButton editProducersButton = new JButton( "Edit" );
	editProducersButton.setActionCommand( "editProducers" );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( editProducersButton, constraints );

	class EditProducersActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected Producers ID
		int selectedProducersId = producersComboBox.getSelectedProducersId( );

		// Check if producers has been selected
		if ( selectedProducersId == 0 ) {
		    JOptionPane.showMessageDialog( dialog,
						   "Geen producers geselecteerd",
						   "Edit opname error",
						   JOptionPane.ERROR_MESSAGE );
		    return;
		}

		// Do dialog
		EditProducersDialog editProducersDialog =
		    new EditProducersDialog( conn, dialog, selectedProducersId );

		if ( editProducersDialog.producersUpdated( ) ) {
		    // Setup the producers combo box again
		    producersComboBox.setupProducersComboBox( );
		}
	    }
	}
	editProducersButton.addActionListener( new EditProducersActionListener( ) );


	//////////////////////////////////////////
	// Opname Techniek Radio Buttons
	//////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 12;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opname techniek:" ), constraints );

	JPanel opnameTechniekPanel = new JPanel( );
	ButtonGroup opnameTechniekButtonGroup = new ButtonGroup( );

	class opnameTechniekActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		opnameTechniekString = actionEvent.getActionCommand( );
		logger.finest( "opnameTechniekString: " + opnameTechniekString );
	    }
	}
	opnameTechniekActionListener opnameTechniekActionListener = new opnameTechniekActionListener( );

	JRadioButton analogOpnameTechniekRadioButton = new JRadioButton( "Analog" );
	analogOpnameTechniekRadioButton.setActionCommand( "A" );
	analogOpnameTechniekRadioButton.addActionListener( opnameTechniekActionListener );
	opnameTechniekPanel.add( analogOpnameTechniekRadioButton );
	opnameTechniekButtonGroup.add( analogOpnameTechniekRadioButton );

	JRadioButton digitalOpnameTechniekRadioButton = new JRadioButton( "Digital" );
	digitalOpnameTechniekRadioButton.setActionCommand( "D" );
	digitalOpnameTechniekRadioButton.addActionListener( opnameTechniekActionListener );
	opnameTechniekPanel.add( digitalOpnameTechniekRadioButton );
	opnameTechniekButtonGroup.add( digitalOpnameTechniekRadioButton );

	JRadioButton unknownOpnameTechniekRadioButton = new JRadioButton( "Unknown" );
	unknownOpnameTechniekRadioButton.setActionCommand( "U" );
	unknownOpnameTechniekRadioButton.addActionListener( opnameTechniekActionListener );
	opnameTechniekPanel.add( unknownOpnameTechniekRadioButton );
	opnameTechniekButtonGroup.add( unknownOpnameTechniekRadioButton );

	unknownOpnameTechniekRadioButton.setSelected( true );
	if ( defaultOpnameTechniekString != null ) {
	    if ( defaultOpnameTechniekString.equals( "A" ) ) analogOpnameTechniekRadioButton.setSelected( true );
	    if ( defaultOpnameTechniekString.equals( "D" ) ) digitalOpnameTechniekRadioButton.setSelected( true );
	}

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( opnameTechniekPanel, constraints );


	//////////////////////////////////////////
	// Mix Techniek Radio Buttons
	//////////////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 13;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Mix techniek:" ), constraints );

	JPanel mixTechniekPanel = new JPanel( );
	ButtonGroup mixTechniekButtonGroup = new ButtonGroup( );

	class mixTechniekActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		mixTechniekString = actionEvent.getActionCommand( );
		logger.finest( "mixTechniekString: " + mixTechniekString );
	    }
	}
	mixTechniekActionListener mixTechniekActionListener = new mixTechniekActionListener( );

	JRadioButton analogMixTechniekRadioButton = new JRadioButton( "Analog" );
	analogMixTechniekRadioButton.setActionCommand( "A" );
	analogMixTechniekRadioButton.addActionListener( mixTechniekActionListener );
	mixTechniekPanel.add( analogMixTechniekRadioButton );
	mixTechniekButtonGroup.add( analogMixTechniekRadioButton );

	JRadioButton digitalMixTechniekRadioButton = new JRadioButton( "Digital" );
	digitalMixTechniekRadioButton.setActionCommand( "D" );
	digitalMixTechniekRadioButton.addActionListener( mixTechniekActionListener );
	mixTechniekPanel.add( digitalMixTechniekRadioButton );
	mixTechniekButtonGroup.add( digitalMixTechniekRadioButton );

	JRadioButton unknownMixTechniekRadioButton = new JRadioButton( "Unknown" );
	unknownMixTechniekRadioButton.setActionCommand( "U" );
	unknownMixTechniekRadioButton.addActionListener( mixTechniekActionListener );
	mixTechniekPanel.add( unknownMixTechniekRadioButton );
	mixTechniekButtonGroup.add( unknownMixTechniekRadioButton );

	unknownMixTechniekRadioButton.setSelected( true );
	if ( defaultMixTechniekString != null ) {
	    if ( defaultMixTechniekString.equals( "A" ) ) analogMixTechniekRadioButton.setSelected( true );
	    if ( defaultMixTechniekString.equals( "D" ) ) digitalMixTechniekRadioButton.setSelected( true );
	}

	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.gridwidth = 3;
	container.add( mixTechniekPanel, constraints );


	//////////////////////////////////////////
	// Update/Insert, Cancel buttons
	//////////////////////////////////////////

	JPanel buttonPanel = new JPanel( );

	class EditOpnameActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		boolean result = true;

		if ( actionEvent.getActionCommand( ).equals( insertOpnameActionCommand ) ) {
		    result = insertOpname( );
		} else if ( actionEvent.getActionCommand( ).equals( updateOpnameActionCommand ) ) {
		    result = updateOpname( );
		}

		// Any other actionCommand, including cancel, has no action
		if ( result ) {
		    dialog.setVisible( false );
		}
	    }
	}

	JButton editOpnameButton = new JButton( editOpnameButtonText );
	editOpnameButton.setActionCommand( editOpnameButtonActionCommand );
	editOpnameButton.addActionListener( new EditOpnameActionListener( ) );
	buttonPanel.add( editOpnameButton );

	JButton cancelOpnameButton = new JButton( "Cancel" );
	cancelOpnameButton.setActionCommand( "cancelOpname" );
	cancelOpnameButton.addActionListener( new EditOpnameActionListener( ) );
	buttonPanel.add( cancelOpnameButton );

	constraints.gridx = 1;
	constraints.gridy = 14;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );


	dialog.setSize( 1050, 700 );
	dialog.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	dialog.setVisible( true );
    }


    private boolean insertOpname( ) {
	int selectedMediumId = mediumComboBox.getSelectedMediumId( );
	if ( selectedMediumId == 0 ) {
	    JOptionPane.showMessageDialog( dialog,
					   "Medium niet geselecteerd",
					   "Insert Opname error",
					   JOptionPane.ERROR_MESSAGE );
	    return false;
	}

	int selectedOpusId = opusComboBox.getSelectedOpusId( );
	if ( selectedOpusId == 0 ) {
	    JOptionPane.showMessageDialog( dialog,
					   "Opus niet geselecteerd",
					   "Insert Opname error",
					   JOptionPane.ERROR_MESSAGE );
	    return false;
	}

	int selectedMusiciId = musiciComboBox.getSelectedMusiciId( );
	if ( selectedMusiciId == 0 ) {
	    JOptionPane.showMessageDialog( dialog,
					   "Musici niet geselecteerd",
					   "Insert Opname error",
					   JOptionPane.ERROR_MESSAGE );
	    return false;
	}

	int opnameNummer = ( ( Integer )opnameNummerSpinner.getValue( ) ).intValue( );

	String insertString = ( "INSERT INTO opname SET" +
				" medium_id = " + selectedMediumId +
				", opus_id = " + selectedOpusId +
				", opname_nummer = " + opnameNummer +
				", musici_id = " + selectedMusiciId );

	int opnamePlaatsId = opnamePlaatsComboBox.getSelectedOpnamePlaatsId( );
	if ( opnamePlaatsId != 0 ) insertString += ", opname_plaats_id = " + opnamePlaatsId;

	int opnameDatumId = opnameDatumComboBox.getSelectedOpnameDatumId( );
	if ( opnameDatumId != 0 ) insertString += ", opname_datum_id = " + opnameDatumId;

	int producersId = producersComboBox.getSelectedProducersId( );
	if ( producersId != 0 ) insertString += ", producers_id = " + producersId;

	if ( opnameTechniekString != null ) {
	    // Check if opname techniek is not set to unknown
	    if ( !( opnameTechniekString.equals( "U" ) ) ) {
		insertString += ", opname_techniek = '" + opnameTechniekString + "'";
	    }
	}

	if ( mixTechniekString != null ) {
	    // Check if mix techniek is not set to unknown
	    if ( !( mixTechniekString.equals( "U" ) ) ) {
		insertString += ", mix_techniek = '" + mixTechniekString + "'";
	    }
	}

	logger.fine( "insertString: " + insertString );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( insertString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not insert in opname" );
	    	return false;
	    }

	    // Also insert the related records in the tracks table
	    tracksTableModel.insertTable( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
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

    private boolean updateOpname( ) {
	// Initialise string holding the update query
	updateString = null;

	int selectedMediumId = mediumComboBox.getSelectedMediumId( );
	// Check if selectedMediumId changed
	if ( selectedMediumId != opnameKey.getMediumId( ) ) {
	    // Medium Id changed: do not allow for a zero value
	    if ( selectedMediumId == 0 ) {
		JOptionPane.showMessageDialog( dialog,
					       "Medium niet geselecteerd",
					       "Update Opname error",
					       JOptionPane.ERROR_MESSAGE );
		return false;
	    }

	    // Add selectedMediumId to update string
	    addToUpdateString( "medium_id = " + selectedMediumId );
	}

	int selectedOpusId = opusComboBox.getSelectedOpusId( );
	// Check if selectedOpusId changed
	if ( selectedOpusId != opnameKey.getOpusId( ) ) {
	    // Opus Id changed: do not allow for a zero value
	    if ( selectedOpusId == 0 ) {
		JOptionPane.showMessageDialog( dialog,
					       "Opus niet geselecteerd",
					       "Update Opname error",
					       JOptionPane.ERROR_MESSAGE );
		return false;
	    }

	    // Add selectedOpusId to update string
	    addToUpdateString( "opus_id = " + selectedOpusId );
	}

	int selectedMusiciId = musiciComboBox.getSelectedMusiciId( );
	// Check if selectedMusiciId changed
	if ( selectedMusiciId != opnameKey.getMusiciId( ) ) {
	    // Musici Id changed: do not allow for a zero value
	    if ( selectedMusiciId == 0 ) {
		JOptionPane.showMessageDialog( dialog,
					       "Musici niet geselecteerd",
					       "Update Opname error",
					       JOptionPane.ERROR_MESSAGE );
		return false;
	    }

	    // Add selectedMusiciId to update string
	    addToUpdateString( "musici_id = " + selectedMusiciId );
	}

	int opnameNummer = ( ( Integer )opnameNummerSpinner.getValue( ) ).intValue( );
	// Check if opnameNummer changed
	if ( opnameNummer != opnameKey.getOpnameNummer( ) ) {
	    // Opname nummer changed: add opnameNummer to update string
	    addToUpdateString( "opname_nummer = " + opnameNummer );
	}

	int opnamePlaatsId = opnamePlaatsComboBox.getSelectedOpnamePlaatsId( );
	// Check if opnamePlaatsId changed (allow for a zero value)
	if ( opnamePlaatsId != defaultOpnamePlaatsId ) {
	    if ( opnamePlaatsId == 0 ) {
		addToUpdateString( "opname_plaats_id = NULL" );
	    } else {
		addToUpdateString( "opname_plaats_id = " + opnamePlaatsId );
	    }
	}

	int opnameDatumId = opnameDatumComboBox.getSelectedOpnameDatumId( );
	// Check if opnameDatumId changed (allow for a zero value)
	if ( opnameDatumId != defaultOpnameDatumId ) {
	    if ( opnameDatumId == 0 ) {
		addToUpdateString( "opname_datum_id = NULL" );
	    } else {
		addToUpdateString( "opname_datum_id = " + opnameDatumId );
	    }
	}

	int producersId = producersComboBox.getSelectedProducersId( );
	// Check if producersId changed (allow for a zero value)
	if ( producersId != defaultProducersId ) {
	    if ( producersId == 0 ) {
		addToUpdateString( "producers_id = NULL" );
	    } else {
		addToUpdateString( "producers_id = " + producersId );
	    }
	}

	// Set opname techniek in record when necessary
	boolean opnameTechniekChanged = true;

	if ( ( opnameTechniekString == null ) && ( defaultOpnameTechniekString == null ) ) {
	    opnameTechniekChanged = false;
	} else {
	    // Check if defaultOpnameTechniekString exists
	    if ( defaultOpnameTechniekString != null ) {
		// Check if opname techniek changed from default value
		if ( defaultOpnameTechniekString.equals( opnameTechniekString ) ) {
		    // Current opname techniek is the same as default: no change
		    opnameTechniekChanged = false;
		}
	    }
	}

	// Check if opname techniek changed, and therefore needs update
	if ( opnameTechniekChanged ) {
	    // Check if current opname techniek string exists
	    if ( opnameTechniekString == null ) {
		// Current opname techniek string does not exist: set data base entry to NULL
		addToUpdateString( "opname_techniek = NULL" );
	    } else {
		// Check if opname techniek is set to unknown
		if ( opnameTechniekString.equals( "U" ) ) {
		    // Current opname techniek set to unknown: set data base entry to NULL
		    addToUpdateString( "opname_techniek = NULL" );
		} else {
		    // Current opname techniek set to known value
		    addToUpdateString( "opname_techniek = '" + opnameTechniekString + "'" );
		}
	    }
	}

	// Set mix techniek in record when necessary
	boolean mixTechniekChanged = true;

	if ( ( mixTechniekString == null ) && ( defaultMixTechniekString == null ) ) {
	    mixTechniekChanged = false;
	} else {
	    // Check if defaultMixTechniekString exists
	    if ( defaultMixTechniekString != null ) {
		// Check if mix techniek changed from default value
		if ( defaultMixTechniekString.equals( mixTechniekString ) ) {
		    // Current mix techniek is the same as default: no change
		    mixTechniekChanged = false;
		}
	    }
	}

	// Check if mix techniek changed, and therefore needs update
	if ( mixTechniekChanged ) {
	    // Check if current mix techniek string exists
	    if ( mixTechniekString == null ) {
		// Current mix techniek string does not exist: set data base entry to NULL
		addToUpdateString( "mix_techniek = NULL" );
	    } else {
		// Check if mix techniek is set to unknown
		if ( mixTechniekString.equals( "U" ) ) {
		    // Current mix techniek set to unknown: set data base entry to NULL
		    addToUpdateString( "mix_techniek = NULL" );
		} else {
		    // Current mix techniek set to known value
		    addToUpdateString( "mix_techniek = '" + mixTechniekString + "'" );
		}
	    }
	}


	// Check if any update is necessary at all
	if ( updateString == null ) {
	    logger.info( "EditOpnameDialog.updateOpname: no update necessary" );

	    // But update in tracks table may still be necessary
	    tracksTableModel.updateTable( );

	    return true;
	}

	updateString  = "UPDATE opname SET " + updateString;

	// Use the original key for selection of the existing record
	updateString += " WHERE medium_id = " + opnameKey.getMediumId( );
	updateString += " AND opus_id = " + opnameKey.getOpusId( );
	updateString += " AND opname_nummer = " + opnameKey.getOpnameNummer( );
	updateString += " AND musici_id = " + opnameKey.getMusiciId( );

	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update in opname" );
	    	return false;
	    }

	    // Also update the related records in the tracks table
	    tracksTableModel.updateTable( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return false;
	}

	return true;
    }

    public boolean opnameUpdated( ) { return nUpdate > 0; }

    public OpnameKey getOpnameKey( ) {
	// Return the new, possibly updated opname key
	return new OpnameKey( mediumComboBox.getSelectedMediumId( ),
			      opusComboBox.getSelectedOpusId( ),
			      ( ( Integer )opnameNummerSpinner.getValue( ) ).intValue( ),
			      musiciComboBox.getSelectedMusiciId( ) );
    }
}