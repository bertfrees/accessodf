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

import java.util.ArrayList;
import java.util.TreeMap;

/**
 *
 * @author Bert Frees
 */
public class FilterSorter implements java.util.Comparator<Issue> {

    public static enum Property { NATURAL, CATEGORY, CHECKID, NAME }
    public static final Property NATURAL = Property.NATURAL;
    public static final Property CHECKID = Property.CHECKID;
    public static final Property NAME = Property.NAME;

    private TreeMap<Property, Boolean> upDown;
    private ArrayList<Property> priority;


    public FilterSorter() {

        upDown = new TreeMap<Property, Boolean>();
        upDown.put(NATURAL, true);
        upDown.put(NAME, true);
        upDown.put(CHECKID, true);
        priority = new ArrayList<Property>();
        priority.add(NATURAL);
        priority.add(NAME);
        priority.add(CHECKID);

    }

    public void setOrder(Property property,
                         boolean upDown) {

        if (this.upDown.containsKey(property)) {
            this.upDown.put(property, upDown);
        }
    }

    public void setOrderPriority(Property property,
                                 boolean highestLowest) {

        if (priority.contains(property)) {
            priority.remove(property);
            if (highestLowest) {
                priority.add(0, property);
            } else {
                priority.add(property);
            }
        }
    }

    public int compare(Issue entry1,
                       Issue entry2) {

        if (entry1 == null && entry2 == null) {
            return 0;
        } else if (entry1 == null) {
            return -1;
        } else if (entry2 == null) {
            return 1;
        }

        Property property;
        int compare = 0;

        for (int i=0; i<priority.size(); i++) {
            property = priority.get(i);
            switch (property) {
                case NAME:
                    compare = (entry1.getName().compareTo(entry2.getName()));
                    break;
                case CHECKID:
                    compare = (entry1.getCheck().getIdentifier().compareTo(entry2.getCheck().getIdentifier()));
                    break;
                case NATURAL:
                default:
                    compare = 0;
            }
            if (compare != 0) {
                if (upDown.get(property)) {
                    return compare;
                } else {
                    return -compare;
                }
            }
        }

        return compare;
    }

    public boolean accept(Issue entry) {
        return (entry != null);
    }
}
