package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import common.State;
import common.Tracker;
import communication.BlueCorp;
import communication.CommunicationServer;
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
    final int SLEEP_TIME = 700;
    final int CENTER_INDEX = 5;

    /* communication module */
    CommunicationServer communicationServer;

    /* movement */
    Tracker roverTracker;

    /* coordinates */
    private Coord startCoord = new Coord(9, 9);
    private Coord targetCoord;

    public ROVER_06(String serverAddress) {
        System.out.println("ROVER_06 rover object constructed");
        rovername = "ROVER_06";
        SERVER_ADDRESS = serverAddress;

        // keep track of rover current and target location
        roverTracker = new Tracker();
    }

    /** Connects to the server then enters the processing loop. */
    private void run() throws IOException, InterruptedException {

        // Make connection and initialize streams
        Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // ********* SET UP COMMUNICATION MODULE *********

        /* Setup communication Server */
        communicationServer = new CommunicationServer(BlueCorp.GROUP_06);
        communicationServer.setGroupList(BlueCorp.COMMAND_CENTER, BlueCorp.GROUP_07);

        // ****************************************************************

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
        targetCoord = requestCoordFromServer(TARGET_LOC);

        /* initialize current location */
        roverTracker.setCurrentLocation(startCoord);

        /* move the rover towards its destination */
        doScan();
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

    /** This is how the ROVER moves. A lot of credit to GROUP 3. The majority of
     * these code came from them. Our code are similar but they implement a
     * method to head towards a certain location, while previously our ROVER was
     * mindlessly moving around obstacle. I've made some additional changes and
     * Improvements */
    private void startMission(Coord destination) throws IOException, InterruptedException {

        /* set up rover tracker before it start its mission */
        roverTracker.initDestination(destination);

        /* initial movement */
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

        String direction = calculateDirection(xVelocity, yVelocity);

        while ((xVelocity != 0) ? roverTracker.getDistanceTracker().xpos != 0
                : roverTracker.getDistanceTracker().ypos != 0) {

            Coord destination = roverTracker.getDestination();
            Coord current = roverTracker.getCurrentLocation();

            /* if the rove arrives "sees" it destination and the map tile is
             * blocked, then it will end the mission */
            if (isInDestinationRange(roverTracker.getDistanceTracker())
                    && isDestinationBlocked(current, destination)) {
                System.out.println("Destination is blocked!");

                roverTracker.setArrived(true);
                return;
            }

            if (!blocked(xVelocity, yVelocity)) {
                move(direction);
            } else {
                roverTracker.addMarker(
                        new State(new Coord(roverTracker.getCurrentLocation().xpos + xVelocity,
                                roverTracker.getCurrentLocation().ypos + yVelocity)));
                roverTracker.setLastSuccessfulMove(roverTracker.getCurrentLocation());
                goAround(direction);
            }
        }
    }

    /** Calculate direction. Priority: E, W, S, N. This mean the rover will move
     * left-right first. When it can't, it will move up-down */
    private String calculateDirection(int xVelocity, int yVelocity) {
        if (xVelocity == 1)
            return "E";
        else if (xVelocity == -1)
            return "W";
        else if (yVelocity == 1)
            return "S";
        else if (yVelocity == -1)
            return "N";
        else
            return null;
    }

    /** attempt to move around a "blocked" map tile */
    private void goAround(String direction) throws InterruptedException, IOException {

        String previousDirection = "";
        String direction1 = previousDirection;
        while ((roverTracker.getCurrentLocation().ypos > roverTracker.peekMarker().getY()
                && direction.equals("N"))
                || (roverTracker.getCurrentLocation().xpos > roverTracker.peekMarker().getX()
                        && direction.equals("W"))
                || (roverTracker.getCurrentLocation().ypos < roverTracker.peekMarker().getY()
                        && direction.equals("S"))
                || (roverTracker.getCurrentLocation().xpos < roverTracker.peekMarker().getX()
                        && direction.equals("E"))) {
            int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
            direction1 = previousDirection;

            if ((!blocked(0, 1) && (blocked(1, -1, centerIndex, centerIndex + 1) || blocked(1, 1)))
                    && !previousDirection.equals("N")) {
                move("S");
                previousDirection = "S";
                continue;
            }

            if ((!blocked(0, -1)
                    && (blocked(-1, 1, centerIndex, centerIndex - 1) || blocked(-1, -1)))
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

            if ((!blocked(1, 0)
                    && (blocked(-1, -1, centerIndex + 1, centerIndex) || blocked(1, -1)))
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
        /* request the server to move the rover */
        out.println("MOVE " + direction);

        /* Update rover current location */
        roverTracker.setCurrentLocation(requestCurrentLOC());

        /* Update the scan map */
        this.doScan();
        scanMap.debugPrintMap();

        /* if the ROVER moved, update its distance tracker */
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

            /* scan the map for science, share result to other ROVEERS, and
             * dispaly science discovery summary */
            communicationServer.detectAndShare(scanMap, roverTracker.getCurrentLocation());

            /* display current loc, destination, distances to destination) */
            displaySummary();
        }
        Thread.sleep(SLEEP_TIME);
    }

    /** Display summary of the current trip: current LOC, Destination LOC, and
     * the x,y distance to the destination */
    private void displaySummary() {
        System.out.println(rovername + " Distance Left = " + roverTracker.getDistanceTracker().xpos
                + "," + roverTracker.getDistanceTracker().ypos);
        System.out.println(rovername + " Current LOC: " + roverTracker.getCurrentLocation());
        System.out.println(rovername + " Destination LOC: " + roverTracker.getDestination());
        System.out.println("--------------------------------");

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
        return mapTile.getHasRover() || mapTile.getTerrain() == Terrain.SAND
                || mapTile.getTerrain() == Terrain.ROCK || mapTile.getTerrain() == Terrain.NONE;
    }

    /** s Returns coordinate object that represents rover's current location */
    private Coord requestCurrentLOC() throws IOException, InterruptedException {

        /* Coordinate of ROVER current Location */
        Coord currentLOC = null;

        /* Request LOC from Swarm Server */
        out.println("LOC");

        /* Read the response from the server */
        String results = in.readLine();

        /* No result probalby means ROVER disconnected from the server. For
         * debugging purposes */
        if (results == null) {
            System.out.println(rovername + " check connection to server");
        }

        /* Process the data and convert to Coord object */
        if (results.startsWith("LOC")) {
            currentLOC = extractLOC(results);
        }

        return currentLOC;
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

                if (blocked(map[x + 5][y + 5]) && xAdjusted == destination.xpos
                        && yAdjusted == destination.ypos) {
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
