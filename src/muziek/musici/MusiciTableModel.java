package muziek.musici;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

/**
 * Class to setup a TableModel for records in musici
 */
class MusiciTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( MusiciTableModel.class.getCanonicalName() );

    private final Connection connection;
    private final JFrame parentFrame;

    private final String[ ] headings = { "Id", "Musici", "Persoon", "Rol", "Ensemble" };

    private class MusiciRecord {
	int	musiciId;
	String	musiciString;
	int	persoonId;
	String	persoonString;
	int	rolId;
	String	rolString;
	int	ensembleId;
	String	ensembleString;

	MusiciRecord( int     musiciId,
                      String  musiciString,
                      int     persoonId,
                      String  persoonString,
                      int     rolId,
                      String  rolString,
                      int     ensembleId,
                      String  ensembleString ) {
	    this.musiciId = musiciId;
	    this.musiciString = musiciString;
	    this.persoonId = persoonId;
	    this.persoonString = persoonString;
	    this.rolId = rolId;
	    this.rolString = rolString;
	    this.ensembleId = ensembleId;
	    this.ensembleString = ensembleString;
	}
    }

    private final ArrayList<MusiciRecord> musiciRecordList = new ArrayList<>( 500 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    MusiciTableModel( Connection connection, JFrame parentFrame ) {
	this.connection = connection;
        this.parentFrame = parentFrame;

	setupMusiciTableModel( null, 0, 0, 0 );
    }

    void setupMusiciTableModel( String musiciFilterString,
                                int selectedPersoonId,
                                int selectedRolId,
                                int selectedEnsembleId ) {
	// Setup the table
	try {
	    String musiciQueryString =
		"SELECT musici.musici_id, musici.musici, " +
		"musici_persoon.persoon_id, persoon.persoon, musici_persoon.rol_id, rol.rol, " +
		"musici_ensemble.ensemble_id, ensemble.ensemble " +
		"FROM musici " +
		"LEFT JOIN musici_persoon ON musici_persoon.musici_id = musici.musici_id " +
		"LEFT JOIN persoon ON persoon.persoon_id = musici_persoon.persoon_id " +
		"LEFT JOIN rol ON rol.rol_id = musici_persoon.rol_id " +
		"LEFT JOIN musici_ensemble ON musici_ensemble.musici_id = musici.musici_id " +
		"LEFT JOIN ensemble ON ensemble.ensemble_id = musici_ensemble.ensemble_id ";

	    if ( ( ( musiciFilterString != null ) && ( musiciFilterString.length( ) > 0 ) ) ||
		 ( selectedPersoonId != 0 ) ||
		 ( selectedRolId != 0 ) ||
		 ( selectedEnsembleId != 0 ) ) {
		musiciQueryString += "WHERE ";

		if ( ( musiciFilterString != null ) && ( musiciFilterString.length( ) > 0 ) ) {
		    musiciQueryString += "musici.musici LIKE \"%" + musiciFilterString + "%\" ";

		    if ( ( selectedPersoonId != 0 ) ||
			 ( selectedRolId != 0 ) ||
			 ( selectedEnsembleId != 0 ) ) {
			musiciQueryString += "AND ";
		    }
		}

		if ( selectedPersoonId != 0 ) {
		    musiciQueryString += "musici_persoon.persoon_id = " + selectedPersoonId + " ";

		    if ( ( selectedRolId != 0 ) ||
			 ( selectedEnsembleId != 0 ) ) {
			musiciQueryString += "AND ";
		    }
		}

		if ( selectedRolId != 0 ) {
		    musiciQueryString += "musici_persoon.rol_id = " + selectedRolId + " ";

		    if ( selectedEnsembleId != 0 ) {
			musiciQueryString += "AND ";
		    }
		}

		if ( selectedEnsembleId != 0 ) {
		    musiciQueryString += "musici_ensemble.ensemble_id = " + selectedEnsembleId + " ";
		}
	    }

	    musiciQueryString += "ORDER BY persoon.persoon, ensemble.ensemble";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( musiciQueryString );

	    // Clear the list
	    musiciRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		musiciRecordList.add( new MusiciRecord( resultSet.getInt( 1 ),
							resultSet.getString( 2 ),
							resultSet.getInt( 3 ),
							resultSet.getString( 4 ),
							resultSet.getInt( 5 ),
							resultSet.getString( 6 ),
							resultSet.getInt( 7 ),
							resultSet.getString( 8 ) ) );
	    }

	    musiciRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in select: " + sqlException.getMessage(),
                                           "MusiciTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return musiciRecordList.size( ); }

    public int getColumnCount( ) { return 5; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public Class getColumnClass( int column ) {
	switch ( column ) {
	case 0: // Id
	    return Integer.class;
	}
	return String.class;
    }

    public boolean isCellEditable( int row, int column ) {
	switch ( column ) {
	case 0: // Id
	case 2: // persoon
	case 3: // rol
	case 4: // ensemble
	    // Do not allow editing
	    return false;
	}
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= musiciRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final MusiciRecord musiciRecord = musiciRecordList.get( row );

	if ( column == 0 ) return musiciRecord.musiciId;
	if ( column == 1 ) return musiciRecord.musiciString;
	if ( column == 2 ) return musiciRecord.persoonString;
	if ( column == 3 ) return musiciRecord.rolString;
	if ( column == 4 ) return musiciRecord.ensembleString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= musiciRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final MusiciRecord musiciRecord = musiciRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		String musiciString = ( String )object;
		if ( ( ( musiciString == null ) || ( musiciString.length( ) == 0 ) ) &&
		     ( musiciRecord.musiciString != null ) &&
		     ( musiciRecord.musiciString.length( ) != 0 ) ) {
		    updateString = "musici = NULL ";
		    musiciRecord.musiciString = musiciString;
		} else if ( ( musiciString != null ) &&
			    ( !musiciString.equals( musiciRecord.musiciString ) ) ) {
		    // Matcher to find single quotes in musici, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( musiciString );
		    updateString = "musici = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    musiciRecord.musiciString = musiciString;
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

	// Check if update is not necessary
	if ( updateString == null ) return;

	updateString = "UPDATE musici SET " + updateString + " WHERE musici_id = " + musiciRecord.musiciId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
		logger.severe( "Could not update record with musici_id " + musiciRecord.musiciId +
			       " in musici, nUpdate = " + nUpdate );
		return;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in update: " + sqlException.getMessage(),
                                           "MusiciTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	musiciRecordList.set( row, musiciRecord );

	fireTableCellUpdated( row, column );
    }

    int getMusiciId( int row ) {
        if ( ( row < 0 ) || ( row >= musiciRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return musiciRecordList.get( row ).musiciId;
    }

    String getMusiciString( int row ) {
	if ( ( row < 0 ) || ( row >= musiciRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return musiciRecordList.get( row ).musiciString;
    }
}
