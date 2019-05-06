package backend;

public interface Auth {

    /**
     * Checks if user is already registered to use backup application
     * @param username - user's inputted username
     * @param password - user's inputted password
     * @return -1 for an error, 0 for username / password mismatch, 1 for success, 2 for user doesn't exits
     */
    public int logIn(String username, String password);

    /**
     * Allow new user to register for the application
     * @param username - user's inputted username
     * @param password - user's inputted password
     * @return -1 for error, 0 for fail due to username being taken, 1 for successful registration,
     *         2 for invalid username, 3 for invalid password.
     */
    public int register(String username, String password);

}
