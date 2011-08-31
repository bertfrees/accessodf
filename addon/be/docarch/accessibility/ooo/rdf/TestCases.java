package be.docarch.accessibility.ooo.rdf;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;

import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XNamedGraph;

import be.docarch.accessibility.Check;
import be.docarch.accessibility.Constants;
import be.docarch.accessibility.Provider;
import be.docarch.accessibility.ooo.URIs;

/**
 *
 * @author Bert Frees
 */
public class TestCases extends RDFClass {

    private final XNamedGraph graph;

    private Map<String,TestCase> xURIMap = new TreeMap<String,TestCase>();
    private Map<Check,TestCase> checkMap = new HashMap<Check,TestCase>();

    public TestCases(XNamedGraph graph) {
        this.graph = graph;
    }

    public TestCase create(Check check) {
        TestCase tc = checkMap.get(check);
        if (tc == null) {
            tc = new TestCase(check);
            checkMap.put(check, tc);
        }
        return tc;
    }

    public TestCase read(XURI testcase,
                         Provider<Check> checks)
                  throws Exception {

        TestCase tc = xURIMap.get(testcase.getStringValue());
        if (tc == null) {
            if (graph.getStatements(testcase, URIs.RDF_TYPE, URIs.EARL_TESTCASE).hasMoreElements()) {
                if (testcase.getNamespace().equals(Constants.A11Y_CHECKS)) {
                    Check check = checks.get(testcase.getLocalName());
                    if (check != null) {
                        tc = new TestCase(check, testcase);
                    }
                }
            }
        }
        if (tc == null) { throw new Exception(); }
        xURIMap.put(testcase.getStringValue(), tc);
        return tc;
    }

    public class TestCase {

        private final Check check;
        private XURI testcase;

        private TestCase(Check check) {
            this.check = check;
        }

        private TestCase(Check check,
                         XURI testcase) {
            
            this(check);
            this.testcase = testcase;
        }

        public Check getCheck() {
            return check;
        }

        public XURI write() throws Exception {

            if (testcase == null) {
                testcase = URI.createNS(xContext, Constants.A11Y_CHECKS, check.getIdentifier());
                graph.addStatement(testcase, URIs.RDF_TYPE, URIs.EARL_TESTCASE);
            }
            return testcase;
        }
    }
}
