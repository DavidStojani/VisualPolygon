package com.bachelor.visualpolygon.model.geometry;


import com.bachelor.visualpolygon.logging.Logger;
import javafx.scene.paint.Color;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.operation.overlay.validate.FuzzyPointLocator;
import org.locationtech.jts.operation.overlayng.OverlayNGRobust;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@NoArgsConstructor
@Getter
public class Builder extends Initializer {

    public static final GeometryCamera camera = new GeometryCamera();
    Polygon polygon;
    Polygon stepPolygon;
    Polygon visPolygon;
    List<Vertex> vertices;
    CoordinateList visPolygonVertices;
    CoordinateList coordinateList = new CoordinateList();
    List<Vertex> initialVertices = new ArrayList<>();
    List<Vertex> extraVertices = new ArrayList<>();
    List<Coordinate> stepCoordinates = new ArrayList<>();
    @Setter
    Vertex nextVertex;
    private LineSegment alpha;
    private LineSegment beta;
    private List<Vertex> active;
    private List<Vertex> tempVisible;
    private List<Vertex> tempInvisible;
    private final Logger logger = Logger.getLogger();


    /**
     * Takes Polygon and Camera as Shape Objects from View and updates with those the Geometry Objects
     */
    public void updateBuilder(List<Vertex> vertices, List<Double> cameraDetails) {
        this.vertices = vertices;
        logger.info("In Total : " + vertices.size() + "Vertices");
        if (!cameraDetails.isEmpty()) {
            camera.setDetails(cameraDetails);
            init();
        } else {
            polygon = createGeometryPolygon(vertices);
            logger.error("No camera:  size of vertices " + vertices.size());
        }
    }

    /**
     * After every update from View initializes the vertices and the Geometry
     */
    public void init() {
        logger.setContext("Builder: ");
        polygon = createGeometryPolygon(vertices);
        calculatePolarCoordinates(vertices);
        polarSortedVertices = sortPolarCoordinate(vertices);
    }


    /**
     * Creates the Step for the next Vertex
     * @param vertex is where the Stripe will holt again
     * Checks if the vertex is inside the current Step. If so the next Step will be build using Beta, otherwise using Alpha*/
    public void createNextStep(Vertex vertex) {
        if (Objects.isNull(vertex)) {
            vertex = polarSortedVertices.stream().max(Comparator.comparing(Vertex::getTheta)).orElseThrow();
        }
        clearLines();
        logger.debug("===========NEXT STEP=========");

        if (isInsideActive(vertex)) {
            createStepFromBETA(vertex);
        } else {
            createStepFromALPHA(vertex);
        }
        setActive();
        setTemps();
        doubleCheckInvisible();
        addNewVertices();
        nextVertex = findNextVertex();

    }

    /**
     * The Vertices inside the Stripe will be preserved in active-List*/
    private void setActive() {
        active = new ArrayList<>();
        FuzzyPointLocator pointLocator = new FuzzyPointLocator(stepPolygon, 0.001);
        for (Vertex vertex : polarSortedVertices) {
            if (pointLocator.getLocation(vertex) != 2) {
                active.add(vertex);
            }
        }
        logger.debug("ACTIVE SIZE: " + active.size());
    }


    //for every point in active build a parallel to the Step and check if it intersects with the circle with no interruption
    /**
     * Determines which of the Vertices in active are visible*/
    private void setTemps() {
        tempInvisible = new ArrayList<>();
        tempVisible = new ArrayList<>();
        for (Vertex vertex : active) {
            LineSegment parallelCameraToVertex = new LineSegment(getParallelLine(vertex).p0, vertex);
            if (parallelCameraToVertex.toGeometry(factory).within(polygon)) {
                tempVisible.add(vertex);
                vertex.setIsVisible(1);
                addLine(parallelCameraToVertex, Color.GREEN);
            } else {

                tempInvisible.add(vertex);
                if (vertex.getIsVisible() != 1) {
                    vertex.setIsVisible(-1);
                }
                addLine(parallelCameraToVertex, Color.RED);
            }
        }
    }

