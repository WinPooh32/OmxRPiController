package tk.winpooh32.omxrpicontroller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_settings);
        //addPreferencesFromResource(R.xml.preferences);


        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        setTitle("Settings");
    }

    public static class SettingsFragment extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            final ControllerApp app = (ControllerApp) this.getActivity().getApplicationContext();

            final Preference ip = findPreference("ip");
            final Preference port = findPreference("port");
            final Preference path = findPreference("path");
            final Preference connections = findPreference("connections");
            final Preference uploads = findPreference("uploads");

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);

            ip.setSummary(app.serverIp);
            port.setSummary(app.serverPort);

            path.setSummary(prefs.getString("path",""));
            connections.setSummary(prefs.getString("connections",""));
            uploads.setSummary(prefs.getString("uploads",""));

            prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
                @Override
                public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                    if(key.equals("ip")){
                        app.serverIp = app.settings.getIp();
                        ip.setSummary(app.serverIp);
                    }
                    else if(key.equals("port")){
                        app.serverPort = app.settings.getPort();
                        port.setSummary(app.serverPort);
                    }
                    else if(key.equals("connections")){
                        connections.setSummary(sharedPreferences.getString("connections",""));
                    }
                    else if(key.equals("uploads")){
                        uploads.setSummary(sharedPreferences.getString("uploads",""));
                    }
                    else if(key.equals("path")){
                        path.setSummary(sharedPreferences.getString("path",""));
                    }
                }
            });
        }

    }

//    public static class SettingsFragment extends PreferenceFragment {
//
//        private void setPreferencesFromResource(int preferences, String rootKey) {
//            setPreferencesFromResource(R.xml.preferences, rootKey);
//        }
//
////        @Override
////        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
////
////            mListPreference = (ListPreference)  getPreferenceManager().findPreference("preference_key");
////            mListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
////                @Override
////                public boolean onPreferenceChange(Preference preference, Object newValue) {
////                    // your code here
////                }
////            }
////
////            return inflater.inflate(R.layout.fragment_settings, container, false);
////        }
//    }
}
