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

import java.time.LocalDateTime;
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
        
        // Auto-cancel pending orders older than 1 hour
        boolean hasAutoCancelled = false;
        for (Order order : userOrders) {
            if (order.shouldAutoCancelled()) {
                order.setStatus(Order.OrderStatus.AUTO_CANCELLED);
                hasAutoCancelled = true;
            }
        }
        if (hasAutoCancelled) {
            databaseService.saveDataToFiles();
        }

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
        card.setStyle("-fx-border-color: transparent; -fx-border-radius: 12; -fx-background-color: #87CEEB; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0, 0, 3);");

        HBox headerBox = new HBox();
        headerBox.setSpacing(10);
        headerBox.setStyle("-fx-padding: 0 0 10 0;");

        String statusIcon = getStatusIcon(order.getStatus());
        Label statusLabel = new Label(statusIcon + " " + order.getStatus().toString());
        statusLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: #2c3e50;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label orderIdLabel = new Label("Order ID: " + order.getOrderId());
        orderIdLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        headerBox.getChildren().addAll(statusLabel, spacer, orderIdLabel);

        Restaurant restaurant = databaseService.getRestaurant(order.getRestaurantId());
        Label restaurantLabel = new Label("Restaurant: " + (restaurant != null ? restaurant.getName() : "Unknown"));
        restaurantLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        StringBuilder itemsText = new StringBuilder("Items: ");
        for (int j = 0; j < order.getItems().size(); j++) {
            MenuItem item = order.getItems().get(j);
            itemsText.append(item.getName());
            if (j < order.getItems().size() - 1) {
                itemsText.append(", ");
            }
        }
        Label itemsLabel = new Label(itemsText.toString());
        itemsLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");
        itemsLabel.setWrapText(true);

        Label priceLabel = new Label("Total: ৳" + String.format("%.2f", order.getTotalPrice()));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #1B5E20;");

        Label dateLabel = new Label("Placed: " + order.getCreatedAt().toString().substring(0, 10) + " on " + order.getCreatedAt().toString().substring(11, 19));
        dateLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold; -fx-text-fill: #333333;");
        VBox stagesBox = new VBox();
        stagesBox.setSpacing(8);
        stagesBox.setStyle("-fx-padding: 12; -fx-background-color: #2E7D32; -fx-border-radius: 6; -fx-border-color: transparent;");

        String[] stages = {
            "✓ Your order is placed",
            "🍽 Order is confirmed by restaurant",
            "🚴 Biker is on the way to deliver your order",
            "📦 Your delivery is completed"
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
            String isCompleted = isStageCompleted(order.getStatus(), stageStatuses[i]);
            if (isCompleted.equals("completed")) {
                stageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
            } else if (isCompleted.equals("current")) {
                stageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #FFD700; -fx-font-weight: bold;");
            } else if (isCompleted.equals("cancelled")) {
                stageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #FF6B6B; -fx-font-weight: bold;");
            } else {
                stageLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #CCCCCC; -fx-font-weight: bold;");
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

        card.getChildren().addAll(headerBox, restaurantLabel, itemsLabel, priceLabel, dateLabel, stagesBox);

        if (order.getStatus() == Order.OrderStatus.PENDING) {
            javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Cancel Order");
            cancelBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 11; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;");
            cancelBtn.setMaxWidth(Double.MAX_VALUE);
            cancelBtn.setOnAction(e -> handleCancelOrder(order, card));
            card.getChildren().add(cancelBtn);
        }

        if (order.getStatus() == Order.OrderStatus.DELIVERED) {
            boolean hasReviewed = databaseService.hasUserReviewedOrder(currentUser.getUsername(), order.getOrderId());
            if (!hasReviewed) {
                javafx.scene.control.Button reviewBtn = new javafx.scene.control.Button("Write Review");
                reviewBtn.setStyle("-fx-padding: 8 15; -fx-font-size: 11; -fx-background-color: #f39c12; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;");
                reviewBtn.setMaxWidth(Double.MAX_VALUE);
                reviewBtn.setOnAction(e -> handleWriteReview(order));
                card.getChildren().add(reviewBtn);
            } else {
                Label reviewedLabel = new Label("✓ You have reviewed this order");
                reviewedLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #27ae60; -fx-padding: 8;");
                card.getChildren().add(reviewedLabel);
            }
        }

        return card;
    }

    private String isStageCompleted(Order.OrderStatus currentStatus, Order.OrderStatus stageStatus) {
        if (currentStatus == Order.OrderStatus.CANCELLED) {
            return "pending";
        }
        if (currentStatus == Order.OrderStatus.AUTO_CANCELLED) {
            // For auto-cancelled orders, all stages show as cancelled with red cross marks
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
            case AUTO_CANCELLED:
                return "⌛";
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

    private void handleWriteReview(Order order) {
        Restaurant restaurant = databaseService.getRestaurant(order.getRestaurantId());
        if (restaurant == null) return;

        javafx.scene.control.Dialog<javafx.scene.control.ButtonType> dialog = new javafx.scene.control.Dialog<>();
        dialog.setTitle("Write Review");
        dialog.setHeaderText("Review " + restaurant.getName());

        VBox content = new VBox();
        content.setSpacing(15);
        content.setPadding(new Insets(20));

        Label ratingLabel = new Label("Rating (1-5 stars):");
        ratingLabel.setStyle("-fx-font-weight: bold;");

        HBox starsBox = new HBox(10);
        javafx.scene.control.ToggleGroup ratingGroup = new javafx.scene.control.ToggleGroup();
        javafx.scene.control.RadioButton[] stars = new javafx.scene.control.RadioButton[5];

        for (int i = 0; i < 5; i++) {
            int starValue = i + 1;
            stars[i] = new javafx.scene.control.RadioButton(starValue + " ★");
            stars[i].setToggleGroup(ratingGroup);
            stars[i].setStyle("-fx-font-size: 14;");
            starsBox.getChildren().add(stars[i]);
        }
        stars[4].setSelected(true);

        Label commentLabel = new Label("Comment:");
        commentLabel.setStyle("-fx-font-weight: bold;");

        javafx.scene.control.TextArea commentArea = new javafx.scene.control.TextArea();
        commentArea.setPromptText("Share your experience...");
        commentArea.setPrefRowCount(4);
        commentArea.setWrapText(true);

        content.getChildren().addAll(ratingLabel, starsBox, commentLabel, commentArea);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK, javafx.scene.control.ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                javafx.scene.control.RadioButton selectedRating = (javafx.scene.control.RadioButton) ratingGroup.getSelectedToggle();
                if (selectedRating != null) {
                    int rating = Integer.parseInt(selectedRating.getText().split(" ")[0]);
                    String comment = commentArea.getText().trim();

                    if (comment.isEmpty()) {
                        comment = "No comment provided.";
                    }

                    String reviewId = "REV_" + System.currentTimeMillis();
                    Review review = new Review(reviewId, restaurant.getId(), currentUser.getUsername(), order.getOrderId(), rating, comment);
                    databaseService.addReview(review);

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Review Submitted");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Thank you for your review! Your feedback helps others make better choices.");
                    successAlert.showAndWait();

                    loadOrderHistory();
                }
            }
        });
    }
}
