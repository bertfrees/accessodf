package be.docarch.accessibility.ooo.toolpanel;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Locale;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.ServiceLoader;
import java.util.ResourceBundle;
import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;
import java.io.FileFilter;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.beans.XPropertySet;
import com.sun.star.beans.XMultiPropertySet;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XComponent;
import com.sun.star.accessibility.XAccessible;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlModel;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XScrollBar;
import com.sun.star.awt.ScrollBarOrientation;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.WindowClass;
import com.sun.star.awt.PosSize;
import com.sun.star.awt.WindowDescriptor;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XWindowListener;
import com.sun.star.awt.WindowEvent;
import com.sun.star.awt.XAdjustmentListener;
import com.sun.star.awt.AdjustmentEvent;
import com.sun.star.awt.tree.XTreeControl;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.ui.XToolPanel;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.container.XNamed;
import com.sun.star.text.XTextTableCursor;
import com.sun.star.text.XTextRange;
import com.sun.star.text.XTextTable;
import com.sun.star.text.XTextContent;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XTextViewCursorSupplier;
import com.sun.star.table.XCellRange;
import com.sun.star.view.XSelectionSupplier;
import com.sun.star.view.XSelectionChangeListener;
import com.sun.star.lib.uno.helper.ComponentBase;

import be.docarch.accessibility.ooo.*;
import be.docarch.accessibility.*;

import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.DisposedException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;
import com.sun.star.awt.tree.ExpandVetoException;


