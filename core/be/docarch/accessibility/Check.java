package be.docarch.accessibility;

import java.util.Locale;

/**
 *
 * @author Bert Frees
 */
public abstract class Check {

    public static enum Status { ALERT,
                                ERROR };

    public abstract String getIdentifier();

    public abstract Status getStatus();
    
    public abstract String getName(Locale locale);

    public abstract String getDescription(Locale locale);

    public abstract String getSuggestion(Locale locale);

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
   }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof Check)) { return false; }
        final Check that = (Check)obj;
        return (this.getIdentifier().equals(that.getIdentifier()));
    }
}
