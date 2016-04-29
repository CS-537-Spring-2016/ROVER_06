package test.com.updates;

/*
 * A test client.
 * 
 * It will connects to the UpdateServer, read the server message, and prints it out onto the console. 
 */
public class GreenClient {

    public static void main(String[] args) {
        Client client = new Client("localhost", 9000, "GREEN");
        client.run();
    }
}
