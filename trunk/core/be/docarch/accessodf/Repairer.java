package be.docarch.accessodf;

/**
 *
 * @author Bert Frees
 */
public interface Repairer {

    public static enum RepairMode { AUTO,
                                    SEMI_AUTOMATED};

    public String getIdentifier();

    public boolean supports(Issue issue);

    public abstract RepairMode getRepairMode(Issue issue);

    public boolean repair(Issue issue);
    
}
