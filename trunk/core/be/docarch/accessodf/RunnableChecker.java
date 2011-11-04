package be.docarch.accessodf;

/**
 *
 * @author Bert Frees
 */
public interface RunnableChecker extends Checker {

    /**
     * @return <code>true</code> if the check was successful
     */
    public boolean run();

}
