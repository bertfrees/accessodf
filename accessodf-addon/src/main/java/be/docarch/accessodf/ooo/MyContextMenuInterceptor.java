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

package be.docarch.accessodf.ooo;

import com.sun.star.ui.XContextMenuInterceptor;
import com.sun.star.ui.ContextMenuInterceptorAction;
import com.sun.star.ui.ActionTriggerSeparatorType;
import com.sun.star.ui.ContextMenuExecuteEvent;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.beans.XPropertySet;
import com.sun.star.container.XIndexContainer;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.view.XSelectionSupplier;


public class MyContextMenuInterceptor implements XContextMenuInterceptor {

    private Document document = null;

    public MyContextMenuInterceptor(Document document) {
        this.document = document;
    }

    public ContextMenuInterceptorAction notifyContextMenuExecute(ContextMenuExecuteEvent aEvent)
                                                          throws RuntimeException {

        try {

            XIndexContainer xContextMenu = aEvent.ActionTriggerContainer;
            XSelectionSupplier selection = aEvent.Selection;
            XPropertySet xMenuEntry = null;

            XMultiServiceFactory xMenuElementFactory = (XMultiServiceFactory)UnoRuntime.queryInterface(
                                                        XMultiServiceFactory.class, xContextMenu);
            if (xMenuElementFactory != null) {

              XPropertySet xRootMenuEntry = (XPropertySet)UnoRuntime.queryInterface(
                                             XPropertySet.class, xMenuElementFactory.createInstance(
                                             "com.sun.star.ui.ActionTrigger"));
              XPropertySet xSeparator = (XPropertySet)UnoRuntime.queryInterface(
                                         XPropertySet.class, xMenuElementFactory.createInstance(
                                         "com.sun.star.ui.ActionTriggerSeparator") );
              XIndexContainer xSubMenuContainer = (XIndexContainer)UnoRuntime.queryInterface(
                                                   XIndexContainer.class, xMenuElementFactory.createInstance(
                                                   "com.sun.star.ui.ActionTriggerContainer"));

              xSeparator.setPropertyValue("SeparatorType", new Short(ActionTriggerSeparatorType.LINE));

              xRootMenuEntry.setPropertyValue("Text", new String("Accessiblity"));
              xRootMenuEntry.setPropertyValue("CommandURL", new String(""));
              xRootMenuEntry.setPropertyValue("SubContainer", xSubMenuContainer );

              xMenuEntry = (XPropertySet)UnoRuntime.queryInterface(
                            XPropertySet.class, xMenuElementFactory.createInstance(
                            "com.sun.star.ui.ActionTrigger"));

              xMenuEntry.setPropertyValue("Text", new String("Braille Settings"));
              xMenuEntry.setPropertyValue("CommandURL", new String("be.docarch.odt2braille.addon.odt2brailleaddon:SettingsCommand"));

              xSubMenuContainer.insertByIndex(0, xMenuEntry);

              xMenuEntry = (XPropertySet)UnoRuntime.queryInterface(
                            XPropertySet.class, xMenuElementFactory.createInstance(
                            "com.sun.star.ui.ActionTrigger"));

              xMenuEntry.setPropertyValue("Text", new String("Task Pane"));
              xMenuEntry.setPropertyValue("CommandURL", new String(".uno:TaskPane"));

              xSubMenuContainer.insertByIndex(1, xMenuEntry);

              xContextMenu.insertByIndex (0, xSeparator );
              xContextMenu.insertByIndex (0, xRootMenuEntry );

              return ContextMenuInterceptorAction.EXECUTE_MODIFIED ;
            }

        } catch ( com.sun.star.beans.UnknownPropertyException ex ) {
          // do something useful
          // we used a unknown property
        } catch ( com.sun.star.lang.IndexOutOfBoundsException ex ) {
          // do something useful
          // we used an invalid index for accessing a container
        } catch ( com.sun.star.uno.Exception ex ) {
          // something strange has happend!
        } catch ( java.lang.Throwable ex ) {
          // catch java exceptions - do something useful
        }

        return ContextMenuInterceptorAction.IGNORED;
    }
}