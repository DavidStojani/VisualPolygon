package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.view.shapes.Camera;
import com.bachelor.visualpolygon.view.shapes.Point;
import com.bachelor.visualpolygon.view.shapes.PolygonModified;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;

import java.util.Objects;


public class ViewController {

    @FXML
    public AnchorPane pane;
    @FXML
    private Label statusText;

    private ViewModel viewModel;

    private final Group root = new Group();
    private EventHandler<MouseEvent> mouseHandlerForPane;

    private ObservableList<Vertex> polCord = FXCollections.observableArrayList();
    private ObservableList<Double> cameraRequirements = FXCollections.observableArrayList();
    private PolygonModified polygon;
    private Polyline polyline;
    private Camera camera;
    private ListProperty<Vertex> listPropertyForVertex;
    private ListProperty<Double> listPropertyForCamera;


    public ViewController() {
        initMouseHandlerForPane();
        polyline = new Polyline();
        listPropertyForVertex = new SimpleListProperty<>(polCord);
        listPropertyForCamera = new SimpleListProperty<>(cameraRequirements);
    }

    private void initMouseHandlerForPane() {
        mouseHandlerForPane = mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {

                if (Objects.isNull(polygon)) {      /**Polygon not created yet*/
                    if (mouseEvent.getTarget() instanceof AnchorPane) {
                        polCord.add(new Vertex(mouseEvent.getX(), mouseEvent.getY())); //safe the vertex temporarily
                        polyline.getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                        refreshLine();
                    }
                    if (mouseEvent.getTarget() instanceof Point) {  /**When the polyLine closes to create a Polygon*/
                        Point point = (Point) mouseEvent.getTarget();
                        if (point.getCenterX() == polyline.getPoints().get(0) && point.getCenterY() == polyline.getPoints().get(1)) {
                            polygon = new PolygonModified(polCord);
                            listPropertyForVertex.set(polygon.getVertices());
                            drawPolygon();
                        }
                    }
                } else if (mouseEvent.getTarget() instanceof AnchorPane) {      /**Adding Points to existing polygon*/
                    polygon.addVertexAndPoint(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                    if (Objects.isNull(camera)) {
                        drawPolygon();
                    } else {
                        updateStatus();
                    }
                }
            }
            onSecondaryButton(mouseEvent);
        };
    }

    private void onSecondaryButton(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            if (mouseEvent.getTarget() instanceof Point) {
                Point point = (Point) mouseEvent.getTarget();

                if (Objects.nonNull(polygon)) {
                    polygon.removeVertexAndPoint(point);
                    updateStatus();
                } else {
                    polyline.getPoints().removeAll(point.getCenterX(), point.getCenterY());
                    refreshLine();
                }
            }
            if (mouseEvent.getTarget() instanceof Polygon && Objects.isNull(camera)) {

                cameraRequirements.addAll(mouseEvent.getX(), mouseEvent.getY(), 30.0);
                camera = Camera.createCamera(cameraRequirements);
                updateStatus();

            }
        }
    }

    public void init(ViewModel viewModel) {
        this.viewModel = viewModel;
        statusText.textProperty().bindBidirectional(viewModel.labelTextProperty());
        pane.setOnMouseClicked(mouseHandlerForPane);
        pane.getChildren().add(root);
        listPropertyForVertex.bindContentBidirectional(viewModel.getVertices());
        listPropertyForCamera.bindContentBidirectional(viewModel.getCameraDetails());
    }

    public void testFeature() {

    }

    public void resetApplication() {
        root.getChildren().clear();
        camera = null;
        polygon.getPoints().clear();
        polygon.getVertices().clear();
        polyline.getPoints().clear();
        polCord.clear();
        cameraRequirements.clear();
        polygon = null;
        viewModel.resetView();
    }

    public void updateStatus() {
        viewModel.updatePolygon();
        refreshView();
    }


    private void refreshLine() {
        root.getChildren().clear();
        root.getChildren().add(drawPolyline());
        root.getChildren().addAll(createControlPointsFor(polyline.getPoints()));
    }

    private void refreshView() {
        root.getChildren().clear();
        root.getChildren().add(polygon.draw());
        if (Objects.nonNull(camera)) {
            root.getChildren().add(camera);
        }
        root.getChildren().addAll(polygon.createModeratePoints());
    }

    private void drawPolygon() {
        root.getChildren().clear();
        root.getChildren().add(polygon.draw());
        root.getChildren().addAll(polygon.createModeratePoints());
    }


    private Polyline drawPolyline() {
        polyline.setStroke(Color.AZURE);
        polyline.setStrokeWidth(3);
        polyline.setStrokeLineCap(StrokeLineCap.ROUND);

        return polyline;
    }


    private ObservableList<Point> createControlPointsFor(final ObservableList<Double> coordinates) throws IndexOutOfBoundsException {
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
                e.printStackTrace();
                statusText.setText("Error, Something went wrong! Reset to try again!");
            }
        }
        return points;
    }
}

