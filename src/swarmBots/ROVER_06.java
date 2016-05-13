package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import common.State;
import common.Tracker;
import communication.CommunicationServer;
import communication.Group;
import enums.Science;
import enums.Terrain;

/** The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples */

public class ROVER_06 {

    BufferedReader in;
    PrintWriter out;
    String rovername;
    ScanMap scanMap;
    String SERVER_ADDRESS;
    static final int PORT_ADDRESS = 9537;

    /* swarm server constants */
    final String CURRENT_LOC = "LOC";
    final String TARGET_LOC = "TARGET_LOC";
    final int SLEEP_TIME = 500;
    final int CENTER_INDEX = 5;

    CommunicationServer communicationServer;
    Set<Coord> discoveredScience = new HashSet<Coord>();

    /* movement */
    Tracker roverTracker;
    private Coord startCoord;
    private Coord targetCoord;

    boolean beginNewMission = false;

    public ROVER_06(String serverAddress) {
        System.out.println("ROVER_06 rover object constructed");
        rovername = "ROVER_06";
        SERVER_ADDRESS = serverAddress;

        // keep track of rover current and target location
        roverTracker = new Tracker();

        // server with all the ROVER INFO from blue corp
        communicationServer = new CommunicationServer(Group.BLUE_CORP());
    }

    /** Connects to the server then enters the processing loop. */
    private void run() throws IOException, InterruptedException {

        // Make connection and initialize streams
        Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        /* starts the communication server. will try to connect to all the other
         * ROVERS on a separate thread. */
        new Thread(communicationServer).start();

        // This sets the name of this instance of a swarmBot for
        // identifying thread to the server
        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {
                out.println(rovername);
                break;
            }
        }

        // **** get equipment listing ****
        System.out.println("ROVER_06 equipment list results " + getEquipment() + "\n");

        /* request current and target location */
        startCoord = requestCoordFromServer(CURRENT_LOC);
        targetCoord = requestCoordFromServer(TARGET_LOC);

        /* initialize current location */
        roverTracker.setCurrentLocation(startCoord);

        /* move the rover towards its destination */
        doScan();
        // startMission(startCoord);
        // startMission(targetCoord);

