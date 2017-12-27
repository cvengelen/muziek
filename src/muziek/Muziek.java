// Package:	muziek
// File:	Muziek.java
// Description:	Muziek main program
// Author:	Chris van Engelen
// History:	2017/12/26: Initial version

package muziek;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * Muziek main program
 * @author Chris van Engelen
 */
public class Muziek extends JFrame implements ActionListener {
    private final static Logger logger = Logger.getLogger( muziek.Main.class.getCanonicalName() );

    private JDesktopPane desktopPane;
    private Connection connection;
    private String password;

    private int openFrameCount = 0;
    private static final int xOffset = 30, yOffset = 30;

    private Muziek() {
        super("Muziek");

        final int inset = 100;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setBounds(inset, inset, screenSize.width  - (3 * inset), screenSize.height - (2 * inset));

        // Set up the GUI.
        desktopPane = new JDesktopPane();
        setContentPane(desktopPane);
        setJMenuBar(createMenuBar());

        // Make dragging a little faster but perhaps uglier.
        desktopPane.setDragMode(JDesktopPane.OUTLINE_DRAG_MODE);

        try {
            // Load the MySQL JDBC driver
            Class.forName( "com.mysql.jdbc.Driver" );
        } catch ( ClassNotFoundException classNotFoundException ) {
            logger.severe( "ClassNotFoundException: " + classNotFoundException.getMessage() );
            System.exit( 1 );
        }

        try {
            // Get the password for the muziek account, which gives access to schema muziek.
            final muziek.gui.PasswordPanel passwordPanel = new muziek.gui.PasswordPanel();
            password = passwordPanel.getPassword();
            if (password == null) {
                logger.info("No password");
                System.err.println("Geen password gegeven");
                System.exit( 1 );
            }

            // Get the connection to the muziek schema in the MySQL database
            connection = DriverManager.getConnection( "jdbc:mysql://localhost/muziek?user=muziek&password=" + password );
        } catch ( SQLException sqlException ) {
            logger.severe( "SQLException: " + sqlException.getMessage( ) );
            System.exit( 1 );
        } catch ( Exception exception ) {
            logger.severe( "Exception: " + exception.getMessage( ) );
            System.exit( 1 );
        }

        // Add a window listener to close the connection when the frame is disposed
        addWindowListener( new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                try {
                    // Close the connection to the MySQL database
                    connection.close( );
                } catch (SQLException sqlException) {
                    logger.severe( "SQL exception closing connection: " + sqlException.getMessage() );
                }
            }
        } );
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // Set up the Edit menu
        JMenu menu = new JMenu("Edit");
        menu.setMnemonic(KeyEvent.VK_E);
        menuBar.add(menu);

        // Edit opname
        JMenuItem menuItem = new JMenuItem("Opname", KeyEvent.VK_O);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editOpname");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit medium
        menuItem = new JMenuItem("Medium", KeyEvent.VK_M);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editMedium");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit componisten
        menuItem = new JMenuItem("Componisten", KeyEvent.VK_C);
        menuItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.ALT_MASK));
        menuItem.setActionCommand("editComponisten");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit ensemble
        menuItem = new JMenuItem("Ensemble");
        menuItem.setActionCommand("editEnsemble");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit label
        menuItem = new JMenuItem("Label");
        menuItem.setActionCommand("editLabel");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit musici
        menuItem = new JMenuItem("Musici");
        menuItem.setActionCommand("editMusici");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit opname datum
        menuItem = new JMenuItem("Opname datum");
        menuItem.setActionCommand("editOpnameDatum");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit opname plaats
        menuItem = new JMenuItem("Opname plaats");
        menuItem.setActionCommand("editOpnamePlaats");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit opslag
        menuItem = new JMenuItem("Opslag");
        menuItem.setActionCommand("editOpslag");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit opus
        menuItem = new JMenuItem("Opus");
        menuItem.setActionCommand("editOpus");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit persoon
        menuItem = new JMenuItem("Persoon");
        menuItem.setActionCommand("editPersoon");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit producers
        menuItem = new JMenuItem("Producers");
        menuItem.setActionCommand("editProducers");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit rol
        menuItem = new JMenuItem("Rol");
        menuItem.setActionCommand("editRol");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        // Edit subtype
        menuItem = new JMenuItem("Subtype");
        menuItem.setActionCommand("editSubtype");
        menuItem.addActionListener(this);
        menu.add(menuItem);

        return menuBar;
    }

    // React to menu selections.
    public void actionPerformed(ActionEvent actionEvent) {
        JInternalFrame internalFrame = null;
        if ("editOpname".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.opname.EditOpname( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editMedium".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.medium.EditMedium( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editComponisten".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.componisten.EditComponisten( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editEnsemble".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.ensemble.EditEnsemble( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editLabel".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.label.EditLabel( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editMusici".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.musici.EditMusici( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editOpnameDatum".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.opnamedatum.EditOpnameDatum( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editOpnamePlaats".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.opnameplaats.EditOpnamePlaats( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editOpslag".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.opslag.EditOpslag( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editOpus".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.opus.EditOpus( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editPersoon".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.persoon.EditPersoon( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editProducers".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.producers.EditProducers( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editRol".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.rol.EditRol( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }
        else if ("editSubtype".equals(actionEvent.getActionCommand())) {
            internalFrame = new muziek.subtype.EditSubtype( connection, this, xOffset * openFrameCount, yOffset * openFrameCount );
        }

        if (internalFrame == null) {
            logger.severe( "Invalid action command: " + actionEvent.getActionCommand() );
            return;
        }

        internalFrame.setVisible( true );
        desktopPane.add( internalFrame );
        try {
            internalFrame.setSelected( true );
            openFrameCount++;
        } catch ( java.beans.PropertyVetoException propertyVetoException ) {
            JOptionPane.showMessageDialog( this, propertyVetoException.getMessage( ),
                    "The internal frame could not be dusplayed",
                    JOptionPane.ERROR_MESSAGE);
            logger.severe( propertyVetoException.getMessage() );
        }
    }

    public static void main(String[] args) {
        // Schedule a job for the event-dispatching thread, creating and showing this application's GUI.
        // See: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/initial.html
        // and: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/dispatch.html
        try {
            javax.swing.SwingUtilities.invokeAndWait( () -> {
                // Use the default window decorations.
                JFrame.setDefaultLookAndFeelDecorated( true );

                // Create and set up the window.
                Muziek muziek = new Muziek();
                muziek.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );

                // Display the window.
                muziek.setVisible( true );
            } );
        }
        catch (InvocationTargetException | InterruptedException exc) {
            System.err.print("Exception: " + exc.getMessage());
            System.exit(1);
        }
    }
}
