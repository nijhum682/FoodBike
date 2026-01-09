package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.*;

public class MenuController {
    @FXML private Label restaurantNameLabel;
    @FXML private Label ratingLabel;
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
        if (ratingLabel != null) {
            ratingLabel.setText("★ " + String.format("%.1f", restaurant.getRating()));
        }
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
        
        String bgColor = "#F5DEB3";
        
        card.setStyle("-fx-border-color: transparent; -fx-border-radius: 12; -fx-padding: 15; " +
                     "-fx-background-color: " + bgColor + "; " +
                     "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 10, 0, 0, 3);");
        card.setSpacing(8);

        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #1a1a1a;");

        Label descLabel = new Label(item.getDescription());
        descLabel.setStyle("-fx-font-size: 12; -fx-font-weight: 600; -fx-text-fill: #333333;");
        descLabel.setWrapText(true);

        Label priceLabel = new Label("৳" + String.format("%.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1B5E20;");

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

            Label itemLabel = new Label(item.getName());
            itemLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2c3e50;");

            Region spacer = new Region();
            spacer.setPrefWidth(1);
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox quantityBox = new HBox();
            quantityBox.setSpacing(5);
            quantityBox.setStyle("-fx-alignment: center;");

            Button minusBtn = new Button("−");
            minusBtn.setStyle("-fx-padding: 2 6; -fx-font-size: 11; -fx-background-color: #e74c3c; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;");
            minusBtn.setOnAction(e -> decreaseQuantity(item));

            Label quantityLabel = new Label(String.valueOf(quantity));
            quantityLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-min-width: 20; -fx-text-alignment: center;");

            Button plusBtn = new Button("+");
            plusBtn.setStyle("-fx-padding: 2 6; -fx-font-size: 11; -fx-background-color: #27ae60; -fx-text-fill: white; -fx-border-radius: 4; -fx-cursor: hand;");
            plusBtn.setOnAction(e -> increaseQuantity(item));

            quantityBox.getChildren().addAll(minusBtn, quantityLabel, plusBtn);

            Label priceLabel = new Label("৳" + String.format("%.2f", itemTotal));
            priceLabel.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #27ae60;");

            itemRow.getChildren().addAll(itemLabel, spacer, quantityBox, priceLabel);
            selectedItemsVBox.getChildren().add(itemRow);
        }

