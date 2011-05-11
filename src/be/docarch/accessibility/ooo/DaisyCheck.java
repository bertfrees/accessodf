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
    private static ResourceBundle names = ResourceBundle.getBundle("be/docarch/accessibility/l10n/names", locale);
    private static ResourceBundle descriptions = ResourceBundle.getBundle("be/docarch/accessibility/l10n/descriptions", locale);
    private static ResourceBundle suggestions = ResourceBundle.getBundle("be/docarch/accessibility/l10n/suggestions", locale);

    public static enum ID {

        E_UnsupportedImageFormat,        
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
            default:
                return null;
        }
    }
    
    public Category getCategory() {

        switch (identifier) {
            case E_UnsupportedImageFormat:
                return Category.GRAPHICS;
            default:
                return Category.OTHER;
        }
    }
    
    public String getName() {

        if (identifier == null) {
            return null;
        } else if (names.containsKey(identifier.name())) {
            return names.getString(identifier.name());
        } else {
            return identifier.name();
        }
    }

    public String getDescription() {

        if (identifier == null) {
            return null;
        } else if (descriptions.containsKey(identifier.name())) {
            return descriptions.getString(identifier.name());
        } else {
            return identifier.name();
        }
    }

    public String getSuggestion() {

        if (identifier == null) {
            return null;
        } else if (suggestions.containsKey(identifier.name())) {
            return suggestions.getString(identifier.name());
        } else {
            return identifier.name();
        }
    }

    public RepairMode getRepairMode() {

        switch (identifier) {
            default:
                return RepairMode.MANUAL;
        }
    }
}
