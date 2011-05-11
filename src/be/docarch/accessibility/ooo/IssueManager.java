package be.docarch.accessibility.ooo;

import java.util.Date;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.io.File;
import java.text.SimpleDateFormat;

import com.sun.star.lang.EventObject;
import com.sun.star.container.XEnumeration;
import com.sun.star.rdf.Statement;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.XRepository;
import com.sun.star.rdf.XNamedGraph;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.accessibility.URIs;
import be.docarch.accessibility.Check;
import be.docarch.accessibility.Checker;
import be.docarch.accessibility.ExternalChecker;

/**
 *
 * @author Bert Frees
 */
public class IssueManager {

    private final static Logger logger = Logger.getLogger("be.docarch.accessibility");

    public static enum Status { ALERT, ERROR, REPAIRED, IGNORED };

    private XURI RDF_TYPE = null;
    private XURI EARL_ASSERTION = null;
    private XURI CHECKER = null;

    private Document document = null;
    private Settings settings = null;
    private InternalChecker internalChecker = null;
    private List<ExternalChecker> externalCheckers = null;

    private ArrayList<Issue> allIssues = null;
    private FilterSorter filterSorter = null;
    private Issue selectedIssue = null;
    private Check selectedCheck = null;
    private HashMap<Check,List<Issue>> check2IssuesMap = null;


    public IssueManager(Document document,
                        InternalChecker internalChecker,
                        List<ExternalChecker> externalCheckers)
                 throws IllegalArgumentException,
                        NoSuchElementException,
                        RepositoryException,
                        PropertyVetoException,
                        WrappedTargetException,
                        com.sun.star.uno.Exception,
                        java.text.ParseException {
    
        logger.entering("IssueManager", "<init>");

        this.document = document;
        this.internalChecker = internalChecker;
        this.externalCheckers = externalCheckers;
        this.settings = new Settings(document.xContext);

        Checker[] allCheckers = new Checker[1 + externalCheckers.size()];
        allCheckers[0] = internalChecker;
        for (int i=0; i<externalCheckers.size(); i++) {
            allCheckers[i+1] = externalCheckers.get(i);
        }

        Issue.setDocument(document, allCheckers);

        check2IssuesMap = new HashMap<Check,List<Issue>>();

        RDF_TYPE = URI.create(document.xContext, URIs.RDF_TYPE);
        EARL_ASSERTION = URI.create(document.xContext, URIs.EARL_ASSERTION);
        CHECKER = URI.create(document.xContext, URIs.CHECKER);

        filterSorter = new FilterSorter();
        filterSorter.setOrderPriority(FilterSorter.NAME, true);
        filterSorter.setOrderPriority(FilterSorter.CHECKID, true);
        filterSorter.setOrderPriority(FilterSorter.CATEGORY, true);
        allIssues = new ArrayList<Issue>();

        // Load accessibility issues from metadata
        loadMetadata();

        logger.exiting("IssueManager", "<init>");
        
    }

    public void refresh() throws IllegalArgumentException,
                                 RepositoryException,
                                 UnknownPropertyException,
                                 NoSuchElementException,
                                 WrappedTargetException,
                                 PropertyVetoException,
                                 java.text.ParseException,
                                 java.io.IOException,
                                 com.sun.star.uno.Exception {

        // Extract accessibility issues from document and store in metadata
        internalChecker.check();

        // Get external accessibility reports and store in metadata
        settings.loadData();
        if (settings.brailleChecks()) {
            if (externalCheckers.size() > 0) {
                File odtFile = File.createTempFile("accessibility", ".odt");
                odtFile.deleteOnExit();
                document.ensureMetadataReferences();
                document.storeToFile(odtFile);
                for (ExternalChecker checker : externalCheckers) {
                    checker.setOdtFile(odtFile);
                    checker.check();
                    document.importAccessibilityData(checker.getAccessibilityReport(),
                        checker.getIdentifier() + "/" + new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(checker.getLastChecked()) + ".rdf");
                }
            }
        }

        // Load accessibility issues from metadata
        loadMetadata();
    }

    public void clear() throws IllegalArgumentException,
                               RepositoryException,
                               PropertyVetoException,
                               NoSuchElementException {

        XNamedGraph[] accessibilityDataGraphs = getAccessibilityDataGraphs();

        for (XNamedGraph accessibilityDataGraph : accessibilityDataGraphs) {
            document.xDMA.removeMetadataFile(accessibilityDataGraph.getName());
        }
        if (allIssues.size() > 0) {
            document.setModified();
            allIssues.clear();
        }
    }

