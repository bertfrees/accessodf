package be.docarch.accessodf.ooo.toolpanel;

import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lang.XInitialization;
import com.sun.star.awt.XWindow;
import com.sun.star.ui.XUIElementFactory;
import com.sun.star.ui.XUIElement;
import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.registry.XRegistryKey;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.IllegalArgumentException;

import be.docarch.accessodf.Constants;

public class ToolPanelFactory  {

    public static class _ToolPanelFactory extends WeakBase
                                       implements XUIElementFactory,
                                                  XServiceInfo,
                                                  XInitialization {

        private final XComponentContext m_xContext;
        private static final String _implementationName = _ToolPanelFactory.class.getName();
        private static final String[] _serviceNames = { ToolPanelFactory.class.getName() };

        private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
        private static final String TMP_NAME = Constants.TMP_PREFIX;
        private static final File TMP_DIR = Constants.getTmpDirectory();
        private Handler fh = null;
        private File logFile = null;

        public _ToolPanelFactory(XComponentContext context) {

            m_xContext = context;

            try {

                logFile = File.createTempFile(TMP_NAME, ".log", TMP_DIR);
                fh = new FileHandler(logFile.getAbsolutePath());
                fh.setFormatter(new SimpleFormatter());
                Logger.getLogger("").addHandler(fh);
                Logger.getLogger("").setLevel(Level.FINEST);

            } catch (java.io.IOException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (RuntimeException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }

        // XUIElementFactory
        public XUIElement createUIElement(String i_rResourceURL,
                                          PropertyValue[] i_rArgs)
                                   throws NoSuchElementException,
                                          IllegalArgumentException {

            logger.entering("_ToolPanelFactory", "createUIElement");

            if (!i_rResourceURL.startsWith("private:resource/toolpanel/be.docarch.accessibility.ooo.toolpanel")) {
                throw new NoSuchElementException(i_rResourceURL, this);
            }

            // retrieve the parent window
            XWindow xParentWindow = null;
            for (int i=0; i<i_rArgs.length; i++) {
                if (i_rArgs[i].Name.equals("ParentWindow")) {
                    xParentWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, i_rArgs[i].Value);
                    break;
                }
            }

            if (xParentWindow == null) {                
                throw new IllegalArgumentException("No parent window provided in the creation arguments. Cannot create tool panel.");
            }

            // create the panel
            return new PanelUIElement(m_xContext, xParentWindow, i_rResourceURL);

        }

        // XServiceInfo
        public String getImplementationName() {
            return _implementationName;
        }

        public boolean supportsService(String sService) {

            int len = _serviceNames.length;
            for( int i=0; i < len; i++) {
                if (sService.equals(_serviceNames[i]))
                    return true;
            }
            return false;
        }

        public String[] getSupportedServiceNames() {
            return _serviceNames;
        }

        // XInitialization:
        public void initialize(Object[] object )
                        throws com.sun.star.uno.Exception {

        }
    }

    public static XSingleComponentFactory __getComponentFactory(String sImplementationName) {

        XSingleComponentFactory xFactory = null;
        if (sImplementationName.equals(_ToolPanelFactory._implementationName)) {
            xFactory = Factory.createComponentFactory(_ToolPanelFactory.class, _ToolPanelFactory._serviceNames);
        }
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo(XRegistryKey xRegistryKey) {

        return Factory.writeRegistryServiceInfo(_ToolPanelFactory._implementationName,
                                                _ToolPanelFactory._serviceNames,
                                                xRegistryKey);
    }
}
