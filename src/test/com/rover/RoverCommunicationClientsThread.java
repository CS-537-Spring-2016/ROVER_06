package test.com.rover;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

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
