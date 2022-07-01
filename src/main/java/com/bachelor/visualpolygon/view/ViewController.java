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
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Callback;

import java.io.File;
import java.util.Objects;


public class ViewController {

    @FXML
    public AnchorPane pane;
    @FXML
    private Label statusText;
    @FXML
    private ListView<File> uploadList;

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
        initList();
        listPropertyForVertex.bindContentBidirectional(viewModel.getVertices());
        listPropertyForCamera.bindContentBidirectional(viewModel.getCameraDetails());
    }

    public void initList() {
        uploadList.getItems().add(new File("output.txt"));
        uploadList.getItems().add(new File("alfaBug.txt"));
        uploadList.getItems().add(new File("alfaStucked.txt"));

        uploadList.setOnMouseClicked(click -> {
            System.out.println("CLICKED");
            if (click.getClickCount() == 2) {
                //Use ListView's getSelected Item
                File f = uploadList.getSelectionModel().getSelectedItem();
                uploadPolygon(f);
                //use this to do whatever you want to. Open Link etc.
            }
        });
    }

    public void nextStep() {
        Polygon stepPoly = viewModel.getStepPolygon();
        /**TODO Keep the stepPolygon inside the AnchorPane or change to another Pane ?*?
         /*   System.out.println("STEPPOLY COORD-----" + stepPoly.getPoints() );
         System.out.println("LAYOUT X-----" + stepPoly.getBoundsInParent());
         System.out.println("LAYOUT Y-----" + pane.getBoundsInLocal());

         if (stepPoly.getBoundsInParent().getMaxX() > pane.getBoundsInLocal().getMaxX()) {
         //stepPoly.getBoundsInLocal().getMaxX() = pane.getBoundsInLocal().getMaxX();
         System.out.println("NEW STEP POLY-------" + stepPoly.getPoints());
         }

         if (stepPoly.getBoundsInParent().getMaxY() > pane.getBoundsInLocal().getMaxY()) {

         stepPoly.getPoints().replaceAll(aDouble -> aDouble == stepPoly.getBoundsInLocal().getMaxY() ? 0 : pane.getBoundsInLocal().getMaxY());
         System.out.println("NEW STEP POLY-------" + stepPoly.getPoints());
         }*/

        viewModel.setStepInfo();
        if (stepPoly != null) {
            refreshView();
            drawStepPolygon(stepPoly);
            root.getChildren().add(stepPoly);
        }

        if (index == viewModel.getVertices().size() - 1) {
            //DRAW FINAL POLYGON
            viewModel.setLabelText("Last Step!");
            index = 0;
        } else {
            index++;
        }
    }

    public void updatePolygon() {
        if (!isPolygonReady()) return;
        if (Objects.isNull(camera)) {
            viewModel.setLabelText("Polygon Updated! Click to add Camera");
        }
        viewModel.updatePolygon();
        refreshView();
    }

    public void playStep(ActionEvent actionEvent) {
        if (!isPolygonReady()) return;
        if (Objects.isNull(camera)) {
            viewModel.setLabelText("Camera not yet in Polygon!");
            return;
        }

        if (viewModel.getParallels().empty()) {
            nextStep();
        } else {
            root.getChildren().add(viewModel.getParallels().pop());
        }

    }

    public void savePolygon(ActionEvent actionEvent) {
        if (!isPolygonReady()) return;

        viewModel.setLabelText("Saved was pressed");
        viewModel.save();
    }

    public void uploadPolygon(File file) {
        resetApplication();
        viewModel.uploadFile(file);
        polygon = new PolygonModified();
        updatePolygon();
        refreshView();
        viewModel.setLabelText("Update was pressed");
    }

    public void resetApplication() {
        root.getChildren().clear();
        camera = null;
        polyline.getPoints().clear();
        if (Objects.nonNull(polygon)) {
            polygon.getPoints().clear();
            polygon.getVertices().clear();
        }
        listPropertyForVertex.clear();
        cameraRequirements.clear();
        polygon = null;
        viewModel.resetView();
        index = 0;
    }

    private void initMouseHandlerForPane() {
        mouseHandlerForPane = mouseEvent -> {
            if (isPrimaryOnPaneAndEmptyPolygon(mouseEvent)) {
                polyline.getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                PolygonModified.vertices.add(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                refreshLine();
            } else if (isPrimaryOnPaneAndFullPolygon(mouseEvent)) {
                polygon.addVertexAndPoint(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                updatePolygon();
            }

            if (isPrimaryOnPointAndEmptyPolygon(mouseEvent)) {
                Point point = (Point) mouseEvent.getTarget();
                if (point.getCenterX() == polyline.getPoints().get(0) && point.getCenterY() == polyline.getPoints().get(1)) {
                    polygon = new PolygonModified();
                    updatePolygon();
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
            updatePolygon();
        }

        if (mouseEvent.getTarget() instanceof Polygon && Objects.isNull(camera)) {
            cameraRequirements.addAll(mouseEvent.getX(), mouseEvent.getY(), 30.0);
            camera = Camera.createCamera(cameraRequirements);
            camera.setOnMouseReleased(mouseEvent1 -> updatePolygon());
            updatePolygon();
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

    private void drawStepPolygon(Polygon stepPolygon) {

        stepPolygon.setStroke(Color.DARKRED);
        stepPolygon.setStrokeWidth(0.1);
        stepPolygon.setStrokeLineCap(StrokeLineCap.ROUND);
        stepPolygon.setFill(Color.RED.deriveColor(0, 1, 1, 0.3));
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

    private boolean isPolygonReady() {
        if (listPropertyForVertex.isEmpty() || Objects.isNull(polygon)) {
            viewModel.setLabelText("No Polygon formed yet, start Clicking, close the Polyline or Upload!");
            return false;
        }
        return true;
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

