package test.com.capitals;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * This program will start a socket server.
 * When a client connect, and input a string, it will
 * return that string capitalized.
 * 
 * This is use for testing and building up some
 * experience with socket programming since our group
 * have no experience with it. 
 * 
 * Once our capital server/client programs works as intended,
 * we can implement a similar feature with Rover_6. 
 */
public class CapitalServer {

    private ServerSocket serverSocket;
    private Socket socket;
    private int port;
    private int clientNo;

    public CapitalServer(int port) {
        this.port = port;
        this.clientNo = 0;
    }

    /**
     * server will accept client's string input, then return a capitalize
     * version of it back to the user.
     */
    public void run() {

        // create socket server. will listen for clients
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("SERVER: ready");

            // wait for connection
            while (!serverSocket.isClosed()) {

                socket = serverSocket.accept();

                // keep track of who is connected
                clientNo++;

                // retrieve client's host name and IP address
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println("SERVER: Client NO: " + clientNo + ": "
                        + inetAddress.toString());

                // create and start a new thread for the connection
                new Thread(new ClientThread(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Multiple clients can connect to the sever and be serve at the same time
     * 
     * As our rover communication module would required to talk to 8 other
     * teams, we need to test basic communication over multiple clients at the
     * same time.
     * 
     * When a client connects, a thread will be created and serve that client
     * continuously
     * 
     * the threads run independently of one another communicate with the
     * clients.
     */
    public class ClientThread implements Runnable {

        private Socket socket;
        private DataOutputStream outToClient;
        private BufferedReader inFromClient;

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                // initialize I/O to communicate with clients
                inFromClient = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                outToClient = new DataOutputStream(socket.getOutputStream());

                // serve this client forever until they disconnect
                while (true) {

                    // get input
                    String clientInput = inFromClient.readLine();
                    System.out.println("SERVER: input = " + clientInput);

                    // capitalize string and return output as bytes
                    // add "\n" so readLine() knows when to "stop".
                    outToClient.writeBytes(clientInput.toUpperCase() + "\n");
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void main(String[] args) {
        CapitalServer server = new CapitalServer(8000);
        server.run();
    }
}
