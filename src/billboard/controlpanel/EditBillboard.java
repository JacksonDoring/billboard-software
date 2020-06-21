package billboard.controlpanel;

import billboard.server.DbBillboard;
import billboard.viewer.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.sql.SQLException;
import java.util.Base64;

/**
 * GUI for editing a billboard
 */
public class EditBillboard {
    private JPanel panel;

    private JTextField billboardNameField;
    private JCheckBox backgroundUseColour;
    private JPanel backgroundColourPanel;
    private JButton backgroundColourButton;

    private JButton previewBillboardButton;
    private JButton editBillboardButton;

    private JCheckBox hasMessage;
    private JPanel messagePanel;
    private JTextField messageField;
    private JCheckBox messageUseColour;
    private JPanel messageColourPanel;
    private JButton messageColourButton;

    private JCheckBox hasPicture;
    private JPanel picturePanel;
    private JButton pictureUploadButton;

    private JCheckBox hasInformation;
    private JPanel informationPanel;
    private JTextField informationField;
    private JCheckBox informationUseColour;
    private JPanel informationColourPanel;
    private JButton informationColourButton;
    private JButton exportXMLFileButton;
    private JButton importXMLFileButton;
    private JTextField imageURL;
    private JRadioButton imageURLRadioButton;
    private JRadioButton uploadImageRadioButton;
    private JLabel imageUrlLabel;
    private JLabel uploadLabel;

    private File pictureFile;

    private Color backgroundColour = Color.WHITE;
    private Color messageColour = Color.BLACK;
    private Color informationColour = Color.BLACK;

    private BillboardPreview preview = new BillboardPreview();
    private BufferedImage pictureImage = null;

    private BillboardElements storedElements;

    /**
     * Gets billboard elements from the GUI
     * @return Billboard elements
     */
    private BillboardElements GetBillboardElements() {
        BillboardElements elements = new BillboardElements();

        if (backgroundColourPanel.isVisible()) {
            elements.backgroundColour = backgroundColour;
        }

        if (messagePanel.isVisible()) {
            if (!messageColourPanel.isVisible()) {
                elements.message = new BillboardMessage(messageField.getText());
            } else {
                elements.message = new BillboardMessage(messageField.getText(), messageColour);
            }
        }

        if (informationPanel.isVisible()) {
            if (!informationColourPanel.isVisible()) {
                elements.information = new BillboardInformation(informationField.getText());
            } else {
                elements.information = new BillboardInformation(informationField.getText(), informationColour);
            }
        }

        if (picturePanel.isVisible()) {
            // Checking if they put in a URL
            if (!imageURL.getText().isEmpty()) {
                try {
                    elements.picture = new BillboardPicture(imageURL.getText());
                } catch (IOException e) {
                    elements.picture = new BillboardPicture();
                }
            }

            // Checking if they selected a file
            if (pictureFile != null && pictureFile.exists()){
                try {
                    pictureImage = ImageIO.read(pictureFile);
                    elements.picture = new BillboardPicture(pictureImage);
                } catch (IOException ex) {
                    elements.picture = new BillboardPicture();
                }
            }
        }

        return elements;
    }

