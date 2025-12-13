package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.regex.Pattern;

public class SignUpController {
    @FXML private RadioButton adminTypeRadio;
    @FXML private RadioButton userTypeRadio;
    @FXML private RadioButton entrepreneurTypeRadio;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField visiblePasswordField;
    @FXML private Button passwordToggleBtn;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField visibleConfirmPasswordField;
    @FXML private Button confirmPasswordToggleBtn;
    @FXML private Label emailError;
    @FXML private Label phoneError;
    @FXML private Label usernameError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label successMessage;

    private DatabaseService databaseService;
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        clearErrors();
    }

    @FXML
    public void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setManaged(false);
            passwordField.setVisible(false);
            visiblePasswordField.setManaged(true);
            visiblePasswordField.setVisible(true);
            passwordToggleBtn.setText("🙈");
        } else {
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setManaged(false);
            visiblePasswordField.setVisible(false);
            passwordField.setManaged(true);
            passwordField.setVisible(true);
            passwordToggleBtn.setText("👁");
        }
    }

    @FXML
    public void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;
        if (confirmPasswordVisible) {
            visibleConfirmPasswordField.setText(confirmPasswordField.getText());
            confirmPasswordField.setManaged(false);
            confirmPasswordField.setVisible(false);
            visibleConfirmPasswordField.setManaged(true);
            visibleConfirmPasswordField.setVisible(true);
            confirmPasswordToggleBtn.setText("🙈");
        } else {
            confirmPasswordField.setText(visibleConfirmPasswordField.getText());
            visibleConfirmPasswordField.setManaged(false);
            visibleConfirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(true);
            confirmPasswordField.setVisible(true);
            confirmPasswordToggleBtn.setText("👁");
        }
    }

    @FXML
    public void handleSignUp() {
        clearErrors();
        boolean isValid = true;

        // Validate Email
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            emailError.setText("Email is required");
            isValid = false;
        } else if (!isValidEmail(email)) {
            emailError.setText("Invalid email format");
            isValid = false;
        }

        // Validate Phone Number
        String phone = phoneField.getText().trim();
        if (phone.isEmpty()) {
            phoneError.setText("Phone number is required");
            isValid = false;
        } else if (!isValidPhone(phone)) {
            phoneError.setText("Phone number must be 10-15 digits");
            isValid = false;
        }

        // Validate Username
        String username = usernameField.getText().trim();
        if (username.isEmpty()) {
            usernameError.setText("Username is required");
            isValid = false;
        } else if (databaseService.userExists(username)) {
            usernameError.setText("Username already exists");
            isValid = false;
        } else if (username.length() < 3) {
            usernameError.setText("Username must be at least 3 characters");
            isValid = false;
        }

        // Validate Password
        String password = passwordVisible ? visiblePasswordField.getText() : passwordField.getText();
        if (password.isEmpty()) {
            passwordError.setText("Password is required");
            isValid = false;
        } else {
            String passwordValidation = validatePassword(password);
            if (!passwordValidation.isEmpty()) {
                passwordError.setText(passwordValidation);
                isValid = false;
            }
        }

        // Validate Confirm Password
        String confirmPassword = confirmPasswordVisible ? visibleConfirmPasswordField.getText() : confirmPasswordField.getText();
        if (confirmPassword.isEmpty()) {
            confirmPasswordError.setText("Please confirm your password");
            isValid = false;
        } else if (!password.equals(confirmPassword)) {
            confirmPasswordError.setText("Passwords do not match");
            isValid = false;
        }

        if (isValid) {
            // Register user
            User.UserType userType = getSelectedAccountType();
            boolean registered = databaseService.registerUser(username, email, phone, password, userType);
            
            if (registered) {
                successMessage.setText("✓ Your Sign Up is Successful! Redirecting to Sign In...");
                successMessage.setStyle("-fx-text-fill: #27ae60; -fx-font-size: 12; -fx-font-weight: bold;");
                
                // Redirect to Sign In page after 2 seconds
                javafx.application.Platform.runLater(() -> {
                    try {
                        Thread.sleep(2000);
                        handleSignInButton();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
            }
        }
    }

    private User.UserType getSelectedAccountType() {
        if (adminTypeRadio.isSelected()) {
            return User.UserType.ADMIN;
        } else if (entrepreneurTypeRadio.isSelected()) {
            return User.UserType.ENTREPRENEUR;
        } else {
            return User.UserType.USER;
        }
    }

    private String validatePassword(String password) {
        if (password.length() < 6) {
            return "At least 6 characters";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "Must contain uppercase letters";
        }
        if (!password.matches(".*[a-z].*")) {
            return "Must contain lowercase letters";
        }
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>?].*")) {
            return "Must contain special symbols";
        }
        return "";
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private boolean isValidPhone(String phone) {
        return phone.matches("\\d{10,15}");
    }

    private void clearErrors() {
        emailError.setText("");
        phoneError.setText("");
        usernameError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        successMessage.setText("");
    }

    @FXML
    public void handleSignInButton() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("signin-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 700);
            Stage stage = (Stage) emailField.getScene().getWindow();
            stage.setTitle("FoodBike - Sign In");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
