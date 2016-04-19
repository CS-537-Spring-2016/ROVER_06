package test.com.rover;

import java.io.IOException;

public class RedRover {
    
    public static void main(String[] args) throws IOException {
        String message = "Go Warriors\r\n";
        Rover red = new Rover(8000, "localhost", 9000, message);
        red.start();
    }
}
