package be.docarch.accessibility;

import java.util.Date;

/**
 *
 * @author Bert Frees
 */
public abstract class Issue {

    public abstract Element getElement();

    public abstract boolean ignored();

    public abstract boolean repaired();

    public abstract Date getCheckDate();

    public abstract Check getCheck();

    public abstract Checker getChecker();

    public abstract void ignored(boolean ignored);

    public abstract void repaired(boolean repaired);

    public abstract void remove();

    public String getName() {

        Element element = getElement();
        if (element == null) {
            return "";
        } else {
            return element.toString();
        }
    }

    @Override
    public int hashCode() {

        final int PRIME = 31;
        int hash = 1;
        hash = hash * PRIME + getCheck().hashCode();
        Element element = getElement();
        if (element != null) {
            hash = hash * PRIME + element.hashCode();
        }
        return hash;
   }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof Issue))
            return false;
        final Issue that = (Issue)obj;
        if (!this.getCheck().equals(that.getCheck())) {
            return false;
        }
        Element element = getElement();
        if (element == null) {
            return (that.getElement() == null);
        } else {
            return element.equals(that.getElement());
        }
    }
}
