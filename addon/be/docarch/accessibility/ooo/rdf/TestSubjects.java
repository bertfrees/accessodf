package be.docarch.accessibility.ooo.rdf;

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

    private final XNamedGraph graph;

    public TestSubjects(XNamedGraph graph) {
        this.graph = graph;
    }

    public TestSubject create(Element element) {
        return new TestSubject(element);
    }

    public TestSubject read(XResource subject) throws Exception {

        if (graph.getStatements(subject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT).hasMoreElements()) {
            XEnumeration types = graph.getStatements(subject, URIs.RDF_TYPE, null);
            if (types.hasMoreElements()) {
                String t = ((Statement)types.nextElement()).Object.getStringValue();
                if (t.equals(Constants.A11Y_DOCUMENT)) {
                    return new TestSubject(null);
                } else if (t.equals(Constants.A11Y_PARAGRAPH)) {
                    return new TestSubject(readParagraph(subject));
                } else if (t.equals(Constants.A11Y_SPAN)) {
                    return new TestSubject(readSpan(subject));
                } else if (t.equals(Constants.A11Y_TABLE)) {
                    return new TestSubject(readTable(subject));
                } else if (t.equals(Constants.A11Y_OBJECT)) {
                    return new TestSubject(readDrawObject(subject));
                }
            }
        }

        throw new Exception();
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

        private Element element;

        private TestSubject(Element element) {
            this.element = element;
        }

        public Element getElement() {
            return element;
        }

        public XResource write() throws Exception {

            if (element == null) {
                XResource subject = xRepository.createBlankNode();
                graph.addStatement(subject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
                graph.addStatement(subject, URIs.RDF_TYPE, URIs.A11Y_DOCUMENT);
                return subject;
            } else if (element instanceof Paragraph) {
                return writeParagraph((Paragraph)element);
            } else if (element instanceof Span) {
                return writeSpan((Span)element);
            } else if (element instanceof Table) {
                return writeTable((Table)element);
            } else if (element instanceof DrawObject) {
                return writeDrawObject((DrawObject)element);
            }

            throw new Exception("Invalid element");
        }

        private XResource writeParagraph(Paragraph paragraph) throws Exception {

            XResource testsubject = xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_PARAGRAPH);
            graph.addStatement(testsubject, URIs.A11Y_START, paragraph.getXMetadatable());

            return testsubject;
        }

        private XResource writeSpan(Span span) throws Exception {

            XResource testsubject = xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_SPAN);
            graph.addStatement(testsubject, URIs.A11Y_START, span.getStartXMetadatable());
            graph.addStatement(testsubject, URIs.A11Y_END, span.getEndXMetadatable());

            return testsubject;
        }

        private XResource writeTable(Table table) throws Exception {

            XResource testsubject = xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_TABLE);
            graph.addStatement(testsubject, URIs.DCT_TITLE, Literal.create(xContext, table.getXNamed().getName()));

            return testsubject;
        }

        private XResource writeDrawObject(DrawObject object) throws Exception {

            XResource testsubject = xRepository.createBlankNode();
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.EARL_TESTSUBJECT);
            graph.addStatement(testsubject, URIs.RDF_TYPE, URIs.A11Y_OBJECT);
            graph.addStatement(testsubject, URIs.DCT_TITLE, Literal.create(xContext, object.getXNamed().getName()));

            return testsubject;
        }
    }
}
