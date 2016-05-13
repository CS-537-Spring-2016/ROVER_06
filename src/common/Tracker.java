package common;

import java.util.Stack;

import common.Coord;

public class Tracker {

    // Will have location coordinate and direction rover went in a string
    private Coord currentLocation;
    private Stack<State> markers;
    private Coord returnLocation;
    private Coord targetLocation;

    private Coord startingPoint;
    private Coord destination;

    private Coord lastSuccessfulMove;
    // Keeps track of how may tiles are left to go
    private Coord distanceTracker;

    public Tracker() {
        distanceTracker = new Coord(0, 0);
        currentLocation = new Coord(0, 0);
        markers = new Stack<State>();
    }
    

    public void setCurrentLocation(Coord currentLocation) {
        this.currentLocation = currentLocation;
    }

    public Coord getCurrentLocation() {
        return currentLocation;
    }

    // It just peeks the last point where rover got stuck
    public State peekMarker() {
        return markers.peek();
    }

    public void updateDistanceTracker() {
        updateXPos(lastSuccessfulMove.xpos - currentLocation.xpos);
        updateYPos(lastSuccessfulMove.ypos - currentLocation.ypos);
    }

    public State removeMarker() {
        return markers.pop();
    }

    public void addMarker(State marker) {
        markers.add(marker);
    }

    public Coord getReturnLocation() {
        return returnLocation;
    }

    public void setReturnLocation(Coord returnLocation) {
        this.returnLocation = returnLocation;
    }

    public Coord getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Coord targetLocation) {
        this.targetLocation = targetLocation;
    }

    public Coord getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(Coord startingPoint) {
        this.startingPoint = startingPoint;
    }

    public Coord getDestination() {
        return destination;
    }

    public void setDestination(Coord destination) {
        this.destination = destination;
    }

    public Coord getDistanceTracker() {
        return distanceTracker;
    }

    public void setDistanceTracker() {
        distanceTracker.xpos = destination.xpos - startingPoint.xpos;
        distanceTracker.ypos = destination.ypos - startingPoint.ypos;
    }

    public void updateXPos(int x) {
        distanceTracker.xpos += x;
    }

    public void updateYPos(int y) {
        distanceTracker.ypos += y;
    }

    public boolean hasArrived() {
        return distanceTracker.xpos == 0 && distanceTracker.ypos == 0;
    }
    
    public void setArrived(boolean arrived) {
        if (arrived) {
            distanceTracker.xpos = 0;
            distanceTracker.ypos = 0;
            destination = currentLocation;
        }
    }

    public void setLastSuccessfulMove(Coord location) {
        lastSuccessfulMove = location;
    }

    public boolean atTargetLocation(Coord location) {
        return location.xpos == targetLocation.xpos
                && location.ypos == targetLocation.ypos;
    }

    /** set Tracker's starting point and destination. calculate the distance
     * to destination */
    public void initDestination(Coord destination) {

        startingPoint = currentLocation;
        this.destination = destination;
        setDistanceTracker();

        System.out.println("********** SUMMARY **********");
        System.out.println("CURRENT LOCATION: " + currentLocation);
        System.out.println("STARTING POINT: " + startingPoint);
        System.out.println("DESTINATION: " + this.destination);
        System.out.println("******************************");
    }
}
