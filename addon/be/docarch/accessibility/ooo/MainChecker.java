package be.docarch.accessibility.ooo;

import java.util.Date;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import java.awt.Color;

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
import com.sun.star.text.XTextSectionsSupplier;
import com.sun.star.text.XTextSection;
import com.sun.star.text.XTextFrame;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextCursor;
import com.sun.star.text.XPageCursor;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XWordCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.text.TextContentAnchorType;
import com.sun.star.table.XCell;
import com.sun.star.table.XTableRows;
import com.sun.star.table.XTableColumns;
import com.sun.star.awt.FontUnderline;
import com.sun.star.awt.FontSlant;
import com.sun.star.view.XSelectionSupplier;
import com.sun.star.style.ParagraphAdjust;
import com.sun.star.style.XStyle;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XNamedGraph;
import com.sun.star.drawing.XDrawPage;
import com.sun.star.drawing.XDrawPageSupplier;
import com.sun.star.form.XFormsSupplier2;
import com.sun.star.linguistic2.XLanguageGuessing;

import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;

import be.docarch.accessibility.Constants;
import be.docarch.accessibility.Check;
import be.docarch.accessibility.Issue;
import be.docarch.accessibility.RunnableChecker;

import be.docarch.accessibility.ooo.rdf.Assertions;

/**
 *
 * @author Bert Frees
 */
public class MainChecker implements RunnableChecker {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private final Document document;
    private final Settings settings;
    private final Collection<String> fakeFonts;
    private final XLanguageGuessing languageGuesser;
    private final XSelectionSupplier selectionSupplier;
    private final Map<String,Check> checks;
    
    private boolean daisyChecks = false;
    private Date lastChecked = null;
    private Collection<String> metadata = null;
    private int numberOfTitles = 0;
    private int numberOfHeadings = 0;
    private XTextViewCursor viewCursor = null;
    private XPageCursor pageCursor = null;
    private String reportName = null;
    private Locale docLocale = null;
    private Map<XTextRange,XTextContent> textMetaMap;
    private Collection<Issue> detectedIssues;
    private Map<Check,Integer> detectedChecks;

    public MainChecker(Document document)
                throws IllegalArgumentException,
                       com.sun.star.uno.Exception {

        this.document = document;
        XComponentContext xContext = document.xContext;
        settings = new Settings(xContext);

        textMetaMap = new HashMap<XTextRange,XTextContent>();
        detectedIssues = new ArrayList<Issue>();
        detectedChecks = new HashMap<Check,Integer>();
        checks = new HashMap<String,Check>();

        for (GeneralCheck.ID id : GeneralCheck.ID.values()) {
            checks.put(id.name(), new GeneralCheck(id));
        }

        for (DaisyCheck.ID id : DaisyCheck.ID.values()) {
            checks.put(id.name(), new DaisyCheck(id));
        }

        fakeFonts = new HashSet<String>();
        fakeFonts.add("Bookshelf Symbol 7");
        fakeFonts.add("Dingbats");
        fakeFonts.add("Marlett");
        fakeFonts.add("MS Reference Speciality");
        fakeFonts.add("MT Extra");
        fakeFonts.add("OpenSymbol");
        fakeFonts.add("Symbol");
        fakeFonts.add("Webdings");
        fakeFonts.add("Wingdings");
        fakeFonts.add("Wingdings 2");
        fakeFonts.add("Wingdings 3");

        languageGuesser = (XLanguageGuessing)UnoRuntime.queryInterface(
                           XLanguageGuessing.class, document.xMCF.createInstanceWithContext(
                           "com.sun.star.linguistic2.LanguageGuessing", document.xContext));

        selectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(
                             XSelectionSupplier.class, document.xModel.getCurrentController());
    }

    public String getIdentifier() {
        return "http://docarch.be/accessibility/ooo/InternalChecker";
    }

    public Collection<Check> list() {
        return checks.values();
    }

    public Check get(String identifier) {
        return checks.get(identifier);
    }

    @Override
    public String toString() {
        return getIdentifier();
    }

 /* public Date getLastCheckDate() {

        try {
            if (lastChecked != null) {
                return dateFormat.parse(dateFormat.format(lastChecked));
            } else {
                return null;
            }
        } catch (java.text.ParseException ex) {
            return null;
        }
    } */

