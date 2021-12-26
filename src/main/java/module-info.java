module com.bachelor.visualpolygon {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.bachelor.visualpolygon to javafx.fxml;
    exports com.bachelor.visualpolygon;
    exports com.bachelor.visualpolygon.view;
    opens com.bachelor.visualpolygon.view to javafx.fxml;
}