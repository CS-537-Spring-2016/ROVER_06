package test.com.capitals;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.UnknownHostException;

/*
 * This program connects to a Capital Server, ask the users for an input,
 * then transfer this input to a server. The sever will then
 * return a capitalize version of the string input.
 * 
 * This is a socket test. As mentioned in CaptailServer,
 * our group currently have 0 experience with socket programming,
 * this program seeks to familiarize everybody with it. Once we are
 * familiarized with it, we can use what we learned here( hopefully )
 * to implement our communication module which we expect to be something
 * similar to this.
 */
public class CapitalClient {

    private String host;
    private int port;
    private Socket socket;
    private DataOutputStream outToServer;
    private BufferedReader inFromServer;
    private BufferedReader inFromUser;

    public CapitalClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() {

        try {
            socket = new Socket(host, port);
            System.out.println("CLIENT: connected");

            /*
             * Initialize I/O: [ read user input, read server input, write
             * output ]
             */
            inFromUser = new BufferedReader(new InputStreamReader(System.in));
            inFromServer = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            outToServer = new DataOutputStream(socket.getOutputStream());

            while (true) {
                // request String from user
                System.out.print("Enter a String: ");
                String userInput = inFromUser.readLine();

                // write to socket (in bytes)
                // add "\n" so when server call readLine(), it knows when to end
                outToServer.writeBytes(userInput + "\n");
                System.out.println("CLIENT: sending message to server ...");

                // get input from server and display it
                userInput = inFromServer.readLine();
                System.out.println("FROM SERVER: " + userInput);
            }

        } catch (

        UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // run the client
    public static void main(String[] args) {
        CapitalClient client = new CapitalClient("localhost", 8000);
        client.run();

    }

}
