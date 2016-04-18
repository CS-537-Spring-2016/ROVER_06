package test.communication.updates;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

public class UpdateClient {

    private String host;
    private int port;
    private Socket socket;
    private BufferedReader inFromServer;
    private String clientName;

    public UpdateClient(String host, int port, String clientName) {
        this.host = host;
        this.port = port;
        this.clientName = clientName;
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
                String msg = inFromServer.readLine();
                if (msg != null) {
                    System.out.println("to " + clientName + ": "
                            + msg);
                }
            }

        } catch (

        UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
