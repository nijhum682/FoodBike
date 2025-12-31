package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;

public class SignInController {
    @FXML private RadioButton adminRadio;
    @FXML private RadioButton userRadio;
    @FXML private RadioButton entrepreneurRadio;
    @FXML private RadioButton bikerRadio;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button passwordToggleBtn;
    @FXML private Label errorMessage;

    private DatabaseService databaseService;
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        errorMessage.setText("");
    }

    @FXML
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            visiblePasswordField.setVisible(true);
            passwordToggleBtn.setText("︶");
        } else {
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setVisible(false);
            passwordField.setVisible(true);
            passwordToggleBtn.setText("👁");
        }
    }

    @FXML
    public void handleSignIn() {
        String username = usernameField.getText().trim();
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorMessage.setText("Please enter username and password");
            return;
        }

        User user = databaseService.loginUser(username, password);
        if (user != null) {
            User.UserType selectedType = getSelectedUserType();
            if (user.getUserType() == selectedType) {
                errorMessage.setText("");
                showSuccessMessage(user);
            } else {
                errorMessage.setText("Invalid user type for this account");
            }
        } else {
            errorMessage.setText("Invalid username or password");
        }
    }

    private void showSuccessMessage(User user) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Login Successful");
        alert.setHeaderText(null);
        alert.setContentText("✓ Your Login is Successful!\nWelcome back, " + user.getUsername() + "!");
        alert.showAndWait();
        openDashboard(user);
    }

    @FXML
    public void handleSignUpButton() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("signup-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 700);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle("FoodBike - Sign Up");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private User.UserType getSelectedUserType() {
        if (adminRadio.isSelected()) {
            return User.UserType.ADMIN;
        } else if (entrepreneurRadio.isSelected()) {
            return User.UserType.ENTREPRENEUR;
        } else if (bikerRadio.isSelected()) {
            return User.UserType.BIKER;
        } else {
            return User.UserType.USER;
        }
    }

    private void openDashboard(User user) {
        try {
            FXMLLoader fxmlLoader = null;
            String title = "";

            if (user.getUserType() == User.UserType.ADMIN) {
                fxmlLoader = new FXMLLoader(getClass().getResource("admin-view.fxml"));
                title = "FoodBike - Admin Dashboard";
            } else if (user.getUserType() == User.UserType.USER) {
                fxmlLoader = new FXMLLoader(getClass().getResource("restaurant-view.fxml"));
                title = "FoodBike - Restaurants";
            } else if (user.getUserType() == User.UserType.ENTREPRENEUR) {
                fxmlLoader = new FXMLLoader(getClass().getResource("entrepreneur-view.fxml"));
                title = "FoodBike - Entrepreneur Dashboard";
            } else if (user.getUserType() == User.UserType.BIKER) {
                fxmlLoader = new FXMLLoader(getClass().getResource("biker-view.fxml"));
                title = "FoodBike - Biker Dashboard";
            }

            Scene scene = new Scene(fxmlLoader.load(), 1000, 700);
            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setTitle(title);
            stage.setScene(scene);
            
            if (user.getUserType() == User.UserType.ADMIN) {
                AdminController controller = fxmlLoader.getController();
                controller.setCurrentUser(user);
            } else if (user.getUserType() == User.UserType.USER) {
                RestaurantController controller = fxmlLoader.getController();
                controller.setCurrentUser(user);
            } else if (user.getUserType() == User.UserType.ENTREPRENEUR) {
                EntrepreneurController controller = fxmlLoader.getController();
                controller.setCurrentUser(user);
            } else if (user.getUserType() == User.UserType.BIKER) {
                BikerController controller = fxmlLoader.getController();
                controller.setCurrentUser(user);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
