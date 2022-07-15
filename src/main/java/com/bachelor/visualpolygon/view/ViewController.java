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

    private ViewModel viewModel;

    Logger logger = new Logger("controller");
    LogView logView = new LogView(logger);

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
        ChoiceBox<Level> filterLevel = new ChoiceBox<>(
                FXCollections.observableArrayList(
                        Level.values()
                )
        );
        filterLevel.getSelectionModel().select(Level.DEBUG);
        logView.filterLevelProperty().bind(
                filterLevel.getSelectionModel().selectedItemProperty()
        );

        ToggleButton showTimestamp = new ToggleButton("Show Timestamp");
        logView.showTimeStampProperty().bind(showTimestamp.selectedProperty());

        ToggleButton tail = new ToggleButton("Tail");
        logView.tailProperty().bind(tail.selectedProperty());

        ToggleButton pause = new ToggleButton("Pause");
        logView.pausedProperty().bind(pause.selectedProperty());

        HBox controls = new HBox(10, filterLevel, showTimestamp, tail, pause);
        controls.setMinHeight(HBox.USE_PREF_SIZE);

        VBox layout = new VBox(10, controls, logView);
        VBox.setVgrow(logView, Priority.ALWAYS);
        border.setRight(layout);
        layout.setPrefSize(370, 407);

    }

    public void initList() {
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
                File f = uploadList.getSelectionModel().getSelectedItem();
                uploadPolygon(f);
            }
        });
    }

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
        logger.error("TE QIFSHA MOTREN");
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
        /**TODO Keep the stepPolygon inside the AnchorPane or change to another Pane ?*/
        viewModel.setStepInfo();
        if (stepPoly != null) {
            refreshView();
            drawStepPolygon(stepPoly);
            root.getChildren().add(stepPoly);
            root.getChildren().add(viewModel.getParallels().pop());
        }
    }

    public void playStep() {
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

    private void drawPolygon(boolean movalbe) {
        root.getChildren().clear();
        root.getChildren().add(polygon.draw());
        root.getChildren().addAll(polygon.createModeratePoints(movalbe));
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

