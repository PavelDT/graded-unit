package ui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

// Inheretance, App inhertis from JavaFX's Application
public class App extends Application {

    /**
     * The start function is part of JavaFX's Application class. As it's abstract it has to be overriden
     * The function starts the UI application.
     * @param primaryStage
     */
    @Override
    public void start(Stage primaryStage) {

        Scene auth = new AuthenticationForm(primaryStage).getScene();

        primaryStage.setTitle("Backup App");
        primaryStage.setScene(auth);
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
