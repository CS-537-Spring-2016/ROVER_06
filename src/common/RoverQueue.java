package common;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import common.Coord;

public class RoverQueue {

    private ArrayList<Point2D> positionList;
    private Double xPosition;
    private Double yPosition;
    private Point2D closestPoint;

    public RoverQueue() {
        closestPoint = new Point2D.Double();
        positionList = new ArrayList<Point2D>();
        // These are hard coded cordinates but will not be there for final
        // implementation..will be receiving these from other rovers or server
        // target location

        // addLocation("LOC 27 7");
        // addLocation("LOC 34 4");
        // addLocation("LOC 41 4");
        // addLocation("LOC 53 2");
        // addLocation("LOC 71 4");
        // addLocation("LOC 69 13");
        // addLocation("LOC 73 17");
        // addLocation("LOC 90 16");
    }

    public void addLocation(Coord location) {
        xPosition = (double) location.xpos;
        yPosition = (double) location.ypos;

        Point2D point = new Point2D.Double(xPosition, yPosition);
        positionList.add(point);
    }

    // finds the closest location from the current location
    public Coord closestTargetLocation(Coord currentLocation) {
        Double currentXPosition = (double) currentLocation.xpos;
        Double currentYPosition = (double) currentLocation.ypos;
        ;
        Point2D point = new Point2D.Double(currentXPosition, currentYPosition);

        ArrayList<Double> closestArray = new ArrayList<Double>();
        ArrayList<Point2D> PointArray = new ArrayList<Point2D>();
        Double closest = 10000.0;

        for (Point2D pt : positionList) {
            System.out.println("The distance from :" + point + " to: " + pt
                    + " is: " + point.distance(pt));
            closestArray.add(point.distance(pt));
            PointArray.add(pt);
        }

        for (int j = 0; j < closestArray.size(); j++) // finds the shortest
                                                      // distance
        {
            if (closestArray != null)
                if (closestArray.get(j) < closest) {
                    closest = closestArray.get(j);
                    closestPoint = PointArray.get(j);
                }
        }
        System.out.println("The closest distance is:" + closest
                + " which is at: " + closestPoint);
        // if the x and y coordinates are required to be extracted to Integer
        int x = (int) closestPoint.getX();
        int y = (int) closestPoint.getY();
        // String [] closestLocation = closestPoint.toString().split(" ");

        return new Coord(x, y);
    }

    public boolean isEmpty() {
        return positionList.isEmpty();
    }

    // need to get correct index
    public void removeCompletedJob() {
        positionList.remove(closestPoint);
    }

    public ArrayList<Point2D> getPositionList() {
        return positionList;
    }

    public void setPositionList(ArrayList<Point2D> positionList) {
        this.positionList = positionList;
    }

    public void displayLocation() {
        for (Point2D str : positionList) {
            System.out.println("Location from Rover:" + str);
        }
    }

}
