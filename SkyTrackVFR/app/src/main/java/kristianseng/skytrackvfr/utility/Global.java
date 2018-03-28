package kristianseng.skytrackvfr.utility;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.support.v7.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;

public abstract class Global
{
    //region Enums

    public enum Orientation
    {
        None, Portrait, Landscape, ReversePortrait, ReverseLandscape
    }

    //endregion

    //region Fields

    private static final Gson _gson = new Gson();

    private static final String _waypointsFilename = "prefsWaypoints";
    private static final String _routesFilename = "prefsRoutes";

    //endregion

    //region Members

    public static final int EarthRadius = 6371008;
    public static final float MetresToNauticalMiles = 0.000539957f;

    public static final ObservableValue<Boolean> TrackUp = new ObservableValue<>(false);
    public static final ObservableValue<Boolean> CentreOnAircraft = new ObservableValue<>(false);
    public static Float LastAircraftHeading;
    public static final ObservableValue<Coords> LastAircraftLocation = new ObservableValue<>();

    public static final ArrayList<Waypoint> AllWaypoints = new ArrayList<>();
    public static final ArrayList<Route> AllRoutes = new ArrayList<>();
    public static ActiveRoute ActiveRoute;

    public static PointF MapCentre = new PointF(0, 0);
    public static float MapScaleFactor;
    public static final ObservableValue<Float> MapRotation = new ObservableValue<>(0.0f);

    public static short LocationUpdateRate;
    public static short CurveResolution;
    public static float SymbolScale;
    public static Orientation OrientationLock;
    public static boolean PreventSleep;

    //endregion

    //region Public methods

