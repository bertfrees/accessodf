package be.docarch.accessibility;

import java.io.File;

/**
 *
 * @author Bert Frees
 */
public interface RemoteRunnableChecker extends RunnableChecker {

    public abstract void setOdtFile(File odtFile);

    /**
     * @return Returns the accessibility report
     */
    public abstract Report getAccessibilityReport();

}
