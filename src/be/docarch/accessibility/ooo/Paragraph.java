package be.docarch.accessibility.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.container.XEnumeration;
import com.sun.star.text.XTextContent;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.Statement;
import com.sun.star.rdf.XMetadatable;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class Paragraph extends FocusableElement {

    protected boolean exists = false;
    private String sample = "";
    private String id = "";

    private XTextContent xTextContent = null;

    public Paragraph(XResource testsubject)
              throws RepositoryException,
                     NoSuchElementException,
                     IllegalArgumentException,
                     WrappedTargetException {

        logger.entering("Paragraph", "<init>");

        XEnumeration paragraphs = xRepository.getStatements(testsubject, URIs.CHECKER_START, null);
        if (paragraphs.hasMoreElements()) {
            XURI paragraph = URI.create(xContext, ((Statement)paragraphs.nextElement()).Object.getStringValue());
            XMetadatable element = xDMA.getElementByURI(paragraph);
            if (element != null) {
                id = element.getMetadataReference().Second;
                xTextContent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, xDMA.getElementByURI(paragraph));
                if (xTextContent != null) {
                    if (((XServiceInfo)UnoRuntime.queryInterface(
                          XServiceInfo.class, xTextContent)).supportsService("com.sun.star.text.Paragraph")) {
                        exists = true;
                        sample = xTextContent.getAnchor().getString();
                        if (sample.length() > 30) {
                            sample = sample.substring(0, 30) + "\u2026";
                        }
                    }
                }
            }
        }

        logger.exiting("Paragraph", "<init>");
    }

    public boolean exists() {
        return exists;
    }

    public XTextContent getComponent() throws Exception {

        if (exists()) {
            return xTextContent;
        } else {
            throw new Exception("Paragraph does not exist");
        }
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

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Paragraph that = (Paragraph)obj;
        return (!(this.exists()^that.exists()) &&
                  this.id.equals(that.id));
    }

    @Override
    public boolean focus() {

        if (!exists()) { return false; }

        try {

            if (xTextContent != null) {
                return selectionSupplier.select(xTextContent.getAnchor());
            }

        } catch (IllegalArgumentException e) {
        }
        
        return false;
    }
}
