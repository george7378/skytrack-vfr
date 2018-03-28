package kristianseng.skytrackvfr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.Collections;

import kristianseng.skytrackvfr.utility.ActiveRoute;
import kristianseng.skytrackvfr.utility.Coords;
import kristianseng.skytrackvfr.utility.Global;
import kristianseng.skytrackvfr.utility.Route;
import kristianseng.skytrackvfr.utility.Waypoint;
import kristianseng.skytrackvfr.utility.WaypointComparator;

public class EditWaypointFragment extends Fragment
{
    //region Fields

    private boolean _addingNew;

    private ListView _waypointsListView;
    private EditText _editTextName, _editTextLatitude, _editTextLongitude;
    private Button _buttonDelete, _buttonSave;

    //endregion

    //region Overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_edit_waypoint, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final FrameLayout editorFragmentHolder = (FrameLayout)getActivity().findViewById(R.id.waypointEditorFragmentHolder);

        _waypointsListView = (ListView)getActivity().findViewById(R.id.listViewWaypoints);

        _addingNew = getArguments() == null;

        _editTextName = (EditText)getActivity().findViewById(R.id.editTextWaypointName);
        if (!_addingNew)
            _editTextName.setText(getArguments().getString("name"));

        _editTextLatitude = (EditText)getActivity().findViewById(R.id.editTextWaypointLatitude);
        _editTextLatitude.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s)
            {
                try
                {
                    Double latValue = Double.parseDouble(s.toString());
                    if (latValue > 90)
                        s.replace(0, s.length(), "90");
                    else if (latValue < -90)
                        s.replace(0, s.length(), "-90");
                }
                catch (Exception e) { }
            }
        });
        if (!_addingNew)
            _editTextLatitude.setText(Double.toString(getArguments().getDouble("lat")));

        _editTextLongitude = (EditText)getActivity().findViewById(R.id.editTextWaypointLongitude);
        _editTextLongitude.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void afterTextChanged(Editable s)
            {
                try
                {
                    Double lonValue = Double.parseDouble(s.toString());
                    if (lonValue > 180)
                        s.replace(0, s.length(), "180");
                    else if (lonValue < -180)
                        s.replace(0, s.length(), "-180");
                }
                catch (Exception e) { }
            }
        });
        if (!_addingNew)
            _editTextLongitude.setText(Double.toString(getArguments().getDouble("lon")));

        _buttonDelete = (Button)getActivity().findViewById(R.id.buttonDeleteWaypoint);
        _buttonDelete.setVisibility(_addingNew ? View.INVISIBLE : View.VISIBLE);
        _buttonDelete.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                for (Route route : Global.AllRoutes)
                {
                    for (String name : route.WaypointNames)
                    {
                        if (getArguments().getString("name").equals(name))
                        {
                            Global.showToast(getActivity(), "Waypoint is being used by route(s)");
                            return;
                        }
                    }
                }

                Global.AllWaypoints.remove(getArguments().getInt("index"));

                if (_waypointsListView != null)
                    ((ArrayAdapter)(_waypointsListView.getAdapter())).notifyDataSetChanged();

                Global.saveWaypointsToFile(getActivity());

                if (editorFragmentHolder != null)
                    getFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.waypointEditorFragmentHolder, new EmptyEditorFragment()).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, new WaypointsFragment()).commit();

                Global.showToast(getActivity(), "Waypoint deleted");
                Global.hideKeyboard(getActivity());
            }
        });

        _buttonSave = (Button)getActivity().findViewById(R.id.buttonSaveWaypoint);
        _buttonSave.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                String editorName = _editTextName.getText().toString();
                if (editorName.length() == 0)
                {
                    Global.showToast(getActivity(), "Enter a name");
                    return;
                }

                Double editorLatitude, editorLongitude;
                try
                {
                    editorLatitude = Double.parseDouble(_editTextLatitude.getText().toString());
                    editorLongitude = Double.parseDouble(_editTextLongitude.getText().toString());
                }
                catch (Exception e)
                {
                    Global.showToast(getActivity(), "Numeric fields must be filled");
                    return;
                }

                if (_addingNew)
                {
                    for (Waypoint waypoint : Global.AllWaypoints)
                    {
                        if (editorName.equals(waypoint.Name))
                        {
                            Global.showToast(getActivity(), "Waypoint name already exists");
                            return;
                        }
                    }

                    Global.AllWaypoints.add(new Waypoint(editorName, new Coords(editorLatitude, editorLongitude)));
                }
                else
                {
                    for (Waypoint waypoint : Global.AllWaypoints)
                    {
                        if (editorName.equals(waypoint.Name) && Global.AllWaypoints.indexOf(waypoint) != getArguments().getInt("index"))
                        {
                            Global.showToast(getActivity(), "Waypoint name already exists");
                            return;
                        }
                    }

                    Global.AllWaypoints.set(getArguments().getInt("index"), new Waypoint(editorName, new Coords(editorLatitude, editorLongitude)));

                    for (Route route : Global.AllRoutes)
                    {
                        for (int i = 0; i < route.WaypointNames.size(); i++)
                        {
                            if (route.WaypointNames.get(i).equals(getArguments().getString("name")))
                                route.WaypointNames.set(i, editorName);
                        }
                    }

                    if (Global.ActiveRoute != null)
                        Global.ActiveRoute = new ActiveRoute(Global.ActiveRoute.ParentRouteName);

                    Global.saveRoutesToFile(getActivity());
                    Global.saveActiveRouteToFile(getActivity());
                }

                Collections.sort(Global.AllWaypoints, new WaypointComparator());

                if (_waypointsListView != null)
                    ((ArrayAdapter)(_waypointsListView.getAdapter())).notifyDataSetChanged();

                Global.saveWaypointsToFile(getActivity());

                if (editorFragmentHolder != null)
                    getFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.waypointEditorFragmentHolder, new EmptyEditorFragment()).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, new WaypointsFragment()).commit();

                Global.showToast(getActivity(), _addingNew ? "Waypoint added" : "Waypoint updated");
                Global.hideKeyboard(getActivity());
            }
        });
    }

    //endregion
}
