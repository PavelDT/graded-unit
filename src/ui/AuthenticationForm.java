package ui;

import backend.Authentication;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.net.URL;

public class AuthenticationForm {

    private Stage primaryStage;
    private MenuBar menu;
    private Scene scene;
    private Authentication auth;
    // controls
    private Button btnLogin;
    private Button btnRegister;
    private TextField txtUsername;
    private PasswordField txtPassword;
    private Hyperlink link;

    /**
     * Non-default constructor
     * @param primaryStage - requires primaryStage from JavaFX library
     */
    public AuthenticationForm(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.menu = generateMenu();
        auth = new Authentication();
    }

    /**
     * Generated the Scene used for the authentication form, defines some local controls and initialises non-initialised
     * private controls.
     * @return Scene - the authentication form scene
     */
    public Scene getScene() {

        // Scene controls are declared here as local variables.
        // no NullPointerException as these controlls are initialized before their values are ever checked in ifs
        btnLogin = ControlFactory.getButton("Login", "Authenticates and logs user in");
        btnRegister = ControlFactory.getButton("Register", "Registers new user");
        txtUsername = ControlFactory.getTextField("username", "Please input your username");
        txtPassword = ControlFactory.getPasswordField("Please input your password");
        link = ControlFactory.getHyperlink("Register User", "Opens dialogue to register new user");

        // vbox used for alignment in the scene. The parameter '10' is spacing between controls.
        // allows controls to be stacked from top to bottom, one control per line.
        VBox rootVBox = new VBox(10, menu);
        rootVBox.setAlignment(Pos.TOP_CENTER);

        // link for registering a new user. Control configuration
        link.setText("Register User");
        // todo
        //link.setOnAction();

        // login button, on click will check user credentials
        // and log them in if valid.;
        btnLogin.setOnAction(login());

        // onclick of register link will hide the login button and register link,
        // and then will show itself. Finally, a box of instructions will be displayed.
        link.setOnAction(registerLink());

        // allow new users to register
        btnRegister.setOnAction(register());

        // register button is hidden by default
        btnRegister.setVisible(false);

        // Attach the controls to the layout vbox
        rootVBox.getChildren().add(txtUsername);
        rootVBox.getChildren().add(txtPassword);
        rootVBox.getChildren().add(link);
        rootVBox.getChildren().add(btnLogin);
        rootVBox.getChildren().add(btnRegister);

        /* configure the scene to have the correct background colour
         * requires Colour class to allow for a specific RGB rather than using one
         * of the preset colours */
        rootVBox.setBackground(new Background(new BackgroundFill(Color.rgb(201,191,228), CornerRadii.EMPTY, Insets.EMPTY)));
        // bind "Enter" aka return key to fire form's login button when pressed
        rootVBox.addEventHandler(KeyEvent.KEY_PRESSED, ev -> {
            if (ev.getCode() == KeyCode.ENTER) {
                btnLogin.fire();
                ev.consume();
            }
        });

        /* Create a new scene to be used for the authentication form
         * and sets the size of the scene */
        scene = new Scene(rootVBox, 360, 510);

        // This is to ensure that when the application is packaged as a jar, it can still find the css file
        URL styleURL = this.getClass().getResource("/ui/assets/style.css");
        scene.getStylesheets().add(styleURL.toExternalForm());

        return scene;
    }

    /**
     * Event Handler for clicking on ther "registerLink" link, will hide login button and show
     * register button.
     * @return EventHanlder for registerLink link
     */
    private EventHandler<ActionEvent> registerLink() {
        return event -> {
            // hide login button
            btnLogin.setVisible(false);
            // prevent the button from being triggered via Enter key press
            btnLogin.setDisable(true);
            // hide register link
            link.setVisible(false);
            // show register button
            btnRegister.setVisible(true);
        };
    }

    /**
     * Triggers event to register a user using Authentication.java
     * @return EventHandler to register user
     */
    private EventHandler<ActionEvent> register() {
        return event -> {
            String username = txtUsername.getText();
            int registered = auth.register(username, txtPassword.getText());
            Alert alert = null;
            switch (registered) {
                case -1:
                    alert = new Alert(Alert.AlertType.ERROR, "Error during registration, please try again.");
                    alert.show();
                    break;
                case 0:
                    // username already taken
                    alert = new Alert(Alert.AlertType.ERROR, "Username already taken, please choose a different username");
                    alert.show();
                    break;
                case 1:
                    // registered successfully
                    alert = new Alert(Alert.AlertType.INFORMATION, "Registration successful");
                    alert.setTitle("Success");
                    alert.showAndWait();
                    switchToDeviceScene(username);
                    break;
                case 2:
                    // invalid username, only chars and numbs allowed
                    alert = new Alert(Alert.AlertType.ERROR, "Username can contain only character and numbers, please choose a different username");
                    alert.show();
                    break;
                case 3:
                    // invalid username, only chars and numbs allowed
                    alert = new Alert(Alert.AlertType.ERROR, "Password requires a letter, a number and a special-character. Minimum password length is 8, please choose a different password");
                    alert.show();
                    break;
                default:
                    alert = new Alert(Alert.AlertType.ERROR, "Unknown problem during registration.\nPlease restart the application.");
                    alert.show();
                    break;
            }
        };
    }

