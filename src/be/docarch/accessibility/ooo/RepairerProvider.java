package be.docarch.accessibility.ooo;

import be.docarch.accessibility.Provider;
import be.docarch.accessibility.Repairer;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.ServiceLoader;

/**
 *
 * @author Bert Frees
 */
public class RepairerProvider implements Provider<Repairer> {

    private final Map<String,Repairer> repairers;

    public RepairerProvider(ClassLoader classLoader,
                            Document document) {

        repairers = new HashMap<String,Repairer>();

        for (Repairer repairer : ServiceLoader.load(Repairer.class, classLoader)) {
            repairers.put(repairer.getIdentifier(), repairer);
        }

        try {
            Repairer mainRepairer = new MainRepairer(document);
            repairers.put(mainRepairer.getIdentifier(), mainRepairer);
        } catch (com.sun.star.uno.Exception e) {
        }

    }

    public Collection<Repairer> list() {
        return repairers.values();
    }

    public Repairer get(String identifier) {
        return repairers.get(identifier);
    }
}
