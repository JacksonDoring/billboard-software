package billboard.viewer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;

public class BillboardMessage implements BillboardElement {
    private String text;
    private Color colour;
    private boolean loaded;

    /**
     * Creates the billboard message GUI element
     * @param bounds - Bounds to fit the information in
     * @return Created JLabel
     */
    public JLabel Create(Dimension bounds) {
        final int maxSize = 75;

        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setSize(bounds);
        label.setForeground(colour);

        label.setFont(new Font(Font.SANS_SERIF, Font.BOLD, maxSize));
        Billboard.SetTextFit(label, text);

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
        Element e = doc.createElement("message");
        e.appendChild(doc.createTextNode(this.text));

        if (this.colour != Color.black) { // Not default colour
            e.setAttribute("colour", Billboard.HexColour(this.colour));
        }

        return e;
    }

    /**
     * Constructor for creating the billboard message from an XML element
     * @param element - Message XML element
     */
    public BillboardMessage(Element element) {
        this.text = element.getTextContent();
        this.colour = Color.black;

        if (element.getAttribute("colour") != "") {
            this.colour = Color.decode(element.getAttribute("colour"));
        }

        this.loaded = true;
    }

    /**
     * Constructor for creating the billboard message from text and a colour
     * @param text - Message text
     * @param colour - Message colour
     */
    public BillboardMessage(String text, Color colour) {
        this.text = text;
        this.colour = colour;

        this.loaded = true;
    }

    /**
     * Constructor for creating the billboard message from text
     * @param text - Message text
     */
    public BillboardMessage(String text) {
        this.text = text;
        this.colour = Color.black;

        this.loaded = true;
    }

    /**
     * Constructor for creating an empty billboard message
     */
    public BillboardMessage() {
        this.loaded = false;
    }

    /**
     * Returns the message's text
     * @return Message text
     */
    public String getText() {
        return text;
    }

    /**
     * Returns the message's colour
     * @return Message colour
     */
    public Color getColour() {
        return colour;
    }
}
