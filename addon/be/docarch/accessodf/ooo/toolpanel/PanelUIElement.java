/**
 *  AccessODF - Accessibility checker for OpenOffice.org and LibreOffice Writer.
 *
 *  Copyright (c) 2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.accessodf.ooo.toolpanel;

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

import be.docarch.accessodf.Constants;

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