    public boolean run(){

      //document.removeAccessibilityData(getIdentifier());

        lastChecked = new Date();
        reportName = MainChecker.class.getCanonicalName()
                        + "/" + new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(lastChecked) + ".rdf";

        try {

            settings.loadData();
            daisyChecks = settings.daisyChecks();
            detectedIssues.clear();
            detectedChecks.clear();
            textMetaMap.clear();

            // Traverse document
            traverseDocument();

            // Save detected issues in RDF

            XURI[] types = new XURI[]{ URI.create(document.xContext, getIdentifier()) };
            XURI graphURI = null;
            try {
                graphURI = document.xDMA.addMetadataFile(document.metaFolder + reportName, types);
            } catch (ElementExistException e) {
                graphURI = URI.create(document.xContext, document.metaFolderURI.getStringValue() + reportName);
            }

            XNamedGraph graph = document.xRepository.getGraph(graphURI);
            Assertions assertions = new Assertions(graph);
            for (Issue i : detectedIssues) {
                if (detectedChecks.get(i.getCheck()) <= 10) {
                    assertions.create(i).write();
                }
            }
            for (Check c : detectedChecks.keySet()) {
                if (detectedChecks.get(c) > 10) {
                    assertions.create(new Issue(null, c, this)).write();
                }
            }

            document.setModified();

            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }

        return false;
    }

    private void traverseDocument() throws IllegalArgumentException,
                                           WrappedTargetException,
                                           RepositoryException,
                                           NoSuchElementException,
                                           IndexOutOfBoundsException,
                                           UnknownPropertyException,
                                           com.sun.star.uno.Exception,
                                           Exception {

        selectionSupplier.select(document.getFirstParagraph().getAnchor());
        XTextViewCursorSupplier xViewCursorSupplier = (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                                                       XTextViewCursorSupplier.class, document.xModel.getCurrentController());
        viewCursor = xViewCursorSupplier.getViewCursor();
        pageCursor = (XPageCursor)UnoRuntime.queryInterface(XPageCursor.class, viewCursor);

        metadata = new ArrayList<String>();
        numberOfTitles = 0;
        numberOfHeadings = 0;

        // Language
        docLocale = (Locale)AnyConverter.toObject(Locale.class, document.docPropertySet.getPropertyValue("CharLocale"));

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

        // Traverse sections
        XTextSectionsSupplier xTextSectionsSupplier = (XTextSectionsSupplier) UnoRuntime.queryInterface(
                                                       XTextSectionsSupplier.class, document.doc);
        XIndexAccess indexAccess = (XIndexAccess) UnoRuntime.queryInterface(
                                    XIndexAccess.class, xTextSectionsSupplier.getTextSections());
        for (int i=0; i<indexAccess.getCount(); i++) {
           traverseTextSection((XTextSection) UnoRuntime.queryInterface(
                                XTextSection.class, indexAccess.getByIndex(i)), 0);
        }

        // Form components
        XDrawPageSupplier drawPageSupplier = (XDrawPageSupplier)UnoRuntime.queryInterface(XDrawPageSupplier.class, document.doc);
        XDrawPage xDrawPage = drawPageSupplier.getDrawPage();
        XFormsSupplier2 xSuppForms = (XFormsSupplier2)UnoRuntime.queryInterface(XFormsSupplier2.class, xDrawPage);

        // General warnings
        if (docLocale.Language.equals("zxx")) {
            metadata.add(GeneralCheck.ID.E_NoDefaultLanguage.name());
        }
        if (daisyChecks) {
            if (document.docProperties.getTitle().length() == 0) { metadata.add(DaisyCheck.ID.A_EmptyTitleField.name()); }
        }
        if (xSuppForms.hasForms()) { metadata.add(GeneralCheck.ID.A_HasForms.name()); }
        if (numberOfTitles == 0)   { metadata.add(GeneralCheck.ID.A_NoTitle.name()); }
        if (numberOfHeadings == 0) { metadata.add(GeneralCheck.ID.A_NoHeadings.name()); }
        if (metadata.size() > 0) {
            for (String id : metadata) {
                addIssue(new Issue(null, get(id), this));
            }
            metadata.clear();
        }
    }

 /* private void traverseFormComponents(XNameAccess formComponentContainer) throws Exception {

       String aNames[] = formComponentContainer.getElementNames();
       for (int i=0; i<aNames.length; ++i) {

          System.out.println(aNames[i]);

          Object formComponent = formComponentContainer.getByName(aNames[i]);

          XServiceInfo xServiceInfo = (XServiceInfo)UnoRuntime.queryInterface(XServiceInfo.class, formComponent);
          if (xServiceInfo.supportsService("com.sun.star.form.FormComponents")) {
             XNameAccess xChildContainer = (XNameAccess)UnoRuntime.queryInterface(XNameAccess.class, xServiceInfo);
             traverseFormComponents(xChildContainer);
          }
       }
    } */

