package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.info.Level;
import com.bachelor.visualpolygon.info.Logger;
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
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;

import java.io.File;
import java.util.Objects;

public class ViewController {

    @FXML
    BorderPane border;
    @FXML
    public AnchorPane pane;
    @FXML
    private Label statusText;
    @FXML
    private ListView<File> uploadList;
    @FXML
    VBox buttonBox;
    @FXML
    VBox logPanel;
    @FXML
    HBox logController;
    @FXML
    private ChoiceBox<Level> filterLevel;
    @FXML
    private ToggleButton showTS;
    @FXML
    private ToggleButton tail;
    @FXML
    private ToggleButton pause;
    @FXML
    private CheckBox redBox;
    @FXML
    private CheckBox greenBox;
    @FXML
    private CheckBox yellowBox;

    private final Group redLines = new Group();
    private final Group greenLines = new Group();
    private final Group yellowLines = new Group();

    private ViewModel viewModel;

    private static final Logger logger = Logger.getLogger();
    private final LogView logView = new LogView(logger);

    private final Group root = new Group();
    private EventHandler<MouseEvent> mouseHandlerForPane;

    private final ObservableList<Double> cameraRequirements = FXCollections.observableArrayList();
    private PolygonModified polygon;
    private final Polyline polyline;
    private Camera camera;
    private final ListProperty<Vertex> listPropertyForVertex;
    private final ListProperty<Double> listPropertyForCamera;
    private boolean isScanDone = false;

    public ViewController() {
        logger.setContext("Controller: ");
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
        redLines.visibleProperty().bindBidirectional(redBox.selectedProperty());
        greenLines.visibleProperty().bindBidirectional(greenBox.selectedProperty());
        yellowLines.visibleProperty().bindBidirectional(yellowBox.selectedProperty());

        initUploadList();
        listPropertyForVertex.bindContentBidirectional(viewModel.getVertices());
        listPropertyForCamera.bindContentBidirectional(viewModel.getCameraDetails());
        initLogView();
    }

    private void initLogView() {
        filterLevel.setItems(FXCollections.observableArrayList(Level.values()));
        filterLevel.getSelectionModel().select(Level.INFO);
        logView.filterLevelProperty().bind(filterLevel.getSelectionModel().selectedItemProperty());
        logView.showTimeStampProperty().bind(showTS.selectedProperty());
        logView.tailProperty().bind(tail.selectedProperty());
        logView.pausedProperty().bind(pause.selectedProperty());
        logPanel.getChildren().add(logView);
        VBox.setVgrow(logView, Priority.ALWAYS);
    }

