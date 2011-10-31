package be.docarch.accessibility.ooo;

import java.io.File;
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
import be.docarch.accessibility.DummyCheck;
import be.docarch.accessibility.Checker;
import be.docarch.accessibility.Provider;
import be.docarch.accessibility.RunnableChecker;
import be.docarch.accessibility.RemoteRunnableChecker;
import be.docarch.accessibility.Constants;
import be.docarch.accessibility.Report;
import be.docarch.accessibility.Issue;
import be.docarch.accessibility.FilterSorter;
import be.docarch.accessibility.Repairer;
import be.docarch.accessibility.Repairer.RepairMode;
import be.docarch.accessibility.ooo.rdf.*;

/**
 *
 * @author Bert Frees
 */
public class IssueManager {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();
    
    public static enum Status { ALERT,
                                ERROR,
                                REPAIRED,
                                IGNORED };

    private final Document document;
    private final Settings settings;
    private final Provider<Checker> checkers;
    private final Provider<Repairer> repairers;

    private final List<Issue> allIssues;
    private FilterSorter filterSorter = null;
    private Issue selectedIssue = null;
    private Check selectedCheck = null;
    private Map<Check,List<Issue>> check2IssuesMap = null;
  //private Map<Check,Repairer> check2repairerMap = null;

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
        this.repairers = repairers;
      //Provider<Check> checks = new CheckProvider(checkers);

        settings = new Settings(document.xContext);
      //check2repairerMap = new HashMap<Check,Repairer>();
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
                if (!settings.brailleChecks() && checker.getIdentifier().equals("http://docarch.be/odt2braille/checker/BrailleChecker")) {
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

        allIssues.clear();

        Issue issue, sameIssue;
        for (XNamedGraph graph : accessibilityDataGraphs) {
            Assertions assertions = new Assertions(graph, document);
            XEnumeration assertionEnum = graph.getStatements(null, URIs.RDF_TYPE, URIs.EARL_ASSERTION);
            XResource assertion = null;
            while (assertionEnum.hasMoreElements()) {
                assertion = ((Statement)assertionEnum.nextElement()).Subject;
                try {
                    issue = assertions.read(assertion, checkers).getIssue();
                } catch (Exception e) {
                    logger.log(Level.SEVERE, null, e);
                    continue;
                }
                if (issue.getCheck() instanceof DummyCheck) {
                    checkerDates.put(issue.getChecker().getIdentifier(), issue.getCheckDate());
                    break;
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
            String checker = i.getChecker().getIdentifier();
            if (i.getCheckDate().after(checkerDates.get(checker))) {
                checkerDates.put(checker, i.getCheckDate());
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
        for (Repairer repairer : repairers.list()) {
            try {
                if (repairer.repair(issue)) {
                    issue.repaired(true);
                    return true;
                }
            } catch (Exception e) {
            }
        }
        return false;
    }

    public boolean repairable(Issue issue) {

        for (Repairer repairer : repairers.list()) {
            if (repairer.supports(issue)) {
                return true;
            }
        }
        return false;
    }

    public RepairMode getRepairMode(Issue issue)
                             throws java.lang.IllegalArgumentException {

        for (Repairer repairer : repairers.list()) {
            if (repairer.supports(issue)) {
                return repairer.getRepairMode(issue);
            }
        }
        throw new java.lang.IllegalArgumentException();
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
