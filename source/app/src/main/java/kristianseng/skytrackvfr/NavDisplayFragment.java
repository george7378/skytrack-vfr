package kristianseng.skytrackvfr;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Observable;
import java.util.Observer;

import kristianseng.skytrackvfr.utility.Coords;
import kristianseng.skytrackvfr.utility.Global;

public class NavDisplayFragment extends Fragment implements Observer
{
    //region Fields

    private NavDisplayView _navDisplayView;
    private FloatingActionButton _fabNavInfo, _fabTrackUp, _fabCentreAircraft;
    private ImageView _imageViewRestoreNorthUp;

    //endregion

    //region Overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_nav_display, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        _navDisplayView = (NavDisplayView)getActivity().findViewById(R.id.navDisplayView);

        _fabNavInfo = (FloatingActionButton)getActivity().findViewById(R.id.fabNavInfo);
        _fabNavInfo.setVisibility(Global.ActiveRoute == null ? View.INVISIBLE : View.VISIBLE);
        _fabNavInfo.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                _navDisplayView.ShowLabels = !_navDisplayView.ShowLabels;
                _navDisplayView.invalidate();
            }
        });

        _fabTrackUp = (FloatingActionButton)getActivity().findViewById(R.id.fabTrackUp);
        _fabTrackUp.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Global.TrackUp.setValue(!Global.TrackUp.getValue());
            }
        });

        _fabCentreAircraft = (FloatingActionButton)getActivity().findViewById(R.id.fabCentreOnAircraft);
        _fabCentreAircraft.setImageResource(Global.LastAircraftHeading == null ? R.mipmap.ic_aircraft_location_noheading : R.mipmap.ic_aircraft_location);
        _fabCentreAircraft.setVisibility(Global.LastAircraftLocation.getValue() == null ? View.INVISIBLE : View.VISIBLE);
        _fabCentreAircraft.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                    Global.CentreOnAircraft.setValue(!Global.CentreOnAircraft.getValue());
            }
        });

        _imageViewRestoreNorthUp = (ImageView)getActivity().findViewById(R.id.imageViewRestoreNorthUp);
        _imageViewRestoreNorthUp.setRotation(-Global.MapRotation.getValue());
        _imageViewRestoreNorthUp.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (!Global.TrackUp.getValue())
                    Global.MapRotation.setValue(0.0f);
            }
        });
    }

    @Override
    public void onStart()
    {
        super.onStart();

        Global.TrackUp.addObserver(this);
        Global.CentreOnAircraft.addObserver(this);
        Global.LastAircraftLocation.addObserver(this);
        Global.MapRotation.addObserver(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        Global.CentreOnAircraft.setValue(false);

        Global.TrackUp.deleteObserver(this);
        Global.CentreOnAircraft.deleteObserver(this);
        Global.LastAircraftLocation.deleteObserver(this);
        Global.MapRotation.deleteObserver(this);
    }

    //endregion

    //region Observer implementation

    @Override
    public void update(Observable o, Object arg)
    {
        if (o == Global.LastAircraftLocation)
        {
            Coords aircraftLocation = Global.LastAircraftLocation.getValue();

            if (aircraftLocation != null)
            {
                _fabCentreAircraft.setImageResource(Global.LastAircraftHeading == null ? R.mipmap.ic_aircraft_location_noheading : R.mipmap.ic_aircraft_location);
                _fabCentreAircraft.setVisibility(View.VISIBLE);

                if (Global.CentreOnAircraft.getValue())
                {
                    Global.MapCentre.x = (float)aircraftLocation.Latitude;
                    Global.MapCentre.y = (float)aircraftLocation.Longitude;

                    if (Global.TrackUp.getValue() && Global.LastAircraftHeading != null)
                        Global.MapRotation.setValue(Global.LastAircraftHeading);
                }
            }
            else
            {
                _fabCentreAircraft.setVisibility(View.INVISIBLE);

                Global.CentreOnAircraft.setValue(false);
            }

            _navDisplayView.invalidate();
        }
        else if (o == Global.CentreOnAircraft)
        {
            Coords aircraftLocation = Global.LastAircraftLocation.getValue();

            if (Global.CentreOnAircraft.getValue() && aircraftLocation != null)
            {
                    Global.MapCentre.x = (float)aircraftLocation.Latitude;
                    Global.MapCentre.y = (float)aircraftLocation.Longitude;

                    _fabCentreAircraft.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorFloatingButtonBackgroundActive));

                    _fabTrackUp.setVisibility(View.VISIBLE);

                    _navDisplayView.invalidate();
            }
            else
            {
                _fabCentreAircraft.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorPrimary));

                _fabTrackUp.setVisibility(View.INVISIBLE);

                Global.TrackUp.setValue(false);
            }
        }
        else if (o == Global.TrackUp)
        {
            if (Global.TrackUp.getValue())
            {
                if (Global.LastAircraftHeading != null)
                {
                    Global.MapRotation.setValue(Global.LastAircraftHeading);
                }

                _fabTrackUp.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorFloatingButtonBackgroundActive));
            }
            else
                _fabTrackUp.setBackgroundTintList(ContextCompat.getColorStateList(getActivity(), R.color.colorPrimary));
        }
        else if (o == Global.MapRotation)
        {
            _imageViewRestoreNorthUp.setRotation(-Global.MapRotation.getValue());

            _navDisplayView.invalidate();
        }
    }

    //endregion
}
