package swarmBots;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/** used to for testing. run this to see if your rover is sending out
 * information tao others properly
 * 
 * @author Shay */
public class DummyRover {

    ServerSocket listenSocket;

    public void startServer() throws IOException {

        new Thread(() -> {

            try {
                System.out.println("Starting Server...");
                listenSocket = new ServerSocket(53799);
                System.out.println("Server Started");
                // wait for a rover to connect

                while (true) {
                    Socket connectionSocket = listenSocket.accept();
                    System.out.println("SOMEONE CONNECTS");
                    new Thread(new DummyClientHandler(connectionSocket)).start();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }).start();
    }

    public static void main(String[] args) throws IOException {
        new DummyRover().startServer();
    }

}
