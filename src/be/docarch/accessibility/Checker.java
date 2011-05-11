package be.docarch.accessibility;

import java.io.File;
import java.util.Collection;
import java.util.Date;


/**
 *
 * @author Bert Frees
 */
public interface Checker {

    public String getIdentifier();

    public Collection<Check> getChecks();

    public Check getCheck(String identifier);

    public File getAccessibilityReport();

    public void check();

    public Date getLastChecked();
    
}
