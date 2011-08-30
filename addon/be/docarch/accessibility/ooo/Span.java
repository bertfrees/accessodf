package be.docarch.accessibility.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;
import com.sun.star.rdf.XMetadatable;

/**
 *
 * @author Bert Frees
 */
public class Span extends FocusableElement {

    private final XTextContent startXTextContent;
    private final XTextContent endXTextContent;

    private final XMetadatable startXMetadatable;
    private final XMetadatable endXMetadatable;

    private XTextCursor cursor;

    private String startId;
    private String endId = "";
    private String sample = "";

    public Span(XTextContent start,
                XTextContent end)
         throws Exception {

        if (start != null && end!= null) {
            startXTextContent = start;
            endXTextContent = end;
            startXMetadatable = (XMetadatable)UnoRuntime.queryInterface(XMetadatable.class, startXTextContent);
            endXMetadatable = (XMetadatable)UnoRuntime.queryInterface(XMetadatable.class, endXTextContent);
            if (startXMetadatable != null && endXMetadatable != null) {
                startXMetadatable.ensureMetadataReference();
                endXMetadatable.ensureMetadataReference();
                init();
                return;
            }
        }
        throw new Exception();
    }

    public Span(XMetadatable start,
                XMetadatable end)
         throws Exception {

         if (start != null && end != null) {
            startXMetadatable = start;
            endXMetadatable = end;
            startXMetadatable.ensureMetadataReference();
            endXMetadatable.ensureMetadataReference();
            startXTextContent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, startXMetadatable);
            endXTextContent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, endXMetadatable);
            if (startXTextContent != null && endXTextContent != null) {
                init();
                return;
            }
        }
        throw new Exception();
    }

    private void init() throws Exception {

        startId = startXMetadatable.getMetadataReference().Second;
        endId = endXMetadatable.getMetadataReference().Second;
        if (((XServiceInfo)UnoRuntime.queryInterface(
                  XServiceInfo.class, startXTextContent)).supportsService("com.sun.star.text.InContentMetadata") &&
                ((XServiceInfo)UnoRuntime.queryInterface(
                  XServiceInfo.class, endXTextContent)).supportsService("com.sun.star.text.InContentMetadata")) {
            cursor = startXTextContent.getAnchor().getEnd().getText().createTextCursorByRange(startXTextContent.getAnchor().getEnd());
            cursor.gotoRange(endXTextContent.getAnchor().getStart(), true);
            sample = cursor.getString();
            if (sample.length() > 30) {
                sample = sample.substring(0, 30) + "\u2026";
            }
            return;
        }
        throw new Exception();

    }

    public XTextCursor getXTextCursor() {
        return cursor;
    }

    public XMetadatable getStartXMetadatable() {
        return startXMetadatable;
    }

    public XMetadatable getEndXMetadatable() {
        return endXMetadatable;
    }

    public String toString() {
        return sample;
    }

    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = hash * PRIME + startId.hashCode();
        hash = hash * PRIME + endId.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {

        if (this == obj) { return true; }
        if (obj == null) {return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Span that = (Span)obj;
        return (this.startId.equals(that.startId) &&
                this.endId.equals(that.endId));
    }

    @Override
    public boolean focus() {

        if (cursor != null) {
            viewCursor.gotoRange(cursor, false);
            return true;
        }
        return false;
    }
}
