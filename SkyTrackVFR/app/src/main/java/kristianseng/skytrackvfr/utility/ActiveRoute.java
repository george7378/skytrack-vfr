package kristianseng.skytrackvfr.utility;

import java.util.ArrayList;

public class ActiveRoute
{
    //region Members

    public final String ParentRouteName;

    public final ArrayList<RouteLeg> Legs;

    //endregion

    //region Constructors

    public ActiveRoute(String parentRouteName)
    {
        ParentRouteName = parentRouteName;

        Legs = generateLegs();
    }

    //endregion

    //region Private methods

    private ArrayList<RouteLeg> generateLegs()
    {
        ArrayList<RouteLeg> results = new ArrayList<>();

        Route parentRoute = null;
        for (Route route : Global.AllRoutes)
        {
            if (route.Name.equals(ParentRouteName))
                parentRoute = route;
        }

        if (parentRoute != null)
        {
            ArrayList<Waypoint> parentRouteWaypoints = new ArrayList<>();
            for (String name : parentRoute.WaypointNames)
            {
                Waypoint matchingWaypoint = null;
                for (Waypoint waypoint : Global.AllWaypoints)
                {
                    if (waypoint.Name.equals(name))
                    {
                        matchingWaypoint = waypoint;
                        break;
                    }
                }

                parentRouteWaypoints.add(matchingWaypoint);
            }

            if (parentRouteWaypoints.indexOf(null) == -1)
            {
                for (int i = 0; i < parentRouteWaypoints.size() - 1; i++)
                {
                    results.add(new RouteLeg(parentRouteWaypoints.get(i), parentRouteWaypoints.get(i + 1)));
                }
            }
        }

        return results;
    }

    //endregion
}
