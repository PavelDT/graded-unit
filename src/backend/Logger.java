package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;

public class Logger {

    private static final String pathToLog = System.getProperty("user.home") + File.separator + "Desktop" +  File.separator + "logs" + File.separator;

    public static void addToLog(String username, String logLine) {
        try {
            // todo - check if this writes to just a single line
            //        or if it adds a new line marker at the end of each line
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

    public static List<String> readLog(String username) {
        try {
            // todo - check if this writes to just a single line
            //        or if it adds a new line marker at the end of each line
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

    public static String getPath() {
        return pathToLog;
    }
}
