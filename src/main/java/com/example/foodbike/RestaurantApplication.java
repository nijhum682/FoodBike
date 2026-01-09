package com.example.foodbike;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RestaurantApplication implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String applicationId;
    private String entrepreneurUsername;
    private String restaurantName;
    private String division;
    private String district;
    private String address;
    private double rating;
    private List<MenuItem> menuItems;
    private ApplicationStatus status;
    private LocalDateTime appliedDate;
    private String adminMessage;
    private boolean messageViewed;
    
    public enum ApplicationStatus {
        PENDING, APPROVED, REJECTED
    }
    
    public RestaurantApplication(String applicationId, String entrepreneurUsername, 
                                String restaurantName, String division, String district, String address, double rating) {
        this.applicationId = applicationId;
        this.entrepreneurUsername = entrepreneurUsername;
        this.restaurantName = restaurantName;
        this.division = division;
        this.district = district;
        this.address = address;
        this.rating = rating;
        this.menuItems = new ArrayList<>();
        this.status = ApplicationStatus.PENDING;
        this.appliedDate = LocalDateTime.now();
        this.adminMessage = "";
        this.messageViewed = false;
    }
    
    public String getApplicationId() {
        return applicationId;
    }
    
    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }
    
    public String getEntrepreneurUsername() {
        return entrepreneurUsername;
    }
    
    public void setEntrepreneurUsername(String entrepreneurUsername) {
        this.entrepreneurUsername = entrepreneurUsername;
    }
    
    public String getRestaurantName() {
        return restaurantName;
    }
    
    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }
    
    public String getDivision() {
        return division;
    }
    
    public void setDivision(String division) {
        this.division = division;
    }
    
    public String getDistrict() {
        return district;
    }
    
    public void setDistrict(String district) {
        this.district = district;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public double getRating() {
        return rating;
    }
    
    public void setRating(double rating) {
        this.rating = rating;
    }
    
    public List<MenuItem> getMenuItems() {
        return menuItems;
    }
    
    public void addMenuItem(MenuItem item) {
        this.menuItems.add(item);
    }
    
    public ApplicationStatus getStatus() {
        return status;
    }
    
    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getAppliedDate() {
        return appliedDate;
    }
    
    public void setAppliedDate(LocalDateTime appliedDate) {
        this.appliedDate = appliedDate;
    }
    
    public String getAdminMessage() {
        return adminMessage;
    }
    
    public void setAdminMessage(String adminMessage) {
        this.adminMessage = adminMessage;
    }
    
    public boolean isMessageViewed() {
        return messageViewed;
    }
    
    public void setMessageViewed(boolean messageViewed) {
        this.messageViewed = messageViewed;
    }
}
