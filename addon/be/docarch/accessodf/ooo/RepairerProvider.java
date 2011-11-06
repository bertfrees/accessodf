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

import be.docarch.accessodf.Provider;
import be.docarch.accessodf.Repairer;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import javax.imageio.spi.ServiceRegistry;

/**
 *
 * @author Bert Frees
 */
public class RepairerProvider implements Provider<Repairer> {

    private final Map<String,Repairer> repairers;

    public RepairerProvider(ClassLoader classLoader,
                            Document document) {

        repairers = new HashMap<String,Repairer>();

        Repairer repairer;
        Iterator<Repairer> i = ServiceRegistry.lookupProviders(Repairer.class, classLoader);
        while (i.hasNext()) {
            repairer = i.next();
            repairers.put(repairer.getIdentifier(), repairer);
        }

        try {
            Repairer mainRepairer = new MainRepairer(document);
            repairers.put(mainRepairer.getIdentifier(), mainRepairer);
        } catch (com.sun.star.uno.Exception e) {
        }

    }

    public Collection<Repairer> list() {
        return repairers.values();
    }

    public Repairer get(String identifier) {
        return repairers.get(identifier);
    }
}
