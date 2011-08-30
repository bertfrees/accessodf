package be.docarch.accessibility;

import java.util.EventListener;

public interface IssueListener extends EventListener {
    
    public void issueUpdated(IssueEvent event);
    
}
