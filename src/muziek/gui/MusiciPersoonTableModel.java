// Class to setup a TableModel for musici_persoon

package muziek.gui;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

import java.util.ArrayList;
import java.util.logging.*;
import java.util.regex.*;


public class MusiciPersoonTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "boeken.gui.MusiciPersoonTableModel" );

    private Connection connection;

    private String[ ] headings = { "Musicus", "Rol" };

    class MusicusRecord {
	int	persoonId;
	String  persoonString;
	int	rolId;
	String  rolString;

	public MusicusRecord( int    persoonId,
			      String persoonString,
			      int    rolId,
			      String rolString ) {
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	    this.rolId = rolId;
	    this.rolString = rolString;
	}
    }

    ArrayList musicusRecordList = new ArrayList( 10 );
    private int musiciId = 0;

    RolComboBox rolComboBox;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    public MusiciPersoonTableModel( Connection connection ) {
	this.connection = connection;

	// Create the rol combo box
	rolComboBox = new RolComboBox( connection, null, false );
    }

    // Constructor
    public MusiciPersoonTableModel( Connection connection, int musiciId ) {
	this.connection = connection;

	// Create the rol combo box
	rolComboBox = new RolComboBox( connection, null, false );

	showTable( musiciId );
    }

    public int getRowCount( ) { return musicusRecordList.size( ); }

    public int getColumnCount( ) { return 2; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public boolean isCellEditable( int row, int column ) { return true; }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= musicusRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	switch ( column ) {
	case 0:
	    return ( ( MusicusRecord )musicusRecordList.get( row ) ).persoonString;
	case 1:
	    return ( ( MusicusRecord )musicusRecordList.get( row ) ).rolString;
	}

	return null;
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= musicusRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final MusicusRecord musicusRecord =
	    ( MusicusRecord )musicusRecordList.get( row );

	try {
	    switch ( column ) {
	    case 0:
		String updateString = null;
		String persoonString = ( String )object;
		if ( ( ( persoonString == null ) || ( persoonString.length( ) == 0 ) ) &&
		     ( musicusRecord.persoonString != null ) &&
		     ( musicusRecord.persoonString.length( ) != 0 ) ) {
		    updateString = "persoon = NULL ";
		    musicusRecord.persoonString = persoonString;
		} else if ( ( persoonString != null ) &&
			    ( !persoonString.equals( musicusRecord.persoonString ) ) ) {
		    // Matcher to find single quotes in persoon, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( persoonString );
		    updateString = "persoon = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    musicusRecord.persoonString = persoonString;
		} else {
		    // No action necessary
		    return;
		}

		updateString = "UPDATE persoon SET " + updateString + " WHERE persoon_id = " + musicusRecord.persoonId;
		logger.fine( "updateString: " + updateString );

		try {
		    Statement statement = connection.createStatement( );
		    int nUpdate = statement.executeUpdate( updateString );
		    if ( nUpdate != 1 ) {
			logger.severe( "Could not update record with persoon_id " + musicusRecord.persoonId +
				       " in persoon, nUpdate = " + nUpdate );
			return;
		    }
		} catch ( SQLException sqlException ) {
		    logger.severe( "SQLException: " + sqlException.getMessage( ) );
		    return;
		}

		break;

	    case 1:
		// No need to update musici_persoon table,
		// since all records will be removed and inserted again
		int rolId = rolComboBox.getRolId( ( String )object );
		if ( rolId != musicusRecord.rolId ) {
		    musicusRecord.rolId = rolId;
		    musicusRecord.rolString = ( String )object;
		} else {
		    // No action necessary
		    return;
		}

		break;

	    default:
		logger.severe( "Invalid column: " + column );
		return;
	    }
	} catch ( Exception exception ) {
	    logger.severe( "could not get value from " +
			   object + " for column " + column + " in row " + row );
	    return;
	}

	// Store record in list
	musicusRecordList.set( row, musicusRecord );

	fireTableCellUpdated( row, column );
    }

    public void showTable( int musiciId ) {
	this.musiciId = musiciId;
	showTable( );
    }

    public void showTable( ) {
	// Clear the musici list
	musicusRecordList.clear( );

	try {
	    Statement musiciStmt = connection.createStatement( );
	    ResultSet musiciResultSet = musiciStmt.executeQuery( "SELECT persoon.persoon_id, persoon.persoon, " +
								 "rol.rol_id, rol.rol " +
								 "FROM musici_persoon " +
								 "LEFT JOIN persoon ON persoon.persoon_id = musici_persoon.persoon_id " +
								 "LEFT JOIN rol ON rol.rol_id = musici_persoon.rol_id " +
								 "WHERE musici_persoon.musici_id = " + musiciId + " " +
								 "ORDER BY persoon.persoon, rol.rol" );

	    while ( musiciResultSet.next( ) ) {
		musicusRecordList.add( new MusicusRecord( musiciResultSet.getInt( 1 ),
							  musiciResultSet.getString( 2 ),
							  musiciResultSet.getInt( 3 ),
							  musiciResultSet.getString( 4 ) ) );
	    }

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void addRow( int persoonId, String persoonString ) {
	musicusRecordList.add( new MusicusRecord( persoonId, persoonString, 0, "" ) );
	fireTableDataChanged( );
    }

    public void replaceRow( int row, int persoonId, String persoonString ) {
	if ( ( row < 0 ) || ( row >= musicusRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	musicusRecordList.set( row,
			       new MusicusRecord( persoonId, persoonString, 0, "" ) );
	fireTableDataChanged( );
    }

    public void removeRow( int row ) {
	if ( ( row < 0 ) || ( row >= musicusRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}
	musicusRecordList.remove( row );
	fireTableDataChanged( );
    }


    // When inserting for a new musici record, the musiciId still must be set:
    // only allow inserting in the table for other classes when the musici ID is specified.
    public void insertTable( int musiciId ) {
	this.musiciId = musiciId;

	insertTable( );
    }

    private void insertTable( ) {
	// Check for valid musici ID
	if ( musiciId == 0 ) {
	    logger.severe( "musici not selected" );
	    return;
	}

	try {
	    // Insert new rows in the musici_persoon table
	    Statement statement = connection.createStatement( );
	    for ( int musicusIndex = 0; musicusIndex < musicusRecordList.size( ); musicusIndex++ ) {
		int persoonId = ( ( MusicusRecord )musicusRecordList.get( musicusIndex ) ).persoonId;
		int rolId = ( ( MusicusRecord )musicusRecordList.get( musicusIndex ) ).rolId;
		int nUpdate = statement.executeUpdate( "INSERT INTO musici_persoon SET musici_id = " + musiciId +
						       ", persoon_id = " + persoonId +
						       ", rol_id = " + rolId );
		if ( nUpdate == 0 ) {
		    logger.severe( "Could not insert in musici_persoon for index " + musicusIndex );
		    return;
		}
	    }
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public void updateTable( ) {
	// Check for valid musici ID
	if ( musiciId == 0 ) {
	    logger.severe( "musici not selected" );
	    return;
	}

	try {
	    // Remove all existing rows with the current musici ID from the musici_persoon table
	    Statement statement = connection.createStatement( );
	    statement.executeUpdate( "DELETE FROM musici_persoon WHERE musici_id = " + musiciId );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}

	// Insert the table in musici_persoon
	insertTable( );
    }
}
