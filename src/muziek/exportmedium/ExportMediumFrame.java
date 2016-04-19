// frame to export medium selected on genre, sorted on medium titel
// frame to select a genre

package muziek.exportmedium;

import muziek.gui.GenreComboBox;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;


public class ExportMediumFrame {
    final Connection conn;
    final JFrame frame = new JFrame( "Export Medium" );

    private GenreComboBox genreComboBox;

    public ExportMediumFrame( final Connection conn ) {
	this.conn = conn;

	// put the controls the content pane
	Container container = frame.getContentPane( );

	// Set grid bag layout manager
	container.setLayout( new GridBagLayout( ) );

	GridBagConstraints constraints = new GridBagConstraints( );
	constraints.anchor = GridBagConstraints.WEST;
	constraints.insets = new Insets( 5, 5, 5, 5 );

	constraints.gridx = 0;
	constraints.gridy = 0;
	container.add( new JLabel( "Genre:" ), constraints );

	// Setup a JComboBox with the results of the query on genre
	genreComboBox = new GenreComboBox( conn, 0 );
	constraints.gridx = GridBagConstraints.RELATIVE;
	container.add( genreComboBox, constraints );

	////////////////////////////////////////////////
	// OK, Cancel Buttons
	////////////////////////////////////////////////

	class TextFileFilter extends FileFilter {
	    public boolean accept( File file ) {

		//Accept directories, and text files only
		if ( file.isDirectory( ) ) {
		    return true;
		}

		String fileNameString = file.getName( );
		int dotIndex = fileNameString.lastIndexOf('.');

		if ( ( dotIndex > 0 ) && ( dotIndex < ( fileNameString.length( ) - 1 ) ) ) {
		    String extString = fileNameString.substring( dotIndex + 1 ).toLowerCase( );
		    if ( extString.equals( "txt" ) ) return true;
		}

		return false;
	    }

	    public String getDescription() {
		return "Text files";
	    }
	}

	class EditOpusActionListener implements ActionListener {
	    public void actionPerformed( ActionEvent ae ) {
		if ( ae.getActionCommand( ).equals( "OK" ) ) {
		    int selectedGenreId = genreComboBox.getSelectedGenreId( );

		    // Check if genre has been selected
		    if ( selectedGenreId == 0 ) {
			JOptionPane.showMessageDialog( frame,
						       "Geen Genre geselecteerd",
						       "Export Medium error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    final JFileChooser exportFileChooser = new JFileChooser( "/home/cvengelen/java/muziek" );
		    exportFileChooser.setFileFilter( new TextFileFilter( ) );

		    int returnVal = exportFileChooser.showDialog( frame, "Export Medium" );
		    if ( returnVal == JFileChooser.APPROVE_OPTION ) {
			File exportFile = exportFileChooser.getSelectedFile( );
			FileWriter exportFileWriter = null;

			try {
			    exportFileWriter = new FileWriter( exportFile );
			} catch ( IOException ioException ) {
			    System.err.println( "ExportMediumFrame IOException:\n\t" +
						ioException.getMessage( ) );
			}

			PrintWriter exportPrintWriter = new PrintWriter( exportFileWriter );

			// Export the medium records for the selected genre to the selected file
			exportMedium( selectedGenreId, exportPrintWriter );

			try {
			    exportFileWriter.close( );
			} catch ( IOException ioException ) {
			    System.err.println( "ExportMediumFrame IOException:\n\t" +
						ioException.getMessage( ) );
			}
		    }
		}

		frame.setVisible( false );
		System.exit( 0 );
	    }
	}

	JButton okButton = new JButton( "OK" );
	okButton.setActionCommand( "OK" );
	okButton.addActionListener( new EditOpusActionListener( ) );

	JButton cancelButton = new JButton( "Cancel" );
	cancelButton.setActionCommand( "cancel" );
	cancelButton.addActionListener( new EditOpusActionListener( ) );

	JPanel buttonPanel = new JPanel( );
	buttonPanel.add( okButton );
	buttonPanel.add( cancelButton );
	constraints.gridx = 1;
	constraints.gridy = 12;
	constraints.gridwidth = 2;
	container.add( buttonPanel, constraints );

	frame.setSize( 300, 120 );
	frame.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
	frame.setVisible( true );
    }

    private void exportMedium( final int         selectedGenreId,
			       final PrintWriter printWriter ) {
	final char tabChar = '\t';
	final char crChar  = '\r';

	// Header line (windows end-of-line: carriage-return + line-feed)
	printWriter.println( "Medium Titel" + tabChar + "Uitvoerenden" + tabChar +
			     "Sub-genre" + tabChar + "Medium" + tabChar +
			     "Label" + tabChar + "Label Nummer" + tabChar +
			     "Aankoop datum" + tabChar + "Opslag" + crChar );

	String mediumQueryString =
	    "SELECT medium_titel, uitvoerenden, subgenre, medium_type, " +
	    "label, label_nummer, aankoop_datum, opslag FROM medium " +
	    "LEFT JOIN subgenre ON medium.subgenre_id = subgenre.subgenre_id " +
	    "LEFT JOIN medium_type ON medium.medium_type_id = medium_type.medium_type_id " +
	    "LEFT JOIN label ON medium.label_id = label.label_id " +
	    "LEFT JOIN opslag ON medium.opslag_id = opslag.opslag_id " +
	    "WHERE genre_id = " + selectedGenreId + " " +
	    "ORDER BY medium_titel, subgenre, uitvoerenden";

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( mediumQueryString );

	    while ( resultSet.next( ) ) {
		String mediumTitelString = resultSet.getString( 1 );
		if ( mediumTitelString != null ) printWriter.print( mediumTitelString );
		printWriter.print( tabChar );

		String uitvoerendenString = resultSet.getString( 2 );
		if ( uitvoerendenString != null ) printWriter.print( uitvoerendenString );
		printWriter.print( tabChar );

		String subgenreString = resultSet.getString( 3 );
		if ( subgenreString != null ) printWriter.print( subgenreString );
		printWriter.print( tabChar );

		String mediumTypeString = resultSet.getString( 4 );
		if ( mediumTypeString != null ) printWriter.print( mediumTypeString );
		printWriter.print( tabChar );

		String labelString = resultSet.getString( 5 );
		if ( labelString != null ) printWriter.print( labelString );
		printWriter.print( tabChar );

		String labelNummerString = resultSet.getString( 6 );
		if ( labelNummerString != null ) printWriter.print( labelNummerString );
		printWriter.print( tabChar );

		String aankoopDatumString = resultSet.getString( 7 );
		if ( aankoopDatumString != null ) printWriter.print( aankoopDatumString );
		printWriter.print( tabChar );

		String opslagString = resultSet.getString( 8 );
		if ( opslagString != null ) printWriter.print( opslagString );

		// Windows end-of-line: carriage-return + line-feed
		printWriter.println( crChar );
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "ExportMediumFrame.exportMedium SQLException:\n\t" + ex.getMessage( ) );
	}
    }
}
