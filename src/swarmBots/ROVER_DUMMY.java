package swarmBots;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ROVER_DUMMY {

    public static void main(String[] args) {

        try {
            ServerSocket serverSocket = new ServerSocket(53799);
            System.out.println("BLUE waiting...");

            Socket socket = serverSocket.accept();
            System.out.println("BLUE someone has connected");

            while (true) {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                System.out.println(br.readLine());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
