package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.info.Level;
import com.bachelor.visualpolygon.info.Logger;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.view.shapes.Camera;
import com.bachelor.visualpolygon.view.shapes.Point;
import com.bachelor.visualpolygon.view.shapes.PolygonModified;
import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.animation.AnimationTimer;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewController {
    @FXML
    public Button playAllButton;
    public Button visPolyButton;
    @FXML
    BorderPane border;
    @FXML
    public Pane pane;
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

    @Getter
    Group mainGroup = new Group();
    private final Group root = new Group();
    private final Group redLines = new Group();
    private final Group greenLines = new Group();
    private final Group yellowLines = new Group();

    private final Polyline polyline;
    private PolygonModified polygon;
    private Polygon visPoly;
    private Camera camera;
    private final ObservableList<Double> cameraRequirements = FXCollections.observableArrayList();
    private final ListProperty<Vertex> listPropertyForVertex;
    private final ListProperty<Double> listPropertyForCamera;

    private ViewModel viewModel;

    private static final Logger logger = Logger.getLogger();
    private final LogView logView = new LogView(logger);

    private EventHandler<MouseEvent> mouseHandlerForPane;

    private boolean isScanStartedOrDone = false;
    AnimationTimer scannerAnimator;
    private static final double SCALE_DELTA = 1.1;


    public ViewController() {
        logger.setContext("Controller: ");
        initMouseHandlerForPane();
        polyline = new Polyline();
        listPropertyForVertex = new SimpleListProperty<>(PolygonModified.vertices);
        listPropertyForCamera = new SimpleListProperty<>(cameraRequirements);
        scannerAnimator = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 158_000_000) {
                    playStep();
                    lastUpdate = now;
                }
            }
        };
    }

    public void init(ViewModel viewModel) {
        this.viewModel = viewModel;
        initBindings(viewModel);
        initUploadList();
        initLogView();
        mainGroup.getChildren().addAll(root, redLines, greenLines, yellowLines);
        makePaneDraggable();
        pane.setOnMouseClicked(mouseHandlerForPane);
        pane.getChildren().add(mainGroup);
    }

    public void calculateAll() {
        visPoly = null;
        visPoly = viewModel.getInstantVisPoly();
        addVisiblePolygonToView();
    }

    public void playAll() {

        if (playAllButton.getText().equals("Play All") || playAllButton.getText().equals("Continue") || playAllButton.getText().equals("Restart Scan")) {
            scannerAnimator.start();
            playAllButton.setText("Pause");
            viewModel.setLabelText("Scanning Polygon");
        } else if (playAllButton.getText().equals("Pause")) {
            scannerAnimator.stop();
            playAllButton.setText("Continue");
            viewModel.setLabelText("Scanning Paused");
        }
    }

    public void playStep() {
        if (!isPolygonReady()) return;
        if (Objects.isNull(camera)) {
            viewModel.setLabelText("Camera not yet in Polygon!");
            return;
        }
        isScanStartedOrDone = true;
        if (viewModel.isScanDone()) {
            visPoly = viewModel.getVisPoly();
            addVisiblePolygonToView();
            playAllButton.setText("Restart Scan");
            scannerAnimator.stop();
            return;
        }
        Polygon stepPoly = viewModel.getStepPolygon();
        if (stepPoly != null) {
            drawPolygon(false);
            root.getChildren().add(camera);

            updateColorLineGroups();
            drawStepPolygon(stepPoly);
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
        viewModel.setLabelText("Polygon Uploaded");
    }

    public void resetApplication() {
        root.getChildren().clear();
        logView.resetLogg();
        clearColorLines();
        polyline.getPoints().clear();
        if (Objects.nonNull(polygon)) {
            polygon.getPoints().clear();
            polygon.getPoints().clear();
            PolygonModified.vertices.clear();
        }
        camera = null;
        visPoly = null;
        polygon = null;
        scannerAnimator.stop();
        listPropertyForVertex.clear();
        cameraRequirements.clear();
        viewModel.reset();
        playAllButton.setText("Play All");
        resetMainGroup();
    }

    private void resetMainGroup() {
        mainGroup.setScaleX(1.0);
        mainGroup.setScaleY(1.0);
        mainGroup.setTranslateX(0.0);
        mainGroup.setTranslateY(0.0);
    }

    private void updatePolygon() {
        if (!isPolygonReady()) return;
        viewModel.updateModel();
        drawPolygon(true);
        if (Objects.isNull(camera)) {
            viewModel.setLabelText("Polygon Updated! Click to add Camera");
        } else {
            root.getChildren().add(camera);
        }
    }

    private void updateColorLineGroups() {
        if (viewModel.getAllLines().isEmpty()) {
            return;
        }

        clearColorLines();

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

    private void clearColorLines() {
        greenLines.getChildren().clear();
        redLines.getChildren().clear();
        yellowLines.getChildren().clear();
    }

    private void refreshPolyLine() {
        root.getChildren().clear();
        root.getChildren().add(drawPolyline());
        root.getChildren().addAll(createControlPointsFor(polyline.getPoints()));
    }

    private void addVisiblePolygonToView() {
        root.getChildren().clear();
        drawPolygon(false);
        drawVisPolygon(visPoly);
        root.getChildren().add(camera);
        root.getChildren().addAll(visPolyPoints(visPoly.getPoints()));
    }

    private void drawVisPolygon(Polygon visPolygon) {
        visPolygon.setStroke(Color.BLUE);
        visPolygon.setStrokeWidth(3.3);
        visPolygon.setStrokeLineCap(StrokeLineCap.ROUND);
        visPolygon.setFill(Color.BLUE.deriveColor(0, 1, 1, 0.3));
        root.getChildren().add(visPoly);
    }

    private void drawStepPolygon(Polygon stepPolygon) {
        stepPolygon.setStroke(Color.DARKRED);
        stepPolygon.setStrokeWidth(0.1);
        stepPolygon.setStrokeLineCap(StrokeLineCap.ROUND);
        stepPolygon.setFill(Color.RED.deriveColor(0, 1, 1, 0.3));
        root.getChildren().add(stepPolygon);
    }

    private void drawPolygon(boolean movable) {
        root.getChildren().clear();
        root.getChildren().add(polygon.draw());
        ObservableList<Point> points = polygon.createModeratePoints(movable);
        points.forEach(point -> point.setOnMouseClicked(mouseEvent ->
                viewModel.setLabelText("VERTEX { " + point.getCenterX() + " ; " + point.getCenterY() + " }")));
        root.getChildren().addAll(points);
    }

    private Polyline drawPolyline() {
        polyline.setStroke(Color.AZURE);
        polyline.setStrokeWidth(3);
        polyline.setStrokeLineCap(StrokeLineCap.ROUND);

        return polyline;
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

    private ObservableList<Point> createControlPointsFor(final ObservableList<Double> coordinates) {
        ObservableList<Point> points = FXCollections.observableArrayList();
        for (int i = 0; i < coordinates.size(); i += 2) {
            final int idx = i;
            DoubleProperty xProperty = new SimpleDoubleProperty(coordinates.get(i));
            DoubleProperty yProperty = new SimpleDoubleProperty(coordinates.get(i + 1));

            xProperty.addListener((ov, oldX, x) -> {
                coordinates.set(idx, (double) x);
                PolygonModified.vertices.get(idx / 2).getXProperty().set(x.doubleValue());
                PolygonModified.vertices.get(idx / 2).setX(x.doubleValue());
            });
            yProperty.addListener((ov, oldY, y) -> {
                coordinates.set(idx + 1, (double) y);
                PolygonModified.vertices.get(idx / 2).getYProperty().set(y.doubleValue());
                PolygonModified.vertices.get(idx / 2).setY(y.doubleValue());
            });
            points.add(new Point(xProperty, yProperty, true));
        }
        return points;
    }

    void makePaneDraggable() {
        SceneGestures sceneGestures = new SceneGestures(mainGroup);
        mainGroup.addEventFilter(MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        mainGroup.addEventFilter(MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        mainGroup.setOnScroll(this::handleScroll);
    }

    private void initBindings(ViewModel viewModel) {
        statusText.textProperty().bindBidirectional(viewModel.labelTextProperty());
        redLines.visibleProperty().bindBidirectional(redBox.selectedProperty());
        greenLines.visibleProperty().bindBidirectional(greenBox.selectedProperty());
        yellowLines.visibleProperty().bindBidirectional(yellowBox.selectedProperty());
        listPropertyForVertex.bindContentBidirectional(viewModel.getAllVertices());
        listPropertyForCamera.bindContentBidirectional(viewModel.getCameraDetails());
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

    private void initMouseHandlerForPane() {
        mouseHandlerForPane = mouseEvent -> {
            if (isPrimaryOnPaneAndEmptyPolygon(mouseEvent)) {
                polyline.getPoints().addAll(mouseEvent.getX(), mouseEvent.getY());
                PolygonModified.vertices.add(new Vertex(mouseEvent.getX(), mouseEvent.getY()));
                refreshPolyLine();
            } else if (isPrimaryOnPaneAndFullPolygonAndScanNotDone(mouseEvent)) {
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
            onSecondaryMouseButton(mouseEvent);
        };
    }

    private void onSecondaryMouseButton(MouseEvent mouseEvent) {

        if (isSecondaryOnPointAndEmptyPolygon(mouseEvent)) {
            Point point = (Point) mouseEvent.getTarget();
            polyline.getPoints().removeAll(point.getCenterX(), point.getCenterY());
            PolygonModified.vertices.removeIf(vertex -> vertex.getXCoordinate() == point.getCenterX() && vertex.getYCoordinate() == point.getCenterY());
            refreshPolyLine();
        }

        if (isSecondaryOnPointAndFullPolygon(mouseEvent)) {
            Point point = (Point) mouseEvent.getTarget();
            polygon.removeVertexAndPoint(point);
            updatePolygon();
        }

        if (mouseEvent.getTarget() instanceof Polygon && Objects.isNull(camera)) {
            cameraRequirements.addAll(mouseEvent.getX(), mouseEvent.getY(), 40.0);
            camera = new Camera(cameraRequirements);
            camera.setOnMousePressed(mouseEvent3 -> {
                resetVertices();
                updatePolygon();
                isScanStartedOrDone = false;
                camera.getScene().setCursor(Cursor.MOVE);
            });

            camera.setOnMouseClicked(click -> {
                if (click.getClickCount() == 2) {
                    camera.getScene().setCursor(Cursor.HAND);
                    updatePolygon();
                    calculateAll();
                }
            });
            camera.setOnMouseExited(mouseEvent2 -> {
                if (!mouseEvent2.isPrimaryButtonDown()) {
                    cameraRequirements.set(2,camera.getR().getValue());
                    viewModel.updateModel();
                    camera.getScene().setCursor(javafx.scene.Cursor.HAND);
                }
            });
            updatePolygon();
        }
    }

    private void resetVertices() {
        List<Vertex> temp = new ArrayList<>();
        for (int i = 0; i < PolygonModified.vertices.size(); i++) {
            Vertex vertex = PolygonModified.vertices.get(i);
            logger.debug("IS For Visual Polygon ? " + vertex.isForVisualPolygon());
            if (!vertex.isForVisualPolygon()) {
                vertex.setIsVisible(0);
                temp.add(vertex);
            }
        }
        PolygonModified.vertices.clear();
        PolygonModified.vertices.addAll(temp);
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
        return (isPrimaryAndEmptyPolygon(mouseEvent) && mouseEvent.getTarget() instanceof Pane);
    }

    private boolean isPrimaryOnPointAndEmptyPolygon(MouseEvent mouseEvent) {
        return (isPrimaryAndEmptyPolygon(mouseEvent) && mouseEvent.getTarget() instanceof Point);
    }

    private boolean isPrimaryOnPaneAndFullPolygonAndScanNotDone(MouseEvent mouseEvent) {
        return (!isScanStartedOrDone && mouseEvent.getButton().equals(MouseButton.PRIMARY) && mouseEvent.getTarget() instanceof Pane);
    }

    private boolean isSecondaryOnPointAndEmptyPolygon(MouseEvent mouseEvent) {
        return mouseEvent.getButton().equals(MouseButton.SECONDARY) && Objects.isNull(polygon) && mouseEvent.getTarget() instanceof Point;
    }

    private boolean isSecondaryOnPointAndFullPolygon(MouseEvent mouseEvent) {
        return mouseEvent.getButton().equals(MouseButton.SECONDARY) && Objects.nonNull(polygon) && mouseEvent.getTarget() instanceof Point;
    }

    private void handleScroll(ScrollEvent scrollEvent) {
        scrollEvent.consume();

        if (scrollEvent.getDeltaY() == 0) {
            return;
        }

        double scaleFactor =
                (scrollEvent.getDeltaY() > 0)
                        ? SCALE_DELTA
                        : 1 / SCALE_DELTA;

        mainGroup.setScaleX(mainGroup.getScaleX() * scaleFactor);
        mainGroup.setScaleY(mainGroup.getScaleY() * scaleFactor);

    }
}