    private int[] traverseParagraphs(XEnumeration paragraphs,
                                     boolean inFrame,
                                     boolean inTable)
                              throws UnknownPropertyException,
                                     WrappedTargetException,
                                     IllegalArgumentException,
                                     NoSuchElementException,
                                     IndexOutOfBoundsException,
                                     RepositoryException,
                                     com.sun.star.uno.Exception,
                                     Exception {

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
        Paragraph caption = null;
        boolean captionAfterTable = false;
        boolean afterTable = false;
        boolean keepWithTableBefore = false;
        int tablePages = 0;
        Span fakeTable = null;
        Collection<Span> fakeTableSpans = new ArrayList<Span>();
        int fakeTableColumns = 0;
        int fakeTableRows = 0;

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
                fakeTable = null;
                fakeTableRows = 0;
                fakeTableColumns = 0;

            } else if (serviceInfo.supportsService("com.sun.star.text.Paragraph")) {

                Paragraph paragraph = new Paragraph(textContent);

                textRange = textContent.getAnchor();
                text = textRange.getString();

                keepWithNext = AnyConverter.toBoolean(properties.getPropertyValue("ParaKeepTogether"));
                newLevel = AnyConverter.toShort(properties.getPropertyValue("OutlineLevel"));
                alignment = AnyConverter.toInt(properties.getPropertyValue("ParaAdjust"));
                styleName = AnyConverter.toString(properties.getPropertyValue("ParaStyleName"));
              //numberingStyleName = AnyConverter.toString(properties.getPropertyValue("NumberingStyleName"));

                if (caption != null && captionAfterTable && bigTable) {
                    addIssue(new Issue(new be.docarch.accessibility.ooo.Paragraph(textContent),
                                                 get(GeneralCheck.ID.A_CaptionBelowBigTable.name()),
                                                 this));
                }

                caption = null;
                String style = styleName;
                do {
                    if (style.equals("Table")) {
                        caption = paragraph;
                        break;
                    }
                    style = ((XStyle)UnoRuntime.queryInterface(
                              XStyle.class, document.paragraphStyles.getByName(style))).getParentStyle();
                } while (style.length() > 0);

                if (text.length() > 0) {
                    if (alignment == ParagraphAdjust.BLOCK_value ||
                        alignment == ParagraphAdjust.STRETCH_value) {
                        metadata.add(GeneralCheck.ID.A_JustifiedText.name());
                    }
                }
                if (!inFrame && styleName.equals("Title")) {
                    if (text.length() == 0) {
                        metadata.add(GeneralCheck.ID.E_EmptyTitle.name());
                    } else {
                        numberOfTitles++;
                        if (numberOfTitles > 1) {
                            metadata.add(GeneralCheck.ID.E_ManyTitles.name());
                        }
                    }
                } else if (inFrame && newLevel > 0) {
                    metadata.add(GeneralCheck.ID.E_HeadingInFrame.name());
                    currentLevel = newLevel;
                } else if (newLevel > 0 && text.length() == 0) {
                    metadata.add(GeneralCheck.ID.E_EmptyHeading.name());
                } else if (newLevel > currentLevel + 1) {
                    metadata.add(GeneralCheck.ID.E_HeadingSkip.name());
                    numberOfHeadings ++;
                    currentLevel = newLevel;
                } else if (!alternateLevel && newLevel > 6) {
                    alternateLevel = true;
                    metadata.add(GeneralCheck.ID.A_AlternateLevel.name());
                    numberOfHeadings ++;
                    currentLevel = newLevel;
                } else if (newLevel > 0) {
                    numberOfHeadings ++;
                    currentLevel = newLevel;
                }

                if (text.length() > 0) {

                    int columns = getNumberOfColumns(text);
                    if (columns > 1) {
                        if (fakeTable != null && columns == fakeTableColumns) {
                            fakeTable.add(paragraph.getEndPoint(), text);
                            fakeTableRows++;
                            if (fakeTableRows == 2) {
                                fakeTableSpans.add(fakeTable);
                            }
                        } else {
                            fakeTable = new Span(paragraph.getStartPoint(), paragraph.getEndPoint(), text);
                            fakeTableColumns = columns;
                            fakeTableRows = 1;
                        }
                    } else {
                        fakeTable = null;
                        fakeTableColumns = 0;
                        fakeTableRows = 0;
                    }

                    if (text.matches("([\\.\\-\\+\\*\\#\\=\\_])\\1{10,}")) {
                        metadata.add(GeneralCheck.ID.A_FakeLine.name());
                    }

                    if (caption != null && afterTable && bigTable) {
                        if (keepWithTableBefore || !keepWithNext) {                            
                            addIssue(new Issue(new be.docarch.accessibility.ooo.Paragraph(textContent),
                                                         get(GeneralCheck.ID.A_CaptionBelowBigTable.name()),
                                                         this));
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
                        metadata.add(GeneralCheck.ID.A_NoSubtitle.name());
                    }
                    
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
                    
                    traverseTextPortions(paragraph, checkFakeSubtitle, checkFakeHeading);

                    if (styleName.equals("Title")) {
                        afterTitle = true;
                        titleCentered = (alignment == ParagraphAdjust.CENTER_value);
                    } else if (afterTitle) {
                        afterTitle = false;
                    }
                }

                if (metadata.size() > 0) {
                    be.docarch.accessibility.ooo.Paragraph p = new be.docarch.accessibility.ooo.Paragraph(textContent);
                    for (String id : metadata) {
                        addIssue(new Issue(p, get(id), this));
                    }
                    metadata.clear();
                }

                afterTable = false;

            } else {
                fakeTable = null;
                fakeTableRows = 0;
                fakeTableColumns = 0;
            }
        }

