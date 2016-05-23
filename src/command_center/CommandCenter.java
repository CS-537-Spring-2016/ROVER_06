package command_center;

import java.io.IOException;

import communication.Group;

public class CommandCenter {
    
    public final static int PORT = 53799;
    public final static String NAME = "Blue Command Center";
    public CommandServer commandServer;
    public final static int TIME_TILL_NEXT_BROADCAST = 5000;
    
    
    public CommandCenter(String name, int port) {
        commandServer = new CommandServer(name, port);
    }
    
    public static void main(String[] args) throws IOException {
        
        CommandServer commandCenter = new CommandServer(NAME, PORT);
        commandCenter.addToBroadcastGroup(Group.G3);
        commandCenter.setTimeWaitTillNextBroadcast(TIME_TILL_NEXT_BROADCAST);
        commandCenter.startServer();
    }
}
