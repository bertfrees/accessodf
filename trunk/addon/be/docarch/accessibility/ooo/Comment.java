package be.docarch.accessodf.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.beans.XPropertySet;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextCursor;

import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;


/**
 *
 * @author Bert Frees
 */
public class Comment {

    XTextContent annotation = null;
    XPropertySet annotationProps = null;

    public Comment(XMultiServiceFactory xMSF)
            throws UnknownPropertyException,
                   WrappedTargetException,
                   IllegalArgumentException,
                   PropertyVetoException,
                   com.sun.star.uno.Exception {

        annotation = (XTextContent)UnoRuntime.queryInterface(
                      XTextContent.class, xMSF.createInstance("com.sun.star.text.textfield.Annotation"));
        annotationProps = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, annotation);
        setAuthor("Accessibility checker");

    }

    public void setAuthor(String author)
                   throws UnknownPropertyException,
                          PropertyVetoException,
                          WrappedTargetException,
                          IllegalArgumentException {

        annotationProps.setPropertyValue("Author", author);
    }

    public void setContent(String content)
                    throws UnknownPropertyException,
                           PropertyVetoException,
                           WrappedTargetException,
                           IllegalArgumentException {

        annotationProps.setPropertyValue("Content", content);
    }

    public void insertCommentAtCursor(XTextCursor cursor)
                               throws IllegalArgumentException{

        cursor.getText().insertTextContent(cursor.getStart(), annotation, true);
    }
}
