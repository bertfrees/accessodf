package be.docarch.accessibility;

import java.io.File;

/**
 * Class representing an accessibility report (EARL)
 *
 * @author Bert Frees
 */
public class Report {

    private File file = null;
    private String name = null;

    public Report(File file,
                  String name) {

        this.file = file;
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public String getName() {
        return name;
    }
}
