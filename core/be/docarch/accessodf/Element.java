package be.docarch.accessodf;

/**
 *
 * @author Bert Frees
 */
public abstract class Element {

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
