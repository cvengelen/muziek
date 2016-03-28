// Dialog for inserting or updating a record in opname_datum

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.util.logging.*;
import java.util.regex.*;           // Pattern
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class EditOpnameDatumDialog {
    final private Logger logger = Logger.getLogger( "muziek.gui.EditOpnameDatumDialog" );

    Connection conn;
    Object parentObject;
    JDialog dialog;

    int opnameDatumId = 0;

    String opnameDatumString = null;

    JaarComboBox opnameJaar1ComboBox;
    int defaultOpnameJaar1 = 0;

    MaandComboBox opnameMaand1ComboBox;
    int defaultOpnameMaand1 = 0;

    JaarComboBox opnameJaar2ComboBox;
    int defaultOpnameJaar2 = 0;

    MaandComboBox opnameMaand2ComboBox;
    int defaultOpnameMaand2 = 0;

    private static final String regex1 = "\\s*-\\s*";
    private static final Pattern pattern1 = Pattern.compile( regex1 );
    private static final String regex2 = "\\s+";
    private static final Pattern pattern2 = Pattern.compile( regex2 );

    private static final String [ ] maandString = { "",
						    "Januari", "Februari", "Maart",
						    "April", "Mei", "Juni",
						    "Juli", "Augustus", "September",
						    "Oktober", "November", "December" };

    private static int findMonth( String monthString ) {
	for ( int monthIdx = 0; monthIdx < maandString.length; monthIdx++ ) {
	    if ( maandString[ monthIdx ].toUpperCase( ).matches( ".*" + monthString.toUpperCase( ) + ".*" ) ) {
		return monthIdx;
	    }
	}
	return 0;
    }

    int nUpdate = 0;
    final String insertOpnameDatumActionCommand = "insertOpnameDatum";
    final String updateOpnameDatumActionCommand = "updateOpnameDatum";

    // Constructor
    public EditOpnameDatumDialog( Connection conn,
				  Object     parentObject,
				  String     opnameDatumString ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.opnameDatumString = opnameDatumString;

	// Check if opnameDatumString is present
	if ( ( opnameDatumString != null ) && ( opnameDatumString.length( ) > 0 ) ) {
	    // Try to get defaults from the opname datum string

	    // Split the string on a dash, if present
	    String [ ] fields = pattern1.split( opnameDatumString );

	    // Loop over the maand jaar 1 default, maand jaar 2 default
	    for ( int idx1 = 0; idx1 < fields.length; idx1++ ) {
		logger.finest( "fields[" + idx1 + "]: " + fields[ idx1 ] );

		// Split the string on white space (\\s+)
		String [ ] monthYear = pattern2.split( fields[ idx1 ] );

		// Check if anything has been found
		if ( monthYear.length > 0 ) {
		    // Check if both a month and a year has been found
		    if ( monthYear.length > 1 ) {
			logger.finest( "monthYear[0, 1]: " + monthYear[ 0 ] + " " + monthYear[ 1 ] );

			// Check if this is maand jaar 1
			if ( idx1 == 0 ) {
			    // Maand jaar 1: get maand 1 from the first item
			    defaultOpnameMaand1 = findMonth( monthYear[ 0 ] );

			    // Maand jaar 1: get jaar 1 from the second item (catch parse errors)
			    try {
				defaultOpnameJaar1 = Integer.parseInt( monthYear[ 1 ] );
			    } catch ( Exception exception ) { }
			} else {
			    // Maand jaar 2: get maand 2 from the first item
			    defaultOpnameMaand2 = findMonth( monthYear[ 0 ] );

			    // Maand jaar 2: get jaar 2 from the second item (catch parse errors)
			    try {
				defaultOpnameJaar2 = Integer.parseInt( monthYear[ 1 ] );
			    } catch ( Exception exception ) { }
			}
		    } else {
			logger.finest( "monthYear[0]: " + monthYear[ 0 ] );

			// Check if this is maand jaar 1
			if ( idx1 == 0 ) {
			    // Maand or jaar 1: first try if an integer can be found
			    try {
				// Get jaar 1 from the first item
				defaultOpnameJaar1 = Integer.parseInt( monthYear[ 0 ] );
			    } catch ( Exception exception ) {
				// Not an integer, so get maand 1 from the first item
				defaultOpnameMaand1 = findMonth( monthYear[ 0 ] );
			    }
			} else {
			    // Maand or jaar 2: first try if an integer can be found
			    try {
				// Get jaar 2 from the first item
				defaultOpnameJaar2 = Integer.parseInt( monthYear[ 0 ] );
			    } catch ( Exception exception ) {
				// Not an integer, so get maand 2 from the first item
				defaultOpnameMaand2 = findMonth( monthYear[ 0 ] );
			    }
			}
		    }
		}
	    }
	}

	setupOpnameDatumDialog( "Insert Opname Datum", "Insert",
				 insertOpnameDatumActionCommand );
    }

    // Constructor
    public EditOpnameDatumDialog( Connection conn,
				  Object     parentObject,
				  int        opnameDatumId ) {
	this.conn = conn;
	this.parentObject = parentObject;
	this.opnameDatumId = opnameDatumId;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT opname_jaar_1, opname_maand_1, " +
							  "opname_jaar_2, opname_maand_2 " +
							  "FROM opname_datum " +
							  "WHERE opname_datum_id = " + opnameDatumId );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get record for opname_datum_id " +
			       opnameDatumId + " in opname_datum" );
		return;
	    }

	    defaultOpnameJaar1 = resultSet.getInt( 1 );
	    defaultOpnameMaand1 = resultSet.getInt( 2 );
	    defaultOpnameJaar2 = resultSet.getInt( 3 );
	    defaultOpnameMaand2 = resultSet.getInt( 4 );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	setupOpnameDatumDialog( "Edit Opname Datum", "Update",
				 updateOpnameDatumActionCommand );
    }

    // Setup opnameDatum dialog
    void setupOpnameDatumDialog( String dialogTitle,
				 String editOpnameDatumButtonText,
				 String editOpnameDatumButtonActionCommand ) {
	// Create modal dialog for editing opnameDatum
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
	constraints.insets = new Insets( 0, 0, 5, 5 );

	constraints.gridx = 0;
	constraints.gridy = 0;
	container.add( new JLabel( "Opname datum 1: " ), constraints );

	opnameMaand1ComboBox = new MaandComboBox( defaultOpnameMaand1 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( opnameMaand1ComboBox, constraints );

	opnameJaar1ComboBox = new JaarComboBox( defaultOpnameJaar1 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( opnameJaar1ComboBox, constraints );


	constraints.gridx = 0;
	constraints.gridy = 1;
	container.add( new JLabel( "Opname datum 2: " ), constraints );

	opnameMaand2ComboBox = new MaandComboBox( defaultOpnameMaand2 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( opnameMaand2ComboBox, constraints );

	opnameJaar2ComboBox = new JaarComboBox( defaultOpnameJaar2 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( opnameJaar2ComboBox, constraints );


	class EditOpnameDatumActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( insertOpnameDatumActionCommand ) ) {
		    insertOpnameDatum( );
		} else if ( ae.getActionCommand( ).equals( updateOpnameDatumActionCommand ) ) {
		    updateOpnameDatum( );
		}

		// Any other actionCommand, including cancel, has no action
		dialog.setVisible( false );
	    }
	}

	JPanel buttonPanel = new JPanel( );

	JButton editOpnameDatumButton = new JButton( editOpnameDatumButtonText );
	editOpnameDatumButton.setActionCommand( editOpnameDatumButtonActionCommand );
	editOpnameDatumButton.addActionListener( new EditOpnameDatumActionListener( ) );
	buttonPanel.add( editOpnameDatumButton );

	JButton cancelOpnameDatumButton = new JButton( "Cancel" );
	cancelOpnameDatumButton.setActionCommand( "cancelOpnameDatum" );
	cancelOpnameDatumButton.addActionListener( new EditOpnameDatumActionListener( ) );
	buttonPanel.add( cancelOpnameDatumButton );

	constraints.gridx = 0;
	constraints.gridy = 2;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	dialog.setSize( 700, 150 );
	dialog.setVisible( true );
    }

    private static String getOpnameDatumString( int opnameJaar1,
						int opnameMaand1,
						int opnameJaar2,
						int opnameMaand2 ) {
	String opnameDatumString = "";
	if ( opnameMaand1 > 0 ) {
	    opnameDatumString += maandString[ opnameMaand1 ] + " ";
	}
	if ( opnameJaar1 > 0 ) {
	    opnameDatumString += opnameJaar1;
	}
	if ( ( opnameMaand2 > 0 ) || ( opnameJaar2 > 0 ) ) {
	    opnameDatumString += " - ";
	    if ( opnameMaand2 > 0 ) {
		opnameDatumString += maandString[ opnameMaand2 ] + " ";
	    }
	    if ( opnameJaar2 == 0 ) {
		opnameDatumString += opnameJaar1;
	    } else {
		opnameDatumString += opnameJaar2;
	    }
	}
	return opnameDatumString;
    }

    private void insertOpnameDatum( ) {
	int opnameJaar1 = 0;
	int opnameMaand1 = 0;
	int opnameJaar2 = 0;
	int opnameMaand2 = 0;

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( "SELECT MAX( opname_datum_id ) FROM opname_datum" );
	    if ( ! resultSet.next( ) ) {
		logger.severe( "Could not get maximum for opname_datum_id in opnameDatum" );
		dialog.setVisible( false );
		return;
	    }
	    opnameDatumId = resultSet.getInt( 1 ) + 1;

	    String insertString = ( "INSERT INTO opname_datum SET " +
				    "opname_datum_id = " + opnameDatumId );

	    String opnameJaar1String = ( String )opnameJaar1ComboBox.getSelectedItem( );
	    if ( opnameJaar1String != null ) {
		if ( opnameJaar1String.length( ) > 0 ) {
		    try {
			opnameJaar1 = Integer.parseInt( opnameJaar1String );
			if ( opnameJaar1 != 0 ) insertString += ", opname_jaar_1 = " + opnameJaar1;
		    } catch ( Exception exception ) {
			logger.severe( "Exception opnameJaar1: " + exception.getMessage( ) );
		    }
		}
	    }

	    opnameMaand1 = opnameMaand1ComboBox.getSelectedMaand( );
	    if ( opnameMaand1 != 0 ) insertString += ", opname_maand_1 = " + opnameMaand1;

	    String opnameJaar2String = ( String )opnameJaar2ComboBox.getSelectedItem( );
	    if ( opnameJaar2String != null ) {
		if ( opnameJaar2String.length( ) > 0 ) {
		    try {
			opnameJaar2 = Integer.parseInt( opnameJaar2String );
			if ( opnameJaar2 != 0 ) insertString += ", opname_jaar_2 = " + opnameJaar2;
		    } catch ( Exception exception ) {
			logger.severe( "Exception opnameJaar2: " + exception.getMessage( ) );
		    }
		}
	    }

	    opnameMaand2 = opnameMaand2ComboBox.getSelectedMaand( );
	    if ( opnameMaand2 != 0 ) insertString += ", opname_maand_2 = " + opnameMaand2;

	    insertString += ( ", opname_datum = '" +
			      getOpnameDatumString( opnameJaar1, opnameMaand1,
						    opnameJaar2, opnameMaand2 ) +
			      "'" );

	    logger.fine( "insertString: " + insertString );

	    nUpdate = statement.executeUpdate( insertString );

	    if ( nUpdate != 1 ) {
		logger.severe( "Could not insert in opname_datum" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    private String updateString = null;

    private void addToUpdateString( String additionalUpdateString ) {
	if ( updateString == null ) {
	    updateString = additionalUpdateString;
	} else {
	    updateString += ", " + additionalUpdateString;
	}
    }

    void updateOpnameDatum( ) {
	// Initialise string holding the update query
	updateString = null;

	int opnameJaar1 = 0;
	int opnameMaand1 = 0;
	int opnameJaar2 = 0;
	int opnameMaand2 = 0;

	String opnameJaar1String = ( String )opnameJaar1ComboBox.getSelectedItem( );
	if ( opnameJaar1String != null ) {
	    if ( opnameJaar1String.length( ) > 0 ) {
		try {
		    opnameJaar1 = Integer.parseInt( opnameJaar1String );
		    // Check if opnameJaar1 changed to a non-zero value
		    if ( ( opnameJaar1 != 0 ) && ( opnameJaar1 != defaultOpnameJaar1 ) ) {
			addToUpdateString( "opname_jaar_1 = " + opnameJaar1 );
		    }
		} catch ( Exception exception ) {
		    logger.severe( "Exception opnameJaar1: " + exception.getMessage( ) );
		}
	    }
	}

	// Check if opnameMaand1 changed to a non-zero value
	opnameMaand1 = opnameMaand1ComboBox.getSelectedMaand( );
	if ( ( opnameMaand1 != 0 ) && ( opnameMaand1 != defaultOpnameMaand1 ) ) {
	    addToUpdateString( "opname_maand_1 = " + opnameMaand1 );
	}

	String opnameJaar2String = ( String )opnameJaar2ComboBox.getSelectedItem( );
	if ( opnameJaar2String != null ) {
	    if ( opnameJaar2String.length( ) > 0 ) {
		try {
		    opnameJaar2 = Integer.parseInt( opnameJaar2String );
		    // Check if opnameJaar2 changed to a non-zero value
		    if ( ( opnameJaar2 != 0 ) && ( opnameJaar2 != defaultOpnameJaar2 ) ) {
			addToUpdateString( "opname_jaar_2 = " + opnameJaar2 );
		    }
		} catch ( Exception exception ) {
		    logger.severe( "Exception opnameJaar2: " + exception.getMessage( ) );
		}
	    }
	}

	// Check if opnameMaand2 changed to a non-zero value
	opnameMaand2 = opnameMaand2ComboBox.getSelectedMaand( );
	if ( ( opnameMaand2 != 0 ) && ( opnameMaand2 != defaultOpnameMaand2 ) ) {
	    addToUpdateString( "opname_maand_2 = " + opnameMaand2 );
	}

	// Check if any update is necessary at all
	if ( updateString == null ) {
	    logger.info( "No update necessary" );
	    return;
	}

	updateString += ( ", opname_datum = '" +
			  getOpnameDatumString( opnameJaar1, opnameMaand1,
						opnameJaar2, opnameMaand2 ) +
			  "'" );

	updateString  = "UPDATE opname_datum SET " + updateString;
	updateString += " WHERE opname_datum_id = " + opnameDatumId;
	logger.info( "EditOpnameDatumDialog.updateOpnameDatum, updateString: " + updateString );

	try {
	    Statement statement = conn.createStatement( );
	    nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update in opname_datum" );
		dialog.setVisible( false );
		return;
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public boolean opnameDatumUpdated( ) { return nUpdate > 0; }

    public String getOpnameDatumString( ) { return opnameDatumString; }

    public int getOpnameDatumId( ) { return opnameDatumId; }
}
