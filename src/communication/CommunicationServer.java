package communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import common.RoverQueue;

public class CommunicationServer implements Runnable {

    /* after connected to a ROVER, create and save that ROVER output stream so
     * they can be contact later */
    public List<DataOutputStream> outputStreamList;

    /* A list of all the GROUP/ROVER's information (Group's Name, ROVER's IP,
     * ROVER's Port */
    public List<Group> groupList;
    private RoverQueue roverQueue;

    public CommunicationServer(List<Group> groupList) {
        this.groupList = groupList;
        this.outputStreamList = new ArrayList<DataOutputStream>();
    }

    @Override
    /** Connect to each ROVER's socket but after X attempts, will stop trying */
    public void run() {
        for (Group g : groupList) {
            /* start a new thread  for each group and try to connect to each GROUP's socket.
             * keep trying until a socket has created or if fail attempts exceed
             * max */
            new Thread(() -> {

                Socket socket = null;
                int createSocketAttempts = 0;
                final int MAX_CREATE_SOCKET_ATTEMPTS = 10;
                final int TIME_WAIT_TILL_NEXT_ATTEMPT = 1000;

                do {
                    try {
                        socket = new Socket(g.ip, g.port);
                        System.out.println("CONNECTED to: " + g.toString());
                        outputStreamList.add(new DataOutputStream(socket.getOutputStream()));
                    } catch (Exception e) {
                        try {
                            Thread.sleep(TIME_WAIT_TILL_NEXT_ATTEMPT);
                        } catch (Exception e1) {
                        }
                        createSocketAttempts++;
                    }
                } while (socket == null && createSocketAttempts < MAX_CREATE_SOCKET_ATTEMPTS);
            }).start();
        }
    }

    /** Write a message to all the connected ROVERS */
    public void writeToRovers(String string) {

        for (DataOutputStream dos : outputStreamList) {
            try {
                dos.writeBytes(string + "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public RoverQueue getQueue() {
        return roverQueue;
    }

}
