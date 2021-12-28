package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
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


    EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            if (mouseEvent.getTarget().toString().contains("Anchor") && mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {

                x.add(mouseEvent.getX());
                y.add(mouseEvent.getY());
                xCoordinates.setValue(x);
                yCoordinates.setValue(y);
                System.out.println(mouseEvent.getX()+"und"+mouseEvent.getY());
                root.getChildren().add(new Circle(mouseEvent.getX(),mouseEvent.getY(),4));

            }
        }
    };


    public void     init(ViewModel viewModel) {
        this.viewModel = viewModel;
        System.out.println("Works");
        statusText.textProperty().bindBidirectional(viewModel.labelTextProperty());
        pane.setOnMouseClicked(mouseHandler);
        pane.getChildren().add(root);
        xCoordinates.bindBidirectional(viewModel.xCoordinateProperty());
        yCoordinates.bindBidirectional(viewModel.yCoordinateProperty());
    }

    public void resetApplication() {
        statusText.setText("RESETTING");
        root.getChildren().clear();
        x.clear();
        y.clear();
    }

    public void updateStatus() {
        viewModel.test();
    }



}

