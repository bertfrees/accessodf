package be.docarch.accessibility.ooo.toolpanel;

import java.util.logging.Logger;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XWindow;
import com.sun.star.frame.XFrame;
import com.sun.star.lang.XComponent;
import com.sun.star.ui.XUIElement;
import com.sun.star.ui.XToolPanel;
import com.sun.star.ui.UIElementType;
import com.sun.star.lib.uno.helper.ComponentBase;

import com.sun.star.lang.DisposedException;

import be.docarch.accessibility.Constants;

public class PanelUIElement extends ComponentBase
                         implements XUIElement {

    private final String m_sResourceURL;
    private XToolPanel m_xToolPanel;

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    public PanelUIElement(XComponentContext context,
                          XWindow i_rParentWindow,
                          String i_rResourceURL) {

        logger.entering("PanelUIElement", "<init>");

        m_sResourceURL = i_rResourceURL;
        m_xToolPanel = new AccessibilityPanel(context, i_rParentWindow);

    }

    // XUIElement
    public XFrame getFrame() {

        // TODO
        return null;
    }

    public String getResourceURL() {

        return m_sResourceURL;
    }

    public short getType() {

        return UIElementType.TOOLPANEL;
    }

    public Object getRealInterface() {

        if (m_xToolPanel == null) {
            throw new DisposedException("", this );
        }
        return m_xToolPanel;
    }

    public void disposing() {

        XComponent xPanelComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, m_xToolPanel);
        xPanelComponent.dispose();
    }
}


