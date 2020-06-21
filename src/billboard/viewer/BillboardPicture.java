package billboard.viewer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Base64;

public class BillboardPicture implements BillboardElement {
    private BufferedImage image;
    private boolean loaded;
    private String url;
    private String data;

    /**
     * Creates the billboard picture GUI element
     * @param bounds - Bounds to fit the information in
     * @return Created JLabel
     */
    public JLabel Create(Dimension bounds) {
        JLabel label = new JLabel(Billboard.ScaleImage(new ImageIcon(image), bounds), SwingConstants.CENTER);

        return label;
    }

    /**
     * Returns whether or not the element is valid
     * @return Validity
     */
    public boolean Valid() {
        return this.loaded;
    }

    /**
     * Converts the billboard element into an XML element
     * @param doc - XML document to add to
     * @return XML element
     */
    public Element GetXML(Document doc) {
        Element e = doc.createElement("picture");

        if (this.url != null) { // URL image
            e.setAttribute("url", this.url);
        } else if (this.data != null) { // Base64 image
            e.setAttribute("data", this.data);
        }

        return e;
    }

    /**
     * Constructor for creating the billboard picture from an XML element
     * @param element - Picture XML element
     * @throws IOException if loading the image fails
     */
    public BillboardPicture(Element element) throws IOException {
        String url = element.getAttribute("url");
        String data = element.getAttribute("data");

        if (url != "") {
            // Load image from URL
            URL urlObject = new URL(url);
            this.image = ImageIO.read(urlObject);
            this.url = url;
        } else {
            // Load image from base64
            byte[] btDataFile = Base64.getDecoder().decode(data);
            this.image = ImageIO.read(new ByteArrayInputStream(btDataFile));
            this.data = data;
        }

        this.loaded = true;
    }

    /**
     * Constructor for creating the billboard picture from an image
     * @param image - Image
     * @throws IOException if loading the image fails
     */
    public BillboardPicture(BufferedImage image) throws IOException {
        this.image = image;

        // Store base64 image data
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(image, "png", os);
        this.data = Base64.getEncoder().encodeToString(os.toByteArray());

        this.loaded = true;
    }

    /**
     * Constructor for creating the billboard picture from an image URL
     * @param url - Image URL
     * @throws IOException if loading the image fails
     */
    public BillboardPicture(String url) throws IOException {
        // Load image from URL
        URL urlObject = new URL(url);
        this.image = ImageIO.read(urlObject);
        this.url = url;

        this.loaded = true;
    }

    /**
     * Constructor for creating an empty billboard picture
     */
    public BillboardPicture() {
        this.loaded = false;
    }

    /**
     * Returns the image's URL
     * @return Image url
     */
    public String getURL() {
        return url;
    }

    /**
     * Returns the image's data
     * @return Image data
     */
    public String getData() {
        return data;
    }
}
