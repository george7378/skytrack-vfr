package kristianseng.skytrackvfr.utility;

import java.util.ArrayList;

public class RouteLeg
{
    //region Fields

    private final double _greatCircleAngle;

    //endregion

    //region Members

    public final Waypoint StartPoint, EndPoint;

    public final double GreatCircleDistance;

    public final ArrayList<Coords> GreatCirclePoints;

    //endregion

    //region Constructors

    public RouteLeg(Waypoint start, Waypoint end)
    {
        StartPoint = start;
        EndPoint = end;

        _greatCircleAngle = Global.calculateGreatCircleAngle(StartPoint.Coords, EndPoint.Coords);
        GreatCircleDistance = Global.EarthRadius*_greatCircleAngle;

        GreatCirclePoints = generateGreatCirclePoints();
    }

    //endregion

    //region Private methods

    private Coords interpolateGreatCirclePoint(float f)
    {
        double A = Math.sin((1 - f) * _greatCircleAngle) / Math.sin(_greatCircleAngle);
        double B = Math.sin(f * _greatCircleAngle) / Math.sin(_greatCircleAngle);

        double x = A * Math.cos(StartPoint.Coords.LatitudeRad()) * Math.cos(StartPoint.Coords.LongitudeRad()) +
                   B * Math.cos(EndPoint.Coords.LatitudeRad()) * Math.cos(EndPoint.Coords.LongitudeRad());
        double y = A * Math.cos(StartPoint.Coords.LatitudeRad()) * Math.sin(StartPoint.Coords.LongitudeRad()) +
                   B * Math.cos(EndPoint.Coords.LatitudeRad()) * Math.sin(EndPoint.Coords.LongitudeRad());
        double z = A * Math.sin(StartPoint.Coords.LatitudeRad()) + B * Math.sin(EndPoint.Coords.LatitudeRad());

        double lat = Math.toDegrees(Math.atan2(z, Math.sqrt(x * x + y * y)));
        double lon = Math.toDegrees(Math.atan2(y, x));

        return new Coords(lat, lon);
    }

    private ArrayList<Coords> generateGreatCirclePoints()
    {
        ArrayList<Coords> results = new ArrayList<>();

        for (int i = 0; i <= Global.CurveResolution; i++)
        {
            float fraction = i / (float) Global.CurveResolution;
            results.add(interpolateGreatCirclePoint(fraction));
        }

        return results;
    }

    //endregion
}