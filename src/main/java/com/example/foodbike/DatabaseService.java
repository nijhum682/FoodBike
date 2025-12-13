package com.example.foodbike;

import java.io.*;
import java.util.*;

public class DatabaseService {
    private static DatabaseService instance;
    private Map<String, User> users;
    private Map<String, Restaurant> restaurants;
    private Map<String, Order> orders;
    private static final String USERS_FILE = "users.dat";
    private static final String RESTAURANTS_FILE = "restaurants.dat";
    private static final String ORDERS_FILE = "orders.dat";

    private DatabaseService() {
        users = new HashMap<>();
        restaurants = new HashMap<>();
        orders = new HashMap<>();
        loadDataFromFiles();
        if (users.isEmpty()) {
            initializeSampleData();
            saveDataToFiles();
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    // Load data from persistent files
    @SuppressWarnings("unchecked")
    private void loadDataFromFiles() {
        // Load users
        File usersFile = new File(USERS_FILE);
        if (usersFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(usersFile))) {
                users = (Map<String, User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading users file: " + e.getMessage());
                users = new HashMap<>();
            }
        }

        // Load restaurants
        File restaurantsFile = new File(RESTAURANTS_FILE);
        if (restaurantsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(restaurantsFile))) {
                restaurants = (Map<String, Restaurant>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading restaurants file: " + e.getMessage());
                restaurants = new HashMap<>();
            }
        }

        // Load orders
        File ordersFile = new File(ORDERS_FILE);
        if (ordersFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ordersFile))) {
                orders = (Map<String, Order>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading orders file: " + e.getMessage());
                orders = new HashMap<>();
            }
        }
    }

    // Save data to persistent files
    private void saveDataToFiles() {
        // Save users
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.out.println("Error saving users file: " + e.getMessage());
        }

        // Save restaurants
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RESTAURANTS_FILE))) {
            oos.writeObject(restaurants);
        } catch (IOException e) {
            System.out.println("Error saving restaurants file: " + e.getMessage());
        }

        // Save orders
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ORDERS_FILE))) {
            oos.writeObject(orders);
        } catch (IOException e) {
            System.out.println("Error saving orders file: " + e.getMessage());
        }
    }

    private void initializeSampleData() {
        // Add sample users
        users.put("admin1", new User("admin1", "Admin@123", "admin@foodbike.com", "01234567890", User.UserType.ADMIN));
        users.put("user1", new User("user1", "User@123", "user@foodbike.com", "01987654321", User.UserType.USER));
        users.put("entrepreneur1", new User("entrepreneur1", "Entrepreneur@123", "ent@foodbike.com", "01111111111", User.UserType.ENTREPRENEUR));

        // Add sample restaurants
        String[] divisions = {"Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh"};
        String[] restaurantNames = {"Burger King", "Pizza Hut", "KFC", "Biryani House", "Naan Paradise", "Sushi Paradise", 
                                    "Thai Express", "Chinese Delight", "Mexican Fiesta", "Kebab King", "Taco Bell", "Dominos",
                                    "Dunkin Donuts", "Chipotle", "Subway", "Chick-fil-A", "Popeyes", "Five Guys",
                                    "Panera Bread", "In-N-Out", "Shake Shack", "Culver's", "Whataburger", "Cook Out",
                                    "Bojangles", "Wingstop", "Raising Cane's", "Jollibee", "Zaxby's", "Jack in the Box",
                                    "Sonic Drive-In", "Carl's Jr", "Wendy's", "Taco John's", "Del Taco", "Qdoba",
                                    "Panda Express", "Asian Station", "Ramen House", "Pho King Good", "Dim Sum Palace", "Mongolian Grill",
                                    "Indian Curry House", "Tandoori Palace", "Spice Route", "Samosa Corner", "Dhaba Style", "Biryani Delight",
                                    "Middle Eastern Grill", "Shawarma King", "Falafel Express", "Hummus Hub", "Lebanese Kitchen", "Turkish Delight",
                                    "Pasta Paradise", "Italian Kitchen", "Trattoria Roma", "Mozzarella House", "Risotto Magic", "Carbonara King",
                                    "Vegan Garden", "Green Bowl", "Salad Station", "Smoothie Bar", "Juice Junction", "Acai Bowl",
                                    "Chocolate Shop", "Dessert Paradise", "Ice Cream Dreams", "Donut Palace", "Cake House", "Pastry Corner",
                                    "Coffee House", "Tea Paradise", "Cappuccino King", "Espresso Express", "Latte Lounge", "Mocha Moments",
                                    "Breakfast Club", "Pancake House", "Waffle King", "Omelette Station", "Toast&Jam", "Bagel Bakery",
                                    "Seafood Shack", "Fish&Chips", "Crab Palace", "Lobster House", "Oyster Bar", "Prawn Paradise",
                                    "Steak House", "BBQ Pit", "Grilled Delights", "Meat Lovers", "Ribeye King", "Lamb Chops"};

        int restaurantId = 1;
        for (int i = 0; i < 100; i++) {
            String name = restaurantNames[i % restaurantNames.length];
            if (i >= restaurantNames.length) {
                name = name + " " + (i / restaurantNames.length);
            }
            String division = divisions[i % divisions.length];
            Restaurant restaurant = new Restaurant("rest_" + restaurantId, name, division, "Address " + restaurantId + ", " + division);
            
            // Set random rating between 1.0 and 5.0 with variety
            double randomRating = 1.0 + Math.random() * 4.0;
            randomRating = Math.round(randomRating * 10) / 10.0; // Round to 1 decimal place
            restaurant.setRating(randomRating);
            
            // Add some menu items
            restaurant.addMenuItem(new MenuItem("item_" + restaurantId + "_1", "Special Combo", "Our signature dish", 250));
            restaurant.addMenuItem(new MenuItem("item_" + restaurantId + "_2", "Deluxe Meal", "Premium items", 350));
            restaurant.addMenuItem(new MenuItem("item_" + restaurantId + "_3", "Basic Meal", "Standard items", 150));
            restaurant.addMenuItem(new MenuItem("item_" + restaurantId + "_4", "Beverage", "Drinks and juices", 50));
            
            restaurants.put("rest_" + restaurantId, restaurant);
            restaurantId++;
        }
    }

    // User Methods
    public boolean registerUser(String username, String email, String phoneNumber, String password, User.UserType userType) {
        if (users.containsKey(username)) {
            return false;
        }
        User user = new User(username, password, email, phoneNumber, userType);
        users.put(username, user);
        saveDataToFiles();
        return true;
    }

    public User loginUser(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    public User getUser(String username) {
        return users.get(username);
    }

    public boolean userExists(String username) {
        return users.containsKey(username);
    }

    // Restaurant Methods
    public List<Restaurant> getAllRestaurants() {
        return new ArrayList<>(restaurants.values());
    }

    public List<Restaurant> searchRestaurants(String query) {
        List<Restaurant> results = new ArrayList<>();
        for (Restaurant restaurant : restaurants.values()) {
            if (restaurant.getName().toLowerCase().contains(query.toLowerCase())) {
                results.add(restaurant);
            }
        }
        return results;
    }

    public List<Restaurant> getRestaurantsByDivision(String division) {
        List<Restaurant> results = new ArrayList<>();
        for (Restaurant restaurant : restaurants.values()) {
            if (restaurant.getDivision().equalsIgnoreCase(division)) {
                results.add(restaurant);
            }
        }
        return results;
    }

    public Restaurant getRestaurant(String restaurantId) {
        return restaurants.get(restaurantId);
    }

    public boolean addRestaurant(Restaurant restaurant) {
        if (restaurants.containsKey(restaurant.getId())) {
            return false;
        }
        restaurants.put(restaurant.getId(), restaurant);
        return true;
    }

    public boolean deleteRestaurant(String restaurantId) {
        return restaurants.remove(restaurantId) != null;
    }

    public List<String> getAllDivisions() {
        Set<String> divisionsSet = new HashSet<>();
        for (Restaurant restaurant : restaurants.values()) {
            divisionsSet.add(restaurant.getDivision());
        }
        List<String> divisions = new ArrayList<>(divisionsSet);
        Collections.sort(divisions);
        return divisions;
    }

    // Order Methods
    public void createOrder(Order order) {
        orders.put(order.getOrderId(), order);
    }

    public Order getOrder(String orderId) {
        return orders.get(orderId);
    }

    public List<Order> getUserOrders(String userId) {
        List<Order> userOrders = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getUserId().equals(userId)) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    public List<Order> getRestaurantOrders(String restaurantId) {
        List<Order> restaurantOrders = new ArrayList<>();
        for (Order order : orders.values()) {
            if (order.getRestaurantId().equals(restaurantId)) {
                restaurantOrders.add(order);
            }
        }
        return restaurantOrders;
    }
}
