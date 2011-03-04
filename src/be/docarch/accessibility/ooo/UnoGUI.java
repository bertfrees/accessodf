package be.docarch.accessibility.ooo;

import java.io.File;
import java.util.Locale;
import java.util.Collection;
import java.util.logging.Logger;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

import com.sun.star.beans.PropertyValue;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XDesktop;
import com.sun.star.frame.XController;
import com.sun.star.frame.XStorable;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.XWindow;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XComponent;
import com.sun.star.ui.XContextMenuInterception;
import com.sun.star.ui.XContextMenuInterceptor;

import java.io.IOException;
import java.text.ParseException;
import javax.xml.transform.TransformerException;

import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;


/**
 *
 * @author Bert Frees
 */
public class UnoGUI {

    private static final Logger logger = Logger.getLogger("be.docarch.accessibility");

    private XComponentContext xContext = null;
    private XFrame xFrame = null;
    private XMultiComponentFactory xMCF = null;
    private XModel xDoc = null;
    private XWindow parentWindow = null;
    private XWindowPeer parentWindowPeer = null;

    private Handler fh = null;
    private File logFile = null;

    private Locale odtLocale = null;
    private Locale oooLocale = null;


    public UnoGUI(XComponentContext m_xContext,
                  XFrame m_xFrame) {

        logger.entering("UnoGUI", "<init>");

        try {

            xContext = m_xContext;
            xFrame = m_xFrame;
            xMCF = (XMultiComponentFactory) UnoRuntime.queryInterface(
	            XMultiComponentFactory.class, m_xContext.getServiceManager());
            xDoc = (XModel) UnoRuntime.queryInterface(XModel.class, m_xFrame.getController().getModel());
            parentWindow = xDoc.getCurrentController().getFrame().getContainerWindow();
            parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface(XWindowPeer.class, parentWindow);

            // L10N
            Locale.setDefault(Locale.ENGLISH);
            try {
                oooLocale = new Locale(UnoUtils.getUILocale(m_xContext));
            } catch (com.sun.star.uno.Exception ex) {
                logger.log(Level.SEVERE, null, ex);
                oooLocale = Locale.getDefault();
            }

            logger.exiting("UnoGUI", "<init>");

        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
        }
    }

    public void showAccessibilityDialog() {

        logger.entering("UnoGUI", "showAccessibilityView");

        try {

            // Load file to new, read-only document
//            Document document = new Document(odtUnoUrl, xContext);

            // Current document
            Document document = new Document(xContext);

            // Internal checker
            InternalChecker mainChecker = new InternalChecker(document);

            // Create dialog
            AccessibilityDialog dialog = new AccessibilityDialog(document, mainChecker, null);

            // Insert comments
            // dialog.showComments();

            // Execute dialog
            dialog.executeDialog();

            // Create context menu interceptor
//            if (xFrame != null) {
//                com.sun.star.frame.XController xController = xFrame.getController();
//                if (xController != null) {
//                     XContextMenuInterception xContextMenuInterception = (XContextMenuInterception)UnoRuntime.queryInterface(
//                                                                          XContextMenuInterception.class, xController);
//                    if (xContextMenuInterception != null) {
//                        XContextMenuInterceptor xContextMenuInterceptor = new MyContextMenuInterceptor(document);
//                        xContextMenuInterception.registerContextMenuInterceptor(xContextMenuInterceptor);
//                    }
//                }
//            }

//            XDispatchHelper dispatcher;
//            XDispatchProvider dispatchProvider;
//            PropertyValue[] dispatchProperties;
//
//            dispatcher = (XDispatchHelper)UnoRuntime.queryInterface(
//                         XDispatchHelper.class, document.xMCF.createInstanceWithContext(
//                             "com.sun.star.frame.DispatchHelper", document.xContext));
//            dispatchProvider = (XDispatchProvider)UnoRuntime.queryInterface(
//                                XDispatchProvider.class, document.xModel.getCurrentController().getFrame());

        /*
        } catch (IOException ex) {
            handleUnexpectedException(ex);
        } catch(TransformerException ex) {
            handleUnexpectedException(ex);
        */
        } catch (com.sun.star.io.IOException ex) {
            handleUnexpectedException(ex);
        } catch (IllegalArgumentException ex) {
            handleUnexpectedException(ex);
        } catch (WrappedTargetException ex) {
            handleUnexpectedException(ex);
        } catch (NoSuchElementException ex) {
            handleUnexpectedException(ex);
        } catch (ElementExistException ex) {
            handleUnexpectedException(ex);
        } catch (RepositoryException ex) {
            handleUnexpectedException(ex);
        } catch (java.text.ParseException ex) {
            handleUnexpectedException(ex);
        } catch (com.sun.star.uno.Exception ex) {
            handleUnexpectedException(ex);
        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
        }

        logger.exiting("UnoGUI", "showAccessibilityView");

    }

    /**
     * Handling of an unexpected exception.
     * A message box is shown with a reference to the log file.
     *
     * @param   ex     The exception
     */
    private void handleUnexpectedException (Exception ex) {

        logger.log(Level.SEVERE, null, ex);
        UnoAwtUtils.showErrorMessageBox(parentWindowPeer, "Unexpected exception","Unexpected exception");

    }

    /**
     * Flush and close the logfile handler.
     */
    public void clean () {
        if (fh != null) {
            fh.flush();
            fh.close();
        }
    }
}
