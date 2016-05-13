package communication;

import java.util.ArrayList;
import java.util.List;

public class Group {

    public String ip;
    public int port;
    public String name;

    public Group(String name, String ip, int port) {
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public String toString() {
        return name + " " + ip + " " + port;
    }

    public static List<Group> BLUE_CORP() {
        List<Group> blueCorp = new ArrayList<Group>();
        // dummy value # 1
        blueCorp.add(new Group("ROVER_DUMMY", "localhost", 53799));

        // blue rooster
        blueCorp.add(new Group("ROVER_01", "localhost", 53701));
        blueCorp.add(new Group("ROVER_02", "localhost", 53702));
        blueCorp.add(new Group("ROVER_03", "localhost", 53703));
        blueCorp.add(new Group("ROVER_04", "localhost", 53704));
        blueCorp.add(new Group("ROVER_05", "localhost", 53705));
        blueCorp.add(new Group("ROVER_07", "localhost", 53707));
        blueCorp.add(new Group("ROVER_08", "localhost", 53708));
        blueCorp.add(new Group("ROVER_09", "localhost", 53709));

        return blueCorp;
    }
}
