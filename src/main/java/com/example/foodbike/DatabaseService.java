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
    private Map<String, Review> reviews;
    private static final String USERS_FILE = "users.dat";
    private static final String RESTAURANTS_FILE = "restaurants.dat";
    private static final String ORDERS_FILE = "orders.dat";
    private static final String APPLICATIONS_FILE = "applications.dat";
    private static final String ADMIN_ACTIONS_FILE = "admin_actions.dat";
    private static final String REVIEWS_FILE = "reviews.dat";

    private DatabaseService() {
        users = new HashMap<>();
        restaurants = new HashMap<>();
        orders = new HashMap<>();
        applications = new HashMap<>();
        adminActions = new HashMap<>();
        reviews = new HashMap<>();
        loadDataFromFiles();
        
        if (needsDistrictUpdate()) {
            restaurants.clear();
            File restaurantsFile = new File(RESTAURANTS_FILE);
            if (restaurantsFile.exists()) {
                restaurantsFile.delete();
            }
        }
        
        if (users.isEmpty()) {
            initializeSampleData();
            saveDataToFiles();
        }

        Map<String, Restaurant> preservedRestaurants = new HashMap<>();
        for (Map.Entry<String, Restaurant> entry : restaurants.entrySet()) {
            String id = entry.getKey();
            if (id.matches("[A-Z]{2}\\d{3}") && Integer.parseInt(id.substring(2)) > 13) {
                preservedRestaurants.put(id, entry.getValue());
            }
        }

        List<RestaurantApplication> approvedApps = new ArrayList<>();
        for (RestaurantApplication app : applications.values()) {
            if (app.getStatus() == RestaurantApplication.ApplicationStatus.APPROVED) {
                approvedApps.add(app);
            }
        }

        if (restaurants.size() < 256 || shouldReinitializeDefaultRestaurants() || needsDistrictUpdate()) {
            initializeRestaurants();
            restaurants.putAll(preservedRestaurants);
            
            for (RestaurantApplication app : approvedApps) {
                Restaurant existingRestaurant = null;
                for (Restaurant r : restaurants.values()) {
                    if (r.getName().equals(app.getRestaurantName()) && 
                        r.getDivision().equals(app.getDivision())) {
                        existingRestaurant = r;
                        break;
                    }
                }
                
                if (existingRestaurant == null) {
                    Map<String, String> divisionPrefixes = new HashMap<>();
                    divisionPrefixes.put("Dhaka", "DH");
                    divisionPrefixes.put("Chittagong", "CH");
                    divisionPrefixes.put("Sylhet", "SY");
                    divisionPrefixes.put("Rajshahi", "RJ");
                    divisionPrefixes.put("Khulna", "KH");
                    divisionPrefixes.put("Barisal", "BA");
                    divisionPrefixes.put("Rangpur", "RP");
                    divisionPrefixes.put("Mymensingh", "MY");
                    
                    int count = 0;
                    for (Restaurant r : restaurants.values()) {
                        if (r.getDivision().equals(app.getDivision())) {
                            count++;
                        }
                    }
                    String prefix = divisionPrefixes.get(app.getDivision());
                    String restaurantId = String.format("%s%03d", prefix, count + 1);
                    
                    Restaurant restaurant = new Restaurant(restaurantId, app.getRestaurantName(), 
                                                          app.getDivision(), app.getDistrict(), app.getAddress());
                    restaurant.setRating(app.getRating());
                    
                    for (MenuItem item : app.getMenuItems()) {
                        restaurant.addMenuItem(item);
                    }
                    
                    restaurants.put(restaurantId, restaurant);
                }
            }
            
            saveDataToFiles();
        }
    }

    private boolean shouldReinitializeDefaultRestaurants() {
        for (Restaurant r : restaurants.values()) {
            if (r.getName().equals("Burger King") || r.getName().equals("Pizza Hut") || 
                r.getName().equals("KFC") || r.getName().contains("Whattacup")) {
                return true;
            }
        }
        return false;
    }

    private boolean needsDistrictUpdate() {
        for (Restaurant r : restaurants.values()) {
            if (r.getDistrict() == null || r.getDistrict().isEmpty()) {
                return true;
            }
        }
        
        // Check if any district has fewer than 4 restaurants
        Map<String, Integer> districtCounts = new HashMap<>();
        for (Restaurant r : restaurants.values()) {
            String district = r.getDistrict();
            if (district != null && !district.isEmpty()) {
                districtCounts.put(district, districtCounts.getOrDefault(district, 0) + 1);
            }
        }
        
        for (List<String> districts : getDivisionDistrictsMap().values()) {
            for (String district : districts) {
                int count = districtCounts.getOrDefault(district, 0);
                if (count < 4) {
                    return true;
                }
            }
        }
        
        return false;
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

        File reviewsFile = new File(REVIEWS_FILE);
        if (reviewsFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(reviewsFile))) {
                reviews = (Map<String, Review>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error loading reviews file: " + e.getMessage());
                reviews = new HashMap<>();
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

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(REVIEWS_FILE))) {
            oos.writeObject(reviews);
        } catch (IOException e) {
            System.out.println("Error saving reviews file: " + e.getMessage());
        }
    }

    private void initializeSampleData() {
        users.put("admin1", new User("admin1", "Admin@123", "admin@foodbike.com", "01234567890", User.UserType.ADMIN));
        users.put("user1", new User("user1", "User@123", "user@foodbike.com", "01987654321", User.UserType.USER));
        users.put("entrepreneur1", new User("entrepreneur1", "Entrepreneur@123", "ent@foodbike.com", "01111111111", User.UserType.ENTREPRENEUR));
        
        initializeRestaurants();
    }

    private void initializeRestaurants() {
        Map<String, List<String>> divisionDistricts = new HashMap<>();
        divisionDistricts.put("Dhaka", Arrays.asList("Dhaka", "Gazipur", "Narayanganj", "Tangail", "Munshiganj", "Manikganj", "Narsingdi", "Faridpur", "Rajbari", "Gopalganj", "Madaripur", "Shariatpur", "Kishoreganj"));
        divisionDistricts.put("Chittagong", Arrays.asList("Chittagong", "Cox's Bazar", "Comilla", "Feni", "Brahmanbaria", "Rangamati", "Noakhali", "Chandpur", "Lakshmipur", "Bandarban", "Khagrachari"));
        divisionDistricts.put("Sylhet", Arrays.asList("Sylhet", "Moulvibazar", "Habiganj", "Sunamganj"));
        divisionDistricts.put("Rajshahi", Arrays.asList("Rajshahi", "Bogra", "Pabna", "Natore", "Sirajganj", "Naogaon", "Chapainawabganj", "Joypurhat"));
        divisionDistricts.put("Khulna", Arrays.asList("Khulna", "Jessore", "Satkhira", "Bagerhat", "Jhenaidah", "Magura", "Narail", "Kushtia", "Chuadanga", "Meherpur"));
        divisionDistricts.put("Barisal", Arrays.asList("Barisal", "Patuakhali", "Bhola", "Pirojpur", "Jhalokati", "Barguna"));
        divisionDistricts.put("Rangpur", Arrays.asList("Rangpur", "Dinajpur", "Lalmonirhat", "Nilphamari", "Gaibandha", "Thakurgaon", "Panchagarh", "Kurigram"));
        divisionDistricts.put("Mymensingh", Arrays.asList("Mymensingh", "Jamalpur", "Netrokona", "Sherpur"));

        Map<String, String> divisionPrefixes = new HashMap<>();
        divisionPrefixes.put("Dhaka", "DH");
        divisionPrefixes.put("Chittagong", "CH");
        divisionPrefixes.put("Sylhet", "SY");
        divisionPrefixes.put("Rajshahi", "RJ");
        divisionPrefixes.put("Khulna", "KH");
        divisionPrefixes.put("Barisal", "BA");
        divisionPrefixes.put("Rangpur", "RP");
        divisionPrefixes.put("Mymensingh", "MY");
        
        String[] restaurantNames = {"Khabar Ghar", "Bhoj Bari", "Ruchi Bhandar", "Pakghor", "Swaad Kutir", 
                                    "Amader Rannaghor", "Khana Khazana", "Rasoi Ghar", "Bhojan Griha", "Annapurna Bhoj",
                                    "Spice Lounge", "Flavour Junction", "Royal Feast", "Golden Spoon", "Heritage Kitchen",
                                    "Bawarchi Khana", "Dawat Ghar", "Mehfil Restaurant", "Sultan's Kitchen", "Mughal Durbar",
                                    "Kacchi Bhai", "Biryani Mahal", "Tehari House", "Pulao Palace", "Rice Bowl",
                                    "Tandoori Adda", "Kebab Corner", "Tikka Time", "Grill Master", "BBQ Nation",
                                    "Curry Hub", "Masala Magic", "Spice Garden", "Chili Chicken", "Pepper Pot",
                                    "Roti Ghar", "Naan Stop", "Paratha Plaza", "Chapati Corner", "Bread Basket",
                                    "Desi Dhaba", "Village Kitchen", "Gram Bangla", "Shobar Rannaghor", "Bazar Bhoj",
                                    "Fish Fry", "Machher Bazar", "Prawn Paradise", "Seafood Station", "Ocean Delight",
                                    "Chicken King", "Murgh Mahal", "Roast House", "Fry Point", "Korai Kitchen",
                                    "Sweet Corner", "Mishti Mukh", "Rosogolla House", "Dessert Delight", "Cake Palace",
                                    "Tea Time", "Cha Chakra", "Coffee Adda", "Cafe Culture", "Brew Station",
                                    "Breakfast Bazar", "Morning Meals", "Nashta Ghar", "Brunch Spot", "Early Bites",
                                    "Fast Food Fusion", "Quick Bites", "Snack Attack", "Chatpata Corner", "Street Food",
                                    "Pizza Point", "Pasta House", "Italian Touch", "Continental Cafe", "Western Grill",
                                    "Chinese Wok", "Thai Spice", "Asian Bowl", "Oriental Kitchen", "Dragon House",
                                    "Burger Spot", "Sandwich Shop", "Wrap Zone", "Hot Dog Hub", "Sub Station",
                                    "Juice Junction", "Lassi Bar", "Smoothie Corner", "Borhani Bazar", "Drink Depot",
                                    "Vegetarian Villa", "Green Plate", "Salad Bowl", "Healthy Eats", "Organic Oasis"};

        String[] divisions = {"Dhaka", "Chittagong", "Sylhet", "Rajshahi", "Khulna", "Barisal", "Rangpur", "Mymensingh"};
        
        int restaurantId = 1;
        int nameIndex = 0;
        
        for (String division : divisions) {
            List<String> districts = divisionDistricts.get(division);
            String prefix = divisionPrefixes.get(division);
            int divisionCount = 0;
            
            for (String district : districts) {
                for (int j = 0; j < 4; j++) {
                    divisionCount++;
                    
                    String name = restaurantNames[nameIndex % restaurantNames.length];
                    if (nameIndex >= restaurantNames.length) {
                        name = name + " " + (nameIndex / restaurantNames.length);
                    }
                    nameIndex++;
                    
                    String[] areaNames = {"Shadar Road", "Station Road", "College Road", "Market Area", "City Center", "Sadar", "Pourashava", "Bypass Road", "Main Road", "Upazila Road"};
                    String areaName = areaNames[restaurantId % areaNames.length];
                    String address = areaName + ", " + district;
                    
                    String restaurantIdStr = String.format("%s%03d", prefix, divisionCount);
                    
                    Restaurant restaurant = new Restaurant(restaurantIdStr, name, division, district, address);

                    double randomRating = 3.5 + Math.random() * 1.5;
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
            if (restaurant.getName().toLowerCase().contains(query.toLowerCase()) ||
                (restaurant.getDistrict() != null && restaurant.getDistrict().toLowerCase().contains(query.toLowerCase())) ||
                restaurant.getDivision().toLowerCase().contains(query.toLowerCase()) ||
                restaurant.getAddress().toLowerCase().contains(query.toLowerCase())) {
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

    public List<Restaurant> getRestaurantsByDistrict(String district) {
        List<Restaurant> results = new ArrayList<>();
        for (Restaurant restaurant : restaurants.values()) {
            if (restaurant.getDistrict() != null && restaurant.getDistrict().equalsIgnoreCase(district)) {
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

    public List<String> getAllDistricts() {
        Set<String> districtsSet = new HashSet<>();
        for (Restaurant restaurant : restaurants.values()) {
            if (restaurant.getDistrict() != null) {
                districtsSet.add(restaurant.getDistrict());
            }
        }
        List<String> districts = new ArrayList<>(districtsSet);
        Collections.sort(districts);
        return districts;
    }

    public Map<String, List<String>> getDivisionDistrictsMap() {
        Map<String, List<String>> divisionDistricts = new HashMap<>();
        divisionDistricts.put("Dhaka", Arrays.asList("Dhaka", "Gazipur", "Narayanganj", "Tangail", "Munshiganj", "Manikganj", "Narsingdi", "Faridpur", "Rajbari", "Gopalganj", "Madaripur", "Shariatpur", "Kishoreganj"));
        divisionDistricts.put("Chittagong", Arrays.asList("Chittagong", "Cox's Bazar", "Comilla", "Feni", "Brahmanbaria", "Rangamati", "Noakhali", "Chandpur", "Lakshmipur", "Bandarban", "Khagrachari"));
        divisionDistricts.put("Sylhet", Arrays.asList("Sylhet", "Moulvibazar", "Habiganj", "Sunamganj"));
        divisionDistricts.put("Rajshahi", Arrays.asList("Rajshahi", "Bogra", "Pabna", "Natore", "Sirajganj", "Naogaon", "Chapainawabganj", "Joypurhat"));
        divisionDistricts.put("Khulna", Arrays.asList("Khulna", "Jessore", "Satkhira", "Bagerhat", "Jhenaidah", "Magura", "Narail", "Kushtia", "Chuadanga", "Meherpur"));
        divisionDistricts.put("Barisal", Arrays.asList("Barisal", "Patuakhali", "Bhola", "Pirojpur", "Jhalokati", "Barguna"));
        divisionDistricts.put("Rangpur", Arrays.asList("Rangpur", "Dinajpur", "Lalmonirhat", "Nilphamari", "Gaibandha", "Thakurgaon", "Panchagarh", "Kurigram"));
        divisionDistricts.put("Mymensingh", Arrays.asList("Mymensingh", "Jamalpur", "Netrokona", "Sherpur"));
        return divisionDistricts;
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

    public void addReview(Review review) {
        reviews.put(review.getReviewId(), review);
        updateRestaurantRating(review.getRestaurantId());
        saveDataToFiles();
    }

    public List<Review> getRestaurantReviews(String restaurantId) {
        List<Review> restaurantReviews = new ArrayList<>();
        for (Review review : reviews.values()) {
            if (review.getRestaurantId().equals(restaurantId)) {
                restaurantReviews.add(review);
            }
        }
        restaurantReviews.sort((r1, r2) -> r2.getCreatedAt().compareTo(r1.getCreatedAt()));
        return restaurantReviews;
    }

    public boolean hasUserReviewedOrder(String userId, String orderId) {
        for (Review review : reviews.values()) {
            if (review.getUserId().equals(userId) && review.getOrderId().equals(orderId)) {
                return true;
            }
        }
        return false;
    }

    private void updateRestaurantRating(String restaurantId) {
        Restaurant restaurant = restaurants.get(restaurantId);
        if (restaurant != null) {
            List<Review> restaurantReviews = getRestaurantReviews(restaurantId);
            if (!restaurantReviews.isEmpty()) {
                double totalRating = 0;
                for (Review review : restaurantReviews) {
                    totalRating += review.getRating();
                }
                double averageRating = totalRating / restaurantReviews.size();
                restaurant.setRating(Math.round(averageRating * 10.0) / 10.0);
            }
        }
    }
}
