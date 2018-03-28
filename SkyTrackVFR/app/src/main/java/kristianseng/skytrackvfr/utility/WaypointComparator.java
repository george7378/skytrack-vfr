package kristianseng.skytrackvfr.utility;

import java.util.Comparator;

public class WaypointComparator implements Comparator<Waypoint>
{
    @Override
    public int compare(Waypoint o1, Waypoint o2)
    {
        return o1.compareTo(o2);
    }
}