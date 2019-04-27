package backend;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.Date;

import static org.junit.Assert.assertEquals;

// enforce order of testing
// A user has to be registered before
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthenticationTest {

    private static final String username = "a64c7652-68f0-11e9-a923-1681be663d3e_" + new Date().getTime();
    private static final String password = "1234567";
    private final Authentication auth = new Authentication();

    @Test
    public void test1Register() {
        // check registration, 1 = successful reg
        assertEquals(auth.register(username, password), 1);
        // check that the username now exists, 0 = usr name take
        assertEquals(auth.register(username, password), 0);
    }

    @Test
    public void test2Login() {
        // check everything wrong, returned number should be 2
        // user not found returns before user / password mismatch
        assertEquals(auth.logIn(" ", " "), 2);

        // check user doesn't exist, returned number should be 2
        assertEquals(auth.logIn(username + "_doesn't_exist", password), 2);

        // check wrong password, return should be 0
        assertEquals(auth.logIn(username, password + "_wrong_password"), 0);

        // check right credentials, return should be 1
        assertEquals(auth.logIn(username, password), 1);
    }
}
