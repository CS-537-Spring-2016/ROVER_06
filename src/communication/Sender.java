package communication;

import java.io.IOException;
import java.util.List;

import common.Coord;

/** Used to send messages to other ROVERS
 * 
 * @author Shay */
public interface Sender {

    /** @param groups
     *            A list of all the groups (ROVER INFO). Will used this to write
     *            a message to each individual ROVER's socket
     * @param coord
     *            The coordinate that you want to share to other ROVERS
     * @throws IOException */
    void shareScience(List<Group> groupList, Coord coord);
}
