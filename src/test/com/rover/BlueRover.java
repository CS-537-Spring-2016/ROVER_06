package test.com.rover;

import java.io.IOException;

public class BlueRover {
    
    public static void main(String[] args) throws IOException {
        String message = "hello world\r\n";
        Rover blue = new Rover(9000, "localhost", 8000, message);
        blue.start();
    }
}
