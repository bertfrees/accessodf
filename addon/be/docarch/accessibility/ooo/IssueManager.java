package be.docarch.accessibility.ooo;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Logger;
import java.util.logging.Level;

import java.text.ParseException;
import java.io.IOException;

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

import be.docarch.accessibility.Check;
import be.docarch.accessibility.Checker;
import be.docarch.accessibility.Provider;
import be.docarch.accessibility.CheckProvider;
import be.docarch.accessibility.RunnableChecker;
import be.docarch.accessibility.RemoteRunnableChecker;
import be.docarch.accessibility.Constants;
import be.docarch.accessibility.Report;
import be.docarch.accessibility.Issue;
import be.docarch.accessibility.FilterSorter;
import be.docarch.accessibility.Repairer;
import be.docarch.accessibility.Repairer.RepairMode;

/**
 *
 * @author Bert Frees
 */
public class IssueManager {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();
    
    public static enum Status { ALERT, ERROR, REPAIRED, IGNORED };

    private Document document = null;
    private Settings settings = null;
    private Provider<Checker> checkers = null;
    
    private List<Issue> allIssues = null;
    private FilterSorter filterSorter = null;
    private Issue selectedIssue = null;
    private Check selectedCheck = null;
    private Map<Check,List<Issue>> check2IssuesMap = null;
    private Map<Check,Repairer> check2repairerMap = null;

    public IssueManager(Document document,
                        Provider<Checker> checkers,
                        Provider<Repairer> repairers)
                 throws IllegalArgumentException,
                        NoSuchElementException,
                        RepositoryException,
                        PropertyVetoException,
                        WrappedTargetException,
                        ParseException,
                        com.sun.star.uno.Exception {
    
        logger.entering("IssueManager", "<init>");

        this.document = document;
        this.checkers = checkers;

        settings = new Settings(document.xContext);
        check2repairerMap = new HashMap<Check,Repairer>();

        for (Check check : new CheckProvider(checkers).list()) {
            for (Repairer repairer : repairers.list()) {
                if (repairer.supports(check)) {
                    check2repairerMap.put(check, repairer);
                }
            }
        }

        RDFIssue.initialise(document, checkers);

        check2IssuesMap = new HashMap<Check,List<Issue>>();

        filterSorter = new FilterSorter();
        filterSorter.setOrderPriority(FilterSorter.NAME, true);
        filterSorter.setOrderPriority(FilterSorter.CHECKID, true);
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
                                 ParseException,
                                 IOException,
                                 com.sun.star.uno.Exception {

        settings.loadData();
        File odtFile = null;

        for (Checker checker : checkers.list()) {
            if (checker instanceof RemoteRunnableChecker) {
                if (!settings.brailleChecks() && checker.getIdentifier().startsWith("be.docarch.odt2braille")) {
                    break;
                }
                if (odtFile == null) {
                    odtFile = File.createTempFile(TMP_NAME, ".odt", TMP_DIR);
                    odtFile.deleteOnExit();
                    document.ensureMetadataReferences();
                    document.storeToFile(odtFile);
                }
                RemoteRunnableChecker remoteChecker = (RemoteRunnableChecker)checker;
                remoteChecker.setOdtFile(odtFile);
                if (remoteChecker.run()) {
                    document.removeAccessibilityData(remoteChecker.getIdentifier());
                    Report report = remoteChecker.getAccessibilityReport();
                    if (report != null) {
                        document.importAccessibilityData(report.getFile(), remoteChecker.getIdentifier(), report.getName());
                    }
                }
            } else if (checker instanceof RunnableChecker) {
                ((RunnableChecker)checker).run();
            }
        }

        // Load accessibility issues from metadata
        loadMetadata();
    }

    public void clear() throws IllegalArgumentException,
                               RepositoryException,
                               PropertyVetoException,
                               NoSuchElementException {

        Collection<XNamedGraph> accessibilityDataGraphs = getAccessibilityDataGraphs();

        for (XNamedGraph accessibilityDataGraph : accessibilityDataGraphs) {
            document.xDMA.removeMetadataFile(accessibilityDataGraph.getName());
        }
        if (allIssues.size() > 0) {
            document.setModified();
            allIssues.clear();
        }

        check2IssuesMap.clear();

        select(null);
    }

    private Collection<XNamedGraph> getAccessibilityDataGraphs() throws IllegalArgumentException,
                                                                        RepositoryException {
    
        Collection<XNamedGraph> accessibilityDataGraphs = new ArrayList<XNamedGraph>();
        
        XRepository xRepository = document.xDMA.getRDFRepository();

        for (Checker checker : checkers.list()) {
            XURI type = URI.create(document.xContext, checker.getIdentifier());
            XURI[] graphNames = document.xDMA.getMetadataGraphsWithType(type);
            for (int i=0; i<graphNames.length; i++) {
                accessibilityDataGraphs.add(xRepository.getGraph(graphNames[i]));
            }
        }

        return accessibilityDataGraphs;
    }

    private void loadMetadata() throws NoSuchElementException,
                                       RepositoryException,
                                       WrappedTargetException,
                                       IllegalArgumentException,
                                       PropertyVetoException {

        logger.entering("IssueManager", "loadMetadata");

        Collection<XNamedGraph> accessibilityDataGraphs = getAccessibilityDataGraphs();

        Map<String,Date> checkerDates = new HashMap<String,Date>();
        Date longAgo = new Date(0);
        for (Checker checker : checkers.list()) {
            checkerDates.put(checker.getIdentifier(), longAgo);
        }

        for (XNamedGraph accessibilityDataGraph : accessibilityDataGraphs) {
            XEnumeration assertors = accessibilityDataGraph.getStatements(null, URIs.RDF_TYPE, URIs.EARL_ASSERTOR);
            if (assertors.hasMoreElements()) {
                XURI assertor = URI.create(document.xContext, ((Statement)assertors.nextElement()).Subject.getStringValue());
                Checker checker = checkers.get(assertor.getStringValue());
                if (checker != null) {
                    XEnumeration timestamps = accessibilityDataGraph.getStatements(assertor, URIs.DCT_DATE, null);
                    if (timestamps.hasMoreElements()) {
                        try {
                            Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").parse(((Statement)timestamps.nextElement()).Object.getStringValue());
                            if (date.after(checkerDates.get(checker.getIdentifier()))) {
                                checkerDates.put(checker.getIdentifier(), date);
                            }
                        } catch (ParseException ex) {
                            logger.info("No check date specified for checker");
                        }
                    }
                }
            }
        }

        allIssues.clear();

        Issue issue, sameIssue;
        for (XNamedGraph accessibilityDataGraph : accessibilityDataGraphs) {
            XEnumeration assertions = accessibilityDataGraph.getStatements(null, URIs.RDF_TYPE, URIs.EARL_ASSERTION);
            XResource assertion = null;
            while (assertions.hasMoreElements()) {
                assertion = ((Statement)assertions.nextElement()).Subject;
                try {
                    issue = new RDFIssue(assertion, accessibilityDataGraph);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, null, e);
                    continue;
                }
                if (issue.getElement() != null) {
                    if (!issue.getElement().exists()) {
                        logger.info("Element does not exist, removing issue");
                        issue.remove();
                        continue;
                    }
                }
                if (allIssues.contains(issue)) {
                    sameIssue = allIssues.remove(allIssues.indexOf(issue));
                    if (issue.getCheckDate().before(sameIssue.getCheckDate())) {
                        if (issue.ignored()) {
                            sameIssue.ignored(true);
                        }
                        issue.remove();
                        issue = sameIssue;
                    } else {
                        if (sameIssue.ignored()) {
                            issue.ignored(true);
                        }
                        sameIssue.remove();
                    }
                }
                allIssues.add(issue);
            }
        }

        for (Issue i : allIssues) {
            if (i.getCheckDate().before(checkerDates.get(i.getChecker().getIdentifier()))) {
                i.repaired(true);
            }
        }

        arrangeIssues();

        logger.exiting("IssueManager", "loadMetadata");

    }

