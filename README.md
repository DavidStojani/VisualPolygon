# VisualPolygon

VisualPolygon is a JavaFX desktop application for drawing polygons, placing a circular "camera," and visualizing which vertices and edges are visible from that camera. It combines interactive editing with computational geometry powered by JTS to animate the step-by-step construction of a visibility polygon.

## Key Features
- Draw polygons interactively or load them from WKT files stored in `src/test/resources`.
- Place a circular camera inside the polygon and visualize visibility lines (green), occluded lines (red), and intermediate intersections (yellow).
- Step through the scanning process or run it automatically to build the full visibility polygon.
- Save polygons back to disk via a WKT writer.
- Basic logging panel with filtering and pause/tail controls.

## High-Level Architecture
```
+--------------------+       +----------------+       +---------------------------+
| JavaFX View (FXML) | <---> | ViewModel      | <---> | Model (Geometry Builder)  |
| - ViewController   |       | - ViewModel    |       | - DataModel / Builder     |
| - Shapes & Camera  |       | - Observable   |       | - GeometryCamera, Vertex  |
+--------------------+       +----------------+       +---------------------------+
        |                            |                            |
        | user events                | binds / updates            | JTS geometry
        v                            v                            v
  Render polygons,         Tracks vertices, camera       Computes steps, lines,
  drag/zoom/pan,           details, files, status,       and visibility polygon
  trigger scan steps       and delegates to model        using JTS operations
```

## Technologies
- Java 11
- JavaFX (controls, FXML)
- JTS Topology Suite (geometry operations)
- Lombok
- Maven
- JUnit 5

## Setup & Installation
1. Ensure Java 11+ and Maven are installed.
2. Clone the repository and navigate to its root.
3. Download dependencies:
   ```bash
   mvn dependency:resolve
   ```

## Running the Application
```bash
mvn clean javafx:run
```
This launches the JavaFX UI, starting from `com.bachelor.visualpolygon.StartApp`.

### Running Tests
```bash
mvn test
```
_Note: in restricted environments Maven may be unable to resolve plugins from Maven Central._

## API & Interaction Overview
- **View layer (`view` package)**: `ViewController` wires FXML controls, handles mouse events for drawing/editing polygons, manages zoom/pan (`SceneGestures`), and renders polygons/lines via `PolygonModified`, `Point`, and `Camera` shapes.
- **ViewModel layer (`viewmodel` package)**: `ViewModel` exposes observable lists for vertices, camera details, status text, file picker entries, and delegates geometry updates to the model. It also loads/saves polygons using WKT readers/writers.
- **Model layer (`model` package)**: `DataModelManager` implements `DataModel` by coordinating the `geometry` package. `Builder` drives the sweep/stripe algorithm, leveraging `Initializer` utilities, `GeometryCamera` for tangents, and `Vertex` objects tied to JavaFX properties.

## Folder Structure
```
VisualPolygon/
├── pom.xml
├── README.md
├── src/
│   ├── main/java/com/bachelor/visualpolygon/
│   │   ├── App.java
│   │   ├── StartApp.java
│   │   ├── logging/
│   │   ├── model/
│   │   │   └── geometry/
│   │   ├── view/
│   │   │   └── shapes/
│   │   └── viewmodel/
│   └── test/java/com/bachelor/visualpolygon/
│       └── ...
└── src/test/resources/   # Sample WKT polygons
```

## Future Roadmap & Improvement Ideas
- Add validation to prevent creating invalid or self-intersecting polygons during drawing.
- Persist user settings (last loaded file, default camera radius, theme).
- Provide headless computation mode (CLI) for batch visibility calculations.
- Replace static collections (e.g., vertices) with instance-level state to support multiple polygons or sessions.
- Improve error handling and user feedback for geometry exceptions (e.g., camera inside polygon).

## Contribution Guidelines
1. Fork the repository and create a feature branch.
2. Format code consistently with existing style (Java 11, Lombok-enabled).
3. Add tests for new functionality where feasible (`src/test/java`).
4. Run `mvn test` (or explain environment limitations) before submitting.
5. Open a pull request describing the change and rationale.
