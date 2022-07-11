package com.bachelor.visualpolygon.model.geometry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.operation.overlay.validate.FuzzyPointLocator;
import org.locationtech.jts.precision.GeometryPrecisionReducer;

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
    List<Vertex> vertices;
    List<Coordinate> stepCoordinates = new ArrayList<>();
    @Setter
    Vertex nextVertex;
    private LineSegment ALPHA;
    private LineSegment BETA;
    private List<Vertex> active;
    private List<Vertex> tempVisible;
    private List<Vertex> tempInvisible;


    /**
     * Takes Polygon and Camera as Shape Objects from View and updates with those the Geometry Objects
     */
    public void updateBuilder(List<Vertex> vertices, List<Double> cameraDetails) {
        lineStack.clear();
        this.vertices = vertices;
        if (!cameraDetails.isEmpty()) {
            camera.setDetails(cameraDetails);
            init();
        } else {
            polygon = createGeometryPolygon(vertices);
        }
    }

    /**
     * After every update from View initializes the vertices
     */
    public void init() {
        polygon = createGeometryPolygon(vertices);
        calculatePolarCoordinates(vertices);
        polarSortedVertices = sortPolarCoordinate(vertices);
        firstVertex = polarSortedVertices.stream().max(Comparator.comparing(Vertex::getTheta)).orElseThrow();
    }


    public void createStep(Vertex vertex) {
        if (Objects.isNull(vertex)) {
            vertex = firstVertex;
        }
        System.out.println("========================= ON VERTEX ------- " + vertex);

        if (isInsideActive(vertex)) {
            System.out.println("+++++++WAS INSIDE STEP+++++++++++");
            createStepFromBETA(vertex);

        } else {
            System.out.println("-------WAS OUTSIDE STEP-----------");
            createStepFromALPHA(vertex);
        }
        setActive();
        setTemps();
        doubleChekInvisibles();
        addNewVertecies();
        nextVertex = findNextVertex();
    }


    public Vertex findNextVertex() {
        if (active.isEmpty()) {
            throw new RuntimeException("active  IS EMPTY");
        }
        Vertex tempALFA = null;
        double angleToALPHA = 999999;
        Vertex tempBETA = null;
        double angleToBETA = 99999;

        List<Vertex> leftToALPHA = polarSortedVertices.stream()
                .filter(vertex -> ALPHA.orientationIndex(vertex) == Orientation.CLOCKWISE)
                .filter(vertex -> !active.contains(vertex))
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());

        for (Vertex v : active) {
            double angle = Angle.angleBetween(v, BETA.p0, BETA.p1);
            if (angle < angleToBETA && angle > EPSILON) {
                angleToBETA = angle;
                tempBETA = v;
            }
        }

        for (Vertex v : leftToALPHA) {
            double angle = Angle.angleBetween(v, ALPHA.p0, ALPHA.p1);
            if (angle < angleToALPHA && angle > 0) {
                angleToALPHA = angle;
                tempALFA = v;
            }
        }

        if (angleToBETA < angleToALPHA) {
            return tempBETA;
        }
        return tempALFA;
    }


    public boolean isInsideActive(Vertex vertex) {
        if (Objects.isNull(active)) {
            return false;
        }
        return active.contains(vertex);
    }


    private void setActive() {
        active = new ArrayList<>();
        FuzzyPointLocator pointLocator = new FuzzyPointLocator(stepPolygon, 0.1);
        for (Vertex vertex : polarSortedVertices) {
            if (pointLocator.getLocation(vertex) != 2) {
                active.add(vertex);
            }
        }
    }

    //for every point in active build a parallel to the Streifen and check if it intersects with the circle with no interruption
    private void setTemps() {
        tempInvisible = new ArrayList<>();
        tempVisible = new ArrayList<>();
        for (Vertex vertex : active) {
            LineSegment parallelCameraToVertex = new LineSegment(getParallelLine(vertex).p0, vertex);
            if (parallelCameraToVertex.toGeometry(factory).within(polygon)) {
                tempVisible.add(vertex);
                vertex.setIsVisible(1);
                addToGreenLines(parallelCameraToVertex);

            } else {
                tempInvisible.add(vertex);
                if (vertex.getIsVisible() != 1) {
                    vertex.setIsVisible(-1);
                }
                addToRedLines(parallelCameraToVertex);
            }
        }
    }

    public void addNewVertecies() {

        for (Vertex vertex : tempVisible) {
            LineSegment line = getParallelLine(vertex);
            if (addNewVertex(vertex, line)) {
                System.out.println("Nothing was added!");
                return;
            }
        }
    }

    private boolean addNewVertex(Vertex vertex, LineSegment line) {
        Coordinate base = line.p0;
        Geometry intersections = polygon.getBoundary().intersection(line.toGeometry(factory));
        intersections = GeometryPrecisionReducer.reduce(intersections, precision);
        Coordinate nearestIntersection = null;
        double minDistanceBaseToIntersection = 999999;
        for (Coordinate intersection : intersections.getCoordinates()) {
            if (vertex.equalCoordinate(intersection) || base.equals(intersection)) {
                System.out.println("Skipped " + intersection);
                continue;
            }
            if (base.distance(intersection) < minDistanceBaseToIntersection) {
                minDistanceBaseToIntersection = base.distance(intersection);
                nearestIntersection = intersection;
            }
        }
        if (nearestIntersection == null) {
            System.out.println("No Intersection found!!!");
            return true;
        }

        if (minDistanceBaseToIntersection < base.distance(vertex)) {
            System.out.println("Intersection is before Vertex!!");
            return true;
        }

        Vertex v = new Vertex(nearestIntersection);
        v.setIsVisible(1);
        vertices.add(v);
        System.out.println(v + " was added!!");
        LineSegment toTest = new LineSegment(base, nearestIntersection);
        System.out.println("IS toTEST in ? " + polygon.within(toTest.toGeometry(factory)));
        addToBlackLine(toTest);
        return false;
    }


    public void doubleChekInvisibles() {
        if (tempInvisible.isEmpty()) {
            return;
        }
        for (int i = 0; i < tempVisible.size(); i++) {
            Vertex visible = tempVisible.get(i);
            for (int j = 0; j < tempInvisible.size(); j++) {
                Vertex invisible = tempInvisible.get(j);
                LineSegment visibleToInvisible = new LineSegment(visible, invisible);
                if (polygon.covers(visibleToInvisible.toGeometry(factory)) && isInCollisionWithCamera(visibleToInvisible)) {
                    invisible.setIsVisible(1);
                    addToGreenLines(new LineSegment(invisible, getIntersectionPointWithCamera(visibleToInvisible)));
                    tempVisible.add(invisible);
                    LineSegment extentOfVisibleToInvisible = new LineSegment(visible, getExtentCoordinate(invisible, visible));
                    addNewVertex(invisible, extentOfVisibleToInvisible);
                    tempInvisible.remove(invisible);
                }
            }
        }
    }

    public LineSegment getParallelLine(Coordinate point) {
        Coordinate baseMirror = ALPHA.pointAlongOffset(0, ALPHA.distancePerpendicular(point));
        Coordinate endMirror = ALPHA.pointAlongOffset(1, ALPHA.distancePerpendicular(point));

        return new LineSegment(baseMirror, endMirror);
    }


    //Should give back the 4 coordinates. Those should be given to form the polygon and
    //to the viewController to render the view. In the view they should not cross the borders of pane
    public void createStepFromBETA(Vertex vertex) {
        CoordinateList streifenCoordinates = new CoordinateList();

        Coordinate rightPointOnCircle = camera.getRightTangentPoint(vertex);
        streifenCoordinates.add(rightPointOnCircle);
        streifenCoordinates.add(getExtentCoordinate(vertex, rightPointOnCircle));
        LineSegment rightTangent = new LineSegment(rightPointOnCircle, getExtentCoordinate(vertex, rightPointOnCircle));

        Coordinate leftPointOnCircle = rightTangent.pointAlongOffset(0, -camera.getRadius() * 2);
        Coordinate mirrorOfExtent = rightTangent.pointAlongOffset(1, -camera.getRadius() * 2);
        streifenCoordinates.add(mirrorOfExtent);
        streifenCoordinates.add(leftPointOnCircle);

        stepPolygon = createStepPolygon(streifenCoordinates);
        stepCoordinates = streifenCoordinates;
        setALPHA(streifenCoordinates.get(3), streifenCoordinates.get(2));
        setBETA(streifenCoordinates.get(0), streifenCoordinates.get(1));
        vertex.increaseVisited();

    }

    public void createStepFromALPHA(Vertex vertex) {
        CoordinateList streifenCoordinates = new CoordinateList();
        System.out.println("ALPHA CALLED");
        Coordinate leftPointOnCircle = camera.getLeftTangentPoint(vertex);
        streifenCoordinates.add(leftPointOnCircle);
        streifenCoordinates.add(getExtentCoordinate(vertex, leftPointOnCircle));
        LineSegment leftTangent = new LineSegment(leftPointOnCircle, getExtentCoordinate(vertex, leftPointOnCircle));

        Coordinate rightPointOnCircle = leftTangent.pointAlongOffset(0, camera.getRadius() * 2);
        Coordinate mirrorOfExtent = leftTangent.pointAlongOffset(1, camera.getRadius() * 2);
        streifenCoordinates.add(mirrorOfExtent);
        streifenCoordinates.add(rightPointOnCircle);

        stepPolygon = createStepPolygon(streifenCoordinates);
        stepCoordinates = streifenCoordinates;
        setALPHA(streifenCoordinates.get(0), streifenCoordinates.get(1));
        setBETA(streifenCoordinates.get(3), streifenCoordinates.get(2));
        vertex.increaseVisited();

    }

    private Coordinate getExtentCoordinate(Vertex vertex, Coordinate base) {
        Vector2D vector2D = new Vector2D(base, vertex);
        double k = getMaxDistanceFrom() / base.distance(vertex);
        Vector2D extentVector = vector2D.multiply(k);
        double x = extentVector.getX() + base.getX();
        double y = extentVector.getY() + base.getY();

        return new Coordinate(x, y);

    }


    void setALPHA(Coordinate start, Coordinate end) {
        ALPHA = new LineSegment(start, end);
    }

    void setBETA(Coordinate start, Coordinate end) {
        BETA = new LineSegment(start, end);
    }
}
