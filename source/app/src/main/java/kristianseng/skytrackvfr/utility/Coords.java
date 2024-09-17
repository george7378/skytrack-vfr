package kristianseng.skytrackvfr.utility;

import java.util.Locale;

public class Coords
{
    //region Fields

    private boolean _latitudeRadCalculated, _longitudeRadCalculated;

    private double _latitudeRad, _longitudeRad;

    //endregion

    //region Members

    public final double Latitude, Longitude;

    //endregion

    //region Constructors

    public Coords(double lat, double lon)
    {
        Latitude = lat;
        Longitude = lon;
    }

    //endregion

    //region Public methods

    public double LatitudeRad()
    {
        if (!_latitudeRadCalculated)
        {
            _latitudeRad = Math.toRadians(Latitude);
            _latitudeRadCalculated = true;
        }

        return _latitudeRad;
    }

    public double LongitudeRad()
    {
        if (!_longitudeRadCalculated)
        {
            _longitudeRad = Math.toRadians(Longitude);
            _longitudeRadCalculated = true;
        }

        return _longitudeRad;
    }

    //endregion

    //region Overrides

    @Override
    public String toString()
    {
        String latSuffix = Latitude >= 0 ? "N" : "S";
        String lonSuffix = Longitude >= 0 ? "E" : "W";

        return String.format(Locale.getDefault(), "%.4f %s\n%.4f %s", Math.abs(Latitude), latSuffix, Math.abs(Longitude), lonSuffix);
    }

    //endregion
}