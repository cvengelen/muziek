// frame to show and select records from medium

package muziek.medium;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

import java.util.logging.*;

import muziek.gui.*;
import table.*;


public class MediumFrame {
    private final Logger logger = Logger.getLogger( MediumFrame.class.getCanonicalName() );

    private final JFrame frame = new JFrame( "Medium");

    private JTextField mediumTitelFilterTextField;

    private JTextField uitvoerendenFilterTextField;

    private int selectedGenreId = 0;
    private GenreComboBox genreComboBox;

    private int selectedSubgenreId = 0;
    private SubgenreComboBox subgenreComboBox;

    private int selectedMediumTypeId = 0;
    private MediumTypeComboBox mediumTypeComboBox;

    private int selectedMediumStatusId = 0;
    private MediumStatusComboBox mediumStatusComboBox;

    private int selectedLabelId = 0;
    private LabelComboBox labelComboBox;

    private int selectedOpslagId = 0;
    private OpslagComboBox opslagComboBox;

    private JTextField opmerkingenFilterTextField;

    private MediumTableModel mediumTableModel;
    private TableSorter mediumTableSorter;


    public MediumFrame( final Connection connection ) {

	// put the controls the content pane
	Container container = frame.getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.insets = new Insets( 5, 10, 5, 10 );
	constraints.weighty = 0.0;


	/////////////////////////////////
	// Text filter action listener
	/////////////////////////////////

	class TextFilterActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Setup the medium table
		mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
							uitvoerendenFilterTextField.getText( ),
							opmerkingenFilterTextField.getText( ),
							selectedGenreId,
							selectedSubgenreId,
							selectedMediumTypeId,
							selectedMediumStatusId,
							selectedLabelId,
							selectedOpslagId );
	    }
	}
	TextFilterActionListener textFilterActionListener = new TextFilterActionListener( );


	/////////////////////////////////
	// Medium titel filter string
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Medium Titel Filter:" ), constraints );

	mediumTitelFilterTextField = new JTextField( 55 );
	mediumTitelFilterTextField.addActionListener( textFilterActionListener );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( mediumTitelFilterTextField, constraints );


	/////////////////////////////////
	// Uitvoerenden filter string
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.gridwidth = 1;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Uitvoerenden Filter:" ), constraints );

	uitvoerendenFilterTextField = new JTextField( 55 );
	uitvoerendenFilterTextField.addActionListener( textFilterActionListener );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( uitvoerendenFilterTextField, constraints );


	/////////////////////////////////
	// Genre Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Genre:" ), constraints );

	genreComboBox = new GenreComboBox( connection, selectedGenreId );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( genreComboBox, constraints );

	class SelectGenreActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected genre ID from the combo box
		selectedGenreId = genreComboBox.getSelectedGenreId( );

		// Setup the medium table for the selected genre
		mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
							uitvoerendenFilterTextField.getText( ),
							opmerkingenFilterTextField.getText( ),
							selectedGenreId,
							selectedSubgenreId,
							selectedMediumTypeId,
							selectedMediumStatusId,
							selectedLabelId,
							selectedOpslagId  );
	    }
	}
	genreComboBox.addActionListener( new SelectGenreActionListener( ) );


	/////////////////////////////////
	// Subgenre Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Subgenre:" ), constraints );

	subgenreComboBox = new SubgenreComboBox( connection, selectedSubgenreId );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( subgenreComboBox, constraints );

	class SelectSubgenreActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected subgenre ID from the combo box
		selectedSubgenreId = subgenreComboBox.getSelectedSubgenreId( );

		// Setup the medium table for the selected subgenre
		mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
							uitvoerendenFilterTextField.getText( ),
							opmerkingenFilterTextField.getText( ),
							selectedGenreId,
							selectedSubgenreId,
							selectedMediumTypeId,
							selectedMediumStatusId,
							selectedLabelId,
							selectedOpslagId );
	    }
	}
	subgenreComboBox.addActionListener( new SelectSubgenreActionListener( ) );


	/////////////////////////////////
	// Medium Type Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "MediumType:" ), constraints );

	mediumTypeComboBox = new MediumTypeComboBox( connection, selectedMediumTypeId );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( mediumTypeComboBox, constraints );

	class SelectMediumTypeActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected mediumType ID from the combo box
		selectedMediumTypeId = mediumTypeComboBox.getSelectedMediumTypeId( );

		// Setup the medium table for the selected mediumType
		mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
							uitvoerendenFilterTextField.getText( ),
							opmerkingenFilterTextField.getText( ),
							selectedGenreId,
							selectedSubgenreId,
							selectedMediumTypeId,
							selectedMediumStatusId,
							selectedLabelId,
							selectedOpslagId );
	    }
	}
	mediumTypeComboBox.addActionListener( new SelectMediumTypeActionListener( ) );


	/////////////////////////////////
	// Medium Status Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 5;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "MediumStatus:" ), constraints );

	mediumStatusComboBox = new MediumStatusComboBox( connection, selectedMediumStatusId );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( mediumStatusComboBox, constraints );

	class SelectMediumStatusActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected mediumStatus ID from the combo box
		selectedMediumStatusId = mediumStatusComboBox.getSelectedMediumStatusId( );

		// Setup the medium table for the selected mediumStatus
		mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
							uitvoerendenFilterTextField.getText( ),
							opmerkingenFilterTextField.getText( ),
							selectedGenreId,
							selectedSubgenreId,
							selectedMediumTypeId,
							selectedMediumStatusId,
							selectedLabelId,
							selectedOpslagId );
	    }
	}
	mediumStatusComboBox.addActionListener( new SelectMediumStatusActionListener( ) );


	/////////////////////////////////
	// Label Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 6;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Label:" ), constraints );

	labelComboBox = new LabelComboBox( connection, frame, selectedLabelId );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( labelComboBox, constraints );

	class SelectLabelActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected label ID from the combo box
		selectedLabelId = labelComboBox.getSelectedLabelId( );

		// Setup the medium table for the selected label
		mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
							uitvoerendenFilterTextField.getText( ),
							opmerkingenFilterTextField.getText( ),
							selectedGenreId,
							selectedSubgenreId,
							selectedMediumTypeId,
							selectedMediumStatusId,
							selectedLabelId,
							selectedOpslagId );
	    }
	}
	labelComboBox.addActionListener( new SelectLabelActionListener( ) );


	/////////////////////////////////
	// Opslag Combo Box
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 7;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Opslag:" ), constraints );

	opslagComboBox = new OpslagComboBox( connection, frame, selectedLabelId );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( opslagComboBox, constraints );

	class SelectOpslagActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		// Get the selected opslagcombobox ID from the combo box
		selectedOpslagId = opslagComboBox.getSelectedOpslagId( );

		// Setup the medium table for the selected opslag
		mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
							uitvoerendenFilterTextField.getText( ),
							opmerkingenFilterTextField.getText( ),
							selectedGenreId,
							selectedSubgenreId,
							selectedMediumTypeId,
							selectedMediumStatusId,
							selectedLabelId,
							selectedOpslagId );
	    }
	}
	opslagComboBox.addActionListener( new SelectOpslagActionListener( ) );


	/////////////////////////////////
	// Opmerkingen filter string
	/////////////////////////////////

	constraints.gridx = 0;
	constraints.gridy = 8;
	constraints.weightx = 0.0;
	constraints.anchor = GridBagConstraints.EAST;
	constraints.gridwidth = 1;
	container.add( new JLabel( "Opmerkingen Filter:" ), constraints );

	opmerkingenFilterTextField = new JTextField( 55 );
	opmerkingenFilterTextField.addActionListener( textFilterActionListener );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.weightx = 1.0;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( opmerkingenFilterTextField, constraints );


	/////////////////////////////////
	// Medium Table
	/////////////////////////////////

	// Define the open dialog, enable edit, cancel, save, delete and show tracks buttons
	// These are enabled/disabled by the table model and the list selection listener.
	final JButton openMediumDialogButton = new JButton( "Open Dialog" );
	final JButton enableRowEditButton = new JButton( "Edit" );
	final JButton cancelRowEditButton = new JButton( "Cancel" );
	final JButton saveRowEditButton = new JButton( "Save" );
	final JButton deleteMediumButton = new JButton( "Delete" );
	final JButton showMediumTracksButton = new JButton( "Show tracks" );

	// Create medium table from title table model
	mediumTableModel = new MediumTableModel( connection,
						 cancelRowEditButton,
						 saveRowEditButton );
	mediumTableSorter = new TableSorter( mediumTableModel );
	JTable mediumTable = new JTable( mediumTableSorter );
	mediumTableSorter.setTableHeader( mediumTable.getTableHeader( ) );
	// mediumTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	mediumTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	mediumTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	mediumTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 200 );  // medium titel
	mediumTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 200 );  // uitvoerenden
	mediumTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth( 100 );  // genre
	mediumTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth( 100 );  // subgenre
	mediumTable.getColumnModel( ).getColumn( 5 ).setPreferredWidth(  80 );  // mediumType
	mediumTable.getColumnModel( ).getColumn( 6 ).setPreferredWidth(  80 );  // mediumStatus
	mediumTable.getColumnModel( ).getColumn( 7 ).setPreferredWidth( 100 );  // label
	mediumTable.getColumnModel( ).getColumn( 8 ).setPreferredWidth( 100 );  // labelNummer
	mediumTable.getColumnModel( ).getColumn( 9 ).setPreferredWidth( 100 );  // aanschafdatum
	mediumTable.getColumnModel( ).getColumn( 10 ).setPreferredWidth( 100 );  // opslag
	mediumTable.getColumnModel( ).getColumn( 11 ).setPreferredWidth( 200 );  // opmerkingen

	// Set vertical size just enough for 20 entries
	mediumTable.setPreferredScrollableViewportSize( new Dimension( 990, 320 ) );

	final DefaultCellEditor genreDefaultCellEditor =
	    new DefaultCellEditor( new GenreComboBox( connection, 0 ) );
	mediumTable.getColumnModel( ).getColumn( 3 ).setCellEditor( genreDefaultCellEditor );

	final DefaultCellEditor subgenreDefaultCellEditor =
	    new DefaultCellEditor( new SubgenreComboBox( connection, 0 ) );
	mediumTable.getColumnModel( ).getColumn( 4 ).setCellEditor( subgenreDefaultCellEditor );

	final DefaultCellEditor mediumTypeDefaultCellEditor =
	    new DefaultCellEditor( new MediumTypeComboBox( connection, 0 ) );
	mediumTable.getColumnModel( ).getColumn( 5 ).setCellEditor( mediumTypeDefaultCellEditor );

	final DefaultCellEditor mediumStatusDefaultCellEditor =
	    new DefaultCellEditor( new MediumStatusComboBox( connection, 0 ) );
	mediumTable.getColumnModel( ).getColumn( 6 ).setCellEditor( mediumStatusDefaultCellEditor );

	final DefaultCellEditor labelDefaultCellEditor =
	    new DefaultCellEditor( new LabelComboBox( connection, null, false ) );
	mediumTable.getColumnModel( ).getColumn( 7 ).setCellEditor( labelDefaultCellEditor );

	final DefaultCellEditor opslagDefaultCellEditor =
	    new DefaultCellEditor( new OpslagComboBox( connection, null, false ) );
	mediumTable.getColumnModel( ).getColumn( 10 ).setCellEditor( opslagDefaultCellEditor );

	constraints.gridx = 0;
	constraints.gridy = 9;
	constraints.gridwidth = 3;
	constraints.anchor = GridBagConstraints.CENTER;

	// Setting weighty and fill is necessary for proper filling the frame when resized.
	constraints.fill = GridBagConstraints.BOTH;
	constraints.weightx = 1.0;
	constraints.weighty = 1.0;

	container.add( new JScrollPane( mediumTable ), constraints );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel mediumListSelectionModel = mediumTable.getSelectionModel( );

	class MediumListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Check if current row has modified values
		if ( mediumTableModel.getRowModified( ) ) {
		    if ( selectedRow == -1 ) {
			logger.severe( "Invalid selected row" );
		    } else {
			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Data zijn gewijzigd: modificaties opslaan?",
							   "Record is gewijzigd",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result == JOptionPane.YES_OPTION ) {
			    // Save the changes in the table model, and in the database
			    if ( !( mediumTableModel.saveEditRow( selectedRow ) ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Error: row not saved",
							       "Save medium record error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} else {
			    // Cancel any edits in the selected row
			    mediumTableModel.cancelEditRow( selectedRow );
			}
		    }
		}

		// Ignore if nothing is selected
		if ( mediumListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;

		    openMediumDialogButton.setEnabled( false );
		    enableRowEditButton.setEnabled( false );
		    cancelRowEditButton.setEnabled( false );
		    saveRowEditButton.setEnabled( false );
		    deleteMediumButton.setEnabled( false );
		    showMediumTracksButton.setEnabled( false );

		    return;
		}

		// Disable row edit
		mediumTableModel.unsetEditRow( );

		// Get the selected row
		int viewRow = mediumListSelectionModel.getMinSelectionIndex( );
		selectedRow = mediumTableSorter.modelIndex( viewRow );

		// Enable the open dialog and enable edit buttons
		openMediumDialogButton.setEnabled( true );
		enableRowEditButton.setEnabled( true );

		// Disable the cancel and save buttons (these will be enabled
		// when any data in the row is actually modified)
		cancelRowEditButton.setEnabled( false );
		saveRowEditButton.setEnabled( false );

		// Enable the delete and show tracks buttons
		deleteMediumButton.setEnabled( true );
		showMediumTracksButton.setEnabled( true );
	    }

	    public int getSelectedRow ( ) { return selectedRow; }
	}

	// Add mediumListSelectionListener object to the selection model of the musici table
	final MediumListSelectionListener mediumListSelectionListener = new MediumListSelectionListener( );
	mediumListSelectionModel.addListSelectionListener( mediumListSelectionListener );

	// Class to handle button actions: uses mediumListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    frame.setVisible( false );
		    System.exit( 0 );
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new medium record
                    new EditMediumDialog( connection, frame,
                                          mediumTitelFilterTextField.getText( ),
                                          uitvoerendenFilterTextField.getText( ),
                                          selectedGenreId,
                                          selectedSubgenreId,
                                          selectedMediumTypeId,
                                          selectedMediumStatusId,
                                          selectedLabelId );

		    // Records may have been modified: setup the table model again
		    mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
							    uitvoerendenFilterTextField.getText( ),
							    opmerkingenFilterTextField.getText( ),
							    selectedGenreId,
							    selectedSubgenreId,
							    selectedMediumTypeId,
							    selectedMediumStatusId,
							    selectedLabelId,
							    selectedOpslagId );
		} else {
		    int selectedRow = mediumListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen medium geselecteerd",
						       "Medium frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected medium id
		    int selectedMediumId = mediumTableModel.getMediumId( selectedRow );

		    // Check if medium has been selected
		    if ( selectedMediumId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen medium geselecteerd",
						       "Medium frame error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "openDialog" ) ) {
			// Do dialog
                        new EditMediumDialog( connection, frame, selectedMediumId );

			// Records may have been modified: setup the table model again
			mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
								uitvoerendenFilterTextField.getText( ),
							        opmerkingenFilterTextField.getText( ),
								selectedGenreId,
								selectedSubgenreId,
								selectedMediumTypeId,
							        selectedMediumStatusId,
								selectedLabelId,
							        selectedOpslagId );
		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			String selectedMediumTitelString = mediumTableModel.getMediumTitelString( selectedRow );

			// Replace null or empty string by single space for messages
			if ( ( selectedMediumTitelString == null ) ||
			     ( selectedMediumTitelString.length( ) == 0  ) ) {
			    selectedMediumTitelString = " ";
			}

			// Check if selectedMediumId is present in opname table
			try {
			    Statement statement = connection.createStatement( );
			    ResultSet resultSet =
				statement.executeQuery( "SELECT medium_id FROM opname WHERE medium_id = " +
							selectedMediumId );
			    if ( resultSet.next( ) ) {
				JOptionPane.showMessageDialog( frame,
							       "Tabel opname heeft nog verwijzing naar '" +
							       selectedMediumTitelString + "'",
							       "Medium frame error",
							       JOptionPane.ERROR_MESSAGE );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			int result =
			    JOptionPane.showConfirmDialog( frame,
							   "Delete '" + selectedMediumTitelString + "' ?",
							   "Delete Medium record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM medium";
			deleteString += " WHERE medium_id = " + selectedMediumId;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with medium_id  = " +
						       selectedMediumId + " in medium" );
				JOptionPane.showMessageDialog( frame,
							       errorString,
							       "Delete medium record",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}

			// Records may have been modified: setup the table model again
			mediumTableSorter.clearSortingState( );
			mediumTableModel.setupMediumTableModel( mediumTitelFilterTextField.getText( ),
								uitvoerendenFilterTextField.getText( ),
							        opmerkingenFilterTextField.getText( ),
								selectedGenreId,
								selectedSubgenreId,
								selectedMediumTypeId,
							        selectedMediumStatusId,
								selectedLabelId,
				                                selectedOpslagId );
		    } else if ( actionEvent.getActionCommand( ).equals( "enableRowEdit" ) ) {
			// Allow to edit the selected row
			mediumTableModel.setEditRow( selectedRow );

			// Disable the enable edit button
			enableRowEditButton.setEnabled( false );
		    } else if ( actionEvent.getActionCommand( ).equals( "cancelRowEdit" ) ) {
			// Cancel any edits in the selected row
			mediumTableModel.cancelEditRow( selectedRow );

			// Remove the capability to edit the row
			mediumTableModel.unsetEditRow( );

			// Enable the enable edit button, so that the user can select edit again
			enableRowEditButton.setEnabled( true );

			// Disable the cancel and save buttons
			cancelRowEditButton.setEnabled( false );
			saveRowEditButton.setEnabled( false );
		    } else if ( actionEvent.getActionCommand( ).equals( "saveRowEdit" ) ) {
			// Save the changes in the table model, and in the database
			if ( !( mediumTableModel.saveEditRow( selectedRow ) ) ) {
			    JOptionPane.showMessageDialog( frame,
							   "Error: row not saved",
							   "Save medium record error",
							   JOptionPane.ERROR_MESSAGE );
			    return;
			}

			// Remove the capability to edit the row
			mediumTableModel.unsetEditRow( );

			// Enable the enable edit button, so that the user can select edit again
			enableRowEditButton.setEnabled( true );

			// Disable the cancel and save buttons
			cancelRowEditButton.setEnabled( false );
			saveRowEditButton.setEnabled( false );
		    } else if ( actionEvent.getActionCommand( ).equals( "showMediumTracks" ) ) {
			// Do dialog
                        new ShowMediumTracksDialog( connection, frame, selectedMediumId );
		    }
		}
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertMediumButton = new JButton( "Insert" );
	insertMediumButton.setActionCommand( "insert" );
	insertMediumButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertMediumButton );

	openMediumDialogButton.setActionCommand( "openDialog" );
	openMediumDialogButton.setEnabled( false );
	openMediumDialogButton.addActionListener( buttonActionListener );
	buttonPanel.add( openMediumDialogButton );

	enableRowEditButton.setActionCommand( "enableRowEdit" );
	enableRowEditButton.setEnabled( false );
	enableRowEditButton.addActionListener( buttonActionListener );
	buttonPanel.add( enableRowEditButton );

	cancelRowEditButton.setActionCommand( "cancelRowEdit" );
	cancelRowEditButton.setEnabled( false );
	cancelRowEditButton.addActionListener( buttonActionListener );
	buttonPanel.add( cancelRowEditButton );

	saveRowEditButton.setActionCommand( "saveRowEdit" );
	saveRowEditButton.setEnabled( false );
	saveRowEditButton.addActionListener( buttonActionListener );
	buttonPanel.add( saveRowEditButton );

	deleteMediumButton.setActionCommand( "delete" );
	deleteMediumButton.setEnabled( false );
	deleteMediumButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteMediumButton );

	showMediumTracksButton.setActionCommand( "showMediumTracks" );
	showMediumTracksButton.setEnabled( false );
	showMediumTracksButton.addActionListener( buttonActionListener );
	buttonPanel.add( showMediumTracksButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

	constraints.gridx = 0;
	constraints.gridy = 10;
	constraints.weightx = 1.0;
	constraints.weighty = 0.0;
	constraints.fill = GridBagConstraints.NONE;
	container.add( buttonPanel, constraints );

	frame.setSize( 1080, 820 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible(true);
    }
}
