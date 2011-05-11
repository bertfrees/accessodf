package be.docarch.accessibility.ooo;

import java.io.File;
import java.util.Date;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.text.SimpleDateFormat;

import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.Locale;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XEnumerationAccess;
import com.sun.star.container.XEnumeration;
import com.sun.star.container.XIndexAccess;
import com.sun.star.container.XNamed;
import com.sun.star.graphic.XGraphic;
import com.sun.star.text.XTextFramesSupplier;
import com.sun.star.text.XTextGraphicObjectsSupplier;
import com.sun.star.text.XTextEmbeddedObjectsSupplier;
import com.sun.star.text.XTextFrame;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XWordCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.table.XCell;
import com.sun.star.table.XTableRows;
import com.sun.star.table.XTableColumns;
import com.sun.star.awt.FontUnderline;
import com.sun.star.awt.FontSlant;
import com.sun.star.view.XSelectionSupplier;
import com.sun.star.style.ParagraphAdjust;
import com.sun.star.style.XStyle;
import com.sun.star.rdf.XMetadatable;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XNamedGraph;
import com.sun.star.rdf.XBlankNode;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.XNode;
import com.sun.star.rdf.Literal;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;

import be.docarch.accessibility.URIs;
import be.docarch.accessibility.Check;
import be.docarch.accessibility.Checker;


/**
 *
 * @author Bert Frees
 */
public class InternalChecker implements Checker {

    private Document document = null;
    private Settings settings = null;
    private TreeMap<String,Check> checks = null;
    private TreeMap<String,XURI> checkURIs = null;
    private XNamedGraph currentGraph = null;
    private XResource currentAssertor = null;
    private SimpleDateFormat dateFormat = null;
    private Date lastChecked = null;
    private ArrayList<XURI> metadata = null;
    private int numberOfTitles = 0;
    private int numberOfHeadings = 0;
    private XTextViewCursor viewCursor = null;
    private XPageCursor pageCursor = null;
    private boolean modified = false;

    private XURI CHECKER = null;
    private XURI CHECKER_CHECKS = null;
    private XURI CHECKER_DOCUMENT = null;
    private XURI CHECKER_PARAGRAPH = null;
    private XURI CHECKER_SPAN = null;
    private XURI CHECKER_TABLE = null;
    private XURI CHECKER_OBJECT = null;
    private XURI CHECKER_START = null;
    private XURI CHECKER_END = null;
    private XURI CHECKER_NAME = null;
    private XURI CHECKER_INDEX = null;
    private XURI CHECKER_LASTCHECKED = null;
    private XURI CHECKER_SAMPLE = null;
    private XURI RDF_TYPE = null;
    private XURI FOAF_GROUP = null;
    private XURI FOAF_MEMBER = null;
    private XURI FOAF_PERSON = null;
    private XURI FOAF_NAME = null;
    private XURI EARL_MAINASSERTOR = null;
    private XURI EARL_SOFTWARE = null;
    private XURI EARL_TESTSUBJECT = null;
    private XURI EARL_TESTRESULT = null;
    private XURI EARL_TESTCASE = null;
    private XURI EARL_ASSERTION = null;
    private XURI EARL_OUTCOME = null;
    private XURI EARL_FAILED = null;
    private XURI EARL_PASSED = null;
    private XURI EARL_RESULT = null;
    private XURI EARL_TEST = null;
    private XURI EARL_SUBJECT = null;
    private XURI EARL_ASSERTEDBY = null;

    public InternalChecker(Document document)
                    throws IllegalArgumentException {

        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        this.document = document;
        XComponentContext xContext = document.xContext;
        settings = new Settings(xContext);

        CHECKER = URI.create(xContext, URIs.CHECKER);
        CHECKER_CHECKS = URI.create(xContext, URIs.CHECKER_CHECKS);
        CHECKER_DOCUMENT = URI.create(xContext, URIs.CHECKER_DOCUMENT);
        CHECKER_PARAGRAPH = URI.create(xContext, URIs.CHECKER_PARAGRAPH);
        CHECKER_SPAN = URI.create(xContext, URIs.CHECKER_SPAN);
        CHECKER_TABLE = URI.create(xContext, URIs.CHECKER_TABLE);
        CHECKER_OBJECT = URI.create(xContext, URIs.CHECKER_OBJECT);
        CHECKER_START = URI.create(xContext, URIs.CHECKER_START);
        CHECKER_END = URI.create(xContext, URIs.CHECKER_END);
        CHECKER_NAME = URI.create(xContext, URIs.CHECKER_NAME);
        CHECKER_INDEX = URI.create(xContext, URIs.CHECKER_INDEX);
        CHECKER_LASTCHECKED = URI.create(xContext, URIs.CHECKER_LASTCHECKED);
        CHECKER_SAMPLE = URI.create(xContext, URIs.CHECKER_SAMPLE);
        RDF_TYPE = URI.create(xContext, URIs.RDF_TYPE);
        FOAF_GROUP = URI.create(xContext, URIs.FOAF_GROUP);
        FOAF_MEMBER = URI.create(xContext, URIs.FOAF_MEMBER);
        FOAF_PERSON = URI.create(xContext, URIs.FOAF_PERSON);
        FOAF_NAME = URI.create(xContext, URIs.FOAF_NAME);
        EARL_MAINASSERTOR = URI.create(xContext, URIs.EARL_MAINASSERTOR);
        EARL_SOFTWARE = URI.create(xContext, URIs.EARL_SOFTWARE);
        EARL_TESTSUBJECT = URI.create(xContext, URIs.EARL_TESTSUBJECT);
        EARL_TESTRESULT = URI.create(xContext, URIs.EARL_TESTRESULT);
        EARL_TESTCASE = URI.create(xContext, URIs.EARL_TESTCASE);
        EARL_ASSERTION = URI.create(xContext, URIs.EARL_ASSERTION);
        EARL_OUTCOME = URI.create(xContext, URIs.EARL_OUTCOME);
        EARL_RESULT = URI.create(xContext, URIs.EARL_RESULT);
        EARL_TEST = URI.create(xContext, URIs.EARL_TEST);
        EARL_SUBJECT = URI.create(xContext, URIs.EARL_SUBJECT);
        EARL_ASSERTEDBY = URI.create(xContext, URIs.EARL_ASSERTEDBY);
        EARL_FAILED = URI.create(xContext, URIs.EARL_FAILED);
        EARL_PASSED = URI.create(xContext, URIs.EARL_PASSED);

        checkURIs = new TreeMap<String,XURI>();
        checks = new TreeMap<String,Check>();

        for (GeneralCheck.ID id : GeneralCheck.ID.values()) {
            checks.put(id.name(), new GeneralCheck(id));
            checkURIs.put(id.name(), URI.createNS(xContext, URIs.CHECKER_CHECKS, id.name()));
        }

        for (DaisyCheck.ID id : DaisyCheck.ID.values()) {
            checks.put(id.name(), new DaisyCheck(id));
            checkURIs.put(id.name(), URI.createNS(xContext, URIs.CHECKER_CHECKS, id.name()));
        }
    }

