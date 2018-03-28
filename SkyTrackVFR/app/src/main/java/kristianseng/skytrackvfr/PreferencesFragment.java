package kristianseng.skytrackvfr;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import kristianseng.skytrackvfr.utility.ActiveRoute;
import kristianseng.skytrackvfr.utility.Global;

public class PreferencesFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener
{
    //region Overrides

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        addPreferencesFromResource(R.xml.preferences);
    }

    //endregion

    //region OnSharedPreferenceChangeListener implementation

    @Override
    public void onPause()
    {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key)
    {
        Global.loadSettingsFromFile(getActivity());

        if (key.equals("pref_curve_resolution"))
        {
            if (Global.ActiveRoute != null)
                Global.ActiveRoute = new ActiveRoute(Global.ActiveRoute.ParentRouteName);
        }
        else if (key.equals("pref_orientation_lock") || key.equals("pref_prevent_sleep"))
        {
            Activity parentActivity = getActivity();
            if (parentActivity instanceof MainActivity)
                ((MainActivity)parentActivity).applySettings();
        }
    }

    //endregion
}