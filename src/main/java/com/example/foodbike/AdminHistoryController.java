package com.example.foodbike;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.List;
import java.util.stream.Collectors;

public class AdminHistoryController {
    @FXML private VBox historyBox;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label totalCountLabel;
    
    private DatabaseService databaseService;
    private User currentUser;
    private List<AdminAction> allActions;
    
    @FXML
    public void initialize() {
        databaseService = DatabaseService.getInstance();
        setupFilterCombo();
        loadHistory();
    }
    
    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
    
    private void setupFilterCombo() {
        filterCombo.setItems(FXCollections.observableArrayList(
            "All Actions",
            "Approved Applications",
            "Rejected Applications",
            "Added Restaurants",
            "Deleted Restaurants",
            "Menu Edits"
        ));
        filterCombo.setValue("All Actions");
    }
    
    private void loadHistory() {
        historyBox.getChildren().clear();
        allActions = databaseService.getAllAdminActions();
        
        totalCountLabel.setText("Total Actions: " + allActions.size());
        
        if (allActions.isEmpty()) {
            Label emptyLabel = new Label("No admin actions recorded yet.");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d; -fx-padding: 20;");
            historyBox.getChildren().add(emptyLabel);
            return;
        }
        
        for (AdminAction action : allActions) {
            VBox actionCard = createActionCard(action);
            historyBox.getChildren().add(actionCard);
        }
    }
    
