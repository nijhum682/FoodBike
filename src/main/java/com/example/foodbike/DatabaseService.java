package com.example.foodbike;

import java.io.*;
import java.util.*;

public class DatabaseService {
    private static DatabaseService instance;
    private Map<String, User> users;
    private Map<String, Restaurant> restaurants;
    private Map<String, Order> orders;
    private Map<String, RestaurantApplication> applications;
    private Map<String, AdminAction> adminActions;
    private static final String USERS_FILE = "users.dat";
    private static final String RESTAURANTS_FILE = "restaurants.dat";
    private static final String ORDERS_FILE = "orders.dat";
    private static final String APPLICATIONS_FILE = "applications.dat";
    private static final String ADMIN_ACTIONS_FILE = "admin_actions.dat";

    private DatabaseService() {
        users = new HashMap<>();
        restaurants = new HashMap<>();
        orders = new HashMap<>();
        applications = new HashMap<>();
        adminActions = new HashMap<>();
        loadDataFromFiles();
        if (users.isEmpty()) {
            initializeSampleData();
            saveDataToFiles();
        }

        if (restaurants.isEmpty()) {
            initializeRestaurants();
            saveDataToFiles();
        }
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    @SuppressWarnings("unchecked")
    private void loadDataFromFiles() {
        File usersFile = new File(USERS_FILE);
        if (usersFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(usersFile))) {
                users = (Map<String, User>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading users file: " + e.getMessage());
                users = new HashMap<>();
            }
        }

        File restaurantsFile = new File(RESTAURANTS_FILE);
        if (restaurantsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(restaurantsFile))) {
                restaurants = (Map<String, Restaurant>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading restaurants file: " + e.getMessage());
                restaurants = new HashMap<>();
            }
        }

