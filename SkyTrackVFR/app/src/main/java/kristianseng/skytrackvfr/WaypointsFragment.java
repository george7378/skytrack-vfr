package kristianseng.skytrackvfr;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;

import java.util.Collections;

import kristianseng.skytrackvfr.utility.Global;
import kristianseng.skytrackvfr.utility.Waypoint;
import kristianseng.skytrackvfr.utility.WaypointComparator;

public class WaypointsFragment extends Fragment
{
    //region Fields

    private ListView _waypointsListView;
    private Button _addNewButton;

    //endregion

    //region Overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_waypoints, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Collections.sort(Global.AllWaypoints, new WaypointComparator());

        final FrameLayout editorFragmentHolder = (FrameLayout)getActivity().findViewById(R.id.waypointEditorFragmentHolder);

        _addNewButton = (Button)getActivity().findViewById(R.id.buttonAddWaypoint);
        _addNewButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (editorFragmentHolder != null)
                    getChildFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.waypointEditorFragmentHolder, new EditWaypointFragment()).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, new EditWaypointFragment()).commit();

                Global.hideKeyboard(getActivity());
            }
        });

        _waypointsListView = (ListView)getActivity().findViewById(R.id.listViewWaypoints);
        _waypointsListView.setEmptyView(getActivity().findViewById(R.id.emptyListWaypoints));
        _waypointsListView.setAdapter(new ArrayAdapter <>(getActivity(), android.R.layout.simple_list_item_1, Global.AllWaypoints));
        _waypointsListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Waypoint selectedWaypoint;
                try
                {
                    selectedWaypoint = (Waypoint)_waypointsListView.getItemAtPosition(position);
                }
                catch (Exception e)
                {
                    return;
                }

                Bundle selectedWaypointBundle = new Bundle();
                selectedWaypointBundle.putInt("index", position);
                selectedWaypointBundle.putString("name", selectedWaypoint.Name);
                selectedWaypointBundle.putDouble("lat", selectedWaypoint.Coords.Latitude);
                selectedWaypointBundle.putDouble("lon", selectedWaypoint.Coords.Longitude);

                Fragment editorFragment = new EditWaypointFragment();
                editorFragment.setArguments(selectedWaypointBundle);

                if (editorFragmentHolder != null)
                    getChildFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.waypointEditorFragmentHolder, editorFragment).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, editorFragment).commit();

                Global.hideKeyboard(getActivity());
            }
        });

        if (editorFragmentHolder != null)
            getChildFragmentManager().beginTransaction().replace(R.id.waypointEditorFragmentHolder, new EmptyEditorFragment()).commit();
    }

    //endregion
}
