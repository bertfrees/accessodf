package be.docarch.accessibility.ooo;

import be.docarch.accessibility.Checker;
import be.docarch.accessibility.Provider;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ServiceLoader;

import com.sun.star.lang.IllegalArgumentException;

/**
 *
 * @author Bert Frees
 */
public class CheckerProvider implements Provider<Checker> {

    private final Map<String,Checker> checkers;

    public CheckerProvider(ClassLoader classLoader,
                           Document document) {

        checkers = new HashMap<String,Checker>();

        for (Checker checker : ServiceLoader.load(Checker.class, classLoader)) {
            checkers.put(checker.getIdentifier(), checker);
        }

        try {
            Checker mainChecker = new MainChecker(document);
            checkers.put(mainChecker.getIdentifier(), mainChecker);
        } catch (IllegalArgumentException e) {
        } catch (com.sun.star.uno.Exception e) {
        }
    }

    public Collection<Checker> list() {
        return checkers.values();
    }

    public Checker get(String identifier) {
        return checkers.get(identifier);
    }
}
