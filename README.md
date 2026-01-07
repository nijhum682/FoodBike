Foodbike is a comprehensive JavaFX-based food delivery management system designed for Bangladesh, featuring multi-role architecture with distinct interfaces for four user types: regular customers, entrepreneurs (restaurant owners), delivery bikers, and administrators. The platform enables restaurant owners to apply for registration and manage their menus, customers to browse restaurants across eight divisions (Dhaka, Chittagong, Sylhet, Rajshahi, Khulna, Barisal, Rangpur, and Mymensingh), place orders with multiple payment options (Cash on Delivery, bKash, Nagad, Credit/Debit Card), and rate their experiences.

Functionality
User Authentication & Authorization: Secure sign-in/sign-up system with role-based access control that routes users to their specific interfaces (Admin, Customer, Entrepreneur, Biker).

Restaurant Application System: Entrepreneurs can submit restaurant applications with complete details including name, division, address, rating and menu items for administrative review.

Administrative Approval Workflow: Administrators can review pending applications, view detailed information, approve or reject with custom messages and automatically create restaurants with division-based unique IDs upon approval.

Restaurant Management: Comprehensive CRUD operations for restaurants including viewing all restaurants, editing details, managing menus and deleting restaurants with proper administrative oversight.

Menu Management: Restaurant owners can add, edit, and manage menu items with details including name, description, price and availability status.

Restaurant Search & Filtering: Customers, Bikers and Admin can search restaurants by name and filter by division across eight Bangladesh regions for easy restaurant discovery.

Order Placement & Tracking: Users can browse menus, add items to cart, select payment methods (Cash on Delivery, bKash, Nagad), place orders and track status through multiple states (Pending, Confirmed, Preparing, Ready, Delivered, Cancelled).

Auto-Cancellation Mechanism: Unconfirmed orders are automatically cancelled after a specified time threshold to maintain order queue efficiency.

Delivery Management: Bikers can view all ready-for-pickup orders, filter by division, accept deliveries and update order status upon successful delivery.

Order History & Reviews: Complete order history tracking for all users with the ability to submit ratings and reviews for completed deliveries.

Administrative Action Logging: Comprehensive audit trail system that records all administrative activities including application decisions, restaurant additions/deletions, menu modifications with timestamps and detailed descriptions.

Data Persistence: Centralized DatabaseService using Java serialization to persist users, restaurants, orders, applications, administrative actions and reviews across sessions.

Multi-Division Support: Full support for eight Bangladesh divisions with 64 districts, location-based filtering and division-specific restaurant ID generation.
