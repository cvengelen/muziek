//
// Project:	muziek
// Component:	gui
// File:	OpnameTableModel.java
// Description:	TableModel for records in opname
// Author:	Chris van Engelen
// History:	2006/02/19: Initial version
//              2011/08/13: Add selection on medium status
//

package muziek.opname;

import muziek.gui.OpnameKey;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.table.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;



public class OpnameTableModel extends AbstractTableModel {
    final Logger logger = Logger.getLogger( "muziek.opname.OpnameTableModel" );

    private Connection connection;
    private String[ ] headings = { "Medium", "Opus", "Componisten", "Genre", "Type",
				   "Musici", "Opname datum", "Opname plaats", "Producers" };

    class OpnameRecord {
	String	mediumString;
	String	opusString;
        String  componistString;
	String	componistenString;
	String	genreString;
	String	typeString;
	String	musiciString;
	String	opnameDatumString;
	String	opnamePlaatsString;
	String	producersString;
	int	mediumId;
	int	opusId;
	int	opnameNummer;
	int	musiciId;

	public OpnameRecord( String mediumString,
			     String opusString,
                             String componistString,
			     String componistenString,
			     String genreString,
			     String typeString,
			     String musiciString,
			     String opnameDatumString,
			     String opnamePlaatsString,
			     String producersString,
			     int    mediumId,
			     int    opusId,
			     int    opnameNummer,
			     int    musiciId ) {
	    this.mediumString = mediumString;
	    this.opusString = opusString;
            this.componistString = componistString;
	    this.componistenString = componistenString;
	    this.genreString = genreString;
	    this.typeString = typeString;
	    this.musiciString = musiciString;
	    this.opnameDatumString = opnameDatumString;
	    this.opnamePlaatsString = opnamePlaatsString;
	    this.producersString = producersString;
	    this.mediumId = mediumId;
	    this.opusId = opusId;
	    this.opnameNummer = opnameNummer;
	    this.musiciId = musiciId;
	}

	// Copy constructor
	public OpnameRecord( OpnameRecord opnameRecord ) {
	    this.mediumString = opnameRecord.mediumString;
	    this.opusString = opnameRecord.opusString;
            this.componistString = opnameRecord.componistString;
	    this.componistenString = opnameRecord.componistenString;
	    this.genreString = opnameRecord.genreString;
	    this.typeString = opnameRecord.typeString;
	    this.musiciString = opnameRecord.musiciString;
	    this.opnameDatumString = opnameRecord.opnameDatumString;
	    this.opnamePlaatsString = opnameRecord.opnamePlaatsString;
	    this.producersString = opnameRecord.producersString;
	    this.mediumId = opnameRecord.mediumId;
	    this.opusId = opnameRecord.opusId;
	    this.opnameNummer = opnameRecord.opnameNummer;
	    this.musiciId = opnameRecord.musiciId;
 	}

        // Check if two opnameRecords are equal, except for the componist string
        // so that that two opnameRecords which differ only in componist will be regarded as the same.
        @Override
        public boolean equals(Object object) {
            if (object == null) return false;
            if (!(object instanceof OpnameRecord)) return false;
            final OpnameRecord opnameRecord = (OpnameRecord)object;

            // No check for componistString!
            if (!OpnameTableModel.stringEquals(mediumString, opnameRecord.mediumString)) return false;
            if (!OpnameTableModel.stringEquals(opusString, opnameRecord.opusString)) return false;
            if (!OpnameTableModel.stringEquals(componistenString, opnameRecord.componistenString)) return false;
            if (!OpnameTableModel.stringEquals(genreString, opnameRecord.genreString)) return false;
            if (!OpnameTableModel.stringEquals(typeString, opnameRecord.typeString)) return false;
            if (!OpnameTableModel.stringEquals(musiciString, opnameRecord.musiciString)) return false;
            if (!OpnameTableModel.stringEquals(opnameDatumString, opnameRecord.opnameDatumString)) return false;
            if (!OpnameTableModel.stringEquals(opnamePlaatsString, opnameRecord.opnamePlaatsString)) return false;
            if (!OpnameTableModel.stringEquals(producersString, opnameRecord.producersString)) return false;
            if (mediumId != opnameRecord.mediumId) return false;
            if (opusId != opnameRecord.opusId) return false;
            if (opnameNummer != opnameRecord.opnameNummer) return false;
            if (musiciId != opnameRecord.musiciId) return false;

            return true;
        }
    }

