package test.com.updates;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

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
public class Server {

    private ServerSocket serverSocket;
    private Socket socket;
    private int port;
    private int clientNo;
    private static int count;

    public Server(int port) {
        this.port = port;
        clientNo = 0;
        count = 0;
    }

    /* start the server */
    public void run() {

        /* create socket server. will listen for clients */
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("UPDATE SERVER: ready");

            /* wait for connection */
            while (!serverSocket.isClosed()) {

                socket = serverSocket.accept();

                /* keep track of who is connected */
                clientNo++;

                // retrieve client's host name and IP address
                InetAddress inetAddress = socket.getInetAddress();
                System.out.println("SERVER: Client NO: " + clientNo + ": "
                        + inetAddress.toString());

                /* create and start a new thread for the connection. each client
                 * thread will simply open a IO stream and then write a message
                 * to the client */
                new Thread(new ClientThread(socket)).start();

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /* Multiple clients can connect to the sever and be served at the same time
     * 
     * As our rover communication module would required to talk to 8 other
     * teams, we need to test basic communication over multiple clients at the
     * same time.
     * 
     * When a client connects, a thread will be created and serve that client
     * continuously
     * 
     * the threads run independently of one another communicate with the
     * clients. */
    public class ClientThread implements Runnable {

        private Socket clientSocket;
        private DataOutputStream outToClient;

        public ClientThread(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {

            try {
                /* initialize I/O to communicate with clients */
                outToClient = new DataOutputStream(
                        clientSocket.getOutputStream());

                while (true) {

                    outToClient.writeBytes(count + " hello world\n");

                    /* keep track of the total number of message the server
                     * writes for testing purpose */
                    count++;
                    Thread.sleep(1000);
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (!clientSocket.isClosed()) {
                    try {
                        clientSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public static void main(String[] args) {
        Server server = new Server(9000);
        server.run();
    }

}