        File ordersFile = new File(ORDERS_FILE);
        if (ordersFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(ordersFile))) {
                orders = (Map<String, Order>) ois.readObject();
                System.out.println("Successfully loaded " + orders.size() + " orders from file.");
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading orders file: " + e.getMessage());
                e.printStackTrace();
                // Try to create backup before clearing
                try {
                    File backup = new File("orders_backup_" + System.currentTimeMillis() + ".dat");
                    java.nio.file.Files.copy(ordersFile.toPath(), backup.toPath());
                    System.out.println("Created backup: " + backup.getName());
                } catch (IOException backupError) {
                    System.out.println("Could not create backup: " + backupError.getMessage());
                }
                orders = new HashMap<>();
            }
        }

        File applicationsFile = new File(APPLICATIONS_FILE);
        if (applicationsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(applicationsFile))) {
                applications = (Map<String, RestaurantApplication>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading applications file: " + e.getMessage());
                applications = new HashMap<>();
            }
        }
        
        File adminActionsFile = new File(ADMIN_ACTIONS_FILE);
        if (adminActionsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(adminActionsFile))) {
                adminActions = (Map<String, AdminAction>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading admin actions file: " + e.getMessage());
                adminActions = new HashMap<>();
            }
        }
    }
    public void saveDataToFiles() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        } catch (IOException e) {
            System.out.println("Error saving users file: " + e.getMessage());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RESTAURANTS_FILE))) {
            oos.writeObject(restaurants);
        } catch (IOException e) {
            System.out.println("Error saving restaurants file: " + e.getMessage());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ORDERS_FILE))) {
            oos.writeObject(orders);
        } catch (IOException e) {
            System.out.println("Error saving orders file: " + e.getMessage());
        }

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(APPLICATIONS_FILE))) {
            oos.writeObject(applications);
        } catch (IOException e) {
            System.out.println("Error saving applications file: " + e.getMessage());
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ADMIN_ACTIONS_FILE))) {
            oos.writeObject(adminActions);
        } catch (IOException e) {
            System.out.println("Error saving admin actions file: " + e.getMessage());
        }
    }

    private void initializeSampleData() {
        users.put("admin1", new User("admin1", "Admin@123", "admin@foodbike.com", "01234567890", User.UserType.ADMIN));
        users.put("user1", new User("user1", "User@123", "user@foodbike.com", "01987654321", User.UserType.USER));
        users.put("entrepreneur1", new User("entrepreneur1", "Entrepreneur@123", "ent@foodbike.com", "01111111111", User.UserType.ENTREPRENEUR));
        
        initializeRestaurants();
    }

    private void initializeRestaurants() {
        String[] divisions = {"Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh"};
        Map<String, String> divisionPrefixes = new HashMap<>();
        divisionPrefixes.put("Dhaka", "DH");
        divisionPrefixes.put("Chittagong", "CH");
        divisionPrefixes.put("Sylhet", "SY");
        divisionPrefixes.put("Rajshahi", "RJ");
        divisionPrefixes.put("Khulna", "KH");
        divisionPrefixes.put("Barisal", "BA");
        divisionPrefixes.put("Rangpur", "RP");
        divisionPrefixes.put("Mymensingh", "MY");
        
        String[] restaurantNames = {"Burger King", "Pizza Hut", "KFC", "Biryani House", "Naan Paradise", "Kebab Paradise",
                                    "Thai Express", "Chinese Delight", "Mexican Fiesta", "Kebab King", "Mama Noura", "Dominos Pizza",
                                    "Dunkin Donuts", "Whattacup", "Subway Lunch", "Chicken Fiesta", "Popeyes", "Five Guys",
                                    "Panera Bread", "In-N-Out", "Shake Shack", "Culver's", "Whataburger", "Cook Out",
                                    "Bojangles", "Wings Nudget", "Raising Cane's", "Jollibee", "Zaxby's", "Jack in the Box",
                                    "Pizza Drive-In", "Carl's Hotel", "Wendy's", "Sultan's Dine", "Kacchi Bhai", "Qdoba",
                                    "Panda Express", "Asian Station", "Ramen House", "Smelling Good", "Tandoori Palace", "Mongolian Grill",
                                    "Indian Curry House", "Tandoori Palace", "Spice Route", "Samosa Corner", "Dhaba Style", "Biryani Delight",
                                    "Middle Eastern Grill", "Shawarma King", "Hotel Express", "Biriyani Hub", "Lebanese Kitchen", "Turkish Delight",
                                    "Pasta Paradise", "Italian Kitchen", "Trattoria Roma", "Mozzarella House", "Risotto Magic", "Carbonara King",
                                    "Vegan Garden", "Green Bowl", "Salad Station", "Juice Bar", "Juice Junction", "Bar-B-Q Fish",
                                    "Chocolate Shop", "Dessert Paradise", "Ice Cream Dreams", "Donut Palace", "Cake House", "Pastry Corner",
                                    "Coffee House", "Tea Paradise", "Cappuccino King", "Espresso Express", "BarBQ Lounge", "Moja Moments",
                                    "Breakfast Club", "Pancake House", "Waffle King", "Omelette Station", "Toast&Jam", "Bagel Bakery",
                                    "Seafood Shack", "Fish&Chips", "Crab Palace", "Lobster House", "Oyster Bar", "Prawn Paradise",
                                    "Steak House", "BBQ Pit", "Grilled Delights", "Meat Lovers", "Ribeye King", "Lamb Chops"};

        Map<String, Integer> divisionCount = new HashMap<>();
        int restaurantId = 1;
        for (int i = 0; i < 100; i++) {
            String name = restaurantNames[i % restaurantNames.length];
            if (i >= restaurantNames.length) {
                name = name + " " + (i / restaurantNames.length);
            }
            String division = divisions[i % divisions.length];

            int count = divisionCount.getOrDefault(division, 0) + 1;
            divisionCount.put(division, count);
            String prefix = divisionPrefixes.get(division);
            String restaurantIdStr = String.format("%s%03d", prefix, count);
            
            Restaurant restaurant = new Restaurant(restaurantIdStr, name, division, "Address " + restaurantId + ", " + division);

            double randomRating = 1.0 + Math.random() * 4.0; // Round to 1 decimal place
            randomRating = Math.round(randomRating * 10) / 10.0;
            restaurant.setRating(randomRating);
            restaurant.addMenuItem(new MenuItem("item_" + restaurantId + "_1", "Special Combo", "Our signature dish", 250));
            restaurant.addMenuItem(new MenuItem("item_" + restaurantId + "_2", "Deluxe Meal", "Premium items", 350));
            restaurant.addMenuItem(new MenuItem("item_" + restaurantId + "_3", "Basic Meal", "Standard items", 150));
            restaurant.addMenuItem(new MenuItem("item_" + restaurantId + "_4", "Beverage", "Drinks and juices", 50));
            
            restaurants.put(restaurantIdStr, restaurant);
            restaurantId++;
        }
    }

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
        saveDataToFiles();
        return true;
    }

    public boolean deleteRestaurant(String restaurantId) {
        boolean removed = restaurants.remove(restaurantId) != null;
        if (removed) {
            saveDataToFiles();
        }
        return removed;
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

    public void createOrder(Order order) {
        orders.put(order.getOrderId(), order);
        saveDataToFiles();
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

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public void submitApplication(RestaurantApplication application) {
        applications.put(application.getApplicationId(), application);
        saveDataToFiles();
    }

    public List<RestaurantApplication> getPendingApplications() {
        List<RestaurantApplication> pendingApps = new ArrayList<>();
        for (RestaurantApplication app : applications.values()) {
            if (app.getStatus() == RestaurantApplication.ApplicationStatus.PENDING) {
                pendingApps.add(app);
            }
        }
        return pendingApps;
    }

    public List<RestaurantApplication> getEntrepreneurApplications(String username) {
        List<RestaurantApplication> entrepreneurApps = new ArrayList<>();
        for (RestaurantApplication app : applications.values()) {
            if (app.getEntrepreneurUsername().equals(username)) {
                entrepreneurApps.add(app);
            }
        }
        return entrepreneurApps;
    }

    public RestaurantApplication getApplication(String applicationId) {
        return applications.get(applicationId);
    }

    public void updateApplicationStatus(String applicationId, RestaurantApplication.ApplicationStatus status, String message) {
        RestaurantApplication app = applications.get(applicationId);
        if (app != null) {
            app.setStatus(status);
            app.setAdminMessage(message);
            saveDataToFiles();
        }
    }
    
    public void logAdminAction(AdminAction action) {
        adminActions.put(action.getActionId(), action);
        saveDataToFiles();
    }
    
    public List<AdminAction> getAllAdminActions() {
        List<AdminAction> actions = new ArrayList<>(adminActions.values());
        actions.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return actions;
    }
    
    public List<AdminAction> getAdminActionsByType(AdminAction.ActionType type) {
        List<AdminAction> filteredActions = new ArrayList<>();
        for (AdminAction action : adminActions.values()) {
            if (action.getActionType() == type) {
                filteredActions.add(action);
            }
        }
        filteredActions.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));
        return filteredActions;
    }
    
    public List<RestaurantApplication> getAllApplications() {
        return new ArrayList<>(applications.values());
    }
}
