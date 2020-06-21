package billboard.controlpanel;

import billboard.viewer.Billboard;
import billboard.viewer.BillboardElements;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class for the GUI behind importing and exporting billboards
 */
public class BillboardFileFunctions {
    /**
     * Creates an interface to export a billboard's XML data
     * @param frame - frame to draw to
     * @param billboardName - name of the billboard being exported
     * @param elements - elements of the billboard being exported
     */
    public static void exportXML(JFrame frame, String billboardName, BillboardElements elements){
        //opens the file chooser
        JFileChooser fileChooser = new JFileChooser();
        //making it so it will only show directories and not files
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = fileChooser.showDialog(frame, "Select Folder");

        //if the user selects a folder
        if (result == JFileChooser.APPROVE_OPTION){
            String directory = fileChooser.getSelectedFile().toString();

            try {
                //getting the billboard name so that the file can be called that
                if (billboardName == "") {
                    JOptionPane.showMessageDialog(frame, "Billboard needs a name");
                    return;
                }

                // Get billboard XML
                Document doc = Billboard.ConvertToXML(elements);

                // Convert billboard XML to string
                String xmlString = Billboard.XMLToString(doc);

                //initializing the file writer with the chosen directory
                FileWriter writer = new FileWriter(directory + "/" + billboardName + ".xml");

                //writing the xml string to a .xml file
                writer.write(xmlString);
                writer.close();

                JOptionPane.showMessageDialog(frame, "File exported to " + directory);
            } catch (TransformerException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, ex.getMessage() + " Please try another folder.");
            } catch (ParserConfigurationException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Creates an interface to import a billboard's XML data
     * @param frame - frame to draw to
     * @return XML data
     * @throws ParserConfigurationException if XML functions fail
     * @throws IOException if XML functions fail
     * @throws SAXException if XML functions fail
     * @throws TransformerException if XML functions fail
     */
    public static String importXML(JFrame frame) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        //opens the file chooser
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("XML Files", "xml");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showDialog(frame, "Choose File");

        //if the user selects a folder
        if (result == JFileChooser.APPROVE_OPTION){
            File file = fileChooser.getSelectedFile();

            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document doc = docBuilder.parse(file);

            return Billboard.XMLToString(doc);

        }

        return "";
    }
}
