package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.beans.property.*;
import javafx.beans.value.ObservableDoubleValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeType;
import javafx.util.FXPermission;

public class ViewController {

    @FXML
    public AnchorPane pane;
    @FXML
    private Label statusText;

    private ViewModel viewModel;

    ObservableList<Double> coordinates = FXCollections.observableArrayList();
    ObservableList<Point> points = FXCollections.observableArrayList();

    ObservableList<Double> cameraCoordinates = FXCollections.observableArrayList();

    ListProperty<Double> coordinatesProperty = new SimpleListProperty<>();
    Group root = new Group();

    Polygon polygon = new Polygon();


    EventHandler<MouseEvent> mouseHandler = mouseEvent -> {

         if (mouseEvent.getTarget().toString().contains("Anchor") && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {

             points.clear();

             coordinates.add(mouseEvent.getX());
             coordinates.add(mouseEvent.getY());

             points = createControlPointsFor(coordinates);

             root.getChildren().addAll(points);
             coordinatesProperty.set(coordinates);

         }else if (mouseEvent.getTarget().toString().contains("Polygon") && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {


            if (viewModel.getCamera() == null) {
                cameraCoordinates.addAll(mouseEvent.getX(),mouseEvent.getY());
                root.getChildren().add(viewModel.createCamera(cameraCoordinates));
            }else {
               // System.out.println("CAMERA IS DA" + cameraCoordinates + ":::RADIUS:::" + camera.radiusProperty().getBean() );
            }
     }

    };
    private ObservableList<Point> createControlPointsFor(final ObservableList<Double> coordinates) {

        ObservableList<Point> points = FXCollections.observableArrayList();

        for (int i = 0; i < coordinates.size(); i += 2) {

            final int idx = i;

            DoubleProperty xProperty = new SimpleDoubleProperty(coordinates.get(i));
            DoubleProperty yProperty = new SimpleDoubleProperty(coordinates.get(i + 1));

            xProperty.addListener((ov, oldX, x) -> coordinates.set(idx, (double) x));

            yProperty.addListener((ov, oldY, y) -> coordinates.set(idx + 1, (double) y));

            points.add(new Point(xProperty, yProperty));

        }

        return points;

    }




    public void init(ViewModel viewModel) {
        this.viewModel = viewModel;
        statusText.textProperty().bindBidirectional(viewModel.labelTextProperty());

        pane.setOnMouseClicked(mouseHandler);
        pane.setOnMouseDragged(mouseHandler);
        pane.getChildren().add(root);
        coordinatesProperty.bindBidirectional(viewModel.coordinatesProperty());

    }

    public void resetApplication() {
        statusText.setText("RESETTING");
        polygon.getPoints().clear();
        root.getChildren().clear();
        coordinates.clear();
        points.clear();
        cameraCoordinates.clear();
        System.out.println(points);

    }

    public void updateStatus() {
        polygon.getPoints().clear();
        polygon.getPoints().addAll(coordinates);
        polygon.setStroke(Color.FORESTGREEN);
        polygon.setStrokeWidth(3);
        polygon.setStrokeLineCap(StrokeLineCap.ROUND);
        polygon.setFill(Color.GOLDENROD.deriveColor(0, 1.2, 1, 0.6));


        root.getChildren().clear();
        root.getChildren().add(polygon);
        root.getChildren().addAll(points);

        if (viewModel.getCamera() != null) {
            root.getChildren().add(viewModel.getCamera());
        }


      //  root.getChildren().addAll(points);
        viewModel.test();

    }

}

