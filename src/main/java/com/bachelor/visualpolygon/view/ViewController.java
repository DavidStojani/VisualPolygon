package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.view.shapes.Camera;
import com.bachelor.visualpolygon.view.shapes.Point;
import com.bachelor.visualpolygon.view.shapes.PolygonModified;
import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
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
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;


public class ViewController {

    @FXML
    public AnchorPane pane;
    @FXML
    private Label statusText;

    private ViewModel viewModel;

    private final Group root = new Group();
    private EventHandler<MouseEvent> mouseHandlerForPane;

    //private ObservableList<Vertex> polCord = FXCollections.observableArrayList();
    private ObservableList<Double> cameraRequirements = FXCollections.observableArrayList();
    private PolygonModified polygon;
    private Polyline polyline;
    private Camera camera;
    private ListProperty<Vertex> listPropertyForVertex;
    private ListProperty<Double> listPropertyForCamera;


    public ViewController() {
        initMouseHandlerForPane();
        polyline = new Polyline();
        listPropertyForVertex = new SimpleListProperty<>(PolygonModified.vertices);
        listPropertyForCamera = new SimpleListProperty<>(cameraRequirements);
    }

    private void initMouseHandlerForPane() {
        mouseHandlerForPane = mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {

                if (Objects.isNull(polygon)) {      /**Polygon not created yet*/
                    if (mouseEvent.getTarget() instanceof AnchorPane) {
                        polyline.getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                        Vertex vertex = new Vertex(polyline.getPoints().get(polyline.getPoints().size() - 2), polyline.getPoints().get(polyline.getPoints().size() - 1));

                        PolygonModified.vertices.add(vertex);
                        refreshLine();
                    }
                    if (mouseEvent.getTarget() instanceof Point) {  /**When the polyLine closes to create a Polygon*/
                        Point point = (Point) mouseEvent.getTarget();
                        if (point.getCenterX() == polyline.getPoints().get(0) && point.getCenterY() == polyline.getPoints().get(1)) {

                            polygon = new PolygonModified();
                            drawPolygon();
                            updateStatus();
                        }
                    }
                } else if (mouseEvent.getTarget() instanceof AnchorPane) {      /**Adding Points to existing polygon*/
                    polygon.addVertexAndPoint(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                    if (Objects.isNull(camera)) {
                        drawPolygon();
                    }
                    updateStatus();
                }
            }
            onSecondaryButton(mouseEvent);
        };
    }

    private void onSecondaryButton(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseButton.SECONDARY) {
            System.out.println("HERE  0" + mouseEvent.getTarget());
            if (mouseEvent.getTarget() instanceof Point) {

                System.out.println("HERE  1");

                Point point = (Point) mouseEvent.getTarget();

                if (Objects.nonNull(polygon)) {
                    System.out.println("HERE  2");
                    polygon.removeVertexAndPoint(point);
                    refreshView();
                } else {
                    polyline.getPoints().removeAll(point.getCenterX(), point.getCenterY());
                    //polCord.removeIf(v -> v.getX() == point.getCenterX() & v.getY() == point.getCenterY());
                    refreshLine();
                }
            }
            if (mouseEvent.getTarget() instanceof Polygon && Objects.isNull(camera)) {

                cameraRequirements.addAll(mouseEvent.getX(), mouseEvent.getY(), 30.0);
                camera = Camera.createCamera(cameraRequirements);
                camera.setOnMouseReleased(mouseEvent1 -> updateStatus());
                refreshView();
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

        if (polygon != null) {
            System.out.println("==========INSIEDE POLYGON/VIEW=========");
            System.out.println("---VERTEX:::" + '\n' + polygon.getVertices());
            System.out.println("--POINTS:::");
            for (int i = 0; i < polygon.getPoints().size(); i += 2) {
                System.out.println("Position: " + i + "und " + (i + 1) + " Double: " + polygon.getPoints().get(i) + "---" + polygon.getPoints().get(i + 1));
                if (!polygon.getPoints().get(i).equals(polygon.getVertices().get(i / 2).getX())) {
                    System.out.println("X Point at position : " + i + "not the same as the X of Vertex " + polygon.getVertices().get(i / 2));
                }
            }
        }

        if (camera != null) {
            System.out.println("--CAMERA:::" + camera);
        }
        viewModel.testFeature();
    }

    public void resetApplication() {
        root.getChildren().clear();
        camera = null;
        polyline.getPoints().clear();

        polygon.getPoints().clear();
        polygon.getVertices().clear();


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
        drawPolygon();
        if (Objects.nonNull(camera)) {
            root.getChildren().add(camera);
        }
    }

    private void drawPolygon() {
        root.getChildren().clear();
        root.getChildren().add(polygon.draw());
        root.getChildren().addAll(createModeratePoints(PolygonModified.vertices));

    }


    private Polyline drawPolyline() {
        polyline.setStroke(Color.AZURE);
        polyline.setStrokeWidth(3);
        polyline.setStrokeLineCap(StrokeLineCap.ROUND);

        return polyline;
    }

    public ObservableList<Point> createModeratePoints(List<Vertex> vertices) {
        ObservableList<Point> points = FXCollections.observableArrayList();

        for (int i = 0; i < vertices.size(); i++) {
            final int idx = i;

            DoubleProperty xProperty = vertices.get(idx).getXProperty();
            DoubleProperty yProperty = vertices.get(idx).getYProperty();

            xProperty.addListener((observableValue, number, t1) -> {
                vertices.get(idx).setX((double) t1);
                polygon.updatePoints();
            });
            yProperty.addListener((observableValue, number, t1) -> {
                vertices.get(idx).setY((double) t1);
                polygon.updatePoints();
            });

            Point p = new Point(xProperty, yProperty);
            p.setOnMouseReleased(mouseEvent -> updateStatus());
            //Here can be added smth like "isVisible" at all not only from center
            if (!vertices.get(idx).isVisibleFromCenter()) {
                p.changeColor();
            }
            points.add(p);
        }
        return points;
    }

    private ObservableList<Point> createControlPointsFor(final ObservableList<Double> coordinates) throws IndexOutOfBoundsException {
        ObservableList<Point> points = FXCollections.observableArrayList();
        for (int i = 0; i < coordinates.size(); i += 2) {
            final int idx = i;

            try {
                DoubleProperty xProperty = new SimpleDoubleProperty(coordinates.get(i));
                DoubleProperty yProperty = new SimpleDoubleProperty(coordinates.get(i + 1));

                xProperty.addListener((ov, oldX, x) -> {
                    coordinates.set(idx, (double) x);
                    PolygonModified.vertices.get(idx / 2).setX(x.doubleValue());
                });
                yProperty.addListener((ov, oldY, y) -> {
                    coordinates.set(idx + 1, (double) y);
                    PolygonModified.vertices.get(idx / 2).setY(y.doubleValue());
                });


                points.add(new Point(xProperty, yProperty));

            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                statusText.setText("Error, Something went wrong! Reset to try again!");
            }
        }
        return points;
    }
}

