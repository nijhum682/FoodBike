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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;
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
    private List<Restaurant> allRestaurants;

    private static final Map<String, String> DIVISION_PREFIXES = new HashMap<>();
    static {
        DIVISION_PREFIXES.put("Dhaka", "DH");
        DIVISION_PREFIXES.put("Chittagong", "CH");
        DIVISION_PREFIXES.put("Sylhet", "SY");
        DIVISION_PREFIXES.put("Rajshahi", "RJ");
        DIVISION_PREFIXES.put("Khulna", "KH");
        DIVISION_PREFIXES.put("Barisal", "BA");
        DIVISION_PREFIXES.put("Rangpur", "RP");
        DIVISION_PREFIXES.put("Mymensingh", "MY");
    }

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

    @SuppressWarnings("unchecked")
    private void setupTableColumns() {
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
        addActionColumns();
    }

    private void addActionColumns() {
        restaurantsTable.getColumns().removeIf(col -> col.getText().equals("Menu") || col.getText().equals("Delete"));
        
        TableColumn<Restaurant, Void> menuCol = new TableColumn<>("Menu");
        menuCol.setPrefWidth(150);
        
        menuCol.setCellFactory(col -> new TableCell<Restaurant, Void>() {
            private HBox buttonBox = new HBox(5);
            private Button viewMenuBtn = new Button("View");
            private Button addMenuBtn = new Button("Add");

            {
                viewMenuBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 4;");
                addMenuBtn.setStyle("-fx-padding: 5 10; -fx-font-size: 11; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 4;");
                
                viewMenuBtn.setOnAction(e -> {
                    Restaurant restaurant = getTableView().getItems().get(getIndex());
                    showViewMenuDialog(restaurant);
                });
                
                addMenuBtn.setOnAction(e -> {
                    Restaurant restaurant = getTableView().getItems().get(getIndex());
                    showMenuDialog(restaurant);
                });
                
                buttonBox.getChildren().addAll(viewMenuBtn, addMenuBtn);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(buttonBox);
                }
            }
        });
        
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
        
        restaurantsTable.getColumns().addAll(menuCol, deleteCol);
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
        addActionColumns();
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
        addActionColumns();
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
        
        Spinner<Double> ratingSpinner = new Spinner<>(0.0, 5.0, 4.5, 0.1);
        ratingSpinner.setEditable(true);
        ratingSpinner.setPrefWidth(250);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Division:"), 0, 1);
        grid.add(divisionComboAdd, 1, 1);
        grid.add(new Label("Address:"), 0, 2);
        grid.add(addressField, 1, 2);
        grid.add(new Label("Rating (0-5):"), 0, 3);
        grid.add(ratingSpinner, 1, 3);
        
        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String name = nameField.getText().trim();
                String division = divisionComboAdd.getValue();
                String address = addressField.getText().trim();
                Double rating = ratingSpinner.getValue();
                
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
                if (rating < 0 || rating > 5) {
                    showAlert("Validation Error", "Invalid Rating", "Rating must be between 0 and 5.");
                    return null;
                }

                int count = 0;
                for (Restaurant r : allRestaurants) {
                    if (r.getDivision().equals(division)) {
                        count++;
                    }
                }
                String prefix = DIVISION_PREFIXES.get(division);
                String restaurantId = String.format("%s%03d", prefix, count + 1);
                Restaurant newRestaurant = new Restaurant(
                    restaurantId,
                    name,
                    division,
                    address
                );
                newRestaurant.setRating(rating);
                return newRestaurant;
            }
            return null;
        });
        
        Optional<Restaurant> result = dialog.showAndWait();
        result.ifPresent(restaurant -> {
            if (databaseService.addRestaurant(restaurant)) {
                showMenuDialog(restaurant);
                loadRestaurants();
                showAlert("Success", "Restaurant Added", "Restaurant '" + restaurant.getName() + "' has been added successfully!");
            } else {
                showAlert("Error", "Failed to Add Restaurant", "Could not add the restaurant. Please try again.");
            }
        });
    }

    private void showMenuDialog(Restaurant restaurant) {
        Dialog<Void> menuDialog = new Dialog<>();
        menuDialog.setTitle("Add Menu Items");
        menuDialog.setHeaderText("Add menu items for " + restaurant.getName());
        
        VBox vbox = new VBox();
        vbox.setSpacing(15);
        vbox.setPadding(new Insets(20));
        
        Label instructionLabel = new Label("Add menu items for this restaurant:");
        instructionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        
        VBox addedItemsList = new VBox();
        addedItemsList.setSpacing(5);
        addedItemsList.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: #f0f0f0;");
        
        Label addedItemsHeader = new Label("Added Items:");
        addedItemsHeader.setStyle("-fx-font-weight: bold; -fx-font-size: 11;");
        addedItemsList.getChildren().add(addedItemsHeader);
        
        ScrollPane addedItemsScroll = new ScrollPane(addedItemsList);
        addedItemsScroll.setPrefHeight(150);
        addedItemsScroll.setFitToWidth(true);
        
        VBox menuItemsContainer = new VBox();
        menuItemsContainer.setSpacing(10);
        menuItemsContainer.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: #f9f9f9;");
        
        ScrollPane scrollPane = new ScrollPane(menuItemsContainer);
        scrollPane.setPrefHeight(200);
        scrollPane.setFitToWidth(true);
        
        Button addItemButton = new Button("+ Add Item");
        addItemButton.setStyle("-fx-padding: 8 15; -fx-font-size: 12; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 4;");
        
        List<MenuItem> menuItems = new ArrayList<>();
        
        addItemButton.setOnAction(e -> {
            VBox itemBox = createMenuItemInputBox(menuItemsContainer, menuItems, addedItemsList);
            menuItemsContainer.getChildren().add(itemBox);
        });
        
        vbox.getChildren().addAll(instructionLabel, addedItemsScroll, new Label("Input Form:"), addItemButton, scrollPane);
        
        menuDialog.getDialogPane().setContent(vbox);
        menuDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        menuDialog.setOnCloseRequest(e -> {
            if (menuDialog.getResult() == null) {
                for (MenuItem item : menuItems) {
                    restaurant.addMenuItem(item);
                }
                databaseService.saveDataToFiles();
            }
        });
        
        Optional<Void> menuResult = menuDialog.showAndWait();
        if (menuResult.isPresent() || menuResult.isEmpty()) {
            for (MenuItem item : menuItems) {
                restaurant.addMenuItem(item);
            }
            databaseService.saveDataToFiles();
        }
    }

    private VBox createMenuItemInputBox(VBox container, List<MenuItem> menuItems, VBox addedItemsList) {
        VBox itemBox = new VBox();
        itemBox.setSpacing(10);
        itemBox.setStyle("-fx-padding: 15; -fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 4;");
        
        Label nameLabel = new Label("Item Name:");
        nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        TextField itemNameField = new TextField();
        itemNameField.setPromptText("Enter item name");
        itemNameField.setMaxWidth(Double.MAX_VALUE);
        
        Label descLabel = new Label("Description:");
        descLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        TextField itemDescField = new TextField();
        itemDescField.setPromptText("Enter description");
        itemDescField.setMaxWidth(Double.MAX_VALUE);
        
        Label priceLabel = new Label("Price (৳):");
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 10000.0, 100.0, 10.0);
        priceSpinner.setEditable(true);
        priceSpinner.setMaxWidth(Double.MAX_VALUE);
        
        HBox buttonBox = new HBox(10);
        Button saveItemBtn = new Button("Add Item");
        saveItemBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 4;");
        saveItemBtn.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(saveItemBtn, javafx.scene.layout.Priority.ALWAYS);
        
        Button removeBtn = new Button("Remove");
        removeBtn.setStyle("-fx-padding: 8 20; -fx-font-size: 12; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 4;");
        
        buttonBox.getChildren().addAll(saveItemBtn, removeBtn);
        
        saveItemBtn.setOnAction(e -> {
            String itemName = itemNameField.getText().trim();
            String itemDesc = itemDescField.getText().trim();
            Double price = priceSpinner.getValue();
            
            if (itemName.isEmpty()) {
                showAlert("Validation Error", "Missing Data", "Please enter item name.");
                return;
            }
            if (itemDesc.isEmpty()) {
                showAlert("Validation Error", "Missing Data", "Please enter item description.");
                return;
            }
            if (price <= 0) {
                showAlert("Validation Error", "Invalid Price", "Price must be greater than 0.");
                return;
            }
            
            String itemId = "item_" + System.currentTimeMillis();
            MenuItem menuItem = new MenuItem(itemId, itemName, itemDesc, price);
            menuItems.add(menuItem);
            
            Label itemLabel = new Label(itemName + " - ৳" + String.format("%.2f", price));
            itemLabel.setStyle("-fx-font-size: 11; -fx-padding: 3; -fx-text-fill: #2c3e50;");
            addedItemsList.getChildren().add(itemLabel);
            
            itemNameField.clear();
            itemDescField.clear();
            priceSpinner.getValueFactory().setValue(100.0);
        });
        
        removeBtn.setOnAction(e -> {
            container.getChildren().remove(itemBox);
        });
        
        itemBox.getChildren().addAll(nameLabel, itemNameField, descLabel, itemDescField, priceLabel, priceSpinner, buttonBox);
        
        return itemBox;
    }

    private void showViewMenuDialog(Restaurant restaurant) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Restaurant Menu");
        dialog.setHeaderText(restaurant.getName() + " - Menu Items");
        
        VBox vbox = new VBox();
        vbox.setSpacing(10);
        vbox.setPadding(new Insets(20));
        
        if (restaurant.getMenu().isEmpty()) {
            Label emptyLabel = new Label("No menu items available for this restaurant.");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
            vbox.getChildren().add(emptyLabel);
        } else {
            for (MenuItem item : restaurant.getMenu()) {
                VBox itemCard = new VBox(8);
                itemCard.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-padding: 12; -fx-background-color: #f9f9f9;");
                
                HBox nameBox = new HBox(8);
                nameBox.setStyle("-fx-alignment: center-left;");
                Label nameLabel = new Label(item.getName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
                HBox.setHgrow(nameLabel, javafx.scene.layout.Priority.ALWAYS);
                
                Button editBtn = new Button("Edit Item");
                editBtn.setStyle("-fx-padding: 5 12; -fx-font-size: 10; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 3;");
                editBtn.setOnAction(e -> {
                    showEditItemDialog(item, nameLabel, itemCard, restaurant);
                });
                
                nameBox.getChildren().addAll(nameLabel, editBtn);
                
                Label descLabel = new Label(item.getDescription());
                descLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");
                descLabel.setWrapText(true);
                
                Label priceLabel = new Label("৳" + String.format("%.2f", item.getPrice()));
                priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #27ae60;");
                
                itemCard.getChildren().addAll(nameBox, descLabel, priceLabel);
                vbox.getChildren().add(itemCard);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefHeight(400);
        scrollPane.setPrefWidth(450);
        scrollPane.setFitToWidth(true);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }

    private void showEditItemDialog(MenuItem item, Label nameLabel, VBox itemCard, Restaurant restaurant) {
        Dialog<Void> editDialog = new Dialog<>();
        editDialog.setTitle("Edit Menu Item");
        editDialog.setHeaderText("Edit item details");
        
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));
        
        TextField nameField = new TextField(item.getName());
        nameField.setPromptText("Item Name");
        nameField.setPrefWidth(250);
        
        TextField descField = new TextField(item.getDescription());
        descField.setPromptText("Description");
        descField.setPrefWidth(250);
        
        Spinner<Double> priceSpinner = new Spinner<>(0.0, 10000.0, item.getPrice(), 10.0);
        priceSpinner.setEditable(true);
        priceSpinner.setPrefWidth(250);
        
        grid.add(new Label("Item Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Description:"), 0, 1);
        grid.add(descField, 1, 1);
        grid.add(new Label("Price (৳):"), 0, 2);
        grid.add(priceSpinner, 1, 2);
        
        editDialog.getDialogPane().setContent(grid);
        editDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        editDialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String newName = nameField.getText().trim();
                String newDesc = descField.getText().trim();
                Double newPrice = priceSpinner.getValue();
                
                if (newName.isEmpty()) {
                    showAlert("Validation Error", "Missing Data", "Please enter item name.");
                    return null;
                }
                if (newDesc.isEmpty()) {
                    showAlert("Validation Error", "Missing Data", "Please enter item description.");
                    return null;
                }
                if (newPrice <= 0) {
                    showAlert("Validation Error", "Invalid Price", "Price must be greater than 0.");
                    return null;
                }
                
                item.setName(newName);
                item.setDescription(newDesc);
                item.setPrice(newPrice);
                
                nameLabel.setText(newName);
                Label descLabel = (Label) itemCard.getChildren().get(1);
                descLabel.setText(newDesc);
                Label priceLabel = (Label) itemCard.getChildren().get(2);
                priceLabel.setText("৳" + String.format("%.2f", newPrice));
                
                databaseService.saveDataToFiles();
                showAlert("Success", "Item Updated", "Menu item has been updated successfully.");
            }
            return null;
        });
        
        editDialog.showAndWait();
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
