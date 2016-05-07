package swarmBots;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
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
import enums.Direction;
import enums.Science;
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

	Direction current = Direction.EAST;

	Queue<Direction> paths = new LinkedList<Direction>();
	Set<Coord> discoveredMap = new HashSet<Coord>();

	Coord currentLoc;

	Set<Coord> science_collection = new HashSet<Coord>();
	Set<Coord> displayed_science = new HashSet<Coord>();
	List<Link> blue = new ArrayList<Link>();
	List<Socket> sockets = new ArrayList<Socket>();

	Direction previous = Direction.EAST;
	boolean startCount = false;
	int count = 0;
	final int MAX_COUNT = 20;

	public ROVER_06() {
		System.out.println("ROVER_06 rover object constructed");
		rovername = "ROVER_06";
		SERVER_ADDRESS = "localhost";

		// this should be a safe but slow timer value
		// in milliseconds - smaller is faster, but the server
		// will cut connection if it is too small
		sleepTime = 500;
	}

	public ROVER_06(String serverAddress) {
		System.out.println("ROVER_06 rover object constructed");
		rovername = "ROVER_06";
		SERVER_ADDRESS = serverAddress;

		// in milliseconds - smaller is faster, but the server
		// will cut connection if it is too small
		sleepTime = 500;
	}

	class RoverComm implements Runnable {

		String ip;
		int port;
		Socket socket;

		public RoverComm(String ip, int port) {
			this.ip = ip;
			this.port = port;
		}

		@Override
		public void run() {
			do {
				try {
					socket = new Socket(ip, port);
				} catch (UnknownHostException e) {

				} catch (IOException e) {

				}
			} while (socket == null);
			sockets.add(socket);
			System.out.println(socket.getPort() + " " + socket.getInetAddress());
		}

	}

	/**
	 * add all rover's ip and port number into a list so they can be connected
	 */
	public void initConnection() {
		// dummy value # 1
		blue.add(new Link("Dummy Group #1", "localhost", 8000));
		// dummy value # 2
		blue.add(new Link("Dummy Group #2", "localhost", 9000));

		// blue rooster
		blue.add(new Link("GROUP_01", "localhost", 53701));
		blue.add(new Link("GROUP_02", "localhost", 53702));
		blue.add(new Link("GROUP_03", "localhost", 53703));
		blue.add(new Link("GROUP_04", "localhost", 53704));
		blue.add(new Link("GROUP_05", "localhost", 53705));
		blue.add(new Link("GROUP_07", "localhost", 53707));
		blue.add(new Link("GROUP_08", "localhost", 53708));
		blue.add(new Link("GROUP_09", "localhost", 53709));
	}

	/**
	 * Connects to the server then enters the processing loop.
	 */
	private void run() throws IOException, InterruptedException {

		// Make connection and initialize streams
		Socket socket = new Socket(SERVER_ADDRESS, PORT_ADDRESS);
		in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		out = new PrintWriter(socket.getOutputStream(), true);

		// connect to all all the other rovers
		initConnection();
		for (Link link : blue) {
			new Thread(new RoverComm(link.ip, link.port)).start();
		}

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

		// **** get equipment listing ****
		ArrayList<String> equipment = new ArrayList<String>();
		equipment = getEquipment();
		// System.out.println("ROVER_06 equipment list results drive " +
		// equipment.get(0));
		System.out.println("ROVER_06 equipment list results " + equipment + "\n");

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

			// add all newly discovered map to hash set
			updateMap(scanMapTiles, centerIndex);
			// ********************

			// **** movement ***
			if (paths.isEmpty()) {
				findPath(current, scanMapTiles, centerIndex);
			} else {
				if (startCount) {
					count++;

					if (count >= MAX_COUNT) {
						startCount = false;
						count = 0;
						current = previous;
					}
				}
				masterMove();

			}
			// ********************

			// *** science ***
			detectRadioactive(scanMapTiles);
			shareScience();
			System.out.println("SCIENCE COLLECTED: " + science_collection);
			// ********************

			// *** wait ***
			Thread.sleep(sleepTime);
			// ********************

			// **** Finish 1 move ***
			System.out.println("-------------------------- END PANEL --------------------------------");
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

	/** determine if the rover is about to reach a "blocked" tile */
	private boolean isNextBlock(Direction direction, MapTile[][] scanMapTiles, int centerIndex) {

		switch (direction) {
		case NORTH:
			return isBlocked(scanMapTiles[centerIndex][centerIndex - 1]);
		case SOUTH:
			return isBlocked(scanMapTiles[centerIndex][centerIndex + 1]);
		case WEST:
			return isBlocked(scanMapTiles[centerIndex - 1][centerIndex]);
		case EAST:
			return isBlocked(scanMapTiles[centerIndex + 1][centerIndex]);
		default:
			// this code should be unreachable
			return false;
		}
	}

	/** determine if the rover is on ROCK NONE OR SAND */
	private boolean isBlocked(MapTile tile) {
		List<Terrain> blockers = Arrays.asList(Terrain.ROCK, Terrain.NONE, Terrain.SAND);
		Terrain terrain = tile.getTerrain();
		return tile.getHasRover() || blockers.contains(terrain);
	}

	/** return a DIFFERENT direction */
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

	/**
	 * recursively call itself until it find a direction that won't lead to a
	 * blocked path
	 */
	private Direction findGoodDirection(Direction direction, MapTile[][] scanMapTiles, int centerIndex) {

		if (isNextBlock(direction, scanMapTiles, centerIndex)) {
			return findGoodDirection(changeDirection(direction), scanMapTiles, centerIndex);
		} else {
			return direction;
		}
	}

	/**
	 * the rover move logic
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	private void masterMove() throws IOException, InterruptedException {
		System.out.println("Moving: " + paths.peek());
		move(paths.poll());
	}

	/**
	 * determine if the rover is about to reach a "NONE" tile. use to indicate
	 * that you've reach the edge of the map and may need to permantly change
	 * direction
	 */
	private boolean isNextEdge(Direction direction, MapTile[][] scanMapTiles, int centerIndex) {

		switch (direction) {
		case NORTH:
			return isNone(scanMapTiles[centerIndex][centerIndex - 1]);
		case SOUTH:
			return isNone(scanMapTiles[centerIndex][centerIndex + 1]);
		case WEST:
			return isNone(scanMapTiles[centerIndex - 1][centerIndex]);
		case EAST:
			return isNone(scanMapTiles[centerIndex + 1][centerIndex]);
		default:
			// this code should be unreachable
			return false;
		}
	}

	/**
	 * iterate through a scan map to find a tile with radiation. get the
	 * adjusted (absolute) coordinate of the tile and added into a hash set
	 */
	private void detectRadioactive(MapTile[][] scanMapTiles) {
		for (int x = 0; x < scanMapTiles.length; x++) {
			for (int y = 0; y < scanMapTiles[x].length; y++) {
				MapTile mapTile = scanMapTiles[x][y];
				if (mapTile.getScience() == Science.RADIOACTIVE) {
					int tileX = currentLoc.xpos + (x - 5);
					int tileY = currentLoc.ypos + (y - 5);
					Coord coord = new Coord(mapTile.getTerrain(), mapTile.getScience(), tileX, tileY);
					science_collection.add(coord);
				}
			}
		}
	}

	private void findPath(Direction d, MapTile[][] mts, int c) {

		switch (d) {
		case NORTH:
			System.out.println("CURRENT: NORTH");
			// go NORTH if there is nothing blocking you
			if (isValid(mts[c][c - 1])) {
				paths.add(Direction.NORTH);
			} else {

				// if you arrived to the edge of the map, change direction: WEST
				if (isNone(mts[c][c - 1])) {
					current = Direction.WEST;
					findPath(current, mts, c);
				}
				// if there is something block your path, go west
				else if (isValid(mts[c - 1][c])) {
					System.out.println("ADDED WEST to PATH");
					paths.add(Direction.WEST);
				}
				// if you can't go NORTH AND WEST, go SOUTH then WEST
				else {

					for (int x = c; x < mts.length; x++) {

						if (isValid(mts[c - 1][x])) {

							System.out.println("Added WEST to PATH");
							paths.add(Direction.WEST);
							break;

						} else {

							System.out.println("Added SOUTH TO PATH");

							if (isValid(mts[c][c + 1])) {
								paths.add(Direction.SOUTH);
							} else {
								current = Direction.SOUTH;
							}

						}
					}
				}
			}
			break;
		case EAST:
			System.out.println("CURRENT: EAST");

			// go east if possible
			if (isValid(mts[c + 1][c])) {
				System.out.println("ADDED EAST  TO PATH");
				paths.add(Direction.EAST);
			} else {

				// if what is blocking you is a "corners" of "NONE", try to go
				// around it
				if (isNone(mts[c + 1][c]) && isNone(mts[c][c - 1])) {

					System.out.println("INSIDE A CORNER");
					System.out.println("ADDED WEST TO PATH #1");
					paths.add(Direction.WEST);

					if (isValid(mts[c - 1][c - 1])) {
						System.out.println("ADDED NORTH TO PATH");
						paths.add(Direction.NORTH);
					}

				} else if (isValid(mts[c][c - 1])) {

					// if you can't go east, try north
					System.out.println("ADDED NORTH TO PATH");
					paths.add(Direction.NORTH);

				} else if (isNone(mts[c + 1][c]) && isValid(mts[c + 1][c - 1])) {
					

					current = Direction.NORTH;

				} else {

					// if you can't go EAST and NORTH: check if theres is a wall
					// above
					// you probably reach the edge, try going west now
					if (isNone(mts[c][c - 1])) {
						current = Direction.WEST;
						findPath(current, mts, c);
					} else {

						for (int x = c; x > 0; x--) {

							if (isValid(mts[x][c - 1])) {

								System.out.println("Added NORTH to PATH");
								paths.add(Direction.NORTH);
								break;

							} else {

								if (isValid(mts[x - 1][c])) {

									System.out.println("Added WEST TO PATH #2");
									paths.add(Direction.WEST);

								} else if (isValid(mts[x - 1][c + 1])) {
									System.out.println("ADDED SOUTH TO PATH");
									System.out.println("HELLO HELLO HELLO");
									paths.add(Direction.SOUTH);
									startCount = true;
									previous = Direction.EAST;
								}
								
								if (startCount) {
									System.out.println("WORLD WORLD WORLD");
									current = Direction.NORTH;
								}
								
							}
						}
					}

				}
			}

			break;
		case SOUTH:
			System.out.println("CURRENT: SOUTH");
			// go west if there is nothing blocking you
			if (isValid(mts[c][c + 1])) {
				paths.add(Direction.SOUTH);
			} else {

				// if you arrived to the edge of the map, then go EAST
				if (isNone(mts[c][c + 1])) {
					System.out.println("YOU HAVE REACH THE EDGE");
					current = Direction.EAST;
					findPath(current, mts, c);
				}
				// if there is something block your path, try to go south
				// instead
				else if (isValid(mts[c + 1][c])) {
					System.out.println("CAN'T GO SOUTH, BUT EAST IS SAFE");
					paths.add(Direction.EAST);
				}
				// if you can't go SOUTH, then try going EAST until you can go
				// SOUTH.
				else {

					for (int x = c; x > 0; x--) {
						System.out.println("CANT GO EAST, READJUSTING...");
						System.out.println("Currently x is: " + x);

						if (isValid(mts[c + 1][x])) {

							System.out.println("Added EAST to PATH");
							paths.add(Direction.EAST);
							break;

						} else {

							System.out.println("Added NORTH TO PATH");
							paths.add(Direction.NORTH);

						}
					}
				}

			}
			break;
		case WEST:
			System.out.println("CURRENT: WEST");
			// go west if there is nothing blocking you
			if (isValid(mts[c - 1][c])) {
				paths.add(d);
			} else {

				// if you arrived to the edge of the map, then go SOUTH
				if (isNone(mts[c - 1][c])) {
					System.out.println("YOU HAVE REACH TEH EDGE");
					current = Direction.SOUTH;
					findPath(current, mts, c);
				}
				// if there is something block your path, try to go south
				// instead
				else if (isValid(mts[c][c + 1])) {
					System.out.println("CAN'T GO WEST, BUT SOUTH IS SAFE");
					paths.add(Direction.SOUTH);
				}
				// if you can't go SOUTH, then try going EAST until you can go
				// SOUTH.
				else {

					for (int x = c; x < mts.length; x++) {
						System.out.println("CANT GO WEST, READJUSTING...");

						if (isValid(mts[x][c + 1])) {

							System.out.println("Added SOUTH to PATH");
							paths.add(Direction.SOUTH);
							break;

						} else {

							if (isValid(mts[x][c + 1])) {
								System.out.println("Added EAST TO PATH");
								paths.add(Direction.EAST);
							} else if (isValid(mts[x][c - 1])) {
								System.out.println("Added NORTH TO PATH");
								paths.add(Direction.NORTH);
							}

						}
					}
				}

			}
			break;
		}

	}

	private boolean isValid(MapTile mt) {

		return !mt.getHasRover() && mt.getTerrain() != Terrain.SAND && mt.getTerrain() != Terrain.ROCK
				&& mt.getTerrain() != Terrain.NONE;
	}

	/** determine if the tile is NONE */
	private boolean isNone(MapTile tile) {
		return tile.getTerrain() == Terrain.NONE;
	}

	/**
	 * write to each rover the coords of a tile that contains radiation. will
	 * only write to them if the coords haven't is new.
	 */
	public void shareScience() {
		for (Coord c : science_collection) {
			if (!displayed_science.contains(c)) {
				for (Socket s : sockets)
					try {
						new DataOutputStream(s.getOutputStream()).writeBytes(c.toString() + "\r\n");
					} catch (Exception e) {

					}
				displayed_science.add(c);
			}
		}
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

	private void updateMap(MapTile[][] scanMapTiles, int centerIndex) {

		for (int x = 0; x < scanMapTiles.length; x++) {

			for (int y = 0; y < scanMapTiles[x].length; y++) {
				MapTile mapTile = scanMapTiles[x][y];
				int tileX = currentLoc.xpos + (x - 5);
				int tileY = currentLoc.ypos + (y - 5);
				Coord coord = new Coord(mapTile.getTerrain(), mapTile.getScience(), tileX, tileY);
				discoveredMap.add(coord);
			}
		}
	}

	/**
	 * Runs the client
	 */
	public static void main(String[] args) throws Exception {
		if (args.length == 0) {
			System.out.println(1);
			new ROVER_06().run();
		} else {
			System.out.println(2);
			new ROVER_06(args[0]).run();
		}
	}

}
