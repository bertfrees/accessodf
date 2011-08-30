package be.docarch.accessibility.ooo.rdf;

import com.sun.star.uno.XComponentContext;
import com.sun.star.rdf.XDocumentMetadataAccess;
import com.sun.star.rdf.XRepository;
import be.docarch.accessibility.ooo.Document;

/**
 *
 * @author Bert Frees
 */
public abstract class RDFClass {

    protected static Document document;
    protected static XComponentContext xContext;
    protected static XDocumentMetadataAccess xDMA;
    protected static XRepository xRepository;

    public static void initialize(Document document) {
        RDFClass.document = document;
        RDFClass.xContext = document.xContext;
        RDFClass.xDMA = document.xDMA;
        RDFClass.xRepository = xDMA.getRDFRepository();
    }
}
