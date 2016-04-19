package test.com.updates;

import java.io.IOException;

/*
 * This is a striped down test version of our Rover.
 * The Rover is ONLY sending and recieving information to/from other Rovers
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
    private RoverCommunicationHostThread roverCommunicationHost;

    public Rover(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        // proccess incoming clients' connection
        roverCommunicationHost = new RoverCommunicationHostThread(port);
        new Thread(roverCommunicationHost).start();
        new Thread(new TimeCounterThread()).start();
    }

    public static void main(String[] args) throws IOException {
        Rover rover = new Rover(9000);
        rover.start();
    }

}
