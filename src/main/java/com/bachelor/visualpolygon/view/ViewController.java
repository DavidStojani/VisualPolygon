package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.view.shapes.Camera;
import com.bachelor.visualpolygon.view.shapes.Point;
import com.bachelor.visualpolygon.view.shapes.PolygonModified;
import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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

    private ObservableList<Double> cameraRequirements = FXCollections.observableArrayList();
    private PolygonModified polygon;
    private Polyline polyline;
    private Camera camera;
    private ListProperty<Vertex> listPropertyForVertex;
    private ListProperty<Double> listPropertyForCamera;
    private int index = 0;


    public ViewController() {
        initMouseHandlerForPane();
        polyline = new Polyline();
        listPropertyForVertex = new SimpleListProperty<>(PolygonModified.vertices);
        listPropertyForCamera = new SimpleListProperty<>(cameraRequirements);
    }

    public void init(ViewModel viewModel) {
        this.viewModel = viewModel;
        statusText.textProperty().bindBidirectional(viewModel.labelTextProperty());
        pane.setOnMouseClicked(mouseHandlerForPane);
        pane.getChildren().add(root);
        listPropertyForVertex.bindContentBidirectional(viewModel.getVertices());
        listPropertyForCamera.bindContentBidirectional(viewModel.getCameraDetails());
    }

    public void nextStep() {
        Polygon stepPoly = viewModel.testFeature(index);
        if (stepPoly != null) {
            refreshView();
            root.getChildren().add(stepPoly);
        }

        if (index == viewModel.getVertices().size() - 1) {
            //DRAW FINAL POLYGON
            index = 0;
        } else {
            index++;
        }
    }

    public void playStep(ActionEvent actionEvent) {
        if (viewModel.getParallels().empty()){
            nextStep();
        }

        root.getChildren().add(viewModel.getParallels().pop());

    }

    public void savePolygon(ActionEvent actionEvent) {
        viewModel.setLabelText("Saved was pressed");
    }

    public void uploadPolygon(ActionEvent actionEvent) {
        viewModel.setLabelText("Update was pressed");
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
        index = 0;
    }

    public void updateStatus() {
        viewModel.updatePolygon();
        refreshView();
    }


    private void initMouseHandlerForPane() {
        mouseHandlerForPane = mouseEvent -> {
            if (isPrimaryOnPaneAndEmptyPolygon(mouseEvent)) {
                polyline.getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                PolygonModified.vertices.add(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                refreshLine();
            } else if (isPrimaryOnPaneAndFullPolygon(mouseEvent)) {
                polygon.addVertexAndPoint(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                updateStatus();
            }

            if (isPrimaryOnPointAndEmptyPolygon(mouseEvent)) {
                Point point = (Point) mouseEvent.getTarget();
                if (point.getCenterX() == polyline.getPoints().get(0) && point.getCenterY() == polyline.getPoints().get(1)) {
                    polygon = new PolygonModified();
                    updateStatus();
                }
            }
            onSecondaryButton(mouseEvent);
        };
    }


    private void onSecondaryButton(MouseEvent mouseEvent) {

        if (isSecondaryOnPointAndEmptyPolygon(mouseEvent)) {
            Point point = (Point) mouseEvent.getTarget();
            polyline.getPoints().removeAll(point.getCenterX(), point.getCenterY());
            PolygonModified.vertices.removeIf(vertex -> vertex.getXCoordinate() == point.getCenterX() && vertex.getYCoordinate() == point.getCenterY());
            refreshLine();
        }

        if (isSecondaryOnPointAndFullPolygon(mouseEvent)) {
            Point point = (Point) mouseEvent.getTarget();
            polygon.removeVertexAndPoint(point);
            updateStatus();
        }

        if (mouseEvent.getTarget() instanceof Polygon && Objects.isNull(camera)) {
            cameraRequirements.addAll(mouseEvent.getX(), mouseEvent.getY(), 30.0);
            camera = Camera.createCamera(cameraRequirements);
            camera.setOnMouseReleased(mouseEvent1 -> updateStatus());
            updateStatus();
        }
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
        root.getChildren().addAll(polygon.createModeratePoints());
    }


    private Polyline drawPolyline() {
        polyline.setStroke(Color.AZURE);
        polyline.setStrokeWidth(3);
        polyline.setStrokeLineCap(StrokeLineCap.ROUND);

        return polyline;
    }


    private ObservableList<Point> createControlPointsFor(final ObservableList<Double> coordinates) {
        ObservableList<Point> points = FXCollections.observableArrayList();
        for (int i = 0; i < coordinates.size(); i += 2) {
            final int idx = i;
            DoubleProperty xProperty = new SimpleDoubleProperty(coordinates.get(i));
            DoubleProperty yProperty = new SimpleDoubleProperty(coordinates.get(i + 1));

            xProperty.addListener((ov, oldX, x) -> {
                coordinates.set(idx, (double) x);
                PolygonModified.vertices.get(idx / 2).getXProperty().set(x.doubleValue());
            });
            yProperty.addListener((ov, oldY, y) -> {
                coordinates.set(idx + 1, (double) y);
                PolygonModified.vertices.get(idx / 2).getYProperty().set(y.doubleValue());
            });
            points.add(new Point(xProperty, yProperty));
        }
        return points;
    }

    private boolean isPrimaryAndEmptyPolygon(MouseEvent mouseEvent) {
        return mouseEvent.getButton().equals(MouseButton.PRIMARY) && Objects.isNull(polygon);
    }

    private boolean isPrimaryOnPaneAndEmptyPolygon(MouseEvent mouseEvent) {
        return (isPrimaryAndEmptyPolygon(mouseEvent) && mouseEvent.getTarget() instanceof AnchorPane);
    }

    private boolean isPrimaryOnPointAndEmptyPolygon(MouseEvent mouseEvent) {
        return (isPrimaryAndEmptyPolygon(mouseEvent) && mouseEvent.getTarget() instanceof Point);
    }

    private boolean isPrimaryOnPaneAndFullPolygon(MouseEvent mouseEvent) {
        return (mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getTarget() instanceof AnchorPane);
    }

    private boolean isSecondaryOnPointAndEmptyPolygon(MouseEvent mouseEvent) {
        return mouseEvent.getButton().equals(MouseButton.SECONDARY) && Objects.isNull(polygon) && mouseEvent.getTarget() instanceof Point;
    }

    private boolean isSecondaryOnPointAndFullPolygon(MouseEvent mouseEvent) {
        return mouseEvent.getButton().equals(MouseButton.SECONDARY) && Objects.nonNull(polygon) && mouseEvent.getTarget() instanceof Point;
    }
}

