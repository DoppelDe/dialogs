package de.ghci.dialog.io;

import java.io.File;

/**
 * @author Dominik
 */
public class Resources {

    public static String getResourcesPath() {
        File currDir = new File(".");
        String path = currDir.getAbsolutePath();
        path = path.substring(0, path.length() - 2);
        return path + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator;
    }

}
