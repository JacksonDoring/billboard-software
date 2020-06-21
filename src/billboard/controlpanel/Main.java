package billboard.controlpanel;

import billboard.viewer.BillboardElements;
import billboard.viewer.BillboardInformation;
import billboard.viewer.BillboardMessage;
import billboard.viewer.BillboardPicture;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.IOException;
import java.util.HashMap;

/**
 * Main control panel class
 */
public class Main {
    /**
     * Runs the control panel
     * @param args - Main args
     * @throws BillboardServerException if a billboard-server exception occurred
     */
    public static void main(String[] args) throws BillboardServerException {
        new Login();
    }
}