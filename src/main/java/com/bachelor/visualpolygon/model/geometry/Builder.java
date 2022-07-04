package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.algorithm.PointLocator;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;
import org.locationtech.jts.operation.overlay.validate.FuzzyPointLocator;

import java.util.*;
import java.util.stream.Collectors;


@NoArgsConstructor
@Getter
public class Builder {

    Polygon polygon;
    Polygon stepPolygon;
    public final static GeometryCamera camera = new GeometryCamera();
    List<Vertex> vertices;
    List<Vertex> polarSortedVertices;
    private static final GeometryFactory factory = new GeometryFactory();
    List<Coordinate> stepCoordinates = new ArrayList<>();
    Stack<Line> lineStack = new Stack<>();
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
        Initializer.calculatePolarCoordinates(vertices, camera);
        polarSortedVertices = Initializer.sortPolarCoordinate(vertices);
        nextVertex = polarSortedVertices.stream().max(Comparator.comparing(Vertex::getTheta)).get();
    }


    /**
     * TODO Here sometimes ends in a loop and sometimes calls wrong function for points inside active
     */
    public void createStep(Vertex vertex) {
        System.out.println("[ON builder.createStreife] ::: getting " + vertex + " as parameter");
        createStepFromBETA(vertex);
        setActive();
        //setTemps();

        //resetColors();
        nextVertex = findNextAfterBETA();


        vertex.setInBlue(true);
        if (Orientation.index(BETA.p0, vertex, nextVertex) == Orientation.COLLINEAR) {
            System.out.println("IS COLLINEAR");
        }


    }

    private void resetColors() {
        for (Vertex v : polarSortedVertices) {
            v.setGrey(false);
            v.setInBlue(false);
        }
    }

    public Vertex findNextAfterBETA() {

        if (Objects.isNull(active) || active.size() == 1) {
            return nextVertex;
        }
        active.stream().sorted(Comparator.comparing(Vertex::getTheta));
        System.out.println("====ACTIVe =====");

       /* List<Vertex> leftToBETA = getPolarSortedVertices().stream()
                .filter(vertex -> BETA.orientationIndex(vertex.getCoordinate()) == -1)
                .filter(vertex -> active.contains(vertex))
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());

        System.out.println("======LEFT TO BETAA=====");
        leftToBETA.forEach(System.out::println);*/

        Map<Double, Vertex> mapper = new HashMap<>();
        List<Double> keys = new ArrayList<>();
        for (Vertex v : active) {
            Double angle = Angle.angleBetween(v.getCoordinate(), BETA.p0, BETA.p1);
            mapper.put(angle, v);
            keys.add(angle);
        }

        Double d = Collections.min(keys);
        keys.removeIf(aDouble -> aDouble == d);
        mapper.remove(d);

        Vertex result = mapper.get(Collections.min(keys));


        /**TODO: Was passiert wenn next Vertex in gleiche Linie liegt?*/
        FuzzyPointLocator locator = new FuzzyPointLocator(stepPolygon, 2);
        if (locator.getLocation(result.getCoordinate()) == 1) {
            System.out.println(" NEXT Point on BETA  " + result.getCoordinate());
            Double key = Collections.min(keys);
            keys.removeIf(aDouble -> aDouble == key);
            mapper.remove(key);
            result = mapper.get(Collections.min(keys));
        }

        result.setInGrey(true);
        return result;

    }

    public Vertex findNextAfterALPHA() {

        if (Objects.isNull(active) || active.isEmpty()) {
            return polarSortedVertices.get(0);
        }
        active.stream().sorted(Comparator.comparing(Vertex::getTheta));

        List<Vertex> leftToALPHA = getPolarSortedVertices().stream()
                .filter(vertex -> ALPHA.orientationIndex(vertex.getCoordinate()) == -1)
                .filter(vertex -> !active.contains(vertex))
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());

        Map<Double, Vertex> mapper = new HashMap<>();
        List<Double> keys = new ArrayList<>();
        for (Vertex v : leftToALPHA) {
            Double angle = Angle.angleBetween(v.getCoordinate(), ALPHA.p0, ALPHA.p1);
            mapper.put(angle, v);
            keys.add(angle);
        }

        Double d = Collections.min(keys);

        Vertex result = mapper.get(d);

        result.setInBlue(true);
        return result;
    }


    public boolean isInsideActive(Vertex vertex) {
        int i = Orientation.index(ALPHA.getCoordinate(0), ALPHA.getCoordinate(1), vertex.getCoordinate());
        if (i == Orientation.LEFT) {
            System.out.println("[ON builder.isInsideActive]" + i + "ON THE LEFT");
            return true;
        }
        System.out.println("[ON builder.isInsideActive]" + i + "ON THE RIGHT");

        return false;
    }


    private void setActive() {
        active = new ArrayList<>();
        /**TODO Warum ist Tolerance 2 ?*/
        FuzzyPointLocator pointLocator = new FuzzyPointLocator(stepPolygon, 2);
        for (Vertex vertex : getPolarSortedVertices()) {
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
            LineSegment parallelToBeChecked = getParallelLineForALPHA(vertex.getCoordinate());
            if (parallelToBeChecked.toGeometry(factory).within(polygon)) {
                tempVisible.add(vertex);
                vertex.setIsVisible(1);
                addToGreenLines(parallelToBeChecked);
            } else {
                tempInvisible.add(vertex);
                if (vertex.getIsVisible() != 1) {
                    vertex.setIsVisible(-1);
                }
                addToRedLines(parallelToBeChecked);
            }
        }

    }


    public LineSegment getParallelLineForBETA(Coordinate point) {
        LineSegment lineSegment = new LineSegment(stepPolygon.getCoordinates()[0], stepPolygon.getCoordinates()[1]);
        Coordinate baseMirror = lineSegment.pointAlongOffset(0, -lineSegment.distance(point));
        Coordinate endMirror = lineSegment.pointAlongOffset(1, -lineSegment.distance(point));

        return new LineSegment(baseMirror, point);
    }

    public LineSegment getParallelLineForALPHA(Coordinate point) {
        LineSegment lineSegment = new LineSegment(stepPolygon.getCoordinates()[0], stepPolygon.getCoordinates()[1]);
        Coordinate baseMirror = lineSegment.pointAlongOffset(0, lineSegment.distancePerpendicular(point));
        Coordinate endMirror = lineSegment.pointAlongOffset(1, lineSegment.distancePerpendicular(point));

        return new LineSegment(baseMirror, point);
    }

    public void addToGreenLines(LineSegment greenLine) {
        Line parallelLine = new Line(greenLine.getCoordinate(0).getX(), greenLine.getCoordinate(0).getY(), greenLine.getCoordinate(1).getX(), greenLine.getCoordinate(1).getY());
        parallelLine.setStroke(Color.GREEN);
        parallelLine.setStrokeWidth(1.9);
        lineStack.push(parallelLine);

    }

    public void addToRedLines(LineSegment redLine) {
        Line parallelLine = new Line(redLine.getCoordinate(0).getX(), redLine.getCoordinate(0).getY(), redLine.getCoordinate(1).getX(), redLine.getCoordinate(1).getY());
        parallelLine.setStroke(Color.RED);
        parallelLine.setStrokeWidth(1.9);
        lineStack.push(parallelLine);

    }

    //Should give back the 4 coordinates. Those should be given to form the polygon and
    //to the viewController to render the view. In the view they should not cross the borders of pane
    public void createStepFromBETA(Vertex vertex) {
        CoordinateList streifenCoordinates = new CoordinateList();
        System.out.println("BETA CALLED");

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
        addRadiusLines(BETA);

        System.out.println("BETA TERMINDATED");
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
        addRadiusLines(ALPHA);

        System.out.println("ALPHA TERMINATED");
    }


    public LineSegment getAlphaForVertex(Vertex vertex) {
        Coordinate leftPointOnCircle = camera.getLeftTangentPoint(vertex);
        return new LineSegment(leftPointOnCircle, getExtentCoordinateForALPHA(vertex));
    }

    public void addRadiusLines(LineSegment AB) {
        Line line = new Line(AB.getCoordinate(0).getX(), AB.getCoordinate(0).getY(), AB.getCoordinate(1).getX(), AB.getCoordinate(1).getY());
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1.9);
        lineStack.push(line);
    }


    public Polygon createStepPolygon(CoordinateList vertices) {
        vertices.add(vertices.get(0));
        return factory.createPolygon(vertices.toCoordinateArray());
    }

    private Polygon createGeometryPolygon(List<Vertex> vertices) {
        CoordinateList tempVertices = new CoordinateList();
        for (Vertex vertex : vertices) {
            tempVertices.add(vertex.getCoordinate());
        }
        tempVertices.add(vertices.get(0).getCoordinate());
        return factory.createPolygon(tempVertices.toCoordinateArray());
    }

    private double getMax() {
        double max = 0;
        for (Vertex vertex : polarSortedVertices) {
            if (max < vertex.getCoordinate().distance(camera.getRightTangentPoint(vertex))) {
                max = vertex.getCoordinate().distance(camera.getRightTangentPoint(vertex));
            }
        }
        return max;
    }

    private Coordinate getExtentCoordinateForBETA(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getRightTangentPoint(vertex), vertex.getCoordinate());
        double k = getMax() / (camera.getRightTangentPoint(vertex).distance(vertex.getCoordinate()));
        Vector2D extentVector = vector.multiply(k);
        double x = extentVector.getX() + camera.getRightTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getRightTangentPoint(vertex).getY();
        return new Coordinate(x, y);
    }

    private Coordinate getExtentCoordinateForALPHA(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getLeftTangentPoint(vertex), vertex.getCoordinate());
        double k = getMax() / (camera.getLeftTangentPoint(vertex).distance(vertex.getCoordinate()));
        Vector2D extentVector = vector.multiply(k);
        double x = extentVector.getX() + camera.getLeftTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getLeftTangentPoint(vertex).getY();
        return new Coordinate(x, y);
    }

    private void setALPHA(Coordinate start, Coordinate end) {
        ALPHA = new LineSegment(start, end);
    }

    private void setBETA(Coordinate start, Coordinate end) {
        BETA = new LineSegment(start, end);
    }
}
