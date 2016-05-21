package command_center;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Coord;
import enums.Science;
import enums.Terrain;

public class CommandServer {

    public ServerSocket listenSocket;
    public int port;
    public String name;

    public Map<Science, List<Coord>> scienceCoordMap;
    List<Coord> rList = new ArrayList<Coord>();
    List<Coord> oList = new ArrayList<Coord>();
    List<Coord> mList = new ArrayList<Coord>();
    List<Coord> cList = new ArrayList<Coord>();

    public CommandServer(String name, int port) {
        this.port = port;
        this.name = name;

        scienceCoordMap = new HashMap<Science, List<Coord>>();
    }

    public void startServer() throws IOException {
        listenSocket = new ServerSocket(port);
        System.out.println(name + " Activated!");

        while (true) {

            Socket connectionSocket = listenSocket.accept();
            System.out.println(connectionSocket.getInetAddress() + " at PORT: "
                    + connectionSocket.getPort() + " connected");

            new Thread(new ClientHandler(connectionSocket)).start();
        }
    }

    class ClientHandler implements Runnable {

        Socket clientSocket;

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
        }

        @Override
        public void run() {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                while (true) {
                    String line = reader.readLine();
                    String[] result = line.split(" ");

                    Terrain terrain = Terrain.valueOf(result[0]);
                    Science science = Science.valueOf(result[1]);
                    int xpos = Integer.valueOf(result[2]);
                    int ypos = Integer.valueOf(result[3]);
                    Coord coord = new Coord(terrain, science, xpos, ypos);

                    switch (result[1]) {
                    case "RADIOACTIVE":
                        if (!rList.contains(coord))
                            rList.add(coord);
                        break;
                    case "ORGANIC":
                        if (!oList.contains(coord))
                            oList.add(coord);
                        break;
                    case "MINERAL":
                        if (!mList.contains(coord))
                            mList.add(coord);
                        break;
                    case "CRYSTAL":
                        if (!rList.contains(coord))
                            cList.add(coord);
                        break;
                    }

                    displayResult();
                }

            } catch (IOException e) {
               System.out.println("Connection Dropped");
            }
        }
    }

    public void displayResult() {
        
        for (int i = 0; i < 5; i++) {
            System.out.println();
        }
        
        displayFormatedList(rList, "RADIOACTIVE");
        displayFormatedList(cList, "CRYSTAL");
        displayFormatedList(oList, "ORGANIC");
        displayFormatedList(mList, "MINERAL");
    }

    public void displayFormatedList(List<Coord> coords, String message) {
        System.out.println("***********************");
        System.out.println(message);

        for (int i = 0; i < coords.size(); i++) {

            if (i % 3 == 0) {
                System.out.println();
            }

            System.out.printf("%-15s", coords.get(i).toCommandCenterFormat());
        }
        System.out.println("\nTOTAL: " + coords.size());
    }
}
