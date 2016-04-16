package test.communication.capital;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
    private DataOutputStream outToClient;
    private BufferedReader inFromClient;
    private Socket socket;
    private int port;
    
    public CapitalServer(int port) {
        this.port = port;
    }

    /**
     * server will accept client's string input, then return a capitalize
     * version of it back to the user.
     */
    public void run() {
        
        String clientInput;
        
        // create socket server. will listen for clients
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("SERVER: ready");

            // wait for connection
            while (!serverSocket.isClosed()) {
                socket = serverSocket.accept();
                System.out.println("SERVER: " + socket.getRemoteSocketAddress() + " connected");
                
                // initialize I/O to communicate with clients
                inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outToClient = new DataOutputStream(socket.getOutputStream());
                
                // get input
                clientInput = inFromClient.readLine();
                System.out.println("SERVER: input = " + clientInput);
                
                // capitalize string and return output as bytes
                // add "\n" so readLine() knows when to "stop". 
                outToClient.writeBytes(clientInput.toUpperCase() + "\n");
                
                socket.close();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        CapitalServer server = new CapitalServer(8000);
        server.run();
    }
}
