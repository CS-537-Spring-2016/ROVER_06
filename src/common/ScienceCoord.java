package common;

import enums.Science;
import enums.Terrain;

/**
 * A more detailed Coor class, contain the terrain and the science on the
 * coodrinate
 * 
 * @author shay
 *
 */
public class ScienceCoord extends Coord {

    String terrain;
    String science;

    // takes in a enum but will conver it to a string
    public ScienceCoord(Terrain terrain, Science science, int x, int y) {
        super(x, y);
        this.terrain = terrain.name();
        this.science = science.name();
    }

    // for convience, when sending out data to other rovers of location of
    // sicnece, can call the toString method instead coding it thorugh a loop or
    // whatever
    @Override
    public String toString() {
        return terrain + " " + science + " " + xpos + " " + ypos;
    }

}
