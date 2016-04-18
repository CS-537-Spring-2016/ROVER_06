package test.communication.updates;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;

/*
 * This is a test. In this test, the server continuously
 * write a message to all its clients at the same time. It will 
 * wait, then write a message, then wait, etc etc.
 * 
 * This program is a simulation of what the rover communication module.
 * We expect our rover to continuously update all the other rover about its
 * location and the map. But before we can begin writing for it, we should
 * familiarize ourself with a simple program first.
 * 
 * This program/test seek to build on what we learned on our capital client/server.
 * That is: socket programming, threading, and IO streams.
 */
public class UpdateServer {

    private ServerSocket serverSocket;
    private Socket socket;
    private int port;
    private HashSet<DataOutputStream> writers;
    private int clientNo;
    private static int count;

    public UpdateServer(int port) {
        this.port = port;
        clientNo = 0;
        writers = new HashSet<DataOutputStream>();
        count = 0;
    }

    public void run() {

        // create socket server. will listen for clients
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("UPDATE SERVER: ready");

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
     * Multiple clients can connect to the sever and be served at the same time
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

        public ClientThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                // initialize I/O to communicate with clients;
                outToClient = new DataOutputStream(socket.getOutputStream());

                /*
                 * add sockets to data structure so we can communicate to all of
                 * them together
                 */
                writers.add(outToClient);

                while (true) {

                    // write a msg to all the clients
                    for (DataOutputStream element : writers) {
                        element.writeBytes(count + " hello world\n");

                        /*
                         * keep track of the total number of message the server
                         * writes for testing purpose
                         */
                        count++;
                        Thread.sleep(1000);
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if(!socket.isClosed()) {
                    try {
                    socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } 
            }
        }

    }

    public static void main(String[] args) {
        UpdateServer server = new UpdateServer(9000);
        server.run();
    }

}
