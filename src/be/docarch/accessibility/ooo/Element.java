package be.docarch.accessibility.ooo;

import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNamed;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.text.XTextEmbeddedObjectsSupplier;
import com.sun.star.rdf.XDocumentMetadataAccess;
import com.sun.star.rdf.XRepository;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.Statement;
import com.sun.star.rdf.XMetadatable;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IllegalArgumentException;

import be.docarch.accessibility.URIs;


/**
 *
 * @author Bert Frees
 */
public class Element {

    private final static Logger logger = Logger.getLogger("be.docarch.accessibility");

    public static enum Type { DOCUMENT,
                              PARAGRAPH,
                              SPAN,
                              TABLE,
                              OBJECT,
                              OTHER }

    private static XComponentContext xContext = null;
    private static XDocumentMetadataAccess xDMA = null;
    private static XRepository xRepository = null;
    private static XNameAccess tables = null;
    private static XNameAccess embeddedObjects = null;
    private static XNameAccess graphicObjects = null;

    private static XURI RDF_TYPE = null;
    private static XURI CHECKER_START = null;
    private static XURI CHECKER_END = null;
    private static XURI CHECKER_NAME = null;
    private static XURI CHECKER_SAMPLE = null;

    private boolean exists = false;
    private Type type = Type.OTHER;
    private String id = "";
    private String id2 = "";
    private String sample = "";

    private XTextContent paragraph = null;
    private XTextContent[] span = null;
    private XTextTable table = null;
    private XNamed embeddedObject = null;


    static void setDocument(Document document)
                     throws IllegalArgumentException {

        Element.xContext = document.xContext;
        Element.xDMA = document.xDMA;
        Element.xRepository = xDMA.getRDFRepository();

        XTextTablesSupplier tablesSupplier =
            (XTextTablesSupplier)UnoRuntime.queryInterface(
                XTextTablesSupplier.class, document.doc);
        tables = tablesSupplier.getTextTables();
        XTextGraphicObjectsSupplier textGraphicObjectsSupplier =
            (XTextGraphicObjectsSupplier)UnoRuntime.queryInterface(
                XTextGraphicObjectsSupplier.class, document.doc);
        XTextEmbeddedObjectsSupplier textEmbeddedObjectsSupplier =
            (XTextEmbeddedObjectsSupplier)UnoRuntime.queryInterface(
                XTextEmbeddedObjectsSupplier.class, document.doc);
        embeddedObjects = textEmbeddedObjectsSupplier.getEmbeddedObjects();
        graphicObjects = textGraphicObjectsSupplier.getGraphicObjects();

        RDF_TYPE = URI.create(xContext, URIs.RDF_TYPE);
        CHECKER_START = URI.create(xContext, URIs.CHECKER_START);
        CHECKER_END = URI.create(xContext, URIs.CHECKER_END);
        CHECKER_NAME = URI.create(xContext, URIs.CHECKER_NAME);
        CHECKER_SAMPLE = URI.create(xContext, URIs.CHECKER_SAMPLE);

    }

