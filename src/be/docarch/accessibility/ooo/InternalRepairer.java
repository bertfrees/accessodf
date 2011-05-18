package be.docarch.accessibility.ooo;

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
import com.sun.star.table.XTableRows;

import com.sun.star.container.NoSuchElementException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.lang.IndexOutOfBoundsException;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class InternalRepairer implements Repairer {

    private Collection<Check> checks;
    private Document document;
    private static XDispatchHelper dispatcher;
    private static XDispatchProvider dispatchProvider;

    public InternalRepairer(Document document)
                     throws com.sun.star.uno.Exception {

        this.document = document;
        XComponentContext xContext = document.xContext;
        dispatcher = (XDispatchHelper)UnoRuntime.queryInterface(
                         XDispatchHelper.class, document.xMCF.createInstanceWithContext(
                             "com.sun.star.frame.DispatchHelper", xContext));
        dispatchProvider = (XDispatchProvider)UnoRuntime.queryInterface(
                                XDispatchProvider.class, document.xModel.getCurrentController().getFrame());

        checks = new HashSet<Check>();
        checks.add(new GeneralCheck(GeneralCheck.ID.A_ImageWithoutAlt));
        checks.add(new DaisyCheck(DaisyCheck.ID.A_EmptyTitleField));
    }

    public String getIdentifier() {
        return "be.docarch.accessibility.ooo.InternalRepairer";
    }

    public Collection<Check> getChecks() {
        return checks;
    }

    public boolean repair(Issue issue) {

        boolean succes = false;

        try {

            PropertyValue[] dispatchProperties;
            XPropertySet properties;
            Check check = issue.getCheck();
            Element element = issue.getElement();

            if (check != null &&
                element != null &&
                check.getClass() == GeneralCheck.class &&
               (check.getRepairMode() == Check.RepairMode.AUTO ||
                check.getRepairMode() == Check.RepairMode.SEMI_AUTOMATED)) {

                String id = check.getIdentifier();
                
                if (id.equals(GeneralCheck.ID.A_ImageWithoutAlt.name()) ||
                    id.equals(GeneralCheck.ID.A_FormulaWithoutAlt.name()) ||
                    id.equals(GeneralCheck.ID.A_ObjectWithoutAlt.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:ObjectTitleDescription", "", 0, dispatchProperties);
                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, element.getObject());
                    succes = (AnyConverter.toString(properties.getPropertyValue("Title")).length() +
                              AnyConverter.toString(properties.getPropertyValue("Description")).length() > 0);

                } else if (id.equals(DaisyCheck.ID.A_EmptyTitleField.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:SetDocumentProperties",  "", 0, dispatchProperties);
                    succes = (document.docProperties.getTitle().length() > 0);

                } else if (id.equals(GeneralCheck.ID.A_LinkedImage.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:GraphicDialog",          "", 0, dispatchProperties);

                } else if (id.equals(GeneralCheck.ID.A_NoTableHeading.name())) {

                    dispatchProperties = new PropertyValue[]{};
                    dispatcher.executeDispatch(dispatchProvider, ".uno:TableDialog",            "", 0, dispatchProperties);
                    succes = AnyConverter.toBoolean(((XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                                element.getTable())).getPropertyValue("RepeatHeadline"));

                } else if (id.equals(GeneralCheck.ID.E_DefaultLanguage.name())) {

                        dispatchProperties = new PropertyValue[1];
                        dispatchProperties[0] = new PropertyValue();
                        dispatchProperties[0].Name = "Language";
                        dispatchProperties[0].Value = "*";
                        dispatcher.executeDispatch(dispatchProvider, ".uno:LanguageStatus",         "", 0, dispatchProperties);
                        succes = !(((Locale)AnyConverter.toObject(
                                     Locale.class, document.docPropertySet.getPropertyValue("CharLocale"))).Language.equals("zxx"));

                } else if (id.equals(GeneralCheck.ID.A_NoSubtitle.name())) {

                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                            element.getParagraph());
                    if (document.paragraphStyles.getByName("Subtitle") != null) {
                        properties.setPropertyValue("ParaStyleName", "Subtitle");
                        succes = true;
                    }

                } else if (id.equals(GeneralCheck.ID.A_JustifiedText.name())) {

                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                            element.getParagraph());
                    properties.setPropertyValue("ParaAdjust", ParagraphAdjust.LEFT_value);
                    properties = (XPropertySet)UnoRuntime.queryInterface(
                                   XPropertySet.class, document.paragraphStyles.getByName(
                                   AnyConverter.toString(properties.getPropertyValue("ParaStyleName"))));
                    int paraAdjust = AnyConverter.toInt(properties.getPropertyValue("ParaAdjust"));
                    if (paraAdjust == ParagraphAdjust.BLOCK_value ||
                        paraAdjust == ParagraphAdjust.STRETCH_value) {
                        properties.setPropertyValue("ParaAdjust", ParagraphAdjust.LEFT_value);
                    }
                    succes = true;

                } else if (id.equals(GeneralCheck.ID.E_EmptyTitle.name()) ||
                           id.equals(GeneralCheck.ID.E_EmptyHeading.name())) {

                    properties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                            element.getParagraph());
                    properties.setPropertyValue("ParaStyleName", "Standard");
                    succes = true;

                } else if (id.equals(GeneralCheck.ID.A_BreakRows.name())) {

                    XTableRows tableRows = element.getTable().getRows();
                    XPropertySet rowProperties = null;
                    for (int i=0; i<tableRows.getCount(); i++) {
                        rowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, tableRows.getByIndex(i));
                        if (rowProperties.getPropertySetInfo().hasPropertyByName("IsSplitAllowed")) {
                            rowProperties.setPropertyValue("IsSplitAllowed", false);
                        }
                    }
                    succes = true;
                }
            }
        } catch (NoSuchElementException e) {
        } catch (WrappedTargetException e) {
        } catch (UnknownPropertyException e) {
        } catch (PropertyVetoException e) {
        } catch (IndexOutOfBoundsException e) {
        } catch (IllegalArgumentException e) {
        }

        return succes;
    }
}
