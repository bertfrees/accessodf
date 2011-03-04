package be.docarch.accessibility.ooo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.AnyConverter;
import com.sun.star.lang.Locale;
import com.sun.star.container.XEnumeration;
import com.sun.star.style.ParagraphAdjust;
import com.sun.star.table.XTableRows;
import com.sun.star.rdf.Statement;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.BlankNode;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.XNamedGraph;
import com.sun.star.rdf.Literal;

import java.text.ParseException;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.accessibility.URIs;
import be.docarch.accessibility.Check;
import be.docarch.accessibility.Checker;


/**
 *
 * @author Bert Frees
 */
public class Issue {

    private final static Logger logger = Logger.getLogger("be.docarch.accessibility");

//    private static XURI CHECKER_INDEX = null;
    private static XURI CHECKER_LASTCHECKED = null;
    private static XURI CHECKER_IGNORE = null;
    private static XURI RDF_TYPE = null;
    private static XURI EARL_TESTSUBJECT = null;
    private static XURI EARL_TESTRESULT = null;
    private static XURI EARL_TESTCASE = null;
    private static XURI EARL_ASSERTION = null;
    private static XURI EARL_OUTCOME = null;
    private static XURI EARL_FAILED = null;
    private static XURI EARL_PASSED = null;
    private static XURI EARL_RESULT = null;
    private static XURI EARL_TEST = null;
    private static XURI EARL_SUBJECT = null;
    private static XURI EARL_ASSERTEDBY = null;
    
    private static Document document = null;
    private static Checker[] checkers = null;
    private static XDispatchHelper dispatcher = null;
    private static XDispatchProvider dispatchProvider = null;

    private XNamedGraph graph = null;
    private XResource assertion = null;
    private XResource testresult = null;
//    private XNode assertor = null;
    private XURI testcase = null;

    private Element element = null;
    private Check check = null;
    private Checker checker = null;
    private boolean valid = false;
    private boolean ignored = false;
    private boolean repaired = false;
    protected int index = 0;
    protected int position = 0;
    protected Date lastChecked = null;


    static void setDocument(Document document,
                            Checker[] checkers)
                     throws IllegalArgumentException,
                            com.sun.star.uno.Exception {

        Issue.document = document;
        Issue.checkers = checkers;
        XComponentContext xContext = document.xContext;

        Element.setDocument(document);
        dispatcher = (XDispatchHelper)UnoRuntime.queryInterface(
                         XDispatchHelper.class, document.xMCF.createInstanceWithContext(
                             "com.sun.star.frame.DispatchHelper", xContext));
        dispatchProvider = (XDispatchProvider)UnoRuntime.queryInterface(
                                XDispatchProvider.class, document.xModel.getCurrentController().getFrame());

//        CHECKER_INDEX = URI.create(xContext, URIs.CHECKER_INDEX);
        CHECKER_LASTCHECKED = URI.create(xContext, URIs.CHECKER_LASTCHECKED);
        CHECKER_IGNORE = URI.create(xContext, URIs.CHECKER_IGNORE);
        RDF_TYPE = URI.create(xContext, URIs.RDF_TYPE);
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

    }

