package be.docarch.accessibility;

import java.util.Collection;
//import java.util.Date;

/**
 *
 * @author Bert Frees
 */
public interface Checker {

    public String getIdentifier();

    public Collection<Check> getChecks();

    /**
     * @return Returns the accessibility report or <code>null</code> if not applicable
     */
    public Report getAccessibilityReport();

    /**
     * @return Returns <code>true</code> if the check was successful
     */
    public boolean check();

    //public Date getLastChecked();

}
