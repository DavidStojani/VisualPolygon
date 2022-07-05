package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Orientation;
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
    private final double EPSILON = 0.0000005;
    private Map<Double, Vertex> bigMap = new HashMap<>();


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

    }


    /**
     * TODO implement both for ALPHA and BETA
     */
    public void createStep(Vertex vertex) {
        bigMap.clear();
        if (Objects.isNull(vertex)) {
            vertex = polarSortedVertices.stream().max(Comparator.comparing(Vertex::getTheta)).get();
        }
        System.out.println("========================= ON VERTEX ------- " + vertex.getCoordinate());

        if (isInsideActive(vertex)) {
            System.out.println("+++++++IS INSIDE+++++++++++");
            vertex.setInGrey(true);
            createStepFromBETA(vertex);
        } else {
            System.out.println("-------IS OUTSIDE-----------");
            vertex.setInBlue(true);
            createStepFromALPHA(vertex);
        }
        setActive();
        nextVertex = findNextAfterALPHA();
        /*if (Objects.isNull(vertex)) {
            vertex = polarSortedVertices.stream().max(Comparator.comparing(Vertex::getTheta)).get();
            createStepFromALPHA(vertex);
            setActive();
            Vertex temp1 = findNextAfterBETA();
            Vertex temp2 = findNextAfterALPHA();
            System.out.println("=====MAPPER FOR BIG FIRST=====   --size " + bigMap.size());
            bigMap.forEach(((aDouble, vertex2) -> System.out.println("[ " + aDouble + "]" + "[ " + vertex2.getCoordinate() + " ]")));
            if (Collections.min(bigMap.keySet()) != 0) {
                nextVertex = bigMap.get(Collections.min(bigMap.keySet()));
                nextVertex.setInBlue(true);
                return;
            }
            nextVertex = bigMap.get(Collections.max(bigMap.keySet()));
            nextVertex.setInBlue(true);
            return;
        }

        if (isInsideActive(vertex)) {
            createStepFromBETA(vertex);
            setActive();
            Vertex temp1 = findNextAfterBETA();
            Vertex temp2 = findNextAfterALPHA();
            System.out.println("=====MAPPER FOR BIG createBETA=====   --size " + bigMap.size());
            bigMap.forEach(((aDouble, vertex2) -> System.out.println("[ " + aDouble + "]" + "[ " + vertex2.getCoordinate() + " ]")));
            if (Collections.min(bigMap.keySet()) != 0) {
                nextVertex = bigMap.get(Collections.min(bigMap.keySet()));
                nextVertex.setInBlue(true);
                return;
            }
            nextVertex = bigMap.get(Collections.max(bigMap.keySet()));
            nextVertex.setInBlue(true);
            return;

        }

        createStepFromALPHA(vertex);
        setActive();
        Vertex temp1 = findNextAfterBETA();
        Vertex temp2 = findNextAfterALPHA();
        System.out.println("=====MAPPER FOR BIG createALPHA=====   --size " + bigMap.size());
        bigMap.forEach(((aDouble, vertex2) -> System.out.println("[ " + aDouble + "]" + "[ " + vertex2.getCoordinate() + " ]")));
        if (Collections.min(bigMap.keySet()) != 0) {
            nextVertex = bigMap.get(Collections.min(bigMap.keySet()));
            nextVertex.setInBlue(true);
            return;
        }
        nextVertex = bigMap.get(Collections.max(bigMap.keySet()));
        nextVertex.setInBlue(true);
        return;
*/

/*
        System.out.println("[ON builder.createStreife]PARAMETER ::: " + vertex.getCoordinate());

        //setTemps();

        //resetColors();
        nextVertex = findNextAfterBETA();
        Vertex temp = findNextAfterALPHA();

        System.out.println(" BIG MAP ");
        bigMap.forEach(((aDouble, vertex1) -> System.out.println("[ " + aDouble + "]" + "[ " + vertex1.getCoordinate() + " ]")));

        vertex.setInBlue(true);
*/

    }

    private void resetColors() {
        for (Vertex v : polarSortedVertices) {
            v.setGrey(false);
            v.setInBlue(false);
        }
    }


    public Vertex findNextAfterBETA() {
        double minAgle = 999999;
        Vertex tempBETA = null;
        if (active.size() == 1) {
            System.out.println("ONLY ONE ON ACTIVE");
            return active.get(0);
        }

        FuzzyPointLocator locator = new FuzzyPointLocator(stepPolygon, 1.1);

        for (Vertex v : active) {
            System.out.println("ANGLE ---- " + Angle.angleBetween(v.getCoordinate(), BETA.p0, BETA.p1) + "  VERTEX----- " + v.getCoordinate());
            v.setAngleToBETA(Angle.angleBetween(v.getCoordinate(), BETA.p0, BETA.p1));
            if (Angle.angleBetween(v.getCoordinate(), BETA.p0, BETA.p1) < minAgle) {
                if (locator.getLocation(v.getCoordinate()) != Location.BOUNDARY) {
                    minAgle = Angle.angleBetween(v.getCoordinate(), BETA.p0, BETA.p1);
                    tempBETA = v;
                }
            }
        }

        System.out.println("RESULT OF FINDBETA ---" + tempBETA.getCoordinate());

        return tempBETA;
    }

    public Vertex findNextAfterALPHA() {

        List<Vertex> leftToALPHA = getPolarSortedVertices().stream()
                .filter(vertex -> ALPHA.orientationIndex(vertex.getCoordinate()) == -1)
                .filter(vertex -> !active.contains(vertex))
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());


        double minAgle = 999999;
        Vertex tempALFA = null;
        for (Vertex v : leftToALPHA) {
            if (Angle.angleBetween(v.getCoordinate(), ALPHA.p0, ALPHA.p1) < minAgle) {
                minAgle = Angle.angleBetween(v.getCoordinate(), ALPHA.p0, ALPHA.p1);
                tempALFA = v;
            }
        }

        Vertex fromBeta = findNextAfterBETA();

        if (Angle.angleBetween(fromBeta.getCoordinate(), BETA.p0, BETA.p1) < Angle.angleBetween(tempALFA.getCoordinate(), ALPHA.p0, ALPHA.p1)) {
            if (Angle.angleBetween(fromBeta.getCoordinate(), BETA.p0, BETA.p1) != 0) {
                if (Angle.angleBetween(fromBeta.getCoordinate(), BETA.p0, BETA.p1) > EPSILON) {
                    System.out.println("BETA  SMALLER THAN APLHA ::: BETA is" + Angle.angleBetween(fromBeta.getCoordinate(), BETA.p0, BETA.p1));
                    System.out.println("ALFA IS   " + Angle.angleBetween(tempALFA.getCoordinate(), ALPHA.p0, ALPHA.p1));
                    return fromBeta;
                }
            }
        }


        return tempALFA;
    }


    public boolean isInsideActive(Vertex vertex) {
       /* int i = Orientation.index(ALPHA.getCoordinate(0), ALPHA.getCoordinate(1), vertex.getCoordinate());
        if (i == Orientation.LEFT) {
            System.out.println("[ON builder.isInsideActive]" + i + "ON THE LEFT");
            return true;
        }
        System.out.println("[ON builder.isInsideActive]" + i + "ON THE RIGHT");

        return false;*/
        if (Objects.isNull(active)) {
            return false;
        }
        if (active.contains(vertex)) {
            return true;
        }
        return false;
    }


    private void setActive() {
        active = new ArrayList<>();
        FuzzyPointLocator pointLocator = new FuzzyPointLocator(stepPolygon, 1.1);
        for (Vertex vertex : getPolarSortedVertices()) {
            if (pointLocator.getLocation(vertex.getCoordinate()) != 2) {
                active.add(vertex);
            }
        }
        active.stream().sorted(Comparator.comparing(Vertex::getTheta));
        System.out.println("===ACTIVE CREATED AND SORTED--- SIZE-- ");
        active.forEach(System.out::println);
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
        addRadiusLines(BETA, vertex);

        System.out.println("BETA STEP TERMINDATED");
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
        addRadiusLines(ALPHA, vertex);

        System.out.println("ALPHA TERMINATED");
    }


    public void addRadiusLines(LineSegment AB, Vertex vertex) {
        Line line = new Line(AB.getCoordinate(0).getX(), AB.getCoordinate(0).getY(), vertex.getXCoordinate(), vertex.getYCoordinate());
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
