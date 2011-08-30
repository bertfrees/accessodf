package be.docarch.accessibility.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.container.XNamed;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class DrawObject extends FocusableElement {

    private XNamed xNamed = null;

    public DrawObject(String name)
               throws Exception {

        if (name != null) {
            Object o = null;
            try {
                o = embeddedObjects.getByName(name);
            } catch (NoSuchElementException e) {
                try {
                    o = graphicObjects.getByName(name);
                } catch (NoSuchElementException ee) {
                }
            }
            if (o != null) {
                xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class, o);
                return;
            }
        }
        throw new Exception();
    }

    public DrawObject(XNamed xNamed)
               throws Exception {

        if (xNamed != null) {
            this.xNamed = xNamed;
            return;
        }
        throw new Exception();
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
                return selectionSupplier.select(xNamed);
            }
        } catch (IllegalArgumentException e) {
        }
        return false;
    }
}
