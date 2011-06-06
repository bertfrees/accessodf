package be.docarch.accessibility.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.container.XEnumeration;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.Statement;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.table.XCellRange;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class Table extends FocusableElement {

    private boolean exists = false;
    private String name = "";

    private XTextTable xTextTable = null;

    public Table(XResource testsubject)
          throws RepositoryException,
                 NoSuchElementException,
                 IllegalArgumentException,
                 WrappedTargetException {

        logger.entering("Table", "<init>");

        XEnumeration names = xRepository.getStatements(testsubject, URIs.CHECKER_NAME, null);
        if (names.hasMoreElements()) {
            name = ((Statement)names.nextElement()).Object.getStringValue();
            try {
                Object o = tables.getByName(name);
                xTextTable = (XTextTable)UnoRuntime.queryInterface(XTextTable.class, o);
                exists = true;
            } catch (NoSuchElementException e) {
            }
        }

        logger.exiting("Table", "<init>");
    }

    public boolean exists() {
        return exists;
    }

    public XTextTable getComponent() throws Exception {

        if (exists()) {
            return xTextTable;
        } else {
            throw new Exception("Table does not exist");
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
        final Table that = (Table)obj;
        return (!(this.exists()^that.exists()) &&
                  this.name.equals(that.name));
    }

    @Override
    public boolean focus() {

        if (!exists()) { return false; }

        try {

            if (xTextTable != null) {
                String[] cellNames = xTextTable.getCellNames();
                viewCursor.gotoRange((XTextRange)UnoRuntime.queryInterface(
                                      XTextRange.class, xTextTable.getCellByName(cellNames[cellNames.length - 1])), false);
                if (cellNames.length > 1) {
                    XCellRange cellRange = (XCellRange)UnoRuntime.queryInterface(XCellRange.class, xTextTable);
                    XTextTableCursor cursor = xTextTable.createCursorByCellName(cellNames[0]);
                    cursor.gotoCellByName(cellNames[cellNames.length - 1], true);
                    selectionSupplier.select(cellRange.getCellRangeByName(cursor.getRangeName()));
                }
                return true;
            }

        } catch (IllegalArgumentException e) {
        }

        return false;
    }
}
