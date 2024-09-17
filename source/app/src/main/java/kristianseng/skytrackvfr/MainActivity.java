package kristianseng.skytrackvfr;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import kristianseng.skytrackvfr.utility.Coords;
import kristianseng.skytrackvfr.utility.Global;

public class MainActivity extends FragmentActivity
{
    //region Enums

    private enum SelectedAppSection
    {
        NavDisplay, Waypoints, Routes, Settings
    }

    //endregion

    //region Fields

    private SelectedAppSection _selectedSection = SelectedAppSection.NavDisplay;

    private Button _buttonNavDisplay, _buttonWaypoints, _buttonRoutes, _buttonSettings;

    private LocationManager _locationManager;
    private LocationListener _locationListener;

    //endregion

    //region Overrides

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Global.loadSettingsFromFile(this);
        Global.loadMiscInfoFromFile(this);
        Global.loadWaypointsFromFile(this);
        Global.loadRoutesFromFile(this);
        Global.loadActiveRouteFromFile(this);

        applySettings();

        /*
        // TEST DATA
        Global.AllWaypoints.clear();
        Global.AllWaypoints.add(new Waypoint("M1/M25", new Coords(51.716095, -0.385135)));
        Global.AllWaypoints.add(new Waypoint("Princes Risborough", new Coords(51.727176, -0.830693)));
        Global.AllWaypoints.add(new Waypoint("Silverstone", new Coords(52.090771, -1.029521)));
        Global.AllWaypoints.add(new Waypoint("Wellesbourne", new Coords(52.192225, -1.612562)));
        Global.AllWaypoints.add(new Waypoint("Gloucester", new Coords(51.894167, -2.167222)));

        ArrayList<String> testWaypointNames = new ArrayList<>();
        for (int i = 0; i < Global.AllWaypoints.size() - 1; i++)
            testWaypointNames.add(Global.AllWaypoints.get(i).Name);

        Global.AllRoutes.clear();
        Global.AllRoutes.add(new Route("Test Route", testWaypointNames));
        Global.ActiveRoute = new ActiveRoute(Global.AllRoutes.get(0).Name);

        Global.saveWaypointsToFile(this);
        Global.saveRoutesToFile(this);
        Global.saveActiveRouteToFile(this);
        // TEST DATA
        */

        _buttonNavDisplay = (Button)findViewById(R.id.buttonNavDisplay);
        _buttonWaypoints = (Button)findViewById(R.id.buttonWaypoints);
        _buttonRoutes = (Button)findViewById(R.id.buttonRoutes);
        _buttonSettings = (Button)findViewById(R.id.buttonSettings);

        _locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        _locationListener = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                Coords newLocation = new Coords(location.getLatitude(), location.getLongitude());

                Coords prevLocation = Global.LastAircraftLocation.getValue();
                if (prevLocation != null)
                {
                    // Only want to include heading information if new position is sufficiently offset from previous
                    if (Math.sqrt(Math.pow(newLocation.Latitude - prevLocation.Latitude, 2) + Math.pow(newLocation.Longitude - prevLocation.Longitude, 2)) > 0.0001f)
                    {
                        /*
                        double newHeading = Math.toDegrees(Math.atan2(Math.sin(newLocation.LongitudeRad() - prevLocation.LongitudeRad())*Math.cos(newLocation.LatitudeRad()),
                                            Math.cos(prevLocation.LatitudeRad())*Math.sin(newLocation.LatitudeRad()) - Math.sin(prevLocation.LatitudeRad())*Math.cos(newLocation.LatitudeRad())
                                            *Math.cos(newLocation.LongitudeRad() - prevLocation.LongitudeRad())));
                        */
                        double newHeading = Math.toDegrees(Math.atan2(newLocation.Longitude - prevLocation.Longitude, newLocation.Latitude - prevLocation.Latitude));
                        Global.LastAircraftHeading = (float)((newHeading + 360) % 360);
                    }
                    else
                        Global.LastAircraftHeading = null;
                }

                Global.LastAircraftLocation.setValue(newLocation);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {
                if (status == LocationProvider.OUT_OF_SERVICE)
                {
                    Global.LastAircraftHeading = null;
                    Global.LastAircraftLocation.setValue(null);
                }
            }

            @Override
            public void onProviderEnabled(String provider)
            {

            }

            @Override
            public void onProviderDisabled(String provider)
            {
                Global.LastAircraftHeading = null;
                Global.LastAircraftLocation.setValue(null);
            }
        };

        ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 0);

        updateBottomBarAndFragment();
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        Global.saveMiscInfoToFile(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for (int result : grantResults)
        {
            if (result == PackageManager.PERMISSION_DENIED)
            {
                Global.showAlertMessage(this, getResources().getString(R.string.error), getResources().getString(R.string.permission_warning));
                return;
            }
        }

        try
        {
            _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Global.LocationUpdateRate*1000, 0, _locationListener);
        }
        catch (SecurityException se)
        {
            Global.showAlertMessage(this, getResources().getString(R.string.error), getResources().getString(R.string.gps_permission_problem));
        }
    }

    //endregion

    //region Public methods

    public void onButtonNavDisplayClick(View v)
    {
        _selectedSection = SelectedAppSection.NavDisplay;
        updateBottomBarAndFragment();
    }

    public void onButtonWaypointsClick(View v)
    {
        _selectedSection = SelectedAppSection.Waypoints;
        updateBottomBarAndFragment();
    }

    public void onButtonRoutesClick(View v)
    {
        _selectedSection = SelectedAppSection.Routes;
        updateBottomBarAndFragment();
    }

    public void onButtonSettingsClick(View v)
    {
        _selectedSection = SelectedAppSection.Settings;
        updateBottomBarAndFragment();
    }

    public void applySettings()
    {
        if (Global.PreventSleep)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        else
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        switch (Global.OrientationLock)
        {
            case Portrait:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;

            case Landscape:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;

            case ReversePortrait:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;

            case ReverseLandscape:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;

            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }

    //endregion

    //region Private methods

    private void updateBottomBarAndFragment()
    {
        _buttonNavDisplay.setBackgroundResource(_selectedSection == SelectedAppSection.NavDisplay ? R.color.colorAccent : R.color.colorPrimaryDark);
        _buttonWaypoints.setBackgroundResource(_selectedSection == SelectedAppSection.Waypoints ? R.color.colorAccent : R.color.colorPrimaryDark);
        _buttonRoutes.setBackgroundResource(_selectedSection == SelectedAppSection.Routes ? R.color.colorAccent : R.color.colorPrimaryDark);
        _buttonSettings.setBackgroundResource(_selectedSection == SelectedAppSection.Settings ? R.color.colorAccent : R.color.colorPrimaryDark);

        Fragment newFragment;
        if (_selectedSection == SelectedAppSection.NavDisplay)
        {
            newFragment = new NavDisplayFragment();
        }
        else if (_selectedSection == SelectedAppSection.Waypoints)
        {
            newFragment = new WaypointsFragment();
        }
        else if (_selectedSection == SelectedAppSection.Routes)
        {
            newFragment = new RoutesFragment();
        }
        else if (_selectedSection == SelectedAppSection.Settings)
        {
            newFragment = new PreferencesFragment();
        }
        else
        {
            newFragment = new Fragment();
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.mainFragmentHolder, newFragment).commit();
    }

    //endregion
}
