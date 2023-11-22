module com.example.weatherdekstop {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.google.gson;


    opens com.example.weatherdekstop to javafx.fxml;
    exports com.example.weatherdekstop;
    exports com.example.weatherdesktop;
    opens com.example.weatherdesktop to javafx.fxml;
}
