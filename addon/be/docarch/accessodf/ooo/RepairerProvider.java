package be.docarch.accessodf.ooo;

import be.docarch.accessodf.Provider;
import be.docarch.accessodf.Repairer;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.spi.ServiceRegistry;

/**
 *
 * @author Bert Frees
 */
public class RepairerProvider implements Provider<Repairer> {

    private final Map<String,Repairer> repairers;

    public RepairerProvider(ClassLoader classLoader,
                            Document document) {

        repairers = new HashMap<String,Repairer>();

        Repairer repairer;
        Iterator<Repairer> i = ServiceRegistry.lookupProviders(Repairer.class, classLoader);
        while (i.hasNext()) {
            repairer = i.next();
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
