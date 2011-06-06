package be.docarch.accessibility.ooo;

import java.util.logging.Logger;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;

import com.sun.star.container.XNameAccess;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.text.XTextEmbeddedObjectsSupplier;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.rdf.XDocumentMetadataAccess;
import com.sun.star.rdf.XRepository;
import com.sun.star.view.XSelectionSupplier;

import com.sun.star.lang.IllegalArgumentException;

import be.docarch.accessibility.Constants;
import be.docarch.accessibility.Element;

/**
 *
 * @author Bert Frees
 */
public abstract class FocusableElement extends Element {

    protected static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    protected static XComponentContext xContext = null;
    protected static XDocumentMetadataAccess xDMA = null;
    protected static XRepository xRepository = null;
    protected static XNameAccess tables = null;
    protected static XNameAccess embeddedObjects = null;
    protected static XNameAccess graphicObjects = null;
    protected static XTextViewCursor viewCursor = null;
    protected static XSelectionSupplier selectionSupplier = null;

    static void initialise(Document document)
                    throws IllegalArgumentException {

        FocusableElement.xContext = document.xContext;
        FocusableElement.xDMA = document.xDMA;
        FocusableElement.xRepository = xDMA.getRDFRepository();

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

        selectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(
                             XSelectionSupplier.class, document.xModel.getCurrentController());
        XTextViewCursorSupplier xViewCursorSupplier =
            (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                XTextViewCursorSupplier.class, document.xModel.getCurrentController());
        viewCursor = xViewCursorSupplier.getViewCursor();

    }

    public abstract boolean focus();

}
