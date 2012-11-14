/**
 *  AccessODF - Accessibility checker for OpenOffice.org and LibreOffice Writer.
 *
 *  Copyright (c) 2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.accessodf.ooo;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.MissingResourceException;

import be.docarch.accessodf.Check;

/**
 *
 * @author Bert Frees
 */
public class DaisyCheck extends Check {

    private static final String L10N_BUNDLE = "be/docarch/accessodf/ooo/l10n/Bundle";

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
