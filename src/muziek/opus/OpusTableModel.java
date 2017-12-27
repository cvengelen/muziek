// Project:	muziek
// Package:	muziek.opus
// File:	OpusTableModel.java
// Description:	TableModel for records in opus
// Author:	Chris van Engelen
// History:	2006/02/26: Initial version
//              2016/04/27: Refactoring, and use of Java 7, 8 features

package muziek.opus;

import muziek.gui.GenreComboBox;
import muziek.gui.SubtypeComboBox;
import muziek.gui.TijdperkComboBox;
import muziek.gui.TypeComboBox;

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
 * TableModel for opus records
 */
class OpusTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( OpusTableModel.class.getCanonicalName() );

    private final Connection connection;
    private final JFrame parentFrame;

    private final String[ ] headings = { "Id", "Titel", "Componisten", "Opus", "Genre",
                                         "Tijdperk", "Type", "Subtype" };

    private class OpusRecord {
	int	opusId;
	String	opusTitelString;
        String  componistString;
	String	componistenString;
	String	opusNummerString;
	int	genreId;
	String	genreString;
	int	tijdperkId;
	String	tijdperkString;
	int	typeId;
	String	typeString;
	int	subtypeId;
	String	subtypeString;

	OpusRecord( int    opusId,
                    String opusTitelString,
                    String componistString,
                    String componistenString,
                    String opusNummerString,
                    int    genreId,
                    String genreString,
                    int    tijdperkId,
                    String tijdperkString,
                    int    typeId,
                    String typeString,
                    int    subtypeId,
                    String subtypeString ) {
	    this.opusId = opusId;
	    this.opusTitelString = opusTitelString;
            this.componistString = componistString;
	    this.componistenString = componistenString;
	    this.opusNummerString = opusNummerString;
	    this.genreId = genreId;
	    this.genreString = genreString;
	    this.tijdperkId = tijdperkId;
	    this.tijdperkString = tijdperkString;
	    this.typeId = typeId;
	    this.typeString = typeString;
	    this.subtypeId = subtypeId;
	    this.subtypeString = subtypeString;
	}

	// Copy constructor
	OpusRecord( OpusRecord opusRecord ) {
	    this.opusId = opusRecord.opusId;
	    this.opusTitelString = opusRecord.opusTitelString;
            this.componistString = opusRecord.componistString;
	    this.componistenString = opusRecord.componistenString;
	    this.opusNummerString = opusRecord.opusNummerString;
	    this.genreId = opusRecord.genreId;
	    this.genreString = opusRecord.genreString;
	    this.tijdperkId = opusRecord.tijdperkId;
	    this.tijdperkString = opusRecord.tijdperkString;
	    this.typeId = opusRecord.typeId;
	    this.typeString = opusRecord.typeString;
	    this.subtypeId = opusRecord.subtypeId;
	    this.subtypeString = opusRecord.subtypeString;
	}

        // Check if two OpusRecords are equal, except for the componist string
        // so that that two OpusRecords which differ only in componist will be regarded as the same.
        @Override
        public boolean equals(Object object) {
            if (object == null) return false;
            if (!(object instanceof OpusRecord)) return false;
            final OpusRecord opusRecord = (OpusRecord)object;

            // No check for componistString!
            if (opusId != opusRecord.opusId) return false;
            if (!OpusTableModel.stringEquals(opusTitelString, opusRecord.opusTitelString)) return false;
            if (!OpusTableModel.stringEquals(componistenString, opusRecord.componistenString)) return false;
            if (!OpusTableModel.stringEquals(opusNummerString, opusRecord.opusNummerString)) return false;
            if (genreId != opusRecord.genreId) return false;
            if (!OpusTableModel.stringEquals(genreString, opusRecord.genreString)) return false;
            if (tijdperkId != opusRecord.tijdperkId) return false;
            if (!OpusTableModel.stringEquals(tijdperkString, opusRecord.tijdperkString)) return false;
            if (typeId != opusRecord.typeId) return false;
            if (!OpusTableModel.stringEquals(typeString, opusRecord.typeString)) return false;
            if (subtypeId != opusRecord.subtypeId) return false;
            if (!OpusTableModel.stringEquals(subtypeString, opusRecord.subtypeString)) return false;

            return true;
        }
    }

    private final ArrayList<OpusRecord> opusRecordList = new ArrayList<>( 1500 );

    private GenreComboBox genreComboBox;
    private TijdperkComboBox tijdperkComboBox;
    private TypeComboBox typeComboBox;
    private SubtypeComboBox subtypeComboBox;

    private JButton cancelRowEditButton;
    private JButton saveRowEditButton;

    private boolean	rowModified = false;
    private int		editRow = -1;
    private OpusRecord	opusRecord;
    private OpusRecord	originalOpusRecord;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );

    private final static Pattern componistPattern = Pattern.compile( "(.+?), (.*)" );


    // Constructor
    OpusTableModel( final Connection connection,
                    final JFrame     parentFrame,
                    final JButton    cancelRowEditButton,
                    final JButton    saveRowEditButton ) {
	this.connection = connection;
        this.parentFrame = parentFrame;
	this.cancelRowEditButton = cancelRowEditButton;
	this.saveRowEditButton = saveRowEditButton;

	// Create the combo boxes
	genreComboBox = new GenreComboBox( connection, 0 );
	tijdperkComboBox = new TijdperkComboBox( connection, 0 );
	typeComboBox = new TypeComboBox( connection, 0 );
	subtypeComboBox = new SubtypeComboBox( connection, null, false );

	setupOpusTableModel( null, null, 0, 0, 0, 0, 0, 0 );
    }

    void setupOpusTableModel( String opusTitelFilterString,
                              String opusNummerFilterString,
                              int    componistenPersoonId,
                              int    componistenId,
                              int    genreId,
                              int    tijdperkId,
                              int    typeId,
                              int    subtypeId ) {

	// Setup the table
	try {
	    String opusQueryString =
		"SELECT opus.opus_id, opus.opus_titel, componist.persoon, componisten.componisten, opus.opus_nummer, " +
		"opus.genre_id, genre.genre, opus.tijdperk_id, tijdperk.tijdperk, " +
		"opus.type_id, type.type, opus.subtype_id, subtype.subtype " +
		"FROM opus " +
		"LEFT JOIN componisten ON componisten.componisten_id = opus.componisten_id " +
		"LEFT JOIN componisten_persoon ON componisten_persoon.componisten_id = opus.componisten_id " +
		"LEFT JOIN persoon as componist ON componist.persoon_id = componisten_persoon.persoon_id " +
		"LEFT JOIN genre ON genre.genre_id = opus.genre_id " +
		"LEFT JOIN tijdperk ON tijdperk.tijdperk_id = opus.tijdperk_id " +
		"LEFT JOIN type ON type.type_id = opus.type_id " +
		"LEFT JOIN subtype ON subtype.subtype_id = opus.subtype_id ";

	    if ( ( ( opusTitelFilterString != null ) && ( opusTitelFilterString.length( ) > 0 ) ) ||
		 ( ( opusNummerFilterString != null ) && ( opusNummerFilterString.length( ) > 0 ) ) ||
		 ( componistenPersoonId != 0 ) ||
		 ( componistenId != 0 ) ||
		 ( genreId != 0 ) ||
		 ( tijdperkId != 0 ) ||
		 ( typeId != 0 ) ||
		 ( subtypeId != 0 ) ) {
		opusQueryString += "WHERE ";

		if ( ( opusTitelFilterString != null ) && ( opusTitelFilterString.length( ) > 0 ) ) {
		    // Matcher to find single quotes in opusTitelFilterString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    Matcher quoteMatcher = quotePattern.matcher( opusTitelFilterString );
		    opusQueryString += "opus.opus_titel LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
		    if ( ( ( opusNummerFilterString != null ) && ( opusNummerFilterString.length( ) > 0 ) ) ||
			 ( componistenPersoonId != 0 ) ||
			 ( componistenId != 0 ) ||
			 ( genreId != 0 ) ||
			 ( tijdperkId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( subtypeId != 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		if ( ( opusNummerFilterString != null ) && ( opusNummerFilterString.length( ) > 0 ) ) {
		    opusQueryString += "opus.opus_nummer LIKE \"%" + opusNummerFilterString + "%\" ";
		    if ( ( componistenPersoonId != 0 ) ||
			 ( componistenId != 0 ) ||
			 ( genreId != 0 ) ||
			 ( tijdperkId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( subtypeId != 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		if ( componistenPersoonId != 0 ) {
		    opusQueryString += "componisten_persoon.persoon_id = " + componistenPersoonId + " ";
		    if ( ( componistenId != 0 ) ||
			 ( genreId != 0 ) ||
			 ( tijdperkId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( subtypeId != 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		if ( componistenId != 0 ) {
		    opusQueryString += "opus.componisten_id = " + componistenId + " ";
		    if ( ( genreId != 0 ) ||
			 ( tijdperkId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( subtypeId != 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		if ( genreId != 0 ) {
		    opusQueryString += "opus.genre_id = " + genreId + " ";
		    if ( ( tijdperkId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( subtypeId != 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		if ( tijdperkId != 0 ) {
		    opusQueryString += "opus.tijdperk_id = " + tijdperkId + " ";
		    if ( ( typeId != 0 ) ||
			 ( subtypeId != 0 ) ) {
			opusQueryString += "AND ";
		    }
		}

		if ( typeId != 0 ) {
		    opusQueryString += "opus.type_id = " + typeId + " ";
		    if ( subtypeId != 0 ) {
			opusQueryString += "AND ";
		    }
		}

		if ( subtypeId != 0 ) {
		    opusQueryString += "opus.subtype_id = " + subtypeId + " ";
		}
	    }

	    opusQueryString += "ORDER BY componist.persoon, type.type, opus.opus_nummer, opus.opus_titel";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opusQueryString );

	    // Clear the list
	    opusRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		final OpusRecord opusRecord = new OpusRecord( resultSet.getInt( 1 ),
						              resultSet.getString( 2 ),
						              resultSet.getString( 3 ),
                                                              resultSet.getString( 4 ),
						              resultSet.getString( 5 ),
						              resultSet.getInt( 6 ),
						              resultSet.getString( 7 ),
						              resultSet.getInt( 8 ),
						              resultSet.getString( 9 ),
						              resultSet.getInt( 10 ),
						              resultSet.getString( 11 ),
						              resultSet.getInt( 12 ),
						              resultSet.getString( 13 ) );
                if (!opusRecordList.contains(opusRecord)) {
                    opusRecordList.add(opusRecord);
                }
	    }

	    opusRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in select: " + sqlException.getMessage(),
                                           "OpusTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return opusRecordList.size( ); }

    public int getColumnCount( ) { return 8; }

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
	// Only allow editing for the selected edit row
	if ( row != editRow ) return false;

	switch ( column ) {
	case 0:	// id
	case 2: // componisten
	    return false;
	}

	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= opusRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final OpusRecord opusRecord = opusRecordList.get( row );

        switch ( column ) {
        case 0:
            return opusRecord.opusId;
        case 1:
            return opusRecord.opusTitelString;
        case 2:
            // Use the componistString, and add the componistenString if it is different from the componistString.
            // First check the componistString for null (should normally not be the case).
            String componistenString = opusRecord.componistString;
            if ( componistenString == null ) {
                // Use the componistenString if set
                if ( opusRecord.componistenString != null ) {
                    componistenString = opusRecord.componistenString;
                } else {
                    componistenString = "-";
                }
            } else {
                String componistFullName = null;
                Matcher componistMatcher = componistPattern.matcher( componistenString );
                if ( componistMatcher.matches() ) {
                    componistFullName = componistMatcher.group( 2 ) + " " + componistMatcher.group( 1 );
                }
                if ( ( opusRecord.componistenString != null ) &&
                        !componistenString.equals( opusRecord.componistenString ) &&
                        ( componistFullName == null || !componistFullName.equals( opusRecord.componistenString ) ) ) {
                    componistenString += " (" + opusRecord.componistenString + ")";
                }
            }
            return componistenString;
        case 3:
            return opusRecord.opusNummerString;
        case 4:
            return opusRecord.genreString;
        case 5:
            return opusRecord.tijdperkString;
        case 6:
            return opusRecord.typeString;
        case 7:
            return opusRecord.subtypeString;
        default:
            break;
        }

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= opusRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	try {
	    switch ( column ) {
	    case 1:
		String opusTitelString = ( String )object;
		if ( ( ( opusTitelString == null ) || ( opusTitelString.length( ) == 0 ) ) &&
		     ( opusRecord.opusTitelString != null ) ) {
		    opusRecord.opusTitelString = null;
		    rowModified = true;
		} else if ( ( opusTitelString != null ) &&
			    ( !opusTitelString.equals( opusRecord.opusTitelString ) ) ) {
		    opusRecord.opusTitelString = opusTitelString;
		    rowModified = true;
		}
		break;

	    case 3:
		String opusNummerString = ( String )object;
		if ( ( ( opusNummerString == null ) || ( opusNummerString.length( ) == 0 ) ) &&
		     ( opusRecord.opusNummerString != null ) ) {
		    opusRecord.opusNummerString = null;
		    rowModified = true;
		} else if ( ( opusNummerString != null ) &&
			    ( !opusNummerString.equals( opusRecord.opusNummerString ) ) ) {
		    opusRecord.opusNummerString = opusNummerString;
		    rowModified = true;
		}
		break;

	    case 4:
		int genreId = genreComboBox.getGenreId( ( String )object );
		if ( genreId != opusRecord.genreId ) {
		    opusRecord.genreId = genreId;
		    opusRecord.genreString = ( String )object;
		    rowModified = true;
		}

		break;

	    case 5:
		int tijdperkId = tijdperkComboBox.getTijdperkId( ( String )object );
		if ( tijdperkId != opusRecord.tijdperkId ) {
		    opusRecord.tijdperkId = tijdperkId;
		    opusRecord.tijdperkString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 6:
		int typeId = typeComboBox.getTypeId( ( String )object );
		if ( typeId != opusRecord.typeId ) {
		    opusRecord.typeId = typeId;
		    opusRecord.typeString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 7:
		int subtypeId = subtypeComboBox.getSubtypeId( ( String )object );
		if ( subtypeId != opusRecord.subtypeId ) {
		    opusRecord.subtypeId = subtypeId;
		    opusRecord.subtypeString = ( String )object;
		    rowModified = true;
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
	opusRecordList.set( row, opusRecord );

	if ( rowModified ) {
	    // Enable cancel and save buttons
	    cancelRowEditButton.setEnabled( true );
	    saveRowEditButton.setEnabled( true );
	}

	fireTableCellUpdated( row, column );
    }

    int getOpusId( int row ) {
	final OpusRecord opusRecord = opusRecordList.get( row );

	return opusRecord.opusId;
    }

    String getOpusTitelString( int row ) {
	final OpusRecord opusRecord = opusRecordList.get( row );

	return opusRecord.opusTitelString;
    }

    void setEditRow( int editRow ) {
	// Initialize record to be edited
	opusRecord = opusRecordList.get( editRow );

	// Copy record to use as key in table update
	originalOpusRecord = new OpusRecord( opusRecord );

	// Initialize row modified status
	rowModified = false;

	// Allow editing for the selected row
	this.editRow = editRow;
    }

    void unsetEditRow( ) {
	this.editRow = -1;
    }

    void cancelEditRow( int row ) {
	// Check if row being canceled equals the row currently being edited
	if ( row != editRow ) return;

	// Check if row was modified
	if ( !rowModified ) return;

	// Initialize row modified status
	rowModified = false;

	// Store original record in list
	opusRecordList.set( row, originalOpusRecord );

	// Trigger update of table row data
	fireTableRowUpdated( row );
    }

    boolean saveEditRow( int row ) {
	String updateString = null;

	// Compare each field with the value in the original record
	// If modified, add entry to update query string

	String opusTitelString = opusRecord.opusTitelString;
	if ( ( opusTitelString == null ) &&
	     ( originalOpusRecord.opusTitelString != null ) ) {
	    updateString = "opus_titel = NULL ";
	} else if ( ( opusTitelString != null ) &&
		    ( !opusTitelString.equals( originalOpusRecord.opusTitelString ) ) ) {
	    // Matcher to find single quotes in opusTitelString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( opusTitelString );
	    updateString = "opus_titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	}

	String opusNummerString = opusRecord.opusNummerString;
	if ( ( opusNummerString == null ) &&
	     ( originalOpusRecord.opusNummerString != null ) ) {
	    updateString = "opus_nummer = NULL ";
	} else if ( ( opusNummerString != null ) &&
		    ( !opusNummerString.equals( originalOpusRecord.opusNummerString ) ) ) {
	    // Matcher to find single quotes in opusNummerString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( opusNummerString );
	    updateString = "opus_nummer = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'";
	}

	int genreId = opusRecord.genreId;
	if ( genreId != originalOpusRecord.genreId ) {
	    updateString = "genre_id = " + genreId;
	}

	int tijdperkId = opusRecord.tijdperkId;
	if ( tijdperkId != originalOpusRecord.tijdperkId ) {
	    updateString = "tijdperk_id = " + tijdperkId;
	}

	int typeId = opusRecord.typeId;
	if ( typeId != originalOpusRecord.typeId ) {
	    updateString = "type_id = " + typeId;
	}

	int subtypeId = opusRecord.subtypeId;
	if ( subtypeId != originalOpusRecord.subtypeId ) {
	    updateString = "subtype_id = " + subtypeId;
	}

	// Check if update is not necessary
	if ( updateString == null ) return true;

	updateString = "UPDATE opus SET " + updateString + " WHERE opus_id = " + originalOpusRecord.opusId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with opus_id " + originalOpusRecord.opusId );
	    	return false;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in update: " + sqlException.getMessage(),
                                           "OpusTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return false;
	}

	// Store record in list
	opusRecordList.set( row, opusRecord );

	// Initialize row modified status
	rowModified = false;

	// Trigger update of table row data
	fireTableRowUpdated( row );

	// Successful completion
	return true;
    }

    private void fireTableRowUpdated( int row ) {
	for ( int column = 0; column < getColumnCount( ); column++ ) {
	    fireTableCellUpdated( row, column );
	}
    }

    boolean getRowModified( ) { return rowModified; }

    private static boolean stringEquals(final String stringA, final String stringB) {
        // Two strings are equal if:
        // - both are null
        // - are the same object
        // - have the same string value
        return (stringA == stringB) || (stringA != null && stringB != null && stringA.equals(stringB));
    }
}
