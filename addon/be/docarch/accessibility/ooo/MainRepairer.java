package be.docarch.accessibility.ooo;

import be.docarch.accessibility.Repairer;
import java.util.HashSet;
import java.util.Collection;

import com.sun.star.beans.PropertyValue;
import com.sun.star.beans.XPropertySet;
import com.sun.star.frame.XDispatchHelper;
import com.sun.star.frame.XDispatchProvider;
import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.AnyConverter;
import com.sun.star.lang.Locale;
import com.sun.star.style.ParagraphAdjust;
import com.sun.star.style.XStyle;
import com.sun.star.table.XTableRows;

import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.accessibility.Check;
import be.docarch.accessibility.Issue;
import be.docarch.accessibility.Element;

/**
 *
 * @author Bert Frees
 */
public class MainRepairer implements Repairer {

    private Collection<Check> supportedChecks;
    private Document document;
    private static XDispatchHelper dispatcher;
    private static XDispatchProvider dispatchProvider;

    public MainRepairer(Document document)
                 throws com.sun.star.uno.Exception {

        this.document = document;
        XComponentContext xContext = document.xContext;
        dispatcher = (XDispatchHelper)UnoRuntime.queryInterface(
                         XDispatchHelper.class, document.xMCF.createInstanceWithContext(
                             "com.sun.star.frame.DispatchHelper", xContext));
        dispatchProvider = (XDispatchProvider)UnoRuntime.queryInterface(
                                XDispatchProvider.class, document.xModel.getCurrentController().getFrame());

        supportedChecks = new HashSet<Check>();
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_ImageWithoutAlt));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_FormulaWithoutAlt));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_ObjectWithoutAlt));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.E_NoDefaultLanguage));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_LinkedImage));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.E_NoHyperlinkLanguage));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_NoHyperlinkText));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_NoTableHeading));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_JustifiedText));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_NoSubtitle));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_BreakRows));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.E_EmptyTitle));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.E_EmptyHeading));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_FlashText));
        supportedChecks.add(new GeneralCheck(GeneralCheck.ID.A_SmallText));
        supportedChecks.add(new DaisyCheck(DaisyCheck.ID.A_EmptyTitleField));
    }

    public String getIdentifier() {
        return "be.docarch.accessibility.ooo.InternalRepairer";
    }

    public boolean repair(Issue issue) {

        try {

            PropertyValue[] dispatchProperties;
            XPropertySet properties;
            Check check = issue.getCheck();
            Element element = issue.getElement();

            if (check != null &&
                supports(check)) {

                String id = check.getIdentifier();
                
                if (id.equals(GeneralCheck.ID.A_ImageWithoutAlt.name()) ||
                    id.equals(GeneralCheck.ID.A_FormulaWithoutAlt.name()) ||
                    id.equals(GeneralCheck.ID.A_ObjectWithoutAlt.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:ObjectTitleDescription", "", 0, dispatchProperties);

                    if (element == null) { return false; }
                    try {
                        properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, ((DrawObject)element).getXNamed());
                        return (AnyConverter.toString(properties.getPropertyValue("Title")).length() > 0 &&
                                AnyConverter.toString(properties.getPropertyValue("Description")).length() > 0);
                    } catch (ClassCastException e) {
                    } catch (Exception e) {
                    }

                } else if (id.equals(DaisyCheck.ID.A_EmptyTitleField.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:SetDocumentProperties", "", 0, dispatchProperties);
                    return (document.docProperties.getTitle().length() > 0);

                } else if (id.equals(GeneralCheck.ID.A_LinkedImage.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:GraphicDialog", "", 0, dispatchProperties);

                    // TODO: return value

                } else if (id.equals(GeneralCheck.ID.A_NoTableHeading.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:TableDialog", "", 0, dispatchProperties);

                    if (element == null) { return false; }
                    try {
                        return AnyConverter.toBoolean(((XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                                    ((Table)element).getXTextTable())).getPropertyValue("RepeatHeadline"));
                    } catch (ClassCastException e) {
                    } catch (Exception e) {
                    }

                } else if (id.equals(GeneralCheck.ID.A_NoHyperlinkText.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:EditHyperlink", "", 0, dispatchProperties);

                    // TODO: return value

                } else if (id.equals(GeneralCheck.ID.E_NoHyperlinkLanguage.name())) {

                    Locale docLocale = (Locale)AnyConverter.toObject(Locale.class, document.docPropertySet.getPropertyValue("CharLocale"));

                    if (!docLocale.Language.equals("zxx")) {
                        try {
                            XStyle hyperlinkStyle = (XStyle)UnoRuntime.queryInterface(
                                                     XStyle.class, document.characterStyles.getByName("Internet Link"));
                            properties =(XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, hyperlinkStyle);
                            properties.setPropertyValue("CharLocale", docLocale);
                        } catch (NoSuchElementException e) {
                        } catch (PropertyVetoException e) {
                        }
                    }

                    // TODO: return value

                } else if (id.equals(GeneralCheck.ID.E_NoDefaultLanguage.name())) {

                    dispatchProperties = new PropertyValue[1];
                    dispatchProperties[0] = new PropertyValue();
                    dispatchProperties[0].Name = "Language";
                    dispatchProperties[0].Value = "*";

                  //dispatcher.executeDispatch(dispatchProvider, ".uno:OptionsTreeDialog", "", 0, dispatchProperties);
                    dispatcher.executeDispatch(dispatchProvider, ".uno:LanguageStatus", "", 0, dispatchProperties);

                    return !(((Locale)AnyConverter.toObject(
                                 Locale.class, document.docPropertySet.getPropertyValue("CharLocale"))).Language.equals("zxx"));                    
                    
                    // TODO: return value NIET JUIST ! docPropertySet is op dit moment nog niet aangepast

                } else if (id.equals(GeneralCheck.ID.A_NoSubtitle.name())) {

                    if (element == null) { return false; }
                    try {
                        properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, ((Paragraph)element).getXTextContent());
                        try {
                            document.paragraphStyles.getByName("Subtitle");
                            properties.setPropertyValue("ParaStyleName", "Subtitle");
                            return true;
                        } catch (NoSuchElementException e) {
                        }
                    } catch (ClassCastException e) {
                    } catch (Exception e) {
                    }

                } else if (id.equals(GeneralCheck.ID.A_JustifiedText.name())) {

                    if (element == null) { return false; }
                    try {
                        properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, ((Paragraph)element).getXTextContent());
                        properties.setPropertyValue("ParaAdjust", ParagraphAdjust.LEFT_value);
                        properties = (XPropertySet)UnoRuntime.queryInterface(
                                       XPropertySet.class, document.paragraphStyles.getByName(
                                       AnyConverter.toString(properties.getPropertyValue("ParaStyleName"))));
                        int paraAdjust = AnyConverter.toInt(properties.getPropertyValue("ParaAdjust"));
                        if (paraAdjust == ParagraphAdjust.BLOCK_value ||
                            paraAdjust == ParagraphAdjust.STRETCH_value) {
                            properties.setPropertyValue("ParaAdjust", ParagraphAdjust.LEFT_value);
                        }
                        return true;
                    } catch (ClassCastException e) {
                    } catch (Exception e) {
                    }

                } else if (id.equals(GeneralCheck.ID.E_EmptyTitle.name()) ||
                           id.equals(GeneralCheck.ID.E_EmptyHeading.name())) {

                    if (element == null) { return false; }
                    try {
                        properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, ((Paragraph)element).getXTextContent());
                        properties.setPropertyValue("ParaStyleName", "Standard");
                        return true;
                    } catch (ClassCastException e) {
                    } catch (Exception e) {
                    }

                } else if (id.equals(GeneralCheck.ID.A_FlashText.name())) {

                    if (element == null) { return false; }
                    try {
                        properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, ((Span)element).getXTextCursor());
                        properties.setPropertyValue("CharFlash", false);
                        return true;
                    } catch (ClassCastException e) {
                    } catch (Exception e) {
                    }

                } else if (id.equals(GeneralCheck.ID.A_SmallText.name())) {

                    if (element == null) { return false; }
                    try {
                        properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, ((Span)element).getXTextCursor());
                        properties.setPropertyValue("CharHeight", 10);
                        return true;
                    } catch (ClassCastException e) {
                    } catch (Exception e) {
                    }

                } else if (id.equals(GeneralCheck.ID.A_BreakRows.name())) {

                    if (element == null) { return false; }
                    try {
                        XTableRows tableRows = ((Table)element).getXTextTable().getRows();
                        XPropertySet rowProperties = null;
                        for (int i=0; i<tableRows.getCount(); i++) {
                            rowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, tableRows.getByIndex(i));
                            if (rowProperties.getPropertySetInfo().hasPropertyByName("IsSplitAllowed")) {
                                rowProperties.setPropertyValue("IsSplitAllowed", false);
                            }
                        }
                        return true;
                    } catch (ClassCastException e) {
                    } catch (Exception e) {
                    }
                }
            }
        } catch (WrappedTargetException e) {
        } catch (UnknownPropertyException e) {
        } catch (IllegalArgumentException e) {
        }

        return false;
    }

    public RepairMode getRepairMode(Check check)
                             throws java.lang.IllegalArgumentException {

        if (supports(check)) {

            String id = check.getIdentifier();

            if (id.equals(GeneralCheck.ID.A_ImageWithoutAlt.name()) ||
                id.equals(GeneralCheck.ID.A_FormulaWithoutAlt.name()) ||
                id.equals(GeneralCheck.ID.A_ObjectWithoutAlt.name()) ||
                id.equals(GeneralCheck.ID.E_NoDefaultLanguage.name()) ||
                id.equals(GeneralCheck.ID.A_NoTableHeading.name()) ||
                id.equals(GeneralCheck.ID.A_LinkedImage.name()) ||
                id.equals(GeneralCheck.ID.A_NoHyperlinkText.name()) ||
                id.equals(DaisyCheck.ID.A_EmptyTitleField.name())) {

                return RepairMode.SEMI_AUTOMATED;

            } else if (id.equals(GeneralCheck.ID.A_JustifiedText.name()) ||
                id.equals(GeneralCheck.ID.A_NoSubtitle.name()) ||
                id.equals(GeneralCheck.ID.A_BreakRows.name()) ||
                id.equals(GeneralCheck.ID.E_EmptyTitle.name()) ||
                id.equals(GeneralCheck.ID.E_NoHyperlinkLanguage.name()) ||
                id.equals(GeneralCheck.ID.E_EmptyHeading.name()) ||
                id.equals(GeneralCheck.ID.A_FlashText.name()) ||
                id.equals(GeneralCheck.ID.A_SmallText.name())) {

                return RepairMode.AUTO;

            }
        }

        throw new java.lang.IllegalArgumentException("Check is not supported");
    }

    public boolean supports(Check check) {
        return supportedChecks.contains(check);
    }
}
