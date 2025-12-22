module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    exports oop2.demo.api;

    opens oop2.demo.api.controllers to javafx.fxml;

}