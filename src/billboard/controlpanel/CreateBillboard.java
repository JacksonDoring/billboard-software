package billboard.controlpanel;

import billboard.server.DbBillboard;
import billboard.viewer.*;
import org.w3c.dom.Document;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

/**
 * GUI for creating a billboard
 */
public class CreateBillboard {
    private JPanel panel;

    private JTextField billboardNameField;
    private JCheckBox backgroundUseColour;
    private JPanel backgroundColourPanel;
    private JButton backgroundColourButton;

    private JButton previewBillboardButton;
    private JButton createBillboardButton;

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
     * Main GUI
     */
    public CreateBillboard(){
        JFrame frame = new JFrame("Create Billboard");
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
            backgroundColour = JColorChooser.showDialog(frame, "Select a background colour", Color.RED);
        });

        messageColourButton.addActionListener(e -> {
            messageColour = JColorChooser.showDialog(frame, "Select a message colour", Color.RED);
        });

        informationColourButton.addActionListener(e -> {
            informationColour = JColorChooser.showDialog(frame, "Select an information colour", Color.RED);
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
        createBillboardButton.addActionListener(new ActionListener() {
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
                        if (ServerBillboards.billboardNameExists(billboardName)){
                            ServerBillboards.updateBillboard(billboardName, xmlString, ServerBillboards.getBillboardId(billboardName));

                            JOptionPane.showMessageDialog(frame, billboardName + " was already created so it has been updated.");
                        }
                        else{
                            // Upload the billboard to the server
                            ServerBillboards.createBillboard(billboardName, xmlString);

                            //Showing an a message if the billboard was created successfully
                            JOptionPane.showMessageDialog(frame, billboardName + " has been created");
                        }

                        // Disposing of the create billboard frame once the billboard has been created
                        frame.dispose();
                    } catch (BillboardServerException ex) {
                        JOptionPane.showMessageDialog(frame, ex.getMessage());
                    }
                } catch (ParserConfigurationException | TransformerException err) {
                    err.printStackTrace();

                    JOptionPane.showMessageDialog(frame, "Failed to create billboard");
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
