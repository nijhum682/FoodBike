package com.example.foodbike;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Restaurant implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String division;
    private String address;
    private List<MenuItem> menu;
    private double rating;

    public Restaurant(String id, String name, String division, String address) {
        this.id = id;
        this.name = name;
        this.division = division;
        this.address = address;
        this.menu = new ArrayList<>();
        this.rating = 4.5;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDivision() {
        return division;
    }

    public void setDivision(String division) {
        this.division = division;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<MenuItem> getMenu() {
        return menu;
    }

    public void addMenuItem(MenuItem item) {
        this.menu.add(item);
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return name + " - " + division + " (" + rating + "★)";
    }
}
