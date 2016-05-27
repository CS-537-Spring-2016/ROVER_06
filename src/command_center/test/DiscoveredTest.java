package command_center.test;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import communication.BlueCorp;

public class DiscoveredTest {
    
    public static void main(String[] args) throws UnknownHostException, IOException {
        
        Socket socket;
        DataOutputStream dos;
        
        
        socket = new Socket(BlueCorp.COMMAND_CENTER.ip, BlueCorp.COMMAND_CENTER.port);
        dos = new DataOutputStream(socket.getOutputStream());
        dos.writeBytes("DISCOVERED SOIL CRYSTAL 25 13\n");
        dos.flush();
        dos.close();
        socket.close();
        
        
        socket = new Socket(BlueCorp.COMMAND_CENTER.ip, BlueCorp.COMMAND_CENTER.port);
        dos = new DataOutputStream(socket.getOutputStream());
        dos.writeBytes("DISCOVERED SAND MINERAL 6 6\n");
        dos.flush();
        dos.close();
        socket.close();
        
        
        socket = new Socket(BlueCorp.COMMAND_CENTER.ip, BlueCorp.COMMAND_CENTER.port);  
        dos = new DataOutputStream(socket.getOutputStream());
        dos.writeBytes("DISCOVERED GRAVEL ORGANIC 50 50\n");
        dos.flush();
        dos.close();
        socket.close();
        
        System.out.println("DONE");
    }
}
