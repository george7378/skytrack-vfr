package kristianseng.skytrackvfr.utility;

import java.util.Comparator;

public class RouteComparator implements Comparator<Route>
{
    @Override
    public int compare(Route o1, Route o2)
    {
        return o1.compareTo(o2);
    }
}