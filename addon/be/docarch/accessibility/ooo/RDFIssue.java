package be.docarch.accessibility.ooo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.sun.star.container.XEnumeration;
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
import com.sun.star.lang.IllegalArgumentException;

import be.docarch.accessibility.Check;
import be.docarch.accessibility.Element;
import be.docarch.accessibility.Issue;
import be.docarch.accessibility.Constants;
import be.docarch.accessibility.Provider;
import be.docarch.accessibility.CheckProvider;
import be.docarch.accessibility.Checker;

/**
 *
 * @author Bert Frees
 */
public class RDFIssue extends Issue {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    
    private static Document document = null;

    private static Provider<Check> checks = null;
    private static Provider<Checker> checkers = null;

    private XNamedGraph graph = null;
    private XResource assertion = null;
    private XResource testresult = null;
    private XURI testcase = null;
    private XURI assertor = null;

    private Element element = null;
    private Check check = null;
    private Checker checker = null;
    private boolean ignored = false;
    private boolean repaired = false;
    private Date checkDate = null;

    static void initialise(Document document,
                           Provider<Checker> checkers)
                    throws IllegalArgumentException {

        RDFIssue.document = document;

        RDFIssue.checkers = checkers;
        RDFIssue.checks = new CheckProvider(checkers);

        FocusableElement.initialise(document);
    }

    public RDFIssue(XResource assertion,
                    XNamedGraph graph)
             throws IllegalArgumentException,
                    RepositoryException,
                    NoSuchElementException,
                    WrappedTargetException,
                    Exception {

        logger.entering("Issue", "<init>");

        boolean valid = false;

        try {

            if (!graph.getStatements(assertion, URIs.RDF_TYPE, URIs.EARL_ASSERTION).hasMoreElements()) {
                return;
            }
            this.graph = graph;
            this.assertion = assertion;
            XEnumeration assertors = graph.getStatements(assertion, URIs.EARL_ASSERTEDBY, null);
            XEnumeration testsubjects = graph.getStatements(assertion, URIs.EARL_SUBJECT, null);
            XEnumeration testcases = graph.getStatements(assertion, URIs.EARL_TEST, null);
            XEnumeration testresults = graph.getStatements(assertion, URIs.EARL_RESULT, null);
            if (!assertors.hasMoreElements() ||
                !testsubjects.hasMoreElements() ||
                !testcases.hasMoreElements() ||
                !testresults.hasMoreElements()) {
                return;
            }
            testcase = URI.create(document.xContext, ((Statement)testcases.nextElement()).Object.getStringValue());
            testresult = BlankNode.create(document.xContext, ((Statement)testresults.nextElement()).Object.getStringValue());
            assertor = URI.create(document.xContext, ((Statement)assertors.nextElement()).Object.getStringValue());
            XResource testsubject = BlankNode.create(document.xContext, ((Statement)testsubjects.nextElement()).Object.getStringValue());
            if (!graph.getStatements(testresult, URIs.EARL_OUTCOME, null).hasMoreElements() ||
                !graph.getStatements(testresult, URIs.RDF_TYPE, URIs.EARL_TESTRESULT).hasMoreElements() ||
                !graph.getStatements(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT).hasMoreElements() ||
                !graph.getStatements(testcase, URIs.RDF_TYPE, URIs.EARL_TESTCASE).hasMoreElements() ||
                !graph.getStatements(assertor, URIs.RDF_TYPE, URIs.EARL_ASSERTOR).hasMoreElements()) {
                return;
            }
            if (graph.getStatements(testresult, URIs.EARL_OUTCOME, URIs.EARL_PASSED).hasMoreElements()) {
                repaired = true;
            }
            
            XEnumeration types = graph.getStatements(testsubject, URIs.RDF_TYPE, null);            
            if (!types.hasMoreElements()) {
                return;
            }
            
            String t = ((Statement)types.nextElement()).Object.getStringValue();
            if (t.equals(Constants.A11Y_DOCUMENT)) {
                element = null;
            } else if (t.equals(Constants.A11Y_PARAGRAPH)) {
                element = new Paragraph(testsubject);
            } else if (t.equals(Constants.A11Y_SPAN)) {
                element = new Span(testsubject);
            } else if (t.equals(Constants.A11Y_TABLE)) {
                element = new Table(testsubject);
            } else if (t.equals(Constants.A11Y_OBJECT)) {
                element = new DrawObject(testsubject);
            } else {
                return;
            }

            check = checks.get(testcase.getLocalName());
            if (check == null) {
                return;
            }

            checker = checkers.get(assertor.getStringValue());
            if (checker == null) {
                return;
            }

            XEnumeration timestamps = graph.getStatements(testresult, URIs.DCT_DATE, null);
            if (!timestamps.hasMoreElements()) {
                return;
            }
            checkDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(
                    ((Statement)timestamps.nextElement()).Object.getStringValue());

            valid = true;
            XEnumeration ignore = graph.getStatements(testresult, URIs.A11Y_IGNORE, null);
            if (ignore.hasMoreElements()) {
                if (((Statement)ignore.nextElement()).Object.getStringValue().equals("true")) {
                    ignored = true;
                }
            }
        
        } catch (ParseException e) {
            logger.log(Level.SEVERE, null, e);
        } finally {
            if (!valid) { throw new Exception("Invalid assertion"); }
            logger.exiting("Issue", "<init>");
        }
    }

    public Element getElement() {
        return element;
    }

    public boolean ignored() {
        return ignored;
    }

    public boolean repaired() {
        return repaired;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public Check getCheck() {
        return check;
    }

    public Checker getChecker() {
        return checker;
    }

    public void ignored(boolean ignored) {

        try {

            this.ignored = ignored;
            graph.removeStatements(testresult, URIs.A11Y_IGNORE, null);
            if (ignored) {
                graph.addStatement(testresult, URIs.A11Y_IGNORE, Literal.create(document.xContext, "true"));
            }
            document.setModified();

        } catch (IllegalArgumentException e) {
        } catch (RepositoryException e) {
        } catch (NoSuchElementException e) {
        }
    }

    public void repaired(boolean repaired) {

        try {

            this.repaired = repaired;
            graph.removeStatements(testresult, URIs.EARL_OUTCOME, null);
            if (repaired) {
                graph.addStatement(testresult, URIs.EARL_OUTCOME, URIs.EARL_PASSED);
            } else {
                graph.addStatement(testresult, URIs.EARL_OUTCOME, URIs.EARL_FAILED);
            }
            document.setModified();

        } catch (IllegalArgumentException e) {
        } catch (RepositoryException e) {
        } catch (NoSuchElementException e) {
        }
    }

    public void remove() {

        try {

            graph.removeStatements(assertion, null, null);
            document.setModified();

        } catch (NoSuchElementException e) {
        } catch (RepositoryException e) {
        }
    }
}
