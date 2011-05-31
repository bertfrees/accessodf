package be.docarch.accessibility.ooo;

import java.util.Locale;
import java.util.ResourceBundle;

import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class GeneralCheck extends Check {

    private static Locale locale = Locale.getDefault();
    private static ResourceBundle bundle = ResourceBundle.getBundle("be/docarch/accessibility/l10n/Bundle", locale);

    public static enum ID {
                                    // Hele document    Paragraaf      Stukje tekst      Tabel      Image/Object

        E_ManyTitles,
        E_EmptyTitle,
        E_EmptyHeading,
        E_HeadingSkip,
        E_HeadingInFrame,
        E_HeadingInSection,         // *** (?)
        E_DefaultLanguage,
        E_ImageAnchorFloat,         // ***
        A_NoHeadings,
        A_NoSubtitle,
        A_AlternateLevel,
        A_EmptyList,                // ***
        A_FakeUnorderedList,        // ***
        A_FakeOrderedList,          // ***
        A_FakeTable,                // ***
        A_FakeText,                 // ***
        A_FakeQuote,                // ***
        A_FakeHeading,
        A_LinkedImage,
        A_ImageWithoutAlt,
        A_FormulaWithoutAlt,
        A_ObjectWithoutAlt,
        A_MergedCells,
        A_NestedTable,
        A_NoTableHeading,
        A_BreakRows,
        A_BigTable,
        A_CaptionBelowBigTable,
        A_UnidentifiedLanguage,     // ***
        A_LowContrast,              // ***
        A_JustifiedText,
        A_SmallText,
        A_AllCaps,                  // voorlopig genegeerd
        A_LongUnderline,
        A_LongItalic,
        A_NoTitle
        
    }

    private ID identifier;

    public GeneralCheck(ID identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier.name();
    }

    public Status getStatus() {

        switch (identifier) {
            case E_ManyTitles:
            case E_EmptyTitle:
            case E_EmptyHeading:
            case E_HeadingSkip:
            case E_HeadingInFrame:
            case E_HeadingInSection:
            case E_DefaultLanguage:
            case E_ImageAnchorFloat:
                return Status.ERROR;
            
            case A_NoTitle:
            case A_NoHeadings:
            case A_NoSubtitle:
            case A_AlternateLevel:
            case A_FakeUnorderedList:
            case A_FakeOrderedList:
            case A_EmptyList:
            case A_LinkedImage:
            case A_ImageWithoutAlt:
            case A_FormulaWithoutAlt:
            case A_ObjectWithoutAlt:
            case A_FakeTable:
            case A_MergedCells:
            case A_NestedTable:
            case A_NoTableHeading:
            case A_BreakRows:
            case A_BigTable:
            case A_CaptionBelowBigTable:
            case A_UnidentifiedLanguage:
            case A_LowContrast:
            case A_FakeText:
            case A_JustifiedText:
            case A_SmallText:
            case A_AllCaps:
            case A_LongUnderline:
            case A_LongItalic:
            case A_FakeHeading:
            case A_FakeQuote:
                return Status.ALERT;

            default:
                return null;
        }
    }
    
    public Category getCategory() {

        switch (identifier) {
            case E_ManyTitles:
            case E_EmptyTitle:
            case A_NoTitle:
            case A_NoSubtitle:
                return Category.TITLES;
            case E_EmptyHeading:
            case E_HeadingSkip:
            case E_HeadingInFrame:
            case E_HeadingInSection:
            case A_NoHeadings:
            case A_AlternateLevel:
            case A_FakeHeading:
                return Category.HEADINGS;
            case E_DefaultLanguage:
            case A_UnidentifiedLanguage:
                return Category.LANGUAGE;
            case A_LinkedImage:
            case A_ImageWithoutAlt:
            case E_ImageAnchorFloat:
                return Category.GRAPHICS;
            case A_FakeUnorderedList:
            case A_FakeOrderedList:
            case A_EmptyList:
                return Category.LISTS;
            case A_FakeTable:
            case A_MergedCells:
            case A_NestedTable:
            case A_NoTableHeading:
            case A_BreakRows:
            case A_BigTable:
            case A_CaptionBelowBigTable:
                return Category.TABLES;
            case A_LowContrast:
                return Category.COLOR;
            case A_FakeText:
            case A_SmallText:
            case A_AllCaps:
            case A_LongUnderline:
            case A_LongItalic:
                return Category.FONT;
            case A_FormulaWithoutAlt:
            case A_JustifiedText:
            case A_ObjectWithoutAlt:
            case A_FakeQuote:
            default:
                return Category.OTHER;
        }
    }
    
    public String getName() {

        if (identifier == null) {
            return null;
        } else if (bundle.containsKey("name_" + identifier.name())) {
            return bundle.getString("name_" + identifier.name());
        } else {
            return identifier.name();
        }
    }

    public String getDescription() {

        if (identifier == null) {
            return null;
        } else if (bundle.containsKey("description_" + identifier.name())) {
            return bundle.getString("description_" + identifier.name());
        } else {
            return identifier.name();
        }
    }

    public String getSuggestion() {
        
        if (identifier == null) {
            return null;
        } else if (bundle.containsKey("suggestion_" + identifier.name())) {
            return bundle.getString("suggestion_" + identifier.name());
        } else {
            return identifier.name();
        }
    }
}
