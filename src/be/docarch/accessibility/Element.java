package be.docarch.accessibility;

/**
 *
 * @author Bert Frees
 */
public abstract class Element {

    public abstract boolean exists();

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract String toString();
}
