package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EntrepreneurController {
    @FXML private TableView<Order> pendingOrdersTable;
    @FXML private TableView<Order> confirmedOrdersTable;
    @FXML private Label userLabel;
    @FXML private VBox restaurantInfoBox;
    @FXML private GridPane menuBox;
    @FXML private Label restaurantNameLabel;

    private User currentUser;
    private DatabaseService databaseService;
    private Restaurant myRestaurant;

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        setupOrderTables();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("Welcome, " + user.getUsername());
        loadMyRestaurant();
        loadOrders();
    }
    
    private void loadMyRestaurant() {
        if (restaurantInfoBox == null || menuBox == null) {
            return;
        }
        
        restaurantInfoBox.getChildren().clear();
        menuBox.getChildren().clear();
        
        // Find approved restaurant for this entrepreneur
        List<RestaurantApplication> applications = databaseService.getEntrepreneurApplications(currentUser.getUsername());
        String approvedRestaurantName = null;
        
        for (RestaurantApplication app : applications) {
            if (app.getStatus() == RestaurantApplication.ApplicationStatus.APPROVED) {
                approvedRestaurantName = app.getRestaurantName();
                break;
            }
        }
        
        if (approvedRestaurantName != null) {
            // Find the restaurant in the database
            for (Restaurant restaurant : databaseService.getAllRestaurants()) {
                if (restaurant.getName().equals(approvedRestaurantName)) {
                    myRestaurant = restaurant;
                    displayRestaurantInfo(restaurant);
                    displayMenu(restaurant);
                    break;
                }
            }
        } else {
            // No approved restaurant
            restaurantInfoBox.setVisible(false);
            restaurantInfoBox.setManaged(false);
        }
    }
    
    private void displayRestaurantInfo(Restaurant restaurant) {
        restaurantInfoBox.setVisible(true);
        restaurantInfoBox.setManaged(true);
        
        VBox infoCard = new VBox(8);
        infoCard.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 6; -fx-padding: 15; -fx-background-color: #d4edda;");
        
        Label nameLabel = new Label("✓ " + restaurant.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #155724;");
        
        Label locationLabel = new Label("📍 " + restaurant.getDivision() + " - " + restaurant.getAddress());
        locationLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #155724;");
        
        Label ratingLabel = new Label("⭐ Rating: " + restaurant.getRating() + "/5.0");
        ratingLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #155724; -fx-font-weight: bold;");
        
        Label idLabel = new Label("ID: " + restaurant.getId());
        idLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6c757d; -fx-font-style: italic;");
        
        infoCard.getChildren().addAll(nameLabel, locationLabel, ratingLabel, idLabel);
        restaurantInfoBox.getChildren().add(infoCard);
        
        if (restaurantNameLabel != null) {
            restaurantNameLabel.setText("My Restaurant: " + restaurant.getName());
        }
    }
    
    private void displayMenu(Restaurant restaurant) {
        menuBox.getChildren().clear();
        
        if (restaurant.getMenu().isEmpty()) {
            Label emptyLabel = new Label("No menu items available. Contact admin to add menu items.");
            emptyLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d; -fx-padding: 10;");
            menuBox.add(emptyLabel, 0, 0);
            return;
        }
        
        int row = 0, col = 0;
        for (MenuItem item : restaurant.getMenu()) {
            VBox menuCard = new VBox();
            menuCard.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 12; -fx-background-color: white;");
            menuCard.setSpacing(8);
            menuCard.setPrefWidth(200);
            
            Label nameLabel = new Label(item.getName());
            nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            nameLabel.setWrapText(true);
            
            Label descLabel = new Label(item.getDescription());
            descLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");
            descLabel.setWrapText(true);
            
            Label priceLabel = new Label("৳" + String.format("%.2f", item.getPrice()));
            priceLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
            
            menuCard.getChildren().addAll(nameLabel, descLabel, priceLabel);
            menuBox.add(menuCard, col, row);
            
            col++;
            if (col == 4) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    public void handleApplyRestaurant() {
        Dialog<RestaurantApplication> dialog = new Dialog<>();
        dialog.setTitle("Apply for Restaurant");
        dialog.setHeaderText("Submit Restaurant Application");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Restaurant Name");
        nameField.setPrefWidth(250);
        
        ComboBox<String> divisionCombo = new ComboBox<>();
        divisionCombo.getItems().addAll("Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh");
        divisionCombo.setPrefWidth(250);
        
        TextField addressField = new TextField();
        addressField.setPromptText("Address");
        addressField.setPrefWidth(250);
        
        Spinner<Double> ratingSpinner = new Spinner<>(0.0, 5.0, 4.5, 0.1);
        ratingSpinner.setEditable(true);
        ratingSpinner.setPrefWidth(250);
        
        VBox menuItemsBox = new VBox(10);
        menuItemsBox.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: #f9f9f9;");
        
        Label menuLabel = new Label("Menu Items (Required):");
        menuLabel.setStyle("-fx-font-weight: bold;");
        
        Button addMenuItemBtn = new Button("+ Add Menu Item");
        addMenuItemBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 4;");
        
        VBox menuItemsContainer = new VBox(5);
        List<MenuItem> menuItems = new ArrayList<>();
        
        addMenuItemBtn.setOnAction(e -> {
            VBox itemBox = createMenuItemBox(menuItemsContainer, menuItems);
            menuItemsContainer.getChildren().add(itemBox);
        });
        
        ScrollPane menuScrollPane = new ScrollPane(menuItemsContainer);
        menuScrollPane.setPrefHeight(200);
        menuScrollPane.setFitToWidth(true);
        menuScrollPane.setStyle("-fx-background-color: transparent;");
        
        menuItemsBox.getChildren().addAll(menuLabel, addMenuItemBtn, menuScrollPane);
        
        grid.add(new Label("Restaurant Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Division:"), 0, 1);
        grid.add(divisionCombo, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(new Label("Rating (0-5):"), 0, 3);
        grid.add(ratingSpinner, 1, 3);
        grid.add(menuItemsBox, 0, 4, 2, 1);
        
        ScrollPane mainScrollPane = new ScrollPane(grid);
        mainScrollPane.setFitToWidth(true);
        mainScrollPane.setFitToHeight(true);
        mainScrollPane.setPrefHeight(500);
        mainScrollPane.setStyle("-fx-background-color: transparent;");
        
        dialog.getDialogPane().setContent(mainScrollPane);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                String division = divisionCombo.getValue();
                String address = addressField.getText().trim();
                Double rating = ratingSpinner.getValue();
                
                if (name.isEmpty() || division == null || address.isEmpty() || menuItems.isEmpty()) {
                    showAlert("Validation Error", "Missing Data", "All fields including at least one menu item are required.");
                    return null;
                }
                
                String appId = "APP_" + System.currentTimeMillis();
                RestaurantApplication application = new RestaurantApplication(
                    appId, currentUser.getUsername(), name, division, address, rating);
                
                for (MenuItem item : menuItems) {
                    application.addMenuItem(item);
                }
                
                return application;
            }
            return null;
        });
        
        Optional<RestaurantApplication> result = dialog.showAndWait();
        result.ifPresent(application -> {
            databaseService.submitApplication(application);
            showAlert("Success", "Application Submitted", "Your restaurant application has been submitted successfully. Please wait for admin approval.");
        });
    }

    private VBox createMenuItemBox(VBox container, List<MenuItem> menuItems) {
        VBox itemBox = new VBox(8);
        itemBox.setStyle("-fx-border-color: #ccc; -fx-border-radius: 3; -fx-padding: 8; -fx-background-color: white;");
        
        TextField itemNameField = new TextField();
        itemNameField.setPromptText("Item Name");
        
        TextField itemDescField = new TextField();
        itemDescField.setPromptText("Description");
        
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 10000.0, 100.0, 10.0);
        priceSpinner.setEditable(true);
        
        HBox buttonBox = new HBox(10);
        Button saveBtn = new Button("Save");
        saveBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 4;");
        
        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 4;");
        
        saveBtn.setOnAction(e -> {
            String name = itemNameField.getText().trim();
            String desc = itemDescField.getText().trim();
            Double price = priceSpinner.getValue();
            
            if (name.isEmpty() || desc.isEmpty() || price <= 0) {
                showAlert("Validation Error", "Invalid Input", "Please fill all menu item fields correctly.");
                return;
            }
            
            String itemId = "item_" + System.currentTimeMillis();
            MenuItem item = new MenuItem(itemId, name, desc, price);
            menuItems.add(item);
            
            itemNameField.clear();
            itemDescField.clear();
            priceSpinner.getValueFactory().setValue(100.0);
        });
        
        removeBtn.setOnAction(e -> container.getChildren().remove(itemBox));
        
        buttonBox.getChildren().addAll(saveBtn, removeBtn);
        itemBox.getChildren().addAll(
            new Label("Item Name:"), itemNameField,
            new Label("Description:"), itemDescField,
            new Label("Price (৳):"), priceSpinner,
            buttonBox
        );
        
        return itemBox;
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
    
    @FXML
    public void handleRefreshOrders() {
        loadOrders();
        showAlert("Refreshed", "Orders Updated", "Order list has been refreshed successfully!");
    }

    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private void setupOrderTables() {
        pendingOrdersTable.getColumns().clear();
        confirmedOrdersTable.getColumns().clear();
        
        setupPendingOrdersTable();
        setupConfirmedOrdersTable();
    }
    
    private void setupPendingOrdersTable() {
        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrderId()));
        orderIdCol.setPrefWidth(150);
        
        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserId()));
        customerCol.setPrefWidth(150);
        
        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(data -> {
            int itemCount = data.getValue().getItems().size();
            return new javafx.beans.property.SimpleStringProperty(itemCount + " item(s)");
        });
        itemsCol.setPrefWidth(100);
        
        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("৳" + String.format("%.2f", data.getValue().getTotalPrice())));
        totalCol.setPrefWidth(100);
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));
        statusCol.setPrefWidth(100);
        
        TableColumn<Order, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button approveBtn = new Button("Approve");
            
            {
                approveBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 4;");
                approveBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    approveOrder(order);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(approveBtn);
                }
            }
        });
        
        pendingOrdersTable.getColumns().addAll(orderIdCol, customerCol, itemsCol, totalCol, statusCol, actionCol);
    }
    
    private void setupConfirmedOrdersTable() {
        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrderId()));
        orderIdCol.setPrefWidth(150);
        
        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserId()));
        customerCol.setPrefWidth(150);
        
        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(data -> {
            int itemCount = data.getValue().getItems().size();
            return new javafx.beans.property.SimpleStringProperty(itemCount + " item(s)");
        });
        itemsCol.setPrefWidth(150);
        
        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("৳" + String.format("%.2f", data.getValue().getTotalPrice())));
        totalCol.setPrefWidth(100);
        
        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));
        statusCol.setPrefWidth(150);
        
        confirmedOrdersTable.getColumns().addAll(orderIdCol, customerCol, itemsCol, totalCol, statusCol);
    }
    
    private void loadOrders() {
        if (myRestaurant == null) {
            return;
        }
        
        List<Order> allOrders = databaseService.getRestaurantOrders(myRestaurant.getId());
        
        List<Order> pendingOrders = new ArrayList<>();
        List<Order> confirmedOrders = new ArrayList<>();
        
        for (Order order : allOrders) {
            if (order.getStatus() == Order.OrderStatus.PENDING) {
                pendingOrders.add(order);
            } else {
                confirmedOrders.add(order);
            }
        }
        
        pendingOrdersTable.getItems().setAll(pendingOrders);
        confirmedOrdersTable.getItems().setAll(confirmedOrders);
    }
    
    private void approveOrder(Order order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Approve Order");
        confirmAlert.setHeaderText("Approve Order: " + order.getOrderId());
        confirmAlert.setContentText("Are you sure you want to approve this order?");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            order.setStatus(Order.OrderStatus.CONFIRMED);
            databaseService.saveDataToFiles();
            loadOrders();
            showAlert("Success", "Order Approved", "Order has been confirmed successfully!");
        }
    }
}
