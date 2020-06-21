package billboard.viewer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * Class for creating/interacting with billboards
 */
public class Billboard extends JFrame {
    private static int padding = 25;
    private boolean fullscreen = true;

    public Dimension billboardSize;

    BillboardElements elements = new BillboardElements();

    /**
     * Scales a JLabel's text to fit its size
     * @param label
     * @param text
     */
    static void SetTextFit(JLabel label, String text) {
        Font originalFont = (Font)label.getClientProperty("originalfont"); // Get the original Font from client properties
        if (originalFont == null) { // First time we call it: add it
            originalFont = label.getFont();
            label.putClientProperty("originalfont", originalFont);
        }

        int stringWidth = label.getFontMetrics(originalFont).stringWidth(text);
        int componentWidth = label.getWidth();

        if (stringWidth > componentWidth) { // Resize only if needed
            // Find out how much the font can shrink in width.
            double widthRatio = (double)componentWidth / (double)stringWidth;

            int newFontSize = (int)Math.floor(originalFont.getSize() * widthRatio); // Keep the minimum size

            // Set the label's font size to the newly determined size.
            label.setFont(new Font(originalFont.getName(), originalFont.getStyle(), newFontSize));
        } else
            label.setFont(originalFont); // Text fits, do not change font size

        label.setText(text);
    }

