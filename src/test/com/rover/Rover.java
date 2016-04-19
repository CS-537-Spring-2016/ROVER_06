package test.com.rover;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * This is a striped down test version of our Rover.
 * The Rover is ONLY sending and receiving information to/from other Rovers
 * The Rover will perform the following task:
 *  - accept all client's connection
 *  - serve (write to) each client until they (or the Rover) disconnect
 *  - connects to all available clients
 *  - be served (read from) each clients
 * 
 * The Rover is going to communicate with other Rovers through a peer to peer network.
 * Thus each Rover is both a "Client" and a "Host".
 */
public class Rover {

    private int port;

    private String otherHost;
    private int otherPort;
    private String message;

    public Rover(int port, String otherHost, int otherPort, String message) {
        this.port = port;
        this.otherHost = otherHost;
        this.otherPort = otherPort;
        this.message = message;
    }

    public void start() throws IOException {
        // thread that will process and serve incoming clients
        RoverCommunicationHostThread roverCommunicationHost = new RoverCommunicationHostThread(port);
        new Thread(roverCommunicationHost).start();

        RoverCommunicationClientsThread clients = new RoverCommunicationClientsThread(
                otherHost, otherPort);
        
        new Thread(clients).start();
    }
    
    public class RoverClientThread implements Runnable {

        private Socket socket;
        private DataOutputStream outToClient;
        private long sleepTime = 1000L;

        public RoverClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                // initialize I/O to communicate with clients
                outToClient = new DataOutputStream(socket.getOutputStream());

                // print a message every second
                while (true) {
                    outToClient.writeBytes(message);
                    Thread.sleep(sleepTime);
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public class RoverCommunicationHostThread implements Runnable {

        private ServerSocket serverSocket;

        public RoverCommunicationHostThread(int port) {

            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server Socket created!");
                System.out.println("Waiting for connections ...");

            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }

      
        // This is an infinite loop.
        // It will wait for a client to connect.
        // Once a client connects, serve them on a seperate thread.
        @Override
        public void run() {
            while (true) {
                try {
                    // wait for a connection
                    Socket socket = serverSocket.accept();

                    // retrieve & display client's ipAddress
                    InetAddress inetAddress = socket.getInetAddress();
                    System.out.println("Connection: " + inetAddress.getHostAddress());

                    // create and start a new thread
                    // serve each client on thread
                    new Thread(new RoverClientThread(socket)).start();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public class RoverCommunicationClientsThread implements Runnable {

        private Socket socket;
        private String host;
        private int port;

        public RoverCommunicationClientsThread(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void run() {

            try {
                while (socket == null) {
                    try {
                        socket = new Socket(host, port);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        System.out.println("waiting for host ...");
                    }
                }
                System.out.println("Rover successfully connected to a host!");
                System.out.println("Waiting for a message from other Rovers ...");

                // initialize i/o
                BufferedReader inFromServer = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));

                while (true) {
                    // read and display message from other Rovers
                    String msg = inFromServer.readLine();
                    System.out.println(msg);
                }
            } catch (IOException ex2) {
                ex2.printStackTrace();
            }
        }
    }
}
