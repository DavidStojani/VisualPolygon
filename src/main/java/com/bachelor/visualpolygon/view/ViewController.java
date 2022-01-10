package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;

import java.util.ArrayList;
import java.util.List;

public class ViewController {

    @FXML
    public AnchorPane pane;
    @FXML
    private Label statusText;

    private ViewModel viewModel;

    ObservableList<Double> x = FXCollections.observableArrayList();
    ObservableList<Double> y = FXCollections.observableArrayList();

    private ListProperty<Double> xCoordinates = new SimpleListProperty<>();
    private ListProperty<Double> yCoordinates = new SimpleListProperty<>();
    Group root = new Group();
    int count = 0;
    List<Double> values = new ArrayList<>();
    Polygon polygon;

    EventHandler<MouseEvent> mouseHandler = new EventHandler<>() {
        @Override
        public void handle(MouseEvent mouseEvent) {

            if (mouseEvent.getTarget().toString().contains("Anchor") && mouseEvent.getEventType() == MouseEvent.MOUSE_PRESSED) {

                x.add(mouseEvent.getX());
                y.add(mouseEvent.getY());
                System.out.println();

            } else if (mouseEvent.getTarget().toString().contains("Anchor") && mouseEvent.getEventType() == MouseEvent.MOUSE_RELEASED) {
                polygon = new Polygon();

                values.add(x.get(count));
                values.add(y.get(count));
                count++;

                polygon.getPoints().addAll(values);
                polygon.setStroke(Color.FORESTGREEN);
                polygon.setStrokeWidth(3);
                polygon.setStrokeLineCap(StrokeLineCap.ROUND);
                polygon.setFill(Color.CORNSILK.deriveColor(0, 1.2, 1, 0.6));


                root.getChildren().clear();
                root.getChildren().add(polygon);
                root.getChildren().addAll(createControlAnchorsFor(polygon.getPoints()));

                xCoordinates.setValue(x);
                yCoordinates.setValue(y);
                System.out.println(mouseEvent.getX()+"und"+mouseEvent.getY());


            }
        }
    };
    private ObservableList<Anchor> createControlAnchorsFor(final ObservableList<Double> points) {

        ObservableList<Anchor> anchors = FXCollections.observableArrayList();

        for (int i = 0; i < points.size(); i += 2) {

            final int idx = i;

            DoubleProperty xProperty = new SimpleDoubleProperty(points.get(i));
            DoubleProperty yProperty = new SimpleDoubleProperty(points.get(i + 1));

            xProperty.addListener((ov, oldX, x) -> points.set(idx, (double) x));

            yProperty.addListener((ov, oldY, y) -> points.set(idx + 1, (double) y));

            anchors.add(new Anchor(xProperty, yProperty));

        }

        return anchors;

    }



    public void init(ViewModel viewModel) {
        this.viewModel = viewModel;
        System.out.println("Works");
        statusText.textProperty().bindBidirectional(viewModel.labelTextProperty());
        pane.setOnMousePressed(mouseHandler);
        pane.setOnMouseReleased(mouseHandler);
        pane.setOnMouseDragged(mouseHandler);
        pane.getChildren().add(root);
        xCoordinates.bindBidirectional(viewModel.xCoordinateProperty());
        yCoordinates.bindBidirectional(viewModel.yCoordinateProperty());
    }

    public void resetApplication() {
        statusText.setText("RESETTING");
        polygon.getPoints().clear();
        root.getChildren().clear();
        values.clear();
        x.clear();
        y.clear();
        count = 0;
    }

    public void updateStatus() {
        viewModel.test();
    }



}

