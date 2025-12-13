package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

import java.io.IOException;

public class EntrepreneurController {
    @FXML private TableView<?> pendingOrdersTable;
    @FXML private TableView<?> confirmedOrdersTable;
    @FXML private Label userLabel;

    private DatabaseService databaseService;

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("signin-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 700);
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setTitle("FoodBike - Sign In");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
