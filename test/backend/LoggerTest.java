package backend;

import org.junit.AfterClass;
import org.junit.Test;

import java.io.File;
import java.util.List;

// static imports are used to import a static function as if it was declared in the class
import static junit.framework.TestCase.*;

public class LoggerTest {
    // UUID used to ensure no user has registered with this username
    private static final String username = "a64c7652-68f0-11e9-a923-1681be663d3e-log";
    private static final String logPath = Logger.getPath();


    // ensure that the tests leave the file system in the same state
    // clean up any files created during the test
    @AfterClass
    public static void cleanUp() {
        File f = new File(logPath + username);
        if (f.exists()) {
            boolean success = f.delete();
            // ensure file was deleted or fail
            assertTrue(success);
        }
    }

    @Test
    public void testPermissions() {
        File f = new File(logPath + username);
        assertTrue(f.canRead());
        assertTrue(f.canWrite());
    }

    // Test adding to log
    @Test
    public void testAddToLog() {
        // create file object
        File f = new File(logPath + username);
        // assert it doesn't exist, it shouldn't be there
        assertFalse(f.exists());

        // write to the log thus creating the file
        Logger.addToLog(username, "Test");

        // checks if file for username above has been created.
        assertTrue(f.exists());
    }

    // Test reading from log
    @Test
    public void testReadLog() {
        List<String> logLines = Logger.readLog(username);

        // check I have expected
        assertEquals(1, logLines.size());
        String fullLog = String.join("\n", logLines);
        // ensure the log contains the word written earlier
        assertTrue(fullLog.contains("Test"));
    }
}
