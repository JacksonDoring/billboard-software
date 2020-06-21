package billboard.controlpanel;

import org.xml.sax.SAXException;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;

/**
 * GUI for importing an XML file containing billboard data
 */
public class ImportXML {
    private JTextField billboardName;
    private JButton uploadBillboardFIleButton;
    private JPanel panel;
    private JButton selectXMLFileButton;
    private String xmlFile;

    /**
     * Main GUI
     */
    public ImportXML(){
        JFrame frame = new JFrame("Import XML File");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.getContentPane().add(panel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        selectXMLFileButton.addActionListener(e -> {
            try {
                xmlFile = BillboardFileFunctions.importXML(frame);
            } catch (ParserConfigurationException | IOException | SAXException | TransformerException ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(frame, "Failed to parse XML");
            }
        });

        uploadBillboardFIleButton.addActionListener(e -> {
            // Making sure the user has a file selected
            if (xmlFile.isEmpty()){
                JOptionPane.showMessageDialog(frame, "Please make sure to select a file");
                return;
            }

            // adding the file to the database
            try {
                ServerBillboards.createBillboard(billboardName.getText(), xmlFile);

                JOptionPane.showMessageDialog(frame, "Billboard file uploaded successfully");
            } catch (BillboardServerException ex) {
                ex.printStackTrace();

                JOptionPane.showMessageDialog(frame, ex.getMessage());
            }
        });
    }
}
