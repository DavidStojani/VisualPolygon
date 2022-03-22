package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.viewmodel.Camera;
import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.Objects;


public class ViewController {

    @FXML
    public AnchorPane pane;
    @FXML
    private Label statusText;

    private ViewModel viewModel;

    private final Group root = new Group();
    private EventHandler<MouseEvent> mouseHandlerForPane;
    private EventHandler<MouseEvent> mouseHandlerForPolygon;
    private ObservableList<Double> cameraCoordinates = FXCollections.observableArrayList();
    private ObservableList<Double> polygonCoordinates = FXCollections.observableArrayList();
    private ObservableList<Vertex> polCord = FXCollections.observableArrayList();
    private ObservableList<Vertex> vertices;
    private ObservableList<Point> goldVertex;
    private ObservableList<Point> redVertex;
    private Camera camera;

    ListProperty<Vertex> listProperty;


    public ViewController() {
        initMouseHandlerForPane();
        initMouseHandlerForPolygon();
        listProperty = new SimpleListProperty<>(polCord);
    }

    private void initMouseHandlerForPane() {
        mouseHandlerForPane = mouseEvent -> {


                if (mouseEvent.getButton() == MouseButton.PRIMARY) {

                    if (polygonCoordinates.isEmpty()) {
                        if (mouseEvent.getTarget() instanceof AnchorPane) {
                            polCord.add(new Vertex(mouseEvent.getX(),mouseEvent.getY())); //safe the clik as a vertex
                            viewModel.getPolyline().getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                            refreshLine();
                        } else if (mouseEvent.getTarget() instanceof Point) {
                            Point point = (Point) mouseEvent.getTarget();
                            if (point.getCenterX() == viewModel.getPolyline().getPoints().get(0) && point.getCenterY() == viewModel.getPolyline().getPoints().get(1)) {
                                polygonCoordinates.addAll(viewModel.getPolyline().getPoints());
                                refreshPolygon();
                            }
                        }
                    } else if (mouseEvent.getTarget() instanceof AnchorPane) {
                        polCord.add(new Vertex(mouseEvent.getX(),mouseEvent.getY()));//save it
                        polygonCoordinates.addAll(mouseEvent.getX(), mouseEvent.getY());
                        refreshPolygon();
                        updateStatus();
                    }

                }
                if (mouseEvent.getButton() == MouseButton.SECONDARY && mouseEvent.getTarget() instanceof Point) {

                    Point point = (Point) mouseEvent.getTarget();
                    int indexOfX = polygonCoordinates.indexOf(point.getCenterX());
                    int indexOfY = polygonCoordinates.indexOf(point.getCenterY());


                    if (!polygonCoordinates.isEmpty()) {
                        if (indexOfY == indexOfX + 1) {
                            polCord.removeIf(v -> v.getX()==point.getCenterX() && v.getY() == point.getCenterY());
                            polygonCoordinates.remove(point.getCenterX());
                            polygonCoordinates.remove(point.getCenterY());
                        }

                        refreshPolygon();
                        updateStatus();
                    } else {
                        viewModel.getPolyline().getPoints().removeAll(point.getCenterX(), point.getCenterY());
                        refreshLine();
                    }
                }
        };
    }

    private void initMouseHandlerForPolygon() {
        mouseHandlerForPolygon = mouseEvent -> {
            try {
                if (viewModel.getCamera() == null) {
                    cameraCoordinates.addAll(mouseEvent.getX(), mouseEvent.getY());
                    camera = viewModel.createCamera(cameraCoordinates);
                    root.getChildren().add(camera);
                }
            } finally {
                updateStatus();
            }
        };

    }


    public void init(ViewModel viewModel) {
        this.viewModel = viewModel;
        statusText.textProperty().bindBidirectional(viewModel.labelTextProperty());

        pane.setOnMouseClicked(mouseHandlerForPane);
        pane.getChildren().add(root);
        viewModel.getPolygon().setOnMouseClicked(mouseHandlerForPolygon);
        polygonCoordinates = viewModel.getPolygon().getPoints();
        listProperty.bindContentBidirectional(viewModel.getPolygon().getVertices());
    }

    public void testFeature() {

        if (!Objects.isNull(redVertex)) {
            root.getChildren().removeAll(redVertex);
        }
        //root.getChildren().removeAll(goldVertex);

        redVertex = createPointsFor(vertices);
        System.out.println(Objects.nonNull(redVertex));
        root.getChildren().addAll(redVertex);
        //refreshViewAfterUpdate();
    }

    public void resetApplication() {
        root.getChildren().clear();
        cameraCoordinates.clear();
        camera = null;
        viewModel.resetView();
    }

    public void updateStatus() {
        viewModel.updatePolygon();
        System.out.println("POLCOR");
        polCord.forEach(System.out::println);
        System.out.println("COOOR");
        viewModel.getPolygon().getVertices().forEach(System.out::println);
        //vertices = viewModel.getVertices();
        //testFeature();
        //System.out.println("AFTER"+polygonCoordinates);
    }

    private void refreshLine() {

        root.getChildren().clear();
        root.getChildren().add(viewModel.drawPolyline());
        root.getChildren().addAll(createControlPointsFor(viewModel.getPolyline().getPoints()));
    }

    private void refreshPolygon() {
        root.getChildren().clear();
        root.getChildren().add(viewModel.drawPolygon());
        if (!Objects.isNull(camera)) {
            root.getChildren().add(camera);
        }
        redVertex = createModeratePoints(polCord);
        goldVertex = createControlPointsFor(polygonCoordinates);
        root.getChildren().addAll(goldVertex);
    }

    private void refreshViewAfterUpdate(){
        root.getChildren().clear();
        root.getChildren().add(viewModel.drawPolygon());
        if (!Objects.isNull(camera)) {
            root.getChildren().add(camera);
        }
        System.out.println("SIZE OF Vertex in Polygon" + viewModel.getPolygon().getVertexFromPoints().size());
        root.getChildren().addAll(createModeratePoints(viewModel.getPolygon().getVertexFromPoints()));

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

    private ObservableList<Point> createModeratePoints(final ObservableList<Vertex> vertices){
        ObservableList<Point> points = FXCollections.observableArrayList();

        for (int i = 0; i < vertices.size(); i++) {
            final  int idx = i;

            DoubleProperty xProperty = vertices.get(idx).getXProperty();
            DoubleProperty yProperty = vertices.get(idx).getYProperty();

            xProperty.addListener((observableValue, number, t1) -> vertices.get(idx).setX((double)t1));
            yProperty.addListener((observableValue, number, t1) -> vertices.get(idx).setY((double) t1));

            if (!vertices.get(i).isVisibleFromCenter()) {
                Point p = new Point(xProperty, yProperty);
                p.changeColor();
                points.add(p);
            }else {
                Point p = new Point(xProperty, yProperty);
                points.add(p);
            }
        }
        return  points;
    }

    public ObservableList<Point> createPointsFor(ObservableList<Vertex> vertices) {
        ObservableList<Point> points = FXCollections.observableArrayList();

        for (int i = 0; i < vertices.size(); i++) {
            try {
                if (!vertices.get(i).isVisibleFromCenter()) {
                    Point p = new Point(vertices.get(i).getXProperty(),vertices.get(i).getYProperty());
                    p.changeColor();
                    points.add(p);
                }
            } catch (IndexOutOfBoundsException e) {
                e.printStackTrace();
                statusText.setText("Error, Something went wrong! Reset to try again!");
            }
        }
        return points;
    }

}