    public Collection<Check> getChecks() {
        return checks.values();
    }

    public Check getCheck(String identifier) {
        return (checks.get(identifier));
    }

    public File getAccessibilityReport() {
        return null;
    }

    public Date getLastChecked() {

        try {
            if (lastChecked != null) {
                return dateFormat.parse(dateFormat.format(lastChecked));
            } else {
                return null;
            }
        } catch (java.text.ParseException ex) {
            return null;
        }
    }

    public void check(){

        try {

            settings.loadData();
            lastChecked = new Date();
            String graphName = getIdentifier() + "/" + new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(lastChecked) + ".rdf";
            XURI graphURI = null;
            try {
                graphURI = document.xDMA.addMetadataFile(document.metaFolder + graphName, new XURI[]{ CHECKER });
            } catch (ElementExistException ex) {
                graphURI = URI.create(document.xContext, document.metaFolderURI.getStringValue() + graphName);
            }

            currentGraph = document.xRepository.getGraph(graphURI);

            // EARL assertor & testcases

            currentAssertor = document.xRepository.createBlankNode();
            XURI software = URI.create(document.xContext, URIs.CHECKER);
            XURI bert = URI.create(document.xContext, URIs.BERT);

            addStatement(bert, RDF_TYPE, FOAF_PERSON);
            addStatement(bert, FOAF_NAME, Literal.create(document.xContext, "Bert Frees"));
            addStatement(software, RDF_TYPE, EARL_SOFTWARE);
            addStatement(currentAssertor, RDF_TYPE, FOAF_GROUP);
            addStatement(currentAssertor, EARL_MAINASSERTOR, software);
            addStatement(currentAssertor, FOAF_MEMBER, bert);

            for (String id : checkURIs.keySet()) {
                addStatement(checkURIs.get(id), RDF_TYPE, EARL_TESTCASE);
            }

            // Traverse document
            traverseDocument();

        } catch (IllegalArgumentException ex) {
        } catch (RepositoryException ex) {
        } catch (UnknownPropertyException ex) {
        } catch (NoSuchElementException ex) {
        } catch (WrappedTargetException ex) {
        } catch (com.sun.star.uno.Exception ex) {
        }
    }

    private void traverseDocument() throws IllegalArgumentException,
                                           WrappedTargetException,
                                           RepositoryException,
                                           NoSuchElementException,
                                           UnknownPropertyException,
                                           com.sun.star.uno.Exception {

        Locale docLocale = (Locale)AnyConverter.toObject(Locale.class, document.docPropertySet.getPropertyValue("CharLocale"));
        XSelectionSupplier selectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(
                                                XSelectionSupplier.class, document.xModel.getCurrentController());
        selectionSupplier.select(document.getFirstParagraph().getAnchor());
        XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                       XTextViewCursorSupplier.class, document.xModel.getCurrentController());
        viewCursor = xViewCursorSupplier.getViewCursor();
        pageCursor = (XPageCursor)UnoRuntime.queryInterface(XPageCursor.class, viewCursor);

        metadata = new ArrayList<XURI>();
        numberOfTitles = 0;
        numberOfHeadings = 0;

        // Traverse all toplevel paragraphs and tables
        XEnumerationAccess enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(
						XEnumerationAccess.class, document.textDocument.getText());
        XEnumeration paragraphs = enumerationAccess.createEnumeration();
        traverseParagraphs(paragraphs, false, false);

        // Traverse all text frames
        XTextFramesSupplier xTextFramesSupplier = (XTextFramesSupplier) UnoRuntime.queryInterface(
						   XTextFramesSupplier.class, document.doc);
        XIndexAccess textFrames = (XIndexAccess) UnoRuntime.queryInterface(
                                   XIndexAccess.class, xTextFramesSupplier.getTextFrames());
        traverseTextFrames(textFrames);

