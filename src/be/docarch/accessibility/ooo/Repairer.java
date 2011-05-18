package be.docarch.accessibility.ooo;

import be.docarch.accessibility.Check;
import java.util.Collection;

/**
 *
 * @author Bert Frees
 */
public interface Repairer {

    public String getIdentifier();

    public Collection<Check> getChecks();

    public boolean repair(Issue issue);
    
}
