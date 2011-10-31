package be.docarch.accessibility.ooo;

import java.io.File;
import java.util.logging.Logger;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.AnyConverter;
import com.sun.star.util.XModifiable;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.frame.XComponentLoader;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XStorable;
import com.sun.star.frame.XDesktop;
import com.sun.star.document.XDocumentPropertiesSupplier;
import com.sun.star.document.XDocumentProperties;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XNameAccess;
import com.sun.star.container.XNameContainer;
import com.sun.star.container.XIndexAccess;
import com.sun.star.table.XCell;
import com.sun.star.text.XTextDocument;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextFramesSupplier;
import com.sun.star.text.XTextFrame;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextTablesSupplier;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.text.XTextEmbeddedObjectsSupplier;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.view.XSelectionSupplier;
import com.sun.star.style.XStyleFamiliesSupplier;
import com.sun.star.rdf.XDocumentMetadataAccess;
import com.sun.star.rdf.XRepository;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XMetadatable;

import java.io.IOException;
import java.net.MalformedURLException;

import com.sun.star.beans.PropertyVetoException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.datatransfer.UnsupportedFlavorException;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.ParseException;
import com.sun.star.rdf.RepositoryException;

import be.docarch.accessibility.Constants;

/**
 *
 * @author Bert Frees
 */
public class Document {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    public XComponentContext xContext = null;
    public XMultiServiceFactory xMSF = null;
    public XMultiComponentFactory xMCF = null;
    public XDocumentMetadataAccess xDMA = null;
    public XRepository xRepository = null;
    public XModel xModel = null;
    public XComponent doc = null;
    public XNameContainer paragraphStyles = null;
    public XNameContainer characterStyles = null;
    public XNameAccess tables = null;
    public XNameAccess embeddedObjects = null;
    public XNameAccess graphicObjects = null;
    public XSelectionSupplier selectionSupplier = null;
    public XTextViewCursor viewCursor = null;
    public XDocumentProperties docProperties = null;
    public XPropertySet docPropertySet = null;
    public XURI metaFolderURI = null;
    public XTextDocument textDocument = null;
    public String metaFolder = "meta/";

    private PropertyValue[] conversionProperties = null;
    private XStorable storable = null;
    private XModifiable xModifiable = null;    
    private XTextContent firstParagraph = null;
    private boolean readOnly = false;


    public Document(XComponentContext xContext)
             throws com.sun.star.uno.Exception {

        logger.entering("Document", "<init>");

        this.xContext = xContext;

        xMCF = (XMultiComponentFactory) UnoRuntime.queryInterface(
	        XMultiComponentFactory.class, xContext.getServiceManager());
        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
        doc = (XComponent)xDesktop.getCurrentComponent();

        initDocument();

        logger.exiting("Document", "<init>");

    }

    public Document(String unoUrl,
                    XComponentContext xContext)
             throws com.sun.star.uno.Exception {

        logger.entering("Document", "<init>");

        this.xContext = xContext;

        xMCF = (XMultiComponentFactory) UnoRuntime.queryInterface(
	        XMultiComponentFactory.class, xContext.getServiceManager());
        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        XComponentLoader loader = (XComponentLoader)UnoRuntime.queryInterface(
                                   XComponentLoader.class, desktop);
        PropertyValue[] loadProps = new PropertyValue[1];
        loadProps[0] = new PropertyValue();
        loadProps[0].Name = "ReadOnly";
        loadProps[0].Value = new Boolean(false);
        doc = loader.loadComponentFromURL(unoUrl, "_blank", 0, loadProps);

        initDocument();

        logger.exiting("Document", "<init>");

    }

