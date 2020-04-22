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

import com.sun.star.frame.XFrame;
import com.sun.star.ui.XUIElement;
import com.sun.star.ui.XToolPanel;
import com.sun.star.ui.UIElementType;

import com.sun.star.lang.DisposedException;

import be.docarch.accessodf.Constants;

public class PanelUIElement implements XUIElement {

    private final String m_sResourceURL;
    private XToolPanel m_xToolPanel;
    private XFrame m_xFrame;

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    public PanelUIElement(String i_rResourceURL,
    		XFrame xFrame, XToolPanel toolPanel) {

        logger.entering("PanelUIElement", "<init>");

        m_xFrame = xFrame;
        m_xToolPanel = toolPanel;
        m_sResourceURL = i_rResourceURL;
    }

//     XUIElement
    public XFrame getFrame() {
    	 return m_xFrame;
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

}


