module com.example.foodbike {
    requires javafx.controls;
    requires javafx.fxml;
    
    opens com.example.foodbike to javafx.fxml;
    exports com.example.foodbike;
}