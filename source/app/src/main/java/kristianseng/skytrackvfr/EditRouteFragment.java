package kristianseng.skytrackvfr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;

import kristianseng.skytrackvfr.utility.ActiveRoute;
import kristianseng.skytrackvfr.utility.Global;
import kristianseng.skytrackvfr.utility.Route;
import kristianseng.skytrackvfr.utility.RouteComparator;
import kristianseng.skytrackvfr.utility.Waypoint;

public class EditRouteFragment extends Fragment
{
    //region Fields

    private boolean _addingNew;
    private ArrayList<String> _routeWaypoints = new ArrayList<>();

    private ListView _routesListView, _routeWaypointsListView;
    private EditText _editTextName;
    private ImageButton _buttonAddWaypoint, _buttonDeleteWaypoint, _buttonUpWaypoint, _buttonDownWaypoint;
    private Button _buttonActivate, _buttonDelete, _buttonSave;

    //endregion

    //region Overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_edit_route, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final FrameLayout editorFragmentHolder = (FrameLayout)getActivity().findViewById(R.id.routeEditorFragmentHolder);

        _routesListView = (ListView)getActivity().findViewById(R.id.listViewRoutes);

        _addingNew = getArguments() == null;
        if (!_addingNew)
            _routeWaypoints.addAll(getArguments().getStringArrayList("waypoints"));

        _editTextName = (EditText)getActivity().findViewById(R.id.editTextRouteName);
        if (!_addingNew)
            _editTextName.setText(getArguments().getString("name"));

        _routeWaypointsListView = (ListView)getActivity().findViewById(R.id.listViewRouteWaypoints);
        _routeWaypointsListView.setEmptyView(getActivity().findViewById(R.id.emptyListRouteWaypoints));
        _routeWaypointsListView.setAdapter(new ArrayAdapter <>(getActivity(), android.R.layout.simple_list_item_activated_1, _routeWaypoints));

        _buttonAddWaypoint = (ImageButton)getActivity().findViewById(R.id.buttonAddRouteWaypoint);
        _buttonAddWaypoint.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("Add Waypoint");
                builder.setNegativeButton("Cancel", null);