    private XNamedGraph[] getAccessibilityDataGraphs() throws IllegalArgumentException,
                                                              RepositoryException {
    
        XRepository xRepository = document.xDMA.getRDFRepository();
        XURI[] graphNames = document.xDMA.getMetadataGraphsWithType(CHECKER);
        XNamedGraph[] accessibilityDataGraphs = new XNamedGraph[graphNames.length];
        for (int i=0; i<accessibilityDataGraphs.length; i++) {
            accessibilityDataGraphs[i] = xRepository.getGraph(graphNames[i]);
        }

        return accessibilityDataGraphs;
    }

    private void loadMetadata() throws NoSuchElementException,
                                       RepositoryException,
                                       WrappedTargetException,
                                       IllegalArgumentException,
                                       PropertyVetoException,
                                       java.text.ParseException {

        logger.entering("IssueManager", "loadMetadata");

        XNamedGraph[] accessibilityDataGraphs = getAccessibilityDataGraphs();

        allIssues.clear();

        Issue issue = null;
        Issue sameIssue = null;
        Date lastChecked = null;
        for (XNamedGraph accessibilityDataGraph : accessibilityDataGraphs) {
            XEnumeration assertions = accessibilityDataGraph.getStatements(null, RDF_TYPE, EARL_ASSERTION);
            XResource assertion = null;
            while (assertions.hasMoreElements()) {
                assertion = ((Statement)assertions.nextElement()).Subject;
                issue = new Issue(assertion, accessibilityDataGraph);
                if (issue.isValid()) {
                    if (issue.getElement().exists()) {
                        if (allIssues.contains(issue)) {
                            sameIssue = allIssues.remove(allIssues.indexOf(issue));
                            if (issue.getLastChecked().before(sameIssue.getLastChecked())) {
                                if (issue.isIgnored()) {
                                    sameIssue.setIgnored(true);
                                }
                                issue.remove();
                                issue = sameIssue;
                            } else {
                                if (sameIssue.isIgnored()) {
                                    issue.setIgnored(true);
                                }
                                sameIssue.remove();
                            }
                        }
                        allIssues.add(issue);
                    } else {
                        logger.info("Element does not exist, removing issue");
                        issue.remove();
                    }
                }
            }
        }

        lastChecked = internalChecker.getLastChecked();
        if (lastChecked != null) {
            for (Issue issue2 : allIssues) {
                if (issue2.getLastChecked().before(lastChecked)) {
                    issue2.setRepaired(true);
                }
            }
        }

        logger.exiting("IssueManager", "loadMetadata");

    }

    public void selectIssue(Issue issue) {

        selectedIssue = issue;
        if (issue != null) {
            selectedCheck = issue.getCheck();
        } else {
            selectedCheck = null;
        }
    }

    public void selectCheck(Check check) {

        selectedCheck = check;
        selectedIssue = null;
    }

    public void arrangeIssues() {

        Collections.sort(allIssues, filterSorter);

        check2IssuesMap.clear();

        Check prevCheck = null;
        ArrayList<Issue> issueList = null;
        for (Issue issue : allIssues) {
            Check check = issue.getCheck();
            if (!check.equals(prevCheck)) {
                issueList = new ArrayList<Issue>();
                check2IssuesMap.put(check, issueList);
            }
            issueList.add(issue);
            prevCheck = check;
        }
    }

    public Collection<Check> getChecks() {
        return check2IssuesMap.keySet();
    }

    public List<Issue> getIssuesByCheck(Check check) {
        return check2IssuesMap.get(check);
    }

    public Status getCheckStatus(Check check) {

        boolean alert = (check.getStatus() == Check.Status.ALERT);
        boolean allRepaired = true;
        boolean allIgnored = true;
        for (Issue issue : check2IssuesMap.get(check)) {
            if (!issue.isIgnored()) {
                allIgnored = false;
                if (!issue.isRepaired()) {
                    allRepaired = false;
                    break;
                }
            }
        }
        return (allIgnored?  Status.IGNORED:
                allRepaired? Status.REPAIRED:
                alert?       Status.ALERT:
                             Status.ERROR);
    }

    public Status getIssueStatus(Issue issue) {

        boolean alert = (issue.getCheck().getStatus() == Check.Status.ALERT);
        boolean repaired = issue.isRepaired();
        boolean ignored = issue.isIgnored();

        return (ignored?  Status.IGNORED:
                repaired? Status.REPAIRED:
                alert?    Status.ALERT:
                          Status.ERROR);
    }

    public Issue getSelectedIssue() {
        return selectedIssue;
    }

    public Check getSelectedCheck() {
        return selectedCheck;
    }

    public void disposing(EventObject event) {}

}
