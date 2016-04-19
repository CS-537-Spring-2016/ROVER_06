package test.com.updates;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/*
 * On a seperate thread, serve each client (write a message to them)
 */
public class RoverClientThread implements Runnable {

    private Socket socket;
    private DataOutputStream outToClient;
    private long sleepTime = 1000L;

    public RoverClientThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            // initialize I/O to communicate with clients
            outToClient = new DataOutputStream(socket.getOutputStream());

            // print a message every second
            while (true) {
                outToClient.writeBytes("hello world\r\n");
                Thread.sleep(sleepTime);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
