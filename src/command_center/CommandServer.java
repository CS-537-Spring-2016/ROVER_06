package command_center;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import common.Coord;
import enums.Science;

public class CommandServer {

    public ServerSocket listenSocket;
    public int port;
    public String name;

    public Map<Science, List<Coord>> scienceCoordMap;
    int rCount = 0;
    int oCount = 0;
    int mCount = 0;
    int cCount = 0;

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

                    switch (result[1]) {
                    case "RADIOACTIVE":
                        rCount++;
                        break;
                    case "ORGANIC":
                        oCount++;
                        break;
                    case "MINERAL":
                        mCount++;
                        break;
                    case "CRYSTAL":
                        cCount++;
                        break;
                    }
                    
                    System.out.println("R: " + rCount);
                    System.out.println("O: " + oCount);
                    System.out.println("M: " + mCount);
                    System.out.println("C: " + cCount);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
