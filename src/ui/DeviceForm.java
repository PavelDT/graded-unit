package ui;

import backend.Device;
import backend.DeviceManager;
import backend.Logger;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
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
    private ProgressBar progressBar;

    /**
     * Non-default constructor
     * @param primaryStage - primaryStage from JavaFX library
     * @param username - user's username from AuthenticationForm
     */
    public DeviceForm(Stage primaryStage, String username) {
        this.primaryStage = primaryStage;
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

    /**
     * Returns value of the device-list combo box
     * @return String representing combo box value
     */
    private String getComboboxValue() {
        String value = comboDevices.getValue();
        // todo - will be handled by disabling the register device button until a item in the combo
        //        box is selected, but until then force a check here.
        if (value.trim().equals("")) {
            throw new IllegalArgumentException("Please select a device to register.");
        }
        return value;
    }

    /**
     * Generated the Scene used for the device form, defines some local controls and initialises non-initialised
     * private controls.
     * @return Scene - the device form scene
     */
    public Scene getScene() {

        // Define and configure controls
        // Combo box for devices found, currently hardcoded for demo UI
        comboDevices = ControlFactory.getComboBox("List of detected devices connected to machine.");
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
        // Progress bar used for Backup / Restore / Sync
        progressBar = new ProgressBar();
        progressBar.setMaxWidth(300);
        progressBar.setProgress(0);
        progressBar.setVisible(false);

        // vbox used for alignment in the scene. The parameter '10' is spacing between controls.
        VBox rootVbox = new VBox(10, menu);
        // align the vbox controls to be centered
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
        rootVbox.getChildren().add(progressBar);

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
            // JavaFX has a concurrency package that contains a lot of utility to make
            // the UI interactive and not block while stuff is happening in the background
            // Like for example copying large files.
            // The task class allows something to be run a separate thread preventing the UI
            // from freezing and allowing for a progress bar to be created
            // source: https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm#sthref8
            // source2: https://stackoverflow.com/questions/29844344
            Task task = new Task<Void>() {
                @Override public Void call() throws IOException{
                    deviceManager.backup(getComboboxValue());
                    // just to fill up the progress bar
                    updateProgress(100, 100);
                    return null;
                }
            };

            // handle what happens when the tasks stops executing
            // source: https://stackoverflow.com/a/38476765
            task.setOnSucceeded(evt -> taskStopped(task.getException(), "Backup", 1));
            task.setOnCancelled(evt -> taskStopped(task.getException(), "Backup", 2));
            task.setOnFailed(evt -> taskStopped(task.getException(), "Backup", 3));



            // disable all the controls before starting the tasks to prevent users
            // from launching multiple tasks together
            disableAllControls();

            // the task has to be started in a separate thread to not freeze the device form
            // source: https://docs.oracle.com/javafx/2/threads/jfxpub-threads.htm#sthref8
            // source2: https://stackoverflow.com/questions/29844344
            new Thread(task).start();

            // not the most detailed progress bar, but at-least gives users a sense of the application not stalling
            progressBar.setVisible(true);
            progressBar.progressProperty().bind(task.progressProperty());
        };
    }

    /**
     * Handles what happens when a backup / restore / sync task has completed
     * @param t - Throwable object that may contain an excepting thrown during the task's execution
     * @param taskType - String describing the task
     * @param status - how did the task complete? 1 - success, 2 - cancelled, 3 - error.
     */
    private void taskStopped(Throwable t, String taskType, int status) {

        String errorMsg = "";
        if(t != null) {
            t.printStackTrace();
            errorMsg = t.getMessage();
        }

        switch (status) {
            case 1:
                new Alert(Alert.AlertType.INFORMATION, taskType + " successful").showAndWait();
                break;
            case 2:
                new Alert(Alert.AlertType.INFORMATION, taskType + " cancelled " + errorMsg).showAndWait();
                break;
            case 3:
                new Alert(Alert.AlertType.ERROR, taskType + " failed " + errorMsg).showAndWait();
                break;
            default:
                new Alert(Alert.AlertType.ERROR, taskType + " experienced unknown problem " + errorMsg).showAndWait();
        }

        // unbind the progress bar from the progress source and reset the progress bar
        progressBar.progressProperty().unbind();
        progressBar.setProgress(0);
        progressBar.setVisible(false);

        // enable all necessary controls, assumption is that device is registered as if app is
        // backing up / restoring / syncing then a registered device had to be selected
        comboDevices.setDisable(false);
        btnBackup.setDisable(false);
        btnSync.setDisable(false);
        btnRestore.setDisable(false);
    }

    /**
     * Disables all the controls on the form, except for the exit button and view logs link
     */
    private void disableAllControls() {
        comboDevices.setDisable(true);
        btnRegisterDevice.setDisable(true);
        btnBackup.setDisable(true);
        btnSync.setDisable(true);
        btnRestore.setDisable(true);
    }

    /**
     * Syncs previously unsynced files (aka dirty files) to the sync folder
     * @return Event handler for syncing dirty files to device
     */
    private EventHandler<ActionEvent> sync() {
        return event -> {

            // wrap sync in a task to avoid app from looking like it has frozen
            // for detailed explanation view the backup function
            Task task = new Task<Void>() {
                @Override public Void call() throws IOException{
                    deviceManager.synchronise(getComboboxValue());
                    // just to fill up the progress bar
                    updateProgress(100, 100);
                    return null;
                }
            };

            // set what happens when the task completes
            // for detailed explanation view the backup function
            task.setOnSucceeded(evt -> taskStopped(task.getException(), "Sync", 1));
            task.setOnCancelled(evt -> taskStopped(task.getException(), "Sync", 2));
            task.setOnFailed(evt -> taskStopped(task.getException(), "Sync", 3));

            // disable all the controls before starting the tasks to prevent users
            // from launching multiple tasks together
            disableAllControls();

            // start the task thread
            // for detailed explanation view the backup function
            new Thread(task).start();

            // not the most detailed progress bar, but at-least gives users a sense of the application not stalling
            progressBar.setVisible(true);
            progressBar.progressProperty().bind(task.progressProperty());
        };
    }

    /**
     * Restores backup to a device
     * @return Event handler for restoring a backup
     */
    private EventHandler<ActionEvent> restore() {
        return event -> {

            // Create an alert for the type of retoration
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Choose restoration type.");
            alert.setTitle("Choose restore method");
            alert.setHeaderText("Do you want to restore from the last full snapshot, or from they sync folder?");

            ButtonType btnSyncBased = new ButtonType("Sync");
            ButtonType btnBackupBased = new ButtonType("Backup");

            alert.getButtonTypes().setAll(btnSyncBased, btnBackupBased);
            Optional<ButtonType> result = alert.showAndWait();

            // wrap sync in a task to avoid app from looking like it has frozen
            // for detailed explanation view the backup function
            Task task = new Task<Void>() {
                @Override public Void call() throws IOException{
                    if (result.get() == btnSyncBased) {
                        deviceManager.syncRestore(getComboboxValue());
                    } else if (result.get() == btnBackupBased) {
                        deviceManager.restore(getComboboxValue());
                    }
                    // just to fill up the progress bar
                    updateProgress(100, 100);
                    return null;
                }
            };

            // disable all the controls before starting the tasks to prevent users
            // from launching multiple tasks together
            disableAllControls();

            // set what happens when the task completes
            // for detailed explanation view the backup function
            task.setOnSucceeded(evt -> taskStopped(task.getException(), "Restore", 1));
            task.setOnCancelled(evt -> taskStopped(task.getException(), "Restore", 2));
            task.setOnFailed(evt -> taskStopped(task.getException(), "Restore", 3));

            // start the task thread
            // for detailed explanation view the backup function
            new Thread(task).start();

            // not the most detailed progress bar, but at-least gives users a sense of the application not stalling
            progressBar.setVisible(true);
            progressBar.progressProperty().bind(task.progressProperty());
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
     *       This is a known JDK8 bug that won't be fixed: https://bugs.openjdk.java.net/browse/JDK-8188899
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
     * Exits application
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
