package be.docarch.accessibility;

/**
 *
 * @author Bert Frees
 */
public abstract class Check {

    public static enum Status { ALERT,
                                ERROR };

    public static enum RepairMode { MANUAL,
                                    AUTO,
                                    SEMI_AUTOMATED};

    public static enum Category { GRAPHICS,
                                  HEADINGS,
                                  LANGUAGE,
                                  LISTS,
                                  TABLES,
                                  TITLES,
                                  FONT,
                                  COLOR,
                                  DAISY,
                                  BRAILLE,
                                  OTHER };

    public abstract String getIdentifier();

    public abstract Status getStatus();
    
    public abstract Category getCategory();
    
    public abstract String getName();

    public abstract String getDescription();

    public abstract String getSuggestion();

    public abstract RepairMode getRepairMode();

    @Override
    public int hashCode() {

        return getIdentifier().hashCode();
   }

    @Override
    public boolean equals(Object obj) {

        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Check that = (Check)obj;
        return (this.getIdentifier().equals(that.getIdentifier()));
    }
}
