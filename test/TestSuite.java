import backend.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        LoggerTest.class,
        AuthenticationTest.class,
        DeviceTest.class
})

/**
 * JUnit test suite for running all unit tests for the backend of the Backup Application
 */
public class TestSuite {
    // class stays empty
    // it's only used to hold the suite annotations
}
