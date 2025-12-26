package com.example.foodbike;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminAction implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum ActionType {
        APPROVED_APPLICATION,
        REJECTED_APPLICATION,
        ADDED_RESTAURANT,
        DELETED_RESTAURANT,
        EDITED_MENU,
        ADDED_MENU_ITEM,
        EDITED_MENU_ITEM
    }
    
    private String actionId;
    private String adminUsername;
    private ActionType actionType;
    private String targetName;
    private String details;
    private LocalDateTime timestamp;
    
    public AdminAction(String adminUsername, ActionType actionType, String targetName, String details) {
        this.actionId = "ACT_" + System.currentTimeMillis();
        this.adminUsername = adminUsername;
        this.actionType = actionType;
        this.targetName = targetName;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }
    
    public String getActionId() {
        return actionId;
    }
    
    public void setActionId(String actionId) {
        this.actionId = actionId;
    }
    
    public String getAdminUsername() {
        return adminUsername;
    }
    
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }
    
    public ActionType getActionType() {
        return actionType;
    }
    
    public void setActionType(ActionType actionType) {
        this.actionType = actionType;
    }
    
    public String getTargetName() {
        return targetName;
    }
    
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    
    public String getDetails() {
        return details;
    }
    
    public void setDetails(String details) {
        this.details = details;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getFormattedTimestamp() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
        return timestamp.format(formatter);
    }
    
    public String getActionDescription() {
        switch (actionType) {
            case APPROVED_APPLICATION:
                return "Approved application for " + targetName;
            case REJECTED_APPLICATION:
                return "Rejected application for " + targetName;
            case ADDED_RESTAURANT:
                return "Added restaurant: " + targetName;
            case DELETED_RESTAURANT:
                return "Deleted restaurant: " + targetName;
            case EDITED_MENU:
                return "Edited menu for " + targetName;
            case ADDED_MENU_ITEM:
                return "Added menu item to " + targetName;
            case EDITED_MENU_ITEM:
                return "Edited menu item in " + targetName;
            default:
                return "Performed action on " + targetName;
        }
    }
}
