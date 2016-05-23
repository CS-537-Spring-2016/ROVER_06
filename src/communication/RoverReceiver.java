package communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import common.Coord;
import enums.Science;
import enums.Terrain;

/** Communication Module. Receive science and process science coordinates
 * 
 * @author Shay */
public class RoverReceiver implements Receiver {

    private ServerSocket listenSocket;

    /** Coordinates of all science share to this receiver from other ROVERS */
    private List<Coord> sharedScienceCoords = new ArrayList<Coord>();

    /** Terrains to ignore. Used to filter out Science coordinate */
    private List<Terrain> ignoredTerrains = new ArrayList<Terrain>();

    @Override
    public void startServer(ServerSocket serverSocket) throws IOException {
        listenSocket = serverSocket;

        /* Create a thread that waits for client to connects */
        new Thread(() -> {
            while (true) {
                try {
                    /* wait for a connection */
                    Socket connectionSocket = listenSocket.accept();

                    /* once there is a connection, serve them on a separate
                     thread */
                    new Thread(new RoverHandler(connectionSocket)).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    /**@return List of all the coordinate shared from other ROVERS */
    public List<Coord> getSharedCoords() {
        return sharedScienceCoords;
    }

    @Override
    /** Set the Terrains to ignore by this receiver */
    public void ignoreTerrains(Terrain... terrains) {
        ignoredTerrains = Arrays.asList(terrains);
    }

    @Override
    /**@return A list of Terrain this receiver is ignoring*/
    public List<Terrain> getIgnoredTerrains() {
        return ignoredTerrains;
    }

    /** Handle ROVER'S incoming message. Filtered and parse the data.
     * 
     * @author Shay */
    class RoverHandler implements Runnable {

        private Socket roverSocket;

        public RoverHandler(Socket socket) {
            this.roverSocket = socket;
        }

        @Override
        public void run() {

            BufferedReader input = null;
            try {
                input = new BufferedReader(
                        new InputStreamReader(roverSocket.getInputStream()));
                String[] line = input.readLine().split(" ");
                
                /* protocol: ROCK CRYSTAL 25 30 */
                /* parse and process the input */
                if (line.length == 4) {
                    Terrain terrain = Terrain.valueOf(line[0]);
                    Science science = Science.valueOf(line[1]);
                    int xpos = Integer.valueOf(line[2]);
                    int ypos = Integer.valueOf(line[3]);
                    Coord coord = new Coord(terrain, science, xpos, ypos);
                    
                    /* Add Coordinate if possible*/
                    updateShareScience(coord);
                }
               
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if(input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        
        /** If the Science Coordinate is not on an "ignored terrains" and is unique, add it to "shared science list"*/
        private void updateShareScience(Coord coord) {
            if (!ignoredTerrains.contains(coord.terrain) && !sharedScienceCoords.contains(coord)) {
                sharedScienceCoords.add(coord);
            }
        }
    }
}