    private void initUploadList() {
        uploadList.setItems(viewModel.getFileObservableList());
        uploadList.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);
                setText(file == null ? null : file.getName());
            }
        });
        uploadList.setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                File file = uploadList.getSelectionModel().getSelectedItem();
                uploadPolygon(file);
            }
        });
    }

    /**
     * TODO: When is this button actually used ? Camera location wont update even when is pressed>???
     */
    public void updatePolygon() {
        logger.info("INFO POLYGON UPDATED");
        if (!isPolygonReady()) return;
        if (Objects.isNull(camera)) {
            viewModel.setLabelText("Polygon Updated! Click to add Camera");
        }
        viewModel.updatePolygon();
        refreshView();

    }

    public void nextStep() {
        if (viewModel.isScanDone()) {
            isScanDone = true;
            Polygon visPoly = viewModel.getVisPoly();
            refreshView();
            drawPolygon(false);
            drawVisPolygon(visPoly);
            root.getChildren().add(camera);
            root.getChildren().add(visPoly);
            root.getChildren().addAll(visPolyPoints(visPoly.getPoints()));
            return;
        }
        Polygon stepPoly = viewModel.getStepPolygon();
        /**TODO Keep the Polygon always in the center*/
        if (stepPoly != null) {
            refreshView();
            updateGroups();
            drawStepPolygon(stepPoly);
            root.getChildren().add(stepPoly);
            root.getChildren().add(redLines);
            root.getChildren().add(greenLines);
            root.getChildren().add(yellowLines);
        }
    }

    public void playStep() {
        if (!isPolygonReady()) return;
        if (Objects.isNull(camera)) {
            viewModel.setLabelText("Camera not yet in Polygon!");
            return;
        }
        nextStep();
    }

    public void updateGroups() {
        if (viewModel.getAllLines().isEmpty()) {
            return;
        }

        greenLines.getChildren().clear();
        redLines.getChildren().clear();
        yellowLines.getChildren().clear();

        for (Line line : viewModel.getAllLines()) {
            if (line.getStroke().equals(Color.GREEN)) {
                greenLines.getChildren().add(line);
            }
            if (line.getStroke().equals(Color.RED)) {
                redLines.getChildren().add(line);
            }
            if (line.getStroke().equals(Color.YELLOW)) {
                yellowLines.getChildren().add(line);
            }
        }
    }

    public void savePolygon() {
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

    }

    private void initMouseHandlerForPane() {
        mouseHandlerForPane = mouseEvent -> {
            if (isPrimaryOnPaneAndEmptyPolygon(mouseEvent)) {
                polyline.getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                PolygonModified.vertices.add(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                refreshLine();
            } else if (isPrimaryOnPaneAndFullPolygonAndScanNotDone(mouseEvent)) {
                polygon.addVertexAndPoint(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                updatePolygon();
            }

            if (isPrimaryOnPointAndEmptyPolygon(mouseEvent)) {
                Point point = (Point) mouseEvent.getTarget();
                if (point.getCenterX() == polyline.getPoints().get(0) && point.getCenterY() == polyline.getPoints().get(1)) {
                    polygon = new PolygonModified();
                    polygon.scaleXProperty().bind(pane.scaleXProperty());
                    point.scaleYProperty().bind(pane.scaleYProperty());
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
            camera.setOnMouseReleased(mouseEvent1 -> {
                updatePolygon();
            });
            updatePolygon();
        }
    }

    private void refreshLine() {
        root.getChildren().clear();
        root.getChildren().add(drawPolyline());
        root.getChildren().addAll(createControlPointsFor(polyline.getPoints()));
    }

    private void refreshView() {
        drawPolygon(true);
        if (Objects.nonNull(camera)) {
            root.getChildren().add(camera);
        }
    }

    private ObservableList<Point> visPolyPoints(ObservableList<Double> coordinates) {
        ObservableList<Point> points = FXCollections.observableArrayList();
        for (int i = 0; i < coordinates.size() - 2; i += 2) {
            Double x = coordinates.get(i);
            Double y = coordinates.get(i + 1);
            points.add(new Point(x, y));
        }
        return points;
    }


    private void drawVisPolygon(Polygon visPolygon) {

        visPolygon.setStroke(Color.BLUE);
        visPolygon.setStrokeWidth(3.3);
        visPolygon.setStrokeLineCap(StrokeLineCap.ROUND);
        visPolygon.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.3));
    }

    private void drawStepPolygon(Polygon stepPolygon) {

        stepPolygon.setStroke(Color.DARKRED);
        stepPolygon.setStrokeWidth(0.1);
        stepPolygon.setStrokeLineCap(StrokeLineCap.ROUND);
        stepPolygon.setFill(Color.RED.deriveColor(0, 1, 1, 0.3));
    }

    private void drawPolygon(boolean movable) {
        root.getChildren().clear();
        root.getChildren().add(polygon.draw());
        root.getChildren().addAll(polygon.createModeratePoints(movable));
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
            points.add(new Point(xProperty, yProperty, true));
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

    private boolean isPrimaryOnPaneAndFullPolygonAndScanNotDone(MouseEvent mouseEvent) {
        return (!isScanDone && mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getTarget() instanceof AnchorPane);
    }

    private boolean isSecondaryOnPointAndEmptyPolygon(MouseEvent mouseEvent) {
        return mouseEvent.getButton().equals(MouseButton.SECONDARY) && Objects.isNull(polygon) && mouseEvent.getTarget() instanceof Point;
    }

    private boolean isSecondaryOnPointAndFullPolygon(MouseEvent mouseEvent) {
        return mouseEvent.getButton().equals(MouseButton.SECONDARY) && Objects.nonNull(polygon) && mouseEvent.getTarget() instanceof Point;
    }
}

