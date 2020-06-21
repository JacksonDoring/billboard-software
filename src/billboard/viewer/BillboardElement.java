package billboard.viewer;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.swing.*;
import java.awt.*;

/**
 * Interface for a billboard element
 */
public interface BillboardElement {
    boolean loaded = false;

    JComponent Create(Dimension bounds);
    boolean Valid();
    Element GetXML(Document doc);
}

