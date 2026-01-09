package com.example.foodbike;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BikerController {
    @FXML private TableView<Order> readyOrdersTable;
    @FXML private TableView<Order> deliveredOrdersTable;
    @FXML private Label userLabel;
    @FXML private ComboBox<String> divisionFilter;
    @FXML private ComboBox<String> districtFilter;

    private User currentUser;
    private DatabaseService databaseService;
    private List<Order> allReadyOrders = new ArrayList<>();

    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        setupReadyOrdersTable();
        setupDeliveredOrdersTable();
        setupDivisionFilter();
        setupDistrictFilter();
    }

    private void setupDivisionFilter() {
        divisionFilter.getItems().add("All Locations");
        divisionFilter.getItems().addAll("Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh");
        divisionFilter.setValue("All Locations");
        divisionFilter.setOnAction(e -> updateDistrictFilter());
    }

    private void setupDistrictFilter() {
        if (districtFilter != null) {
            districtFilter.getItems().add("All Districts");
            List<String> districts = databaseService.getAllDistricts();
            districtFilter.getItems().addAll(districts);
            districtFilter.setValue("All Districts");
        }
    }

    private void updateDistrictFilter() {
        if (districtFilter != null) {
            districtFilter.getItems().clear();
            districtFilter.getItems().add("All Districts");
            
            String selectedDivision = divisionFilter.getValue();
            if (selectedDivision != null && !selectedDivision.equals("All Locations")) {
                Map<String, List<String>> divisionDistrictsMap = databaseService.getDivisionDistrictsMap();
                List<String> districts = divisionDistrictsMap.get(selectedDivision);
                if (districts != null) {
                    districtFilter.getItems().addAll(districts);
                }
            } else {
                List<String> allDistricts = databaseService.getAllDistricts();
                districtFilter.getItems().addAll(allDistricts);
            }
            districtFilter.setValue("All Districts");
        }
    }

    @FXML
    private void handleFilter() {
        String selectedDivision = divisionFilter.getValue();
        String selectedDistrict = districtFilter != null ? districtFilter.getValue() : "All Districts";
        
        if (selectedDivision.equals("All Locations") && selectedDistrict.equals("All Districts")) {
            readyOrdersTable.getItems().setAll(allReadyOrders);
        } else {
            List<Order> filtered = new ArrayList<>();
            for (Order order : allReadyOrders) {
                Restaurant restaurant = databaseService.getRestaurant(order.getRestaurantId());
                if (restaurant != null) {
                    boolean matchesDivision = selectedDivision.equals("All Locations") || restaurant.getDivision().equals(selectedDivision);
                    boolean matchesDistrict = selectedDistrict.equals("All Districts") || (restaurant.getDistrict() != null && restaurant.getDistrict().equals(selectedDistrict));
                    if (matchesDivision && matchesDistrict) {
                        filtered.add(order);
                    }
                }
            }
            readyOrdersTable.getItems().setAll(filtered);
        }
        readyOrdersTable.refresh();
    }
    
    @FXML
    private void handleClearFilter() {
        divisionFilter.setValue("All Locations");
        districtFilter.setValue("All Districts");
        readyOrdersTable.getItems().setAll(allReadyOrders);
        readyOrdersTable.refresh();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        userLabel.setText("Welcome, " + user.getUsername());
        loadOrders();
    }

    private void setupReadyOrdersTable() {
        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrderId()));
        orderIdCol.setPrefWidth(150);

        TableColumn<Order, String> restaurantCol = new TableColumn<>("Restaurant");
        restaurantCol.setCellValueFactory(data -> {
            Restaurant restaurant = databaseService.getRestaurant(data.getValue().getRestaurantId());
            return new javafx.beans.property.SimpleStringProperty(restaurant != null ? restaurant.getName() : "Unknown");
        });
        restaurantCol.setPrefWidth(150);

        TableColumn<Order, String> locationCol = new TableColumn<>("Location");
        locationCol.setCellValueFactory(data -> {
            Restaurant restaurant = databaseService.getRestaurant(data.getValue().getRestaurantId());
            String location = restaurant != null ? (restaurant.getDistrict() + ", " + restaurant.getDivision()) : "Unknown";
            return new javafx.beans.property.SimpleStringProperty(location);
        });
        locationCol.setPrefWidth(150);

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserId()));
        customerCol.setPrefWidth(120);

        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(data -> {
            int itemCount = data.getValue().getItems().size();
            return new javafx.beans.property.SimpleStringProperty(itemCount + " item(s)");
        });
        itemsCol.setPrefWidth(100);

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("৳" + String.format("%.2f", data.getValue().getTotalPrice())));
        totalCol.setPrefWidth(100);

        TableColumn<Order, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(data -> {
            String payment = data.getValue().getPaymentMethod();
            return new javafx.beans.property.SimpleStringProperty(payment != null ? payment : "N/A");
        });
        paymentCol.setPrefWidth(120);

        TableColumn<Order, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(180);
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

        readyOrdersTable.getColumns().addAll(orderIdCol, restaurantCol, locationCol, customerCol, itemsCol, totalCol, paymentCol, actionCol);
    }

    private void setupDeliveredOrdersTable() {
        TableColumn<Order, String> orderIdCol = new TableColumn<>("Order ID");
        orderIdCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getOrderId()));
        orderIdCol.setPrefWidth(150);

        TableColumn<Order, String> restaurantCol = new TableColumn<>("Restaurant");
        restaurantCol.setCellValueFactory(data -> {
            Restaurant restaurant = databaseService.getRestaurant(data.getValue().getRestaurantId());
            return new javafx.beans.property.SimpleStringProperty(restaurant != null ? restaurant.getName() : "Unknown");
        });
        restaurantCol.setPrefWidth(150);

        TableColumn<Order, String> customerCol = new TableColumn<>("Customer");
        customerCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUserId()));
        customerCol.setPrefWidth(120);

        TableColumn<Order, String> itemsCol = new TableColumn<>("Items");
        itemsCol.setCellValueFactory(data -> {
            int itemCount = data.getValue().getItems().size();
            return new javafx.beans.property.SimpleStringProperty(itemCount + " item(s)");
        });
        itemsCol.setPrefWidth(100);

        TableColumn<Order, String> totalCol = new TableColumn<>("Total");
        totalCol.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty("৳" + String.format("%.2f", data.getValue().getTotalPrice())));
        totalCol.setPrefWidth(100);

        TableColumn<Order, String> paymentCol = new TableColumn<>("Payment");
        paymentCol.setCellValueFactory(data -> {
            String payment = data.getValue().getPaymentMethod();
            return new javafx.beans.property.SimpleStringProperty(payment != null ? payment : "N/A");
        });
        paymentCol.setPrefWidth(120);

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
                    if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                        setText("✓✓ " + status);
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else {
                        setText(status);
                        setStyle("");
                    }
                }
            }
        });

        deliveredOrdersTable.getColumns().addAll(orderIdCol, restaurantCol, customerCol, itemsCol, totalCol, paymentCol, statusCol);
    }

    private void loadOrders() {
        List<Order> allOrders = databaseService.getAllOrders();

        List<Order> readyOrders = new ArrayList<>();
        List<Order> deliveredOrders = new ArrayList<>();

        for (Order order : allOrders) {
            if (order.getStatus() == Order.OrderStatus.READY) {
                readyOrders.add(order);
            } else if (order.getStatus() == Order.OrderStatus.DELIVERED) {
                if (order.getBikerId() != null && order.getBikerId().equals(currentUser.getUsername())) {
                    deliveredOrders.add(order);
                }
            }
        }

        readyOrders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
        deliveredOrders.sort((o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));

        allReadyOrders = readyOrders;
        handleFilter();
        deliveredOrdersTable.getItems().setAll(deliveredOrders);

        readyOrdersTable.refresh();
        deliveredOrdersTable.refresh();
    }

    private void handleOrderStatus(Order order) {
        Dialog<ButtonType> statusDialog = new Dialog<>();
        statusDialog.setTitle("Order Status Management");
        statusDialog.setHeaderText("Deliver Order: " + order.getOrderId());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Restaurant restaurant = databaseService.getRestaurant(order.getRestaurantId());
        
        // Restaurant location info - prominently displayed
        Label locationLabel = new Label("📍 Location: " + (restaurant != null ? restaurant.getDivision() : "Unknown"));
        locationLabel.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        
        Label restaurantLabel = new Label("Restaurant: " + (restaurant != null ? restaurant.getName() : "Unknown"));
        restaurantLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold;");

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
            } else {
                tickBox.setText("");
                tickBox.setStyle("-fx-font-size: 14; -fx-text-fill: #95a5a6;");
            }

            stageRow.getChildren().addAll(stageLabel, stageSpacer, tickBox);
            stagesBox.getChildren().add(stageRow);
        }

        content.getChildren().addAll(locationLabel, restaurantLabel, customerLabel, totalLabel, stagesBox);

        statusDialog.getDialogPane().setContent(content);
        statusDialog.getDialogPane().setPrefWidth(450);

        if (order.getStatus() == Order.OrderStatus.READY && (order.getBikerId() == null || !order.getBikerId().equals(currentUser.getUsername()))) {
            ButtonType confirmBtn = new ButtonType("Confirm Delivery", ButtonBar.ButtonData.OK_DONE);
            ButtonType declineBtn = new ButtonType("Decline Order", ButtonBar.ButtonData.OTHER);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            statusDialog.getDialogPane().getButtonTypes().setAll(confirmBtn, declineBtn, cancelButton);

            statusDialog.showAndWait().ifPresent(response -> {
                if (response == confirmBtn) {
                    confirmOrder(order, restaurant);
                } else if (response == declineBtn) {
                    declineOrder(order);
                }
            });
        } else {
            ButtonType deliverBtn = new ButtonType("Mark as Delivered", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

            statusDialog.getDialogPane().getButtonTypes().setAll(deliverBtn, cancelButton);

            statusDialog.showAndWait().ifPresent(response -> {
                if (response == deliverBtn) {
                    markAsDelivered(order);
                }
            });
        }
    }

    private String getStageStatus(Order.OrderStatus currentStatus, Order.OrderStatus stageStatus) {
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

    private void markAsDelivered(Order order) {
        String paymentMethod = order.getPaymentMethod();
        boolean isCOD = paymentMethod != null && paymentMethod.equals("Cash on Delivery");

        if (isCOD) {
            Alert codAlert = new Alert(Alert.AlertType.CONFIRMATION);
            codAlert.setTitle("Cash on Delivery Confirmation");
            codAlert.setHeaderText("Confirm COD Payment: " + order.getOrderId());
            codAlert.setContentText("Have you received the cash payment of ৳" + String.format("%.2f", order.getTotalPrice()) + " from the customer?");
            
            codAlert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            codAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    order.setStatus(Order.OrderStatus.DELIVERED);
                    databaseService.saveDataToFiles();
                    loadOrders();

                    Restaurant restaurant = databaseService.getRestaurant(order.getRestaurantId());
                    String restaurantName = restaurant != null ? restaurant.getName() : "Unknown";

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Order marked as delivered successfully!\n\nCash on Delivery of Order #" + order.getOrderId() + " is received.\nPlease deliver the payment to " + restaurantName + " restaurant.");
                    successAlert.showAndWait();
                }
            });
        } else {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("Mark as Delivered");
            confirmAlert.setHeaderText("Confirm Delivery: " + order.getOrderId());
            confirmAlert.setContentText("Are you sure you want to mark this order as delivered?");

            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    order.setStatus(Order.OrderStatus.DELIVERED);
                    databaseService.saveDataToFiles();
                    loadOrders();

                    Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                    successAlert.setTitle("Success");
                    successAlert.setHeaderText(null);
                    successAlert.setContentText("Order marked as delivered successfully!");
                    successAlert.showAndWait();
                }
            });
        }
    }

    private void confirmOrder(Order order, Restaurant restaurant) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delivery");
        confirmAlert.setHeaderText("Accept Delivery Task");
        confirmAlert.setContentText("Do you want to confirm delivery for this order from " + 
                                   (restaurant != null ? restaurant.getName() : "Unknown Restaurant") + 
                                   "?\n\nLocation: " + (restaurant != null ? restaurant.getDivision() : "Unknown"));

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                order.setBikerId(currentUser.getUsername());
                databaseService.saveDataToFiles();
                loadOrders();
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Success");
                successAlert.setHeaderText(null);
                successAlert.setContentText("You have confirmed the delivery task!\n\nPlease pick up the order and deliver it to the customer.");
                successAlert.showAndWait();
            }
        });
    }

    private void declineOrder(Order order) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Decline Order");
        confirmAlert.setHeaderText("Decline Delivery Task");
        confirmAlert.setContentText("Are you sure you want to decline this delivery?\n\nThe order will remain available for other bikers.");

        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Order remains READY for other bikers
                Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
                infoAlert.setTitle("Declined");
                infoAlert.setHeaderText(null);
                infoAlert.setContentText("You have declined this delivery task.\n\nThe order remains available for other bikers.");
                infoAlert.showAndWait();
            }
        });
    }

    @FXML
    public void handleRefreshOrders() {
        loadOrders();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Refreshed");
        alert.setHeaderText("Orders Updated");
        alert.setContentText("Order list has been refreshed successfully!");
        alert.showAndWait();
    }

    @FXML
    public void handleBalance() {
        int deliveredCount = 0;
        List<Order> allOrders = databaseService.getAllOrders();
        for (Order order : allOrders) {
            if (order.getStatus() == Order.OrderStatus.DELIVERED && 
                order.getBikerId() != null && 
                order.getBikerId().equals(currentUser.getUsername())) {
                deliveredCount++;
            }
        }
        int totalIncome = deliveredCount * 100;
        
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Biker Balance");
        dialog.setHeaderText("💰 Your Income Summary");
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label summaryLabel = new Label("Total Deliveries: " + deliveredCount + "\n" +
                           "Income per Delivery: ৳100\n" +
                           "─────────────────\n" +
                           "Total Balance: ৳" + totalIncome);
        summaryLabel.setStyle("-fx-font-size: 14;");
        
        content.getChildren().add(summaryLabel);
        
        dialog.getDialogPane().setContent(content);
        
        ButtonType withdrawBtn = new ButtonType("Withdraw", ButtonBar.ButtonData.OK_DONE);
        ButtonType closeBtn = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(withdrawBtn, closeBtn);
        
        final int finalTotalIncome = totalIncome;
        dialog.showAndWait().ifPresent(response -> {
            if (response == withdrawBtn) {
                showWithdrawDialog(finalTotalIncome);
            }
        });
    }
    
    private void showWithdrawDialog(int balance) {
        Dialog<ButtonType> withdrawDialog = new Dialog<>();
        withdrawDialog.setTitle("Withdraw Balance");
        withdrawDialog.setHeaderText("💳 Withdraw ৳" + balance);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label methodLabel = new Label("Select Withdrawal Method:");
        methodLabel.setStyle("-fx-font-weight: bold;");
        
        ComboBox<String> methodCombo = new ComboBox<>();
        methodCombo.getItems().addAll("Bank Account", "Bkash", "Nagad");
        methodCombo.setValue("Bkash");
        methodCombo.setStyle("-fx-pref-width: 200;");
        
        Label accountLabel = new Label("Account Number:");
        accountLabel.setStyle("-fx-font-weight: bold;");
        
        TextField accountField = new TextField();
        accountField.setPromptText("Enter account number");
        accountField.setStyle("-fx-pref-width: 200;");
        
        content.getChildren().addAll(methodLabel, methodCombo, accountLabel, accountField);
        
        withdrawDialog.getDialogPane().setContent(content);
        withdrawDialog.getDialogPane().setPrefWidth(350);
        
        ButtonType getOtpBtn = new ButtonType("Get OTP", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        withdrawDialog.getDialogPane().getButtonTypes().addAll(getOtpBtn, cancelBtn);
        
        withdrawDialog.showAndWait().ifPresent(response -> {
            if (response == getOtpBtn) {
                String method = methodCombo.getValue();
                String account = accountField.getText().trim();
                
                if (account.isEmpty()) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Missing Information");
                    errorAlert.setContentText("Please enter account number.");
                    errorAlert.showAndWait();
                    return;
                }
                
                String generatedOtp = String.valueOf((int)(Math.random() * 900000) + 100000);
                showOtpVerificationDialog(balance, method, account, generatedOtp);
            }
        });
    }
    
    private void showOtpVerificationDialog(int balance, String method, String account, String generatedOtp) {
        Alert otpAlert = new Alert(Alert.AlertType.INFORMATION);
        otpAlert.setTitle("OTP Sent");
        otpAlert.setHeaderText("📩 OTP Sent to " + account);
        otpAlert.setContentText("Your OTP is: " + generatedOtp + "\n\nPlease use this OTP to complete the withdrawal.");
        otpAlert.showAndWait();
        
        Dialog<ButtonType> otpDialog = new Dialog<>();
        otpDialog.setTitle("Verify OTP");
        otpDialog.setHeaderText("🔐 Enter OTP to Withdraw ৳" + balance);
        
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        
        Label infoLabel = new Label("Method: " + method + "\nAccount: " + account);
        infoLabel.setStyle("-fx-font-size: 12;");
        
        Label otpLabel = new Label("Enter OTP:");
        otpLabel.setStyle("-fx-font-weight: bold;");
        
        TextField otpField = new TextField();
        otpField.setPromptText("Enter 6-digit OTP");
        otpField.setStyle("-fx-pref-width: 200;");
        
        content.getChildren().addAll(infoLabel, otpLabel, otpField);
        
        otpDialog.getDialogPane().setContent(content);
        otpDialog.getDialogPane().setPrefWidth(350);
        
        ButtonType confirmBtn = new ButtonType("Confirm Withdraw", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        otpDialog.getDialogPane().getButtonTypes().addAll(confirmBtn, cancelBtn);
        
        otpDialog.showAndWait().ifPresent(response -> {
            if (response == confirmBtn) {
                String enteredOtp = otpField.getText().trim();
                
                if (enteredOtp.isEmpty()) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Missing OTP");
                    errorAlert.setContentText("Please enter the OTP.");
                    errorAlert.showAndWait();
                    return;
                }
                
                if (!enteredOtp.equals(generatedOtp)) {
                    Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                    errorAlert.setTitle("Error");
                    errorAlert.setHeaderText("Invalid OTP");
                    errorAlert.setContentText("The OTP you entered is incorrect. Please try again.");
                    errorAlert.showAndWait();
                    return;
                }
                
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("Withdrawal Successful");
                successAlert.setHeaderText("✅ Withdrawal Complete");
                successAlert.setContentText("Successfully withdrawn ৳" + balance + "\n\n" +
                        "Method: " + method + "\n" +
                        "Account: " + account + "\n\n" +
                        "The amount will be transferred within 24 hours.");
                successAlert.showAndWait();
            }
        });
    }

    @FXML
    public void handleLogout() {
        try {
            javafx.fxml.FXMLLoader fxmlLoader = new javafx.fxml.FXMLLoader(getClass().getResource("signin-view.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(fxmlLoader.load(), 600, 700);
            Stage stage = (Stage) userLabel.getScene().getWindow();
            stage.setTitle("FoodBike - Sign In");
            stage.setScene(scene);
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