        // Traverse all graphic objects
        XTextGraphicObjectsSupplier xTextGraphicObjectsSupplier = (XTextGraphicObjectsSupplier) UnoRuntime.queryInterface(
							           XTextGraphicObjectsSupplier.class, document.doc);
        XIndexAccess graphicObjects = (XIndexAccess) UnoRuntime.queryInterface(
                                       XIndexAccess.class, xTextGraphicObjectsSupplier.getGraphicObjects());
        traverseGraphicObjects(graphicObjects);

        // Traverse all other embedded objects
        XTextEmbeddedObjectsSupplier xTextEmbeddedObjectsSupplier = (XTextEmbeddedObjectsSupplier) UnoRuntime.queryInterface(
							             XTextEmbeddedObjectsSupplier.class, document.doc);
        XIndexAccess embeddedObjects = (XIndexAccess) UnoRuntime.queryInterface(
                                        XIndexAccess.class, xTextEmbeddedObjectsSupplier.getEmbeddedObjects());
        traverseEmbeddedObjects(embeddedObjects);

        // Attach general warnings to first paragraph
        if (document.docProperties.getTitle().length() == 0) { metadata.add(checkURIs.get(GeneralCheck.ID.A_EmptyTitleField.name())); }
        if (docLocale.Language.equals("zxx")) { metadata.add(checkURIs.get(GeneralCheck.ID.E_DefaultLanguage.name())); }
        if (numberOfTitles == 0)   { metadata.add(checkURIs.get(GeneralCheck.ID.A_NoTitle.name())); }
        if (numberOfHeadings == 0) { metadata.add(checkURIs.get(GeneralCheck.ID.A_NoHeadings.name())); }
        if (metadata.size() > 0) {
            addMetadataToDocument(metadata);
        }

