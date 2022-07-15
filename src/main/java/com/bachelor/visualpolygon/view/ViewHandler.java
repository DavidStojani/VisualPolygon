package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.viewmodel.ViewModelFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ViewHandler {

    private final Stage stage;
    private final ViewModelFactory viewModelFactory;

    public ViewHandler(Stage stage, ViewModelFactory factory) {
        this.stage = stage;
        this.viewModelFactory = factory;
    }

    public void start() throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("view.fxml"));
        Parent root = fxmlLoader.load();


        ViewController viewController = fxmlLoader.getController();
        viewController.init(viewModelFactory.getViewModel());

        Scene scene = new Scene(root, 1640, 900);
        stage.setTitle("Polygon Visualisation");

        stage.setScene(scene);
        stage.show();
    }

}
