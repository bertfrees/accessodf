package be.docarch.accessodf.ooo;

import be.docarch.accessodf.Constants;
import be.docarch.accessodf.Checker;
import be.docarch.accessodf.Provider;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;

/**
 *
 * @author Bert Frees
 */
public class CheckerProvider implements Provider<Checker> {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final Map<String,Checker> checkers;

    public CheckerProvider(ClassLoader classLoader,
                           Document document) {

        checkers = new HashMap<String,Checker>();

        Checker checker;
        Iterator<Checker> i = ServiceRegistry.lookupProviders(Checker.class, classLoader);
        while (i.hasNext()) {
            checker = i.next();
            checkers.put(checker.getIdentifier(), checker);
            logger.info(checker.getIdentifier() + " loaded");
        }

        try {
            Checker mainChecker = new MainChecker(document);
            checkers.put(mainChecker.getIdentifier(), mainChecker);
            logger.info(mainChecker.getIdentifier() + " loaded");
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public Collection<Checker> list() {
        return checkers.values();
    }

    public Checker get(String identifier) {
        return checkers.get(identifier);
    }
}
