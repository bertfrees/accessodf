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

import be.docarch.accessodf.Constants;
import be.docarch.accessodf.Checker;
import be.docarch.accessodf.Provider;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.imageio.spi.ServiceRegistry;

/**
 *
 * @author Bert Frees
 */
public class CheckerProvider implements Provider<Checker> {

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final Map<String,Checker> checkers;

    public CheckerProvider(ClassLoader classLoader,
                           Document document) {

        checkers = new HashMap<String,Checker>();

        Checker checker;
        Iterator<Checker> i = ServiceRegistry.lookupProviders(Checker.class, classLoader);
        while (i.hasNext()) {
            checker = i.next();
            checkers.put(checker.getIdentifier(), checker);
            logger.info(checker.getIdentifier() + " loaded");
        }

        try {
            Checker mainChecker = new MainChecker(document);
            checkers.put(mainChecker.getIdentifier(), mainChecker);
            logger.info(mainChecker.getIdentifier() + " loaded");
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public Collection<Checker> list() {
        return checkers.values();
    }

    public Checker get(String identifier) {
        return checkers.get(identifier);
    }
}
