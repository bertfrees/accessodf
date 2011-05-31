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
public class Span extends Element {

    private boolean exists = false;
    private String startId = "";
    private String endId = "";
    private String sample = "";

    private XTextContent[] component = null;

    public Span(XResource testsubject)
         throws RepositoryException,
                NoSuchElementException,
                IllegalArgumentException,
                WrappedTargetException {

        logger.entering("Span", "<init>");

        XEnumeration starts = xRepository.getStatements(testsubject, URIs.CHECKER_START, null);
        XEnumeration ends = xRepository.getStatements(testsubject, URIs.CHECKER_END, null);
        XEnumeration samples = xRepository.getStatements(testsubject, URIs.CHECKER_SAMPLE, null);
        if (starts.hasMoreElements() && ends.hasMoreElements()) {
            XURI start = URI.create(xContext, ((Statement)starts.nextElement()).Object.getStringValue());
            XURI end = URI.create(xContext, ((Statement)ends.nextElement()).Object.getStringValue());
            XMetadatable startElement = xDMA.getElementByURI(start);
            XMetadatable endElement = xDMA.getElementByURI(end);
            if (startElement != null && endElement != null) {
                startId = startElement.getMetadataReference().Second;
                endId = endElement.getMetadataReference().Second;
                XTextContent startComponent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, startElement);
                XTextContent endComponent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, endElement);
                if (startComponent != null && endComponent!= null) {
                    if (((XServiceInfo)UnoRuntime.queryInterface(
                          XServiceInfo.class, startComponent)).supportsService("com.sun.star.text.InContentMetadata") &&
                        ((XServiceInfo)UnoRuntime.queryInterface(
                          XServiceInfo.class, endComponent)).supportsService("com.sun.star.text.InContentMetadata")) {
                        component = new XTextContent[] { startComponent, endComponent };
                        exists = true;
                        if (samples.hasMoreElements()) {
                            sample = ((Statement)samples.nextElement()).Object.getStringValue();
                        }
                    }
                }
            }
        }

        logger.exiting("Span", "<init>");
    }

    public boolean exists() {
        return exists;
    }

    public XTextContent[] getComponent() throws Exception {

        if (exists()) {
            return component;
        } else {
            throw new Exception("Span does not exist");
        }
    }

    public String toString() {
        return sample;
    }

    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = hash * PRIME + startId.hashCode();
        hash = hash * PRIME + endId.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Span that = (Span)obj;
        return (!(this.exists()^that.exists()) &&
                  this.startId.equals(that.startId) &&
                  this.endId.equals(that.endId));
    }
}