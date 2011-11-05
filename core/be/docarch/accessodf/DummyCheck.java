package be.docarch.accessodf;

import java.util.Locale;

/**
 *
 * @author Bert Frees
 */
public class DummyCheck extends Check {

    @Override
    public String getIdentifier() {
        return "DUMMY_CHECK";
    }

    @Override
    public Status getStatus() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getName(Locale locale) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getSuggestion(Locale locale) {
        throw new UnsupportedOperationException("Not supported.");
    }
}
