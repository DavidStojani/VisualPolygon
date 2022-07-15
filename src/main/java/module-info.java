module com.bachelor.visualpolygon {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.locationtech.jts;
    requires lombok;
    requires logback.core;
    requires org.slf4j;

    opens com.bachelor.visualpolygon to javafx.fxml;
    exports com.bachelor.visualpolygon;
    exports com.bachelor.visualpolygon.view;
    opens com.bachelor.visualpolygon.view to javafx.fxml;
    exports com.bachelor.visualpolygon.viewmodel;
    opens com.bachelor.visualpolygon.viewmodel to javafx.fxml;
    exports com.bachelor.visualpolygon.view.shapes;
    opens com.bachelor.visualpolygon.view.shapes to javafx.fxml;
}