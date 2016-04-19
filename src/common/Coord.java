package common;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class Coord {

    public int xpos;
    public int ypos;

    @Override
    public String toString() {
        return "Coord [xpos=" + xpos + ", ypos=" + ypos + "]";
    }

    public Coord(int x, int y) {
        this.xpos = x;
        this.ypos = y;
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

        Coord other = (Coord) obj;
        return new EqualsBuilder().
        // if deriving: appendSuper(super.equals(obj)).
                append(xpos, other.xpos).append(ypos, other.ypos).isEquals();
    }

}
