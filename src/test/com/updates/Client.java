package test.com.updates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * This is a test.
 * The client connects to a server and the server will write a message.
 * The client then reads the message and print it on the console. 
 * 
 * This program simulate a Rover receiving an update continuously from other Rover.
 * All the rover will be getting the same updates.
 */
public class Client {

    private String host;
    private int port;
    private Socket socket;
    private BufferedReader inFromServer;
    private String clientName;

    public Client(String host, int port, String clientName) {
        this.host = host;
        this.port = port;
        this.clientName = clientName;
    }

    public void run() {

        try {
            socket = new Socket(host, port);
            System.out.println("UPDATE CLIENT: connected");

            /* Initialize I/O: [read server input ] */

            inFromServer = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            while (!socket.isClosed()) {
                /* display server output and print clients name to differentiate
                 * each rover for testing purpose */
                String msg = inFromServer.readLine();
                    System.out.println("to " + clientName + ": " + msg);
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
                inFromServer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