        totalLabel.setText("৳" + String.format("%.2f", total));
    }

    private void increaseQuantity(MenuItem item) {
        selectedItems.put(item, selectedItems.get(item) + 1);
        updateOrderSummary();
    }

    private void decreaseQuantity(MenuItem item) {
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

        String orderId = "ORD_" + System.currentTimeMillis();
        Order order = new Order(orderId, currentUser.getUsername(), restaurant.getId());
        order.setDistrict(restaurant.getDistrict());

        for (MenuItem item : selectedItems.keySet()) {
            for (int i = 0; i < selectedItems.get(item); i++) {
                order.addItem(item);
            }
        }

        if (showPaymentMethodDialog(order)) {
            databaseService.createOrder(order);
            showOrderConfirmationDialog(order);
            selectedItems.clear();
            updateOrderSummary();
        }
    }

    private boolean showPaymentMethodDialog(Order order) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Select Payment Method");
        dialog.setHeaderText("Choose your payment method");

        VBox content = new VBox();
        content.setSpacing(15);
        content.setPadding(new Insets(20));

        Label totalLabel = new Label("Total Amount: ৳" + String.format("%.2f", order.getTotalPrice()));
        totalLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        ToggleGroup paymentGroup = new ToggleGroup();

        RadioButton bkashRadio = new RadioButton("Bkash");
        bkashRadio.setToggleGroup(paymentGroup);
        bkashRadio.setStyle("-fx-font-size: 14;");

        RadioButton nagadRadio = new RadioButton("Nagad");
        nagadRadio.setToggleGroup(paymentGroup);
        nagadRadio.setStyle("-fx-font-size: 14;");

        RadioButton codRadio = new RadioButton("Cash on Delivery");
        codRadio.setToggleGroup(paymentGroup);
        codRadio.setStyle("-fx-font-size: 14;");

        bkashRadio.setSelected(true);

        content.getChildren().addAll(totalLabel, bkashRadio, nagadRadio, codRadio);

        dialog.getDialogPane().setContent(content);
        ButtonType confirmButton = new ButtonType("Proceed", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, cancelButton);

        Optional<ButtonType> result = dialog.showAndWait();

        if (result.isPresent() && result.get() == confirmButton) {
            RadioButton selected = (RadioButton) paymentGroup.getSelectedToggle();
            String paymentMethod = selected.getText();

            if (paymentMethod.equals("Bkash") || paymentMethod.equals("Nagad")) {
                boolean success = processMobilePayment(paymentMethod);
                if (success) {
                    order.setPaymentMethod(paymentMethod);
                }
                return success;
            } else {
                order.setPaymentMethod("Cash on Delivery");
                return true;
            }
        }
        return false;
    }

    private boolean processMobilePayment(String paymentMethod) {
        TextInputDialog mobileDialog = new TextInputDialog();
        mobileDialog.setTitle(paymentMethod + " Payment");
        mobileDialog.setHeaderText("Enter your " + paymentMethod + " mobile number");
        mobileDialog.setContentText("Mobile Number:");

        Optional<String> mobileResult = mobileDialog.showAndWait();

        if (mobileResult.isPresent() && !mobileResult.get().trim().isEmpty()) {
            String mobile = mobileResult.get().trim();

            int randomOTP = 10000 + new Random().nextInt(90000);

            Alert otpAlert = new Alert(Alert.AlertType.INFORMATION);
            otpAlert.setTitle("OTP");
            otpAlert.setHeaderText("Your OTP is: " + randomOTP);
            otpAlert.setContentText("Please enter this OTP to confirm payment");
            otpAlert.showAndWait();

            TextInputDialog otpDialog = new TextInputDialog();
            otpDialog.setTitle("Enter OTP");
            otpDialog.setHeaderText("Enter the 5-digit OTP");
            otpDialog.setContentText("OTP:");

            Optional<String> otpResult = otpDialog.showAndWait();

            if (otpResult.isPresent() && otpResult.get().trim().length() == 5) {
                try {
                    Integer.parseInt(otpResult.get().trim());

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Payment Successful");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Your payment is successful!");
                    successAlert.showAndWait();
                    return true;
                } catch (NumberFormatException e) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Invalid OTP");
                    errorAlert.setHeaderText(null);
                    errorAlert.setContentText("Please enter valid 5-digit OTP");
                    errorAlert.showAndWait();
                    return false;
                }
            }
        }
        return false;
    }

    private void showOrderConfirmationDialog(Order order) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Order Confirmation");
        dialog.setHeaderText("Order Placed Successfully!");

        VBox content = new VBox();
        content.setSpacing(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-font-size: 14; -fx-alignment: center;");

        Label successLabel = new Label("✓ Your order has been successfully placed!");
        successLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16; -fx-text-fill: #27ae60;");

        Label orderIdLabel = new Label("Order ID: " + order.getOrderId());
        orderIdLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        HBox totalBox = new HBox();
        totalBox.setSpacing(15);
        totalBox.setStyle("-fx-padding: 15; -fx-background-color: #e8f8f5; -fx-border-radius: 8; -fx-alignment: center;");
        Label totalTextLabel = new Label("Total Price:");
        totalTextLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Label totalPriceLabel = new Label("৳" + String.format("%.2f", order.getTotalPrice()));
        totalPriceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 18; -fx-text-fill: #27ae60;");
        totalBox.getChildren().addAll(totalTextLabel, totalPriceLabel);

        Label infoLabel = new Label("Check your Order History for tracking details.");
        infoLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");

        content.getChildren().addAll(
            successLabel,
            orderIdLabel,
            totalBox,
            infoLabel
        );

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        Optional<Void> result = dialog.showAndWait();
        if (!result.isPresent()) {
            Stage stage = (Stage) restaurantNameLabel.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    public void handleViewReviews() {
        List<Review> reviews = databaseService.getRestaurantReviews(restaurant.getId());

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Reviews - " + restaurant.getName());
        dialog.setHeaderText("Customer Reviews");

        VBox content = new VBox();
        content.setSpacing(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);

        Label statsLabel = new Label("Average Rating: ★ " + String.format("%.1f", restaurant.getRating()) + " (" + reviews.size() + " reviews)");
        statsLabel.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: #f39c12;");
        content.getChildren().add(statsLabel);

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setPrefHeight(400);
        scrollPane.setFitToWidth(true);

        VBox reviewsBox = new VBox(15);
        reviewsBox.setPadding(new Insets(10));

        if (reviews.isEmpty()) {
            Label noReviews = new Label("No reviews yet. Be the first to review!");
            noReviews.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d;");
            reviewsBox.getChildren().add(noReviews);
        } else {
            for (Review review : reviews) {
                VBox reviewCard = new VBox(8);
                reviewCard.setStyle("-fx-border-color: #ddd; -fx-border-radius: 8; -fx-padding: 12; -fx-background-color: white;");

                HBox headerBox = new HBox(10);
                Label userLabel = new Label(review.getUserId());
                userLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                String stars = "★".repeat(review.getRating()) + "☆".repeat(5 - review.getRating());
                Label ratingStars = new Label(stars);
                ratingStars.setStyle("-fx-font-size: 14; -fx-text-fill: #f39c12;");

                headerBox.getChildren().addAll(userLabel, spacer, ratingStars);

                Label commentLabel = new Label(review.getComment());
                commentLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2c3e50;");
                commentLabel.setWrapText(true);

                Label dateLabel = new Label(review.getCreatedAt().toString().substring(0, 10));
                dateLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #95a5a6;");

                reviewCard.getChildren().addAll(headerBox, commentLabel, dateLabel);
                reviewsBox.getChildren().add(reviewCard);
            }
        }

        scrollPane.setContent(reviewsBox);
        content.getChildren().add(scrollPane);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);

        dialog.showAndWait();
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