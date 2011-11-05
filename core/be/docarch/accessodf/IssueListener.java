package be.docarch.accessodf;

import java.util.EventListener;

public interface IssueListener extends EventListener {
    
    public void issueUpdated(IssueEvent event);
    
}
