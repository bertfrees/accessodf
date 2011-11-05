package be.docarch.accessodf;

import java.util.Map;
import java.util.TreeMap;

import java.util.Collection;

public class CheckProvider implements Provider<Check> {

    private Map<String,Check> checks = new TreeMap<String,Check>();

    public CheckProvider(Provider<Checker> checkers) {

        for (Checker checker : checkers.list()) {
            for (Check check : checker.list()) {
                checks.put(check.getIdentifier(), check);
            }
        }
    }

    public Collection<Check> list() {
        return checks.values();
    }

    public Check get(String identifier) {
        return checks.get(identifier);
    }
}
