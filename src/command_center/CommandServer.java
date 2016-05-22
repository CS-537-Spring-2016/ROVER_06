package command_center;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import common.Coord;
import communication.Group;
import enums.Science;
import enums.Terrain;

public class CommandServer {

    public ServerSocket listenSocket;
    public int port;
    public String name;

    /** A list of discovered Radioactive Science */
    List<Coord> rList = new ArrayList<Coord>();

    /** A list of discovered Organic Science */
    List<Coord> oList = new ArrayList<Coord>();

    /** A list of discovered Mineral Science */
    List<Coord> mList = new ArrayList<Coord>();

    /** A list of discovered Crystal Science */
    List<Coord> cList = new ArrayList<Coord>();

    /** A list of all the science that have been reported GATAHER */
    List<Coord> gatheredScience = new ArrayList<Coord>();

    /** A list of all the science that have not been gathered */
    List<Coord> ungatheredScience = new ArrayList<Coord>();

    public CommandServer(String name, int port) {
        this.port = port;
        this.name = name;
    }

    public void startServer() throws IOException {
        listenSocket = new ServerSocket(port);
        System.out.println(name + " Activated!");

        /* Send out a list of science that have not been reported GATHER to
         * GROUP 3 every X second */
        new Thread(new UpdateCarlos()).start();
        
        
        while (true) {

            Socket connectionSocket = listenSocket.accept();
            System.out.println(connectionSocket.getInetAddress() + " at PORT: "
                    + connectionSocket.getPort() + " connected");

            new Thread(new ClientHandler(connectionSocket)).start();
        }
    }

    class UpdateCarlos implements Runnable {

        @Override
        public void run() {
            while (true) {
                Socket socket = null;
                DataOutputStream dos = null;
                try {
                    socket = new Socket(Group.G3.getIp(), Group.G3.getPort());
                    dos = new DataOutputStream(socket.getOutputStream());

                    for (Coord c : ungatheredScience) {
                        if (c.terrain != Terrain.ROCK) {
                            dos.writeBytes(c.toProtocol() + "\n");
                            dos.flush();
                        }
                    }
                } catch (UnknownHostException e) {
                    System.out.println(e);
                    System.out.println("line 60-CommandServer");
                } catch (IOException e) {
                    System.out.println(e);
                    System.out.println("line 63-CommandServer");
                } finally {
                    if (dos != null)
                        try {
                            dos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    if (socket != null)
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
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
             * into their own list. After the science is added, display the
             * updated result onto the console. */
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                while (true) {
                    try {

                        String line = reader.readLine();
                        String[] result = line.split(" ");

                        if (result.length == 5 && result[4].equals("GATHER")) {
                            Terrain terrain = Terrain.valueOf(result[0]);
                            Science science = Science.valueOf(result[1]);
                            int xpos = Integer.valueOf(result[2]);
                            int ypos = Integer.valueOf(result[3]);

                            Coord coord = new Coord(terrain, science, xpos, ypos);
                            gatheredScience.add(coord);
                            ungatheredScience.remove(coord);
                            System.out.println("Master List Size: " + ungatheredScience.size());
                        } else if (result.length == 4) {
                            Terrain terrain = Terrain.valueOf(result[0]);
                            Science science = Science.valueOf(result[1]);
                            int xpos = Integer.valueOf(result[2]);
                            int ypos = Integer.valueOf(result[3]);
                            Coord coord = new Coord(terrain, science, xpos, ypos);

                            switch (result[1]) {
                            case "RADIOACTIVE":
                                updateList(rList, coord);
                                break;
                            case "ORGANIC":
                                updateList(oList, coord);
                                break;
                            case "MINERAL":
                                updateList(mList, coord);
                                break;
                            case "CRYSTAL":
                                updateList(cList, coord);
                                break;
                            }

                            /* Display result onto the console */
                            displayResult();
                        } else {

                        }
                    } catch (Exception e) {
                        System.out.println(e);
                    } finally {

                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        /** If COORD is new, add them to the list */
        private void updateList(List<Coord> list, Coord scienceCoord) {
            if (!list.contains(scienceCoord)) {
                list.add(scienceCoord);
                ungatheredScience.add(scienceCoord);
            }
        }

        /** Display all the science discovered onto the console */
        private void displayResult() {

            for (int i = 0; i < 5; i++) {
                System.out.println();
            }

            displayFormatedList(rList, "RADIOACTIVE");
            displayFormatedList(cList, "CRYSTAL");
            displayFormatedList(oList, "ORGANIC");
            displayFormatedList(mList, "MINERAL");
        }

        /** Display result of a list, but only 3 result per line. */
        private void displayFormatedList(List<Coord> coords, String message) {
            System.out.println("***********************");
            System.out.println(message);

            for (int i = 0; i < coords.size(); i++) {

                if (i % 3 == 0) {
                    System.out.println();
                }

                /* Each science entry will take 15 "spaces" */
                System.out.printf("%-15s", coords.get(i).toCommandCenterFormat());
            }
            System.out.println("\nTOTAL: " + coords.size());
        }

    }
}
