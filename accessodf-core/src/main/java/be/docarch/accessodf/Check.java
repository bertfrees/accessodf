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

package be.docarch.accessodf;

import java.util.Locale;

/**
 *
 * @author Bert Frees
 */
public abstract class Check {

    public static enum Status { ALERT,
                                ERROR };

    public abstract String getIdentifier();

    public abstract Status getStatus();
    
    public abstract String getName(Locale locale);

    public abstract String getDescription(Locale locale);

    public abstract String getSuggestion(Locale locale);

    @Override
    public String toString() {
        return getIdentifier();
    }

    @Override
    public int hashCode() {
        return getIdentifier().hashCode();
   }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof Check)) { return false; }
        final Check that = (Check)obj;
        return (this.getIdentifier().equals(that.getIdentifier()));
    }
}
