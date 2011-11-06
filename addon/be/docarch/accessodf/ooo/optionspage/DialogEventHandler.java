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

package be.docarch.accessodf.ooo.optionspage;

import com.sun.star.awt.ItemEvent;
import com.sun.star.lang.EventObject;

import com.sun.star.lib.uno.helper.Factory;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.lang.XServiceInfo;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.Exception;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.awt.XContainerWindowEventHandler;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XItemListener;
import com.sun.star.beans.XPropertySet;

import be.docarch.accessodf.ooo.Settings;


/**
 * A handler which supports an options page which with different controls.
 * Can be easily adapted to support multiple options pages.
 * @author OpenOffice.org
 */
public class DialogEventHandler
{
  public static class _DialogEventHandler extends WeakBase
    implements XServiceInfo, XContainerWindowEventHandler,XItemListener
  {
    /**
     * Names of supported options pages. The name derives from the
     * actual file names of the .xdl files not a XML attribute.
     */
    public static String[] SupportedWindowNames = {"OptionsPage"};

    /**
     * Names of the controls which are supported by this handler. All these
     * controls must have a "Text" property.
     */
    public static String[] ControlNames = {"CheckBox1", "CheckBox2"};

    static private final String __serviceName = "be.docarch.accessodf.ooo.optionspage.DialogEventHandler";

    private XComponentContext context;
    private Settings settings;

    private XCheckBox brailleCheckBox = null;
    private XPropertySet brailleCheckBoxProperties = null;
    private XCheckBox daisyCheckBox = null;
    private XPropertySet daisyCheckBoxProperties = null;

    public _DialogEventHandler(XComponentContext xCompContext)
    {
      this.context = xCompContext;
    }

    /**
     * This method returns an array of all supported service names.
     * @return Array of supported service names.
     */
    public String[] getSupportedServiceNames()
    {
      return getServiceNames();
    }

    /**
     * This method is a simple helper function to used in the
     * static component initialisation functions as well as in
     * getSupportedServiceNames.
     */
    public static String[] getServiceNames()
    {
      String[] sSupportedServiceNames = { __serviceName };
      return sSupportedServiceNames;
    }

    /** This method returns true, if the given service will be
      * supported by the component.
      * @param sServiceName Service name.
      * @return True, if the given service name will be supported.
      */
    public boolean supportsService( String sServiceName )
    {
      return sServiceName.equals( __serviceName );
    }

    /**
     * Return the class name of the component.
     * @return Class name of the component.
     */
    public String getImplementationName()
    {
      return _DialogEventHandler.class.getName();
    }

    /**
     * Is called by the OOo event system.
     * @param aWindow
     * @param aEventObject
     * @param sMethod
     * @return
     * @throws com.sun.star.lang.WrappedTargetException
     */
    public boolean callHandlerMethod(com.sun.star.awt.XWindow aWindow, Object aEventObject, String sMethod)
      throws WrappedTargetException
    {
      if (sMethod.equals("external_event") )
      {
        try
        {
          return handleExternalEvent(aWindow, aEventObject);
        }
        catch (com.sun.star.uno.RuntimeException re)
        {
          throw re;
        }
        catch (com.sun.star.uno.Exception e)
        {
          e.printStackTrace();
          throw new WrappedTargetException(sMethod, this, e);
        }
      }
      return false;
    }

    /**
     * @return A String array containing the method names supported by this handler.
     */
    public String[] getSupportedMethodNames()
    {
      return new String[] {"external_event"};
    }

    private boolean handleExternalEvent(com.sun.star.awt.XWindow aWindow,
                                        Object aEventObject)
      throws com.sun.star.uno.Exception
    {
      try
      {
        String sMethod = AnyConverter.toString(aEventObject);
        if (sMethod.equals("ok")) {
            saveData(aWindow);
        } else if (sMethod.equals("back")) {
            loadData(aWindow);
        } else if (sMethod.equals("initialize")) {
            init(aWindow);
            loadData(aWindow);
        }
      }
      catch (com.sun.star.lang.IllegalArgumentException ex)
      {
        ex.printStackTrace();
          throw new com.sun.star.lang.IllegalArgumentException(
            "Method external_event requires a string in the event object argument.",
            this, (short) -1);
      }
      return true;
    }

    private void init(com.sun.star.awt.XWindow aWindow)
               throws com.sun.star.uno.Exception {
    
        String sWindowName = getWindowName(aWindow);
        if (sWindowName == null) {
            throw new com.sun.star.lang.IllegalArgumentException(
                "This window is not supported by this handler", this, (short) -1);
        }
        if (sWindowName.equals("OptionsPage")) {
            XControlContainer xContainer = (XControlContainer) UnoRuntime.queryInterface(
                                            XControlContainer.class, aWindow);
            if (xContainer == null) {
                throw new com.sun.star.uno.Exception(
                    "Could not get XControlContainer from window.", this);
            }

            this.settings = new Settings(context);
            daisyCheckBox = (XCheckBox)UnoRuntime.queryInterface(XCheckBox.class, xContainer.getControl("CheckBox1"));
            brailleCheckBox = (XCheckBox)UnoRuntime.queryInterface(XCheckBox.class, xContainer.getControl("CheckBox2"));
            daisyCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xContainer.getControl("CheckBox1").getModel());
            brailleCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xContainer.getControl("CheckBox2").getModel());
        }
    }

    public void itemStateChanged(ItemEvent event) {

    }

    public void disposing(EventObject arg0) {
        
    }


    /**
     * Saves data from the dialog into the configuration.
     * @param aWindow
     * @throws com.sun.star.lang.IllegalArgumentException
     * @throws com.sun.star.uno.Exception
     */
    private void saveData(com.sun.star.awt.XWindow aWindow)
                   throws com.sun.star.lang.IllegalArgumentException,
                          com.sun.star.uno.Exception {

        String sWindowName = getWindowName(aWindow);        
        if (sWindowName == null) {
            throw new com.sun.star.lang.IllegalArgumentException(
                "This window is not supported by this handler", this, (short) -1);
        }
        if (sWindowName.equals("OptionsPage")) {

            settings.daisyChecks(daisyCheckBox.getState()==(short)1);
            settings.brailleChecks(brailleCheckBox.getState()==(short)1);
            settings.saveData();
        }
    }

    /**
     * Loads data from the configuration into the dialog.
     * @param aWindow
     * @throws com.sun.star.uno.Exception
     */
    private void loadData(com.sun.star.awt.XWindow aWindow)
                   throws com.sun.star.uno.Exception {

        String sWindowName = getWindowName(aWindow);
        if (sWindowName == null) {
            throw new com.sun.star.lang.IllegalArgumentException(
                "This window is not supported by this handler", this, (short) -1);
        }
        if (sWindowName.equals("OptionsPage")) {

            settings.loadData();
            daisyCheckBoxProperties.setPropertyValue("Enabled", settings.daisyChecksAvailable());
            brailleCheckBoxProperties.setPropertyValue("Enabled", settings.brailleChecksAvailable());
            daisyCheckBox.setState((short)(settings.daisyChecks()?1:0));
            brailleCheckBox.setState((short)(settings.brailleChecks()?1:0));
        }
    }

    // Checks if the name property of the window is one of the supported names and returns
    // always a valid string or null
    private String getWindowName(com.sun.star.awt.XWindow aWindow)
      throws com.sun.star.uno.Exception
    {
      if (aWindow == null)
        new com.sun.star.lang.IllegalArgumentException(
          "Method external_event requires that a window is passed as argument",
          this, (short) -1);

      XControl xControlDlg = (XControl) UnoRuntime.queryInterface(
        XControl.class, aWindow);

      if (xControlDlg == null)
        throw new com.sun.star.uno.Exception(
          "Cannot obtain XControl from XWindow in method external_event.");

      XControlModel xModelDlg = xControlDlg.getModel();

      if (xModelDlg == null)
        throw new com.sun.star.uno.Exception(
          "Cannot obtain XControlModel from XWindow in method external_event.", this);
      
      XPropertySet xPropDlg = (XPropertySet) UnoRuntime.queryInterface(
        XPropertySet.class, xModelDlg);
      if (xPropDlg == null)
        throw new com.sun.star.uno.Exception(
          "Cannot obtain XPropertySet from window in method external_event.", this);

      Object aWindowName = xPropDlg.getPropertyValue("Name");

      String sName = null;
      try
      {
        sName = AnyConverter.toString(aWindowName);
      }
      catch (com.sun.star.lang.IllegalArgumentException ex)
      {
        ex.printStackTrace();
        throw new com.sun.star.uno.Exception(
          "Name - property of window is not a string.", this);
      }

      for (int i = 0; i < SupportedWindowNames.length; i++)
      {
        if (SupportedWindowNames[i].equals(sName))
        {
          return sName;
        }
      }
      return null;
    }
  }

  /**
   * Gives a factory for creating the service.
   * This method is called by the CentralRegistrationClass.
   * @return returns a <code>XSingleComponentFactory</code> for creating
   * the component
   * @param sImplName the name of the implementation for which a
   * service is desired
   * @see com.sun.star.comp.loader.JavaLoader
   */
  public static XSingleComponentFactory __getComponentFactory(String sImplName)
  {
    XSingleComponentFactory xFactory = null;

    if ( sImplName.equals( _DialogEventHandler.class.getName() ) )
    xFactory = Factory.createComponentFactory(_DialogEventHandler.class,
    _DialogEventHandler.getServiceNames());

    return xFactory;
  }

  /**
   * Writes the service information into the given registry key.
   * This method is called by the CentralRegistrationClass.
   * @return returns true if the operation succeeded
   * @param regKey the registryKey
   * @see com.sun.star.comp.loader.JavaLoader
   */
  public static boolean __writeRegistryServiceInfo(XRegistryKey regKey)
  {
    return Factory.writeRegistryServiceInfo(_DialogEventHandler.class.getName(),
      _DialogEventHandler.getServiceNames(),
      regKey);
  }

  /**
   * This method is a member of the interface for initializing an object
   * directly after its creation.
   * @param object This array of arbitrary objects will be passed to the
   * component after its creation.
   * @throws Exception Every exception will not be handled, but will be
   * passed to the caller.
   */
  public void initialize( Object[] object )
    throws com.sun.star.uno.Exception
  {}
}