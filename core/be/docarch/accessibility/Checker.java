package be.docarch.accessibility;

import java.util.Collection;

/**
 *
 * @author Bert Frees
 */
public interface Checker {

    /*
     * @return  Returns a valid URI reference:
     *          http://www.w3.org/TR/2003/WD-rdf-concepts-20030123/#dfn-URI-reference
     */
    public String getIdentifier();

    public Collection<Check> getChecks();

}
