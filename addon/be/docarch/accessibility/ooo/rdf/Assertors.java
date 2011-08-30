package be.docarch.accessibility.ooo.rdf;

import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XNamedGraph;

import be.docarch.accessibility.Checker;
import be.docarch.accessibility.Provider;
import be.docarch.accessibility.ooo.URIs;

/**
 *
 * @author Bert Frees
 */
public class Assertors extends RDFClass {

    private final XNamedGraph graph;

    // + code om dubbel uitlezen/schrijven te voorkomen => checkerURIs ?

    public Assertors(XNamedGraph graph) {        
        this.graph = graph;
    }

    public Assertor create(Checker checker) {
        return new Assertor(checker);
    }

    public Assertor read(XURI assertor,
                         Provider<Checker> checkers)
                  throws Exception {

        if (graph.getStatements(assertor, URIs.RDF_TYPE, URIs.EARL_ASSERTOR).hasMoreElements()) {
            Checker checker = checkers.get(assertor.getStringValue());
            if (checker != null) {
                return new Assertor(checker);
            }
        }
        throw new Exception();
    }

    public class Assertor {

        private Checker checker;
        private XURI assertor = null;

        private Assertor(Checker checker) {
            this.checker = checker;
        }

        public Checker getChecker() {
            return checker;
        }

        public XURI write() throws Exception {

            assertor = URI.create(xContext, checker.getIdentifier());
            graph.addStatement(assertor, URIs.RDF_TYPE, URIs.EARL_ASSERTOR);
            graph.addStatement(assertor, URIs.RDF_TYPE, URIs.A11Y_CHECKER);
          //graph.addStatement(assertor, URIs.DCT_DATE, Literal.create(xContext, dateFormat.format(lastChecked)));

            return assertor;
        }
    }
}