        /* Test */
        startMission(targetCoord);
        startMission(startCoord);
        while (true) {
            startMission(generateRandomCoord());
        }

    }

    // ################ Support Methods ###########################

    /** clear in.readLine() */
    private void clearReadLineBuffer() throws IOException {
        while (in.ready()) {
            in.readLine();
        }
    }

    // method to retrieve a list of the rover's equipment from the server
    private ArrayList<String> getEquipment() throws IOException {
        // System.out.println("ROVER_06 method getEquipment()");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        out.println("EQUIPMENT");

        String jsonEqListIn = in.readLine(); // grabs the string that was
                                             // returned first
        if (jsonEqListIn == null) {
            jsonEqListIn = "";
        }
        StringBuilder jsonEqList = new StringBuilder();
        // System.out.println("ROVER_06 incomming EQUIPMENT result - first
        // readline: " + jsonEqListIn);

        if (jsonEqListIn.startsWith("EQUIPMENT")) {
            while (!(jsonEqListIn = in.readLine()).equals("EQUIPMENT_END")) {
                if (jsonEqListIn == null) {
                    break;
                }
                // System.out.println("ROVER_06 incomming EQUIPMENT result: " +
                // jsonEqListIn);
                jsonEqList.append(jsonEqListIn);
                jsonEqList.append("\n");
                // System.out.println("ROVER_06 doScan() bottom of while");
            }
        } else {
            // in case the server call gives unexpected results
            clearReadLineBuffer();
            return null; // server response did not start with "EQUIPMENT"
        }

        String jsonEqListString = jsonEqList.toString();
        ArrayList<String> returnList;
        returnList = gson.fromJson(jsonEqListString, new TypeToken<ArrayList<String>>() {
        }.getType());
        // System.out.println("ROVER_06 returnList " + returnList);

        return returnList;
    }

    // sends a SCAN request to the server and puts the result in the scanMap
    // array
    public void doScan() throws IOException {
        // System.out.println("ROVER_06 method doScan()");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        out.println("SCAN");

        String jsonScanMapIn = in.readLine(); // grabs the string that was
                                              // returned first
        if (jsonScanMapIn == null) {
            System.out.println("ROVER_06 check connection to server");
            jsonScanMapIn = "";
        }
        StringBuilder jsonScanMap = new StringBuilder();
        System.out.println("ROVER_06 incomming SCAN result - first readline: " + jsonScanMapIn);

        if (jsonScanMapIn.startsWith("SCAN")) {
            while (!(jsonScanMapIn = in.readLine()).equals("SCAN_END")) {
                // System.out.println("ROVER_06 incomming SCAN result: " +
                // jsonScanMapIn);
                jsonScanMap.append(jsonScanMapIn);
                jsonScanMap.append("\n");
                // System.out.println("ROVER_06 doScan() bottom of while");
            }
        } else {
            // in case the server call gives unexpected results
            clearReadLineBuffer();
            return; // server response did not start with "SCAN"
        }
        // System.out.println("ROVER_06 finished scan while");

        String jsonScanMapString = jsonScanMap.toString();
        // debug print json object to a file
        // new MyWriter( jsonScanMapString, 0); //gives a strange result -
        // prints the \n instead of newline character in the file

        // System.out.println("ROVER_06 convert from json back to ScanMap
        // class");
        // convert from the json string back to a ScanMap object
        scanMap = gson.fromJson(jsonScanMapString, ScanMap.class);
    }

    // this takes the LOC response string, parses out the x and x values and
    // returns a Coord object
    public static Coord extractLOC(String sStr) {
        String[] coordinates = sStr.split(" ");
        return new Coord(Integer.parseInt(coordinates[1]), Integer.parseInt(coordinates[2]));
    }

    /** iterate through a scan map to find a tile with radiation. get the
     * adjusted (absolute) coordinate of the tile and added into a hash set */
    private void detectRadioactive(Tracker tracker, MapTile[][] scanMapTiles) {
        for (int x = 0; x < scanMapTiles.length; x++) {
            for (int y = 0; y < scanMapTiles[x].length; y++) {
                MapTile mapTile = scanMapTiles[x][y];
                if (mapTile.getScience() == Science.RADIOACTIVE) {
                    int tileX = tracker.getCurrentLocation().xpos + (x - 5);
                    int tileY = tracker.getCurrentLocation().ypos + (y - 5);
                    Coord coord = new Coord(mapTile.getTerrain(), mapTile.getScience(), tileX, tileY);

                    if (!discoveredScience.contains(coord)) {
                        discoveredScience.add(coord);
                        communicationServer.writeToRovers(coord.toProtocol());
                    }
                }
            }
        }
    }

    private void startMission(Coord destination) throws IOException, InterruptedException {

        roverTracker.initDestination(destination);

        while (!roverTracker.hasArrived()) {

            switch (resolveDirection()) {
            case "E":
                System.out.println("HEADED EAST");
                accelerate(1, 0);
                break;
            case "W":
                System.out.println("HEADED WEST");
                accelerate(-1, 0);
                break;
            case "S":
                System.out.println("HEADED SOUTH");
                accelerate(0, 1);
                break;
            case "N":
                System.out.println("HEADED NORTH");
                accelerate(0, -1);
                break;
            }
        }
    }

    /** This method is used to decide what direction the rover will go next
     * Priority: EAST, WEST, SOUTH, NORTH */
    private String resolveDirection() {
        if (roverTracker.getDistanceTracker().xpos > 0)
            return "E";
        if (roverTracker.getDistanceTracker().xpos < 0)
            return "W";
        if (roverTracker.getDistanceTracker().ypos > 0)
            return "S";
        if (roverTracker.getDistanceTracker().ypos < 0)
            return "N";
        return null;
    }

    private void accelerate(int xVelocity, int yVelocity) throws IOException, InterruptedException {

        String direction = (xVelocity == 1) ? "E"
                : (xVelocity == -1) ? "W" : (yVelocity == 1) ? "S" : (yVelocity == -1) ? "N" : null;
        while ((xVelocity != 0) ? roverTracker.getDistanceTracker().xpos != 0
                : roverTracker.getDistanceTracker().ypos != 0) {

            detectRadioactive(roverTracker, scanMap.getScanMap());
            displayDiscoveredScience();

            Coord destination = roverTracker.getDestination();
            Coord current = roverTracker.getCurrentLocation();

            if (isInDestinationRange(roverTracker.getDistanceTracker()) && isDestinationBlocked(current, destination)) {
                System.out.println("Destination is blocked!");

                roverTracker.setArrived(true);
                return;
            }

            if (!blocked(xVelocity, yVelocity)) {
                move(direction);
            } else {
                roverTracker.addMarker(new State(new Coord(roverTracker.getCurrentLocation().xpos + xVelocity,
                        roverTracker.getCurrentLocation().ypos + yVelocity)));
                roverTracker.setLastSuccessfulMove(roverTracker.getCurrentLocation());
                goAround(direction);
            }
            getLocation();
        }
    }

    /** display a list of all the discovered science. list will follow the
     * protocol: TERRAIN SCIENCE X Y */
    private void displayDiscoveredScience() {
        StringBuilder science = new StringBuilder("DISCOVERED SCIENCE: [");
        for (Coord c : discoveredScience) {
            science.append(c.toProtocol() + " ");
        }
        science.append("]");
        System.out.println(science.toString());
    }

    private void goAround(String direction) throws InterruptedException, IOException {

        String previousDirection = "";
        String direction1 = previousDirection;
        while ((roverTracker.getCurrentLocation().ypos > roverTracker.peekMarker().getY() && direction.equals("N"))
                || (roverTracker.getCurrentLocation().xpos > roverTracker.peekMarker().getX() && direction.equals("W"))
                || (roverTracker.getCurrentLocation().ypos < roverTracker.peekMarker().getY() && direction.equals("S"))
                || (roverTracker.getCurrentLocation().xpos < roverTracker.peekMarker().getX()
                        && direction.equals("E"))) {
            getLocation();
            int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
            direction1 = previousDirection;

            detectRadioactive(roverTracker, scanMap.getScanMap());
            displayDiscoveredScience();

            if ((!blocked(0, 1) && (blocked(1, -1, centerIndex, centerIndex + 1) || blocked(1, 1)))
                    && !previousDirection.equals("N")) {
                move("S");
                previousDirection = "S";
                continue;
            }

            if ((!blocked(0, -1) && (blocked(-1, 1, centerIndex, centerIndex - 1) || blocked(-1, -1)))
                    && !previousDirection.equals("S")) {
                move("N");
                previousDirection = "N";
                continue;
            }

            if ((!blocked(-1, 0) && (blocked(1, 1, centerIndex - 1, centerIndex) || blocked(-1, 1)))
                    && !previousDirection.equals("E")) {
                move("W");
                previousDirection = "W";
                continue;
            }

            if ((!blocked(1, 0) && (blocked(-1, -1, centerIndex + 1, centerIndex) || blocked(1, -1)))
                    && !previousDirection.equals("W")) {
                move("E");
                previousDirection = "E";
                continue;
            }

            if (direction1.equals(previousDirection)) {
                previousDirection = "";
            }
        }
    }

    private void move(String direction) throws IOException, InterruptedException {
        Coord previousLocation = roverTracker.getCurrentLocation();
        out.println("MOVE " + direction);
        getLocation();
        if (!previousLocation.equals(roverTracker.getCurrentLocation())) {
            switch (direction.charAt(0)) {
            case ('N'):
                roverTracker.updateYPos(1);
                break;
            case ('W'):
                roverTracker.updateXPos(1);
                break;
            case ('S'):
                roverTracker.updateYPos(-1);
                break;
            case ('E'):
                roverTracker.updateXPos(-1);
                break;
            }
            System.out.println("Distance Left = " + roverTracker.getDistanceTracker().xpos + ","
                    + roverTracker.getDistanceTracker().ypos);
            System.out.println("Current LOC: " + roverTracker.getCurrentLocation());
            System.out.println("Destination LOC: " + roverTracker.getDestination());
            System.out.println("--------------------------------");
        }
        Thread.sleep(SLEEP_TIME);
    }

    private boolean blocked(int xOffset, int yOffset) {
        MapTile[][] map = scanMap.getScanMap();
        int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
        return map[centerIndex + xOffset][centerIndex + yOffset].getHasRover()
                || map[centerIndex + xOffset][centerIndex + yOffset].getTerrain() == Terrain.ROCK
                || map[centerIndex + xOffset][centerIndex + yOffset].getTerrain() == Terrain.NONE
                || map[centerIndex + xOffset][centerIndex + yOffset].getTerrain() == Terrain.SAND;
    }

    private boolean blocked(int xOffset, int yOffset, int roverX, int roverY) {
        MapTile[][] map = scanMap.getScanMap();
        return map[roverX + xOffset][roverY + yOffset].getHasRover()
                || map[roverX + xOffset][roverY + yOffset].getTerrain() == Terrain.ROCK
                || map[roverX + xOffset][roverY + yOffset].getTerrain() == Terrain.NONE
                || map[roverX + xOffset][roverY + yOffset].getTerrain() == Terrain.SAND;
    }

    /** @param mapTile
     * @return true if the tile is "blocked" or not pass-able: SAND, ROCK, NONE,
     *         or has a ROVER */
    private boolean blocked(MapTile mapTile) {
        return mapTile.getHasRover() || mapTile.getTerrain() == Terrain.SAND || mapTile.getTerrain() == Terrain.ROCK
                || mapTile.getTerrain() == Terrain.NONE;
    }

    /* Returns coordinate object that represents rover's current location */
    private void getLocation() throws IOException, InterruptedException {
        Coord previousLocation = roverTracker.getCurrentLocation();
        out.println("LOC");
        String results = in.readLine();
        if (results == null) {
            System.out.println(rovername + " check connection to server");
            results = "";
        }
        if (results.startsWith("LOC"))
            roverTracker.setCurrentLocation(extractLOC(results));
        if (!roverTracker.getCurrentLocation().equals(previousLocation)) {
            this.doScan();
            scanMap.debugPrintMap();
        }
    }

    /** @param current
     *            ROVER's current location
     * @param destination
     *            ROVER's destination location
     * @return true if the destination location is blocked, i.e. (location is on
     *         a SAND, ROCK, NONE) tile */
    private boolean isDestinationBlocked(Coord current, Coord destination) {

        MapTile[][] map = scanMap.getScanMap();

        for (int x = -5; x < 6; x++) {
            for (int y = -5; y < 6; y++) {
                int xAdjusted = current.xpos + x;
                int yAdjusted = current.ypos + y;

                if (blocked(map[x + 5][y + 5]) && xAdjusted == destination.xpos && yAdjusted == destination.ypos) {
                    return true;
                }
            }
        }
        return false;
    }

    /** @param distanceTracker
     * @return true if the distance is within "range" of the ROVER. The rover
     *         can see in a 11x11 range */
    private boolean isInDestinationRange(Coord distanceTracker) {

        /* 5 because the rover can only see 5 tiles NORTH, WEST, SOUTH, EAST.
         * The rover is in the middle of the 11x11. */
        boolean xInRange = distanceTracker.xpos <= 5;
        boolean yInRange = distanceTracker.ypos <= 5;
        return xInRange && yInRange;
    }

    /** @param request
     *            The type of Coordinate you want from the server (LOC,
     *            TARGET_LOC)
     * @return The Swarm Server's response to your command
     * @throws IOException */
    private Coord requestCoordFromServer(String request) throws IOException {
        String response;
        do {
            out.println(request);
            response = in.readLine();
        } while (!response.startsWith(request));
        return extractLOC(response);

    }

    /** @return a coordinate in which the XPOS and YPOS is somewhere between the
     *         starting and target XPOS and YPOS */
    private Coord generateRandomCoord() {
        int xLow = startCoord.xpos;
        int xHigh = targetCoord.xpos;
        int yLow = startCoord.ypos;
        int yHigh = targetCoord.ypos;

        Random r = new Random();

        int x = r.nextInt(xHigh - xLow) + xLow;
        int y = r.nextInt(yHigh - yLow) + yLow;

        return new Coord(x, y);
    }

    /** Runs the client */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            new ROVER_06("localhost").run();
        } else {
            new ROVER_06(args[0]).run();
        }
    }

}