        for (Span span : fakeTableSpans) {
            addMetadataToSpan(span, GeneralCheck.ID.A_FakeTable.name());
        }

        if (caption != null && captionAfterTable) {
            addIssue(new Issue(new be.docarch.accessibility.ooo.Paragraph(textContent),
                                         get(GeneralCheck.ID.A_CaptionBelowBigTable.name()),
                                         this));
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

    private void traverseTextPortions(Paragraph paragraph,
                                      boolean checkNoSubtitle,
                                      boolean checkFakeHeading)
                               throws UnknownPropertyException,
                                      WrappedTargetException,
                                      IllegalArgumentException,
                                      RepositoryException,
                                      NoSuchElementException,
                                      com.sun.star.uno.Exception,
                                      Exception {

        float charHeight = 0;
        float charWeight = 0;
        short underline = 0;
        double contrast;
        Locale locale;
        Locale guessedLocale;
        String fontName;
        FontSlant italic = null;
        boolean flash = false;
        String hyperlinkURL = "";
        Span smallText = null;
        Span underlinedText = null;
        Span italicText = null;
        Span lowContrastText = null;
        Span fakeText = null;
        Span flashText = null;
        Hyperlink hyperlink = null;
        LocalizedSpan localizedText = null;
        int maxUnderlineLength = 250;
        int maxItalicLength = 250;
        int minLocaleGuessingLength = 100;
        boolean allBig = true;
        boolean allBold = true;
        Collection<Span> smallTextSpans = new ArrayList<Span>();
        Collection<Span> underlinedTextSpans = new ArrayList<Span>();
        Collection<Span> italicTextSpans = new ArrayList<Span>();
        Collection<Span> lowContrastTextSpans = new ArrayList<Span>();
        Collection<Span> fakeTextSpans = new ArrayList<Span>();
        Collection<LocalizedSpan> localizedTextSpans = new ArrayList<LocalizedSpan>();
        Collection<Hyperlink> hyperlinks = new ArrayList<Hyperlink>();
        Collection<Span> flashTextSpans = new ArrayList<Span>();

        for (TextPortion textPortion : paragraph.getTextPortions()) {

            XPropertySet properties = textPortion.properties;
            String text = textPortion.text;
            XTextRange start = textPortion.startPoint;
            XTextRange end = textPortion.endPoint;

            fontName = AnyConverter.toString(properties.getPropertyValue("CharFontName"));
            charHeight = AnyConverter.toFloat(properties.getPropertyValue("CharHeight"));
            charWeight = AnyConverter.toFloat(properties.getPropertyValue("CharWeight"));
            underline = AnyConverter.toShort(properties.getPropertyValue("CharUnderline"));
            italic = (FontSlant)AnyConverter.toObject(FontSlant.class, properties.getPropertyValue("CharPosture"));
            flash = AnyConverter.toBoolean(properties.getPropertyValue("CharFlash"));
            Color charColor = convertColor(AnyConverter.toInt(properties.getPropertyValue("CharColor")));
            Color charBackColor = convertColor(AnyConverter.toInt(properties.getPropertyValue("CharBackColor")));
            Color paraBackColor = convertColor(AnyConverter.toInt(properties.getPropertyValue("ParaBackColor")));
            Color foreground = (charColor.getAlpha() == 0) ? Color.BLACK : charColor;
            Color background = (charBackColor.getAlpha() == 0) ? paraBackColor : charBackColor;
            contrast = contrastRatio(foreground, background);
            locale = (Locale)AnyConverter.toObject(Locale.class, properties.getPropertyValue("CharLocale"));

            try {
                hyperlinkURL = AnyConverter.toString(properties.getPropertyValue("HyperLinkURL"));
            } catch (UnknownPropertyException e) {
                hyperlinkURL = "";
            }
            
            if (contrast < 4.5d) {
                if (lowContrastText == null) {
                    lowContrastText = new Span(start, end, text);
                    lowContrastTextSpans.add(lowContrastText);
                } else {
                    lowContrastText.add(end, text);
                }
            } else {
                lowContrastText = null;
            }

            if (fakeFonts.contains(fontName)) {
                if (fakeText == null) {
                    fakeText = new Span(start, end, text);
                    fakeTextSpans.add(fakeText);
                } else {
                    fakeText.add(end, text);
                }
            } else {
                fakeText = null;
            }

            if (charHeight < 10) {
                if (smallText == null) {
                    smallText = new Span(start, end, text);
                    smallTextSpans.add(smallText);
                } else {
                    smallText.add(end, text);
                }
            } else {
                smallText = null;
            }

            if (flash) {
                if (flashText == null) {
                    flashText = new Span(start, end, text);
                    flashTextSpans.add(flashText);
                } else {
                    flashText.add(end, text);
                }
            } else {
                flashText = null;
            }

            if (underline != FontUnderline.NONE) {
                if (underlinedText == null) {
                    underlinedText = new Span(start, end, text);
                    underlinedTextSpans.add(underlinedText);
                } else {
                    underlinedText.add(end, text);
                }
            } else {
                underlinedText = null;
            }

            if (italic == FontSlant.OBLIQUE ||
                italic == FontSlant.ITALIC ||
                italic == FontSlant.REVERSE_OBLIQUE ||
                italic == FontSlant.REVERSE_ITALIC) {
                if (italicText == null) {
                    italicText = new Span(start, end, text);
                    italicTextSpans.add(italicText);
                } else {
                    italicText.add(end, text);
                }
            } else {
                italicText = null;
            }

            if (localizedText != null &&
                localizedText.locale().Language.equals(locale.Language) &&
                localizedText.locale().Country.equals(locale.Country)) {
                localizedText.add(end, text);
            } else {
                localizedText = new LocalizedSpan(start, end, text, locale);
                localizedTextSpans.add(localizedText);
            }

            if (hyperlinkURL.length() > 0) {
                if (hyperlink == null) {
                    hyperlink = new Hyperlink(start, end, text, hyperlinkURL);
                    hyperlinks.add(hyperlink);
                } else {
                    hyperlink.add(end, text);
                }
            } else {
                hyperlink = null;
            }

            allBig = allBig && (charHeight > 13);
            allBold = allBold && (charWeight > 100);
        }

        for (Span span : smallTextSpans) {
            addMetadataToSpan(span, GeneralCheck.ID.A_SmallText.name());
        }

        for (Span span : flashTextSpans) {
            addMetadataToSpan(span, GeneralCheck.ID.A_FlashText.name());
        }

        for (Span span : underlinedTextSpans) {
            if (span.getText().length() > maxUnderlineLength) {
                addMetadataToSpan(span, GeneralCheck.ID.A_LongUnderline.name());
            }
        }

        for (Span span : italicTextSpans) {
            if (span.getText().length() > maxItalicLength) {
                addMetadataToSpan(span, GeneralCheck.ID.A_LongItalic.name());
            }
        }

        for (Span span : lowContrastTextSpans) {
            addMetadataToSpan(span, GeneralCheck.ID.A_LowContrast.name());
        }

        for (Span span : fakeTextSpans) {
            addMetadataToSpan(span, GeneralCheck.ID.A_FakeText.name());
        }

        for (LocalizedSpan span : localizedTextSpans) {
            if (span.locale().Language.equals("zxx") && !(docLocale.Language.equals("zxx"))) {
                if (hyperlinks.contains(span)) {
                    addMetadataToSpan(span, GeneralCheck.ID.E_NoHyperlinkLanguage.name());
                } else {
                    addMetadataToSpan(span, GeneralCheck.ID.E_NoLanguage.name());
                }
            } else if (!span.locale().Language.equals("zxx") &&
                       span.locale().Language.equals(docLocale.Language) && span.locale().Country.equals(docLocale.Country) &&
                       span.getText().length() > minLocaleGuessingLength) {
                guessedLocale = languageGuesser.guessPrimaryLanguage(span.getText(), 0, span.getText().length());
                if (!span.locale().Language.equals(guessedLocale.Language) &&
                    guessedLocale.Language.length() > 0) {
                    addMetadataToSpan(span, GeneralCheck.ID.A_UnidentifiedLanguage.name());
                }
            }
        }

        for (Hyperlink span : hyperlinks) {
            if (span.getText().equals(span.url())) {
                addMetadataToSpan(span, GeneralCheck.ID.A_NoHyperlinkText.name());
            }
        }

        if (checkNoSubtitle && (allBig || allBold)) {
            metadata.add(GeneralCheck.ID.A_NoSubtitle.name());
        }
        if (checkFakeHeading && (allBig || allBold)) {
            metadata.add(GeneralCheck.ID.A_FakeHeading.name());
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
                                  com.sun.star.uno.Exception,
                                  Exception  {

        XPropertySet properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, table);
        XPropertySet rowProperties = null;
        XCell tableCell = null;
        XEnumerationAccess enumerationAccess = null;
        XEnumeration paragraphsInTableCell = null;
        boolean repeatTableHeading = AnyConverter.toBoolean(properties.getPropertyValue("RepeatHeadline"));
      //boolean keepTableTogether = !AnyConverter.toBoolean(properties.getPropertyValue("Split"));
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
            metadata.add(GeneralCheck.ID.A_NestedTable.name());
        }
        if (!repeatTableHeading) {
            metadata.add(GeneralCheck.ID.A_NoTableHeading.name());
        }
        if (!keepTableRowsTogether) {
            metadata.add(GeneralCheck.ID.A_BreakRows.name());
        }
        if (pageRange[1] > pageRange[0] + 1) {
            metadata.add(GeneralCheck.ID.A_BigTable.name());
        }
        if (tableRows.getCount() * tableColumns.getCount() != cellNames.length) {    // Dit is geen waterdichte oplossing !
            metadata.add(GeneralCheck.ID.A_MergedCells.name());
        }

        if (metadata.size() > 0) {
            be.docarch.accessibility.ooo.Table t = new be.docarch.accessibility.ooo.Table(table);
            for (String id : metadata) {
                addIssue(new Issue(t, get(id), this));
            }
            metadata.clear();
        }

        return pageRange[1] - pageRange[0] + 1;
    }

    private void traverseTextFrames(XIndexAccess textFrames)
                             throws IndexOutOfBoundsException,
                                    UnknownPropertyException,
                                    WrappedTargetException,
                                    NoSuchElementException,
                                    RepositoryException,
                                    IllegalArgumentException,
                                    com.sun.star.uno.Exception,
                                    Exception {

        XTextFrame textFrame = null;

        for (int i=0; i<textFrames.getCount(); i++) {
            textFrame = (XTextFrame)UnoRuntime.queryInterface(XTextFrame.class, textFrames.getByIndex(i));
            XEnumerationAccess enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(
						    XEnumerationAccess.class, textFrame.getText());
            XEnumeration paragraphsInFrame = enumerationAccess.createEnumeration();
            traverseParagraphs(paragraphsInFrame, true, false);
        }
    }

    private void traverseTextSection(XTextSection section,
                                     int level)
                              throws WrappedTargetException,
                                     IllegalArgumentException,
                                     RepositoryException,
                                     NoSuchElementException {

        if (level > 0 && daisyChecks) {
            String name = ((XNamed)UnoRuntime.queryInterface(XNamed.class, section)).getName();
            if (name.equals("BodyMatterStart")) {
                metadata.add(DaisyCheck.ID.A_BodyMatterStartSectionNested.name());
            } else if (name.equals("RearMatterStart")) {
                metadata.add(DaisyCheck.ID.A_RearMatterStartSectionNested.name());
            }
            if (metadata.size() > 0) {
                for (String id : metadata) {
                    addIssue(new Issue(null, get(id), this));
                }
                metadata.clear();
            }
        }
        for (XTextSection childSection : section.getChildSections()) {
             traverseTextSection(childSection, level+1);
        }
    }

    private void traverseGraphicObjects(XIndexAccess graphicObjects)
                                 throws IndexOutOfBoundsException,
                                        UnknownPropertyException,
                                        WrappedTargetException,
                                        RepositoryException,
                                        IllegalArgumentException,
                                        NoSuchElementException,
                                        Exception {

        Object graphicObject = null;
        XPropertySet properties = null;
        XGraphic graphic = null;
        XPropertySet mediaProperties = null;
        String title = null;
        String description = null;
        String url = null;
        String fileExtension = null;
        String mimeType = null;

        Collection<String> graphicMetadata = new ArrayList<String>();

        for (int i=0; i<graphicObjects.getCount(); i++) {

            graphicObject = graphicObjects.getByIndex(i);
            properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, graphicObject);

            title = AnyConverter.toString(properties.getPropertyValue("Title"));
            description = AnyConverter.toString(properties.getPropertyValue("Description"));
            url = AnyConverter.toString(properties.getPropertyValue("GraphicURL"));

            TextContentAnchorType anchorType = (TextContentAnchorType)AnyConverter.toObject(
                                                TextContentAnchorType.class, properties.getPropertyValue("AnchorType"));
            if (anchorType != TextContentAnchorType.AS_CHARACTER) {
                graphicMetadata.add(GeneralCheck.ID.E_ImageAnchorFloat.name());
            }
            if (title.length() == 0) {
                graphicMetadata.add(GeneralCheck.ID.A_ImageWithoutAlt.name());
            }
            if (url.startsWith("vnd.sun.star.GraphicObject:")) {
                if (daisyChecks) {
                    graphic = (XGraphic)AnyConverter.toObject(
                               XGraphic.class, properties.getPropertyValue("Graphic"));
                    mediaProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, graphic);
                    mimeType = AnyConverter.toString(mediaProperties.getPropertyValue("MimeType"));
                    if (!mimeType.equals("image/jpeg") &&              
                        !mimeType.equals("image/png") &&
                        !mimeType.equals("image/x-vclgraphic")) {
                        graphicMetadata.add(DaisyCheck.ID.E_UnsupportedImageFormat.name());
                    }
                }
            } else {
                graphicMetadata.add(GeneralCheck.ID.A_LinkedImage.name());
                fileExtension = url.substring(url.lastIndexOf(".") + 1);
                if (daisyChecks &&
                    !fileExtension.equals("png") &&
                    !fileExtension.equals("jpg")) {
                    graphicMetadata.add(DaisyCheck.ID.E_UnsupportedImageFormat.name());
                }
            }

            if (graphicMetadata.size() > 0) {
                XNamed namedGraphic = (XNamed)UnoRuntime.queryInterface(XNamed.class, graphicObject);
                DrawObject o = new DrawObject(namedGraphic);
                for (String id : graphicMetadata) {
                    addIssue(new Issue(o, get(id), this));
                }
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
                                         com.sun.star.uno.Exception,
                                         Exception {

        Object embeddedObject = null;
        XPropertySet properties = null;
        String title = null;
        String description = null;

        for (int i=0; i<embeddedObjects.getCount(); i++) {

            embeddedObject = embeddedObjects.getByIndex(i);
            properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, embeddedObject);
            title = AnyConverter.toString(properties.getPropertyValue("Title"));
            description = AnyConverter.toString(properties.getPropertyValue("Description"));

            if (title.length() == 0) {
                XNamed namedObject = (XNamed)UnoRuntime.queryInterface(XNamed.class, embeddedObject);
                DrawObject o = new DrawObject(namedObject);
                if (AnyConverter.toString(properties.getPropertyValue("CLSID")).equals("078B7ABA-54FC-457F-8551-6147e776a997")) {
                    addIssue(new Issue(o, get(GeneralCheck.ID.A_FormulaWithoutAlt.name()), this));
                } else {
                    addIssue(new Issue(o, get(GeneralCheck.ID.A_ObjectWithoutAlt.name()), this));
                }
            }
        }
    }

    private void addMetadataToSpan(Span span,
                                   String id)
                            throws IllegalArgumentException,
                                   NoSuchElementException,
                                   RepositoryException,
                                   com.sun.star.uno.Exception,
                                   Exception {

        XTextContent start = getTextMeta(span.getStartPoint());
        XTextContent end = getTextMeta(span.getEndPoint());
        addIssue(new Issue(new be.docarch.accessibility.ooo.Span(start, end), get(id), this));
    }

    private void addIssue(Issue issue) {
        detectedIssues.add(issue);
        Integer cnt = detectedChecks.get(issue.getCheck());
        detectedChecks.put(issue.getCheck(), (cnt == null) ? 1 : cnt + 1);
    }



    // TODO: in aparte klassen zetten

    private class Paragraph { // TODO: samenvoegen met be.docarch.accessibility.ooo.Paragraph

        private final XTextRange startPoint;
        private final XTextRange endPoint;
        private final List<TextPortion> textPortions;

        public Paragraph(XTextContent textContent) {

            textPortions = new ArrayList<TextPortion>();
            XEnumerationAccess enumerationAccess = (XEnumerationAccess)UnoRuntime.queryInterface(
                                                    XEnumerationAccess.class, textContent);
            XEnumeration enumeration = enumerationAccess.createEnumeration();
            try {
                XTextContent textMeta = null;
                TextPortion portion = null;
                while (enumeration.hasMoreElements()) {
                    XTextRange range = (XTextRange)UnoRuntime.queryInterface(XTextRange.class, enumeration.nextElement());
                    XPropertySet propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, range);
                    String textPortionType = AnyConverter.toString(propertySet.getPropertyValue("TextPortionType"));
                    if (textPortionType.equals("InContentMetadata")) {
                        textMeta = (XTextContent)AnyConverter.toObject(
                                    XTextContent.class, propertySet.getPropertyValue("InContentMetadata"));
                        if (portion != null && !textMetaMap.containsKey(portion.endPoint)) {
                            setTextMeta(portion.endPoint, textMeta);
                        }
                    } else if (textPortionType.equals("Text")) {
                        if (range.getString().length() > 0) {
                            portion = new TextPortion(range);
                            if (textMeta != null) { setTextMeta(portion.startPoint, textMeta); }
                            textMeta = null;
                            textPortions.add(portion);
                        }
                    } else if (textPortionType.equals("SoftPageBreak")) {
                        portion = null;
                    }
                }
            } catch (Exception e) {
            }
            if (textPortions.size() > 0) {
                startPoint = textPortions.get(0).startPoint;
                endPoint = textPortions.get(textPortions.size()-1).endPoint;
            } else {
                startPoint = textContent.getAnchor().getStart();
                endPoint = textContent.getAnchor().getEnd();
            }
        }

        public List<TextPortion> getTextPortions() {
            return textPortions;
        }

        public XTextRange getStartPoint() { return startPoint; }
        public XTextRange getEndPoint() { return endPoint; }
    }

