package be.docarch.accessibility;

import java.util.Collection;

/**
 *
 * @author Bert Frees
 */
public interface Checker {

    public String getIdentifier();

    public Collection<Check> getChecks();

}
