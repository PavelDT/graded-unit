package backend;

public class OSUtility {

    public static boolean isMac() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.indexOf("mac") >= 0;
    }

    public static boolean isWindows() {
        String osName = System.getProperty("os.name").toLowerCase();
        return osName.indexOf("win") >= 0;
    }
}

