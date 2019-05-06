package backend;

/**
 * Utility for operating systems is stored here
 */
public class OSUtility {

    /**
     * Checks if current os is Macintosh based
     * @return boolean representing if current os is Mac
     */
    public static boolean isMac() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.indexOf("mac") >= 0;
    }

    /**
     * Checks if current os is Windows based
     * @return boolean representing if current os is Windows
     */
    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.indexOf("win") >= 0;
    }
}

