package com.bachelor.visualpolygon.model.geometry;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.operation.overlay.validate.FuzzyPointLocator;

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
        System.out.println("========================= ON VERTEX ------- " + vertex.getCoordinate());

        if (isInsideActive(vertex)) {
            System.out.println("+++++++WAS INSIDE STEP+++++++++++");
            createStepFromBETA(vertex);

        } else {
            System.out.println("-------WAS OUTSIDE STEP-----------");
            createStepFromALPHA(vertex);
        }

        setActive();
        setTemps();
        testTempInvisible();
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
                .filter(vertex -> ALPHA.orientationIndex(vertex.getCoordinate()) == Orientation.CLOCKWISE)
                .filter(vertex -> !active.contains(vertex))
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());

        for (Vertex v : active) {
            double angle = Angle.angleBetween(v.getCoordinate(), BETA.p0, BETA.p1);
            if (angle < angleToBETA && angle > 0 && angle > EPSILON) {
                angleToBETA = angle;
                tempBETA = v;
            }
        }

        for (Vertex v : leftToALPHA) {
            double angle = Angle.angleBetween(v.getCoordinate(), ALPHA.p0, ALPHA.p1);
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
            if (pointLocator.getLocation(vertex.getCoordinate()) != 2) {
                active.add(vertex);
            }
        }
    }

    //for every point in active build a parallel to the Streifen and check if it intersects with the circle with no interruption
    private void setTemps() {
        tempInvisible = new ArrayList<>();
        tempVisible = new ArrayList<>();
        for (Vertex vertex : active) {
            LineSegment check = new LineSegment(vertex.getCoordinate(), getParallelLine(vertex.getCoordinate()).p1);
            LineSegment parallelToBeChecked = new LineSegment(getParallelLine(vertex.getCoordinate()).p0, vertex.getCoordinate());
            if (parallelToBeChecked.toGeometry(factory).within(polygon)) {
                tempVisible.add(vertex);
                vertex.setIsVisible(1);
                addToGreenLines(parallelToBeChecked);
                getAllPolygonIntersection(vertex);
                //addToBlackLine(check);

            } else {
                tempInvisible.add(vertex);
                if (vertex.getIsVisible() != 1) {
                    vertex.setIsVisible(-1);
                }
                addToRedLines(parallelToBeChecked);
            }
        }
    }

    public void getAllPolygonIntersection(Vertex vertex) {

        LineSegment line = getParallelLine(vertex.getCoordinate());
        Geometry intersection = polygon.getBoundary().intersection(line.toGeometry(factory));
        Coordinate nearest = null;
        double minDistance = 999999;
        for (Coordinate c : intersection.getCoordinates()) {
            if (vertex.getCoordinate().equals(c)) {
                continue;
            }
            if (vertex.getCoordinate().distance(c) < minDistance) {
                minDistance = vertex.getCoordinate().distance(c);
                nearest = c;
            }
        }
        if (nearest == null) {
            System.out.println("Nothing found");
            return;
        }
            System.out.println("FOUND NEAREST " + nearest);
        LineSegment toTest = new LineSegment(vertex.getCoordinate(), nearest);
        addToBlackLine(toTest);

        if (polygon.covers(toTest.toGeometry(factory))) {
            System.out.println("touched || within");
            Vertex v = new Vertex(nearest);
            v.setIsVisible(1);
            vertices.add(v);

        }else {
            System.out.println("ADDED as RED to CHECK ");
            Vertex vv = new Vertex(nearest);
            vv.setIsVisible(-1);
            vertices.add(vv);
            System.out.println("IS WITHIN ? " + toTest.toGeometry(factory).within(polygon));
            System.out.println( "IS TOUCHING ? " + toTest.toGeometry(factory).touches(polygon));
            System.out.println(" IS COVERING ? "+ polygon.covers(toTest.toGeometry(factory)));
        }
    }

    public void testTempInvisible() {
        if (tempInvisible.isEmpty()) {
            return;
        }
        for (int i = 0; i < tempVisible.size(); i++) {
            Vertex visible = tempVisible.get(i);
            for (int j = 0; j < tempInvisible.size(); j++) {
                Vertex invisible = tempInvisible.get(j);
                LineSegment uv = new LineSegment(invisible.getCoordinate(), visible.getCoordinate());
                if (polygon.contains(uv.toGeometry(factory))) {
                    if (isInCollisionWithCamera(uv)) {
                        invisible.setIsVisible(1);
                        addToGreenLines(new LineSegment(invisible.getCoordinate(), getIntersection(uv)));
                        tempVisible.add(invisible);
                        tempInvisible.remove(invisible);
                    }
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
        streifenCoordinates.add(getExtentCoordinateForBETA(vertex));
        LineSegment rightTangent = new LineSegment(rightPointOnCircle, getExtentCoordinateForBETA(vertex));

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
        streifenCoordinates.add(getExtentCoordinateForALPHA(vertex));
        LineSegment leftTangent = new LineSegment(leftPointOnCircle, getExtentCoordinateForALPHA(vertex));

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


    private Coordinate getExtentCoordinateForBETA(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getRightTangentPoint(vertex), vertex.getCoordinate());
        double k = getMaxDistanceFrom() / (camera.getRightTangentPoint(vertex).distance(vertex.getCoordinate()));
        Vector2D extentVector = vector.multiply(k);
        double x = extentVector.getX() + camera.getRightTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getRightTangentPoint(vertex).getY();
        return new Coordinate(x, y);
    }

    private Coordinate getExtentCoordinateForALPHA(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getLeftTangentPoint(vertex), vertex.getCoordinate());
        double k = getMaxDistanceFrom() / (camera.getLeftTangentPoint(vertex).distance(vertex.getCoordinate()));
        Vector2D extentVector = vector.multiply(k);
        double x = extentVector.getX() + camera.getLeftTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getLeftTangentPoint(vertex).getY();
        return new Coordinate(x, y);
    }

    void setALPHA(Coordinate start, Coordinate end) {
        ALPHA = new LineSegment(start, end);
    }

    void setBETA(Coordinate start, Coordinate end) {
        BETA = new LineSegment(start, end);
    }
}
