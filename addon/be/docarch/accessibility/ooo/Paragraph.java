package be.docarch.accessibility.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.XTextContent;
import com.sun.star.rdf.XMetadatable;

import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class Paragraph extends FocusableElement {

    private final XTextContent xTextContent;
    private final XMetadatable metadatable;
    private String sample = "";
    private String id = "";

    public Paragraph(XTextContent textContent) throws Exception {
        
        if (textContent != null) {
            xTextContent = textContent;
            metadatable = (XMetadatable)UnoRuntime.queryInterface(XMetadatable.class, xTextContent);
            if (metadatable != null) {
                metadatable.ensureMetadataReference();
                init();
                return;
            }
        }
        throw new Exception();
    }

    public Paragraph(XMetadatable metadatable) throws Exception {

        if (metadatable != null) {
            this.metadatable = metadatable;
            metadatable.ensureMetadataReference();
            xTextContent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, metadatable);
            if (xTextContent != null) {
                init();
                return;
            }
        }
        throw new Exception();
    }

    private void init() throws Exception {

        id = metadatable.getMetadataReference().Second;
        if (((XServiceInfo)UnoRuntime.queryInterface(
              XServiceInfo.class, xTextContent)).supportsService("com.sun.star.text.Paragraph")) {
            sample = xTextContent.getAnchor().getString();
            if (sample.length() > 30) {
                sample = sample.substring(0, 30) + "\u2026";
            }
            return;
        }

        throw new Exception();
    }

    public XTextContent getXTextContent() {
        return xTextContent;
    }

    public XMetadatable getXMetadatable() {
        return metadatable;
    }

    public String toString() {
        return sample;
    }

    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = PRIME + id.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Paragraph that = (Paragraph)obj;
        return (this.id.equals(that.id));
    }

    @Override
    public boolean focus() {

        try {
            if (xTextContent != null) {
                return selectionSupplier.select(xTextContent.getAnchor());
            }
        } catch (IllegalArgumentException e) {
        }
        
        return false;
    }
}