    private void initDocument() throws com.sun.star.uno.Exception {

        URIs.init(xContext);

        textDocument = (XTextDocument)UnoRuntime.queryInterface(XTextDocument.class, doc);
        xMSF = (XMultiServiceFactory) UnoRuntime.queryInterface(XMultiServiceFactory.class, textDocument);
        xModel = (XModel) UnoRuntime.queryInterface(XModel.class, textDocument);
        xDMA = (XDocumentMetadataAccess)UnoRuntime.queryInterface(XDocumentMetadataAccess.class, xModel);
        xRepository = xDMA.getRDFRepository();
        metaFolderURI = URI.create(xContext, xDMA.getNamespace() + metaFolder);
        XStyleFamiliesSupplier xSupplier = (XStyleFamiliesSupplier)UnoRuntime.queryInterface(
                                            XStyleFamiliesSupplier.class, textDocument);
        XNameAccess xFamilies = (XNameAccess)UnoRuntime.queryInterface (
                                 XNameAccess.class, xSupplier.getStyleFamilies());
        paragraphStyles = (XNameContainer)UnoRuntime.queryInterface(
                           XNameContainer.class, xFamilies.getByName("ParagraphStyles"));
        characterStyles = (XNameContainer)UnoRuntime.queryInterface(
                           XNameContainer.class, xFamilies.getByName("CharacterStyles"));
        XTextTablesSupplier tablesSupplier =
            (XTextTablesSupplier)UnoRuntime.queryInterface(
                XTextTablesSupplier.class, doc);
        tables = tablesSupplier.getTextTables();
        XTextGraphicObjectsSupplier textGraphicObjectsSupplier =
            (XTextGraphicObjectsSupplier)UnoRuntime.queryInterface(
                XTextGraphicObjectsSupplier.class, doc);
        XTextEmbeddedObjectsSupplier textEmbeddedObjectsSupplier =
            (XTextEmbeddedObjectsSupplier)UnoRuntime.queryInterface(
                XTextEmbeddedObjectsSupplier.class, doc);
        embeddedObjects = textEmbeddedObjectsSupplier.getEmbeddedObjects();
        graphicObjects = textGraphicObjectsSupplier.getGraphicObjects();

        selectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(
                             XSelectionSupplier.class, xModel.getCurrentController());
        XTextViewCursorSupplier xViewCursorSupplier =
            (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                XTextViewCursorSupplier.class, xModel.getCurrentController());
        viewCursor = xViewCursorSupplier.getViewCursor();

        XDocumentPropertiesSupplier xDocPropSuppl = (XDocumentPropertiesSupplier) UnoRuntime.queryInterface(
						     XDocumentPropertiesSupplier.class, textDocument);
        docProperties = xDocPropSuppl.getDocumentProperties();
        docPropertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, doc);
        Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", xContext);
        XDesktop xDesktop = (XDesktop)UnoRuntime.queryInterface(XDesktop.class, desktop);
        xModifiable = (XModifiable)UnoRuntime.queryInterface(XModifiable.class, xDesktop.getCurrentComponent());

        conversionProperties = new PropertyValue[1];
        conversionProperties[0] = new PropertyValue();
        conversionProperties[0].Name = "FilterName";
        conversionProperties[0].Value = "writer8";
        storable = (XStorable) UnoRuntime.queryInterface(XStorable.class, xModel);

