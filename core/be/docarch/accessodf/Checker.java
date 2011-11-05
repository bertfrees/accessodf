package be.docarch.accessodf;

/**
 *
 * @author Bert Frees
 */
public interface Checker extends Provider<Check> {

    /*
     * @return  Returns a valid URI reference:
     *          http://www.w3.org/TR/2003/WD-rdf-concepts-20030123/#dfn-URI-reference
     */
    public String getIdentifier();

    @Override
    public String toString();
}
