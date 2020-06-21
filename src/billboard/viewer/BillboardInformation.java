package billboard.viewer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;

/**
 * Billboard information class
 */
public class BillboardInformation implements BillboardElement {
    public String text;
    public Color colour;
    private boolean loaded;

    /**
     * Creates the billboard information GUI element
     * @param bounds - Bounds to fit the information in
     * @return Created JLabel
     */
    public JLabel Create(Dimension bounds) {
        final int maxSize = 30;

        JLabel label = new JLabel("<html><div style='text-align: center' width=" + bounds.width + ">" + text + "</div></html>", SwingConstants.CENTER);
        label.setSize(bounds);
        label.setForeground(colour);

        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, maxSize));

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
        Element e = doc.createElement("information");
        e.appendChild(doc.createTextNode(this.text));

        if (this.colour != Color.black) { // Not default colour
            e.setAttribute("colour", Billboard.HexColour(this.colour));
        }

        return e;
    }

    /**
     * Constructor for creating the billboard information from an XML element
     * @param element - Information XML element
     */
    public BillboardInformation(Element element) {
        this.text = element.getTextContent();
        this.colour = Color.black;

        if (element.getAttribute("colour") != "") {
            this.colour = Color.decode(element.getAttribute("colour"));
        }

        this.loaded = true;
    }

    /**
     * Constructor for creating the billboard information from text and a colour
     * @param text - Information text
     * @param colour - Information colour
     */
    public BillboardInformation(String text, Color colour) {
        this.text = text;
        this.colour = colour;

        this.loaded = true;
    }

    /**
     * Constructor for creating the billboard information from text
     * @param text - Information text
     */
    public BillboardInformation(String text) {
        this.text = text;
        this.colour = Color.black;

        this.loaded = true;
    }

    /**
     * Constructor for creating an empty billboard information
     */
    public BillboardInformation() {
        this.loaded = false;
    }

    /**
     * Returns the information's text
     * @return Information text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the information's colour
     * @return Information colour
     */
    public Color getColour() {
        return colour;
    }
}