    private class TextPortion {

        public final String text;
        public final XTextRange startPoint;
        public final XTextRange endPoint;
        public final XPropertySet properties;

        public TextPortion(XTextRange range) {
            text = range.getString();
            startPoint = range.getStart();
            endPoint = range.getEnd();
            properties = (XPropertySet) UnoRuntime.queryInterface(XPropertySet.class, range);
        }
    }

    private class Span { // TODO: samenvoegen met be.docarch.accessibility.ooo.Span ?

        private String text;
        private XTextRange startPoint;
        private XTextRange endPoint;

        public Span(XTextRange start, XTextRange end, String text) {
            startPoint = start;
            endPoint = end;
            this.text = text;
        }

        public void add(XTextRange end, String text) {
            endPoint = end;
            this.text += text;
        }

        public XTextRange getStartPoint() { return startPoint; }
        public XTextRange getEndPoint() { return endPoint; }

        public String getText() { return text; }

        @Override
        public boolean equals(Object object) {
            if (this == object) { return true; }
            if (!(object instanceof Span)) { return false; }
            try {
                Span that = (Span)object;
                return this.startPoint == that.startPoint &&
                       this.endPoint == that.endPoint;
            } catch (ClassCastException e) {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 11 * hash + startPoint.hashCode();
            hash = 11 * hash + endPoint.hashCode();
            return hash;
        }
    }

