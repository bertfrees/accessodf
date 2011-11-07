/**
 *  AccessODF - Accessibility checker for OpenOffice.org and LibreOffice Writer.
 *
 *  Copyright (c) 2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.accessodf.ooo.rdf;

import java.util.Map;
import java.util.HashMap;
import java.util.TreeMap;

import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XNamedGraph;

import be.docarch.accessodf.Checker;
import be.docarch.accessodf.Provider;
import be.docarch.accessodf.ooo.URIs;
import be.docarch.accessodf.ooo.Document;

/**
 *
 * @author Bert Frees
 */
public class Assertors {

    private final XNamedGraph graph;
    private final Document doc;

    private Map<String,Assertor> xURIMap = new TreeMap<String,Assertor>();
    private Map<Checker,Assertor> checkerMap = new HashMap<Checker,Assertor>();

    public Assertors(XNamedGraph graph,
                     Document doc) {
        this.graph = graph;
        this.doc = doc;
    }

    public Assertor create(Checker checker) {
        Assertor a = checkerMap.get(checker);
        if (a == null) {
            a = new Assertor(checker);
            checkerMap.put(checker, a);
        }
        return a;
    }

    public Assertor read(XURI assertor,
                         Provider<Checker> checkers)
                  throws Exception {

        Assertor a = xURIMap.get(assertor.getStringValue());
        if (a == null) {
            if (!graph.getStatements(assertor, URIs.RDF_TYPE, URIs.EARL_ASSERTOR).hasMoreElements()) { throw new Exception("Not of type earl:Assertor"); }
            if (!graph.getStatements(assertor, URIs.RDF_TYPE, URIs.A11Y_CHECKER).hasMoreElements()) { throw new Exception("Not of type Checker"); }
            Checker checker = checkers.get(assertor.getStringValue());
            if (checker == null) { throw new Exception("Checker ID not found"); }
            a = new Assertor(checker, assertor);
        }
        xURIMap.put(assertor.getStringValue(), a);
        return a;
    }

    public class Assertor {

        private final Checker checker;
        private XURI assertor;

        private Assertor(Checker checker) {
            this.checker = checker;
        }

        private Assertor(Checker checker,
                         XURI assertor) {

            this(checker);
            this.assertor = assertor;
        }

        public Checker getChecker() {
            return checker;
        }

        public XURI write() throws Exception {

            if (assertor == null) {
                assertor = URI.create(doc.xContext, checker.getIdentifier());
                graph.addStatement(assertor, URIs.RDF_TYPE, URIs.EARL_ASSERTOR);
              //graph.addStatement(URIs.A11Y_CHECKER, URIs.RDFS_SUBCLASSOF, URIs.EARL_ASSERTOR); // in plaats van vorige ?
                graph.addStatement(assertor, URIs.RDF_TYPE, URIs.A11Y_CHECKER);
              //graph.addStatement(assertor, URIs.DCT_DATE, Literal.create(xContext, dateFormat.format(lastChecked))); // terug invoeren !
            }
            return assertor;
        }
    }
}