    /**
     * Converting the string data from the database back into an xml file
     * @param xmlString - XML string to convert
     * @return XML document
     * @throws ParserConfigurationException if XML functions fail
     * @throws IOException if XML functions fail
     * @throws SAXException if XML functions fail
     */
    public static Document convertStringToXMLDocument(String xmlString) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        //API to obtain DOM Document instance
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(xmlString)));

        return doc;
    }

    /**
     * Initialises editing a billboard
     * @param billboardId - Billboard ID to edit
     * @throws IOException if XML functions fail
     * @throws SAXException if XML functions fail
     * @throws ParserConfigurationException if XML functions fail
     * @throws BillboardServerException if the billboard data fails to load
     */
    public static void edit(int billboardId) throws IOException, SAXException, ParserConfigurationException, BillboardServerException {
        //Retrieving the billboard data
        String billboardName = ServerBillboards.getBillboardName(billboardId);
        String billboardData = ServerBillboards.getBillboardData(billboardId);

        Document elements = convertStringToXMLDocument(billboardData);

        new EditBillboard(elements, billboardId, billboardName);
    }

    /**
     * Main GUI
     * @param doc - XML document containing the billboard's current data
     * @param billboardId - Editing billboard ID
     * @param originalBillboardName - Editing billboard name
     */
    public EditBillboard(Document doc, int billboardId, String originalBillboardName) {
        JFrame frame = new JFrame("Edit Billboard");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // Hide all the panels
        backgroundColourPanel.setVisible(false);

        messagePanel.setVisible(false);
        messageColourPanel.setVisible(false);

        picturePanel.setVisible(false);

        informationPanel.setVisible(false);
        informationColourPanel.setVisible(false);

        // Load already existing values
        storedElements = new BillboardElements();

        Element root = doc.getDocumentElement();
        root.normalize();

        //If there is a selected background colour, then it will be added to elements
        String backgroundColourText = root.getAttribute("background");
        if(backgroundColourText != ""){
            storedElements.backgroundColour = Color.decode(backgroundColourText);
        }

        //Going through the xml file and adding any messages, info and pic to elements if it exists in the file
        NodeList childNodes = root.getChildNodes();
        for(int i = 0; i < childNodes.getLength(); i++){
            Node childNode = childNodes.item(i);

            if(childNode instanceof Element){
                Element childElement = (Element)childNode;

                String elementType = childElement.getTagName();

                switch(elementType){
                    case "message":
                        storedElements.message = new BillboardMessage(childElement);
                        break;
                    case "information":
                        storedElements.information = new BillboardInformation(childElement);
                        break;
                    case "picture":
                        try {
                            storedElements.picture = new BillboardPicture(childElement);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                }

            }
        }

        //Setting the values that will appear on the GUI for message, info,
        //picture and colours to the values in the xml file

        billboardNameField.setText(originalBillboardName);

        if(storedElements.backgroundColour != null){
            backgroundColourPanel.setVisible(true);
            backgroundUseColour.setSelected(true);
            backgroundColour = storedElements.backgroundColour;
        }

        if(storedElements.message.getText() != null){
            hasMessage.setSelected(true);
            messageField.setText(storedElements.message.getText());
            messagePanel.setVisible(true);

            if(storedElements.message.getColour() != null){
                messageUseColour.setSelected(true);
                messageColourPanel.setVisible(true);
                messageColour = storedElements.message.getColour();
            }
        }

        if(storedElements.information.getText() != null){
            hasInformation.setSelected(true);
            informationField.setText(storedElements.information.getText());
            informationPanel.setVisible(true);

            if(storedElements.information.getColour() != null){
                informationUseColour.setSelected(true);
                informationColourPanel.setVisible(true);
                informationColour = storedElements.information.getColour();
            }
        }

        // If the xml file contains an image, then the image will be converted back into a file
        if(storedElements.picture.getData() != null){
            try {
                picturePanel.setVisible(true);
                hasPicture.setSelected(true);

                byte[] img = Base64.getDecoder().decode(storedElements.picture.getData());
                InputStream in = new ByteArrayInputStream(img);
                BufferedImage buffImage = ImageIO.read(in);

                File image = new File("image.png");
                ImageIO.write(buffImage, "PNG", image);
                pictureFile = image;

                uploadImageRadioButton.doClick();
            } catch (IOException e) {
                e.printStackTrace();

                JOptionPane.showMessageDialog(frame, "Failed to load billboard picture");
            }
        }

        if (storedElements.picture.getURL() != null){
            picturePanel.setVisible(true);
            hasPicture.setSelected(true);

            imageURL.setText(storedElements.picture.getURL());

            imageURLRadioButton.doClick();
        }

        // Show/hide billboard content panels on checkbox pressing
        hasMessage.addActionListener(e -> {
            messagePanel.setVisible(!messagePanel.isVisible());
        });

        hasPicture.addActionListener(e -> {
            picturePanel.setVisible(!picturePanel.isVisible());
        });

        hasInformation.addActionListener(e -> {
            informationPanel.setVisible(!informationPanel.isVisible());
        });


        backgroundUseColour.addActionListener(e -> {
            backgroundColourPanel.setVisible(!backgroundColourPanel.isVisible());
        });

        messageUseColour.addActionListener(e -> {
            messageColourPanel.setVisible(!messageColourPanel.isVisible());
        });

        informationUseColour.addActionListener(e -> {
            informationColourPanel.setVisible(!informationColourPanel.isVisible());
        });

        // Add colour pickers
        backgroundColourButton.addActionListener(e -> {
            backgroundColour = JColorChooser.showDialog(frame, "Select a background colour", backgroundColour);
        });

        messageColourButton.addActionListener(e -> {
            messageColour = JColorChooser.showDialog(frame, "Select a message colour", messageColour);
        });

        informationColourButton.addActionListener(e -> {
            informationColour = JColorChooser.showDialog(frame, "Select an information colour", informationColour);
        });

        imageUrlLabel.setVisible(false);
        imageURL.setVisible(false);
        uploadLabel.setVisible(false);
        pictureUploadButton.setVisible(false);



        //making the button group to link the radio buttons together
        ButtonGroup imageRadio = new ButtonGroup();
        imageRadio.add(imageURLRadioButton);
        imageRadio.add(uploadImageRadioButton);

        //displays the image URL link box
        imageURLRadioButton.addActionListener(e -> {
            imageUrlLabel.setVisible(true);
            imageURL.setVisible(true);
            uploadLabel.setVisible(false);
            pictureUploadButton.setVisible(false);
            pictureImage = null;
        });

        //displays the upload image box
        uploadImageRadioButton.addActionListener(e -> {
            imageUrlLabel.setVisible(false);
            imageURL.setVisible(false);
            uploadLabel.setVisible(true);
            pictureUploadButton.setVisible(true);
            pictureImage = null;
            imageURL.setText("");
        });

        // Add picture upload
        pictureUploadButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();

            // Allow images only
            FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "bmp", "jpeg", "jpg", "png");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showDialog(frame, "Upload billboard picture");
            switch (result) {
                case JFileChooser.APPROVE_OPTION:
                    pictureFile = fileChooser.getSelectedFile();
                    break;
            }
        });

        // Preview button
        previewBillboardButton.addActionListener(e -> {
            BillboardElements elements = GetBillboardElements();

            preview.LoadVariables(elements);
        });

        //Submit button
        editBillboardButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    String billboardName = billboardNameField.getText();
                    if (billboardName == "") {
                        JOptionPane.showMessageDialog(frame, "Billboard needs a name");
                        return;
                    }

                    // Get billboard elements
                    BillboardElements elements = GetBillboardElements();

                    // Get billboard XML
                    Document doc = Billboard.ConvertToXML(elements);

                    // Convert billboard XML to string
                    String xmlString = Billboard.XMLToString(doc);

                    try {
                        // Update the billboard on the server
                        ServerBillboards.updateBillboard(billboardName, xmlString, billboardId);

                        // Showing an a message if the billboard was edited successfully
                        JOptionPane.showMessageDialog(frame, billboardName + " has been edited");

                        // Disposing of the edit billboard frame once the billboard has been edited
                        frame.dispose();
                    } catch (BillboardServerException ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage());
                    }
                } catch (ParserConfigurationException | TransformerException err) {
                    err.printStackTrace();

                    JOptionPane.showMessageDialog(frame, "Failed to edit billboard");
                }
            }
        });

        exportXMLFileButton.addActionListener(e -> {
            String billboardName = billboardNameField.getText();
            BillboardElements elements = GetBillboardElements();

            BillboardFileFunctions.exportXML(frame, billboardName, elements);
        });

        importXMLFileButton.addActionListener(e -> {
            new ImportXML();
        });
    }
}
