package muziek.musici;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import java.util.logging.*;

import muziek.gui.EditMusiciDialog;
import muziek.gui.EnsembleComboBox;
import muziek.gui.PersoonComboBox;
import muziek.gui.RolComboBox;
import table.*;

/**
 * Frame to show, insert and update records in the musici table in schema muziek.
 * @author Chris van Engelen
 */
public class EditMusici extends JInternalFrame {
    private final Logger logger = Logger.getLogger( EditMusici.class.getCanonicalName() );

    private final Connection connection;
    private final JFrame parentFrame;

    private JTextField musiciFilterTextField;

    private PersoonComboBox persoonComboBox;
    private int selectedPersoonId = 0;

    private RolComboBox rolComboBox;
    private int selectedRolId = 0;

    private EnsembleComboBox ensembleComboBox;
    private int selectedEnsembleId = 0;

    private MusiciTableModel musiciTableModel;
    private TableSorter musiciTableSorter;

    private class Musici {
	int	id;
	String  string;

	Musici( int    id,
                String string ) {
	    this.id = id;
	    this.string = string;
	}

	boolean presentInTable( String tableString ) {
	    // Check if musiciId is present in table
	    try {
		Statement statement = connection.createStatement( );
		ResultSet resultSet = statement.executeQuery( "SELECT musici_id FROM " + tableString +
							      " WHERE musici_id = " + id );
		if ( resultSet.next( ) ) {
		    JOptionPane.showMessageDialog( parentFrame,
						   "Tabel " + tableString +
						   " heeft nog verwijzing naar '" + string + "'",
						   "Edit musici error",
						   JOptionPane.ERROR_MESSAGE );
		    return true;
		}
	    } catch ( SQLException sqlException ) {
		logger.severe( "SQLException: " + sqlException.getMessage( ) );
		return true;
	    }
	    return false;
	}
    }

    public EditMusici( final Connection connection, final JFrame parentFrame, int x, int y ) {
        super("Edit musici", true, true, true, true);
        this.connection = connection;
        this.parentFrame = parentFrame;

	// put the controls the content pane
	Container container = getContentPane();

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );
	GridBagConstraints constraints = new GridBagConstraints( );

	final ActionListener textFilterActionListener = ( ActionEvent actionEvent ) -> {
            // Setup the musici table
            musiciTableSorter.clearSortingState();
            musiciTableModel.setupMusiciTableModel( musiciFilterTextField.getText( ),
                    selectedPersoonId,
                    selectedRolId,
                    selectedEnsembleId );
        };

