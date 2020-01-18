// Class to setup a TableModel for records in medium

package muziek.medium;

import muziek.gui.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.swing.*;
import javax.swing.table.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;

class MediumTableModel extends AbstractTableModel {
    private final Logger logger = Logger.getLogger( MediumTableModel.class.getCanonicalName() );

    private final Connection connection;
    private final JFrame parentFrame;

    private final String[ ] headings = { "Id", "Medium", "Uitvoerenden",
                                         "Genre", "Subgenre", "Type", "Status", "Label", "Nummer",
                                         "Aankoopdatum", "Import", "Importdatum", "Opslag", "Opmerkingen" };

    private final SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MM-dd" );

    private class MediumRecord {
	int	mediumId;
	String	mediumTitelString;
	String	uitvoerendenString;
	int	genreId;
	String	genreString;
	int	subgenreId;
	String	subgenreString;
	int	mediumTypeId;
	String	mediumTypeString;
	int	mediumStatusId;
	String	mediumStatusString;
	int	labelId;
	String	labelString;
	String  labelNummerString;
	Date	aankoopDatumDate;
        int	importTypeId;
        String	importTypeString;
        Date	importDatumDate;
	int	opslagId;
	String	opslagString;
	String	opmerkingenString;

	MediumRecord( int     mediumId,
                      String  mediumTitelString,
                      String  uitvoerendenString,
                      int     genreId,
                      String  genreString,
                      int     subgenreId,
                      String  subgenreString,
                      int     mediumTypeId,
                      String  mediumTypeString,
                      int     mediumStatusId,
                      String  mediumStatusString,
                      int     labelId,
                      String  labelString,
                      String  labelNummerString,
                      Date    aankoopDatumDate,
                      int     importTypeId,
                      String  importTypeString,
                      Date    importDatumDate,
                      int     opslagId,
                      String  opslagString,
                      String  opmerkingenString ) {
	    this.mediumId = mediumId;
	    this.mediumTitelString = mediumTitelString;
	    this.uitvoerendenString = uitvoerendenString;
	    this.genreId = genreId;
	    this.genreString = genreString;
	    this.subgenreId = subgenreId;
	    this.subgenreString = subgenreString;
	    this.mediumTypeId = mediumTypeId;
	    this.mediumTypeString = mediumTypeString;
	    this.mediumStatusId = mediumStatusId;
	    this.mediumStatusString = mediumStatusString;
	    this.labelId = labelId;
	    this.labelString = labelString;
	    this.labelNummerString = labelNummerString;
	    this.aankoopDatumDate = aankoopDatumDate;
            this.importTypeId = importTypeId;
            this.importTypeString = importTypeString;
            this.importDatumDate = importDatumDate;
	    this.opslagId = opslagId;
	    this.opslagString = opslagString;
	    this.opmerkingenString = opmerkingenString;
	}

	// Copy constructor
	MediumRecord( MediumRecord mediumRecord ) {
	    this.mediumId = mediumRecord.mediumId;
	    this.mediumTitelString = mediumRecord.mediumTitelString;
	    this.uitvoerendenString = mediumRecord.uitvoerendenString;
	    this.genreId = mediumRecord.genreId;
	    this.genreString = mediumRecord.genreString;
	    this.subgenreId = mediumRecord.subgenreId;
	    this.subgenreString = mediumRecord.subgenreString;
	    this.mediumTypeId = mediumRecord.mediumTypeId;
	    this.mediumTypeString = mediumRecord.mediumTypeString;
	    this.mediumStatusId = mediumRecord.mediumStatusId;
	    this.mediumStatusString = mediumRecord.mediumStatusString;
	    this.labelId = mediumRecord.labelId;
	    this.labelString = mediumRecord.labelString;
	    this.labelNummerString = mediumRecord.labelNummerString;
	    this.aankoopDatumDate = mediumRecord.aankoopDatumDate;
            this.importTypeId = mediumRecord.importTypeId;
            this.importTypeString = mediumRecord.importTypeString;
            this.importDatumDate = mediumRecord.importDatumDate;
	    this.opslagId = mediumRecord.opslagId;
	    this.opslagString = mediumRecord.opslagString;
	    this.opmerkingenString = mediumRecord.opmerkingenString;
	}
    }

    private final ArrayList<MediumRecord> mediumRecordList = new ArrayList<>( 1000 );

