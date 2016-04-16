package test.communication.updates;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class UpdateClient {

    private String host;
    private int port;
    private Socket socket;
    private BufferedReader inFromServer;

    public UpdateClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {

        try {
            socket = new Socket(host, port);
            System.out.println("UPDATE CLIENT: connected");

            /*
             * Initialize I/O: [read server input ]
             */
            inFromServer = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            while (true) {
                // display server output
                

            }

        } catch (

        UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // run the client
    public static void main(String[] args) {
        UpdateClient client = new UpdateClient("localhost", 9000);
        client.run();

    }

}
