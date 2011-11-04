package be.docarch.accessodf;

import java.io.File;

/**
 *
 * @author Bert Frees
 */
public interface RemoteRunnableChecker extends RunnableChecker {

    public void setOdtFile(File odtFile);

    /**
     * @return Returns the accessibility report
     */
    public Report getAccessibilityReport();

}