    private GenreComboBox genreComboBox;
    private SubgenreComboBox subgenreComboBox;
    private MediumTypeComboBox mediumTypeComboBox;
    private MediumStatusComboBox mediumStatusComboBox;
    private LabelComboBox labelComboBox;
    private ImportTypeComboBox importTypeComboBox;
    private OpslagComboBox opslagComboBox;

    private JButton cancelRowEditButton;
    private JButton saveRowEditButton;

    private boolean	 rowModified = false;
    private int		 editRow = -1;
    private MediumRecord mediumRecord = null;
    private MediumRecord originalMediumRecord = null;

    // Pattern to find a single quote in the titel, to be replaced
    // with escaped quote (the double slashes are really necessary)
    private final Pattern quotePattern = Pattern.compile( "\\'" );

    // Constructor
    MediumTableModel( final Connection connection,
                      final JFrame     parentFrame,
                      final JButton    cancelRowEditButton,
                      final JButton    saveRowEditButton ) {
	this.connection = connection;
        this.parentFrame = parentFrame;
	this.cancelRowEditButton = cancelRowEditButton;
	this.saveRowEditButton = saveRowEditButton;

	// Create the combo boxes
	genreComboBox = new GenreComboBox( connection, 0 );
	subgenreComboBox = new SubgenreComboBox( connection, 0 );
	mediumTypeComboBox = new MediumTypeComboBox( connection, 0 );
	mediumStatusComboBox = new MediumStatusComboBox( connection, 0 );
	labelComboBox = new LabelComboBox( connection, null, false );
        importTypeComboBox = new ImportTypeComboBox( connection, 0 );
	opslagComboBox = new OpslagComboBox( connection, null, false );

	setupMediumTableModel( null, null, null, 0, 0, 0, 0, 0, 0, 0 );
    }

