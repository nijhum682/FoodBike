package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class RestaurantController {
    @FXML private TextField searchField;
    @FXML private ComboBox<String> divisionCombo;
    @FXML private GridPane restaurantsGrid;
    @FXML private Label userLabel;

    private DatabaseService databaseService;
    private User currentUser;
    private List<Restaurant> currentRestaurants;

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        loadDivisions();
        loadAllRestaurants();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("Welcome, " + user.getUsername() + " (" + user.getUserType().toString() + ")");
    }

    private void loadDivisions() {
        List<String> divisions = databaseService.getAllDivisions();
        divisionCombo.getItems().add("All");
        divisionCombo.getItems().addAll(divisions);
        divisionCombo.setValue("All");
    }

    private void loadAllRestaurants() {
        currentRestaurants = databaseService.getAllRestaurants();
        displayRestaurants(currentRestaurants);
    }

    private void displayRestaurants(List<Restaurant> restaurants) {
        restaurantsGrid.getChildren().clear();
        
        int row = 0, col = 0;
        for (Restaurant restaurant : restaurants) {
            VBox restaurantCard = createRestaurantCard(restaurant);
            restaurantsGrid.add(restaurantCard, col, row);
            
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createRestaurantCard(Restaurant restaurant) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 15; -fx-background-color: white; -fx-cursor: hand;");
        card.setSpacing(10);
        card.setPrefHeight(280);

        // Restaurant Name
        Label nameLabel = new Label(restaurant.getName());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);

        // Division
        Label divisionLabel = new Label("📍 " + restaurant.getDivision());
        divisionLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        // Rating
        Label ratingLabel = new Label("⭐ " + String.format("%.1f", restaurant.getRating()));
        ratingLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #f39c12;");

        // Address
        Label addressLabel = new Label(restaurant.getAddress());
        addressLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #95a5a6; -fx-wrap-text: true;");
        addressLabel.setWrapText(true);

        // View Menu Button
        Button viewMenuBtn = new Button("View Menu");
        viewMenuBtn.setStyle("-fx-padding: 10; -fx-font-size: 12; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;");
        viewMenuBtn.setMaxWidth(Double.MAX_VALUE);
        viewMenuBtn.setOnAction(e -> openMenuView(restaurant));

        card.getChildren().addAll(nameLabel, divisionLabel, ratingLabel, addressLabel, viewMenuBtn);
        card.setPadding(new Insets(15));

        return card;
    }

    private void openMenuView(Restaurant restaurant) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("menu-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 900, 600);
            Stage stage = new Stage();
            stage.setTitle("FoodBike - " + restaurant.getName() + " Menu");
            stage.setScene(scene);

            MenuController menuController = fxmlLoader.getController();
            menuController.setRestaurant(restaurant);
            menuController.setCurrentUser(currentUser);
            menuController.loadMenu();

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            loadAllRestaurants();
        } else {
            List<Restaurant> results = databaseService.searchRestaurants(query);
            displayRestaurants(results);
        }
    }

    @FXML
    public void handleFilter() {
        String division = divisionCombo.getValue();
        if (division == null || division.equals("All")) {
            loadAllRestaurants();
        } else {
            List<Restaurant> results = databaseService.getRestaurantsByDivision(division);
            displayRestaurants(results);
        }
    }

    @FXML
    public void handleClear() {
        searchField.clear();
        divisionCombo.setValue("All");
        loadAllRestaurants();
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
