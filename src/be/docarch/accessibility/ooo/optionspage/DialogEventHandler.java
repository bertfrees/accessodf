package be.docarch.accessibility.ooo.optionspage;

import com.sun.star.awt.ItemEvent;
import com.sun.star.lang.EventObject;
import java.util.ArrayList;
import java.util.ServiceLoader;
import java.io.File;
import java.net.URLClassLoader;
import java.net.URL;

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
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XItemListener;
import com.sun.star.container.XNameAccess;
import com.sun.star.beans.XPropertySet;
import com.sun.star.util.XChangesBatch;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;

import be.docarch.accessibility.Check;
import be.docarch.accessibility.ExternalChecker;
import be.docarch.accessibility.ooo.InternalChecker;


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
    public static String[] ControlNames = {"Label1", "ListBox1", "CheckBox1", "ListBox2"};

    static private final String __serviceName = "be.docarch.accessibility.ooo.optionspage.DialogEventHandler";

    private XComponentContext context;
    private XNameAccess       accessLeaves;

    private InternalChecker generalChecker = null;
    private ExternalChecker odt2brailleChecker = null;

    private XCheckBox odt2brailleCheckBox = null;
    private XListBox generalListBox = null;
    private XListBox odt2brailleListBox = null;
    private XPropertySet odt2brailleCheckBoxProperties = null;
    private XPropertySet generalListBoxProperties = null;
    private XPropertySet odt2brailleListBoxProperties = null;


    public _DialogEventHandler(XComponentContext xCompContext)
    {
      this.context = xCompContext;
//      this.accessLeaves = ConfigurationAccess.createUpdateAccess(context,
//        "/be.docarch.accessibility.ooo.AccessibilityCheckerAddOn/Leaves");
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

            generalChecker = new InternalChecker();
            odt2brailleChecker = loadOdt2Braille();
            
            odt2brailleCheckBox = (XCheckBox)UnoRuntime.queryInterface(XCheckBox.class, xContainer.getControl("CheckBox1"));
            generalListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, xContainer.getControl("ListBox1"));
            odt2brailleListBox = (XListBox)UnoRuntime.queryInterface(XListBox.class, xContainer.getControl("ListBox2"));

            odt2brailleCheckBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xContainer.getControl("CheckBox1").getModel());
            generalListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xContainer.getControl("ListBox1").getModel());
            odt2brailleListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, xContainer.getControl("ListBox2").getModel());

            short i = 0;
            for (Check check : generalChecker.getChecks()) {
                generalListBox.addItem(check.getName(), i++);
            }
            if (odt2brailleChecker != null) {
                odt2brailleCheckBoxProperties.setPropertyValue("Enabled", true);
                odt2brailleListBoxProperties.setPropertyValue("Enabled", true);
                i = 0;
                for (Check check : odt2brailleChecker.getChecks()) {
                    odt2brailleListBox.addItem(check.getName(), i++);
                }
            } else {
                odt2brailleCheckBoxProperties.setPropertyValue("Enabled", false);
                odt2brailleListBoxProperties.setPropertyValue("Enabled", false);
            }
        }
    }

    private ExternalChecker loadOdt2Braille() {
        
        try {
            
            XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(context);
            String odt2braillePackage = xPkgInfo.getPackageLocation("be.docarch.odt2braille.ooo.odt2brailleaddon");
            if (odt2braillePackage.length() > 0) {
                File dir = new File(odt2braillePackage.substring(6));
                ArrayList<URL> urls = new ArrayList<URL>();
                File[] jars = dir.listFiles(new java.io.FileFilter() {
                    public boolean accept(File file) {
                        return file.getAbsolutePath().endsWith(".jar");
                    }
                });
                for (File jar : jars) {
                    urls.add(new URL("jar:file://" + jar.toURI().toURL().getPath() + "!/"));
                }
                File lib = new File(dir.getAbsolutePath() + File.separator + "lib");
                jars = lib.listFiles(new java.io.FileFilter() {
                    public boolean accept(File file) {
                        return file.getAbsolutePath().endsWith(".jar");
                    }
                });
                for (File jar : jars) {
                    urls.add(new URL("jar:file://" + jar.toURI().toURL().getPath() + "!/"));
                }
                URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), ExternalChecker.class.getClassLoader());                
                for (ExternalChecker checker : ServiceLoader.load(ExternalChecker.class, classLoader)) {
                    return checker;
                }
            }
        } catch (java.net.MalformedURLException ex) {
        } finally {
            return null;
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


            
        }