    /**Uses the Edges of the Visibility Graph to make sure a Vertex is really invisible*/
    private void doubleCheckInvisible() {
        if (tempInvisible.isEmpty()) {
            return;
        }
        for (int i = 0; i < tempInvisible.size(); i++) {
            boolean flagVisible = false;
            Vertex invisible = tempInvisible.get(i);
            for (int j = 0; j < tempVisible.size(); j++) {
                Vertex visible = tempVisible.get(j);
                LineSegment visibleToInvisible = new LineSegment(visible, invisible);
                if (polygon.covers(visibleToInvisible.toGeometry(factory)) && isInCollisionWithCamera(visibleToInvisible)) {
                    addLine(new LineSegment(invisible, getIntersectionPointWithCamera(visibleToInvisible)), Color.GREEN);
                    LineSegment extentOfVisibleToInvisible = new LineSegment(visible, getExtentCoordinate(invisible, visible));
                    addNewVertex(invisible, extentOfVisibleToInvisible);
                    flagVisible = true;
                }
            }
            if (flagVisible) {
                invisible.setIsVisible(1);
                tempVisible.add(invisible);
                tempInvisible.remove(invisible);
            }
        }
    }

    public void addNewVertices() {
        for (Vertex vertex : tempVisible) {
            LineSegment line = getParallelLine(vertex);
            addNewVertex(vertex, line);
        }
    }

    /**Finds the first Intersection of the Segment with the Polygon and saves it*/
    private boolean addNewVertex(Vertex vertex, LineSegment line) {
        Polygon testPolygon;
        try {
            testPolygon = addIntersectionsToTestPolygon(line);
        } catch (Exception e) {
            return true;
        }
        logger.debug("From Camera to Extension " + line.toGeometry(factory));

        Coordinate base = line.p0;
        Geometry intersections = testPolygon.getBoundary().intersection(line.toGeometry(factory));
        Coordinate nearestIntersection = null;
        double minDistanceBaseToIntersection = 999999;
        for (Coordinate intersection : intersections.getCoordinates()) {
            precision.makePrecise(intersection);
            if (vertex.equalCoordinate(intersection) || base.equals(intersection)) {
                logger.debug("Skipped Intersection: " + intersection + "--- VERTEX: " + vertex);
                continue;
            }
            if (base.distance(intersection) < minDistanceBaseToIntersection) {
                minDistanceBaseToIntersection = base.distance(intersection);
                if (minDistanceBaseToIntersection < base.distance(vertex)) {
                    logger.debug("Intersection is before Vertex!!");
                    continue;
                }
                nearestIntersection = intersection;
            }
        }

        if (nearestIntersection == null) {
            logger.debug("No Intersection found!!!");
            return true;
        }

        logger.debug("FOUND FIRST INTERSECTION --- " + nearestIntersection);

        LineSegment toTest = new LineSegment(vertex, nearestIntersection);
        LineString vertexToNearestIntersection = toTest.toGeometry(factory);
        logger.debug("Line from: Vertex to nearestIntersection ---- " + vertexToNearestIntersection);
        if (vertexToNearestIntersection.getLength() == 0.0) {
            logger.debug(" BUG avoidance ");
            return true;
        }


        /**TODO: Add another Test for Coordinates different only with +/- 0.1*/
        if (testPolygon.covers(vertexToNearestIntersection) || testPolygon.covers(vertexToNearestIntersection.reverse())) {
            Vertex v = new Vertex(nearestIntersection);
            v.setIsVisible(2);
            v.setForVisualPolygon(true);
            vertices.add(v);
            extraVertices.add(v);
            addLine(toTest, Color.YELLOW);
        }

        return false;
    }

