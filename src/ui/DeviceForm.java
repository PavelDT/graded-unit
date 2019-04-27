package ui;

import backend.Device;
import backend.DeviceManager;
import backend.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class DeviceForm {

    private Stage primaryStage;
    private MenuBar menu;
    private Scene scene;
    private DeviceManager deviceManager;
    private String username;
    private List<Device> allDevices;
    private ComboBox<String> comboDevices;

    //construncotr
    public DeviceForm(Stage priamryStage, String username) {
        this.primaryStage = priamryStage;
        menu = generateMenu();
        this.username = username;
        deviceManager = new DeviceManager(username);
        initializeDevices();
    }

    private void initializeDevices() {
        try {
            allDevices = deviceManager.scanForDevices();
        } catch (Exception ex) {
            System.out.println("Couldn't find devices");
            ex.printStackTrace();
        }
    }

    /**
     * Converts list of devices to an observable list of strings
     * @return An observable list that is used by JavaFX controls like the combo box.
     */
    private ObservableList<String> devicesAsString() {
        List<String> devicesAsString = new ArrayList<String>();
        for (Device currentDevice : allDevices) {
            devicesAsString.add(currentDevice.getPath());
        }
        return FXCollections.observableArrayList(devicesAsString);
    }

    private String getComboboxValue() {
        String value = comboDevices.getValue();
        // todo - will be handled by disabling the register device button until a item in the combo
        //        box is selected, but until then force a check here.
        if (value.trim().equals("")) {
            throw new IllegalArgumentException("Please select a device to register.");
        }
        return value;
    }

    public Scene getScene() {

        // Define and configure controls
        // Combo box for devices found, currently hardcoded for demo UI
        comboDevices = ControlFactory.getComboBox("", "List of detected devices connected to machine.");
        // setting values of combo box
        comboDevices.setItems(devicesAsString());
        // label to describe the currently selected device.
        // Will update every time a new device is selected.
        Label labelDeviceInfo = ControlFactory.getLabel("device info: Select Device", "Describes information about currently selected device.");
        // Register device button
        Button btnRegisterDevice = ControlFactory.getButton("Register New Device", "Registers a new device.");
        btnRegisterDevice.setOnAction(registerDevice());
        // Backup button
        Button btnBackup = ControlFactory.getButton("Backup Device", "Backs up currently selected device.");
        btnBackup.setOnAction(backup());
        // Sync button
        Button btnSync = ControlFactory.getButton("Synchronise Device", "Synchronises currently selected divice");
        // Restore button
        Button btnRestore = ControlFactory.getButton("Restore Device", "Creates backup of existing device");
        btnRestore.setOnAction(restore());
        // View Logs link
        Hyperlink linkViewLogs  = ControlFactory.getHyperlink("View Device Logs", "Displays device logs.");
        linkViewLogs.setOnAction(showLogs());
        // Exit button
        Button btnExit = ControlFactory.getButton("Exit", "Shuts down application.");
        btnExit.setOnAction(terminateApplication());

        // vbox used for alignment in the scene. The parameter '10' is spacing between controls.
        VBox rootVbox = new VBox(10, menu);
        // todo - maybe make this part of CSS rather than centering programmatically
        rootVbox.setAlignment(Pos.TOP_CENTER);
        // sets background colour for vbox
        //for detailed explanation view AuthenticationForm's getScene function.
        rootVbox.setBackground(new Background(new BackgroundFill(Color.rgb(201,191,228), CornerRadii.EMPTY, Insets.EMPTY)));

        // Attach the controls to the layout vbox
        rootVbox.getChildren().add(comboDevices);
        rootVbox.getChildren().add(labelDeviceInfo);
        rootVbox.getChildren().add(btnRegisterDevice);
        rootVbox.getChildren().add(btnBackup);
        rootVbox.getChildren().add(btnSync);
        rootVbox.getChildren().add(btnRestore);
        rootVbox.getChildren().add(btnExit);
        rootVbox.getChildren().add(linkViewLogs);

        scene = new Scene(rootVbox, 360, 510);

        // This is to ensure that when the application is packaged as a jar, it can still find the css file
        URL styleURL = this.getClass().getResource("/ui/assets/style.css");
        scene.getStylesheets().add(styleURL.toExternalForm());

        primaryStage.setTitle("Device Form");
        primaryStage.setScene(scene);
        primaryStage.show();

        return scene;
    }

    /**
     * Registers a selected device
     * @return Event handler for registering currently selected device from combo box
     */
    private EventHandler<ActionEvent> registerDevice() {
        return event -> {
            try {
                String id = deviceManager.registerNew(getComboboxValue());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Registered device successfully with ID " + id);
                alert.show();
            } catch(Exception ex){
                //notifies user of problem and displays first line of exception in an alert.
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error registering a device " + ex.getMessage());
                alert.show();
                ex.printStackTrace();
            }
        };
    }

    /**
     * Backups up all data of a device
     * @return Event handler for backing up currently selected device
     */
    private EventHandler<ActionEvent> backup() {
        return event -> {
            try {
                String path = getComboboxValue();
                String id = deviceManager.readId(path);
                deviceManager.backup(path, id);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Backup successful");
                alert.show();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error during backup " + ex.getMessage());
                alert.show();
                ex.printStackTrace();
            }
        };
    }

    /**
     * Restores backup to a device
     * @return Event handler for restoring a backup
     */
    private EventHandler<ActionEvent> restore() {
        return event -> {
            try {
                deviceManager.restore(getComboboxValue());
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Restore successful");
                alert.show();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error during restore " + ex.getMessage());
                alert.show();
                ex.printStackTrace();
            }
        };
    }

    /**
     * Displays logs in an alert
     * @return Event handler for displaying the logs when link is clicked
     */
    private EventHandler<ActionEvent> showLogs() {
        return event -> {
            // read log via logger class
            List<String> logLines = Logger.readLog(username);
            // create and display info alert
            Alert alert = new Alert(Alert.AlertType.INFORMATION, String.join("", logLines));
            alert.getDialogPane().setMinHeight(Region.USE_PREF_SIZE);
            alert.setTitle("Log");
            alert.show();
        };
    }

    /**
     * todo - check if device is in progress of sync or backup before exiting.
     * Exits application
     *
     * @return EventHandler for button click that will call system exit and close the application
     */
    private EventHandler<ActionEvent> terminateApplication() {
        return event -> System.exit(0);
    }

    /**
     * Builds menu for different colour themes.
     * @return MenuBar containing the 3 settings - regular, dark and colour-blind modes.
     */
    public MenuBar generateMenu() {
        MenuBar menuBar = new MenuBar();
        // todo - Windows & Linux
        // This makes the native appear more in the style of the operating system
        // It's a usability improvement, keeps things familiar for the user.
        if( System.getProperty("os.name","UNKNOWN").equals("Mac OS X")) {
            menuBar.setUseSystemMenuBar(true);
        }

        Menu menuItem = new Menu("Colour Mode");
        MenuItem menuItem1 = new MenuItem("Regular Mode");
        menuItem1.setOnAction(changeTheme("/ui/assets/style.css"));
        MenuItem menuItem2 = new MenuItem("Dark Mode");
        menuItem2.setOnAction(changeTheme("/ui/assets/dark.css"));
        MenuItem menuItem3 = new MenuItem("Colour-blind Mode");
        // todo
        // menuItem3.setOnAction(changeColourTest("/ui/assets/colourblind.css"));
        menuItem.getItems().add(menuItem1);
        menuItem.getItems().add(menuItem2);
        menuItem.getItems().add(menuItem3);

        // http://tutorials.jenkov.com/javafx/menubar.html
        menuBar.getMenus().add(menuItem);

        return menuBar;
    }

    /**
     * utility that is part of the Menu control
     * @param stylesheet - style to apply for the theme change
     * @return An EventHandler for the on-click action
     */
    private EventHandler<ActionEvent> changeTheme(String stylesheet) {

        // this is kept as a demonstration of why lambda's were used for all other actions.
        // It's leaner more condensed and readable code.
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
