package org.example.event;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import utils.MyDataBase;
import utils.Router;

import java.sql.Connection;

public class Main extends Application {

    public static void main(String[] args) {
        Connection connection = MyDataBase.getInstance().getConnection();
        System.out.println("connecté à la base de données");
        System.out.println(connection);
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Router.setMainStage(primaryStage);
        Router.navigateTo("/login.fxml");

        Scene scene = primaryStage.getScene();
        scene.getStylesheets().add(
                getClass().getResource("/css/styles.css").toExternalForm()
        );
        primaryStage.setTitle("TawwDons - Donation Management System");
        primaryStage.show();
    }
}