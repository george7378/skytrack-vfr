package kristianseng.skytrackvfr.utility;

public class Waypoint implements Comparable<Waypoint>
{
    //region Members

    public String Name;

    public Coords Coords;

    //endregion

    //region Constructors

    public Waypoint(String name, Coords coords)
    {
        Name = name;
        Coords = coords;
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
    public int compareTo(Waypoint o)
    {
        return Name.compareToIgnoreCase(o.Name);
    }

    //endregion
}