                final ArrayList<String> unusedWaypointNames = new ArrayList<>();
                for (Waypoint waypoint : Global.AllWaypoints)
                {
                    if (!_routeWaypoints.contains(waypoint.Name))
                        unusedWaypointNames.add(waypoint.Name);
                }
                builder.setItems(unusedWaypointNames.toArray(new CharSequence[unusedWaypointNames.size()]), new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int item)
                    {
                        _routeWaypoints.add(unusedWaypointNames.get(item));

                        ((ArrayAdapter)(_routeWaypointsListView.getAdapter())).notifyDataSetChanged();
                    }
                });

                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        _buttonDeleteWaypoint = (ImageButton)getActivity().findViewById(R.id.buttonDeleteRouteWaypoint);
        _buttonDeleteWaypoint.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                int waypointIndex = _routeWaypointsListView.getCheckedItemPosition();

                if (waypointIndex != -1)
                {
                    _routeWaypoints.remove(waypointIndex);

                    _routeWaypointsListView.clearChoices();

                    ((ArrayAdapter)(_routeWaypointsListView.getAdapter())).notifyDataSetChanged();
                }
            }
        });

        _buttonUpWaypoint = (ImageButton)getActivity().findViewById(R.id.buttonUpRouteWaypoint);
        _buttonUpWaypoint.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                int waypointIndex = _routeWaypointsListView.getCheckedItemPosition();

                if (waypointIndex > 0)
                {
                    String waypointToSwapWith = _routeWaypoints.get(waypointIndex - 1);
                    _routeWaypoints.set(waypointIndex - 1, _routeWaypoints.get(waypointIndex));
                    _routeWaypoints.set(waypointIndex, waypointToSwapWith);

                    _routeWaypointsListView.clearChoices();
                    _routeWaypointsListView.setItemChecked(waypointIndex - 1, true);

                    ((ArrayAdapter)(_routeWaypointsListView.getAdapter())).notifyDataSetChanged();
                }
            }
        });

        _buttonDownWaypoint = (ImageButton)getActivity().findViewById(R.id.buttonDownRouteWaypoint);
        _buttonDownWaypoint.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                int waypointIndex = _routeWaypointsListView.getCheckedItemPosition();

                if (waypointIndex != -1 && waypointIndex < _routeWaypoints.size() - 1)
                {
                    String waypointToSwapWith = _routeWaypoints.get(waypointIndex + 1);
                    _routeWaypoints.set(waypointIndex + 1, _routeWaypoints.get(waypointIndex));
                    _routeWaypoints.set(waypointIndex, waypointToSwapWith);

                    _routeWaypointsListView.clearChoices();
                    _routeWaypointsListView.setItemChecked(waypointIndex + 1, true);

                    ((ArrayAdapter)(_routeWaypointsListView.getAdapter())).notifyDataSetChanged();
                }
            }
        });

        _buttonActivate = (Button)getActivity().findViewById(R.id.buttonActivateRoute);
        _buttonActivate.setVisibility(_addingNew ? View.INVISIBLE : View.VISIBLE);
        _buttonActivate.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Global.ActiveRoute = new ActiveRoute(getArguments().getString("name"));

                Global.saveActiveRouteToFile(getActivity());

                DisplayMetrics displayMetrics = new DisplayMetrics();
                getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                Global.MapScaleFactor = displayMetrics.widthPixels;
                Waypoint firstWaypoint = Global.ActiveRoute.Legs.get(0).StartPoint;
                Global.MapCentre.x = (float)firstWaypoint.Coords.Latitude;
                Global.MapCentre.y = (float)firstWaypoint.Coords.Longitude;

                if (editorFragmentHolder != null)
                    getFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.routeEditorFragmentHolder, new EmptyEditorFragment()).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, new RoutesFragment()).commit();

                Global.showToast(getActivity(), "Route activated");
                Global.hideKeyboard(getActivity());
            }
        });

        _buttonDelete = (Button)getActivity().findViewById(R.id.buttonDeleteRoute);
        _buttonDelete.setVisibility(_addingNew ? View.INVISIBLE : View.VISIBLE);
        _buttonDelete.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (Global.ActiveRoute != null && getArguments().getString("name").equals(Global.ActiveRoute.ParentRouteName))
                {
                    Global.showToast(getActivity(), "Route is currently active");
                    return;
                }

                Global.AllRoutes.remove(getArguments().getInt("index"));

                if (_routesListView != null)
                    ((ArrayAdapter)(_routesListView.getAdapter())).notifyDataSetChanged();

                Global.saveRoutesToFile(getActivity());

                if (editorFragmentHolder != null)
                    getFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.routeEditorFragmentHolder, new EmptyEditorFragment()).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, new RoutesFragment()).commit();

                Global.showToast(getActivity(), "Route deleted");
                Global.hideKeyboard(getActivity());
            }
        });

        _buttonSave = (Button)getActivity().findViewById(R.id.buttonSaveRoute);
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

                if (_routeWaypoints.size() < 2)
                {
                    Global.showToast(getActivity(), "Enter at least two waypoints");
                    return;
                }

                if (_addingNew)
                {
                    for (Route route : Global.AllRoutes)
                    {
                        if (editorName.equals(route.Name))
                        {
                            Global.showToast(getActivity(), "Route name already exists");
                            return;
                        }
                    }

                    Global.AllRoutes.add(new Route(editorName, _routeWaypoints));
                }
                else
                {
                    for (Route route : Global.AllRoutes)
                    {
                        if (editorName.equals(route.Name) && Global.AllRoutes.indexOf(route) != getArguments().getInt("index"))
                        {
                            Global.showToast(getActivity(), "Route name already exists");
                            return;
                        }
                    }

                    Global.AllRoutes.set(getArguments().getInt("index"), new Route(editorName, _routeWaypoints));

                    if (Global.ActiveRoute != null && Global.ActiveRoute.ParentRouteName.equals(getArguments().getString("name")))
                        Global.ActiveRoute = new ActiveRoute(editorName);

                    Global.saveActiveRouteToFile(getActivity());
                }

                Collections.sort(Global.AllRoutes, new RouteComparator());

                if (_routesListView != null)
                    ((ArrayAdapter)(_routesListView.getAdapter())).notifyDataSetChanged();

                Global.saveRoutesToFile(getActivity());

                if (editorFragmentHolder != null)
                    getFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.routeEditorFragmentHolder, new EmptyEditorFragment()).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, new RoutesFragment()).commit();

                Global.showToast(getActivity(), _addingNew ? "Route added" : "Route updated");
                Global.hideKeyboard(getActivity());
            }
        });
    }

    //endregion
}
