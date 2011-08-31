package be.docarch.accessibility.ooo.rdf;

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

import be.docarch.accessibility.Constants;
import be.docarch.accessibility.ooo.URIs;
import be.docarch.accessibility.ooo.Paragraph;
import be.docarch.accessibility.ooo.Span;
import be.docarch.accessibility.ooo.Table;
import be.docarch.accessibility.ooo.DrawObject;

import be.docarch.accessibility.Element;

/**
 *
 * @author Bert Frees
 */
public class TestSubjects extends RDFClass {

    private static enum Type { PARAGRAPH, SPAN, TABLE, OBJECT, DOCUMENT };

    private final XNamedGraph graph;

    private Map<String,TestSubject> xResourceMap = new TreeMap<String,TestSubject>();
    private Map<Element,TestSubject> elementMap = new HashMap<Element,TestSubject>();
    private TestSubject documentSubject;

    public TestSubjects(XNamedGraph graph) {
        this.graph = graph;
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
            if (graph.getStatements(subject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT).hasMoreElements()) {
                XEnumeration types = graph.getStatements(subject, URIs.RDF_TYPE, null);
                if (types.hasMoreElements()) {
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
                    }
                }
            }
        }
        if (ts == null) { throw new Exception(); }
        xResourceMap.put(subject.getStringValue(), ts);
        return ts;
    }

    private Paragraph readParagraph(XResource subject) throws Exception {

        XEnumeration paragraphs = graph.getStatements(subject, URIs.A11Y_START, null);
        if (paragraphs.hasMoreElements()) {
            XURI paragraph = URI.create(xContext, ((Statement)paragraphs.nextElement()).Object.getStringValue());
            XMetadatable metadatable = xDMA.getElementByURI(paragraph);
            if (metadatable != null) {
                return new Paragraph(metadatable);
            }
        }

        throw new Exception();
    }

    private Span readSpan(XResource subject) throws Exception {

        XEnumeration starts = graph.getStatements(subject, URIs.A11Y_START, null);
        XEnumeration ends = graph.getStatements(subject, URIs.A11Y_END, null);
        if (starts.hasMoreElements() && ends.hasMoreElements()) {
            XURI start = URI.create(xContext, ((Statement)starts.nextElement()).Object.getStringValue());
            XURI end = URI.create(xContext, ((Statement)ends.nextElement()).Object.getStringValue());
            XMetadatable startElement = xDMA.getElementByURI(start);
            XMetadatable endElement = xDMA.getElementByURI(end);
            if (startElement != null && endElement != null) {
                return new Span(startElement, endElement);
            }
        }

        throw new Exception();
    }

    private Table readTable(XResource subject) throws Exception {

        XEnumeration names = graph.getStatements(subject, URIs.DCT_TITLE, null);
        if (names.hasMoreElements()) {
            return new Table(((Statement)names.nextElement()).Object.getStringValue());
        }

        throw new Exception();
    }

    private DrawObject readDrawObject(XResource subject) throws Exception {

        XEnumeration names = graph.getStatements(subject, URIs.DCT_TITLE, null);
        if (names.hasMoreElements()) {
            return new DrawObject(((Statement)names.nextElement()).Object.getStringValue());
        }

        throw new Exception();
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
                    case DOCUMENT:
                        testsubject = xRepository.createBlankNode();
                        graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
                  //graph.addStatement(URIs.A11Y_DOCUMENT, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
                        graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_DOCUMENT);
                        break;
                    case PARAGRAPH: writeParagraph((Paragraph)element); break;
                    case SPAN: writeSpan((Span)element); break;
                    case TABLE: writeTable((Table)element); break;
                    case OBJECT: writeDrawObject((DrawObject)element); break;
                }
            }
            return testsubject;
        }

        private void writeParagraph(Paragraph paragraph) throws Exception {

            testsubject = xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
          //graph.addStatement(URIs.A11Y_PARAGRAPH, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_PARAGRAPH);
            graph.addStatement(testsubject, URIs.A11Y_START, paragraph.getXMetadatable());
        }

        private void writeSpan(Span span) throws Exception {

            testsubject = xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
          //graph.addStatement(URIs.A11Y_SPAN, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_SPAN);
            graph.addStatement(testsubject, URIs.A11Y_START, span.getStartXMetadatable());
            graph.addStatement(testsubject, URIs.A11Y_END, span.getEndXMetadatable());
        }

        private void writeTable(Table table) throws Exception {

            testsubject = xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
          //graph.addStatement(URIs.A11Y_TABLE, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_TABLE);
            graph.addStatement(testsubject, URIs.DCT_TITLE, Literal.create(xContext, table.getXNamed().getName()));
        }

        private void writeDrawObject(DrawObject object) throws Exception {

            testsubject = xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
          //graph.addStatement(URIs.A11Y_OBJECT, URIs.RDFS_SUBCLASSOF, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_OBJECT);
            graph.addStatement(testsubject, URIs.DCT_TITLE, Literal.create(xContext, object.getXNamed().getName()));
        }
    }
}
