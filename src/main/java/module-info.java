module com.example.demo {
    requires javafx.controls;
    requires javafx.fxml;


    opens oop2.demo to javafx.fxml;
    exports oop2.demo;
}