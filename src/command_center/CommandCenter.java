package command_center;

import java.io.IOException;

public class CommandCenter {
    
    public final static int PORT = 53799;
    public final static String NAME = "Blue Command Center";
    public CommandServer commandServer;
    
    
    public CommandCenter(String name, int port) {
        commandServer = new CommandServer(name, port);
    }
    
    public static void main(String[] args) throws IOException {
        
        CommandServer commandCenter = new CommandServer(NAME, PORT);
        commandCenter.startServer();
    }
}