        for (PropertyValue prop: xModel.getArgs()) {
            if (prop.Name.equals("ReadOnly")) {
                readOnly = (Boolean)AnyConverter.toBoolean(prop.Value);
                break;
            }
        }
    }

    public void storeToFile(File output)
                     throws MalformedURLException,
                            com.sun.star.io.IOException {

        logger.entering("Document", "storeToFile");

        String outputUnoUrl = UnoUtils.createUnoFileURL(output.getAbsolutePath(), xContext);        
        storable.storeToURL(outputUnoUrl, conversionProperties);

        logger.exiting("Document", "storeToFile");

    }

    public void removeAccessibilityData(String checkerID) {
    
        try {

            XURI type = URI.create(xContext, checkerID);
            for (XURI graph : xDMA.getMetadataGraphsWithType(type)) {
                xDMA.removeMetadataFile(graph);
            }

        } catch (IllegalArgumentException e) {
        } catch (NoSuchElementException e) {
        }
    }

    public void importAccessibilityData(File earlReport,
                                        String checkerID,
                                        String graphName)
                                 throws IOException,
                                        IllegalArgumentException,
                                        UnsupportedFlavorException,
                                        ParseException,
                                        NoSuchElementException,
                                        WrappedTargetException,
                                        RepositoryException,
                                        com.sun.star.io.IOException {

        logger.entering("Document", "importAccessibilityData");

        InputStream inputStream = InputStream.newInstance(earlReport);

        XURI[] types = new XURI[]{ URI.create(xContext, checkerID) };
        XURI graphURI = null;
        try {
            graphURI = xDMA.importMetadataFile((short)0, inputStream, metaFolder + graphName, metaFolderURI, types);
        } catch (ElementExistException ex) {
            graphURI = URI.create(xContext, metaFolderURI.getStringValue() + graphName);
            xDMA.removeMetadataFile(graphURI);
            try {
                xDMA.importMetadataFile((short)0, inputStream, metaFolder + graphName, metaFolderURI, types);
            } catch (ElementExistException e) {
            }
        }

        logger.exiting("Document", "importAccessibilityData");

    }

    public void ensureMetadataReferences() throws NoSuchElementException,
                                                  WrappedTargetException,
                                                  IndexOutOfBoundsException {

        XEnumerationAccess enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(
                                                XEnumerationAccess.class, textDocument.getText());
        XEnumeration paragraphs = enumerationAccess.createEnumeration();
        while (paragraphs.hasMoreElements()) {
            ensureMetadataReferences(paragraphs.nextElement());
        }
        XTextFramesSupplier xTextFramesSupplier = (XTextFramesSupplier) UnoRuntime.queryInterface(
						   XTextFramesSupplier.class, doc);
        XIndexAccess textFrames = (XIndexAccess) UnoRuntime.queryInterface(
                                   XIndexAccess.class, xTextFramesSupplier.getTextFrames());
        XTextFrame frame = null;
        XEnumeration paragraphsInFrame = null;
        for (int i=0; i<textFrames.getCount(); i++) {
            frame = (XTextFrame)UnoRuntime.queryInterface(XTextFrame.class, textFrames.getByIndex(i));
            enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(
				 XEnumerationAccess.class, frame.getText());
            paragraphsInFrame = enumerationAccess.createEnumeration();
            while (paragraphsInFrame.hasMoreElements()) {
                ensureMetadataReferences(paragraphsInFrame.nextElement());
            }
        }
    }

    private void ensureMetadataReferences(Object element)
                                   throws NoSuchElementException,
                                          WrappedTargetException {

        XServiceInfo serviceInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, element);
        if (serviceInfo.supportsService("com.sun.star.text.TextTable")) {
            XTextTable table = (XTextTable)UnoRuntime.queryInterface(XTextTable.class, element);
            String[] cellNames = table.getCellNames();
            XEnumerationAccess enumerationAccess = null;
            XEnumeration paragraphsInTableCell = null;
            XCell tableCell = null;
            for (int i=0; i<cellNames.length; i++) {
                tableCell = table.getCellByName(cellNames[i]);
                enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(XEnumerationAccess.class, tableCell);
                paragraphsInTableCell = enumerationAccess.createEnumeration();
                while (paragraphsInTableCell.hasMoreElements()) {
                    ensureMetadataReferences(paragraphsInTableCell.nextElement());
                }
            }
        } else if (serviceInfo.supportsService("com.sun.star.text.Paragraph")) {
            XTextContent paragraph = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, element);
            XMetadatable xMetadatable = (XMetadatable)UnoRuntime.queryInterface(XMetadatable.class, paragraph);
            xMetadatable.ensureMetadataReference();
        }
    }

    public XTextContent getFirstParagraph() throws NoSuchElementException,
                                                   WrappedTargetException {

        try {
            firstParagraph.getAnchor();
        } catch (Exception ex) {
            XEnumerationAccess enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(
                                                    XEnumerationAccess.class, textDocument.getText());
            XEnumeration paragraphs = enumerationAccess.createEnumeration();
            while (paragraphs.hasMoreElements()) {
                Object element  = paragraphs.nextElement();
                XServiceInfo serviceInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, element);
                if (serviceInfo.supportsService("com.sun.star.text.Paragraph")) {
                    firstParagraph = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, element);
                    break;
                }
            }
        }
        
        return firstParagraph;
    }

    public void setModified() {

        try {
            xModifiable.setModified(true);
        } catch (PropertyVetoException e) {
            // read-only document
        }
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
