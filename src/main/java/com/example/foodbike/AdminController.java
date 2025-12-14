package com.example.foodbike;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AdminController {
    @FXML private TableView<Restaurant> restaurantsTable;
    @FXML private TableColumn<Restaurant, Integer> idColumn;
    @FXML private TableColumn<Restaurant, String> nameColumn;
    @FXML private TableColumn<Restaurant, String> divisionColumn;
    @FXML private TableColumn<Restaurant, String> addressColumn;
    @FXML private TableColumn<Restaurant, Double> ratingColumn;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> divisionCombo;

    private DatabaseService databaseService;
    private static int nextRestaurantId = 101;
    private List<Restaurant> allRestaurants;

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        setupDivisionCombo();
        setupTableColumns();
        loadRestaurants();
    }

    private void setupDivisionCombo() {
        divisionCombo.setItems(FXCollections.observableArrayList(
            "All", "Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh"
        ));
        divisionCombo.setValue("All");
    }

    private void setupTableColumns() {
        // Set up table columns to display restaurant properties
        idColumn = (TableColumn<Restaurant, Integer>) restaurantsTable.getColumns().get(0);
        nameColumn = (TableColumn<Restaurant, String>) restaurantsTable.getColumns().get(1);
        divisionColumn = (TableColumn<Restaurant, String>) restaurantsTable.getColumns().get(2);
        addressColumn = (TableColumn<Restaurant, String>) restaurantsTable.getColumns().get(3);
        ratingColumn = (TableColumn<Restaurant, Double>) restaurantsTable.getColumns().get(4);

        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        divisionColumn.setCellValueFactory(new PropertyValueFactory<>("division"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("address"));
        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
    }

    private void loadRestaurants() {
        allRestaurants = databaseService.getAllRestaurants();
        ObservableList<Restaurant> observableRestaurants = FXCollections.observableArrayList(allRestaurants);
        restaurantsTable.setItems(observableRestaurants);
        
        // Add delete button column
        addDeleteButtonColumn();
    }

    private void addDeleteButtonColumn() {
        // Remove existing delete column if any
        restaurantsTable.getColumns().removeIf(col -> col.getText().equals("Delete"));
        
        TableColumn<Restaurant, Void> deleteCol = new TableColumn<>("Delete");
        deleteCol.setPrefWidth(80);
        
        deleteCol.setCellFactory(col -> new TableCell<Restaurant, Void>() {
            private Button deleteBtn = new Button("Delete");

            {
                deleteBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 4;");
                deleteBtn.setOnAction(e -> {
                    Restaurant restaurant = getTableView().getItems().get(getIndex());
                    Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmAlert.setTitle("Confirm Delete");
                    confirmAlert.setHeaderText("Delete Restaurant?");
                    confirmAlert.setContentText("Are you sure you want to delete " + restaurant.getName() + "?");
                    
                    Optional<ButtonType> result = confirmAlert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        databaseService.deleteRestaurant(restaurant.getId());
                        loadRestaurants();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteBtn);
                }
            }
        });
        
        restaurantsTable.getColumns().add(deleteCol);
    }

    @FXML
    public void handleSearch() {
        String searchText = searchField.getText().toLowerCase().trim();
        
        if (searchText.isEmpty()) {
            loadRestaurants();
            return;
        }
        
        List<Restaurant> filteredList = allRestaurants.stream()
            .filter(r -> r.getName().toLowerCase().contains(searchText) || 
                        r.getAddress().toLowerCase().contains(searchText))
            .collect(Collectors.toList());
        
        ObservableList<Restaurant> observableList = FXCollections.observableArrayList(filteredList);
        restaurantsTable.setItems(observableList);
        addDeleteButtonColumn();
    }

    @FXML
    public void handleFilter() {
        String selectedDivision = divisionCombo.getValue();
        
        if (selectedDivision == null || selectedDivision.equals("All")) {
            loadRestaurants();
            return;
        }
        
        List<Restaurant> filteredList = allRestaurants.stream()
            .filter(r -> r.getDivision().equals(selectedDivision))
            .collect(Collectors.toList());
        
        ObservableList<Restaurant> observableList = FXCollections.observableArrayList(filteredList);
        restaurantsTable.setItems(observableList);
        addDeleteButtonColumn();
    }

    @FXML
    public void handleClear() {
        searchField.clear();
        divisionCombo.setValue("All");
        loadRestaurants();
    }

    @FXML
    public void handleAddRestaurant() {
        Dialog<Restaurant> dialog = new Dialog<>();
        dialog.setTitle("Add New Restaurant");
        dialog.setHeaderText("Enter Restaurant Details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Restaurant Name");
        nameField.setPrefWidth(250);
        
        ComboBox<String> divisionComboAdd = new ComboBox<>();
        divisionComboAdd.setItems(FXCollections.observableArrayList(
            "Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh"
        ));
        divisionComboAdd.setPrefWidth(250);
        
        TextField addressField = new TextField();
        addressField.setPromptText("Restaurant Address");
        addressField.setPrefWidth(250);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Division:"), 0, 1);
        grid.add(divisionComboAdd, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                String division = divisionComboAdd.getValue();
                String address = addressField.getText().trim();
                
                if (name.isEmpty()) {
                    showAlert("Validation Error", "Missing Data", "Please enter restaurant name.");
                    return null;
                }
                if (division == null || division.isEmpty()) {
                    showAlert("Validation Error", "Missing Data", "Please select a division.");
                    return null;
                }
                if (address.isEmpty()) {
                    showAlert("Validation Error", "Missing Data", "Please enter restaurant address.");
                    return null;
                }
                
                Restaurant newRestaurant = new Restaurant(
                    String.valueOf(nextRestaurantId++),
                    name,
                    division,
                    address
                );
                return newRestaurant;
            }
            return null;
        });
        
        Optional<Restaurant> result = dialog.showAndWait();
        result.ifPresent(restaurant -> {
            if (databaseService.addRestaurant(restaurant)) {
                loadRestaurants();
                showAlert("Success", "Restaurant Added", "Restaurant '" + restaurant.getName() + "' has been added successfully!");
            } else {
                showAlert("Error", "Failed to Add Restaurant", "Could not add the restaurant. Please try again.");
            }
        });
    }

    @FXML
    public void handleViewRestaurants() {
        loadRestaurants();
    }

    @FXML
    public void handleLogout() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("signin-view.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 600, 700);
            Stage stage = (Stage) restaurantsTable.getScene().getWindow();
            stage.setTitle("FoodBike - Sign In");
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