    public void select(Object o) {

        selectedCheck = null;
        selectedIssue = null;

        if (o != null) {
            if (o instanceof Check) {                
                Check check = (Check)o;
                for (Check c : getChecks()) {
                    if (c.equals(check)) {
                        selectedCheck = c;
                    }
                }
            } else if (o instanceof Issue) {
                Issue issue = (Issue)o;
                for (Issue i : allIssues) {
                    if (issue.equals(i)) {
                        selectedIssue = i;
                        selectedCheck = selectedIssue.getCheck();
                    }
                }
            }
        }
    }

    public boolean repair(Issue issue)
                   throws IllegalArgumentException,
                          RepositoryException,
                          PropertyVetoException,
                          NoSuchElementException {

        if (issue == null) { return false; }
        Repairer r = check2repairerMap.get(issue.getCheck());
        if (r == null) { return false; }
        boolean succes = r.repair(issue);
        issue.repaired(succes);
        return succes;
    }

    public boolean repairable(Check check) {
        return check2repairerMap.containsKey(check);
    }

    public RepairMode getRepairMode(Check check)
                             throws java.lang.IllegalArgumentException {

        Repairer r = check2repairerMap.get(check);
        if (r == null) { 
            throw new java.lang.IllegalArgumentException();
        }
        return r.getRepairMode(check);
    }

    private void arrangeIssues() {

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

    public Status getStatus(Object o) {

        if (o != null) {
            if (o instanceof Check) {

                Check check = (Check)o;
                boolean alert = (check.getStatus() == Check.Status.ALERT);
                boolean allRepaired = true;
                boolean allIgnored = true;
                for (Issue issue : check2IssuesMap.get(check)) {
                    if (!issue.ignored()) {
                        allIgnored = false;
                    }
                    if (!issue.repaired()) {
                        allRepaired = false;
                    }
                }
                return (allRepaired? Status.REPAIRED:
                        allIgnored?  Status.IGNORED:
                        alert?       Status.ALERT:
                                     Status.ERROR);

            } else if (o instanceof Issue) {

                Issue issue = (Issue)o;
                boolean alert = (issue.getCheck().getStatus() == Check.Status.ALERT);

                return (issue.repaired()? Status.REPAIRED:
                        issue.ignored()?  Status.IGNORED:
                        alert?            Status.ALERT:
                                          Status.ERROR);
            }
        }

        return null;
    }

    public Issue selectedIssue() {
        return selectedIssue;
    }

    public Check selectedCheck() {
        return selectedCheck;
    }

    public void disposing(EventObject event) {}

}