public class AccessibilityPanel extends ComponentBase
                             implements XToolPanel,
                                        XItemListener,
                                        XActionListener,
                                        XWindowListener,
                                        XAdjustmentListener,
                                        XSelectionChangeListener {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private static Locale locale;
    private static ResourceBundle bundle;

    private static String L10N_errors;
    private static String L10N_warnings;
    private static String L10N_issue;
    private static String L10N_description;
    private static String L10N_suggestions;
    private static String L10N_clear;
    private static String L10N_ignore;
    private static String L10N_ignoreAll;
    private static String L10N_repair;
    private static String L10N_repairAll;
    private static String L10N_recheck;
    private static String L10N_moreHelp;

    private Rectangle[] controlRectangles = new Rectangle[13];

    private XWindow window = null;
    private XWindow mainWindow = null;
    private XWindow rightWindow = null;
    private XWindow bottomWindow = null;

    private XComponentContext xContext = null;
    private XWindowPeer docWindowPeer = null;

    private Document document = null;
    private IssueManager manager = null;

    private XButton refreshButton = null;
    private XButton ignoreButton = null;
    private XButton clearButton = null;
    private XButton repairButton = null;
    private XButton helpButton = null;
    private XTextComponent nameField = null;
    private XTextComponent descriptionField = null;
    private XTextComponent suggestionField = null;
    private XTreeControl treeControl = null;
    private XScrollBar hScrollBar = null;
    private XScrollBar vScrollBar = null;

    private XWindow refreshButtonWindow = null;
    private XWindow ignoreButtonWindow = null;
    private XWindow clearButtonWindow = null;
    private XWindow repairButtonWindow = null;
    private XWindow helpButtonWindow = null;
    private XWindow nameFieldWindow = null;
    private XWindow descriptionFieldWindow = null;
    private XWindow suggestionFieldWindow = null;
    private XWindow treeControlWindow = null;
    private XWindow hScrollBarWindow = null;
    private XWindow vScrollBarWindow = null;
    private XWindow nameLabelWindow = null;
    private XWindow statusImageWindow = null;
    private XWindow descriptionLabelWindow = null;
    private XWindow suggestionLabelWindow = null;

    private XPropertySet repairButtonProperties = null;
    private XPropertySet ignoreButtonProperties = null;
    private XPropertySet helpButtonProperties = null;
    private XPropertySet treeControlProperties = null;
    private XPropertySet statusImageControlProperties = null;

    private String imageDir = null;
    private int horizontalOffset = 0;
    private int verticalOffset = 0;

    private XTextViewCursor viewCursor = null;
    private XSelectionSupplier selectionSupplier = null;

    private Issue currentFocus = null;

    private XMutableTreeDataModel dataModel = null;
    private HashMap<Issue,XMutableTreeNode> issue2NodeMap = null;
    private HashMap<Check,XMutableTreeNode> check2NodeMap = null;
    private HashMap<Integer,Issue> node2IssueMap = null;
    private HashMap<Integer,Check> node2CheckMap = null;
    private HashMap<Integer,XMutableTreeNode> child2ParentMap = null; // (XMutableTreeNode)node.getParent(); => randomly results in ClassCastException ?

    private boolean ignoreSelectionEvent = false;
    private boolean showRepairedIssues = false;

    public AccessibilityPanel(XComponentContext xContext,
                              XWindow panelAnchorWindow) {

        logger.entering("AccessibilityPanel", "<init>");

        try {
            locale = new Locale(UnoUtils.getUILocale(xContext));
            Locale.setDefault(locale);
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            locale = Locale.getDefault();
        }
        bundle = ResourceBundle.getBundle("be/docarch/accessibility/ooo/toolpanel/l10n/toolpanel", locale);

        L10N_errors = bundle.getString("errors");
        L10N_warnings = bundle.getString("warnings");
        L10N_issue = bundle.getString("issue");
        L10N_description = bundle.getString("description");
        L10N_suggestions = bundle.getString("suggestions");
        L10N_clear = bundle.getString("clear");
        L10N_ignore = bundle.getString("ignore");
        L10N_ignoreAll = bundle.getString("ignoreAll");
        L10N_repair = bundle.getString("repair");
        L10N_repairAll = bundle.getString("repairAll");
        L10N_recheck = bundle.getString("recheck");
        L10N_moreHelp = bundle.getString("moreHelp");

        try {

            this.xContext = xContext;
            XMultiComponentFactory xMCF = (XMultiComponentFactory) UnoRuntime.queryInterface(
                                           XMultiComponentFactory.class, xContext.getServiceManager());
            XWindowPeer panelAnchorPeer = (XWindowPeer)UnoRuntime.queryInterface(XWindowPeer.class, panelAnchorWindow);
            docWindowPeer = panelAnchorPeer;

            if (panelAnchorPeer != null) {

                Rectangle panelAnchorSize = panelAnchorWindow.getPosSize();
                XToolkit toolkit = panelAnchorPeer.getToolkit();                
                XWindowPeer windowPeer = createWindow(toolkit, panelAnchorPeer);
                window = (XWindow)UnoRuntime.queryInterface(XWindow.class, windowPeer);

                if (window != null) {

                    window.setPosSize(0, 0, panelAnchorSize.Width, panelAnchorSize.Height, PosSize.POSSIZE);

                    XWindowPeer mainWindowPeer = createWindow(toolkit, windowPeer);
                    XWindowPeer rightWindowPeer = createWindow(toolkit, windowPeer);
                    XWindowPeer bottomWindowPeer = createWindow(toolkit, windowPeer);
                    mainWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, mainWindowPeer);
                    rightWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, rightWindowPeer);
                    bottomWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, bottomWindowPeer);

                    if (mainWindow != null && rightWindow != null && bottomWindow != null) {

                        for (int i=0; i< controlRectangles.length; i++) {
                            controlRectangles[i] = new Rectangle();
                        }

                        mainWindow.setVisible(true);
                        rightWindow.setVisible(false);
                        bottomWindow.setVisible(false);

                        rightWindow.setPosSize(panelAnchorSize.Width, 0, 20, panelAnchorSize.Height,  PosSize.POSSIZE);
                        bottomWindow.setPosSize(0, panelAnchorSize.Height, panelAnchorSize.Width, 20, PosSize.POSSIZE);

                        Rectangle mainRectangle = window.getPosSize();
                        Rectangle totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
                        boolean hScrollBarEnabled = totalRectangle.Width > mainRectangle.Width;
                        boolean vScrollBarEnabled = totalRectangle.Height > mainRectangle.Height;
                        if (hScrollBarEnabled && vScrollBarEnabled) {
                            mainRectangle.Width = mainRectangle.Width - 20;
                            mainRectangle.Height = mainRectangle.Height - 20;
                            totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
                        } else if (hScrollBarEnabled) {
                            mainRectangle.Height = mainRectangle.Height - 20;
                            totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
                            vScrollBarEnabled = totalRectangle.Height > mainRectangle.Height;
                            if (vScrollBarEnabled) {
                                mainRectangle.Width = mainRectangle.Width - 20;
                                totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
                            }
                        } else if (vScrollBarEnabled) {
                            mainRectangle.Width = mainRectangle.Width - 20;
                            totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
                            hScrollBarEnabled = totalRectangle.Width > mainRectangle.Width;
                            if (hScrollBarEnabled) {
                                mainRectangle.Height = mainRectangle.Height - 20;
                                totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
                            }
                        }

                        setWindowPosSize(mainWindow, mainRectangle);

                        XControl treeControlControl =      createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.tree.TreeControl",       null, null, controlRectangles[0]);
                        XControl nameLabelControl =        createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlFixedText",    null, null, controlRectangles[1]);
                        XControl statusImageControl =      createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlImageControl", null, null, controlRectangles[2]);
                        XControl nameFieldControl =        createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlEdit",         new String[] {"VScroll"},
                                                                                                                                                             new Object[] {false},
                                                                                                                                                             controlRectangles[3]);
                        XControl descriptionLabelControl = createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlFixedText",    null, null, controlRectangles[4]);
                        XControl descriptionFieldControl = createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlEdit",         null, null, controlRectangles[5]);
                        XControl suggestionLabelControl =  createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlFixedText",    null, null, controlRectangles[6]);
                        XControl suggestionFieldControl =  createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlEdit",         null, null, controlRectangles[7]);
                        XControl clearButtonControl =      createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlButton",       null, null, controlRectangles[8]);
                        XControl refreshButtonControl =    createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlButton",       null, null, controlRectangles[9]);
                        XControl ignoreButtonControl =     createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlButton",       null, null, controlRectangles[10]);
                        XControl repairButtonControl =     createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlButton",       null, null, controlRectangles[11]);
                        XControl helpButtonControl =       createControl(xMCF, xContext, toolkit, mainWindowPeer, "com.sun.star.awt.UnoControlButton",       new String[] {"PushButtonType"},
                                                                                                                                                             new Object[] {(short)PushButtonType.HELP_value},
                                                                                                                                                             controlRectangles[12]);
                        XControl hScrollBarControl =       createControl(xMCF, xContext, toolkit, bottomWindowPeer, "com.sun.star.awt.UnoControlScrollBar",  null, null, bottomWindow.getPosSize());
                        XControl vScrollBarControl =       createControl(xMCF, xContext, toolkit, rightWindowPeer,  "com.sun.star.awt.UnoControlScrollBar",  null, null, rightWindow.getPosSize());

                        refreshButton = (XButton)UnoRuntime.queryInterface(XButton.class, refreshButtonControl);
                        repairButton = (XButton)UnoRuntime.queryInterface(XButton.class, repairButtonControl);
                        ignoreButton = (XButton)UnoRuntime.queryInterface(XButton.class, ignoreButtonControl);
                        clearButton = (XButton)UnoRuntime.queryInterface(XButton.class, clearButtonControl);
                        helpButton = (XButton)UnoRuntime.queryInterface(XButton.class, helpButtonControl);
                        nameField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, nameFieldControl);
                        descriptionField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, descriptionFieldControl);
                        suggestionField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, suggestionFieldControl);
                        treeControl = (XTreeControl)UnoRuntime.queryInterface(XTreeControl.class, treeControlControl);
                        hScrollBar = (XScrollBar)UnoRuntime.queryInterface(XScrollBar.class, hScrollBarControl);
                        vScrollBar = (XScrollBar)UnoRuntime.queryInterface(XScrollBar.class, vScrollBarControl);

                        treeControlWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, treeControlControl);
                        nameLabelWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, nameLabelControl);
                        statusImageWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, statusImageControl);
                        nameFieldWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, nameFieldControl);
                        descriptionLabelWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, descriptionLabelControl);
                        descriptionFieldWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, descriptionFieldControl);
                        suggestionLabelWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, suggestionLabelControl);
                        suggestionFieldWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, suggestionFieldControl);
                        clearButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, clearButtonControl);
                        refreshButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, refreshButtonControl);
                        ignoreButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, ignoreButtonControl);
                        repairButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, repairButtonControl);
                        helpButtonWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, helpButtonControl);
                        hScrollBarWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, hScrollBarControl);
                        vScrollBarWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, vScrollBarControl);

                        repairButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, repairButtonControl.getModel());
                        ignoreButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, ignoreButtonControl.getModel());
                        helpButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, helpButtonControl.getModel());
                        treeControlProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, treeControlControl.getModel());
                        statusImageControlProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, statusImageControl.getModel());

                        XFixedText nameLabel = (XFixedText)UnoRuntime.queryInterface(XFixedText.class, nameLabelControl);
                        XFixedText descriptionLabel = (XFixedText)UnoRuntime.queryInterface(XFixedText.class, descriptionLabelControl);
                        XFixedText suggestionLabel = (XFixedText)UnoRuntime.queryInterface(XFixedText.class, suggestionLabelControl);

                        hScrollBar.setLineIncrement(10);
                        hScrollBar.setBlockIncrement(50);
                        hScrollBar.setOrientation(ScrollBarOrientation.HORIZONTAL);
                        vScrollBar.setLineIncrement(10);
                        vScrollBar.setBlockIncrement(50);
                        vScrollBar.setOrientation(ScrollBarOrientation.VERTICAL);

                        updateScrollBars(hScrollBarEnabled,    vScrollBarEnabled,
                                         mainRectangle.Width,  mainRectangle.Height,
                                         totalRectangle.Width, totalRectangle.Height);

                        nameLabel.setText(L10N_issue + ":");
                        descriptionLabel.setText(L10N_description + ":");
                        suggestionLabel.setText(L10N_suggestions + ":");
                        clearButton.setLabel(L10N_clear);
                        refreshButton.setLabel(L10N_recheck);
                        helpButton.setLabel(L10N_moreHelp);
                        
                        initialize();
                    }
                }
            }

        } catch (RuntimeException ex) {
            handleUnexpectedException(ex);
        }

        logger.exiting("AccessibilityPanel", "<init>");
    }

    private Rectangle computePosSizes(int visibleWidth,
                                      int visibleHeight) {

        int totalWidth = 0;
        int totalHeight = 0;

        int MARGIN =          2*2;
        int MARGIN2 =         2*6;
        int SIZE_IMAGE =      2*16;
        int HEIGHT_LABEL =    2*10;
        int WIDTH_BUTTON =    2*40;
        int HEIGHT_BUTTON =   2*13;
        int MIN_WIDTH =       2*100;
        int MIN_HEIGHT_TREE = 2*80;

        totalWidth = Math.max(MIN_WIDTH, visibleWidth);

        controlRectangles[0].X =       MARGIN;
        controlRectangles[0].Y =       MARGIN;
        controlRectangles[0].Width =   totalWidth - controlRectangles[0].X - MARGIN;
        controlRectangles[0].Height =  MIN_HEIGHT_TREE;

        controlRectangles[1].X =       MARGIN2;
        controlRectangles[1].Y =       controlRectangles[0].Y + controlRectangles[0].Height + MARGIN2;
        controlRectangles[1].Width =   totalWidth - controlRectangles[1].X - MARGIN;
        controlRectangles[1].Height =  HEIGHT_LABEL;

        controlRectangles[2].X =       MARGIN2;
        controlRectangles[2].Y =       controlRectangles[1].Y + controlRectangles[1].Height;
        controlRectangles[2].Width =   SIZE_IMAGE;
        controlRectangles[2].Height =  SIZE_IMAGE;
        
        controlRectangles[3].X =       controlRectangles[2].X + controlRectangles[2].Width + MARGIN2;
        controlRectangles[3].Y =       controlRectangles[2].Y + controlRectangles[2].Height - 24 - 4;
        controlRectangles[3].Width =   totalWidth - controlRectangles[3].X - MARGIN;
        controlRectangles[3].Height =  24;

        controlRectangles[4].X =       MARGIN2;
        controlRectangles[4].Y =       controlRectangles[3].Y + controlRectangles[3].Height + MARGIN2;
        controlRectangles[4].Width =   totalWidth - controlRectangles[4].X - MARGIN;
        controlRectangles[4].Height =  HEIGHT_LABEL;

        controlRectangles[5].X =       MARGIN;
        controlRectangles[5].Y =       controlRectangles[4].Y + controlRectangles[4].Height;
        controlRectangles[5].Width =   totalWidth - controlRectangles[5].X - MARGIN;
        controlRectangles[5].Height =  70;

        controlRectangles[6].X =       MARGIN2;
        controlRectangles[6].Y =       controlRectangles[5].Y + controlRectangles[5].Height + MARGIN2;
        controlRectangles[6].Width =   totalWidth - controlRectangles[6].X - MARGIN;
        controlRectangles[6].Height =  HEIGHT_LABEL;

        controlRectangles[7].X =       MARGIN;
        controlRectangles[7].Y =       controlRectangles[6].Y + controlRectangles[6].Height;
        controlRectangles[7].Width =   totalWidth - controlRectangles[7].X - MARGIN;
        controlRectangles[7].Height =  70;

        controlRectangles[8].X =       MARGIN;
        controlRectangles[8].Y =       controlRectangles[7].Y + controlRectangles[7].Height + MARGIN2;
        controlRectangles[8].Width =   WIDTH_BUTTON;
        controlRectangles[8].Height =  HEIGHT_BUTTON;

        controlRectangles[9].X =       controlRectangles[8].X + controlRectangles[8].Width + MARGIN;
        controlRectangles[9].Y =       controlRectangles[8].Y;
        controlRectangles[9].Width =   WIDTH_BUTTON;
        controlRectangles[9].Height =  HEIGHT_BUTTON;

        controlRectangles[10].X =      controlRectangles[8].X;
        controlRectangles[10].Y =      controlRectangles[8].Y + controlRectangles[8].Height + MARGIN;
        controlRectangles[10].Width =  WIDTH_BUTTON;
        controlRectangles[10].Height = HEIGHT_BUTTON;

        controlRectangles[11].X =      controlRectangles[9].X;
        controlRectangles[11].Y =      controlRectangles[10].Y;
        controlRectangles[11].Width =  WIDTH_BUTTON;
        controlRectangles[11].Height = HEIGHT_BUTTON;

        controlRectangles[12].X =      controlRectangles[10].X;
        controlRectangles[12].Y =      controlRectangles[10].Y + controlRectangles[10].Height + MARGIN;
        controlRectangles[12].Width =  WIDTH_BUTTON;
        controlRectangles[12].Height = HEIGHT_BUTTON;

        totalWidth = controlRectangles[0].X + controlRectangles[0].Width;
        totalHeight = controlRectangles[12].Y + controlRectangles[12].Height;

        if (totalHeight + MARGIN < visibleHeight) {
            controlRectangles[0].Height = controlRectangles[0].Height + (visibleHeight - totalHeight - MARGIN);
            for (int i=1; i<controlRectangles.length; i++) {
                controlRectangles[i].Y = controlRectangles[i].Y + (visibleHeight - totalHeight - MARGIN);
            }
        }

        totalHeight = controlRectangles[12].Y + controlRectangles[12].Height;
        return new Rectangle(0, 0, totalWidth, totalHeight);

    }

    public void updateScrollBars(boolean hScrollBarEnabled,
                                 boolean vScrollBarEnabled,
                                 int visibleWidth,
                                 int visibleHeight,
                                 int totalWidth,
                                 int totalHeight) {

        bottomWindow.setVisible(hScrollBarEnabled);
        rightWindow.setVisible(vScrollBarEnabled);

        horizontalOffset = 0;
        verticalOffset = 0;

        if (hScrollBarEnabled) {
            bottomWindow.setPosSize(    0, visibleHeight, visibleWidth, 20, PosSize.POSSIZE);
            hScrollBarWindow.setPosSize(0, 0,             visibleWidth, 20, PosSize.POSSIZE);
            hScrollBar.setMaximum(totalWidth);
            hScrollBar.setVisibleSize(visibleWidth);
            hScrollBar.setValue(horizontalOffset);
        }
        if (vScrollBarEnabled) {
            rightWindow.setPosSize(     visibleWidth, 0, 20, visibleHeight, PosSize.POSSIZE);
            vScrollBarWindow.setPosSize(0,            0, 20, visibleHeight, PosSize.POSSIZE);
            vScrollBar.setMaximum(totalHeight);
            vScrollBar.setVisibleSize(visibleHeight);
            vScrollBar.setValue(verticalOffset);
        }
    }

    public void initialize() {

        logger.entering("AccessibilityPanel", "initialize");

        try {

            XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
            imageDir = xPkgInfo.getPackageLocation("be.docarch.accessibility.ooo.accessibilitycheckeraddon") + "/images";
            document = new Document(xContext);
            InternalChecker internalChecker = new InternalChecker(document);
            List<ExternalChecker> externalCheckers = new ArrayList<ExternalChecker>();

            // Find & load odt2braille package

            FileFilter jarFilter = new FileFilter() {
                public boolean accept(File file) {
                    return file.getAbsolutePath().endsWith(".jar");
                }};
            String odt2braillePackage = xPkgInfo.getPackageLocation("be.docarch.odt2braille.ooo.odt2brailleaddon");
            if (odt2braillePackage.length() > 0) {
                File dir = new File(odt2braillePackage.substring(6));
                List<URL> urls = new ArrayList<URL>();
                File[] jars = dir.listFiles(jarFilter);
                for (File jar : jars) {
                    urls.add(new URL("jar:file://" + jar.toURI().toURL().getPath() + "!/"));
                    break;
                }
                File lib = new File(dir.getAbsolutePath() + File.separator + "lib");
                jars = lib.listFiles(jarFilter);
                for (File jar : jars) {
                    urls.add(new URL("jar:file://" + jar.toURI().toURL().getPath() + "!/"));
                }
                URLClassLoader classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), ExternalChecker.class.getClassLoader());                
                for (ExternalChecker checker : ServiceLoader.load(ExternalChecker.class, classLoader)) {
                    externalCheckers.add(checker);
                }
            }

            manager = new IssueManager(document, internalChecker, externalCheckers);
            selectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(
                                 XSelectionSupplier.class, document.xModel.getCurrentController());
            XTextViewCursorSupplier xViewCursorSupplier =
                (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                    XTextViewCursorSupplier.class, document.xModel.getCurrentController());
            viewCursor = xViewCursorSupplier.getViewCursor();

            issue2NodeMap = new HashMap<Issue,XMutableTreeNode>();
            check2NodeMap = new HashMap<Check,XMutableTreeNode>();
            node2IssueMap = new HashMap<Integer,Issue>();
            node2CheckMap = new HashMap<Integer,Check>();
            child2ParentMap = new HashMap<Integer,XMutableTreeNode>();

            window.addWindowListener(this);
            hScrollBar.addAdjustmentListener(this);
            vScrollBar.addAdjustmentListener(this);
            refreshButton.addActionListener(this);
            repairButton.addActionListener(this);
            clearButton.addActionListener(this);
            ignoreButton.addActionListener(this);
            treeControl.addSelectionChangeListener(this);

            manager.select(null);

            buildTreeView();
            updateDialogFields();
            updateFocus();

        } catch (java.net.MalformedURLException ex) {
            handleUnexpectedException(ex);
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

        logger.exiting("AccessibilityPanel", "initialize");
    }

    private void refresh() throws IllegalArgumentException,
                                  ElementExistException,
                                  RepositoryException,
                                  UnknownPropertyException,
                                  NoSuchElementException,
                                  WrappedTargetException,
                                  PropertyVetoException,
                                  ExpandVetoException,
                                  java.text.ParseException,
                                  java.io.IOException,
                                  com.sun.star.uno.Exception {

        logger.entering("AccessibilityPanel", "refresh");

        manager.refresh();

        currentFocus = null;
        Issue oldIssue = manager.selectedIssue();
        Check oldCheck = manager.selectedCheck();

        manager.select(null);

        buildTreeView();

        if (selectNode(oldIssue)) {
            manager.select(oldIssue);
        } else if (selectNode(oldCheck)) {
            manager.select(oldCheck);
        } else {
            treeControl.clearSelection();
        }

        updateDialogFields();
        updateFocus();

        ignoreButtonProperties.setPropertyValue("Enabled", true); // hack
        repairButtonProperties.setPropertyValue("Enabled", true);

        logger.exiting("AccessibilityPanel", "refresh");

    }

    private boolean selectNode(Object o)
                        throws IllegalArgumentException,
                               ExpandVetoException {

        if (o != null) {
            if (o instanceof Check) {
                Check check = (Check)o;
                if (check2NodeMap.containsKey(check)) {
                    ignoreSelectionEvent = true;
                    treeControl.makeNodeVisible(check2NodeMap.get(check));
                    treeControl.select(check2NodeMap.get(check));
                    ignoreSelectionEvent = false;
                    return true;
                }
            } else if (o instanceof Issue) {
                Issue issue = (Issue)o;
                if (issue2NodeMap.containsKey(issue)) {
                    ignoreSelectionEvent = true;
                    treeControl.makeNodeVisible(issue2NodeMap.get(issue));
                    treeControl.select(issue2NodeMap.get(issue));
                    ignoreSelectionEvent = false;
                    return true;
                }
            }
        }

        return false;
    }

    private void clear() throws IllegalArgumentException,
                                RepositoryException,
                                UnknownPropertyException,
                                NoSuchElementException,
                                WrappedTargetException,
                                PropertyVetoException,
                                com.sun.star.uno.Exception {

        logger.entering("AccessibilityPanel", "clear");

        manager.clear();

        manager.select(null);        
        buildTreeView();
        updateDialogFields();
        updateFocus();

        logger.exiting("AccessibilityPanel", "clear");

    }

    private void buildTreeView() throws com.sun.star.uno.Exception {

        manager.arrangeIssues();

        dataModel = (XMutableTreeDataModel)UnoRuntime.queryInterface(
                        XMutableTreeDataModel.class, document.xMCF.createInstanceWithContext(
                            "com.sun.star.awt.tree.MutableTreeDataModel", document.xContext));

        issue2NodeMap.clear();
        check2NodeMap.clear();
        node2IssueMap.clear();
        node2CheckMap.clear();
        child2ParentMap.clear();

        int i = 0;
        int checkNodeValue;
        int issueNodeValue;
        List<Issue> issueList;
        XMutableTreeNode issueNode;
        XMutableTreeNode checkNode;
        XMutableTreeNode rootNode = dataModel.createNode("root", true);
        XMutableTreeNode warningsNode = dataModel.createNode(L10N_warnings.toUpperCase(), true);
        XMutableTreeNode errorsNode = dataModel.createNode(L10N_errors.toUpperCase(), true);
        dataModel.setRoot(rootNode);

        for (Check check : manager.getChecks()) {
            
            issueList = manager.getIssuesByCheck(check);
            checkNodeValue = i++;

            if (issueList.size() == 1 &&
                issueList.get(0).getElement().getType() == Element.Type.DOCUMENT) {
                checkNode = dataModel.createNode("", false);
                checkNode.setDataValue(new Any(Type.LONG, checkNodeValue));
                node2IssueMap.put(checkNodeValue, issueList.get(0));
                issue2NodeMap.put(issueList.get(0), checkNode);
                check2NodeMap.put(check, checkNode);
            } else {
                checkNode = dataModel.createNode("", true);
                checkNode.setDataValue(new Any(Type.LONG, checkNodeValue));
                node2CheckMap.put(checkNodeValue, check);
                check2NodeMap.put(check, checkNode);
                for (Issue issue : issueList) {
                    issueNode = dataModel.createNode(issue.getName(), false);
                    issueNodeValue = i++;
                    issueNode.setDataValue(new Any(Type.LONG, issueNodeValue));
                    node2IssueMap.put(issueNodeValue, issue);
                    issue2NodeMap.put(issue, issueNode);
                    checkNode.appendChild(issueNode);
                    child2ParentMap.put(issueNodeValue, checkNode);
                    updateNode(issue);
                }
            }

            switch (check.getStatus()) {
                case ERROR:
                    errorsNode.appendChild(checkNode);
                    child2ParentMap.put(checkNodeValue, errorsNode);
                    break;
                case ALERT:
                    warningsNode.appendChild(checkNode);
                    child2ParentMap.put(checkNodeValue, warningsNode);
                    break;
            }

            updateNode(check);
        }

        if (errorsNode.getChildCount()>0) {
            rootNode.appendChild(errorsNode);
            errorsNode.setNodeGraphicURL(imageDir + "/alert_20x20.gif");
        }
        if (warningsNode.getChildCount()>0) {
            rootNode.appendChild(warningsNode);
            warningsNode.setNodeGraphicURL(imageDir + "/warning_20x20.gif");
        }

        treeControlProperties.setPropertyValue("DataModel", dataModel);
        treeControlProperties.setPropertyValue("RowHeight", 20);
        treeControlProperties.setPropertyValue("RootDisplayed", false);
        treeControlProperties.setPropertyValue("ShowsHandles", false);
        treeControlProperties.setPropertyValue("ShowsRootHandles", false);
        treeControlProperties.setPropertyValue("SelectionType", com.sun.star.view.SelectionType.SINGLE);

        if (warningsNode.getChildCount()>0) {
            treeControl.makeNodeVisible(warningsNode.getChildAt(0));
        }
        if (errorsNode.getChildCount()>0) {
            treeControl.makeNodeVisible(errorsNode.getChildAt(0));
        }
    }

    private void updateDialogFields() throws UnknownPropertyException,
                                             PropertyVetoException,
                                             WrappedTargetException,
                                             IllegalArgumentException {

        Issue selectedIssue = manager.selectedIssue();
        Check selectedCheck = manager.selectedCheck();

        String image = "";
        IssueManager.Status status = null;
        nameField.setText("");
        descriptionField.setText("");
        suggestionField.setText("");
        statusImageControlProperties.setPropertyValue("ImageURL", "");

        if (selectedCheck != null) {
            nameField.setText(selectedCheck.getName());
            descriptionField.setText(selectedCheck.getDescription());
            suggestionField.setText(selectedCheck.getSuggestion());
            if (selectedIssue != null) {
                status = manager.getIssueStatus(selectedIssue);
            } else {
                status = manager.getCheckStatus(selectedCheck);
            }
            switch (status) {
                case IGNORED:
                    image = "ignored.png"; break;
                case REPAIRED:
                    image = "repaired.gif"; break;
                case ALERT:
                    image = "warning.png"; break;
                case ERROR:
                    image = "alert.png"; break;
            }

            statusImageControlProperties.setPropertyValue("ImageURL", imageDir + "/" + image);
        }

        updateButtons();
    }

    private void updateFocus() {

        Issue selectedIssue = manager.selectedIssue();

        try {

            if (selectedIssue == null) {
                //removeSelection();
            } else if (currentFocus == null) {
                focus(selectedIssue.getElement());
            } else if (!selectedIssue.getElement().equals(currentFocus.getElement())) {
                focus(selectedIssue.getElement());
            }

            currentFocus = selectedIssue;

        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (NoSuchElementException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private void updateButtons() throws UnknownPropertyException,
                                        PropertyVetoException,
                                        WrappedTargetException,
                                        IllegalArgumentException {

        Issue selectedIssue = manager.selectedIssue();
        Check selectedCheck = manager.selectedCheck();

        boolean repair = false;
        boolean ignore = false;

        if (selectedIssue != null) {
            ignoreButton.setLabel(L10N_ignore);
            repairButton.setLabel(L10N_repair);
            if (!selectedIssue.repaired() && !selectedIssue.ignored()) {
                ignore = true;
                if (selectedCheck.getRepairMode() == Check.RepairMode.AUTO ||
                    selectedCheck.getRepairMode() == Check.RepairMode.SEMI_AUTOMATED) {
                    repair = true;
                }
            }
        } else {
            ignoreButton.setLabel(L10N_ignoreAll);
            repairButton.setLabel(L10N_repairAll);
            if (selectedCheck != null) {
                IssueManager.Status status = manager.getCheckStatus(selectedCheck);
                if (status != IssueManager.Status.REPAIRED &&
                    status != IssueManager.Status.IGNORED) {
                    ignore = true;
                    if (selectedCheck.getRepairMode() == Check.RepairMode.AUTO) {
                        repair = true;
                    }
                }
            }
        }

        ignoreButtonProperties.setPropertyValue("Enabled", ignore);
        repairButtonProperties.setPropertyValue("Enabled", repair);
        helpButtonProperties.setPropertyValue("HelpURL", getHelpURL(selectedCheck));
    }

    private String getHelpURL(Check check) {

        if (check != null) {
            return "be.docarch.accessibility.ooo.accessibilitycheckeraddon:" + check.getIdentifier();
        } else {
            return "be.docarch.accessibility.ooo.accessibilitycheckeraddon:main";
        }
    }

    private boolean updateNode(Check check)
                        throws IllegalArgumentException {

        XMutableTreeNode node = check2NodeMap.get(check);
        if (node == null) { return false; }
        Object d = node.getDataValue();
        if (d == null || AnyConverter.isVoid(d)) { return false; }
        XMutableTreeNode parent = child2ParentMap.get(AnyConverter.toInt(d));
        if (parent == null) { return false; }

        String image = "";
        String text = "";
        switch (manager.getCheckStatus(check)) {
            case IGNORED:
                image = "gray_15x20.png";
                text = "IGNORED";
                break;
            case REPAIRED:
                if (!showRepairedIssues) {
                    try {
                        check2NodeMap.remove(check);
                        ignoreSelectionEvent = true;
                        parent.removeChildByIndex(parent.getIndex(node));
                        ignoreSelectionEvent = false;
                        return false;
                    } catch (IndexOutOfBoundsException e) {
                    }
                }
                image = "green_15x20.png";
                text = "REPAIRED";
                break;
            case ALERT:
                image = "orange_15x20.png";
                text = "WARNING";
                break;

            case ERROR:
                image = "red_15x20.png";
                text = "ERROR";
                break;
        }

        //text += ": " + check.getName();
        text = check.getName();

        if (node.getChildCount()>0) {
            text += " (" + node.getChildCount() + ")";
        }

        //node.setNodeGraphicURL(imageDir + "/" + image);
        node.setDisplayValue(text);
        return true;
    }

    private boolean updateNode(Issue issue)
                        throws IllegalArgumentException {

        XMutableTreeNode node = issue2NodeMap.get(issue);
        if (node == null) { return false; }
        Object d = node.getDataValue();
        if (d == null || AnyConverter.isVoid(d)) { return false; }
        XMutableTreeNode parent = child2ParentMap.get(AnyConverter.toInt(d));
        if (parent == null) { return false; }

        String image = "";
        switch (manager.getIssueStatus(issue)) {
            case IGNORED:
                image = "gray_15x20.png";
                break;
            case REPAIRED:
                if (!showRepairedIssues) {
                    try {
                        issue2NodeMap.remove(issue);
                        if (issue.getElement().getType() == Element.Type.DOCUMENT) {
                            check2NodeMap.remove(issue.getCheck());
                        }
                        ignoreSelectionEvent = true;
                        parent.removeChildByIndex(parent.getIndex(node));
                        ignoreSelectionEvent = false;
                        return false;
                    } catch (IndexOutOfBoundsException e) {
                    }
                }
                image = "green_15x20.png";
                break;
            case ALERT:
                image = "orange_15x20.png";
                break;
            case ERROR:
                image = "red_15x20.png";
                break;
        }

        //node.setNodeGraphicURL(imageDir + "/" + image);
        return true;
    }

    private boolean focus(Element element)
                   throws IllegalArgumentException,
                          NoSuchElementException,
                          WrappedTargetException {

        removeSelection();

        if (element.exists()) {

            switch(element.getType()) {
                case DOCUMENT:
                    return true;

                case PARAGRAPH:
                    XTextContent paragraph = element.getParagraph();
                    if (paragraph != null) {
                        return selectionSupplier.select(paragraph.getAnchor());
                    }
                    break;

                case SPAN:
                    XTextContent[] text = element.getSpan();
                    if (text != null) {
                        viewCursor.gotoRange(text[0].getAnchor().getEnd(), false);
                        viewCursor.gotoRange(text[1].getAnchor().getStart(), true);
                        return true;
                    }
                    break;

                case TABLE:
                    XTextTable table = element.getTable();
                    if (table != null) {
                        String[] cellNames = table.getCellNames();
                        viewCursor.gotoRange((XTextRange)UnoRuntime.queryInterface(
                                              XTextRange.class, table.getCellByName(cellNames[cellNames.length - 1])), false);
                        if (cellNames.length > 1) {
                            XCellRange cellRange = (XCellRange)UnoRuntime.queryInterface(XCellRange.class, table);
                            XTextTableCursor cursor = table.createCursorByCellName(cellNames[0]);
                            cursor.gotoCellByName(cellNames[cellNames.length - 1], true);
                            selectionSupplier.select(cellRange.getCellRangeByName(cursor.getRangeName()));
                        }
                        return true;
                    }
                    break;

                case OBJECT:
                    XNamed object = element.getObject();
                    if (object != null) {
                        return selectionSupplier.select(object);
                    }
                    break;

                default:
                    return false;
            }
        }

        return false;
    }

    private void removeSelection() throws IllegalArgumentException,
                                          NoSuchElementException,
                                          WrappedTargetException {

        selectionSupplier.select(document.getFirstParagraph().getAnchor());
        viewCursor.gotoStart(false);

    }

    // XActionListener
    public void actionPerformed(ActionEvent actionEvent) {

        Object source = actionEvent.Source;
        Issue selectedIssue = manager.selectedIssue();
        Check selectedCheck = manager.selectedCheck();

        try {

            if (source.equals(refreshButton)) {

                refresh();

            } else if (source.equals(clearButton)) {

                clear();

            } else if (source.equals(ignoreButton)) {

                if (selectedIssue != null) {
                    selectedIssue.ignored(true);
                    if (!updateNode(selectedIssue)) {
                        manager.select(selectedIssue.getCheck());
                        selectNode(manager.selectedCheck());
                    }
                    if (!updateNode(manager.selectedCheck())) {
                        manager.select(null);
                        ignoreSelectionEvent = true;
                        treeControl.clearSelection();
                        ignoreSelectionEvent = false;
                    }                    
                } else if (selectedCheck != null) {
                    for (Issue issue : manager.getIssuesByCheck(selectedCheck)) {
                        issue.ignored(true);
                        updateNode(issue);
                    }
                    if (!updateNode(selectedCheck)) {
                        manager.select(null);
                        ignoreSelectionEvent = true;
                        treeControl.clearSelection();
                        ignoreSelectionEvent = false;
                    }
                }
                updateDialogFields();
                updateFocus();

            } else if (source.equals(repairButton)) {

                if (selectedIssue != null) {
                    if (manager.repair(selectedIssue)) {
                        if (!updateNode(selectedIssue)) {
                            manager.select(selectedIssue.getCheck());
                            selectNode(manager.selectedCheck());
                        }
                        if (!updateNode(manager.selectedCheck())) {
                            manager.select(null);
                            ignoreSelectionEvent = true;
                            treeControl.clearSelection();
                            ignoreSelectionEvent = false;
                        }
                    }
                } else if (selectedCheck != null) {
                    boolean repaired = false;
                    for (Issue issue : manager.getIssuesByCheck(selectedCheck)) {
                        if (!issue.repaired() && !issue.ignored()) {
                            if (manager.repair(issue)) {
                                repaired = true;
                                updateNode(issue);
                            }
                        }
                    }
                    if (repaired) {
                        if (!updateNode(selectedCheck)) {
                            manager.select(null);
                            ignoreSelectionEvent = true;
                            treeControl.clearSelection();
                            ignoreSelectionEvent = false;
                        }
                    }
                }
                updateDialogFields();
                updateFocus();
            }

        } catch (java.io.IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch(IllegalArgumentException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (ElementExistException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (RepositoryException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (UnknownPropertyException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (NoSuchElementException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (PropertyVetoException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IndexOutOfBoundsException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (java.text.ParseException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (RuntimeException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    // XSelectionChangeListener
    public void selectionChanged(EventObject event) {

        if (!ignoreSelectionEvent) {

            try {

                Object o = treeControl.getSelection();
                if (o != null && !AnyConverter.isVoid(o)) {
                    XMutableTreeNode node = (XMutableTreeNode)AnyConverter.toObject(XMutableTreeNode.class, o);
                    Object d = node.getDataValue();
                    if (d != null && !AnyConverter.isVoid(d)) {
                        int index = AnyConverter.toInt(d);
                        if (node2IssueMap.containsKey(index)) {
                            manager.select(node2IssueMap.get(index));
                        } else if (node2CheckMap.containsKey(index)) {
                            manager.select(node2CheckMap.get(index));
                        } else {
                            manager.select(null);
                        }
                    } else {
                        manager.select(null);
                    }
                    updateDialogFields();
                    updateFocus();
                }

            } catch (IllegalArgumentException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (UnknownPropertyException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (PropertyVetoException ex) {
                logger.log(Level.SEVERE, null, ex);
            } catch (WrappedTargetException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        }
    }

    public void disposing() {

        if (window == null) {
            return;
        }

        window.removeWindowListener(this);
        hScrollBar.removeAdjustmentListener(this);
        vScrollBar.removeAdjustmentListener(this);
        refreshButton.removeActionListener(this);
        repairButton.removeActionListener(this);
        clearButton.removeActionListener(this);
        ignoreButton.removeActionListener(this);
        treeControl.removeSelectionChangeListener(this);

        try{
            XComponent xWindowComp = (XComponent)UnoRuntime.queryInterface(XComponent.class, window);
            xWindowComp.dispose();
        } catch(Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    // XItemListener
    public void itemStateChanged(ItemEvent event) {
    }

    // XWindowListener
    public void windowResized(WindowEvent event) {

        Rectangle mainRectangle = window.getPosSize();
        Rectangle totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
        boolean hScrollBarEnabled = totalRectangle.Width > mainRectangle.Width;
        boolean vScrollBarEnabled = totalRectangle.Height > mainRectangle.Height;
        if (hScrollBarEnabled && vScrollBarEnabled) {
            mainRectangle.Width = mainRectangle.Width - 20;
            mainRectangle.Height = mainRectangle.Height - 20;
            totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
        } else if (hScrollBarEnabled) {
            mainRectangle.Height = mainRectangle.Height - 20;
            totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
            vScrollBarEnabled = totalRectangle.Height > mainRectangle.Height;
            if (vScrollBarEnabled) {
                mainRectangle.Width = mainRectangle.Width - 20;
                totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
            }
        } else if (vScrollBarEnabled) {
            mainRectangle.Width = mainRectangle.Width - 20;
            totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
            hScrollBarEnabled = totalRectangle.Width > mainRectangle.Width;
            if (hScrollBarEnabled) {
                mainRectangle.Height = mainRectangle.Height - 20;
                totalRectangle = computePosSizes(mainRectangle.Width, mainRectangle.Height);
            }
        }

        setWindowPosSize(treeControlWindow,      controlRectangles[0]);
        setWindowPosSize(nameLabelWindow,        controlRectangles[1]);
        setWindowPosSize(statusImageWindow,      controlRectangles[2]);
        setWindowPosSize(nameFieldWindow,        controlRectangles[3]);
        setWindowPosSize(descriptionLabelWindow, controlRectangles[4]);
        setWindowPosSize(descriptionFieldWindow, controlRectangles[5]);
        setWindowPosSize(suggestionLabelWindow,  controlRectangles[6]);
        setWindowPosSize(suggestionFieldWindow,  controlRectangles[7]);
        setWindowPosSize(clearButtonWindow,      controlRectangles[8]);
        setWindowPosSize(refreshButtonWindow,    controlRectangles[9]);
        setWindowPosSize(ignoreButtonWindow,     controlRectangles[10]);
        setWindowPosSize(repairButtonWindow,     controlRectangles[11]);
        setWindowPosSize(helpButtonWindow,       controlRectangles[12]);

        setWindowPosSize(mainWindow, mainRectangle);

        updateScrollBars(hScrollBarEnabled,    vScrollBarEnabled,
                         mainRectangle.Width,  mainRectangle.Height,
                         totalRectangle.Width, totalRectangle.Height);

    }

    public void windowMoved(WindowEvent event) {
    }

    public void windowShown(EventObject event) {
    }

    public void windowHidden(EventObject event) {
    }

    // XAdjustmentListener
    public void adjustmentValueChanged(AdjustmentEvent event) {

        Object source = event.Source;

        if (source.equals(hScrollBar) ||
            source.equals(vScrollBar)) {

            horizontalOffset = hScrollBar.getValue();
            verticalOffset = vScrollBar.getValue();

            setWindowPosSize(treeControlWindow,      controlRectangles[0],  horizontalOffset, verticalOffset);
            setWindowPosSize(nameLabelWindow,        controlRectangles[1],  horizontalOffset, verticalOffset);
            setWindowPosSize(statusImageWindow,      controlRectangles[2],  horizontalOffset, verticalOffset);
            setWindowPosSize(nameFieldWindow,        controlRectangles[3],  horizontalOffset, verticalOffset);
            setWindowPosSize(descriptionLabelWindow, controlRectangles[4],  horizontalOffset, verticalOffset);
            setWindowPosSize(descriptionFieldWindow, controlRectangles[5],  horizontalOffset, verticalOffset);
            setWindowPosSize(suggestionLabelWindow,  controlRectangles[6],  horizontalOffset, verticalOffset);
            setWindowPosSize(suggestionFieldWindow,  controlRectangles[7],  horizontalOffset, verticalOffset);
            setWindowPosSize(clearButtonWindow,      controlRectangles[8],  horizontalOffset, verticalOffset);
            setWindowPosSize(refreshButtonWindow,    controlRectangles[9],  horizontalOffset, verticalOffset);
            setWindowPosSize(ignoreButtonWindow,     controlRectangles[10], horizontalOffset, verticalOffset);
            setWindowPosSize(repairButtonWindow,     controlRectangles[11], horizontalOffset, verticalOffset);
            setWindowPosSize(helpButtonWindow,       controlRectangles[12], horizontalOffset, verticalOffset);
        }
    }

    // XEventListener
    public void disposing(EventObject event) {}

    XWindowPeer createWindow(XToolkit toolkit,
                             XWindowPeer parentWindow) {

        try {

            WindowDescriptor aWindow = new WindowDescriptor();
            aWindow.Type = WindowClass.SIMPLE;
            aWindow.WindowServiceName = "window";
            aWindow.Parent = parentWindow;
            aWindow.WindowAttributes = 0;

            return toolkit.createWindow(aWindow);

        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private XControl createControl(XMultiComponentFactory xMCF,
                                   XComponentContext xContext,
                                   XToolkit toolkit,
                                   XWindowPeer windowPeer,
                                   String type,
                                   String[] propNames,
                                   Object[] propValues,
                                   Rectangle rectangle) {
        try {

            XControl control = (XControl)UnoRuntime.queryInterface(
                                XControl.class, xMCF.createInstanceWithContext(type, xContext));
            XControlModel controlModel = (XControlModel)UnoRuntime.queryInterface(
                                          XControlModel.class, xMCF.createInstanceWithContext(type + "Model", xContext));
            control.setModel(controlModel);
            XMultiPropertySet properties = (XMultiPropertySet)UnoRuntime.queryInterface(XMultiPropertySet.class, control.getModel());
            SortedMap<String,Object> props = new TreeMap<String,Object>();
            
            if (type.equals("com.sun.star.awt.UnoControlImageControl")) {
                props.put("Border", (short)0);
            } else if (type.equals("com.sun.star.awt.UnoControlEdit")){
                props.put("MultiLine", true);
                props.put("ReadOnly",  true);
                props.put("VScroll",   true);
            }            
            if (propNames != null) {            
                for (int i=0; i<propNames.length; i++) {
                    props.put(propNames[i], propValues[i]);                    
                }
            }            
            if (props.size() > 0) {
                properties.setPropertyValues((String[])props.keySet().toArray(new String[props.size()]),
                                             (Object[])props.values().toArray(new Object[props.size()]));
            }
            control.createPeer(toolkit, windowPeer);
            XWindow controlWindow = (XWindow)UnoRuntime.queryInterface(XWindow.class, control);
            setWindowPosSize(controlWindow, rectangle);

            return control;

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private void setWindowPosSize(XWindow window,
                                  Rectangle posSize) {

        setWindowPosSize(window, posSize, 0, 0);
    }

    private void setWindowPosSize(XWindow window,
                                  Rectangle posSize,
                                  int horizontalOffset,
                                  int verticalOffset) {

        window.setPosSize(posSize.X - horizontalOffset, posSize.Y - verticalOffset, posSize.Width, posSize.Height, PosSize.POSSIZE);
    }

    private void handleUnexpectedException (Exception ex) {

        logger.log(Level.SEVERE, null, ex);
        UnoAwtUtils.showErrorMessageBox(docWindowPeer, "Unexpected exception","Unexpected exception");

    }

    //XToolPanel
    public XWindow getWindow() {

        if (window == null) {
            throw new DisposedException("", this );
        }
        return window;
    }

    public XAccessible createAccessible(XAccessible i_rParentAccessible ) {

        if (window == null) {
            throw new DisposedException("", this );
        }
        // TODO: the following is wrong, since it doesn't respect i_rParentAccessible. In a real extension, you should
        // implement this correctly :)
        return (XAccessible)UnoRuntime.queryInterface(XAccessible.class, getWindow());
    }
}
