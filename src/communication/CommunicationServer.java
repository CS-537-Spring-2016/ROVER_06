package communication;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class CommunicationServer implements Runnable {

    public List<DataOutputStream> outputStreamList;
    public List<Group> groupList = new ArrayList<Group>();

    public CommunicationServer(List<Group> groupList) {
        this.groupList = groupList;
        this.outputStreamList = new ArrayList<DataOutputStream>();
    }

    @Override
    public void run() {
        for (Group g : groupList) {
            new Thread(() -> {

                Socket socket = null;

                do {
                    try {
                        socket = new Socket(g.ip, g.port);
                        outputStreamList.add(
                                new DataOutputStream(socket.getOutputStream()));
                    } catch (Exception e) {
                    }

                } while (socket == null);

            }).start();
        }

    }

    public void shareScience(String string) {

        for (DataOutputStream dos : outputStreamList) {
            try {
                dos.writeBytes(string + "\r\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

}
