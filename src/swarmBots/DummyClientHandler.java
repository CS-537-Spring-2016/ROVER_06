package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class DummyClientHandler implements Runnable {

    private Socket connectionSocket;

    public DummyClientHandler(Socket connectionSocket) {

        this.connectionSocket = connectionSocket;
    }

    @Override
    public void run() {
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connectionSocket.getInputStream()));

            while (true) {
                System.out.println(br.readLine());
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}
