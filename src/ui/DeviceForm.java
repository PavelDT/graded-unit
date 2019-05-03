package ui;

import backend.Device;
import backend.DeviceManager;
import backend.Logger;
import javafx.beans.value.ChangeListener;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class DeviceForm {

    private Stage primaryStage;
    private MenuBar menu;
    private Scene scene;
    private DeviceManager deviceManager;
    private String username;
    // instance controls
    private ComboBox<String> comboDevices;
    private Label labelDeviceInfo;
    private Button btnRegisterDevice;
    private Button btnBackup;
    private Button btnSync;
    private Button btnRestore;

    //constructor
    public DeviceForm(Stage priamryStage, String username) {
        this.primaryStage = priamryStage;
        menu = generateMenu();
        this.username = username;
        deviceManager = new DeviceManager(username);
    }

    /**
     * Converts list of devices to an observable list of strings
     * @return An observable list that is used by JavaFX controls like the combo box.
     */
    private ObservableList<String> devicesAsString() {
        List<String> devicesAsString = new ArrayList<String>();
        // scan for devices every time
        for (Device currentDevice : deviceManager.scanForDevices()) {
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
        comboDevices.valueProperty().addListener(comboDevicesChanged());
        // label to describe the currently selected device.
        // Will update every time a new device is selected.
        labelDeviceInfo = ControlFactory.getLabel("device info: Select Device", "Describes information about currently selected device.");
        // Register device button
        btnRegisterDevice = ControlFactory.getButton("Register New Device", "Registers a new device.");
        btnRegisterDevice.setOnAction(registerDevice());
        // disabled until an unregistered device is selected
        btnRegisterDevice.setDisable(true);
        // Backup button
        btnBackup = ControlFactory.getButton("Backup Device", "Backs up currently selected device.");
        // disabled until a registered device is selected
        btnBackup.setOnAction(backup());
        // disabled until a registered device is selected
        btnBackup.setDisable(true);
        // Sync button
        btnSync = ControlFactory.getButton("Synchronise Device", "Synchronises currently selected divice");
        // disabled until a registered device is selected
        btnSync.setOnAction(sync());
        btnSync.setDisable(true);
        // Restore button
        btnRestore = ControlFactory.getButton("Restore Device", "Creates backup of existing device");
        btnRestore.setOnAction(restore());
        btnRestore.setDisable(true);
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
                // disable "register new"
                btnRegisterDevice.setDisable(true);
                // enable "backup", "sync" and "restore".
                btnBackup.setDisable(false);
                btnSync.setDisable(false);
                btnRestore.setDisable(false);
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
                deviceManager.backup(path);
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
     * Syncs previously unsynced files (aka dirty files) to the sync folder
     * @return Event handler for syncing dirty files to device
     */
    private EventHandler<ActionEvent> sync() {
        return event -> {
            try {
                String devicePath = getComboboxValue();
                deviceManager.synchronise(devicePath);
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Sync successful");
                alert.show();
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error during sync " + ex.getMessage());
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
                // Create an alert for the type of retoration
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Choose restoration type.");
                alert.setTitle("Choose restore method");
                alert.setHeaderText("Do you want to restore from the last full snapshot, or from they sync folder?");

                ButtonType btnSyncBased = new ButtonType("Sync");
                ButtonType btnBackupBased = new ButtonType("Backup");

                alert.getButtonTypes().setAll(btnSyncBased, btnBackupBased);
                Optional<ButtonType> result = alert.showAndWait();

                if (result.get() == btnSyncBased) {
                    deviceManager.syncRestore(getComboboxValue());
                } else if (result.get() == btnBackupBased) {
                    deviceManager.restore(getComboboxValue());
                }

                alert = new Alert(Alert.AlertType.INFORMATION, "Restore successful");
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
     * Updates the device info label to represent the status of the newly selected device
     * Note: this can lead to a IndexOutOfBoundsException in the JavaFX threading, the exception won't
     *       crash the application but will lead to a error not being displayed.
     * @return ChangeListener that stores functionality to update label when comboboxDevice's value changes
     */
    private ChangeListener<String> comboDevicesChanged() {
        return (observableValue, oldValue, newValue) -> {
            String status = deviceManager.readId(newValue + File.separator);
            String msg = "Status unknown";

            if (status.equals("error")) {
                // update info status
                msg = "Error reading device status";
                labelDeviceInfo.setTextFill(Color.RED);

                // disable "register new", "backup", "sync" and "restore".
                btnRegisterDevice.setDisable(true);
                btnBackup.setDisable(true);
                btnSync.setDisable(true);
                btnRestore.setDisable(true);
            } else if (status.equals("new")) {
                // update info status
                msg = "Unregistered device detected";
                labelDeviceInfo.setTextFill(Color.DARKCYAN);

                // disable "backup", "sync" and "restore".
                btnBackup.setDisable(true);
                btnSync.setDisable(true);
                btnRestore.setDisable(true);
                // enable "register new"
                btnRegisterDevice.setDisable(false);
            } else if (status.length() == 32) {
                // update info status
                msg = "Registered device detected";
                labelDeviceInfo.setTextFill(Color.GREEN);

                // disable "register new"
                btnRegisterDevice.setDisable(true);
                // enable "backup", "sync" and "restore".
                btnBackup.setDisable(false);
                btnSync.setDisable(false);
                btnRestore.setDisable(false);
            } else {
                labelDeviceInfo.setTextFill(Color.BLACK);
            }

            // ensure a refresh of device list
            comboDevices.setItems(devicesAsString());
            this.labelDeviceInfo.setText("device info: " + msg);
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
