package communication;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Terrain;

public class CommunicationServer implements Detector {

    /** List of group that this ROVER is communicating to */
    private List<Group> groupList = new ArrayList<Group>();

    /** This ROVER key information: name, IP, port number */
    private Group group;

    /** Coordinates of all the Science discovered by this ROVER */
    private List<Coord> discoveredSciences = new ArrayList<Coord>();

    /** Communication Module use to receive message from others */
    private Receiver receiver = new RoverReceiver();

    /** Communication Module use to write message to others */
    private Sender sender = new RoverSender();

    public CommunicationServer(Group group) throws IOException {
        this.group = group;
    }

    /** Set the Groups this ROVER will communicate with */
    public void setGroupList(Group... groups) {
        List<Group> newGroupList = new ArrayList<Group>();
        for (Group g : groups)
            newGroupList.add(g);
        groupList = removeSelfFromGroups(newGroupList);
    }

    /** Set the Groups this ROVER will communicate with */
    public void setGroupList(List<Group> groups) {
        groupList = removeSelfFromGroups(groups);
    }

    /** Start receiver server. receive incoming science coordinates from other
     * ROVERS */
    public void startServer() throws IOException {
        receiver.startServer(new ServerSocket(group.getPort()));
    }

    /** ROVER will ignore Science on these terrains during communication */
    public void ignoreTerrain(Terrain... terrains) {
        receiver.ignoreTerrains(terrains);
    }

    /** @return A list of science shared to me from other ROVERS */
    public List<Coord> getShareScience() {
        return receiver.getSharedCoords();
    }

    /** Scan the map for science. Update rover science list. Share the science
     * to all the ROVERS. Display result on console. Also display the list of
     * connected ROVER and all the SCIENCE shared to you that are not filtered
     * 
     * @param map
     *            Result of scanMap.getScanMap(). Use to check for science
     * @param currentLoc
     *            ROVER current location. Use to calculate the science absolute
     *            location
     * @param sightRange
     *            Either 3, if your radius is 7x7, or 5, if your radius is 11x11
     * @throws IOException */
    public void detectAndShare(ScanMap scanMap, Coord currentLOC)
            throws IOException {
        List<Coord> detectedSciences = detectScience(scanMap, currentLOC);
        List<Coord> newSciences = updateDiscoveries(detectedSciences);
        for (Coord c : newSciences) {
            shareScience(groupList, c);
        }
        displayAllDiscoveries();
        displayShareScience();
    }

    @Override
    public List<Coord> detectScience(ScanMap scanMap, Coord roverLOC) {

        /* A list of all the science coordinates found in this map */
        List<Coord> scienceCoords = new ArrayList<Coord>();

        /* Current map of surronding */
        MapTile[][] map = scanMap.getScanMap();

        /* Will return the center index */
        int centerIndex = scanMap.getEdgeSize() / 2;

        /* iterate through every MapTile Object in the 2D Array. If the MapTile
         * contains science, calculate and save the coordinates of the tiles. */
        for (int x = 0; x < map.length; x++) {
            for (int y = 0; y < map[x].length; y++) {

                MapTile mapTile = map[x][y];

                if (Detector.DETECTABLE_SCIENCES.contains(mapTile.getScience())) {

                    /* Use the ROVER current LOC (absoulte) and the scan map LOC
                     * (relative) to calculate the true (absoulte) location of
                     * the SCIENCE Coord. */
                    int tileX = roverLOC.xpos + (x - centerIndex);
                    int tileY = roverLOC.ypos + (y - centerIndex);
                    Coord coord = new Coord(mapTile.getTerrain(), mapTile.getScience(), tileX,
                            tileY);
                    scienceCoords.add(coord);
                }
            }
        }
        return scienceCoords;
    }

    /** Display summary of sciences discovered by this ROVER */
    public void displayAllDiscoveries() {
        System.out.println(group.getName() + " SCIENCE-DISCOVERED-BY-ME: "
                + toProtocolString(discoveredSciences));
        System.out.println(group.getName() + " TOTAL-NUMBER-OF-SCIENCE-DISCOVERED-BY-ME: "
                + discoveredSciences.size());
    }

    /** Display summary of all the science share to me by other ROVERS */
    public void displayShareScience() {
        System.out.println(
                group.getName() + " SCIENCES-SHARED-TO-ME: " + toProtocolString(getShareScience()));
        System.out.println(
                group.getName() + " TOTAL-SCIENCE-SHARED-TO-ME: " + getShareScience().size());
    }

    /** Remove this ROVER group from a List of Group. Used primary to avoid
     * communicating with self */
    private List<Group> removeSelfFromGroups(List<Group> groups) {
        List<Group> groupsWithoutMe = new ArrayList<Group>();
        for (Group g : groups) {
            if (!g.getName().equals(group.getName())) {
                groupsWithoutMe.add(g);
            }
        }
        return groupsWithoutMe;
    }

    /** @param coords
     *            Coord with Science
     * @return A list of Coord.toProtocol(). For example (SOIL CRYSTAL 5 3, ROCK
     *         MINERAL 52 13) */
    private String toProtocolString(List<Coord> coords) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = coords.size() - 1; i >= 0; i--) {
            sb.append(coords.get(i).toProtocol() + " ");
        }
        sb.append("]");
        return sb.toString();
    }

    /** @param detectedSciences
     *            The science that your ROVER found on its scanned map
     * @return A list of Coordinates that are new. Will compare the
     *         detected_sciences list with the ALL the science the ROVER has
     *         discovered. The result , what this method is returning, is the
     *         difference between detected_sciences and all the sciences
     *         discovered so far by the ROVER */
    public List<Coord> updateDiscoveries(List<Coord> detectedSciences) {
        List<Coord> new_sciences = new ArrayList<Coord>();
        for (Coord c : detectedSciences) {
            if (!discoveredSciences.contains(c)) {
                discoveredSciences.add(c);
                new_sciences.add(c);
            }
        }
        return new_sciences;
    }

    /** Share the Science Coordinate to everybody all the Groups */
    public void shareScience(List<Group> groupList, Coord coord) {
        sender.shareScience(groupList, coord);
    }
}
