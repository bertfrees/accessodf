package be.docarch.accessodf.ooo;

import java.io.File;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.Handler;
import java.util.logging.FileHandler;
import java.util.logging.SimpleFormatter;

import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;
import com.sun.star.frame.XModel;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XLayoutManager;
import com.sun.star.frame.XDesktop;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XComponent;

import com.sun.star.lang.XSingleComponentFactory;
import com.sun.star.registry.XRegistryKey;
import com.sun.star.lib.uno.helper.WeakBase;
import com.sun.star.lib.uno.helper.Factory;

import be.docarch.accessodf.Constants;


public final class AccessODFAddOn extends WeakBase
   implements com.sun.star.lang.XInitialization,
              com.sun.star.frame.XDispatch,
              com.sun.star.lang.XServiceInfo,
              com.sun.star.frame.XDispatchProvider
{
    private final XComponentContext m_xContext;
    private com.sun.star.frame.XFrame m_xFrame;
    private static final String m_implementationName = AccessODFAddOn.class.getName();
    private static final String[] m_serviceNames = {
        "com.sun.star.frame.ProtocolHandler" };

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private static final String TMP_NAME = Constants.TMP_PREFIX;
    private static final File TMP_DIR = Constants.getTmpDirectory();
    private Handler fh = null;
    private File logFile = null;
    private int counter = 0;

    public AccessODFAddOn( XComponentContext context )
    {
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

    };

    public static XSingleComponentFactory __getComponentFactory( String sImplementationName ) {
        XSingleComponentFactory xFactory = null;

        if ( sImplementationName.equals( m_implementationName ) )
            xFactory = Factory.createComponentFactory(AccessODFAddOn.class, m_serviceNames);
        return xFactory;
    }

    public static boolean __writeRegistryServiceInfo( XRegistryKey xRegistryKey ) {
        return Factory.writeRegistryServiceInfo(m_implementationName,
                                                m_serviceNames,
                                                xRegistryKey);
    }

    // com.sun.star.lang.XInitialization:
    public void initialize( Object[] object )
        throws com.sun.star.uno.Exception
    {
        if ( object.length > 0 )
        {
            m_xFrame = (com.sun.star.frame.XFrame)UnoRuntime.queryInterface(
                com.sun.star.frame.XFrame.class, object[0]);
        }
    }

    // com.sun.star.frame.XDispatch:
     public void dispatch( com.sun.star.util.URL aURL,
                           com.sun.star.beans.PropertyValue[] aArguments ) {

         logger.info(String.valueOf(counter++));

         if ( aURL.Protocol.startsWith("be.docarch.accessodf.ooo.accessodfaddon")) {

            if ( aURL.Path.compareTo("TaskPanelCommand") == 0 ) {

                try {

                    XMultiComponentFactory xMCF = (XMultiComponentFactory) UnoRuntime.queryInterface(
                                                   XMultiComponentFactory.class, m_xContext.getServiceManager());
                    Object desktop = xMCF.createInstanceWithContext("com.sun.star.frame.Desktop", m_xContext);
                    XDesktop xDesktop = (XDesktop) UnoRuntime.queryInterface(XDesktop.class, desktop);
                    XComponent doc = (XComponent)xDesktop.getCurrentComponent();

                    XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, doc);
                    XFrame xFrame = xModel.getCurrentController().getFrame();
                    XPropertySet xPropertySet = (XPropertySet)UnoRuntime.queryInterface(
                                                 XPropertySet.class, xFrame);
                    XLayoutManager xLayoutManager = (XLayoutManager) UnoRuntime.queryInterface(
                                                     XLayoutManager.class, xPropertySet.getPropertyValue("LayoutManager"));

                    String toolPanel = "private:resource/toolpanel/be.docarch.accessibility.ooo.toolpanel";

                    if (xLayoutManager.isElementVisible(toolPanel)) { // geeft altijd false
                        xLayoutManager.hideElement(toolPanel);
                    } else {
                        xLayoutManager.showElement(toolPanel);
                    }

                } catch (Exception e) {
                    logger.log(Level.SEVERE, null, e);
                }

                return;
            }
        }
    }

    public void addStatusListener( com.sun.star.frame.XStatusListener xControl,
                                    com.sun.star.util.URL aURL )
    {
        // add your own code here
    }

    public void removeStatusListener( com.sun.star.frame.XStatusListener xControl,
                                       com.sun.star.util.URL aURL )
    {
        // add your own code here
    }

    // com.sun.star.lang.XServiceInfo:
    public String getImplementationName() {
         return m_implementationName;
    }

    public boolean supportsService( String sService ) {
        int len = m_serviceNames.length;

        for( int i=0; i < len; i++) {
            if (sService.equals(m_serviceNames[i]))
                return true;
        }
        return false;
    }

    public String[] getSupportedServiceNames() {
        return m_serviceNames;
    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch queryDispatch( com.sun.star.util.URL aURL,
                                                       String sTargetFrameName,
                                                       int iSearchFlags )
    {
        if ( aURL.Protocol.startsWith("be.docarch.accessodf.ooo.accessodfaddon"))
        {
            if ( aURL.Path.compareTo("TaskPanelCommand") == 0 )
                return this;
        }
        return null;
    }

    // com.sun.star.frame.XDispatchProvider:
    public com.sun.star.frame.XDispatch[] queryDispatches(
         com.sun.star.frame.DispatchDescriptor[] seqDescriptors )
    {
        int nCount = seqDescriptors.length;
        com.sun.star.frame.XDispatch[] seqDispatcher =
            new com.sun.star.frame.XDispatch[seqDescriptors.length];

        for( int i=0; i < nCount; ++i )
        {
            seqDispatcher[i] = queryDispatch(seqDescriptors[i].FeatureURL,
                                             seqDescriptors[i].FrameName,
                                             seqDescriptors[i].SearchFlags );
        }
        return seqDispatcher;
    }

}
