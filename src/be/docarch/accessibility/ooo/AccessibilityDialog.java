package be.docarch.accessibility.ooo;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.HashMap;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.AnyConverter;
import com.sun.star.uno.Any;
import com.sun.star.uno.Type;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ActionEvent;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XButton;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.tree.XTreeControl;
import com.sun.star.awt.tree.XMutableTreeDataModel;
import com.sun.star.awt.tree.XMutableTreeNode;
import com.sun.star.lang.EventObject;
import com.sun.star.lang.XComponent;
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

import com.sun.star.container.NoSuchElementException;
import com.sun.star.container.ElementExistException;
import com.sun.star.rdf.RepositoryException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.accessibility.Check;
import be.docarch.accessibility.ExternalChecker;
import be.docarch.accessibility.Constants;

/**
 *
 * @author Bert Frees
 */
public class AccessibilityDialog implements XActionListener,
                                            XItemListener,
                                            XSelectionChangeListener {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private Document document = null;
    private IssueManager manager = null;

    private String imageDir = null;

    private XDialog dialog = null;
    private XButton refreshButton = null;
    private XButton ignoreButton = null;
    private XButton clearButton = null;
    private XButton repairButton = null;
    private XTextComponent nameField = null;
    private XTextComponent descriptionField = null;
    private XTextComponent suggestionField = null;
    private XTreeControl treeControl = null;

    private XPropertySet repairButtonProperties = null;
    private XPropertySet ignoreButtonProperties = null;
    private XPropertySet treeControlProperties = null;
    private XPropertySet statusImageControlProperties = null;

    private XTextViewCursor viewCursor = null;
    private XSelectionSupplier selectionSupplier = null;
    private XDispatchHelper dispatcher = null;
    private XDispatchProvider dispatchProvider = null;

    private XMutableTreeDataModel dataModel = null;
    private HashMap<Issue,XMutableTreeNode> issue2NodeMap = null;
    private HashMap<Check,XMutableTreeNode> check2NodeMap = null;
    private HashMap<Integer,Issue> node2IssueMap = null;
    private HashMap<Integer,Check> node2CheckMap = null;


    public AccessibilityDialog(Document document,
                               InternalChecker internalChecker,
                               ExternalChecker[] externalCheckers)
                        throws IllegalArgumentException,
                               NoSuchElementException,
                               WrappedTargetException,
                               RepositoryException,
                               java.text.ParseException,
                               com.sun.star.uno.Exception {
    
        logger.entering("AccessibilityDialog", "<init>");

        this.document = document;
        manager = new IssueManager(document, internalChecker, null);

        selectionSupplier = (XSelectionSupplier)UnoRuntime.queryInterface(
                             XSelectionSupplier.class, document.xModel.getCurrentController());
        XTextViewCursorSupplier xViewCursorSupplier =
            (XTextViewCursorSupplier)UnoRuntime.queryInterface(
                XTextViewCursorSupplier.class, document.xModel.getCurrentController());
        viewCursor = xViewCursorSupplier.getViewCursor();
        dispatcher = (XDispatchHelper)UnoRuntime.queryInterface(
                         XDispatchHelper.class, document.xMCF.createInstanceWithContext(
                             "com.sun.star.frame.DispatchHelper", document.xContext));
        dispatchProvider = (XDispatchProvider)UnoRuntime.queryInterface(
                                XDispatchProvider.class, document.xModel.getCurrentController().getFrame());
        
        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(document.xContext);
        XDialogProvider2 xDialogProvider = DialogProvider2.create(document.xContext);
        dialog = xDialogProvider.createDialog(xPkgInfo.getPackageLocation(
                "be.docarch.accessibility.ooo.accessibilitycheckeraddon") + "/dialogs/Dialog.xdl");
        XControlContainer dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        refreshButton = (XButton)UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl("CommandButton3"));
        repairButton = (XButton)UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl("CommandButton2"));
        ignoreButton = (XButton)UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl("CommandButton1"));
        clearButton = (XButton)UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl("CommandButton4"));
        nameField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, dialogControlContainer.getControl("TextField2"));
        treeControl = (XTreeControl)UnoRuntime.queryInterface(XTreeControl.class, dialogControlContainer.getControl("TreeControl1"));

        repairButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, 
                                    ((XControl)UnoRuntime.queryInterface(XControl.class, repairButton)).getModel());
        ignoreButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                                    ((XControl)UnoRuntime.queryInterface(XControl.class, ignoreButton)).getModel());
        treeControlProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                                    ((XControl)UnoRuntime.queryInterface(XControl.class, treeControl)).getModel());
        statusImageControlProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                                        ((XControl)UnoRuntime.queryInterface(XControl.class,
                                        dialogControlContainer.getControl("ImageControl1"))).getModel());

        imageDir = xPkgInfo.getPackageLocation("be.docarch.accessibility.ooo.AccessibilityCheckerAddOn") + "/images";

        refreshButton.addActionListener(this);
        repairButton.addActionListener(this);
        clearButton.addActionListener(this);
        ignoreButton.addActionListener(this);
        treeControl.addSelectionChangeListener(this);
        clearButton.setLabel("Clear");

        issue2NodeMap = new HashMap<Issue,XMutableTreeNode>();
        check2NodeMap = new HashMap<Check,XMutableTreeNode>();
        node2IssueMap = new HashMap<Integer,Issue>();
        node2CheckMap = new HashMap<Integer,Check>();

        logger.exiting("AccessibilityDialog", "<init>");
        
    }

    private void refresh() throws IllegalArgumentException,
                                  ElementExistException,
                                  RepositoryException,
                                  UnknownPropertyException,
                                  NoSuchElementException,
                                  WrappedTargetException,
                                  PropertyVetoException,
                                  java.text.ParseException,
                                  java.io.IOException,
                                  com.sun.star.uno.Exception {

        logger.entering("AccessibilityDialog", "refresh");

        manager.refresh();

        Issue selectedIssue = manager.selectedIssue();
        Check selectedCheck = manager.selectedCheck();

        buildTreeView();
        if (issue2NodeMap.containsKey(selectedIssue)) {
            if (check2NodeMap.containsKey(selectedCheck)) {
                treeControl.expandNode(check2NodeMap.get(selectedCheck));
            }
            treeControl.select(issue2NodeMap.get(selectedIssue));
            selectIssue(selectedIssue);
        } else if (check2NodeMap.containsKey(selectedCheck)) {
            treeControl.select(check2NodeMap.get(selectedCheck));
            selectCheck(selectedCheck);
        } else {
            selectIssue(null);
        }       
        updateDialogFields();

        logger.exiting("AccessibilityDialog", "refresh");

    }

    private void clear() throws IllegalArgumentException,
                                RepositoryException,
                                UnknownPropertyException,
                                NoSuchElementException,
                                WrappedTargetException,
                                PropertyVetoException,
                                com.sun.star.uno.Exception {

        logger.entering("AccessibilityDialog", "clear");

        manager.clear();

        selectIssue(null);
        buildTreeView();
        updateDialogFields();

        logger.exiting("AccessibilityDialog", "clear");

    }

    public void executeDialog() throws IllegalArgumentException,
                                       NoSuchElementException,
                                       WrappedTargetException,
                                       com.sun.star.uno.Exception {

        logger.entering("AccessibilityDialog", "executeDialog");

        selectIssue(null);
        buildTreeView();
        updateDialogFields();

        dialog.execute();

        XComponent dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        dialogComponent.dispose();

        logger.exiting("AccessibilityDialog", "executeDialog");

//        XController xController = document.xModel.getCurrentController();
//        XFrame parentFrame = xController.getFrame();
//        XWindow parentWindow = parentFrame.getContainerWindow();
//        XWindowPeer parentWindowPeer = (XWindowPeer) UnoRuntime.queryInterface (
//                                        XWindowPeer.class, parentWindow);
//        XToolkit toolkit = parentWindowPeer.getToolkit();
//
//        try {
//
//            Rectangle rectangle = new Rectangle();
//            rectangle.X = 100;
//            rectangle.Y = 100;
//            rectangle.Width = 100;
//            rectangle.Height = 100;
//
//            WindowDescriptor aWindowDescriptor = new WindowDescriptor();
//            aWindowDescriptor.Type = WindowClass.SIMPLE;
//            aWindowDescriptor.WindowServiceName = "dialog";
//            aWindowDescriptor.ParentIndex = -1;
//            aWindowDescriptor.Bounds = rectangle;
//            aWindowDescriptor.Parent = parentWindowPeer;
//            aWindowDescriptor.WindowAttributes = WindowAttribute.CLOSEABLE + WindowAttribute.BORDER + WindowAttribute.SIZEABLE + WindowAttribute.MOVEABLE + WindowAttribute.SHOW;
//
//            XWindowPeer windowPeer = toolkit.createWindow(aWindowDescriptor);
//
//            XWindow window = (XWindow)UnoRuntime.queryInterface(XWindow.class, windowPeer);
//            XFrame frame = (XFrame)UnoRuntime.queryInterface(XFrame.class, document.xMCF.createInstanceWithContext("com.sun.star.frame.Frame", document.xContext));
//            frame.initialize(window);
//
//        } catch (com.sun.star.uno.Exception ex) {
//            logger.log(Level.SEVERE, null, ex);
//        }
    }

    private void selectIssue(Issue issue) {

        Issue selectedIssue = manager.selectedIssue();

        try {   
            
            if (issue == null) {
                removeSelection();
            } else if (selectedIssue == null) {
                focusOn(issue.getElement());
            } else if (!issue.getElement().equals(selectedIssue.getElement())) {
                focusOn(issue.getElement());
            }

        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (NoSuchElementException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        manager.select(issue);
    }

    private void selectCheck(Check check) {

        try {
            removeSelection();
        } catch (IllegalArgumentException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (NoSuchElementException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (WrappedTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }

        manager.select(check);
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

        int index = 0;
        List<Issue> issueList;
        XMutableTreeNode issueNode;
        XMutableTreeNode checkNode;
        XMutableTreeNode rootNode = dataModel.createNode("root", true);
        dataModel.setRoot(rootNode);
        for (Check check : manager.getChecks()) {
            issueList = manager.getIssuesByCheck(check);
            if (issueList.size() == 1 &&
                issueList.get(0).getElement().getType() == Element.Type.DOCUMENT) {
                checkNode = dataModel.createNode(check.getIdentifier(), false);
                checkNode.setDataValue(new Any(Type.LONG, index));
                node2IssueMap.put(index++, issueList.get(0));
                issue2NodeMap.put(issueList.get(0), checkNode);
                check2NodeMap.put(check, checkNode);
            } else {
                checkNode = dataModel.createNode(check.getIdentifier() + " (" + issueList.size() + ")", true);
                checkNode.setDataValue(new Any(Type.LONG, index));
                node2CheckMap.put(index++, check);
                check2NodeMap.put(check, checkNode);
                for (Issue issue : issueList) {
                    issueNode = dataModel.createNode(issue.getName(), false);
                    issueNode.setDataValue(new Any(Type.LONG, index));
                    node2IssueMap.put(index++, issue);
                    issue2NodeMap.put(issue, issueNode);
                    updateIssueNode(issueNode, issue);
                    checkNode.appendChild(issueNode);
                }
            }
            updateCheckNode(checkNode, check);
            rootNode.appendChild(checkNode);
        }

        treeControlProperties.setPropertyValue("DataModel", dataModel);
        treeControlProperties.setPropertyValue("RowHeight", 20);
        treeControlProperties.setPropertyValue("RootDisplayed", false);
        treeControlProperties.setPropertyValue("SelectionType", com.sun.star.view.SelectionType.SINGLE);

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
        statusImageControlProperties.setPropertyValue("ImageURL", "");

        if (selectedCheck != null) {
            nameField.setText(selectedCheck.getIdentifier());
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
                    image = "error.png"; break;
            }

            statusImageControlProperties.setPropertyValue("ImageURL", imageDir + "/" + image);
        }

        updateButtons();
    }

    private void updateButtons() throws UnknownPropertyException,
                                        PropertyVetoException,
                                        WrappedTargetException,
                                        IllegalArgumentException {

        Issue selectedIssue = manager.selectedIssue();
        Check selectedCheck = manager.selectedCheck();

        boolean repair = false;
        boolean ignore = false;

//        if (selectedIssue != null) {
//            ignoreButton.setLabel("Ignore");
//            repairButton.setLabel("Repair");
//            if (!selectedIssue.isRepaired() && !selectedIssue.isIgnored()) {
//                ignore = true;
//                switch (selectedCheck) {
//                    case A_ImageWithoutAlt:
//                    case A_FormulaWithoutAlt:
//                    case A_EmptyTitleField:
//                    case E_DefaultLanguage:
//                    case A_LinkedImage:
//                    case A_NoTableHeading:
//                    case A_JustifiedText:
//                    case A_NoSubtitle:
//                    case A_BreakRows:
//                    case E_EmptyTitle:
//                    case E_EmptyHeading:
//                        repair = true;
//                }
//            }
//        } else {
//            ignoreButton.setLabel("Ignore All");
//            repairButton.setLabel("Repair All");
//            if (selectedCheck != null) {
//                IssueManager.Status status = manager.getCheckStatus(selectedCheck);
//                if (status != IssueManager.Status.REPAIRED &&
//                    status != IssueManager.Status.IGNORED) {
//
//                    ignore = true;
//                    switch (selectedCheck) {
//                        case A_JustifiedText:
//                        case A_NoSubtitle:
//                        case A_BreakRows:
//                            repair = true;
//                    }
//                }
//            }
//        }

        ignoreButtonProperties.setPropertyValue("Enabled", ignore);
        repairButtonProperties.setPropertyValue("Enabled", repair);
    }

    private void updateCheckNode(XMutableTreeNode node, 
                                 Check check)
                          throws com.sun.star.uno.Exception {

        if (node != null) {
            String image = "";
            switch (manager.getCheckStatus(check)) {
                case IGNORED:
                    image = "gray_15x20.png"; break;
                case REPAIRED:
                    image = "green_15x20.png"; break;
                case ALERT:
                    image = "orange_15x20.png"; break;
                case ERROR:
                    image = "red_15x20.png"; break;
            }
            node.setNodeGraphicURL(imageDir + "/" + image);
        }
    }

    private void updateIssueNode(XMutableTreeNode node,
                                 Issue issue) {

        if (node != null) {
            String image = "";
            switch (manager.getIssueStatus(issue)) {
                case IGNORED:
                    image = "gray_15x20.png"; break;
                case REPAIRED:
                    image = "green_15x20.png"; break;
                case ALERT:
                    image = "orange_15x20.png"; break;
                case ERROR:
                    image = "red_15x20.png"; break;
            }
            node.setNodeGraphicURL(imageDir + "/" + image);
        }
    }

    private boolean focusOn(Element element)
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
                        viewCursor.gotoRange(text[0].getAnchor(), false);
                        viewCursor.gotoRange(text[1].getAnchor(), true);
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

    public boolean repair(Issue issue)
                   throws NoSuchElementException,
                          WrappedTargetException,
                          UnknownPropertyException,
                          PropertyVetoException,
                          IndexOutOfBoundsException,
                          IllegalArgumentException {

//        if (issue != null) {
//            PropertyValue[] dispatchProperties;
//            XPropertySet properties;
//
//            switch (issue.getCheckID()) {
//                case A_ImageWithoutAlt:
//                case A_FormulaWithoutAlt:
//                    dispatchProperties = new PropertyValue[]{};
//                    dispatcher.executeDispatch(dispatchProvider, ".uno:ObjectTitleDescription", "", 0, dispatchProperties);
//                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, issue.getElement().getGraphic());
//                    return (AnyConverter.toString(properties.getPropertyValue("Title")).length() +
//                            AnyConverter.toString(properties.getPropertyValue("Description")).length() > 0);
//                case A_EmptyTitleField:
//                    dispatchProperties = new PropertyValue[]{};
//                    dispatcher.executeDispatch(dispatchProvider, ".uno:SetDocumentProperties",  "", 0, dispatchProperties);
//                    return (document.docProperties.getTitle().length() > 0);
//                case A_LinkedImage:
//                    dispatchProperties = new PropertyValue[]{};
//                    dispatcher.executeDispatch(dispatchProvider, ".uno:GraphicDialog",          "", 0, dispatchProperties);
//                    break;
//                case A_NoTableHeading:
//                    dispatchProperties = new PropertyValue[]{};
//                    dispatcher.executeDispatch(dispatchProvider, ".uno:TableDialog",            "", 0, dispatchProperties);
//                    return AnyConverter.toBoolean(((XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
//                                issue.getElement().getTable())).getPropertyValue("RepeatHeadline"));
//                case E_DefaultLanguage:
//                    dispatchProperties = new PropertyValue[1];
//                    dispatchProperties[0] = new PropertyValue();
//                    dispatchProperties[0].Name = "Language";
//                    dispatchProperties[0].Value = "*";
//                    dispatcher.executeDispatch(dispatchProvider, ".uno:LanguageStatus",         "", 0, dispatchProperties);
//                    return !(((Locale)AnyConverter.toObject(
//                               Locale.class, document.docPropertySet.getPropertyValue("CharLocale"))).Language.equals("zxx"));
//                case A_NoSubtitle:
//                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
//                            issue.getElement().getParagraph());
//                    if (document.paragraphStyles.getByName("Subtitle") != null) {
//                        properties.setPropertyValue("ParaStyleName", "Subtitle");
//                        return true;
//                    }
//                    break;
//                case A_JustifiedText:
//                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
//                            issue.getElement().getParagraph());
//                    properties.setPropertyValue("ParaAdjust", ParagraphAdjust.LEFT_value);
//                    properties = (XPropertySet)UnoRuntime.queryInterface(
//                                   XPropertySet.class, document.paragraphStyles.getByName(
//                                   AnyConverter.toString(properties.getPropertyValue("ParaStyleName"))));
//                    int paraAdjust = AnyConverter.toInt(properties.getPropertyValue("ParaAdjust"));
//                    if (paraAdjust == ParagraphAdjust.BLOCK_value ||
//                        paraAdjust == ParagraphAdjust.STRETCH_value) {
//                        properties.setPropertyValue("ParaAdjust", ParagraphAdjust.LEFT_value);
//                    }
//                    return true;
//                case E_EmptyTitle:
//                case E_EmptyHeading:
//                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
//                            issue.getElement().getParagraph());
//                    properties.setPropertyValue("ParaStyleName", "Standard");
//                    return true;
//                case A_BreakRows:
//                    XTableRows tableRows = issue.getElement().getTable().getRows();
//                    XPropertySet rowProperties = null;
//                    for (int i=0; i<tableRows.getCount(); i++) {
//                        rowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, tableRows.getByIndex(i));
//                        if (rowProperties.getPropertySetInfo().hasPropertyByName("IsSplitAllowed")) {
//                            rowProperties.setPropertyValue("IsSplitAllowed", false);
//                        }
//                    }
//                    return true;
//            }
//        }
        return false;
    }

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
                    updateIssueNode(issue2NodeMap.get(selectedIssue), selectedIssue);
                    //updateCheckNode(check2NodeMap.get(selectedCheck), selectedCheck);
                    updateDialogFields();
                } else if (selectedCheck != null) {
                    for (Issue issue : manager.getIssuesByCheck(selectedCheck)) {
                        issue.ignored(true);
                        updateIssueNode(issue2NodeMap.get(issue), issue);
                    }
                    //updateCheckNode(check2NodeMap.get(selectedCheck), selectedCheck);
                    updateDialogFields();
                }

            } else if (source.equals(repairButton)) {

                if (selectedIssue != null) {
                    if (repair(selectedIssue)) {
                        selectedIssue.repaired(true);
                        updateIssueNode(issue2NodeMap.get(selectedIssue), selectedIssue);
                        //updateCheckNode(check2NodeMap.get(selectedCheck), selectedCheck);
                        updateDialogFields();
                    }
                } else if (selectedCheck != null) {
                    boolean repaired = false;
                    for (Issue issue : manager.getIssuesByCheck(selectedCheck)) {
                        if (!issue.repaired() && !issue.ignored()) {
                            if (repair(issue)) {
                                repaired = true;
                                issue.repaired(true);
                                updateIssueNode(issue2NodeMap.get(issue), issue);
                            }
                        }
                    }
                    if (repaired) {
                        //updateCheckNode(check2NodeMap.get(selectedCheck), selectedCheck);
                        updateDialogFields();
                    }
                }
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

    public void itemStateChanged(ItemEvent event) {}

    public void selectionChanged(EventObject event) {

        try {

            Object o = treeControl.getSelection();
            if (o != null && !AnyConverter.isVoid(o)) {
                XMutableTreeNode node = (XMutableTreeNode)AnyConverter.toObject(XMutableTreeNode.class, o);
                Object d = node.getDataValue();
                if (d != null && !AnyConverter.isVoid(d)) {
                    
                    int index = AnyConverter.toInt(d);
                    
                    if (node2IssueMap.containsKey(index)) {
                        selectIssue(node2IssueMap.get(index));
                    } else if (node2CheckMap.containsKey(index)) {
                        selectCheck(node2CheckMap.get(index));
                    } else {
                        selectIssue(null);
                    }
                }
                updateDialogFields();
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
    
    public void disposing(EventObject event) {}

//    private void showComments() throws IllegalArgumentException,
//                                       UnknownPropertyException,
//                                       PropertyVetoException,
//                                       WrappedTargetException,
//                                       com.sun.star.uno.Exception {
//
//        logger.entering("IssueManager", "showComments");
//
//        XTextRange textRange = null;
//        XTextCursor cursor = null;
//        Comment comment = null;
//        Check.ID check = null;
//        Element element = null;
//
//        for (Issue issue : allIssues) {
//            if (issue.isValid()) {
//                if ((element = issue.getElement()).exists()) {
//                    switch (element.getType()) {
//                        case PARAGRAPH:
//                            textRange = element.getParagraph().getAnchor();
//                        case SPAN:
//                            textRange = element.getSpan()[0].getAnchor();
//                        default:
//                    }
//                    if (textRange != null) {
//                        cursor = textRange.getText().createTextCursorByRange(textRange);
//                        comment = new Comment(document.xMSF);
//                        if ((check = issue.getCheckID()) != null) {
//                            comment.setContent(check.name());
//                        }
//                        comment.insertCommentAtCursor(cursor);
//                    }
//                }
//            }
//        }
//
//        logger.exiting("IssueManager", "showComments");
//
//    }

}
