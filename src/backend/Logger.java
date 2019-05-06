package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class Logger {

    private static final String pathToLog = System.getProperty("user.home") + File.separator + "Desktop" +  File.separator + "logs" + File.separator;

    /**
     * Appends line to log for specific user
     * @param username - username to be used for building directory path of the log
     * @param logLine - text to be logged
     */
    public static void addToLog(String username, String logLine) {
        try {
            File logFile = new File(pathToLog + username);

            // check if directory exists
            if (!logFile.getParentFile().exists()) {
                System.out.println("Log file directory not found, creating dir...");
                logFile.getParentFile().mkdirs();
            }
            // check if log file exists
            if (!logFile.exists()) {
                System.out.println("Log file " + logFile + " not found, creating log file...");
                // create log file
                logFile.createNewFile();
            }

            // write to log file
            Files.write(logFile.toPath(), (logLine + "\n").getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.out.println("Failed to write to log");
            ex.printStackTrace();
        }
    }

    /**
     * Reads the log for a spefici user
     * @param username - username to be used for building directory path of the log
     * @return List of Strings representing the log lines.
     */
    public static List<String> readLog(String username) {
        try {
            File logFile = new File(pathToLog + username);

            // check if directory exists
            if (!logFile.getParentFile().exists()) {
                System.out.println("Log file directory not found, creating dir...");
                logFile.getParentFile().mkdirs();
            }
            // check if log file exists
            if (!logFile.exists()) {
                System.out.println("Log file " + logFile + " not found");
                return Arrays.asList("Failed to locate log");
            }

            // write to log file
            return Files.readAllLines(logFile.toPath());
        } catch (IOException ex) {
            System.out.println("Failed to read log");
            ex.printStackTrace();
            return Arrays.asList("Failed to locate log");
        }
    }

    /**
     * Returns path to log
     * @return path to the log file
     */
    public static String getPath() {
        return pathToLog;
    }
}