        constraints.insets = new Insets( 20, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 0;
	constraints.gridwidth = 1;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Musici Filter:" ), constraints );

	musiciFilterTextField = new JTextField( 20 );
	musiciFilterTextField.addActionListener( textFilterActionListener );

        constraints.insets = new Insets( 20, 5, 5, 400 );
	constraints.gridx = GridBagConstraints.RELATIVE;
        constraints.weightx = 1d;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.WEST;
	container.add( musiciFilterTextField, constraints );


	////////////////////////////////////////////////
	// Persoon ComboBox
	////////////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 1;
	constraints.anchor = GridBagConstraints.EAST;
        constraints.weightx = 0d;
        constraints.fill = GridBagConstraints.NONE;
	container.add( new JLabel( "Persoon:" ), constraints );

	final JPanel persoonPanel = new JPanel( );
	final Border emptyBorder = new EmptyBorder( -5, -5, -5, -5 );
	persoonPanel.setBorder( emptyBorder );

	// Setup a JComboBox with the results of the query on persoon
	// Do not allow to enter new record in persoon
	persoonComboBox = new PersoonComboBox( connection, parentFrame, false );
	persoonComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected persoon ID from the combo box
            selectedPersoonId = persoonComboBox.getSelectedPersoonId( );

            // Setup the musici table
            musiciTableSorter.clearSortingState();
            musiciTableModel.setupMusiciTableModel( musiciFilterTextField.getText( ),
                    selectedPersoonId,
                    selectedRolId,
                    selectedEnsembleId );
        } );
        persoonPanel.add( persoonComboBox );

	JButton filterPersoonButton = new JButton( "Filter" );
	filterPersoonButton.setActionCommand( "filterPersoon" );
        filterPersoonButton.addActionListener( ( ActionEvent actionEvent ) -> persoonComboBox.filterPersoonComboBox( ) );
        persoonPanel.add( filterPersoonButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( persoonPanel, constraints );


	////////////////////////////////////////////////
	// Rol ComboBox
	////////////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Rol:" ), constraints );

	final JPanel rolPanel = new JPanel( );
	rolPanel.setBorder( emptyBorder );

	// Setup a JComboBox with the results of the query on rol
	// Do not allow to enter new record in rol
	rolComboBox = new RolComboBox( connection, parentFrame, false );
	rolComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected rol ID from the combo box
            selectedRolId = rolComboBox.getSelectedRolId( );

            // Setup the musici table
            musiciTableSorter.clearSortingState();
            musiciTableModel.setupMusiciTableModel( musiciFilterTextField.getText( ),
                    selectedPersoonId,
                    selectedRolId,
                    selectedEnsembleId );
        } );
        rolPanel.add( rolComboBox );

	JButton filterRolButton = new JButton( "Filter" );
	filterRolButton.setActionCommand( "filterRol" );
        filterRolButton.addActionListener( ( ActionEvent actionEvent ) -> rolComboBox.filterRolComboBox( ) );
        rolPanel.add( filterRolButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( rolPanel, constraints );


	////////////////////////////////////////////////
	// Ensemble ComboBox
	////////////////////////////////////////////////

        constraints.insets = new Insets( 5, 20, 5, 5 );
	constraints.gridx = 0;
	constraints.gridy = 3;
	constraints.anchor = GridBagConstraints.EAST;
	container.add( new JLabel( "Ensemble:" ), constraints );

	final JPanel ensemblePanel = new JPanel( );
	ensemblePanel.setBorder( emptyBorder );

	// Setup a JComboBox with the results of the query on ensemble
	// Do not allow to enter new record in ensemble
	ensembleComboBox = new EnsembleComboBox( connection, parentFrame, false );
	ensembleComboBox.addActionListener( ( ActionEvent actionEvent ) -> {
            // Get the selected ensemble ID from the combo box
            selectedEnsembleId = ensembleComboBox.getSelectedEnsembleId( );

            // Setup the musici table
            musiciTableSorter.clearSortingState();
            musiciTableModel.setupMusiciTableModel( musiciFilterTextField.getText( ),
                    selectedPersoonId,
                    selectedRolId,
                    selectedEnsembleId );
        } );
        ensemblePanel.add( ensembleComboBox );

	JButton filterEnsembleButton = new JButton( "Filter" );
	filterEnsembleButton.setActionCommand( "filterEnsemble" );
	filterEnsembleButton.addActionListener( ( ActionEvent actionEvent ) -> ensembleComboBox.filterEnsembleComboBox( ) );
        ensemblePanel.add( filterEnsembleButton );

        constraints.insets = new Insets( 5, 5, 5, 20 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	constraints.anchor = GridBagConstraints.WEST;
	container.add( ensemblePanel, constraints );


	// Create musici table from title table model
	musiciTableModel = new MusiciTableModel( connection, parentFrame );
	musiciTableSorter = new TableSorter( musiciTableModel );
	final JTable musiciTable = new JTable( musiciTableSorter );
	musiciTableSorter.setTableHeader( musiciTable.getTableHeader( ) );
	// musiciTableSorter.setSortingStatus( 0, TableSorter.DESCENDING );

	musiciTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
	musiciTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

	musiciTable.getColumnModel( ).getColumn( 0 ).setPreferredWidth(  50 );  // id
	musiciTable.getColumnModel( ).getColumn( 1 ).setPreferredWidth( 300 );  // musici
	musiciTable.getColumnModel( ).getColumn( 2 ).setPreferredWidth( 150 );  // persoon
	musiciTable.getColumnModel( ).getColumn( 3 ).setPreferredWidth( 100 );  // rol
	musiciTable.getColumnModel( ).getColumn( 4 ).setPreferredWidth( 250 );  // ensemble

	// Set vertical size just enough for 20 entries
	musiciTable.setPreferredScrollableViewportSize( new Dimension( 850, 320 ) );

        constraints.insets = new Insets( 5, 20, 5, 20 );
	constraints.gridx = 0;
	constraints.gridy = 4;
	constraints.gridwidth = 4;
	constraints.anchor = GridBagConstraints.CENTER;
        constraints.weightx = 1d;
        constraints.weighty = 1d;
        constraints.fill = GridBagConstraints.BOTH;
	container.add( new JScrollPane( musiciTable ), constraints );


	// Define the edit and delete buttons because these are used by the list selection listener
	final JButton editMusiciButton = new JButton( "Edit" );
	final JButton deleteMusiciButton = new JButton( "Delete" );

	// Get the selection model related to the rekening_mutatie table
	final ListSelectionModel musiciListSelectionModel = musiciTable.getSelectionModel( );

	class MusiciListSelectionListener implements ListSelectionListener {
	    private int selectedRow = -1;

	    public void valueChanged( ListSelectionEvent listSelectionEvent ) {
		// Ignore extra messages.
		if ( listSelectionEvent.getValueIsAdjusting( ) ) return;

		// Ignore if nothing is selected
		if ( musiciListSelectionModel.isSelectionEmpty( ) ) {
		    selectedRow = -1;
		    editMusiciButton.setEnabled( false );
		    deleteMusiciButton.setEnabled( false );
		    return;
		}

		int viewRow = musiciListSelectionModel.getMinSelectionIndex( );
		selectedRow = musiciTableSorter.modelIndex( viewRow );
		editMusiciButton.setEnabled( true );
		deleteMusiciButton.setEnabled( true );
	    }

	    int getSelectedRow ( ) { return selectedRow; }
	}

	// Add musiciListSelectionListener object to the selection model of the musici table
	final MusiciListSelectionListener musiciListSelectionListener = new MusiciListSelectionListener( );
	musiciListSelectionModel.addListSelectionListener( musiciListSelectionListener );

	// Class to handle button actions: uses musiciListSelectionListener
	class ButtonActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent actionEvent ) {
		if ( actionEvent.getActionCommand( ).equals( "close" ) ) {
		    setVisible( false );
                    dispose();
                    return;
		} else if ( actionEvent.getActionCommand( ).equals( "insert" ) ) {
		    // Insert new musici record
		    new EditMusiciDialog( connection, parentFrame,
                                          musiciFilterTextField.getText( ) );
		} else {
		    int selectedRow = musiciListSelectionListener.getSelectedRow( );
		    if ( selectedRow < 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen musici geselecteerd",
						       "Edit musici error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    // Get the selected musici id
		    int selectedMusiciId = musiciTableModel.getMusiciId( selectedRow );

		    // Check if musici has been selected
		    if ( selectedMusiciId == 0 ) {
			JOptionPane.showMessageDialog( parentFrame,
						       "Geen musici geselecteerd",
						       "Edit musici error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    if ( actionEvent.getActionCommand( ).equals( "edit" ) ) {
			// Do dialog
			new EditMusiciDialog( connection, parentFrame, selectedMusiciId );
		    } else if ( actionEvent.getActionCommand( ).equals( "delete" ) ) {
			final Musici musici = new Musici( musiciTableModel.getMusiciId( selectedRow ),
							  musiciTableModel.getMusiciString( selectedRow ) );

			// Check if musici ID is still used
			if ( musici.presentInTable( "musici_persoon" ) ) return;
			if ( musici.presentInTable( "musici_ensemble" ) ) return;
			if ( musici.presentInTable( "opname" ) ) return;

			// Replace null or empty string by single space for messages
			if ( ( musici.string == null ) || ( musici.string.length( ) == 0  ) ) {
			    musici.string = " ";
			}

			int result =
			    JOptionPane.showConfirmDialog( parentFrame,
							   "Delete '" + musici.string + "' ?",
							   "Delete musici record",
							   JOptionPane.YES_NO_OPTION,
							   JOptionPane.QUESTION_MESSAGE,
							   null );

			if ( result != JOptionPane.YES_OPTION ) return;

			String deleteString  = "DELETE FROM musici";
			deleteString += " WHERE musici_id = " + musici.id;

			logger.info( "deleteString: " + deleteString );

			try {
			    Statement statement = connection.createStatement( );
			    int nUpdate = statement.executeUpdate( deleteString );
			    if ( nUpdate != 1 ) {
				String errorString = ( "Could not delete record with musici_id  = " +
						       musici.id + " in musici" );
				JOptionPane.showMessageDialog( parentFrame,
							       errorString,
							       "Edit musici error",
							       JOptionPane.ERROR_MESSAGE);
				logger.severe( errorString );
				return;
			    }
			} catch ( SQLException sqlException ) {
                            JOptionPane.showMessageDialog( parentFrame,
                                                           "SQL exception in delete: " + sqlException.getMessage(),
                                                           "EditMusici SQL exception",
                                                           JOptionPane.ERROR_MESSAGE );
			    logger.severe( "SQLException: " + sqlException.getMessage( ) );
			    return;
			}
		    }
		}

		// Records may have been modified: setup the musici table model again
                musiciTableSorter.clearSortingState();
		musiciTableModel.setupMusiciTableModel( musiciFilterTextField.getText( ),
							selectedPersoonId,
							selectedRolId,
							selectedEnsembleId );
	    }
	}
	final ButtonActionListener buttonActionListener = new ButtonActionListener( );

	JPanel buttonPanel = new JPanel( );

	final JButton insertMusiciButton = new JButton( "Insert" );
	insertMusiciButton.setActionCommand( "insert" );
	insertMusiciButton.addActionListener( buttonActionListener );
	buttonPanel.add( insertMusiciButton );

	editMusiciButton.setActionCommand( "edit" );
	editMusiciButton.setEnabled( false );
	editMusiciButton.addActionListener( buttonActionListener );
	buttonPanel.add( editMusiciButton );

	deleteMusiciButton.setActionCommand( "delete" );
	deleteMusiciButton.setEnabled( false );
	deleteMusiciButton.addActionListener( buttonActionListener );
	buttonPanel.add( deleteMusiciButton );

	final JButton closeButton = new JButton( "Close" );
	closeButton.setActionCommand( "close" );
	closeButton.addActionListener( buttonActionListener );
	buttonPanel.add( closeButton );

        constraints.insets = new Insets( 5, 20, 20, 20 );
	constraints.gridx = 0;
	constraints.gridy = 5;
        constraints.weightx = 0d;
        constraints.weighty = 0d;
        constraints.fill = GridBagConstraints.NONE;
	container.add( buttonPanel, constraints );

	setSize( 910, 600 );
        setLocation( x, y );
	setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	setVisible(true);
    }
}
