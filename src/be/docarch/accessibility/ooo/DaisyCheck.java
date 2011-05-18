package be.docarch.accessibility.ooo;

import java.util.Locale;
import java.util.ResourceBundle;

import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class DaisyCheck extends Check {

    private static Locale locale = Locale.getDefault();
    private static ResourceBundle bundle = ResourceBundle.getBundle("be/docarch/accessibility/l10n/Bundle", locale);

    public static enum ID {

        E_UnsupportedImageFormat,
        A_EmptyTitleField

    }

    private ID identifier;

    public DaisyCheck(ID identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier.name();
    }

    public Status getStatus() {

        switch (identifier) {
            case E_UnsupportedImageFormat:
                return Status.ERROR;
            case A_EmptyTitleField:
                return Status.ALERT;
            default:
                return null;
        }
    }
    
    public Category getCategory() {

        switch (identifier) {
            case A_EmptyTitleField:
                return Category.TITLES;
            case E_UnsupportedImageFormat:
                return Category.GRAPHICS;
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

    public RepairMode getRepairMode() {

        switch (identifier) {
            case A_EmptyTitleField:
                return RepairMode.SEMI_AUTOMATED;
            case E_UnsupportedImageFormat:
            default:
                return RepairMode.MANUAL;
        }
    }
}
