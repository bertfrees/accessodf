package be.docarch.accessodf;

/**
 *
 * @author Bert Frees
 */
public class CheckerException extends Exception {

    
    private String error = null;

    public CheckerException() {
        super();
        this.error = "accessibility checker exception";
    }

    public CheckerException(String error) {
        super(error);
        this.error = error;
    }

    public String getError() {
        return error;
    }

}
