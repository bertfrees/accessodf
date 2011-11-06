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

import java.util.Map;
import java.util.TreeMap;

import java.util.Collection;

public class CheckProvider implements Provider<Check> {

    private Map<String,Check> checks = new TreeMap<String,Check>();

    public CheckProvider(Provider<Checker> checkers) {

        for (Checker checker : checkers.list()) {
            for (Check check : checker.list()) {
                checks.put(check.getIdentifier(), check);
            }
        }
    }

    public Collection<Check> list() {
        return checks.values();
    }

    public Check get(String identifier) {
        return checks.get(identifier);
    }
}
