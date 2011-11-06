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

package be.docarch.accessodf;

import java.util.Date;
import java.util.Collection;
import java.util.ArrayList;

/**
 *
 * @author Bert Frees
 */
public class Issue {

    private Collection<IssueListener> listeners;

    private final Element element;
    private final Check check;
    private final Checker checker;
    private final Date checkDate;
    private final int count;

    private boolean ignored = false;
    private boolean repaired = false;

    public Issue(Element element, Check check, Checker checker) {
        this(element, check, checker, 1);
    }

    public Issue(Element element, Check check, Checker checker, int count) {
        this(element, check, checker, new Date(), count);
    }

    public Issue(Element element, Check check, Checker checker, Date checkDate) {
        this(element, check, checker, checkDate, 1);
    }

    public Issue(Element element,
                 Check check,
                 Checker checker,
                 Date checkDate,
                 int count) {

        this.element = element;
        this.check = check;
        this.checker = checker;
        this.checkDate = checkDate;
        this.count = count;
    }

    public Element getElement() {
        return element;
    }

    public boolean ignored() {
        return ignored;
    }

    public boolean repaired() {
        return repaired;
    }

    public Date getCheckDate() {
        return checkDate;
    }

    public Check getCheck() {
        return check;
    }

    public Checker getChecker() {
        return checker;
    }

    public int getCount() {
        return count;
    }

    public void ignored(boolean ignored) {
        this.ignored = ignored;
        fireEvent(IssueEvent.Type.IGNORE);
    }

    public void repaired(boolean repaired) {
        this.repaired = repaired;
        fireEvent(IssueEvent.Type.REPAIR);
    }

    public void remove() {
        fireEvent(IssueEvent.Type.REMOVE);
    }

    public String getName() {

        if (element == null) {
            return "";
        } else {
            return element.toString();
        }
    }

    private void fireEvent(IssueEvent.Type type) {
        if (listeners == null) { return; }
        IssueEvent event = new IssueEvent(this, type);
        for (IssueListener listener : listeners) {
            listener.issueUpdated(event);
        }
    }

    public void addListener(IssueListener listener) {
        if (listeners == null) { listeners = new ArrayList<IssueListener>(); }
        listeners.add(listener);
    }

    @Override
    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = hash * PRIME + getCheck().hashCode();
        if (element != null) {
            hash = hash * PRIME + element.hashCode();
        }
        return hash;
   }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof Issue)){ return false; }
        final Issue that = (Issue)obj;
        if (!this.getCheck().equals(that.getCheck())) {
            return false;
        }
        if (element == null) {
            return (that.getElement() == null);
        } else {
            return element.equals(that.getElement());
        }
    }
}
