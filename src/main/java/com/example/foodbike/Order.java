package com.example.foodbike;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum OrderStatus {
        PENDING, CONFIRMED, PREPARING, READY, DELIVERED, CANCELLED
    }

    private String orderId;
    private String userId;
    private String restaurantId;
    private List<MenuItem> items;
    private double totalPrice;
    private OrderStatus status;
    private LocalDateTime createdAt;

    public Order(String orderId, String userId, String restaurantId) {
        this.orderId = orderId;
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.items = new ArrayList<>();
        this.totalPrice = 0;
        this.status = OrderStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void addItem(MenuItem item) {
        this.items.add(item);
        this.totalPrice += item.getPrice();
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
