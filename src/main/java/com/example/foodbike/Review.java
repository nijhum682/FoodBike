package com.example.foodbike;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Review implements Serializable {
    private static final long serialVersionUID = 1L;

    private String reviewId;
    private String restaurantId;
    private String userId;
    private String orderId;
    private int rating;
    private String comment;
    private LocalDateTime createdAt;

    public Review(String reviewId, String restaurantId, String userId, String orderId, int rating, String comment) {
        this.reviewId = reviewId;
        this.restaurantId = restaurantId;
        this.userId = userId;
        this.orderId = orderId;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = LocalDateTime.now();
    }

    public String getReviewId() {
        return reviewId;
    }

    public void setReviewId(String reviewId) {
        this.reviewId = reviewId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