//      for (int i = 0; i < ControlNames.length; i++)
//      {
//        XControl xControl = xContainer.getControl(ControlNames[i]);
//
//        if (xControl == null)
//          continue;
//
//        XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
//          XPropertySet.class, xControl.getModel());
//
//        if (xProp == null)
//          throw new com.sun.star.uno.Exception(
//            "Could not get XPropertySet from control.", this);
//
//        Object   aObj  = null;
//        Object[] value = new Object[1];
//        String[] keys  = new String[] {ControlNames[i]};
//        try
//        {
//          if(ControlNames[i].startsWith("txt"))
//          {
//            aObj     = xProp.getPropertyValue("Text");
//            value[0] = AnyConverter.toString(aObj);
//          }
//          else if(ControlNames[i].startsWith("lst"))
//          {
//            keys  = new String[]{ControlNames[i] + "Selected", ControlNames[i]};
//            value = new Object[2];
//
//            // Read out indices of selected items
//            aObj     = xProp.getPropertyValue("SelectedItems");
//            value[0] = AnyConverter.toArray(aObj);
//
//            // Read out items (they are read-only though, but perhaps someone wants to change this)
//            aObj     = xProp.getPropertyValue("StringItemList");
//            value[1] = AnyConverter.toArray(aObj);
//          }
//          else if(ControlNames[i].startsWith("chk"))
//          {
//            aObj     = xProp.getPropertyValue("State");
//            value[0] = new Short(AnyConverter.toShort(aObj)).toString();
//          }
//        }
//        catch (com.sun.star.lang.IllegalArgumentException ex)
//        {
//          ex.printStackTrace();
//          throw new com.sun.star.lang.IllegalArgumentException(
//            "Wrong property type.", this, (short) -1);
//        }
//
//        XPropertySet xLeaf = (XPropertySet) UnoRuntime.queryInterface(
//          XPropertySet.class, accessLeaves.getByName(sWindowName));
//        if (xLeaf == null)
//          throw new com.sun.star.uno.Exception("XPropertySet not supported.", this);
//
//        for(int n = 0; n < keys.length; n++)
//          xLeaf.setPropertyValue(keys[n], value[n]);
//      }

//      XChangesBatch xUpdateCommit =
//      (XChangesBatch) UnoRuntime.queryInterface(XChangesBatch.class, accessLeaves);
//      xUpdateCommit.commitChanges();
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





        }

//      for (int i = 0; i < ControlNames.length; i++)
//      {
//        XPropertySet xLeaf = (XPropertySet) UnoRuntime.queryInterface(
//          XPropertySet.class, this.accessLeaves.getByName(sWindowName));
//        if (xLeaf == null)
//          throw new com.sun.star.uno.Exception("XPropertySet not supported.", this);
//
//        Object aValue = xLeaf.getPropertyValue(ControlNames[i]);
//
//        XControl xControl = xContainer.getControl(ControlNames[i]);
//
//        if (xControl == null)
//          continue;
//
//        XPropertySet xProp = (XPropertySet) UnoRuntime.queryInterface(
//          XPropertySet.class, xControl.getModel());
//
//        if (xProp == null)
//          throw new com.sun.star.uno.Exception("Could not get XPropertySet from control.", this);
//
//        if(ControlNames[i].startsWith("txt"))
//        {
//          xProp.setPropertyValue("Text", aValue);
//        }
//
//        else if(ControlNames[i].startsWith("chk"))
//        {
//          xProp.setPropertyValue("State", aValue);
//        }
//
//        else if(ControlNames[i].startsWith("lst"))
//        {
//          xProp.setPropertyValue("StringItemList", aValue);
//
//          aValue = xLeaf.getPropertyValue(ControlNames[i] + "Selected");
//          xProp.setPropertyValue("SelectedItems", aValue);
//        }
//      }
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
    System.out.println("DialogEventHandler::_getComponentFactory");
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
    System.out.println("DialogEventHandler::__writeRegistryServiceInfo");
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