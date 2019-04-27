import backend.*;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
        LoggerTest.class,
        AuthenticationTest.class,
        DeviceTest.class
})

public class TestSuite {
    // class stays empty
    // it's only used to hold the suite annotations
}
