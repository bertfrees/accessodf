package be.docarch.accessodf.ooo;

import java.util.logging.Logger;

import be.docarch.accessodf.Constants;
import be.docarch.accessodf.Element;

/**
 *
 * @author Bert Frees
 */
public abstract class FocusableElement extends Element {

    protected static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    public abstract boolean focus();

}