    private class LocalizedSpan extends Span {

        private final Locale locale;

        public LocalizedSpan(XTextRange start, XTextRange end, String text, Locale locale) {
            super(start, end, text);
            this.locale = locale;
        }

        public Locale locale() { return locale; }
    }

    private class Hyperlink extends Span {

        private final String url;

        public Hyperlink(XTextRange start, XTextRange end, String text, String url) {
            super(start, end, text);
            this.url = url;
        }

        public String url() { return url; }
    }








    public void setTextMeta(XTextRange range, XTextContent textMeta) {
        textMetaMap.put(range, textMeta);
    }

    public XTextContent getTextMeta(XTextRange range) {
        XTextContent textMeta = textMetaMap.get(range);
        if (textMeta == null) {
            try {
                textMeta = (XTextContent)UnoRuntime.queryInterface(
                            XTextContent.class, document.xMSF.createInstance("com.sun.star.text.InContentMetadata"));
                range.getText().insertTextContent(range, textMeta, true);
            } catch (Exception e) {
            }
        }
        return textMeta;
    }

    private static int getNumberOfColumns(String paragraph) {

        paragraph = paragraph.replaceAll("[\\x20]{5,}", "\t");
        paragraph = paragraph.replaceAll("[\\x20]+", "");
        paragraph = paragraph.replaceAll("[\\t]+", "\t");
        paragraph = paragraph.trim();

        if (paragraph.length() == 0) { return 0; }
        return countOccurrences(paragraph, '\t') + 1;
    }

