package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import common.Coord;
import common.MapTile;
import common.ScanMap;
import communication.CommunicationServer;
import communication.Group;
import enums.Direction;
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
    int sleepTime;
    String SERVER_ADDRESS;
    static final int PORT_ADDRESS = 9537;

    CommunicationServer communicationServer;
    Set<Coord> discoveredScience = new HashSet<Coord>();

    Queue<Direction> paths = new LinkedList<Direction>();
    Coord currentLoc;
    Direction current = Direction.EAST;

    public ROVER_06(String serverAddress) {
        System.out.println("ROVER_06 rover object constructed");
        rovername = "ROVER_06";
        SERVER_ADDRESS = serverAddress;
        
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

        // connect to all all the other rovers
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

        // start Rover controller process
        while (true) {

            // **** location call ****
            updateLoc();
            // ********************

            // ***** do a SCAN *****
            // System.out.println("ROVER_06 sending SCAN request");
            this.doScan();
            scanMap.debugPrintMap();

            // pull the MapTile array out of the ScanMap object
            // tile S = y + 1; N = y - 1; E = x + 1; W = x - 1
            MapTile[][] scanMapTiles = scanMap.getScanMap();
            int centerIndex = (scanMap.getEdgeSize() - 1) / 2;

            // ********************

            // **** movement ***
            if (paths.isEmpty()) {
                findPath(current, scanMapTiles, centerIndex);
            } else {
                masterMove();
            }

            communicationServer.shareScience("hello world");
            // ********************

            // *** science ***
            detectRadioactive(scanMapTiles);
            System.out.println("SCIENCE COLLECTED: " + discoveredScience);
            // ********************

            // *** wait ***
            Thread.sleep(sleepTime);
            // ********************

            // **** Finish 1 move ***
            System.out.println(
                    "-------------------------- END PANEL --------------------------------");
            // ********************
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

    /** move the rover one tile */
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

    /** the rover move logic
     * 
     * @throws InterruptedException
     * @throws IOException */
    private void masterMove() throws IOException, InterruptedException {
        System.out.println("Moving: " + paths.peek());
        move(paths.poll());
    }

    /** iterate through a scan map to find a tile with radiation. get the
     * adjusted (absolute) coordinate of the tile and added into a hash set */
    private void detectRadioactive(MapTile[][] scanMapTiles) {
        for (int x = 0; x < scanMapTiles.length; x++) {
            for (int y = 0; y < scanMapTiles[x].length; y++) {
                MapTile mapTile = scanMapTiles[x][y];
                if (mapTile.getScience() == Science.RADIOACTIVE) {
                    int tileX = currentLoc.xpos + (x - 5);
                    int tileY = currentLoc.ypos + (y - 5);
                    Coord coord = new Coord(mapTile.getTerrain(),
                            mapTile.getScience(), tileX, tileY);

                    if (!discoveredScience.contains(coord)) {
                        discoveredScience.add(coord);
                        communicationServer.shareScience(coord.toString());
                    }
                }
            }
        }
    }

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

    private void updateLoc() throws IOException {

        String temp;

        out.println("LOC");
        temp = in.readLine();
        if (temp == null) {
            System.out.println("ROVER_06 check connection to server");
            temp = "";
        }

        if (temp.startsWith("LOC")) {
            currentLoc = extractLOC(temp);
        }

        System.out.println("ROVER_06 currentLoc at start: " + currentLoc);
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
