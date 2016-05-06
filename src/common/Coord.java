package common;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import enums.Science;
import enums.Terrain;

public class Coord  implements Comparable<Coord>{
    // thanks to this posting
    // http://stackoverflow.com/questions/27581/what-issues-should-be-considered-when-overriding-equals-and-hashcode-in-java

    public final int xpos;
    public final int ypos;
    Terrain terrain;
    boolean hasRover;
    Science science;

    @Override
    public String toString() {
        return terrain + " " + science + " " + xpos + " " + ypos;
    }

    public Coord(int x, int y) {
        this.xpos = x;
        this.ypos = y;
    }

    public Coord(Terrain terrain, Science science, int x, int y) {
        this(x, y);
        this.science = science;
        this.terrain = terrain;
    }

    @Override
    public int hashCode() {
        // two randomly chosen prime numbers
        // if deriving: appendSuper(super.hashCode()).
        return new HashCodeBuilder(17, 31).append(xpos).append(ypos)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Coord))
            return false;
        if (obj == this)
            return true;

        Coord theOther = (Coord) obj;
        // return new EqualsBuilder().
        // // if deriving: appendSuper(super.equals(obj)).
        // append(xpos, theOther.xpos).
        // append(ypos, theOther.ypos).
        // isEquals();
        return ((this.xpos == theOther.xpos) && (this.ypos == theOther.ypos));
    }
    

    @Override
    public int compareTo(Coord o) {

        int result = Integer.compare(xpos, o.xpos);
        
        if (result == 0) {
            result = Integer.compare(ypos, o.ypos);
        }
        
        return result;
    } 

}
