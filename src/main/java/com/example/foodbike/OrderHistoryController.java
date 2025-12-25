package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OrderHistoryController {
    @FXML private VBox ordersVBox;
    @FXML private Label titleLabel;

    private DatabaseService databaseService;
    private User currentUser;
    private Stage currentStage;

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
    }

    public void setCurrentUser(User user, Stage stage) {
        this.currentUser = user;
        this.currentStage = stage;
        titleLabel.setText(user.getUsername() + "'s Orders");
        loadOrderHistory();
    }

    private void loadOrderHistory() {
        List<Order> userOrders = databaseService.getUserOrders(currentUser.getUsername());

        userOrders = userOrders.stream()
                .sorted((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()))
                .collect(Collectors.toList());
        
        ordersVBox.getChildren().clear();

        if (userOrders.isEmpty()) {
            Label emptyLabel = new Label("You haven't placed any orders yet.");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
            ordersVBox.getChildren().add(emptyLabel);
        } else {
            for (Order order : userOrders) {
                VBox orderCard = createOrderCard(order);
                ordersVBox.getChildren().add(orderCard);
            }
        }
    }

    private VBox createOrderCard(Order order) {
        VBox card = new VBox();
        card.setSpacing(12);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");

        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setStyle("-fx-padding: 0 0 10 0;");

        String statusIcon = getStatusIcon(order.getStatus());
        Label statusLabel = new Label(statusIcon + " " + order.getStatus().toString());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label orderIdLabel = new Label("Order ID: " + order.getOrderId());
        orderIdLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #7f8c8d;");

        headerBox.getChildren().addAll(statusLabel, spacer, orderIdLabel);

        Restaurant restaurant = databaseService.getRestaurant(order.getRestaurantId());
        Label restaurantLabel = new Label("Restaurant: " + (restaurant != null ? restaurant.getName() : "Unknown"));
        restaurantLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        StringBuilder itemsText = new StringBuilder("Items: ");
        for (int j = 0; j < order.getItems().size(); j++) {
            MenuItem item = order.getItems().get(j);
            itemsText.append(item.getName());
            if (j < order.getItems().size() - 1) {
                itemsText.append(", ");
            }
        }
        Label itemsLabel = new Label(itemsText.toString());
        itemsLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
        itemsLabel.setWrapText(true);

        Label priceLabel = new Label("Total: ৳" + String.format("%.2f", order.getTotalPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #27ae60;");

        Label dateLabel = new Label("Placed: " + order.getCreatedAt().toString().substring(0, 10) + " on " + order.getCreatedAt().toString().substring(11, 19));
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #95a5a6;");
        VBox stagesBox = new VBox();
        stagesBox.setSpacing(8);
        stagesBox.setStyle("-fx-padding: 12; -fx-background-color: #f9f9f9; -fx-border-radius: 6; -fx-border-color: #e0e0e0;");

        String[] stages = {
            "✓ Your order is placed",
            "🍽️ Order is confirmed by restaurant",
            "🚴 Biker is on the way to deliver your order",
            "📦 Your delivery is completed"
        };

        Order.OrderStatus[] stageStatuses = {
            Order.OrderStatus.PENDING,
            Order.OrderStatus.CONFIRMED,
            Order.OrderStatus.PREPARING,
            Order.OrderStatus.READY
        };

        for (int i = 0; i < stages.length; i++) {
            HBox stageRow = new HBox();
            stageRow.setSpacing(10);
            stageRow.setStyle("-fx-padding: 8; -fx-alignment: center-left;");

            Label stageLabel = new Label(stages[i]);
            String isCompleted = isStageCompleted(order.getStatus(), stageStatuses[i]);
            if (isCompleted.equals("completed")) {
                stageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
            } else if (isCompleted.equals("current")) {
                stageLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #3498db; -fx-font-weight: bold;");
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
                tickBox.setText("◐");
                tickBox.setStyle("-fx-font-size: 14; -fx-text-fill: #3498db;");
            } else {
                tickBox.setText("");
                tickBox.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");
            }

            stageRow.getChildren().addAll(stageLabel, stageSpacer, tickBox);
            stagesBox.getChildren().add(stageRow);
        }

        card.getChildren().addAll(headerBox, restaurantLabel, itemsLabel, priceLabel, dateLabel, stagesBox);

        if (order.getStatus() == Order.OrderStatus.PENDING) {
            javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Cancel Order");
            cancelBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 11; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;");
            cancelBtn.setMaxWidth(Double.MAX_VALUE);
            cancelBtn.setOnAction(e -> handleCancelOrder(order, card));
            card.getChildren().add(cancelBtn);
        }

        return card;
    }

    private String isStageCompleted(Order.OrderStatus currentStatus, Order.OrderStatus stageStatus) {
        if (currentStatus == Order.OrderStatus.CANCELLED) {
            return "pending";
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

    private void handleCancelOrder(Order order, VBox orderCard) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Cancel Order");
        confirmAlert.setHeaderText("Are you sure?");
        confirmAlert.setContentText("Do you want to cancel order #" + order.getOrderId() + "?\nNote: You can only cancel before the restaurant confirms.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            order.setStatus(Order.OrderStatus.CANCELLED);
            databaseService.createOrder(order);

            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("Order Cancelled");
            successAlert.setHeaderText(null);
            successAlert.setContentText("Your order #" + order.getOrderId() + " has been cancelled successfully!");
            successAlert.showAndWait();
            loadOrderHistory();
        }
    }

    private String getStatusIcon(Order.OrderStatus status) {
        switch (status) {
            case PENDING:
                return "⭕";
            case CONFIRMED:
                return "✓";
            case PREPARING:
                return "🍳";
            case READY:
                return "📦";
            case DELIVERED:
                return "✓✓";
            case CANCELLED:
                return "✗";
            default:
                return "❓";
        }
    }

    @FXML
    public void handleBack() {
        if (currentStage != null) {
            currentStage.close();
        }
    }
}
