package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import enums.Direction;
import enums.Terrain;

/**
 * The seed that this program is built on is a chat program example found here:
 * http://cs.lmu.edu/~ray/notes/javanetexamples/ Many thanks to the authors for
 * publishing their code examples
 */

public class ROVER_06 {

    BufferedReader in;
    PrintWriter out;
    String rovername;
    ScanMap scanMap;
    int sleepTime;
    String SERVER_ADDRESS = "localhost";
    static final int PORT_ADDRESS = 9537;

    Direction currentDirection = Direction.SOUTH;
    Direction previousDirection;
    boolean changeDirection = false;
    final int MAX_CHANGE_COUNT = 3;
    int changeCount = 0;
    int moveCount = 0;
    final int MAX_MOVE_COUNT = 6;

    // just means it did not change locations between requests, could be
    // velocity limit or obstruction etc.
    boolean stuck = false;
    boolean blocked = false;

    public ROVER_06() {
        System.out.println("ROVER_06 rover object constructed");
        rovername = "ROVER_06";
        SERVER_ADDRESS = "localhost";

        // this should be a safe but slow timer value
        // in milliseconds - smaller is faster, but the server
        // will cut connection if it is too small
        sleepTime = 300;

    }

    public ROVER_06(String serverAddress) {
        System.out.println("ROVER_06 rover object constructed");
        rovername = "ROVER_06";
        SERVER_ADDRESS = serverAddress;

        // in milliseconds - smaller is faster, but the server
        // will cut connection if it is too small
        sleepTime = 200;

    }

    /**
     * Connects to the server then enters the processing loop.
     */
    private void run() throws IOException, InterruptedException {

        // Make connection and initialize streams
        Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);

        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Gson gson = new GsonBuilder().setPrettyPrinting().create();

        // Process all messages from server, wait until server requests Rover ID
        // name
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

        String[] cardinals = new String[4];
        cardinals[0] = "N";
        cardinals[1] = "E";
        cardinals[2] = "S";
        cardinals[3] = "W";

        String currentDir = cardinals[0];
        Coord currentLoc = null;
        Coord previousLoc = null;

        // start Rover controller process
        while (true) {

            // currently the requirements allow sensor calls to be made with no
            // simulated resource cost

            // **** location call ****
            out.println("LOC");
            line = in.readLine();
            if (line == null) {
                System.out.println("ROVER_06 check connection to server");
                line = "";
            }
            if (line.startsWith("LOC")) {
                // loc = line.substring(4);
                currentLoc = extractLOC(line);
            }
            System.out.println("ROVER_06 currentLoc at start: " + currentLoc);

            // after getting location set previous equal current to be able to
            // check for stuckness and blocked later
            previousLoc = currentLoc;

            // **** get equipment listing ****
            ArrayList<String> equipment = new ArrayList<String>();
            equipment = getEquipment();
            // System.out.println("ROVER_06 equipment list results drive " +
            // equipment.get(0));
            System.out.println(
                    "ROVER_06 equipment list results " + equipment + "\n");

            // ***** do a SCAN *****
            // System.out.println("ROVER_06 sending SCAN request");
            this.doScan();
            scanMap.debugPrintMap();

            // ***** MOVING *****

            // pull the MapTile array out of the ScanMap object
            MapTile[][] scanMapTiles = scanMap.getScanMap();
            int centerIndex = (scanMap.getEdgeSize() - 1) / 2;
            // tile S = y + 1; N = y - 1; E = x + 1; W = x - 1

            // if the path the rover is heading to is block then check to see if
            // the next path [east, west, north, south].
            // is blocked. if it IS block, check the next path, and so on etc,
            if (blocked) {
                System.out.println("###Rover is blocked###");
                previousDirection = currentDirection;
                currentDirection = changeDirection(currentDirection);
                blocked = false;
                changeDirection = true;
            } else if (changeDirection) {
                System.out.println("### CHANGE COUNT: " + changeCount + " ###");
                blocked = isNextBlock(currentDirection, scanMapTiles,
                        centerIndex);
                if (!blocked) {
                    move(currentDirection);
                    if (changeCount++ % MAX_CHANGE_COUNT == 0) {
                        changeDirection = false;
                        currentDirection = previousDirection;
                    }
                }
            } else {
                
                System.out.println("### MOVE COUNT: " + moveCount + " ###");
                blocked = isNextBlock(currentDirection, scanMapTiles,
                        centerIndex);
                if (!blocked) {
                    move(currentDirection);
                }
//                if(moveCount++ % MAX_MOVE_COUNT == 0) {
//                    changeDirection = true;
//                    previousDirection = currentDirection;
//                    currentDirection = changeDirection(currentDirection);
//                }
            }

            // another call for current location
            out.println("LOC");
            line = in.readLine();
            if (line == null) {
                System.out.println("ROVER_06 check connection to server");
                line = "";
            }
            if (line.startsWith("LOC")) {
                currentLoc = extractLOC(line);
            }

            // System.out.println("ROVER_06 currentLoc after recheck: " +
            // currentLoc);
            // System.out.println("ROVER_06 previousLoc: " + previousLoc);

            // test for stuckness
            stuck = currentLoc.equals(previousLoc);

            // System.out.println("ROVER_06 stuck test " + stuck);
            System.out.println("ROVER_06 blocked test " + blocked);

            // TODO - logic to calculate where to move next

            Thread.sleep(sleepTime);

            System.out.println(
                    "ROVER_06 ------------ bottom process control --------------");
        }

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
        sStr = sStr.substring(4);
        if (sStr.lastIndexOf(" ") != -1) {
            String xStr = sStr.substring(0, sStr.lastIndexOf(" "));
            // System.out.println("extracted xStr " + xStr);

            String yStr = sStr.substring(sStr.lastIndexOf(" ") + 1);
            // System.out.println("extracted yStr " + yStr);
            return new Coord(Integer.parseInt(xStr), Integer.parseInt(yStr));
        }
        return null;
    }

    public boolean isNextBlock(Direction inputDirection,
            MapTile[][] scanMapTiles, int centerIndex) {

        switch (inputDirection) {
        case NORTH:
            return isBlock(scanMapTiles[centerIndex][centerIndex - 1]);
        case SOUTH:
            return isBlock(scanMapTiles[centerIndex][centerIndex + 1]);
        case WEST:
            return isBlock(scanMapTiles[centerIndex - 1][centerIndex]);
        case EAST:
            return isBlock(scanMapTiles[centerIndex + 1][centerIndex]);
        default:
            // this code should be unreachable
            return false;
        }
    }

    private boolean isBlock(MapTile tile) {
        List<Terrain> blockers = Arrays.asList(Terrain.ROCK, Terrain.NONE,
                Terrain.SAND);
        Terrain terrain = tile.getTerrain();
        return tile.getHasRover() || blockers.contains(terrain);
    }

    private Direction changeDirection(Direction direction) {
        switch (direction) {
        case NORTH:
            return Direction.WEST;
        case SOUTH:
            return Direction.EAST;
        case WEST:
            return Direction.SOUTH;
        case EAST:
            return Direction.NORTH;
        default:
            return null;
        }
    }

    private void move(Direction direction) {
        switch (direction) {
        case NORTH:
            out.println("MOVE N");
            break;
        case SOUTH:
            out.println("MOVE S");
            break;
        case WEST:
            out.println("MOVE W");
            break;
        case EAST:
            out.println("MOVE E");
            break;
        }
    }

    /**
     * Runs the client
     */
    public static void main(String[] args) throws Exception {
        ROVER_06 client = new ROVER_06();
        client.run();
    }
}