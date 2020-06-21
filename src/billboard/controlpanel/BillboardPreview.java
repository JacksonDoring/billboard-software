package billboard.controlpanel;

import billboard.viewer.Billboard;
import billboard.viewer.BillboardElements;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;

/**
 * Class for previewing billboards
 */
public class BillboardPreview {
    private JFrame frame;

    private Billboard billboard;
    private boolean showingBillboard = false;

    /**
     * Loads a blank billboard
     */
    private void Load() {
        // Close the previous preview
        if (showingBillboard) {
            billboard.dispose();
        }

        billboard = new Billboard(false);
        billboard.setTitle("Billboard Preview");

        showingBillboard = true;
    }

    /**
     * Loads a billboard from elements
     * @param elements - billboard elements
     */
    public void LoadVariables(BillboardElements elements) {
        Load();

        billboard.LoadFromElements(elements);
        billboard.DrawBillboard();
    }

    /**
     * Loads a billboard from an XML document
     * @param doc - XML document
     */
    public void LoadXML(Document doc) {
        Load();

        try {
            billboard.LoadFromXML(doc);
            billboard.DrawBillboard();
        } catch (IOException e) {
            billboard.ShowErrorBillboard();
        }
    }

    /**
     * Loads a billboard from an XML string
     * @param xmlString - XML string
     * @throws ParserConfigurationException if parsing the XML fails
     * @throws IOException if parsing the XML fails
     * @throws SAXException if parsing the XML fails
     */
    public void LoadXML(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

        LoadXML(doc);
    }
}
