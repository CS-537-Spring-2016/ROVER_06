package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
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
import enums.Direction;
import enums.Terrain;

/** The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples */

public class ROVER_06 {

    BufferedReader in;
    PrintWriter out;
    String rovername;
    ScanMap scanMap;
    int sleepTime;
    String SERVER_ADDRESS;
    static final int PORT_ADDRESS = 9537;

    CommunicationServer communicationServer;
    Set<Coord> discoveredScience = new HashSet<Coord>();

    Coord currentLoc;
    Coord targetLoc;

    // group 3
    Tracker roverTracker;

    public ROVER_06(String serverAddress) {
        System.out.println("ROVER_06 rover object constructed");
        rovername = "ROVER_06";
        SERVER_ADDRESS = serverAddress;
        roverTracker = new Tracker();

        // server with all the ROVER INFO from blue corp
        communicationServer = new CommunicationServer(Group.BLUE_CORP());

        // in milliseconds - smaller is faster, but the server
        // will cut connection if it is too small
        sleepTime = 500;
    }

    /** Connects to the server then enters the processing loop. */
    private void run() throws IOException, InterruptedException {

        // Make connection and initialize streams
        Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        /* starts the communication server. will try to connect to all the other
         * rovers on a seperate thread. */
        new Thread(communicationServer).start();

        while (true) {
            String line = in.readLine();
            if (line.startsWith("SUBMITNAME")) {

                // This sets the name of this instance of a swarmBot for
                // identifying thread to the server
                out.println(rovername);
                break;
            }
        }

        // ******** Rover logic *********
        // int cnt=0;
        String line = "";

        // **** get equipment listing ****
        ArrayList<String> equipment = new ArrayList<String>();
        equipment = getEquipment();
        // System.out.println("ROVER_06 equipment list results drive " +
        // equipment.get(0));
        System.out
                .println("ROVER_06 equipment list results " + equipment + "\n");

        // target_location request
        out.println("TARGET_LOC");
        Coord target = extractLOC(in.readLine());
        roverTracker.setTargetLocation(target);
        getLocation();
        startMission(target);

        /* Puts all the tiles in the target location in the job queue */
        if (roverTracker.atTargetLocation(roverTracker.getCurrentLocation())) {
            for (int x = -3; x < 4; x++)
                for (int y = -3; y < 4; y++)
                    if (!blocked(x, y))
                        communicationServer.getQueue().addLocation(new Coord(
                                x + roverTracker.getCurrentLocation().xpos,
                                y + roverTracker.getCurrentLocation().ypos));
        }

        // Start Rover controller process
//        while (true)
//            if (!communicationServer.getQueue().isEmpty()) {
//                getLocation();
//                startMission(
//                        communicationServer.getQueue().closestTargetLocation(
//                                roverTracker.getCurrentLocation()));
//            }

        // // start Rover controller process
        // while (true) {
        //
        // // **** location call ****
        // currentLoc = getCurrentLoc();
        // // ********************
        //
        // // ***** do a SCAN *****
        // // System.out.println("ROVER_06 sending SCAN request");
        // this.doScan();
        // scanMap.debugPrintMap();
        //
        // // pull the MapTile array out of the ScanMap object
        // // tile S = y + 1; N = y - 1; E = x + 1; W = x - 1
        // MapTile[][] scanMapTiles = scanMap.getScanMap();
        // int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
        //
        // // ********************
        //
        // // **** movement ***
        //
        // // ********************
        //
        // // *** science ***
        // detectRadioactive(scanMapTiles);
        // System.out.println("SCIENCE COLLECTED: " + discoveredScience);
        // // ********************
        //
        // // *** wait ***
        // Thread.sleep(sleepTime);
        // // ********************
        //
        // // **** Finish 1 move ***
        // System.out.println(
        // "-------------------------- END PANEL
        // --------------------------------");
        // // ********************
        // }

    }

    // ################ Support Methods ###########################

