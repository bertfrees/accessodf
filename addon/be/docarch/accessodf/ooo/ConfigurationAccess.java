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

import com.sun.star.beans.PropertyState;
import com.sun.star.beans.PropertyValue;
import com.sun.star.container.XNameAccess;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * Provides generic factory methods around configuration access.
 * @author Christian Lins (christian.lins@sun.com)
 */
public class ConfigurationAccess
{
  /**
   * Creates a XNameAccess instance for read and write access to the
   * configuration at the given node path.
   * @param context
   * @param path
   * @return
   */
  public static XNameAccess createUpdateAccess(XComponentContext context, String path)
  {
    XNameAccess access;

    // Create the com.sun.star.configuration.ConfigurationUpdateAccess
    // for the registry node which contains the data for our option
    // pages.
    XMultiServiceFactory xConfig;

    try
    {
      xConfig = (XMultiServiceFactory) UnoRuntime.queryInterface(
        XMultiServiceFactory.class,
        context.getServiceManager().createInstanceWithContext(
          "com.sun.star.configuration.ConfigurationProvider", context));
    }
    catch (com.sun.star.uno.Exception e)
    {
      e.printStackTrace();
      return null;
    }

    // One argument for creating the ConfigurationUpdateAccess is the "nodepath".
    // Our nodepath point to the node of which the direct subnodes represent the
    // different options pages.
    Object[] args = new Object[1];
    args[0] = new PropertyValue("nodepath", 0, path,PropertyState.DIRECT_VALUE);
    
     // We get the com.sun.star.container.XNameAccess from the instance of
    // ConfigurationUpdateAccess and save it for later use.
    try
    {
      access = (XNameAccess) UnoRuntime.queryInterface(
        XNameAccess.class, xConfig.createInstanceWithArguments(
          "com.sun.star.configuration.ConfigurationUpdateAccess", args));
    }
    catch (com.sun.star.uno.Exception e)
    {
      e.printStackTrace();
      return null;
    }

    return access;
  }
}
