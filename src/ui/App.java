package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Inheritance, App inherts from JavaFX's Application
 * This is the entry point of the application
 */
public class App extends Application {

    /**
     * The start function is part of JavaFX's Application class. As it's abstract it has to be overriden
     * The function starts the UI application.
     * @param primaryStage - Initial application stage
     */
    @Override
    public void start(Stage primaryStage) {

        Scene auth = new AuthenticationForm(primaryStage).getScene();

        primaryStage.setTitle("Backup App");
        primaryStage.setScene(auth);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    /**
     * Main function for application, launches JavaFX application object
     * @param args Arguments to main
     */
    public static void main(String[] args) {
        launch(args);
    }
}
