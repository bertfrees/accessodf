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
 
 package be.docarch.accessodf.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.text.XTextContent;
import com.sun.star.rdf.XMetadatable;

import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class Paragraph extends FocusableElement {

    private final XTextContent xTextContent;
    private final XMetadatable metadatable;
    private final Document doc;
    private String sample = "";
    private String id = "";

    public Paragraph(XTextContent textContent,
                     Document doc)
              throws Exception {
        
        if (textContent == null || doc == null) { throw new IllegalArgumentException(); }
        this.doc = doc;
        xTextContent = textContent;
        metadatable = (XMetadatable)UnoRuntime.queryInterface(XMetadatable.class, xTextContent);
        if (metadatable == null) { throw new Exception("Cannot cast to XMetadatable"); }
        metadatable.ensureMetadataReference();
        init();
    }

    public Paragraph(XMetadatable metadatable,
                     Document doc)
              throws Exception {

        if (metadatable == null || doc == null) { throw new IllegalArgumentException(); }
        this.doc = doc;
        this.metadatable = metadatable;
        metadatable.ensureMetadataReference();
        xTextContent = (XTextContent)UnoRuntime.queryInterface(XTextContent.class, metadatable);
        if (xTextContent == null) { throw new Exception("Cannot cast to XTextContent"); }
        init();
    }

    private void init() throws Exception {

        id = metadatable.getMetadataReference().Second;
        if (!((XServiceInfo)UnoRuntime.queryInterface(
               XServiceInfo.class, xTextContent)).supportsService("com.sun.star.text.Paragraph")) {
            throw new Exception("Does not support service com.sun.star.text.Paragraph");
        }
        sample = xTextContent.getAnchor().getString();
        if (sample.length() > 30) {
            sample = sample.substring(0, 30) + "\u2026";
        }
    }

    public XTextContent getXTextContent() {
        return xTextContent;
    }

    public XMetadatable getXMetadatable() {
        return metadatable;
    }

    public String toString() {
        return sample;
    }

    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = PRIME + id.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Paragraph that = (Paragraph)obj;
        return (this.id.equals(that.id));
    }

    @Override
    public boolean focus() {

        try {
            if (xTextContent != null) {
                return doc.selectionSupplier.select(xTextContent.getAnchor());
            }
        } catch (IllegalArgumentException e) {
        }
        
        return false;
    }
}