    public static int countOccurrences(String haystack, char needle) {
        int count = 0;
        for (int i=0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) { count++; }
        }
        return count;
    }

    /*
     * Convert com.sun.star.util.Color to java.awt.Color
     */
    private static Color convertColor(int comSunStarUtilColor) {

        Color c = new Color(comSunStarUtilColor, true);
        int invertedAlpha = 0xFF - c.getAlpha();

        return new Color(c.getRed(), c.getGreen(), c.getBlue(), invertedAlpha);
    }

    /*
     * Relative luminance of a color as defined at http://www.w3.org/TR/WCAG20/#contrast-ratiodef:
     *    The relative brightness of any point in a colorspace, normalized to 0 for darkest black and 1 for lightest white
     */
    private static double relativeLuminance(Color color) {

        double R, G, B;
        double RsRGB, GsRGB, BsRGB;

        RsRGB = (double)(color.getRed())   / 0xFF;
        GsRGB = (double)(color.getGreen()) / 0xFF;
        BsRGB = (double)(color.getBlue())  / 0xFF;

        R = (RsRGB <= 0.03928) ? RsRGB / 12.92 : Math.pow((RsRGB + 0.055) / 1.055, 2.4);
        G = (GsRGB <= 0.03928) ? GsRGB / 12.92 : Math.pow((GsRGB + 0.055) / 1.055, 2.4);
        B = (BsRGB <= 0.03928) ? BsRGB / 12.92 : Math.pow((BsRGB + 0.055) / 1.055, 2.4);

        return 0.2126*R + 0.7152*G + 0.0722*B;
    }

    /*
     * Contrast ratio of two colors as defined at http://www.w3.org/TR/WCAG20/#contrast-ratiodef
     */
    private static double contrastRatio(Color color1,
                                        Color color2) {

        double L1 = relativeLuminance(color1);
        double L2 = relativeLuminance(color2);

        return (Math.max(L1, L2) + 0.05)/(Math.min(L1, L2) + 0.05);
    }
}
