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
import java.util.TreeMap;
import java.util.HashMap;

import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XNamedGraph;

import be.docarch.accessodf.Check;
import be.docarch.accessodf.Constants;
import be.docarch.accessodf.Provider;
import be.docarch.accessodf.ooo.URIs;
import be.docarch.accessodf.ooo.Document;

/**
 *
 * @author Bert Frees
 */
public class TestCases {

    private final XNamedGraph graph;
    private final Document doc;

    private Map<String,TestCase> xURIMap = new TreeMap<String,TestCase>();
    private Map<Check,TestCase> checkMap = new HashMap<Check,TestCase>();

    public TestCases(XNamedGraph graph,
                     Document doc) {
        this.graph = graph;
        this.doc = doc;
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
            if (!graph.getStatements(testcase, URIs.RDF_TYPE, URIs.EARL_TESTCASE).hasMoreElements()) { throw new Exception("Not of type earl:TestCase"); }
            if (!testcase.getNamespace().equals(Constants.A11Y_CHECKS)) { throw new Exception("Invalid namespace for check"); }
            Check check = checks.get(testcase.getLocalName());
            if (check == null) { throw new Exception("Check ID not found"); }
            tc = new TestCase(check, testcase);
        }
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
                testcase = URI.createNS(doc.xContext, Constants.A11Y_CHECKS, check.getIdentifier());
                graph.addStatement(testcase, URIs.RDF_TYPE, URIs.EARL_TESTCASE);
            }
            return testcase;
        }
    }
}
