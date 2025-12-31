package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
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
    @FXML private Button applyRestaurantButton;

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
                    if (applyRestaurantButton != null) {
                        applyRestaurantButton.setVisible(false);
                        applyRestaurantButton.setManaged(false);
                    }
                    break;
                }
            }
        } else {
            // No approved restaurant
            restaurantInfoBox.setVisible(false);
            restaurantInfoBox.setManaged(false);
            if (applyRestaurantButton != null) {
                applyRestaurantButton.setVisible(true);
                applyRestaurantButton.setManaged(true);
            }
        }
    }
    
    private void displayRestaurantInfo(Restaurant restaurant) {
        restaurantInfoBox.setVisible(true);
        restaurantInfoBox.setManaged(true);
        
        HBox headerBox = new HBox();
        headerBox.setStyle("-fx-alignment: center-left;");
        
        Label titleLabel = new Label("My Restaurant");
        titleLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button totalBalanceBtn = new Button("Total Balance");
        totalBalanceBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #f39c12; -fx-text-fill: white; -fx-border-radius: 4;");
        totalBalanceBtn.setOnAction(e -> handleTotalBalance());
        
        headerBox.getChildren().addAll(titleLabel, spacer, totalBalanceBtn);
        
        VBox infoCard = new VBox(8);
        infoCard.setStyle("-fx-border-color: #27ae60; -fx-border-width: 2; -fx-border-radius: 6; -fx-padding: 15; -fx-background-color: #d4edda; -fx-alignment: center;");
        
        Label nameLabel = new Label("✓ " + restaurant.getName());
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #155724;");
        
        Label locationLabel = new Label("📍 " + restaurant.getDivision() + " - " + restaurant.getAddress());
        locationLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #155724;");
        
        Label ratingLabel = new Label("⭐ Rating: " + restaurant.getRating() + "/5.0");
        ratingLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #155724; -fx-font-weight: bold;");
        
        Label idLabel = new Label("ID: " + restaurant.getId());
        idLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #6c757d; -fx-font-style: italic;");
        
        infoCard.getChildren().addAll(nameLabel, locationLabel, ratingLabel, idLabel);
        restaurantInfoBox.getChildren().addAll(headerBox, infoCard);
        
        if (restaurantNameLabel != null) {
            restaurantNameLabel.setText("My Restaurant: " + restaurant.getName());
        }
    }
    
    private void displayMenu(Restaurant restaurant) {
        menuBox.getChildren().clear();
        
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
            
            HBox actionBox = new HBox(5);
            Button editBtn = new Button("Edit");
            editBtn.setStyle("-fx-padding: 4 8; -fx-font-size: 10; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 3;");
            editBtn.setOnAction(e -> handleEditMenuItem(item));
            
            Button deleteBtn = new Button("Delete");
            deleteBtn.setStyle("-fx-padding: 4 8; -fx-font-size: 10; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 3;");
            deleteBtn.setOnAction(e -> handleDeleteMenuItem(item));
            
            actionBox.getChildren().addAll(editBtn, deleteBtn);
            
            menuCard.getChildren().addAll(nameLabel, descLabel, priceLabel, actionBox);
            menuBox.add(menuCard, col, row);
            
            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    @FXML
    public void handleApplyRestaurant() {
        List<RestaurantApplication> existingApps = databaseService.getEntrepreneurApplications(currentUser.getUsername());
        for (RestaurantApplication app : existingApps) {
            if (app.getStatus() == RestaurantApplication.ApplicationStatus.PENDING ||
                app.getStatus() == RestaurantApplication.ApplicationStatus.APPROVED) {
                showAlert("Application Exists", "Already Applied", "You already have a restaurant application. You can only apply for one restaurant.");
                return;
            }
        }

        Dialog<RestaurantApplication> dialog = new Dialog<>();
        dialog.setTitle("Apply for Restaurant");
        dialog.setHeaderText("Submit Restaurant Application");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Restaurant Name (e.g., Pakghor, Swaad Kutir, Amader Rannaghor, Khana Khazana)");
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

    @FXML
    public void handleTotalBalance() {
        if (myRestaurant == null) {
            showAlert("No Restaurant", "Error", "You don't have an approved restaurant yet.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Total Balance");
        dialog.setHeaderText("Payment Transactions");

        VBox content = new VBox();
        content.setSpacing(15);
        content.setPadding(new Insets(20));

        TableView<Order> balanceTable = new TableView<>();
        balanceTable.setPrefHeight(400);
        balanceTable.setPrefWidth(700);

        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrderId()));
        orderIdCol.setPrefWidth(150);

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserId()));
        customerCol.setPrefWidth(120);

        TableColumn<Order, String> paymentCol = new TableColumn<>("Payment Method");
        paymentCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getPaymentMethod()));
        paymentCol.setPrefWidth(150);

        TableColumn<Order, String> amountCol = new TableColumn<>("Amount");
        amountCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("৳" + String.format("%.2f", data.getValue().getTotalPrice())));
        amountCol.setPrefWidth(120);

        TableColumn<Order, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus().toString()));
        statusCol.setPrefWidth(120);

        balanceTable.getColumns().addAll(orderIdCol, customerCol, paymentCol, amountCol, statusCol);

        List<Order> payments = new ArrayList<>();
        double totalBalance = 0.0;

        List<Order> restaurantOrders = databaseService.getRestaurantOrders(myRestaurant.getId());
        for (Order order : restaurantOrders) {
            String payment = order.getPaymentMethod();
            if (payment != null && (payment.equals("Bkash") || payment.equals("Nagad") || 
                (payment.equals("Cash on Delivery") && order.getStatus() == Order.OrderStatus.DELIVERED))) {
                payments.add(order);
                totalBalance += order.getTotalPrice();
            }
        }

        balanceTable.getItems().setAll(payments);

        Label totalLabel = new Label("Total Balance: ৳" + String.format("%.2f", totalBalance));
        totalLabel.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-padding: 10;");

        content.getChildren().addAll(balanceTable, totalLabel);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
    }

    @FXML
    public void handleAddMenuItem() {
        if (myRestaurant == null) {
            showAlert("No Restaurant", "Error", "You don't have an approved restaurant yet.");
            return;
        }

        Dialog<MenuItem> dialog = new Dialog<>();
        dialog.setTitle("Add Menu Item");
        dialog.setHeaderText("Add New Item to Menu");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Item Name");

        TextField descField = new TextField();
        descField.setPromptText("Description");

        Spinner<Double> priceSpinner = new Spinner<>(0.0, 10000.0, 100.0, 10.0);
        priceSpinner.setEditable(true);

        grid.add(new Label("Item Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Price (৳):"), 0, 2);
        grid.add(priceSpinner, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                Double price = priceSpinner.getValue();

                if (name.isEmpty() || desc.isEmpty() || price <= 0) {
                    showAlert("Validation Error", "Invalid Input", "Please fill all fields correctly.");
                    return null;
                }

                String itemId = "item_" + System.currentTimeMillis();
                return new MenuItem(itemId, name, desc, price);
            }
            return null;
        });

        Optional<MenuItem> result = dialog.showAndWait();
        result.ifPresent(menuItem -> {
            myRestaurant.addMenuItem(menuItem);
            databaseService.saveDataToFiles();
            displayMenu(myRestaurant);
            showAlert("Success", "Item Added", "Menu item added successfully!");
        });
    }

    private void handleEditMenuItem(MenuItem item) {
        Dialog<MenuItem> dialog = new Dialog<>();
        dialog.setTitle("Edit Menu Item");
        dialog.setHeaderText("Update Menu Item");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField(item.getName());
        TextField descField = new TextField(item.getDescription());
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 10000.0, item.getPrice(), 10.0);
        priceSpinner.setEditable(true);

        grid.add(new Label("Item Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Price (৳):"), 0, 2);
        grid.add(priceSpinner, 1, 2);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                String desc = descField.getText().trim();
                Double price = priceSpinner.getValue();

                if (name.isEmpty() || desc.isEmpty() || price <= 0) {
                    showAlert("Validation Error", "Invalid Input", "Please fill all fields correctly.");
                    return null;
                }

                return new MenuItem(item.getId(), name, desc, price);
            }
            return null;
        });

        Optional<MenuItem> result = dialog.showAndWait();
        result.ifPresent(updatedItem -> {
            myRestaurant.getMenu().remove(item);
            myRestaurant.addMenuItem(updatedItem);
            databaseService.saveDataToFiles();
            displayMenu(myRestaurant);
            showAlert("Success", "Item Updated", "Menu item updated successfully!");
        });
    }

    private void handleDeleteMenuItem(MenuItem item) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Delete Menu Item");
        confirmAlert.setHeaderText("Delete: " + item.getName());
        confirmAlert.setContentText("Are you sure you want to delete this menu item?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            myRestaurant.getMenu().remove(item);
            databaseService.saveDataToFiles();
            displayMenu(myRestaurant);
            showAlert("Success", "Item Deleted", "Menu item deleted successfully!");
        }
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
        statusCol.setCellFactory(col -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    if (order.getStatus() == Order.OrderStatus.AUTO_CANCELLED) {
                        setText("✗ " + status);
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (order.getStatus() == Order.OrderStatus.CANCELLED) {
                        setText("✗ " + status);
                        setStyle("-fx-text-fill: #e74c3c;");
                    } else {
                        setText(status);
                        setStyle("");
                    }
                }
            }
        });
        
        TableColumn<Order, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button statusBtn = new Button("Order Status");
            
            {
                statusBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 4;");
                statusBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleOrderStatus(order);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(statusBtn);
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
        statusCol.setCellFactory(col -> new TableCell<Order, String>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    if (order.getStatus() == Order.OrderStatus.AUTO_CANCELLED) {
                        setText("✗ " + status);
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    } else if (order.getStatus() == Order.OrderStatus.CANCELLED) {
                        setText("✗ " + status);
                        setStyle("-fx-text-fill: #e74c3c;");
                    } else if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                        setText("✓✓ " + status);
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
                        setText("✓ " + status);
                        setStyle("-fx-text-fill: #27ae60;");
                    } else {
                        setText(status);
                        setStyle("");
                    }
                }
            }
        });
        
        TableColumn<Order, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<Order, Void>() {
            private final Button statusBtn = new Button("Order Status");
            
            {
                statusBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 4;");
                statusBtn.setOnAction(e -> {
                    Order order = getTableView().getItems().get(getIndex());
                    handleOrderStatus(order);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Order order = getTableView().getItems().get(getIndex());
                    if (order.getStatus() == Order.OrderStatus.CONFIRMED || 
                        order.getStatus() == Order.OrderStatus.READY ||
                        order.getStatus() == Order.OrderStatus.DELIVERED) {
                        setGraphic(statusBtn);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        });
        
        confirmedOrdersTable.getColumns().addAll(orderIdCol, customerCol, itemsCol, totalCol, statusCol, actionCol);
    }
    
    private void loadOrders() {
        if (myRestaurant == null) {
            return;
        }
        
        List<Order> allOrders = databaseService.getRestaurantOrders(myRestaurant.getId());
        
        // Auto-cancel pending orders older than 5 hours
        boolean hasAutoCancelled = false;
        for (Order order : allOrders) {
            if (order.shouldAutoCancelled()) {
                order.setStatus(Order.OrderStatus.AUTO_CANCELLED);
                hasAutoCancelled = true;
            }
        }
        if (hasAutoCancelled) {
            databaseService.saveDataToFiles();
        }
        
        List<Order> pendingOrders = new ArrayList<>();
        List<Order> confirmedOrders = new ArrayList<>();
        
        for (Order order : allOrders) {
            if (order.getStatus() == Order.OrderStatus.PENDING) {
                pendingOrders.add(order);
            } else {
                confirmedOrders.add(order);
            }
        }
        
        // Sort by creation date (newest first)
        pendingOrders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        confirmedOrders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        
        pendingOrdersTable.getItems().setAll(pendingOrders);
        confirmedOrdersTable.getItems().setAll(confirmedOrders);
        
        // Refresh tables
        pendingOrdersTable.refresh();
        confirmedOrdersTable.refresh();
    }
    
    private void handleOrderStatus(Order order) {
        Dialog<ButtonType> statusDialog = new Dialog<>();
        statusDialog.setTitle("Order Status Management");
        statusDialog.setHeaderText("Manage Order: " + order.getOrderId());
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");
        
        Label customerLabel = new Label("Customer: " + order.getUserId());
        customerLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");
        
        Label totalLabel = new Label("Total: ৳" + String.format("%.2f", order.getTotalPrice()));
        totalLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        VBox stagesBox = new VBox();
        stagesBox.setSpacing(8);
        stagesBox.setStyle("-fx-padding: 12; -fx-background-color: #f9f9f9; -fx-border-radius: 6; -fx-border-color: #e0e0e0;");
        
        Label stagesTitle = new Label("Order Stages:");
        stagesTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        stagesBox.getChildren().add(stagesTitle);
        
        String[] stages = {
            "✓ Order is placed",
            "🍽 Order is confirmed by restaurant",
            "🚴 Biker is on the way to deliver your order",
            "📦 Delivery is completed"
        };
        
        Order.OrderStatus[] stageStatuses = {
            Order.OrderStatus.PENDING,
            Order.OrderStatus.CONFIRMED,
            Order.OrderStatus.READY,
            Order.OrderStatus.DELIVERED
        };
        
        for (int i = 0; i < stages.length; i++) {
            HBox stageRow = new HBox();
            stageRow.setSpacing(10);
            stageRow.setStyle("-fx-padding: 8; -fx-alignment: center-left;");
            
            Label stageLabel = new Label(stages[i]);
            String isCompleted = getStageStatus(order.getStatus(), stageStatuses[i]);
            if (isCompleted.equals("completed")) {
                stageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else if (isCompleted.equals("current")) {
                stageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #3498db; -fx-font-weight: bold;");
            } else if (isCompleted.equals("cancelled")) {
                stageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                stageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #95a5a6;");
            }
            
            Region stageSpacer = new Region();
            HBox.setHgrow(stageSpacer, Priority.ALWAYS);
            
            Label tickBox = new Label();
            if (isCompleted.equals("completed")) {
                tickBox.setText("☑");
                tickBox.setStyle("-fx-font-size: 14; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else if (isCompleted.equals("current")) {
                tickBox.setText("☑");
                tickBox.setStyle("-fx-font-size: 14; -fx-text-fill: #3498db;");
            } else if (isCompleted.equals("cancelled")) {
                tickBox.setText("✗");
                tickBox.setStyle("-fx-font-size: 14; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                tickBox.setText("");
                tickBox.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");
            }
            
            stageRow.getChildren().addAll(stageLabel, stageSpacer, tickBox);
            stagesBox.getChildren().add(stageRow);
        }
        
        content.getChildren().addAll(customerLabel, totalLabel, stagesBox);
        
        statusDialog.getDialogPane().setContent(content);
        statusDialog.getDialogPane().setPrefWidth(450);
        
        ButtonType approveBtn = new ButtonType("Approve Order", ButtonBar.ButtonData.OK_DONE);
        ButtonType readyBtn = new ButtonType("Mark Food Ready", ButtonBar.ButtonData.OK_DONE);
        ButtonType declineBtn = new ButtonType("Decline Order", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        
        if (order.getStatus() == Order.OrderStatus.PENDING) {
            statusDialog.getDialogPane().getButtonTypes().setAll(approveBtn, declineBtn, cancelButton);
        } else if (order.getStatus() == Order.OrderStatus.CONFIRMED) {
            statusDialog.getDialogPane().getButtonTypes().setAll(readyBtn, cancelButton);
        } else {
            statusDialog.getDialogPane().getButtonTypes().setAll(cancelButton);
        }
        
        Optional<ButtonType> result = statusDialog.showAndWait();
        if (result.isPresent()) {
            if (result.get() == approveBtn) {
                approveOrder(order);
            } else if (result.get() == declineBtn) {
                declineOrder(order);
            } else if (result.get() == readyBtn) {
                markOrderReady(order);
            }
        }
    }
    
    private String getStageStatus(Order.OrderStatus currentStatus, Order.OrderStatus stageStatus) {
        if (currentStatus == Order.OrderStatus.CANCELLED) {
            return "pending";
        }
        if (currentStatus == Order.OrderStatus.AUTO_CANCELLED) {
            return "cancelled";
        }
        if (currentStatus == Order.OrderStatus.DELIVERED) {
            return "completed";
        }
        if (currentStatus.ordinal() > stageStatus.ordinal()) {
            return "completed";
        } else if (currentStatus == stageStatus) {
            return "current";
        }
        return "pending";
    }
    
    private void declineOrder(Order order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Decline Order");
        confirmAlert.setHeaderText("Decline Order: " + order.getOrderId());
        confirmAlert.setContentText("Are you sure you want to decline this order? This will cancel the order.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            databaseService.saveDataToFiles();
            loadOrders();
            showAlert("Order Declined", "Order Cancelled", "Order has been declined and cancelled.");
        }
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
    
    private void markOrderReady(Order order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Mark Order Ready");
        confirmAlert.setHeaderText("Mark Order as Ready: " + order.getOrderId());
        confirmAlert.setContentText("Is the food ready? This will notify that biker is on the way.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            order.setStatus(Order.OrderStatus.READY);
            databaseService.saveDataToFiles();
            loadOrders();
            showAlert("Success", "Order Ready", "Order marked as ready! Biker is on the way to deliver.");
        }
    }
}
