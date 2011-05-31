package be.docarch.accessibility.ooo;

import java.util.logging.Logger;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;

import com.sun.star.container.XNameAccess;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.text.XTextEmbeddedObjectsSupplier;
import com.sun.star.rdf.XDocumentMetadataAccess;
import com.sun.star.rdf.XRepository;

import com.sun.star.lang.IllegalArgumentException;

import be.docarch.accessibility.Constants;

/**
 *
 * @author Bert Frees
 */
public abstract class Element {

    protected static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    protected static XComponentContext xContext = null;
    protected static XDocumentMetadataAccess xDMA = null;
    protected static XRepository xRepository = null;
    protected static XNameAccess tables = null;
    protected static XNameAccess embeddedObjects = null;
    protected static XNameAccess graphicObjects = null;

    static void initialise(Document document)
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

    }

    public abstract boolean exists();

    @Override
    public abstract String toString();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);
}
