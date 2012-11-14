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
