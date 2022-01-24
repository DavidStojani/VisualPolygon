package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class ViewController {

    @FXML
    public AnchorPane pane;
    @FXML
    private Label statusText;

    private ViewModel viewModel;

    ObservableList<Double> cameraCoordinates = FXCollections.observableArrayList();
    Group root = new Group();


    EventHandler<MouseEvent> mouseHandler = mouseEvent -> {

         if (mouseEvent.getTarget().toString().contains("Anchor")) {
             if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {

                 viewModel.getPolygon().getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());

                 root.getChildren().clear();
                 root.getChildren().add(viewModel.drawPolygon());
                 root.getChildren().addAll(createControlPointsFor(viewModel.getPolygon().getPoints()));
             }
         }
         else if (mouseEvent.getButton() == MouseButton.SECONDARY && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {

            if (viewModel.getCamera() == null) {
                cameraCoordinates.addAll(mouseEvent.getX(),mouseEvent.getY());
                root.getChildren().add(viewModel.createCamera(cameraCoordinates));
            }
     }

    };
    public ObservableList<Point> createControlPointsFor(final ObservableList<Double> coordinates) {

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

        pane.setOnMousePressed(mouseHandler);
        pane.setOnMouseReleased(mouseHandler);
        pane.setOnMouseClicked(mouseHandler);
        pane.setOnMouseDragged(mouseHandler);
        pane.getChildren().add(root);
    }

    public void resetApplication() {

        root.getChildren().clear();
        cameraCoordinates.clear();
        viewModel.resetView();

    }

    public void updateStatus() {

        viewModel.updatePolygon();

    }



}