    public Issue(XResource assertion,
                 XNamedGraph graph)
          throws IllegalArgumentException,
                 RepositoryException,
                 NoSuchElementException,
                 WrappedTargetException {

        logger.entering("Issue", "<init>");

        if (graph.getStatements(assertion, RDF_TYPE, EARL_ASSERTION).hasMoreElements()) {

            this.graph = graph;
            this.assertion = assertion;

            XEnumeration assertors = graph.getStatements(assertion, EARL_ASSERTEDBY, null);
            XEnumeration testsubjects = graph.getStatements(assertion, EARL_SUBJECT, null);
            XEnumeration testcases = graph.getStatements(assertion, EARL_TEST, null);
            XEnumeration testresults = graph.getStatements(assertion, EARL_RESULT, null);

//            XEnumeration indexes = xRepository.getStatements(assertion, CHECKER_INDEX, null);
//            if (indexes.hasMoreElements()) {
//                String s = ((Statement)indexes.nextElement()).Object.getStringValue();
//                if (s.length() > 0) {
//                    index = Integer.parseInt(s);
//                } else {
//                    index = 0;
//                }
//            }

            if (assertors.hasMoreElements() &&
                testsubjects.hasMoreElements() &&
                testcases.hasMoreElements() &&
                testresults.hasMoreElements()) {

//                assertor = ((Statement)assertors.nextElement()).Object;
                testcase = URI.create(document.xContext, ((Statement)testcases.nextElement()).Object.getStringValue());
                testresult = BlankNode.create(document.xContext, ((Statement)testresults.nextElement()).Object.getStringValue());
                XResource testsubject = BlankNode.create(document.xContext, ((Statement)testsubjects.nextElement()).Object.getStringValue());

                if (graph.getStatements(testresult, EARL_OUTCOME, null).hasMoreElements() &&
                    graph.getStatements(testresult, RDF_TYPE, EARL_TESTRESULT).hasMoreElements() &&
                    graph.getStatements(testsubject, RDF_TYPE, EARL_TESTSUBJECT).hasMoreElements() &&
                    graph.getStatements(testcase, RDF_TYPE, EARL_TESTCASE).hasMoreElements()) {

                    if (graph.getStatements(testresult, EARL_OUTCOME, EARL_PASSED).hasMoreElements()) {
                        repaired = true;
                    }
                    element = new Element(testsubject);
                    for (Checker chckr: checkers) {
                        check = chckr.getCheck(testcase.getLocalName());
                        if (check != null) {
                            checker = chckr;
                            logger.info(check.getIdentifier());
                            break;
                        }
                    }
                    XEnumeration timestamps = graph.getStatements(testresult, CHECKER_LASTCHECKED, null);
                    if (timestamps.hasMoreElements()) {
                        try {
                            lastChecked = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(
                                    ((Statement)timestamps.nextElement()).Object.getStringValue());
                            valid = true;
                            XEnumeration ignore = graph.getStatements(testresult, CHECKER_IGNORE, null);
                            if (ignore.hasMoreElements()) {
                                if (((Statement)ignore.nextElement()).Object.getStringValue().equals("true")) {
                                    ignored = true;
                                }
                            }
                        } catch (ParseException ex) {
                            logger.log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }

        if (!valid) {
            logger.info("invalid assertion");
        }

        logger.exiting("Issue", "<init>");
    }
    
    public boolean isValid() {
        return valid;
    }

    public Element getElement() {

        return element;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public boolean isRepaired() {
        return repaired;
    }

    public void setIgnored(boolean ignored) 
                    throws IllegalArgumentException,
                           PropertyVetoException,
                           RepositoryException,
                           NoSuchElementException{

        this.ignored = ignored;
        graph.removeStatements(testresult, CHECKER_IGNORE, null);
        if (ignored) {
            graph.addStatement(testresult, CHECKER_IGNORE, Literal.create(document.xContext, "true"));
        }
        document.setModified();
    }

    public void setRepaired(boolean repaired)
                     throws IllegalArgumentException,
                            RepositoryException,
                            PropertyVetoException,
                            NoSuchElementException {

        this.repaired = repaired;
        graph.removeStatements(testresult, EARL_OUTCOME, null);
        if (repaired) {
            graph.addStatement(testresult, EARL_OUTCOME, EARL_PASSED);
        } else {
            graph.addStatement(testresult, EARL_OUTCOME, EARL_FAILED);
        }
        document.setModified();
    }

    public boolean repair() throws NoSuchElementException,
                                   WrappedTargetException,
                                   UnknownPropertyException,
                                   PropertyVetoException,
                                   IndexOutOfBoundsException,
                                   RepositoryException,
                                   IllegalArgumentException {

        PropertyValue[] dispatchProperties;
        XPropertySet properties;
        boolean succes = false;

        if (check != null &&
            element != null &&
            check.getClass() == InternalCheck.class &&
           (check.getRepairMode() == Check.RepairMode.AUTO ||
            check.getRepairMode() == Check.RepairMode.SEMI_AUTOMATED)) {

            switch (InternalCheck.ID.valueOf(check.getIdentifier())) {
                case A_ImageWithoutAlt:
                case A_FormulaWithoutAlt:
                case A_ObjectWithoutAlt:
                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:ObjectTitleDescription", "", 0, dispatchProperties);
                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, element.getObject());
                    succes = (AnyConverter.toString(properties.getPropertyValue("Title")).length() +
                              AnyConverter.toString(properties.getPropertyValue("Description")).length() > 0);
                    break;
                case A_EmptyTitleField:
                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:SetDocumentProperties",  "", 0, dispatchProperties);
                    succes = (document.docProperties.getTitle().length() > 0);
                    break;
                case A_LinkedImage:
                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:GraphicDialog",          "", 0, dispatchProperties);
                    break;
                case A_NoTableHeading:
                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:TableDialog",            "", 0, dispatchProperties);
                    succes = AnyConverter.toBoolean(((XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                                element.getTable())).getPropertyValue("RepeatHeadline"));
                    break;
                case E_DefaultLanguage:
                    dispatchProperties = new PropertyValue[1];
                    dispatchProperties[0] = new PropertyValue();
                    dispatchProperties[0].Name = "Language";
                    dispatchProperties[0].Value = "*";
                    dispatcher.executeDispatch(dispatchProvider, ".uno:LanguageStatus",         "", 0, dispatchProperties);
                    succes = !(((Locale)AnyConverter.toObject(
                                 Locale.class, document.docPropertySet.getPropertyValue("CharLocale"))).Language.equals("zxx"));
                    break;
                case A_NoSubtitle:
                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                            element.getParagraph());
                    if (document.paragraphStyles.getByName("Subtitle") != null) {
                        properties.setPropertyValue("ParaStyleName", "Subtitle");
                        succes = true;
                    }
                    break;
                case A_JustifiedText:
                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                            element.getParagraph());
                    properties.setPropertyValue("ParaAdjust", ParagraphAdjust.LEFT_value);
                    properties = (XPropertySet)UnoRuntime.queryInterface(
                                   XPropertySet.class, document.paragraphStyles.getByName(
                                   AnyConverter.toString(properties.getPropertyValue("ParaStyleName"))));
                    int paraAdjust = AnyConverter.toInt(properties.getPropertyValue("ParaAdjust"));
                    if (paraAdjust == ParagraphAdjust.BLOCK_value ||
                        paraAdjust == ParagraphAdjust.STRETCH_value) {
                        properties.setPropertyValue("ParaAdjust", ParagraphAdjust.LEFT_value);
                    }
                    succes = true;
                    break;
                case E_EmptyTitle:
                case E_EmptyHeading:
                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                            element.getParagraph());
                    properties.setPropertyValue("ParaStyleName", "Standard");
                    succes = true;
                    break;
                case A_BreakRows:
                    XTableRows tableRows = element.getTable().getRows();
                    XPropertySet rowProperties = null;
                    for (int i=0; i<tableRows.getCount(); i++) {
                        rowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, tableRows.getByIndex(i));
                        if (rowProperties.getPropertySetInfo().hasPropertyByName("IsSplitAllowed")) {
                            rowProperties.setPropertyValue("IsSplitAllowed", false);
                        }
                    }
                    succes = true;
                    break;
            }
        }

        if (succes) { setRepaired(true); }

        return succes;
    }

    public void remove() throws NoSuchElementException,
                                RepositoryException,
                                PropertyVetoException {
    
        graph.removeStatements(assertion, null, null);
        document.setModified();
    }

    public Date getLastChecked() {
        return lastChecked;
    }

    public Check getCheck() {
        return check;
    }

    public Checker getChecker() {
        return checker;
    }

    public String getName() {
        return element.toString();
    }

    public Check.Category getCategory() {
        
        if (check != null) {
            return check.getCategory();
        }
        return null;
    }

    public void getDescription() {

    }

    public void getSuggestion() {

    }

    @Override
    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        if (element != null) {
            hash = hash * PRIME + element.hashCode(); }
            if (check != null)   {
                hash = hash * PRIME + check.hashCode();
            }
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
        final Issue that = (Issue)obj;
        return (getElement().equals(that.getElement()) &&
                getCheck().equals(that.getCheck()));
    }
}
