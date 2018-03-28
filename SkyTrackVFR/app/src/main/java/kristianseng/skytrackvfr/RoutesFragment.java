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
import kristianseng.skytrackvfr.utility.Route;
import kristianseng.skytrackvfr.utility.RouteComparator;

public class RoutesFragment extends Fragment
{
    //region Fields

    private ListView _routesListView;
    private Button _addNewButton, _clearActiveButton;

    //endregion

    //region Overrides

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_routes, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        Collections.sort(Global.AllRoutes, new RouteComparator());

        final FrameLayout editorFragmentHolder = (FrameLayout)getActivity().findViewById(R.id.routeEditorFragmentHolder);

        _addNewButton = (Button)getActivity().findViewById(R.id.buttonAddRoute);
        _addNewButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                if (editorFragmentHolder != null)
                    getChildFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.routeEditorFragmentHolder, new EditRouteFragment()).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, new EditRouteFragment()).commit();

                Global.hideKeyboard(getActivity());
            }
        });

        _clearActiveButton = (Button)getActivity().findViewById(R.id.buttonClearActiveRoute);
        _clearActiveButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                Global.ActiveRoute = null;

                Global.saveActiveRouteToFile(getActivity());

                Global.showToast(getActivity(), "Active route cleared");
            }
        });

        _routesListView = (ListView)getActivity().findViewById(R.id.listViewRoutes);
        _routesListView.setEmptyView(getActivity().findViewById(R.id.emptyListRoutes));
        _routesListView.setAdapter(new ArrayAdapter <>(getActivity(), android.R.layout.simple_list_item_1, Global.AllRoutes));
        _routesListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Route selectedRoute;
                try
                {
                    selectedRoute = (Route)_routesListView.getItemAtPosition(position);
                }
                catch (Exception e)
                {
                    return;
                }

                Bundle selectedRouteBundle = new Bundle();
                selectedRouteBundle.putInt("index", position);
                selectedRouteBundle.putString("name", selectedRoute.Name);
                selectedRouteBundle.putStringArrayList("waypoints", selectedRoute.WaypointNames);

                Fragment editorFragment = new EditRouteFragment();
                editorFragment.setArguments(selectedRouteBundle);

                if (editorFragmentHolder != null)
                    getChildFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.routeEditorFragmentHolder, editorFragment).commit();
                else
                    getActivity().getSupportFragmentManager().beginTransaction().setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out).replace(R.id.mainFragmentHolder, editorFragment).commit();

                Global.hideKeyboard(getActivity());
            }
        });

        if (editorFragmentHolder != null)
            getChildFragmentManager().beginTransaction().replace(R.id.routeEditorFragmentHolder, new EmptyEditorFragment()).commit();
    }

    //endregion
}
