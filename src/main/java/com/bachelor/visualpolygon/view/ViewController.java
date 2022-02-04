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








    public void init(ViewModel viewModel) {
        this.viewModel = viewModel;
        statusText.textProperty().bindBidirectional(viewModel.labelTextProperty());

        pane.setOnMousePressed(mouseHandlerForPane);
        pane.setOnMouseReleased(mouseHandlerForPane);
        pane.setOnMouseClicked(mouseHandlerForPane);
        pane.getChildren().add(root);
        viewModel.getPolygon().setOnMouseClicked(mouseHandlerForPolygon);
    }


    EventHandler<MouseEvent> mouseHandlerForPolygon = mouseEvent -> {
        if (viewModel.getCamera() == null) {
            cameraCoordinates.addAll(mouseEvent.getX(), mouseEvent.getY());
            root.getChildren().add(viewModel.createCamera(cameraCoordinates));
        }
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {

            if (mouseEvent.getTarget().getClass().getSimpleName().equals("Camera")) {
                root.getChildren().remove(viewModel.getCamera());
                cameraCoordinates.clear();
                viewModel.setCamera(null);
            }
        }
    };

    EventHandler<MouseEvent> mouseHandlerForPane = mouseEvent -> {

        if (mouseEvent.getEventType() == MouseEvent.MOUSE_CLICKED) {
            if ((mouseEvent.getButton() == MouseButton.PRIMARY)) {

                if (mouseEvent.getTarget().getClass().getSimpleName().equals("AnchorPane")) {

                    if (viewModel.getPolygon().getPoints().isEmpty()) {
                        viewModel.getPolyline().getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                        refreshLine();
                    } else {
                        viewModel.getPolygon().getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                        refreshPolygon();
                    }
                } else if (mouseEvent.getTarget().getClass().getSimpleName().equals("Point")) {

                    Point point = (Point) mouseEvent.getTarget();
                    if (point.getCenterX() == viewModel.getPolyline().getPoints().get(0) && point.getCenterY() == viewModel.getPolyline().getPoints().get(1)) {

                        viewModel.getPolygon().getPoints().addAll(viewModel.getPolyline().getPoints());
                        refreshPolygon();
                    }
                }
            }

            if (mouseEvent.getButton() == MouseButton.SECONDARY) {

                if (mouseEvent.getTarget().getClass().getSimpleName().equals("Point")) {

                    Point point = (Point) mouseEvent.getTarget();
                    if (!viewModel.getPolygon().getPoints().isEmpty()) {
                        viewModel.getPolygon().getPoints().removeAll(point.getCenterX(), point.getCenterY());
                        refreshPolygon();
                    } else {
                        viewModel.getPolyline().getPoints().removeAll(point.getCenterX(), point.getCenterY());
                        refreshLine();
                    }
                }
            }
        }

    };
    public void resetApplication() {

        root.getChildren().clear();
        cameraCoordinates.clear();
        viewModel.resetView();

    }

    public void updateStatus() {
        viewModel.updatePolygon();
    }

    public void refreshLine() {
        root.getChildren().clear();
        root.getChildren().add(viewModel.drawPolyline());
        root.getChildren().addAll(createControlPointsFor(viewModel.getPolyline().getPoints()));
    }

    public void refreshPolygon() {
        root.getChildren().clear();
        cameraCoordinates.clear();
        viewModel.setCamera(null);
        root.getChildren().add(viewModel.drawPolygon());
        root.getChildren().addAll(createControlPointsFor(viewModel.getPolygon().getPoints()));
    }


    public ObservableList<Point> createControlPointsFor(final ObservableList<Double> coordinates) throws IndexOutOfBoundsException {

        ObservableList<Point> points = FXCollections.observableArrayList();

        for (int i = 0; i < coordinates.size(); i += 2) {

            final int idx = i;

            try {
                DoubleProperty xProperty = new SimpleDoubleProperty(coordinates.get(i));
                DoubleProperty yProperty = new SimpleDoubleProperty(coordinates.get(i + 1));

                xProperty.addListener((ov, oldX, x) -> coordinates.set(idx, (double) x));

                yProperty.addListener((ov, oldY, y) -> coordinates.set(idx + 1, (double) y));

                points.add(new Point(xProperty, yProperty));

            } catch (IndexOutOfBoundsException e) {
                System.out.println("Smth went wrong::size of coordinates " + coordinates.size() + " und i = " + i);
                e.printStackTrace();
                statusText.setText("Error, Something went wrong! Reset to try again!");
            }
        }

        return points;
    }

}