    ArrayList<OpnameRecord> opnameRecordList = new ArrayList<OpnameRecord>( 500 );

    private int mediumId = 0;
    private int mediumStatusId = 0;
    private String opusFilterString = null;
    private int componistenPersoonId = 0;
    private int componistenId = 0;
    private int genreId = 0;
    private int typeId = 0;
    private int persoonAllMusiciId = 0;
    private int musiciId = 0;
    private int musiciEnsembleId = 0;
    private int opnameDatumId = 0;
    private int opnamePlaatsId = 0;
    private int producersId = 0;

    private final static Pattern componistPattern = Pattern.compile( "(.+?), (.*)" );

    // Constructor
    public OpnameTableModel( Connection connection ) {
	this.connection = connection;

	setupOpnameTableModel( mediumId, mediumStatusId, null,
			       componistenPersoonId, componistenId, genreId, typeId,
			       persoonAllMusiciId, musiciId, musiciEnsembleId,
			       opnameDatumId, opnamePlaatsId, producersId );
    }

    public void setupOpnameTableModel( int    mediumId,
                                       int    mediumStatusId,
				       String opusFilterString,
				       int    componistenPersoonId,
				       int    componistenId,
				       int    genreId,
				       int    typeId,
				       int    persoonAllMusiciId,
				       int    musiciId,
				       int    musiciEnsembleId,
				       int    opnameDatumId,
				       int    opnamePlaatsId,
				       int    producersId ) {
	this.mediumId = mediumId;
	this.mediumStatusId = mediumStatusId;
	this.opusFilterString = opusFilterString;
	this.componistenId = componistenId;
	this.genreId = genreId;
	this.typeId = typeId;
	this.persoonAllMusiciId = persoonAllMusiciId;
	this.musiciId = musiciId;
	this.musiciEnsembleId = musiciEnsembleId;
	this.opnameDatumId = opnameDatumId;
	this.opnamePlaatsId = opnamePlaatsId;
	this.producersId = producersId;

	// Setup the table
	try {
	    String opnameQueryString =
		"SELECT DISTINCT medium.medium_titel, opus.opus_titel, opus.opus_nummer, " +
		"componist.persoon, componisten.componisten, " +
		"genre.genre, type.type, " +
		"musici.musici, " +
		"opname_datum.opname_datum, opname_plaats.opname_plaats, producers.producers, " +
		"medium.medium_id, opus.opus_id, opname.opname_nummer, opname.musici_id " +
		"FROM opname " +
		"LEFT JOIN medium ON medium.medium_id = opname.medium_id " +
		"LEFT JOIN opus ON opus.opus_id = opname.opus_id " +
		"LEFT JOIN componisten ON componisten.componisten_id = opus.componisten_id " +
		"LEFT JOIN componisten_persoon ON componisten_persoon.componisten_id = opus.componisten_id " +
		"LEFT JOIN persoon AS componist ON componist.persoon_id = componisten_persoon.persoon_id " +
		"LEFT JOIN genre ON genre.genre_id = opus.genre_id " +
		"LEFT JOIN type ON type.type_id = opus.type_id " +
		"LEFT JOIN musici ON musici.musici_id = opname.musici_id " +
		"LEFT JOIN musici_persoon ON musici_persoon.musici_id = musici.musici_id " +
		"LEFT JOIN musici_ensemble ON musici_ensemble.musici_id = musici.musici_id " +
		"LEFT JOIN opname_datum ON opname_datum.opname_datum_id = opname.opname_datum_id " +
		"LEFT JOIN opname_plaats ON opname_plaats.opname_plaats_id = opname.opname_plaats_id " +
		"LEFT JOIN producers ON producers.producers_id = opname.producers_id ";

	    if ( ( mediumId != 0 ) ||
	         ( mediumStatusId != 0 ) ||
		 ( ( opusFilterString != null ) && ( opusFilterString.length( ) > 0 ) ) ||
		 ( componistenPersoonId != 0 ) ||
		 ( componistenId != 0 ) ||
		 ( genreId != 0 ) ||
		 ( typeId != 0 ) ||
		 ( persoonAllMusiciId != 0 ) ||
		 ( musiciId != 0 ) ||
		 ( musiciEnsembleId != 0 ) ||
		 ( opnameDatumId != 0 ) ||
		 ( opnamePlaatsId != 0 ) ||
		 ( producersId != 0 ) ) {
		opnameQueryString += "WHERE ";

		if ( mediumId != 0 ) {
		    opnameQueryString += "medium.medium_id = " + mediumId + " ";
		    if ( ( mediumStatusId != 0 ) ||
		         ( ( opusFilterString != null ) && ( opusFilterString.length( ) > 0 ) ) ||
			 ( componistenPersoonId != 0 ) ||
			 ( componistenId != 0 ) ||
			 ( genreId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( persoonAllMusiciId != 0 ) ||
			 ( musiciId != 0 ) ||
			 ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( mediumStatusId != 0 ) {
		    opnameQueryString += "medium.medium_status_id = " + mediumStatusId + " ";
		    if ( ( ( opusFilterString != null ) && ( opusFilterString.length( ) > 0 ) ) ||
			 ( componistenPersoonId != 0 ) ||
			 ( componistenId != 0 ) ||
			 ( genreId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( persoonAllMusiciId != 0 ) ||
			 ( musiciId != 0 ) ||
			 ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( ( opusFilterString != null ) && ( opusFilterString.length( ) > 0 ) ) {
		    opnameQueryString += "( opus.opus_titel LIKE \"%" + opusFilterString + "%\" OR ";
		    opnameQueryString += "opus.opus_nummer LIKE \"%" + opusFilterString + "%\" ) ";
		    if ( ( componistenPersoonId != 0 ) ||
			 ( componistenId != 0 ) ||
			 ( genreId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( persoonAllMusiciId != 0 ) ||
			 ( musiciId != 0 ) ||
			 ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( componistenPersoonId != 0 ) {
		    opnameQueryString += "componist.persoon_id = " + componistenPersoonId + " ";
		    if ( ( componistenId != 0 ) ||
			 ( genreId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( persoonAllMusiciId != 0 ) ||
			 ( musiciId != 0 ) ||
			 ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( componistenId != 0 ) {
		    opnameQueryString += "opus.componisten_id = " + componistenId + " ";
		    if ( ( genreId != 0 ) ||
			 ( typeId != 0 ) ||
			 ( persoonAllMusiciId != 0 ) ||
			 ( musiciId != 0 ) ||
			 ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( genreId != 0 ) {
		    opnameQueryString += "opus.genre_id = " + genreId + " ";
		    if ( ( typeId != 0 ) ||
			 ( persoonAllMusiciId != 0 ) ||
			 ( musiciId != 0 ) ||
			 ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( typeId != 0 ) {
		    opnameQueryString += "opus.type_id = " + typeId + " ";
		    if ( ( persoonAllMusiciId != 0 ) ||
			 ( musiciId != 0 ) ||
			 ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( persoonAllMusiciId != 0 ) {
		    opnameQueryString += "musici_persoon.persoon_id = " + persoonAllMusiciId + " ";
		    if ( ( musiciId != 0 ) ||
			 ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( musiciId != 0 ) {
		    opnameQueryString += "musici.musici_id = " + musiciId + " ";
		    if ( ( musiciEnsembleId != 0 ) ||
			 ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( musiciEnsembleId != 0 ) {
		    opnameQueryString += "musici_ensemble.ensemble_id = " + musiciEnsembleId + " ";
		    if ( ( opnameDatumId != 0 ) ||
			 ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( opnameDatumId != 0 ) {
		    opnameQueryString += "opname.opname_datum_id = " + opnameDatumId + " ";
		    if ( ( opnamePlaatsId != 0 ) ||
			 ( producersId != 0 ) ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( opnamePlaatsId != 0 ) {
		    opnameQueryString += "opname.opname_plaats_id = " + opnamePlaatsId + " ";
		    if ( producersId != 0 ) {
			opnameQueryString += "AND ";
		    }
		}

		if ( producersId != 0 ) {
		    opnameQueryString += "opname.producers_id = " + producersId + " ";
		}
	    }

	    opnameQueryString += "ORDER BY genre.genre, componist.persoon, type.type, ";
	    opnameQueryString += "opus.opus_nummer, opus.opus_titel";
            // logger.info( "opnameQueryString: "  + opnameQueryString );

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opnameQueryString );

	    // Clear the list
	    opnameRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		// Get the opus title and opus number, if present
		String opusString = resultSet.getString( 2 );
		String opusNummerString = resultSet.getString( 3 );
		if ( ( opusNummerString != null ) && ( opusNummerString.length( ) > 0 ) ) {
		    opusString += ", " + opusNummerString;
		}

                /*
		String componistenString = resultSet.getString( 4 );
		if ( ( componistenString != null ) &&
		     !componistenString.equals( resultSet.getString( 5 ) ) ) {
		    // Set componistenString to "Composer (composer-group)"
		    componistenString += " (" + resultSet.getString( 5 ) + ")";
		}
		*/

		final OpnameRecord opnameRecord= ( new OpnameRecord( resultSet.getString( 1 ),
							             opusString,
                                                                     resultSet.getString( 4 ),
                                                                     resultSet.getString( 5 ),
							             resultSet.getString( 6 ),
							             resultSet.getString( 7 ),
							             resultSet.getString( 8 ),
							             resultSet.getString( 9 ),
							             resultSet.getString( 10 ),
							             resultSet.getString( 11 ),
							             resultSet.getInt( 12 ),
							             resultSet.getInt( 13 ),
							             resultSet.getInt( 14 ),
							             resultSet.getInt( 15 ) ) );
                if (!opnameRecordList.contains(opnameRecord)) {
                    opnameRecordList.add(opnameRecord);
                }
	    }

	    opnameRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return opnameRecordList.size( ); }

    public int getColumnCount( ) { return 9; }

    // Indicate the class for each column for setting the correct default renderer
    // see file:///home/cvengelen/java/tutorial/uiswing/components/table.html
    public Class getColumnClass( int column ) {
	return String.class;
    }

    public boolean isCellEditable( int row, int column ) {
	// Do not allow editing
	return false;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= opnameRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final OpnameRecord opnameRecord = opnameRecordList.get( row );

        switch ( column ) {
        case 0:
            return opnameRecord.mediumString;
        case 1:
            return opnameRecord.opusString;
        case 2:
            // Use the componistString, and add the componistenString if it is different from the componistString.
            // First check the componistString for null (should normally not be the case).
            String componistenString = opnameRecord.componistString;
            if ( componistenString == null ) {
                // Use the componistenString if set
                if ( opnameRecord.componistenString != null ) {
                    componistenString = opnameRecord.componistenString;
                } else {
                    componistenString = "-";
                }
            } else {
                String componistFullName = null;
                Matcher componistMatcher = componistPattern.matcher( componistenString );
                if ( componistMatcher.matches() ) {
                    componistFullName = componistMatcher.group( 2 ) + " " + componistMatcher.group( 1 );
                }
                if ( ( opnameRecord.componistenString != null ) &&
                        !componistenString.equals( opnameRecord.componistenString ) &&
                        ( componistFullName == null || !componistFullName.equals( opnameRecord.componistenString ) ) ) {
                    componistenString += " (" + opnameRecord.componistenString + ")";
                }
            }
            return componistenString;
        case 3:
            return opnameRecord.genreString;
        case 4:
            return opnameRecord.typeString;
        case 5:
            return opnameRecord.musiciString;
        case 6:
            return opnameRecord.opnameDatumString;
        case 7:
            return opnameRecord.opnamePlaatsString;
        case 8:
            return opnameRecord.producersString;
        default:
            break;
        }

	return "";
    }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    public int getNumberOfRecords( ) { return opnameRecordList.size( ); }

    public OpnameKey getSelectedOpnameKey( int row ) {
	final OpnameKey nullOpnameKey = new OpnameKey( );

	final OpnameRecord opnameRecord = opnameRecordList.get( row );

	return new OpnameKey( opnameRecord.mediumId,
			      opnameRecord.opusId,
			      opnameRecord.opnameNummer,
			      opnameRecord.musiciId );
    }

    private static boolean stringEquals(final String stringA, final String stringB) {
        // Two strings are equal if:
        // - both are null
        // - are the same object
        // - have the same string value
        return (stringA == stringB) || (stringA != null && stringB != null && stringA.equals(stringB));
    }
}
