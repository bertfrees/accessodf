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

import com.sun.star.container.XEnumeration;
import com.sun.star.rdf.Statement;
import com.sun.star.rdf.XMetadatable;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XNamedGraph;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.Literal;

import be.docarch.accessodf.Constants;
import be.docarch.accessodf.ooo.URIs;
import be.docarch.accessodf.ooo.Paragraph;
import be.docarch.accessodf.ooo.Span;
import be.docarch.accessodf.ooo.Table;
import be.docarch.accessodf.ooo.DrawObject;
import be.docarch.accessodf.ooo.Document;

import be.docarch.accessodf.Element;

/**
 *
 * @author Bert Frees
 */
public class TestSubjects {

    private static enum Type { PARAGRAPH, SPAN, TABLE, OBJECT, DOCUMENT };

    private final XNamedGraph graph;
    private final Document doc;

    private Map<String,TestSubject> xResourceMap = new TreeMap<String,TestSubject>();
    private Map<Element,TestSubject> elementMap = new HashMap<Element,TestSubject>();
    private TestSubject documentSubject;

    public TestSubjects(XNamedGraph graph,
                        Document doc) {
        this.graph = graph;
        this.doc = doc;
    }

    public TestSubject create(Element element) throws Exception {
        
        TestSubject ts = (element==null) ? documentSubject : elementMap.get(element);
        if (ts == null) {
            ts = new TestSubject(element);
            if (element==null) {
                documentSubject = ts;
            } else {
                elementMap.put(element, ts);
            }
        }
        return ts;
    }

    public TestSubject read(XResource subject) throws Exception {

        TestSubject ts = xResourceMap.get(subject.getStringValue());
        if (ts == null) {
            if (!graph.getStatements(subject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT).hasMoreElements()) { throw new Exception("Not of type earl:TestSubject"); }
            XEnumeration types = graph.getStatements(subject, URIs.RDF_TYPE, null);
            if (!types.hasMoreElements()) { throw new Exception("No rdf:type statement"); }
            String t = ((Statement)types.nextElement()).Object.getStringValue();
            if (t.equals(Constants.A11Y_DOCUMENT)) {
                ts = new TestSubject(null, subject);
            } else if (t.equals(Constants.A11Y_PARAGRAPH)) {
                ts = new TestSubject(readParagraph(subject), subject);
            } else if (t.equals(Constants.A11Y_SPAN)) {
                ts = new TestSubject(readSpan(subject), subject);
            } else if (t.equals(Constants.A11Y_TABLE)) {
                ts = new TestSubject(readTable(subject), subject);
            } else if (t.equals(Constants.A11Y_OBJECT)) {
                ts = new TestSubject(readDrawObject(subject), subject);
            } else {
                throw new Exception("Invalid type of TestSubject");
            }
        }
        xResourceMap.put(subject.getStringValue(), ts);
        return ts;
    }

    private Paragraph readParagraph(XResource subject) throws Exception {

        XEnumeration paragraphs = graph.getStatements(subject, URIs.A11Y_START, null);
        if (!paragraphs.hasMoreElements()) { throw new Exception("No start statement"); }
        XURI paragraph = URI.create(doc.xContext, ((Statement)paragraphs.nextElement()).Object.getStringValue());
        XMetadatable metadatable = doc.xDMA.getElementByURI(paragraph);
        if (metadatable == null) { throw new Exception("Cannot find paragraph"); }
        return new Paragraph(metadatable, doc);
    }

    private Span readSpan(XResource subject) throws Exception {

        XEnumeration starts = graph.getStatements(subject, URIs.A11Y_START, null);
        XEnumeration ends = graph.getStatements(subject, URIs.A11Y_END, null);
        
        if (!starts.hasMoreElements() || !ends.hasMoreElements()) { throw new Exception("No start or end statement"); }
        XURI start = URI.create(doc.xContext, ((Statement)starts.nextElement()).Object.getStringValue());
        XURI end = URI.create(doc.xContext, ((Statement)ends.nextElement()).Object.getStringValue());
        XMetadatable startElement = doc.xDMA.getElementByURI(start);
        XMetadatable endElement = doc.xDMA.getElementByURI(end);
        if (startElement == null || endElement == null) { throw new Exception("Cannot find span"); }
        return new Span(startElement, endElement, doc);
    }

    private Table readTable(XResource subject) throws Exception {

        XEnumeration names = graph.getStatements(subject, URIs.DCT_TITLE, null);
        if (!names.hasMoreElements()) { throw new Exception("No dct:title statement"); }
        return new Table(((Statement)names.nextElement()).Object.getStringValue(), doc);
    }

    private DrawObject readDrawObject(XResource subject) throws Exception {

        XEnumeration names = graph.getStatements(subject, URIs.DCT_TITLE, null);
        if (!names.hasMoreElements()) { throw new Exception("No dct:title statement"); }
        return new DrawObject(((Statement)names.nextElement()).Object.getStringValue(), doc);
    }
    
    public class TestSubject {


        private final Element element;
        private final Type type;
        private XResource testsubject;

        private TestSubject(Element element)
                     throws Exception {

            this.element = element;
            if (element == null) {
                type = Type.DOCUMENT;
            } else if (element instanceof Paragraph) {
                type = Type.PARAGRAPH;
            } else if (element instanceof Span) {
                type = Type.SPAN;
            } else if (element instanceof Table) {
                type = Type.TABLE;
            } else if (element instanceof DrawObject) {
                type = Type.OBJECT;
            } else {
                throw new Exception("Invalid element");
            }
        }

        private TestSubject(Element element,
                            XResource testsubject)
                     throws Exception {

            this(element);
            this.testsubject = testsubject;
        }

        public Element getElement() {
            return element;
        }

        public XResource write() throws Exception {

            if (testsubject == null) {
                switch (type) {
                    case DOCUMENT: writeDocument(); break;
                    case PARAGRAPH: writeParagraph((Paragraph)element); break;
                    case SPAN: writeSpan((Span)element); break;
                    case TABLE: writeTable((Table)element); break;
                    case OBJECT: writeDrawObject((DrawObject)element); break;
                }
            }
            return testsubject;
        }

        private void writeDocument() throws Exception {
        
            testsubject = doc.xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
      //graph.addStatement(URIs.A11Y_DOCUMENT, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_DOCUMENT);
        }

        private void writeParagraph(Paragraph paragraph) throws Exception {

            testsubject = doc.xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
          //graph.addStatement(URIs.A11Y_PARAGRAPH, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_PARAGRAPH);
            graph.addStatement(testsubject, URIs.A11Y_START, paragraph.getXMetadatable());
        }

        private void writeSpan(Span span) throws Exception {

            testsubject = doc.xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
          //graph.addStatement(URIs.A11Y_SPAN, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_SPAN);
            graph.addStatement(testsubject, URIs.A11Y_START, span.getStartXMetadatable());
            graph.addStatement(testsubject, URIs.A11Y_END, span.getEndXMetadatable());
        }

        private void writeTable(Table table) throws Exception {

            testsubject = doc.xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
          //graph.addStatement(URIs.A11Y_TABLE, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_TABLE);
            graph.addStatement(testsubject, URIs.DCT_TITLE, Literal.create(doc.xContext, table.getXNamed().getName()));
        }

        private void writeDrawObject(DrawObject object) throws Exception {

            testsubject = doc.xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
          //graph.addStatement(URIs.A11Y_OBJECT, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_OBJECT);
            graph.addStatement(testsubject, URIs.DCT_TITLE, Literal.create(doc.xContext, object.getXNamed().getName()));
        }
    }
}
