package be.docarch.accessodf;

import java.util.EventObject;

public class IssueEvent extends EventObject {

    public static enum Type { REPAIR, IGNORE, REMOVE }
    
    public final Type type;

    public IssueEvent(Issue issue,
                      Type type) {
        super(issue);
        this.type = type;
    }
}