    private void clearReadLineBuffer() throws IOException {
        while (in.ready()) {
            // System.out.println("ROVER_06 clearing readLine()");
            String garbage = in.readLine();
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
        returnList = gson.fromJson(jsonEqListString,
                new TypeToken<ArrayList<String>>() {
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
        System.out.println("ROVER_06 incomming SCAN result - first readline: "
                + jsonScanMapIn);

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
        return new Coord(Integer.parseInt(coordinates[1].trim()),
                Integer.parseInt(coordinates[2].trim()));
    }

    // /** iterate through a scan map to find a tile with radiation. get the
    // * adjusted (absolute) coordinate of the tile and added into a hash set */
    // private void detectRadioactive(MapTile[][] scanMapTiles) {
    // for (int x = 0; x < scanMapTiles.length; x++) {
    // for (int y = 0; y < scanMapTiles[x].length; y++) {
    // MapTile mapTile = scanMapTiles[x][y];
    // if (mapTile.getScience() == Science.RADIOACTIVE) {
    // int tileX = currentLoc.xpos + (x - 5);
    // int tileY = currentLoc.ypos + (y - 5);
    // Coord coord = new Coord(mapTile.getTerrain(),
    // mapTile.getScience(), tileX, tileY);
    //
    // if (!discoveredScience.contains(coord)) {
    // discoveredScience.add(coord);
    // communicationServer.shareScience(coord.toString());
    // }
    // }
    // }
    // }
    // }

    private void findPath(Direction d, MapTile[][] mts, int c) {

    }

    private boolean isValid(MapTile mt) {

        return !mt.getHasRover() && mt.getTerrain() != Terrain.SAND
                && mt.getTerrain() != Terrain.ROCK
                && mt.getTerrain() != Terrain.NONE;
    }

    /** determine if the tile is NONE */
    private boolean isNone(MapTile tile) {
        return tile.getTerrain() == Terrain.NONE;
    }

    /** get the rover's curren't location on the */
    private Coord getCurrentLoc() throws IOException {

        String temp;
        Coord coord = null;

        out.println("LOC");
        temp = in.readLine();
        if (temp == null) {
            System.out.println("ROVER_06 check connection to server");
            temp = "";
        } else if (temp.startsWith("LOC")) {
            coord = extractLOC(temp);
        }

        System.out.println("ROVER_06 currentLoc at start: [X:" + coord.xpos
                + ", Y:" + coord.ypos + "]");

        return coord;
    }

    private void startMission(Coord destination)
            throws IOException, InterruptedException {
        System.out.println(
                "\nCURRENT LOCATION: " + roverTracker.getCurrentLocation());
        roverTracker.setStartingPoint(roverTracker.getCurrentLocation());
        System.out
                .println("STARTING POINT: " + roverTracker.getStartingPoint());
        roverTracker.setDestination(destination);
        System.out.println("DESTINATION: " + destination);
        roverTracker.setDistanceTracker();
        System.out.println("DISTANCE: " + roverTracker.getDistanceTracker());

        String direction = null;

        while (!roverTracker.hasArrived()) {
            direction = resolveDirection();
            if (direction.equals("E")) {
                System.out.println("HEADED EAST");
                accelerate(1, 0);
            }
            if (direction.equals("W")) {
                System.out.println("HEADED WEST");
                accelerate(-1, 0);
            }
            if (direction.equals("S")) {
                System.out.println("HEADED SOUTH");
                accelerate(0, 1);
            }
            if (direction.equals("N")) {
                System.out.println("HEADED NORTH");
                accelerate(0, -1);
            }
        }
    }

    /* This method is used to decide what direction the rover will go next */
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

    private void accelerate(int xVelocity, int yVelocity)
            throws IOException, InterruptedException {

        String direction = (xVelocity == 1) ? "E"
                : (xVelocity == -1) ? "W"
                        : (yVelocity == 1) ? "S"
                                : (yVelocity == -1) ? "N" : null;
        while ((xVelocity != 0) ? roverTracker.getDistanceTracker().xpos != 0
                : roverTracker.getDistanceTracker().ypos != 0) {
            if (!blocked(xVelocity, yVelocity))
                move(direction);
            else {
                roverTracker.addMarker(new State(new Coord(
                        roverTracker.getCurrentLocation().xpos + xVelocity,
                        roverTracker.getCurrentLocation().ypos + yVelocity)));
                roverTracker.setLastSuccessfulMove(
                        roverTracker.getCurrentLocation());
                goAround(direction);
            }
            getLocation();
        }
    }

    private void goAround(String direction)
            throws InterruptedException, IOException {

        String previousDirection = "";
        String direction1 = previousDirection;
        while ((roverTracker.getCurrentLocation().ypos > roverTracker
                .peekMarker().getY() && direction.equals("N"))
                || (roverTracker.getCurrentLocation().xpos > roverTracker
                        .peekMarker().getX() && direction.equals("W"))
                || (roverTracker.getCurrentLocation().ypos < roverTracker
                        .peekMarker().getY() && direction.equals("S"))
                || (roverTracker.getCurrentLocation().xpos < roverTracker
                        .peekMarker().getX() && direction.equals("E"))) {
            getLocation();
            int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
            direction1 = previousDirection;

            if ((!blocked(0, 1) && (blocked(1, -1, centerIndex, centerIndex + 1)
                    || blocked(1, 1))) && !previousDirection.equals("N")) {
                move("S");
                previousDirection = "S";
                continue;
            }

            if ((!blocked(0, -1)
                    && (blocked(-1, 1, centerIndex, centerIndex - 1)
                            || blocked(-1, -1)))
                    && !previousDirection.equals("S")) {
                move("N");
                previousDirection = "N";
                continue;
            }

            if ((!blocked(-1, 0) && (blocked(1, 1, centerIndex - 1, centerIndex)
                    || blocked(-1, 1))) && !previousDirection.equals("E")) {
                move("W");
                previousDirection = "W";
                continue;
            }

            if ((!blocked(1, 0)
                    && (blocked(-1, -1, centerIndex + 1, centerIndex)
                            || blocked(1, -1)))
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

    private void move(String direction)
            throws IOException, InterruptedException {
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
            System.out.println(
                    "Distance Left = " + roverTracker.getDistanceTracker().xpos
                            + "," + roverTracker.getDistanceTracker().ypos);
        }
        Thread.sleep(sleepTime);
    }

    private boolean blocked(int xOffset, int yOffset) {
        MapTile[][] map = scanMap.getScanMap();
        int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
        return map[centerIndex + xOffset][centerIndex + yOffset].getHasRover()
                || map[centerIndex + xOffset][centerIndex + yOffset]
                        .getTerrain() == Terrain.ROCK
                || map[centerIndex + xOffset][centerIndex + yOffset]
                        .getTerrain() == Terrain.NONE
                || map[centerIndex + xOffset][centerIndex + yOffset]
                        .getTerrain() == Terrain.SAND;
    }

    private boolean blocked(int xOffset, int yOffset, int roverX, int roverY) {
        MapTile[][] map = scanMap.getScanMap();
        return map[roverX + xOffset][roverY + yOffset].getHasRover()
                || map[roverX + xOffset][roverY + yOffset]
                        .getTerrain() == Terrain.ROCK
                || map[roverX + xOffset][roverY + yOffset]
                        .getTerrain() == Terrain.NONE
                || map[roverX + xOffset][roverY + yOffset]
                        .getTerrain() == Terrain.SAND;
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
            // scanMap.debugPrintMap();
        }
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
