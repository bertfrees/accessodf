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
import com.sun.star.container.XNamed;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class DrawObject extends FocusableElement {

    private final Document doc;
    private XNamed xNamed = null;

    public DrawObject(String name,
                      Document doc)
               throws Exception {

        if (name == null || doc == null) { throw new IllegalArgumentException(); }
        this.doc = doc;
        Object o = null;
        try {
            o = doc.embeddedObjects.getByName(name);
        } catch (NoSuchElementException e) {
            try {
                o = doc.graphicObjects.getByName(name);
            } catch (NoSuchElementException ee) {
            }
        }
        if (o == null) { throw new Exception("No object found with name " + name); }
        xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class, o);
    }

    public DrawObject(XNamed xNamed,
                      Document doc)
               throws IllegalArgumentException {

        if (xNamed == null || doc == null) { throw new IllegalArgumentException(); }
        this.doc = doc;
        this.xNamed = xNamed;
    }

    public XNamed getXNamed() {
        return xNamed;
    }

    public String toString() {
        return xNamed.getName();
    }

    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = hash * PRIME + toString().hashCode();
        return hash;
    }

    public boolean equals(Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final DrawObject that = (DrawObject)obj;
        return (this.toString().equals(that.toString()));
    }

    @Override
    public boolean focus() {

        try {
            if (xNamed != null) {
                return doc.selectionSupplier.select(xNamed);
            }
        } catch (IllegalArgumentException e) {
        }
        return false;
    }
}
