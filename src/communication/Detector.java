package communication;

import java.util.Arrays;
import java.util.List;

import common.Coord;
import common.ScanMap;
import enums.Science;

/** Used to detect stuff on the ROVER'S scanned map.
 * 
 * @author Shay */
public interface Detector {

    /** A list of all the detectable sciences: Crystal, Mineral, Organic,
     * Radioactive. */
    List<Science> DETECTABLE_SCIENCES = Arrays.asList(Science.CRYSTAL, Science.MINERAL,
            Science.ORGANIC, Science.RADIOACTIVE);

    /** @param scanMap
     *            A scannedMap object, map of tiles surronding the ROVER
     * @param roverCoord
     *            Rover current coordinates.
     * @return A list of all the science detected in the scanned map. */
    List<Coord> detectScience(ScanMap scanMap, Coord roverLOC);
}
