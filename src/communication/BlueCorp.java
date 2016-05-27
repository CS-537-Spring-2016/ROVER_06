package communication;

import java.util.Arrays;
import java.util.List;

import enums.RoverDriveType;
import enums.RoverToolType;

public enum BlueCorp {

    GROUP_01(
            "localhost",
            53701,
            RoverDriveType.WALKER,
            RoverToolType.DRILL,
            RoverToolType.SPECTRAL_SENSOR),
    GROUP_02(
            "localhost",
            53702,
            RoverDriveType.WALKER,
            RoverToolType.RADIATION_SENSOR,
            RoverToolType.CHEMICAL_SENSOR),
    GROUP_03(
            "localhost",
            53703,
            RoverDriveType.TREADS,
            RoverToolType.DRILL,
            RoverToolType.HARVESTER),
    GROUP_04(
            "localhost",
            53704,
            RoverDriveType.WALKER,
            RoverToolType.DRILL,
            RoverToolType.RADAR_SENSOR),
    GROUP_05(
            "localhost",
            53705,
            RoverDriveType.WHEELS,
            RoverToolType.RANGE_BOOSTER,
            RoverToolType.SPECTRAL_SENSOR),
    GROUP_06(
            "localhost",
            53706,
            RoverDriveType.WHEELS,
            RoverToolType.RANGE_BOOSTER,
            RoverToolType.RADIATION_SENSOR),
    GROUP_07(
            "localhost",
            53707,
            RoverDriveType.TREADS,
            RoverToolType.HARVESTER,
            RoverToolType.RADAR_SENSOR),
    GROUP_08(
            "localhost",
            53708,
            RoverDriveType.TREADS,
            RoverToolType.HARVESTER,
            RoverToolType.SPECTRAL_SENSOR),
    GROUP_09(
            "localhost",
            53709,
            RoverDriveType.WALKER,
            RoverToolType.DRILL,
            RoverToolType.CHEMICAL_SENSOR),
    COMMAND_CENTER(
            "localhost",
            53799,
            RoverDriveType.NONE,
            RoverToolType.NONE,
            RoverToolType.NONE);

    public final String ip;
    public final int port;
    public final RoverDriveType driveType;
    public final RoverToolType toolA;
    public final RoverToolType toolB;

    private BlueCorp(String ip, int port, RoverDriveType driveType, RoverToolType toolA,
            RoverToolType toolB) {
        this.ip = ip;
        this.port = port;
        this.driveType = driveType;
        this.toolA = toolA;
        this.toolB = toolB;
    }
    
    public RoverToolType[] getTools() {
        return new RoverToolType[] { toolA, toolB };
    }
    
   public static List<BlueCorp> getGatherers() {
       return Arrays.asList(
               BlueCorp.GROUP_01,
               BlueCorp.GROUP_03,
               BlueCorp.GROUP_04,
               BlueCorp.GROUP_07,
               BlueCorp.GROUP_08,
               BlueCorp.GROUP_09);
   }

}
