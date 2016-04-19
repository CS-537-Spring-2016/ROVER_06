package test.com.updates;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/*
 * Intended to be executed by a thread.
 * Serves every clients that connects.
 */
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