    private VBox createActionCard(AdminAction action) {
        VBox card = new VBox(8);
        card.setStyle("-fx-border-color: #ddd; -fx-border-radius: 6; -fx-padding: 15; -fx-background-color: white; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        HBox headerBox = new HBox(10);
        headerBox.setStyle("-fx-alignment: center-left;");
        
        Label typeLabel = new Label(getActionIcon(action.getActionType()) + " " + getActionTypeText(action.getActionType()));
        typeLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: " + getActionColor(action.getActionType()) + ";");
        
        Label timestampLabel = new Label(action.getFormattedTimestamp());
        timestampLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #7f8c8d;");
        
        headerBox.getChildren().addAll(typeLabel, new Label("•"), timestampLabel);
        
        Label descriptionLabel = new Label(action.getActionDescription());
        descriptionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #2c3e50;");
        descriptionLabel.setWrapText(true);
        
        Label detailsLabel = new Label(action.getDetails());
        detailsLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #555; -fx-padding: 5 0 0 0;");
        detailsLabel.setWrapText(true);
        
        Label adminLabel = new Label("By: " + action.getAdminUsername());
        adminLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #95a5a6; -fx-font-style: italic;");
        
        card.getChildren().addAll(headerBox, descriptionLabel, detailsLabel, adminLabel);
        
        return card;
    }
    
    private String getActionIcon(AdminAction.ActionType type) {
        switch (type) {
            case APPROVED_APPLICATION:
                return "✓";
            case REJECTED_APPLICATION:
                return "✗";
            case ADDED_RESTAURANT:
                return "➕";
            case DELETED_RESTAURANT:
                return "🗑";
            case EDITED_MENU:
            case ADDED_MENU_ITEM:
            case EDITED_MENU_ITEM:
                return "✏";
            default:
                return "•";
        }
    }
    
    private String getActionTypeText(AdminAction.ActionType type) {
        switch (type) {
            case APPROVED_APPLICATION:
                return "Application Approved";
            case REJECTED_APPLICATION:
                return "Application Rejected";
            case ADDED_RESTAURANT:
                return "Restaurant Added";
            case DELETED_RESTAURANT:
                return "Restaurant Deleted";
            case EDITED_MENU:
                return "Menu Edited";
            case ADDED_MENU_ITEM:
                return "Menu Item Added";
            case EDITED_MENU_ITEM:
                return "Menu Item Edited";
            default:
                return "Action Performed";
        }
    }
    
    private String getActionColor(AdminAction.ActionType type) {
        switch (type) {
            case APPROVED_APPLICATION:
            case ADDED_RESTAURANT:
            case ADDED_MENU_ITEM:
                return "#27ae60";
            case REJECTED_APPLICATION:
            case DELETED_RESTAURANT:
                return "#e74c3c";
            case EDITED_MENU:
            case EDITED_MENU_ITEM:
                return "#3498db";
            default:
                return "#2c3e50";
        }
    }
    
    @FXML
    public void handleFilter() {
        String selectedFilter = filterCombo.getValue();
        historyBox.getChildren().clear();
        
        List<AdminAction> filteredActions = allActions;
        
        switch (selectedFilter) {
            case "Approved Applications":
                filteredActions = allActions.stream()
                    .filter(a -> a.getActionType() == AdminAction.ActionType.APPROVED_APPLICATION)
                    .collect(Collectors.toList());
                break;
            case "Rejected Applications":
                filteredActions = allActions.stream()
                    .filter(a -> a.getActionType() == AdminAction.ActionType.REJECTED_APPLICATION)
                    .collect(Collectors.toList());
                break;
            case "Added Restaurants":
                filteredActions = allActions.stream()
                    .filter(a -> a.getActionType() == AdminAction.ActionType.ADDED_RESTAURANT)
                    .collect(Collectors.toList());
                break;
            case "Deleted Restaurants":
                filteredActions = allActions.stream()
                    .filter(a -> a.getActionType() == AdminAction.ActionType.DELETED_RESTAURANT)
                    .collect(Collectors.toList());
                break;
            case "Menu Edits":
                filteredActions = allActions.stream()
                    .filter(a -> a.getActionType() == AdminAction.ActionType.EDITED_MENU ||
                               a.getActionType() == AdminAction.ActionType.ADDED_MENU_ITEM ||
                               a.getActionType() == AdminAction.ActionType.EDITED_MENU_ITEM)
                    .collect(Collectors.toList());
                break;
            default:
                filteredActions = allActions;
                break;
        }
        
        totalCountLabel.setText("Showing: " + filteredActions.size() + " actions");
        
        if (filteredActions.isEmpty()) {
            Label emptyLabel = new Label("No actions found for this filter.");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #7f8c8d; -fx-padding: 20;");
            historyBox.getChildren().add(emptyLabel);
            return;
        }
        
        for (AdminAction action : filteredActions) {
            VBox actionCard = createActionCard(action);
            historyBox.getChildren().add(actionCard);
        }
    }
    
    @FXML
    public void handleClear() {
        filterCombo.setValue("All Actions");
        loadHistory();
    }
    
    @FXML
    public void handleViewAllApplications() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("All Applications History");
        dialog.setHeaderText("Complete Application Records");
        
        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));
        
        List<RestaurantApplication> allApplications = databaseService.getAllApplications();
        
        if (allApplications.isEmpty()) {
            Label emptyLabel = new Label("No applications found.");
            emptyLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #7f8c8d;");
            vbox.getChildren().add(emptyLabel);
        } else {
            for (RestaurantApplication app : allApplications) {
                VBox appCard = new VBox(5);
                String borderColor = app.getStatus() == RestaurantApplication.ApplicationStatus.PENDING ? "#f39c12" :
                                   app.getStatus() == RestaurantApplication.ApplicationStatus.APPROVED ? "#27ae60" : "#e74c3c";
                String bgColor = app.getStatus() == RestaurantApplication.ApplicationStatus.PENDING ? "#fff3cd" :
                               app.getStatus() == RestaurantApplication.ApplicationStatus.APPROVED ? "#d4edda" : "#f8d7da";
                appCard.setStyle("-fx-border-color: " + borderColor + "; -fx-border-width: 2; -fx-border-radius: 4; -fx-padding: 10; -fx-background-color: " + bgColor + ";");
                
                Label nameLabel = new Label("Restaurant: " + app.getRestaurantName());
                nameLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
                
                Label statusLabel = new Label("Status: " + app.getStatus());
                statusLabel.setStyle("-fx-font-size: 11; -fx-font-weight: bold;");
                
                Label entrepreneurLabel = new Label("By: " + app.getEntrepreneurUsername());
                entrepreneurLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #555;");
                
                Label locationLabel = new Label("Location: " + app.getDivision() + " - " + app.getAddress());
                locationLabel.setStyle("-fx-font-size: 10;");
                
                appCard.getChildren().addAll(nameLabel, statusLabel, entrepreneurLabel, locationLabel);
                
                if (!app.getAdminMessage().isEmpty()) {
                    Label messageLabel = new Label("Admin Message: " + app.getAdminMessage());
                    messageLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #555; -fx-font-style: italic;");
                    messageLabel.setWrapText(true);
                    appCard.getChildren().add(messageLabel);
                }
                
                vbox.getChildren().add(appCard);
            }
        }
        
        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setPrefHeight(500);
        scrollPane.setPrefWidth(600);
        scrollPane.setFitToWidth(true);
        
        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    @FXML
    public void handleClose() {
        Stage stage = (Stage) historyBox.getScene().getWindow();
        stage.close();
    }
}
