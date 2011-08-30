package be.docarch.accessibility.ooo;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.container.XNamed;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.table.XCellRange;

import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class Table extends FocusableElement {

    private final XTextTable xTextTable;
    private final XNamed xNamed;

    public Table(String name)
          throws Exception {

        if (name != null) {
            Object o = tables.getByName(name);
            xTextTable = (XTextTable)UnoRuntime.queryInterface(XTextTable.class, o);
            xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class, xTextTable);
            return;
        }
        throw new Exception();
    }

    public Table(XTextTable xTextTable)
          throws Exception {

        if (xTextTable != null) {
            this.xTextTable = xTextTable;
            xNamed = (XNamed)UnoRuntime.queryInterface(XNamed.class, xTextTable);
            return;
        }
        throw new Exception();
    }

    public XTextTable getXTextTable() {
        return xTextTable;
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

        if (this == obj) {return true; }
        if (obj == null) { return false; }
        if (getClass() != obj.getClass()) { return false; }
        final Table that = (Table)obj;
        return (this.toString().equals(that.toString()));
    }

    @Override
    public boolean focus() {

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