    public static void saveWaypointsToFile(Context context)
    {
        SharedPreferences savedWaypoints = context.getSharedPreferences(_waypointsFilename, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedWaypoints.edit();

        editor.clear();

        for (Waypoint waypoint : AllWaypoints)
        {
            String serialisedWaypoint;
            try
            {
                serialisedWaypoint = _gson.toJson(waypoint);
            }
            catch (Exception e)
            {
                continue;
            }

            editor.putString(waypoint.Name, serialisedWaypoint);
        }

        editor.commit();
    }

    public static void loadWaypointsFromFile(Context context)
    {
        SharedPreferences savedWaypoints = context.getSharedPreferences(_waypointsFilename, MODE_PRIVATE);
        Map<String, ?> allSavedWaypoints = savedWaypoints.getAll();

        AllWaypoints.clear();

        for (String waypointKey : allSavedWaypoints.keySet())
        {
            String waypointItem;
            try
            {
                waypointItem = (String)allSavedWaypoints.get(waypointKey);
            }
            catch (Exception e)
            {
                continue;
            }

            Waypoint parsedWaypoint;
            try
            {
                parsedWaypoint = _gson.fromJson(waypointItem, Waypoint.class);
            }
            catch (Exception e)
            {
                continue;
            }

            AllWaypoints.add(parsedWaypoint);
        }
    }

    public static void saveRoutesToFile(Context context)
    {
        SharedPreferences savedRoutes = context.getSharedPreferences(_routesFilename, MODE_PRIVATE);
        SharedPreferences.Editor editor = savedRoutes.edit();

        editor.clear();

        for (Route route : AllRoutes)
        {
            String serialisedRoute;
            try
            {
                serialisedRoute = _gson.toJson(route);
            }
            catch (Exception e)
            {
                continue;
            }

            editor.putString(route.Name, serialisedRoute);
        }

        editor.commit();
    }

    public static void loadRoutesFromFile(Context context)
    {
        SharedPreferences savedRoutes = context.getSharedPreferences(_routesFilename, MODE_PRIVATE);
        Map<String, ?> allSavedRoutes = savedRoutes.getAll();

        AllRoutes.clear();

        for (String routeKey : allSavedRoutes.keySet())
        {
            String routeItem;
            try
            {
                routeItem = (String)allSavedRoutes.get(routeKey);
            }
            catch (Exception e)
            {
                continue;
            }

            Route parsedRoute;
            try
            {
                parsedRoute = _gson.fromJson(routeItem, Route.class);
            }
            catch (Exception e)
            {
                continue;
            }

            AllRoutes.add(parsedRoute);
        }
    }

    public static void saveActiveRouteToFile(Context context)
    {
        SharedPreferences savedSettings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = savedSettings.edit();

        String serialisedActiveRoute;
        try
        {
            serialisedActiveRoute = _gson.toJson(ActiveRoute);
        }
        catch (Exception e)
        {
            return;
        }

        editor.putString("pref_active_route", serialisedActiveRoute);
        editor.commit();
    }

    public static void loadActiveRouteFromFile(Context context)
    {
        SharedPreferences savedSettings = PreferenceManager.getDefaultSharedPreferences(context);

        String activeRouteItem;
        try
        {
            activeRouteItem = savedSettings.getString("pref_active_route", null);
        }
        catch (Exception e)
        {
            return;
        }

        ActiveRoute parsedActiveRoute;
        try
        {
            parsedActiveRoute = _gson.fromJson(activeRouteItem, ActiveRoute.class);
        }
        catch (Exception e)
        {
            return;
        }

        ActiveRoute = parsedActiveRoute;
    }

    public static void saveMiscInfoToFile(Context context)
    {
        SharedPreferences savedSettings = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = savedSettings.edit();

        editor.putFloat("pref_map_centre_x", MapCentre.x);
        editor.putFloat("pref_map_centre_y", MapCentre.y);
        editor.putFloat("pref_map_scale_factor", MapScaleFactor);
        editor.putFloat("pref_map_rotation", MapRotation.getValue());
        editor.commit();
    }

    public static void loadMiscInfoFromFile(Context context)
    {
        SharedPreferences savedSettings = PreferenceManager.getDefaultSharedPreferences(context);

        MapCentre.x = savedSettings.getFloat("pref_map_centre_x", 0);
        MapCentre.y = savedSettings.getFloat("pref_map_centre_y", 0);
        MapScaleFactor = savedSettings.getFloat("pref_map_scale_factor", 100);
        MapRotation.setValue(savedSettings.getFloat("pref_map_rotation", 0));
    }

    public static void loadSettingsFromFile(Context context)
    {
        SharedPreferences savedSettings = PreferenceManager.getDefaultSharedPreferences(context);

        LocationUpdateRate = Short.parseShort(savedSettings.getString("pref_location_update_rate", "5"));
        CurveResolution = Short.parseShort(savedSettings.getString("pref_curve_resolution", "10"));
        SymbolScale = Float.parseFloat(savedSettings.getString("pref_symbol_scale", "0.75"));
        OrientationLock = Orientation.valueOf(savedSettings.getString("pref_orientation_lock", "Portrait"));
        PreventSleep = savedSettings.getBoolean("pref_prevent_sleep", true);
    }

    public static double calculateGreatCircleAngle(Coords start, Coords end)
    {
        double deltaLat = start.LatitudeRad() - end.LatitudeRad();
        double deltaLon = start.LongitudeRad() - end.LongitudeRad();

        double z = Math.pow(Math.sin(deltaLat/2), 2) + Math.cos(start.LatitudeRad())*Math.cos(end.LatitudeRad())*Math.pow(Math.sin(deltaLon/2), 2);

        return 2*Math.asin(Math.sqrt(z));
    }

    public static void showToast(Context context, String message)
    {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.TOP, 0, 20);
        toast.show();
    }

    public static void showAlertMessage(Context context, String title, String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setTitle(title);
        builder.setMessage(message);
        builder.setNeutralButton("OK", null);

        AlertDialog alert = builder.create();
        alert.show();
    }

    public static void hideKeyboard(Context context)
    {
        InputMethodManager inputManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);

        View v = ((Activity)context).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    //endregion
}