    /**
     * Determines the next Vertex that the Stripe will hold comparing the angle it takes to rotate and choosing the smalles one*/
    private Vertex findNextVertex() {
        if (active.isEmpty()) {
            throw new RuntimeException("active  IS EMPTY");
        }
        Vertex tempALFA = null;
        double angleToALPHA = 999999;
        Vertex tempBETA = null;
        double angleToBETA = 99999;

        List<Vertex> leftToALPHA = polarSortedVertices.stream()
                .filter(vertex -> alpha.orientationIndex(vertex) == Orientation.CLOCKWISE)
                .filter(vertex -> !active.contains(vertex))
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());

        for (Vertex v : leftToALPHA) {
            double angle = Angle.angleBetween(alpha.p0, camera.getCenter(), camera.getLeftTangentPoint(v));
            if (angle < angleToALPHA && angle > 0) {
                angleToALPHA = angle;
                tempALFA = v;
            }
        }

        for (Vertex v : active) {
            double angle = Angle.angleBetween(beta.p0, camera.getCenter(), camera.getRightTangentPoint(v));
            if (angle < angleToBETA && angle > EPSILON) {
                angleToBETA = angle;
                tempBETA = v;
            }
        }


        if (angleToBETA < angleToALPHA) {
            return tempBETA;
        }
        return tempALFA;
    }


    /**Iterates the Edges of the Polygon and creates the Visibility-Polygon*/
    public void createVisPolygon() {
        List<Vertex> verticesOnAB;
        coordinateList.clear();

        for (int i = 0; i < polygon.getCoordinates().length - 1; i++) {
            Vertex a = (Vertex) polygon.getCoordinates()[i];
            Vertex b = (Vertex) polygon.getCoordinates()[i + 1];
            verticesOnAB = findVerticesOnAB(a, b);

            if (a.getIsVisible() == 1 && b.getIsVisible() == 1) {
                coordinateList.add(a);
                coordinateList.add(b);
            }

            if (a.getIsVisible() == 1 && b.getIsVisible() == -1) {
                coordinateList.add(a);
                addClosestToEndpoint(verticesOnAB, b);
            }

            if (a.getIsVisible() == -1 && b.getIsVisible() == 1) {
                addClosestToEndpoint(verticesOnAB, a);
                coordinateList.add(b);
            }
            if (a.getIsVisible() == -1 && b.getIsVisible() == -1) {
                addClosestToEndpoint(verticesOnAB, a);
                addClosestToEndpoint(verticesOnAB, b);
            }
        }

        Coordinate[] array = CoordinateArrays.removeRepeatedPoints(coordinateList.toCoordinateArray());

        visPolygonVertices = new CoordinateList(array);
    }

    private Polygon addIntersectionsToTestPolygon(LineSegment line) throws Exception {

        Geometry pol = polygon.copy();
        Geometry union = OverlayNGRobust.overlay(pol, line.toGeometry(factory), 2);
        GeometryCollectionIterator iterator = new GeometryCollectionIterator(union);
        while (iterator.hasNext()) {
            Geometry g = (Geometry) iterator.next();
            if (g.getGeometryType().equals("Polygon")) {
                for (Coordinate c : g.getCoordinates()) {
                    precision.makePrecise(c);
                }
                return (Polygon) g;
            }
        }
        throw new Exception("POLYGON NOT FOUND");
    }

    private void addClosestToEndpoint(List<Vertex> verticesOnAB, Vertex endpoint) {
        Vertex vertex = null;
        double minDistance = 999999;
        for (Vertex v : verticesOnAB) {
            if (endpoint.distance(v) < minDistance) {
                minDistance = endpoint.distance(v);
                vertex = v;
            }
        }
        if (Objects.nonNull(vertex)) {
            coordinateList.add(vertex);
            verticesOnAB.remove(vertex);
        }
    }

    private List<Vertex> findVerticesOnAB(Vertex a, Vertex b) {

        List<Vertex> verticesOnAB = new ArrayList<>();
        LineSegment segmentAB = new LineSegment(a, b);
        for (Vertex vertex : extraVertices) {

            if (segmentAB.distancePerpendicular(vertex) <= 0.009) {
                verticesOnAB.add(vertex);
            }
        }
        return verticesOnAB;
    }

    private LineSegment getParallelLine(Coordinate point) {
        Coordinate baseMirror = alpha.pointAlongOffset(0, alpha.distancePerpendicular(point));
        Coordinate endMirror = alpha.pointAlongOffset(1, alpha.distancePerpendicular(point));

        return new LineSegment(baseMirror, endMirror);
    }

    //Should give back the 4 coordinates. Those should be given to form the polygon and
    //to the viewController to render the view. In the view they should not cross the borders of pane

    private void createStepFromBETA(Vertex vertex) {
        CoordinateList coordinates = new CoordinateList();

        Coordinate rightPointOnCircle = camera.getRightTangentPoint(vertex);
        coordinates.add(rightPointOnCircle);
        coordinates.add(getExtentCoordinate(vertex, rightPointOnCircle));
        LineSegment rightTangent = new LineSegment(rightPointOnCircle, getExtentCoordinate(vertex, rightPointOnCircle));

        Coordinate leftPointOnCircle = rightTangent.pointAlongOffset(0, -camera.getRadius() * 2);
        Coordinate mirrorOfExtent = rightTangent.pointAlongOffset(1, -camera.getRadius() * 2);
        coordinates.add(mirrorOfExtent);
        coordinates.add(leftPointOnCircle);

        stepPolygon = createStepPolygon(coordinates);
        this.stepCoordinates = coordinates;
        setAlpha(coordinates.get(3), coordinates.get(2));
        setBeta(coordinates.get(0), coordinates.get(1));
        addLine(new LineSegment(coordinates.get(3), coordinates.get(0)), Color.RED);
        increaseCount();
    }

    private void createStepFromALPHA(Vertex vertex) {
        CoordinateList coordinates = new CoordinateList();
        Coordinate leftPointOnCircle = camera.getLeftTangentPoint(vertex);
        coordinates.add(leftPointOnCircle);
        coordinates.add(getExtentCoordinate(vertex, leftPointOnCircle));
        LineSegment leftTangent = new LineSegment(leftPointOnCircle, getExtentCoordinate(vertex, leftPointOnCircle));

        Coordinate rightPointOnCircle = leftTangent.pointAlongOffset(0, camera.getRadius() * 2);
        Coordinate mirrorOfExtent = leftTangent.pointAlongOffset(1, camera.getRadius() * 2);
        coordinates.add(mirrorOfExtent);
        coordinates.add(rightPointOnCircle);

        stepPolygon = createStepPolygon(coordinates);
        this.stepCoordinates = coordinates;
        setAlpha(coordinates.get(0), coordinates.get(1));
        setBeta(coordinates.get(3), coordinates.get(2));
        addLine(new LineSegment(coordinates.get(3), coordinates.get(0)), Color.RED);
        increaseCount();

    }

    private Coordinate getExtentCoordinate(Vertex vertex, Coordinate base) {
        Vector2D vector2D = new Vector2D(base, vertex);
        double k = getMaxDistanceFrom() / base.distance(vertex);
        Vector2D extentVector = vector2D.multiply(k);
        double x = extentVector.getX() + base.getX();
        double y = extentVector.getY() + base.getY();

        return new Coordinate(x, y);

    }

    private void setAlpha(Coordinate start, Coordinate end) {
        alpha = new LineSegment(start, end);
    }

    private void setBeta(Coordinate start, Coordinate end) {
        beta = new LineSegment(start, end);
    }

    private boolean isInsideActive(Vertex vertex) {
        if (Objects.isNull(active)) {
            return false;
        }
        return active.contains(vertex);
    }

    public CoordinateList getInstantVisualPolygon() {
        setCount(0);
        extraVertices.clear();
        double startTime = System.currentTimeMillis();
        while (!isScanComplete()) {
            createNextStep(nextVertex);
        }
        createVisPolygon();
        double endTime = System.currentTimeMillis();
        logger.info("Time Required: " + (endTime - startTime));
        return visPolygonVertices;
    }
}
