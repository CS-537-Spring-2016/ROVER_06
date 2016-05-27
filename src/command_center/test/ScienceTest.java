package command_center.test;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import communication.BlueCorp;

public class ScienceTest {

    class Handler implements Runnable {

        Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            BufferedReader br;
            try {
                br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                System.out.println(br.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public void start() {
        new Thread(() -> {

            System.out.println("THREAD STARTED...");

            try {
                ServerSocket listenSocket = new ServerSocket(53706);
                Socket connectionSocket = listenSocket.accept();
                new Thread(new Handler(connectionSocket)).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void main(String[] args) throws UnknownHostException, IOException {

        ScienceTest st = new ScienceTest();
        st.start();

        Socket socket;
        DataOutputStream dos;

        socket = new Socket(BlueCorp.COMMAND_CENTER.ip, BlueCorp.COMMAND_CENTER.port);
        dos = new DataOutputStream(socket.getOutputStream());
        dos.writeBytes("SCIENCE 23 15 TREADS G6\n");
        dos.flush();
        dos.close();
        socket.close();

        System.out.println("DONE");
    }

}
