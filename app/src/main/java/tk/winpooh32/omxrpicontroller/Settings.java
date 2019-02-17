package tk.winpooh32.omxrpicontroller;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Settings {
    private final static String KEY_IP = "ip";
    private final static String KEY_PORT = "port";

    private static Settings ourInstance = new Settings();
    private static SharedPreferences _sharedPref;
    private static ControllerApp _app;
    private static SharedPreferences.Editor _editor;

    public static Settings getInstance() {
        return ourInstance;
    }

    private Settings() {
    }

    public void init(ControllerApp app){
        _app = app;
        _sharedPref = PreferenceManager.getDefaultSharedPreferences(_app);
        _editor = _sharedPref.edit();
    }

    public void setIp(String ip){
        _editor.putString(KEY_IP, ip);
        _editor.commit();
    }

    public void setPort(String port){
        _editor.putString(KEY_PORT, port);
        _editor.commit();
    }

    public String getIp(){
        return _sharedPref.getString(KEY_IP, "192.168.1.2");
    }

    public String getPort(){
        return _sharedPref.getString(KEY_PORT, "8181");
    }
}
