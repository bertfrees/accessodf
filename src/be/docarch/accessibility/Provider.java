package be.docarch.accessibility;

import java.util.Collection;

/**
 *
 * @author Bert Frees
 */
public interface Provider<T> {

    public Collection<T> list();

    public T get(String identifier);

}
