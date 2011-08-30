package be.docarch.accessibility.ooo;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class GeneralCheck extends Check {

    private static final String L10N_BUNDLE = "be/docarch/accessibility/ooo/l10n/Bundle";

    public static enum ID {

        E_ManyTitles,
        E_EmptyTitle,
        E_EmptyHeading,
        E_HeadingSkip,
        E_HeadingInFrame,
        E_NoLanguage,
        E_NoDefaultLanguage,
        E_NoHyperlinkLanguage,
        E_ImageAnchorFloat,
        A_NoTitle,
        A_NoHeadings,
        A_NoSubtitle,
        A_AlternateLevel,
        A_FakeUnorderedList,        // ***
        A_FakeOrderedList,          // ***
        A_FakeTable,
        A_FakeText,
        A_FakeQuote,                // ***  paragraaf met marges aan beide kanten
        A_FakeHeading,
        A_LinkedImage,
        A_ImageWithoutAlt,
        A_FormulaWithoutAlt,
        A_ObjectWithoutAlt,
        A_MergedCells,              // niet 100% betrouwbaar => nieuwe checker mbv xpath?
        A_NestedTable,
        A_NoTableHeading,
        A_BreakRows,
        A_BigTable,
        A_CaptionBelowBigTable,
        A_UnidentifiedLanguage,
        A_LowContrast,
        A_JustifiedText,
        A_SmallText,
        A_AllCaps,                  // voorlopig genegeerd
        A_LongUnderline,
        A_LongItalic,
        A_HasForms,
        A_NoHyperlinkText,
        A_FakeLine,
        A_FlashText
        
    }

    private ID identifier;

    public GeneralCheck(ID identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier.name();
    }

    public Status getStatus() {

        if (identifier.name().startsWith("E_")) {
            return Status.ERROR;
        } else if (identifier == ID.A_ImageWithoutAlt) {
            return Status.ERROR;
        } else if (identifier == ID.A_LowContrast) {
            return Status.ERROR;
        } else if (identifier.name().startsWith("A_")) {
            return Status.ALERT;
        } else {
            return null;
        }
    }
    
    public String getName(Locale locale) {

        if (identifier == null) {
            return null;
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(L10N_BUNDLE, locale);
            return bundle.getString("name_" + identifier.name());
        } catch (MissingResourceException e) {
            return identifier.name();
        }
    }

    public String getDescription(Locale locale) {

        if (identifier == null) {
            return null;
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(L10N_BUNDLE, locale);
            return bundle.getString("description_" + identifier.name());
        } catch (MissingResourceException e) {
            return "";
        }
    }

    public String getSuggestion(Locale locale) {

        if (identifier == null) {
            return null;
        }

        try {
            ResourceBundle bundle = ResourceBundle.getBundle(L10N_BUNDLE, locale);
            return bundle.getString("suggestion_" + identifier.name());
        } catch (MissingResourceException e) {
            return "";
        }
    }
}
