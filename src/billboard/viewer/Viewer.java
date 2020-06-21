package billboard.viewer;


import billboard.controlpanel.BillboardServerException;
import billboard.controlpanel.ServerSchedules;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

/**
 * Class for the billboard viewer
 */
public class Viewer {
    public boolean open = true;

    private Billboard currentBillboard;

    /**
     * Creates the billboard viewer
     */
    public Viewer(){
        // Create billboard
        currentBillboard = new Billboard(true);


        // Close when window closed
        currentBillboard.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Close();
                System.exit(0); // Close instantly, don't wait for the while loop
            }
        });

        // Close when escape key is pressed
        currentBillboard.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    Close();
                    System.exit(0);
                }
            }
        });

        // Close when the mouse is clicked
        currentBillboard.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 0) {
                    Close();
                    System.exit(0);
                }
            }
        });
    }

    /**
     * Loads an XML document into the billboard viewer
     * @param doc - XML Document
     */
    public void LoadXML(Document doc) {
        try {
            currentBillboard.LoadFromXML(doc);
            currentBillboard.DrawBillboard();
        } catch (IOException e) {
            currentBillboard.ShowErrorBillboard();
        }
    }

    /**
     * Loads an XML string into the billboard viewer
     * @param xmlString - XML String
     */
    public void LoadXMLString(String xmlString) {
        try {
            currentBillboard.LoadFromXMLString(xmlString);
            currentBillboard.DrawBillboard();
        } catch (IOException | ParserConfigurationException | SAXException e) {
            currentBillboard.ShowErrorBillboard();
        }
    }

    /**
     * Connects to the Billboard Server and receives the current billboard to be displayed,
     * if there's an error, then the billboard error screen will be shown
     */
    public void UpdateBillboard(){
        try {
            // Get the current billboard from the server
            HashMap billboard = ServerSchedules.getCurrentBillboard();

            // getting the data
            String billboardData = (String) billboard.get("data");

            // loading it into an XML file to be displayed
            LoadXMLString(billboardData);
        }
        catch (Exception e) { // Catch all exceptions
            e.printStackTrace();

            currentBillboard.ShowErrorBillboard();
        }
    }

    static final int testingBillboards = 16;
    static int billboard = 0;

    /**
     * Updates the billboard viewer, iterating through the testing files
     */
    public void UpdateTestBillboard(){
        try {
            billboard++;
            if (billboard > testingBillboards) billboard = 1;
            String billboardFile = "testing/billboard_test_xmls/" + billboard + ".xml";

            File xmlFile = new File(billboardFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(xmlFile);

            currentBillboard.LoadFromXML(doc);
            currentBillboard.DrawBillboard();
        }
        catch (Exception e){
            e.printStackTrace();
            currentBillboard.ShowErrorBillboard();
        }
    }

    /**
     * Closes the billboard viewer
     */
    void Close() {
        open = false;
        currentBillboard.Close();
    }
}