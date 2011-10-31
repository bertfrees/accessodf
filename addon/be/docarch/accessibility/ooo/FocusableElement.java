package be.docarch.accessibility.ooo;

import java.util.logging.Logger;

import be.docarch.accessibility.Constants;
import be.docarch.accessibility.Element;

/**
 *
 * @author Bert Frees
 */
public abstract class FocusableElement extends Element {

    protected static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    public abstract boolean focus();

}
