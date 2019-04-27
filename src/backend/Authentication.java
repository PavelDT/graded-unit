package backend;

import javax.crypto.spec.PBEKeySpec;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;

public class Authentication {

    final static String SPLITTER = "@@@";
    // path separator used to keep os cross-compatibility
    final static String vaultPath = System.getProperty("user.home") + File.separator + "Desktop" +  File.separator + "vault.txt";

    // default constructor
    public Authentication() {

    }

    /**
     * Checks if user is already registered to use backup application
     * @param username
     * @param password
     * @return -1 for an error, 0 for username / password mismatch, 1 for success, 2 for user doesn't exits
     */
    public int logIn(String username, String password){

        password = encryptPassword(password);

        try {
            //check user exists
            String[] userAuthenticationInfo = userExists(username);

            if (userAuthenticationInfo == null) {
                // user doesn't exist
                return 2;
            }

            // check if password matches, use index 1 as that represents password
            if (password.equals(userAuthenticationInfo[1])) {
                // username and password match
                return 1;
            }
        } catch (IOException ex) {
            // handle the exception by printing a message
            System.out.println("Failed to log in");
            ex.printStackTrace();
            return -1;
        }

        // username and password didnt match
        return 0;
    }

    /**
     * Allow new user to register for the application
     * @param username
     * @param password
     * @return -1 for error, 0 for fail due to username being taken, 1 for successful registration
     */
    // todo - create username rules, fail if trailing spaces are present
    public int register(String username, String password){
        try {
            // check if vault exists, create it if it doesn't
            createPasswordVault();
            // check if user exists
            if (userExists(username) != null) {
                System.out.println("Username already taken.");
                return 0;
            }

            // create user-pass string and encrypt the password
            // apparently "\n" isn't universal for all os' so user System.getProperty("line.separator") instead.
            String userPassAuth = username + SPLITTER + encryptPassword(password) + System.getProperty("line.separator");
            // save user authentication details to file
            Files.write(Paths.get(vaultPath), userPassAuth.getBytes(), StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.out.println("Failed to create user due to fault access error");
            ex.printStackTrace();
            return -1;
        }

        // user registered successfully
        return 1;
    }

    private void createPasswordVault() throws IOException {
        File vault = new File(vaultPath);
        if (!vault.exists())
            vault.createNewFile();
    }

    private String[] userExists(String username) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(vaultPath));
        // Loop over existing users and search for the currently given username
        for(String userpassPair : lines){
            String[] userPassInfo = userpassPair.split(SPLITTER);
            // get the first index of the split, as the first index represents the username
            if (userPassInfo[0].equals(username))
                return userPassInfo;
        }
        return null;
    }

    private String encryptPassword(String password) {
        String base64password = Base64.getEncoder().encodeToString(password.getBytes());
        return base64password;
    }
}
