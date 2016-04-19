// frame to export opus selected on genre, sorted on componisten

package muziek.exportopus;

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


public class ExportOpusFrame {
    final Connection conn;
    final JFrame frame = new JFrame( "Export Opus" );

    private GenreComboBox genreComboBox;

    public ExportOpusFrame( final Connection conn ) {
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
						       "Export Opus error",
						       JOptionPane.ERROR_MESSAGE );
			return;
		    }

		    final JFileChooser exportFileChooser = new JFileChooser( "/home/cvengelen/java/muziek" );
		    exportFileChooser.setFileFilter( new TextFileFilter( ) );

		    int returnVal = exportFileChooser.showDialog( frame, "Export Opus" );
		    if ( returnVal == JFileChooser.APPROVE_OPTION ) {
			File exportFile = exportFileChooser.getSelectedFile( );
			FileWriter exportFileWriter = null;

			try {
			    exportFileWriter = new FileWriter( exportFile );
			} catch ( IOException ioException ) {
			    System.err.println( "ExportOpusFrame IOException:\n\t" +
						ioException.getMessage( ) );
			}

			PrintWriter exportPrintWriter = new PrintWriter( exportFileWriter );

			// Export the opus records for the selected genre to the selected file
			exportOpus( selectedGenreId, exportPrintWriter );

			try {
			    exportFileWriter.close( );
			} catch ( IOException ioException ) {
			    System.err.println( "ExportOpusFrame IOException:\n\t" +
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

    private void exportOpus( final int         selectedGenreId,
			     final PrintWriter printWriter ) {
	final char tabChar = '\t';
	final char crChar  = '\r';

	// Make Windows end-of-line: carriage-return + line-feed
	printWriter.println( "Componist" + tabChar + "Type" + tabChar + "Titel" + tabChar +
			     "Opus" + tabChar + "Tijdperk" + tabChar + "Musici" + tabChar +
			     "Medium" + tabChar + "Label" + tabChar + "Label Nummer" + tabChar +
			     "Medium Titel" + crChar );

	String opusQueryString =
	    "SELECT persoon.persoon, type.type, opus.opus_titel, opus.opus_nummer, tijdperk.tijdperk, " +
	    "musici.musici, medium_type.medium_type, label.label, medium.label_nummer, medium.medium_titel " +
	    "FROM opname " +
	    "LEFT JOIN opus ON opus.opus_id = opname.opus_id " +
	    "LEFT JOIN type ON type.type_id = opus.type_id " +
	    "LEFT JOIN tijdperk ON tijdperk.tijdperk_id = opus.tijdperk_id " +
	    "LEFT JOIN componisten_persoon ON componisten_persoon.componisten_id = opus.componisten_id " +
	    "LEFT JOIN persoon ON persoon.persoon_id = componisten_persoon.persoon_id " +
	    "LEFT JOIN musici ON musici.musici_id = opname.musici_id " +
	    "LEFT JOIN medium ON medium.medium_id = opname.medium_id " +
	    "LEFT JOIN medium_type ON medium.medium_type_id = medium_type.medium_type_id " +
	    "LEFT JOIN label ON medium.label_id = label.label_id " +
	    "WHERE opus.genre_id = " + selectedGenreId + " " +
	    "ORDER BY persoon.persoon, type.type, opus.opus_titel";

	System.err.println( "Opus Query String: " + opusQueryString );

	try {
	    Statement statement = conn.createStatement( );
	    ResultSet resultSet = statement.executeQuery( opusQueryString );

	    while ( resultSet.next( ) ) {
		String componistString = resultSet.getString( 1 );
		if ( componistString != null ) printWriter.print( componistString );
		printWriter.print( tabChar );

		String typeString = resultSet.getString( 2 );
		if ( typeString != null ) printWriter.print( typeString );
		printWriter.print( tabChar );

		String opusTitelString = resultSet.getString( 3 );
		if ( opusTitelString != null ) printWriter.print( opusTitelString );
		printWriter.print( tabChar );

		String opusNummerString = resultSet.getString( 4 );
		if ( opusNummerString != null ) printWriter.print( opusNummerString );
		printWriter.print( tabChar );

		String tijdperkString = resultSet.getString( 5 );
		if ( tijdperkString != null ) printWriter.print( tijdperkString );
		printWriter.print( tabChar );

		String musiciString = resultSet.getString( 6 );
		if ( musiciString != null ) printWriter.print( musiciString );
		printWriter.print( tabChar );

		String mediumTypeString = resultSet.getString( 7 );
		if ( mediumTypeString != null ) printWriter.print( mediumTypeString );
		printWriter.print( tabChar );

		String labelString = resultSet.getString( 8 );
		if ( labelString != null ) printWriter.print( labelString );
		printWriter.print( tabChar );

		String labelNummerString = resultSet.getString( 9 );
		if ( labelNummerString != null ) printWriter.print( labelNummerString );
		printWriter.print( tabChar );

		String mediumTitelString = resultSet.getString( 10 );
		if ( mediumTitelString != null ) printWriter.print( mediumTitelString );

		// Make Windows end-of-line: carriage-return + line-feed
		printWriter.println( crChar );
	    }
	} catch ( SQLException ex ) {
	    System.err.println( "ExportOpusFrame.exportOpus SQLException:\n\t" + ex.getMessage( ) );
	}
    }
}