    void setupMediumTableModel( String mediumTitelFilterString,
                                String uitvoerendenFilterString,
                                String opmerkingenFilterString,
                                int    genreId,
                                int    subgenreId,
                                int    mediumTypeId,
                                int    mediumStatusId,
                                int    labelId,
                                int    importTypeId,
                                int    opslagId ) {
	// Setup the table
	try {
	    String mediumQueryString =
		"SELECT medium.medium_id, medium.medium_titel, medium.uitvoerenden, " +
		"medium.genre_id, genre.genre, medium.subgenre_id, subgenre.subgenre, " +
		"medium.medium_type_id, medium_type.medium_type, medium.medium_status_id, medium_status.medium_status, " +
		"medium.label_id, label.label, medium.label_nummer, " +
		"medium.aankoop_datum, medium.import_type_id, import_type.import_type, medium.import_datum, " +
                "medium.opslag_id, opslag.opslag, medium.opmerkingen " +
		"FROM medium " +
		"LEFT JOIN genre ON genre.genre_id = medium.genre_id " +
		"LEFT JOIN subgenre ON subgenre.subgenre_id = medium.subgenre_id " +
		"LEFT JOIN medium_type ON medium_type.medium_type_id = medium.medium_type_id " +
		"LEFT JOIN medium_status ON medium_status.medium_status_id = medium.medium_status_id " +
		"LEFT JOIN label ON label.label_id = medium.label_id " +
                "LEFT JOIN import_type ON import_type.import_type_id = medium.import_type_id " +
		"LEFT JOIN opslag ON opslag.opslag_id = medium.opslag_id ";

	    if ( ( ( mediumTitelFilterString != null ) && ( mediumTitelFilterString.length( ) > 0 ) ) ||
		 ( ( uitvoerendenFilterString != null ) && ( uitvoerendenFilterString.length( ) > 0 ) ) ||
		 ( ( opmerkingenFilterString != null ) && ( opmerkingenFilterString.length( ) > 0 ) ) ||
		 ( genreId != 0 ) ||
		 ( subgenreId != 0 ) ||
		 ( mediumTypeId != 0 ) ||
		 ( mediumStatusId != 0 ) ||
                    ( labelId != 0 ) ||
                    ( importTypeId != 0 ) ||
		 ( opslagId != 0 ) ) {
		mediumQueryString += "WHERE ";

		if ( ( mediumTitelFilterString != null ) && ( mediumTitelFilterString.length( ) > 0 ) ) {
		    // Matcher to find single quotes in mediumTitelFilterString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    Matcher quoteMatcher = quotePattern.matcher( mediumTitelFilterString );
		    mediumQueryString += "medium.medium_titel LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
		    if ( ( ( uitvoerendenFilterString != null ) && ( uitvoerendenFilterString.length( ) > 0 ) ) ||
	 	         ( ( opmerkingenFilterString != null ) && ( opmerkingenFilterString.length( ) > 0 ) ) ||
			 ( genreId != 0 ) ||
			 ( subgenreId != 0 ) ||
			 ( mediumTypeId != 0 ) ||
		         ( mediumStatusId != 0 ) ||
			 ( labelId != 0 ) ||
                            ( importTypeId != 0 ) ||
			 ( opslagId != 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

		if ( ( uitvoerendenFilterString != null ) && ( uitvoerendenFilterString.length( ) > 0 ) ) {
		    // Matcher to find single quotes in uitvoerendenFilterString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    Matcher quoteMatcher = quotePattern.matcher( uitvoerendenFilterString );
		    mediumQueryString += "medium.uitvoerenden LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
		    if ( ( ( opmerkingenFilterString != null ) && ( opmerkingenFilterString.length( ) > 0 ) ) ||
                         ( genreId != 0 ) ||
			 ( subgenreId != 0 ) ||
			 ( mediumTypeId != 0 ) ||
		         ( mediumStatusId != 0 ) ||
			 ( labelId != 0 ) ||
                            ( importTypeId != 0 ) ||
			 ( opslagId != 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

		if ( ( opmerkingenFilterString != null ) && ( opmerkingenFilterString.length( ) > 0 ) ) {
		    // Matcher to find single quotes in opmerkingenFilterString, in order to replace these
		    // with escaped quotes (the quadruple slashes are really necessary)
		    Matcher quoteMatcher = quotePattern.matcher( opmerkingenFilterString );
		    mediumQueryString += "medium.opmerkingen LIKE \"%" + quoteMatcher.replaceAll( "\\\\'" ) + "%\" ";
		    if ( ( genreId != 0 ) ||
			 ( subgenreId != 0 ) ||
			 ( mediumTypeId != 0 ) ||
		         ( mediumStatusId != 0 ) ||
			 ( labelId != 0 ) ||
                            ( importTypeId != 0 ) ||
			 ( opslagId != 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

		if ( genreId != 0 ) {
		    mediumQueryString += "medium.genre_id = " + genreId + " ";
		    if ( ( subgenreId != 0 ) ||
			 ( mediumTypeId != 0 ) ||
		         ( mediumStatusId != 0 ) ||
			 ( labelId != 0 ) ||
                            ( importTypeId != 0 ) ||
			 ( opslagId != 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

		if ( subgenreId != 0 ) {
		    mediumQueryString += "medium.subgenre_id = " + subgenreId + " ";
		    if ( ( mediumTypeId != 0 ) ||
		         ( mediumStatusId != 0 ) ||
			 ( labelId != 0 ) ||
                            ( importTypeId != 0 ) ||
			 ( opslagId != 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

		if ( mediumTypeId != 0 ) {
		    mediumQueryString += "medium.medium_type_id = " + mediumTypeId + " ";
		    if ( ( mediumStatusId != 0 ) ||
                         ( labelId != 0 ) ||
                            ( importTypeId != 0 ) ||
			 ( opslagId != 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

		if ( mediumStatusId != 0 ) {
		    mediumQueryString += "medium.medium_status_id = " + mediumStatusId + " ";
		    if ( ( labelId != 0 ) ||
                            ( importTypeId != 0 ) ||
			 ( opslagId != 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

		if ( labelId != 0 ) {
		    mediumQueryString += "medium.label_id = " + labelId + " ";
                    if ( ( importTypeId != 0 ) ||
                            ( opslagId != 0 ) ) {
			mediumQueryString += "AND ";
		    }
		}

                if ( importTypeId != 0 ) {
                    mediumQueryString += "medium.import_type_id = " + importTypeId + " ";
                    if ( opslagId != 0 ) {
                        mediumQueryString += "AND ";
                    }
                }

		if ( opslagId != 0 ) {
		    mediumQueryString += "medium.opslag_id = " + opslagId + " ";
		}
	    }

	    mediumQueryString += "ORDER BY opslag.opslag, medium.medium_titel, medium.subgenre_id, medium.aankoop_datum";

	    Statement statement = connection.createStatement( );
	    ResultSet resultSet = statement.executeQuery( mediumQueryString );

	    // Clear the list
	    mediumRecordList.clear( );

	    // Add all query results to the list
	    while ( resultSet.next( ) ) {
		try {
		    String aankoopDatumString = resultSet.getString( 15 );
		    Date aankoopDatumDate = null;
		    if ( ( aankoopDatumString != null ) && ( aankoopDatumString.length( ) > 0 ) ) {
			aankoopDatumDate = dateFormat.parse( aankoopDatumString );
		    }

                    String importDatumString = resultSet.getString( 18 );
                    Date importDatumDate = null;
                    if ( ( importDatumString != null ) && ( importDatumString.length( ) > 0 ) ) {
                        importDatumDate = dateFormat.parse( importDatumString );
                    }

		    mediumRecordList.add( new MediumRecord( resultSet.getInt( 1 ),
							    resultSet.getString( 2 ),
							    resultSet.getString( 3 ),
							    resultSet.getInt( 4 ),
							    resultSet.getString( 5 ),
							    resultSet.getInt( 6 ),
							    resultSet.getString( 7 ),
							    resultSet.getInt( 8 ),
							    resultSet.getString( 9 ),
							    resultSet.getInt( 10 ),
							    resultSet.getString( 11 ),
							    resultSet.getInt( 12 ),
							    resultSet.getString( 13 ),
							    resultSet.getString( 14 ),
							    aankoopDatumDate,
							    resultSet.getInt( 16 ),
                                                            resultSet.getString( 17 ),
                                                            importDatumDate,
                                                            resultSet.getInt( 19 ),
							    resultSet.getString( 20 ),
							    resultSet.getString( 21 ) ) );
		} catch( ParseException parseException ) {
		    logger.severe( "Datum parse exception: " + parseException.getMessage( ) );
		}
	    }

	    mediumRecordList.trimToSize( );

	    // Trigger update of table data
	    fireTableDataChanged( );
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in select: " + sqlException.getMessage(),
                                           "MediumTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	}
    }

    public int getRowCount( ) { return mediumRecordList.size( ); }

    public int getColumnCount( ) { return 14; }

    public String getColumnName( int column ) {
	return headings[ column ];
    }

    // Indicate the class for each column for setting the correct default renderer
    // see file:///home/cvengelen/java/tutorial/uiswing/components/table.html
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
	case 0:		// medium id
	    // Do not allow editing
	    return false;
	}

	return true;
    }

    public Object getValueAt( int row, int column ) {
	if ( ( row < 0 ) || ( row >= mediumRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	final MediumRecord mediumRecord = mediumRecordList.get( row );

	if ( column == 0 ) return mediumRecord.mediumId;
	if ( column == 1 ) return mediumRecord.mediumTitelString;
	if ( column == 2 ) return mediumRecord.uitvoerendenString;
	if ( column == 3 ) return mediumRecord.genreString;
	if ( column == 4 ) return mediumRecord.subgenreString;
	if ( column == 5 ) return mediumRecord.mediumTypeString;
	if ( column == 6 ) return mediumRecord.mediumStatusString;
	if ( column == 7 ) return mediumRecord.labelString;
	if ( column == 8 ) return mediumRecord.labelNummerString;
	if ( column == 9 ) {
	    // Check if aankoopDatumDate is present
	    if ( mediumRecord.aankoopDatumDate == null ) return "";
	    // Convert the Date object to a string
	    return dateFormat.format( mediumRecord.aankoopDatumDate );
	}
        if ( column == 10 ) return mediumRecord.importTypeString;
        if ( column == 11 ) {
            // Check if importDatumDate is present
            if ( mediumRecord.importDatumDate == null ) return "";
            // Convert the Date object to a string
            return dateFormat.format( mediumRecord.importDatumDate );
        }
	if ( column == 12 ) return mediumRecord.opslagString;
	if ( column == 13 ) return mediumRecord.opmerkingenString;

	return "";
    }

    public void setValueAt( Object object, int row, int column ) {
	if ( ( row < 0 ) || ( row >= mediumRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return;
	}

	try {
	    switch ( column ) {
	    case 1:
		String mediumTitelString = ( String )object;
		if ( ( ( mediumTitelString == null ) || ( mediumTitelString.length( ) == 0 ) ) &&
		     ( mediumRecord.mediumTitelString != null ) ) {
		    mediumRecord.mediumTitelString = null;
		    rowModified = true;
		} else if ( ( mediumTitelString != null ) &&
			    ( !mediumTitelString.equals( mediumRecord.mediumTitelString ) ) ) {
		    mediumRecord.mediumTitelString = mediumTitelString;
		    rowModified = true;
		}
		break;

	    case 2:
		String uitvoerendenString = ( String )object;
		if ( ( ( uitvoerendenString == null ) || ( uitvoerendenString.length( ) == 0 ) ) &&
		     ( mediumRecord.uitvoerendenString != null ) ) {
		    mediumRecord.uitvoerendenString = null;
		    rowModified = true;
		} else if ( ( uitvoerendenString != null ) &&
			    ( !uitvoerendenString.equals( mediumRecord.uitvoerendenString ) ) ) {
		    mediumRecord.uitvoerendenString = uitvoerendenString;
		    rowModified = true;
		}
		break;

	    case 3:
		int genreId = genreComboBox.getGenreId( ( String )object );
		if ( genreId != mediumRecord.genreId ) {
		    mediumRecord.genreId = genreId;
		    mediumRecord.genreString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 4:
		int subgenreId = subgenreComboBox.getSubgenreId( ( String )object );
		if ( subgenreId != mediumRecord.subgenreId ) {
		    mediumRecord.subgenreId = subgenreId;
		    mediumRecord.subgenreString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 5:
		int mediumTypeId = mediumTypeComboBox.getMediumTypeId( ( String )object );
		if ( mediumTypeId != mediumRecord.mediumTypeId ) {
		    mediumRecord.mediumTypeId = mediumTypeId;
		    mediumRecord.mediumTypeString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 6:
		int mediumStatusId = mediumStatusComboBox.getMediumStatusId( ( String )object );
		if ( mediumStatusId != mediumRecord.mediumStatusId ) {
		    mediumRecord.mediumStatusId = mediumStatusId;
		    mediumRecord.mediumStatusString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 7:
		int labelId = labelComboBox.getLabelId( ( String )object );
		if ( labelId != mediumRecord.labelId ) {
		    mediumRecord.labelId = labelId;
		    mediumRecord.labelString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 8:
		String labelNummerString = ( String )object;
		if ( ( ( labelNummerString == null ) || ( labelNummerString.length( ) == 0 ) ) &&
		     ( mediumRecord.mediumTitelString != null ) ) {
		    mediumRecord.mediumTitelString = null;
		    rowModified = true;
		} else if ( ( labelNummerString != null ) &&
			    ( !( labelNummerString.equals( mediumRecord.labelNummerString ) ) ) ) {
		    mediumRecord.labelNummerString = labelNummerString;
		    rowModified = true;
		}
		break;

	    case 9:
		String aankoopDatumString = ( String )object;

		// Check if string in table is null or empty, and date in record is not null
		if ( ( ( aankoopDatumString == null ) || ( aankoopDatumString.length( ) == 0 ) ) &&
		     ( mediumRecord.aankoopDatumDate != null ) ) {
		    // Set the aankoop datum to null in the record
		    mediumRecord.aankoopDatumDate = null;
		    rowModified = true;
		} else {
		    // Convert the string to a Date object
		    try {
			final Date aankoopDatumDate = dateFormat.parse( aankoopDatumString );
			logger.fine( "Date: " + aankoopDatumDate );

			// Check if aankoopDatumDate is valid, and unequal to aankoopDatumDate in record
			if ( ( aankoopDatumDate != null ) &&
			     ( !aankoopDatumDate.equals( mediumRecord.aankoopDatumDate ) ) ) {
			    // Modified aankoopDatumDate: update record
			    mediumRecord.aankoopDatumDate = aankoopDatumDate;
			    rowModified = true;
			}
		    } catch( ParseException parseException ) {
			logger.severe( "Datum parse exception: " + parseException.getMessage( ) );
		    }
		}
		break;

            case 10:
                int importTypeId = importTypeComboBox.getImportTypeId( ( String )object );
                if ( importTypeId != mediumRecord.importTypeId ) {
                    mediumRecord.importTypeId = importTypeId;
                    mediumRecord.importTypeString = ( String )object;
                    rowModified = true;
                }
                break;

            case 11:
                String importDatumString = ( String )object;

                // Check if string in table is null or empty, and date in record is not null
                if ( ( ( importDatumString == null ) || ( importDatumString.length( ) == 0 ) ) &&
                        ( mediumRecord.importDatumDate != null ) ) {
                    // Set the import datum to null in the record
                    mediumRecord.importDatumDate = null;
                    rowModified = true;
                } else {
                    // Convert the string to a Date object
                    try {
                        final Date importDatumDate = dateFormat.parse( importDatumString );
                        logger.fine( "Date: " + importDatumDate );

                        // Check if importDatumDate is valid, and unequal to importDatumDate in record
                        if ( ( importDatumDate != null ) &&
                                ( !importDatumDate.equals( mediumRecord.importDatumDate ) ) ) {
                            // Modified importDatumDate: update record
                            mediumRecord.importDatumDate = importDatumDate;
                            rowModified = true;
                        }
                    } catch( ParseException parseException ) {
                        logger.severe( "Datum parse exception: " + parseException.getMessage( ) );
                    }
                }
                break;

            case 12:
		int opslagId = opslagComboBox.getOpslagId( ( String )object );
		if ( opslagId != mediumRecord.opslagId ) {
		    mediumRecord.opslagId = opslagId;
		    mediumRecord.opslagString = ( String )object;
		    rowModified = true;
		}
		break;

	    case 13:
		String opmerkingenString = ( String )object;
		if ( ( ( opmerkingenString == null ) || ( opmerkingenString.length( ) == 0 ) ) &&
		     ( mediumRecord.opmerkingenString != null ) ) {
		    mediumRecord.opmerkingenString = null;
		    rowModified = true;
		} else if ( ( opmerkingenString != null ) &&
			    ( !opmerkingenString.equals( mediumRecord.opmerkingenString ) ) ) {
		    mediumRecord.opmerkingenString = opmerkingenString;
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
	mediumRecordList.set( row, mediumRecord );

	if ( rowModified ) {
	    // Enable cancel and save buttons
	    cancelRowEditButton.setEnabled( true );
	    saveRowEditButton.setEnabled( true );
	}

	fireTableCellUpdated( row, column );
    }

    int getMediumId( int row ) {
        if ( ( row < 0 ) || ( row >= mediumRecordList.size( ) ) ) {
            logger.severe( "Invalid row: " + row );
            return 0;
        }

	return mediumRecordList.get( row ).mediumId;
    }

    String getMediumTitelString( int row ) {
	if ( ( row < 0 ) || ( row >= mediumRecordList.size( ) ) ) {
	    logger.severe( "Invalid row: " + row );
	    return null;
	}

	return mediumRecordList.get( row ).mediumTitelString;
    }

    void setEditRow( int editRow ) {
	// Initialize record to be edited
	mediumRecord = mediumRecordList.get( editRow );

	// Copy record to use as key in table update
	originalMediumRecord = new MediumRecord( mediumRecord );

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
	mediumRecordList.set( row, originalMediumRecord );

	// Trigger update of table row data
	fireTableRowUpdated( row );
    }

    private String addToUpdateString( String updateString, String additionalUpdateString ) {
	if ( updateString != null ) {
	    return updateString + ", " + additionalUpdateString;
	}
	return additionalUpdateString;
    }

    boolean saveEditRow( int row ) {
	String updateString = null;

	// Compare each field with the value in the original record
	// If modified, add entry to update query string

	String mediumTitelString = mediumRecord.mediumTitelString;
	if ( ( mediumTitelString == null ) && ( originalMediumRecord.mediumTitelString != null ) ) {
            updateString = addToUpdateString( updateString, "medium_titel = NULL " );
	} else if ( ( mediumTitelString != null ) &&
		    ( !mediumTitelString.equals( originalMediumRecord.mediumTitelString ) ) ) {
	    // Matcher to find single quotes in mediumTitelString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( mediumTitelString );
	    updateString = addToUpdateString( updateString, "medium_titel = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	String uitvoerendenString = mediumRecord.uitvoerendenString;
	if ( ( uitvoerendenString == null ) && ( originalMediumRecord.uitvoerendenString != null ) ) {
	    updateString = addToUpdateString( updateString, "uitvoerenden = NULL " );
	} else if ( ( uitvoerendenString != null ) &&
		    ( !uitvoerendenString.equals( originalMediumRecord.uitvoerendenString ) ) ) {
	    // Matcher to find single quotes in uitvoerendenString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( uitvoerendenString );
	    updateString = addToUpdateString( updateString, "uitvoerenden = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	int genreId = mediumRecord.genreId;
	if ( genreId != originalMediumRecord.genreId ) {
	    updateString = addToUpdateString( updateString, "genre_id = " + genreId );
	}

	int subgenreId = mediumRecord.subgenreId;
	if ( subgenreId != originalMediumRecord.subgenreId ) {
	    updateString = addToUpdateString( updateString, "subgenre_id = " + subgenreId );
	}

	int mediumTypeId = mediumRecord.mediumTypeId;
	if ( mediumTypeId != originalMediumRecord.mediumTypeId ) {
	    updateString = addToUpdateString( updateString, "medium_type_id = " + mediumTypeId );
	}

	int mediumStatusId = mediumRecord.mediumStatusId;
	if ( mediumStatusId != originalMediumRecord.mediumStatusId ) {
	    updateString = addToUpdateString( updateString, "medium_status_id = " + mediumStatusId );
	}

	int labelId = mediumRecord.labelId;
	if ( labelId != originalMediumRecord.labelId ) {
	    updateString = addToUpdateString( updateString, "label_id = " + labelId );
	}

	String labelNummerString = mediumRecord.labelNummerString;
	if ( ( labelNummerString == null ) && ( originalMediumRecord. labelNummerString != null ) ) {
            updateString = addToUpdateString( updateString, "label_nummer = NULL " );
	} else if ( ( labelNummerString != null ) &&
		    ( !( labelNummerString.equals( originalMediumRecord. labelNummerString ) ) ) ) {
	    // Matcher to find single quotes in labelNummerString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( labelNummerString );
	    updateString = addToUpdateString( updateString, "label_nummer = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	Date aankoopDatumDate = mediumRecord.aankoopDatumDate;
	if ( ( aankoopDatumDate == null ) && ( originalMediumRecord.aankoopDatumDate != null ) ) {
	    updateString = addToUpdateString( updateString, "aankoop_datum = NULL " );
	} else if ( ( aankoopDatumDate != null ) &&
		    ( !aankoopDatumDate.equals( originalMediumRecord.aankoopDatumDate ) ) ) {
	    // Convert the date object to a string
	    String aankoopDatumString  = dateFormat.format( mediumRecord.aankoopDatumDate );
	    updateString = addToUpdateString( updateString, "aankoop_datum = '" + aankoopDatumString + "'" );
	}

	int opslagId = mediumRecord.opslagId;
	if ( opslagId != originalMediumRecord.opslagId ) {
	    updateString = addToUpdateString( updateString, "opslag_id = " + opslagId );
	}

	String opmerkingenString = mediumRecord.opmerkingenString;
	if ( ( opmerkingenString == null ) && ( originalMediumRecord.opmerkingenString != null ) ) {
	    updateString = addToUpdateString( updateString, "opmerkingen = NULL " );
	} else if ( ( opmerkingenString != null ) &&
		    ( !opmerkingenString.equals( originalMediumRecord.opmerkingenString ) ) ) {
	    // Matcher to find single quotes in opmerkingenString, in order to replace these
	    // with escaped quotes (the quadruple slashes are really necessary)
	    final Matcher quoteMatcher = quotePattern.matcher( opmerkingenString );
	    updateString = addToUpdateString( updateString, "opmerkingen = '" + quoteMatcher.replaceAll( "\\\\'" ) + "'" );
	}

	// Check if update is not necessary
	if ( updateString == null ) {
	   rowModified = false;
	   return true;
	}

	updateString = "UPDATE medium SET " + updateString + " WHERE medium_id = " + originalMediumRecord.mediumId;
	logger.fine( "updateString: " + updateString );

	try {
	    Statement statement = connection.createStatement( );
	    int nUpdate = statement.executeUpdate( updateString );
	    if ( nUpdate != 1 ) {
	    	logger.severe( "Could not update record with medium_id " + originalMediumRecord.mediumId );
	    	return false;
	    }
	} catch ( SQLException sqlException ) {
            JOptionPane.showMessageDialog( parentFrame,
                                           "SQL exception in update: " + sqlException.getMessage(),
                                           "MediumTableModel SQL exception",
                                           JOptionPane.ERROR_MESSAGE );
	    logger.severe( "SQLException: " + sqlException.getMessage( ) );
	    return false;
	}

	// Store record in list
	mediumRecordList.set( row, mediumRecord );

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
}
