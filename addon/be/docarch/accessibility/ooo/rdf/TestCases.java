package be.docarch.accessibility.ooo.rdf;

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

    // + code om dubbel uitlezen/schrijven te voorkomen => checkURIs ?

    public TestCases(XNamedGraph graph) {

        this.graph = graph;
    }

    public TestCase create(Check check) {
        return new TestCase(check);
    }

    public TestCase read(XURI testcase,
                         Provider<Check> checks)
                  throws Exception {

        if (graph.getStatements(testcase, URIs.RDF_TYPE, URIs.EARL_TESTCASE).hasMoreElements()) {
            if (testcase.getNamespace().equals(Constants.A11Y_CHECKS)) {
                Check check = checks.get(testcase.getLocalName());
                if (check != null) {
                    return new TestCase(check);
                }
            }
        }

        throw new Exception();
    }

    public class TestCase {

        private Check check;
        private XURI testcase = null;

        private TestCase(Check check) {
            this.check = check;
        }

        public Check getCheck() {
            return check;
        }

        public XURI write() throws Exception {

            testcase = URI.createNS(xContext, Constants.A11Y_CHECKS, check.getIdentifier());
            graph.addStatement(testcase, URIs.RDF_TYPE, URIs.EARL_TESTCASE);

            return testcase;
        }
    }
}
