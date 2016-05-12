package common;

import java.util.LinkedList;

import common.Coord;

public class State {

    private Coord coordinate;
    private LinkedList<String> possibleDirections;

    public State(Coord coordinate) {
        this.coordinate = coordinate;
    }

    public State(Coord coordinate, String... directions) {
        this.coordinate = coordinate;
        possibleDirections = new LinkedList<>();
        for (String s : directions) {
            possibleDirections.add(s);
        }
    }

    public String getNextDirection() {
        return possibleDirections.getFirst();
    }

    public boolean hasNext() {
        return !possibleDirections.isEmpty();
    }

    public int getX() {
        return coordinate.xpos;
    }

    public int getY() {
        return coordinate.ypos;
    }

}