    /**
     * Login function. Checks if username and password are valid.
     * Upon successful authentication the device form is displayed
     * Displays error message if auth fails.
     * Case statuses: -1 for an error, 0 for username / password mismatch, 1 for success, 2 for user doesn't exits
     * @return Returns an EventHandler to handle when the login button is clicked.
     */
    private EventHandler<ActionEvent> login() {
        // use lambdas as they're more compact in code
        return event -> {
            // check if username and password are valid for login
            // trim username to remove any unwanted spaces before or after the username
            // i.e. "  jack " becomes "jack"
            String username = txtUsername.getText().trim();
            int loggedIn = auth.logIn(username, txtPassword.getText());
            Alert alert = null;
            switch (loggedIn) {
                case -1:
                    // Error logging in
                    alert = new Alert(Alert.AlertType.ERROR, "Error during login, please try again.");
                    alert.show();
                    break;
                case 0:
                    // username / password mismatch
                    alert = new Alert(Alert.AlertType.ERROR, "Wrong username & password combination");
                    alert.show();
                    break;
                case 1:
                    // logged in successfully
                    switchToDeviceScene(username);
                    break;
                case 2:
                    // user not registered
                    alert = new Alert(Alert.AlertType.ERROR, "User not registered");
                    alert.show();
                    break;
                default:
                    alert = new Alert(Alert.AlertType.ERROR, "Unknown problem during login.\nPlease restart the application.");
                    alert.show();
                    break;
            }
        };
    }


    /**
     * Switches from the current login scene to the device scene
     * This should be triggered on successful authentication.
     * @param username - Username of the user that has authenticated
     */
    public void switchToDeviceScene(String username) {
        // hide current form
        primaryStage.hide();

        // display the device form.
        primaryStage.setTitle("Device Form");
        primaryStage.setScene(new DeviceForm(primaryStage, username).getScene());
        primaryStage.show();
    }

    /**
     * Builds menu for different colour themes.
     * @return MenuBar containing the 3 settings - regular, dark and colour-blind modes.
     */
    public MenuBar generateMenu() {
        MenuBar menuBar = new MenuBar();
        // This makes the native appear more in the style of the operating system
        // It's a usability improvement, keeps things familiar for the user.
        // Note: adding this to windows creates usability issues with hidden menu items, won't do native
        // windows menu due to these issues.
        if( System.getProperty("os.name","UNKNOWN").equals("Mac OS X")) {
            menuBar.setUseSystemMenuBar(true);
        }

        Menu menuItem = new Menu("Colour Mode");
        MenuItem menuItem1 = new MenuItem("Regular Mode");
        menuItem1.setOnAction(changeTheme("/ui/assets/style.css"));
        MenuItem menuItem2 = new MenuItem("Dark Mode");
        menuItem2.setOnAction(changeTheme("/ui/assets/dark.css"));
        // todo
        // menuItem3.setOnAction(changeColourTest("/ui/assets/colourblind.css"));
        menuItem.getItems().add(menuItem1);
        menuItem.getItems().add(menuItem2);

        // http://tutorials.jenkov.com/javafx/menubar.html
        menuBar.getMenus().add(menuItem);

        return menuBar;
    }

    /**
     * Allows for changing colour theme of application.
     * @param stylesheet - style to apply for the theme change
     * @return An EventHandler for the on-click action
     */
    private EventHandler<ActionEvent> changeTheme(String stylesheet) {
        return new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                // very important to clear previous style sheet otherwise
                // style sheets pile up and prevent a toggle-like switch between
                // the different colour modes
                scene.getStylesheets().clear();
                // set through CSS. can be set programmatically:
                // root.setBackground(new Background(new BackgroundFill(Color.rgb(0,0,0), CornerRadii.EMPTY, Insets.EMPTY)));
                URL styleURL = this.getClass().getResource(stylesheet);
                scene.getStylesheets().add(styleURL.toExternalForm());

                for(String s: scene.getStylesheets()) {
                    System.out.println(s);
                }
            }
        };
    }
}