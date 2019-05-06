package backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import java.util.List;


/**
 * Authentication mechanism for the backup app
 */
public class Authentication implements Auth {

    private final static String SPLITTER = "@@@";
    // path separator used to keep os cross-compatibility
    private final static String vaultPath = System.getProperty("user.home") + File.separator + "Desktop" +  File.separator + "vault.txt";

    // default constructor
    public Authentication() {

    }

    /**
     * Checks if user is already registered to use backup application
     * @param username - user's inputted username
     * @param password - user's inputted password
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
     * @param username - user's inputted username
     * @param password - user's inputted password
     * @return -1 for error, 0 for fail due to username being taken, 1 for successful registration,
     *         2 for invalid username, 3 for invalid password.
     */
    public int register(String username, String password){
        try {
            // check if vault exists, create it if it doesn't
            createPasswordVault();

            if (!validateUsername(username)) {
                System.out.println("Username must be only letters and numbers");
                return 2;
            }

            if (!validatePassword(password)) {
                System.out.println("Password requires a letter, a number and a special-character. Minimum password length is 8.");
                return 3;
            }

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

    /**
     * Created required vault text file to store passwords and user-names
     * @throws IOException - If a file cannot be read / written
     */
    private void createPasswordVault() throws IOException {
        File vault = new File(vaultPath);
        if (!vault.exists())
            vault.createNewFile();
    }

    /**
     * Checks if the user is already registered
     * @param username - username to be checked for existence
     * @return Array of Strings containing 0 - the username, 1 - the password
     * @throws IOException - If a file cannot be read / written
     */
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

    /**
     * Applies Base64 encryption to the password
     * @param password - password to be encrypted
     * @return Base64 encrypted string
     */
    private String encryptPassword(String password) {
        String base64password = Base64.getEncoder().encodeToString(password.getBytes());
        return base64password;
    }

    /**
     * Checks if username is valid, usernames must contain only numbers and letters
     * @param username - username to be validated
     * @return boolean if username is valid
     */
    private boolean validateUsername(String username) {
        // use regular expresion to ensure username is only letters and numbers
        // regex source: https://stackoverflow.com/questions/33467536
        return username.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Checks if password is long enough
     * @param password - password to be validated
     * @return If password is strong enough
     */
    private boolean validatePassword(String password) {
        boolean numbCheck = false;
        boolean letterCheck = false;
        boolean specialCharacterCheck = false;
        boolean lengthCheck = password.length() > 7;

        for(char c: password.toCharArray()) {
            // Convert char to string and store in variable as its used many times
            String current = c + "";

            // special char check
            if (!(current).matches("[\\w\\s]*")) {
                specialCharacterCheck = true;
            }

            // number check
            if (current.matches("\\d+")) {
                numbCheck = true;
            }

            // letter check
            if (current.matches("^[a-zA-Z]*$")) {
                letterCheck = true;
            }
        }

        // all 4 booleans have to be true or this will == false
        return specialCharacterCheck && numbCheck && letterCheck && lengthCheck;
    }
}