        if (modified) {
            document.setModified();
            modified = false;
        }
    }

    private int[] traverseParagraphs(XEnumeration paragraphs,
                                     boolean inFrame,
                                     boolean inTable)
                              throws UnknownPropertyException,
                                     WrappedTargetException,
                                     IllegalArgumentException,
                                     NoSuchElementException,
                                     RepositoryException,
                                     com.sun.star.uno.Exception {

        Object element = null;
        XServiceInfo serviceInfo = null;
        XTextContent textContent = null;
        XTextTable textTable = null;
        XTextRange textRange = null;
        XPropertySet properties = null;
        String text = null;
        short newLevel = (short)0;
        short currentLevel = (short)0;
        String styleName = null;
        int alignment = 0;
        boolean afterTitle = false;
        boolean titleCentered = false;
        boolean alternateLevel = false;
        boolean bigTable = false;
        boolean keepWithNext = false;
        XTextContent caption = null;
        boolean captionAfterTable = false;
        boolean afterTable = false;
        boolean keepWithTableBefore = false;
        int tablePages = 0;

        while (paragraphs.hasMoreElements()) {

            element = paragraphs.nextElement();
            serviceInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, element);
            textContent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, element);
            properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, serviceInfo);

            if (serviceInfo.supportsService("com.sun.star.text.TextTable")) {

                textTable = (XTextTable)UnoRuntime.queryInterface(XTextTable.class, element);
                tablePages = traverseTextTable(textTable, inFrame, inTable);
                keepWithTableBefore = AnyConverter.toBoolean(properties.getPropertyValue("KeepTogether"));
                bigTable = (tablePages > 1);
                afterTable = (caption == null);
                caption = null;

            } else if (serviceInfo.supportsService("com.sun.star.text.Paragraph")) {

                textRange = textContent.getAnchor();
                text = textRange.getString();

                keepWithNext = AnyConverter.toBoolean(properties.getPropertyValue("ParaKeepTogether"));
                newLevel = AnyConverter.toShort(properties.getPropertyValue("OutlineLevel"));
                alignment = AnyConverter.toInt(properties.getPropertyValue("ParaAdjust"));
                styleName = AnyConverter.toString(properties.getPropertyValue("ParaStyleName"));

                if (caption != null && captionAfterTable && bigTable) {
                    ArrayList<XURI> meta = new ArrayList<XURI>();
                    meta.add(checkURIs.get(GeneralCheck.ID.A_CaptionBelowBigTable.name()));
                    addMetadataToParagraph(caption, meta);
                }

                caption = null;
                String style = styleName;
                do {
                    if (style.equals("Table")) {
                        caption = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, element);
                        break;
                    }
                    style = ((XStyle)UnoRuntime.queryInterface(
                              XStyle.class, document.paragraphStyles.getByName(style))).getParentStyle();
                } while (style.length() > 0);

                if (text.length() > 0) {
                    if (alignment == ParagraphAdjust.BLOCK_value ||
                        alignment == ParagraphAdjust.STRETCH_value) {
                        metadata.add(checkURIs.get(GeneralCheck.ID.A_JustifiedText.name()));
                    }
                }
                if (!inFrame && styleName.equals("Title")) {
                    if (text.length() == 0) {
                        metadata.add(checkURIs.get(GeneralCheck.ID.E_EmptyTitle.name()));
                    } else {
                        numberOfTitles++;
                        if (numberOfTitles > 1) {
                            metadata.add(checkURIs.get(GeneralCheck.ID.E_ManyTitles.name()));
                        }
                    }
                } else if (inFrame && newLevel > 0) {
                    metadata.add(checkURIs.get(GeneralCheck.ID.E_HeadingInFrame.name()));
                    currentLevel = newLevel;
                } else if (newLevel > 0 && text.length() == 0) {
                    metadata.add(checkURIs.get(GeneralCheck.ID.E_EmptyHeading.name()));
                } else if (newLevel > currentLevel + 1) {
                    metadata.add(checkURIs.get(GeneralCheck.ID.E_HeadingSkip.name()));
                    numberOfHeadings ++;
                    currentLevel = newLevel;
                } else if (!alternateLevel && newLevel > 6) {
                    alternateLevel = true;
                    metadata.add(checkURIs.get(GeneralCheck.ID.A_AlternateLevel.name()));
                    numberOfHeadings ++;
                    currentLevel = newLevel;
                } else if (newLevel > 0) {
                    numberOfHeadings ++;
                    currentLevel = newLevel;
                }

                if (text.length() > 0) {

                    if (caption != null && afterTable && bigTable) {
                        if (keepWithTableBefore || !keepWithNext) {
                            ArrayList<XURI> meta = new ArrayList<XURI>();
                            meta.add(checkURIs.get(GeneralCheck.ID.A_CaptionBelowBigTable.name()));
                            addMetadataToParagraph(caption, meta);
                            caption = null;
                        } else {
                            captionAfterTable = true;
                        }
                    } else {
                        captionAfterTable = false;
                    }

                    boolean fakeSubtitle = newLevel == 0 &&
                                           !styleName.equals("Title") &&
                                           !styleName.equals("Subtitle") &&
                                           afterTitle &&
                                           alignment == ParagraphAdjust.CENTER_value &&
                                           titleCentered;
                    if (fakeSubtitle) {
                        metadata.add(checkURIs.get(GeneralCheck.ID.A_NoSubtitle.name()));
                    }

                    XEnumerationAccess enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(
                                                            XEnumerationAccess.class, textContent);
                    XEnumeration textPortions = enumerationAccess.createEnumeration();
                    
                    boolean checkFakeSubtitle = newLevel == 0 &&
                                                !styleName.equals("Title") &&
                                                !styleName.equals("Subtitle") &&
                                                afterTitle &&
                                                alignment != ParagraphAdjust.CENTER_value &&
                                                !titleCentered;
                    boolean checkFakeHeading = newLevel == 0 &&
                                               !styleName.equals("Title") &&
                                               !styleName.equals("Subtitle") &&
                                               !checkFakeSubtitle &&
                                               !inTable;
                    traverseTextPortions(textPortions, checkFakeSubtitle, checkFakeHeading);

                    if (styleName.equals("Title")) {
                        afterTitle = true;
                        titleCentered = (alignment == ParagraphAdjust.CENTER_value);
                    } else if (afterTitle) {
                        afterTitle = false;
                    }
                }

                if (metadata.size() > 0) {
                    addMetadataToParagraph(textContent, metadata);
                }

                afterTable = false;
            }
        }

        if (caption != null && captionAfterTable) {
            ArrayList<XURI> meta = new ArrayList<XURI>();
            meta.add(checkURIs.get(GeneralCheck.ID.A_CaptionBelowBigTable.name()));
            addMetadataToParagraph(caption, meta);
        }

        XTextCursor cursor = textRange.getText().createTextCursorByRange(textRange);
        cursor.gotoStart(false);
        //traverseWords((XWordCursor)UnoRuntime.queryInterface(XWordCursor.class, cursor));

        int[] pageRange = new int[2];
        if (inTable) {
            viewCursor.gotoRange(cursor, false);
            pageRange[0] = pageCursor.getPage();
            cursor.gotoEnd(false);
            viewCursor.gotoRange(cursor, false);
            pageRange[1] = pageCursor.getPage();
        }

        return pageRange;
    }

    private void traverseTextPortions(XEnumeration textPortions,
                                      boolean checkNoSubtitle,
                                      boolean checkFakeHeading)
                               throws UnknownPropertyException,
                                      WrappedTargetException,
                                      IllegalArgumentException,
                                      RepositoryException,
                                      NoSuchElementException,
                                      com.sun.star.uno.Exception {

        Object element = null;
        XPropertySet properties = null;
        XTextRange textRange = null;
        String text = null;
        String textPortionType = null;
        float charHeight = 0;
        float charWeight = 0;
        short underline = 0;
        FontSlant italic = null;
        XTextRange startRange = null;
        XTextRange endRange = null;
        XTextRange startSmallText = null;
        XTextRange startUnderline = null;
        XTextRange startItalic = null;
        String smallTextSample = "";
        String underlineSample = "";
        String italicSample = "";
        int underlineLength = 0;
        int italicLength = 0;
        int maxUnderlineLength = 250;
        int maxItalicLength = 250;
        boolean allBig = true;
        boolean allBold = true;
        XTextContent textMeta = null;
        Map<XTextRange,XTextContent> range2textMeta = new HashMap<XTextRange,XTextContent>();
        ArrayList<XTextRange[]> smallTextRanges = new ArrayList<XTextRange[]>();
        ArrayList<XTextRange[]> longUnderlineRanges = new ArrayList<XTextRange[]>();
        ArrayList<XTextRange[]> longItalicRanges = new ArrayList<XTextRange[]>();
        ArrayList<String> smallTextSamples = new ArrayList<String>();
        ArrayList<String> longUnderlineSamples = new ArrayList<String>();
        ArrayList<String> longItalicSamples = new ArrayList<String>();

        while (textPortions.hasMoreElements()) {

            element = textPortions.nextElement();
            textRange = (XTextRange)UnoRuntime.queryInterface(XTextRange.class, element);
            properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, textRange);
            textPortionType = AnyConverter.toString(properties.getPropertyValue("TextPortionType"));

            if (textPortionType.equals("InContentMetadata")) {

                if (textMeta==null && endRange!=null) {
                    textMeta = (XTextContent)AnyConverter.toObject(
                                XTextContent.class, properties.getPropertyValue("InContentMetadata"));
                    range2textMeta.put(endRange, textMeta);
                } else {
                    textMeta = (XTextContent)AnyConverter.toObject(
                                XTextContent.class, properties.getPropertyValue("InContentMetadata"));
                }

            } else if (textPortionType.equals("SoftPageBreak")) {

            } else if (textPortionType.equals("Text")) {

                text = textRange.getString();
                charHeight = AnyConverter.toFloat(properties.getPropertyValue("CharHeight"));
                charWeight = AnyConverter.toFloat(properties.getPropertyValue("CharWeight"));
                underline = AnyConverter.toShort(properties.getPropertyValue("CharUnderline"));
                italic = (FontSlant)AnyConverter.toObject(FontSlant.class, properties.getPropertyValue("CharPosture"));

                startRange = textRange.getStart();

                if (text.length() > 0) {

                    range2textMeta.put(startRange, textMeta);
                    textMeta = null;

                    if (charHeight < 10) {
                        if (startSmallText == null) {
                            startSmallText = startRange;
                            smallTextSample = "";
                        }
                        smallTextSample += text;
                    } else {
                        if (startSmallText != null) {
                            smallTextRanges.add(new XTextRange[]{startSmallText, endRange});
                            smallTextSamples.add(smallTextSample);
                        }
                        startSmallText = null;
                    }
                    if (underline != FontUnderline.NONE) {
                        if (startUnderline == null) {
                            startUnderline = startRange;
                            underlineLength = 0;
                            underlineSample = "";
                        }
                        underlineLength += text.length();
                        underlineSample += text;
                    } else {
                        if (startUnderline != null && underlineLength > maxUnderlineLength) {
                            longUnderlineRanges.add(new XTextRange[]{startUnderline, endRange});
                            longUnderlineSamples.add(underlineSample);
                        }
                        startUnderline = null;
                    }
                    if (italic == FontSlant.OBLIQUE ||
                        italic == FontSlant.ITALIC ||
                        italic == FontSlant.REVERSE_OBLIQUE ||
                        italic == FontSlant.REVERSE_ITALIC) {
                        if (startItalic == null) {
                            startItalic = startRange;
                            italicLength = 0;
                            italicSample = "";
                        }
                        italicLength += text.length();
                        italicSample += text;
                    } else {
                        if (startItalic != null && italicLength > maxItalicLength) {
                            longItalicRanges.add(new XTextRange[]{startItalic, endRange});
                            longItalicSamples.add(italicSample);
                        }
                        startItalic = null;
                    }

                    allBig = allBig && (charHeight > 13);
                    allBold = allBold && (charWeight > 100);
                }

                endRange = textRange.getEnd();
            }
        }

        if (startSmallText != null) {
            smallTextRanges.add(new XTextRange[]{startSmallText, endRange});
            smallTextSamples.add(smallTextSample);
        }
        if (startUnderline != null && underlineLength > maxUnderlineLength) {
            longUnderlineRanges.add(new XTextRange[]{startUnderline, endRange});
            longUnderlineSamples.add(underlineSample);
        }
        if (startItalic != null && italicLength > maxItalicLength) {
            longItalicRanges.add(new XTextRange[]{startItalic, endRange});
            longItalicSamples.add(italicSample);
        }

        XTextRange start = null;
        XTextRange end = null;
        XTextContent startTextMeta = null;
        XTextContent endTextMeta = null;
        for (int i=0; i<smallTextRanges.size(); i++) {
            start = smallTextRanges.get(i)[0];
            end = smallTextRanges.get(i)[1];
            startTextMeta = range2textMeta.get(start);
            endTextMeta = range2textMeta.get(end);
            if (startTextMeta == null) {
                startTextMeta = (XTextContent)UnoRuntime.queryInterface(
                                 XTextContent.class, document.xMSF.createInstance("com.sun.star.text.InContentMetadata"));
                start.getText().insertTextContent(start, startTextMeta, true);
            }
            if (endTextMeta == null) {
                endTextMeta = (XTextContent)UnoRuntime.queryInterface(
                               XTextContent.class, document.xMSF.createInstance("com.sun.star.text.InContentMetadata"));
                end.getText().insertTextContent(end, endTextMeta, true);
            }
            addMetadataToSpan(startTextMeta,
                              endTextMeta,
                              checkURIs.get(GeneralCheck.ID.A_SmallText.name()),
                              smallTextSamples.get(i));
        }
        for (int i=0; i<longUnderlineRanges.size(); i++) {
            start = longUnderlineRanges.get(i)[0];
            end = longUnderlineRanges.get(i)[1];
            startTextMeta = range2textMeta.get(start);
            endTextMeta = range2textMeta.get(end);
            if (startTextMeta == null) {
                startTextMeta = (XTextContent)UnoRuntime.queryInterface(
                                 XTextContent.class, document.xMSF.createInstance("com.sun.star.text.InContentMetadata"));
                start.getText().insertTextContent(start, startTextMeta, true);
            }
            if (endTextMeta == null) {
                endTextMeta = (XTextContent)UnoRuntime.queryInterface(
                               XTextContent.class, document.xMSF.createInstance("com.sun.star.text.InContentMetadata"));
                end.getText().insertTextContent(end, endTextMeta, true);
            }
            addMetadataToSpan(startTextMeta,
                              endTextMeta,
                              checkURIs.get(GeneralCheck.ID.A_LongUnderline.name()),
                              longUnderlineSamples.get(i));
        }
        for (int i=0; i<longItalicRanges.size(); i++) {
            start = longItalicRanges.get(i)[0];
            end = longItalicRanges.get(i)[1];
            startTextMeta = range2textMeta.get(start);
            endTextMeta = range2textMeta.get(end);
            if (startTextMeta == null) {
                startTextMeta = (XTextContent)UnoRuntime.queryInterface(
                                 XTextContent.class, document.xMSF.createInstance("com.sun.star.text.InContentMetadata"));
                start.getText().insertTextContent(start, startTextMeta, true);
            }
            if (endTextMeta == null) {
                endTextMeta = (XTextContent)UnoRuntime.queryInterface(
                               XTextContent.class, document.xMSF.createInstance("com.sun.star.text.InContentMetadata"));
                end.getText().insertTextContent(end, endTextMeta, true);
            }
            addMetadataToSpan(startTextMeta,
                              endTextMeta,
                              checkURIs.get(GeneralCheck.ID.A_LongItalic.name()),
                              longItalicSamples.get(i));
        }
        if (checkNoSubtitle && (allBig || allBold)) {
            metadata.add(checkURIs.get(GeneralCheck.ID.A_NoSubtitle.name()));
        }
        if (checkFakeHeading && (allBig || allBold)) {
            metadata.add(checkURIs.get(GeneralCheck.ID.A_FakeHeading.name()));
        }
    }

    private void traverseWords(XWordCursor cursor)
                        throws IllegalArgumentException,
                               RepositoryException,
                               NoSuchElementException,
                               com.sun.star.uno.Exception {

        String word = null;
        XTextRange startRange = null;
        XTextRange startCaps = null;
        XTextRange endCaps = null;
        String capsSample = "";
        int numberOfWordsInCaps = 0;

        do {
            cursor.gotoEndOfWord(true);
            startRange = cursor.getStart();
            word = cursor.getString();

            if (word.length() > 0) {
                if (word.equals(word.toUpperCase()) &&
                   !word.matches("[0-9]+")) {
                    if (startCaps == null) {
                        startCaps = startRange;
                        numberOfWordsInCaps = 0;
                        capsSample = "";
                    }
                    numberOfWordsInCaps++;
                    capsSample += (word + " ");
                    endCaps = cursor.getEnd();
                } else {
                    if (startCaps != null && numberOfWordsInCaps >= 3) {
                        //addMetadataToSpan(startCaps, endCaps, checkURIs.get(GeneralCheck.ID.A_AllCaps), capsSample);
                    }
                    startCaps = null;
                }
            }

        } while (cursor.gotoNextWord(false));

        if (startCaps != null && numberOfWordsInCaps >= 3) {
            //addMetadataToSpan(startCaps, endCaps, checkURIs.get(GeneralCheck.ID.A_AllCaps), capsSample);
        }
    }

    private int traverseTextTable(XTextTable table,
                                  boolean inFrame,
                                  boolean inTable)
                           throws UnknownPropertyException,
                                  IllegalArgumentException,
                                  RepositoryException,
                                  IndexOutOfBoundsException,
                                  NoSuchElementException,
                                  WrappedTargetException,
                                  com.sun.star.uno.Exception {

        XPropertySet properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, table);
        XPropertySet rowProperties = null;
        XCell tableCell = null;
        XEnumerationAccess enumerationAccess = null;
        XEnumeration paragraphsInTableCell = null;
        boolean repeatTableHeading = AnyConverter.toBoolean(properties.getPropertyValue("RepeatHeadline"));
        boolean keepTableTogether = !AnyConverter.toBoolean(properties.getPropertyValue("Split"));
        boolean keepTableRowsTogether = true;
        int[] pageRange = null;
        int[] cellPageRange = null;
        String[] cellNames = table.getCellNames();
        XTableRows tableRows = table.getRows();
        XTableColumns tableColumns = table.getColumns();

        for (int i=0; i<tableRows.getCount(); i++) {
            rowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, tableRows.getByIndex(i));
            if (rowProperties.getPropertySetInfo().hasPropertyByName("IsSplitAllowed")) {
                if (AnyConverter.toBoolean(rowProperties.getPropertyValue("IsSplitAllowed"))) {
                    keepTableRowsTogether = false;
                    break;
                }
            }
	}

	for (int i=0; i<cellNames.length; i++) {
            tableCell = table.getCellByName(cellNames[i]);
            enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(XEnumerationAccess.class, tableCell);
            paragraphsInTableCell = enumerationAccess.createEnumeration();
            cellPageRange = traverseParagraphs(paragraphsInTableCell, inFrame, true);
            if (pageRange == null) {
                pageRange = cellPageRange;
            } else {
                pageRange[0] = Math.min(pageRange[0], cellPageRange[0]);
                pageRange[1] = Math.max(pageRange[1], cellPageRange[1]);
            }
	}

        if (inTable) {
            metadata.add(checkURIs.get(GeneralCheck.ID.A_NestedTable.name()));
        }
        if (!repeatTableHeading) {
            metadata.add(checkURIs.get(GeneralCheck.ID.A_NoTableHeading.name()));
        }
        if (!keepTableRowsTogether) {
            metadata.add(checkURIs.get(GeneralCheck.ID.A_BreakRows.name()));
        }
        if (pageRange[1] > pageRange[0] + 1) {
            metadata.add(checkURIs.get(GeneralCheck.ID.A_BigTable.name()));
        }
        if (tableRows.getCount() * tableColumns.getCount() != cellNames.length) {    // Dit is geen waterdichte oplossing !
            metadata.add(checkURIs.get(GeneralCheck.ID.A_MergedCells.name()));
        }

        if (metadata.size() > 0) {
            addMetadataToTable(table, metadata);
        }

        return pageRange[1] - pageRange[0] + 1;
    }

    private void traverseTextFrames(XIndexAccess textFrames)
                             throws IndexOutOfBoundsException,
                                    UnknownPropertyException,
                                    WrappedTargetException,
                                    IllegalArgumentException,
                                    com.sun.star.uno.Exception {

        XTextFrame textFrame = null;

        for (int i=0; i<textFrames.getCount(); i++) {
            textFrame = (XTextFrame)UnoRuntime.queryInterface(XTextFrame.class, textFrames.getByIndex(i));
            XEnumerationAccess enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(
						    XEnumerationAccess.class, textFrame.getText());
            XEnumeration paragraphsInFrame = enumerationAccess.createEnumeration();
            traverseParagraphs(paragraphsInFrame, true, false);
        }
    }

    private void traverseGraphicObjects(XIndexAccess graphicObjects)
                                 throws IndexOutOfBoundsException,
                                        UnknownPropertyException,
                                        WrappedTargetException,
                                        RepositoryException,
                                        IllegalArgumentException,
                                        NoSuchElementException,
                                        com.sun.star.uno.Exception {

        Object graphicObject = null;
        XPropertySet properties = null;
        XMetadatable xMetadatable = null;
        XTextContent textContent = null;
        XTextCursor cursor = null;
        XGraphic graphic = null;
        XPropertySet mediaProperties = null;
        String title = null;
        String description = null;
        String url = null;
        String fileExtension = null;
        String mimeType = null;

        ArrayList<XURI> graphicMetadata = new ArrayList<XURI>();

        for (int i=0; i<graphicObjects.getCount(); i++) {

            graphicObject = graphicObjects.getByIndex(i);
            properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, graphicObject);

            title = AnyConverter.toString(properties.getPropertyValue("Title"));
            description = AnyConverter.toString(properties.getPropertyValue("Description"));
            url = AnyConverter.toString(properties.getPropertyValue("GraphicURL"));

            if (title.length() + description.length() == 0) {
                graphicMetadata.add(checkURIs.get(GeneralCheck.ID.A_ImageWithoutAlt.name()));
            }
            if (url.startsWith("vnd.sun.star.GraphicObject:")) {
                if (settings.daisyChecksAvailable()) {
                    graphic = (XGraphic)AnyConverter.toObject(
                               XGraphic.class, properties.getPropertyValue("Graphic"));
                    mediaProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, graphic);
                    mimeType = AnyConverter.toString(mediaProperties.getPropertyValue("MimeType"));
                    if (!mimeType.equals("image/jpeg") &&
                        !mimeType.equals("image/png")) {
                        graphicMetadata.add(checkURIs.get(DaisyCheck.ID.E_UnsupportedImageFormat.name()));
                    }
                }
            } else {
                graphicMetadata.add(checkURIs.get(GeneralCheck.ID.A_LinkedImage.name()));
                fileExtension = url.substring(url.lastIndexOf(".") + 1);
                if (settings.daisyChecksAvailable() &&
                    !fileExtension.equals("png") &&
                    !fileExtension.equals("jpg")) {
                    graphicMetadata.add(checkURIs.get(DaisyCheck.ID.E_UnsupportedImageFormat.name()));
                }
            }

            if (graphicMetadata.size() > 0) {
                addMetadataToGraphic(graphicObject, graphicMetadata);
            }
        }
    }

    private void traverseEmbeddedObjects(XIndexAccess embeddedObjects)
                                  throws IndexOutOfBoundsException,
                                         UnknownPropertyException,
                                         WrappedTargetException,
                                         RepositoryException,
                                         IllegalArgumentException,
                                         NoSuchElementException,
                                         com.sun.star.uno.Exception {

        Object embeddedObject = null;
        XPropertySet properties = null;
        String title = null;
        String description = null;

        for (int i=0; i<embeddedObjects.getCount(); i++) {

            embeddedObject = embeddedObjects.getByIndex(i);
            properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, embeddedObject);
            title = AnyConverter.toString(properties.getPropertyValue("Title"));
            description = AnyConverter.toString(properties.getPropertyValue("Description"));

            if (title.length() + description.length() == 0) {
                if (AnyConverter.toString(properties.getPropertyValue("CLSID")).equals("078B7ABA-54FC-457F-8551-6147e776a997")) {
                    addMetadataToObject(embeddedObject, checkURIs.get(GeneralCheck.ID.A_FormulaWithoutAlt.name()));
                } else {
                    addMetadataToObject(embeddedObject, checkURIs.get(GeneralCheck.ID.A_ObjectWithoutAlt.name()));
                }
            }
        }
    }

    private void addMetadataToDocument(ArrayList<XURI> data)
                                throws IllegalArgumentException,
                                       NoSuchElementException,
                                       RepositoryException {

        XBlankNode subject = document.xRepository.createBlankNode();

        addStatement(subject, RDF_TYPE, EARL_TESTSUBJECT);
        addStatement(subject, RDF_TYPE, CHECKER_DOCUMENT);

        for (XURI check : data) {
            addAssertion(subject, check);
        }

        data.clear();
    }

    private void addMetadataToParagraph(XTextContent paragraph,
                                        ArrayList<XURI> data)
                                 throws IllegalArgumentException,
                                        RepositoryException,
                                        NoSuchElementException {

        XMetadatable xMetadatable = (XMetadatable)UnoRuntime.queryInterface(XMetadatable.class, paragraph);
        xMetadatable.ensureMetadataReference();

        XBlankNode subject = document.xRepository.createBlankNode();

        addStatement(subject, RDF_TYPE, EARL_TESTSUBJECT);
        addStatement(subject, RDF_TYPE, CHECKER_PARAGRAPH);
        addStatement(subject, CHECKER_START, xMetadatable);

        for (XURI check : data) {
            addAssertion(subject, check);
        }

        data.clear();
    }

    private void addMetadataToSpan(XTextContent start,
                                   XTextContent end,
                                   XURI data,
                                   String sample)
                            throws IllegalArgumentException,
                                   NoSuchElementException,
                                   RepositoryException,
                                   com.sun.star.uno.Exception {

        XMetadatable startMetadatable = (XMetadatable)UnoRuntime.queryInterface(
                                         XMetadatable.class, start);
        XMetadatable endMetadatable = (XMetadatable)UnoRuntime.queryInterface(
                                       XMetadatable.class, end);
        startMetadatable.ensureMetadataReference();
        endMetadatable.ensureMetadataReference();

        if (sample.length() > 30) {
            sample = sample.substring(0, 30) + "\u2026";
        }

        XBlankNode subject = document.xRepository.createBlankNode();

        addStatement(subject, RDF_TYPE, EARL_TESTSUBJECT);
        addStatement(subject, RDF_TYPE, CHECKER_SPAN);
        addStatement(subject, CHECKER_START, startMetadatable);
        addStatement(subject, CHECKER_END, endMetadatable);
        addStatement(subject, CHECKER_SAMPLE, Literal.create(document.xContext, sample));

        addAssertion(subject, data);
    }

    private void addMetadataToTable(XTextTable table,
                                    ArrayList<XURI> data)
                             throws IllegalArgumentException,
                                    NoSuchElementException,
                                    RepositoryException {

        XNamed namedTable = (XNamed)UnoRuntime.queryInterface(XNamed.class, table);

        XBlankNode subject = document.xRepository.createBlankNode();

        addStatement(subject, RDF_TYPE, EARL_TESTSUBJECT);
        addStatement(subject, RDF_TYPE, CHECKER_TABLE);
        addStatement(subject, CHECKER_NAME, Literal.create(document.xContext, namedTable.getName()));

        for (XURI check : data) {
            addAssertion(subject, check);
        }

        data.clear();
    }

    private void addMetadataToGraphic(Object graphic,
                                      ArrayList<XURI> data)
                               throws IllegalArgumentException,
                                      NoSuchElementException,
                                      RepositoryException {

        XNamed namedGraphic = (XNamed)UnoRuntime.queryInterface(XNamed.class, graphic);

        XBlankNode subject = document.xRepository.createBlankNode();

        addStatement(subject, RDF_TYPE, EARL_TESTSUBJECT);
        addStatement(subject, RDF_TYPE, CHECKER_OBJECT);
        addStatement(subject, CHECKER_NAME, Literal.create(document.xContext, namedGraphic.getName()));

        for (XURI check : data) {
            addAssertion(subject, check);
        }

        data.clear();
    }

    private void addMetadataToObject(Object object,
                                     XURI data)
                              throws IllegalArgumentException,
                                     NoSuchElementException,
                                     RepositoryException {

        XNamed namedObject = (XNamed)UnoRuntime.queryInterface(XNamed.class, object);

        XBlankNode subject = document.xRepository.createBlankNode();

        addStatement(subject, RDF_TYPE, EARL_TESTSUBJECT);
        addStatement(subject, RDF_TYPE, CHECKER_OBJECT);
        addStatement(subject, CHECKER_NAME, Literal.create(document.xContext, namedObject.getName()));

        addAssertion(subject, data);
    }

    private void addAssertion(XResource subject,
                              XURI check)
                       throws IllegalArgumentException,
                              RepositoryException,
                              NoSuchElementException {

        XBlankNode assertion = document.xRepository.createBlankNode();
        XBlankNode testresult = document.xRepository.createBlankNode();

        addStatement(testresult, RDF_TYPE, EARL_TESTRESULT);
        addStatement(testresult, EARL_OUTCOME, EARL_FAILED);
        addStatement(testresult, CHECKER_LASTCHECKED, Literal.create(document.xContext, dateFormat.format(lastChecked)));
        addStatement(assertion, RDF_TYPE, EARL_ASSERTION);
        addStatement(assertion, EARL_RESULT, testresult);
        addStatement(assertion, EARL_TEST, check);
        addStatement(assertion, EARL_SUBJECT, subject);
        addStatement(assertion, EARL_ASSERTEDBY, currentAssertor);

//        addStatement(assertion, CHECKER_INDEX, Literal.create(xContext, String.valueOf(++assertionNr)));
//        addStatement(assertion, CHECKER_INDEX,
//                Literal.createWithType(xContext, String.valueOf(++assertionNr), XSD_INTEGER));

        modified = true;

    }

    private void addStatement(XResource subject,
                              XURI predicate,
                              XNode object)
                       throws IllegalArgumentException,
                              NoSuchElementException,
                              RepositoryException {

        if (currentGraph != null) {
            currentGraph.addStatement(subject, predicate, object);
        }
    }

    public String getIdentifier() {
        return "be.docarch.accessibility.ooo.InternalChecker";
    }
}
