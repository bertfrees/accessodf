package be.docarch.accessibility;

/**
 *
 * @author Bert Frees
 */
public interface Repairer {

    public static enum RepairMode { AUTO,
                                    SEMI_AUTOMATED};

    public String getIdentifier();

    public boolean supports(Check check);

    public abstract RepairMode getRepairMode(Check check);

    public boolean repair(Issue issue);
    
}
