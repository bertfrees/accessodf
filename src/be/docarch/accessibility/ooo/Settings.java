package be.docarch.accessibility.ooo;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.container.XNameAccess;
import com.sun.star.beans.XPropertySet;
import com.sun.star.util.XChangesBatch;

/**
 *
 * @author Bert Frees
 */
public class Settings {

    private boolean daisyChecks = false;
    private boolean brailleChecks = false;

    private final boolean daisyChecksAvailable;
    private final boolean brailleChecksAvailable;

    private XNameAccess accessLeaves = null;
    private XPropertySet xLeaf = null;

    public Settings(XComponentContext context) {

        accessLeaves = ConfigurationAccess.createUpdateAccess(context,
                        "/be.docarch.accessibility.ooo.optionspage.Settings/Leaves");
        try {
            xLeaf = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, accessLeaves.getByName("accessibilityChecks"));
        } catch (com.sun.star.container.NoSuchElementException e) {
        } catch (com.sun.star.lang.WrappedTargetException e) {
        }

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(context);

        daisyChecksAvailable = (xPkgInfo.getPackageLocation("com.versusoft.packages.ooo.odt2daisy.addon.Odt2DaisyAddOn-macosx_x86").length() > 0)
                            || (xPkgInfo.getPackageLocation("com.versusoft.packages.ooo.odt2daisy.addon.Odt2DaisyAddOn-macosx_powerpc").length() > 0)
                            || (xPkgInfo.getPackageLocation("com.versusoft.packages.ooo.odt2daisy.addon.Odt2DaisyAddOn-windows_x86").length() > 0)
                            || (xPkgInfo.getPackageLocation("com.versusoft.packages.ooo.odt2daisy.addon.Odt2DaisyAddOn-linux_x86").length() > 0)
                            || (xPkgInfo.getPackageLocation("com.versusoft.packages.ooo.odt2daisy.addon.Odt2DaisyAddOn-linux_x86_64").length() > 0)
                            || (xPkgInfo.getPackageLocation("com.versusoft.packages.ooo.odt2daisy.addon.Odt2DaisyAddOn-solaris_x86").length() > 0)
                            || (xPkgInfo.getPackageLocation("com.versusoft.packages.ooo.odt2daisy.addon.Odt2DaisyAddOn-solaris_sparc").length() > 0);
        brailleChecksAvailable = (xPkgInfo.getPackageLocation("be.docarch.odt2braille.ooo.odt2brailleaddon").length() > 0);

        daisyChecks(true);
        brailleChecks(true);
        
    }

    public void loadData() throws com.sun.star.uno.Exception {

        if (xLeaf != null) {
            daisyChecks(xLeaf.getPropertyValue("daisyChecks").toString().equals("1"));
            brailleChecks(xLeaf.getPropertyValue("brailleChecks").toString().equals("1"));
        }
    }

    public void saveData() throws com.sun.star.uno.Exception {

        if (xLeaf != null) {
            xLeaf.setPropertyValue("daisyChecks", (short)(daisyChecks?1:0));
            xLeaf.setPropertyValue("brailleChecks", (short)(brailleChecks?1:0));

            XChangesBatch xUpdateCommit = (XChangesBatch)UnoRuntime.queryInterface(
                                           XChangesBatch.class, accessLeaves);
            xUpdateCommit.commitChanges();
        }
    }

    public boolean daisyChecks() {
        return daisyChecks;
    }

    public boolean brailleChecks() {
        return brailleChecks;
    }

    public void daisyChecks(boolean enable) {
        daisyChecks = daisyChecksAvailable && enable;
    }

    public void brailleChecks(boolean enable) {
        brailleChecks = brailleChecksAvailable && enable;
    }

    public boolean daisyChecksAvailable() {
        return daisyChecksAvailable;
    }

    public boolean brailleChecksAvailable() {
        return brailleChecksAvailable;
    }
}