    public Element(XResource testsubject)
            throws RepositoryException,
                   NoSuchElementException,
                   IllegalArgumentException,
                   WrappedTargetException {

        logger.entering("Element", "<init>");

        XEnumeration types = xRepository.getStatements(testsubject, RDF_TYPE, null);

        if (types.hasMoreElements()) {
            String t = ((Statement)types.nextElement()).Object.getStringValue();
            if (t == null) {
                type = Type.OTHER;
            } else if (t.equals(URIs.CHECKER_DOCUMENT)) {
                type = Type.DOCUMENT;
            } else if (t.equals(URIs.CHECKER_PARAGRAPH)) {
                type = Type.PARAGRAPH;
            } else if (t.equals(URIs.CHECKER_SPAN)) {
                type = Type.SPAN;
            } else if (t.equals(URIs.CHECKER_TABLE)) {
                type = Type.TABLE;
            } else if (t.equals(URIs.CHECKER_OBJECT)) {
                type = Type.OBJECT;
            } else {
                type = Type.OTHER;
            }
        } else {
            type = Type.OTHER;
        }

        XEnumeration names = null;
        XEnumeration starts = null;
        XEnumeration ends = null;
        XEnumeration samples = null;

        switch (type) {
            case DOCUMENT:
                exists = true;
                break;

            case PARAGRAPH:
                starts = xRepository.getStatements(testsubject, CHECKER_START, null);
                if (starts.hasMoreElements()) {
                    XURI start = URI.create(xContext, ((Statement)starts.nextElement()).Object.getStringValue());
                    XMetadatable element = xDMA.getElementByURI(start);
                    if (element != null) {
                        id = element.getMetadataReference().Second;
                        paragraph = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, xDMA.getElementByURI(start));
                        if (paragraph != null) {
                            if (((XServiceInfo)UnoRuntime.queryInterface(
                                  XServiceInfo.class, paragraph)).supportsService("com.sun.star.text.Paragraph")) {
                                exists = true;
                                sample = paragraph.getAnchor().getString();
                                if (sample.length() > 30) {
                                    sample = sample.substring(0, 30) + "\u2026";
                                }
                            }
                        }
                    }
                }
                break;

            case SPAN:
                starts = xRepository.getStatements(testsubject, CHECKER_START, null);
                ends = xRepository.getStatements(testsubject, CHECKER_END, null);
                samples = xRepository.getStatements(testsubject, CHECKER_SAMPLE, null);
                if (starts.hasMoreElements() && ends.hasMoreElements()) {
                    XURI start = URI.create(xContext, ((Statement)starts.nextElement()).Object.getStringValue());
                    XURI end = URI.create(xContext, ((Statement)ends.nextElement()).Object.getStringValue());
                    XMetadatable element = xDMA.getElementByURI(start);
                    XMetadatable element2 = xDMA.getElementByURI(end);
                    if (element != null && element2 != null) {
                        id = element.getMetadataReference().Second;
                        id2 = element2.getMetadataReference().Second;
                        XTextContent textStart = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, element);
                        XTextContent textEnd = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, element2);
                        if (textStart != null && textEnd!= null) {
                            if (((XServiceInfo)UnoRuntime.queryInterface(
                                  XServiceInfo.class, textStart)).supportsService("com.sun.star.text.InContentMetadata") &&
                                ((XServiceInfo)UnoRuntime.queryInterface(
                                  XServiceInfo.class, textEnd)).supportsService("com.sun.star.text.InContentMetadata")) {
                                span = new XTextContent[] { textStart, textEnd };
                                exists = true;
                                if (samples.hasMoreElements()) {
                                    sample = ((Statement)samples.nextElement()).Object.getStringValue();
                                }
                            }
                        }
                    }
                }
                break;

            case TABLE:
                names = xRepository.getStatements(testsubject, CHECKER_NAME, null);
                if (names.hasMoreElements()) {
                    id = ((Statement)names.nextElement()).Object.getStringValue();
                    try {
                        Object element = tables.getByName(id);
                        table = (XTextTable)UnoRuntime.queryInterface(XTextTable.class, element);
                        exists = true;
                    } catch (NoSuchElementException ex) {
                    }
                }
                break;

            case OBJECT:
                names = xRepository.getStatements(testsubject, CHECKER_NAME, null);
                if (names.hasMoreElements()) {
                    id = ((Statement)names.nextElement()).Object.getStringValue();
                    Object element = null;
                    try {
                        element = embeddedObjects.getByName(id);
                    } catch (NoSuchElementException ex) {
                        try {
                            element = graphicObjects.getByName(id);
                        } catch (NoSuchElementException e) {
                        }
                    } finally {
                        if (element != null) {
                            embeddedObject = (XNamed)UnoRuntime.queryInterface(XNamed.class, element);
                            exists = true;
                        }
                    }
                }
                break;

            default:
        }

        logger.exiting("Element", "<init>");
    }

    public XTextContent getParagraph() throws IllegalArgumentException {

        if (exists && type == Element.Type.PARAGRAPH) {
            return paragraph;
        }
        return null;
    }

    public XTextContent[] getSpan() throws IllegalArgumentException {

        if (exists && type == Type.SPAN) {
            return span;
        }
        return null;
    }

    public XTextTable getTable() throws NoSuchElementException,
                                        WrappedTargetException {

        if (exists && type == Type.TABLE) {
            return table;
        }
        return null;
    }

    public XNamed getObject() throws NoSuchElementException,
                                     WrappedTargetException {

        if (exists && type == Type.OBJECT) {
            return embeddedObject;
        }
        return null;
    }

    public boolean exists() {
        return exists;
    }

    public Type getType() {
        return type;
    }

    @Override
    public String toString() {

        switch (type) {

            case PARAGRAPH:
            case SPAN:
                return sample;
            case TABLE:
            case OBJECT:
                return id;
            case DOCUMENT:
            default:
                return "";
        }
    }

    @Override
    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = hash * PRIME + type.hashCode();
        hash = hash * PRIME + id.hashCode();
        hash = hash * PRIME + id2.hashCode();
        return hash;

    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Element that = (Element)obj;
        return (this.type == that.type &&
                this.id.equals(that.id) &&
                this.id2.equals(that.id2));
    }
}
