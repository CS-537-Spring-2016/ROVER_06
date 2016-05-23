package command_center;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.plaf.synth.SynthSpinnerUI;

import common.Coord;
import communication.Group;
import communication.RoverSender;
import communication.Sender;
import enums.Science;
import enums.Terrain;

/** Store all the science coordinates sent to by other ROVERS. Command Server
 * can rebroadcast all the ungathered sciences to ROVERS Science to selected
 * ROVERS. Serve as an insurance policy. If a gatherer ROVER disconnects, it
 * will probably lost all its sciences coordinate. This server can rebroadcast
 * all the science so all work won't be lost
 * 
 * @author Shay */
public class CommandServer {

    /* Basic Server Information */
    public ServerSocket listenSocket;
    public int port;
    public String name;

    /** A list of all the science that have been discovered but still need to be
     * GATHERED */
    private List<Coord> ungatheredScience = new ArrayList<Coord>();

    /** A list of all the Science discovered */
    private List<Coord> allScience = new ArrayList<Coord>();

    /** Time to wait till next broadcast of science. */
    private int timeWaitTillNextBroadcast = 5000;

    /** A List of groups that will receive a Coordinates of Science to still
     * need to be GATHRED */
    private List<Group> broadcastGroups = new ArrayList<Group>();

    /** Communicate module use to write message to other ROVERS */
    private Sender sender = new RoverSender();

    public CommandServer(String name, int port) {
        this.port = port;
        this.name = name;
    }

    /** Add a Group, or Groups, to the broadcast list. Command Server will
     * rebroadcast all ungathered science to all the ROVERS in the broadcast
     * list */
    public void addToBroadcastGroup(Group... groups) {
        for (Group g : groups) {
            if (!broadcastGroups.contains(g))
                broadcastGroups.add(g);
        }
    }

    /** @param groups
     *            List of groups you want the command server to rebroadcast
     *            too */
    public void setBroadcastGroup(List<Group> groups) {
        broadcastGroups = groups;
    }

    /** @return The groups that will the command center will rebroadcast to. */
    public List<Group> getBroadcastGroup() {
        return broadcastGroups;
    }

    /** Set time in milliseconds to wait till next rebroadcast of ungathered
     * science coordinate */
    public void setTimeWaitTillNextBroadcast(int milliseconds) {
        this.timeWaitTillNextBroadcast = milliseconds;
    }

    /** Filter a list of coordinate, return coordinate that match the filter
     * criteria */
    private List<Coord> filterCoords(List<Coord> list, Science filterScience) {
        return list.stream().filter((Coord c) -> c.science == filterScience)
                .collect(Collectors.toList());
    }

    /** Display all the science discovered onto the console */
    private void displayResult(List<Coord> list) {
        
        synchronized (this) {
            System.out.println("*************************************");
            
            displayFormatedList(filterCoords(list, Science.RADIOACTIVE), "RADIOACTIVE");
            displayFormatedList(filterCoords(list, Science.CRYSTAL), "CRYSTAL");
            displayFormatedList(filterCoords(list, Science.ORGANIC), "ORGANIC");
            displayFormatedList(filterCoords(list, Science.MINERAL), "MINERAL");
        }
        
    }

    /** Display result of a list, but only 3 result per line. */
    private void displayFormatedList(List<Coord> coords, String message) {
        System.out.println(message);

        for (int i = 0; i < coords.size(); i++) {

            if (i % 3 == 0) {
                System.out.println();
            }

            /* Each science entry will take 15 "spaces" */
            System.out.printf("%-15s", coords.get(i).toCommandCenterFormat());
        }
        System.out.println("\nTOTAL: " + coords.size());

        System.out.println("---------------------------------");
    }

    /** Start the Command Center's server. Accept Science from all ROVERS and
     * rebroadcast ungathered Science to selected ROVERS */
    public void startServer() throws IOException {

        /* Start the Server Socket */
        listenSocket = new ServerSocket(port);
        System.out.println(name + " Activated!");

        /* Start a thread. In this thread, will continuously rebroadcast all
         * ungathered science to ROVERS in a infinite loop */
        new Thread(new RebroadcastHandler()).start();
        System.out.println("REBROACAST Module Activated");

        while (true) {

            /* Wait for a connection */
            Socket connectionSocket = listenSocket.accept();
            System.out.println("Connection: " + connectionSocket.getInetAddress().toString());

            /* Serve the client on a separated Thread */
            new Thread(new ClientHandler(connectionSocket)).start();
        }
    }

    /** At the "broadcast rate", rebroadcast all ungathered science to the
     * Groups in "broadcast group list". */
    class RebroadcastHandler implements Runnable {

        @Override
        public void run() {
            while (true) {

                /* Broadcast the coordinate of all science that still need to be
                 * GATHERED */
                synchronized (ungatheredScience) {
                    for (Coord c : ungatheredScience) {
                        sender.shareScience(broadcastGroups, c);
                    }
                }

                try {

                    /* Wait for a while till next broadcast */
                    Thread.sleep(timeWaitTillNextBroadcast);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ClientHandler implements Runnable {

        Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            /* Whenever a ROVER discovered SCIENCE they will write a message.
             * That message will be received here. The science will be added
             * into a master list. After the science is added, display the
             * updated result onto the console. */
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                try {

                    String line = reader.readLine();
                    String[] result = line.split(" ");

                    if (result.length == 5 && result[4].equals("GATHERED")) {
                        Terrain terrain = Terrain.valueOf(result[0]);
                        Science science = Science.valueOf(result[1]);
                        int xpos = Integer.valueOf(result[2]);
                        int ypos = Integer.valueOf(result[3]);

                        Coord coord = new Coord(terrain, science, xpos, ypos);
                        System.out.println("Ungathered List: " + ungatheredScience.size());
                        removeCoord(ungatheredScience, coord);
                        System.out.println(
                                "Ungathered List after removeal: " + ungatheredScience.size());
                    } else if (result.length == 4) {
                        Terrain terrain = Terrain.valueOf(result[0]);
                        Science science = Science.valueOf(result[1]);
                        int xpos = Integer.valueOf(result[2]);
                        int ypos = Integer.valueOf(result[3]);
                        Coord coord = new Coord(terrain, science, xpos, ypos);

                        /* Add Science to the list if it is not already there */
                        addCoordToLists(coord);
                        
                        /* Display result onto the console */
                        displayResult(allScience);

                    }
                } catch (SocketException e) {
                    System.out.println("Connection Dropped");
                } finally {
                    if (reader != null) {
                        reader.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /** "Synchronized" removal of Science Coordinate */
        private void removeCoord(List<Coord> list, Coord coord) {

            synchronized (list) {
                list.remove(coord);
            }
        }

        /** "Synchronized" addition of Science Coordinate */
        private void addCoordToLists(Coord coord) {
            synchronized (allScience) {
                if (!allScience.contains(coord))
                    allScience.add(coord);
            }

            synchronized (ungatheredScience) {
                if (!ungatheredScience.add(coord))
                    ungatheredScience.add(coord);
            }
        }
    }
}
