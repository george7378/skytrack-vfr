package kristianseng.skytrackvfr.utility;

import java.util.ArrayList;

public class Route implements Comparable<Route>
{
    //region Members

    public String Name;

    public ArrayList<String> WaypointNames;

    //endregion

    //region Constructors

    public Route(String name, ArrayList<String> waypointNames)
    {
        Name = name;
        WaypointNames = waypointNames;
    }

    //endregion

    //region Overrides

    @Override
    public String toString()
    {
        return Name;
    }

    //endregion

    //region Implementation of Comparable

    @Override
    public int compareTo(Route o)
    {
        return Name.compareToIgnoreCase(o.Name);
    }

    //endregion
}