    /**
     * Scales an image to fit inside bounds
     * @param icon - Image
     * @param bounds - Bounds to scale image to fit inside
     * @return ImageIcon
     */
    public static ImageIcon ScaleImage(ImageIcon icon, Dimension bounds) {
        int newWidth = bounds.width;
        int newHeight = (newWidth * icon.getIconHeight()) / icon.getIconWidth();

        if (newHeight > bounds.height) {
            newHeight = bounds.height;
            newWidth = (icon.getIconWidth() * newHeight) / icon.getIconHeight();
        }

        return new ImageIcon(icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_DEFAULT));
    }

    /**
     * Adds an element with options for placement in the grid
     * @param element
     * @param gbc
     * @param row
     * @param weighty
     */
    private void AddElement(JComponent element, GridBagConstraints gbc, int row, double weighty) {
        gbc.gridy = row;
        gbc.weighty = weighty;

        add(element, gbc);
    }

    /**
     * Loads billboard elements into the billboard from variables
     * @param elements - Billboard elements to load
     */
    public void LoadFromElements(BillboardElements elements) {
        this.elements = elements;
    }

    /**
     * Gets the billboard elements
     * @return billboard elements
     */
    public BillboardElements GetElements() {
        return this.elements;
    }

    /**
     * Converts an XML string to a document
     * @param xmlString - XML String
     * @return XML Document
     * @throws ParserConfigurationException if XML functions fail
     * @throws IOException if XML functions fail
     * @throws SAXException if XML functions fail
     */
    public static Document XMLStringToDocument(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

        return doc;
    }

    /**
     * Loads a billboard from an XML string
     * @param xmlString - XML String
     * @throws ParserConfigurationException if XML functions fail
     * @throws IOException if XML functions fail
     * @throws SAXException if XML functions fail
     */
    public void LoadFromXMLString(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        Document doc = XMLStringToDocument(xmlString);

        LoadFromXML(doc);
    }

    /**
     * Loads the billboard from an XML file
     * @param doc - XML Document
     * @throws IOException if loading the picture fails
     */
    public void LoadFromXML(Document doc) throws IOException {
        // Reset the billboard elements
        elements = new BillboardElements();

        Element root = doc.getDocumentElement();
        root.normalize();

        // Get information about the billboard
        String backgroundColour = root.getAttribute("background");
        if (backgroundColour != "") {
            elements.backgroundColour = Color.decode(backgroundColour);
        }

        NodeList childNodes = root.getChildNodes();

        // Get the elements of the billboard from the xml file
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode instanceof Element) {
                Element childElement = (Element)childNode;

                String elementType = childElement.getTagName();

                switch (elementType) {
                    case "message":
                        elements.message = new BillboardMessage(childElement);
                        break;

                    case "information":
                        elements.information = new BillboardInformation(childElement);
                        break;

                    case "picture":
                        elements.picture = new BillboardPicture(childElement);
                        break;
                }
            }
        }
    }

    /**
     * Helper method for converting a colour to a hex string
     * @param colour - Colour to convert
     * @return Hex string
     */
    public static String HexColour(Color colour) {
        return "#" + Integer.toHexString(colour.getRGB()).substring(2);
    }

    /**
     * Converts billboard elements into an XML file
     * @param elements - Billboard elements
     * @return XML document
     * @throws ParserConfigurationException if creating the XML document fails
     */
    public static Document ConvertToXML(BillboardElements elements) throws ParserConfigurationException {
        // Set up creating the XML file
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Make root element
        Document doc = builder.newDocument();
        Element root = doc.createElement("billboard");

        if (elements.backgroundColour != Color.white) { // not default colour
            root.setAttribute("background", HexColour(elements.backgroundColour));
        }

        doc.appendChild(root);

        // Add message
        if (elements.message.Valid()) {
            root.appendChild(elements.message.GetXML(doc));
        }

        // Add information
        if (elements.information.Valid()) {
            root.appendChild(elements.information.GetXML(doc));
        }

        // Add picture
        if (elements.picture.Valid()) {
            root.appendChild(elements.picture.GetXML(doc));
        }

        // Return the final XML document
        return doc;
    }

    /**
     * Converts an XML file to a string
     * @param doc - XML document
     * @return XML string
     * @throws TransformerException if converting the document fails
     */
    public static String XMLToString(Document doc) throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();

        // Transform the document to string
        transformer.transform(new DOMSource(doc), new StreamResult(writer));

        return writer.getBuffer().toString();
    }

    /**
     * Draws the billboard using the stored elements
     */
    public void DrawBillboard() {
        Initialise();

        // Set layout (centered)
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        // Set background colour
        getContentPane().setBackground(elements.backgroundColour);

        // One element visible
        if (elements.message.Valid() && !elements.information.Valid() && !elements.picture.Valid()) {
            // Message only
            // Message is centered, and fills the screen

            JLabel label = elements.message.Create(billboardSize);

            add(label, gbc);
        }
        else if (!elements.message.Valid() && elements.information.Valid() && !elements.picture.Valid()) {
            // Information only
            // Information is centered, and fills up no more than 75% of the screen's width and 50% of the screen's height

            Dimension bounds = new Dimension((int)(billboardSize.width * 0.75f), (int)(billboardSize.height * 0.5f));
            JLabel label = elements.information.Create(bounds);

            add(label, gbc);
        }
        else if (!elements.message.Valid() && !elements.information.Valid() && elements.picture.Valid()) {
            // Picture only
            // Picture is centered, and scaled up to 50% of the screen's width and height, retaining aspect ratio

            Dimension bounds = new Dimension((int)(billboardSize.width * 0.5f), (int)(billboardSize.height * 0.5f));
            JLabel label = elements.picture.Create(bounds);

            add(label, gbc);
        }

        // Two elements visible
        else if (elements.message.Valid() && elements.information.Valid() && !elements.picture.Valid()) {
            // Message and information
            // Message text fits in the top 1/2, information fits in the bottom 1/2

            Dimension messageBounds = new Dimension(billboardSize.width, (int)(billboardSize.height * 0.5f));
            JLabel messageLabel = elements.message.Create(messageBounds);

            Dimension informationBounds = new Dimension((int)(billboardSize.width * 0.75f), (int)(billboardSize.height * 0.5f));
            JLabel informationLabel = elements.information.Create(informationBounds);

            AddElement(messageLabel, gbc, 0, 1);
            AddElement(informationLabel, gbc, 1, 1);
        }
        else if (elements.message.Valid() && !elements.information.Valid() && elements.picture.Valid()) {
            // Message and picture
            // Message is centered in the top 1/3
            // Picture is centered in the bottom 2/3, and is scaled up to 50% of the screen's width and height, retaining aspect ratio

            Dimension messageBounds = new Dimension(billboardSize.width, (int)(billboardSize.height * 1/3));
            JLabel messageLabel = elements.message.Create(messageBounds);

            Dimension pictureBounds = new Dimension((int)(billboardSize.width * 0.5f), (int)(billboardSize.height * 0.5f));
            JLabel pictureLabel = elements.picture.Create(pictureBounds);

            AddElement(messageLabel, gbc, 0, 1);
            AddElement(pictureLabel, gbc, 1, 2);
        }
        else if (!elements.message.Valid() && elements.information.Valid() && elements.picture.Valid()) {
            // Information and picture
            // Picture is centered in the top 2/3, and is scaled up to 50% of the screen's width and height, retaining aspect ratio
            // Information is centered in the bottom 1/3, and fills up no more than 75% of the screen's width and 50% of the screen's height

            Dimension informationBounds = new Dimension((int)(billboardSize.width * 0.75f), (int)(billboardSize.height * 0.5f));
            JLabel informationLabel = elements.information.Create(informationBounds);

            Dimension pictureBounds = new Dimension((int)(billboardSize.width * 0.5f), (int)(billboardSize.height * 0.5f));
            JLabel pictureLabel = elements.picture.Create(pictureBounds);

            AddElement(pictureLabel, gbc, 0, 2);
            AddElement(informationLabel, gbc, 1, 1);
        }

        // Three elements visible
        else if (elements.message.Valid() && elements.information.Valid() && elements.picture.Valid()) {
            // Message, information and picture
            // Message is centered in the top 1/3
            // Picture is centered in the middle 1/3, and is scaled up to 1/3 of the screen's width and height, retaining aspect ratio
            // Information is centered in the bottom 1/3

            Dimension messageBounds = new Dimension(billboardSize.width, (int)(billboardSize.height * 1/3));
            JLabel messageLabel = elements.message.Create(messageBounds);

            Dimension pictureBounds = new Dimension((int)(billboardSize.width * (1.f / 3.f)), (int)(billboardSize.height * (1.f / 3.f)));
            JLabel pictureLabel = elements.picture.Create(pictureBounds);

            Dimension informationBounds = new Dimension((int)(billboardSize.width * 0.75f), (int)(billboardSize.height * 1/3));
            JLabel informationLabel = elements.information.Create(informationBounds);

            AddElement(messageLabel, gbc, 0, 1);
            AddElement(pictureLabel, gbc, 1, 1);
            AddElement(informationLabel, gbc, 2, 1);
        }

        Finalise();
    }

    /**
     * Shows the error billboard
     */
    public void ShowErrorBillboard(){
        Initialise();

        // Set layout (centered)
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        // Add error screen content
        JLabel titleLabel = new JLabel("Error");
        titleLabel.setFont(new Font("Default", Font.BOLD, 30));
        add(titleLabel, gbc);

        JLabel label = new JLabel("Failed to get current billboard");
        label.setFont(new Font("Default", Font.PLAIN, 18));
        add(label, gbc);

        Finalise();
    }

    /**
     * Initialises the billboard for drawing
     */
    private void Initialise() {
        getContentPane().removeAll();
    }

    /**
     * Finalises the billboard drawing
     */
    private void Finalise() {
        setVisible(true); // Update JFrame
    }

    /**
     * Closes the billboard
     */
    public void Close() {
        setVisible(false);
        dispose();
    }

    /**
     * Sets up the billboard
     * @param fullscreen - Whether or not the billboard is fullscreen
     */
    public Billboard(boolean fullscreen){
        setTitle("Billboard Viewer");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        this.fullscreen = fullscreen;
        if (fullscreen) {
            // Set fullscreen
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setUndecorated(true);

            setResizable(false);

            billboardSize = Toolkit.getDefaultToolkit().getScreenSize();
            setSize(billboardSize);
        } else {
            billboardSize = new Dimension(800, 600);

            setResizable(true);
            setSize(billboardSize);
        }

        // Add some padding, having all of the elements reach the edges looks bad
        billboardSize.width *= 0.8f;
        billboardSize.height *= 0.8f;

        setVisible(true);
        setLocationRelativeTo(null); // Center the window
    }
}