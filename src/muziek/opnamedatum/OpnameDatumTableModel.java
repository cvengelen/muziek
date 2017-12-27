package muziek.opnamedatum;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.table.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;

/**
 * Class to setup a TableModel for records in opname_datum
 */
class OpnameDatumTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( OpnameDatumTableModel.class.getCanonicalName() );

    private final Connection connection;
    private final JFrame parentFrame;

    private final String[ ] headings = { "Id",
                                         "Jaar 1", "Maand 1",
                                         "Jaar 2", "Maand 2",
                                         "Opname Datum" };

    private class OpnameDatumRecord {
	int	opnameDatumId;
	Integer	opnameJaar1Integer;
	Integer opnameMaand1Integer;
	Integer	opnameJaar2Integer;
	Integer	opnameMaand2Integer;
	String	opnameDatumString;

	OpnameDatumRecord( int	 opnameDatumId,
                           int	 opnameJaar1,
                           int	 opnameMaand1,
                           int	 opnameJaar2,
                           int	 opnameMaand2,
                           String opnameDatumString ) {
	    this.opnameDatumId       = opnameDatumId;
	    this.opnameJaar1Integer  = opnameJaar1;
	    this.opnameMaand1Integer = opnameMaand1;
	    this.opnameJaar2Integer  = opnameJaar2;
	    this.opnameMaand2Integer = opnameMaand2;
	    this.opnameDatumString   = opnameDatumString;
	}
    }

    private final ArrayList<OpnameDatumRecord> opnameDatumRecordList = new ArrayList<>( 200 );

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );


    // Constructor
    OpnameDatumTableModel( Connection connection, JFrame parentFrame ) {
	this.connection = connection;
        this.parentFrame = parentFrame;

	setupOpnameDatumTableModel( null );
    }

    void setupOpnameDatumTableModel( String opnameDatumFilterString ) {

	// Setup the table
	try {
	    String opnameDatumQueryString =
		"SELECT opname_datum.opname_datum_id, " +
		"opname_datum.opname_jaar_1, opname_datum.opname_maand_1, " +
		"opname_datum.opname_jaar_2, opname_datum.opname_maand_2, " +
		"opname_datum.opname_datum " +
		"FROM opname_datum ";

	    if ( ( opnameDatumFilterString != null ) && ( opnameDatumFilterString.length( ) > 0 ) ) {
		    opnameDatumQueryString += " WHERE opname_datum.opname_datum LIKE \"%" + opnameDatumFilterString + "%\" ";
	    }

	    opnameDatumQueryString += "ORDER BY opname_datum.opname_jaar_1, opname_datum.opname_maand_1";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opnameDatumQueryString );

	    // Clear the list
	    opnameDatumRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		opnameDatumRecordList.add( new OpnameDatumRecord( resultSet.getInt( 1 ),
								  resultSet.getInt( 2 ),
								  resultSet.getInt( 3 ),
								  resultSet.getInt( 4 ),
								  resultSet.getInt( 5 ),
								  resultSet.getString( 6 ) ) );
	    }

	    opnameDatumRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in select: " + sqlException.getMessage(),
                                           "OpnameDatumTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return opnameDatumRecordList.size( ); }

    public int getColumnCount( ) { return 6; }


    public String getColumnName( int column ) {
	return headings[ column ];
    }

    // Indicate the class for each column for setting the correct default renderer
    // see file:///home/cvengelen/java/tutorial/uiswing/components/table.html
    public Class getColumnClass( int column ) {
	switch ( column ) {
	case 5: // opname datum
	    return String.class;
	}

	return Integer.class;
    }

    public boolean isCellEditable( int row, int column ) {
	switch ( column ) {
	case 0:	// id
	    return false;
	}
	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= opnameDatumRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final OpnameDatumRecord opnameDatumRecord = opnameDatumRecordList.get( row );

	if ( column == 0 ) return opnameDatumRecord.opnameDatumId;
	if ( column == 1 ) return opnameDatumRecord.opnameJaar1Integer;
	if ( column == 2 ) return opnameDatumRecord.opnameMaand1Integer;
	if ( column == 3 ) return opnameDatumRecord.opnameJaar2Integer;
	if ( column == 4 ) return opnameDatumRecord.opnameMaand2Integer;
	if ( column == 5 ) return opnameDatumRecord.opnameDatumString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= opnameDatumRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	final OpnameDatumRecord opnameDatumRecord = opnameDatumRecordList.get( row );

	String updateString = null;

	try {
	    switch ( column ) {
	    case 1:
		int jaar1 = ( Integer )object;
		if ( ( jaar1 == 0 ) &&
		     ( opnameDatumRecord.opnameJaar1Integer != 0 ) ) {
		    updateString = "opname_jaar_1 = NULL ";
		    opnameDatumRecord.opnameJaar1Integer = ( Integer )object;
		} else if ( jaar1 != opnameDatumRecord.opnameJaar1Integer ) {
		    updateString = "opname_jaar_1 = " + jaar1;
		    opnameDatumRecord.opnameJaar1Integer = ( Integer )object;
		}
		break;

	    case 2:
		int maand1 = ( Integer )object;
		if ( ( maand1 == 0 ) &&
		     ( opnameDatumRecord.opnameMaand1Integer != 0 ) ) {
		    updateString = "opname_maand_1 = NULL ";
		    opnameDatumRecord.opnameMaand1Integer = ( Integer )object;
		} else if ( maand1 != opnameDatumRecord.opnameMaand1Integer ) {
		    updateString = "opname_maand_1 = " + maand1;
		    opnameDatumRecord.opnameMaand1Integer = ( Integer )object;
		}
		break;

	    case 3:
		int jaar2 = ( Integer )object;
		if ( ( jaar2 == 0 ) &&
		     ( opnameDatumRecord.opnameJaar2Integer != 0 ) ) {
		    updateString = "opname_jaar_2 = NULL ";
		    opnameDatumRecord.opnameJaar2Integer = ( Integer )object;
		} else if ( jaar2 != opnameDatumRecord.opnameJaar2Integer ) {
		    updateString = "opname_jaar_2 = " + jaar2;
		    opnameDatumRecord.opnameJaar2Integer = ( Integer )object;
		}
		break;

	    case 4:
		int maand2 = ( Integer )object;
		if ( ( maand2 == 0 ) &&
		     ( opnameDatumRecord.opnameMaand2Integer != 0 ) ) {
		    updateString = "opname_maand_2 = NULL ";
		    opnameDatumRecord.opnameMaand2Integer = ( Integer )object;
		} else if ( maand2 != opnameDatumRecord.opnameMaand2Integer ) {
		    updateString = "opname_maand_2 = " + maand2;
		    opnameDatumRecord.opnameMaand2Integer = ( Integer )object;
		}
		break;

	    case 5:
		String opnameDatumString = ( String )object;
		if ( ( ( opnameDatumString == null ) || ( opnameDatumString.length( ) == 0 ) ) &&
		     ( opnameDatumRecord.opnameDatumString != null ) &&
		     ( opnameDatumRecord.opnameDatumString.length( ) != 0 ) ) {
		    updateString = "opname_datum = NULL ";
		    opnameDatumRecord.opnameDatumString = opnameDatumString;
		} else if ( ( opnameDatumString != null ) &&
			    ( !opnameDatumString.equals( opnameDatumRecord.opnameDatumString ) ) ) {
		    // Matcher to find single quotes in opnameDatumString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    final Matcher quoteMatcher = quotePattern.matcher( opnameDatumString );
		    updateString = "opname_datum = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
		    opnameDatumRecord.opnameDatumString = opnameDatumString;
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

	updateString = "UPDATE opname_datum SET " + updateString + " WHERE opname_datum_id = " + opnameDatumRecord.opnameDatumId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with opname_datum_id " +
			       opnameDatumRecord.opnameDatumId );
	    	return;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in update: " + sqlException.getMessage(),
                                           "OpnameDatumTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return;
	}

	// Store record in list
	opnameDatumRecordList.set( row, opnameDatumRecord );

	fireTableCellUpdated( row, column );
    }

    int getOpnameDatumId( int row ) {
        if ( ( row < 0 ) || ( row >= opnameDatumRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return opnameDatumRecordList.get( row ).opnameDatumId;
    }

    String getOpnameDatumString( int row ) {
	if ( ( row < 0 ) || ( row >= opnameDatumRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return opnameDatumRecordList.get( row ).opnameDatumString;
    }
}
