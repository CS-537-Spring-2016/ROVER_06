package communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

import common.Coord;

/**
 * Communication module used to communicate with other Groups
 * @author Shay
 *
 */
public class RoverSender implements Sender{

    @Override
    public void shareScience(List<Group> groupList, Coord coord) {
        /* Use Thread so the ROVER can move and communicate with other ROVERS concurrently. 
         * Else the ROVER will stop, broadcast Science Coordinates, then resume moving */
        new Thread(() -> {
            /* Iterate through the "broadcast group list". For each element,
             * create a socket. If the socket is successfully, if the ROVER
             * is online, create a output stream and "rebroadcast" all the
             * ungathered science coordinates. After all the sciences have
             * been "rebroadcast", close the output stream. After all groups
             * have been served, sleep for "broadcast rate" then repeat the
             * loop again */
            for (Group g : groupList) {
                Socket socket;
                try {
                    socket = new Socket(g.getIp(), g.getPort());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                    dos.writeBytes(coord.toProtocol() + "\n");
                    dos.flush();
                    dos.close();
                } catch (ConnectException e) {
                    /* No Connection Exception. If the ROVER is not online, do
                     * nothing. Try again later */
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
