package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDateTime;
import java.util.*;

public class MenuController {
    @FXML private Label restaurantNameLabel;
    @FXML private VBox menuItemsVBox;
    @FXML private VBox selectedItemsVBox;
    @FXML private Label totalLabel;

    private Restaurant restaurant;
    private User currentUser;
    private Map<MenuItem, Integer> selectedItems;
    private DatabaseService databaseService;

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        selectedItems = new LinkedHashMap<>();
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
        restaurantNameLabel.setText(restaurant.getName());
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void loadMenu() {
        menuItemsVBox.getChildren().clear();

        for (MenuItem item : restaurant.getMenu()) {
            VBox itemCard = createMenuItemCard(item);
            menuItemsVBox.getChildren().add(itemCard);
        }
    }

    private VBox createMenuItemCard(MenuItem item) {
        VBox card = new VBox();
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 12; -fx-background-color: white;");
        card.setSpacing(8);

        // Item Name
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Item Description
        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");
        descLabel.setWrapText(true);

        // Price
        Label priceLabel = new Label("$" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

        // Add to Order Button
        Button addBtn = new Button("+ Add to Order");
        addBtn.setStyle("-fx-padding: 8; -fx-font-size: 12; -fx-background-color: #3498db; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;");
        addBtn.setMaxWidth(Double.MAX_VALUE);
        addBtn.setOnAction(e -> addItemToOrder(item));

        card.getChildren().addAll(nameLabel, descLabel, priceLabel, addBtn);

        return card;
    }

    private void addItemToOrder(MenuItem item) {
        selectedItems.put(item, selectedItems.getOrDefault(item, 0) + 1);
        updateOrderSummary();
    }

    private void updateOrderSummary() {
        selectedItemsVBox.getChildren().clear();

        double total = 0;
        for (MenuItem item : selectedItems.keySet()) {
            int quantity = selectedItems.get(item);
            double itemTotal = item.getPrice() * quantity;
            total += itemTotal;

            HBox itemRow = new HBox();
            itemRow.setStyle("-fx-padding: 8; -fx-background-color: #f9f9f9; -fx-border-radius: 4;");
            itemRow.setSpacing(10);

            Label itemLabel = new Label(item.getName() + " x" + quantity);
            itemLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2c3e50;");

            Region spacer = new Region();
            spacer.setPrefWidth(1);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label priceLabel = new Label("$" + String.format("%.2f", itemTotal));
            priceLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

            Button removeBtn = new Button("−");
            removeBtn.setStyle("-fx-padding: 2 8; -fx-font-size: 11; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;");
            removeBtn.setOnAction(e -> removeItemFromOrder(item));

            itemRow.getChildren().addAll(itemLabel, spacer, priceLabel, removeBtn);
            selectedItemsVBox.getChildren().add(itemRow);
        }

        totalLabel.setText("$" + String.format("%.2f", total));
    }

    private void removeItemFromOrder(MenuItem item) {
        int quantity = selectedItems.get(item);
        if (quantity > 1) {
            selectedItems.put(item, quantity - 1);
        } else {
            selectedItems.remove(item);
        }
        updateOrderSummary();
    }

    @FXML
    public void handleConfirmOrder() {
        if (selectedItems.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Empty Order");
            alert.setHeaderText(null);
            alert.setContentText("Please select at least one item");
            alert.showAndWait();
            return;
        }

        // Create order
        String orderId = "ORD_" + System.currentTimeMillis();
        Order order = new Order(orderId, currentUser.getUsername(), restaurant.getId());

        for (MenuItem item : selectedItems.keySet()) {
            for (int i = 0; i < selectedItems.get(item); i++) {
                order.addItem(item);
            }
        }

        databaseService.createOrder(order);

        // Show success message
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Order Confirmed");
        alert.setHeaderText(null);
        alert.setContentText("Your order #" + orderId + " has been confirmed!\nTotal: $" + String.format("%.2f", order.getTotalPrice()));
        alert.showAndWait();

        // Clear the order
        selectedItems.clear();
        updateOrderSummary();
    }

    @FXML
    public void handleClearOrder() {
        selectedItems.clear();
        updateOrderSummary();
    }

    @FXML
    public void handleBack() {
        Stage stage = (Stage) restaurantNameLabel.getScene().getWindow();
        stage.close();
    }
}
