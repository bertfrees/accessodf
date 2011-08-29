package be.docarch.accessibility.ooo;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class DaisyCheck extends Check {

    private static final String L10N_BUNDLE = "be/docarch/accessibility/ooo/l10n/Bundle";

    public static enum ID {

        E_UnsupportedImageFormat,
        A_EmptyTitleField,
        A_BodyMatterStartSectionNested,
        A_RearMatterStartSectionNested

    }

    private ID identifier;

    public DaisyCheck(ID identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier.name();
    }

    public Status getStatus() {

        if (identifier.name().startsWith("E_")) {
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
            switch (identifier) {
                case A_BodyMatterStartSectionNested:
                    return "The section \"BodyMatterStart\" is placed inside another section. odt2daisy will not recognize it.";
                case A_RearMatterStartSectionNested:
                    return "The section \"RearMatterStart\" is placed inside another section. odt2daisy will not recognize it.";
                default:
                    return "";
            }
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
