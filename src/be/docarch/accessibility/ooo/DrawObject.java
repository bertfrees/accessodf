package be.docarch.accessibility.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.container.XNamed;
import com.sun.star.container.XEnumeration;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.Statement;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class DrawObject extends Element {

    private boolean exists = false;
    private String name = "";

    private XNamed component = null;

    public DrawObject(XResource testsubject)
               throws RepositoryException,
                      NoSuchElementException,
                      IllegalArgumentException,
                      WrappedTargetException {

        logger.entering("DrawObject", "<init>");

        XEnumeration names = xRepository.getStatements(testsubject, URIs.CHECKER_NAME, null);
        if (names.hasMoreElements()) {
            name = ((Statement)names.nextElement()).Object.getStringValue();
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
                component = (XNamed)UnoRuntime.queryInterface(XNamed.class, o);
                exists = true;
            }
        }

        logger.exiting("DrawObject", "<init>");
    }

    public boolean exists() {
        return exists;
    }

    public XNamed getComponent() throws Exception {

        if (exists()) {
            return component;
        } else {
            throw new Exception("Object does not exist");
        }
    }

    public String toString() {
        return name;
    }

    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = hash * PRIME + name.hashCode();
        return hash;
    }

    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final DrawObject that = (DrawObject)obj;
        return (!(this.exists()^that.exists()) &&
                  this.name.equals(that.name));
    }
}
